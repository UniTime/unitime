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
package org.unitime.timetable.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.springframework.web.util.HtmlUtils;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ApplicationConfigForm;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.model.SessionConfig;
import org.unitime.timetable.model.dao.ApplicationConfigDAO;
import org.unitime.timetable.model.dao.SessionConfigDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;


/** 
 * @author Tomas Muller
 */
@Action(value = "applicationConfig", results = {
		@Result(name = "list", type = "tiles", location = "applicationConfigList.tiles"),
		@Result(name = "add", type = "tiles", location = "applicationConfigAdd.tiles"),
		@Result(name = "edit", type = "tiles", location = "applicationConfigEdit.tiles")
	})
@TilesDefinitions({
	@TilesDefinition(name = "applicationConfigList.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Application Configuration"),
			@TilesPutAttribute(name = "body", value = "/admin/applicationConfig.jsp")
		}),
	@TilesDefinition(name = "applicationConfigAdd.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Add Application Setting"),
			@TilesPutAttribute(name = "body", value = "/admin/applicationConfig.jsp")
		}),
	@TilesDefinition(name = "applicationConfigEdit.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Edit Application Setting"),
			@TilesPutAttribute(name = "body", value = "/admin/applicationConfig.jsp")
		})
})
public class ApplicationConfigAction extends UniTimeAction<ApplicationConfigForm> {
	private static final long serialVersionUID = -980973696522046141L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String apply;
	private String id;
	
