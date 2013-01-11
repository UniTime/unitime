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
import org.apache.struts.action.ActionMessage;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/** 
 * MyEclipse Struts
 * Creation date: 04-18-2006
 * 
 * XDoclet definition:
 * @struts:form name="crossListsModifyForm"
 */
public class CrossListsModifyForm extends ActionForm {

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
    // --------------------------------------------------------- Instance Variables

	private static final long serialVersionUID = 3638385556572422628L;
	private String op;   
    private Long subjectAreaId;
    private Long instrOfferingId;
    private Long addCourseOfferingId;
    private Long ctrlCrsOfferingId;
    private String instrOfferingName;
    private Boolean ownedInstrOffr;
    private List courseOfferingIds;
    private List courseOfferingNames;
    private List ownedCourse;

    private List resvId;
    private List limits;
    private List requested;
    private List projected;
    private List lastTerm;
    
	private Integer ioLimit;
	private Boolean unlimited;
	
    private Long readOnlyCrsOfferingId;
    private String originalOfferings;
    

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

        ActionErrors errors = new ActionErrors();

        if (op.equals(MSG.actionAddCourseToCrossList())) {
            // Check Added Course
	        if (this.addCourseOfferingId==null || this.addCourseOfferingId.intValue()<=0) {
	            errors.add("addCourseOfferingId", new ActionMessage("errors.generic", MSG.errorRequiredCourseOffering()));            
	        }
        }
        
        if (op.equals(MSG.actionUpdateCrossLists())) {
	        // Check controlling course
	        if (this.ctrlCrsOfferingId==null || this.ctrlCrsOfferingId.intValue()<=0) {
	            errors.add("ctrlCrsOfferingId", new ActionMessage("errors.generic", MSG.errorRequiredControllingCourse()));            
	        }
        }
        
