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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ApplicationConfigForm;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.model.SessionConfig;
import org.unitime.timetable.model.dao.ApplicationConfigDAO;
import org.unitime.timetable.model.dao.SessionConfigDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;


/** 
 * MyEclipse Struts
 * Creation date: 08-28-2006
 * 
 * XDoclet definition:
 * @struts:action path="/applicationConfig" name="applicationConfigForm" input="/admin/applicationConfig.jsp" scope="request"
 *
 * @author Tomas Muller
 */
@Service("/applicationConfig")
public class ApplicationConfigAction extends Action {

    // --------------------------------------------------------- Instance Variables
	
	@Autowired SessionContext sessionContext;
	@Autowired SolverServerService solverServerService;

    // --------------------------------------------------------- Methods

    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {
    	
    	sessionContext.checkPermission(Right.ApplicationConfig);
        
        ApplicationConfigForm frm = (ApplicationConfigForm) form;
        MessageResources rsc = getResources(request);

        String op = (frm.getOp()!=null?frm.getOp():request.getParameter("op"));
        
        if (op==null) {
            frm.reset(mapping, request);
            op = frm.getOp();
        }
        
        if ("1".equals(request.getParameter("apply"))) {
        	sessionContext.getUser().setProperty("ApplicationConfig.showAll", frm.getShowAll() ? "1" : "0");
        }

        ActionMessages errors = new ActionMessages();
        
        // Edit Config - Load existing config values to be edited
        if (op.equals("edit")) {
        	sessionContext.checkPermission(Right.ApplicationConfigEdit);
            String id = request.getParameter("id");
            if (id == null || id.trim().isEmpty()) {
                errors.add("key", new ActionMessage("errors.invalid", "Name : " + id));
                saveErrors(request, errors);
            } else {
            	SessionConfig sessionConfig = SessionConfig.getConfig(id, sessionContext.getUser().getCurrentAcademicSessionId());
            	if (sessionConfig == null) {
            		ApplicationConfig appConfig = ApplicationConfigDAO.getInstance().get(id);
            		if (appConfig == null) {
                    	ApplicationProperty p = ApplicationProperty.fromKey(id);
                    	if (p != null) {
                    		frm.setOp("add");
                    		frm.setKey(id);
                            frm.setValue(ApplicationProperties.getProperty(id, ""));
                            frm.setDescription(p.description());
                            frm.setAllSessions(true);
                    	} else {
                    		errors.add("key", new ActionMessage("errors.invalid", "Name : " + id));
                    		saveErrors(request, errors);
                    	}
            		} else {
                        frm.setKey(appConfig.getKey());
                        frm.setValue(appConfig.getValue());
                        frm.setDescription(appConfig.getDescription());
                        if (frm.getDescription() == null || frm.getDescription().isEmpty())
                        	frm.setDescription(ApplicationProperty.getDescription(frm.getKey()));
                        frm.setAllSessions(true);
            		}
            	} else {
            		frm.setKey(sessionConfig.getKey());
                    frm.setValue(sessionConfig.getValue());
                    frm.setDescription(sessionConfig.getDescription());
                    if (frm.getDescription() == null || frm.getDescription().isEmpty())
                    	frm.setDescription(ApplicationProperty.getDescription(frm.getKey()));
                    frm.setAllSessions(false);
                    List<Long> sessionIds = SessionConfigDAO.getInstance().getSession().createQuery("select session.uniqueId from SessionConfig where key = :key and value = :value")
                    		.setString("key", id).setString("value", sessionConfig.getValue()).list();
                    Long[] sessionIdsArry = new Long[sessionIds.size()];
                    for (int i = 0; i < sessionIds.size(); i++)
                    	sessionIdsArry[i] = sessionIds.get(i);
                    frm.setSessions(sessionIdsArry);
                    if (sessionConfig.getDescription() == null || sessionConfig.getDescription().isEmpty()) {
                    	ApplicationConfig appConfig = ApplicationConfigDAO.getInstance().get(id);
                    	if (appConfig != null)
                    		frm.setDescription(appConfig.getDescription());
                    }
            	}
            }
        }
        
        if (op.equals(rsc.getMessage("button.addAppConfig"))) {
        	sessionContext.checkPermission(Right.ApplicationConfigEdit);
            frm.reset(mapping, request);
            frm.setAllSessions(true);
            if (sessionContext.getUser().getCurrentAcademicSessionId() != null)
            	frm.setSessions(new Long[] { sessionContext.getUser().getCurrentAcademicSessionId() });
            frm.setOp(rsc.getMessage("button.createAppConfig"));
        }

        // Save or update config
        if(op.equals(rsc.getMessage("button.updateAppConfig")) || op.equals(rsc.getMessage("button.createAppConfig"))) {
        	sessionContext.checkPermission(Right.ApplicationConfigEdit);
            // Validate input
            errors = frm.validate(mapping, request);
            if (!errors.isEmpty()) {
                saveErrors(request, errors);
            } else {
            	try {
                	org.hibernate.Session hibSession = SessionConfigDAO.getInstance().getSession();
                	
                	boolean update = op.equals(rsc.getMessage("button.updateAppConfig")); 
                	String oldValue = null;
                	boolean wasSession = false;
            		SessionConfig sessionConfig = SessionConfig.getConfig(frm.getKey(), sessionContext.getUser().getCurrentAcademicSessionId());
            		if (sessionConfig == null) {
            			ApplicationConfig appConfig = ApplicationConfigDAO.getInstance().get(frm.getKey());
            			if (appConfig != null)
            				oldValue = appConfig.getValue();
            		} else {
            			oldValue = sessionConfig.getValue();
            			wasSession = true;
            		}
                	
                	if (frm.isAllSessions()) {
                		if (wasSession) { // there was a session config for the current session
                			if (update) { // update --> delete all with the same value
                        		for (SessionConfig config: (List<SessionConfig>)hibSession.createQuery("from SessionConfig where key = :key and value = :value").setString("key", frm.getKey()).setString("value", oldValue).list()) {
                        			solverServerService.setApplicationProperty(config.getSession().getUniqueId(), frm.getKey(), null);
                        			hibSession.delete(config);
                        		}
                			} else { // create --> delete just the current one
                				SessionConfig config = SessionConfig.getConfig(frm.getKey(), sessionContext.getUser().getCurrentAcademicSessionId());
                				if (config != null) {
                					solverServerService.setApplicationProperty(config.getSession().getUniqueId(), frm.getKey(), null);
                        			hibSession.delete(config);
                				}
                			}
                		}
                		
                		ApplicationConfig config = ApplicationConfigDAO.getInstance().get(frm.getKey());
                		if (config == null) {
                			config = new ApplicationConfig();
                			config.setKey(frm.getKey());
                		}
                		config.setValue(frm.getValue());
    	                config.setDescription(frm.getDescription());
    	                
    	                solverServerService.setApplicationProperty(null, frm.getKey(), frm.getValue());
    	                
    	                hibSession.saveOrUpdate(config);
                	} else {
                		if (update && !wasSession) {
                			// update --> delete global value
                			ApplicationConfig config = ApplicationConfigDAO.getInstance().get(frm.getKey());
                			if (config != null) {
                    			solverServerService.setApplicationProperty(null, frm.getKey(), null);
                    			hibSession.delete(config);
                			}
                		}
                		
                		Set<Long> updatedSessionIds = new HashSet<Long>();
                		
                		for (Long sessionId: frm.getSessions()) {
                			SessionConfig config = (SessionConfig)hibSession.createQuery(
                					"from SessionConfig where key = :key and session.uniqueId = :sessionId")
                					.setLong("sessionId", sessionId).setString("key", frm.getKey()).uniqueResult();
                			if (config == null) {
                				config = new SessionConfig();
                				config.setKey(frm.getKey());
                				config.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
                			}
                			
                			config.setValue(frm.getValue());
        	                config.setDescription(frm.getDescription());
        	                
        	                solverServerService.setApplicationProperty(sessionId, frm.getKey(), frm.getValue());
        	                
        	                hibSession.saveOrUpdate(config);
        	                updatedSessionIds.add(sessionId);
                		}
                		
                		if (update && oldValue != null) {
                			// update --> delete old session values
                			for (SessionConfig other: (List<SessionConfig>)hibSession.createQuery(
                					"from SessionConfig where key = :key and value = :value")
                					.setString("key", frm.getKey()).setString("value", oldValue).list()) {
                				if (!updatedSessionIds.contains(other.getSession().getUniqueId())) {
                					solverServerService.setApplicationProperty(other.getSession().getUniqueId(), frm.getKey(), null);
                					hibSession.delete(other);
                				}
                			}
                		}
                	}
                	
                	hibSession.flush();
                	request.setAttribute("hash", frm.getKey());
                	
                	frm.reset(mapping, request);
                } catch (Exception e) {
                	errors.add("key", new ActionMessage("errors.generic", e.getMessage()));
                	saveErrors(request, errors);
                }
            }
        }

        // Delete config
        if(op.equals(rsc.getMessage("button.deleteAppConfig"))) {
        	sessionContext.checkPermission(Right.ApplicationConfigEdit);
            // Validate input
            errors = frm.validate(mapping, request);
            if (!errors.isEmpty()) {
                saveErrors(request, errors);
            } else {
            	try {
                	org.hibernate.Session hibSession = SessionConfigDAO.getInstance().getSession();
                	
                	SessionConfig sessionConfig = null;
                	if (sessionContext.getUser().getCurrentAcademicSessionId() != null) {
                		sessionConfig = (SessionConfig)hibSession.createQuery(
        					"from SessionConfig where key = :key and session.uniqueId = :sessionId")
        					.setLong("sessionId", sessionContext.getUser().getCurrentAcademicSessionId()).setString("key", frm.getKey()).uniqueResult();
                	}
                	
                	if (sessionConfig == null) {
                		ApplicationConfig appConfig = ApplicationConfigDAO.getInstance().get(frm.getKey());
                		if (appConfig != null) {
                			hibSession.delete(appConfig);
                			solverServerService.setApplicationProperty(null, frm.getKey(), null);
                		}
                	} else {
                		String oldValue = sessionConfig.getValue();
                		hibSession.delete(sessionConfig);
            			solverServerService.setApplicationProperty(sessionContext.getUser().getCurrentAcademicSessionId(), frm.getKey(), null);
            			
            			for (SessionConfig other: (List<SessionConfig>)hibSession.createQuery(
            					"from SessionConfig where key = :key and value = :value")
            					.setString("key", frm.getKey()).setString("value", oldValue).list()) {
            				solverServerService.setApplicationProperty(other.getSession().getUniqueId(), frm.getKey(), null);
            				hibSession.delete(other);
            			}
                	}
                	
                	hibSession.flush();
                	
                	frm.reset(mapping, request);
            	} catch (Exception e) {
                    e.printStackTrace();
                    errors.add("key", new ActionMessage("errors.generic", e.getMessage()));
                    saveErrors(request, errors);
                }
            }
        }
        
        // Cancel update
        if(op.equals(rsc.getMessage("button.cancelUpdateAppConfig"))) {
        	request.setAttribute("hash", frm.getKey());
            frm.reset(mapping, request);
        }
        
        frm.setShowAll("1".equals(sessionContext.getUser().getProperty("ApplicationConfig.showAll", "0")));
        
        if ("list".equals(frm.getOp())) {
            //Read all existing ApplicationConfig and store in request
            getApplicationConfigList(request, frm.getShowAll());        
            return mapping.findForward("list");
        }
        
        return mapping.findForward(rsc.getMessage("button.createAppConfig").equals(frm.getOp())?"add":"edit");
    }

