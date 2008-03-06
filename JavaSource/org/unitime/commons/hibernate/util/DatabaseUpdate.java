/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.commons.hibernate.util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * Process resource given by application property tmtbl.db.update (defaults to file dbupdate.xml) 
 * and the update database accordingly.
 * 
 * @author Tomas Muller
 *
 */
public class DatabaseUpdate {
    protected static Log sLog = LogFactory.getLog(DatabaseUpdate.class);
    private Element iRoot = null;
    private String iDialectSQL = null;
    
    private DatabaseUpdate(Document document) throws Exception {
        if (!"dbupdate".equals(document.getRootElement().getName())) throw new Exception("Unknown format.");
        iRoot = document.getRootElement();
    }
    
    public int getVersion() {
        return Integer.parseInt(ApplicationConfig.getConfigValue("tmtbl.db.version", "0"));
    }
    
    public void performUpdate() {
        sLog.info("Current database version: "+getVersion());
        String dialect = _RootDAO.getConfiguration().getProperty("dialect");
        for (Iterator i=iRoot.elementIterator("dialect");i.hasNext();) {
            Element dialectElement = (Element)i.next();
            if (dialect.equals(dialectElement.getTextTrim())) iDialectSQL = dialectElement.attributeValue("type");
        }
        for (Iterator i=iRoot.elementIterator("update");i.hasNext();) {
            Element updateElement = (Element)i.next();
            int updateVersion = Integer.parseInt(updateElement.attributeValue("version"));
            if (updateVersion>getVersion() && !performUpdate(updateElement)) break;
        }
        sLog.info("New database version: "+getVersion());
    }
    
    public boolean performUpdate(Element updateElement) {
        int version = Integer.parseInt(updateElement.attributeValue("version"));
        Session hibSession = new _RootDAO().getSession();
        String schema = _RootDAO.getConfiguration().getProperty("default_schema");
        Transaction tx = null;
        Hashtable variables = new Hashtable();
        try {
            tx = hibSession.beginTransaction();
            sLog.info("  Performing update to version "+version+" ("+updateElement.attributeValue("comment")+")");
            for (Iterator i=updateElement.elementIterator();i.hasNext();) {
                Element queryElement = (Element)i.next();
                String type = queryElement.getName();
                String query = queryElement.getText().trim().replaceAll("%SCHEMA%", schema);
                for (Iterator j=variables.entrySet().iterator();j.hasNext();) {
                    Map.Entry entry = (Map.Entry)j.next();
                    query = query.replaceAll("%"+entry.getKey()+"%", entry.getValue().toString());
                }
                String condition = queryElement.attributeValue("condition","none");
                String action = queryElement.attributeValue("action","next");
                String value = queryElement.attributeValue("value");
                String into = queryElement.attributeValue("into");
                if (queryElement.attribute("onFail")!=null) {
                    condition="fail";
                    action=queryElement.attributeValue("onFail");
                }
                if (queryElement.attribute("onEqual")!=null) {
                    condition="equal";
                    action=queryElement.attributeValue("onEqual");
                }
                if (queryElement.attribute("onNotEqual")!=null) {
                    condition="notEqual";
                    action=queryElement.attributeValue("onNotEqual");
                }
                if (query.length()==0) continue;
                try {
                    if (type.equals("hql") || type.equals("sql") || type.equals(iDialectSQL)) {
                        sLog.debug("  -- HQL: "+query+" (con:"+condition+", act:"+action+", val:"+value+")");
                        Query q = (type.equals("hql")?hibSession.createQuery(query):hibSession.createSQLQuery(query));
                        boolean ok = true;
                        if (into!=null) {
                            variables.put(into, q.uniqueResult().toString());
                        } else if ("equal".equals(condition) && value!=null) {
                            ok = value.equals(q.uniqueResult().toString());
                        } else if("notEqual".equals(condition) && value!=null) {
                            ok = !value.equals(q.uniqueResult().toString());
                        } else {
                            int x = q.executeUpdate();
                            sLog.debug("  -- "+x+" lines affected.");
                            if ("noChange".equals(condition)) ok = (x==0);
                            else if ("change".equals(condition)) ok = (x>0);
                        }
                        if (ok) {
                            if ("next".equals(action)) continue;
                            if ("done".equals(action)) break;
                            if ("fail".equals(action)) {
                                sLog.error("Update to version "+version+" failed (condition not met for query '"+query+"', con:"+condition+", act:"+action+", val:"+value+").");
                                tx.rollback();
                                return false;
                            }
                        }
                    } else {
                        sLog.debug("  -- skip: "+query+" (con:"+condition+", act:"+action+", val:"+value+")");
                    }
                } catch (Exception e) {
                    sLog.warn("Query '"+query+"' failed, "+e.getMessage());
                    if (e.getCause()!=null && e.getCause().getMessage()!=null)
                        sLog.warn("Cause: "+e.getCause().getMessage());
                    if ("fail".equals(condition)) {
                        if ("next".equals(action)) continue;
                        if ("done".equals(action)) break;
                    }
                    sLog.error("Update to version "+version+" failed.");
                    tx.rollback();
                    return false;
                }
            }

            ApplicationConfig versionCfg = ApplicationConfig.getConfig("tmtbl.db.version");
            if (versionCfg==null) {
                versionCfg = new ApplicationConfig("tmtbl.db.version");
                versionCfg.setDescription("Timetabling database version (please do not change -- this key is used by automatic database update)");
            }
            versionCfg.setValue(String.valueOf(version));
            hibSession.saveOrUpdate(versionCfg);
            sLog.info("    Database version increased to: "+version);
            
            if (tx!=null && tx.isActive()) tx.commit();
            HibernateUtil.clearCache();
            return true;
        } catch (Exception e) {
            if (tx!=null && tx.isActive()) tx.rollback();
            sLog.error("Update to version "+version+" failed, reason:"+e.getMessage(),e);
            return false;
        }
    }
    
    public static void update() {
        try {
            Document document = null;
            String dbUpdateFile = ApplicationProperties.getProperty("tmtbl.db.update","dbupdate.xml");
            URL dbUpdateFileUrl = ApplicationProperties.class.getClassLoader().getResource(dbUpdateFile);
            if (dbUpdateFileUrl!=null) {
                Debug.info("Reading " + URLDecoder.decode(dbUpdateFileUrl.getPath(), "UTF-8") + " ...");
                document = (new SAXReader()).read(dbUpdateFileUrl.openStream());
            } else if (new File(dbUpdateFile).exists()) {
                Debug.info("Reading " + dbUpdateFile + " ...");
                document = (new SAXReader()).read(new File(dbUpdateFile));
            }
            if (document==null) {
                sLog.error("Unable to execute database auto-update, reason: resource "+dbUpdateFile+" not found.");
                return;
            }
            new DatabaseUpdate(document).performUpdate();
        } catch (Exception e) {
            sLog.error("Unable to execute database auto-update, reason: "+e.getMessage(), e);
        }
    }
}
