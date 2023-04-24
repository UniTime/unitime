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

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseStudentGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iGroupAbbreviation;
	private String iGroupName;
	private String iExternalUniqueId;
	private Integer iExpectedSize;

	private Session iSession;
	private StudentGroupType iType;
	private Set<Student> iStudents;

	public BaseStudentGroup() {
	}

	public BaseStudentGroup(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "student_group_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "student_group_seq")
	})
	@GeneratedValue(generator = "student_group_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "group_abbreviation", nullable = false, length = 30)
	public String getGroupAbbreviation() { return iGroupAbbreviation; }
	public void setGroupAbbreviation(String groupAbbreviation) { iGroupAbbreviation = groupAbbreviation; }

	@Column(name = "group_name", nullable = false, length = 90)
	public String getGroupName() { return iGroupName; }
	public void setGroupName(String groupName) { iGroupName = groupName; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "expected_size", nullable = true)
	public Integer getExpectedSize() { return iExpectedSize; }
	public void setExpectedSize(Integer expectedSize) { iExpectedSize = expectedSize; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "type_id", nullable = true)
	public StudentGroupType getType() { return iType; }
	public void setType(StudentGroupType type) { iType = type; }

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "student_to_group",
		joinColumns = { @JoinColumn(name = "group_id") },
		inverseJoinColumns = { @JoinColumn(name = "student_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Student> getStudents() { return iStudents; }
	public void setStudents(Set<Student> students) { iStudents = students; }
	public void addTostudents(Student student) {
		if (iStudents == null) iStudents = new HashSet<Student>();
		iStudents.add(student);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentGroup)) return false;
		if (getUniqueId() == null || ((StudentGroup)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentGroup)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
