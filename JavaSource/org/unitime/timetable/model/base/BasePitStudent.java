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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_FIRST_NAME = "firstName";
	public static String PROP_MIDDLE_NAME = "middleName";
	public static String PROP_LAST_NAME = "lastName";
	public static String PROP_EMAIL = "email";

	public BasePitStudent() {
		initialize();
	}

	public BasePitStudent(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	public PointInTimeData getPointInTimeData() { return iPointInTimeData; }
	public void setPointInTimeData(PointInTimeData pointInTimeData) { iPointInTimeData = pointInTimeData; }

	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	public Set<PitStudentAcadAreaMajorClassification> getPitAcadAreaMajorClassifications() { return iPitAcadAreaMajorClassifications; }
	public void setPitAcadAreaMajorClassifications(Set<PitStudentAcadAreaMajorClassification> pitAcadAreaMajorClassifications) { iPitAcadAreaMajorClassifications = pitAcadAreaMajorClassifications; }
	public void addTopitAcadAreaMajorClassifications(PitStudentAcadAreaMajorClassification pitStudentAcadAreaMajorClassification) {
		if (iPitAcadAreaMajorClassifications == null) iPitAcadAreaMajorClassifications = new HashSet<PitStudentAcadAreaMajorClassification>();
		iPitAcadAreaMajorClassifications.add(pitStudentAcadAreaMajorClassification);
	}

	public Set<PitStudentAcadAreaMinorClassification> getPitAcadAreaMinorClassifications() { return iPitAcadAreaMinorClassifications; }
	public void setPitAcadAreaMinorClassifications(Set<PitStudentAcadAreaMinorClassification> pitAcadAreaMinorClassifications) { iPitAcadAreaMinorClassifications = pitAcadAreaMinorClassifications; }
	public void addTopitAcadAreaMinorClassifications(PitStudentAcadAreaMinorClassification pitStudentAcadAreaMinorClassification) {
		if (iPitAcadAreaMinorClassifications == null) iPitAcadAreaMinorClassifications = new HashSet<PitStudentAcadAreaMinorClassification>();
		iPitAcadAreaMinorClassifications.add(pitStudentAcadAreaMinorClassification);
	}

	public Set<PitStudentClassEnrollment> getPitClassEnrollments() { return iPitClassEnrollments; }
	public void setPitClassEnrollments(Set<PitStudentClassEnrollment> pitClassEnrollments) { iPitClassEnrollments = pitClassEnrollments; }
	public void addTopitClassEnrollments(PitStudentClassEnrollment pitStudentClassEnrollment) {
		if (iPitClassEnrollments == null) iPitClassEnrollments = new HashSet<PitStudentClassEnrollment>();
		iPitClassEnrollments.add(pitStudentClassEnrollment);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitStudent)) return false;
		if (getUniqueId() == null || ((PitStudent)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitStudent)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
