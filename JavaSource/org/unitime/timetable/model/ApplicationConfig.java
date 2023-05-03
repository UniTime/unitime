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
package org.unitime.timetable.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Iterator;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.base.BaseApplicationConfig;
import org.unitime.timetable.model.dao.ApplicationConfigDAO;


/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "application_config")
public class ApplicationConfig extends BaseApplicationConfig {
	private static final long serialVersionUID = 1L;
	public static final String APP_CFG_ATTR_NAME = "appConfig";

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ApplicationConfig () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ApplicationConfig (java.lang.String key) {
		super(key);
	}

/*[CONSTRUCTOR MARKER END]*/

    /**
	 * Get the config object for a given key
	 * @param key Configuration key
	 * @return Object if found, null otherwise
	 */
	public static ApplicationConfig getConfig(String key) {
		return ApplicationConfigDAO.getInstance().getSession()
				.createQuery("from ApplicationConfig where key = :key", ApplicationConfig.class)
				.setParameter("key", key)
				.setCacheable(true)
                .uniqueResult();
	}
	
	/**
	 * Get the config object for a given key
	 * @param key Configuration key
	 * @return Value if found, null otherwise
	 */
	public static String getConfigValue(String key, String defaultValue) {
	    //return defaultValue if hibernate is not yet initialized
        if (!HibernateUtil.isConfigured()) return defaultValue;
        
        String value = ApplicationConfigDAO.getInstance().
            getSession().
            createQuery("select c.value from ApplicationConfig c where c.key=:key", String.class).
            setParameter("key", key).setCacheable(true).uniqueResult();
        
        return (value==null?defaultValue:value);
	}
    
    public static Properties toProperties() {
        Properties properties = new Properties();
        if (!HibernateUtil.isConfigured()) return properties;
        
        org.hibernate.Session hibSession = ApplicationConfigDAO.getInstance().createNewSession();
        try {
            for (Iterator i=ApplicationConfigDAO.getInstance().findAll(hibSession).iterator();i.hasNext();) {
                ApplicationConfig appcfg = (ApplicationConfig)i.next();
                 properties.setProperty(appcfg.getKey(), appcfg.getValue()==null?"":appcfg.getValue());
            }
        } finally {
        	hibSession.close();
        }
        return properties;
    }
    
    public static boolean configureLogging() {
    	if (!HibernateUtil.isConfigured()) return false;
    	
        org.hibernate.Session hibSession = ApplicationConfigDAO.getInstance().createNewSession();
        try {
        	for (ApplicationConfig config: hibSession.createQuery("from ApplicationConfig where key like 'log4j.logger.%'", ApplicationConfig.class).list()) {
        		Level level = Level.getLevel(config.getValue());
        		boolean root = "log4j.logger.root".equals(config.getKey());
        		if (root)
        			Configurator.setRootLevel(level);
        		else
        			Configurator.setLevel(config.getKey().substring("log4j.logger.".length()), level);
        		Debug.info("Logging level for " + (root ? "root" : config.getKey().substring("log4j.logger.".length())) + " set to " + level);
        	}
        } finally {
        	hibSession.close();
        }
        
        return true;
    }

}
