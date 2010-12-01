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
 * This is an object that contains data related to the APPLICATION_CONFIG table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="APPLICATION_CONFIG"
 */

public abstract class BaseApplicationConfig  implements Serializable {

	public static String REF = "ApplicationConfig";
	public static String PROP_VALUE = "value";
	public static String PROP_DESCRIPTION = "description";


	// constructors
	public BaseApplicationConfig () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseApplicationConfig (java.lang.String key) {
		this.setKey(key);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.String key;

	// fields
	private java.lang.String value;
	private java.lang.String description;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  column="KEY"
     */
	public java.lang.String getKey () {
		return key;
	}

	/**
	 * Set the unique identifier of this class
	 * @param key the new ID
	 */
	public void setKey (java.lang.String key) {
		this.key = key;
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





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ApplicationConfig)) return false;
		else {
			org.unitime.timetable.model.ApplicationConfig applicationConfig = (org.unitime.timetable.model.ApplicationConfig) obj;
			if (null == this.getKey() || null == applicationConfig.getKey()) return false;
			else return (this.getKey().equals(applicationConfig.getKey()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getKey()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getKey().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}
