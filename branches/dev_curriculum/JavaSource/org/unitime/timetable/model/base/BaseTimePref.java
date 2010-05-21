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
 * This is an object that contains data related to the TIME_PREF table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="TIME_PREF"
 */

public abstract class BaseTimePref extends org.unitime.timetable.model.Preference  implements Serializable {

	public static String REF = "TimePref";
	public static String PROP_PREFERENCE = "preference";


	// constructors
	public BaseTimePref () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseTimePref (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseTimePref (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.PreferenceGroup owner,
		org.unitime.timetable.model.PreferenceLevel prefLevel) {

		super (
			uniqueId,
			owner,
			prefLevel);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.String preference;

	// many to one
	private org.unitime.timetable.model.TimePattern timePattern;






	/**
	 * Return the value associated with the column: PREFERENCE
	 */
	public java.lang.String getPreference () {
		return preference;
	}

	/**
	 * Set the value related to the column: PREFERENCE
	 * @param preference the PREFERENCE value
	 */
	public void setPreference (java.lang.String preference) {
		this.preference = preference;
	}



	/**
	 * Return the value associated with the column: TIME_PATTERN_ID
	 */
	public org.unitime.timetable.model.TimePattern getTimePattern () {
		return timePattern;
	}

	/**
	 * Set the value related to the column: TIME_PATTERN_ID
	 * @param timePattern the TIME_PATTERN_ID value
	 */
	public void setTimePattern (org.unitime.timetable.model.TimePattern timePattern) {
		this.timePattern = timePattern;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.TimePref)) return false;
		else {
			org.unitime.timetable.model.TimePref timePref = (org.unitime.timetable.model.TimePref) obj;
			if (null == this.getUniqueId() || null == timePref.getUniqueId()) return false;
			else return (this.getUniqueId().equals(timePref.getUniqueId()));
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
