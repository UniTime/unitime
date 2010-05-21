
package org.unitime.timetable.form;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.StandardEventNote;
import org.unitime.timetable.model.dao.MeetingDAO;
import org.unitime.timetable.util.Constants;

public class EventDetailForm extends ActionForm {

	private String iId;
	private String iOp;
	private String iEventName;
	private String iEventType;
	private String iEnrollment;
	private String iMinCapacity;
	private String iMaxCapacity;
	private String iSponsoringOrgName;
	private Long iSelected;
	private String iSelectedStandardNote;
	private String iNewEventNote;
	private Vector<MeetingBean> iMeetings = new Vector<MeetingBean>();
	private Vector<String> iNotes = new Vector<String>();
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
	private Boolean iCanApprove;
	private Boolean iIsManager;
	
	/** 
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(
		ActionMapping mapping,
		HttpServletRequest request) {

		ActionErrors errors = new ActionErrors();
		
		if (iOp != null && !("Edit Event".equals(iOp) || "Back".equals(iOp)
				|| "Previous".equals(iOp) || "Next".equals(iOp)
				|| "Add Meetings".equals(iOp) || "Approve".equals(iOp)
				|| "Reject".equals(iOp) || "Delete".equals(iOp)
			    || "view".equals(iOp) || "Cancel".equals(iOp)
				|| "Update".equals(iOp) || "Submit".equals(iOp)
				)){
			errors.add("op", new ActionMessage("errors.generic", "Invalid Operation."));
			iOp = null;
		}

		if (iId != null){
			try {
				long id = Long.parseLong(iId);
			} catch (NumberFormatException nfe) {
				iId = "0";
				iOp = null;
				errors.add("id", new ActionMessage("errors.generic", "Event id should be a long."));
			}

		}
		return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {

		iEvent = null;
		iEventName = null;
		iEnrollment = null;
		iMinCapacity = null;
		iMaxCapacity = null;
		iSponsoringOrgName = null;
		iMainContact = null;
		iAdditionalContacts.clear();
		iMeetings.clear();
		iNotes.clear();
		iCanEdit = false;
		iCanDelete = false;
		iCanApprove = false;
		iPreviousId = null;
		iNextId = null;
		iAttendanceRequired = false;
		iAdditionalEmails = null;
		iSelectedMeetings = null;
		iIsManager = false;
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
    public void addMeeting(MeetingBean meeting) {
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
    
    public Vector<String> getNotes() {return iNotes;}
    public void addNote (String note) {
        iNotes.add(note);
    }
    
    public Collection<StandardEventNote> getStandardNotes() {
    	return StandardEventNote.findAll();
    }
    
    public String getSelectedStandardNote() {return iSelectedStandardNote;}
    public void setSelectedStandardNote(String selected) {iSelectedStandardNote = selected;}
    
    public String getNewEventNote() {return iNewEventNote;}
    public void setNewEventNote(String note) {iNewEventNote = note;}
    
    public boolean getCanEdit() {return iCanEdit;}
    public void setCanEdit(boolean canEdit) {iCanEdit = canEdit;}

    public boolean getCanDelete() {return iCanDelete;}
    public void setCanDelete(boolean canDelete) {iCanDelete = canDelete;}
    
    public boolean getCanApprove() {return iCanApprove;}
    public void setCanApprove(boolean canApprove) {iCanApprove = canApprove;}

    public String getPreviousId () {return iPreviousId;}
    public void setPreviousId(String prevId) {iPreviousId = prevId;}

    public String getNextId () {return iNextId; }
    public void setNextId(String nextId) {iNextId = nextId;}
    
	public Meeting getSelectedMeeting() {
		return (MeetingDAO.getInstance()).get(iSelected);
	} 	
	
	public Long[] getSelectedMeetings() { return iSelectedMeetings; }
	public void setSelectedMeetings(Long[] selectedMeetings) { iSelectedMeetings = selectedMeetings; }
    
    public boolean getCanSelectAll() {
        for (MeetingBean m:getMeetings()) {
            if (m.getCanSelect()) return true;
        }
        return false;
    }
    
    public void setIsManager(Boolean isManager) { iIsManager = isManager; }
    public Boolean getIsManager() { return iIsManager; }

    public static class MeetingBean implements Comparable<MeetingBean> {
    	private String iDate;
    	private String iTime;
    	private String iLocation;
    	private int iLocationCapacity;
    	private String iApprovedDate = null;
    	private Long iUniqueId = null;
    	private boolean iIsPast = false;
    	private boolean iCanSelectMeeting = false;
    	private TreeSet<MeetingBean> iOverlaps = new TreeSet();
        private Long iEventId = null;
    	private String iName = null;
    	private String iType = null;
    	
    	public MeetingBean(Date date, int startTime, int endTime, Location location) {
    	    iDate = new SimpleDateFormat("EEE MM/dd, yyyy", Locale.US).format(date);
    	    iTime = (startTime==0 && endTime==Constants.SLOTS_PER_DAY?"All Day":
    	            Constants.toTime(Constants.SLOT_LENGTH_MIN*startTime+Constants.FIRST_SLOT_TIME_MIN)+" - "+
    	            Constants.toTime(Constants.SLOT_LENGTH_MIN*endTime+Constants.FIRST_SLOT_TIME_MIN));
    	    iLocation = (location==null?"":location.getLabel());
    	    iLocationCapacity = (location==null?0:location.getCapacity());
    	}
    	
    	public MeetingBean(Meeting meeting) {
    	    iEventId = meeting.getEvent().getUniqueId();
    	    iName = meeting.getEvent().getEventName();
    	    iType = meeting.getEvent().getEventTypeAbbv();
    	    iUniqueId = meeting.getUniqueId();
    	    iTime = (meeting.isAllDay()?"All Day":
    	        Constants.toTime(Constants.SLOT_LENGTH_MIN*meeting.getStartPeriod()+Constants.FIRST_SLOT_TIME_MIN+(meeting.getStartOffset()==null?0:meeting.getStartOffset()))+" - "+
    	        Constants.toTime(Constants.SLOT_LENGTH_MIN*meeting.getStopPeriod()+Constants.FIRST_SLOT_TIME_MIN+(meeting.getStopOffset()==null?0:meeting.getStopOffset())));
            Location location = meeting.getLocation();
            iLocation = (location==null?"":location.getLabel());
            iLocationCapacity = (location==null?0:location.getCapacity());
            iApprovedDate = (meeting.getApprovedDate()==null?"":new SimpleDateFormat("MM/dd/yy", Locale.US).format(meeting.getApprovedDate()));
            iDate = new SimpleDateFormat("EEE MM/dd, yyyy", Locale.US).format(meeting.getMeetingDate());
            iIsPast = meeting.getStartTime().before(new Date());
    	}
    	
    	public Long getEventId() { return iEventId; }
    	public String getName() { return iName; }
    	public String getType() { return iType; }
    	public TreeSet<MeetingBean> getOverlaps() { return iOverlaps; }
    	
    	public String getDate() { return iDate; }
    	public void setDate(String date) { iDate = date; }
 
//    	public Long getId() { return Math.round(1000.0*Math.random()); }
    	
    	public String getTime() { return iTime;}
    	public void setTime(String time) {iTime = time;}
    	
    	public String getLocation() { return iLocation;}
    	public void setLocation(String location) {iLocation = location;}
    	
    	public int getLocationCapacity() {return iLocationCapacity;}
    	public void setLocationCapacity(int capacity) {iLocationCapacity = capacity;}

    	public String getApprovedDate() { return iApprovedDate;}
    	public void setApprovedDate(String approvedDate) {iApprovedDate = approvedDate;}
    	
    	public Long getUniqueId() {return iUniqueId;}
    	public void setUniqueId(Long id) {iUniqueId = id;}
    	
    	public boolean getIsPast() {return iIsPast;}
    	public void setIsPast(boolean isPast) {iIsPast = isPast;}
    	
    	public boolean getCanSelect() {return iCanSelectMeeting;}
    	public void setCanSelect(boolean canSelect) {iCanSelectMeeting = canSelect;}
    	
    	public int compareTo(MeetingBean meeting) {
    	    int cmp = iName.compareTo(meeting.iName);
    	    if (cmp!=0) return cmp;
    	    return iUniqueId.compareTo(meeting.iUniqueId);
    	}
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

	public String getEnrollment() {
		return iEnrollment;
	}

	public void setEnrollment(String enrollment) {
		iEnrollment = enrollment;
	}
}