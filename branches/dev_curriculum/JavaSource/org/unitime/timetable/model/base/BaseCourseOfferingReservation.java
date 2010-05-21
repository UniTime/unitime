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
 * This is an object that contains data related to the COURSE_RESERVATION table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="COURSE_RESERVATION"
 */

public abstract class BaseCourseOfferingReservation extends org.unitime.timetable.model.CharacteristicReservation  implements Serializable {

	public static String REF = "CourseOfferingReservation";


	// constructors
	public BaseCourseOfferingReservation () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseCourseOfferingReservation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseCourseOfferingReservation (
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


	// many to one
	private org.unitime.timetable.model.CourseOffering courseOffering;






	/**
	 * Return the value associated with the column: COURSE_OFFERING
	 */
	public org.unitime.timetable.model.CourseOffering getCourseOffering () {
		return courseOffering;
	}

	/**
	 * Set the value related to the column: COURSE_OFFERING
	 * @param courseOffering the COURSE_OFFERING value
	 */
	public void setCourseOffering (org.unitime.timetable.model.CourseOffering courseOffering) {
		this.courseOffering = courseOffering;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.CourseOfferingReservation)) return false;
		else {
			org.unitime.timetable.model.CourseOfferingReservation courseOfferingReservation = (org.unitime.timetable.model.CourseOfferingReservation) obj;
			if (null == this.getUniqueId() || null == courseOfferingReservation.getUniqueId()) return false;
			else return (this.getUniqueId().equals(courseOfferingReservation.getUniqueId()));
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
