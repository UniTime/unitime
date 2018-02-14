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

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseStudentGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iSessionId;
	private String iGroupAbbreviation;
	private String iGroupName;
	private String iExternalUniqueId;
	private Integer iExpectedSize;

	private Session iSession;
	private StudentGroupType iType;
	private Set<Student> iStudents;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_SESSION_ID = "sessionId";
	public static String PROP_GROUP_ABBREVIATION = "groupAbbreviation";
	public static String PROP_GROUP_NAME = "groupName";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_EXPECTED_SIZE = "expectedSize";

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

	public Integer getExpectedSize() { return iExpectedSize; }
	public void setExpectedSize(Integer expectedSize) { iExpectedSize = expectedSize; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public StudentGroupType getType() { return iType; }
	public void setType(StudentGroupType type) { iType = type; }

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
			"\n	ExpectedSize: " + getExpectedSize() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	GroupAbbreviation: " + getGroupAbbreviation() +
			"\n	GroupName: " + getGroupName() +
			"\n	Session: " + getSession() +
			"\n	SessionId: " + getSessionId() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
