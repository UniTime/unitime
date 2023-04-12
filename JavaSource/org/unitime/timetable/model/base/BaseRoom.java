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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomPicture;
import org.unitime.timetable.model.RoomType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseRoom extends Location implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iBuildingAbbv;
	private String iRoomNumber;
	private String iClassification;

	private RoomType iRoomType;
	private Building iBuilding;
	private Room iParentRoom;
	private Set<RoomPicture> iPictures;
	private Set<EventServiceProvider> iAllowedServices;
	private Set<Room> iPartitions;

	public BaseRoom() {
	}

	public BaseRoom(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Formula(" (select b.abbreviation from %SCHEMA%.building b where b.uniqueid = building_id) ")
	public String getBuildingAbbv() { return iBuildingAbbv; }
	public void setBuildingAbbv(String buildingAbbv) { iBuildingAbbv = buildingAbbv; }

	@Column(name = "room_number", nullable = false, length = 40)
	public String getRoomNumber() { return iRoomNumber; }
	public void setRoomNumber(String roomNumber) { iRoomNumber = roomNumber; }

	@Column(name = "classification", nullable = true, length = 20)
	public String getClassification() { return iClassification; }
	public void setClassification(String classification) { iClassification = classification; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "room_type", nullable = false)
	public RoomType getRoomType() { return iRoomType; }
	public void setRoomType(RoomType roomType) { iRoomType = roomType; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "building_id", nullable = false)
	public Building getBuilding() { return iBuilding; }
	public void setBuilding(Building building) { iBuilding = building; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_room_id", nullable = true)
	public Room getParentRoom() { return iParentRoom; }
	public void setParentRoom(Room parentRoom) { iParentRoom = parentRoom; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "location", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<RoomPicture> getPictures() { return iPictures; }
	public void setPictures(Set<RoomPicture> pictures) { iPictures = pictures; }
	public void addTopictures(RoomPicture roomPicture) {
		if (iPictures == null) iPictures = new HashSet<RoomPicture>();
		iPictures.add(roomPicture);
	}

	@ManyToMany
	@JoinTable(name = "room_service_provider",
		joinColumns = { @JoinColumn(name = "location_id") },
		inverseJoinColumns = { @JoinColumn(name = "provider_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<EventServiceProvider> getAllowedServices() { return iAllowedServices; }
	public void setAllowedServices(Set<EventServiceProvider> allowedServices) { iAllowedServices = allowedServices; }
	public void addToallowedServices(EventServiceProvider eventServiceProvider) {
		if (iAllowedServices == null) iAllowedServices = new HashSet<EventServiceProvider>();
		iAllowedServices.add(eventServiceProvider);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parentRoom")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Room> getPartitions() { return iPartitions; }
	public void setPartitions(Set<Room> partitions) { iPartitions = partitions; }
	public void addTopartitions(Room room) {
		if (iPartitions == null) iPartitions = new HashSet<Room>();
		iPartitions.add(room);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Room)) return false;
		if (getUniqueId() == null || ((Room)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Room)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Room["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Room[" +
			"\n	Area: " + getArea() +
			"\n	BreakTime: " + getBreakTime() +
			"\n	Building: " + getBuilding() +
			"\n	Capacity: " + getCapacity() +
			"\n	Classification: " + getClassification() +
			"\n	CoordinateX: " + getCoordinateX() +
			"\n	CoordinateY: " + getCoordinateY() +
			"\n	DisplayName: " + getDisplayName() +
			"\n	EventAvailability: " + getEventAvailability() +
			"\n	EventDepartment: " + getEventDepartment() +
			"\n	EventStatus: " + getEventStatus() +
			"\n	ExamCapacity: " + getExamCapacity() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	IgnoreRoomCheck: " + getIgnoreRoomCheck() +
			"\n	IgnoreTooFar: " + getIgnoreTooFar() +
			"\n	ManagerIds: " + getManagerIds() +
			"\n	Note: " + getNote() +
			"\n	ParentRoom: " + getParentRoom() +
			"\n	Pattern: " + getPattern() +
			"\n	PermanentId: " + getPermanentId() +
			"\n	RoomNumber: " + getRoomNumber() +
			"\n	RoomType: " + getRoomType() +
			"\n	Session: " + getSession() +
			"\n	ShareNote: " + getShareNote() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
