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
package org.unitime.timetable.server.administration.utils;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.admin.ApplicationConfigPage.ApplicationSettingEditRequest;
import org.unitime.timetable.gwt.client.admin.ApplicationConfigPage.ApplicationSettingEditResponse;
import org.unitime.timetable.gwt.client.admin.ApplicationConfigPage.ApplicationSettingInterface;
import org.unitime.timetable.gwt.client.admin.ApplicationConfigPage.ApplicationSettingEditRequest.Operation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SessionConfig;
import org.unitime.timetable.model.dao.ApplicationConfigDAO;
import org.unitime.timetable.model.dao.SessionConfigDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;

@GwtRpcImplements(ApplicationSettingEditRequest.class)
public class ApplicationSettingEditBackend implements GwtRpcImplementation<ApplicationSettingEditRequest, ApplicationSettingEditResponse>{
	@Autowired SolverServerService solverServerService;

	@Override
	public ApplicationSettingEditResponse execute(ApplicationSettingEditRequest request, SessionContext context) {
		context.checkPermission(Right.ApplicationConfigEdit);
		switch (request.getOperation()) {
		case ADD:
			ApplicationSettingEditResponse addResponse = new ApplicationSettingEditResponse();
			addResponse.setCurrentSessionId(context.getUser().getCurrentAcademicSessionId());
			addResponse.setCanDelete(false);
			addResponse.setSetting(new ApplicationSettingInterface());
			setupSessions(addResponse, context);
			return addResponse;
		case EDIT:
			ApplicationSettingEditResponse editResponse = new ApplicationSettingEditResponse();
			editResponse.setCurrentSessionId(context.getUser().getCurrentAcademicSessionId());
			ApplicationSettingInterface setting = new ApplicationSettingInterface();
			setting.setKey(request.getKey());
			setting.setAllSessions(true);
			setting.setDefaultValue(ApplicationProperties.getDefaultProperties().getProperty(request.getKey()));
			editResponse.setSetting(setting);
			editResponse.setCanDelete(false);
			SessionConfig sessionConfig = SessionConfig.getConfig(request.getKey(), context.getUser().getCurrentAcademicSessionId());
			ApplicationConfig appConfig = ApplicationConfigDAO.getInstance().get(request.getKey());
			ApplicationProperty p = ApplicationProperty.fromKey(request.getKey());
			if (sessionConfig != null) {
				setting.setAllSessions(false);
				setting.setValue(sessionConfig.getValue());
				setting.setDescription(sessionConfig.getDescription());
				if (!setting.hasDescription() && appConfig != null)
					setting.setDescription(appConfig.getDescription());
				if (!setting.hasDescription())
					setting.setDescription(ApplicationProperty.getDescription(request.getKey()));
				setting.setSessionIds(SessionConfigDAO.getInstance().getSession().createQuery(
						"select session.uniqueId from SessionConfig where key = :key and value = :value", Long.class)
						.setParameter("key", request.getKey()).setParameter("value", sessionConfig.getValue()).list());
				editResponse.setCanDelete(true);
			} else if (appConfig != null) {
				setting.setValue(appConfig.getValue());
				setting.setDescription(appConfig.getDescription());
				setting.addSessionId(context.getUser().getCurrentAcademicSessionId());
				if (!setting.hasDescription())
					setting.setDescription(ApplicationProperty.getDescription(request.getKey()));
				editResponse.setCanDelete(true);
			} else if (p != null) {
				setting.setValue(p.value());
				setting.addSessionId(context.getUser().getCurrentAcademicSessionId());
			} else {
				setting.setValue(ApplicationProperties.getProperty(request.getKey()));
				setting.addSessionId(context.getUser().getCurrentAcademicSessionId());
			}
			if (p != null) {
				if (!setting.hasDescription())
					setting.setDescription(p.description());
				if (!setting.hasDefaultValue())
					setting.setDefaultValue(p.defaultValue());
				if (p.availableValues() != null)
					for (String av: p.availableValues())
						setting.addPossibleValue(av);
				setting.setType(p.type().getSimpleName());
				setting.setReference(p.reference());
			}
			setupSessions(editResponse, context);
			return editResponse;
		case DELETE:
			deleteSetting(request.getKey(), context);
			return null;
		case SAVE:
		case UPDATE:
			saveOrUpdateSetting(request.getSetting(), context, request.getOperation() == Operation.UPDATE);
			return null;
		}
		return null;
	}
	
	protected void setupSessions(ApplicationSettingEditResponse response, SessionContext context) {
		ApplicationSettingInterface setting = response.getSetting();
		for (Session session: Session.getAllSessions()) {
			if (context.getUser().getCurrentAcademicSessionId().equals(session.getUniqueId())) {
				response.addSession(session.getUniqueId(), session.getLabel()).setColor(session.getStatusType().isActive() ? null : "#646464");
			} else if (session.getStatusType().isActive()) {
				response.addSession(session.getUniqueId(), session.getLabel());
			} else if (setting != null && setting.hasSessionId(session.getUniqueId())) {
				response.addSession(session.getUniqueId(), session.getLabel()).setColor("#646464");
			}
		}
	}
	
