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
import java.util.Iterator;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.dao.OverrideTypeDAO;


/** 
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
public class InstructionalOfferingDetailForm implements UniTimeForm {
	private static final long serialVersionUID = -5161466018324037153L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

    private String op;   
    private Long subjectAreaId;
    private Long crsOfferingId;
    private Long instrOfferingId;
    private Long ctrlCrsOfferingId;
    private Integer projectedDemand;
    private Integer enrollment;
    private Integer snapshotLimit;
    private Integer demand;
    private Integer limit;
    private Boolean unlimited;
    private Boolean notOffered;
    private String instrOfferingName;
    private String instrOfferingNameNoTitle;
    private List courseOfferings;
    private String subjectAreaAbbr;
    private String courseNbr;
    private String nextId;
    private String previousId;
    private String catalogLinkLabel;
    private String catalogLinkLocation;
    private boolean byReservationOnly;
    private String coordinators;
    private String wkEnroll, wkChange, wkDrop;
    private String waitList;
    private String weekStartDayOfWeek;
    private String accommodation;
    private boolean hasConflict;
    private String notes;
    private boolean teachingRequests;
    private String fundingDepartment;
    private boolean instructorSurvey;

    public InstructionalOfferingDetailForm() {
        reset();
    }

    /** 
     * Method validate
     */
    public void validate(UniTimeAction action) {
        throw new UnsupportedOperationException(
            MSG.exceptionValidateNotImplemented());
    }

    /** 
     * Method reset
     */
    public void reset() {
        op = "view";    
        subjectAreaId = null;
        subjectAreaAbbr = null;
        courseNbr = null;
        crsOfferingId = null;
        instrOfferingId = null;
        ctrlCrsOfferingId = null;
        enrollment = null;
        snapshotLimit = null;
        demand = null;
        projectedDemand = null;
        limit = null;
        unlimited = Boolean.valueOf(false);
        notOffered = null;
        instrOfferingName = "";
        instrOfferingNameNoTitle = "";
        courseOfferings = new ArrayList();
        nextId = previousId = null;
        catalogLinkLabel = null;
        catalogLinkLocation = null;
        byReservationOnly = false; coordinators = null;
        wkEnroll = null; wkChange = null; wkDrop = null;
        waitList = null;
        weekStartDayOfWeek = null;
        accommodation = null;
        hasConflict = false;
        notes = null;
        teachingRequests = false;
        fundingDepartment = null;
        instructorSurvey = false;
    }
    
    public List getCourseOfferings() {
        return courseOfferings;
    }
    public String getCourseOfferings(int key) {
        return courseOfferings.get(key).toString();
    }
    public void setCourseOfferings(int key, Object value) {
        this.courseOfferings.set(key, value);
    }
    public void setCourseOfferings(List courseOfferings) {
        this.courseOfferings = courseOfferings;
    }
    
    public Long getSubjectAreaId() {
        return subjectAreaId;
    }
    public void setSubjectAreaId(Long subjectAreaId) {
        this.subjectAreaId = subjectAreaId;
    }
    
    public Long getCrsOfferingId() {
        return crsOfferingId;
    }
    public void setCrsOfferingId(Long crsOfferingId) {
        this.crsOfferingId = crsOfferingId;
    }
    
    public Long getCtrlCrsOfferingId() {
        return ctrlCrsOfferingId;
    }
    public void setCtrlCrsOfferingId(Long ctrlCrsOfferingId) {
        this.ctrlCrsOfferingId = ctrlCrsOfferingId;
    }
    
    public Integer getDemand() {
        return demand;
    }
    public void setDemand(Integer demand) {
        this.demand = demand;
    }
    
    public Integer getProjectedDemand() {
        return projectedDemand;
    }
    public void setProjectedDemand(Integer projectedDemand) {
        this.projectedDemand = projectedDemand;
    }

    public Long getInstrOfferingId() {
        return instrOfferingId;
    }
    public void setInstrOfferingId(Long instrOfferingId) {
        this.instrOfferingId = instrOfferingId;
    }
    
    public Integer getLimit() {
        return limit;
    }    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public Boolean getUnlimited() {
        return unlimited;
    }    
    public void setUnlimited(Boolean unlimited) {
        this.unlimited = unlimited;
    }

    public Boolean getNotOffered() {
        return notOffered;
    }
    public void setNotOffered(Boolean notOffered) {
        this.notOffered = notOffered;
    }
    
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
        
    public String getSubjectAreaAbbr() {
        return subjectAreaAbbr;
    }
    public void setSubjectAreaAbbr(String subjectAreaAbbr) {
        this.subjectAreaAbbr = subjectAreaAbbr;
    }
    
    public String getCourseNbr() {
        return courseNbr;
    }
    public void setCourseNbr(String courseNbr) {
        this.courseNbr = courseNbr;
    }
    
    public String getInstrOfferingName() {
        return instrOfferingName;
    }
    public void setInstrOfferingName(String instrOfferingName) {
        this.instrOfferingName = instrOfferingName;
    }    
    public String getInstrOfferingNameNoTitle() {
        return instrOfferingNameNoTitle;
    }
    public void setInstrOfferingNameNoTitle(String instrOfferingNameNoTitle) {
        this.instrOfferingNameNoTitle = instrOfferingNameNoTitle;
    }    
        
    public String getCatalogLinkLabel() {
		return catalogLinkLabel;
	}

	public void setCatalogLinkLabel(String catalogLinkLabel) {
		this.catalogLinkLabel = catalogLinkLabel;
	}

	public String getCatalogLinkLocation() {
		return catalogLinkLocation;
	}

	public void setCatalogLinkLocation(String catalogLinkLocation) {
		this.catalogLinkLocation = catalogLinkLocation;
	}

	/**
     * Add a course offering to the existing list
     * @param co Course Offering
     */
    public void addToCourseOfferings(CourseOffering co) {
        this.courseOfferings.add(co);
    }

    /**
     * @return No. of course offerings in the instr offering
     */
    public Integer getCourseOfferingCount() {
        return Integer.valueOf(this.courseOfferings.size());
    }
    
    public String getNextId() { return nextId; }
    public void setNextId(String nextId) { this.nextId = nextId; }
    public String getPreviousId() { return previousId; }
    public void setPreviousId(String previousId) { this.previousId = previousId; }
    public boolean getHasDemandOfferings() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getDemandOffering()!=null) return true;
    	return false;
    }
    public boolean getHasAlternativeCourse() {
    	if (courseOfferings==null || courseOfferings.isEmpty() || ApplicationProperty.StudentSchedulingAlternativeCourse.isFalse()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getAlternativeOffering()!=null) return true;
    	return false;
    }
    
    public boolean getHasParentCourse() {
    	if (courseOfferings==null || courseOfferings.isEmpty() || ApplicationProperty.StudentSchedulingParentCourse.isFalse()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getParentOffering()!=null) return true;
    	return false;
    }

	public Integer getEnrollment() {
		return enrollment;
	}

	public void setEnrollment(Integer enrollment) {
		this.enrollment = enrollment;
	}
	
	public Integer getSnapshotLimit() {
		return snapshotLimit;
	}

	public void setSnapshotLimit(Integer snapshotLimit) {
		this.snapshotLimit = snapshotLimit;
	}
	
	public boolean isByReservationOnly() { return byReservationOnly; }
	public void setByReservationOnly(boolean byReservationOnly) { this.byReservationOnly = byReservationOnly; }
	
	public String getCoordinators() { return coordinators; }
	public void setCoordinators(String coordinators) { this.coordinators = coordinators; }
	
    public String getWkEnroll() { return wkEnroll; }
    public void setWkEnroll(String wkEnroll) { this.wkEnroll = wkEnroll; }

    public String getWkChange() { return wkChange; }
    public void setWkChange(String wkChange) { this.wkChange = wkChange; }

    public String getWkDrop() { return wkDrop; }
    public void setWkDrop(String wkDrop) { this.wkDrop = wkDrop; }
    
    public String getWeekStartDayOfWeek() { return weekStartDayOfWeek; }
    public void setWeekStartDayOfWeek(String weekStartDayOfWeek) { this.weekStartDayOfWeek = weekStartDayOfWeek; }
    
    public String getWaitList() { return waitList; }
    public void setWaitList(String waitList) { this.waitList = waitList; }
    
    public boolean isDisplayEnrollmentDeadlineNote() {
    	return (wkEnroll != null && !wkEnroll.isEmpty()) || (wkChange != null && !wkChange.isEmpty()) || (wkDrop != null && !wkDrop.isEmpty());
    }
    
    public boolean getHasCourseTypes() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getCourseType()!=null) return true;
    	return false;
    }
    
    public boolean getHasConsent() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getConsentType()!=null) return true;
    	return false;
    }
    
    public String getAccommodation() { return accommodation; }
    public void setAccommodation(String accommodation) { this.accommodation = accommodation; }
    
    public boolean getHasCredit() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getCredit() != null) return true;
    	return false;
    }
    
    public boolean getHasScheduleBookNote() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();) {
    		CourseOffering course = (CourseOffering)i.next();
    		if (course.getScheduleBookNote() != null && !course.getScheduleBookNote().isEmpty()) return true;
    	}
    	return false;
    }

    public boolean getHasCourseReservation() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getReservation() != null) return true;
    	return false;
    }

	public boolean isHasConflict() { return hasConflict; }
	public void setHasConflict(boolean hasConflict) { this.hasConflict = hasConflict; }

    public boolean getHasCourseExternalId() {
    	if (!ApplicationProperty.CourseOfferingShowExternalIds.isTrue()) return false;
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();) {
    		CourseOffering co = (CourseOffering)i.next();
    		if (co.getExternalUniqueId() != null && !co.getExternalUniqueId().isEmpty()) return true;
    	}
    	return false;
    }
    
    public boolean getHasDisabledOverrides() {
    	for (Iterator i=courseOfferings.iterator();i.hasNext();) {
    		CourseOffering co = (CourseOffering)i.next();
    		if (!co.getDisabledOverrides().isEmpty()) return true;
    	}
    	return false; 
    }
    
    public boolean getHasOverrides() {
    	return !OverrideTypeDAO.getInstance().findAll().isEmpty();
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean getTeachingRequests() { return teachingRequests; }
    public void setTeachingRequests(boolean teachingRequests) { this.teachingRequests = teachingRequests; }
    public String getFundingDepartment() { return fundingDepartment; }
    public void setFundingDepartment(String fundingDepartment) { this.fundingDepartment = fundingDepartment; }
    public boolean getInstructorSurvey() { return instructorSurvey; }
    public void setInstructorSurvey(boolean instructorSurvey) { this.instructorSurvey = instructorSurvey; }
}
