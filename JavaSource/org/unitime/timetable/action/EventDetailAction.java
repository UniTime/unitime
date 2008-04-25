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

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EventDetailForm;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.StandardEventNote;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;


public class EventDetailAction extends Action {

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

		EventDetailForm myForm = (EventDetailForm) form;
		
		String iOp = myForm.getOp();
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
			
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}			
		
		if (iOp != null) {
		
			//return to event list
			if(iOp.equals("Back")) {
				if (myForm.getId()!=null)
					request.setAttribute("hash", "A"+myForm.getId());
				return mapping.findForward("showEventList");
			}

			if(iOp.equals("Previous")) {
				if (myForm.getPreviousId() != null)
					response.sendRedirect(response.encodeURL("eventDetail.do?id="+myForm.getPreviousId()));
				return null;
			}

			if(iOp.equals("Next")) {
				if (myForm.getPreviousId() != null)
					response.sendRedirect(response.encodeURL("eventDetail.do?id="+myForm.getNextId()));
				return null;
			}
			
		}

		
		if (request.getParameter("id")==null && myForm.getId()==null)
		    return mapping.findForward("showEventList");		
		
		
		if (request.getParameter("id")!=null) {
			String id = request.getParameter("id");
			Event event = new EventDAO().get(Long.valueOf(id));
			if (event!=null) {
				myForm.setEventName(event.getEventName()==null?"":event.getEventName());
				myForm.setMinCapacity(event.getMinCapacity()==null?"":event.getMinCapacity().toString());
				myForm.setMaxCapacity(event.getMaxCapacity()==null?"":event.getMaxCapacity().toString());
				myForm.setSponsoringOrg("N/A yet");
				for (Iterator i = event.getNotes().iterator(); i.hasNext();) {
					EventNote en = (EventNote) i.next();
					if (en.getTextNote()!= null) {myForm.addNote(en.getTextNote());}
				}
				for (Iterator i = event.getNotes().iterator(); i.hasNext();) {
					EventNote en2 = (EventNote) i.next();
					StandardEventNote sen = en2.getStandardNote();
					if (sen!=null) {myForm.addNote(sen.getNote());}
				}			
				if (event.getMainContact()!=null)
				    myForm.setMainContact(event.getMainContact());
				for (Iterator i = event.getAdditionalContacts().iterator(); i.hasNext();) {
					EventContact ec = (EventContact) i.next();
					myForm.addAdditionalContact(
							(ec.getFirstName()==null?"":ec.getFirstName()),
							(ec.getMiddleName()==null?"":ec.getMiddleName()),
							(ec.getLastName()==null?"":ec.getLastName()),
							(ec.getEmailAddress()==null?"":ec.getEmailAddress()),
							(ec.getPhone()==null?"":ec.getPhone()));
				}
				SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
				SimpleDateFormat dateFormatDay = new SimpleDateFormat("EEE", Locale.US);	
				SimpleDateFormat df2 = new SimpleDateFormat("MM/dd/yy", Locale.US);
				for (Iterator i=new TreeSet(event.getMeetings()).iterator();i.hasNext();) {
					Meeting meeting = (Meeting)i.next();
					int start = Constants.SLOT_LENGTH_MIN*meeting.getStartPeriod()+
								Constants.FIRST_SLOT_TIME_MIN+
								(meeting.getStartOffset()==null?0:meeting.getStartOffset());
					int startHour = start/60;
					int startMin = start%60;
					int end = Constants.SLOT_LENGTH_MIN*meeting.getStopPeriod()+
					Constants.FIRST_SLOT_TIME_MIN+
					(meeting.getStopOffset()==null?0:meeting.getStopOffset());
					int endHour = end/60;
					int endMin = end%60;
					String location = (meeting.getLocation()==null?"":meeting.getLocation().getLabel());
					String approvedDate = (meeting.getApprovedDate()==null?"":df2.format(meeting.getApprovedDate()));
					myForm.addMeeting(
							dateFormat.format(meeting.getMeetingDate())+"&nbsp;&nbsp;"+"<font color='gray'><i>("+dateFormatDay.format(meeting.getMeetingDate())+")</i></font>",
							(startHour>12?startHour-12:startHour)+":"+(startMin<10?"0":"")+startMin+(startHour>=12?"p":"a"),
							(endHour>12?endHour-12:endHour)+":"+(endMin<10?"0":"")+endMin+(endHour>=12?"p":"a"), 
							location,approvedDate);
				}
				myForm.setCanEdit(user.isAdmin()||user.hasRole(Roles.EVENT_MGR_ROLE)||user.getId().equals(event.getMainContact().getExternalUniqueId()));
				
		        Long nextId = Navigation.getNext(request.getSession(), Navigation.sInstructionalOfferingLevel, event.getUniqueId());
		        Long prevId = Navigation.getPrevious(request.getSession(), Navigation.sInstructionalOfferingLevel, event.getUniqueId());
		        myForm.setPreviousId(prevId==null?null:prevId.toString());
		        myForm.setNextId(nextId==null?null:nextId.toString());
			} else {
				myForm.setEventName("There is no event with this ID");
			}	
		}
		
        BackTracker.markForBack(
                request,
                "eventDetail.do?id=" + myForm.getId(),
                myForm.getEventName(),
                true, false);

		return mapping.findForward("show");
	}
	
}