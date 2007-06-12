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
 * This is an object that contains data related to the ROOM_GROUP table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="ROOM_GROUP"
 */

public abstract class BaseRoomGroup  implements Serializable {

	public static String REF = "RoomGroup";
	public static String PROP_NAME = "name";
	public static String PROP_DESCRIPTION = "description";
	public static String PROP_GLOBAL = "global";
	public static String PROP_DEFAULT_GROUP = "defaultGroup";


	// constructors
	public BaseRoomGroup () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseRoomGroup (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseRoomGroup (
		java.lang.Long uniqueId,
		java.lang.String name,
		java.lang.Boolean global,
		java.lang.Boolean defaultGroup) {

		this.setUniqueId(uniqueId);
		this.setName(name);
		this.setGlobal(global);
		this.setDefaultGroup(defaultGroup);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String name;
	private java.lang.String description;
	private java.lang.Boolean global;
	private java.lang.Boolean defaultGroup;

	// many to one
	private org.unitime.timetable.model.Department department;
	private org.unitime.timetable.model.Session session;

	// collections
	private java.util.Set rooms;



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
	 * Return the value associated with the column: GLOBAL
	 */
	public java.lang.Boolean isGlobal () {
		return global;
	}

	/**
	 * Set the value related to the column: GLOBAL
	 * @param global the GLOBAL value
	 */
	public void setGlobal (java.lang.Boolean global) {
		this.global = global;
	}



	/**
	 * Return the value associated with the column: DEFAULT_GROUP
	 */
	public java.lang.Boolean isDefaultGroup () {
		return defaultGroup;
	}

	/**
	 * Set the value related to the column: DEFAULT_GROUP
	 * @param defaultGroup the DEFAULT_GROUP value
	 */
	public void setDefaultGroup (java.lang.Boolean defaultGroup) {
		this.defaultGroup = defaultGroup;
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
	 * Return the value associated with the column: rooms
	 */
	public java.util.Set getRooms () {
		return rooms;
	}

	/**
	 * Set the value related to the column: rooms
	 * @param rooms the rooms value
	 */
	public void setRooms (java.util.Set rooms) {
		this.rooms = rooms;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.RoomGroup)) return false;
		else {
			org.unitime.timetable.model.RoomGroup roomGroup = (org.unitime.timetable.model.RoomGroup) obj;
			if (null == this.getUniqueId() || null == roomGroup.getUniqueId()) return false;
			else return (this.getUniqueId().equals(roomGroup.getUniqueId()));
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