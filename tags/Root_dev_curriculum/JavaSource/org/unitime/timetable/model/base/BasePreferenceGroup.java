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
 * This is an object that contains data related to the  table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table=""
 */

public abstract class BasePreferenceGroup  implements Serializable {

	public static String REF = "PreferenceGroup";


	// constructors
	public BasePreferenceGroup () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BasePreferenceGroup (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// collections
	private java.util.Set preferences;
	private java.util.Set distributionObjects;



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
	 * Return the value associated with the column: preferences
	 */
	public java.util.Set getPreferences () {
		return preferences;
	}

	/**
	 * Set the value related to the column: preferences
	 * @param preferences the preferences value
	 */
	public void setPreferences (java.util.Set preferences) {
		this.preferences = preferences;
	}

	public void addTopreferences (org.unitime.timetable.model.Preference preference) {
		if (null == getPreferences()) setPreferences(new java.util.HashSet());
		getPreferences().add(preference);
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
		if (!(obj instanceof org.unitime.timetable.model.PreferenceGroup)) return false;
		else {
			org.unitime.timetable.model.PreferenceGroup preferenceGroup = (org.unitime.timetable.model.PreferenceGroup) obj;
			if (null == this.getUniqueId() || null == preferenceGroup.getUniqueId()) return false;
			else return (this.getUniqueId().equals(preferenceGroup.getUniqueId()));
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
