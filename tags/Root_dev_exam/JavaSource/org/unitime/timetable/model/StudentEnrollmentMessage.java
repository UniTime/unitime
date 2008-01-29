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

import org.unitime.timetable.model.base.BaseStudentEnrollmentMessage;



public class StudentEnrollmentMessage extends BaseStudentEnrollmentMessage implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public StudentEnrollmentMessage () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public StudentEnrollmentMessage (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public StudentEnrollmentMessage (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CourseDemand courseDemand,
		java.lang.String message,
		java.lang.Integer level,
		java.lang.Integer type,
		java.util.Date timestamp,
        java.lang.Integer order) {

		super (
			uniqueId,
			courseDemand,
			message,
			level,
			type,
			timestamp,
            order);
	}

/*[CONSTRUCTOR MARKER END]*/

	public int compareTo(Object o) {
        if (o==null || !(o instanceof StudentEnrollmentMessage)) return -1;
        StudentEnrollmentMessage m = (StudentEnrollmentMessage)o;
        int cmp = getCourseDemand().compareTo(m.getCourseDemand());
        if (cmp!=0) return cmp;
        cmp = getOrder().compareTo(m.getOrder());
        if (cmp!=0) return cmp;
        return getUniqueId().compareTo(m.getUniqueId());
    }
}