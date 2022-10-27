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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.criterion.Order;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.OverrideType;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.dao.OverrideTypeDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.ComboBoxLookup;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
public class CourseOfferingEditForm implements UniTimeForm {
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	private static final long serialVersionUID = 5719027599139781262L;

    private String op;
    private boolean add = false;
    private Long subjectAreaId;
    private Long courseOfferingId;
    private Long instrOfferingId;
    private String courseName;
    private String courseNbr;
    private String title;
    private String scheduleBookNote;
    private Long demandCourseOfferingId;
    private boolean allowDemandCourseOfferings;
    private Long consent;
    private String creditFormat;
    private Long creditType;
    private Long creditUnitType;
    private Float units;
    private Float maxUnits;
    private Boolean fractionalIncrementsAllowed;
    private String creditText;
    private Boolean isControl;
    private Boolean ioNotOffered;
    private String catalogLinkLabel;
    private String catalogLinkLocation;
    private Boolean byReservationOnly;
    private List<String> instructors;
    private List<String> responsibilities;
    private List<String> percentShares;
    private String wkEnroll, wkChange, wkDrop;
    private Integer wkEnrollDefault, wkChangeDefault, wkDropDefault;
    private String weekStartDayOfWeek;
    private String courseTypeId;
    private String externalId;
    private Long alternativeCourseOfferingId;
    private boolean allowAlternativeCourseOfferings;
    private String notes;
    private String defaultTeachingResponsibilityId;
    private Set<String> overrides;
    private String waitList;
    
    public CourseOfferingEditForm() {
        reset();
    }

    @Override
    public void validate(UniTimeAction action) {
        if (getCourseNbr() != null && ApplicationProperty.CourseOfferingNumberUpperCase.isTrue()) {
        	setCourseNbr(getCourseNbr().toUpperCase());
        }

		if (subjectAreaId == null || subjectAreaId == 0) {
			action.addFieldError("form.subjectAreaId", MSG.errorSubjectRequired());
		} else if (courseNbr==null || courseNbr.trim().length()==0) {
			action.addFieldError("form.courseNbr", MSG.errorCourseNumberRequired());
		} else {
	    	String courseNbrRegex = ApplicationProperty.CourseOfferingNumberPattern.value(); 
	    	String courseNbrInfo = ApplicationProperty.CourseOfferingNumberPatternInfo.value();
	    	try { 
		    	Pattern pattern = Pattern.compile(courseNbrRegex);
		    	Matcher matcher = pattern.matcher(courseNbr);
		    	if (!matcher.find()) {
		    		action.addFieldError("form.courseNbr", courseNbrInfo);
		    	}
	    	}
	    	catch (Exception e) {
	    		action.addFieldError("form.courseNbr", MSG.errorCourseNumberCannotBeMatched(courseNbrRegex,e.getMessage()));
	    	}

			
	    	if (ApplicationProperty.CourseOfferingNumberMustBeUnique.isTrue()){
				SubjectArea sa = new SubjectAreaDAO().get(subjectAreaId);
				CourseOffering co = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(sa.getSessionId(), sa.getSubjectAreaAbbreviation(), courseNbr);
				if (add && co != null) {
					action.addFieldError("form.courseNbr", MSG.errorCourseCannotBeCreated());
				} else if (!add && co!=null && !co.getUniqueId().equals(courseOfferingId)) {
					action.addFieldError("form.courseNbr", MSG.errorCourseCannotBeRenamed());
				}
	    	}

		}
		
		for (int i = 0; i < instructors.size(); i++) {
			String id1 = (String)instructors.get(i);
			String r1 = (String)responsibilities.get(i);
			if (Preference.BLANK_PREF_VALUE.equals(id1)) continue;
			
			for (int j = i + 1; j < instructors.size(); j++) {
				String id2 = (String)instructors.get(j);
				String r2 = (String)responsibilities.get(j);
				if (id1.equals(id2) && r1.equals(r2)) {
					action.addFieldError("form.instructors", MSG.errorDuplicateCoordinator());
				}
			}
		}
		
		OverrideType prohibitedOverride = OverrideType.findByReference(ApplicationProperty.OfferingWaitListProhibitedOverride.value());
		if (prohibitedOverride != null && (waitList == null || waitList.isEmpty() ? ApplicationProperty.OfferingWaitListDefault.isTrue() : "true".equalsIgnoreCase(waitList)) && !overrides.contains(prohibitedOverride.getUniqueId().toString()))
			action.addFieldError("form.waitList", MSG.errorWaitListingOverrideMustBeProhibited(prohibitedOverride.getLabel()));
    }
    
