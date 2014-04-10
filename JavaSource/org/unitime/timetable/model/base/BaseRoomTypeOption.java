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
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.RoomTypeOption;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseRoomTypeOption implements Serializable {
	private static final long serialVersionUID = 1L;

	private RoomType iRoomType;
	private Department iDepartment;
	private Integer iStatus;
	private String iMessage;
	private Integer iBreakTime;


	public static String PROP_STATUS = "status";
	public static String PROP_MESSAGE = "message";
	public static String PROP_BREAK_TIME = "breakTime";

	public BaseRoomTypeOption() {
		initialize();
	}

	protected void initialize() {}

	public RoomType getRoomType() { return iRoomType; }
	public void setRoomType(RoomType roomType) { iRoomType = roomType; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public Integer getStatus() { return iStatus; }
	public void setStatus(Integer status) { iStatus = status; }

	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	public Integer getBreakTime() { return iBreakTime; }
	public void setBreakTime(Integer breakTime) { iBreakTime = breakTime; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomTypeOption)) return false;
		RoomTypeOption roomTypeOption = (RoomTypeOption)o;
		if (getRoomType() == null || roomTypeOption.getRoomType() == null || !getRoomType().equals(roomTypeOption.getRoomType())) return false;
		if (getDepartment() == null || roomTypeOption.getDepartment() == null || !getDepartment().equals(roomTypeOption.getDepartment())) return false;
		return true;
	}

	public int hashCode() {
		if (getRoomType() == null || getDepartment() == null) return super.hashCode();
		return getRoomType().hashCode() ^ getDepartment().hashCode();
	}

	public String toString() {
		return "RoomTypeOption[" + getRoomType() + ", " + getDepartment() + "]";
	}

	public String toDebugString() {
		return "RoomTypeOption[" +
			"\n	BreakTime: " + getBreakTime() +
			"\n	Department: " + getDepartment() +
			"\n	Message: " + getMessage() +
			"\n	RoomType: " + getRoomType() +
			"\n	Status: " + getStatus() +
			"]";
	}
}
