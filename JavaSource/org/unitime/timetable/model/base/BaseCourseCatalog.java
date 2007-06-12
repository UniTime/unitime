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
 * This is an object that contains data related to the COURSE_CATALOG table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="COURSE_CATALOG"
 */

public abstract class BaseCourseCatalog  implements Serializable {

	public static String REF = "CourseCatalog";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_SUBJECT = "subject";
	public static String PROP_COURSE_NUMBER = "courseNumber";
	public static String PROP_TITLE = "title";
	public static String PROP_PERMANENT_ID = "permanentId";
	public static String PROP_APPROVAL_TYPE = "approvalType";
	public static String PROP_DESIGNATOR_REQUIRED = "designatorRequired";
	public static String PROP_PREVIOUS_SUBJECT = "previousSubject";
	public static String PROP_PREVIOUS_COURSE_NUMBER = "previousCourseNumber";
	public static String PROP_CREDIT_TYPE = "creditType";
	public static String PROP_CREDIT_UNIT_TYPE = "creditUnitType";
	public static String PROP_CREDIT_FORMAT = "creditFormat";
	public static String PROP_FIXED_MINIMUM_CREDIT = "fixedMinimumCredit";
	public static String PROP_MAXIMUM_CREDIT = "maximumCredit";
	public static String PROP_FRACTIONAL_CREDIT_ALLOWED = "fractionalCreditAllowed";


