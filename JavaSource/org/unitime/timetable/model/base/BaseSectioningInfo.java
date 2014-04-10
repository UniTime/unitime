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

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.SectioningInfo;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseSectioningInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Double iNbrExpectedStudents;
	private Double iNbrHoldingStudents;

	private Class_ iClazz;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NBR_EXP_STUDENTS = "nbrExpectedStudents";
	public static String PROP_NBR_HOLD_STUDENTS = "nbrHoldingStudents";

	public BaseSectioningInfo() {
		initialize();
	}

	public BaseSectioningInfo(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Double getNbrExpectedStudents() { return iNbrExpectedStudents; }
	public void setNbrExpectedStudents(Double nbrExpectedStudents) { iNbrExpectedStudents = nbrExpectedStudents; }

	public Double getNbrHoldingStudents() { return iNbrHoldingStudents; }
	public void setNbrHoldingStudents(Double nbrHoldingStudents) { iNbrHoldingStudents = nbrHoldingStudents; }

	public Class_ getClazz() { return iClazz; }
	public void setClazz(Class_ clazz) { iClazz = clazz; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SectioningInfo)) return false;
		if (getUniqueId() == null || ((SectioningInfo)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SectioningInfo)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "SectioningInfo["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SectioningInfo[" +
			"\n	Clazz: " + getClazz() +
			"\n	NbrExpectedStudents: " + getNbrExpectedStudents() +
			"\n	NbrHoldingStudents: " + getNbrHoldingStudents() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
