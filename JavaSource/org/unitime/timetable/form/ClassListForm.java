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
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller, Zuzana Mullerova
 */
public class ClassListForm extends ActionForm implements ClassListFormInterface {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = -6985831814265952068L;
	private Collection classes;
	private Collection subjectAreas;
	private String[] subjectAreaIds; 
	private String courseNbr;
	private String buttonAction;
	private String subjectAreaAbbv;
	private String ctrlInstrOfferingId;
	private Boolean demandIsVisible;
	private Boolean demand;
	private Boolean limit;
	private Boolean roomLimit;
	private Boolean timePattern;
	private Boolean datePattern;
	private Boolean instructor;
	private Boolean preferences;
	private Boolean timetable;
	private Boolean manager;
	private Boolean divSec;
	private Boolean schedulePrintNote;
	private Boolean note;
	private Boolean exams;
	private Boolean instructorAssignment;
	private boolean includeCancelledClasses;
	private boolean filterNeedInstructor;
	
	private String sortBy;
	private String filterAssignedRoom;
	private String filterInstructor;
	private String filterManager;
	private String filterIType;
	private boolean filterAssignedTimeMon;
	private boolean filterAssignedTimeTue;
	private boolean filterAssignedTimeWed;
	private boolean filterAssignedTimeThu;
	private boolean filterAssignedTimeFri;
	private boolean filterAssignedTimeSat;
	private boolean filterAssignedTimeSun;
	private String filterAssignedTimeHour;
	private String filterAssignedTimeMin;
	private String filterAssignedTimeAmPm;
	private String filterAssignedTimeLength;
	private boolean sortByKeepSubparts;
	private boolean showCrossListedClasses;
	
	private boolean returnAllControlClassesForSubjects;
	private boolean sessionInLLREditStatus;
	
	/**
     * @return Returns the ctrlInstrOfferingId.
     */
    public String getCtrlInstrOfferingId() {
        return ctrlInstrOfferingId;
    }
    /**
     * @param ctrlInstrOfferingId The ctrlInstrOfferingId to set.
     */
    public void setCtrlInstrOfferingId(String ctrlInstrOfferingId) {
        this.ctrlInstrOfferingId = ctrlInstrOfferingId;
    }
    /**
     * @return Returns the subjectAreaAbbv.
     */
    public String getSubjectAreaAbbv() {
        return subjectAreaAbbv;
    }
    /**
     * @param subjectAreaAbbv The subjectAreaAbbv to set.
     */
    public void setSubjectAreaAbbv(String subjectAreaAbbv) {
        this.subjectAreaAbbv = subjectAreaAbbv;
    }
    /**
     * @return Returns the buttonAction.
     */
    public String getButtonAction() {
        return buttonAction;
    }
    /**
     * @param buttonAction The buttonAction to set.
     */
    public void setButtonAction(String buttonAction) {
        this.buttonAction = buttonAction;
    }
    /**
     * @return Returns the courseNbr.
     */
    public String getCourseNbr() {
        return courseNbr;
    }
    /**
     * @param courseNbr The courseNbr to set.
     */
    public void setCourseNbr(String courseNbr) {
        this.courseNbr = courseNbr;
    }
	/**
	 * @return Returns the subjectAreaIds.
	 */
	public String[] getSubjectAreaIds() {
		return subjectAreaIds;
	}
	/**
	 * @param subjectAreaIds The subjectAreaIds to set.
	 */
	public void setSubjectAreaIds(String[] subjectAreaIds) {
		this.subjectAreaIds = subjectAreaIds;
	}

	// --------------------------------------------------------- Methods
	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		courseNbr = "";
		classes = new ArrayList();
		subjectAreas = new ArrayList();
		subjectAreaIds = new String[0];
		
		demandIsVisible = new Boolean(false);
		demand = new Boolean(false);
		limit = new Boolean(false);
		roomLimit = new Boolean(false);
		datePattern = new Boolean(false);
		timePattern = new Boolean(false);
		instructor = new Boolean(false);
		preferences = new Boolean(false);
		timetable = new Boolean(false);
		manager = new Boolean(false);
		divSec = new Boolean(false);
		schedulePrintNote = new Boolean(false);
		note = new Boolean(false);
		exams = new Boolean(false);
		instructorAssignment = new Boolean(false);
		includeCancelledClasses = false;
		filterNeedInstructor = false;
		
