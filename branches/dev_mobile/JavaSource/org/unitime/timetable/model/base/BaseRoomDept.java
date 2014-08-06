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

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomDept;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseRoomDept implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Boolean iControl;

	private Location iRoom;
	private Department iDepartment;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_IS_CONTROL = "control";

	public BaseRoomDept() {
		initialize();
	}

	public BaseRoomDept(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Boolean isControl() { return iControl; }
	public Boolean getControl() { return iControl; }
	public void setControl(Boolean control) { iControl = control; }

	public Location getRoom() { return iRoom; }
	public void setRoom(Location room) { iRoom = room; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomDept)) return false;
		if (getUniqueId() == null || ((RoomDept)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomDept)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "RoomDept["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "RoomDept[" +
			"\n	Control: " + getControl() +
			"\n	Department: " + getDepartment() +
			"\n	Room: " + getRoom() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
