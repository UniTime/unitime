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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseCourseOfferingReservation;



public class CourseOfferingReservation extends BaseCourseOfferingReservation {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseOfferingReservation () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseOfferingReservation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public CourseOfferingReservation (
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

/*[CONSTRUCTOR MARKER END]*/

	/**
	 * Get Course Name (Subject Abbreviation & Course Number)
	 */
	
	public String getCourseName() {
		return getCourseOffering().getCourseName();
	}
	
    public Object clone() {
    	CourseOfferingReservation cor = new CourseOfferingReservation();
		cor.setCourseOffering(this.getCourseOffering());
		cor.setOwner(this.getOwner());
		cor.setOwnerClassId(this.getOwnerClassId());
		cor.setPriorEnrollment(this.getPriorEnrollment());
		cor.setPriority(this.getPriority());
		cor.setProjectedEnrollment(this.getProjectedEnrollment());
		cor.setRequested(this.getRequested());
		cor.setReservationType(this.getReservationType());
		cor.setReserved(this.getReserved());
    	return cor;
    }
}
