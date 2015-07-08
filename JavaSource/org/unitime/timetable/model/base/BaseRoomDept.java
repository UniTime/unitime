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
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomDept;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseRoomDept implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Boolean iControl;

	private PreferenceLevel iPreference;
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

	public PreferenceLevel getPreference() { return iPreference; }
	public void setPreference(PreferenceLevel preference) { iPreference = preference; }

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
