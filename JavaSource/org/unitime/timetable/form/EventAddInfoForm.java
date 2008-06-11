package org.unitime.timetable.form;

import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.timetable.form.EventDetailForm.ContactBean;
import org.unitime.timetable.form.EventRoomAvailabilityForm.DateLocation;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.SpecialEvent;
import org.unitime.timetable.model.dao._RootDAO;

public class EventAddInfoForm extends ActionForm {

	private TreeSet<DateLocation> iDateLocations = new TreeSet();
	private int iStartTime;
	private int iStopTime;
	private String iOp;
	private String iStartTimeString;
	private String iStopTimeString;
	private String iEventName;
	private ContactBean iMainContact;
	private String iAdditionalInfo;
	private String iMainContactEmail;
	private String iMainContactPhone;
	private String iMainContactFirstName;	
	private String iMainContactLastName;
	
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();

		if (iEventName==null || iEventName.length()==0) {
			errors.add("eventName", new ActionMessage("errors.generic", "The event name is mandatory."));
		}

		if (iMainContactEmail==null || iMainContactEmail.length()==0) {
			errors.add("mcEmail", new ActionMessage("errors.generic", "The contact email is mandatory."));
		}

		if (iMainContactPhone==null || iMainContactPhone.length()==0) {
			errors.add("mcPhone", new ActionMessage("errors.generic", "The contact phone number is mandatory."));
		}

		if (iAdditionalInfo.length()>999) {
			errors.add("note", new ActionMessage("errors.generic", "Additional information is too long. Please, limit it to no more than 1000 characters."));
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
		iAdditionalInfo = "";
		iMainContactEmail = null;
		iMainContactPhone = null;
		iMainContactFirstName = null;
		iMainContactLastName = null;
		load(request.getSession());
	}
	
	public void load(HttpSession session) {
		iDateLocations = (TreeSet<DateLocation>) session.getAttribute("Event.DateLocations");
		iStartTime = (Integer) session.getAttribute("Event.StartTime");
		iStopTime = (Integer) session.getAttribute("Event.StopTime");
	}
	
	public void save(HttpSession session) {
	}

	public void submit(HttpSession session) {

		Transaction tx = null;
		try {
			Session hibSession = new _RootDAO().getSession();
			tx = hibSession.beginTransaction();

			// search database for a contact with this e-mail
			// if not in db, create a new contact
			// update information from non-empty fields
			EventContact mainContact = EventContact.findByEmail(iMainContactEmail); 
			if (mainContact==null) mainContact = new EventContact();
			if (iMainContactFirstName!=null && iMainContactFirstName.length()>0) 
				mainContact.setFirstName(iMainContactFirstName);
			if (iMainContactLastName!=null && iMainContactLastName.length()>0)
				mainContact.setLastName(iMainContactLastName);
			if (iMainContactEmail!=null && iMainContactEmail.length()>0)
				mainContact.setEmailAddress(iMainContactEmail);
			if (iMainContactPhone!=null && iMainContactPhone.length()>0)
				mainContact.setPhone(iMainContactPhone);
			hibSession.saveOrUpdate(mainContact);
			
			// create event 
			SpecialEvent event = new SpecialEvent();
			event.setEventName(iEventName);
			event.setMainContact(mainContact);
			
			hibSession.saveOrUpdate(event);
			
			// create event meetings
			event.setMeetings(new HashSet());
			for (Iterator i=iDateLocations.iterator();i.hasNext();) {
				DateLocation dl = (DateLocation) i.next();
				Meeting m = new Meeting();
				m.setMeetingDate(dl.getDate());
				m.setStartPeriod(iStartTime);
				m.setStopPeriod(iStopTime);
				m.setLocationPermanentId(dl.getLocation());
				m.setClassCanOverride(true);
				m.setEvent(event);
				hibSession.saveOrUpdate(m); // save each meeting to db
				event.getMeetings().add(m); // link each meeting with event
			}
			hibSession.saveOrUpdate(event);
			
			// add event note (additional info)
			EventNote en = new EventNote();
			en.setEvent(event);
			en.setTextNote(iAdditionalInfo);
			hibSession.saveOrUpdate(en);
			// attach the note to event
			event.setNotes(new HashSet());
			event.getNotes().add(en);
			hibSession.saveOrUpdate(event);

			tx.commit();
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			e.printStackTrace();
		}
		
	}
	
	public int getStartTime() {return iStartTime;	}
	public int getStopTime() {return iStopTime;}
	
	public String getOp() {return iOp;}
	public void setOp(String op) {iOp = op;}
	
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
	
	public String getEventName() {return iEventName;}
	public void setEventName(String name) {iEventName = name;}
	
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
	
	public String getMainContactLastName() {return iMainContactLastName;}
	public void setMainContactLastName(String lastName) {iMainContactLastName = lastName;}

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
	}
	
}
