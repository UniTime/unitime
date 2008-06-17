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
 * This is an object that contains data related to the DISTRIBUTION_OBJECT table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="DISTRIBUTION_OBJECT"
 */

public abstract class BaseDistributionObject  implements Serializable {

	public static String REF = "DistributionObject";
	public static String PROP_SEQUENCE_NUMBER = "sequenceNumber";


	// constructors
	public BaseDistributionObject () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseDistributionObject (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseDistributionObject (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.DistributionPref distributionPref,
		org.unitime.timetable.model.PreferenceGroup prefGroup) {

		this.setUniqueId(uniqueId);
		this.setDistributionPref(distributionPref);
		this.setPrefGroup(prefGroup);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer sequenceNumber;

	// many to one
	private org.unitime.timetable.model.DistributionPref distributionPref;
	private org.unitime.timetable.model.PreferenceGroup prefGroup;



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
	 * Return the value associated with the column: SEQUENCE_NUMBER
	 */
	public java.lang.Integer getSequenceNumber () {
		return sequenceNumber;
	}

	/**
	 * Set the value related to the column: SEQUENCE_NUMBER
	 * @param sequenceNumber the SEQUENCE_NUMBER value
	 */
	public void setSequenceNumber (java.lang.Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}



	/**
	 * Return the value associated with the column: DIST_PREF_ID
	 */
	public org.unitime.timetable.model.DistributionPref getDistributionPref () {
		return distributionPref;
	}

	/**
	 * Set the value related to the column: DIST_PREF_ID
	 * @param distributionPref the DIST_PREF_ID value
	 */
	public void setDistributionPref (org.unitime.timetable.model.DistributionPref distributionPref) {
		this.distributionPref = distributionPref;
	}



	/**
	 * Return the value associated with the column: PREF_GROUP_ID
	 */
	public org.unitime.timetable.model.PreferenceGroup getPrefGroup () {
		return prefGroup;
	}

	/**
	 * Set the value related to the column: PREF_GROUP_ID
	 * @param prefGroup the PREF_GROUP_ID value
	 */
	public void setPrefGroup (org.unitime.timetable.model.PreferenceGroup prefGroup) {
		this.prefGroup = prefGroup;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.DistributionObject)) return false;
		else {
			org.unitime.timetable.model.DistributionObject distributionObject = (org.unitime.timetable.model.DistributionObject) obj;
			if (null == this.getUniqueId() || null == distributionObject.getUniqueId()) return false;
			else return (this.getUniqueId().equals(distributionObject.getUniqueId()));
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
