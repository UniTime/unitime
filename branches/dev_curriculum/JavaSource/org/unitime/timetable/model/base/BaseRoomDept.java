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
 * This is an object that contains data related to the ROOM_DEPT table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="ROOM_DEPT"
 */

public abstract class BaseRoomDept  implements Serializable {

	public static String REF = "RoomDept";
	public static String PROP_CONTROL = "control";


	// constructors
	public BaseRoomDept () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseRoomDept (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseRoomDept (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Location room,
		org.unitime.timetable.model.Department department,
		java.lang.Boolean control) {

		this.setUniqueId(uniqueId);
		this.setRoom(room);
		this.setDepartment(department);
		this.setControl(control);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Boolean control;

	// many to one
	private org.unitime.timetable.model.Location room;
	private org.unitime.timetable.model.Department department;



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
	 * Return the value associated with the column: IS_CONTROL
	 */
	public java.lang.Boolean isControl () {
		return control;
	}

	/**
	 * Set the value related to the column: IS_CONTROL
	 * @param control the IS_CONTROL value
	 */
	public void setControl (java.lang.Boolean control) {
		this.control = control;
	}



	/**
	 * Return the value associated with the column: ROOM_ID
	 */
	public org.unitime.timetable.model.Location getRoom () {
		return room;
	}

	/**
	 * Set the value related to the column: ROOM_ID
	 * @param room the ROOM_ID value
	 */
	public void setRoom (org.unitime.timetable.model.Location room) {
		this.room = room;
	}



	/**
	 * Return the value associated with the column: DEPARTMENT_ID
	 */
	public org.unitime.timetable.model.Department getDepartment () {
		return department;
	}

	/**
	 * Set the value related to the column: DEPARTMENT_ID
	 * @param department the DEPARTMENT_ID value
	 */
	public void setDepartment (org.unitime.timetable.model.Department department) {
		this.department = department;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.RoomDept)) return false;
		else {
			org.unitime.timetable.model.RoomDept roomDept = (org.unitime.timetable.model.RoomDept) obj;
			if (null == this.getUniqueId() || null == roomDept.getUniqueId()) return false;
			else return (this.getUniqueId().equals(roomDept.getUniqueId()));
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
