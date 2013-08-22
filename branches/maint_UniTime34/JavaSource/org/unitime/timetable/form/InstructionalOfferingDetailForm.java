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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/** 
 * MyEclipse Struts
 * Creation date: 03-20-2006
 * 
 * XDoclet definition:
 * @struts:form name="instructionalOfferingConfigDetailForm"
 */
public class InstructionalOfferingDetailForm extends ActionForm {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

    /**
	 * 
	 */
	private static final long serialVersionUID = -5161466018324037153L;
	// --------------------------------------------------------- Instance Variables

    private String op;   
    private Long subjectAreaId;
    private Long crsOfferingId;
    private Long instrOfferingId;
    private Long ctrlCrsOfferingId;
    private Integer projectedDemand;
    private Integer enrollment;
    private Integer demand;
    private Integer limit;
    private Boolean unlimited;
    private Boolean notOffered;
    private String instrOfferingName;
    private String instrOfferingNameNoTitle;
    private List courseOfferings;
    private String subjectAreaAbbr;
    private String courseNbr;
    private String creditText;
    private String nextId;
    private String previousId;
    private String catalogLinkLabel;
    private String catalogLinkLocation;
    private boolean byReservationOnly;
    private String coordinators;
    private String wkEnroll, wkChange, wkDrop;
    private String weekStartDayOfWeek;
    private String accommodation;


    // --------------------------------------------------------- Classes

    /** Factory to create dynamic list element for Course Offerings */
    protected DynamicListObjectFactory factoryCourseOfferings = new DynamicListObjectFactory() {
        public Object create() {
            return new String("");
        }
    };

   
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

        throw new UnsupportedOperationException(
            MSG.exceptionValidateNotImplemented());
    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        op = "view";    
        subjectAreaId = null;
        subjectAreaAbbr = null;
        courseNbr = null;
        crsOfferingId = null;
        instrOfferingId = null;
        ctrlCrsOfferingId = null;
        enrollment = null;
        demand = null;
        projectedDemand = null;
        limit = null;
        unlimited = new Boolean(false);
        notOffered = null;
        instrOfferingName = "";
        instrOfferingNameNoTitle = "";
        courseOfferings = DynamicList.getInstance(new ArrayList(), factoryCourseOfferings);
        nextId = previousId = null;
        creditText = "";
        catalogLinkLabel = null;
        catalogLinkLocation = null;
        byReservationOnly = false; coordinators = null;
        wkEnroll = null; wkChange = null; wkDrop = null;
        weekStartDayOfWeek = null;
        accommodation = null;
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
        return new Integer(this.courseOfferings.size());
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

	public String getCreditText() {
		return creditText;
	}

	public void setCreditText(String creditText) {
		this.creditText = creditText;
	}

	public Integer getEnrollment() {
		return enrollment;
	}

	public void setEnrollment(Integer enrollment) {
		this.enrollment = enrollment;
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
}
