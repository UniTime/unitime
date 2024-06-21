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
import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseAdvisor implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iFirstName;
	private String iMiddleName;
	private String iLastName;
	private String iAcademicTitle;
	private String iEmail;

	private Session iSession;
	private Roles iRole;
	private Set<Student> iStudents;

	public BaseAdvisor() {
	}

	public BaseAdvisor(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "pref_group_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = false, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "first_name", nullable = true, length = 100)
	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	@Column(name = "middle_name", nullable = true, length = 100)
	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	@Column(name = "last_name", nullable = true, length = 100)
	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	@Column(name = "acad_title", nullable = true, length = 50)
	public String getAcademicTitle() { return iAcademicTitle; }
	public void setAcademicTitle(String academicTitle) { iAcademicTitle = academicTitle; }

	@Column(name = "email", nullable = true, length = 200)
	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "role_id", nullable = false)
	public Roles getRole() { return iRole; }
	public void setRole(Roles role) { iRole = role; }

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "student_advisor",
		joinColumns = { @JoinColumn(name = "advisor_id") },
		inverseJoinColumns = { @JoinColumn(name = "student_id") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<Student> getStudents() { return iStudents; }
	public void setStudents(Set<Student> students) { iStudents = students; }
	public void addToStudents(Student student) {
		if (iStudents == null) iStudents = new HashSet<Student>();
		iStudents.add(student);
	}
	@Deprecated
	public void addTostudents(Student student) {
		addToStudents(student);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Advisor)) return false;
		if (getUniqueId() == null || ((Advisor)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Advisor)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Advisor["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Advisor[" +
			"\n	AcademicTitle: " + getAcademicTitle() +
			"\n	Email: " + getEmail() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FirstName: " + getFirstName() +
			"\n	LastName: " + getLastName() +
			"\n	MiddleName: " + getMiddleName() +
			"\n	Role: " + getRole() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
