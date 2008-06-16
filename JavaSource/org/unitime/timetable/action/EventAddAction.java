/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org, and individual contributors
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

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EventAddForm;
import org.unitime.timetable.model.TimetableManager;

/**
 * @author Zuzana Mullerova
 */
public class EventAddAction extends Action {

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

//Collect initial info
		EventAddForm myForm = (EventAddForm) form;
		User user = Web.getUser(request.getSession());

/*        EventModel model = (EventModel)request.getSession().getAttribute("Event.model");
        if (model==null) {
            model = new EventModel();
            request.getSession().setAttribute("Event.model", model);
        }
*/		

       
//Verification of user being logged in
		if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }		

//Operations
		String iOp = myForm.getOp();

		if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
			iOp = request.getParameter("op2");
		
		// if user is returning from the Event Room Availability screen, 
		// load the parameters he/she entered before
		if ("eventRoomAvailability".equals(request.getAttribute("back"))) {
			myForm.load(request.getSession());
			iOp = null;
		}
		
		if (iOp!=null && !"SessionChanged".equals(iOp)) {
			myForm.loadDates(request);
		}
		
//		if ("EventTypeChanged".equals(iOp)) {
//			
//		}
			
        if ("Add Object".equals(iOp)) {
            for (int i=0; i<myForm.PREF_ROWS_ADDED; i++) {
                myForm.addRelatedCourseInfo(null);
            }
            request.setAttribute("hash", "objects");
        }
		
        if ("Delete".equals(iOp)) {
	        if (myForm.getSelected() >= 0) {
	            myForm.deleteRelatedCourseInfo(myForm.getSelected());
	        }
        }
        
        if ("Show Scheduled Events".equals(iOp)) {
        	ActionMessages errors = myForm.validate(mapping, request);
        	if (!errors.isEmpty()) {
        		saveErrors(request, errors);
        	} else {
        		System.out.println("Event dates:"+myForm.getMeetingDates());
        		myForm.save(request.getSession());
        	}
        }

        if ("Show Availability".equals(iOp)) {
        	ActionMessages errors = myForm.validate(mapping, request);
        	if (!errors.isEmpty()) {
        		saveErrors(request, errors);
        	} else {
        		myForm.save(request.getSession());
        		return mapping.findForward("showEventRoomAvailability");
        	}
        }    
        
        if ("Back".equals(iOp)) {
        	return mapping.findForward("back");
        }
		
        
        myForm.setSubjectAreas(TimetableManager.getSubjectAreas(user));
        
//test:        System.out.println(">>> "+op+" <<<");


//set the model        
//        myForm.setModel(model);
//        model.apply(request, myForm);
        
        
//Display the page        
        return mapping.findForward("show");
	}
}

