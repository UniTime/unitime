/*
 * TO DO 
 * - getSelected
 * - setSelected
 * 
 */

package org.unitime.timetable.form;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.EventContact;

public class EventDetailForm extends ActionForm {

	private String iId;
	private String iOp;
	private String iEventName;
	private String iMinCapacity;
	private String iMaxCapacity;
	private String iSponsoringOrg;
	private Long iSelected;
	private Vector<MeetingBean> iMeetings = new Vector<MeetingBean>();
	private Vector<NoteBean> iNotes = new Vector<NoteBean>();
	private ContactBean iMainContact;
	private Vector<ContactBean> iAdditionalContacts = new Vector<ContactBean>();
	private boolean iCanEdit;
	private String iPreviousId;
	private String iNextId;
	
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

		iEventName = null;
		iMinCapacity = null;
		iMaxCapacity = null;
		iSponsoringOrg = null;
		iMainContact = null;
		iAdditionalContacts.clear();
		iMeetings.clear();
		iNotes.clear();
		iCanEdit = false;
		iPreviousId = null;
		iNextId = null;
	}
	

    public String getEventName() {return iEventName;}
    public void setEventName(String eventName) {iEventName = eventName;}
    
    public String getMinCapacity() {return iMinCapacity;}
    public void setMinCapacity(String minCapacity) {iMinCapacity = minCapacity;}
    
    public String getMaxCapacity() {return iMaxCapacity;}
    public void setMaxCapacity(String maxCapacity) {iMaxCapacity = maxCapacity;}
    
    public String getSponsoringOrg() {return iSponsoringOrg;}
    public void setSponsoringOrg(String sponsoringOrg) {iSponsoringOrg = sponsoringOrg;}
    
	public String getId() {return iId;}
	public void setId(String id) {this.iId = id;}

	public String getOp() {return iOp;}
	public void setOp(String op) {this.iOp = op;}
	
    public Long getSelected() {return iSelected;}
    public void setSelected(Long selectedId) {iSelected = selectedId;}
    
    public Vector<MeetingBean> getMeetings() {return iMeetings;}
    public void addMeeting(String date, String startTime, String endTime, String location, String approvedDate) {
    	MeetingBean meeting = new MeetingBean();
    	meeting.setDate(date);
    	meeting.setStartTime(startTime);
    	meeting.setEndTime(endTime);
    	meeting.setLocation(location);
    	meeting.setApprovedDate(approvedDate);
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
    
    public boolean getCanEdit() {return iCanEdit;}
    public void setCanEdit(boolean canEdit) {iCanEdit = canEdit;}
    
    public String getPreviousId () {return iPreviousId;}
    public void setPreviousId(String prevId) {iPreviousId = prevId;}

    public String getNextId () {return iNextId; }
    public void setNextId(String nextId) {iNextId = nextId;}
 
    
    public class MeetingBean {
    	private String iDate;
    	private String iStartTime;
    	private String iEndTime;
    	private String iLocation;
    	private String iApprovedDate;
   	
    	public MeetingBean() {
    	}
    	
    	public String getDate() { return iDate; }
    	public void setDate(String date) { iDate = date; }
 
    	public Long getId() { return Math.round(1000.0*Math.random()); }
    	
    	public String getStartTime() { return iStartTime;}
    	public void setStartTime(String time) {iStartTime = time;}
    	
    	public String getEndTime() { return iEndTime;}
    	public void setEndTime(String time) {iEndTime = time;}

    	public String getLocation() { return iLocation;}
    	public void setLocation(String location) {iLocation = location;}

    	public String getApprovedDate() { return iApprovedDate;}
    	public void setApprovedDate(String approvedDate) {iApprovedDate = approvedDate;}
    	
    	
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