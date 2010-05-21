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
 * This is an object that contains data related to the EXTERNAL_ROOM_DEPARTMENT table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="EXTERNAL_ROOM_DEPARTMENT"
 */

public abstract class BaseExternalRoomDepartment  implements Serializable {

	public static String REF = "ExternalRoomDepartment";
	public static String PROP_DEPARTMENT_CODE = "departmentCode";
	public static String PROP_PERCENT = "percent";
	public static String PROP_ASSIGNMENT_TYPE = "assignmentType";


	// constructors
	public BaseExternalRoomDepartment () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExternalRoomDepartment (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExternalRoomDepartment (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.ExternalRoom room,
		java.lang.String departmentCode,
		java.lang.Integer percent,
		java.lang.String assignmentType) {

		this.setUniqueId(uniqueId);
		this.setRoom(room);
		this.setDepartmentCode(departmentCode);
		this.setPercent(percent);
		this.setAssignmentType(assignmentType);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String departmentCode;
	private java.lang.Integer percent;
	private java.lang.String assignmentType;

	// many to one
	private org.unitime.timetable.model.ExternalRoom room;



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
	 * Return the value associated with the column: DEPARTMENT_CODE
	 */
	public java.lang.String getDepartmentCode () {
		return departmentCode;
	}

	/**
	 * Set the value related to the column: DEPARTMENT_CODE
	 * @param departmentCode the DEPARTMENT_CODE value
	 */
	public void setDepartmentCode (java.lang.String departmentCode) {
		this.departmentCode = departmentCode;
	}



	/**
	 * Return the value associated with the column: PERCENT
	 */
	public java.lang.Integer getPercent () {
		return percent;
	}

	/**
	 * Set the value related to the column: PERCENT
	 * @param percent the PERCENT value
	 */
	public void setPercent (java.lang.Integer percent) {
		this.percent = percent;
	}



	/**
	 * Return the value associated with the column: ASSIGNMENT_TYPE
	 */
	public java.lang.String getAssignmentType () {
		return assignmentType;
	}

	/**
	 * Set the value related to the column: ASSIGNMENT_TYPE
	 * @param assignmentType the ASSIGNMENT_TYPE value
	 */
	public void setAssignmentType (java.lang.String assignmentType) {
		this.assignmentType = assignmentType;
	}



	/**
	 * Return the value associated with the column: EXTERNAL_ROOM_ID
	 */
	public org.unitime.timetable.model.ExternalRoom getRoom () {
		return room;
	}

	/**
	 * Set the value related to the column: EXTERNAL_ROOM_ID
	 * @param room the EXTERNAL_ROOM_ID value
	 */
	public void setRoom (org.unitime.timetable.model.ExternalRoom room) {
		this.room = room;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ExternalRoomDepartment)) return false;
		else {
			org.unitime.timetable.model.ExternalRoomDepartment externalRoomDepartment = (org.unitime.timetable.model.ExternalRoomDepartment) obj;
			if (null == this.getUniqueId() || null == externalRoomDepartment.getUniqueId()) return false;
			else return (this.getUniqueId().equals(externalRoomDepartment.getUniqueId()));
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
