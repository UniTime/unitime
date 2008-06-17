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

import org.unitime.timetable.model.base.BaseClassWaitList;



public class ClassWaitList extends BaseClassWaitList {
    public static Integer TYPE_WAITLIST = new Integer(0);
    public static Integer TYPE_SELECTION = new Integer(1);
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ClassWaitList () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ClassWaitList (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public ClassWaitList (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Student student,
		org.unitime.timetable.model.Class_ clazz,
		java.lang.Integer type,
		java.util.Date timestamp) {

		super (
			uniqueId,
			student,
			clazz,
			type,
			timestamp);
	}

/*[CONSTRUCTOR MARKER END]*/


}
