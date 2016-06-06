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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/**
 * MyEclipse Struts
 * Creation date: 07-25-2006
 *
 * XDoclet definition:
 * @struts:form name="courseOfferingEditForm"
 *
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
public class CourseOfferingEditForm extends ActionForm {

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private static final long serialVersionUID = 5719027599139781262L;
	// --------------------------------------------------------- Instance Variables
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
    private List instructors;
    private String wkEnroll, wkChange, wkDrop;
    private Integer wkEnrollDefault, wkChangeDefault, wkDropDefault;
    private String weekStartDayOfWeek;
    private String courseTypeId;
    private String externalId;
    private Long alternativeCourseOfferingId;
    private boolean allowAlternativeCourseOfferings;

    // --------------------------------------------------------- Methods

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
        
        if (getCourseNbr() != null && ApplicationProperty.CourseOfferingNumberUpperCase.isTrue()) {
        	setCourseNbr(getCourseNbr().toUpperCase());
        }

		if (op.equals(MSG.actionUpdateCourseOffering()) || op.equals(MSG.actionSaveCourseOffering())) {
			if (subjectAreaId == null || subjectAreaId == 0) {
				errors.add("subjectAreaId", new ActionMessage("errors.generic", MSG.errorSubjectRequired()));
			} else if (courseNbr==null || courseNbr.trim().length()==0) {
				errors.add("courseNbr", new ActionMessage("errors.generic", MSG.errorCourseNumberRequired()));
			}
			else {
				
		    	String courseNbrRegex = ApplicationProperty.CourseOfferingNumberPattern.value(); 
		    	String courseNbrInfo = ApplicationProperty.CourseOfferingNumberPatternInfo.value();
		    	try { 
			    	Pattern pattern = Pattern.compile(courseNbrRegex);
			    	Matcher matcher = pattern.matcher(courseNbr);
			    	if (!matcher.find()) {
				        errors.add("courseNbr", new ActionMessage("errors.generic", courseNbrInfo));
			    	}
		    	}
		    	catch (Exception e) {
			        errors.add("courseNbr", new ActionMessage("errors.generic", MSG.errorCourseNumberCannotBeMatched(courseNbrRegex,e.getMessage())));
		    	}

				
		    	if (ApplicationProperty.CourseOfferingNumberMustBeUnique.isTrue()){
					SubjectArea sa = new SubjectAreaDAO().get(subjectAreaId);
					CourseOffering co = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(sa.getSessionId(), sa.getSubjectAreaAbbreviation(), courseNbr);
					if (add && co != null) {
						errors.add("courseNbr", new ActionMessage("errors.generic", MSG.errorCourseCannotBeCreated()));
					} else if (!add && co!=null && !co.getUniqueId().equals(courseOfferingId)) {
			            errors.add("courseNbr", new ActionMessage("errors.generic", MSG.errorCourseCannotBeRenamed()));
					}
		    	}

			}
		}

        return errors;
    }
    
    protected DynamicListObjectFactory factory = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

    /**
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
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
        fractionalIncrementsAllowed = new Boolean(false);
        creditText = "";
        courseNbr = "";
        ioNotOffered = null;
        catalogLinkLabel = null;
        catalogLinkLocation = null;
        instructors = DynamicList.getInstance(new ArrayList(), factory);
        byReservationOnly = false;
        wkEnroll = null; wkChange = null; wkDrop = null;
        wkEnrollDefault = null; wkChangeDefault = null; wkDropDefault = null;
        weekStartDayOfWeek = null;
        courseTypeId = null;
        add = false;
        externalId = null;
        alternativeCourseOfferingId = null;
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
	
    public List getInstructors() { return instructors; }
    public String getInstructors(int key) { return instructors.get(key).toString(); }
    public void setInstructors(int key, Object value) { this.instructors.set(key, value); }
    public void setInstructors(List instructors) { this.instructors = instructors; }

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
    
    public String getCourseTypeId() { return courseTypeId; }
    public void setCourseTypeId(String courseTypeId) { this.courseTypeId = courseTypeId; }
    
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    
    public Long getAlternativeCourseOfferingId() { return alternativeCourseOfferingId; }
    public void setAlternativeCourseOfferingId(Long alternativeCourseOfferingId) { this.alternativeCourseOfferingId = alternativeCourseOfferingId; }
    
    public boolean getAllowAlternativeCourseOfferings() { return allowAlternativeCourseOfferings; }
    public void setAllowAlternativeCourseOfferings(boolean allowAlternativeCourseOfferings) { this.allowAlternativeCourseOfferings = allowAlternativeCourseOfferings; }
}
