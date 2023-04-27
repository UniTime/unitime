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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomDept;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseRoomDept implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Boolean iControl;

	private PreferenceLevel iPreference;
	private Location iRoom;
	private Department iDepartment;

	public BaseRoomDept() {
	}

	public BaseRoomDept(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "room_dept_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "room_sharing_group_seq")
	})
	@GeneratedValue(generator = "room_dept_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "is_control", nullable = false)
	public Boolean isControl() { return iControl; }
	@Transient
	public Boolean getControl() { return iControl; }
	public void setControl(Boolean control) { iControl = control; }

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinFormula("(select p.pref_level_id from %SCHEMA%.room_pref p where p.owner_id = department_id and p.room_id = room_id)")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public PreferenceLevel getPreference() { return iPreference; }
	public void setPreference(PreferenceLevel preference) { iPreference = preference; }

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "room_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Location getRoom() { return iRoom; }
	public void setRoom(Location room) { iRoom = room; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "department_id", nullable = false)
	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof RoomDept)) return false;
		if (getUniqueId() == null || ((RoomDept)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RoomDept)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
