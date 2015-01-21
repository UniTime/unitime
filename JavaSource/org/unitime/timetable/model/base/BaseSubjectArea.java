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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
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
