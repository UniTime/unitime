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
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.ReferenceList;


/** 
 * MyEclipse Struts
 * Creation date: 02-18-2005
 * 
 * XDoclet definition:
 * @struts:form name="sessionEditForm"
 */
public class SessionEditForm extends ActionForm {

	private static final long serialVersionUID = 3258410646873060656L;
	
	// --------------------------------------------------------- Instance Variables
	
	Session session = new Session();
	ReferenceList statusOptions;
	
	String academicInitiative;
	String academicYear;
	String academicTerm;
	String sessionStart;
	String sessionEnd;
	String classesEnd;
	String examStart;
	String eventStart;
	String eventEnd;
	String defaultDatePatternId;
	String defaultDatePatternLabel;
	
	Hashtable<String,String> roomOptionMessage = new Hashtable();
	Hashtable<String,Boolean> roomOptionScheduleEvents = new Hashtable();
	
	// --------------------------------------------------------- Methods
	
	public ActionErrors validate(ActionMapping arg0, HttpServletRequest arg1) {
		ActionErrors errors = new ActionErrors();
		
		// Check data fields
		if (academicInitiative==null || academicInitiative.trim().length()==0) 
			errors.add("academicInitiative", new ActionMessage("errors.required", "Academic Initiative"));
		
		if (academicTerm==null || academicTerm.trim().length()==0) 
			errors.add("academicTerm", new ActionMessage("errors.required", "Academic Term"));

		if (academicYear==null || academicYear.trim().length()==0) 
			errors.add("academicYear", new ActionMessage("errors.required", "Academic Year"));
		else {
			try {
				int year = Integer.parseInt(academicYear); 
			}
			catch (Exception e) {
				errors.add("academicYear", new ActionMessage("errors.numeric", "Academic Year"));
			}
		}
		
		validateDates(errors);
		
		if (getStatus()==null || getStatus().trim().length()==0) 
			errors.add("status", new ActionMessage("errors.required", "Session Status"));
		
		
		// Check for duplicate academic initiative, year & term
		if (errors.size()==0) {
			Session sessn = Session.getSessionUsingInitiativeYearTerm(academicInitiative, academicYear, academicTerm);
			if (session.getSessionId()==null && sessn!=null)
				errors.add("sessionId", new ActionMessage("errors.generic", "An academic session for the initiative, year and term already exists"));
				
			if (session.getSessionId()!=null && sessn!=null) {
				if (!session.getSessionId().equals(sessn.getSessionId()))
					errors.add("sessionId", new ActionMessage("errors.generic", "Another academic session for the same initiative, year and term already exists"));
			}
		}
		
		return errors;
	}

	
	/**
	 * Validates all the dates
	 * @param errors
	 */
	public void validateDates(ActionErrors errors) {
		String df = "MM/dd/yyyy";
		if (!CalendarUtils.isValidDate(sessionStart, df))
			errors.add("sessionStart", new ActionMessage("errors.invalidDate", "Session Start Date"));
		else {
			Date d1 = CalendarUtils.getDate(sessionStart, df);
			
			if (!CalendarUtils.isValidDate(sessionEnd, df))
				errors.add("sessionEnd", new ActionMessage("errors.invalidDate", "Session End Date"));
			else {
				Date d2 = CalendarUtils.getDate(sessionEnd, df);
				if (!d2.after(d1)) 
					errors.add("sessionEnd", new ActionMessage("errors.generic", "Session End Date must occur AFTER Session Start Date"));
				else {
					if (!CalendarUtils.isValidDate(classesEnd, df))
						errors.add("classesEnd", new ActionMessage("errors.invalidDate", "Classes End Date"));
					else {
						Date d3 = CalendarUtils.getDate(classesEnd, df);
						if (!d3.after(d1)) {
							errors.add("classesEnd", new ActionMessage("errors.generic", "Classes End Date must occur AFTER Session Start Date"));
						} else if (!(d3.before(d2) || d3.equals(d2))) { 
							errors.add("classesEnd", new ActionMessage("errors.generic", "Classes End Date must occur ON or BEFORE Session End Date"));
						} else {
						    if (!CalendarUtils.isValidDate(examStart, df))
						        errors.add("examStart", new ActionMessage("errors.invalidDate", "Examinations Start Date"));
                            if (!CalendarUtils.isValidDate(eventStart, df))
                                errors.add("eventStart", new ActionMessage("errors.invalidDate", "Event Start Date"));
                            if (!CalendarUtils.isValidDate(eventEnd, df))
                                errors.add("eventEnd", new ActionMessage("errors.invalidDate", "Event End Date"));
                            Date d4 = CalendarUtils.getDate(eventStart, df);
                            Date d5 = CalendarUtils.getDate(eventEnd, df);
                            if (!d4.before(d5)) {
                                errors.add("eventEnd", new ActionMessage("errors.generic", "Event End Date must occur AFTER Event Start Date"));
                            }
						}
					}
				}
			}
		}
	}


