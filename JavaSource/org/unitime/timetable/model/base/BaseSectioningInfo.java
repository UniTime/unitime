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
 * This is an object that contains data related to the SECTIONING_INFO table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="SECTIONING_INFO"
 */

public abstract class BaseSectioningInfo  implements Serializable {

	public static String REF = "SectioningInfo";
	public static String PROP_NBR_EXPECTED_STUDENTS = "nbrExpectedStudents";
	public static String PROP_NBR_HOLDING_STUDENTS = "nbrHoldingStudents";


	// constructors
	public BaseSectioningInfo () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseSectioningInfo (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseSectioningInfo (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Class_ clazz,
		java.lang.Double nbrExpectedStudents,
		java.lang.Double nbrHoldingStudents) {

		this.setUniqueId(uniqueId);
		this.setClazz(clazz);
		this.setNbrExpectedStudents(nbrExpectedStudents);
		this.setNbrHoldingStudents(nbrHoldingStudents);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Double nbrExpectedStudents;
	private java.lang.Double nbrHoldingStudents;

	// many to one
	private org.unitime.timetable.model.Class_ clazz;



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
	 * Return the value associated with the column: NBR_EXP_STUDENTS
	 */
	public java.lang.Double getNbrExpectedStudents () {
		return nbrExpectedStudents;
	}

	/**
	 * Set the value related to the column: NBR_EXP_STUDENTS
	 * @param nbrExpectedStudents the NBR_EXP_STUDENTS value
	 */
	public void setNbrExpectedStudents (java.lang.Double nbrExpectedStudents) {
		this.nbrExpectedStudents = nbrExpectedStudents;
	}



	/**
	 * Return the value associated with the column: NBR_HOLD_STUDENTS
	 */
	public java.lang.Double getNbrHoldingStudents () {
		return nbrHoldingStudents;
	}

	/**
	 * Set the value related to the column: NBR_HOLD_STUDENTS
	 * @param nbrHoldingStudents the NBR_HOLD_STUDENTS value
	 */
	public void setNbrHoldingStudents (java.lang.Double nbrHoldingStudents) {
		this.nbrHoldingStudents = nbrHoldingStudents;
	}



	/**
	 * Return the value associated with the column: CLASS_ID
	 */
	public org.unitime.timetable.model.Class_ getClazz () {
		return clazz;
	}

	/**
	 * Set the value related to the column: CLASS_ID
	 * @param clazz the CLASS_ID value
	 */
	public void setClazz (org.unitime.timetable.model.Class_ clazz) {
		this.clazz = clazz;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.SectioningInfo)) return false;
		else {
			org.unitime.timetable.model.SectioningInfo sectioningInfo = (org.unitime.timetable.model.SectioningInfo) obj;
			if (null == this.getUniqueId() || null == sectioningInfo.getUniqueId()) return false;
			else return (this.getUniqueId().equals(sectioningInfo.getUniqueId()));
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