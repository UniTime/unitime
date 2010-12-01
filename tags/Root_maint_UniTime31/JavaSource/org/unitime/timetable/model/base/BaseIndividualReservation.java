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
 * This is an object that contains data related to the INDIVIDUAL_RESERVATION table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="INDIVIDUAL_RESERVATION"
 */

public abstract class BaseIndividualReservation extends org.unitime.timetable.model.Reservation  implements Serializable {

	public static String REF = "IndividualReservation";
	public static String PROP_EXTERNAL_UNIQUE_ID = "externalUniqueId";
	public static String PROP_OVER_LIMIT = "overLimit";
	public static String PROP_EXPIRATION_DATE = "expirationDate";


	// constructors
	public BaseIndividualReservation () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseIndividualReservation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseIndividualReservation (
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
	private java.lang.String externalUniqueId;
	private java.lang.Boolean overLimit;
	private java.util.Date expirationDate;






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
	 * Return the value associated with the column: OVER_LIMIT
	 */
	public java.lang.Boolean isOverLimit () {
		return overLimit;
	}

	/**
	 * Set the value related to the column: OVER_LIMIT
	 * @param overLimit the OVER_LIMIT value
	 */
	public void setOverLimit (java.lang.Boolean overLimit) {
		this.overLimit = overLimit;
	}



	/**
	 * Return the value associated with the column: EXPIRATION_DATE
	 */
	public java.util.Date getExpirationDate () {
		return expirationDate;
	}

	/**
	 * Set the value related to the column: EXPIRATION_DATE
	 * @param expirationDate the EXPIRATION_DATE value
	 */
	public void setExpirationDate (java.util.Date expirationDate) {
		this.expirationDate = expirationDate;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.IndividualReservation)) return false;
		else {
			org.unitime.timetable.model.IndividualReservation individualReservation = (org.unitime.timetable.model.IndividualReservation) obj;
			if (null == this.getUniqueId() || null == individualReservation.getUniqueId()) return false;
			else return (this.getUniqueId().equals(individualReservation.getUniqueId()));
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
