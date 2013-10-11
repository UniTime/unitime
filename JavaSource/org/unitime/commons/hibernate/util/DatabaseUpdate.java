/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008-2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.commons.hibernate.util;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
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
public abstract class DatabaseUpdate {
    protected static Log sLog = LogFactory.getLog(DatabaseUpdate.class);
    private Element iRoot = null;
    private String iDialectSQL = null;
    
    protected abstract String findDbUpdateFileName();
    protected abstract String versionParameterName();
    protected abstract String updateName();
    
    protected DatabaseUpdate(Document document) throws Exception {
        if (!"dbupdate".equals(document.getRootElement().getName())) throw new Exception("Unknown format.");
        iRoot = document.getRootElement();
    }
    
    protected DatabaseUpdate() throws Exception {
        Document document = null;
        String dbUpdateFile = findDbUpdateFileName();
        URL dbUpdateFileUrl = ApplicationProperties.class.getClassLoader().getResource(dbUpdateFile);
        if (dbUpdateFileUrl!=null) {
            Debug.info("Reading " + URLDecoder.decode(dbUpdateFileUrl.getPath(), "UTF-8") + " ...");
            document = (new SAXReader()).read(dbUpdateFileUrl.openStream());
        } else if (new File(dbUpdateFile).exists()) {
            Debug.info("Reading " + dbUpdateFile + " ...");
            document = (new SAXReader()).read(new File(dbUpdateFile));
        }
        if (document==null) {
            sLog.error("Unable to execute " + updateName() + " database auto-update, reason: resource "+dbUpdateFile+" not found.");
            return;
        }

        if (!"dbupdate".equals(document.getRootElement().getName())) throw new Exception("Unknown format.");
        iRoot = document.getRootElement();
    }
    
    
    public int getVersion() {
        return Integer.parseInt(ApplicationConfig.getConfigValue(versionParameterName(), "0"));
    }
    
    public void performUpdate() {
        sLog.info("Current " + updateName() + " database version: "+getVersion());
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
        sLog.info("New " + updateName() + " database version: "+getVersion());
    }
    
    public boolean performUpdate(Element updateElement) {
        int version = Integer.parseInt(updateElement.attributeValue("version"));
        Session hibSession = new _RootDAO().getSession();
        String schema = _RootDAO.getConfiguration().getProperty("default_schema");
        Transaction tx = null;
        Hashtable variables = new Hashtable();
        try {
            tx = hibSession.beginTransaction();
            sLog.info("  Performing " + updateName() + " update to version "+version+" ("+updateElement.attributeValue("comment")+")");
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
                        Query q = null;
                        try {
                        	q = (type.equals("hql")?hibSession.createQuery(query):hibSession.createSQLQuery(query));
                        } catch (QueryException e) {
                        	// Work-around Hibernate issue HHH-2697 (https://hibernate.onjira.com/browse/HHH-2697)
                        	if (!"hql".equals(type)) {
                        		final String sql = query;
                        		hibSession.doWork(new Work() {
									@Override
									public void execute(Connection connection) throws SQLException {
		                                Statement statement = connection.createStatement();
		                                int lines = statement.executeUpdate(sql);
		                                sLog.debug("  -- "+lines+" lines affected.");
		                                statement.close();
									}
								});
                        	} else throw e;
                        }
                        boolean ok = true;
                        if (into!=null) {
                            variables.put(into, q.uniqueResult().toString());
                        } else if ("equal".equals(condition) && value!=null) {
                            ok = value.equals(q.uniqueResult().toString());
                        } else if("notEqual".equals(condition) && value!=null) {
                            ok = !value.equals(q.uniqueResult().toString());
                        } else if (q != null) {
                            int lines = q.executeUpdate();
                            sLog.debug("  -- "+lines+" lines affected.");
                            if ("noChange".equals(condition)) ok = (lines==0);
                            else if ("change".equals(condition)) ok = (lines>0);
                        }
                        if (ok) {
                            if ("next".equals(action)) continue;
                            if ("done".equals(action)) break;
                            if ("fail".equals(action)) {
                                sLog.error("Update to " + updateName() + " version "+version+" failed (condition not met for query '"+query+"', con:"+condition+", act:"+action+", val:"+value+").");
                                tx.rollback();
                                return false;
                            }
                        }
                    } else {
                        sLog.debug("  -- skip: "+query+" (con:"+condition+", act:"+action+", val:"+value+")");
                    }
                } catch (Exception e) {
                    sLog.warn("Query '"+query+"' failed, "+e.getMessage(), e);
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

            ApplicationConfig versionCfg = ApplicationConfig.getConfig(versionParameterName());
            if (versionCfg==null) {
                versionCfg = new ApplicationConfig(versionParameterName());
                versionCfg.setDescription("Timetabling " + updateName() + " DB version (do not change -- this is used by automatic database update)");
            }
            versionCfg.setValue(String.valueOf(version));
            hibSession.saveOrUpdate(versionCfg);
            sLog.info("    " + updateName() + " Database version increased to: "+version);
            
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
            new UniTimeCoreDatabaseUpdate().performUpdate();
            
            String additionalUpdates = ApplicationProperties.getProperty("tmtbl.db.addon.update.class");
            if (additionalUpdates != null && !additionalUpdates.trim().isEmpty()){
            	DatabaseUpdate du = (DatabaseUpdate) (Class.forName(additionalUpdates).newInstance());;
            	du.performUpdate();
            }
        } catch (Exception e) {
            sLog.error("Unable to execute database auto-update, reason: "+e.getMessage(), e);
        } finally {
        	_RootDAO.closeCurrentThreadSessions();
        }
    }
}