	// constructors
	public BaseCourseCatalog () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCourseCatalog (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCourseCatalog (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.String subject,
		java.lang.String courseNumber,
		java.lang.String title,
		java.lang.Boolean designatorRequired,
		java.lang.String creditType,
		java.lang.String creditUnitType,
		java.lang.String creditFormat,
		java.lang.Float fixedMinimumCredit) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setSubject(subject);
		this.setCourseNumber(courseNumber);
		this.setTitle(title);
		this.setDesignatorRequired(designatorRequired);
		this.setCreditType(creditType);
		this.setCreditUnitType(creditUnitType);
		this.setCreditFormat(creditFormat);
		this.setFixedMinimumCredit(fixedMinimumCredit);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String externalUniqueId;
	private java.lang.String subject;
	private java.lang.String courseNumber;
	private java.lang.String title;
	private java.lang.String permanentId;
	private java.lang.String approvalType;
	private java.lang.Boolean designatorRequired;
	private java.lang.String previousSubject;
	private java.lang.String previousCourseNumber;
	private java.lang.String creditType;
	private java.lang.String creditUnitType;
	private java.lang.String creditFormat;
	private java.lang.Float fixedMinimumCredit;
	private java.lang.Float maximumCredit;
	private java.lang.Boolean fractionalCreditAllowed;

	// many to one
	private org.unitime.timetable.model.Session session;

	// collections
	private java.util.Set subparts;



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
	 * Return the value associated with the column: SUBJECT
	 */
	public java.lang.String getSubject () {
		return subject;
	}

	/**
	 * Set the value related to the column: SUBJECT
	 * @param subject the SUBJECT value
	 */
	public void setSubject (java.lang.String subject) {
		this.subject = subject;
	}



	/**
	 * Return the value associated with the column: COURSE_NBR
	 */
	public java.lang.String getCourseNumber () {
		return courseNumber;
	}

	/**
	 * Set the value related to the column: COURSE_NBR
	 * @param courseNumber the COURSE_NBR value
	 */
	public void setCourseNumber (java.lang.String courseNumber) {
		this.courseNumber = courseNumber;
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
	 * Return the value associated with the column: PERM_ID
	 */
	public java.lang.String getPermanentId () {
		return permanentId;
	}

	/**
	 * Set the value related to the column: PERM_ID
	 * @param permanentId the PERM_ID value
	 */
	public void setPermanentId (java.lang.String permanentId) {
		this.permanentId = permanentId;
	}



	/**
	 * Return the value associated with the column: APPROVAL_TYPE
	 */
	public java.lang.String getApprovalType () {
		return approvalType;
	}

	/**
	 * Set the value related to the column: APPROVAL_TYPE
	 * @param approvalType the APPROVAL_TYPE value
	 */
	public void setApprovalType (java.lang.String approvalType) {
		this.approvalType = approvalType;
	}



	/**
	 * Return the value associated with the column: DESIGNATOR_REQ
	 */
	public java.lang.Boolean isDesignatorRequired () {
		return designatorRequired;
	}

	/**
	 * Set the value related to the column: DESIGNATOR_REQ
	 * @param designatorRequired the DESIGNATOR_REQ value
	 */
	public void setDesignatorRequired (java.lang.Boolean designatorRequired) {
		this.designatorRequired = designatorRequired;
	}



	/**
	 * Return the value associated with the column: PREV_SUBJECT
	 */
	public java.lang.String getPreviousSubject () {
		return previousSubject;
	}

	/**
	 * Set the value related to the column: PREV_SUBJECT
	 * @param previousSubject the PREV_SUBJECT value
	 */
	public void setPreviousSubject (java.lang.String previousSubject) {
		this.previousSubject = previousSubject;
	}



	/**
	 * Return the value associated with the column: PREV_CRS_NBR
	 */
	public java.lang.String getPreviousCourseNumber () {
		return previousCourseNumber;
	}

	/**
	 * Set the value related to the column: PREV_CRS_NBR
	 * @param previousCourseNumber the PREV_CRS_NBR value
	 */
	public void setPreviousCourseNumber (java.lang.String previousCourseNumber) {
		this.previousCourseNumber = previousCourseNumber;
	}



	/**
	 * Return the value associated with the column: CREDIT_TYPE
	 */
	public java.lang.String getCreditType () {
		return creditType;
	}

	/**
	 * Set the value related to the column: CREDIT_TYPE
	 * @param creditType the CREDIT_TYPE value
	 */
	public void setCreditType (java.lang.String creditType) {
		this.creditType = creditType;
	}



	/**
	 * Return the value associated with the column: CREDIT_UNIT_TYPE
	 */
	public java.lang.String getCreditUnitType () {
		return creditUnitType;
	}

	/**
	 * Set the value related to the column: CREDIT_UNIT_TYPE
	 * @param creditUnitType the CREDIT_UNIT_TYPE value
	 */
	public void setCreditUnitType (java.lang.String creditUnitType) {
		this.creditUnitType = creditUnitType;
	}



	/**
	 * Return the value associated with the column: CREDIT_FORMAT
	 */
	public java.lang.String getCreditFormat () {
		return creditFormat;
	}

	/**
	 * Set the value related to the column: CREDIT_FORMAT
	 * @param creditFormat the CREDIT_FORMAT value
	 */
	public void setCreditFormat (java.lang.String creditFormat) {
		this.creditFormat = creditFormat;
	}



	/**
	 * Return the value associated with the column: FIXED_MIN_CREDIT
	 */
	public java.lang.Float getFixedMinimumCredit () {
		return fixedMinimumCredit;
	}

	/**
	 * Set the value related to the column: FIXED_MIN_CREDIT
	 * @param fixedMinimumCredit the FIXED_MIN_CREDIT value
	 */
	public void setFixedMinimumCredit (java.lang.Float fixedMinimumCredit) {
		this.fixedMinimumCredit = fixedMinimumCredit;
	}



	/**
	 * Return the value associated with the column: MAX_CREDIT
	 */
	public java.lang.Float getMaximumCredit () {
		return maximumCredit;
	}

	/**
	 * Set the value related to the column: MAX_CREDIT
	 * @param maximumCredit the MAX_CREDIT value
	 */
	public void setMaximumCredit (java.lang.Float maximumCredit) {
		this.maximumCredit = maximumCredit;
	}



	/**
	 * Return the value associated with the column: FRAC_CREDIT_ALLOWED
	 */
	public java.lang.Boolean isFractionalCreditAllowed () {
		return fractionalCreditAllowed;
	}

	/**
	 * Set the value related to the column: FRAC_CREDIT_ALLOWED
	 * @param fractionalCreditAllowed the FRAC_CREDIT_ALLOWED value
	 */
	public void setFractionalCreditAllowed (java.lang.Boolean fractionalCreditAllowed) {
		this.fractionalCreditAllowed = fractionalCreditAllowed;
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
	 * Return the value associated with the column: subparts
	 */
	public java.util.Set getSubparts () {
		return subparts;
	}

	/**
	 * Set the value related to the column: subparts
	 * @param subparts the subparts value
	 */
	public void setSubparts (java.util.Set subparts) {
		this.subparts = subparts;
	}

	public void addTosubparts (org.unitime.timetable.model.CourseSubpartCredit courseSubpartCredit) {
		if (null == getSubparts()) setSubparts(new java.util.HashSet());
		getSubparts().add(courseSubpartCredit);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CourseCatalog)) return false;
		else {
			org.unitime.timetable.model.CourseCatalog courseCatalog = (org.unitime.timetable.model.CourseCatalog) obj;
			if (null == this.getUniqueId() || null == courseCatalog.getUniqueId()) return false;
			else return (this.getUniqueId().equals(courseCatalog.getUniqueId()));
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