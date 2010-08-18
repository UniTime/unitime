/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.ManagerSettingsForm;
import org.unitime.timetable.model.ManagerSettings;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ManagerSettingsDAO;
import org.unitime.timetable.model.dao.SettingsDAO;


/** 
 * MyEclipse Struts
 * Creation date: 10-17-2005
 * 
 * XDoclet definition:
 * @struts:action path="/managerSettings" name="managerSettingsForm" input="/user/managerSettings.jsp" scope="request"
 */
public class ManagerSettingsAction extends Action {

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
        if(!Web.isLoggedIn( request.getSession() )) 
            throw new Exception ("Access Denied.");
        
		MessageResources rsc = getResources(request);
	    HttpSession session = request.getSession();
	    User user = Web.getUser(session);
        ManagerSettingsForm frm = (ManagerSettingsForm) form;
        String op = frm.getOp();
 
        if(op==null) {
            op = request.getParameter("op");
            if(op==null) {
                frm.setOp("List");
	            op = "List";
            }
        }
        
        // Reset Form
        if(op.equals(rsc.getMessage("button.cancelUpdateSetting"))) {
            frm.reset(mapping, request);
            frm.setOp("List");
        }

        // Edit - Load setting with allowed values for user to update
        if(op.equals("Edit")) {
            // Read parameters
            String sKeyId = request.getParameter("kid"); 
            String sSettingId = request.getParameter("sid");
            Long keyId = new Long(sKeyId);
            Long settingId = new Long(sSettingId);
            
            // Load Settings object
            SettingsDAO sdao = new SettingsDAO();
            Settings s = sdao.get(keyId);

            // Set Form values
            frm.setOp("Edit");            
            frm.setKeyId(keyId);
            frm.setSettingId(settingId);
            frm.setAllowedValues(s.getAllowedValues());
            frm.setKey(s.getDescription());

            // Setting value was taken from the default value
            if(settingId.intValue()<0)
                frm.setValue(s.getDefaultValue());
            
            // User had modified settings earlier
            else {
                Set uSettings = Settings.getSettings(user);
			    String[] data = Settings.getSettingValue(user.getRole(), uSettings, keyId, s.getDefaultValue());
			    frm.setValue(data[1]);
            }
            return mapping.findForward("editManagerSettings");
        }
 
        // Save changes made by the user
        if(op.equals(rsc.getMessage("button.updateSetting"))) {
            ActionMessages errors = frm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                frm.setOp("Edit");
            }
            else {
            	Long keyId = frm.getKeyId(); 
	            Long settingId = frm.getSettingId();
	            String value = frm.getValue();
	            
	            
            	// No user defined setting but inherited from default setting
	            if(settingId.intValue()==-1) {

	                TimetableManager mgr = TimetableManager.findByExternalId(user.getId());
	                
	                SettingsDAO sDao = new SettingsDAO();
	                Settings s = sDao.get(keyId);
	                
	    	        Session hibSession = sDao.getSession();
	    	        Transaction tx = hibSession.beginTransaction();

	    	        ManagerSettings mgrSettings = new ManagerSettings();
	    	        mgrSettings.setKey(s);
	    	        mgrSettings.setManager(mgr);
	    	        mgrSettings.setValue(value);

	    	        mgr.getSettings().add(mgrSettings);
	    	        
	    	        hibSession.saveOrUpdate(mgr);
	    	        hibSession.saveOrUpdate(mgrSettings);
	    	        
	    	        tx.commit();
	    	        hibSession.flush();
	    	        hibSession.clear();
	    	        hibSession.refresh(mgr);
	    	        
	            }
	            
	            // Update user setting
	            else {
	                ManagerSettingsDAO mgrDao = new ManagerSettingsDAO();
	    	        Session hibSession = mgrDao.getSession();
	    	        Transaction tx = hibSession.beginTransaction();
	                
	                ManagerSettings mgrSettings =  mgrDao.get(settingId);
	                TimetableManager mgr = mgrSettings.getManager();
	                mgrSettings.setValue(value);
	                hibSession.saveOrUpdate(mgrSettings);
	    	        hibSession.saveOrUpdate(mgr);
	    	        
	    	        tx.commit();
	    	        hibSession.flush();
	    	        hibSession.clear();
	    	        hibSession.refresh(mgr);
	            }		            
    	    }
        }

        // Read all existing settings and store in request
        getSettingsList(request);        
        return mapping.findForward("showManagerSettings");
    }

    /**
     * Retrieve all existing defined settings
     * @param request Request object
     * @throws Exception
     */
    private void getSettingsList(HttpServletRequest request) throws Exception {
        WebTable.setOrder(request.getSession(),"managerSettings.ord",request.getParameter("ord"),1);
		org.hibernate.Session hibSession = null;

		// Create web table instance 
        WebTable webTable = new WebTable( 2,
			    "Manager Settings", "managerSettings.do?ord=%%",
			    new String[] {"Setting", "Value"},
			    new String[] {"left", "left"},
			    null );

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
		    HttpSession session = request.getSession();
		    User user = Web.getUser(session);
            Set uSettings = Settings.getSettings(user);
			Iterator iterSettings = settingsList.iterator();

			while(iterSettings.hasNext()) {
			    Settings s = (Settings) iterSettings.next();
			    Long uniqueId = s.getUniqueId();
			    String key = s.getDescription();
			    String defaultValue = s.getDefaultValue();
			    
			    String[] data = Settings.getSettingValue(user.getRole(), uSettings, uniqueId, defaultValue);
			    String onClick = "onClick=\"document.location='managerSettings.do?op=Edit&kid="
    				+ s.getUniqueId() + "&sid=" + data[0] + "';\"";
			    
			    webTable.addLine(
			        	onClick, new String[] {key, data[1]}, new String[] {key, data[1]} );			    
			}
		}

	    request.setAttribute(Settings.SETTINGS_ATTR_NAME, webTable.printTable(WebTable.getOrder(request.getSession(),"managerSettings.ord")));
    }   
    
}
