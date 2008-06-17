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
 * This is an object that contains data related to the COURSE_CREDIT_UNIT_CONFIG table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="COURSE_CREDIT_UNIT_CONFIG"
 */

public abstract class BaseCourseCreditUnitConfig  implements Serializable {

	public static String REF = "CourseCreditUnitConfig";
	public static String PROP_CREDIT_FORMAT = "creditFormat";
	public static String PROP_DEFINES_CREDIT_AT_COURSE_LEVEL = "definesCreditAtCourseLevel";


	// constructors
	public BaseCourseCreditUnitConfig () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCourseCreditUnitConfig (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCourseCreditUnitConfig (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CourseCreditType creditType,
		org.unitime.timetable.model.CourseCreditUnitType creditUnitType,
		java.lang.String creditFormat,
		java.lang.Boolean definesCreditAtCourseLevel) {

		this.setUniqueId(uniqueId);
		this.setCreditType(creditType);
		this.setCreditUnitType(creditUnitType);
		this.setCreditFormat(creditFormat);
		this.setDefinesCreditAtCourseLevel(definesCreditAtCourseLevel);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String creditFormat;
	private java.lang.Boolean definesCreditAtCourseLevel;

	// many to one
	private org.unitime.timetable.model.CourseCreditType creditType;
	private org.unitime.timetable.model.CourseCreditUnitType creditUnitType;
	private org.unitime.timetable.model.SchedulingSubpart subpartOwner;
	private org.unitime.timetable.model.InstructionalOffering instructionalOfferingOwner;



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
	 * Return the value associated with the column: DEFINES_CREDIT_AT_COURSE_LEVEL
	 */
	public java.lang.Boolean isDefinesCreditAtCourseLevel () {
		return definesCreditAtCourseLevel;
	}

	/**
	 * Set the value related to the column: DEFINES_CREDIT_AT_COURSE_LEVEL
	 * @param definesCreditAtCourseLevel the DEFINES_CREDIT_AT_COURSE_LEVEL value
	 */
	public void setDefinesCreditAtCourseLevel (java.lang.Boolean definesCreditAtCourseLevel) {
		this.definesCreditAtCourseLevel = definesCreditAtCourseLevel;
	}



	/**
	 * Return the value associated with the column: CREDIT_TYPE
	 */
	public org.unitime.timetable.model.CourseCreditType getCreditType () {
		return creditType;
	}

	/**
	 * Set the value related to the column: CREDIT_TYPE
	 * @param creditType the CREDIT_TYPE value
	 */
	public void setCreditType (org.unitime.timetable.model.CourseCreditType creditType) {
		this.creditType = creditType;
	}



	/**
	 * Return the value associated with the column: CREDIT_UNIT_TYPE
	 */
	public org.unitime.timetable.model.CourseCreditUnitType getCreditUnitType () {
		return creditUnitType;
	}

	/**
	 * Set the value related to the column: CREDIT_UNIT_TYPE
	 * @param creditUnitType the CREDIT_UNIT_TYPE value
	 */
	public void setCreditUnitType (org.unitime.timetable.model.CourseCreditUnitType creditUnitType) {
		this.creditUnitType = creditUnitType;
	}



	/**
	 * Return the value associated with the column: OWNER_ID
	 */
	public org.unitime.timetable.model.SchedulingSubpart getSubpartOwner () {
		return subpartOwner;
	}

	/**
	 * Set the value related to the column: OWNER_ID
	 * @param subpartOwner the OWNER_ID value
	 */
	public void setSubpartOwner (org.unitime.timetable.model.SchedulingSubpart subpartOwner) {
		this.subpartOwner = subpartOwner;
	}



	/**
	 * Return the value associated with the column: INSTR_OFFR_ID
	 */
	public org.unitime.timetable.model.InstructionalOffering getInstructionalOfferingOwner () {
		return instructionalOfferingOwner;
	}

	/**
	 * Set the value related to the column: INSTR_OFFR_ID
	 * @param instructionalOfferingOwner the INSTR_OFFR_ID value
	 */
	public void setInstructionalOfferingOwner (org.unitime.timetable.model.InstructionalOffering instructionalOfferingOwner) {
		this.instructionalOfferingOwner = instructionalOfferingOwner;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CourseCreditUnitConfig)) return false;
		else {
			org.unitime.timetable.model.CourseCreditUnitConfig courseCreditUnitConfig = (org.unitime.timetable.model.CourseCreditUnitConfig) obj;
			if (null == this.getUniqueId() || null == courseCreditUnitConfig.getUniqueId()) return false;
			else return (this.getUniqueId().equals(courseCreditUnitConfig.getUniqueId()));
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
