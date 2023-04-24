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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseExternalRoomDepartment implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iDepartmentCode;
	private Integer iPercent;
	private String iAssignmentType;

	private ExternalRoom iRoom;

	public BaseExternalRoomDepartment() {
	}

	public BaseExternalRoomDepartment(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "external_room_department_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "external_room_department_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "department_code", nullable = false, length = 50)
	public String getDepartmentCode() { return iDepartmentCode; }
	public void setDepartmentCode(String departmentCode) { iDepartmentCode = departmentCode; }

	@Column(name = "percent", nullable = false, length = 3)
	public Integer getPercent() { return iPercent; }
	public void setPercent(Integer percent) { iPercent = percent; }

	@Column(name = "assignment_type", nullable = false, length = 20)
	public String getAssignmentType() { return iAssignmentType; }
	public void setAssignmentType(String assignmentType) { iAssignmentType = assignmentType; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "external_room_id", nullable = false)
	public ExternalRoom getRoom() { return iRoom; }
	public void setRoom(ExternalRoom room) { iRoom = room; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExternalRoomDepartment)) return false;
		if (getUniqueId() == null || ((ExternalRoomDepartment)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExternalRoomDepartment)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
