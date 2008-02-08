/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;


/**
 * @author Stephanie Schluttenhofer
 */
public class InstructionalOfferingListForm extends ActionForm implements InstructionalOfferingListFormInterface {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -6985831814265952068L;

	private Collection instructionalOfferings;

	private Collection subjectAreas;

	private String subjectAreaId;

	private String courseNbr;

	private Boolean showNotOffered;

	private String buttonAction;

	private InstructionalOffering instructionalOffering;

	private String subjectAreaAbbv;

	private Boolean isControl;

	private String ctrlInstrOfferingId;

	private Collection controlCourseOfferings;

	private Boolean divSec;

	private Boolean demand;

	private Boolean projectedDemand;

	private Boolean minPerWk;

	private Boolean limit;

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
	
	private Boolean designatorRequired;
	
	private Boolean title;
	
	private Boolean exams;
	
	private Boolean canSeeExams;
	
	private String sortBy;
		
	/**
	 * @param instructionalOffering
	 *            The instructionalOffering to set.
	 */
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) {
		this.instructionalOffering = instructionalOffering;
	}

	/**
	 * @return Returns the controlCourseOfferings.
	 */
	public Collection getControlCourseOfferings() {
		return controlCourseOfferings;
	}

	/**
	 * @param controlCourseOfferings
	 *            The controlCourseOfferings to set.
	 */
	public void setControlCourseOfferings(Collection controlCourseOfferings) {
		this.controlCourseOfferings = controlCourseOfferings;
	}

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
		this.courseNbr = courseNbr.toUpperCase();
	}

	/**
	 * @return Returns the subjectAreaId.
	 */
	public String getSubjectAreaId() {
		return subjectAreaId;
	}

	/**
	 * @param subjectAreaId
	 *            The subjectAreaId to set.
	 */
	public void setSubjectAreaId(String subjectAreaId) {
		this.subjectAreaId = subjectAreaId;
	}

	// --------------------------------------------------------- Methods
	/**
	 * Method reset
	 * 
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {

		courseNbr = "";
		instructionalOfferings = new ArrayList();
		subjectAreas = new ArrayList();
		divSec = new Boolean(false);
		demand = new Boolean(false);
		projectedDemand = new Boolean(false);
		minPerWk = new Boolean(false);
		limit = new Boolean(false);
		roomLimit = new Boolean(false);
		manager = new Boolean(false);
		datePattern = new Boolean(false);
		timePattern = new Boolean(false);
		preferences = new Boolean(false);
		instructor = new Boolean(false);
		timetable = new Boolean(false);
		credit = new Boolean(false);
		subpartCredit = new Boolean(false);
		schedulePrintNote = new Boolean(false);
		note = new Boolean(false);
		title = new Boolean(false);
		consent = new Boolean(false);
		designatorRequired = new Boolean(false);
		exams = new Boolean(false);
		canSeeExams = new Boolean(false);
		sortBy = ClassListForm.sSortByName;
	}

	/**
	 * @return Returns the instructionalOfferings.
	 */
	public Collection getInstructionalOfferings() {
		return instructionalOfferings;
	}

	/**
	 * @param instructionalOfferings
	 *            The instructionalOfferings to set.
	 */
	public void setInstructionalOfferings(Collection instructionalOfferings) {
		this.instructionalOfferings = instructionalOfferings;
	}

	/**
	 * @return Returns the subjectAreas.
	 */
	public Collection getSubjectAreas() {
		return subjectAreas;
	}

	/**
	 * @param subjectAreas
	 *            The subjectAreas to set.
	 */
	public void setSubjectAreas(Collection subjectAreas) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping,
	 *      javax.servlet.http.HttpServletRequest)
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		if (subjectAreaId == null || subjectAreaId.trim().length() == 0 || subjectAreaId.equals(Constants.BLANK_OPTION_VALUE)) {
			errors.add("subjectAreaId", new ActionMessage("errors.required", "Subject Area"));
		}

		return errors;
	}

	public InstructionalOffering getInstructionalOffering(String uid) {
		Iterator it = this.getInstructionalOfferings().iterator();
		InstructionalOffering io = null;
		while (it.hasNext() && (io == null || !io.getUniqueId().equals(Integer.valueOf(uid)))) {
			io = (InstructionalOffering) it.next();
		}
		return (io);
	}

	public InstructionalOffering getInstructionalOffering(int uid) {
		Iterator it = this.getInstructionalOfferings().iterator();
		InstructionalOffering io = null;
		while (it.hasNext() && (io == null || !(io.getUniqueId().intValue() == uid))) {
			io = (InstructionalOffering) it.next();
		}
		return (io);
	}

	public void setCollections(HttpServletRequest request, Set instrOfferings) throws Exception {
		User user = Web.getUser(request.getSession());
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		setSubjectAreas(TimetableManager.getSubjectAreas(user));
		setInstructionalOfferings(instrOfferings);

		if (Web.hasRole(request.getSession(), new String[] { Roles.ADMIN_ROLE }))
			setControlCourseOfferings(CourseOffering.getControllingCourses(sessionId));
		else
			setControlCourseOfferings(new Vector());

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
    
    public Boolean getDesignatorRequired() {
        return designatorRequired;
    }
    
    public void setDesignatorRequired(Boolean designatorRequired) {
        this.designatorRequired = designatorRequired;
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
    public Boolean getCanSeeExams() {
        return canSeeExams;
    }
    public void setCanSeeExams(Boolean canSeeExams) {
        this.canSeeExams = canSeeExams;
    }

    protected void finalize() throws Throwable {
        Debug.debug("!!! Finalizing InstructionalOfferingListForm ... ");
        instructionalOfferings=null;
        subjectAreas=null;
        subjectAreaId=null;
        courseNbr=null;
        showNotOffered=null;
        buttonAction=null;
        instructionalOffering=null;
        subjectAreaAbbv=null;
        isControl=null;
        ctrlInstrOfferingId=null;
        controlCourseOfferings=null;
        divSec=null;
        demand=null;
        projectedDemand=null;
        minPerWk=null;
        limit=null;
        roomLimit=null;
        manager=null;
        datePattern=null;
        timePattern=null;
        preferences=null;
        instructor=null;
        timetable=null;
        credit=null;
        subpartCredit=null;
       schedulePrintNote=null;
        note=null;
        title=null;
        consent=null;
        designatorRequired=null;
        super.finalize();
    }

	public String getSortBy() { return sortBy; }
	public void setSortBy(String sortBy) { this.sortBy = sortBy; }
	public String[] getSortByOptions() { return ClassListForm.sSortByOptions; }
	
	public Boolean getEnrollmentInformation(){
		return(new Boolean(getDemand().booleanValue() 
				&& getProjectedDemand().booleanValue()
				&& getLimit().booleanValue()
				&& getRoomLimit().booleanValue()));
	}
	public void setEnrollmentInformation(){
		; //do nothing
	}
	
	public Boolean getDateTimeInformation(){
		return(new Boolean(getDatePattern().booleanValue() 
				&& getMinPerWk().booleanValue()
				&& getTimePattern().booleanValue()));
	}
	public void setDateTimeInformation(){
		; //do nothing
	}
	public Boolean getCatalogInformation(){
		return(new Boolean(getTitle().booleanValue() 
				&& getCredit().booleanValue()
				&& getSubpartCredit().booleanValue()
				&& getConsent().booleanValue()
				&& getDesignatorRequired().booleanValue()
				&& getSchedulePrintNote().booleanValue()));
	}
	public void setCatalogInformation(){
		; //do nothing
	}
}
