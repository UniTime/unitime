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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.RoomTypeOption;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
@IdClass(RoomTypeOptionId.class)
public abstract class BaseRoomTypeOption implements Serializable {
	private static final long serialVersionUID = 1L;

	private RoomType iRoomType;
	private Department iDepartment;
	private Integer iStatus;
	private String iMessage;
	private Integer iBreakTime;


	public BaseRoomTypeOption() {
	}


	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "room_type")
	public RoomType getRoomType() { return iRoomType; }
	public void setRoomType(RoomType roomType) { iRoomType = roomType; }

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "department_id")
	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	@Column(name = "status", nullable = false, length = 10)
	public Integer getStatus() { return iStatus; }
	public void setStatus(Integer status) { iStatus = status; }

	@Column(name = "message", nullable = true, length = 2048)
	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	@Column(name = "break_time", nullable = false)
	public Integer getBreakTime() { return iBreakTime; }
	public void setBreakTime(Integer breakTime) { iBreakTime = breakTime; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomTypeOption)) return false;
		RoomTypeOption roomTypeOption = (RoomTypeOption)o;
		if (getRoomType() == null || roomTypeOption.getRoomType() == null || !getRoomType().equals(roomTypeOption.getRoomType())) return false;
		if (getDepartment() == null || roomTypeOption.getDepartment() == null || !getDepartment().equals(roomTypeOption.getDepartment())) return false;
		return true;
	}

	@Override
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
