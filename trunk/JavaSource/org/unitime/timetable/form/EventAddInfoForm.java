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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.EventDetailForm.ContactBean;
import org.unitime.timetable.form.EventDetailForm.MeetingBean;
import org.unitime.timetable.form.EventRoomAvailabilityForm.DateLocation;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SpecialEvent;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.SponsoringOrganizationDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;
import org.unitime.timetable.util.IdValue;
import org.unitime.timetable.webutil.EventEmail;
import org.unitime.timetable.webutil.WebTextValidation;

public class EventAddInfoForm extends ActionForm {

	private TreeSet<DateLocation> iDateLocations = new TreeSet();
	private int iStartTime;
	private int iStopTime;
	private String iOp;
	private String iStartTimeString;
	private String iStopTimeString;
	private String iEventName;
	private ContactBean iMainContact;
	private String iAdditionalEmails;
	private String iAdditionalInfo;
	private String iMainContactEmail;
	private String iMainContactPhone;
	private String iMainContactFirstName;
	private String iMainContactMiddleName;
	private String iMainContactLastName;
	private String iMainContactExternalId;
	private boolean iMainContactLookup;
	private String iEventType;
	private List iSubjectArea;
	private Collection iSubjectAreas;
	private List iCourseNbr;
	private List iItype;
	private List iClassNumber;
	private boolean iAttendanceRequired;
	private Long iSponsoringOrgId;
	//if adding meetings to an existing event
	private String iSponsoringOrgName;
	private Long iEventId; 
	private boolean iIsAddMeetings; 
	private Event iEvent;
	private Vector<MeetingBean> iExistingMeetings = new Vector<MeetingBean>();
	private Vector<MeetingBean> iNewMeetings = new Vector<MeetingBean>();
	private String iCapacity;
		
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();

		if (iEventName==null || iEventName.length()==0) {
			errors.add("eventName", new ActionMessage("errors.generic", "The event name is mandatory."));
		}
		if (iEventName !=null && iEventName.length() > 100) {
			errors.add("eventName", new ActionMessage("errors.generic", "The event name cannot exceed 100 characters."));
		}
		if (!WebTextValidation.isTextValid(iEventName, false)){
			iEventName = "";
			errors.add("eventName", new ActionMessage("errors.invalidCharacters", "Event Name"));
		}

		if (iMainContactEmail==null || iMainContactEmail.length()==0) {
			errors.add("mcEmail", new ActionMessage("errors.generic", "The contact email is mandatory."));
		}
		if (iMainContactEmail !=null && iMainContactEmail.length() > 200) {
			errors.add("mcEmail", new ActionMessage("errors.generic", "The contact's email cannot exceed 200 characters."));
		}
		if (!WebTextValidation.isTextValid(iMainContactEmail, false)){
			iMainContactEmail = "";
			errors.add("mcEmail", new ActionMessage("errors.invalidCharacters", "Email"));
		}

		if (iMainContactLastName==null || iMainContactLastName.length()==0) {
			errors.add("mcLastName", new ActionMessage("errors.generic", "The contact's last name is mandatory."));
		}
		if (iMainContactLastName !=null && iMainContactLastName.length() > 30) {
			errors.add("mcLastName", new ActionMessage("errors.generic", "The contact's last name cannot exceed 30 characters."));
		}
		if (!WebTextValidation.isTextValid(iMainContactLastName, false)){
			iMainContactLastName = "";
			errors.add("mcLastName", new ActionMessage("errors.invalidCharacters", "Last Name"));
		}

		if (iMainContactFirstName !=null && iMainContactFirstName.length() > 30) {
			errors.add("mcFirstName", new ActionMessage("errors.generic", "The contact's first name cannot exceed 20 characters."));
		}
		if (!WebTextValidation.isTextValid(iMainContactFirstName, true)){
			iMainContactFirstName = "";
			errors.add("mcFirstName", new ActionMessage("errors.invalidCharacters", "First Name"));
		}

		if (iMainContactMiddleName !=null && iMainContactMiddleName.length() > 30) {
			errors.add("mcMiddleName", new ActionMessage("errors.generic", "The contact's middle name cannot exceed 20 characters."));
		}
		if (!WebTextValidation.isTextValid(iMainContactMiddleName, true)){
			iMainContactMiddleName = "";
			errors.add("mcMiddleName", new ActionMessage("errors.invalidCharacters", "Middle Name"));
		}

