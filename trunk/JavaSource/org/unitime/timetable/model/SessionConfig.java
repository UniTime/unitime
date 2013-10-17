/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import java.util.List;
import java.util.Properties;

import org.unitime.timetable.model.base.BaseSessionConfig;
import org.unitime.timetable.model.dao.SessionConfigDAO;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
public class SessionConfig extends BaseSessionConfig {
	private static final long serialVersionUID = 1L;

	public SessionConfig() {
		super();
	}

	
	public static SessionConfig getConfig(String key, Long sessionId) {
		if (sessionId == null) return null;
        return (SessionConfig)SessionConfigDAO.getInstance().getSession().createQuery(
        		"from SessionConfig where key = :key and session.uniqueId = :sessionId"
        		).setString("key", key).setLong("sessionId", sessionId).setCacheable(true).uniqueResult();
	}
	
	public static List<SessionConfig> findAll(Long sessionId) {
        return (List<SessionConfig>)SessionConfigDAO.getInstance().getSession().createQuery(
        		"from SessionConfig where session.uniqueId = :sessionId order by key"
        		).setLong("sessionId", sessionId).setCacheable(true).list();
	}

	public static String getConfigValue(String key, Long sessionId, String defaultValue) {
	    //return defaultValue if hibernate is not yet initialized or no session is given
        if (!_RootDAO.isConfigured() || sessionId == null) return defaultValue;
        
        String value = (String)SessionConfigDAO.getInstance().getSession().createQuery(
        		"select value from SessionConfig where key = :key and session.uniqueId = :sessionId"
        		).setString("key", key).setLong("sessionId", sessionId).setCacheable(true).uniqueResult();

        return (value == null ? defaultValue : value);
	}
    
    public static Properties toProperties(Long sessionId) {
        Properties properties = new Properties();
        if (!_RootDAO.isConfigured() || sessionId == null) return properties;
        
        org.hibernate.Session hibSession = SessionConfigDAO.getInstance().createNewSession();
        try {
            for (SessionConfig config: (List<SessionConfig>)hibSession.createQuery(
            		"from SessionConfig where session.uniqueId = :sessionId").setLong("sessionId", sessionId).setCacheable(true).list()) {
            	properties.setProperty(config.getKey(), config.getValue() == null ? "" : config.getValue());
            }
        } finally {
        	hibSession.close();
        }

        return properties;
    }
}
