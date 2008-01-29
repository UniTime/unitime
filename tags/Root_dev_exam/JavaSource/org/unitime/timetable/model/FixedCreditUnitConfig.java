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

import org.unitime.timetable.model.base.BaseFixedCreditUnitConfig;



public class FixedCreditUnitConfig extends BaseFixedCreditUnitConfig {
	private static final long serialVersionUID = 1L;
	public static String CREDIT_FORMAT = "fixedUnit";

/*[CONSTRUCTOR MARKER BEGIN]*/
	public FixedCreditUnitConfig () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public FixedCreditUnitConfig (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public FixedCreditUnitConfig (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CourseCreditType creditType,
		org.unitime.timetable.model.CourseCreditUnitType creditUnitType,
		java.lang.String creditFormat,
		java.lang.Boolean definesCreditAtCourseLevel) {

		super (
			uniqueId,
			creditType,
			creditUnitType,
			creditFormat,
			definesCreditAtCourseLevel);
	}

/*[CONSTRUCTOR MARKER END]*/

	public String creditText() {
		StringBuffer sb = new StringBuffer();
		sb.append(sCreditFormat.format(this.getFixedUnits()));
		sb.append(" ");
		sb.append(this.getCreditUnitType().getLabel());
		sb.append(" of ");
		sb.append(this.getCreditType().getLabel());
		return(sb.toString());
	}
	
	public String creditAbbv() {
		return (getCreditFormatAbbv()+" "+sCreditFormat.format(getFixedUnits())+" "+getCreditUnitType().getAbbv()+" "+getCreditType().getAbbv()).trim();
	}

	public Object clone() {
		FixedCreditUnitConfig newCreditConfig = new FixedCreditUnitConfig();
		baseClone(newCreditConfig);
		newCreditConfig.setFixedUnits(getFixedUnits());
		return (newCreditConfig);
	}
	
}