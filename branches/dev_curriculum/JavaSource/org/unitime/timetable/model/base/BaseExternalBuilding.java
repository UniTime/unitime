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
 * This is an object that contains data related to the EXTERNAL_BUILDING table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="EXTERNAL_BUILDING"
 */

public abstract class BaseExternalBuilding  implements Serializable {

	public static String REF = "ExternalBuilding";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_ABBREVIATION = "abbreviation";
	public static String PROP_COORDINATE_X = "coordinateX";
	public static String PROP_COORDINATE_Y = "coordinateY";
	public static String PROP_DISPLAY_NAME = "displayName";


	// constructors
	public BaseExternalBuilding () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExternalBuilding (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExternalBuilding (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.String abbreviation) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setAbbreviation(abbreviation);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String externalUniqueId;
	private java.lang.String abbreviation;
	private java.lang.Integer coordinateX;
	private java.lang.Integer coordinateY;
	private java.lang.String displayName;

	// many to one
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
	 * Return the value associated with the column: ABBREVIATION
	 */
	public java.lang.String getAbbreviation () {
		return abbreviation;
	}

	/**
	 * Set the value related to the column: ABBREVIATION
	 * @param abbreviation the ABBREVIATION value
	 */
	public void setAbbreviation (java.lang.String abbreviation) {
		this.abbreviation = abbreviation;
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

	public void addTorooms (org.unitime.timetable.model.ExternalRoom externalRoom) {
		if (null == getRooms()) setRooms(new java.util.HashSet());
		getRooms().add(externalRoom);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ExternalBuilding)) return false;
		else {
			org.unitime.timetable.model.ExternalBuilding externalBuilding = (org.unitime.timetable.model.ExternalBuilding) obj;
			if (null == this.getUniqueId() || null == externalBuilding.getUniqueId()) return false;
			else return (this.getUniqueId().equals(externalBuilding.getUniqueId()));
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
