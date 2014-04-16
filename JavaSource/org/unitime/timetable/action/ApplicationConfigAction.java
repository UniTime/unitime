/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import org.unitime.commons.web.WebTable;
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
                        errors.add("key", new ActionMessage("errors.invalid", "Name : " + id));
                        saveErrors(request, errors);
            		} else {
                        frm.setKey(appConfig.getKey());
                        frm.setValue(appConfig.getValue());
                        frm.setDescription(appConfig.getDescription());
                        frm.setAllSessions(true);
            		}
            	} else {
            		frm.setKey(sessionConfig.getKey());
                    frm.setValue(sessionConfig.getValue());
                    frm.setDescription(sessionConfig.getDescription());
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
            frm.reset(mapping, request);
        }

        if ("list".equals(frm.getOp())) {
            //Read all existing ApplicationConfig and store in request
            getApplicationConfigList(request);        
            return mapping.findForward("list");
        }
        
        return mapping.findForward(rsc.getMessage("button.createAppConfig").equals(frm.getOp())?"add":"edit");
    }

    /**
     * Retrieve all existing defined configs
     * @param request Request object
     * @throws Exception
     */
    private void getApplicationConfigList(HttpServletRequest request) throws Exception {
        WebTable.setOrder(sessionContext,"applicationConfig.ord",request.getParameter("ord"),1);
		
		// Create web table instance 
        WebTable webTable = new WebTable( 4,
			    null, "applicationConfig.do?ord=%%",
			    new String[] {"Name", "Value", "Description"},
			    new String[] {"left", "left", "left"},
			    null );
        webTable.enableHR("#9CB0CE");
        
		Map<String, Object> properties = new HashMap<String, Object>();
		for (ApplicationConfig config: ApplicationConfigDAO.getInstance().findAll()) {
			properties.put(config.getKey(), config);
		}
		if (sessionContext.getUser().getCurrentAcademicSessionId() != null) {
			for (SessionConfig config: SessionConfig.findAll(sessionContext.getUser().getCurrentAcademicSessionId())) {
				properties.put(config.getKey(), config);
			}
		}

		boolean editable = sessionContext.hasPermission(Right.ApplicationConfigEdit);
		if (properties.isEmpty()) {
			webTable.addLine(null, new String[] {"No configuration keys found"}, null, null);
		} else {
			for (String key: new TreeSet<String>(properties.keySet())) {
				Object o = properties.get(key);
				if (o instanceof SessionConfig) {
					SessionConfig config = (SessionConfig)o;
				    webTable.addLine(
				    		editable ? "onClick=\"document.location='applicationConfig.do?op=edit&id=" + config.getKey() + "';\"" : null,
				    		new String[] {config.getKey()  + " <sup><font color='#2066CE' title='Applies to " + config.getSession().getLabel() + "'>s)</font></sup>",
				    			config.getValue() == null ? "" : config.getValue(), config.getDescription() == null ? "" : config.getDescription()},
				    		new String[] {config.getKey(), config.getValue() == null ? "" : config.getValue(), config.getDescription() == null ? "" : config.getDescription()}
				    		);
				} else {
					ApplicationConfig config = (ApplicationConfig)o;
				    webTable.addLine(
				    		editable ? "onClick=\"document.location='applicationConfig.do?op=edit&id=" + config.getKey() + "';\"" : null,
				    		new String[] {config.getKey(), config.getValue() == null ? "" : config.getValue(), config.getDescription() == null ? "" : config.getDescription()},
				    		new String[] {config.getKey(), config.getValue() == null ? "" : config.getValue(), config.getDescription() == null ? "" : config.getDescription()}
				    		);
				}
			}
	    }

	    request.setAttribute(ApplicationConfig.APP_CFG_ATTR_NAME, webTable.printTable(WebTable.getOrder(sessionContext,"applicationConfig.ord")));
    }
    
}
