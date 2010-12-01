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
 * This is an object that contains data related to the DATE_PATTERN table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="DATE_PATTERN"
 */

public abstract class BaseDatePattern  implements Serializable {

	public static String REF = "DatePattern";
	public static String PROP_NAME = "name";
	public static String PROP_PATTERN = "pattern";
	public static String PROP_OFFSET = "offset";
	public static String PROP_TYPE = "type";
	public static String PROP_VISIBLE = "visible";


	// constructors
	public BaseDatePattern () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseDatePattern (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseDatePattern (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.String pattern,
		java.lang.Integer offset) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setPattern(pattern);
		this.setOffset(offset);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String name;
	private java.lang.String pattern;
	private java.lang.Integer offset;
	private java.lang.Integer type;
	private java.lang.Boolean visible;

	// many to one
	private org.unitime.timetable.model.Session session;

	// collections
	private java.util.Set departments;



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



	/**
	 * Return the value associated with the column: PATTERN
	 */
	public java.lang.String getPattern () {
		return pattern;
	}

	/**
	 * Set the value related to the column: PATTERN
	 * @param pattern the PATTERN value
	 */
	public void setPattern (java.lang.String pattern) {
		this.pattern = pattern;
	}



	/**
	 * Return the value associated with the column: OFFSET
	 */
	public java.lang.Integer getOffset () {
		return offset;
	}

	/**
	 * Set the value related to the column: OFFSET
	 * @param offset the OFFSET value
	 */
	public void setOffset (java.lang.Integer offset) {
		this.offset = offset;
	}



	/**
	 * Return the value associated with the column: TYPE
	 */
	public java.lang.Integer getType () {
		return type;
	}

	/**
	 * Set the value related to the column: TYPE
	 * @param type the TYPE value
	 */
	public void setType (java.lang.Integer type) {
		this.type = type;
	}



	/**
	 * Return the value associated with the column: VISIBLE
	 */
	public java.lang.Boolean isVisible () {
		return visible;
	}

	/**
	 * Set the value related to the column: VISIBLE
	 * @param visible the VISIBLE value
	 */
	public void setVisible (java.lang.Boolean visible) {
		this.visible = visible;
	}



	/**
	 * Return the value associated with the column: SESSION_ID
	 */
	public org.unitime.timetable.model.Session getSession () {
		return session;
	}

	/**
	 * Set the value related to the column: SESSION_ID
	 * @param session the SESSION_ID value
	 */
	public void setSession (org.unitime.timetable.model.Session session) {
		this.session = session;
	}



	/**
	 * Return the value associated with the column: departments
	 */
	public java.util.Set getDepartments () {
		return departments;
	}

	/**
	 * Set the value related to the column: departments
	 * @param departments the departments value
	 */
	public void setDepartments (java.util.Set departments) {
		this.departments = departments;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.DatePattern)) return false;
		else {
			org.unitime.timetable.model.DatePattern datePattern = (org.unitime.timetable.model.DatePattern) obj;
			if (null == this.getUniqueId() || null == datePattern.getUniqueId()) return false;
			else return (this.getUniqueId().equals(datePattern.getUniqueId()));
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
