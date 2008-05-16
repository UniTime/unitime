package org.unitime.timetable.form;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.ComboBoxLookup;

public class EventAddForm extends ActionForm {

	private String iOp;
	private String iEventType;
	private Long iSessionId;
	private HashSet<String> iMeetingDates = new HashSet<String>();
	private String iStartTime;
	private String iStopTime;
	private String iLocationType;
	private Vector<String> iLocationTypes = new Vector<String>();

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null;
		getEventTypes();
		iEventType = Event.sEventTypes[Event.sEventTypeSpecial];
		iSessionId = null;
		try {
			iSessionId = Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId();
		} catch (Exception e) {}
		iMeetingDates.clear();
		iStartTime = null;
		iStopTime = null;
		iLocationType = null;
		getLocationTypes();
	}
	
	public String getOp (){return iOp;}
	public void setOp (String op) {iOp = op;}
	
	public String[] getEventTypes() {
		String[] types = new String[] {Event.sEventTypes[Event.sEventTypeCourse], Event.sEventTypes[Event.sEventTypeSpecial]}; 
		return types;
	}
	
	public String getEventType() {return iEventType;}
	public void setEventType(String eventType) {iEventType = eventType;}
	
	public Vector<ComboBoxLookup> getAcademicSessions() {
		Vector<ComboBoxLookup> ret = new Vector();
		Date today = new Date();
		for (Iterator i=Session.getAllSessions().iterator();i.hasNext();) {
			Session session = (Session)i.next();
			//if (!session.getStatusType().canOwnerView()) continue;
			//if (session.getClassesEndDateTime().compareTo(today)<0) continue;
			ret.add(new ComboBoxLookup(session.getLabel(),session.getUniqueId().toString()));
		}
		return ret;
	}
		
	public Long getSessionId() {return iSessionId;}
	public void setSessionId(Long sessionId) {iSessionId = sessionId;}
	
    public HashSet<String> getMeetingDates() {return iMeetingDates;}
    public void addMeetingDate(String meetingDate) {
            iMeetingDates.add(meetingDate);
    }

    public String getStartTime() {return iStartTime; }
    public void setStartTime(String startTime) {iStartTime = startTime;}

    public String getStopTime() {return iStopTime; }
    public void setStopTime(String stopTime) {iStopTime = stopTime;}

	public Vector<ComboBoxLookup> getLocationTypes() {
		Vector<ComboBoxLookup> ret = new Vector();
		ret.add(new ComboBoxLookup("classroom", "1"));
		ret.add(new ComboBoxLookup("additional", "2"));
		return ret;
	}

	public String getLocationType() {return iLocationType; }
    public void setLocationType(String locationType) {iLocationType = locationType;}
}
