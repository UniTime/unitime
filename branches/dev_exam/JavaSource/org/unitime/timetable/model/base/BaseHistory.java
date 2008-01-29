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
 * This is an object that contains data related to the HISTORY table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="HISTORY"
 */

public abstract class BaseHistory  implements Serializable {

	public static String REF = "History";
	public static String PROP_OLD_VALUE = "oldValue";
	public static String PROP_NEW_VALUE = "newValue";
	public static String PROP_SESSION_ID = "sessionId";


	// constructors
	public BaseHistory () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseHistory (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseHistory (
		java.lang.Long uniqueId,
		java.lang.String oldValue,
		java.lang.String newValue,
		java.lang.Long sessionId) {

		this.setUniqueId(uniqueId);
		this.setOldValue(oldValue);
		this.setNewValue(newValue);
		this.setSessionId(sessionId);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String oldValue;
	private java.lang.String newValue;
	private java.lang.Long sessionId;

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
	 * Return the value associated with the column: OLD_VALUE
	 */
	public java.lang.String getOldValue () {
		return oldValue;
	}

	/**
	 * Set the value related to the column: OLD_VALUE
	 * @param oldValue the OLD_VALUE value
	 */
	public void setOldValue (java.lang.String oldValue) {
		this.oldValue = oldValue;
	}



	/**
	 * Return the value associated with the column: NEW_VALUE
	 */
	public java.lang.String getNewValue () {
		return newValue;
	}

	/**
	 * Set the value related to the column: NEW_VALUE
	 * @param newValue the NEW_VALUE value
	 */
	public void setNewValue (java.lang.String newValue) {
		this.newValue = newValue;
	}



	/**
	 * Return the value associated with the column: SESSION_ID
	 */
	public java.lang.Long getSessionId () {
		return sessionId;
	}

	/**
	 * Set the value related to the column: SESSION_ID
	 * @param sessionId the SESSION_ID value
	 */
	public void setSessionId (java.lang.Long sessionId) {
		this.sessionId = sessionId;
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
		if (!(obj instanceof org.unitime.timetable.model.History)) return false;
		else {
			org.unitime.timetable.model.History history = (org.unitime.timetable.model.History) obj;
			if (null == this.getUniqueId() || null == history.getUniqueId()) return false;
			else return (this.getUniqueId().equals(history.getUniqueId()));
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