/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
