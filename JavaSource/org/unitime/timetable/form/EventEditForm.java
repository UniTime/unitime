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

package org.unitime.timetable.form;

import java.util.Date;
import java.util.HashSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.Event.MultiMeeting;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.webutil.EventEmail;


/**
 * @author Zuzana Mullerova
 */

public class EventEditForm extends EventAddInfoForm {

	private Long iId;
	
	public Long getId() {return iId;}
	public void setId(Long id) {iId = id;}
	
	public void load (HttpServletRequest request) {
		
		iId = Long.valueOf(request.getParameter("id"));
		HttpSession session = request.getSession();
		User user = Web.getUser(request.getSession());
		setEvent(EventDAO.getInstance().get(iId));
		setEventName(getEvent().getEventName());
		setEventType(getEvent().getEventTypeLabel());
		setMainContactFirstName(getEvent().getMainContact().getFirstName());
		setMainContactMiddleName(getEvent().getMainContact().getMiddleName());
		setMainContactLastName(getEvent().getMainContact().getLastName());
		setMainContactExternalId(getEvent().getMainContact().getExternalUniqueId());
		setMainContactEmail(getEvent().getMainContact().getEmailAddress());
		setMainContactPhone(getEvent().getMainContact().getPhone());
		setAdditionalEmails(getEvent().getEmail());
		if ("Special Event".equals(getEventType())) {
			setSponsoringOrgName((getEvent().getSponsoringOrganization()==null?"":getEvent().getSponsoringOrganization().getName()));
			setSponsoringOrgId((getEvent().getSponsoringOrganization()==null?-1:getEvent().getSponsoringOrganization().getUniqueId()));
		}
		if ("Course Event".equals(getEventType())) setAttendanceRequired(((CourseEvent) getEvent()).isReqAttendance());
		loadExistingMeetings();		

		TimetableManager tm = TimetableManager.getManager(user);
	
	}
	
	public void update(HttpServletRequest request) {
		HttpSession session = request.getSession();
		Transaction tx = null;
		try {
			Session hibSession = new _RootDAO().getSession();
			tx = hibSession.beginTransaction();

			// create event
			Event event = getEvent();//getEvent();
		    EventContact mainContact = event.getMainContact();
			if (getMainContactExternalId()!=null) mainContact = EventContact.findByExternalUniqueId(getMainContactExternalId());
			if (mainContact==null) mainContact = EventContact.findByEmail(getMainContactEmail()); 
			if (mainContact==null) mainContact = new EventContact();
			if (getMainContactFirstName()!=null && getMainContactFirstName().length()>0) 
				mainContact.setFirstName(getMainContactFirstName());
            mainContact.setMiddleName(getMainContactMiddleName());
			if (getMainContactLastName()!=null && getMainContactLastName().length()>0)
				mainContact.setLastName(getMainContactLastName());
			if (getMainContactEmail()!=null && getMainContactEmail().length()>0)
				mainContact.setEmailAddress(getMainContactEmail());
			if (getMainContactPhone()!=null && getMainContactPhone().length()>0)
				mainContact.setPhone(getMainContactPhone());
            if (getMainContactExternalId()!=null && getMainContactExternalId().length()>0)
                mainContact.setExternalUniqueId(getMainContactExternalId());
            hibSession.saveOrUpdate(mainContact);
           
            event.setEventName(getEventName());
			event.setMainContact(mainContact);
			// add additional emails
			if (getAdditionalEmails()!=null && getAdditionalEmails().length()>0) {
				event.setEmail(getAdditionalEmails());
			}
			hibSession.saveOrUpdate(event);

			if ("Special Event".equals(getEventType())) {
				if (getSponsoringOrgId()!=null) {
				    SponsoringOrganization spor = SponsoringOrganizationDAO.getInstance().get(getSponsoringOrgId());
				    event.setSponsoringOrganization(spor);
				}
			}			
			
			User user = Web.getUser(request.getSession());
			String uname = event.getMainContact().getShortName();
	        if (user!=null && (user.isAdmin() || Roles.EVENT_MGR_ROLE.equals(user.getRole()))) {
	            TimetableManager mgr = TimetableManager.getManager(user);
	            if (mgr!=null) uname = mgr.getShortName();
	        }
	        if (uname==null) uname = user.getName();
	        
            // add event note (additional info)
            EventNote en = new EventNote();
            en.setNoteType(EventNote.sEventNoteTypeEditEvent);
            en.setTimeStamp(new Date());
            en.setTextNote(getAdditionalInfo());
            en.setUser(uname);
            en.setEvent(event);
            hibSession.saveOrUpdate(en);
            // attach the note to event
            if (event.getNotes()==null) event.setNotes(new HashSet());
            event.getNotes().add(en);

			hibSession.saveOrUpdate(event);
			
            new EventEmail(event, EventEmail.sActionUpdate, new TreeSet<MultiMeeting>(), getAdditionalInfo()).send(request);
			
			ChangeLog.addChange(
                    hibSession,
                    request,
                    event,
                    ChangeLog.Source.EVENT_EDIT,
                    ChangeLog.Operation.UPDATE,
                    null,null);
			
			tx.commit();
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			e.printStackTrace();
		}
	}
	
}
