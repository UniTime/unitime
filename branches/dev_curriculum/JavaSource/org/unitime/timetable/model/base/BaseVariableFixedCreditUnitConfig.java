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

public abstract class BaseVariableFixedCreditUnitConfig extends org.unitime.timetable.model.CourseCreditUnitConfig  implements Serializable {

	public static String REF = "VariableFixedCreditUnitConfig";
	public static String PROP_MIN_UNITS = "minUnits";
	public static String PROP_MAX_UNITS = "maxUnits";


	// constructors
	public BaseVariableFixedCreditUnitConfig () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseVariableFixedCreditUnitConfig (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseVariableFixedCreditUnitConfig (
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
	private java.lang.Float minUnits;
	private java.lang.Float maxUnits;






	/**
	 * Return the value associated with the column: MIN_UNITS
	 */
	public java.lang.Float getMinUnits () {
		return minUnits;
	}

	/**
	 * Set the value related to the column: MIN_UNITS
	 * @param minUnits the MIN_UNITS value
	 */
	public void setMinUnits (java.lang.Float minUnits) {
		this.minUnits = minUnits;
	}



	/**
	 * Return the value associated with the column: MAX_UNITS
	 */
	public java.lang.Float getMaxUnits () {
		return maxUnits;
	}

	/**
	 * Set the value related to the column: MAX_UNITS
	 * @param maxUnits the MAX_UNITS value
	 */
	public void setMaxUnits (java.lang.Float maxUnits) {
		this.maxUnits = maxUnits;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.VariableFixedCreditUnitConfig)) return false;
		else {
			org.unitime.timetable.model.VariableFixedCreditUnitConfig variableFixedCreditUnitConfig = (org.unitime.timetable.model.VariableFixedCreditUnitConfig) obj;
			if (null == this.getUniqueId() || null == variableFixedCreditUnitConfig.getUniqueId()) return false;
			else return (this.getUniqueId().equals(variableFixedCreditUnitConfig.getUniqueId()));
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