    @Override
    public void reset() {
        op = "";
        subjectAreaId = null;
        courseOfferingId = null;
        instrOfferingId = null;
        courseName = "";
        title = "";
        scheduleBookNote = "";
        demandCourseOfferingId = null;
        consent = null;
        creditFormat = null; creditType = null;
        creditUnitType = null;
        units = null;
        maxUnits = null;
        fractionalIncrementsAllowed = Boolean.valueOf(false);
        creditText = "";
        courseNbr = "";
        ioNotOffered = null;
        catalogLinkLabel = null;
        catalogLinkLocation = null;
        instructors = new ArrayList();
        responsibilities = new ArrayList();
        percentShares = new ArrayList<String>();
        byReservationOnly = false;
        wkEnroll = null; wkChange = null; wkDrop = null;
        wkEnrollDefault = null; wkChangeDefault = null; wkDropDefault = null;
        waitList = null;
        weekStartDayOfWeek = null;
        courseTypeId = null;
        add = false;
        externalId = null;
        alternativeCourseOfferingId = null;
        notes = null;
        TeachingResponsibility tr = TeachingResponsibility.getDefaultCoordinatorTeachingResponsibility();
        if (tr != null)
        	defaultTeachingResponsibilityId = tr.getUniqueId().toString();
        else
        	defaultTeachingResponsibilityId = "";
        overrides = new HashSet<String>();
    }
    
    public boolean isAdd() { return add; }
    public void setAdd(boolean add) { this.add = add; }

    public Long getCourseOfferingId() {
        return courseOfferingId;
    }
    public void setCourseOfferingId(Long courseOfferingId) {
        this.courseOfferingId = courseOfferingId;
    }

