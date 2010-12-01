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
 * This is an object that contains data related to the  table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table=""
 */

public abstract class BaseCharacteristicReservation extends org.unitime.timetable.model.Reservation  implements Serializable {

	public static String REF = "CharacteristicReservation";
	public static String PROP_RESERVED = "reserved";
	public static String PROP_REQUESTED = "requested";
	public static String PROP_PRIOR_ENROLLMENT = "priorEnrollment";
	public static String PROP_PROJECTED_ENROLLMENT = "projectedEnrollment";


	// constructors
	public BaseCharacteristicReservation () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCharacteristicReservation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCharacteristicReservation (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.ReservationType reservationType,
		java.lang.String ownerClassId,
		java.lang.Long owner,
		java.lang.Integer priority) {

		super (
			uniqueId,
			reservationType,
			ownerClassId,
			owner,
			priority);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.Integer reserved;
	private java.lang.Integer requested;
	private java.lang.Integer priorEnrollment;
	private java.lang.Integer projectedEnrollment;






	/**
	 * Return the value associated with the column: RESERVED
	 */
	public java.lang.Integer getReserved () {
		return reserved;
	}

	/**
	 * Set the value related to the column: RESERVED
	 * @param reserved the RESERVED value
	 */
	public void setReserved (java.lang.Integer reserved) {
		this.reserved = reserved;
	}



	/**
	 * Return the value associated with the column: REQUESTED
	 */
	public java.lang.Integer getRequested () {
		return requested;
	}

	/**
	 * Set the value related to the column: REQUESTED
	 * @param requested the REQUESTED value
	 */
	public void setRequested (java.lang.Integer requested) {
		this.requested = requested;
	}



	/**
	 * Return the value associated with the column: PRIOR_ENROLLMENT
	 */
	public java.lang.Integer getPriorEnrollment () {
		return priorEnrollment;
	}

	/**
	 * Set the value related to the column: PRIOR_ENROLLMENT
	 * @param priorEnrollment the PRIOR_ENROLLMENT value
	 */
	public void setPriorEnrollment (java.lang.Integer priorEnrollment) {
		this.priorEnrollment = priorEnrollment;
	}



	/**
	 * Return the value associated with the column: PROJECTED_ENROLLMENT
	 */
	public java.lang.Integer getProjectedEnrollment () {
		return projectedEnrollment;
	}

	/**
	 * Set the value related to the column: PROJECTED_ENROLLMENT
	 * @param projectedEnrollment the PROJECTED_ENROLLMENT value
	 */
	public void setProjectedEnrollment (java.lang.Integer projectedEnrollment) {
		this.projectedEnrollment = projectedEnrollment;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CharacteristicReservation)) return false;
		else {
			org.unitime.timetable.model.CharacteristicReservation characteristicReservation = (org.unitime.timetable.model.CharacteristicReservation) obj;
			if (null == this.getUniqueId() || null == characteristicReservation.getUniqueId()) return false;
			else return (this.getUniqueId().equals(characteristicReservation.getUniqueId()));
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
