/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.IdValue;
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
	Integer wkEnroll = 1, wkChange = 1, wkDrop = 4;
	Long sectStatus;
	boolean includeTestSession;
	
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
				Integer.parseInt(academicYear); 
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
                            else if (!CalendarUtils.isValidDate(eventEnd, df))
                                errors.add("eventEnd", new ActionMessage("errors.invalidDate", "Event End Date"));
                            Date d4 = CalendarUtils.getDate(eventStart, df);
                            Date d5 = CalendarUtils.getDate(eventEnd, df);
                            if (errors.isEmpty() && !d4.before(d5)) {
                                errors.add("eventEnd", new ActionMessage("errors.generic", "Event End Date must occur AFTER Event Start Date"));
                            }
                            Calendar start = Calendar.getInstance(Locale.US);
                            if (d4 != null){
	                            if (d4.before(d1)){
	                            	start.setTime(d4);
	                            } else {
	                            	start.setTime(d1);
	                            }
                            } else {
                            	start.setTime(d1);
                            }
                            Calendar end = Calendar.getInstance(Locale.US);
                            if(d5 != null){
	                            if (d5.after(d2)){
	                            	end.setTime(d5);
	                            } else {
	                            	end.setTime(d2);
	                            }
                            } else {
                            	end.setTime(d2);
                            }
                            int startYear = start.get(Calendar.YEAR);
                            int endYear = end.get(Calendar.YEAR);
                            int startMonth = start.get(Calendar.MONTH);
                            int endMonth = end.get(Calendar.MONTH);
                            int startDay = start.get(Calendar.DAY_OF_MONTH);
                            int endDay = end.get(Calendar.DAY_OF_MONTH);
                            if (startYear < endYear) {
                            	if (startYear + 1 < endYear || startMonth < endMonth || (startMonth == endMonth && startDay <= endDay))
                            		errors.add("sessionDays", new ActionMessage("errors.generic", "Dates associated with a session cannot cover more than one year."));
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
			statusOptions = Session.getSessionStatusList(includeTestSession);
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
        
    public Integer getWkEnroll() { return wkEnroll; }
    public void setWkEnroll(Integer wkEnroll) { this.wkEnroll = wkEnroll; }

    public Integer getWkChange() { return wkChange; }
    public void setWkChange(Integer wkChange) { this.wkChange = wkChange; }

    public Integer getWkDrop() { return wkDrop; }
    public void setWkDrop(Integer wkDrop) { this.wkDrop = wkDrop; }
    
    public Long getSectStatus() { return sectStatus; }
    public void setSectStatus(Long sectStatus) { this.sectStatus = sectStatus; }
    public List<IdValue> getSectStates() {
    	List<IdValue> ret = new ArrayList<IdValue>();
    	for (StudentSectioningStatus status: StudentSectioningStatusDAO.getInstance().findAll())
    		ret.add(new IdValue(status.getUniqueId(), status.getLabel()));
    	return ret;
    }
    
    public void setIncludeTestSession(boolean includeTestSession) { this.includeTestSession = includeTestSession; }

}
