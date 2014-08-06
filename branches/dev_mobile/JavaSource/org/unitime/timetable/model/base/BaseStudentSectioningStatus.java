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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.StudentSectioningStatus;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseStudentSectioningStatus extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iStatus;
	private String iMessage;

	private Set<CourseType> iTypes;

	public static String PROP_STATUS = "status";
	public static String PROP_MESSAGE = "message";

	public BaseStudentSectioningStatus() {
		initialize();
	}

	public BaseStudentSectioningStatus(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Integer getStatus() { return iStatus; }
	public void setStatus(Integer status) { iStatus = status; }

	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	public Set<CourseType> getTypes() { return iTypes; }
	public void setTypes(Set<CourseType> types) { iTypes = types; }
	public void addTotypes(CourseType courseType) {
		if (iTypes == null) iTypes = new HashSet<CourseType>();
		iTypes.add(courseType);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentSectioningStatus)) return false;
		if (getUniqueId() == null || ((StudentSectioningStatus)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentSectioningStatus)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "StudentSectioningStatus["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "StudentSectioningStatus[" +
			"\n	Label: " + getLabel() +
			"\n	Message: " + getMessage() +
			"\n	Reference: " + getReference() +
			"\n	Status: " + getStatus() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