		if (iMainContactPhone !=null && iMainContactPhone.length() > 25) {
			errors.add("mcPhone", new ActionMessage("errors.generic", "The contact's phone cannot exceed 25 characters."));
		}
		if (!WebTextValidation.containsOnlyCharactersUsedInPhoneNumbers(iMainContactPhone, true)){
			iMainContactPhone = "";
			errors.add("mcPhone", new ActionMessage("errors.generic", "Invalid data in event main contact phone"));
		}

		if (iAdditionalEmails!=null && iAdditionalEmails.length()>999) {
			errors.add("emails", new ActionMessage("errors.generic", "Additional emails are too long. Please, limit the field to no more than 1000 characters."));
		}

		if (!WebTextValidation.isTextValid(iAdditionalEmails, true)){
			iAdditionalEmails = "";
			errors.add("emails", new ActionMessage("errors.invalidCharacters", "Additional Emails"));
		}
		
		if (iAdditionalInfo!=null && iAdditionalInfo.length()>999) {
			errors.add("note", new ActionMessage("errors.generic", "Additional information is too long. Please, limit it to no more than 1000 characters."));
		}
		if (!WebTextValidation.isTextValid(iAdditionalInfo, true)){
			iAdditionalInfo = "";
			errors.add("note", new ActionMessage("errors.invalidCharacters", "Additional Information"));
		}
		
