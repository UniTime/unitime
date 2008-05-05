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
package org.unitime.timetable.action;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.criterion.Order;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ApplicationConfigForm;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.dao.ApplicationConfigDAO;


/** 
 * MyEclipse Struts
 * Creation date: 08-28-2006
 * 
 * XDoclet definition:
 * @struts:action path="/applicationConfig" name="applicationConfigForm" input="/admin/applicationConfig.jsp" scope="request"
 */
public class ApplicationConfigAction extends Action {

    // --------------------------------------------------------- Instance Variables

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
        
        // Check Access
        if(!Web.isLoggedIn( request.getSession() )
               || !Web.hasRole(request.getSession(), Roles.getAdminRoles()) ) {
            throw new Exception ("Access Denied.");
        }
        
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
            String id = request.getParameter("id");
            if(id==null || id.trim().length()==0) {
                errors.add("key", new ActionMessage("errors.invalid", "Name : " + id));
                saveErrors(request, errors);
            } else {
                ApplicationConfigDAO sdao = new ApplicationConfigDAO();
                ApplicationConfig s = sdao.get(id);
                if(s==null) {
                    errors.add("key", new ActionMessage("errors.invalid", "Name : " + id));
                    saveErrors(request, errors);
                } else {
                    frm.setKey(s.getKey());
                    frm.setValue(s.getValue());
                    frm.setDescription(s.getDescription());
                }
            }
        }
        
        if (op.equals(rsc.getMessage("button.addAppConfig"))) {
            frm.reset(mapping, request);
            frm.setOp(rsc.getMessage("button.createAppConfig"));
        }

        // Update config
        if(op.equals(rsc.getMessage("button.updateAppConfig"))) {
            // Validate input
            errors = frm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
            } else {

                try {
                    ApplicationConfigDAO sdao = new ApplicationConfigDAO();
                    
	                ApplicationConfig s = sdao.get(frm.getKey());
	                s.setValue(frm.getValue());                
	                s.setDescription(frm.getDescription());                
	                sdao.saveOrUpdate(s);
	                ApplicationProperties.getConfigProperties().put(frm.getKey(), frm.getValue());
	                frm.reset(mapping, request);
                }
                catch (Exception e) {
                    errors.add("key", new ActionMessage("errors.generic", e.getMessage()));
                    saveErrors(request, errors);
                }
            }
        }

        // Delete config
        if(op.equals(rsc.getMessage("button.deleteAppConfig"))) {
            // Validate input
            errors = frm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
            } else {
                
                try {
                    ApplicationConfigDAO sdao = new ApplicationConfigDAO();
                    
                    sdao.delete(frm.getKey());
                    ApplicationProperties.getConfigProperties().remove(frm.getKey());
                    
                    frm.reset(mapping, request);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    errors.add("key", new ActionMessage("errors.generic", e.getMessage()));
                    saveErrors(request, errors);
                }
            }
        }

        // Create config
        if(op.equals(rsc.getMessage("button.createAppConfig"))) {
            // Validate input
            errors = frm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
            } else {
                
                try {
                    ApplicationConfigDAO sdao = new ApplicationConfigDAO();
                    
                    if (sdao.get(frm.getKey())!=null)
                        throw new Exception("A property with this name already exists.");
                    
                    ApplicationConfig s = new ApplicationConfig();
                    s.setKey(frm.getKey());
                    s.setValue(frm.getValue());                
                    s.setDescription(frm.getDescription());                
                    sdao.saveOrUpdate(s);
                    ApplicationProperties.getConfigProperties().put(frm.getKey(), frm.getValue());
                    frm.reset(mapping, request);
                }
                catch (Exception e) {
                    errors.add("key", new ActionMessage("errors.generic", e.getMessage()));
                    saveErrors(request, errors);
                }
            }
        }
        
        // Create config
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
        WebTable.setOrder(request.getSession(),"applicationConfig.ord",request.getParameter("ord"),1);
		org.hibernate.Session hibSession = null;
        MessageResources rsc = getResources(request);

		// Create web table instance 
        WebTable webTable = new WebTable( 3,
			    null, "applicationConfig.do?ord=%%",
			    new String[] {"Name", "Value", "Description"},
			    new String[] {"left", "left", "left"},
			    null );
        webTable.enableHR("#EFEFEF");
        
        try {
            ApplicationConfigDAO sDao = new ApplicationConfigDAO();
			hibSession = sDao.getSession();
            
			List configList = hibSession.createCriteria(ApplicationConfig.class)
			.addOrder(Order.asc("key"))
			.list();
			
			if(configList.size()==0) {
			    webTable.addLine(
			        	null, new String[] {"No configuration keys found"}, null, null );			    
			    
			}
			else {
				Iterator iterConfigs = configList.iterator();
				while(iterConfigs.hasNext()) {
				    ApplicationConfig s = (ApplicationConfig) iterConfigs.next();
				    String key = s.getKey();
				    String value = s.getValue();
				    String description = s.getDescription();
				    
				    String onClick = "onClick=\"document.location='applicationConfig.do?op=edit&id="
	    				+ s.getKey() + "';\"";
				    
				    webTable.addLine(
				        	onClick, new String[] {key, value, description}, new String[] {key, value, description}, null );			    
				}
			}
			
	    }
	    catch (Exception e) {
	        throw new Exception(e);
	    }
	    finally {
	    }

	    request.setAttribute(ApplicationConfig.APP_CFG_ATTR_NAME, webTable.printTable(WebTable.getOrder(request.getSession(),"applicationConfig.ord")));
    }
    
}