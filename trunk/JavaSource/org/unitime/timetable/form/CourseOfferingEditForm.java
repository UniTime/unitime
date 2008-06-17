/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;


/**
 * MyEclipse Struts
 * Creation date: 07-25-2006
 *
 * XDoclet definition:
 * @struts:form name="courseOfferingEditForm"
 */
public class CourseOfferingEditForm extends ActionForm {

    // --------------------------------------------------------- Instance Variables
    private String op;
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
    private Boolean designatorRequired;
    private String creditFormat;
    private Long creditType;
    private Long creditUnitType;
    private Float units;
    private Float maxUnits;
    private Boolean fractionalIncrementsAllowed;
    private String creditText;
    private Boolean isControl;
    private Boolean ioNotOffered;

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
        // Get Message Resources
        MessageResources rsc =
            (MessageResources) super.getServlet()
            	.getServletContext().getAttribute(Globals.MESSAGES_KEY);

		if(op.equals(rsc.getMessage("button.updateCourseOffering")) ) {
			if (courseNbr==null || courseNbr.trim().length()==0) {
				errors.add("courseNbr", new ActionMessage("errors.required", "Course Number"));
			}
			else {
				SubjectArea sa = new SubjectAreaDAO().get(subjectAreaId);
				CourseOffering co = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(sa.getSessionId(), sa.getSubjectAreaAbbreviation(), courseNbr);
				if (co!=null && !co.getUniqueId().equals(courseOfferingId)) {
		            errors.add("courseNbr", new ActionMessage("errors.generic", "The course cannot be renamed. A course with the same course number already exists."));
				}
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
        op = "";
        subjectAreaId = null;
        courseOfferingId = null;
        instrOfferingId = null;
        courseName = "";
        title = "";
        scheduleBookNote = "";
        demandCourseOfferingId = null;
        consent = null;
        designatorRequired = null;
        creditFormat = null; creditType = null;
        creditUnitType = null;
        units = null;
        maxUnits = null;
        fractionalIncrementsAllowed = new Boolean(false);
        creditText = "";
        courseNbr = "";
        ioNotOffered = null;
    }

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
    public Boolean getDesignatorRequired() {
        return designatorRequired;
    }
    public void setDesignatorRequired(Boolean designatorRequired) {
        this.designatorRequired = designatorRequired;
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
		this.courseNbr = courseNbr.toUpperCase();
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

}
