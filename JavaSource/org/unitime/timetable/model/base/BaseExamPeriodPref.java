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
package org.unitime.timetable.model.base;

import java.io.Serializable;

public abstract class BaseExamPeriodPref extends org.unitime.timetable.model.Preference  implements Serializable {

	public static String REF = "ExamPeriodPref";


	// constructors
	public BaseExamPeriodPref () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseExamPeriodPref (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BaseExamPeriodPref (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.PreferenceGroup owner,
		org.unitime.timetable.model.PreferenceLevel prefLevel) {

		super (
			uniqueId,
			owner,
			prefLevel);
	}



	private int hashCode = Integer.MIN_VALUE;


	// many to one
	private org.unitime.timetable.model.ExamPeriod examPeriod;



	public org.unitime.timetable.model.ExamPeriod getExamPeriod() {
		return examPeriod;
	}

	public void setExamPeriod (org.unitime.timetable.model.ExamPeriod examPeriod) {
		this.examPeriod = examPeriod;
	}

	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.timetable.model.ExamPeriodPref)) return false;
		else {
			org.unitime.timetable.model.ExamPeriodPref examPeriodPref = (org.unitime.timetable.model.ExamPeriodPref) obj;
			if (null == this.getUniqueId() || null == examPeriodPref.getUniqueId()) return false;
			else return (this.getUniqueId().equals(examPeriodPref.getUniqueId()));
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