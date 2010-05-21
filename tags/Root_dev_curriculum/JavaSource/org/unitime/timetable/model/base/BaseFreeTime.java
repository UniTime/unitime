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
 * This is an object that contains data related to the FREE_TIME table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="FREE_TIME"
 */

public abstract class BaseFreeTime  implements Serializable {

	public static String REF = "FreeTime";
	public static String PROP_NAME = "name";
	public static String PROP_DAY_CODE = "dayCode";
	public static String PROP_START_SLOT = "startSlot";
	public static String PROP_LENGTH = "length";
	public static String PROP_CATEGORY = "category";


	// constructors
	public BaseFreeTime () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseFreeTime (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseFreeTime (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.String name,
		java.lang.Integer dayCode,
		java.lang.Integer startSlot,
		java.lang.Integer length,
		java.lang.Integer category) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setName(name);
		this.setDayCode(dayCode);
		this.setStartSlot(startSlot);
		this.setLength(length);
		this.setCategory(category);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String name;
	private java.lang.Integer dayCode;
	private java.lang.Integer startSlot;
	private java.lang.Integer length;
	private java.lang.Integer category;

	// many to one
	private org.unitime.timetable.model.Session session;



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
	 * Return the value associated with the column: DAY_CODE
	 */
	public java.lang.Integer getDayCode () {
		return dayCode;
	}

	/**
	 * Set the value related to the column: DAY_CODE
	 * @param dayCode the DAY_CODE value
	 */
	public void setDayCode (java.lang.Integer dayCode) {
		this.dayCode = dayCode;
	}



	/**
	 * Return the value associated with the column: START_SLOT
	 */
	public java.lang.Integer getStartSlot () {
		return startSlot;
	}

	/**
	 * Set the value related to the column: START_SLOT
	 * @param startSlot the START_SLOT value
	 */
	public void setStartSlot (java.lang.Integer startSlot) {
		this.startSlot = startSlot;
	}



	/**
	 * Return the value associated with the column: LENGTH
	 */
	public java.lang.Integer getLength () {
		return length;
	}

	/**
	 * Set the value related to the column: LENGTH
	 * @param length the LENGTH value
	 */
	public void setLength (java.lang.Integer length) {
		this.length = length;
	}



	/**
	 * Return the value associated with the column: CATEGORY
	 */
	public java.lang.Integer getCategory () {
		return category;
	}

	/**
	 * Set the value related to the column: CATEGORY
	 * @param category the CATEGORY value
	 */
	public void setCategory (java.lang.Integer category) {
		this.category = category;
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





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.FreeTime)) return false;
		else {
			org.unitime.timetable.model.FreeTime freeTime = (org.unitime.timetable.model.FreeTime) obj;
			if (null == this.getUniqueId() || null == freeTime.getUniqueId()) return false;
			else return (this.getUniqueId().equals(freeTime.getUniqueId()));
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
