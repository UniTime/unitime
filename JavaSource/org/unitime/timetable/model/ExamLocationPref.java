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

import org.unitime.timetable.model.base.BaseExamLocationPref;


public class ExamLocationPref extends BaseExamLocationPref implements Comparable<ExamLocationPref> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExamLocationPref () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExamLocationPref (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public ExamLocationPref (
	        java.lang.Long uniqueId,
	        org.unitime.timetable.model.Location location,
	        org.unitime.timetable.model.PreferenceLevel prefLevel,
	        org.unitime.timetable.model.ExamPeriod period) {

		super (
			uniqueId,
			location,
			prefLevel,
			period);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public int compareTo(ExamLocationPref pref) {
	    return pref.getExamPeriod().compareTo(pref.getExamPeriod());
	}
	
	public String toString() {
	    return PreferenceLevel.prolog2abbv(getPrefLevel().getPrefProlog())+" "+getExamPeriod().getAbbreviation();
	}
	
}