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

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
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
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class SessionEditForm implements UniTimeForm {
	private static final long serialVersionUID = 3258410646873060656L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
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
	Long defaultDatePatternId;
	String defaultDatePatternLabel;
	Integer wkEnroll = 1, wkChange = 1, wkDrop = 4;
	Long sectStatus;
	boolean includeTestSession;
	Long durationType;
	Long instructionalMethod;
	
	public SessionEditForm() {
		reset();
	}
	
	@Override
	public void reset() {
		academicInitiative = null;
		academicYear = null;
		academicTerm = null;
		sessionStart = null;
		sessionEnd = null;
		classesEnd = null;
		examStart = null;
		eventStart = null;
		eventEnd = null;
		defaultDatePatternId = null;
		defaultDatePatternLabel = null;
		wkEnroll = 1; wkChange = 1; wkDrop = 4;
		sectStatus = null;
		includeTestSession = false;
		durationType = null;
		instructionalMethod = null;
	}
	
	@Override
	public void validate(UniTimeAction action) {
		// Check data fields
		if (academicInitiative==null || academicInitiative.trim().length()==0)
			action.addFieldError("form.academicInitiative", MSG.errorRequiredField(MSG.columnAcademicInitiative()));
		
		if (academicTerm==null || academicTerm.trim().length()==0) 
			action.addFieldError("form.academicTerm", MSG.errorRequiredField(MSG.columnAcademicTerm()));

		if (academicYear==null || academicYear.trim().length()==0) 
			action.addFieldError("form.academicYear", MSG.errorRequiredField(MSG.columnAcademicYear()));
		else {
			try {
				Integer.parseInt(academicYear); 
			}
			catch (Exception e) {
				action.addFieldError("form.academicYear", MSG.errorNotNumber(MSG.columnAcademicYear()));
			}
		}
		
		validateDates(action);
		
		if (getStatus()==null || getStatus().trim().length()==0) 
			action.addFieldError("form.status", MSG.errorRequiredField(MSG.columnSessionStatus()));
		
		// Check for duplicate academic initiative, year & term
		if (!action.hasFieldErrors()) {
			Session sessn = Session.getSessionUsingInitiativeYearTerm(academicInitiative, academicYear, academicTerm);
			if (session.getSessionId()==null && sessn!=null)
				action.addFieldError("form.sessionId", MSG.errorAcademicSessionAlreadyExists());
			if (session.getSessionId()!=null && sessn!=null) {
				if (!session.getSessionId().equals(sessn.getSessionId()))
					action.addFieldError("form.sessionId", MSG.errorAcademicSessionSameAlreadyExists());
			}
		}
	}

	
	/**
	 * Validates all the dates
	 */
	public boolean validateDates(UniTimeAction action) {
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
		Date dStart = null;
		try {
			dStart = df.parse(sessionStart);
		} catch (Exception e) {}
		if (dStart == null) {
			action.addFieldError("form.sessionStart", MSG.errorNotValidDate(MSG.columnSessionStartDate()));
			return false;
		}
		Date dEnd = null;
		try {
			dEnd = df.parse(sessionEnd);
		} catch (Exception e) {}
		if (dEnd == null) {
			action.addFieldError("form.sessionEnd", MSG.errorNotValidDate(MSG.columnSessionEndDate()));
			return false;
		}
		if (!dEnd.after(dStart)) { 
			action.addFieldError("form.sessionEnd", MSG.errorSessionEndDateNotAfterSessionStartDate());
			return false;
		}
		Date dClassEnd = null;
		try {
			dClassEnd = df.parse(classesEnd);
		} catch (Exception e) {}
		if (dClassEnd == null) {
			action.addFieldError("form.classesEnd", MSG.errorNotValidDate(MSG.columnClassesEndDate()));
			return false;
		}
		if (!dClassEnd.after(dStart)) {
			action.addFieldError("form.classesEnd", MSG.errorClassesEndDateNotAfterSessionStartDate());
			return false;
		} else if (!(dClassEnd.before(dEnd) || dClassEnd.equals(dEnd))) { 
			action.addFieldError("form.classesEnd", MSG.errorClassesEndDateNotOnOrBeforeSessionEndDate());
			return false;
		}
		Date dExamStart = null;
		try {
			dExamStart = df.parse(examStart);
		} catch (Exception e) {}
		if (dExamStart == null) {
	        action.addFieldError("form.examStart", MSG.errorNotValidDate(MSG.columnExamStartDate()));
	        return false;
		}
		Date dEventStart = null;
		try {
			dEventStart = df.parse(eventStart);
		} catch (Exception e) {}
		Date dEventEnd = null;
		try {
			dEventEnd = df.parse(eventEnd);
		} catch (Exception e) {}
		if (dEventStart == null) {
            action.addFieldError("form.eventStart", MSG.errorNotValidDate(MSG.columnEventStartDate()));
            return false;
		} else if (dEventEnd == null) {
            action.addFieldError("form.eventEnd", MSG.errorNotValidDate(MSG.columnEventEndDate()));
            return false;
		}
		if (!dEventStart.before(dEventEnd)) {
            action.addFieldError("form.eventEnd", MSG.errorEventEndDateNotAfterEventStartDate());
            return false;
        }
		Calendar start = Calendar.getInstance(Locale.US);
		if (dEventStart.before(dStart)){
        	start.setTime(dEventStart);
        } else {
        	start.setTime(dStart);
        }
        Calendar end = Calendar.getInstance(Locale.US);
        if (dEventEnd.after(dEnd)){
        	end.setTime(dEventEnd);
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
        	if (startYear + 1 < endYear || startMonth < endMonth || (startMonth == endMonth && startDay <= endDay)) {
        		action.addFieldError("form.sessionDays", MSG.errorSessionDatesOverAYear());
        		return false; 
        	}
        }
        return true;
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
	
	public Long getDefaultDatePatternId() {
        return defaultDatePatternId;
    }
    public void setDefaultDatePatternId(Long defaultDatePatternId) {
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
