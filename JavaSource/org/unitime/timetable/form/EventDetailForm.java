
package org.unitime.timetable.form;

import java.util.Collection;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.StandardEventNote;
import org.unitime.timetable.model.dao.MeetingDAO;

public class EventDetailForm extends ActionForm {

	private String iId;
	private String iOp;
	private String iEventName;
	private String iEventType;
	private String iMinCapacity;
	private String iMaxCapacity;
	private String iSponsoringOrgName;
	private Long iSelected;
	private Long[] iSelectedStandardNotes;
	private String iNewEventNote;
	private Vector<MeetingBean> iMeetings = new Vector<MeetingBean>();
	private Vector<NoteBean> iNotes = new Vector<NoteBean>();
	private ContactBean iMainContact;
	private Vector<ContactBean> iAdditionalContacts = new Vector<ContactBean>();
	private String iAdditionalEmails; 
	private boolean iCanEdit;
	private String iPreviousId;
	private String iNextId;
	private boolean iAttendanceRequired;
	private Event iEvent;
	private Long[] iSelectedMeetings;
	private Boolean iCanDelete;
	
	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(
		ActionMapping mapping,
		HttpServletRequest request) {

		return null;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {

		iEvent = null;
		iEventName = null;
		iMinCapacity = null;
		iMaxCapacity = null;
		iSponsoringOrgName = null;
		iMainContact = null;
		iAdditionalContacts.clear();
		iMeetings.clear();
		iNotes.clear();
		iCanEdit = false;
		iCanDelete = false;
		iPreviousId = null;
		iNextId = null;
		iAttendanceRequired = false;
		iAdditionalEmails = null;
		iSelectedMeetings = null;
	}
	
	public Event getEvent() {return iEvent;}
	public void setEvent (Event event) {iEvent = event;}
	
    public String getEventName() {return iEventName;}
    public void setEventName(String eventName) {iEventName = eventName;}
    
    public String getEventType() {return iEventType;}
    public void setEventType(String type) {iEventType = type;}
    
    public String getMinCapacity() {return iMinCapacity;}
    public void setMinCapacity(String minCapacity) {iMinCapacity = minCapacity;}
    
    public String getMaxCapacity() {return iMaxCapacity;}
    public void setMaxCapacity(String maxCapacity) {iMaxCapacity = maxCapacity;}
    
    public String getSponsoringOrgName() {return iSponsoringOrgName;}
    public void setSponsoringOrgName(String name) {iSponsoringOrgName = name;}
    
	public String getId() {return iId;}
	public void setId(String id) {this.iId = id;}

	public String getOp() {return iOp;}
	public void setOp(String op) {this.iOp = op;}
	
    public Long getSelected() {return iSelected;}
    public void setSelected(Long selectedId) {iSelected = selectedId;}
    
    public boolean getAttendanceRequired() {return iAttendanceRequired;}
    public void setAttendanceRequired(boolean attReq) {iAttendanceRequired = attReq;}
    
    public String getAdditionalEmails() {return iAdditionalEmails;}
    public void setAdditionalEmails(String emails) {iAdditionalEmails = emails;}
    
    public Vector<MeetingBean> getMeetings() {return iMeetings;}
    public void addMeeting(Long id, String date, String startTime, String endTime, 
    		String location, String locationCapacity, String approvedDate,
    		boolean isPast, boolean canEdit, boolean canDelete) {
    	MeetingBean meeting = new MeetingBean();
    	meeting.setUniqueId(id);
    	meeting.setDate(date);
    	meeting.setStartTime(startTime);
    	meeting.setEndTime(endTime);
    	meeting.setLocation(location);
    	meeting.setLocationCapacity(locationCapacity);
    	meeting.setApprovedDate(approvedDate);
    	meeting.setIsPast(isPast);
    	meeting.setCanEdit(canEdit);
    	meeting.setCanDelete(canDelete);
    	iMeetings.add(meeting);
    }
    
    public ContactBean getMainContact() {return iMainContact;}
    public void setMainContact(EventContact contact) { 
    	iMainContact = new ContactBean(); 
    	iMainContact.setFirstName(contact.getFirstName());
    	iMainContact.setMiddleName(contact.getMiddleName());
    	iMainContact.setLastName(contact.getLastName());
    	iMainContact.setEmail(contact.getEmailAddress());
    	iMainContact.setPhone(contact.getPhone());
    }
    
    public Vector<ContactBean> getAdditionalContacts() {return iAdditionalContacts;}
    public void addAdditionalContact(String firstName, String middleName, String lastName, String email, String phone) {
    	ContactBean contact = new ContactBean();
    	contact.setFirstName(firstName);
    	contact.setMiddleName(middleName);
    	contact.setLastName(lastName);
    	contact.setEmail(email);
    	contact.setPhone(phone);
    	iAdditionalContacts.add(contact);
    }
    
    public Vector<NoteBean> getNotes() {return iNotes;}
    public void addNote (String textNote) {
    	NoteBean note = new NoteBean();
    	note.setTextNote(textNote);
    	iNotes.add(note);
    }

    public void addStandardNote (String standardNote) {
    	NoteBean note = new NoteBean();
    	note.setStandardNote(standardNote);
    	iNotes.add(note);
    }
    
    public Collection<StandardEventNote> getStandardNotes() {
    	return StandardEventNote.findAll();
    }
    
    public Long[] getSelectedStandardNotes() {return iSelectedStandardNotes;}
    public void setSelectedStandardNotes(Long[] selected) {iSelectedStandardNotes = selected;}
    
    public String getNewEventNote() {return iNewEventNote;}
    public void setNewEventNote(String note) {iNewEventNote = note;}
    
    public boolean getCanEdit() {return iCanEdit;}
    public void setCanEdit(boolean canEdit) {iCanEdit = canEdit;}

    public boolean getCanDelete() {return iCanDelete;}
    public void setCanDelete(boolean canDelete) {iCanDelete = canDelete;}
    
    public String getPreviousId () {return iPreviousId;}
    public void setPreviousId(String prevId) {iPreviousId = prevId;}

    public String getNextId () {return iNextId; }
    public void setNextId(String nextId) {iNextId = nextId;}
    
	public Meeting getSelectedMeeting() {
		return (MeetingDAO.getInstance()).get(iSelected);
	} 	
	
	public Long[] getSelectedMeetings() { return iSelectedMeetings; }
	public void setSelectedMeetings(Long[] selectedMeetings) { iSelectedMeetings = selectedMeetings; }
    
    public static class MeetingBean {
    	private String iDate;
    	private String iStartTime;
    	private String iEndTime;
    	private String iLocation;
    	private String iLocationCapacity;
    	private String iApprovedDate;
    	private Long iUniqueId;
    	private Boolean iIsPast;
    	private Boolean iCanEditMeeting;
    	private Boolean iCanDeleteMeeting;
   	
    	public MeetingBean() {
    	}
    	
    	public String getDate() { return iDate; }
    	public void setDate(String date) { iDate = date; }
 
//    	public Long getId() { return Math.round(1000.0*Math.random()); }
    	
    	public String getStartTime() { return iStartTime;}
    	public void setStartTime(String time) {iStartTime = time;}
    	
    	public String getEndTime() { return iEndTime;}
    	public void setEndTime(String time) {iEndTime = time;}

    	public String getLocation() { return iLocation;}
    	public void setLocation(String location) {iLocation = location;}
    	
    	public String getLocationCapacity() {return iLocationCapacity;}
    	public void setLocationCapacity(String capacity) {iLocationCapacity = capacity;}

    	public String getApprovedDate() { return iApprovedDate;}
    	public void setApprovedDate(String approvedDate) {iApprovedDate = approvedDate;}
    	
    	public Long getUniqueId() {return iUniqueId;}
    	public void setUniqueId(Long id) {iUniqueId = id;}
    	
    	public Boolean getIsPast() {return iIsPast;}
    	public void setIsPast(Boolean isPast) {iIsPast = isPast;}
    	
    	public Boolean getCanEdit() {return iCanEditMeeting;}
    	public void setCanEdit(Boolean canEdit) {iCanEditMeeting = canEdit;}
    	
    	public Boolean getCanDelete() {return iCanDeleteMeeting;}
    	public void setCanDelete(Boolean canDelete) {iCanDeleteMeeting = canDelete;}
    }

    public class ContactBean {
    	
    	private String iFirstName;
    	private String iMiddleName;
    	private String iLastName;
    	private String iEmail;
    	private String iPhone;
    	
    	public ContactBean() {    		
    	}
    	
    	public String getFirstName() {return iFirstName;}
    	public void setFirstName(String firstName) {iFirstName = firstName;}
    	
    	public String getMiddleName() {return iMiddleName;}
    	public void setMiddleName(String middleName) {iMiddleName = middleName;}

    	public String getLastName() {return iLastName;}
    	public void setLastName(String lastName) {iLastName = lastName;}

    	public String getEmail() {return iEmail;}
    	public void setEmail(String email) {iEmail = email;}
    	
    	public String getPhone() {return iPhone;}
    	public void setPhone(String phone) {iPhone = phone;}
   	
    }
    
    public class NoteBean {
 
    	private String iTextNote;
    	private String iStandardNote;
    	
    	public NoteBean () {
    	}
    	
    	public String getTextNote() {return iTextNote;	}
    	public void setTextNote(String textNote) {iTextNote = textNote;	}

    	public String getStandardNote() {return iStandardNote;	}
    	public void setStandardNote(String standardNote) {iStandardNote = standardNote;	}
    }
    
}