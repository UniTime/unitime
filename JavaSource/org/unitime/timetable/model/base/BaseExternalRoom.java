/*
 * UniTime 3.1 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2008, UniTime.org, and individual contributors
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
 * This is an object that contains data related to the EXTERNAL_ROOM table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="EXTERNAL_ROOM"
 */

public abstract class BaseExternalRoom  implements Serializable {

	public static String REF = "ExternalRoom";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_ROOM_NUMBER = "roomNumber";
	public static String PROP_COORDINATE_X = "coordinateX";
	public static String PROP_COORDINATE_Y = "coordinateY";
	public static String PROP_CAPACITY = "capacity";
	public static String PROP_EXAM_CAPACITY = "examCapacity";
	public static String PROP_CLASSIFICATION = "classification";
	public static String PROP_SCHEDULED_ROOM_TYPE = "scheduledRoomType";
	public static String PROP_IS_INSTRUCTIONAL = "isInstructional";
	public static String PROP_DISPLAY_NAME = "displayName";


	// constructors
	public BaseExternalRoom () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExternalRoom (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExternalRoom (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.ExternalBuilding building,
		java.lang.String roomNumber,
		java.lang.Integer capacity,
		java.lang.String classification,
		java.lang.String scheduledRoomType,
		java.lang.Boolean isInstructional) {

		this.setUniqueId(uniqueId);
		this.setBuilding(building);
		this.setRoomNumber(roomNumber);
		this.setCapacity(capacity);
		this.setClassification(classification);
		this.setScheduledRoomType(scheduledRoomType);
		this.setIsInstructional(isInstructional);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String externalUniqueId;
	private java.lang.String roomNumber;
	private java.lang.Integer coordinateX;
	private java.lang.Integer coordinateY;
	private java.lang.Integer capacity;
	private java.lang.Integer examCapacity;
	private java.lang.String classification;
	private java.lang.String scheduledRoomType;
	private java.lang.Boolean isInstructional;
	private java.lang.String displayName;

	// many to one
	private org.unitime.timetable.model.ExternalBuilding building;

	// collections
	private java.util.Set roomDepartments;
	private java.util.Set roomFeatures;



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
	 * Return the value associated with the column: COORDINATE_X
	 */
	public java.lang.Integer getCoordinateX () {
		return coordinateX;
	}

	/**
	 * Set the value related to the column: COORDINATE_X
	 * @param coordinateX the COORDINATE_X value
	 */
	public void setCoordinateX (java.lang.Integer coordinateX) {
		this.coordinateX = coordinateX;
	}



	/**
	 * Return the value associated with the column: COORDINATE_Y
	 */
	public java.lang.Integer getCoordinateY () {
		return coordinateY;
	}

	/**
	 * Set the value related to the column: COORDINATE_Y
	 * @param coordinateY the COORDINATE_Y value
	 */
	public void setCoordinateY (java.lang.Integer coordinateY) {
		this.coordinateY = coordinateY;
	}



	/**
	 * Return the value associated with the column: CAPACITY
	 */
	public java.lang.Integer getCapacity () {
		return capacity;
	}

	/**
	 * Set the value related to the column: CAPACITY
	 * @param capacity the CAPACITY value
	 */
	public void setCapacity (java.lang.Integer capacity) {
		this.capacity = capacity;
	}



	/**
	 * Return the value associated with the column: EXAM_CAPACITY
	 */
	public java.lang.Integer getExamCapacity () {
		return examCapacity;
	}

	/**
	 * Set the value related to the column: EXAM_CAPACITY
	 * @param examCapacity the EXAM_CAPACITY value
	 */
	public void setExamCapacity (java.lang.Integer examCapacity) {
		this.examCapacity = examCapacity;
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
	 * Return the value associated with the column: INSTRUCTIONAL
	 */
	public java.lang.Boolean isIsInstructional () {
		return isInstructional;
	}

	/**
	 * Set the value related to the column: INSTRUCTIONAL
	 * @param isInstructional the INSTRUCTIONAL value
	 */
	public void setIsInstructional (java.lang.Boolean isInstructional) {
		this.isInstructional = isInstructional;
	}



	/**
	 * Return the value associated with the column: DISPLAY_NAME
	 */
	public java.lang.String getDisplayName () {
		return displayName;
	}

	/**
	 * Set the value related to the column: DISPLAY_NAME
	 * @param displayName the DISPLAY_NAME value
	 */
	public void setDisplayName (java.lang.String displayName) {
		this.displayName = displayName;
	}



	/**
	 * Return the value associated with the column: EXTERNAL_BLDG_ID
	 */
	public org.unitime.timetable.model.ExternalBuilding getBuilding () {
		return building;
	}

	/**
	 * Set the value related to the column: EXTERNAL_BLDG_ID
	 * @param building the EXTERNAL_BLDG_ID value
	 */
	public void setBuilding (org.unitime.timetable.model.ExternalBuilding building) {
		this.building = building;
	}



	/**
	 * Return the value associated with the column: roomDepartments
	 */
	public java.util.Set getRoomDepartments () {
		return roomDepartments;
	}

	/**
	 * Set the value related to the column: roomDepartments
	 * @param roomDepartments the roomDepartments value
	 */
	public void setRoomDepartments (java.util.Set roomDepartments) {
		this.roomDepartments = roomDepartments;
	}

	public void addToroomDepartments (org.unitime.timetable.model.ExternalRoomDepartment externalRoomDepartment) {
		if (null == getRoomDepartments()) setRoomDepartments(new java.util.HashSet());
		getRoomDepartments().add(externalRoomDepartment);
	}



	/**
	 * Return the value associated with the column: roomFeatures
	 */
	public java.util.Set getRoomFeatures () {
		return roomFeatures;
	}

	/**
	 * Set the value related to the column: roomFeatures
	 * @param roomFeatures the roomFeatures value
	 */
	public void setRoomFeatures (java.util.Set roomFeatures) {
		this.roomFeatures = roomFeatures;
	}

	public void addToroomFeatures (org.unitime.timetable.model.ExternalRoomFeature externalRoomFeature) {
		if (null == getRoomFeatures()) setRoomFeatures(new java.util.HashSet());
		getRoomFeatures().add(externalRoomFeature);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ExternalRoom)) return false;
		else {
			org.unitime.timetable.model.ExternalRoom externalRoom = (org.unitime.timetable.model.ExternalRoom) obj;
			if (null == this.getUniqueId() || null == externalRoom.getUniqueId()) return false;
			else return (this.getUniqueId().equals(externalRoom.getUniqueId()));
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