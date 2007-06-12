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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseReservation;

/*
 * OwnerID Discriminator
 * 		InstructionalOffering: "I"
 * 		InstrOfferingConfig:   "R"
 * 		Class_:                "C"
 *		CourseOffering:        "U"
 */

public class Reservation extends BaseReservation {
	private static final long serialVersionUID = 1L;

	/** Request Attribute name for Reservations **/
    public static final String RESV_REQUEST_ATTR = "reservationsList";

	/** Request Attribute name for Reservation Class **/
    public static final String RESV_CLASS_REQUEST_ATTR = "reservationsClassList";

	/** Request Attribute name for Reservation Priorities **/
    public static final String RESV_PRIORITY_REQUEST_ATTR = "reservationsPriorityList";

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Reservation () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Reservation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public Reservation (
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


}