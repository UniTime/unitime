/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.commons.hibernate.util;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.naming.spi.NamingManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.ConfigHelper;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.IntegerType;
import org.unitime.commons.LocalContext;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base._BaseRootDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

/**
 * @author Tomas Muller
 */
public class HibernateUtil {
    private static Log sLog = LogFactory.getLog(HibernateUtil.class);
    private static SessionFactory sSessionFactory = null;

	private static void setProperty(org.w3c.dom.Document document, String name, String value) {
        if (value==null) {
            removeProperty(document, name);
        } else {
            org.w3c.dom.Element hibConfiguration = (org.w3c.dom.Element)document.getElementsByTagName("hibernate-configuration").item(0);
            org.w3c.dom.Element sessionFactoryConfig = (org.w3c.dom.Element)hibConfiguration.getElementsByTagName("session-factory").item(0);
            NodeList properties = sessionFactoryConfig.getElementsByTagName("property");
            for (int i=0;i<properties.getLength();i++) {
                org.w3c.dom.Element property = (org.w3c.dom.Element)properties.item(i);
                if (name.equals(property.getAttribute("name"))) {
                    Text text = (Text)property.getFirstChild();
                    if (text==null) {
                        property.appendChild(document.createTextNode(value));
                    } else {
                        text.setData(value);
                    }
                    return;
                }
            }
            org.w3c.dom.Element property = document.createElement("property");
            property.setAttribute("name",name);
            property.appendChild(document.createTextNode(value));
            sessionFactoryConfig.appendChild(property);
        }
	}
	
	private static void removeProperty(org.w3c.dom.Document document, String name) {
		org.w3c.dom.Element hibConfiguration = (org.w3c.dom.Element)document.getElementsByTagName("hibernate-configuration").item(0);
		org.w3c.dom.Element sessionFactoryConfig = (org.w3c.dom.Element)hibConfiguration.getElementsByTagName("session-factory").item(0);
		org.w3c.dom.NodeList properties = sessionFactoryConfig.getElementsByTagName("property");
        for (int i=0;i<properties.getLength();i++) {
        	org.w3c.dom.Element property = (org.w3c.dom.Element)properties.item(i);
        	if (name.equals(property.getAttribute("name"))) {
        		sessionFactoryConfig.removeChild(property);
        		return;
        	}
        }
	}
    
    public static void configureHibernate(String connectionUrl) throws Exception {
        Properties properties = ApplicationProperties.getProperties();
        properties.setProperty("connection.url", connectionUrl);
        configureHibernate(properties);
    }
    
	public static String getProperty(Properties properties, String name) {
        String value = properties.getProperty(name);
        if (value!=null) {
            sLog.debug("   -- " + name + "=" + value);
        	return value;
        }
        sLog.debug("   -- using application properties for " + name);
        value = ApplicationProperties.getProperty(name);
        sLog.debug("     -- " + name + "=" + value);
        return value;
    }
    
    public static void fixSchemaInFormulas(Configuration cfg) {
    	cfg.buildMappings();
        String schema = cfg.getProperty("default_schema"); 
        if (schema!=null) {
            for (Iterator i=cfg.getClassMappings();i.hasNext();) {
                PersistentClass pc = (PersistentClass)i.next();
                for (Iterator j=pc.getPropertyIterator();j.hasNext();) {
                    Property p = (Property)j.next();
                    for (Iterator k=p.getColumnIterator();k.hasNext();) {
                        Selectable c = (Selectable)k.next();
                        if (c instanceof Formula) {
                            Formula f = (Formula)c;
                            if (f.getFormula()!=null && f.getFormula().indexOf("%SCHEMA%")>=0) {
                                f.setFormula(f.getFormula().replaceAll("%SCHEMA%", schema));
                                sLog.debug("Schema updated in "+pc.getClassName()+"."+p.getName()+" to "+f.getFormula());
                            }
                        }
                    }
                }
            }
        }
    }

