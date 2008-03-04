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
 * This is an object that contains data related to the NON_UNIVERSITY_LOCATION table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="NON_UNIVERSITY_LOCATION"
 */

public abstract class BaseNonUniversityLocation extends org.unitime.timetable.model.Location  implements Serializable {

	public static String REF = "NonUniversityLocation";
	public static String PROP_NAME = "name";


	// constructors
	public BaseNonUniversityLocation () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseNonUniversityLocation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseNonUniversityLocation (
		java.lang.Long uniqueId,
		java.lang.Long permanentId,
		java.lang.Integer capacity,
		java.lang.Integer coordinateX,
		java.lang.Integer coordinateY,
		java.lang.Boolean ignoreTooFar,
		java.lang.Boolean ignoreRoomCheck) {

		super (
			uniqueId,
			permanentId,
			capacity,
			coordinateX,
			coordinateY,
			ignoreTooFar,
			ignoreRoomCheck);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.String name;






	/**
	 * Return the value associated with the column: NAME
	 */
	public java.lang.String getName () {
		return name;
	}

	/**
	 * Set the value related to the column: NAME
	 * @param name the NAME value
	 */
	public void setName (java.lang.String name) {
		this.name = name;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.NonUniversityLocation)) return false;
		else {
			org.unitime.timetable.model.NonUniversityLocation nonUniversityLocation = (org.unitime.timetable.model.NonUniversityLocation) obj;
			if (null == this.getUniqueId() || null == nonUniversityLocation.getUniqueId()) return false;
			else return (this.getUniqueId().equals(nonUniversityLocation.getUniqueId()));
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