	/**
	 * @return Returns the session.
	 */
	public Session getSession() {
		return session;
	}
	/**
	 * @param session The session to set.
	 */
	public void setSession(Session session) {
		this.session = session;
	}
	
	public boolean equals(Object arg0) {
		return session.equals(arg0);
	}
	
	public String getAcademicInitiative() {
		return academicInitiative;
	}
	
	public int hashCode() {
		return session.hashCode();
	}
	
	public void setAcademicInitiative(String academicInitiative) {
		this.academicInitiative = academicInitiative;
	}

	/**
	 * @return
	 */
	public Long getSessionId() {
		return session.getSessionId();
	}
	/**
	 * @param sessionId
	 */
	public void setSessionId(Long sessionId) {
		if (sessionId!=null && sessionId.longValue()<=0)
			session.setSessionId(null);
		else
			session.setSessionId(sessionId);
	}
	/**
	 * @return
	 */
	public String getStatus() {
		return (session.getStatusType()==null?null:session.getStatusType().getReference());
	}

	public DepartmentStatusType getStatusType() {
		return session.getStatusType();
	}

	/**
	 * @param status
	 */
	public void setStatus(String status) throws Exception {
		session.setStatusType(status==null || status.length()==0?null:DepartmentStatusType.findByRef(status));
	}

	/**
	 * @return Returns the statusOptions.
	 */
	public ReferenceList getStatusOptions() {
		if (statusOptions==null) 
			statusOptions = Session.getSessionStatusList();
		return statusOptions;
	}
	/**
	 * @param statusOptions The statusOptions to set.
	 */
	public void setStatusOptions(ReferenceList statusOptions) {
		this.statusOptions = statusOptions;
	}
	/**
	 * @return
	 */
	public String getAcademicInitiativeDisplayString() {
		return session.academicInitiativeDisplayString();
	}
	/**
	 * @return
	 */
	public String getLabel() {
		return session.getLabel();
	}
		
    public String getAcademicTerm() {
		return academicTerm;
	}
    
	public void setAcademicTerm(String academicTerm) {
		this.academicTerm = academicTerm;
	}
	
	public String getAcademicYear() {
		return academicYear;
	}
	
	public void setAcademicYear(String academicYear) {
		this.academicYear = academicYear;
	}
	
	public String getDefaultDatePatternId() {
        return defaultDatePatternId;
    }
    public void setDefaultDatePatternId(String defaultDatePatternId) {
        this.defaultDatePatternId = defaultDatePatternId;
    }
    
    public String getDefaultDatePatternLabel() {
        return defaultDatePatternLabel;
    }
    public void setDefaultDatePatternLabel(String defaultDatePatternLabel) {
        this.defaultDatePatternLabel = defaultDatePatternLabel;
    }
    
	public String getClassesEnd() {
		return classesEnd;
	}
	public void setClassesEnd(String classesEnd) {
		this.classesEnd = classesEnd;
	}
	
	public String getSessionEnd() {
		return sessionEnd;
	}
	public void setSessionEnd(String sessionEnd) {
		this.sessionEnd = sessionEnd;
	}
	
	public String getSessionStart() {
		return sessionStart;
	}
	public void setSessionStart(String sessionStart) {
		this.sessionStart = sessionStart;
	}
    public String getExamStart() {
        return examStart;
    }
    public void setExamStart(String examStart) {
        this.examStart = examStart;
    }
    public String getEventStart() {
        return eventStart;
    }
    public void setEventStart(String eventStart) {
        this.eventStart = eventStart;
    }
    public String getEventEnd() {
        return eventEnd;
    }
    public void setEventEnd(String eventEnd) {
        this.eventEnd = eventEnd;
    }
    
    public Set<RoomType> getRoomTypes() {
        return RoomType.findAll();
    }
    
    public String getRoomOptionMessage(String roomType) {
        return roomOptionMessage.get(roomType);
    }
    public void setRoomOptionMessage(String roomType, String message) {
        if (message==null)
            roomOptionMessage.remove(roomType);
        else
            roomOptionMessage.put(roomType, message);
    }
    public boolean getRoomOptionScheduleEvents(String roomType) {
        Boolean ret = roomOptionScheduleEvents.get(roomType);
        return (ret!=null && ret.booleanValue());
    }
    public void setRoomOptionScheduleEvents(String roomType, boolean enable) {
        roomOptionScheduleEvents.put(roomType, enable);
    }

}
