/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseVariableRangeCreditUnitConfig extends VariableFixedCreditUnitConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Boolean iFractionalIncrementsAllowed;


	public static String PROP_FRACTIONAL_INCR_ALLOWED = "fractionalIncrementsAllowed";

	public BaseVariableRangeCreditUnitConfig() {
		initialize();
	}

	public BaseVariableRangeCreditUnitConfig(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Boolean isFractionalIncrementsAllowed() { return iFractionalIncrementsAllowed; }
	public Boolean getFractionalIncrementsAllowed() { return iFractionalIncrementsAllowed; }
	public void setFractionalIncrementsAllowed(Boolean fractionalIncrementsAllowed) { iFractionalIncrementsAllowed = fractionalIncrementsAllowed; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof VariableRangeCreditUnitConfig)) return false;
		if (getUniqueId() == null || ((VariableRangeCreditUnitConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((VariableRangeCreditUnitConfig)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "VariableRangeCreditUnitConfig["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "VariableRangeCreditUnitConfig[" +
			"\n	CourseCreditFormat: " + getCourseCreditFormat() +
			"\n	CourseOwner: " + getCourseOwner() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	DefinesCreditAtCourseLevel: " + getDefinesCreditAtCourseLevel() +
			"\n	FractionalIncrementsAllowed: " + getFractionalIncrementsAllowed() +
			"\n	MaxUnits: " + getMaxUnits() +
			"\n	MinUnits: " + getMinUnits() +
			"\n	SubpartOwner: " + getSubpartOwner() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
