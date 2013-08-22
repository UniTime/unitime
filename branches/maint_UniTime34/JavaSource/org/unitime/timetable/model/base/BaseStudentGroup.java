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

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroup;

public abstract class BaseStudentGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iSessionId;
	private String iGroupAbbreviation;
	private String iGroupName;
	private String iExternalUniqueId;

	private Session iSession;
	private Set<Student> iStudents;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_SESSION_ID = "sessionId";
	public static String PROP_GROUP_ABBREVIATION = "groupAbbreviation";
	public static String PROP_GROUP_NAME = "groupName";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";

	public BaseStudentGroup() {
		initialize();
	}

	public BaseStudentGroup(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getSessionId() { return iSessionId; }
	public void setSessionId(Long sessionId) { iSessionId = sessionId; }

	public String getGroupAbbreviation() { return iGroupAbbreviation; }
	public void setGroupAbbreviation(String groupAbbreviation) { iGroupAbbreviation = groupAbbreviation; }

	public String getGroupName() { return iGroupName; }
	public void setGroupName(String groupName) { iGroupName = groupName; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<Student> getStudents() { return iStudents; }
	public void setStudents(Set<Student> students) { iStudents = students; }
	public void addTostudents(Student student) {
		if (iStudents == null) iStudents = new HashSet<Student>();
		iStudents.add(student);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentGroup)) return false;
		if (getUniqueId() == null || ((StudentGroup)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentGroup)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "StudentGroup["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentGroup[" +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	GroupAbbreviation: " + getGroupAbbreviation() +
			"\n	GroupName: " + getGroupName() +
			"\n	Session: " + getSession() +
			"\n	SessionId: " + getSessionId() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
