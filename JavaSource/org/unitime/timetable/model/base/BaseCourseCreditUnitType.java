/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.RefTableEntry;

public abstract class BaseCourseCreditUnitType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iAbbreviation;


	public static String PROP_ABBREVIATION = "abbreviation";

	public BaseCourseCreditUnitType() {
		initialize();
	}

	public BaseCourseCreditUnitType(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseCreditUnitType)) return false;
		if (getUniqueId() == null || ((CourseCreditUnitType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseCreditUnitType)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CourseCreditUnitType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "CourseCreditUnitType[" +
			"\n	Abbreviation: " + getAbbreviation() +
			"\n	Label: " + getLabel() +
			"\n	Reference: " + getReference() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
