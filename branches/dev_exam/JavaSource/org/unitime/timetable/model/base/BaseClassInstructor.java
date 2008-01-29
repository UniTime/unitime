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
 * This is an object that contains data related to the CLASS_INSTRUCTOR table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="CLASS_INSTRUCTOR"
 */

public abstract class BaseClassInstructor  implements Serializable {

	public static String REF = "ClassInstructor";
	public static String PROP_PERCENT_SHARE = "percentShare";
	public static String PROP_LEAD = "lead";


	// constructors
	public BaseClassInstructor () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseClassInstructor (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseClassInstructor (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Class_ classInstructing,
		org.unitime.timetable.model.DepartmentalInstructor instructor,
		java.lang.Integer percentShare,
		java.lang.Boolean lead) {

		this.setUniqueId(uniqueId);
		this.setClassInstructing(classInstructing);
		this.setInstructor(instructor);
		this.setPercentShare(percentShare);
		this.setLead(lead);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer percentShare;
	private java.lang.Boolean lead;

	// many to one
	private org.unitime.timetable.model.Class_ classInstructing;
	private org.unitime.timetable.model.DepartmentalInstructor instructor;



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
	 * Return the value associated with the column: PERCENT_SHARE
	 */
	public java.lang.Integer getPercentShare () {
		return percentShare;
	}

	/**
	 * Set the value related to the column: PERCENT_SHARE
	 * @param percentShare the PERCENT_SHARE value
	 */
	public void setPercentShare (java.lang.Integer percentShare) {
		this.percentShare = percentShare;
	}



	/**
	 * Return the value associated with the column: IS_LEAD
	 */
	public java.lang.Boolean isLead () {
		return lead;
	}

	/**
	 * Set the value related to the column: IS_LEAD
	 * @param lead the IS_LEAD value
	 */
	public void setLead (java.lang.Boolean lead) {
		this.lead = lead;
	}



	/**
	 * Return the value associated with the column: CLASS_ID
	 */
	public org.unitime.timetable.model.Class_ getClassInstructing () {
		return classInstructing;
	}

	/**
	 * Set the value related to the column: CLASS_ID
	 * @param classInstructing the CLASS_ID value
	 */
	public void setClassInstructing (org.unitime.timetable.model.Class_ classInstructing) {
		this.classInstructing = classInstructing;
	}



	/**
	 * Return the value associated with the column: INSTRUCTOR_ID
	 */
	public org.unitime.timetable.model.DepartmentalInstructor getInstructor () {
		return instructor;
	}

	/**
	 * Set the value related to the column: INSTRUCTOR_ID
	 * @param instructor the INSTRUCTOR_ID value
	 */
	public void setInstructor (org.unitime.timetable.model.DepartmentalInstructor instructor) {
		this.instructor = instructor;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ClassInstructor)) return false;
		else {
			org.unitime.timetable.model.ClassInstructor classInstructor = (org.unitime.timetable.model.ClassInstructor) obj;
			if (null == this.getUniqueId() || null == classInstructor.getUniqueId()) return false;
			else return (this.getUniqueId().equals(classInstructor.getUniqueId()));
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