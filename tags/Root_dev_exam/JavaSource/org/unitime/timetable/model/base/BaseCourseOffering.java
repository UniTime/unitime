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
package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the COURSE_OFFERING table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="COURSE_OFFERING"
 */

public abstract class BaseCourseOffering  implements Serializable {

	public static String REF = "CourseOffering";
	public static String PROP_IS_CONTROL = "isControl";
	public static String PROP_PERM_ID = "permId";
	public static String PROP_PROJECTED_DEMAND = "projectedDemand";
	public static String PROP_NBR_EXPECTED_STUDENTS = "nbrExpectedStudents";
	public static String PROP_DEMAND = "demand";
	public static String PROP_SUBJECT_AREA_ABBV = "subjectAreaAbbv";
	public static String PROP_COURSE_NBR = "courseNbr";
	public static String PROP_TITLE = "title";
	public static String PROP_SCHEDULE_BOOK_NOTE = "scheduleBookNote";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_UNIQUE_ID_ROLLED_FORWARD_FROM = "uniqueIdRolledForwardFrom";


	// constructors
	public BaseCourseOffering () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCourseOffering (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCourseOffering (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.SubjectArea subjectArea,
		org.unitime.timetable.model.InstructionalOffering instructionalOffering,
		java.lang.Boolean isControl,
		java.lang.Integer nbrExpectedStudents,
		java.lang.String courseNbr) {

		this.setUniqueId(uniqueId);
		this.setSubjectArea(subjectArea);
		this.setInstructionalOffering(instructionalOffering);
		this.setIsControl(isControl);
		this.setNbrExpectedStudents(nbrExpectedStudents);
		this.setCourseNbr(courseNbr);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Boolean isControl;
	private java.lang.String permId;
	private java.lang.Integer projectedDemand;
	private java.lang.Integer nbrExpectedStudents;
	private java.lang.Integer demand;
	private java.lang.String subjectAreaAbbv;
	private java.lang.String courseNbr;
	private java.lang.String title;
	private java.lang.String scheduleBookNote;
	private java.lang.String externalUniqueId;
	private java.lang.Long uniqueIdRolledForwardFrom;

	// many to one
	private org.unitime.timetable.model.SubjectArea subjectArea;
	private org.unitime.timetable.model.InstructionalOffering instructionalOffering;
	private org.unitime.timetable.model.CourseOffering demandOffering;
	private org.unitime.timetable.model.DemandOfferingType demandOfferingType;

	// collections
	private java.util.Set courseReservations;
	private java.util.Set acadAreaReservations;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="org.unitime.commons.hibernate.id.UniqueIdGenerator"
     *  column="UNIQUEID"
     */
	public java.lang.Long getUniqueId () {
		return uniqueId;
	}

	/**
	 * Set the unique identifier of this class
	 * @param uniqueId the new ID
	 */
	public void setUniqueId (java.lang.Long uniqueId) {
		this.uniqueId = uniqueId;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: IS_CONTROL
	 */
	public java.lang.Boolean isIsControl () {
		return isControl;
	}

	/**
	 * Set the value related to the column: IS_CONTROL
	 * @param isControl the IS_CONTROL value
	 */
	public void setIsControl (java.lang.Boolean isControl) {
		this.isControl = isControl;
	}



	/**
	 * Return the value associated with the column: PERM_ID
	 */
	public java.lang.String getPermId () {
		return permId;
	}

	/**
	 * Set the value related to the column: PERM_ID
	 * @param permId the PERM_ID value
	 */
	public void setPermId (java.lang.String permId) {
		this.permId = permId;
	}



	/**
	 * Return the value associated with the column: PROJ_DEMAND
	 */
	public java.lang.Integer getProjectedDemand () {
		return projectedDemand;
	}

	/**
	 * Set the value related to the column: PROJ_DEMAND
	 * @param projectedDemand the PROJ_DEMAND value
	 */
	public void setProjectedDemand (java.lang.Integer projectedDemand) {
		this.projectedDemand = projectedDemand;
	}



	/**
	 * Return the value associated with the column: NBR_EXPECTED_STDENTS
	 */
	public java.lang.Integer getNbrExpectedStudents () {
		return nbrExpectedStudents;
	}

	/**
	 * Set the value related to the column: NBR_EXPECTED_STDENTS
	 * @param nbrExpectedStudents the NBR_EXPECTED_STDENTS value
	 */
	public void setNbrExpectedStudents (java.lang.Integer nbrExpectedStudents) {
		this.nbrExpectedStudents = nbrExpectedStudents;
	}



	/**
	 * Return the value associated with the column: demand
	 */
	public java.lang.Integer getDemand () {
		return demand;
	}

	/**
	 * Set the value related to the column: demand
	 * @param demand the demand value
	 */
	public void setDemand (java.lang.Integer demand) {
		this.demand = demand;
	}



	/**
	 * Return the value associated with the column: subjectAreaAbbv
	 */
	public java.lang.String getSubjectAreaAbbv () {
		return subjectAreaAbbv;
	}

	/**
	 * Set the value related to the column: subjectAreaAbbv
	 * @param subjectAreaAbbv the subjectAreaAbbv value
	 */
	public void setSubjectAreaAbbv (java.lang.String subjectAreaAbbv) {
		this.subjectAreaAbbv = subjectAreaAbbv;
	}



	/**
	 * Return the value associated with the column: COURSE_NBR
	 */
	public java.lang.String getCourseNbr () {
		return courseNbr;
	}

	/**
	 * Set the value related to the column: COURSE_NBR
	 * @param courseNbr the COURSE_NBR value
	 */
	public void setCourseNbr (java.lang.String courseNbr) {
		this.courseNbr = courseNbr;
	}



	/**
	 * Return the value associated with the column: TITLE
	 */
	public java.lang.String getTitle () {
		return title;
	}

	/**
	 * Set the value related to the column: TITLE
	 * @param title the TITLE value
	 */
	public void setTitle (java.lang.String title) {
		this.title = title;
	}



	/**
	 * Return the value associated with the column: SCHEDULE_BOOK_NOTE
	 */
	public java.lang.String getScheduleBookNote () {
		return scheduleBookNote;
	}

	/**
	 * Set the value related to the column: SCHEDULE_BOOK_NOTE
	 * @param scheduleBookNote the SCHEDULE_BOOK_NOTE value
	 */
	public void setScheduleBookNote (java.lang.String scheduleBookNote) {
		this.scheduleBookNote = scheduleBookNote;
	}



	/**
	 * Return the value associated with the column: EXTERNAL_UID
	 */
	public java.lang.String getExternalUniqueId () {
		return externalUniqueId;
	}

	/**
	 * Set the value related to the column: EXTERNAL_UID
	 * @param externalUniqueId the EXTERNAL_UID value
	 */
	public void setExternalUniqueId (java.lang.String externalUniqueId) {
		this.externalUniqueId = externalUniqueId;
	}



	/**
	 * Return the value associated with the column: UID_ROLLED_FWD_FROM
	 */
	public java.lang.Long getUniqueIdRolledForwardFrom () {
		return uniqueIdRolledForwardFrom;
	}

	/**
	 * Set the value related to the column: UID_ROLLED_FWD_FROM
	 * @param uniqueIdRolledForwardFrom the UID_ROLLED_FWD_FROM value
	 */
	public void setUniqueIdRolledForwardFrom (java.lang.Long uniqueIdRolledForwardFrom) {
		this.uniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom;
	}



	/**
	 * Return the value associated with the column: SUBJECT_AREA_ID
	 */
	public org.unitime.timetable.model.SubjectArea getSubjectArea () {
		return subjectArea;
	}

	/**
	 * Set the value related to the column: SUBJECT_AREA_ID
	 * @param subjectArea the SUBJECT_AREA_ID value
	 */
	public void setSubjectArea (org.unitime.timetable.model.SubjectArea subjectArea) {
		this.subjectArea = subjectArea;
	}



	/**
	 * Return the value associated with the column: INSTR_OFFR_ID
	 */
	public org.unitime.timetable.model.InstructionalOffering getInstructionalOffering () {
		return instructionalOffering;
	}

	/**
	 * Set the value related to the column: INSTR_OFFR_ID
	 * @param instructionalOffering the INSTR_OFFR_ID value
	 */
	public void setInstructionalOffering (org.unitime.timetable.model.InstructionalOffering instructionalOffering) {
		this.instructionalOffering = instructionalOffering;
	}



	/**
	 * Return the value associated with the column: DEMAND_OFFERING_ID
	 */
	public org.unitime.timetable.model.CourseOffering getDemandOffering () {
		return demandOffering;
	}

	/**
	 * Set the value related to the column: DEMAND_OFFERING_ID
	 * @param demandOffering the DEMAND_OFFERING_ID value
	 */
	public void setDemandOffering (org.unitime.timetable.model.CourseOffering demandOffering) {
		this.demandOffering = demandOffering;
	}



	/**
	 * Return the value associated with the column: DEMAND_OFFERING_TYPE
	 */
	public org.unitime.timetable.model.DemandOfferingType getDemandOfferingType () {
		return demandOfferingType;
	}

	/**
	 * Set the value related to the column: DEMAND_OFFERING_TYPE
	 * @param demandOfferingType the DEMAND_OFFERING_TYPE value
	 */
	public void setDemandOfferingType (org.unitime.timetable.model.DemandOfferingType demandOfferingType) {
		this.demandOfferingType = demandOfferingType;
	}



	/**
	 * Return the value associated with the column: courseReservations
	 */
	public java.util.Set getCourseReservations () {
		return courseReservations;
	}

	/**
	 * Set the value related to the column: courseReservations
	 * @param courseReservations the courseReservations value
	 */
	public void setCourseReservations (java.util.Set courseReservations) {
		this.courseReservations = courseReservations;
	}



	/**
	 * Return the value associated with the column: acadAreaReservations
	 */
	public java.util.Set getAcadAreaReservations () {
		return acadAreaReservations;
	}

	/**
	 * Set the value related to the column: acadAreaReservations
	 * @param acadAreaReservations the acadAreaReservations value
	 */
	public void setAcadAreaReservations (java.util.Set acadAreaReservations) {
		this.acadAreaReservations = acadAreaReservations;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CourseOffering)) return false;
		else {
			org.unitime.timetable.model.CourseOffering courseOffering = (org.unitime.timetable.model.CourseOffering) obj;
			if (null == this.getUniqueId() || null == courseOffering.getUniqueId()) return false;
			else return (this.getUniqueId().equals(courseOffering.getUniqueId()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getUniqueId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getUniqueId().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}