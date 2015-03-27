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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
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
	private Set<SubjectArea> iSubjectAreas;
	private Set<Building> iBuildings;
	private Set<Department> iDepartments;
	private Set<Location> iRooms;
	private Set<InstructionalOffering> iInstructionalOfferings;

	public static String PROP_ACADEMIC_INITIATIVE = "academicInitiative";
	public static String PROP_ACADEMIC_YEAR = "academicYear";
	public static String PROP_ACADEMIC_TERM = "academicTerm";
	public static String PROP_SESSION_BEGIN_DATE_TIME = "sessionBeginDateTime";
	public static String PROP_CLASSES_END_DATE_TIME = "classesEndDateTime";
	public static String PROP_SESSION_END_DATE_TIME = "sessionEndDateTime";
	public static String PROP_EXAM_BEGIN_DATE = "examBeginDate";
	public static String PROP_EVENT_BEGIN_DATE = "eventBeginDate";
	public static String PROP_EVENT_END_DATE = "eventEndDate";
	public static String PROP_HOLIDAYS = "holidays";
	public static String PROP_WK_ENROLL = "lastWeekToEnroll";
	public static String PROP_WK_CHANGE = "lastWeekToChange";
	public static String PROP_WK_DROP = "lastWeekToDrop";

	public BaseSession() {
		initialize();
	}

	public BaseSession(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getAcademicInitiative() { return iAcademicInitiative; }
	public void setAcademicInitiative(String academicInitiative) { iAcademicInitiative = academicInitiative; }

	public String getAcademicYear() { return iAcademicYear; }
	public void setAcademicYear(String academicYear) { iAcademicYear = academicYear; }

	public String getAcademicTerm() { return iAcademicTerm; }
	public void setAcademicTerm(String academicTerm) { iAcademicTerm = academicTerm; }

	public Date getSessionBeginDateTime() { return iSessionBeginDateTime; }
	public void setSessionBeginDateTime(Date sessionBeginDateTime) { iSessionBeginDateTime = sessionBeginDateTime; }

	public Date getClassesEndDateTime() { return iClassesEndDateTime; }
	public void setClassesEndDateTime(Date classesEndDateTime) { iClassesEndDateTime = classesEndDateTime; }

	public Date getSessionEndDateTime() { return iSessionEndDateTime; }
	public void setSessionEndDateTime(Date sessionEndDateTime) { iSessionEndDateTime = sessionEndDateTime; }

	public Date getExamBeginDate() { return iExamBeginDate; }
	public void setExamBeginDate(Date examBeginDate) { iExamBeginDate = examBeginDate; }

	public Date getEventBeginDate() { return iEventBeginDate; }
	public void setEventBeginDate(Date eventBeginDate) { iEventBeginDate = eventBeginDate; }

	public Date getEventEndDate() { return iEventEndDate; }
	public void setEventEndDate(Date eventEndDate) { iEventEndDate = eventEndDate; }

	public String getHolidays() { return iHolidays; }
	public void setHolidays(String holidays) { iHolidays = holidays; }

	public Integer getLastWeekToEnroll() { return iLastWeekToEnroll; }
	public void setLastWeekToEnroll(Integer lastWeekToEnroll) { iLastWeekToEnroll = lastWeekToEnroll; }

	public Integer getLastWeekToChange() { return iLastWeekToChange; }
	public void setLastWeekToChange(Integer lastWeekToChange) { iLastWeekToChange = lastWeekToChange; }

	public Integer getLastWeekToDrop() { return iLastWeekToDrop; }
	public void setLastWeekToDrop(Integer lastWeekToDrop) { iLastWeekToDrop = lastWeekToDrop; }

	public DepartmentStatusType getStatusType() { return iStatusType; }
	public void setStatusType(DepartmentStatusType statusType) { iStatusType = statusType; }

	public DatePattern getDefaultDatePattern() { return iDefaultDatePattern; }
	public void setDefaultDatePattern(DatePattern defaultDatePattern) { iDefaultDatePattern = defaultDatePattern; }

	public StudentSectioningStatus getDefaultSectioningStatus() { return iDefaultSectioningStatus; }
	public void setDefaultSectioningStatus(StudentSectioningStatus defaultSectioningStatus) { iDefaultSectioningStatus = defaultSectioningStatus; }

	public ClassDurationType getDefaultClassDurationType() { return iDefaultClassDurationType; }
	public void setDefaultClassDurationType(ClassDurationType defaultClassDurationType) { iDefaultClassDurationType = defaultClassDurationType; }

	public Set<SubjectArea> getSubjectAreas() { return iSubjectAreas; }
	public void setSubjectAreas(Set<SubjectArea> subjectAreas) { iSubjectAreas = subjectAreas; }
	public void addTosubjectAreas(SubjectArea subjectArea) {
		if (iSubjectAreas == null) iSubjectAreas = new HashSet<SubjectArea>();
		iSubjectAreas.add(subjectArea);
	}

	public Set<Building> getBuildings() { return iBuildings; }
	public void setBuildings(Set<Building> buildings) { iBuildings = buildings; }
	public void addTobuildings(Building building) {
		if (iBuildings == null) iBuildings = new HashSet<Building>();
		iBuildings.add(building);
	}

	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addTodepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet<Department>();
		iDepartments.add(department);
	}

	public Set<Location> getRooms() { return iRooms; }
	public void setRooms(Set<Location> rooms) { iRooms = rooms; }
	public void addTorooms(Location location) {
		if (iRooms == null) iRooms = new HashSet<Location>();
		iRooms.add(location);
	}

	public Set<InstructionalOffering> getInstructionalOfferings() { return iInstructionalOfferings; }
	public void setInstructionalOfferings(Set<InstructionalOffering> instructionalOfferings) { iInstructionalOfferings = instructionalOfferings; }
	public void addToinstructionalOfferings(InstructionalOffering instructionalOffering) {
		if (iInstructionalOfferings == null) iInstructionalOfferings = new HashSet<InstructionalOffering>();
		iInstructionalOfferings.add(instructionalOffering);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Session)) return false;
		if (getUniqueId() == null || ((Session)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Session)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
