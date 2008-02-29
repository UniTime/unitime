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
 * This is an object that contains data related to the DISTRIBUTION_PREF table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="DISTRIBUTION_PREF"
 */

public abstract class BaseDistributionPref extends org.unitime.timetable.model.Preference  implements Serializable {

	public static String REF = "DistributionPref";
	public static String PROP_GROUPING = "grouping";
	public static String PROP_UNIQUE_ID_ROLLED_FORWARD_FROM = "uniqueIdRolledForwardFrom";


	// constructors
	public BaseDistributionPref () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseDistributionPref (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseDistributionPref (
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
	private java.lang.Integer grouping;
	private java.lang.Long uniqueIdRolledForwardFrom;

	// many to one
	private org.unitime.timetable.model.DistributionType distributionType;

	// collections
	private java.util.Set distributionObjects;






	/**
	 * Return the value associated with the column: GROUPING
	 */
	public java.lang.Integer getGrouping () {
		return grouping;
	}

	/**
	 * Set the value related to the column: GROUPING
	 * @param grouping the GROUPING value
	 */
	public void setGrouping (java.lang.Integer grouping) {
		this.grouping = grouping;
	}



	/**
	 * Return the value associated with the column: UID_ROLLED_FWD_FROM
	 */
	public java.lang.Long getUniqueIdRolledForwardFrom () {
		return uniqueIdRolledForwardFrom;
	}

	/**
	 * Set the value related to the column: UID_ROLLED_FWD_FROM
	 * @param uniqueIdRolledForwardFrom the UID_ROLLED_FWD_FROM value
	 */
	public void setUniqueIdRolledForwardFrom (java.lang.Long uniqueIdRolledForwardFrom) {
		this.uniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom;
	}



	/**
	 * Return the value associated with the column: DIST_TYPE_ID
	 */
	public org.unitime.timetable.model.DistributionType getDistributionType () {
		return distributionType;
	}

	/**
	 * Set the value related to the column: DIST_TYPE_ID
	 * @param distributionType the DIST_TYPE_ID value
	 */
	public void setDistributionType (org.unitime.timetable.model.DistributionType distributionType) {
		this.distributionType = distributionType;
	}



	/**
	 * Return the value associated with the column: distributionObjects
	 */
	public java.util.Set getDistributionObjects () {
		return distributionObjects;
	}

	/**
	 * Set the value related to the column: distributionObjects
	 * @param distributionObjects the distributionObjects value
	 */
	public void setDistributionObjects (java.util.Set distributionObjects) {
		this.distributionObjects = distributionObjects;
	}

	public void addTodistributionObjects (org.unitime.timetable.model.DistributionObject distributionObject) {
		if (null == getDistributionObjects()) setDistributionObjects(new java.util.HashSet());
		getDistributionObjects().add(distributionObject);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.DistributionPref)) return false;
		else {
			org.unitime.timetable.model.DistributionPref distributionPref = (org.unitime.timetable.model.DistributionPref) obj;
			if (null == this.getUniqueId() || null == distributionPref.getUniqueId()) return false;
			else return (this.getUniqueId().equals(distributionPref.getUniqueId()));
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