        return errors;
    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        subjectAreaId = null;
        instrOfferingId = null;
        ctrlCrsOfferingId = null;
        readOnlyCrsOfferingId = null;
        instrOfferingName = null;
        courseOfferingIds = DynamicList.getInstance(new ArrayList(), factoryCourseOfferings);
        courseOfferingNames = DynamicList.getInstance(new ArrayList(), factoryCourseOfferings);
        ownedCourse = DynamicList.getInstance(new ArrayList(), factoryCourseOfferings);
        resvId = DynamicList.getInstance(new ArrayList(), factoryCourseOfferings);
        limits = DynamicList.getInstance(new ArrayList(), factoryCourseOfferings);
        requested = DynamicList.getInstance(new ArrayList(), factoryCourseOfferings);
        projected = DynamicList.getInstance(new ArrayList(), factoryCourseOfferings);
        lastTerm = DynamicList.getInstance(new ArrayList(), factoryCourseOfferings);
        originalOfferings = "";
        ioLimit = null;
        unlimited = null;
    }


    public List getCourseOfferingIds() {
        return courseOfferingIds;
    }
    public String getCourseOfferingIds(int key) {
        return courseOfferingIds.get(key).toString();
    }
    public void setCourseOfferingIds(int key, Object value) {
        this.courseOfferingIds.set(key, value);
    }
    public void setCourseOfferingIds(List courseOfferingIds) {
        this.courseOfferingIds = courseOfferingIds;
    }

    public List getCourseOfferingNames() {
        return courseOfferingNames;
    }
    public String getCourseOfferingNames(int key) {
        return courseOfferingNames.get(key).toString();
    }
    public void setCourseOfferingNames(int key, Object value) {
        this.courseOfferingNames.set(key, value);
    }
    public void setCourseOfferingNames(List courseOfferingNames) {
        this.courseOfferingNames = courseOfferingNames;
    }

    public List getOwnedCourse() {
        return ownedCourse;
    }
    public String getOwnedCourse(int key) {
        return ownedCourse.get(key).toString();
    }
    public void setOwnedCourse(int key, Object value) {
        this.ownedCourse.set(key, value);
    }
    public void setOwnedCourse(List ownedCourse) {
        this.ownedCourse = ownedCourse;
    }

    public List getLimits() {
        return limits;
    }
    public String getLimits(int key) {
        return limits.get(key).toString();
    }
    public void setLimits(int key, Object value) {
        this.limits.set(key, value);
    }
    public void setLimits(List limits) {
        this.limits = limits;
    }

    public List getResvId() {
        return resvId;
    }
    public String getResvId(int key) {
        return resvId.get(key).toString();
    }
    public void setResvId(int key, Object value) {
        this.resvId.set(key, value);
    }
    public void setResvId(List resvId) {
        this.resvId = resvId;
    }

    public List getRequested() {
        return requested;
    }
    public String getRequested(int key) {
        return requested.get(key).toString();
    }
    public void setRequested(int key, Object value) {
        this.requested.set(key, value);
    }
    public void setRequested(List requested) {
        this.requested = requested;
    }

    public List getProjected() {
        return projected;
    }
    public String getProjected(int key) {
        return projected.get(key).toString();
    }
    public void setProjected(int key, Object value) {
        this.projected.set(key, value);
    }
    public void setProjected(List projected) {
        this.projected = projected;
    }

    public List getLastTerm() {
        return lastTerm;
    }
    public String getLastTerm(int key) {
        return lastTerm.get(key).toString();
    }
    public void setLastTerm(int key, Object value) {
        this.lastTerm.set(key, value);
    }
    public void setLastTerm(List lastTerm) {
        this.lastTerm = lastTerm;
    }

   public Long getCtrlCrsOfferingId() {
        return ctrlCrsOfferingId;
    }
    public void setCtrlCrsOfferingId(Long ctrlCrsOfferingId) {
        this.ctrlCrsOfferingId = ctrlCrsOfferingId;
    }
    public Long getInstrOfferingId() {
        return instrOfferingId;
    }
    public void setInstrOfferingId(Long instrOfferingId) {
        this.instrOfferingId = instrOfferingId;
    }
    public String getInstrOfferingName() {
        return instrOfferingName;
    }
    public void setInstrOfferingName(String instrOfferingName) {
        this.instrOfferingName = instrOfferingName;
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
    
    public Long getAddCourseOfferingId() {
        return addCourseOfferingId;
    }
    public void setAddCourseOfferingId(Long addCourseOfferingId) {
        this.addCourseOfferingId = addCourseOfferingId;
    }
    
    public Long getReadOnlyCrsOfferingId() {
        return readOnlyCrsOfferingId;
    }
    public void setReadOnlyCrsOfferingId(Long readOnlyCrsOfferingId) {
        this.readOnlyCrsOfferingId = readOnlyCrsOfferingId;
    }
    
    public String getOriginalOfferings() {
        return originalOfferings;
    }
    public void setOriginalOfferings(String originalOfferings) {
        this.originalOfferings = originalOfferings;
    }
    
    public Boolean getOwnedInstrOffr() {
		return ownedInstrOffr;
	}
	public void setOwnedInstrOffr(Boolean ownedInstrOffr) {
		this.ownedInstrOffr = ownedInstrOffr;
	}

	public Integer getIoLimit() {
		return ioLimit;
	}

	public void setIoLimit(Integer ioLimit) {
		this.ioLimit = ioLimit;
	}

	public Boolean getUnlimited() {
		return unlimited;
	}

	public void setUnlimited(Boolean unlimited) {
		this.unlimited = unlimited;
	}

	/**
     * Add course offering to original course offerings list
     * @param co Course Offering object
     */
    public void addToOriginalCourseOfferings(CourseOffering co) {
        this.originalOfferings += " " + co.getUniqueId().toString();
    }
    
    /**
     * Add course offering to the list
     * @param co Course Offering object
     * @param resv
     * @param isOwner
     */
    public void addToCourseOfferings(CourseOffering co, Boolean isOwner) {
        this.courseOfferingIds.add(co.getUniqueId().toString());
        this.courseOfferingNames.add((co.getCourseNameWithTitle()));
        this.ownedCourse.add(isOwner);
        this.resvId.add("");
        this.limits.add(co.getReservation() == null ? "" : co.getReservation().toString());
        this.requested.add("");
        this.projected.add(co.getProjectedDemand() == null ? "" : co.getProjectedDemand().toString());
        this.lastTerm.add(co.getDemand() == null ? "" : co.getDemand().toString());
    }
    
    /**
     * Remove course offering from the list
     * @param courseOfferingId Course Offering Id 
     */
    public void removeFromCourseOfferings(String courseOfferingId) {

        int ct=0;
        for (Iterator i=this.courseOfferingIds.listIterator(); i.hasNext(); ) {
            String co1 = i.next().toString();
            if(co1.equals(courseOfferingId)) {
                i.remove();
                this.courseOfferingNames.remove(ct);
                this.ownedCourse.remove(ct);
                this.resvId.remove(ct);
                this.limits.remove(ct);
                this.requested.remove(ct);
                this.projected.remove(ct);
                this.lastTerm.remove(ct);
                break;
            }
            ++ct;
        }
    }

    /**
     * @param course
     * @return -1 if not found
     */
    public int getIndex(String courseOfferingId) {
        for (int i=0; i<courseOfferingIds.size(); i++ ) {
            String co1 = (String) courseOfferingIds.get(i);
            if(co1.equals(courseOfferingId)) {
                return i;
            }
        }
        return -1;
    }
}
