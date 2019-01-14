/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.IdValue;
import org.unitime.timetable.util.ReferenceList;


/** 
 * MyEclipse Struts
 * Creation date: 02-18-2005
 * 
 * XDoclet definition:
 * @struts:form name="sessionEditForm"
 *
 * @author Tomas Muller, Stephanie Schluttenhofer
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
	Long durationType;
	Long instructionalMethod;
	
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
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
		Date dStart = null;
		try {
			dStart = df.parse(sessionStart);
		} catch (Exception e) {}
		if (dStart == null)
			errors.add("sessionStart", new ActionMessage("errors.invalidDate", "Session Start Date"));
		else {
			Date dEnd = null;
			try {
				dEnd = df.parse(sessionEnd);
			} catch (Exception e) {}
			if (dEnd == null)
				errors.add("sessionEnd", new ActionMessage("errors.invalidDate", "Session End Date"));
			else {
				if (!dEnd.after(dStart)) 
					errors.add("sessionEnd", new ActionMessage("errors.generic", "Session End Date must occur AFTER Session Start Date"));
				else {
					Date dClassEnd = null;
					try {
						dClassEnd = df.parse(classesEnd);
					} catch (Exception e) {}
					if (dClassEnd == null)
						errors.add("classesEnd", new ActionMessage("errors.invalidDate", "Classes End Date"));
					else {
						if (!dClassEnd.after(dStart)) {
							errors.add("classesEnd", new ActionMessage("errors.generic", "Classes End Date must occur AFTER Session Start Date"));
						} else if (!(dClassEnd.before(dEnd) || dClassEnd.equals(dEnd))) { 
							errors.add("classesEnd", new ActionMessage("errors.generic", "Classes End Date must occur ON or BEFORE Session End Date"));
						} else {
							Date dExamStart = null;
							try {
								dExamStart = df.parse(examStart);
							} catch (Exception e) {}
							if (dExamStart == null)
						        errors.add("examStart", new ActionMessage("errors.invalidDate", "Examinations Start Date"));

							Date dEventStart = null;
							try {
								dEventStart = df.parse(eventStart);
							} catch (Exception e) {}
							Date dEventEnd = null;
							try {
								dEventEnd = df.parse(eventEnd);
							} catch (Exception e) {}
							if (dEventStart == null)
                                errors.add("eventStart", new ActionMessage("errors.invalidDate", "Event Start Date"));
                            else if (dEventEnd == null)
                                errors.add("eventEnd", new ActionMessage("errors.invalidDate", "Event End Date"));
                            if (errors.isEmpty() && !dEventStart.before(dEventEnd)) {
                                errors.add("eventEnd", new ActionMessage("errors.generic", "Event End Date must occur AFTER Event Start Date"));
                            }
                            Calendar start = Calendar.getInstance(Locale.US);
                            if (dEventStart != null){
	                            if (dEventStart.before(dStart)){
	                            	start.setTime(dEventStart);
	                            } else {
	                            	start.setTime(dStart);
	                            }
                            } else {
                            	start.setTime(dStart);
                            }
                            Calendar end = Calendar.getInstance(Locale.US);
                            if(dEventEnd != null){
	                            if (dEventEnd.after(dEnd)){
	                            	end.setTime(dEventEnd);
	                            } else {
	                            	end.setTime(dEnd);
	                            }
                            } else {
                            	end.setTime(dEnd);
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
    	for (StudentSectioningStatus status: StudentSectioningStatus.findAll(getSessionId()))
    		ret.add(new IdValue(status.getUniqueId(), status.getLabel()));
    	return ret;
    }
    
    public Long getDurationType() { return durationType; }
    public void setDurationType(Long durationType) { this.durationType = durationType; }
    public List<IdValue> getDurationTypes() {
    	List<IdValue> ret = new ArrayList<IdValue>();
    	for (ClassDurationType type: ClassDurationType.findAll())
    		if (type.isVisible() || type.getUniqueId().equals(durationType))
    			ret.add(new IdValue(type.getUniqueId(), type.getLabel()));
    	return ret;
    }
    
    public Long getInstructionalMethod() { return instructionalMethod; }
    public void setInstructionalMethod(Long instructionalMethod) { this.instructionalMethod = instructionalMethod; }
    public List<IdValue> getInstructionalMethods() {
    	List<IdValue> ret = new ArrayList<IdValue>();
    	for (InstructionalMethod im: InstructionalMethod.findAll())
    		// if (im.isVisible() || im.getUniqueId().equals(instructionalMethod))
    		ret.add(new IdValue(im.getUniqueId(), im.getLabel()));
    	return ret;
    }
    
    public void setIncludeTestSession(boolean includeTestSession) { this.includeTestSession = includeTestSession; }

}
