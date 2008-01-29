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
 * This is an object that contains data related to the TIME_PATTERN_DAYS table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="TIME_PATTERN_DAYS"
 */

public abstract class BaseTimePatternDays  implements Serializable {

	public static String REF = "TimePatternDays";
	public static String PROP_DAY_CODE = "dayCode";


	// constructors
	public BaseTimePatternDays () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseTimePatternDays (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer dayCode;



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
	 * Return the value associated with the column: DAY_CODE
	 */
	public java.lang.Integer getDayCode () {
		return dayCode;
	}

	/**
	 * Set the value related to the column: DAY_CODE
	 * @param dayCode the DAY_CODE value
	 */
	public void setDayCode (java.lang.Integer dayCode) {
		this.dayCode = dayCode;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.TimePatternDays)) return false;
		else {
			org.unitime.timetable.model.TimePatternDays timePatternDays = (org.unitime.timetable.model.TimePatternDays) obj;
			if (null == this.getUniqueId() || null == timePatternDays.getUniqueId()) return false;
			else return (this.getUniqueId().equals(timePatternDays.getUniqueId()));
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