	public String getApply() { return apply; }
	public void setApply(String apply) { this.apply = apply; }
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	@Override
    public String execute() throws Exception {
    	sessionContext.checkPermission(Right.ApplicationConfig);
    	
    	if (form == null)
    		form = new ApplicationConfigForm();
    	
    	if (op == null) op = form.getOp();
        if (op == null) {
            form.reset();
            op = form.getOp();
        }
        
        if ("1".equals(apply)) {
        	sessionContext.getUser().setProperty("ApplicationConfig.showAll", form.getShowAll() ? "1" : "0");
        }

        // Edit Config - Load existing config values to be edited
        if (op.equals("edit")) {
        	form.setOp("edit");
        	sessionContext.checkPermission(Right.ApplicationConfigEdit);
            if (id == null || id.trim().isEmpty()) {
            	addFieldError("form.key", MSG.errorRequiredField(MSG.columnAppConfigKey()));
            } else {
            	SessionConfig sessionConfig = SessionConfig.getConfig(id, sessionContext.getUser().getCurrentAcademicSessionId());
            	if (sessionConfig == null) {
            		ApplicationConfig appConfig = ApplicationConfigDAO.getInstance().get(id);
            		if (appConfig == null) {
                    	ApplicationProperty p = ApplicationProperty.fromKey(id);
                    	if (p != null) {
                    		form.setOp("add");
                    		form.setKey(id);
                            form.setValue(ApplicationProperties.getProperty(id, ""));
                            form.setDescription(p.description());
                            form.setAllSessions(true);
                    	} else {
                    		addFieldError("form.key", MSG.errorDoesNotExists(id));
                    	}
            		} else {
                        form.setKey(appConfig.getKey());
                        form.setValue(appConfig.getValue());
                        form.setDescription(appConfig.getDescription());
                        if (form.getDescription() == null || form.getDescription().isEmpty())
                        	form.setDescription(ApplicationProperty.getDescription(form.getKey()));
                        form.setAllSessions(true);
            		}
            	} else {
            		form.setKey(sessionConfig.getKey());
                    form.setValue(sessionConfig.getValue());
                    form.setDescription(sessionConfig.getDescription());
                    if (form.getDescription() == null || form.getDescription().isEmpty())
                    	form.setDescription(ApplicationProperty.getDescription(form.getKey()));
                    form.setAllSessions(false);
                    List<Long> sessionIds = SessionConfigDAO.getInstance().getSession().createQuery("select session.uniqueId from SessionConfig where key = :key and value = :value", Long.class)
                    		.setParameter("key", id, String.class).setParameter("value", sessionConfig.getValue(), String.class).list();
                    Long[] sessionIdsArry = new Long[sessionIds.size()];
                    for (int i = 0; i < sessionIds.size(); i++)
                    	sessionIdsArry[i] = sessionIds.get(i);
                    form.setSessions(sessionIdsArry);
                    if (sessionConfig.getDescription() == null || sessionConfig.getDescription().isEmpty()) {
                    	ApplicationConfig appConfig = ApplicationConfigDAO.getInstance().get(id);
                    	if (appConfig != null)
                    		form.setDescription(appConfig.getDescription());
                    }
            	}
            }
        }
        
        if (op.equals(MSG.actionAddSetting())) {
        	sessionContext.checkPermission(Right.ApplicationConfigEdit);
            form.reset();
            form.setAllSessions(true);
            if (sessionContext.getUser().getCurrentAcademicSessionId() != null)
            	form.setSessions(new Long[] { sessionContext.getUser().getCurrentAcademicSessionId() });
            form.setOp("add");
        }

        // Save or update config
        if(op.equals(MSG.actionSaveSetting()) || op.equals(MSG.actionUpdateSetting())) {
        	sessionContext.checkPermission(Right.ApplicationConfigEdit);
            // Validate input
        	form.validate(this);
        	if (!hasFieldErrors()) {
            	try {
                	org.hibernate.Session hibSession = SessionConfigDAO.getInstance().getSession();
                	
                	boolean update = op.equals(MSG.actionUpdateSetting()); 
                	String oldValue = null;
                	boolean wasSession = false;
            		SessionConfig sessionConfig = SessionConfig.getConfig(form.getKey(), sessionContext.getUser().getCurrentAcademicSessionId());
            		if (sessionConfig == null) {
            			ApplicationConfig appConfig = ApplicationConfigDAO.getInstance().get(form.getKey());
            			if (appConfig != null)
            				oldValue = appConfig.getValue();
            		} else {
            			oldValue = sessionConfig.getValue();
            			wasSession = true;
            		}
                	
                	if (form.isAllSessions()) {
                		if (wasSession) { // there was a session config for the current session
                			if (update) { // update --> delete all with the same value
                        		for (SessionConfig config: hibSession.createQuery(
                        				"from SessionConfig where key = :key and value = :value", SessionConfig.class)
                        				.setParameter("key", form.getKey(), String.class).setParameter("value", oldValue, String.class).list()) {
                        			getSolverServerService().setApplicationProperty(config.getSession().getUniqueId(), form.getKey(), null);
                        			hibSession.delete(config);
                        		}
                			} else { // create --> delete just the current one
                				SessionConfig config = SessionConfig.getConfig(form.getKey(), sessionContext.getUser().getCurrentAcademicSessionId());
                				if (config != null) {
                					getSolverServerService().setApplicationProperty(config.getSession().getUniqueId(), form.getKey(), null);
                        			hibSession.delete(config);
                				}
                			}
                		}
                		
                		ApplicationConfig config = ApplicationConfigDAO.getInstance().get(form.getKey());
                		if (config == null) {
                			config = new ApplicationConfig();
                			config.setKey(form.getKey());
                		}
                		config.setValue(form.getValue());
    	                config.setDescription(form.getDescription());
    	                
    	                getSolverServerService().setApplicationProperty(null, form.getKey(), form.getValue());
    	                
    	                hibSession.saveOrUpdate(config);
                	} else {
                		if (update && !wasSession) {
                			// update --> delete global value
                			ApplicationConfig config = ApplicationConfigDAO.getInstance().get(form.getKey());
                			if (config != null) {
                				getSolverServerService().setApplicationProperty(null, form.getKey(), null);
                    			hibSession.delete(config);
                			}
                		}
                		
                		Set<Long> updatedSessionIds = new HashSet<Long>();
                		
                		for (Long sessionId: form.getSessions()) {
                			SessionConfig config = hibSession.createQuery(
                					"from SessionConfig where key = :key and session.uniqueId = :sessionId", SessionConfig.class)
                					.setParameter("sessionId", sessionId, Long.class).setParameter("key", form.getKey(), String.class).uniqueResult();
                			if (config == null) {
                				config = new SessionConfig();
                				config.setKey(form.getKey());
                				config.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
                			}
                			
                			config.setValue(form.getValue());
        	                config.setDescription(form.getDescription());
        	                
        	                getSolverServerService().setApplicationProperty(sessionId, form.getKey(), form.getValue());
        	                
        	                hibSession.saveOrUpdate(config);
        	                updatedSessionIds.add(sessionId);
                		}
                		
                		if (update && oldValue != null) {
                			// update --> delete old session values
                			for (SessionConfig other: hibSession.createQuery(
                					"from SessionConfig where key = :key and value = :value", SessionConfig.class)
                					.setParameter("key", form.getKey(), String.class).setParameter("value", oldValue, String.class).list()) {
                				if (!updatedSessionIds.contains(other.getSession().getUniqueId())) {
                					getSolverServerService().setApplicationProperty(other.getSession().getUniqueId(), form.getKey(), null);
                					hibSession.delete(other);
                				}
                			}
                		}
                	}
                	
                	hibSession.flush();
                	request.setAttribute("hash", form.getKey());
                	
                	form.reset();
                } catch (Exception e) {
                	e.printStackTrace();
                	addFieldError("form.key", e.getMessage());
                }
            }
        }

        // Delete config
        if(op.equals(MSG.actionDeleteSetting())) {
        	sessionContext.checkPermission(Right.ApplicationConfigEdit);
            // Validate input
        	form.validate(this);
        	if (!hasFieldErrors()) {
            	try {
                	org.hibernate.Session hibSession = SessionConfigDAO.getInstance().getSession();
                	
                	SessionConfig sessionConfig = null;
                	if (sessionContext.getUser().getCurrentAcademicSessionId() != null) {
                		sessionConfig = hibSession.createQuery(
        					"from SessionConfig where key = :key and session.uniqueId = :sessionId", SessionConfig.class)
        					.setParameter("sessionId", sessionContext.getUser().getCurrentAcademicSessionId(), Long.class).setParameter("key", form.getKey(), String.class).uniqueResult();
                	}
                	
                	if (sessionConfig == null) {
                		ApplicationConfig appConfig = ApplicationConfigDAO.getInstance().get(form.getKey());
                		if (appConfig != null) {
                			hibSession.delete(appConfig);
                			getSolverServerService().setApplicationProperty(null, form.getKey(), null);
                		}
                	} else {
                		String oldValue = sessionConfig.getValue();
                		hibSession.delete(sessionConfig);
                		getSolverServerService().setApplicationProperty(sessionContext.getUser().getCurrentAcademicSessionId(), form.getKey(), null);
            			
            			for (SessionConfig other: hibSession.createQuery(
            					"from SessionConfig where key = :key and value = :value", SessionConfig.class)
            					.setParameter("key", form.getKey(), String.class).setParameter("value", oldValue, String.class).list()) {
            				getSolverServerService().setApplicationProperty(other.getSession().getUniqueId(), form.getKey(), null);
            				hibSession.delete(other);
            			}
                	}
                	
                	hibSession.flush();
                	
                	request.setAttribute("hash", form.getKey());
                	form.reset();
            	} catch (Exception e) {
                    e.printStackTrace();
                    addFieldError("form.key", e.getMessage());
                }
            }
        }
        
        // Cancel update
        if(op.equals(MSG.actionCancelSetting())) {
        	request.setAttribute("hash", form.getKey());
            form.reset();
        }
        
        form.setShowAll("1".equals(sessionContext.getUser().getProperty("ApplicationConfig.showAll", "0")));
        
        if ("list".equals(form.getOp())) {
            //Read all existing ApplicationConfig and store in request
            getApplicationConfigList(form.getShowAll());        
            return "list";
        }
        
        return (MSG.actionSaveSetting().equals(op) || "add".equals(form.getOp())?"add":"edit");
    }

