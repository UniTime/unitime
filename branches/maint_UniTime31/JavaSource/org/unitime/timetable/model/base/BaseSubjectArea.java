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
package org.unitime.timetable.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the SUBJECT_AREA table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SUBJECT_AREA"
 */

public abstract class BaseSubjectArea  implements Serializable {

	public static String REF = "SubjectArea";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_SUBJECT_AREA_ABBREVIATION = "subjectAreaAbbreviation";
	public static String PROP_SHORT_TITLE = "shortTitle";
	public static String PROP_LONG_TITLE = "longTitle";
	public static String PROP_SCHEDULE_BOOK_ONLY = "scheduleBookOnly";
	public static String PROP_PSEUDO_SUBJECT_AREA = "pseudoSubjectArea";


	// constructors
	public BaseSubjectArea () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseSubjectArea (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseSubjectArea (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		org.unitime.timetable.model.Department department,
		java.lang.String externalUniqueId,
		java.lang.String subjectAreaAbbreviation,
		java.lang.String shortTitle,
		java.lang.String longTitle,
		java.lang.Boolean scheduleBookOnly,
		java.lang.Boolean pseudoSubjectArea) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setDepartment(department);
		this.setExternalUniqueId(externalUniqueId);
		this.setSubjectAreaAbbreviation(subjectAreaAbbreviation);
		this.setShortTitle(shortTitle);
		this.setLongTitle(longTitle);
		this.setScheduleBookOnly(scheduleBookOnly);
		this.setPseudoSubjectArea(pseudoSubjectArea);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String externalUniqueId;
	private java.lang.String subjectAreaAbbreviation;
	private java.lang.String shortTitle;
	private java.lang.String longTitle;
	private java.lang.Boolean scheduleBookOnly;
	private java.lang.Boolean pseudoSubjectArea;

	// many to one
	private org.unitime.timetable.model.Session session;
	private org.unitime.timetable.model.Department department;

	// collections
	private java.util.Set courseOfferings;
	private java.util.Set instructionalOfferings;
	private java.util.Set designatorInstructors;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="sequence"
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
	 * Return the value associated with the column: SUBJECT_AREA_ABBREVIATION
	 */
	public java.lang.String getSubjectAreaAbbreviation () {
		return subjectAreaAbbreviation;
	}

	/**
	 * Set the value related to the column: SUBJECT_AREA_ABBREVIATION
	 * @param subjectAreaAbbreviation the SUBJECT_AREA_ABBREVIATION value
	 */
	public void setSubjectAreaAbbreviation (java.lang.String subjectAreaAbbreviation) {
		this.subjectAreaAbbreviation = subjectAreaAbbreviation;
	}



	/**
	 * Return the value associated with the column: SHORT_TITLE
	 */
	public java.lang.String getShortTitle () {
		return shortTitle;
	}

	/**
	 * Set the value related to the column: SHORT_TITLE
	 * @param shortTitle the SHORT_TITLE value
	 */
	public void setShortTitle (java.lang.String shortTitle) {
		this.shortTitle = shortTitle;
	}



	/**
	 * Return the value associated with the column: LONG_TITLE
	 */
	public java.lang.String getLongTitle () {
		return longTitle;
	}

	/**
	 * Set the value related to the column: LONG_TITLE
	 * @param longTitle the LONG_TITLE value
	 */
	public void setLongTitle (java.lang.String longTitle) {
		this.longTitle = longTitle;
	}



	/**
	 * Return the value associated with the column: SCHEDULE_BOOK_ONLY
	 */
	public java.lang.Boolean isScheduleBookOnly () {
		return scheduleBookOnly;
	}

	/**
	 * Set the value related to the column: SCHEDULE_BOOK_ONLY
	 * @param scheduleBookOnly the SCHEDULE_BOOK_ONLY value
	 */
	public void setScheduleBookOnly (java.lang.Boolean scheduleBookOnly) {
		this.scheduleBookOnly = scheduleBookOnly;
	}



	/**
	 * Return the value associated with the column: PSEUDO_SUBJECT_AREA
	 */
	public java.lang.Boolean isPseudoSubjectArea () {
		return pseudoSubjectArea;
	}

	/**
	 * Set the value related to the column: PSEUDO_SUBJECT_AREA
	 * @param pseudoSubjectArea the PSEUDO_SUBJECT_AREA value
	 */
	public void setPseudoSubjectArea (java.lang.Boolean pseudoSubjectArea) {
		this.pseudoSubjectArea = pseudoSubjectArea;
	}



	/**
	 * Return the value associated with the column: SESSION_ID
	 */
	public org.unitime.timetable.model.Session getSession () {
		return session;
	}

	/**
	 * Set the value related to the column: SESSION_ID
	 * @param session the SESSION_ID value
	 */
	public void setSession (org.unitime.timetable.model.Session session) {
		this.session = session;
	}



	/**
	 * Return the value associated with the column: DEPARTMENT_UNIQUEID
	 */
	public org.unitime.timetable.model.Department getDepartment () {
		return department;
	}

	/**
	 * Set the value related to the column: DEPARTMENT_UNIQUEID
	 * @param department the DEPARTMENT_UNIQUEID value
	 */
	public void setDepartment (org.unitime.timetable.model.Department department) {
		this.department = department;
	}



	/**
	 * Return the value associated with the column: courseOfferings
	 */
	public java.util.Set getCourseOfferings () {
		return courseOfferings;
	}

	/**
	 * Set the value related to the column: courseOfferings
	 * @param courseOfferings the courseOfferings value
	 */
	public void setCourseOfferings (java.util.Set courseOfferings) {
		this.courseOfferings = courseOfferings;
	}

	public void addTocourseOfferings (org.unitime.timetable.model.CourseOffering courseOffering) {
		if (null == getCourseOfferings()) setCourseOfferings(new java.util.HashSet());
		getCourseOfferings().add(courseOffering);
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



	/**
	 * Return the value associated with the column: designatorInstructors
	 */
	public java.util.Set getDesignatorInstructors () {
		return designatorInstructors;
	}

	/**
	 * Set the value related to the column: designatorInstructors
	 * @param designatorInstructors the designatorInstructors value
	 */
	public void setDesignatorInstructors (java.util.Set designatorInstructors) {
		this.designatorInstructors = designatorInstructors;
	}

	public void addTodesignatorInstructors (org.unitime.timetable.model.Designator designator) {
		if (null == getDesignatorInstructors()) setDesignatorInstructors(new java.util.HashSet());
		getDesignatorInstructors().add(designator);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.SubjectArea)) return false;
		else {
			org.unitime.timetable.model.SubjectArea subjectArea = (org.unitime.timetable.model.SubjectArea) obj;
			if (null == this.getUniqueId() || null == subjectArea.getUniqueId()) return false;
			else return (this.getUniqueId().equals(subjectArea.getUniqueId()));
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
