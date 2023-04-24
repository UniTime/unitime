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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.SubjectArea;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSession extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iAcademicInitiative;
	private String iAcademicYear;
	private String iAcademicTerm;
	private Date iSessionBeginDateTime;
	private Date iClassesEndDateTime;
	private Date iSessionEndDateTime;
	private Date iExamBeginDate;
	private Date iEventBeginDate;
	private Date iEventEndDate;
	private String iHolidays;
	private Integer iLastWeekToEnroll;
	private Integer iLastWeekToChange;
	private Integer iLastWeekToDrop;

	private DepartmentStatusType iStatusType;
	private DatePattern iDefaultDatePattern;
	private StudentSectioningStatus iDefaultSectioningStatus;
	private ClassDurationType iDefaultClassDurationType;
	private InstructionalMethod iDefaultInstructionalMethod;
	private Set<SubjectArea> iSubjectAreas;
	private Set<Building> iBuildings;
	private Set<Department> iDepartments;
	private Set<Location> iRooms;
	private Set<InstructionalOffering> iInstructionalOfferings;

	public BaseSession() {
	}

	public BaseSession(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "academic_initiative", nullable = false, length = 20)
	public String getAcademicInitiative() { return iAcademicInitiative; }
	public void setAcademicInitiative(String academicInitiative) { iAcademicInitiative = academicInitiative; }

	@Column(name = "academic_year", nullable = false, length = 4)
	public String getAcademicYear() { return iAcademicYear; }
	public void setAcademicYear(String academicYear) { iAcademicYear = academicYear; }

	@Column(name = "academic_term", nullable = false, length = 20)
	public String getAcademicTerm() { return iAcademicTerm; }
	public void setAcademicTerm(String academicTerm) { iAcademicTerm = academicTerm; }

	@Column(name = "session_begin_date_time", nullable = false)
	public Date getSessionBeginDateTime() { return iSessionBeginDateTime; }
	public void setSessionBeginDateTime(Date sessionBeginDateTime) { iSessionBeginDateTime = sessionBeginDateTime; }

	@Column(name = "classes_end_date_time", nullable = false)
	public Date getClassesEndDateTime() { return iClassesEndDateTime; }
	public void setClassesEndDateTime(Date classesEndDateTime) { iClassesEndDateTime = classesEndDateTime; }

	@Column(name = "session_end_date_time", nullable = false)
	public Date getSessionEndDateTime() { return iSessionEndDateTime; }
	public void setSessionEndDateTime(Date sessionEndDateTime) { iSessionEndDateTime = sessionEndDateTime; }

	@Column(name = "exam_begin_date", nullable = false)
	public Date getExamBeginDate() { return iExamBeginDate; }
	public void setExamBeginDate(Date examBeginDate) { iExamBeginDate = examBeginDate; }

	@Column(name = "event_begin_date", nullable = false)
	public Date getEventBeginDate() { return iEventBeginDate; }
	public void setEventBeginDate(Date eventBeginDate) { iEventBeginDate = eventBeginDate; }

	@Column(name = "event_end_date", nullable = false)
	public Date getEventEndDate() { return iEventEndDate; }
	public void setEventEndDate(Date eventEndDate) { iEventEndDate = eventEndDate; }

	@Column(name = "holidays", nullable = true, length = 366)
	public String getHolidays() { return iHolidays; }
	public void setHolidays(String holidays) { iHolidays = holidays; }

	@Column(name = "wk_enroll", nullable = false)
	public Integer getLastWeekToEnroll() { return iLastWeekToEnroll; }
	public void setLastWeekToEnroll(Integer lastWeekToEnroll) { iLastWeekToEnroll = lastWeekToEnroll; }

	@Column(name = "wk_change", nullable = false)
	public Integer getLastWeekToChange() { return iLastWeekToChange; }
	public void setLastWeekToChange(Integer lastWeekToChange) { iLastWeekToChange = lastWeekToChange; }

	@Column(name = "wk_drop", nullable = false)
	public Integer getLastWeekToDrop() { return iLastWeekToDrop; }
	public void setLastWeekToDrop(Integer lastWeekToDrop) { iLastWeekToDrop = lastWeekToDrop; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "status_type", nullable = false)
	public DepartmentStatusType getStatusType() { return iStatusType; }
	public void setStatusType(DepartmentStatusType statusType) { iStatusType = statusType; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "def_datepatt_id", nullable = true)
	public DatePattern getDefaultDatePattern() { return iDefaultDatePattern; }
	public void setDefaultDatePattern(DatePattern defaultDatePattern) { iDefaultDatePattern = defaultDatePattern; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "sect_status", nullable = true)
	public StudentSectioningStatus getDefaultSectioningStatus() { return iDefaultSectioningStatus; }
	public void setDefaultSectioningStatus(StudentSectioningStatus defaultSectioningStatus) { iDefaultSectioningStatus = defaultSectioningStatus; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "duration_type_id", nullable = true)
	public ClassDurationType getDefaultClassDurationType() { return iDefaultClassDurationType; }
	public void setDefaultClassDurationType(ClassDurationType defaultClassDurationType) { iDefaultClassDurationType = defaultClassDurationType; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "instr_method_id", nullable = true)
	public InstructionalMethod getDefaultInstructionalMethod() { return iDefaultInstructionalMethod; }
	public void setDefaultInstructionalMethod(InstructionalMethod defaultInstructionalMethod) { iDefaultInstructionalMethod = defaultInstructionalMethod; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "session", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<SubjectArea> getSubjectAreas() { return iSubjectAreas; }
	public void setSubjectAreas(Set<SubjectArea> subjectAreas) { iSubjectAreas = subjectAreas; }
	public void addTosubjectAreas(SubjectArea subjectArea) {
		if (iSubjectAreas == null) iSubjectAreas = new HashSet<SubjectArea>();
		iSubjectAreas.add(subjectArea);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "session", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Building> getBuildings() { return iBuildings; }
	public void setBuildings(Set<Building> buildings) { iBuildings = buildings; }
	public void addTobuildings(Building building) {
		if (iBuildings == null) iBuildings = new HashSet<Building>();
		iBuildings.add(building);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "session", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addTodepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet<Department>();
		iDepartments.add(department);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "session", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Location> getRooms() { return iRooms; }
	public void setRooms(Set<Location> rooms) { iRooms = rooms; }
	public void addTorooms(Location location) {
		if (iRooms == null) iRooms = new HashSet<Location>();
		iRooms.add(location);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "session", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<InstructionalOffering> getInstructionalOfferings() { return iInstructionalOfferings; }
	public void setInstructionalOfferings(Set<InstructionalOffering> instructionalOfferings) { iInstructionalOfferings = instructionalOfferings; }
	public void addToinstructionalOfferings(InstructionalOffering instructionalOffering) {
		if (iInstructionalOfferings == null) iInstructionalOfferings = new HashSet<InstructionalOffering>();
		iInstructionalOfferings.add(instructionalOffering);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Session)) return false;
		if (getUniqueId() == null || ((Session)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Session)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Session["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Session[" +
			"\n	AcademicInitiative: " + getAcademicInitiative() +
			"\n	AcademicTerm: " + getAcademicTerm() +
			"\n	AcademicYear: " + getAcademicYear() +
			"\n	ClassesEndDateTime: " + getClassesEndDateTime() +
			"\n	DefaultClassDurationType: " + getDefaultClassDurationType() +
			"\n	DefaultDatePattern: " + getDefaultDatePattern() +
			"\n	DefaultInstructionalMethod: " + getDefaultInstructionalMethod() +
			"\n	DefaultSectioningStatus: " + getDefaultSectioningStatus() +
			"\n	EventBeginDate: " + getEventBeginDate() +
			"\n	EventEndDate: " + getEventEndDate() +
			"\n	ExamBeginDate: " + getExamBeginDate() +
			"\n	Holidays: " + getHolidays() +
			"\n	LastWeekToChange: " + getLastWeekToChange() +
			"\n	LastWeekToDrop: " + getLastWeekToDrop() +
			"\n	LastWeekToEnroll: " + getLastWeekToEnroll() +
			"\n	SessionBeginDateTime: " + getSessionBeginDateTime() +
			"\n	SessionEndDateTime: " + getSessionEndDateTime() +
			"\n	StatusType: " + getStatusType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