	public static void configureHibernate(Properties properties) throws Exception {
		if (sSessionFactory!=null) {
			sSessionFactory.close();
			sSessionFactory=null;
		}
		
		if (!NamingManager.hasInitialContextFactoryBuilder())
			NamingManager.setInitialContextFactoryBuilder(new LocalContext(null));
		
		sLog.info("Connecting to "+getProperty(properties,"connection.url"));
		ClassLoader classLoader = HibernateUtil.class.getClassLoader();
		sLog.debug("  -- class loader retrieved");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		sLog.debug("  -- document factory created");
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {
    	    public InputSource resolveEntity(String publicId, String systemId) {
    	        if (publicId.equals("-//Hibernate/Hibernate Mapping DTD 3.0//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-mapping-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Mapping DTD//EN")) {
        	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-mapping-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD 3.0//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-configuration-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-configuration-3.0.dtd"));
        	    }
    	        return null;
    	    }
    	});
        sLog.debug("  -- document builder created");
        Document document = builder.parse(classLoader.getResource("hibernate.cfg.xml").openStream());
        sLog.debug("  -- hibernate.cfg.xml parsed");
        
        String dialect = getProperty(properties, "dialect");
        if (dialect!=null)
        	setProperty(document, "dialect", dialect);

        String idgen = getProperty(properties, "tmtbl.uniqueid.generator");
        if (idgen!=null)
            setProperty(document, "tmtbl.uniqueid.generator", idgen);

        if (ApplicationProperty.HibernateClusterEnabled.isFalse())
        	setProperty(document, "net.sf.ehcache.configurationResourceName", "ehcache-nocluster.xml");

        // Remove second level cache
        setProperty(document, "hibernate.cache.use_second_level_cache", "false");
        setProperty(document, "hibernate.cache.use_query_cache", "false");
        removeProperty(document, "hibernate.cache.region.factory_class");

        for (Enumeration e=properties.propertyNames();e.hasMoreElements();) {
            String name = (String)e.nextElement();
            if (name.startsWith("hibernate.") || name.startsWith("connection.") || name.startsWith("tmtbl.hibernate.")) {
				String value = properties.getProperty(name);
                if ("NULL".equals(value))
                    removeProperty(document, name);
                else
                    setProperty(document, name, value);
                if (!name.equals("connection.password"))
                    sLog.debug("  -- set "+name+": "+value);
                else
                    sLog.debug("  -- set "+name+": *****");
            }
        }

        String default_schema = getProperty(properties, "default_schema");
        if (default_schema!=null)
            setProperty(document, "default_schema", default_schema);

        sLog.debug("  -- hibernate.cfg.xml altered");
        
        Configuration cfg = new Configuration();
        sLog.debug("  -- configuration object created");
        
    	cfg.setEntityResolver(new EntityResolver() {
    	    public InputSource resolveEntity(String publicId, String systemId) {
    	        if (publicId.equals("-//Hibernate/Hibernate Mapping DTD 3.0//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-mapping-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Mapping DTD//EN")) {
        	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-mapping-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD 3.0//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-configuration-3.0.dtd"));
    	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD//EN")) {
    	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-configuration-3.0.dtd"));
        	    }
    	        return null;
    	    }
    	});
        sLog.debug("  -- added entity resolver");
        
        cfg.configure(document);
        sLog.debug("  -- hibernate configured");

        fixSchemaInFormulas(cfg);
        
        UniqueIdGenerator.configure(cfg);
        
        (new _BaseRootDAO() {
    		void setConf(Configuration cfg) {
    			_BaseRootDAO.sConfiguration = cfg;
    		}
    		protected Class getReferenceClass() { return null; }
    	}).setConf(cfg);
        sLog.debug("  -- configuration set to _BaseRootDAO");

        sSessionFactory = cfg.buildSessionFactory();
        sLog.debug("  -- session factory created");
        
        (new _BaseRootDAO() {
    		void setSF(SessionFactory fact) {
    			_BaseRootDAO.sSessionFactory = fact;
    		}
    		protected Class getReferenceClass() { return null; }
    	}).setSF(sSessionFactory);
        sLog.debug("  -- session factory set to _BaseRootDAO");
        
        addBitwiseOperationsToDialect();
        sLog.debug("  -- bitwise operation added to the dialect if needed");
        