    public String getScheduleBookNote() {
        return scheduleBookNote;
    }
    public void setScheduleBookNote(String scheduleBookNote) {
        this.scheduleBookNote = scheduleBookNote;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getCourseName() {
        return courseName;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    public Long getInstrOfferingId() {
        return instrOfferingId;
    }
    public void setInstrOfferingId(Long instrOfferingId) {
        this.instrOfferingId = instrOfferingId;
    }
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
    public Long getSubjectAreaId() {
        return subjectAreaId;
    }
    public void setSubjectAreaId(Long subjectAreaId) {
        this.subjectAreaId = subjectAreaId;
    }
    public Long getDemandCourseOfferingId() {
    	return demandCourseOfferingId;
    }
    public void setDemandCourseOfferingId(Long demandCourseOfferingId) {
    	this.demandCourseOfferingId = demandCourseOfferingId;
    }
    public boolean getAllowDemandCourseOfferings() {
    	return allowDemandCourseOfferings;
    }
    public void setAllowDemandCourseOfferings(boolean allowDemandCourseOfferings) {
    	this.allowDemandCourseOfferings = allowDemandCourseOfferings;
    }
    public Long getConsent() {
        return consent;
    }
    public void setConsent(Long consent) {
        this.consent = consent;
    }

	public String getCreditFormat() {
		return creditFormat;
	}

	public void setCreditFormat(String creditFormat) {
		this.creditFormat = creditFormat;
	}

	public String getCreditText() {
		return creditText;
	}

	public void setCreditText(String creditText) {
		this.creditText = creditText;
	}

	public Long getCreditType() {
		return creditType;
	}

	public void setCreditType(Long creditType) {
		this.creditType = creditType;
	}

	public Long getCreditUnitType() {
		return creditUnitType;
	}

	public void setCreditUnitType(Long creditUnitType) {
		this.creditUnitType = creditUnitType;
	}

	public Boolean getFractionalIncrementsAllowed() {
		return fractionalIncrementsAllowed;
	}

	public void setFractionalIncrementsAllowed(Boolean fractionalIncrementsAllowed) {
		this.fractionalIncrementsAllowed = fractionalIncrementsAllowed;
	}

	public Float getMaxUnits() {
		return maxUnits;
	}

	public void setMaxUnits(Float maxUnits) {
		this.maxUnits = maxUnits;
	}

	public Float getUnits() {
		return units;
	}

	public void setUnits(Float units) {
		this.units = units;
	}

	public String getCourseNbr() {
		return courseNbr;
	}

	public void setCourseNbr(String courseNbr) {
		if (ApplicationProperty.CourseOfferingNumberUpperCase.isTrue())
        	courseNbr = courseNbr.toUpperCase();
		this.courseNbr = courseNbr;
	}

	public Boolean getIsControl() {
		return isControl;
	}

	public void setIsControl(Boolean isControl) {
		this.isControl = isControl;
	}

	public Boolean getIoNotOffered() {
		return ioNotOffered;
	}

	public void setIoNotOffered(Boolean ioNotOffered) {
		this.ioNotOffered = ioNotOffered;
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
	
    public List<String> getInstructors() { return instructors; }
    public String getInstructors(int key) { return instructors.get(key); }
    public void setInstructors(int key, String value) { this.instructors.set(key, value); }
    public void setInstructors(List<String> instructors) { this.instructors = instructors; }
    
    public List<String> getResponsibilities() { return responsibilities; }
    public String getResponsibilities(int key) { return responsibilities.get(key); }
    public void setResponsibilities(int key, String value) { this.responsibilities.set(key, value); }
    public void setResponsibilities(List<String> responsibilities) { this.responsibilities = responsibilities; }

    public List<String> getPercentShares() { return percentShares; }
    public String getPercentShares(int key) { return percentShares.get(key); }
    public void setPercentShares(int key, String value) { this.percentShares.set(key, value); }
    public void setPercentShares(List<String> percentShares) { this.percentShares = percentShares; }

    public boolean isByReservationOnly() { return byReservationOnly; }
    public void setByReservationOnly(boolean byReservationOnly) { this.byReservationOnly = byReservationOnly; }
    
    public String getWkEnroll() { return wkEnroll; }
    public void setWkEnroll(String wkEnroll) { this.wkEnroll = wkEnroll; }
    public Integer getWkEnrollDefault() { return wkEnrollDefault; }
    public void setWkEnrollDefault(Integer wkEnrollDefault) { this.wkEnrollDefault = wkEnrollDefault; }
    
    public String getWkChange() { return wkChange; }
    public void setWkChange(String wkChange) { this.wkChange = wkChange; }
    public Integer getWkChangeDefault() { return wkChangeDefault; }
    public void setWkChangeDefault(Integer wkChangeDefault) { this.wkChangeDefault = wkChangeDefault; }

    public String getWkDrop() { return wkDrop; }
    public void setWkDrop(String wkDrop) { this.wkDrop = wkDrop; }
    public Integer getWkDropDefault() { return wkDropDefault; }
    public void setWkDropDefault(Integer wkDropDefault) { this.wkDropDefault = wkDropDefault; }
    
    public String getWeekStartDayOfWeek() { return weekStartDayOfWeek; }
    public void setWeekStartDayOfWeek(String weekStartDayOfWeek) { this.weekStartDayOfWeek = weekStartDayOfWeek; }
    
    public String getWaitList() { return waitList; }
    public void setWaitList(String waitList) { this.waitList = waitList; }
    
    public String getCourseTypeId() { return courseTypeId; }
    public void setCourseTypeId(String courseTypeId) { this.courseTypeId = courseTypeId; }
    
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    
    public Long getAlternativeCourseOfferingId() { return alternativeCourseOfferingId; }
    public void setAlternativeCourseOfferingId(Long alternativeCourseOfferingId) { this.alternativeCourseOfferingId = alternativeCourseOfferingId; }
    
    public boolean getAllowAlternativeCourseOfferings() { return allowAlternativeCourseOfferings; }
    public void setAllowAlternativeCourseOfferings(boolean allowAlternativeCourseOfferings) { this.allowAlternativeCourseOfferings = allowAlternativeCourseOfferings; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getDefaultTeachingResponsibilityId() { return defaultTeachingResponsibilityId; }
    public void setDefaultTeachingResponsibilityId(String defaultTeachingResponsibilityId) { this.defaultTeachingResponsibilityId = defaultTeachingResponsibilityId; }
    
    public void addCourseOverride(String override) { overrides.add(override); }
    public String getCourseOverride(String id) { return String.valueOf(overrides.contains(id)); }
    public void setCourseOverride(String id, String value) {
    	if ("true".equalsIgnoreCase(value))
    		overrides.add(id);
    	else
    		overrides.remove(id);
    }
    public Set<String> getCourseOverrides() { return overrides; }
    
    public List<OverrideType> getOverrideTypes() {
    	return OverrideTypeDAO.getInstance().findAll(Order.asc("reference"));
    }
    
    public List<ComboBoxLookup> getWaitListOptions() {
    	List<ComboBoxLookup> ret = new ArrayList<ComboBoxLookup>();
    	if (ApplicationProperty.OfferingWaitListDefault.isTrue())
    		ret.add(new ComboBoxLookup(MSG.waitListDefaultEnabled(), ""));
    	else
    		ret.add(new ComboBoxLookup(MSG.waitListDefaultDisabled(), ""));
    	ret.add(new ComboBoxLookup(MSG.waitListEnabled(), "true"));
    	ret.add(new ComboBoxLookup(MSG.waitListDisabled(), "false"));
    	return ret;
    }
}
