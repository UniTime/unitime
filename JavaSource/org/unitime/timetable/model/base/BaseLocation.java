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
 * This is an object that contains data related to the  table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table=""
 */

public abstract class BaseLocation  implements Serializable {

	public static String REF = "Location";
	public static String PROP_PERMANENT_ID = "permanentId";
	public static String PROP_CAPACITY = "capacity";
	public static String PROP_COORDINATE_X = "coordinateX";
	public static String PROP_COORDINATE_Y = "coordinateY";
	public static String PROP_IGNORE_TOO_FAR = "ignoreTooFar";
	public static String PROP_IGNORE_ROOM_CHECK = "ignoreRoomCheck";
	public static String PROP_MANAGER_IDS = "managerIds";
	public static String PROP_PATTERN = "pattern";
	public static String PROP_EXAM_TYPE = "examType";
	public static String PROP_EXAM_CAPACITY = "examCapacity";
	public static String PROP_DISPLAY_NAME = "displayName";


	// constructors
	public BaseLocation () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseLocation (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseLocation (
		java.lang.Long uniqueId,
		java.lang.Long permanentId,
		java.lang.Integer capacity,
		java.lang.Integer coordinateX,
		java.lang.Integer coordinateY,
		java.lang.Boolean ignoreTooFar,
		java.lang.Boolean ignoreRoomCheck) {

		this.setUniqueId(uniqueId);
		this.setPermanentId(permanentId);
		this.setCapacity(capacity);
		this.setCoordinateX(coordinateX);
		this.setCoordinateY(coordinateY);
		this.setIgnoreTooFar(ignoreTooFar);
		this.setIgnoreRoomCheck(ignoreRoomCheck);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Long permanentId;
	private java.lang.Integer capacity;
	private java.lang.Integer coordinateX;
	private java.lang.Integer coordinateY;
	private java.lang.Boolean ignoreTooFar;
	private java.lang.Boolean ignoreRoomCheck;
	private java.lang.String managerIds;
	private java.lang.String pattern;
	private java.lang.Integer examType;
	private java.lang.Integer examCapacity;
	private java.lang.String displayName;

	// many to one
	private org.unitime.timetable.model.Session session;

	// collections
	private java.util.Set features;
	private java.util.Set assignments;
	private java.util.Set roomGroups;
	private java.util.Set roomDepts;
	private java.util.Set examPreferences;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="org.unitime.commons.hibernate.id.UniqueIdGenerator"
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
	 * Return the value associated with the column: PERMANENT_ID
	 */
	public java.lang.Long getPermanentId () {
		return permanentId;
	}

	/**
	 * Set the value related to the column: PERMANENT_ID
	 * @param permanentId the PERMANENT_ID value
	 */
	public void setPermanentId (java.lang.Long permanentId) {
		this.permanentId = permanentId;
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
	 * Return the value associated with the column: IGNORE_TOO_FAR
	 */
	public java.lang.Boolean isIgnoreTooFar () {
		return ignoreTooFar;
	}

	/**
	 * Set the value related to the column: IGNORE_TOO_FAR
	 * @param ignoreTooFar the IGNORE_TOO_FAR value
	 */
	public void setIgnoreTooFar (java.lang.Boolean ignoreTooFar) {
		this.ignoreTooFar = ignoreTooFar;
	}



	/**
	 * Return the value associated with the column: IGNORE_ROOM_CHECK
	 */
	public java.lang.Boolean isIgnoreRoomCheck () {
		return ignoreRoomCheck;
	}

	/**
	 * Set the value related to the column: IGNORE_ROOM_CHECK
	 * @param ignoreRoomCheck the IGNORE_ROOM_CHECK value
	 */
	public void setIgnoreRoomCheck (java.lang.Boolean ignoreRoomCheck) {
		this.ignoreRoomCheck = ignoreRoomCheck;
	}



	/**
	 * Return the value associated with the column: MANAGER_IDS
	 */
	public java.lang.String getManagerIds () {
		return managerIds;
	}

	/**
	 * Set the value related to the column: MANAGER_IDS
	 * @param managerIds the MANAGER_IDS value
	 */
	public void setManagerIds (java.lang.String managerIds) {
		this.managerIds = managerIds;
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
	 * Return the value associated with the column: EXAM_ENABLE
	 */
	public java.lang.Integer getExamType () {
		return examType;
	}

	/**
	 * Set the value related to the column: EXAM_ENABLE
	 * @param examEnabled the EXAM_ENABLE value
	 */
	public void setExamType (java.lang.Integer examType) {
		this.examType = examType;
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
	 * Return the value associated with the column: features
	 */
	public java.util.Set getFeatures () {
		return features;
	}

	/**
	 * Set the value related to the column: features
	 * @param features the features value
	 */
	public void setFeatures (java.util.Set features) {
		this.features = features;
	}



	/**
	 * Return the value associated with the column: assignments
	 */
	public java.util.Set getAssignments () {
		return assignments;
	}

	/**
	 * Set the value related to the column: assignments
	 * @param assignments the assignments value
	 */
	public void setAssignments (java.util.Set assignments) {
		this.assignments = assignments;
	}



	/**
	 * Return the value associated with the column: roomGroups
	 */
	public java.util.Set getRoomGroups () {
		return roomGroups;
	}

	/**
	 * Set the value related to the column: roomGroups
	 * @param roomGroups the roomGroups value
	 */
	public void setRoomGroups (java.util.Set roomGroups) {
		this.roomGroups = roomGroups;
	}



	/**
	 * Return the value associated with the column: roomDepts
	 */
	public java.util.Set getRoomDepts () {
		return roomDepts;
	}

	/**
	 * Set the value related to the column: roomDepts
	 * @param roomDepts the roomDepts value
	 */
	public void setRoomDepts (java.util.Set roomDepts) {
		this.roomDepts = roomDepts;
	}

	public void addToroomDepts (org.unitime.timetable.model.RoomDept roomDept) {
		if (null == getRoomDepts()) setRoomDepts(new java.util.HashSet());
		getRoomDepts().add(roomDept);
	}


	public java.util.Set getExamPreferences() { return examPreferences; }
	public void setExamPreferences(java.util.Set examPreferences) { this.examPreferences = examPreferences; }


	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Location)) return false;
		else {
			org.unitime.timetable.model.Location location = (org.unitime.timetable.model.Location) obj;
			if (null == this.getUniqueId() || null == location.getUniqueId()) return false;
			else return (this.getUniqueId().equals(location.getUniqueId()));
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