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
 * This is an object that contains data related to the INSTRUCTIONAL_OFFERING table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="INSTRUCTIONAL_OFFERING"
 */

public abstract class BaseInstructionalOffering  implements Serializable {

	public static String REF = "InstructionalOffering";
	public static String PROP_INSTR_OFFERING_PERM_ID = "instrOfferingPermId";
	public static String PROP_NOT_OFFERED = "notOffered";
	public static String PROP_DEMAND = "demand";
	public static String PROP_PROJECTED_DEMAND = "projectedDemand";
	public static String PROP_CTRL_COURSE_ID = "ctrlCourseId";
	public static String PROP_LIMIT = "limit";
	public static String PROP_DESIGNATOR_REQUIRED = "designatorRequired";
	public static String PROP_UNIQUE_ID_ROLLED_FORWARD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";


	// constructors
	public BaseInstructionalOffering () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseInstructionalOffering (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseInstructionalOffering (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.Integer instrOfferingPermId,
		java.lang.Boolean notOffered,
		java.lang.Boolean designatorRequired) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setInstrOfferingPermId(instrOfferingPermId);
		this.setNotOffered(notOffered);
		this.setDesignatorRequired(designatorRequired);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer instrOfferingPermId;
	private java.lang.Boolean notOffered;
	private java.lang.Integer demand;
	private java.lang.Integer projectedDemand;
	private java.lang.Integer ctrlCourseId;
	private java.lang.Integer limit;
	private java.lang.Boolean designatorRequired;
	private java.lang.Long uniqueIdRolledForwardFrom;
	private java.lang.String externalUniqueId;

	// many to one
	private org.unitime.timetable.model.Session session;
	private org.unitime.timetable.model.OfferingConsentType consentType;

	// collections
	private java.util.Set courseOfferings;
	private java.util.Set instrOfferingConfigs;
	private java.util.Set courseReservations;
	private java.util.Set individualReservations;
	private java.util.Set studentGroupReservations;
	private java.util.Set acadAreaReservations;
	private java.util.Set posReservations;
	private java.util.Set creditConfigs;



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
	 * Return the value associated with the column: INSTR_OFFERING_PERM_ID
	 */
	public java.lang.Integer getInstrOfferingPermId () {
		return instrOfferingPermId;
	}

	/**
	 * Set the value related to the column: INSTR_OFFERING_PERM_ID
	 * @param instrOfferingPermId the INSTR_OFFERING_PERM_ID value
	 */
	public void setInstrOfferingPermId (java.lang.Integer instrOfferingPermId) {
		this.instrOfferingPermId = instrOfferingPermId;
	}



	/**
	 * Return the value associated with the column: NOT_OFFERED
	 */
	public java.lang.Boolean isNotOffered () {
		return notOffered;
	}

	/**
	 * Set the value related to the column: NOT_OFFERED
	 * @param notOffered the NOT_OFFERED value
	 */
	public void setNotOffered (java.lang.Boolean notOffered) {
		this.notOffered = notOffered;
	}



	/**
	 * Return the value associated with the column: demand
	 */
	public java.lang.Integer getDemand () {
		return demand;
	}

	/**
	 * Set the value related to the column: demand
	 * @param demand the demand value
	 */
	public void setDemand (java.lang.Integer demand) {
		this.demand = demand;
	}



	/**
	 * Return the value associated with the column: projectedDemand
	 */
	public java.lang.Integer getProjectedDemand () {
		return projectedDemand;
	}

	/**
	 * Set the value related to the column: projectedDemand
	 * @param projectedDemand the projectedDemand value
	 */
	public void setProjectedDemand (java.lang.Integer projectedDemand) {
		this.projectedDemand = projectedDemand;
	}



	/**
	 * Return the value associated with the column: ctrlCourseId
	 */
	public java.lang.Integer getCtrlCourseId () {
		return ctrlCourseId;
	}

	/**
	 * Set the value related to the column: ctrlCourseId
	 * @param ctrlCourseId the ctrlCourseId value
	 */
	public void setCtrlCourseId (java.lang.Integer ctrlCourseId) {
		this.ctrlCourseId = ctrlCourseId;
	}



	/**
	 * Return the value associated with the column: limit
	 */
	public java.lang.Integer getLimit () {
		return limit;
	}

	/**
	 * Set the value related to the column: limit
	 * @param limit the limit value
	 */
	public void setLimit (java.lang.Integer limit) {
		this.limit = limit;
	}



	/**
	 * Return the value associated with the column: DESIGNATOR_REQUIRED
	 */
	public java.lang.Boolean isDesignatorRequired () {
		return designatorRequired;
	}

	/**
	 * Set the value related to the column: DESIGNATOR_REQUIRED
	 * @param designatorRequired the DESIGNATOR_REQUIRED value
	 */
	public void setDesignatorRequired (java.lang.Boolean designatorRequired) {
		this.designatorRequired = designatorRequired;
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
	 * Return the value associated with the column: CONSENT_TYPE
	 */
	public org.unitime.timetable.model.OfferingConsentType getConsentType () {
		return consentType;
	}

	/**
	 * Set the value related to the column: CONSENT_TYPE
	 * @param consentType the CONSENT_TYPE value
	 */
	public void setConsentType (org.unitime.timetable.model.OfferingConsentType consentType) {
		this.consentType = consentType;
	}



	/**
	 * Return the value associated with the column: courseOfferings
	 */
	public java.util.Set getCourseOfferings () {
		return courseOfferings;
	}

	/**
	 * Set the value related to the column: courseOfferings
	 * @param courseOfferings the courseOfferings value
	 */
	public void setCourseOfferings (java.util.Set courseOfferings) {
		this.courseOfferings = courseOfferings;
	}

	public void addTocourseOfferings (org.unitime.timetable.model.CourseOffering courseOffering) {
		if (null == getCourseOfferings()) setCourseOfferings(new java.util.HashSet());
		getCourseOfferings().add(courseOffering);
	}



	/**
	 * Return the value associated with the column: instrOfferingConfigs
	 */
	public java.util.Set getInstrOfferingConfigs () {
		return instrOfferingConfigs;
	}

	/**
	 * Set the value related to the column: instrOfferingConfigs
	 * @param instrOfferingConfigs the instrOfferingConfigs value
	 */
	public void setInstrOfferingConfigs (java.util.Set instrOfferingConfigs) {
		this.instrOfferingConfigs = instrOfferingConfigs;
	}

	public void addToinstrOfferingConfigs (org.unitime.timetable.model.InstrOfferingConfig instrOfferingConfig) {
		if (null == getInstrOfferingConfigs()) setInstrOfferingConfigs(new java.util.HashSet());
		getInstrOfferingConfigs().add(instrOfferingConfig);
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



	/**
	 * Return the value associated with the column: creditConfigs
	 */
	public java.util.Set getCreditConfigs () {
		return creditConfigs;
	}

	/**
	 * Set the value related to the column: creditConfigs
	 * @param creditConfigs the creditConfigs value
	 */
	public void setCreditConfigs (java.util.Set creditConfigs) {
		this.creditConfigs = creditConfigs;
	}

	public void addTocreditConfigs (org.unitime.timetable.model.CourseCreditUnitConfig courseCreditUnitConfig) {
		if (null == getCreditConfigs()) setCreditConfigs(new java.util.HashSet());
		getCreditConfigs().add(courseCreditUnitConfig);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.InstructionalOffering)) return false;
		else {
			org.unitime.timetable.model.InstructionalOffering instructionalOffering = (org.unitime.timetable.model.InstructionalOffering) obj;
			if (null == this.getUniqueId() || null == instructionalOffering.getUniqueId()) return false;
			else return (this.getUniqueId().equals(instructionalOffering.getUniqueId()));
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
