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
 * This is an object that contains data related to the SESSIONS table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SESSIONS"
 */

public abstract class BaseSession extends org.unitime.timetable.model.PreferenceGroup  implements Serializable {

	public static String REF = "Session";
	public static String PROP_ACADEMIC_INITIATIVE = "academicInitiative";
	public static String PROP_ACADEMIC_YEAR = "academicYear";
	public static String PROP_ACADEMIC_TERM = "academicTerm";
	public static String PROP_SESSION_BEGIN_DATE_TIME = "sessionBeginDateTime";
	public static String PROP_CLASSES_END_DATE_TIME = "classesEndDateTime";
	public static String PROP_SESSION_END_DATE_TIME = "sessionEndDateTime";
	public static String PROP_EXAM_BEGIN_DATE = "examBeginDate";
	public static String PROP_HOLIDAYS = "holidays";


	// constructors
	public BaseSession () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseSession (java.lang.Long uniqueId) {
		super(uniqueId);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.String academicInitiative;
	private java.lang.String academicYear;
	private java.lang.String academicTerm;
	private java.util.Date sessionBeginDateTime;
	private java.util.Date classesEndDateTime;
	private java.util.Date sessionEndDateTime;
	private java.util.Date examBeginDate;
	private java.lang.String holidays;

	// many to one
	private org.unitime.timetable.model.DepartmentStatusType statusType;
	private org.unitime.timetable.model.DatePattern defaultDatePattern;

	// collections
	private java.util.Set subjectAreas;
	private java.util.Set buildings;
	private java.util.Set departments;
	private java.util.Set rooms;
	private java.util.Set instructionalOfferings;






	/**
	 * Return the value associated with the column: ACADEMIC_INITIATIVE
	 */
	public java.lang.String getAcademicInitiative () {
		return academicInitiative;
	}

	/**
	 * Set the value related to the column: ACADEMIC_INITIATIVE
	 * @param academicInitiative the ACADEMIC_INITIATIVE value
	 */
	public void setAcademicInitiative (java.lang.String academicInitiative) {
		this.academicInitiative = academicInitiative;
	}



	/**
	 * Return the value associated with the column: ACADEMIC_YEAR
	 */
	public java.lang.String getAcademicYear () {
		return academicYear;
	}

	/**
	 * Set the value related to the column: ACADEMIC_YEAR
	 * @param academicYear the ACADEMIC_YEAR value
	 */
	public void setAcademicYear (java.lang.String academicYear) {
		this.academicYear = academicYear;
	}



	/**
	 * Return the value associated with the column: ACADEMIC_TERM
	 */
	public java.lang.String getAcademicTerm () {
		return academicTerm;
	}

	/**
	 * Set the value related to the column: ACADEMIC_TERM
	 * @param academicTerm the ACADEMIC_TERM value
	 */
	public void setAcademicTerm (java.lang.String academicTerm) {
		this.academicTerm = academicTerm;
	}



	/**
	 * Return the value associated with the column: SESSION_BEGIN_DATE_TIME
	 */
	public java.util.Date getSessionBeginDateTime () {
		return sessionBeginDateTime;
	}

	/**
	 * Set the value related to the column: SESSION_BEGIN_DATE_TIME
	 * @param sessionBeginDateTime the SESSION_BEGIN_DATE_TIME value
	 */
	public void setSessionBeginDateTime (java.util.Date sessionBeginDateTime) {
		this.sessionBeginDateTime = sessionBeginDateTime;
	}



	/**
	 * Return the value associated with the column: CLASSES_END_DATE_TIME
	 */
	public java.util.Date getClassesEndDateTime () {
		return classesEndDateTime;
	}

	/**
	 * Set the value related to the column: CLASSES_END_DATE_TIME
	 * @param classesEndDateTime the CLASSES_END_DATE_TIME value
	 */
	public void setClassesEndDateTime (java.util.Date classesEndDateTime) {
		this.classesEndDateTime = classesEndDateTime;
	}



	/**
	 * Return the value associated with the column: SESSION_END_DATE_TIME
	 */
	public java.util.Date getSessionEndDateTime () {
		return sessionEndDateTime;
	}

	/**
	 * Set the value related to the column: SESSION_END_DATE_TIME
	 * @param sessionEndDateTime the SESSION_END_DATE_TIME value
	 */
	public void setSessionEndDateTime (java.util.Date sessionEndDateTime) {
		this.sessionEndDateTime = sessionEndDateTime;
	}



	/**
	 * Return the value associated with the column: EXAM_BEGIN_DATE
	 */
	public java.util.Date getExamBeginDate () {
		return examBeginDate;
	}

	/**
	 * Set the value related to the column: EXAM_BEGIN_DATE
	 * @param examBeginDate the EXAM_BEGIN_DATE value
	 */
	public void setExamBeginDate (java.util.Date examBeginDate) {
		this.examBeginDate = examBeginDate;
	}



	/**
	 * Return the value associated with the column: HOLIDAYS
	 */
	public java.lang.String getHolidays () {
		return holidays;
	}

	/**
	 * Set the value related to the column: HOLIDAYS
	 * @param holidays the HOLIDAYS value
	 */
	public void setHolidays (java.lang.String holidays) {
		this.holidays = holidays;
	}



	/**
	 * Return the value associated with the column: STATUS_TYPE
	 */
	public org.unitime.timetable.model.DepartmentStatusType getStatusType () {
		return statusType;
	}

	/**
	 * Set the value related to the column: STATUS_TYPE
	 * @param statusType the STATUS_TYPE value
	 */
	public void setStatusType (org.unitime.timetable.model.DepartmentStatusType statusType) {
		this.statusType = statusType;
	}



	/**
	 * Return the value associated with the column: DEF_DATEPATT_ID
	 */
	public org.unitime.timetable.model.DatePattern getDefaultDatePattern () {
		return defaultDatePattern;
	}

	/**
	 * Set the value related to the column: DEF_DATEPATT_ID
	 * @param defaultDatePattern the DEF_DATEPATT_ID value
	 */
	public void setDefaultDatePattern (org.unitime.timetable.model.DatePattern defaultDatePattern) {
		this.defaultDatePattern = defaultDatePattern;
	}



	/**
	 * Return the value associated with the column: subjectAreas
	 */
	public java.util.Set getSubjectAreas () {
		return subjectAreas;
	}

	/**
	 * Set the value related to the column: subjectAreas
	 * @param subjectAreas the subjectAreas value
	 */
	public void setSubjectAreas (java.util.Set subjectAreas) {
		this.subjectAreas = subjectAreas;
	}

	public void addTosubjectAreas (org.unitime.timetable.model.SubjectArea subjectArea) {
		if (null == getSubjectAreas()) setSubjectAreas(new java.util.HashSet());
		getSubjectAreas().add(subjectArea);
	}



	/**
	 * Return the value associated with the column: buildings
	 */
	public java.util.Set getBuildings () {
		return buildings;
	}

	/**
	 * Set the value related to the column: buildings
	 * @param buildings the buildings value
	 */
	public void setBuildings (java.util.Set buildings) {
		this.buildings = buildings;
	}



	/**
	 * Return the value associated with the column: departments
	 */
	public java.util.Set getDepartments () {
		return departments;
	}

	/**
	 * Set the value related to the column: departments
	 * @param departments the departments value
	 */
	public void setDepartments (java.util.Set departments) {
		this.departments = departments;
	}

	public void addTodepartments (org.unitime.timetable.model.Department department) {
		if (null == getDepartments()) setDepartments(new java.util.HashSet());
		getDepartments().add(department);
	}



	/**
	 * Return the value associated with the column: rooms
	 */
	public java.util.Set getRooms () {
		return rooms;
	}

	/**
	 * Set the value related to the column: rooms
	 * @param rooms the rooms value
	 */
	public void setRooms (java.util.Set rooms) {
		this.rooms = rooms;
	}

	public void addTorooms (org.unitime.timetable.model.Location location) {
		if (null == getRooms()) setRooms(new java.util.HashSet());
		getRooms().add(location);
	}



	/**
	 * Return the value associated with the column: instructionalOfferings
	 */
	public java.util.Set getInstructionalOfferings () {
		return instructionalOfferings;
	}

	/**
	 * Set the value related to the column: instructionalOfferings
	 * @param instructionalOfferings the instructionalOfferings value
	 */
	public void setInstructionalOfferings (java.util.Set instructionalOfferings) {
		this.instructionalOfferings = instructionalOfferings;
	}

	public void addToinstructionalOfferings (org.unitime.timetable.model.InstructionalOffering instructionalOffering) {
		if (null == getInstructionalOfferings()) setInstructionalOfferings(new java.util.HashSet());
		getInstructionalOfferings().add(instructionalOffering);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Session)) return false;
		else {
			org.unitime.timetable.model.Session session = (org.unitime.timetable.model.Session) obj;
			if (null == this.getUniqueId() || null == session.getUniqueId()) return false;
			else return (this.getUniqueId().equals(session.getUniqueId()));
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