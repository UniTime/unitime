/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimetableManager;

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
	private String iRoomSharingColor;
	private Boolean iExternalManager;
	private String iExternalMgrLabel;
	private String iExternalMgrAbbv;
	private Integer iDistributionPrefPriority;

	private Session iSession;
	private DepartmentStatusType iStatusType;
	private SolverGroup iSolverGroup;
	private Set<SubjectArea> iSubjectAreas;
	private Set<RoomDept> iRoomDepts;
	private Set<DatePattern> iDatePatterns;
	private Set<TimePattern> iTimePatterns;
	private Set<TimetableManager> iTimetableManagers;
	private Set<DepartmentalInstructor> iInstructors;

	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_DEPT_CODE = "deptCode";
	public static String PROP_ABBREVIATION = "abbreviation";
	public static String PROP_NAME = "name";
	public static String PROP_ALLOW_REQ_TIME = "allowReqTime";
	public static String PROP_ALLOW_REQ_ROOM = "allowReqRoom";
	public static String PROP_ALLOW_REQ_DIST = "allowReqDistribution";
	public static String PROP_ALLOW_EVENTS = "allowEvents";
	public static String PROP_RS_COLOR = "roomSharingColor";
	public static String PROP_EXTERNAL_MANAGER = "externalManager";
	public static String PROP_EXTERNAL_MGR_LABEL = "externalMgrLabel";
	public static String PROP_EXTERNAL_MGR_ABBV = "externalMgrAbbv";
	public static String PROP_DIST_PRIORITY = "distributionPrefPriority";

	public BaseDepartment() {
		initialize();
	}

	public BaseDepartment(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getDeptCode() { return iDeptCode; }
	public void setDeptCode(String deptCode) { iDeptCode = deptCode; }

	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public Boolean isAllowReqTime() { return iAllowReqTime; }
	public Boolean getAllowReqTime() { return iAllowReqTime; }
	public void setAllowReqTime(Boolean allowReqTime) { iAllowReqTime = allowReqTime; }

	public Boolean isAllowReqRoom() { return iAllowReqRoom; }
	public Boolean getAllowReqRoom() { return iAllowReqRoom; }
	public void setAllowReqRoom(Boolean allowReqRoom) { iAllowReqRoom = allowReqRoom; }

	public Boolean isAllowReqDistribution() { return iAllowReqDistribution; }
	public Boolean getAllowReqDistribution() { return iAllowReqDistribution; }
	public void setAllowReqDistribution(Boolean allowReqDistribution) { iAllowReqDistribution = allowReqDistribution; }

	public Boolean isAllowEvents() { return iAllowEvents; }
	public Boolean getAllowEvents() { return iAllowEvents; }
	public void setAllowEvents(Boolean allowEvents) { iAllowEvents = allowEvents; }

	public String getRoomSharingColor() { return iRoomSharingColor; }
	public void setRoomSharingColor(String roomSharingColor) { iRoomSharingColor = roomSharingColor; }

	public Boolean isExternalManager() { return iExternalManager; }
	public Boolean getExternalManager() { return iExternalManager; }
	public void setExternalManager(Boolean externalManager) { iExternalManager = externalManager; }

	public String getExternalMgrLabel() { return iExternalMgrLabel; }
	public void setExternalMgrLabel(String externalMgrLabel) { iExternalMgrLabel = externalMgrLabel; }

	public String getExternalMgrAbbv() { return iExternalMgrAbbv; }
	public void setExternalMgrAbbv(String externalMgrAbbv) { iExternalMgrAbbv = externalMgrAbbv; }

	public Integer getDistributionPrefPriority() { return iDistributionPrefPriority; }
	public void setDistributionPrefPriority(Integer distributionPrefPriority) { iDistributionPrefPriority = distributionPrefPriority; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public DepartmentStatusType getStatusType() { return iStatusType; }
	public void setStatusType(DepartmentStatusType statusType) { iStatusType = statusType; }

	public SolverGroup getSolverGroup() { return iSolverGroup; }
	public void setSolverGroup(SolverGroup solverGroup) { iSolverGroup = solverGroup; }

	public Set<SubjectArea> getSubjectAreas() { return iSubjectAreas; }
	public void setSubjectAreas(Set<SubjectArea> subjectAreas) { iSubjectAreas = subjectAreas; }
	public void addTosubjectAreas(SubjectArea subjectArea) {
		if (iSubjectAreas == null) iSubjectAreas = new HashSet<SubjectArea>();
		iSubjectAreas.add(subjectArea);
	}

	public Set<RoomDept> getRoomDepts() { return iRoomDepts; }
	public void setRoomDepts(Set<RoomDept> roomDepts) { iRoomDepts = roomDepts; }
	public void addToroomDepts(RoomDept roomDept) {
		if (iRoomDepts == null) iRoomDepts = new HashSet<RoomDept>();
		iRoomDepts.add(roomDept);
	}

	public Set<DatePattern> getDatePatterns() { return iDatePatterns; }
	public void setDatePatterns(Set<DatePattern> datePatterns) { iDatePatterns = datePatterns; }
	public void addTodatePatterns(DatePattern datePattern) {
		if (iDatePatterns == null) iDatePatterns = new HashSet<DatePattern>();
		iDatePatterns.add(datePattern);
	}

	public Set<TimePattern> getTimePatterns() { return iTimePatterns; }
	public void setTimePatterns(Set<TimePattern> timePatterns) { iTimePatterns = timePatterns; }
	public void addTotimePatterns(TimePattern timePattern) {
		if (iTimePatterns == null) iTimePatterns = new HashSet<TimePattern>();
		iTimePatterns.add(timePattern);
	}

	public Set<TimetableManager> getTimetableManagers() { return iTimetableManagers; }
	public void setTimetableManagers(Set<TimetableManager> timetableManagers) { iTimetableManagers = timetableManagers; }
	public void addTotimetableManagers(TimetableManager timetableManager) {
		if (iTimetableManagers == null) iTimetableManagers = new HashSet<TimetableManager>();
		iTimetableManagers.add(timetableManager);
	}

	public Set<DepartmentalInstructor> getInstructors() { return iInstructors; }
	public void setInstructors(Set<DepartmentalInstructor> instructors) { iInstructors = instructors; }
	public void addToinstructors(DepartmentalInstructor departmentalInstructor) {
		if (iInstructors == null) iInstructors = new HashSet<DepartmentalInstructor>();
		iInstructors.add(departmentalInstructor);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Department)) return false;
		if (getUniqueId() == null || ((Department)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Department)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
			"\n	DeptCode: " + getDeptCode() +
			"\n	DistributionPrefPriority: " + getDistributionPrefPriority() +
			"\n	ExternalManager: " + getExternalManager() +
			"\n	ExternalMgrAbbv: " + getExternalMgrAbbv() +
			"\n	ExternalMgrLabel: " + getExternalMgrLabel() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	Name: " + getName() +
			"\n	RoomSharingColor: " + getRoomSharingColor() +
			"\n	Session: " + getSession() +
			"\n	SolverGroup: " + getSolverGroup() +
			"\n	StatusType: " + getStatusType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
