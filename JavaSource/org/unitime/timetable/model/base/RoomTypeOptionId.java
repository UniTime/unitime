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

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public class RoomTypeOptionId implements Serializable {
	private static final long serialVersionUID = 1L;

	private RoomType iRoomType;
	private Department iDepartment;

	public RoomTypeOptionId() {}

	public RoomTypeOptionId(RoomType roomType, Department department) {
		iRoomType = roomType;
		iDepartment = department;
	}

	public RoomType getRoomType() { return iRoomType; }
	public void setRoomType(RoomType roomType) { iRoomType = roomType; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }


	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomTypeOptionId)) return false;
		RoomTypeOptionId roomTypeOption = (RoomTypeOptionId)o;
		if (getRoomType() == null || roomTypeOption.getRoomType() == null || !getRoomType().equals(roomTypeOption.getRoomType())) return false;
		if (getDepartment() == null || roomTypeOption.getDepartment() == null || !getDepartment().equals(roomTypeOption.getDepartment())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getRoomType() == null || getDepartment() == null) return super.hashCode();
		return getRoomType().hashCode() ^ getDepartment().hashCode();
	}

}