		sortBy = ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME);
		filterInstructor = "";
		filterManager = "";
		filterAssignedRoom = "";
		filterIType = "";
		
		filterAssignedTimeMon = false;
		filterAssignedTimeTue = false;
		filterAssignedTimeWed = false;
		filterAssignedTimeThu = false;
		filterAssignedTimeFri = false;
		filterAssignedTimeSat = false;
		filterAssignedTimeSun = false;
		filterAssignedTimeHour = "";
		filterAssignedTimeMin = "";
		filterAssignedTimeAmPm = "";
		filterAssignedTimeLength = "";
		sortByKeepSubparts = false;
		showCrossListedClasses = false;
		
		returnAllControlClassesForSubjects = false;
		sessionInLLREditStatus = false;
		
		LookupTables.setupItypes(request,true);
	}

    /**
     * @return Returns the classes.
     */
    public Collection getClasses() {
        return classes;
    }
    /**
     * @param classes The classes to set.
     */
    public void setClasses(Collection classes) {
        this.classes = classes;
    }
    /**
     * @return Returns the subjectAreas.
     */
    public Collection getSubjectAreas() {
        return subjectAreas;
    }
    /**
     * @param subjectAreas The subjectAreas to set.
     */
    public void setSubjectAreas(Collection subjectAreas) {
        this.subjectAreas = subjectAreas;
    }
    /* (non-Javadoc)
     * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping, javax.servlet.http.HttpServletRequest)
     */
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }
    
	/**
	 * @return Returns the instructor.
	 */
	public Boolean getInstructor() {
		return instructor;
	}
	/**
	 * @param instructor The instructor to set.
	 */
	public void setInstructor(Boolean instructor) {
		this.instructor = instructor;
	}
	/**
	 * @return Returns the limit.
	 */
	public Boolean getLimit() {
		return limit;
	}
	/**
	 * @param limit The limit to set.
	 */
	public void setLimit(Boolean limit) {
		this.limit = limit;
	}
	/**
	 * @return Returns the preferences.
	 */
	public Boolean getPreferences() {
		return preferences;
	}
	/**
	 * @param preferences The preferences to set.
	 */
	public void setPreferences(Boolean preferences) {
		this.preferences = preferences;
	}
	/**
	 * @return Returns the roomLimit.
	 */
	public Boolean getRoomLimit() {
		return roomLimit;
	}
	/**
	 * @param roomLimit The roomLimit to set.
	 */
	public void setRoomLimit(Boolean roomLimit) {
		this.roomLimit = roomLimit;
	}
	/**
	 * @return Returns the datePattern.
	 */
	public Boolean getDatePattern() {
		return datePattern;
	}
	/**
	 * @param datePattern The datePattern to set.
	 */
	public void setDatePattern(Boolean datePattern) {
		this.datePattern = datePattern;
	}
	/**
	 * @return Returns the timePattern.
	 */
	public Boolean getTimePattern() {
		return timePattern;
	}
	/**
	 * @param timePattern The timePattern to set.
	 */
	public void setTimePattern(Boolean timePattern) {
		this.timePattern = timePattern;
	}
	/**
	 * @return Returns the timetable.
	 */
	public Boolean getTimetable() {
		return timetable;
	}
	/**
	 * @param timetable The timetable to set.
	 */
	public void setTimetable(Boolean timetable) {
		this.timetable = timetable;
	}
	
	public String getSortBy() { return sortBy; }
	public void setSortBy(String sortBy) { this.sortBy = sortBy; }
	public String[] getSortByOptions() { return ClassCourseComparator.getNames(); }
	public String getFilterManager() { return filterManager; }
	public void setFilterManager(String filterManager) { this.filterManager = filterManager; }
	public String getFilterAssignedRoom() { return filterAssignedRoom; }
	public void setFilterAssignedRoom(String filterAssignedRoom) { this.filterAssignedRoom = filterAssignedRoom; }
	public String getFilterInstructor() { return filterInstructor; }
	public void setFilterInstructor(String filterInstructor) { this.filterInstructor = filterInstructor; }
	public String getFilterIType() { return filterIType; }
	public void setFilterIType(String filterIType) { this.filterIType = filterIType; }

	public boolean getFilterAssignedTimeMon() { return filterAssignedTimeMon; }
	public void setFilterAssignedTimeMon(boolean filterAssignedTimeMon) { this.filterAssignedTimeMon = filterAssignedTimeMon; }
	public boolean getFilterAssignedTimeTue() { return filterAssignedTimeTue; }
	public void setFilterAssignedTimeTue(boolean filterAssignedTimeTue) { this.filterAssignedTimeTue = filterAssignedTimeTue; }
	public boolean getFilterAssignedTimeWed() { return filterAssignedTimeWed; }
	public void setFilterAssignedTimeWed(boolean filterAssignedTimeWed) { this.filterAssignedTimeWed = filterAssignedTimeWed; }
	public boolean getFilterAssignedTimeThu() { return filterAssignedTimeThu; }
	public void setFilterAssignedTimeThu(boolean filterAssignedTimeThu) { this.filterAssignedTimeThu = filterAssignedTimeThu; }
	public boolean getFilterAssignedTimeFri() { return filterAssignedTimeFri; }
	public void setFilterAssignedTimeFri(boolean filterAssignedTimeFri) { this.filterAssignedTimeFri = filterAssignedTimeFri; }
	public boolean getFilterAssignedTimeSat() { return filterAssignedTimeSat; }
	public void setFilterAssignedTimeSat(boolean filterAssignedTimeSat) { this.filterAssignedTimeSat = filterAssignedTimeSat; }
	public boolean getFilterAssignedTimeSun() { return filterAssignedTimeSun; }
	public void setFilterAssignedTimeSun(boolean filterAssignedTimeSun) { this.filterAssignedTimeSun = filterAssignedTimeSun; }
	public String getFilterAssignedTimeHour() { return filterAssignedTimeHour; }
	public void setFilterAssignedTimeHour(String filterAssignedTimeHour) { this.filterAssignedTimeHour = filterAssignedTimeHour; }
	public String getFilterAssignedTimeMin() { return filterAssignedTimeMin; }
	public void setFilterAssignedTimeMin(String filterAssignedTimeMin) { this.filterAssignedTimeMin = filterAssignedTimeMin; }
	public String getFilterAssignedTimeAmPm() { return filterAssignedTimeAmPm; }
	public void setFilterAssignedTimeAmPm(String filterAssignedTimeAmPm) { this.filterAssignedTimeAmPm = filterAssignedTimeAmPm; }
	public String getFilterAssignedTimeLength() { return ("0".equals(filterAssignedTimeLength)?"":filterAssignedTimeLength); }
	public void setFilterAssignedTimeLength(String filterAssignedTimeLength) { this.filterAssignedTimeLength = filterAssignedTimeLength; }
	
	public String[] getFilterAssignedTimeHours() {
		return new String[] { "", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" };
	}
	public String[] getFilterAssignedTimeMins() {
		return new String[] { "", "00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55" };
	}
	public String[] getFilterAssignedTimeAmPms() {
		return new String[] { "", MSG.timeAm(), MSG.timePm()};
	}
	public String[] getFilterAssignedTimeLengths() {
		String[] ret = new String[41];
		ret[0]="";
		for (int i=1;i<ret.length;i++)
			ret[i] = String.valueOf(5*i);
		return ret;
	}
	
	public int getFilterDayCode() {
		int dayCode = 0;
		if (filterAssignedTimeMon)
			dayCode += Constants.DAY_CODES[Constants.DAY_MON];
		if (filterAssignedTimeTue)
			dayCode += Constants.DAY_CODES[Constants.DAY_TUE];
		if (filterAssignedTimeWed)
			dayCode += Constants.DAY_CODES[Constants.DAY_WED];
		if (filterAssignedTimeThu)
			dayCode += Constants.DAY_CODES[Constants.DAY_THU];
		if (filterAssignedTimeFri)
			dayCode += Constants.DAY_CODES[Constants.DAY_FRI];
		if (filterAssignedTimeSat)
			dayCode += Constants.DAY_CODES[Constants.DAY_SAT];
		if (filterAssignedTimeSun)
			dayCode += Constants.DAY_CODES[Constants.DAY_SUN];
		return dayCode;
	}
	public void setFilterDayCode(int dayCode) {
		if (dayCode>=0) {
			filterAssignedTimeMon = (dayCode & Constants.DAY_CODES[Constants.DAY_MON]) != 0;
			filterAssignedTimeTue = (dayCode & Constants.DAY_CODES[Constants.DAY_TUE]) != 0;
			filterAssignedTimeWed = (dayCode & Constants.DAY_CODES[Constants.DAY_WED]) != 0;
			filterAssignedTimeThu = (dayCode & Constants.DAY_CODES[Constants.DAY_THU]) != 0;
			filterAssignedTimeFri = (dayCode & Constants.DAY_CODES[Constants.DAY_FRI]) != 0;
			filterAssignedTimeSat = (dayCode & Constants.DAY_CODES[Constants.DAY_SAT]) != 0;
			filterAssignedTimeSun = (dayCode & Constants.DAY_CODES[Constants.DAY_SUN]) != 0;
		} else {
			filterAssignedTimeMon = false;
			filterAssignedTimeTue = false;
			filterAssignedTimeWed = false;
			filterAssignedTimeThu = false;
			filterAssignedTimeFri = false;
			filterAssignedTimeSat = false;
			filterAssignedTimeSun = false;
		}
	}
	public int getFilterStartSlot() {
		try {
			int hour = Integer.parseInt(filterAssignedTimeHour);
			int min = Integer.parseInt(filterAssignedTimeMin);
			boolean morn = !(MSG.timePm().equals(filterAssignedTimeAmPm));
			if (hour==12) hour=0;
			int startTime = ((hour+(morn?0:12))%24)*60 + min;
			return (startTime - Constants.FIRST_SLOT_TIME_MIN) / Constants.SLOT_LENGTH_MIN;			
		} catch (NumberFormatException e) {
		} catch (NullPointerException e) {
		}
		return -1;
	}
	public void setFilterStartSlot(int startSlot) {
		if (startSlot>=0) {
			int startMin = startSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
			int min = startMin % 60;
			int startHour = startMin / 60;
			boolean morn = (startHour<12);
			int hour = startHour % 12;
			if (hour==0) hour = 12;
			filterAssignedTimeHour = String.valueOf(hour);
			filterAssignedTimeMin = (min<10?"0":"")+min;
			filterAssignedTimeAmPm = (morn?MSG.timeAm():MSG.timePm());
			
		} else {
			filterAssignedTimeHour = "";
			filterAssignedTimeMin = "";
			filterAssignedTimeAmPm = "";
		}
	}
	public int getFilterLength() {
		try {
			return Integer.parseInt(filterAssignedTimeLength);
		} catch (NumberFormatException e) {
		} catch (NullPointerException e) {
		}
		return 0;
	}
	public void setFilterLength(int length) {
		if (length>=0) {
			filterAssignedTimeLength = String.valueOf(length);
		} else {
			filterAssignedTimeLength = "";
		}
	}
	
	public boolean getSortByKeepSubparts() {
		return sortByKeepSubparts;
	}
	public void setSortByKeepSubparts(boolean sortByKeepSubparts) {
		this.sortByKeepSubparts = sortByKeepSubparts;
	}
	public boolean isReturnAllControlClassesForSubjects() {
		return returnAllControlClassesForSubjects;
	}
	public void setReturnAllControlClassesForSubjects(
			boolean returnAllControlClassesForSubjects) {
		this.returnAllControlClassesForSubjects = returnAllControlClassesForSubjects;
	}
	public boolean isSessionInLLREditStatus() {
		return sessionInLLREditStatus;
	}
	public void setSessionInLLREditStatus(boolean sessionInLLREditStatus) {
		this.sessionInLLREditStatus = sessionInLLREditStatus;
	}
	public Boolean getDemand() {
		return (demand);
	}
	public Boolean getProjectedDemand() {
		return (new Boolean(false));
	}
	public Boolean getMinPerWk() {
		return (new Boolean(false));
	}
	public Boolean getDivSec() {
		return divSec;
	}
	public void setDivSec(Boolean divSec) {
		this.divSec = divSec;
	}
	public Boolean getManager() {
		return manager;
	}
	public void setManager(Boolean manager) {
		this.manager = manager;
	}
	public Boolean getCredit() {
		return (new Boolean(false));
	}
	public Boolean getSubpartCredit() {
		return (new Boolean(false));
	}
	public Boolean getSchedulePrintNote() {
		return schedulePrintNote;
	}
	public void setSchedulePrintNote(Boolean schedulePrintNote) {
		this.schedulePrintNote = schedulePrintNote;
	}
	public Boolean getNote() {
		return note;
	}
	public void setNote(Boolean note) {
		this.note = note;
	}
    public Boolean getExams() {
        return exams;
    }
    public void setExams(Boolean exams) {
        this.exams = exams;
    }
    public Boolean getConsent() {
		return (new Boolean(false));
    }
    public Boolean getTitle() {
		return (new Boolean(false));
    }
	public void setDemand(Boolean demand) {
		this.demand = demand;
	}
	public Boolean getDemandIsVisible() {
		return demandIsVisible;
	}
	public void setDemandIsVisible(Boolean demandIsVisible) {
		this.demandIsVisible = demandIsVisible;
	}
	public boolean getShowCrossListedClasses() {
		return showCrossListedClasses;
	}
	public void setShowCrossListedClasses(boolean showCrossListedClasses) {
		this.showCrossListedClasses = showCrossListedClasses;
	}
    @Override
    public boolean getIncludeCancelledClasses() {
    	return includeCancelledClasses;
    }
    public void setIncludeCancelledClasses(boolean includeCancelledClasses) {
    	this.includeCancelledClasses = includeCancelledClasses;
    }	
    public Boolean getInstructorAssignment() {
    	return instructorAssignment;
    }
    public void setInstructorAssignment(Boolean instructorAssignment) {
    	this.instructorAssignment = instructorAssignment;
    }
    @Override
    public boolean getFilterNeedInstructor() {
    	return filterNeedInstructor;
    }
    public void setFilterNeedInstructor(boolean filterNeedInstructor) {
    	this.filterNeedInstructor = filterNeedInstructor;
    }
}
