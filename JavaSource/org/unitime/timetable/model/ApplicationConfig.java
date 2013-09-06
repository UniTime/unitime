/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.Iterator;
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
        try {
            return (ApplicationConfig)new ApplicationConfigDAO()
                .getSession()
                .createCriteria(ApplicationConfig.class)
                .add(Restrictions.eq("key", key))
                .setCacheable(true)
                .uniqueResult();
	    } catch (Exception e) {
			Debug.error(e);
			return null;
	    }
	}
	
	/**
	 * Get the config object for a given key
	 * @param key Configuration key
	 * @return Value if found, null otherwise
	 */
	public static String getConfigValue(String key, String defaultValue) {
	    //return defaultValue if hibernate is not yet initialized
        if (!_RootDAO.isConfigured()) return defaultValue;
        
        String value = (String)new ApplicationConfigDAO().
            getSession().
            createQuery("select c.value from ApplicationConfig c where c.key=:key").
            setString("key", key).setCacheable(true).uniqueResult();
        
        return (value==null?defaultValue:value);
	}
    
    public static Properties toProperties() {
        Properties properties = new Properties();
        if (!_RootDAO.isConfigured()) return properties;
        for (Iterator i=new ApplicationConfigDAO().findAll().iterator();i.hasNext();) {
            ApplicationConfig appcfg = (ApplicationConfig)i.next();
             properties.setProperty(appcfg.getKey(), appcfg.getValue()==null?"":appcfg.getValue());
        }
        return properties;
    }

}
