/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;

public abstract class BaseLocation implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iPermanentId;
	private Integer iCapacity;
	private Double iCoordinateX;
	private Double iCoordinateY;
	private Boolean iIgnoreTooFar;
	private Boolean iIgnoreRoomCheck;
	private String iManagerIds;
	private String iPattern;
	private Integer iExamType;
	private Integer iExamCapacity;
	private String iDisplayName;

	private Session iSession;
	private Set<RoomFeature> iFeatures;
	private Set<ExamLocationPref> iExamPreferences;
	private Set<Assignment> iAssignments;
	private Set<RoomGroup> iRoomGroups;
	private Set<RoomDept> iRoomDepts;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_PERMANENT_ID = "permanentId";
	public static String PROP_CAPACITY = "capacity";
	public static String PROP_COORDINATE_X = "coordinateX";
	public static String PROP_COORDINATE_Y = "coordinateY";
	public static String PROP_IGNORE_TOO_FAR = "ignoreTooFar";
	public static String PROP_IGNORE_ROOM_CHECK = "ignoreRoomCheck";
	public static String PROP_MANAGER_IDS = "managerIds";
	public static String PROP_PATTERN = "pattern";
	public static String PROP_EXAM_TYPE = "examType";
	public static String PROP_EXAM_CAPACITY = "examCapacity";
	public static String PROP_DISPLAY_NAME = "displayName";

	public BaseLocation() {
		initialize();
	}

	public BaseLocation(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getPermanentId() { return iPermanentId; }
	public void setPermanentId(Long permanentId) { iPermanentId = permanentId; }

	public Integer getCapacity() { return iCapacity; }
	public void setCapacity(Integer capacity) { iCapacity = capacity; }

	public Double getCoordinateX() { return iCoordinateX; }
	public void setCoordinateX(Double coordinateX) { iCoordinateX = coordinateX; }

	public Double getCoordinateY() { return iCoordinateY; }
	public void setCoordinateY(Double coordinateY) { iCoordinateY = coordinateY; }

	public Boolean isIgnoreTooFar() { return iIgnoreTooFar; }
	public Boolean getIgnoreTooFar() { return iIgnoreTooFar; }
	public void setIgnoreTooFar(Boolean ignoreTooFar) { iIgnoreTooFar = ignoreTooFar; }

	public Boolean isIgnoreRoomCheck() { return iIgnoreRoomCheck; }
	public Boolean getIgnoreRoomCheck() { return iIgnoreRoomCheck; }
	public void setIgnoreRoomCheck(Boolean ignoreRoomCheck) { iIgnoreRoomCheck = ignoreRoomCheck; }

	public String getManagerIds() { return iManagerIds; }
	public void setManagerIds(String managerIds) { iManagerIds = managerIds; }

	public String getPattern() { return iPattern; }
	public void setPattern(String pattern) { iPattern = pattern; }

	public Integer getExamType() { return iExamType; }
	public void setExamType(Integer examType) { iExamType = examType; }

	public Integer getExamCapacity() { return iExamCapacity; }
	public void setExamCapacity(Integer examCapacity) { iExamCapacity = examCapacity; }

	public String getDisplayName() { return iDisplayName; }
	public void setDisplayName(String displayName) { iDisplayName = displayName; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<RoomFeature> getFeatures() { return iFeatures; }
	public void setFeatures(Set<RoomFeature> features) { iFeatures = features; }
	public void addTofeatures(RoomFeature roomFeature) {
		if (iFeatures == null) iFeatures = new HashSet();
		iFeatures.add(roomFeature);
	}

	public Set<ExamLocationPref> getExamPreferences() { return iExamPreferences; }
	public void setExamPreferences(Set<ExamLocationPref> examPreferences) { iExamPreferences = examPreferences; }
	public void addToexamPreferences(ExamLocationPref examLocationPref) {
		if (iExamPreferences == null) iExamPreferences = new HashSet();
		iExamPreferences.add(examLocationPref);
	}

	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToassignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet();
		iAssignments.add(assignment);
	}

	public Set<RoomGroup> getRoomGroups() { return iRoomGroups; }
	public void setRoomGroups(Set<RoomGroup> roomGroups) { iRoomGroups = roomGroups; }
	public void addToroomGroups(RoomGroup roomGroup) {
		if (iRoomGroups == null) iRoomGroups = new HashSet();
		iRoomGroups.add(roomGroup);
	}

	public Set<RoomDept> getRoomDepts() { return iRoomDepts; }
	public void setRoomDepts(Set<RoomDept> roomDepts) { iRoomDepts = roomDepts; }
	public void addToroomDepts(RoomDept roomDept) {
		if (iRoomDepts == null) iRoomDepts = new HashSet();
		iRoomDepts.add(roomDept);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Location)) return false;
		if (getUniqueId() == null || ((Location)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Location)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Location["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Location[" +
			"\n	Capacity: " + getCapacity() +
			"\n	CoordinateX: " + getCoordinateX() +
			"\n	CoordinateY: " + getCoordinateY() +
			"\n	DisplayName: " + getDisplayName() +
			"\n	ExamCapacity: " + getExamCapacity() +
			"\n	ExamType: " + getExamType() +
			"\n	IgnoreRoomCheck: " + getIgnoreRoomCheck() +
			"\n	IgnoreTooFar: " + getIgnoreTooFar() +
			"\n	ManagerIds: " + getManagerIds() +
			"\n	Pattern: " + getPattern() +
			"\n	PermanentId: " + getPermanentId() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
