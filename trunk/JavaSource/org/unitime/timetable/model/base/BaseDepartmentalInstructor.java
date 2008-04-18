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
 * This is an object that contains data related to the DEPARTMENTAL_INSTRUCTOR table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="DEPARTMENTAL_INSTRUCTOR"
 */

public abstract class BaseDepartmentalInstructor extends org.unitime.timetable.model.PreferenceGroup  implements Serializable {

	public static String REF = "DepartmentalInstructor";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_CAREER_ACCT = "careerAcct";
	public static String PROP_FIRST_NAME = "firstName";
	public static String PROP_MIDDLE_NAME = "middleName";
	public static String PROP_LAST_NAME = "lastName";
	public static String PROP_NOTE = "note";
	public static String PROP_EMAIL = "email";
	public static String PROP_IGNORE_TO_FAR = "ignoreToFar";


	// constructors
	public BaseDepartmentalInstructor () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseDepartmentalInstructor (java.lang.Long uniqueId) {
		super(uniqueId);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.String externalUniqueId;
	private java.lang.String careerAcct;
	private java.lang.String firstName;
	private java.lang.String middleName;
	private java.lang.String lastName;
	private java.lang.String note;
	private java.lang.String email;
	private java.lang.Boolean ignoreToFar;

	// many to one
	private org.unitime.timetable.model.PositionType positionType;
	private org.unitime.timetable.model.Department department;

	// collections
	private java.util.Set classes;
	private java.util.Set designatorSubjectAreas;
	private java.util.Set assignments;






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
	 * Return the value associated with the column: CAREER_ACCT
	 */
	public java.lang.String getCareerAcct () {
		return careerAcct;
	}

	/**
	 * Set the value related to the column: CAREER_ACCT
	 * @param careerAcct the CAREER_ACCT value
	 */
	public void setCareerAcct (java.lang.String careerAcct) {
		this.careerAcct = careerAcct;
	}



	/**
	 * Return the value associated with the column: FNAME
	 */
	public java.lang.String getFirstName () {
		return firstName;
	}

	/**
	 * Set the value related to the column: FNAME
	 * @param firstName the FNAME value
	 */
	public void setFirstName (java.lang.String firstName) {
		this.firstName = firstName;
	}



	/**
	 * Return the value associated with the column: MNAME
	 */
	public java.lang.String getMiddleName () {
		return middleName;
	}

	/**
	 * Set the value related to the column: MNAME
	 * @param middleName the MNAME value
	 */
	public void setMiddleName (java.lang.String middleName) {
		this.middleName = middleName;
	}



	/**
	 * Return the value associated with the column: LNAME
	 */
	public java.lang.String getLastName () {
		return lastName;
	}

	/**
	 * Set the value related to the column: LNAME
	 * @param lastName the LNAME value
	 */
	public void setLastName (java.lang.String lastName) {
		this.lastName = lastName;
	}



	/**
	 * Return the value associated with the column: NOTE
	 */
	public java.lang.String getNote () {
		return note;
	}

	/**
	 * Set the value related to the column: NOTE
	 * @param note the NOTE value
	 */
	public void setNote (java.lang.String note) {
		this.note = note;
	}



	/**
	 * Return the value associated with the column: EMAIL
	 */
	public java.lang.String getEmail () {
		return email;
	}

	/**
	 * Set the value related to the column: EMAIL
	 * @param email the EMAIL value
	 */
	public void setEmail (java.lang.String email) {
		this.email = email;
	}



	/**
	 * Return the value associated with the column: IGNORE_TOO_FAR
	 */
	public java.lang.Boolean isIgnoreToFar () {
		return ignoreToFar;
	}

	/**
	 * Set the value related to the column: IGNORE_TOO_FAR
	 * @param ignoreToFar the IGNORE_TOO_FAR value
	 */
	public void setIgnoreToFar (java.lang.Boolean ignoreToFar) {
		this.ignoreToFar = ignoreToFar;
	}



	/**
	 * Return the value associated with the column: POS_CODE_TYPE
	 */
	public org.unitime.timetable.model.PositionType getPositionType () {
		return positionType;
	}

	/**
	 * Set the value related to the column: POS_CODE_TYPE
	 * @param positionType the POS_CODE_TYPE value
	 */
	public void setPositionType (org.unitime.timetable.model.PositionType positionType) {
		this.positionType = positionType;
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
	 * Return the value associated with the column: classes
	 */
	public java.util.Set getClasses () {
		return classes;
	}

	/**
	 * Set the value related to the column: classes
	 * @param classes the classes value
	 */
	public void setClasses (java.util.Set classes) {
		this.classes = classes;
	}

	public void addToclasses (org.unitime.timetable.model.ClassInstructor classInstructor) {
		if (null == getClasses()) setClasses(new java.util.HashSet());
		getClasses().add(classInstructor);
	}



	/**
	 * Return the value associated with the column: designatorSubjectAreas
	 */
	public java.util.Set getDesignatorSubjectAreas () {
		return designatorSubjectAreas;
	}

	/**
	 * Set the value related to the column: designatorSubjectAreas
	 * @param designatorSubjectAreas the designatorSubjectAreas value
	 */
	public void setDesignatorSubjectAreas (java.util.Set designatorSubjectAreas) {
		this.designatorSubjectAreas = designatorSubjectAreas;
	}

	public void addTodesignatorSubjectAreas (org.unitime.timetable.model.Designator designator) {
		if (null == getDesignatorSubjectAreas()) setDesignatorSubjectAreas(new java.util.HashSet());
		getDesignatorSubjectAreas().add(designator);
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





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.DepartmentalInstructor)) return false;
		else {
			org.unitime.timetable.model.DepartmentalInstructor departmentalInstructor = (org.unitime.timetable.model.DepartmentalInstructor) obj;
			if (null == this.getUniqueId() || null == departmentalInstructor.getUniqueId()) return false;
			else return (this.getUniqueId().equals(departmentalInstructor.getUniqueId()));
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