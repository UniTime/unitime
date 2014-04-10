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

import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.FixedCreditUnitConfig;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseFixedCreditUnitConfig extends CourseCreditUnitConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Float iFixedUnits;


	public static String PROP_FIXED_UNITS = "fixedUnits";

	public BaseFixedCreditUnitConfig() {
		initialize();
	}

	public BaseFixedCreditUnitConfig(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Float getFixedUnits() { return iFixedUnits; }
	public void setFixedUnits(Float fixedUnits) { iFixedUnits = fixedUnits; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof FixedCreditUnitConfig)) return false;
		if (getUniqueId() == null || ((FixedCreditUnitConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((FixedCreditUnitConfig)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "FixedCreditUnitConfig["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "FixedCreditUnitConfig[" +
			"\n	CourseCreditFormat: " + getCourseCreditFormat() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	DefinesCreditAtCourseLevel: " + getDefinesCreditAtCourseLevel() +
			"\n	FixedUnits: " + getFixedUnits() +
			"\n	InstructionalOfferingOwner: " + getInstructionalOfferingOwner() +
			"\n	SubpartOwner: " + getSubpartOwner() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
