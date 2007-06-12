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
 * This is an object that contains data related to the COURSE_CREDIT_UNIT_CONFIG table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="COURSE_CREDIT_UNIT_CONFIG"
 */

public abstract class BaseFixedCreditUnitConfig extends org.unitime.timetable.model.CourseCreditUnitConfig  implements Serializable {

	public static String REF = "FixedCreditUnitConfig";
	public static String PROP_FIXED_UNITS = "fixedUnits";


	// constructors
	public BaseFixedCreditUnitConfig () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseFixedCreditUnitConfig (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseFixedCreditUnitConfig (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CourseCreditType creditType,
		org.unitime.timetable.model.CourseCreditUnitType creditUnitType,
		java.lang.String creditFormat,
		java.lang.Boolean definesCreditAtCourseLevel) {

		super (
			uniqueId,
			creditType,
			creditUnitType,
			creditFormat,
			definesCreditAtCourseLevel);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.Float fixedUnits;






	/**
	 * Return the value associated with the column: FIXED_UNITS
	 */
	public java.lang.Float getFixedUnits () {
		return fixedUnits;
	}

	/**
	 * Set the value related to the column: FIXED_UNITS
	 * @param fixedUnits the FIXED_UNITS value
	 */
	public void setFixedUnits (java.lang.Float fixedUnits) {
		this.fixedUnits = fixedUnits;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.FixedCreditUnitConfig)) return false;
		else {
			org.unitime.timetable.model.FixedCreditUnitConfig fixedCreditUnitConfig = (org.unitime.timetable.model.FixedCreditUnitConfig) obj;
			if (null == this.getUniqueId() || null == fixedCreditUnitConfig.getUniqueId()) return false;
			else return (this.getUniqueId().equals(fixedCreditUnitConfig.getUniqueId()));
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