	protected void deleteSetting(String key, SessionContext context) {
    	org.hibernate.Session hibSession = SessionConfigDAO.getInstance().getSession();
    	Transaction tx = null;
        try {
        	tx = hibSession.beginTransaction();
        	
        	SessionConfig sessionConfig = null;
        	if (context.getUser().getCurrentAcademicSessionId() != null) {
        		sessionConfig = hibSession.createQuery(
					"from SessionConfig where key = :key and session.uniqueId = :sessionId", SessionConfig.class)
					.setParameter("sessionId", context.getUser().getCurrentAcademicSessionId()).setParameter("key", key).uniqueResult();
        	}
        	
        	if (sessionConfig == null) {
        		ApplicationConfig appConfig = ApplicationConfigDAO.getInstance().get(key);
        		if (appConfig != null) {
        			hibSession.remove(appConfig);
        			solverServerService.setApplicationProperty(null, key, null);
        		}
        	} else {
        		String oldValue = sessionConfig.getValue();
        		hibSession.remove(sessionConfig);
        		solverServerService.setApplicationProperty(context.getUser().getCurrentAcademicSessionId(), key, null);
    			
    			for (SessionConfig other: hibSession.createQuery(
    					"from SessionConfig where key = :key and value = :value", SessionConfig.class)
    					.setParameter("key", key).setParameter("value", oldValue).list()) {
    				solverServerService.setApplicationProperty(other.getSession().getUniqueId(), key, null);
    				hibSession.remove(other);
    			}
        	}
        	
        	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}
	
	protected void saveOrUpdateSetting(ApplicationSettingInterface setting, SessionContext context, boolean update) {
    	org.hibernate.Session hibSession = SessionConfigDAO.getInstance().getSession();
    	Transaction tx = null;
        try {
        	tx = hibSession.beginTransaction();
        	
        	String oldValue = null;
        	boolean wasSession = false;
    		SessionConfig sessionConfig = SessionConfig.getConfig(setting.getKey(), context.getUser().getCurrentAcademicSessionId());
    		if (sessionConfig == null) {
    			ApplicationConfig appConfig = ApplicationConfigDAO.getInstance().get(setting.getKey());
    			if (appConfig != null) {
    				oldValue = appConfig.getValue();
    				update = true;
    			}
    		} else {
    			oldValue = sessionConfig.getValue();
    			wasSession = true;
    			update = true;
    		}
    		
    		if (setting.isAllSessions()) {
        		if (wasSession) { // there was a session config for the current session
        			if (update) { // update --> delete all with the same value
                		for (SessionConfig config: hibSession.createQuery(
                				"from SessionConfig where key = :key and value = :value", SessionConfig.class)
                				.setParameter("key", setting.getKey()).setParameter("value", oldValue).list()) {
                			solverServerService.setApplicationProperty(config.getSession().getUniqueId(), setting.getKey(), null);
                			hibSession.remove(config);
                		}
        			} else { // create --> delete just the current one
        				SessionConfig config = SessionConfig.getConfig(setting.getKey(), context.getUser().getCurrentAcademicSessionId());
        				if (config != null) {
        					solverServerService.setApplicationProperty(config.getSession().getUniqueId(), setting.getKey(), null);
                			hibSession.remove(config);
        				}
        			}
        		}
        		
        		ApplicationConfig config = ApplicationConfigDAO.getInstance().get(setting.getKey());
        		boolean create = false;
        		if (config == null) {
        			config = new ApplicationConfig();
        			config.setKey(setting.getKey());
        			create = true;
        		}
        		config.setValue(setting.getValue());
                config.setDescription(setting.getDescription());
                
                solverServerService.setApplicationProperty(null, setting.getKey(), setting.getValue());
                
                if (create)
                	hibSession.persist(config);
                else
                	hibSession.merge(config);
    		} else {
    			if (update && !wasSession) {
        			// update --> delete global value
        			ApplicationConfig config = ApplicationConfigDAO.getInstance().get(setting.getKey());
        			if (config != null) {
        				solverServerService.setApplicationProperty(null, setting.getKey(), null);
            			hibSession.remove(config);
        			}
        		}
        		
        		Set<Long> updatedSessionIds = new HashSet<Long>();
        		
        		for (Long sessionId: setting.getSessionIds()) {
        			SessionConfig config = hibSession.createQuery(
        					"from SessionConfig where key = :key and session.uniqueId = :sessionId", SessionConfig.class)
        					.setParameter("sessionId", sessionId).setParameter("key", setting.getKey()).uniqueResult();
        			boolean create = false;
        			if (config == null) {
        				config = new SessionConfig();
        				config.setKey(setting.getKey());
        				config.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
        				create = true;
        			}
        			
        			config.setValue(setting.getValue());
	                config.setDescription(setting.getDescription());
	                
	                solverServerService.setApplicationProperty(sessionId, setting.getKey(), setting.getValue());
	                
	                if (create)
	                	hibSession.persist(config);
	                else
	                	hibSession.merge(config);
	                updatedSessionIds.add(sessionId);
        		}
        		
        		if (update && oldValue != null) {
        			// update --> delete old session values
        			for (SessionConfig other: hibSession.createQuery(
        					"from SessionConfig where key = :key and value = :value", SessionConfig.class)
        					.setParameter("key", setting.getKey()).setParameter("value", oldValue).list()) {
        				if (!updatedSessionIds.contains(other.getSession().getUniqueId())) {
        					solverServerService.setApplicationProperty(other.getSession().getUniqueId(), setting.getKey(), null);
        					hibSession.remove(other);
        				}
        			}
        		}
    		}

        	tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
	}

}
