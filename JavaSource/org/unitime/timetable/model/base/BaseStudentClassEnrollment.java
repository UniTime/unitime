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
import java.util.Date;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseStudentClassEnrollment implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iTimestamp;
	private Date iApprovedDate;
	private String iApprovedBy;
	private String iChangedBy;

	private Student iStudent;
	private CourseRequest iCourseRequest;
	private CourseOffering iCourseOffering;
	private Class_ iClazz;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TIMESTAMP = "timestamp";
	public static String PROP_APPROVED_DATE = "approvedDate";
	public static String PROP_APPROVED_BY = "approvedBy";
	public static String PROP_CHANGED_BY = "changedBy";

	public BaseStudentClassEnrollment() {
		initialize();
	}

	public BaseStudentClassEnrollment(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	public Date getApprovedDate() { return iApprovedDate; }
	public void setApprovedDate(Date approvedDate) { iApprovedDate = approvedDate; }

	public String getApprovedBy() { return iApprovedBy; }
	public void setApprovedBy(String approvedBy) { iApprovedBy = approvedBy; }

	public String getChangedBy() { return iChangedBy; }
	public void setChangedBy(String changedBy) { iChangedBy = changedBy; }

	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	public CourseRequest getCourseRequest() { return iCourseRequest; }
	public void setCourseRequest(CourseRequest courseRequest) { iCourseRequest = courseRequest; }

	public CourseOffering getCourseOffering() { return iCourseOffering; }
	public void setCourseOffering(CourseOffering courseOffering) { iCourseOffering = courseOffering; }

	public Class_ getClazz() { return iClazz; }
	public void setClazz(Class_ clazz) { iClazz = clazz; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentClassEnrollment)) return false;
		if (getUniqueId() == null || ((StudentClassEnrollment)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentClassEnrollment)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "StudentClassEnrollment["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentClassEnrollment[" +
			"\n	ApprovedBy: " + getApprovedBy() +
			"\n	ApprovedDate: " + getApprovedDate() +
			"\n	ChangedBy: " + getChangedBy() +
			"\n	Clazz: " + getClazz() +
			"\n	CourseOffering: " + getCourseOffering() +
			"\n	CourseRequest: " + getCourseRequest() +
			"\n	Student: " + getStudent() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
