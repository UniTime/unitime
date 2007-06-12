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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.RollForwardSessionForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.SessionRollForward;


/** 
 * MyEclipse Struts
 * Creation date: 02-27-2007
 * 
 * XDoclet definition:
 * @struts.action path="/exportSessionToMsf" name="exportSessionToMsfForm" input="/form/exportSessionToMsf.jsp" scope="request" validate="true"
 */
public class RollForwardSessionAction extends Action {
	/*
	 * Generated Methods
	 */

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws Exception 
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response) throws Exception {
	    HttpSession webSession = request.getSession();
        if(!Web.isLoggedIn( webSession )) {
            throw new Exception ("Access Denied.");
        }
        MessageResources rsc = getResources(request);
        
        RollForwardSessionForm rollForwardSessionForm = (RollForwardSessionForm) form;
		HttpSession httpSession = request.getSession();
		User user = Web.getUser(request.getSession());
        // Get operation
        String op = request.getParameter("op");		  
        
        SessionRollForward sessionRollForward = new SessionRollForward();
        			               
   
        if (op != null && op.equals(rsc.getMessage("button.rollForward"))) {
            ActionMessages errors = rollForwardSessionForm.validate(mapping, request);

            if(errors.size() == 0 && rollForwardSessionForm.getRollForwardDatePatterns().booleanValue()){
	        	sessionRollForward.rollDatePatternsForward(errors, rollForwardSessionForm);
	        }
            if(errors.size() == 0 && rollForwardSessionForm.getRollForwardTimePatterns().booleanValue()){
	        	sessionRollForward.rollTimePatternsForward(errors, rollForwardSessionForm);
	        }
        	if (errors.size() == 0 && rollForwardSessionForm.getRollForwardDepartments().booleanValue()){
	        	sessionRollForward.rollDepartmentsForward(errors, rollForwardSessionForm);	
	        }
        	if (errors.size() == 0 && rollForwardSessionForm.getRollForwardManagers().booleanValue()){
        		sessionRollForward.rollManagersForward(errors, rollForwardSessionForm);
        	}
        	if (errors.size() == 0 && rollForwardSessionForm.getRollForwardRoomData().booleanValue()){
        		sessionRollForward.rollBuildingAndRoomDataForward(errors, rollForwardSessionForm);
        	}
        	if (errors.size() == 0 && rollForwardSessionForm.getRollForwardSubjectAreas().booleanValue()){
        		sessionRollForward.rollSubjectAreasForward(errors, rollForwardSessionForm);
        	}
        	if (errors.size() == 0 && rollForwardSessionForm.getRollForwardInstructorData().booleanValue()){
        		sessionRollForward.rollInstructorDataForward(errors, rollForwardSessionForm);
        	}
        	if (errors.size() == 0 && rollForwardSessionForm.getRollForwardCourseOfferings().booleanValue()){
        		sessionRollForward.rollCourseOfferingsForward(errors, rollForwardSessionForm);
        	}
            if (errors.size() != 0) {
                saveErrors(request, errors);
            }

        }            
		rollForwardSessionForm.setAdmin(user.isAdmin());
		rollForwardSessionForm.setSubjectAreas(Session.getCurrentAcadSession(user).getSubjectAreas());
		rollForwardSessionForm.setSessions(Session.getAllSessions());
  		return mapping.findForward("displayRollForwardSessionForm");        

	}

}