    /**
     * Retrieve all existing defined configs
     */
    private void getApplicationConfigList(boolean showAll) throws Exception {
        WebTable.setOrder(sessionContext,"applicationConfig.ord",request.getParameter("ord"),1);
		
		// Create web table instance 
        WebTable webTable = new WebTable( 4,
			    null, "applicationConfig.action?ord=%%",
			    new String[] {
			    		MSG.columnAppConfigKey(),
			    		MSG.columnAppConfigValue(),
			    		MSG.columnAppConfigDescription()
			    		},
			    new String[] {"left", "left", "left"},
			    null );
        webTable.enableHR("#9CB0CE");
        String hash = (String)request.getAttribute("hash");
        
		Map<String, Object> configs = new HashMap<String, Object>();
		for (ApplicationConfig config: ApplicationConfigDAO.getInstance().findAll())
			configs.put(config.getKey(), config);
		
		Map<String, String> properties = new HashMap<String, String>();
		for (Map.Entry<Object, Object> p: ApplicationProperties.getProperties().entrySet())
			properties.put(p.getKey().toString(), p.getValue().toString());
		for (ApplicationProperty property: ApplicationProperty.values()) {
			if (properties.containsKey(property.key()) || property.isSecret() || property.isDeprecated()) continue;
			if (property.reference() == null) {
				properties.put(property.key(), property.defaultValue() == null ? "" : property.defaultValue());
			} else {
				boolean nomatch = true;
				for (Object key: properties.keySet()) {
					if (property.matches(key.toString())) { nomatch = false; break; }
				}
				if (nomatch)
					properties.put(property.key(), property.defaultValue() == null ? "" : property.defaultValue());
			}
		}
				
		if (sessionContext.getUser().getCurrentAcademicSessionId() != null) {
			for (SessionConfig config: SessionConfig.findAll(sessionContext.getUser().getCurrentAcademicSessionId()))
				configs.put(config.getKey(), config);
		}
		
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(ApplicationProperty.ApplicationConfigPattern.value());
		} catch (Exception e) {
			pattern = Pattern.compile(ApplicationProperty.ApplicationConfigPattern.defaultValue());
		}

