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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;

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

	private DepartmentStatusType iStatusType;
	private DatePattern iDefaultDatePattern;
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

	public DepartmentStatusType getStatusType() { return iStatusType; }
	public void setStatusType(DepartmentStatusType statusType) { iStatusType = statusType; }

	public DatePattern getDefaultDatePattern() { return iDefaultDatePattern; }
	public void setDefaultDatePattern(DatePattern defaultDatePattern) { iDefaultDatePattern = defaultDatePattern; }

	public Set<SubjectArea> getSubjectAreas() { return iSubjectAreas; }
	public void setSubjectAreas(Set<SubjectArea> subjectAreas) { iSubjectAreas = subjectAreas; }
	public void addTosubjectAreas(SubjectArea subjectArea) {
		if (iSubjectAreas == null) iSubjectAreas = new HashSet();
		iSubjectAreas.add(subjectArea);
	}

	public Set<Building> getBuildings() { return iBuildings; }
	public void setBuildings(Set<Building> buildings) { iBuildings = buildings; }
	public void addTobuildings(Building building) {
		if (iBuildings == null) iBuildings = new HashSet();
		iBuildings.add(building);
	}

	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addTodepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet();
		iDepartments.add(department);
	}

	public Set<Location> getRooms() { return iRooms; }
	public void setRooms(Set<Location> rooms) { iRooms = rooms; }
	public void addTorooms(Location location) {
		if (iRooms == null) iRooms = new HashSet();
		iRooms.add(location);
	}

	public Set<InstructionalOffering> getInstructionalOfferings() { return iInstructionalOfferings; }
	public void setInstructionalOfferings(Set<InstructionalOffering> instructionalOfferings) { iInstructionalOfferings = instructionalOfferings; }
	public void addToinstructionalOfferings(InstructionalOffering instructionalOffering) {
		if (iInstructionalOfferings == null) iInstructionalOfferings = new HashSet();
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
			"\n	DefaultDatePattern: " + getDefaultDatePattern() +
			"\n	EventBeginDate: " + getEventBeginDate() +
			"\n	EventEndDate: " + getEventEndDate() +
			"\n	ExamBeginDate: " + getExamBeginDate() +
			"\n	Holidays: " + getHolidays() +
			"\n	SessionBeginDateTime: " + getSessionBeginDateTime() +
			"\n	SessionEndDateTime: " + getSessionEndDateTime() +
			"\n	StatusType: " + getStatusType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
