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
 * This is an object that contains data related to the ROOM table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="ROOM"
 */

public abstract class BaseRoom extends org.unitime.timetable.model.Location  implements Serializable {

	public static String REF = "Room";
	public static String PROP_BUILDING_ABBV = "buildingAbbv";
	public static String PROP_ROOM_NUMBER = "roomNumber";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_SCHEDULED_ROOM_TYPE = "scheduledRoomType";
	public static String PROP_CLASSIFICATION = "classification";


	// constructors
	public BaseRoom () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseRoom (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseRoom (
		java.lang.Long uniqueId,
		java.lang.Long permanentId,
		java.lang.Integer capacity,
		java.lang.Integer coordinateX,
		java.lang.Integer coordinateY,
		java.lang.Boolean ignoreTooFar,
		java.lang.Boolean ignoreRoomCheck) {

		super (
			uniqueId,
			permanentId,
			capacity,
			coordinateX,
			coordinateY,
			ignoreTooFar,
			ignoreRoomCheck);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.String buildingAbbv;
	private java.lang.String roomNumber;
	private java.lang.String externalUniqueId;
	private java.lang.String scheduledRoomType;
	private java.lang.String classification;

	// many to one
	private org.unitime.timetable.model.Building building;






	/**
	 * Return the value associated with the column: buildingAbbv
	 */
	public java.lang.String getBuildingAbbv () {
		return buildingAbbv;
	}

	/**
	 * Set the value related to the column: buildingAbbv
	 * @param buildingAbbv the buildingAbbv value
	 */
	public void setBuildingAbbv (java.lang.String buildingAbbv) {
		this.buildingAbbv = buildingAbbv;
	}



	/**
	 * Return the value associated with the column: ROOM_NUMBER
	 */
	public java.lang.String getRoomNumber () {
		return roomNumber;
	}

	/**
	 * Set the value related to the column: ROOM_NUMBER
	 * @param roomNumber the ROOM_NUMBER value
	 */
	public void setRoomNumber (java.lang.String roomNumber) {
		this.roomNumber = roomNumber;
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
	 * Return the value associated with the column: SCHEDULED_ROOM_TYPE
	 */
	public java.lang.String getScheduledRoomType () {
		return scheduledRoomType;
	}

	/**
	 * Set the value related to the column: SCHEDULED_ROOM_TYPE
	 * @param scheduledRoomType the SCHEDULED_ROOM_TYPE value
	 */
	public void setScheduledRoomType (java.lang.String scheduledRoomType) {
		this.scheduledRoomType = scheduledRoomType;
	}



	/**
	 * Return the value associated with the column: CLASSIFICATION
	 */
	public java.lang.String getClassification () {
		return classification;
	}

	/**
	 * Set the value related to the column: CLASSIFICATION
	 * @param classification the CLASSIFICATION value
	 */
	public void setClassification (java.lang.String classification) {
		this.classification = classification;
	}



	/**
	 * Return the value associated with the column: BUILDING_ID
	 */
	public org.unitime.timetable.model.Building getBuilding () {
		return building;
	}

	/**
	 * Set the value related to the column: BUILDING_ID
	 * @param building the BUILDING_ID value
	 */
	public void setBuilding (org.unitime.timetable.model.Building building) {
		this.building = building;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Room)) return false;
		else {
			org.unitime.timetable.model.Room room = (org.unitime.timetable.model.Room) obj;
			if (null == this.getUniqueId() || null == room.getUniqueId()) return false;
			else return (this.getUniqueId().equals(room.getUniqueId()));
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