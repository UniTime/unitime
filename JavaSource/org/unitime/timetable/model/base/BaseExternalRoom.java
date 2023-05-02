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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.model.ExternalRoomFeature;
import org.unitime.timetable.model.RoomType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseExternalRoom implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iRoomNumber;
	private Double iCoordinateX;
	private Double iCoordinateY;
	private Integer iCapacity;
	private Integer iExamCapacity;
	private String iClassification;
	private Boolean iIsInstructional;
	private String iDisplayName;
	private Double iArea;

	private RoomType iRoomType;
	private ExternalBuilding iBuilding;
	private Set<ExternalRoomDepartment> iRoomDepartments;
	private Set<ExternalRoomFeature> iRoomFeatures;

	public BaseExternalRoom() {
	}

	public BaseExternalRoom(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "external_room_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "external_room_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "room_number", nullable = false, length = 40)
	public String getRoomNumber() { return iRoomNumber; }
	public void setRoomNumber(String roomNumber) { iRoomNumber = roomNumber; }

	@Column(name = "coordinate_x", nullable = true)
	public Double getCoordinateX() { return iCoordinateX; }
	public void setCoordinateX(Double coordinateX) { iCoordinateX = coordinateX; }

	@Column(name = "coordinate_y", nullable = true)
	public Double getCoordinateY() { return iCoordinateY; }
	public void setCoordinateY(Double coordinateY) { iCoordinateY = coordinateY; }

	@Column(name = "capacity", nullable = false, length = 6)
	public Integer getCapacity() { return iCapacity; }
	public void setCapacity(Integer capacity) { iCapacity = capacity; }

	@Column(name = "exam_capacity", nullable = true, length = 6)
	public Integer getExamCapacity() { return iExamCapacity; }
	public void setExamCapacity(Integer examCapacity) { iExamCapacity = examCapacity; }

	@Column(name = "classification", nullable = false, length = 20)
	public String getClassification() { return iClassification; }
	public void setClassification(String classification) { iClassification = classification; }

	@Column(name = "instructional", nullable = false)
	public Boolean isIsInstructional() { return iIsInstructional; }
	@Transient
	public Boolean getIsInstructional() { return iIsInstructional; }
	public void setIsInstructional(Boolean isInstructional) { iIsInstructional = isInstructional; }

	@Column(name = "display_name", nullable = true, length = 100)
	public String getDisplayName() { return iDisplayName; }
	public void setDisplayName(String displayName) { iDisplayName = displayName; }

	@Column(name = "area", nullable = true)
	public Double getArea() { return iArea; }
	public void setArea(Double area) { iArea = area; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "room_type", nullable = false)
	public RoomType getRoomType() { return iRoomType; }
	public void setRoomType(RoomType roomType) { iRoomType = roomType; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "external_bldg_id", nullable = false)
	public ExternalBuilding getBuilding() { return iBuilding; }
	public void setBuilding(ExternalBuilding building) { iBuilding = building; }

	@OneToMany
	@JoinColumn(name = "external_room_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<ExternalRoomDepartment> getRoomDepartments() { return iRoomDepartments; }
	public void setRoomDepartments(Set<ExternalRoomDepartment> roomDepartments) { iRoomDepartments = roomDepartments; }
	public void addToRoomDepartments(ExternalRoomDepartment externalRoomDepartment) {
		if (iRoomDepartments == null) iRoomDepartments = new HashSet<ExternalRoomDepartment>();
		iRoomDepartments.add(externalRoomDepartment);
	}
	@Deprecated
	public void addToroomDepartments(ExternalRoomDepartment externalRoomDepartment) {
		addToRoomDepartments(externalRoomDepartment);
	}

	@OneToMany
	@JoinColumn(name = "external_room_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<ExternalRoomFeature> getRoomFeatures() { return iRoomFeatures; }
	public void setRoomFeatures(Set<ExternalRoomFeature> roomFeatures) { iRoomFeatures = roomFeatures; }
	public void addToRoomFeatures(ExternalRoomFeature externalRoomFeature) {
		if (iRoomFeatures == null) iRoomFeatures = new HashSet<ExternalRoomFeature>();
		iRoomFeatures.add(externalRoomFeature);
	}
	@Deprecated
	public void addToroomFeatures(ExternalRoomFeature externalRoomFeature) {
		addToRoomFeatures(externalRoomFeature);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExternalRoom)) return false;
		if (getUniqueId() == null || ((ExternalRoom)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExternalRoom)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ExternalRoom["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ExternalRoom[" +
			"\n	Area: " + getArea() +
			"\n	Building: " + getBuilding() +
			"\n	Capacity: " + getCapacity() +
			"\n	Classification: " + getClassification() +
			"\n	CoordinateX: " + getCoordinateX() +
			"\n	CoordinateY: " + getCoordinateY() +
			"\n	DisplayName: " + getDisplayName() +
			"\n	ExamCapacity: " + getExamCapacity() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	IsInstructional: " + getIsInstructional() +
			"\n	RoomNumber: " + getRoomNumber() +
			"\n	RoomType: " + getRoomType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
