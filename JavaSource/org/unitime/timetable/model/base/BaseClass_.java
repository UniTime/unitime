/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008-2009, UniTime LLC, and individual contributors
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
 * This is an object that contains data related to the CLASS_ table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="CLASS_"
 */

public abstract class BaseClass_ extends org.unitime.timetable.model.PreferenceGroup  implements Serializable {

	public static String REF = "Class_";
	public static String PROP_EXPECTED_CAPACITY = "expectedCapacity";
	public static String PROP_NOTES = "notes";
	public static String PROP_NBR_ROOMS = "nbrRooms";
	public static String PROP_SECTION_NUMBER_CACHE = "sectionNumberCache";
	public static String PROP_DISPLAY_INSTRUCTOR = "displayInstructor";
	public static String PROP_SCHEDULE_PRINT_NOTE = "schedulePrintNote";
	public static String PROP_CLASS_SUFFIX = "classSuffix";
	public static String PROP_DISPLAY_IN_SCHEDULE_BOOK = "displayInScheduleBook";
	public static String PROP_MAX_EXPECTED_CAPACITY = "maxExpectedCapacity";
	public static String PROP_ROOM_RATIO = "roomRatio";
	public static String PROP_UNIQUE_ID_ROLLED_FORWARD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_ENROLLMENT = "enrollment";


