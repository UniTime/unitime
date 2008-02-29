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
 * This is an object that contains data related to the SETTINGS table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SETTINGS"
 */

public abstract class BaseSettings  implements Serializable {

	public static String REF = "Settings";
	public static String PROP_KEY = "key";
	public static String PROP_DEFAULT_VALUE = "defaultValue";
	public static String PROP_ALLOWED_VALUES = "allowedValues";
	public static String PROP_DESCRIPTION = "description";


	// constructors
	public BaseSettings () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseSettings (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseSettings (
		java.lang.Long uniqueId,
		java.lang.String key,
		java.lang.String defaultValue,
		java.lang.String allowedValues,
		java.lang.String description) {

		this.setUniqueId(uniqueId);
		this.setKey(key);
		this.setDefaultValue(defaultValue);
		this.setAllowedValues(allowedValues);
		this.setDescription(description);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String key;
	private java.lang.String defaultValue;
	private java.lang.String allowedValues;
	private java.lang.String description;

	// collections
	private java.util.Set managerSettings;



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
	 * Return the value associated with the column: KEY
	 */
	public java.lang.String getKey () {
		return key;
	}

	/**
	 * Set the value related to the column: KEY
	 * @param key the KEY value
	 */
	public void setKey (java.lang.String key) {
		this.key = key;
	}



	/**
	 * Return the value associated with the column: DEFAULT_VALUE
	 */
	public java.lang.String getDefaultValue () {
		return defaultValue;
	}

	/**
	 * Set the value related to the column: DEFAULT_VALUE
	 * @param defaultValue the DEFAULT_VALUE value
	 */
	public void setDefaultValue (java.lang.String defaultValue) {
		this.defaultValue = defaultValue;
	}



	/**
	 * Return the value associated with the column: ALLOWED_VALUES
	 */
	public java.lang.String getAllowedValues () {
		return allowedValues;
	}

	/**
	 * Set the value related to the column: ALLOWED_VALUES
	 * @param allowedValues the ALLOWED_VALUES value
	 */
	public void setAllowedValues (java.lang.String allowedValues) {
		this.allowedValues = allowedValues;
	}



	/**
	 * Return the value associated with the column: DESCRIPTION
	 */
	public java.lang.String getDescription () {
		return description;
	}

	/**
	 * Set the value related to the column: DESCRIPTION
	 * @param description the DESCRIPTION value
	 */
	public void setDescription (java.lang.String description) {
		this.description = description;
	}



	/**
	 * Return the value associated with the column: managerSettings
	 */
	public java.util.Set getManagerSettings () {
		return managerSettings;
	}

	/**
	 * Set the value related to the column: managerSettings
	 * @param managerSettings the managerSettings value
	 */
	public void setManagerSettings (java.util.Set managerSettings) {
		this.managerSettings = managerSettings;
	}

	public void addTomanagerSettings (org.unitime.timetable.model.ManagerSettings managerSettings) {
		if (null == getManagerSettings()) setManagerSettings(new java.util.HashSet());
		getManagerSettings().add(managerSettings);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Settings)) return false;
		else {
			org.unitime.timetable.model.Settings settings = (org.unitime.timetable.model.Settings) obj;
			if (null == this.getUniqueId() || null == settings.getUniqueId()) return false;
			else return (this.getUniqueId().equals(settings.getUniqueId()));
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