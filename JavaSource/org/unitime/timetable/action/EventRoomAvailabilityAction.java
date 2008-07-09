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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EventRoomAvailabilityForm;

/**
 * @author Zuzana Mullerova
 */
public class EventRoomAvailabilityAction extends Action {

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

		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
			
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}					
		
		EventRoomAvailabilityForm myForm = (EventRoomAvailabilityForm) form;
		try { 
			myForm.load(webSession);
		} catch (Exception e) {
			ActionErrors m = new ActionErrors();
			m.add("dates", new ActionMessage("errors.generic", e.getMessage()));
			saveMessages(request, m);
			return  mapping.findForward("show");
		}

		String iOp = myForm.getOp();
		
		if (iOp != null) {
			
			// if the user is returning from the Event Add Info screen
			if ("eventAddInfo".equals(request.getAttribute("back"))) {
				myForm.load(request.getSession());
				iOp = null;
			}
			
			//return to event list
			if("Change Request".equals(iOp)) {
				myForm.loadData(request); myForm.save(webSession);
				request.setAttribute("back", "eventRoomAvailability");
				return mapping.findForward("back");
			}
			
			if("Continue".equals(iOp)) {
				myForm.loadData(request); myForm.save(webSession);	
				if (myForm.getIsAddMeetings()) return mapping.findForward("eventUpdateMeetings");
				else return mapping.findForward("eventAddInfo");
			}
			
		}
		
		
		
		return  mapping.findForward("show");
	}
		
	
}
