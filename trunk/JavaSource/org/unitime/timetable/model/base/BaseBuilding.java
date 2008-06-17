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
 * This is an object that contains data related to the BUILDING table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="BUILDING"
 */

public abstract class BaseBuilding  implements Serializable {

	public static String REF = "Building";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_ABBREVIATION = "abbreviation";
	public static String PROP_NAME = "name";
	public static String PROP_COORDINATE_X = "coordinateX";
	public static String PROP_COORDINATE_Y = "coordinateY";


	// constructors
	public BaseBuilding () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseBuilding (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseBuilding (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.String externalUniqueId,
		java.lang.String abbreviation,
		java.lang.String name,
		java.lang.Integer coordinateX,
		java.lang.Integer coordinateY) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setExternalUniqueId(externalUniqueId);
		this.setAbbreviation(abbreviation);
		this.setName(name);
		this.setCoordinateX(coordinateX);
		this.setCoordinateY(coordinateY);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String externalUniqueId;
	private java.lang.String abbreviation;
	private java.lang.String name;
	private java.lang.Integer coordinateX;
	private java.lang.Integer coordinateY;

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
		if (!(obj instanceof org.unitime.timetable.model.Building)) return false;
		else {
			org.unitime.timetable.model.Building building = (org.unitime.timetable.model.Building) obj;
			if (null == this.getUniqueId() || null == building.getUniqueId()) return false;
			else return (this.getUniqueId().equals(building.getUniqueId()));
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
