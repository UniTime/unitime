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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EventDetailForm;
import org.unitime.timetable.form.EventDetailForm.MeetingBean;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


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
		
		if (request.getParameter("id")!=null) {
			String id = request.getParameter("id");
			Event event = new EventDAO().get(Long.valueOf(id));
			myForm.setEventName(event.getEventName());
			myForm.setMinCapacity(event.getMinCapacity());
			myForm.setMaxCapacity(event.getMaxCapacity());
			myForm.setSponsoringOrg("N/A yet");
			for (Iterator i = event.getNotes().iterator(); i.hasNext();) {
				EventNote en = (EventNote) i.next();
				myForm.addNote(en.getTextNote());
			}
			myForm.setMainContact(event.getMainContact());
			for (Iterator i = event.getAdditionalContacts().iterator(); i.hasNext();) {
				EventContact ec = (EventContact) i.next();
				myForm.addAdditionalContact(ec.getFirstName(), ec.getMiddleName(), ec.getLastName(), ec.getEmailAddress(), ec.getPhone());
			}
			SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy (EEE)");			
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
				myForm.addMeeting(df.format(meeting.getMeetingDate()),
						(startHour>12?startHour-12:startHour)+":"+(startMin<10?"0":"")+startMin+(startHour>=12?"p":"a"),
						(endHour>12?endHour-12:endHour)+":"+(endMin<10?"0":"")+endMin+(endHour>=12?"p":"a"), 
						"guess where");
			}
		} else {
			myForm.setEventName("WON'T TELL A THING ABOUT SUCH EVENT");
		}			
/**			myForm.setEventName("an Event Name");
			myForm.setMinCapacity(20);
			myForm.setMaxCapacity(30);
			myForm.setSponsoringOrg("Spejbl and Hurvinek, LLC");
			myForm.setAdditionalInfo("some additional info");

			EventContact ec = new EventContact (10101010101l, "Hurvinek", "Obecny");
			myForm.setMainContact(ec);
			myForm.addMeeting("08/01/01", "8:30", "10:30", "UNIV 103");
			myForm.addMeeting("08/01/02", "9:00", "10:00", "REC 313");

			SimpleDateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy hh:mmaa");
			myForm.addMeeting(df.format(new Date()), "6:00", "4:00", "LILY 3114");
		}	
*/
		return mapping.findForward("show");
	}
	
}