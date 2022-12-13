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
import java.util.Map;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassCourseComparator;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
public class InstructionalOfferingListForm implements UniTimeForm, InstructionalOfferingListFormInterface {
	private static final long serialVersionUID = -6985831814265952068L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);


	private Map<Long, TreeSet<InstructionalOffering>> instructionalOfferings;

	private Collection<SubjectArea> subjectAreas;

	private String[] subjectAreaIds;

	private String courseNbr;

	private Boolean showNotOffered;

	private String buttonAction;

	private String subjectAreaAbbv;

	private Boolean isControl;

	private String ctrlInstrOfferingId;

	private Boolean divSec;

	private Boolean demand;

	private Boolean projectedDemand;

	private Boolean minPerWk;

	private Boolean limit;

	private Boolean snapshotLimit;

	private Boolean roomLimit;

	private Boolean manager;

	private Boolean datePattern;

	private Boolean timePattern;

	private Boolean preferences;

	private Boolean instructor;

	private Boolean timetable;

	private Boolean credit;

	private Boolean subpartCredit;

	private Boolean schedulePrintNote;

	private Boolean note;

	private Boolean consent;
	
	private Boolean title;
	
	private Boolean exams;
	
	private Boolean fundingDepartment;
	
	private String sortBy;
	
	private Boolean instructorAssignment;
	
	private Boolean lms;
	
	private String waitlist;

	/**
	 * @return Returns the ctrlInstrOfferingId.
	 */
	public String getCtrlInstrOfferingId() {
		return ctrlInstrOfferingId;
	}

	/**
	 * @param ctrlInstrOfferingId
	 *            The ctrlInstrOfferingId to set.
	 */
	public void setCtrlInstrOfferingId(String ctrlInstrOfferingId) {
		this.ctrlInstrOfferingId = ctrlInstrOfferingId;
	}

	/**
	 * @return Returns the isControl.
	 */
	public Boolean getIsControl() {
		return isControl;
	}

	/**
	 * @param isControl
	 *            The isControl to set.
	 */
	public void setIsControl(Boolean isControl) {
		this.isControl = isControl;
	}

	/**
	 * @return Returns the subjectAreaAbbv.
	 */
	public String getSubjectAreaAbbv() {
		return subjectAreaAbbv;
	}

	/**
	 * @param subjectAreaAbbv
	 *            The subjectAreaAbbv to set.
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
	 * @param buttonAction
	 *            The buttonAction to set.
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
	 * @param courseNbr
	 *            The courseNbr to set.
	 */
	public void setCourseNbr(String courseNbr) {
        if (ApplicationProperty.CourseOfferingNumberUpperCase.isTrue())
        	courseNbr = courseNbr.toUpperCase();
		this.courseNbr = courseNbr;
	}

	/**
	 * @return Returns the subjectAreaId.
	 */
	public String[] getSubjectAreaIds() {
		return subjectAreaIds;
	}

	/**
	 * @param subjectAreaId
	 *            The subjectAreaId to set.
	 */
	public void setSubjectAreaIds(String[] subjectAreaIds) {
		this.subjectAreaIds = subjectAreaIds;
	}

	// --------------------------------------------------------- Methods
	/**
	 * Method reset
	 */
	@Override
	public void reset() {
		courseNbr = "";
		instructionalOfferings = null;
		subjectAreas = new ArrayList();
		divSec = false;
		demand = false;
		projectedDemand = false;
		minPerWk = false;
		limit = false;
		snapshotLimit = false;
		roomLimit = false;
		manager = false;
		datePattern = false;
		timePattern = false;
		preferences = false;
		instructor = false;
		timetable = false;
		credit = false;
		subpartCredit = false;
		schedulePrintNote = false;
		note = false;
		title = false;
		consent = false;
		exams = false;
		fundingDepartment = false;
		instructorAssignment = false;
		lms = false;
		waitlist = null;
		sortBy = ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME);
	}

	/**
	 * @return Returns the instructionalOfferings.
	 */
	public Map<Long, TreeSet<InstructionalOffering>> getInstructionalOfferings() {
		return instructionalOfferings;
	}
	
	public TreeSet<InstructionalOffering> getInstructionalOfferings(Long subjectAreaId) {
		return instructionalOfferings == null ? null : instructionalOfferings.get(subjectAreaId);
	}

	/**
	 * @param instructionalOfferings
	 *            The instructionalOfferings to set.
	 */
	public void setInstructionalOfferings(Map<Long, TreeSet<InstructionalOffering>> instructionalOfferings) {
		this.instructionalOfferings = instructionalOfferings;
	}

	/**
	 * @return Returns the subjectAreas.
	 */
	public Collection<SubjectArea> getSubjectAreas() {
		return subjectAreas;
	}

	/**
	 * @param subjectAreas
	 *            The subjectAreas to set.
	 */
	public void setSubjectAreas(Collection<SubjectArea> subjectAreas) {
		this.subjectAreas = subjectAreas;
	}

	/**
	 * @return Returns the showNotOffered.
	 */
	public Boolean getShowNotOffered() {
		return showNotOffered;
	}

	/**
	 * @param showNotOffered
	 *            The showNotOffered to set.
	 */
	public void setShowNotOffered(Boolean showNotOffered) {
		this.showNotOffered = showNotOffered;
	}

	public void validate(UniTimeAction action) {
		if (subjectAreaIds == null || subjectAreaIds.length == 0)
			action.addFieldError("subjectAreaIds", MSG.errorSubjectRequired());
	}
	
	public Boolean getDatePattern() {
		return datePattern;
	}

	public void setDatePattern(Boolean datePattern) {
		this.datePattern = datePattern;
	}

	public Boolean getDemand() {
		return demand;
	}

	public void setDemand(Boolean demand) {
		this.demand = demand;
	}

	public Boolean getDivSec() {
		return divSec;
	}

	public void setDivSec(Boolean divSec) {
		this.divSec = divSec;
	}

	public Boolean getInstructor() {
		return instructor;
	}

	public void setInstructor(Boolean instructor) {
		this.instructor = instructor;
	}

	public Boolean getLimit() {
		return limit;
	}

	public void setLimit(Boolean limit) {
		this.limit = limit;
	}

	public Boolean getSnapshotLimit() {
		return snapshotLimit;
	}

	public void setSnapshotLimit(Boolean snapshotLimit) {
		this.snapshotLimit = snapshotLimit;
	}

	public Boolean getManager() {
		return manager;
	}

	public void setManager(Boolean manager) {
		this.manager = manager;
	}

	public Boolean getMinPerWk() {
		return minPerWk;
	}

	public void setMinPerWk(Boolean minPerWk) {
		this.minPerWk = minPerWk;
	}

	public Boolean getPreferences() {
		return preferences;
	}

	public void setPreferences(Boolean preferences) {
		this.preferences = preferences;
	}

	public Boolean getProjectedDemand() {
		return projectedDemand;
	}

	public void setProjectedDemand(Boolean projectedDemand) {
		this.projectedDemand = projectedDemand;
	}

	public Boolean getRoomLimit() {
		return roomLimit;
	}

	public void setRoomLimit(Boolean roomLimit) {
		this.roomLimit = roomLimit;
	}

	public Boolean getTimePattern() {
		return timePattern;
	}

	public void setTimePattern(Boolean timePattern) {
		this.timePattern = timePattern;
	}

	public Boolean getTimetable() {
		return timetable;
	}

	public void setTimetable(Boolean timetable) {
		this.timetable = timetable;
	}

	public Boolean getCredit() {
		return credit;
	}

	public void setCredit(Boolean credit) {
		this.credit = credit;
	}

	public Boolean getSubpartCredit() {
		return subpartCredit;
	}

	public void setSubpartCredit(Boolean subpartCredit) {
		this.subpartCredit = subpartCredit;
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
	
    public Boolean getConsent() {
        return consent;
    }
    
    public void setConsent(Boolean consent) {
        this.consent = consent;
    }
    
    public Boolean getTitle() {
        return title;
    }
    
    public void setTitle(Boolean title) {
        this.title = title;
    }
    
    public Boolean getExams() {
        return exams;
    }
    public void setExams(Boolean exams) {
        this.exams = exams;
    }
    public void setFundingDepartment(Boolean fundingDepartment) {
        this.fundingDepartment = fundingDepartment;
    }
    
    public Boolean getInstructorAssignment() {
    	return instructorAssignment;
    }
    
    public void setInstructorAssignment(Boolean instructorAssignment) {
    	this.instructorAssignment = instructorAssignment;
    }
    
    public String getWaitlist() {
    	return waitlist;
    }
    
    public void setWaitlist(String waitlist) {
    	this.waitlist = waitlist;
    }

	public String getSortBy() { return sortBy; }
	public void setSortBy(String sortBy) { this.sortBy = sortBy; }
	public String[] getSortByOptions() { return ClassCourseComparator.getNames(); }
	
	public Boolean getEnrollmentInformation(){
		return(Boolean.valueOf(getDemand().booleanValue() 
				&& getProjectedDemand().booleanValue()
				&& getLimit().booleanValue()
				&& getRoomLimit().booleanValue()));
	}
	public void setEnrollmentInformation(){
		; //do nothing
	}
	
	public Boolean getDateTimeInformation(){
		return(Boolean.valueOf(getDatePattern().booleanValue() 
				&& getMinPerWk().booleanValue()
				&& getTimePattern().booleanValue()));
	}
	public void setDateTimeInformation(){
		; //do nothing
	}
	public Boolean getCatalogInformation(){
		return(Boolean.valueOf(getTitle().booleanValue() 
				&& getCredit().booleanValue()
				&& getSubpartCredit().booleanValue()
				&& getConsent().booleanValue()
				&& getSchedulePrintNote().booleanValue())
				&& getLms().booleanValue());
	}
	public void setCatalogInformation(){
		; //do nothing
	}
	
	public boolean areAllCoursesGiven() {
		return (getCourseNbr()==null || getCourseNbr().isEmpty()) && ("A".equals(getWaitlist()) || getWaitlist() == null || getWaitlist().isEmpty());
	}

	public Boolean getFundingDepartment() {
		return fundingDepartment;
	}

	public Boolean getLms() {
		return lms;
	}

	public void setLms(Boolean lms) {
		this.lms = lms;
	}
	
	public int getSubjectAreaListSize() {
		return Math.min(7, getSubjectAreas().size());
	}
}
