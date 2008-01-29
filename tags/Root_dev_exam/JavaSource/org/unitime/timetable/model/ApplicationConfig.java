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
package org.unitime.timetable.model;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseApplicationConfig;
import org.unitime.timetable.model.dao.ApplicationConfigDAO;
import org.unitime.timetable.model.dao._RootDAO;




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
	    ApplicationConfig config = null;
		org.hibernate.Session hibSession = null;
	    
        try {
            ApplicationConfigDAO sDao = new ApplicationConfigDAO();
			hibSession = sDao.getSession();
            
			List configList = hibSession.createCriteria(ApplicationConfig.class)
			.add(Restrictions.eq("key", key))
			.setCacheable(true)
			.list();
			
			if(configList.size()!=0) 
			    config = (ApplicationConfig) configList.get(0);
			    
	    }
	    catch (Exception e) {
			Debug.error(e);
			config = null;
	    }
	    finally {
	    }
	    
	    return config;
	}
	
	/**
	 * Get the config object for a given key
	 * @param key Configuration key
	 * @return Value if found, null otherwise
	 */
	public static String getConfigValue(String key, String defaultValue) {
	    //return defaultValue if hibernate is not yet initialized
        if (_RootDAO.getConfiguration()==null) return defaultValue;
        
	    ApplicationConfig config = getConfig(key);
	    if (config==null || config.getValue()==null || config.getValue().trim().length()==0)
	        return defaultValue;
	    
	    return config.getValue();
	}
    
    public static Properties toProperties() {
        Properties properties = new Properties();
        if (_RootDAO.getConfiguration()==null) return properties;
        for (Iterator i=new ApplicationConfigDAO().findAll().iterator();i.hasNext();) {
            ApplicationConfig appcfg = (ApplicationConfig)i.next();
             properties.setProperty(appcfg.getKey(), appcfg.getValue()==null?"":appcfg.getValue());
        }
        return properties;
    }

}