    /**
     * Retrieve all existing defined configs
     * @param request Request object
     * @throws Exception
     */
    private void getApplicationConfigList(HttpServletRequest request, boolean showAll) throws Exception {
        WebTable.setOrder(sessionContext,"applicationConfig.ord",request.getParameter("ord"),1);
		
		// Create web table instance 
        WebTable webTable = new WebTable( 4,
			    null, "applicationConfig.do?ord=%%",
			    new String[] {"Name", "Value", "Description"},
			    new String[] {"left", "left", "left"},
			    null );
        webTable.enableHR("#9CB0CE");
        
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
			webTable.addLine(null, new String[] {"No configuration keys found"}, null, null);
		} else {
			for (String key: new TreeSet<String>(properties.keySet())) {
				String value = properties.get(key);
				Object o = configs.get(key);
				ApplicationProperty p = ApplicationProperty.fromKey(key);
				String description = ApplicationProperty.getDescription(key);
				if (description == null) description = "";

				if (o == null) {
					if (!pattern.matcher(key).matches()) continue;
					if (!showAll && (p != null && (value == null ? "": value).equals(p.value() == null ? "" : p.value()))) continue;

					String reference = null;
					if (p != null && p.reference() != null) {
						reference = p.reference();
					}
					
					if (p != null && p.isSecret()) continue;
					
					webTable.addLine(
				    		editable && (p != null && !p.isReadOnly()) ? "onClick=\"document.location='applicationConfig.do?op=edit&id=" + (reference == null ? key : key.replace("%", "<" + reference + ">")) + "';\"" : null,
				    		new String[] {
				    			"<a name='" + (reference == null ? key : key.replace("%", "<" + reference + ">")) + "'>" +
				    			(reference == null ? HtmlUtils.htmlEscape(key) : HtmlUtils.htmlEscape(key).replace("%", "<i><u>" + reference + "</i></u>")) +
				    			"</a>",
				    			"<font color='gray'>" + HtmlUtils.htmlEscape(value) + "</font>",
				    			(reference == null ? description : description.replace("%", "<i><u>" + reference + "</i></u>")) },
				    		new String[] {key, value, description}
				    		);
					continue;
				}
				
				if (o instanceof SessionConfig) {
					SessionConfig config = (SessionConfig)o;
					if (config.getDescription() != null && !config.getDescription().isEmpty())
						description = config.getDescription();

					webTable.addLine(
				    		editable && (p == null || !p.isReadOnly()) ? "onClick=\"document.location='applicationConfig.do?op=edit&id=" + key + "';\"" : null,
				    		new String[] {"<a name='"+key+"'>" + HtmlUtils.htmlEscape(key)  + " <sup><font color='#2066CE' title='Applies to " + config.getSession().getLabel() + "'>s)</font></sup></a>",
				    			HtmlUtils.htmlEscape(value), description},
				    		new String[] {key, value, description}
				    		);
				} else {
					ApplicationConfig config = (ApplicationConfig)o;
					if (config.getDescription() != null && !config.getDescription().isEmpty())
						description = config.getDescription();

				    webTable.addLine(
				    		editable && (p == null || !p.isReadOnly()) ? "onClick=\"document.location='applicationConfig.do?op=edit&id=" + key + "';\"" : null,
				    		new String[] {"<a name='"+key+"'>" + HtmlUtils.htmlEscape(key) + "</a>", HtmlUtils.htmlEscape(value), description},
				    		new String[] {key, value, description}
				    		);
				}
			}
	    }

	    request.setAttribute(ApplicationConfig.APP_CFG_ATTR_NAME, webTable.printTable(WebTable.getOrder(sessionContext,"applicationConfig.ord")));
    }
    
}
