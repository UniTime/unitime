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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Designator;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;

public abstract class BaseSubjectArea implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iSubjectAreaAbbreviation;
	private String iShortTitle;
	private String iLongTitle;
	private Boolean iScheduleBookOnly;
	private Boolean iPseudoSubjectArea;

	private Session iSession;
	private Department iDepartment;
	private Set<CourseOffering> iCourseOfferings;
	private Set<InstructionalOffering> iInstructionalOfferings;
	private Set<Designator> iDesignatorInstructors;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_SUBJECT_AREA_ABBREVIATION = "subjectAreaAbbreviation";
	public static String PROP_SHORT_TITLE = "shortTitle";
	public static String PROP_LONG_TITLE = "longTitle";
	public static String PROP_SCHEDULE_BOOK_ONLY = "scheduleBookOnly";
	public static String PROP_PSEUDO_SUBJECT_AREA = "pseudoSubjectArea";

	public BaseSubjectArea() {
		initialize();
	}

	public BaseSubjectArea(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getSubjectAreaAbbreviation() { return iSubjectAreaAbbreviation; }
	public void setSubjectAreaAbbreviation(String subjectAreaAbbreviation) { iSubjectAreaAbbreviation = subjectAreaAbbreviation; }

	public String getShortTitle() { return iShortTitle; }
	public void setShortTitle(String shortTitle) { iShortTitle = shortTitle; }

	public String getLongTitle() { return iLongTitle; }
	public void setLongTitle(String longTitle) { iLongTitle = longTitle; }

	public Boolean isScheduleBookOnly() { return iScheduleBookOnly; }
	public Boolean getScheduleBookOnly() { return iScheduleBookOnly; }
	public void setScheduleBookOnly(Boolean scheduleBookOnly) { iScheduleBookOnly = scheduleBookOnly; }

	public Boolean isPseudoSubjectArea() { return iPseudoSubjectArea; }
	public Boolean getPseudoSubjectArea() { return iPseudoSubjectArea; }
	public void setPseudoSubjectArea(Boolean pseudoSubjectArea) { iPseudoSubjectArea = pseudoSubjectArea; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public Set<CourseOffering> getCourseOfferings() { return iCourseOfferings; }
	public void setCourseOfferings(Set<CourseOffering> courseOfferings) { iCourseOfferings = courseOfferings; }
	public void addTocourseOfferings(CourseOffering courseOffering) {
		if (iCourseOfferings == null) iCourseOfferings = new HashSet();
		iCourseOfferings.add(courseOffering);
	}

	public Set<InstructionalOffering> getInstructionalOfferings() { return iInstructionalOfferings; }
	public void setInstructionalOfferings(Set<InstructionalOffering> instructionalOfferings) { iInstructionalOfferings = instructionalOfferings; }
	public void addToinstructionalOfferings(InstructionalOffering instructionalOffering) {
		if (iInstructionalOfferings == null) iInstructionalOfferings = new HashSet();
		iInstructionalOfferings.add(instructionalOffering);
	}

	public Set<Designator> getDesignatorInstructors() { return iDesignatorInstructors; }
	public void setDesignatorInstructors(Set<Designator> designatorInstructors) { iDesignatorInstructors = designatorInstructors; }
	public void addTodesignatorInstructors(Designator designator) {
		if (iDesignatorInstructors == null) iDesignatorInstructors = new HashSet();
		iDesignatorInstructors.add(designator);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SubjectArea)) return false;
		if (getUniqueId() == null || ((SubjectArea)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SubjectArea)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "SubjectArea["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SubjectArea[" +
			"\n	Department: " + getDepartment() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	LongTitle: " + getLongTitle() +
			"\n	PseudoSubjectArea: " + getPseudoSubjectArea() +
			"\n	ScheduleBookOnly: " + getScheduleBookOnly() +
			"\n	Session: " + getSession() +
			"\n	ShortTitle: " + getShortTitle() +
			"\n	SubjectAreaAbbreviation: " + getSubjectAreaAbbreviation() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
