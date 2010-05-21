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
 * This is an object that contains data related to the INSTR_OFFERING_CONFIG table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="INSTR_OFFERING_CONFIG"
 */

public abstract class BaseInstrOfferingConfig  implements Serializable {

	public static String REF = "InstrOfferingConfig";
	public static String PROP_LIMIT = "limit";
	public static String PROP_UNLIMITED_ENROLLMENT = "unlimitedEnrollment";
	public static String PROP_NAME = "name";
	public static String PROP_UNIQUE_ID_ROLLED_FORWARD_FROM = "uniqueIdRolledForwardFrom";


	// constructors
	public BaseInstrOfferingConfig () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseInstrOfferingConfig (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseInstrOfferingConfig (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.InstructionalOffering instructionalOffering,
		java.lang.Integer limit,
		java.lang.Boolean unlimitedEnrollment) {

		this.setUniqueId(uniqueId);
		this.setInstructionalOffering(instructionalOffering);
		this.setLimit(limit);
		this.setUnlimitedEnrollment(unlimitedEnrollment);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer limit;
	private java.lang.Boolean unlimitedEnrollment;
	private java.lang.String name;
	private java.lang.Long uniqueIdRolledForwardFrom;

	// many to one
	private org.unitime.timetable.model.InstructionalOffering instructionalOffering;

	// collections
	private java.util.Set schedulingSubparts;
	private java.util.Set courseReservations;
	private java.util.Set individualReservations;
	private java.util.Set studentGroupReservations;
	private java.util.Set acadAreaReservations;
	private java.util.Set posReservations;



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
	 * Return the value associated with the column: CONFIG_LIMIT
	 */
	public java.lang.Integer getLimit () {
		return limit;
	}

	/**
	 * Set the value related to the column: CONFIG_LIMIT
	 * @param limit the CONFIG_LIMIT value
	 */
	public void setLimit (java.lang.Integer limit) {
		this.limit = limit;
	}



	/**
	 * Return the value associated with the column: UNLIMITED_ENROLLMENT
	 */
	public java.lang.Boolean isUnlimitedEnrollment () {
		return unlimitedEnrollment;
	}

	/**
	 * Set the value related to the column: UNLIMITED_ENROLLMENT
	 * @param unlimitedEnrollment the UNLIMITED_ENROLLMENT value
	 */
	public void setUnlimitedEnrollment (java.lang.Boolean unlimitedEnrollment) {
		this.unlimitedEnrollment = unlimitedEnrollment;
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
	 * Return the value associated with the column: UID_ROLLED_FWD_FROM
	 */
	public java.lang.Long getUniqueIdRolledForwardFrom () {
		return uniqueIdRolledForwardFrom;
	}

	/**
	 * Set the value related to the column: UID_ROLLED_FWD_FROM
	 * @param uniqueIdRolledForwardFrom the UID_ROLLED_FWD_FROM value
	 */
	public void setUniqueIdRolledForwardFrom (java.lang.Long uniqueIdRolledForwardFrom) {
		this.uniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom;
	}



	/**
	 * Return the value associated with the column: INSTR_OFFR_ID
	 */
	public org.unitime.timetable.model.InstructionalOffering getInstructionalOffering () {
		return instructionalOffering;
	}

	/**
	 * Set the value related to the column: INSTR_OFFR_ID
	 * @param instructionalOffering the INSTR_OFFR_ID value
	 */
	public void setInstructionalOffering (org.unitime.timetable.model.InstructionalOffering instructionalOffering) {
		this.instructionalOffering = instructionalOffering;
	}



	/**
	 * Return the value associated with the column: schedulingSubparts
	 */
	public java.util.Set getSchedulingSubparts () {
		return schedulingSubparts;
	}

	/**
	 * Set the value related to the column: schedulingSubparts
	 * @param schedulingSubparts the schedulingSubparts value
	 */
	public void setSchedulingSubparts (java.util.Set schedulingSubparts) {
		this.schedulingSubparts = schedulingSubparts;
	}



	/**
	 * Return the value associated with the column: courseReservations
	 */
	public java.util.Set getCourseReservations () {
		return courseReservations;
	}

	/**
	 * Set the value related to the column: courseReservations
	 * @param courseReservations the courseReservations value
	 */
	public void setCourseReservations (java.util.Set courseReservations) {
		this.courseReservations = courseReservations;
	}



	/**
	 * Return the value associated with the column: individualReservations
	 */
	public java.util.Set getIndividualReservations () {
		return individualReservations;
	}

	/**
	 * Set the value related to the column: individualReservations
	 * @param individualReservations the individualReservations value
	 */
	public void setIndividualReservations (java.util.Set individualReservations) {
		this.individualReservations = individualReservations;
	}



	/**
	 * Return the value associated with the column: studentGroupReservations
	 */
	public java.util.Set getStudentGroupReservations () {
		return studentGroupReservations;
	}

	/**
	 * Set the value related to the column: studentGroupReservations
	 * @param studentGroupReservations the studentGroupReservations value
	 */
	public void setStudentGroupReservations (java.util.Set studentGroupReservations) {
		this.studentGroupReservations = studentGroupReservations;
	}



	/**
	 * Return the value associated with the column: acadAreaReservations
	 */
	public java.util.Set getAcadAreaReservations () {
		return acadAreaReservations;
	}

	/**
	 * Set the value related to the column: acadAreaReservations
	 * @param acadAreaReservations the acadAreaReservations value
	 */
	public void setAcadAreaReservations (java.util.Set acadAreaReservations) {
		this.acadAreaReservations = acadAreaReservations;
	}



	/**
	 * Return the value associated with the column: posReservations
	 */
	public java.util.Set getPosReservations () {
		return posReservations;
	}

	/**
	 * Set the value related to the column: posReservations
	 * @param posReservations the posReservations value
	 */
	public void setPosReservations (java.util.Set posReservations) {
		this.posReservations = posReservations;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.InstrOfferingConfig)) return false;
		else {
			org.unitime.timetable.model.InstrOfferingConfig instrOfferingConfig = (org.unitime.timetable.model.InstrOfferingConfig) obj;
			if (null == this.getUniqueId() || null == instrOfferingConfig.getUniqueId()) return false;
			else return (this.getUniqueId().equals(instrOfferingConfig.getUniqueId()));
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
