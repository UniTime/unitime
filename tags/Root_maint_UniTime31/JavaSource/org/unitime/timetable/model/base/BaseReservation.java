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

public abstract class BaseReservation  implements Serializable {

	public static String REF = "Reservation";
	public static String PROP_OWNER_CLASS_ID = "ownerClassId";
	public static String PROP_OWNER = "owner";
	public static String PROP_PRIORITY = "priority";


	// constructors
	public BaseReservation () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseReservation (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseReservation (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.ReservationType reservationType,
		java.lang.String ownerClassId,
		java.lang.Long owner,
		java.lang.Integer priority) {

		this.setUniqueId(uniqueId);
		this.setReservationType(reservationType);
		this.setOwnerClassId(ownerClassId);
		this.setOwner(owner);
		this.setPriority(priority);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String ownerClassId;
	private java.lang.Long owner;
	private java.lang.Integer priority;

	// many to one
	private org.unitime.timetable.model.ReservationType reservationType;



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
	 * Return the value associated with the column: OWNER_CLASS_ID
	 */
	public java.lang.String getOwnerClassId () {
		return ownerClassId;
	}

	/**
	 * Set the value related to the column: OWNER_CLASS_ID
	 * @param ownerClassId the OWNER_CLASS_ID value
	 */
	public void setOwnerClassId (java.lang.String ownerClassId) {
		this.ownerClassId = ownerClassId;
	}



	/**
	 * Return the value associated with the column: OWNER
	 */
	public java.lang.Long getOwner () {
		return owner;
	}

	/**
	 * Set the value related to the column: OWNER
	 * @param owner the OWNER value
	 */
	public void setOwner (java.lang.Long owner) {
		this.owner = owner;
	}



	/**
	 * Return the value associated with the column: PRIORITY
	 */
	public java.lang.Integer getPriority () {
		return priority;
	}

	/**
	 * Set the value related to the column: PRIORITY
	 * @param priority the PRIORITY value
	 */
	public void setPriority (java.lang.Integer priority) {
		this.priority = priority;
	}



	/**
	 * Return the value associated with the column: RESERVATION_TYPE
	 */
	public org.unitime.timetable.model.ReservationType getReservationType () {
		return reservationType;
	}

	/**
	 * Set the value related to the column: RESERVATION_TYPE
	 * @param reservationType the RESERVATION_TYPE value
	 */
	public void setReservationType (org.unitime.timetable.model.ReservationType reservationType) {
		this.reservationType = reservationType;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.Reservation)) return false;
		else {
			org.unitime.timetable.model.Reservation reservation = (org.unitime.timetable.model.Reservation) obj;
			if (null == this.getUniqueId() || null == reservation.getUniqueId()) return false;
			else return (this.getUniqueId().equals(reservation.getUniqueId()));
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
