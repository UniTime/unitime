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
 * This is an object that contains data related to the STUDENT_GROUP table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="STUDENT_GROUP"
 */

public abstract class BaseStudentGroup  implements Serializable {

	public static String REF = "StudentGroup";
	public static String PROP_SESSION_ID = "sessionId";
	public static String PROP_GROUP_ABBREVIATION = "groupAbbreviation";
	public static String PROP_GROUP_NAME = "groupName";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";


	// constructors
	public BaseStudentGroup () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseStudentGroup (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseStudentGroup (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.Long sessionId,
		java.lang.String groupAbbreviation,
		java.lang.String groupName) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setSessionId(sessionId);
		this.setGroupAbbreviation(groupAbbreviation);
		this.setGroupName(groupName);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Long sessionId;
	private java.lang.String groupAbbreviation;
	private java.lang.String groupName;
	private java.lang.String externalUniqueId;

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
	 * Return the value associated with the column: GROUP_ABBREVIATION
	 */
	public java.lang.String getGroupAbbreviation () {
		return groupAbbreviation;
	}

	/**
	 * Set the value related to the column: GROUP_ABBREVIATION
	 * @param groupAbbreviation the GROUP_ABBREVIATION value
	 */
	public void setGroupAbbreviation (java.lang.String groupAbbreviation) {
		this.groupAbbreviation = groupAbbreviation;
	}



	/**
	 * Return the value associated with the column: GROUP_NAME
	 */
	public java.lang.String getGroupName () {
		return groupName;
	}

	/**
	 * Set the value related to the column: GROUP_NAME
	 * @param groupName the GROUP_NAME value
	 */
	public void setGroupName (java.lang.String groupName) {
		this.groupName = groupName;
	}



	/**
	 * Return the value associated with the column: EXTERNAL_UID
	 */
	public java.lang.String getExternalUniqueId () {
		return externalUniqueId;
	}

	/**
	 * Set the value related to the column: EXTERNAL_UID
	 * @param externalUniqueId the EXTERNAL_UID value
	 */
	public void setExternalUniqueId (java.lang.String externalUniqueId) {
		this.externalUniqueId = externalUniqueId;
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
		if (!(obj instanceof org.unitime.timetable.model.StudentGroup)) return false;
		else {
			org.unitime.timetable.model.StudentGroup studentGroup = (org.unitime.timetable.model.StudentGroup) obj;
			if (null == this.getUniqueId() || null == studentGroup.getUniqueId()) return false;
			else return (this.getUniqueId().equals(studentGroup.getUniqueId()));
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
