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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExternalDepartmentStatusType;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimetableManager;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseDepartment extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iExternalUniqueId;
	private String iDeptCode;
	private String iAbbreviation;
	private String iName;
	private Boolean iAllowReqTime;
	private Boolean iAllowReqRoom;
	private Boolean iAllowReqDistribution;
	private Boolean iAllowEvents;
	private Boolean iAllowStudentScheduling;
	private Boolean iInheritInstructorPreferences;
	private String iRoomSharingColor;
	private Boolean iExternalManager;
	private String iExternalMgrLabel;
	private String iExternalMgrAbbv;
	private Integer iDistributionPrefPriority;
	private Boolean iExternalFundingDept;

	private Session iSession;
	private DepartmentStatusType iStatusType;
	private SolverGroup iSolverGroup;
	private Set<SubjectArea> iSubjectAreas;
	private Set<RoomDept> iRoomDepts;
	private Set<DatePattern> iDatePatterns;
	private Set<TimePattern> iTimePatterns;
	private Set<ExternalDepartmentStatusType> iExternalStatusTypes;
	private Set<TimetableManager> iTimetableManagers;
	private Set<DepartmentalInstructor> iInstructors;

	public BaseDepartment() {
	}

	public BaseDepartment(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "dept_code", nullable = false, length = 50)
	public String getDeptCode() { return iDeptCode; }
	public void setDeptCode(String deptCode) { iDeptCode = deptCode; }

	@Column(name = "abbreviation", nullable = true, length = 20)
	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }

	@Column(name = "name", nullable = false, length = 100)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "allow_req_time", nullable = false)
	public Boolean isAllowReqTime() { return iAllowReqTime; }
	@Transient
	public Boolean getAllowReqTime() { return iAllowReqTime; }
	public void setAllowReqTime(Boolean allowReqTime) { iAllowReqTime = allowReqTime; }

	@Column(name = "allow_req_room", nullable = false)
	public Boolean isAllowReqRoom() { return iAllowReqRoom; }
	@Transient
	public Boolean getAllowReqRoom() { return iAllowReqRoom; }
	public void setAllowReqRoom(Boolean allowReqRoom) { iAllowReqRoom = allowReqRoom; }

	@Column(name = "allow_req_dist", nullable = false)
	public Boolean isAllowReqDistribution() { return iAllowReqDistribution; }
	@Transient
	public Boolean getAllowReqDistribution() { return iAllowReqDistribution; }
	public void setAllowReqDistribution(Boolean allowReqDistribution) { iAllowReqDistribution = allowReqDistribution; }

	@Column(name = "allow_events", nullable = false)
	public Boolean isAllowEvents() { return iAllowEvents; }
	@Transient
	public Boolean getAllowEvents() { return iAllowEvents; }
	public void setAllowEvents(Boolean allowEvents) { iAllowEvents = allowEvents; }

	@Column(name = "allow_student_schd", nullable = false)
	public Boolean isAllowStudentScheduling() { return iAllowStudentScheduling; }
	@Transient
	public Boolean getAllowStudentScheduling() { return iAllowStudentScheduling; }
	public void setAllowStudentScheduling(Boolean allowStudentScheduling) { iAllowStudentScheduling = allowStudentScheduling; }

	@Column(name = "instructor_pref", nullable = false)
	public Boolean isInheritInstructorPreferences() { return iInheritInstructorPreferences; }
	@Transient
	public Boolean getInheritInstructorPreferences() { return iInheritInstructorPreferences; }
	public void setInheritInstructorPreferences(Boolean inheritInstructorPreferences) { iInheritInstructorPreferences = inheritInstructorPreferences; }

	@Column(name = "rs_color", nullable = true, length = 6)
	public String getRoomSharingColor() { return iRoomSharingColor; }
	public void setRoomSharingColor(String roomSharingColor) { iRoomSharingColor = roomSharingColor; }

	@Column(name = "external_manager", nullable = false)
	public Boolean isExternalManager() { return iExternalManager; }
	@Transient
	public Boolean getExternalManager() { return iExternalManager; }
	public void setExternalManager(Boolean externalManager) { iExternalManager = externalManager; }

	@Column(name = "external_mgr_label", nullable = true, length = 30)
	public String getExternalMgrLabel() { return iExternalMgrLabel; }
	public void setExternalMgrLabel(String externalMgrLabel) { iExternalMgrLabel = externalMgrLabel; }

	@Column(name = "external_mgr_abbv", nullable = true, length = 10)
	public String getExternalMgrAbbv() { return iExternalMgrAbbv; }
	public void setExternalMgrAbbv(String externalMgrAbbv) { iExternalMgrAbbv = externalMgrAbbv; }

	@Column(name = "dist_priority", nullable = false)
	public Integer getDistributionPrefPriority() { return iDistributionPrefPriority; }
	public void setDistributionPrefPriority(Integer distributionPrefPriority) { iDistributionPrefPriority = distributionPrefPriority; }

	@Column(name = "external_funding_dept", nullable = true)
	public Boolean isExternalFundingDept() { return iExternalFundingDept; }
	@Transient
	public Boolean getExternalFundingDept() { return iExternalFundingDept; }
	public void setExternalFundingDept(Boolean externalFundingDept) { iExternalFundingDept = externalFundingDept; }

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "status_type", nullable = true)
	public DepartmentStatusType getStatusType() { return iStatusType; }
	public void setStatusType(DepartmentStatusType statusType) { iStatusType = statusType; }

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "solver_group_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public SolverGroup getSolverGroup() { return iSolverGroup; }
	public void setSolverGroup(SolverGroup solverGroup) { iSolverGroup = solverGroup; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "department", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<SubjectArea> getSubjectAreas() { return iSubjectAreas; }
	public void setSubjectAreas(Set<SubjectArea> subjectAreas) { iSubjectAreas = subjectAreas; }
	public void addToSubjectAreas(SubjectArea subjectArea) {
		if (iSubjectAreas == null) iSubjectAreas = new HashSet<SubjectArea>();
		iSubjectAreas.add(subjectArea);
	}
	@Deprecated
	public void addTosubjectAreas(SubjectArea subjectArea) {
		addToSubjectAreas(subjectArea);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "department", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<RoomDept> getRoomDepts() { return iRoomDepts; }
	public void setRoomDepts(Set<RoomDept> roomDepts) { iRoomDepts = roomDepts; }
	public void addToRoomDepts(RoomDept roomDept) {
		if (iRoomDepts == null) iRoomDepts = new HashSet<RoomDept>();
		iRoomDepts.add(roomDept);
	}
	@Deprecated
	public void addToroomDepts(RoomDept roomDept) {
		addToRoomDepts(roomDept);
	}

	@ManyToMany
	@JoinTable(name = "date_pattern_dept",
		joinColumns = { @JoinColumn(name = "dept_id") },
		inverseJoinColumns = { @JoinColumn(name = "pattern_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<DatePattern> getDatePatterns() { return iDatePatterns; }
	public void setDatePatterns(Set<DatePattern> datePatterns) { iDatePatterns = datePatterns; }
	public void addToDatePatterns(DatePattern datePattern) {
		if (iDatePatterns == null) iDatePatterns = new HashSet<DatePattern>();
		iDatePatterns.add(datePattern);
	}
	@Deprecated
	public void addTodatePatterns(DatePattern datePattern) {
		addToDatePatterns(datePattern);
	}

	@ManyToMany
	@JoinTable(name = "time_pattern_dept",
		joinColumns = { @JoinColumn(name = "dept_id") },
		inverseJoinColumns = { @JoinColumn(name = "pattern_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<TimePattern> getTimePatterns() { return iTimePatterns; }
	public void setTimePatterns(Set<TimePattern> timePatterns) { iTimePatterns = timePatterns; }
	public void addToTimePatterns(TimePattern timePattern) {
		if (iTimePatterns == null) iTimePatterns = new HashSet<TimePattern>();
		iTimePatterns.add(timePattern);
	}
	@Deprecated
	public void addTotimePatterns(TimePattern timePattern) {
		addToTimePatterns(timePattern);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "externalDepartment", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<ExternalDepartmentStatusType> getExternalStatusTypes() { return iExternalStatusTypes; }
	public void setExternalStatusTypes(Set<ExternalDepartmentStatusType> externalStatusTypes) { iExternalStatusTypes = externalStatusTypes; }
	public void addToExternalStatusTypes(ExternalDepartmentStatusType externalDepartmentStatusType) {
		if (iExternalStatusTypes == null) iExternalStatusTypes = new HashSet<ExternalDepartmentStatusType>();
		iExternalStatusTypes.add(externalDepartmentStatusType);
	}
	@Deprecated
	public void addToexternalStatusTypes(ExternalDepartmentStatusType externalDepartmentStatusType) {
		addToExternalStatusTypes(externalDepartmentStatusType);
	}

	@ManyToMany
	@JoinTable(name = "dept_to_tt_mgr",
		joinColumns = { @JoinColumn(name = "department_id") },
		inverseJoinColumns = { @JoinColumn(name = "timetable_mgr_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<TimetableManager> getTimetableManagers() { return iTimetableManagers; }
	public void setTimetableManagers(Set<TimetableManager> timetableManagers) { iTimetableManagers = timetableManagers; }
	public void addToTimetableManagers(TimetableManager timetableManager) {
		if (iTimetableManagers == null) iTimetableManagers = new HashSet<TimetableManager>();
		iTimetableManagers.add(timetableManager);
	}
	@Deprecated
	public void addTotimetableManagers(TimetableManager timetableManager) {
		addToTimetableManagers(timetableManager);
	}

	@OneToMany(mappedBy = "department", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<DepartmentalInstructor> getInstructors() { return iInstructors; }
	public void setInstructors(Set<DepartmentalInstructor> instructors) { iInstructors = instructors; }
	public void addToInstructors(DepartmentalInstructor departmentalInstructor) {
		if (iInstructors == null) iInstructors = new HashSet<DepartmentalInstructor>();
		iInstructors.add(departmentalInstructor);
	}
	@Deprecated
	public void addToinstructors(DepartmentalInstructor departmentalInstructor) {
		addToInstructors(departmentalInstructor);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Department)) return false;
		if (getUniqueId() == null || ((Department)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Department)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Department["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "Department[" +
			"\n	Abbreviation: " + getAbbreviation() +
			"\n	AllowEvents: " + getAllowEvents() +
			"\n	AllowReqDistribution: " + getAllowReqDistribution() +
			"\n	AllowReqRoom: " + getAllowReqRoom() +
			"\n	AllowReqTime: " + getAllowReqTime() +
			"\n	AllowStudentScheduling: " + getAllowStudentScheduling() +
			"\n	DeptCode: " + getDeptCode() +
			"\n	DistributionPrefPriority: " + getDistributionPrefPriority() +
			"\n	ExternalFundingDept: " + getExternalFundingDept() +
			"\n	ExternalManager: " + getExternalManager() +
			"\n	ExternalMgrAbbv: " + getExternalMgrAbbv() +
			"\n	ExternalMgrLabel: " + getExternalMgrLabel() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	InheritInstructorPreferences: " + getInheritInstructorPreferences() +
			"\n	Name: " + getName() +
			"\n	RoomSharingColor: " + getRoomSharingColor() +
			"\n	Session: " + getSession() +
			"\n	SolverGroup: " + getSolverGroup() +
			"\n	StatusType: " + getStatusType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