		return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iDateLocations.clear();
		iStartTime = 90;
		iStopTime = 210;
		iOp = null;
		iStartTimeString = null;
		iStopTimeString = null;
		iEventName = null;
		iMainContact = null;
		iAdditionalEmails = "";
		iAdditionalInfo = "";
		iMainContactEmail = null;
		iMainContactPhone = null;
		iMainContactFirstName = null;
		iMainContactMiddleName = null;
		iMainContactLastName = null;
		iMainContactExternalId = null;
		iEventType = null;
		iSponsoringOrgId = null;
		iSponsoringOrgName = null;
        iSubjectArea = DynamicList.getInstance(new ArrayList(), idfactory);
        iCourseNbr = DynamicList.getInstance(new ArrayList(), idfactory);
        iItype = DynamicList.getInstance(new ArrayList(), idfactory);
        iClassNumber = DynamicList.getInstance(new ArrayList(), idfactory);
		iAttendanceRequired = false;
		iExistingMeetings.clear();
		iMainContactLookup = false;
		iCapacity = null;
		load(request);
	}
	
	public void load(HttpServletRequest request) {
		HttpSession session = request.getSession();
		User user = Web.getUser(request.getSession());
		iDateLocations = (TreeSet<DateLocation>) session.getAttribute("Event.DateLocations");
		iStartTime = (Integer) session.getAttribute("Event.StartTime");
		iStopTime = (Integer) session.getAttribute("Event.StopTime");
		iEventType = (String) session.getAttribute("Event.EventType");
		iEventName = (String) session.getAttribute("Event.Name");
		iMainContactEmail = (String) session.getAttribute("Event.mcEmail");
		iMainContactFirstName = (String) session.getAttribute("Event.mcFName");
		iMainContactMiddleName = (String) session.getAttribute("Event.mcMName");
		iMainContactLastName = (String) session.getAttribute("Event.mcLName");
		iMainContactExternalId = (String) session.getAttribute("Event.mcUid");
		iMainContactPhone = (String) session.getAttribute("Event.mcPhone");
		iAdditionalEmails = (String) session.getAttribute("Event.AdditionalEmails");
		iAdditionalInfo = (String) session.getAttribute("Event.AdditionalInfo");
		iCapacity = (String) session.getAttribute("Event.Capacity");
		if (session.getAttribute("Event.SubjectArea")!=null) {
			iSubjectArea = (List) session.getAttribute("Event.SubjectArea");
			iSubjectAreas = (Collection) iSubjectArea;
			iCourseNbr = (List) session.getAttribute("Event.CourseNbr");
			iItype = (List) session.getAttribute("Event.SubjectItype");
			iClassNumber = (List) session.getAttribute("Event.ClassNumber");
			iAttendanceRequired = (Boolean) session.getAttribute("Event.AttendanceRequired");
		}
		TimetableManager tm = TimetableManager.getManager(user);
		iIsAddMeetings = (Boolean) (session.getAttribute("Event.IsAddMeetings"));
		iEventId = (Long) (session.getAttribute("Event.EventId"));
		if (iIsAddMeetings) {
			iEvent = EventDAO.getInstance().get(iEventId);
			iEventName = iEvent.getEventName();
			iEventType = iEvent.getEventTypeLabel();
			iMainContactFirstName = iEvent.getMainContact().getFirstName();
			iMainContactMiddleName = iEvent.getMainContact().getMiddleName();
			iMainContactLastName = iEvent.getMainContact().getLastName();
			iMainContactExternalId = iEvent.getMainContact().getExternalUniqueId();
			iMainContactEmail = iEvent.getMainContact().getEmailAddress();
			iMainContactPhone = iEvent.getMainContact().getPhone();
			iAdditionalEmails = iEvent.getEmail();
			if ("Special Event".equals(iEventType)) iSponsoringOrgName = (iEvent.getSponsoringOrganization()==null?"":iEvent.getSponsoringOrganization().getName());
			loadExistingMeetings();
		}/* else if (iEventName==null) {
			if (tm!=null) {
					iMainContactFirstName = (tm.getFirstName()==null?"":tm.getFirstName());
					iMainContactLastName = (tm.getLastName()==null?"":tm.getLastName());
					iMainContactEmail = (tm.getEmailAddress()==null?"":tm.getEmailAddress());
			}
		}*/
		loadNewMeetings();
	}
	
	public void save(HttpSession session) {
		session.setAttribute("Event.Name", iEventName);
		session.setAttribute("Event.mcEmail", iMainContactEmail);
		session.setAttribute("Event.mcFName", iMainContactFirstName);
		session.setAttribute("Event.mcMName", iMainContactMiddleName);
		session.setAttribute("Event.mcLName", iMainContactLastName);
		if (iMainContactExternalId==null)
		    session.removeAttribute("Event.mcUid");
		else
		    session.setAttribute("Event.mcUid", iMainContactExternalId);
		session.setAttribute("Event.mcPhone", iMainContactPhone);
		session.setAttribute("Event.AdditionalEmails", iAdditionalEmails);
		session.setAttribute("Event.AdditionalInfo", iAdditionalInfo);
		session.setAttribute("Event.Capacity", iCapacity);
	}
	
	public void submit(HttpServletRequest request) {

		HttpSession session = request.getSession();
		Transaction tx = null;
		try {
			Session hibSession = new _RootDAO().getSession();
			tx = hibSession.beginTransaction();

			// create event
			Event event = null;//getEvent();
			if (event==null) {
				// search database for a contact with this email
				// if not in db, create a new contact
				// update information from non-empty fields
			    EventContact mainContact = null;
			    if (iMainContactExternalId!=null) mainContact = EventContact.findByExternalUniqueId(iMainContactExternalId);
			    if (mainContact==null) mainContact = EventContact.findByEmail(iMainContactEmail); 
				if (mainContact==null) mainContact = new EventContact();
				if (iMainContactFirstName!=null && iMainContactFirstName.length()>0) 
					mainContact.setFirstName(iMainContactFirstName);
                mainContact.setMiddleName(iMainContactMiddleName);
				if (iMainContactLastName!=null && iMainContactLastName.length()>0)
					mainContact.setLastName(iMainContactLastName);
				if (iMainContactEmail!=null && iMainContactEmail.length()>0)
					mainContact.setEmailAddress(iMainContactEmail);
				if (iMainContactPhone!=null && iMainContactPhone.length()>0)
					mainContact.setPhone(iMainContactPhone);
                if (iMainContactExternalId!=null && iMainContactExternalId.length()>0)
                    mainContact.setExternalUniqueId(iMainContactExternalId);
				hibSession.saveOrUpdate(mainContact);
				if ("Course Related Event".equals(iEventType)) {
					event = new CourseEvent();
					((CourseEvent) event).setReqAttendance(iAttendanceRequired);
					setRelatedCourseInfos((CourseEvent)event);
				} else {
					event = new SpecialEvent();
					if (iSponsoringOrgId!=null) {
					    System.out.println("Sponsoring org id = "+iSponsoringOrgId);
					    SponsoringOrganization spor = SponsoringOrganizationDAO.getInstance().get(iSponsoringOrgId);
					    event.setSponsoringOrganization(spor);
					}
				}
				try {
				    event.setMinCapacity(Integer.parseInt(iCapacity));
				    event.setMaxCapacity(Integer.parseInt(iCapacity));
				} catch (Exception e) {}
				event.setEventName(iEventName);
				event.setMainContact(mainContact);
				// add additional emails
				if (iAdditionalEmails!=null && iAdditionalEmails.length()>0) {
					event.setEmail(iAdditionalEmails);
				}
				hibSession.saveOrUpdate(event);

				event.setMeetings(new HashSet());
			}
			
			// create event meetings
			HashSet<Meeting> createdMeetings = new HashSet();
			for (Iterator i=iDateLocations.iterator();i.hasNext();) {
				DateLocation dl = (DateLocation) i.next();
				Meeting m = new Meeting();
				m.setMeetingDate(dl.getDate());
				m.setStartPeriod(dl.getStartTime()>=0?dl.getStartTime():iStartTime);
				m.setStopPeriod(dl.getStopTime()>=0?dl.getStopTime():iStopTime);
				m.setLocationPermanentId(dl.getLocation());
				m.setClassCanOverride(true);
				m.setEvent(event);
				hibSession.saveOrUpdate(m); // save each meeting to db
				event.getMeetings().add(m); // link each meeting with event
				createdMeetings.add(m);
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
            en.setNoteType(EventNote.sEventNoteTypeCreateEvent);
            en.setTimeStamp(new Date());
            en.setMeetingCollection(createdMeetings);
            en.setTextNote(iAdditionalInfo);
            en.setUser(uname);
            en.setEvent(event);
            //hibSession.saveOrUpdate(en);
            // attach the note to event
            if (event.getNotes()==null) event.setNotes(new HashSet());
            event.getNotes().add(en);

			hibSession.saveOrUpdate(event);
			
			ChangeLog.addChange(
                    hibSession,
                    request,
                    event,
                    ChangeLog.Source.EVENT_EDIT,
                    ChangeLog.Operation.CREATE,
                    null,null);
			
			new EventEmail(event, EventEmail.sActionCreate, event.getMultiMeetings(), iAdditionalInfo).send(request);
			
			tx.commit();
			iEventId = event.getUniqueId();
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			e.printStackTrace();
		}
	}
	
	public void update (HttpServletRequest request) {

		HttpSession session = request.getSession();
		Transaction tx = null;
		try {
			Session hibSession = new _RootDAO().getSession();
			tx = hibSession.beginTransaction();

			// create event meetings
			HashSet<Meeting> createdMeetings = new HashSet();
			for (Iterator i=iDateLocations.iterator();i.hasNext();) {
				DateLocation dl = (DateLocation) i.next();
				Meeting m = new Meeting();
				m.setMeetingDate(dl.getDate());
				m.setStartPeriod(dl.getStartTime()>=0?dl.getStartTime():iStartTime);
				m.setStopPeriod(dl.getStopTime()>=0?dl.getStopTime():iStopTime);
				m.setLocationPermanentId(dl.getLocation());
				m.setClassCanOverride(true);
				m.setEvent(iEvent);
				hibSession.saveOrUpdate(m); // save each meeting to db
				iEvent.getMeetings().add(m); // link each meeting with event
				createdMeetings.add(m);
			}

			User user = Web.getUser(request.getSession());
			String uname = iEvent.getMainContact().getShortName();
	        if (user!=null && (user.isAdmin() || Roles.EVENT_MGR_ROLE.equals(user.getRole()))) {
	            TimetableManager mgr = TimetableManager.getManager(user);
	            if (mgr!=null) uname = mgr.getShortName();
	        }
	        if (uname==null) uname = user.getName();
	        
            // add event note (additional info)
            EventNote en = new EventNote();
            en.setNoteType(EventNote.sEventNoteTypeAddMeetings);
            en.setTimeStamp(new Date());
            en.setMeetingCollection(createdMeetings);
            en.setTextNote(iAdditionalInfo);
            en.setUser(uname);
            en.setEvent(iEvent);
            //hibSession.saveOrUpdate(en);
            // attach the note to event
            if (iEvent.getNotes()==null) iEvent.setNotes(new HashSet());
            iEvent.getNotes().add(en);

			hibSession.saveOrUpdate(iEvent);

			ChangeLog.addChange(
                    hibSession,
                    request,
                    iEvent,
                    ChangeLog.Source.EVENT_EDIT,
                    ChangeLog.Operation.UPDATE,
                    null,null);
			
			new EventEmail(iEvent, EventEmail.sActionAddMeeting, Event.getMultiMeetings(createdMeetings), iAdditionalInfo).send(request);
			
			tx.commit();
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			e.printStackTrace();
		}
	}
	
	
	public Long getEventId() {return iEventId;}	
	
	public int getStartTime() {return iStartTime;	}
	public int getStopTime() {return iStopTime;}
	
	public String getOp() {return iOp;}
	public void setOp(String op) {iOp = op;}
	
	public boolean getAttendanceRequired() {return iAttendanceRequired;}
	public void setAttendanceRequired(boolean save) {iAttendanceRequired = save;}
	
	public String getAdditionalEmails() {return iAdditionalEmails;}
	public void setAdditionalEmails(String emails) {iAdditionalEmails = emails;}
	
	public TreeSet<DateLocation> getDateLocations() {
		return iDateLocations;
	}
	
	public String getTimeString(int time) {
	    int hour = (time/12)%12;
	    if (hour==0) hour = 12;
    	int minute = time%12*5;
    	String ampm = (time/144==0?"am":"pm");
		return hour+":"+(minute<10?"0":"")+minute+" "+ampm;
	}
	
	public String getStartTimeString() {
		return getTimeString(iStartTime);
	}
	
	public String getStopTimeString() {
		return getTimeString(iStopTime);
	}
	
	public String getEventType() {return iEventType;}
	public void setEventType(String type) {iEventType = type;}

	public String getEventName() {return iEventName;}
	public void setEventName(String name) {iEventName = name;}
	
	public List getSponsoringOrgs() {
		return SponsoringOrganization.findAll();
	}
	public Long getSponsoringOrgId() {return iSponsoringOrgId;}
	public void setSponsoringOrgId(Long id) {iSponsoringOrgId = id;}
	
	public String getSponsoringOrgName() {return iSponsoringOrgName;}
	public void setSponsoringOrgName(String name) {iSponsoringOrgName = name;}
	
	public ContactBean getMainContact() {return iMainContact;}
	public void setMainContact (ContactBean contact) {iMainContact = contact;}	
	
	public String getMainContactEmail() {return iMainContactEmail;}
	public void setMainContactEmail(String email) {iMainContactEmail = email;}

	public String getMainContactPhone() {return iMainContactPhone;}
	public void setMainContactPhone(String phone) {iMainContactPhone = phone;}
	
	public String getAdditionalInfo() {return iAdditionalInfo;}
	public void setAdditionalInfo(String info) {iAdditionalInfo = info;}

	public String getMainContactFirstName() {return iMainContactFirstName;}
	public void setMainContactFirstName(String firstName) {iMainContactFirstName = firstName;}
	
    public String getMainContactMiddleName() {return iMainContactMiddleName;}
    public void setMainContactMiddleName(String middleName) {iMainContactMiddleName = middleName;}

    public String getMainContactLastName() {return iMainContactLastName;}
	public void setMainContactLastName(String lastName) {iMainContactLastName = lastName;}
	
    public String getMainContactExternalId() {return iMainContactExternalId;}
    public void setMainContactExternalId(String externalId) {iMainContactExternalId = externalId;}

    public Event getEvent() {return iEvent;}
	public void setEvent(Event e) {iEvent = e;}
	
	public boolean getIsAddMeetings() {return iIsAddMeetings;}
	public void setIsAddMeetings(boolean isAdd) {iIsAddMeetings = isAdd;}

	public void loadExistingMeetings() {
		SimpleDateFormat iDateFormat = new SimpleDateFormat("EEE MM/dd, yyyy", Locale.US);
		for (Iterator i=new TreeSet(iEvent.getMeetings()).iterator();i.hasNext();) {
			Meeting meeting = (Meeting)i.next();
			MeetingBean mb = new MeetingBean(meeting);
            for (Meeting overlap : meeting.getTimeRoomOverlaps())
                mb.getOverlaps().add(new MeetingBean(overlap));
            iExistingMeetings.add(mb);
		}
	}
	
	public Vector<MeetingBean> getExistingMeetings() {return iExistingMeetings;}

	public void loadNewMeetings() {
		for (Iterator i= iDateLocations.iterator();i.hasNext();) {
			DateLocation dl = (DateLocation) i.next();
			Location location = LocationDAO.getInstance().get(dl.getLocUniqueId());
			MeetingBean mb = new MeetingBean(dl.getDate(),(dl.getStartTime()>=0?dl.getStartTime():iStartTime),(dl.getStopTime()>=0?dl.getStopTime():iStopTime),location);
			if (location!=null)
			    for (Meeting overlap : Meeting.findOverlaps(dl.getDate(), iStartTime, iStopTime, location.getPermanentId()))
			        mb.getOverlaps().add(new MeetingBean(overlap));
            iNewMeetings.add(mb);
		}
	}	
	
	public Vector<MeetingBean> getNewMeetings() {return iNewMeetings;}
	
	public void cleanSessionAttributes(HttpSession session) {
		session.removeAttribute("Event.DateLocations");
		session.removeAttribute("Event.StartTime");
		session.removeAttribute("Event.StopTime");
		session.removeAttribute("Event.MeetingDates");
		session.removeAttribute("Event.MinCapacity");
		session.removeAttribute("Event.MaxCapacity");
		session.removeAttribute("Event.BuildingId");
		session.removeAttribute("Event.RoomNumber");
		session.removeAttribute("Event.SessionId");
		session.removeAttribute("back");
		session.removeAttribute("Event.LookAtNearLocations");
		session.removeAttribute("Event.SubjectArea");
		session.removeAttribute("Event.CourseNbr");
		session.removeAttribute("Event.SubjectItype");
		session.removeAttribute("Event.ClassNumber");
		session.removeAttribute("Event.EventId");
		session.removeAttribute("Event.IsAddMeetings");
		session.removeAttribute("Event.Name");
		session.removeAttribute("Event.mcEmail");
		session.removeAttribute("Event.mcFName");
		session.removeAttribute("Event.mcLName");
		session.removeAttribute("Event.mcPhone");
		session.removeAttribute("Event.AdditionalInfo");
		session.removeAttribute("Event.RoomFeatures");
		session.removeAttribute("Event.AdditionalEmails");
		session.removeAttribute("Event.Capacity");
	}

    protected DynamicListObjectFactory idfactory = new DynamicListObjectFactory() {
        public Object create() {
            return new Long(-1);
        }
    };
	
    public Collection getSubjectAreas() { return iSubjectAreas; }
    public void setSubjectAreas(Collection subjectAreas) { this.iSubjectAreas = subjectAreas; }
    public List getSubjectAreaList() { return iSubjectArea; }
    public List getSubjectArea() { return iSubjectArea; }
    public Long getSubjectArea(int key) { return (Long)iSubjectArea.get(key); }
    public void setSubjectArea(int key, Long value) { this.iSubjectArea.set(key, value); }
    public void setSubjectArea(List subjectArea) { this.iSubjectArea = subjectArea; }
    public List getCourseNbr() { return iCourseNbr; }
    public Long getCourseNbr(int key) { return (Long)iCourseNbr.get(key); }
    public void setCourseNbr(int key, Long value) { this.iCourseNbr.set(key, value); }
    public void setCourseNbr(List courseNbr) { this.iCourseNbr = courseNbr; }
    public List getItype() { return iItype; }
    public Long getItype(int key) { return (Long)iItype.get(key); }
    public void setItype(int key, Long value) { this.iItype.set(key, value); }
    public void setItype(List itype) { this.iItype = itype; }
    public List getClassNumber() { return iClassNumber; }
    public Long getClassNumber(int key) { return (Long)iClassNumber.get(key); }
    public void setClassNumber(int key, Long value) { this.iClassNumber.set(key, value); }
    public void setClassNumber(List classNumber) { this.iClassNumber = classNumber; }
    
    
    public Collection getCourseNbrs(int idx) { 
        Vector ret = new Vector();
        boolean contains = false;
        if (getSubjectArea(idx)>=0) {
            for (Iterator i= new CourseOfferingDAO().
                    getSession().
                    createQuery("select co.uniqueId, co.courseNbr, co.title from CourseOffering co "+
                            "where co.subjectArea.uniqueId = :subjectAreaId "+
                            "and co.instructionalOffering.notOffered = false "+
                            "order by co.courseNbr ").
                    setFetchSize(200).
                    setCacheable(true).
                    setLong("subjectAreaId", getSubjectArea(idx)).iterate();i.hasNext();) {
                Object[] o = (Object[])i.next();
                ret.add(new IdValue((Long)o[0],((String)o[1] + " - " + (String)o[2])));
                if (o[0].equals(getCourseNbr(idx))) contains = true;
            }
        }
        if (!contains) setCourseNbr(idx, -1L);
        if (ret.size()==1) setCourseNbr(idx, ((IdValue)ret.firstElement()).getId());
        else ret.insertElementAt(new IdValue(-1L,"-"), 0);
        return ret;
    }
    
    public Collection getItypes(int idx) { 
        Vector ret = new Vector();
        boolean contains = false;
        if (getCourseNbr(idx)>=0) {
            CourseOffering course = new CourseOfferingDAO().get(getCourseNbr(idx));
            if (course.isIsControl())
                ret.add(new IdValue(Long.MIN_VALUE+1,"Offering"));
            ret.add(new IdValue(Long.MIN_VALUE,"Course"));
            if (!course.isIsControl()) {
                setItype(idx, Long.MIN_VALUE);
                return ret;
            }
            TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(null));
            configs.addAll(new InstrOfferingConfigDAO().
                getSession().
                createQuery("select distinct c from " +
                        "InstrOfferingConfig c inner join c.instructionalOffering.courseOfferings co "+
                        "where co.uniqueId = :courseOfferingId").
                setFetchSize(200).
                setCacheable(true).
                setLong("courseOfferingId", course.getUniqueId()).
                list());
            if (!configs.isEmpty()) {
                ret.add(new IdValue(Long.MIN_VALUE+2,"-- Configurations --"));
                for (Iterator i=configs.iterator();i.hasNext();) {
                    InstrOfferingConfig c = (InstrOfferingConfig)i.next();
                    if (c.getUniqueId().equals(getItype(idx))) contains = true;
                    ret.add(new IdValue(-c.getUniqueId(), c.getName()));
                }
            }
            TreeSet subparts = new TreeSet(new SchedulingSubpartComparator(null));
            subparts.addAll(new SchedulingSubpartDAO().
                getSession().
                createQuery("select distinct s from " +
                        "SchedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering.courseOfferings co "+
                        "where co.uniqueId = :courseOfferingId").
                setFetchSize(200).
                setCacheable(true).
                setLong("courseOfferingId", course.getUniqueId()).
                list());
            if (!configs.isEmpty() && !subparts.isEmpty())
                ret.add(new IdValue(Long.MIN_VALUE+2,"-- Subparts --"));
            for (Iterator i=subparts.iterator();i.hasNext();) {
                SchedulingSubpart s = (SchedulingSubpart)i.next();
                Long sid = s.getUniqueId();
                String name = s.getItype().getAbbv();
                String sufix = s.getSchedulingSubpartSuffix();
                while (s.getParentSubpart()!=null) {
                    name = "&nbsp;&nbsp;&nbsp;&nbsp;"+name;
                    s = s.getParentSubpart();
                }
                if (s.getInstrOfferingConfig().getInstructionalOffering().getInstrOfferingConfigs().size()>1)
                    name += " ["+s.getInstrOfferingConfig().getName()+"]";
                if (sid.equals(getItype(idx))) contains = true;
                ret.add(new IdValue(sid, name+(sufix==null || sufix.length()==0?"":" ("+sufix+")")));
            }
        } else {
            ret.addElement(new IdValue(0L,"N/A"));
        }
        if (!contains) setItype(idx, ((IdValue)ret.firstElement()).getId());
        return ret;
    }
	
	
    public RelatedCourseInfo getRelatedCourseInfo(int idx) {
        if (getSubjectArea(idx)<0 || getCourseNbr(idx)<0) return null;
        CourseOffering course = new CourseOfferingDAO().get(getCourseNbr(idx));
        if (course==null) return null;
        if (getItype(idx)==Long.MIN_VALUE) { //course
            RelatedCourseInfo owner = new RelatedCourseInfo();
            owner.setOwner(course);
            return owner;
        } else if (getItype(idx)==Long.MIN_VALUE+1 || getItype(idx)==Long.MIN_VALUE+2) { //offering
            RelatedCourseInfo owner = new RelatedCourseInfo();
            owner.setOwner(course.getInstructionalOffering());
            return owner;
        } else if (getItype(idx)<0) { //config
            InstrOfferingConfig config = new InstrOfferingConfigDAO().get(-getItype(idx));
            if (config==null) return null;
            RelatedCourseInfo owner = new RelatedCourseInfo();
            owner.setOwner(config);
            return owner;
        } else if (getClassNumber(idx)>=0) { //class
            Class_ clazz = new Class_DAO().get(getClassNumber(idx));
            if (clazz==null) return null;
            RelatedCourseInfo owner = new RelatedCourseInfo();
            owner.setOwner(clazz);
            return owner;
        }
        return null;
    }
    
    public void setRelatedCourseInfos(CourseEvent event) {
        if (event.getRelatedCourses()==null) event.setRelatedCourses(new HashSet());
        event.getRelatedCourses().clear();
        for (int idx=0;idx<getSubjectArea().size();idx++) {
            RelatedCourseInfo course = getRelatedCourseInfo(idx);
            if (course!=null) {
                event.getRelatedCourses().add(course);
                course.setEvent(event);
            }
        }
    }

    public String getRelatedCoursesTable() {
        WebTable table = new WebTable(5, null, new String[] {"Object", "Type", "Title", "Students", "Assignment"}, new String[] {"left", "left", "left", "right", "left"}, new boolean[] {true, true, true, true, true});
        for (int idx=0;idx<iSubjectArea.size();idx++) {
            RelatedCourseInfo rci = getRelatedCourseInfo(idx);
            if (rci==null) continue;
            String name = null, type = null, title = null, assignment = null;
            int students = rci.countStudents(); 
            switch (rci.getOwnerType()) {
                case ExamOwner.sOwnerTypeClass :
                    Class_ clazz = (Class_)rci.getOwnerObject();
                    name = rci.getLabel();//clazz.getClassLabel();
                    type = "Class";
                    title = clazz.getSchedulePrintNote();
                    if (title==null || title.length()==0) title=clazz.getSchedulingSubpart().getControllingCourseOffering().getTitle();
                    if (clazz.getCommittedAssignment()!=null)
                        assignment = clazz.getCommittedAssignment().getPlacement().getLongName();
                    break;
                case ExamOwner.sOwnerTypeConfig :
                    InstrOfferingConfig config = (InstrOfferingConfig)rci.getOwnerObject();
                    name = rci.getLabel();//config.getCourseName()+" ["+config.getName()+"]";
                    type = "Configuration";
                    title = config.getControllingCourseOffering().getTitle();
                    break;
                case ExamOwner.sOwnerTypeOffering :
                    InstructionalOffering offering = (InstructionalOffering)rci.getOwnerObject();
                    name = rci.getLabel();//offering.getCourseName();
                    type = "Offering";
                    title = offering.getControllingCourseOffering().getTitle();
                    break;
                case ExamOwner.sOwnerTypeCourse :
                    CourseOffering course = (CourseOffering)rci.getOwnerObject();
                    name = rci.getLabel();//course.getCourseName();
                    type = "Course";
                    title = course.getTitle();
                    break;
                        
            }
            table.addLine(null, new String[] { name, type, title, String.valueOf(students), assignment}, null);
        }
        return (table.getLines().isEmpty()?"":table.printTable());
    }
    
    public boolean getMainContactLookup() { return iMainContactLookup; }
    public void setMainContactLookup(boolean lookup) { iMainContactLookup = lookup; }
    
    public boolean getCanChangeSelection() { return iStartTime>=0; }
    
    public String getCapacity() { return iCapacity; }
    public void setCapacity(String capacity) { iCapacity = capacity; }
    
}
