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
 * This is an object that contains data related to the BUILDING_PREF table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="BUILDING_PREF"
 */

public abstract class BaseBuildingPref extends org.unitime.timetable.model.Preference  implements Serializable {

	public static String REF = "BuildingPref";
	public static String PROP_DISTANCE_FROM = "distanceFrom";


	// constructors
	public BaseBuildingPref () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseBuildingPref (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseBuildingPref (
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
	private java.lang.Integer distanceFrom;

	// many to one
	private org.unitime.timetable.model.Building building;






	/**
	 * Return the value associated with the column: DISTANCE_FROM
	 */
	public java.lang.Integer getDistanceFrom () {
		return distanceFrom;
	}

	/**
	 * Set the value related to the column: DISTANCE_FROM
	 * @param distanceFrom the DISTANCE_FROM value
	 */
	public void setDistanceFrom (java.lang.Integer distanceFrom) {
		this.distanceFrom = distanceFrom;
	}



	/**
	 * Return the value associated with the column: BLDG_ID
	 */
	public org.unitime.timetable.model.Building getBuilding () {
		return building;
	}

	/**
	 * Set the value related to the column: BLDG_ID
	 * @param building the BLDG_ID value
	 */
	public void setBuilding (org.unitime.timetable.model.Building building) {
		this.building = building;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.BuildingPref)) return false;
		else {
			org.unitime.timetable.model.BuildingPref buildingPref = (org.unitime.timetable.model.BuildingPref) obj;
			if (null == this.getUniqueId() || null == buildingPref.getUniqueId()) return false;
			else return (this.getUniqueId().equals(buildingPref.getUniqueId()));
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