	// constructors
	public BaseClass_ () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseClass_ (java.lang.Long uniqueId) {
		super(uniqueId);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.Integer expectedCapacity;
	private java.lang.String notes;
	private java.lang.Integer nbrRooms;
	private java.lang.Integer sectionNumberCache;
	private java.lang.Boolean displayInstructor;
	private java.lang.String schedulePrintNote;
	private java.lang.String classSuffix;
	private java.lang.Boolean displayInScheduleBook;
	private java.lang.Integer maxExpectedCapacity;
	private java.lang.Float roomRatio;
	private java.lang.Long uniqueIdRolledForwardFrom;
	private java.lang.String externalUniqueId;
	private java.lang.Integer enrollment;

	// one to one
	private org.unitime.timetable.model.SectioningInfo sectioningInfo;

	// many to one
	private org.unitime.timetable.model.Department controllingDept;
	private org.unitime.timetable.model.Department managingDept;
	private org.unitime.timetable.model.SchedulingSubpart schedulingSubpart;
	private org.unitime.timetable.model.Class_ parentClass;
	private org.unitime.timetable.model.DatePattern datePattern;
	private org.unitime.timetable.model.Assignment committedAssignment;

	// collections
	private java.util.Set childClasses;
	private java.util.Set classInstructors;
	private java.util.Set assignments;
	private java.util.Set studentEnrollments;
	private java.util.Set courseReservations;
	private java.util.Set individualReservations;
	private java.util.Set studentGroupReservations;
	private java.util.Set acadAreaReservations;
	private java.util.Set posReservations;






	/**
	 * Return the value associated with the column: EXPECTED_CAPACITY
	 */
	public java.lang.Integer getExpectedCapacity () {
		return expectedCapacity;
	}

	/**
	 * Set the value related to the column: EXPECTED_CAPACITY
	 * @param expectedCapacity the EXPECTED_CAPACITY value
	 */
	public void setExpectedCapacity (java.lang.Integer expectedCapacity) {
		this.expectedCapacity = expectedCapacity;
	}



	/**
	 * Return the value associated with the column: NOTES
	 */
	public java.lang.String getNotes () {
		return notes;
	}

	/**
	 * Set the value related to the column: NOTES
	 * @param notes the NOTES value
	 */
	public void setNotes (java.lang.String notes) {
		this.notes = notes;
	}



	/**
	 * Return the value associated with the column: NBR_ROOMS
	 */
	public java.lang.Integer getNbrRooms () {
		return nbrRooms;
	}

	/**
	 * Set the value related to the column: NBR_ROOMS
	 * @param nbrRooms the NBR_ROOMS value
	 */
	public void setNbrRooms (java.lang.Integer nbrRooms) {
		this.nbrRooms = nbrRooms;
	}



	/**
	 * Return the value associated with the column: SECTION_NUMBER
	 */
	public java.lang.Integer getSectionNumberCache () {
		return sectionNumberCache;
	}

	/**
	 * Set the value related to the column: SECTION_NUMBER
	 * @param sectionNumberCache the SECTION_NUMBER value
	 */
	public void setSectionNumberCache (java.lang.Integer sectionNumberCache) {
		this.sectionNumberCache = sectionNumberCache;
	}



	/**
	 * Return the value associated with the column: DISPLAY_INSTRUCTOR
	 */
	public java.lang.Boolean isDisplayInstructor () {
		return displayInstructor;
	}

	/**
	 * Set the value related to the column: DISPLAY_INSTRUCTOR
	 * @param displayInstructor the DISPLAY_INSTRUCTOR value
	 */
	public void setDisplayInstructor (java.lang.Boolean displayInstructor) {
		this.displayInstructor = displayInstructor;
	}



	/**
	 * Return the value associated with the column: SCHED_PRINT_NOTE
	 */
	public java.lang.String getSchedulePrintNote () {
		return schedulePrintNote;
	}

	/**
	 * Set the value related to the column: SCHED_PRINT_NOTE
	 * @param schedulePrintNote the SCHED_PRINT_NOTE value
	 */
	public void setSchedulePrintNote (java.lang.String schedulePrintNote) {
		this.schedulePrintNote = schedulePrintNote;
	}



	/**
	 * Return the value associated with the column: CLASS_SUFFIX
	 */
	public java.lang.String getClassSuffix () {
		return classSuffix;
	}

	/**
	 * Set the value related to the column: CLASS_SUFFIX
	 * @param classSuffix the CLASS_SUFFIX value
	 */
	public void setClassSuffix (java.lang.String classSuffix) {
		this.classSuffix = classSuffix;
	}



	/**
	 * Return the value associated with the column: DISPLAY_IN_SCHED_BOOK
	 */
	public java.lang.Boolean isDisplayInScheduleBook () {
		return displayInScheduleBook;
	}

	/**
	 * Set the value related to the column: DISPLAY_IN_SCHED_BOOK
	 * @param displayInScheduleBook the DISPLAY_IN_SCHED_BOOK value
	 */
	public void setDisplayInScheduleBook (java.lang.Boolean displayInScheduleBook) {
		this.displayInScheduleBook = displayInScheduleBook;
	}



	/**
	 * Return the value associated with the column: MAX_EXPECTED_CAPACITY
	 */
	public java.lang.Integer getMaxExpectedCapacity () {
		return maxExpectedCapacity;
	}

	/**
	 * Set the value related to the column: MAX_EXPECTED_CAPACITY
	 * @param maxExpectedCapacity the MAX_EXPECTED_CAPACITY value
	 */
	public void setMaxExpectedCapacity (java.lang.Integer maxExpectedCapacity) {
		this.maxExpectedCapacity = maxExpectedCapacity;
	}



	/**
	 * Return the value associated with the column: ROOM_RATIO
	 */
	public java.lang.Float getRoomRatio () {
		return roomRatio;
	}

	/**
	 * Set the value related to the column: ROOM_RATIO
	 * @param roomRatio the ROOM_RATIO value
	 */
	public void setRoomRatio (java.lang.Float roomRatio) {
		this.roomRatio = roomRatio;
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
	 * Return the value associated with the column: enrollment
	 */
	public java.lang.Integer getEnrollment () {
		return enrollment;
	}

	/**
	 * Set the value related to the column: enrollment
	 * @param enrollment the enrollment value
	 */
	public void setEnrollment (java.lang.Integer enrollment) {
		this.enrollment = enrollment;
	}



	/**
	 * Return the value associated with the column: sectioningInfo
	 */
	public org.unitime.timetable.model.SectioningInfo getSectioningInfo () {
		return sectioningInfo;
	}

	/**
	 * Set the value related to the column: sectioningInfo
	 * @param sectioningInfo the sectioningInfo value
	 */
	public void setSectioningInfo (org.unitime.timetable.model.SectioningInfo sectioningInfo) {
		this.sectioningInfo = sectioningInfo;
	}



	/**
	 * Return the value associated with the column: controllingDept
	 */
	public org.unitime.timetable.model.Department getControllingDept () {
		return controllingDept;
	}

	/**
	 * Set the value related to the column: controllingDept
	 * @param controllingDept the controllingDept value
	 */
	public void setControllingDept (org.unitime.timetable.model.Department controllingDept) {
		this.controllingDept = controllingDept;
	}



	/**
	 * Return the value associated with the column: MANAGING_DEPT
	 */
	public org.unitime.timetable.model.Department getManagingDept () {
		return managingDept;
	}

	/**
	 * Set the value related to the column: MANAGING_DEPT
	 * @param managingDept the MANAGING_DEPT value
	 */
	public void setManagingDept (org.unitime.timetable.model.Department managingDept) {
		this.managingDept = managingDept;
	}



	/**
	 * Return the value associated with the column: SUBPART_ID
	 */
	public org.unitime.timetable.model.SchedulingSubpart getSchedulingSubpart () {
		return schedulingSubpart;
	}

	/**
	 * Set the value related to the column: SUBPART_ID
	 * @param schedulingSubpart the SUBPART_ID value
	 */
	public void setSchedulingSubpart (org.unitime.timetable.model.SchedulingSubpart schedulingSubpart) {
		this.schedulingSubpart = schedulingSubpart;
	}



	/**
	 * Return the value associated with the column: PARENT_CLASS_ID
	 */
	public org.unitime.timetable.model.Class_ getParentClass () {
		return parentClass;
	}

	/**
	 * Set the value related to the column: PARENT_CLASS_ID
	 * @param parentClass the PARENT_CLASS_ID value
	 */
	public void setParentClass (org.unitime.timetable.model.Class_ parentClass) {
		this.parentClass = parentClass;
	}



	/**
	 * Return the value associated with the column: DATE_PATTERN_ID
	 */
	public org.unitime.timetable.model.DatePattern getDatePattern () {
		return datePattern;
	}

	/**
	 * Set the value related to the column: DATE_PATTERN_ID
	 * @param datePattern the DATE_PATTERN_ID value
	 */
	public void setDatePattern (org.unitime.timetable.model.DatePattern datePattern) {
		this.datePattern = datePattern;
	}



	/**
	 * Return the value associated with the column: committedAssignment
	 */
	public org.unitime.timetable.model.Assignment getCommittedAssignment () {
		return committedAssignment;
	}

	/**
	 * Set the value related to the column: committedAssignment
	 * @param committedAssignment the committedAssignment value
	 */
	public void setCommittedAssignment (org.unitime.timetable.model.Assignment committedAssignment) {
		this.committedAssignment = committedAssignment;
	}



	/**
	 * Return the value associated with the column: childClasses
	 */
	public java.util.Set getChildClasses () {
		return childClasses;
	}

	/**
	 * Set the value related to the column: childClasses
	 * @param childClasses the childClasses value
	 */
	public void setChildClasses (java.util.Set childClasses) {
		this.childClasses = childClasses;
	}

	public void addTochildClasses (org.unitime.timetable.model.Class_ class_) {
		if (null == getChildClasses()) setChildClasses(new java.util.HashSet());
		getChildClasses().add(class_);
	}



	/**
	 * Return the value associated with the column: classInstructors
	 */
	public java.util.Set getClassInstructors () {
		return classInstructors;
	}

	/**
	 * Set the value related to the column: classInstructors
	 * @param classInstructors the classInstructors value
	 */
	public void setClassInstructors (java.util.Set classInstructors) {
		this.classInstructors = classInstructors;
	}

	public void addToclassInstructors (org.unitime.timetable.model.ClassInstructor classInstructor) {
		if (null == getClassInstructors()) setClassInstructors(new java.util.HashSet());
		getClassInstructors().add(classInstructor);
	}



	/**
	 * Return the value associated with the column: assignments
	 */
	public java.util.Set getAssignments () {
		return assignments;
	}

	/**
	 * Set the value related to the column: assignments
	 * @param assignments the assignments value
	 */
	public void setAssignments (java.util.Set assignments) {
		this.assignments = assignments;
	}

	public void addToassignments (org.unitime.timetable.model.Assignment assignment) {
		if (null == getAssignments()) setAssignments(new java.util.HashSet());
		getAssignments().add(assignment);
	}



	/**
	 * Return the value associated with the column: studentEnrollments
	 */
	public java.util.Set getStudentEnrollments () {
		return studentEnrollments;
	}

	/**
	 * Set the value related to the column: studentEnrollments
	 * @param studentEnrollments the studentEnrollments value
	 */
	public void setStudentEnrollments (java.util.Set studentEnrollments) {
		this.studentEnrollments = studentEnrollments;
	}

	public void addTostudentEnrollments (org.unitime.timetable.model.StudentClassEnrollment studentClassEnrollment) {
		if (null == getStudentEnrollments()) setStudentEnrollments(new java.util.HashSet());
		getStudentEnrollments().add(studentClassEnrollment);
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
	 * Return the value associated with the column: individualReservations
	 */
	public java.util.Set getIndividualReservations () {
		return individualReservations;
	}

	/**
	 * Set the value related to the column: individualReservations
	 * @param individualReservations the individualReservations value
	 */
	public void setIndividualReservations (java.util.Set individualReservations) {
		this.individualReservations = individualReservations;
	}



	/**
	 * Return the value associated with the column: studentGroupReservations
	 */
	public java.util.Set getStudentGroupReservations () {
		return studentGroupReservations;
	}

	/**
	 * Set the value related to the column: studentGroupReservations
	 * @param studentGroupReservations the studentGroupReservations value
	 */
	public void setStudentGroupReservations (java.util.Set studentGroupReservations) {
		this.studentGroupReservations = studentGroupReservations;
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



	/**
	 * Return the value associated with the column: posReservations
	 */
	public java.util.Set getPosReservations () {
		return posReservations;
	}

	/**
	 * Set the value related to the column: posReservations
	 * @param posReservations the posReservations value
	 */
	public void setPosReservations (java.util.Set posReservations) {
		this.posReservations = posReservations;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Class_)) return false;
		else {
			org.unitime.timetable.model.Class_ class_ = (org.unitime.timetable.model.Class_) obj;
			if (null == this.getUniqueId() || null == class_.getUniqueId()) return false;
			else return (this.getUniqueId().equals(class_.getUniqueId()));
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