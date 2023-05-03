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

import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;
import java.util.Properties;

import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.model.base.BaseSessionConfig;
import org.unitime.timetable.model.dao.SessionConfigDAO;

/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "session_config")
public class SessionConfig extends BaseSessionConfig {
	private static final long serialVersionUID = 1L;

	public SessionConfig() {
		super();
	}

	
	public static SessionConfig getConfig(String key, Long sessionId) {
		if (sessionId == null) return null;
        return SessionConfigDAO.getInstance().getSession().createQuery(
        		"from SessionConfig where key = :key and session.uniqueId = :sessionId", SessionConfig.class
        		).setParameter("key", key).setParameter("sessionId", sessionId).setCacheable(true).uniqueResult();
	}
	
	public static List<SessionConfig> findAll(Long sessionId) {
        return SessionConfigDAO.getInstance().getSession().createQuery(
        		"from SessionConfig where session.uniqueId = :sessionId order by key", SessionConfig.class
        		).setParameter("sessionId", sessionId).setCacheable(true).list();
	}

	public static String getConfigValue(String key, Long sessionId, String defaultValue) {
	    //return defaultValue if hibernate is not yet initialized or no session is given
        if (!HibernateUtil.isConfigured() || sessionId == null) return defaultValue;
        
        String value = SessionConfigDAO.getInstance().getSession().createQuery(
        		"select value from SessionConfig where key = :key and session.uniqueId = :sessionId", String.class
        		).setParameter("key", key).setParameter("sessionId", sessionId).setCacheable(true).uniqueResult();

        return (value == null ? defaultValue : value);
	}
    
    public static Properties toProperties(Long sessionId) {
        if (!HibernateUtil.isConfigured() || sessionId == null) return null;
        
        org.hibernate.Session hibSession = SessionConfigDAO.getInstance().createNewSession();
        Transaction tx = hibSession.beginTransaction();
        try {
            Properties properties = new Properties();
            for (SessionConfig config: hibSession.createQuery(
            		"from SessionConfig where session.uniqueId = :sessionId", SessionConfig.class).setParameter("sessionId", sessionId).setCacheable(true).list()) {
            	properties.setProperty(config.getKey(), config.getValue() == null ? "" : config.getValue());
            }
			tx.commit();
			return properties;
		} catch (Exception e) {
			if (tx.isActive()) tx.rollback();
		} finally {
        	hibSession.close();
        }
        return null;
    }
}
