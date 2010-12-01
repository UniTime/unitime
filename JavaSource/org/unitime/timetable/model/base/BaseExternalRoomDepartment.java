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

import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;

public abstract class BaseExternalRoomDepartment implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iDepartmentCode;
	private Integer iPercent;
	private String iAssignmentType;

	private ExternalRoom iRoom;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_DEPARTMENT_CODE = "departmentCode";
	public static String PROP_PERCENT = "percent";
	public static String PROP_ASSIGNMENT_TYPE = "assignmentType";

	public BaseExternalRoomDepartment() {
		initialize();
	}

	public BaseExternalRoomDepartment(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getDepartmentCode() { return iDepartmentCode; }
	public void setDepartmentCode(String departmentCode) { iDepartmentCode = departmentCode; }

	public Integer getPercent() { return iPercent; }
	public void setPercent(Integer percent) { iPercent = percent; }

	public String getAssignmentType() { return iAssignmentType; }
	public void setAssignmentType(String assignmentType) { iAssignmentType = assignmentType; }

	public ExternalRoom getRoom() { return iRoom; }
	public void setRoom(ExternalRoom room) { iRoom = room; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExternalRoomDepartment)) return false;
		if (getUniqueId() == null || ((ExternalRoomDepartment)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExternalRoomDepartment)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ExternalRoomDepartment["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ExternalRoomDepartment[" +
			"\n	AssignmentType: " + getAssignmentType() +
			"\n	DepartmentCode: " + getDepartmentCode() +
			"\n	Percent: " + getPercent() +
			"\n	Room: " + getRoom() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
