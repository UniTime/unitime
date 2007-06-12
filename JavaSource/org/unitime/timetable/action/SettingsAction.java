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
import org.hibernate.criterion.Order;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.SettingsForm;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.dao.SettingsDAO;


/** 
 * MyEclipse Struts
 * Creation date: 10-17-2005
 * 
 * XDoclet definition:
 * @struts:action path="/settings" name="settingsForm" input="/admin/settings.jsp" scope="request"
 */
public class SettingsAction extends Action {

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
        
        // Read operation to be performed
        SettingsForm settingsForm = (SettingsForm) form;
        String op = settingsForm.getOp();
        if(op==null) {
            op = request.getParameter("op");
            if(op==null) {
	            settingsForm.setOp("Add New");
	            op = "list";
            }
        }
        
        // Reset Form
        if(op.equals("Clear")) {
            settingsForm.reset(mapping, request);
            settingsForm.setOp("Add New");
        }

        // Add / Update setting
        if(op.equals("Add New") || op.equals("Update")) {
            // Validate input
            ActionMessages errors = settingsForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                mapping.findForward("showSettings");
            }
            else {
                SettingsDAO sdao = new SettingsDAO();
                Settings s = null;

                if(op.equals("Add New"))
                    s = new Settings();
                else 
                    s = sdao.get(settingsForm.getUniqueId());
                
                s.setKey(settingsForm.getKey());
                s.setDefaultValue(settingsForm.getDefaultValue());                
                s.setAllowedValues(settingsForm.getAllowedValues());                
                s.setDescription(settingsForm.getDescription());                
                sdao.saveOrUpdate(s);
                
                settingsForm.reset(mapping, request);
                settingsForm.setOp("Add New");
            }
        }

        // Edit Setting - Load existing setting values to be edited
        if(op.equals("Edit")) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
                mapping.findForward("showSettings");
            }
            else {
                SettingsDAO sdao = new SettingsDAO();
                Settings s = sdao.get(new Long(id));
                if(s==null) {
                    errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
                    mapping.findForward("showSettings");
                }
                else {
                    settingsForm.setUniqueId(s.getUniqueId());
                    settingsForm.setKey(s.getKey());
                    settingsForm.setDefaultValue(s.getDefaultValue());
                    settingsForm.setAllowedValues(s.getAllowedValues());
                    settingsForm.setDescription(s.getDescription());
                    settingsForm.setOp("Update");
                }                
            }
        }

        // Delete Setting 
        if(op.equals("Delete")) {
            SettingsDAO sdao = new SettingsDAO();
            sdao.delete(settingsForm.getUniqueId());
            settingsForm.reset(mapping, request);
            settingsForm.setOp("Add New");
        }

        // Read all existing settings and store in request
        getSettingsList(request);        
        return mapping.findForward("showSettings");
    }

    /**
     * Retrieve all existing defined settings
     * @param request Request object
     * @throws Exception
     */
    private void getSettingsList(HttpServletRequest request) throws Exception {
		org.hibernate.Session hibSession = null;

		// Create web table instance 
        WebTable webTable = new WebTable( 4,
			    "Settings List",
			    new String[] {"Key", "Description", "Default Value", "Allowed Values"},
			    new String[] {"left", "left", "left", "left"},
			    null );

        try {
            SettingsDAO sDao = new SettingsDAO();
			hibSession = sDao.getSession();
            
			List settingsList = hibSession.createCriteria(Settings.class)
			.addOrder(Order.asc("key"))
			.list();
			
			if(settingsList.size()==0) {
			    webTable.addLine(
			        	null, new String[] {"No user settings found"}, null, null );			    
			    
			}
			else {
				Iterator iterSettings = settingsList.iterator();
				while(iterSettings.hasNext()) {
				    Settings s = (Settings) iterSettings.next();
				    String key = s.getKey();
				    String defaultValue = s.getDefaultValue();
				    String allowedValues = s.getAllowedValues();
				    String description = s.getDescription();
				    
				    String onClick = "onClick=\"document.location='settings.do?op=Edit&id="
	    				+ s.getUniqueId() + "';\"";
				    
				    webTable.addLine(
				        	onClick, new String[] {key, description, defaultValue, allowedValues}, null, null );			    
				}
			}
			
	    }
	    catch (Exception e) {
	        throw new Exception(e);
	    }
	    finally {
	    }

	    request.setAttribute(Settings.SETTINGS_ATTR_NAME, webTable.printTable());
    }
}