        DatabaseUpdate.update();
    }
    
    public static void closeHibernate() {
		if (sSessionFactory!=null) {
			sSessionFactory.close();
			sSessionFactory=null;
		}
	}
    
    public static void configureHibernateFromRootDAO(String cfgName, Configuration cfg) {
        try {
        	EntityResolver entityResolver = new EntityResolver() {
        	    public InputSource resolveEntity(String publicId, String systemId) {
        	        if (publicId.equals("-//Hibernate/Hibernate Mapping DTD 3.0//EN")) {
        	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-mapping-3.0.dtd"));
        	        } else if (publicId.equals("-//Hibernate/Hibernate Mapping DTD//EN")) {
            	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-mapping-3.0.dtd"));
        	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD 3.0//EN")) {
        	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-configuration-3.0.dtd"));
        	        } else if (publicId.equals("-//Hibernate/Hibernate Configuration DTD//EN")) {
        	            return new InputSource(HibernateUtil.class.getClassLoader().getResourceAsStream("org/hibernate/hibernate-configuration-3.0.dtd"));
            	    }
        	        return null;
        	    }
        	};
        	
        	cfg.setEntityResolver(entityResolver);
            sLog.debug("  -- added entity resolver");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            sLog.debug("  -- document factory created");
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(entityResolver);
            sLog.debug("  -- document builder created");
            Document document = builder.parse(ConfigHelper.getConfigStream(cfgName==null?"hibernate.cfg.xml":cfgName));
            
            String dialect = ApplicationProperty.DatabaseDialect.value();
            if (dialect!=null) setProperty(document, "dialect", dialect);
            
            String default_schema = ApplicationProperty.DatabaseSchema.value();
            if (default_schema!=null) setProperty(document, "default_schema", default_schema);
            
            String idgen = ApplicationProperty.DatabaseUniqueIdGenerator.value();
            if (idgen!=null) setProperty(document, "tmtbl.uniqueid.generator", idgen);
            
            if (ApplicationProperty.HibernateClusterEnabled.isFalse())
            	setProperty(document, "net.sf.ehcache.configurationResourceName", "ehcache-nocluster.xml");
            
            for (Enumeration e=ApplicationProperties.getProperties().propertyNames();e.hasMoreElements();) {
                String name = (String)e.nextElement();
                if (name.startsWith("hibernate.") || name.startsWith("connection.") || name.startsWith("tmtbl.hibernate.")) {
					String value = ApplicationProperties.getProperty(name);
                    if ("NULL".equals(value))
                        removeProperty(document, name);
                    else
                        setProperty(document, name, value);
                    if (!name.equals("connection.password"))
                        sLog.debug("  -- set "+name+": "+value);
                    else
                        sLog.debug("  -- set "+name+": *****");
                }
            }

            cfg.configure(document);
            sLog.debug("  -- hibernate configured");
            
            HibernateUtil.fixSchemaInFormulas(cfg);
            sLog.debug("  -- %SCHEMA% in formulas changed to "+cfg.getProperty("default_schema"));
            
            UniqueIdGenerator.configure(cfg);
            sLog.debug("  -- UniquId generator configured");
        } catch (Exception e) {
            sLog.error("Unable to configure hibernate, reason: "+e.getMessage(),e);
        }
    }
    
    private static String sConnectionUrl = null;
    
    public static String getConnectionUrl() {
        if (sConnectionUrl==null) {
            try {
                SessionImplementor session = (SessionImplementor)new _RootDAO().getSession();
                Connection connection = session.getJdbcConnectionAccess().obtainConnection();
                sConnectionUrl = connection.getMetaData().getURL();
                session.getJdbcConnectionAccess().releaseConnection(connection);
            } catch (Exception e) {
                sLog.error("Unable to get connection string, reason: "+e.getMessage(),e);
            }
        }
        return sConnectionUrl;
    }

    public static String getDatabaseName() {
        String schema = _RootDAO.getConfiguration().getProperty("default_schema");
        String url = getConnectionUrl();
        if (url==null) return "N/A";
        if (url.startsWith("jdbc:oracle:")) {
            return schema+"@"+url.substring(1+url.lastIndexOf(':'));
        }
        return schema;
    }
    
    public static void clearCache() {
        clearCache(null, true);
    }
    
    public static void clearCache(Class persistentClass) {
        clearCache(persistentClass, false);
    }

    public static void clearCache(Class persistentClass, boolean evictQueries) {
        _RootDAO dao = new _RootDAO();
        org.hibernate.Session hibSession = dao.getSession(); 
        SessionFactory hibSessionFactory = hibSession.getSessionFactory();
        if (persistentClass==null) {
            for (Iterator i=hibSessionFactory.getAllClassMetadata().entrySet().iterator();i.hasNext();) {
                Map.Entry entry = (Map.Entry)i.next();
                String className = (String)entry.getKey();
                ClassMetadata classMetadata = (ClassMetadata)entry.getValue();
                try {
                    hibSessionFactory.getCache().evictEntityRegion(Class.forName(className));
                    for (int j=0;j<classMetadata.getPropertyNames().length;j++) {
                        if (classMetadata.getPropertyTypes()[j].isCollectionType()) {
                            try {
                                hibSessionFactory.getCache().evictCollectionRegion(className+"."+classMetadata.getPropertyNames()[j]);
                            } catch (MappingException e) {}
                        }
                    }
                } catch (ClassNotFoundException e) {}
            }
            hibSessionFactory.getCache().evictEntityRegions();
            hibSessionFactory.getCache().evictCollectionRegions();
        } else {
            ClassMetadata classMetadata = hibSessionFactory.getClassMetadata(persistentClass);
            hibSessionFactory.getCache().evictEntityRegion(persistentClass);
            if (classMetadata!=null) {
                for (int j=0;j<classMetadata.getPropertyNames().length;j++) {
                    if (classMetadata.getPropertyTypes()[j].isCollectionType()) {
                        try {
                            hibSessionFactory.getCache().evictCollectionRegion(persistentClass.getClass().getName()+"."+classMetadata.getPropertyNames()[j]);
                        } catch (MappingException e) {}
                    }
                }
            }
        }
        if (evictQueries) {
            hibSessionFactory.getCache().evictQueryRegions();
            hibSessionFactory.getCache().evictDefaultQueryRegion();
        }
    }
    
    public static Class<?> getDialect() {
    	try {
    		return Class.forName(_RootDAO.getConfiguration().getProperty("dialect"));
    	} catch (ClassNotFoundException e) {
    		return null;
    	}
    }
    
    public static boolean isMySQL() {
    	return MySQLDialect.class.isAssignableFrom(getDialect());
    }
    
    public static boolean isOracle() {
    	return Oracle8iDialect.class.isAssignableFrom(getDialect());
    }
    
    public static String addDate(String dateSQL, String incrementSQL) {
        if (isMySQL())
            return "adddate("+dateSQL+","+incrementSQL+")";
        else
            return dateSQL+(incrementSQL.startsWith("+")||incrementSQL.startsWith("-")?"":"+")+incrementSQL;
    }
    
    public static String dayOfWeek(String field) {
    	if (isOracle())
    		return "to_char(" + field + ",'D')";
    	else
    		return "dayofweek(" + field + ")";
    }
    
    public static String date(Date date) {
    	if (isOracle())
    		return "to_date('" + new SimpleDateFormat("yyyy-MM-dd").format(date) + "', 'YYYY-MM-DD')";
    	else
    		return "str_to_date('" + new SimpleDateFormat("yyyy-MM-dd").format(date) + "', '%Y-%m-%d')";
    }
    
    public static void addBitwiseOperationsToDialect() {
    	SessionFactoryImplementor hibSessionFactory = (SessionFactoryImplementor)new _RootDAO().getSession().getSessionFactory();
    	Dialect dialect = hibSessionFactory.getDialect();
    	if (!dialect.getFunctions().containsKey("bit_and")) {
    		if (isOracle())
    			dialect.getFunctions().put("bit_and", new StandardSQLFunction("bitand", IntegerType.INSTANCE));  
    		else
    			dialect.getFunctions().put("bit_and", new SQLFunctionTemplate(IntegerType.INSTANCE, "?1 & ?2"));
    	}
    }
}
