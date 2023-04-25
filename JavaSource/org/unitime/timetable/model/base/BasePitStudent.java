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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.PitStudent;
import org.unitime.timetable.model.PitStudentAcadAreaMajorClassification;
import org.unitime.timetable.model.PitStudentAcadAreaMinorClassification;
import org.unitime.timetable.model.PitStudentClassEnrollment;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.Student;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitStudent implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iFirstName;
	private String iMiddleName;
	private String iLastName;
	private String iEmail;

	private PointInTimeData iPointInTimeData;
	private Student iStudent;
	private Set<PitStudentAcadAreaMajorClassification> iPitAcadAreaMajorClassifications;
	private Set<PitStudentAcadAreaMinorClassification> iPitAcadAreaMinorClassifications;
	private Set<PitStudentClassEnrollment> iPitClassEnrollments;

	public BasePitStudent() {
	}

	public BasePitStudent(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "pit_student_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "point_in_time_seq")
	})
	@GeneratedValue(generator = "pit_student_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "first_name", nullable = false, length = 100)
	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	@Column(name = "middle_name", nullable = true, length = 100)
	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	@Column(name = "last_name", nullable = false, length = 100)
	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	@Column(name = "email", nullable = true, length = 200)
	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "point_in_time_data_id", nullable = false)
	public PointInTimeData getPointInTimeData() { return iPointInTimeData; }
	public void setPointInTimeData(PointInTimeData pointInTimeData) { iPointInTimeData = pointInTimeData; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "student_id", nullable = true)
	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitStudent", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<PitStudentAcadAreaMajorClassification> getPitAcadAreaMajorClassifications() { return iPitAcadAreaMajorClassifications; }
	public void setPitAcadAreaMajorClassifications(Set<PitStudentAcadAreaMajorClassification> pitAcadAreaMajorClassifications) { iPitAcadAreaMajorClassifications = pitAcadAreaMajorClassifications; }
	public void addTopitAcadAreaMajorClassifications(PitStudentAcadAreaMajorClassification pitStudentAcadAreaMajorClassification) {
		if (iPitAcadAreaMajorClassifications == null) iPitAcadAreaMajorClassifications = new HashSet<PitStudentAcadAreaMajorClassification>();
		iPitAcadAreaMajorClassifications.add(pitStudentAcadAreaMajorClassification);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitStudent", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<PitStudentAcadAreaMinorClassification> getPitAcadAreaMinorClassifications() { return iPitAcadAreaMinorClassifications; }
	public void setPitAcadAreaMinorClassifications(Set<PitStudentAcadAreaMinorClassification> pitAcadAreaMinorClassifications) { iPitAcadAreaMinorClassifications = pitAcadAreaMinorClassifications; }
	public void addTopitAcadAreaMinorClassifications(PitStudentAcadAreaMinorClassification pitStudentAcadAreaMinorClassification) {
		if (iPitAcadAreaMinorClassifications == null) iPitAcadAreaMinorClassifications = new HashSet<PitStudentAcadAreaMinorClassification>();
		iPitAcadAreaMinorClassifications.add(pitStudentAcadAreaMinorClassification);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitStudent", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<PitStudentClassEnrollment> getPitClassEnrollments() { return iPitClassEnrollments; }
	public void setPitClassEnrollments(Set<PitStudentClassEnrollment> pitClassEnrollments) { iPitClassEnrollments = pitClassEnrollments; }
	public void addTopitClassEnrollments(PitStudentClassEnrollment pitStudentClassEnrollment) {
		if (iPitClassEnrollments == null) iPitClassEnrollments = new HashSet<PitStudentClassEnrollment>();
		iPitClassEnrollments.add(pitStudentClassEnrollment);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitStudent)) return false;
		if (getUniqueId() == null || ((PitStudent)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitStudent)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PitStudent["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitStudent[" +
			"\n	Email: " + getEmail() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FirstName: " + getFirstName() +
			"\n	LastName: " + getLastName() +
			"\n	MiddleName: " + getMiddleName() +
			"\n	PointInTimeData: " + getPointInTimeData() +
			"\n	Student: " + getStudent() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
