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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.model.ExternalRoomFeature;
import org.unitime.timetable.model.RoomType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_ROOM_NUMBER = "roomNumber";
	public static String PROP_COORDINATE_X = "coordinateX";
	public static String PROP_COORDINATE_Y = "coordinateY";
	public static String PROP_CAPACITY = "capacity";
	public static String PROP_EXAM_CAPACITY = "examCapacity";
	public static String PROP_CLASSIFICATION = "classification";
	public static String PROP_INSTRUCTIONAL = "isInstructional";
	public static String PROP_DISPLAY_NAME = "displayName";
	public static String PROP_AREA = "area";

	public BaseExternalRoom() {
		initialize();
	}

	public BaseExternalRoom(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getRoomNumber() { return iRoomNumber; }
	public void setRoomNumber(String roomNumber) { iRoomNumber = roomNumber; }

	public Double getCoordinateX() { return iCoordinateX; }
	public void setCoordinateX(Double coordinateX) { iCoordinateX = coordinateX; }

	public Double getCoordinateY() { return iCoordinateY; }
	public void setCoordinateY(Double coordinateY) { iCoordinateY = coordinateY; }

	public Integer getCapacity() { return iCapacity; }
	public void setCapacity(Integer capacity) { iCapacity = capacity; }

	public Integer getExamCapacity() { return iExamCapacity; }
	public void setExamCapacity(Integer examCapacity) { iExamCapacity = examCapacity; }

	public String getClassification() { return iClassification; }
	public void setClassification(String classification) { iClassification = classification; }

	public Boolean isIsInstructional() { return iIsInstructional; }
	public Boolean getIsInstructional() { return iIsInstructional; }
	public void setIsInstructional(Boolean isInstructional) { iIsInstructional = isInstructional; }

	public String getDisplayName() { return iDisplayName; }
	public void setDisplayName(String displayName) { iDisplayName = displayName; }

	public Double getArea() { return iArea; }
	public void setArea(Double area) { iArea = area; }

	public RoomType getRoomType() { return iRoomType; }
	public void setRoomType(RoomType roomType) { iRoomType = roomType; }

	public ExternalBuilding getBuilding() { return iBuilding; }
	public void setBuilding(ExternalBuilding building) { iBuilding = building; }

	public Set<ExternalRoomDepartment> getRoomDepartments() { return iRoomDepartments; }
	public void setRoomDepartments(Set<ExternalRoomDepartment> roomDepartments) { iRoomDepartments = roomDepartments; }
	public void addToroomDepartments(ExternalRoomDepartment externalRoomDepartment) {
		if (iRoomDepartments == null) iRoomDepartments = new HashSet<ExternalRoomDepartment>();
		iRoomDepartments.add(externalRoomDepartment);
	}

	public Set<ExternalRoomFeature> getRoomFeatures() { return iRoomFeatures; }
	public void setRoomFeatures(Set<ExternalRoomFeature> roomFeatures) { iRoomFeatures = roomFeatures; }
	public void addToroomFeatures(ExternalRoomFeature externalRoomFeature) {
		if (iRoomFeatures == null) iRoomFeatures = new HashSet<ExternalRoomFeature>();
		iRoomFeatures.add(externalRoomFeature);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExternalRoom)) return false;
		if (getUniqueId() == null || ((ExternalRoom)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExternalRoom)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
