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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
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
@MappedSuperclass
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

	public BaseLocation() {
	}

	public BaseLocation(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "room_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "room_seq")
	})
	@GeneratedValue(generator = "room_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "permanent_id", nullable = false, length = 20)
	public Long getPermanentId() { return iPermanentId; }
	public void setPermanentId(Long permanentId) { iPermanentId = permanentId; }

	@Column(name = "capacity", nullable = false, length = 6)
	public Integer getCapacity() { return iCapacity; }
	public void setCapacity(Integer capacity) { iCapacity = capacity; }

	@Column(name = "coordinate_x", nullable = true)
	public Double getCoordinateX() { return iCoordinateX; }
	public void setCoordinateX(Double coordinateX) { iCoordinateX = coordinateX; }

	@Column(name = "coordinate_y", nullable = true)
	public Double getCoordinateY() { return iCoordinateY; }
	public void setCoordinateY(Double coordinateY) { iCoordinateY = coordinateY; }

	@Column(name = "ignore_too_far", nullable = false)
	public Boolean isIgnoreTooFar() { return iIgnoreTooFar; }
	@Transient
	public Boolean getIgnoreTooFar() { return iIgnoreTooFar; }
	public void setIgnoreTooFar(Boolean ignoreTooFar) { iIgnoreTooFar = ignoreTooFar; }

	@Column(name = "ignore_room_check", nullable = false)
	public Boolean isIgnoreRoomCheck() { return iIgnoreRoomCheck; }
	@Transient
	public Boolean getIgnoreRoomCheck() { return iIgnoreRoomCheck; }
	public void setIgnoreRoomCheck(Boolean ignoreRoomCheck) { iIgnoreRoomCheck = ignoreRoomCheck; }

	@Column(name = "area", nullable = true)
	public Double getArea() { return iArea; }
	public void setArea(Double area) { iArea = area; }

	@Column(name = "event_status", nullable = true)
	public Integer getEventStatus() { return iEventStatus; }
	public void setEventStatus(Integer eventStatus) { iEventStatus = eventStatus; }

	@Column(name = "note", nullable = true, length = 2048)
	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	@Column(name = "break_time", nullable = true)
	public Integer getBreakTime() { return iBreakTime; }
	public void setBreakTime(Integer breakTime) { iBreakTime = breakTime; }

	@Column(name = "manager_ids", nullable = true, length = 3000)
	public String getManagerIds() { return iManagerIds; }
	public void setManagerIds(String managerIds) { iManagerIds = managerIds; }

	@Column(name = "pattern", nullable = true, length = 2016)
	public String getPattern() { return iPattern; }
	public void setPattern(String pattern) { iPattern = pattern; }

	@Column(name = "share_note", nullable = true, length = 2048)
	public String getShareNote() { return iShareNote; }
	public void setShareNote(String shareNote) { iShareNote = shareNote; }

	@Column(name = "availability", nullable = true, length = 2016)
	public String getEventAvailability() { return iEventAvailability; }
	public void setEventAvailability(String eventAvailability) { iEventAvailability = eventAvailability; }

	@Column(name = "exam_capacity", nullable = true, length = 10)
	public Integer getExamCapacity() { return iExamCapacity; }
	public void setExamCapacity(Integer examCapacity) { iExamCapacity = examCapacity; }

	@Column(name = "display_name", nullable = true, length = 100)
	public String getDisplayName() { return iDisplayName; }
	public void setDisplayName(String displayName) { iDisplayName = displayName; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "event_dept_id", nullable = true)
	public Department getEventDepartment() { return iEventDepartment; }
	public void setEventDepartment(Department eventDepartment) { iEventDepartment = eventDepartment; }

	@ManyToMany
	@JoinTable(name = "room_join_room_feature",
		joinColumns = { @JoinColumn(name = "room_id") },
		inverseJoinColumns = { @JoinColumn(name = "feature_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<RoomFeature> getFeatures() { return iFeatures; }
	public void setFeatures(Set<RoomFeature> features) { iFeatures = features; }
	public void addTofeatures(RoomFeature roomFeature) {
		if (iFeatures == null) iFeatures = new HashSet<RoomFeature>();
		iFeatures.add(roomFeature);
	}

	@ManyToMany
	@JoinTable(name = "room_exam_type",
		joinColumns = { @JoinColumn(name = "location_id") },
		inverseJoinColumns = { @JoinColumn(name = "exam_type_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<ExamType> getExamTypes() { return iExamTypes; }
	public void setExamTypes(Set<ExamType> examTypes) { iExamTypes = examTypes; }
	public void addToexamTypes(ExamType examType) {
		if (iExamTypes == null) iExamTypes = new HashSet<ExamType>();
		iExamTypes.add(examType);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "location", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<ExamLocationPref> getExamPreferences() { return iExamPreferences; }
	public void setExamPreferences(Set<ExamLocationPref> examPreferences) { iExamPreferences = examPreferences; }
	public void addToexamPreferences(ExamLocationPref examLocationPref) {
		if (iExamPreferences == null) iExamPreferences = new HashSet<ExamLocationPref>();
		iExamPreferences.add(examLocationPref);
	}

	@ManyToMany(mappedBy = "rooms")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToassignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet<Assignment>();
		iAssignments.add(assignment);
	}

	@ManyToMany
	@JoinTable(name = "room_group_room",
		joinColumns = { @JoinColumn(name = "room_id") },
		inverseJoinColumns = { @JoinColumn(name = "room_group_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<RoomGroup> getRoomGroups() { return iRoomGroups; }
	public void setRoomGroups(Set<RoomGroup> roomGroups) { iRoomGroups = roomGroups; }
	public void addToroomGroups(RoomGroup roomGroup) {
		if (iRoomGroups == null) iRoomGroups = new HashSet<RoomGroup>();
		iRoomGroups.add(roomGroup);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "room", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<RoomDept> getRoomDepts() { return iRoomDepts; }
	public void setRoomDepts(Set<RoomDept> roomDepts) { iRoomDepts = roomDepts; }
	public void addToroomDepts(RoomDept roomDept) {
		if (iRoomDepts == null) iRoomDepts = new HashSet<RoomDept>();
		iRoomDepts.add(roomDept);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Location)) return false;
		if (getUniqueId() == null || ((Location)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Location)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
