/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomPicture;
import org.unitime.timetable.model.RoomType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseRoom extends Location implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iBuildingAbbv;
	private String iRoomNumber;
	private String iClassification;

	private RoomType iRoomType;
	private Building iBuilding;
	private Set<RoomPicture> iPictures;

	public static String PROP_ROOM_NUMBER = "roomNumber";
	public static String PROP_CLASSIFICATION = "classification";

	public BaseRoom() {
		initialize();
	}

	public BaseRoom(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getBuildingAbbv() { return iBuildingAbbv; }
	public void setBuildingAbbv(String buildingAbbv) { iBuildingAbbv = buildingAbbv; }

	public String getRoomNumber() { return iRoomNumber; }
	public void setRoomNumber(String roomNumber) { iRoomNumber = roomNumber; }

	public String getClassification() { return iClassification; }
	public void setClassification(String classification) { iClassification = classification; }

	public RoomType getRoomType() { return iRoomType; }
	public void setRoomType(RoomType roomType) { iRoomType = roomType; }

	public Building getBuilding() { return iBuilding; }
	public void setBuilding(Building building) { iBuilding = building; }

	public Set<RoomPicture> getPictures() { return iPictures; }
	public void setPictures(Set<RoomPicture> pictures) { iPictures = pictures; }
	public void addTopictures(RoomPicture roomPicture) {
		if (iPictures == null) iPictures = new HashSet<RoomPicture>();
		iPictures.add(roomPicture);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Room)) return false;
		if (getUniqueId() == null || ((Room)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Room)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
