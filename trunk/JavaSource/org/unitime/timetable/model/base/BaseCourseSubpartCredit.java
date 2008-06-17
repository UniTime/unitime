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
 * This is an object that contains data related to the COURSE_SUBPART_CREDIT table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="COURSE_SUBPART_CREDIT"
 */

public abstract class BaseCourseSubpartCredit  implements Serializable {

	public static String REF = "CourseSubpartCredit";
	public static String PROP_SUBPART_ID = "subpartId";
	public static String PROP_CREDIT_TYPE = "creditType";
	public static String PROP_CREDIT_UNIT_TYPE = "creditUnitType";
	public static String PROP_CREDIT_FORMAT = "creditFormat";
	public static String PROP_FIXED_MINIMUM_CREDIT = "fixedMinimumCredit";
	public static String PROP_MAXIMUM_CREDIT = "maximumCredit";
	public static String PROP_FRACTIONAL_CREDIT_ALLOWED = "fractionalCreditAllowed";


	// constructors
	public BaseCourseSubpartCredit () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCourseSubpartCredit (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCourseSubpartCredit (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CourseCatalog courseCatalog,
		java.lang.String subpartId,
		java.lang.String creditType,
		java.lang.String creditUnitType,
		java.lang.String creditFormat,
		java.lang.Float fixedMinimumCredit) {

		this.setUniqueId(uniqueId);
		this.setCourseCatalog(courseCatalog);
		this.setSubpartId(subpartId);
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
	private java.lang.String subpartId;
	private java.lang.String creditType;
	private java.lang.String creditUnitType;
	private java.lang.String creditFormat;
	private java.lang.Float fixedMinimumCredit;
	private java.lang.Float maximumCredit;
	private java.lang.Boolean fractionalCreditAllowed;

	// many to one
	private org.unitime.timetable.model.CourseCatalog courseCatalog;



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
	 * Return the value associated with the column: SUBPART_ID
	 */
	public java.lang.String getSubpartId () {
		return subpartId;
	}

	/**
	 * Set the value related to the column: SUBPART_ID
	 * @param subpartId the SUBPART_ID value
	 */
	public void setSubpartId (java.lang.String subpartId) {
		this.subpartId = subpartId;
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
	 * Return the value associated with the column: COURSE_CATALOG_ID
	 */
	public org.unitime.timetable.model.CourseCatalog getCourseCatalog () {
		return courseCatalog;
	}

	/**
	 * Set the value related to the column: COURSE_CATALOG_ID
	 * @param courseCatalog the COURSE_CATALOG_ID value
	 */
	public void setCourseCatalog (org.unitime.timetable.model.CourseCatalog courseCatalog) {
		this.courseCatalog = courseCatalog;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CourseSubpartCredit)) return false;
		else {
			org.unitime.timetable.model.CourseSubpartCredit courseSubpartCredit = (org.unitime.timetable.model.CourseSubpartCredit) obj;
			if (null == this.getUniqueId() || null == courseSubpartCredit.getUniqueId()) return false;
			else return (this.getUniqueId().equals(courseSubpartCredit.getUniqueId()));
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
