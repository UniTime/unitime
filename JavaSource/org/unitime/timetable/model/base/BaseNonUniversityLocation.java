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
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.NonUniversityLocationPicture;
import org.unitime.timetable.model.RoomType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseNonUniversityLocation extends Location implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iName;

	private RoomType iRoomType;
	private Set<NonUniversityLocationPicture> iPictures;
	private Set<EventServiceProvider> iAllowedServices;

	public BaseNonUniversityLocation() {
	}

	public BaseNonUniversityLocation(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "name", nullable = false, length = 40)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "room_type", nullable = false)
	public RoomType getRoomType() { return iRoomType; }
	public void setRoomType(RoomType roomType) { iRoomType = roomType; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "location", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<NonUniversityLocationPicture> getPictures() { return iPictures; }
	public void setPictures(Set<NonUniversityLocationPicture> pictures) { iPictures = pictures; }
	public void addTopictures(NonUniversityLocationPicture nonUniversityLocationPicture) {
		if (iPictures == null) iPictures = new HashSet<NonUniversityLocationPicture>();
		iPictures.add(nonUniversityLocationPicture);
	}

	@ManyToMany
	@JoinTable(name = "location_service_provider",
		joinColumns = { @JoinColumn(name = "location_id") },
		inverseJoinColumns = { @JoinColumn(name = "provider_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<EventServiceProvider> getAllowedServices() { return iAllowedServices; }
	public void setAllowedServices(Set<EventServiceProvider> allowedServices) { iAllowedServices = allowedServices; }
	public void addToallowedServices(EventServiceProvider eventServiceProvider) {
		if (iAllowedServices == null) iAllowedServices = new HashSet<EventServiceProvider>();
		iAllowedServices.add(eventServiceProvider);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof NonUniversityLocation)) return false;
		if (getUniqueId() == null || ((NonUniversityLocation)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((NonUniversityLocation)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "NonUniversityLocation["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "NonUniversityLocation[" +
			"\n	Area: " + getArea() +
			"\n	BreakTime: " + getBreakTime() +
			"\n	Capacity: " + getCapacity() +
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
			"\n	Name: " + getName() +
			"\n	Note: " + getNote() +
			"\n	Pattern: " + getPattern() +
			"\n	PermanentId: " + getPermanentId() +
			"\n	RoomType: " + getRoomType() +
			"\n	Session: " + getSession() +
			"\n	ShareNote: " + getShareNote() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
