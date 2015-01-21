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

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseLocation implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iPermanentId;
	private Integer iCapacity;
	private Double iCoordinateX;
	private Double iCoordinateY;
	private Boolean iIgnoreTooFar;
	private Boolean iIgnoreRoomCheck;
	private Double iArea;
	private Integer iEventStatus;
	private String iNote;
	private Integer iBreakTime;
	private String iManagerIds;
	private String iPattern;
	private String iShareNote;
	private String iEventAvailability;
	private Integer iExamCapacity;
	private String iDisplayName;
	private String iExternalUniqueId;

	private Session iSession;
	private Department iEventDepartment;
	private Set<RoomFeature> iFeatures;
	private Set<ExamType> iExamTypes;
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
	public static String PROP_AREA = "area";
	public static String PROP_EVENT_STATUS = "eventStatus";
	public static String PROP_NOTE = "note";
	public static String PROP_BREAK_TIME = "breakTime";
	public static String PROP_MANAGER_IDS = "managerIds";
	public static String PROP_PATTERN = "pattern";
	public static String PROP_SHARE_NOTE = "shareNote";
	public static String PROP_AVAILABILITY = "eventAvailability";
	public static String PROP_EXAM_CAPACITY = "examCapacity";
	public static String PROP_DISPLAY_NAME = "displayName";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";

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

	public Double getArea() { return iArea; }
	public void setArea(Double area) { iArea = area; }

	public Integer getEventStatus() { return iEventStatus; }
	public void setEventStatus(Integer eventStatus) { iEventStatus = eventStatus; }

	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	public Integer getBreakTime() { return iBreakTime; }
	public void setBreakTime(Integer breakTime) { iBreakTime = breakTime; }

	public String getManagerIds() { return iManagerIds; }
	public void setManagerIds(String managerIds) { iManagerIds = managerIds; }

	public String getPattern() { return iPattern; }
	public void setPattern(String pattern) { iPattern = pattern; }

	public String getShareNote() { return iShareNote; }
	public void setShareNote(String shareNote) { iShareNote = shareNote; }

	public String getEventAvailability() { return iEventAvailability; }
	public void setEventAvailability(String eventAvailability) { iEventAvailability = eventAvailability; }

	public Integer getExamCapacity() { return iExamCapacity; }
	public void setExamCapacity(Integer examCapacity) { iExamCapacity = examCapacity; }

	public String getDisplayName() { return iDisplayName; }
	public void setDisplayName(String displayName) { iDisplayName = displayName; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Department getEventDepartment() { return iEventDepartment; }
	public void setEventDepartment(Department eventDepartment) { iEventDepartment = eventDepartment; }

	public Set<RoomFeature> getFeatures() { return iFeatures; }
	public void setFeatures(Set<RoomFeature> features) { iFeatures = features; }
	public void addTofeatures(RoomFeature roomFeature) {
		if (iFeatures == null) iFeatures = new HashSet<RoomFeature>();
		iFeatures.add(roomFeature);
	}

	public Set<ExamType> getExamTypes() { return iExamTypes; }
	public void setExamTypes(Set<ExamType> examTypes) { iExamTypes = examTypes; }
	public void addToexamTypes(ExamType examType) {
		if (iExamTypes == null) iExamTypes = new HashSet<ExamType>();
		iExamTypes.add(examType);
	}

	public Set<ExamLocationPref> getExamPreferences() { return iExamPreferences; }
	public void setExamPreferences(Set<ExamLocationPref> examPreferences) { iExamPreferences = examPreferences; }
	public void addToexamPreferences(ExamLocationPref examLocationPref) {
		if (iExamPreferences == null) iExamPreferences = new HashSet<ExamLocationPref>();
		iExamPreferences.add(examLocationPref);
	}

	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToassignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet<Assignment>();
		iAssignments.add(assignment);
	}

	public Set<RoomGroup> getRoomGroups() { return iRoomGroups; }
	public void setRoomGroups(Set<RoomGroup> roomGroups) { iRoomGroups = roomGroups; }
	public void addToroomGroups(RoomGroup roomGroup) {
		if (iRoomGroups == null) iRoomGroups = new HashSet<RoomGroup>();
		iRoomGroups.add(roomGroup);
	}

	public Set<RoomDept> getRoomDepts() { return iRoomDepts; }
	public void setRoomDepts(Set<RoomDept> roomDepts) { iRoomDepts = roomDepts; }
	public void addToroomDepts(RoomDept roomDept) {
		if (iRoomDepts == null) iRoomDepts = new HashSet<RoomDept>();
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
			"\n	Note: " + getNote() +
			"\n	Pattern: " + getPattern() +
			"\n	PermanentId: " + getPermanentId() +
			"\n	Session: " + getSession() +
			"\n	ShareNote: " + getShareNote() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
