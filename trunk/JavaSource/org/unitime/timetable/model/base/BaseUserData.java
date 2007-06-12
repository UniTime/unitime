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
 * This is an object that contains data related to the USER_DATA table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="USER_DATA"
 */

public abstract class BaseUserData  implements Serializable {

	public static String REF = "UserData";
	public static String PROP_VALUE = "value";


	// constructors
	public BaseUserData () {
		initialize();
	}

	/**
	 * Constructor for primary name
	 */
	public BaseUserData (
		java.lang.String externalUniqueId,
		java.lang.String name) {

		this.setExternalUniqueId(externalUniqueId);
		this.setName(name);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseUserData (
		java.lang.String externalUniqueId,
		java.lang.String name,
		java.lang.String value) {

		this.setExternalUniqueId(externalUniqueId);
		this.setName(name);
		this.setValue(value);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary name

	private java.lang.String externalUniqueId;

	private java.lang.String name;

	// fields
	private java.lang.String value;



	/**
     * @hibernate.property
     *  column=EXTERNAL_UID
	 * not-null=true
	 */
	public java.lang.String getExternalUniqueId () {
		return this.externalUniqueId;
	}

	/**
	 * Set the value related to the column: EXTERNAL_UID
	 * @param externalUniqueId the EXTERNAL_UID value
	 */
	public void setExternalUniqueId (java.lang.String externalUniqueId) {
		this.externalUniqueId = externalUniqueId;
		this.hashCode = Integer.MIN_VALUE;
	}

	/**
     * @hibernate.property
     *  column=NAME
	 * not-null=true
	 */
	public java.lang.String getName () {
		return this.name;
	}

	/**
	 * Set the value related to the column: NAME
	 * @param name the NAME value
	 */
	public void setName (java.lang.String name) {
		this.name = name;
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





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.UserData)) return false;
		else {
			org.unitime.timetable.model.UserData userData = (org.unitime.timetable.model.UserData) obj;
			if (null != this.getExternalUniqueId() && null != userData.getExternalUniqueId()) {
				if (!this.getExternalUniqueId().equals(userData.getExternalUniqueId())) {
					return false;
				}
			}
			else {
				return false;
			}
			if (null != this.getName() && null != userData.getName()) {
				if (!this.getName().equals(userData.getName())) {
					return false;
				}
			}
			else {
				return false;
			}
			return true;
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			StringBuffer sb = new StringBuffer();
			if (null != this.getExternalUniqueId()) {
				sb.append(this.getExternalUniqueId().hashCode());
				sb.append(":");
			}
			else {
				return super.hashCode();
			}
			if (null != this.getName()) {
				sb.append(this.getName().hashCode());
				sb.append(":");
			}
			else {
				return super.hashCode();
			}
			this.hashCode = sb.toString().hashCode();
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}