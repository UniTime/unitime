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
