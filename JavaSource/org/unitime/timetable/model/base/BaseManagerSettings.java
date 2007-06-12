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
 * This is an object that contains data related to the MANAGER_SETTINGS table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="MANAGER_SETTINGS"
 */

public abstract class BaseManagerSettings  implements Serializable {

	public static String REF = "ManagerSettings";
	public static String PROP_VALUE = "value";


	// constructors
	public BaseManagerSettings () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseManagerSettings (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseManagerSettings (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Settings key,
		org.unitime.timetable.model.TimetableManager manager,
		java.lang.String value) {

		this.setUniqueId(uniqueId);
		this.setKey(key);
		this.setManager(manager);
		this.setValue(value);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String value;

	// many to one
	private org.unitime.timetable.model.Settings key;
	private org.unitime.timetable.model.TimetableManager manager;



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
	 * Return the value associated with the column: VALUE
	 */
	public java.lang.String getValue () {
		return value;
	}

	/**
	 * Set the value related to the column: VALUE
	 * @param value the VALUE value
	 */
	public void setValue (java.lang.String value) {
		this.value = value;
	}



	/**
	 * Return the value associated with the column: KEY_ID
	 */
	public org.unitime.timetable.model.Settings getKey () {
		return key;
	}

	/**
	 * Set the value related to the column: KEY_ID
	 * @param key the KEY_ID value
	 */
	public void setKey (org.unitime.timetable.model.Settings key) {
		this.key = key;
	}



	/**
	 * Return the value associated with the column: USER_UNIQUEID
	 */
	public org.unitime.timetable.model.TimetableManager getManager () {
		return manager;
	}

	/**
	 * Set the value related to the column: USER_UNIQUEID
	 * @param manager the USER_UNIQUEID value
	 */
	public void setManager (org.unitime.timetable.model.TimetableManager manager) {
		this.manager = manager;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ManagerSettings)) return false;
		else {
			org.unitime.timetable.model.ManagerSettings managerSettings = (org.unitime.timetable.model.ManagerSettings) obj;
			if (null == this.getUniqueId() || null == managerSettings.getUniqueId()) return false;
			else return (this.getUniqueId().equals(managerSettings.getUniqueId()));
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