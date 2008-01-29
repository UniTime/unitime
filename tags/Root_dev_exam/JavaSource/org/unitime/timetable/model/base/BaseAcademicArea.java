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
 * This is an object that contains data related to the ACADEMIC_AREA table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="ACADEMIC_AREA"
 */

public abstract class BaseAcademicArea  implements Serializable {

	public static String REF = "AcademicArea";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_ACADEMIC_AREA_ABBREVIATION = "academicAreaAbbreviation";
	public static String PROP_SHORT_TITLE = "shortTitle";
	public static String PROP_LONG_TITLE = "longTitle";


	// constructors
	public BaseAcademicArea () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseAcademicArea (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseAcademicArea (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.String academicAreaAbbreviation,
		java.lang.String shortTitle,
		java.lang.String longTitle) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setAcademicAreaAbbreviation(academicAreaAbbreviation);
		this.setShortTitle(shortTitle);
		this.setLongTitle(longTitle);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String externalUniqueId;
	private java.lang.String academicAreaAbbreviation;
	private java.lang.String shortTitle;
	private java.lang.String longTitle;

	// many to one
	private org.unitime.timetable.model.Session session;

	// collections
	private java.util.Set posMajors;
	private java.util.Set posMinors;



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
	 * Return the value associated with the column: ACADEMIC_AREA_ABBREVIATION
	 */
	public java.lang.String getAcademicAreaAbbreviation () {
		return academicAreaAbbreviation;
	}

	/**
	 * Set the value related to the column: ACADEMIC_AREA_ABBREVIATION
	 * @param academicAreaAbbreviation the ACADEMIC_AREA_ABBREVIATION value
	 */
	public void setAcademicAreaAbbreviation (java.lang.String academicAreaAbbreviation) {
		this.academicAreaAbbreviation = academicAreaAbbreviation;
	}



	/**
	 * Return the value associated with the column: SHORT_TITLE
	 */
	public java.lang.String getShortTitle () {
		return shortTitle;
	}

	/**
	 * Set the value related to the column: SHORT_TITLE
	 * @param shortTitle the SHORT_TITLE value
	 */
	public void setShortTitle (java.lang.String shortTitle) {
		this.shortTitle = shortTitle;
	}



	/**
	 * Return the value associated with the column: LONG_TITLE
	 */
	public java.lang.String getLongTitle () {
		return longTitle;
	}

	/**
	 * Set the value related to the column: LONG_TITLE
	 * @param longTitle the LONG_TITLE value
	 */
	public void setLongTitle (java.lang.String longTitle) {
		this.longTitle = longTitle;
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
	 * Return the value associated with the column: posMajors
	 */
	public java.util.Set getPosMajors () {
		return posMajors;
	}

	/**
	 * Set the value related to the column: posMajors
	 * @param posMajors the posMajors value
	 */
	public void setPosMajors (java.util.Set posMajors) {
		this.posMajors = posMajors;
	}



	/**
	 * Return the value associated with the column: posMinors
	 */
	public java.util.Set getPosMinors () {
		return posMinors;
	}

	/**
	 * Set the value related to the column: posMinors
	 * @param posMinors the posMinors value
	 */
	public void setPosMinors (java.util.Set posMinors) {
		this.posMinors = posMinors;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.AcademicArea)) return false;
		else {
			org.unitime.timetable.model.AcademicArea academicArea = (org.unitime.timetable.model.AcademicArea) obj;
			if (null == this.getUniqueId() || null == academicArea.getUniqueId()) return false;
			else return (this.getUniqueId().equals(academicArea.getUniqueId()));
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