		boolean editable = sessionContext.hasPermission(Right.ApplicationConfigEdit);
		if (properties.isEmpty()) {
			webTable.addLine(null, new String[] {MSG.messageNoAppConfKeys()}, null, null);
		} else {
			for (String key: new TreeSet<String>(properties.keySet())) {
				String value = properties.get(key);
				Object o = configs.get(key);
				ApplicationProperty p = ApplicationProperty.fromKey(key);
				String description = ApplicationProperty.getDescription(key);
				if (description == null) description = "";
				WebTableLine line = null;

				if (o == null) {
					if (!pattern.matcher(key).matches()) continue;
					if (!showAll && (p != null && (value == null ? "": value).equals(p.value() == null ? "" : p.value()))) continue;

					String reference = null;
					if (p != null && p.reference() != null) {
						reference = p.reference();
					}
					
					if (p != null && p.isSecret()) continue;
					
					line = webTable.addLine(
				    		editable && (p != null && !p.isReadOnly()) ? "onClick=\"document.location='applicationConfig.action?op=edit&id=" + (reference == null ? key : key.replace("%", "<" + reference + ">")) + "';\"" : null,
				    		new String[] {
				    			"<a id='" + (reference == null ? key : key.replace("%", "<" + reference + ">")) + "'>" +
				    			(reference == null ? HtmlUtils.htmlEscape(key) : HtmlUtils.htmlEscape(key).replace("%", "<i><u>" + reference + "</i></u>")) +
				    			"</a>",
				    			"<font color='gray'>" + HtmlUtils.htmlEscape(value) + "</font>",
				    			(reference == null ? description : description.replace("%", "<i><u>" + reference + "</i></u>")) },
				    		new String[] {key, value, description}
				    		);
					if (key.equals(hash)) line.setBgColor("rgb(168,187,225)");
					continue;
				}
				
				if (o instanceof SessionConfig) {
					SessionConfig config = (SessionConfig)o;
					if (config.getDescription() != null && !config.getDescription().isEmpty())
						description = config.getDescription();

					line = webTable.addLine(
				    		editable && (p == null || !p.isReadOnly()) ? "onClick=\"document.location='applicationConfig.action?op=edit&id=" + key + "';\"" : null,
				    		new String[] {"<a id='"+key+"'>" + HtmlUtils.htmlEscape(key)  + " <sup><font color='#2066CE' title='" + MSG.hintAppConfigAppliesTo(config.getSession().getLabel())+ "'>" + MSG.supAppConfigSessionOnly() + "</font></sup></a>",
				    			HtmlUtils.htmlEscape(value), description},
				    		new String[] {key, value, description}
				    		);
				} else {
					ApplicationConfig config = (ApplicationConfig)o;
					if (config.getDescription() != null && !config.getDescription().isEmpty())
						description = config.getDescription();

				    line = webTable.addLine(
				    		editable && (p == null || !p.isReadOnly()) ? "onClick=\"document.location='applicationConfig.action?op=edit&id=" + key + "';\"" : null,
				    		new String[] {"<a id='"+key+"'>" + HtmlUtils.htmlEscape(key) + "</a>", HtmlUtils.htmlEscape(value), description},
				    		new String[] {key, value, description}
				    		);
				}
				if (key.equals(hash)) line.setBgColor("rgb(168,187,225)");
			}
	    }

	    request.setAttribute(ApplicationConfig.APP_CFG_ATTR_NAME, webTable.printTable(WebTable.getOrder(sessionContext,"applicationConfig.ord")));
    }
    
}
