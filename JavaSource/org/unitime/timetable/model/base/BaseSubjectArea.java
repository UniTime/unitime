/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;

/**
 * @author Tomas Muller
 */
public abstract class BaseSubjectArea implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iSubjectAreaAbbreviation;
	private String iTitle;

	private Session iSession;
	private Department iDepartment;
	private Set<CourseOffering> iCourseOfferings;
	private Set<InstructionalOffering> iInstructionalOfferings;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_SUBJECT_AREA_ABBREVIATION = "subjectAreaAbbreviation";
	public static String PROP_LONG_TITLE = "title";

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

	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public Set<CourseOffering> getCourseOfferings() { return iCourseOfferings; }
	public void setCourseOfferings(Set<CourseOffering> courseOfferings) { iCourseOfferings = courseOfferings; }
	public void addTocourseOfferings(CourseOffering courseOffering) {
		if (iCourseOfferings == null) iCourseOfferings = new HashSet<CourseOffering>();
		iCourseOfferings.add(courseOffering);
	}

	public Set<InstructionalOffering> getInstructionalOfferings() { return iInstructionalOfferings; }
	public void setInstructionalOfferings(Set<InstructionalOffering> instructionalOfferings) { iInstructionalOfferings = instructionalOfferings; }
	public void addToinstructionalOfferings(InstructionalOffering instructionalOffering) {
		if (iInstructionalOfferings == null) iInstructionalOfferings = new HashSet<InstructionalOffering>();
		iInstructionalOfferings.add(instructionalOffering);
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
			"\n	Session: " + getSession() +
			"\n	SubjectAreaAbbreviation: " + getSubjectAreaAbbreviation() +
			"\n	Title: " + getTitle() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
