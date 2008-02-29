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
 * This is an object that contains data related to the STUDENT table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="STUDENT"
 */

public abstract class BaseStudent  implements Serializable {

	public static String REF = "Student";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_FIRST_NAME = "firstName";
	public static String PROP_MIDDLE_NAME = "middleName";
	public static String PROP_LAST_NAME = "lastName";
	public static String PROP_EMAIL = "email";
	public static String PROP_FREE_TIME_CATEGORY = "freeTimeCategory";
	public static String PROP_SCHEDULE_PREFERENCE = "schedulePreference";
	public static String PROP_STATUS_CHANGE_DATE = "statusChangeDate";


	// constructors
	public BaseStudent () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseStudent (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseStudent (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.String firstName,
		java.lang.String lastName,
		java.lang.Integer freeTimeCategory,
		java.lang.Integer schedulePreference) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setFirstName(firstName);
		this.setLastName(lastName);
		this.setFreeTimeCategory(freeTimeCategory);
		this.setSchedulePreference(schedulePreference);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String externalUniqueId;
	private java.lang.String firstName;
	private java.lang.String middleName;
	private java.lang.String lastName;
	private java.lang.String email;
	private java.lang.Integer freeTimeCategory;
	private java.lang.Integer schedulePreference;
	private java.util.Date statusChangeDate;

	// many to one
	private org.unitime.timetable.model.StudentStatusType status;
	private org.unitime.timetable.model.Session session;

	// collections
	private java.util.Set academicAreaClassifications;
	private java.util.Set posMajors;
	private java.util.Set posMinors;
	private java.util.Set accomodations;
	private java.util.Set groups;
	private java.util.Set waitlists;
	private java.util.Set courseDemands;
	private java.util.Set classEnrollments;
	private java.util.Set lastLikeCourseDemands;



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
	 * Return the value associated with the column: FIRST_NAME
	 */
	public java.lang.String getFirstName () {
		return firstName;
	}

	/**
	 * Set the value related to the column: FIRST_NAME
	 * @param firstName the FIRST_NAME value
	 */
	public void setFirstName (java.lang.String firstName) {
		this.firstName = firstName;
	}



	/**
	 * Return the value associated with the column: MIDDLE_NAME
	 */
	public java.lang.String getMiddleName () {
		return middleName;
	}

	/**
	 * Set the value related to the column: MIDDLE_NAME
	 * @param middleName the MIDDLE_NAME value
	 */
	public void setMiddleName (java.lang.String middleName) {
		this.middleName = middleName;
	}



	/**
	 * Return the value associated with the column: LAST_NAME
	 */
	public java.lang.String getLastName () {
		return lastName;
	}

	/**
	 * Set the value related to the column: LAST_NAME
	 * @param lastName the LAST_NAME value
	 */
	public void setLastName (java.lang.String lastName) {
		this.lastName = lastName;
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
	 * Return the value associated with the column: FREE_TIME_CAT
	 */
	public java.lang.Integer getFreeTimeCategory () {
		return freeTimeCategory;
	}

	/**
	 * Set the value related to the column: FREE_TIME_CAT
	 * @param freeTimeCategory the FREE_TIME_CAT value
	 */
	public void setFreeTimeCategory (java.lang.Integer freeTimeCategory) {
		this.freeTimeCategory = freeTimeCategory;
	}



	/**
	 * Return the value associated with the column: SCHEDULE_PREFERENCE
	 */
	public java.lang.Integer getSchedulePreference () {
		return schedulePreference;
	}

	/**
	 * Set the value related to the column: SCHEDULE_PREFERENCE
	 * @param schedulePreference the SCHEDULE_PREFERENCE value
	 */
	public void setSchedulePreference (java.lang.Integer schedulePreference) {
		this.schedulePreference = schedulePreference;
	}



	/**
	 * Return the value associated with the column: STATUS_CHANGE_DATE
	 */
	public java.util.Date getStatusChangeDate () {
		return statusChangeDate;
	}

	/**
	 * Set the value related to the column: STATUS_CHANGE_DATE
	 * @param statusChangeDate the STATUS_CHANGE_DATE value
	 */
	public void setStatusChangeDate (java.util.Date statusChangeDate) {
		this.statusChangeDate = statusChangeDate;
	}



	/**
	 * Return the value associated with the column: STATUS_TYPE_ID
	 */
	public org.unitime.timetable.model.StudentStatusType getStatus () {
		return status;
	}

	/**
	 * Set the value related to the column: STATUS_TYPE_ID
	 * @param status the STATUS_TYPE_ID value
	 */
	public void setStatus (org.unitime.timetable.model.StudentStatusType status) {
		this.status = status;
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
	 * Return the value associated with the column: academicAreaClassifications
	 */
	public java.util.Set getAcademicAreaClassifications () {
		return academicAreaClassifications;
	}

	/**
	 * Set the value related to the column: academicAreaClassifications
	 * @param academicAreaClassifications the academicAreaClassifications value
	 */
	public void setAcademicAreaClassifications (java.util.Set academicAreaClassifications) {
		this.academicAreaClassifications = academicAreaClassifications;
	}

	public void addToacademicAreaClassifications (org.unitime.timetable.model.AcademicAreaClassification academicAreaClassification) {
		if (null == getAcademicAreaClassifications()) setAcademicAreaClassifications(new java.util.HashSet());
		getAcademicAreaClassifications().add(academicAreaClassification);
	}



	/**
	 * Return the value associated with the column: posMajors
	 */
	public java.util.Set getPosMajors () {
		return posMajors;
	}

	/**
	 * Set the value related to the column: posMajors
	 * @param posMajors the posMajors value
	 */
	public void setPosMajors (java.util.Set posMajors) {
		this.posMajors = posMajors;
	}



	/**
	 * Return the value associated with the column: posMinors
	 */
	public java.util.Set getPosMinors () {
		return posMinors;
	}

	/**
	 * Set the value related to the column: posMinors
	 * @param posMinors the posMinors value
	 */
	public void setPosMinors (java.util.Set posMinors) {
		this.posMinors = posMinors;
	}



	/**
	 * Return the value associated with the column: accomodations
	 */
	public java.util.Set getAccomodations () {
		return accomodations;
	}

	/**
	 * Set the value related to the column: accomodations
	 * @param accomodations the accomodations value
	 */
	public void setAccomodations (java.util.Set accomodations) {
		this.accomodations = accomodations;
	}



	/**
	 * Return the value associated with the column: groups
	 */
	public java.util.Set getGroups () {
		return groups;
	}

	/**
	 * Set the value related to the column: groups
	 * @param groups the groups value
	 */
	public void setGroups (java.util.Set groups) {
		this.groups = groups;
	}



	/**
	 * Return the value associated with the column: waitlists
	 */
	public java.util.Set getWaitlists () {
		return waitlists;
	}

	/**
	 * Set the value related to the column: waitlists
	 * @param waitlists the waitlists value
	 */
	public void setWaitlists (java.util.Set waitlists) {
		this.waitlists = waitlists;
	}

	public void addTowaitlists (org.unitime.timetable.model.WaitList waitList) {
		if (null == getWaitlists()) setWaitlists(new java.util.HashSet());
		getWaitlists().add(waitList);
	}



	/**
	 * Return the value associated with the column: courseDemands
	 */
	public java.util.Set getCourseDemands () {
		return courseDemands;
	}

	/**
	 * Set the value related to the column: courseDemands
	 * @param courseDemands the courseDemands value
	 */
	public void setCourseDemands (java.util.Set courseDemands) {
		this.courseDemands = courseDemands;
	}

	public void addTocourseDemands (org.unitime.timetable.model.CourseDemand courseDemand) {
		if (null == getCourseDemands()) setCourseDemands(new java.util.HashSet());
		getCourseDemands().add(courseDemand);
	}



	/**
	 * Return the value associated with the column: classEnrollments
	 */
	public java.util.Set getClassEnrollments () {
		return classEnrollments;
	}

	/**
	 * Set the value related to the column: classEnrollments
	 * @param classEnrollments the classEnrollments value
	 */
	public void setClassEnrollments (java.util.Set classEnrollments) {
		this.classEnrollments = classEnrollments;
	}

	public void addToclassEnrollments (org.unitime.timetable.model.StudentClassEnrollment studentClassEnrollment) {
		if (null == getClassEnrollments()) setClassEnrollments(new java.util.HashSet());
		getClassEnrollments().add(studentClassEnrollment);
	}



	/**
	 * Return the value associated with the column: lastLikeCourseDemands
	 */
	public java.util.Set getLastLikeCourseDemands () {
		return lastLikeCourseDemands;
	}

	/**
	 * Set the value related to the column: lastLikeCourseDemands
	 * @param lastLikeCourseDemands the lastLikeCourseDemands value
	 */
	public void setLastLikeCourseDemands (java.util.Set lastLikeCourseDemands) {
		this.lastLikeCourseDemands = lastLikeCourseDemands;
	}

	public void addTolastLikeCourseDemands (org.unitime.timetable.model.LastLikeCourseDemand lastLikeCourseDemand) {
		if (null == getLastLikeCourseDemands()) setLastLikeCourseDemands(new java.util.HashSet());
		getLastLikeCourseDemands().add(lastLikeCourseDemand);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Student)) return false;
		else {
			org.unitime.timetable.model.Student student = (org.unitime.timetable.model.Student) obj;
			if (null == this.getUniqueId() || null == student.getUniqueId()) return false;
			else return (this.getUniqueId().equals(student.getUniqueId()));
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