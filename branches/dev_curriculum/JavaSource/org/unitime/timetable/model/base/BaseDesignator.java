/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Designator;
import org.unitime.timetable.model.SubjectArea;

public abstract class BaseDesignator implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iCode;

	private SubjectArea iSubjectArea;
	private DepartmentalInstructor iInstructor;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CODE = "code";

	public BaseDesignator() {
		initialize();
	}

	public BaseDesignator(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getCode() { return iCode; }
	public void setCode(String code) { iCode = code; }

	public SubjectArea getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(SubjectArea subjectArea) { iSubjectArea = subjectArea; }

	public DepartmentalInstructor getInstructor() { return iInstructor; }
	public void setInstructor(DepartmentalInstructor instructor) { iInstructor = instructor; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Designator)) return false;
		if (getUniqueId() == null || ((Designator)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Designator)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Designator["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Designator[" +
			"\n	Code: " + getCode() +
			"\n	Instructor: " + getInstructor() +
			"\n	SubjectArea: " + getSubjectArea() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
