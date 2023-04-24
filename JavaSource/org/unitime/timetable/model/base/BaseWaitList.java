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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.WaitList;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseWaitList implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iType;
	private Date iTimestamp;
	private Boolean iWaitListed;
	private String iChangedBy;
	private String iRequest;
	private String iEnrollment;
	private Date iWaitListedTimeStamp;

	private Student iStudent;
	private CourseOffering iCourseOffering;
	private CourseOffering iEnrolledCourse;
	private CourseDemand iCourseDemand;
	private CourseOffering iSwapCourseOffering;

	public BaseWaitList() {
	}

	public BaseWaitList(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "waitlist_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "waitlist_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "type", nullable = false, length = 10)
	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	@Column(name = "timestamp", nullable = false)
	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	@Column(name = "waitlisted", nullable = false)
	public Boolean isWaitListed() { return iWaitListed; }
	@Transient
	public Boolean getWaitListed() { return iWaitListed; }
	public void setWaitListed(Boolean waitListed) { iWaitListed = waitListed; }

	@Column(name = "changed_by", nullable = true, length = 40)
	public String getChangedBy() { return iChangedBy; }
	public void setChangedBy(String changedBy) { iChangedBy = changedBy; }

	@Column(name = "request", nullable = true, length = 255)
	public String getRequest() { return iRequest; }
	public void setRequest(String request) { iRequest = request; }

	@Column(name = "enrollment", nullable = true, length = 255)
	public String getEnrollment() { return iEnrollment; }
	public void setEnrollment(String enrollment) { iEnrollment = enrollment; }

	@Column(name = "waitlist_ts", nullable = true)
	public Date getWaitListedTimeStamp() { return iWaitListedTimeStamp; }
	public void setWaitListedTimeStamp(Date waitListedTimeStamp) { iWaitListedTimeStamp = waitListedTimeStamp; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "course_offering_id", nullable = false)
	public CourseOffering getCourseOffering() { return iCourseOffering; }
	public void setCourseOffering(CourseOffering courseOffering) { iCourseOffering = courseOffering; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "enrolled_course_id", nullable = true)
	public CourseOffering getEnrolledCourse() { return iEnrolledCourse; }
	public void setEnrolledCourse(CourseOffering enrolledCourse) { iEnrolledCourse = enrolledCourse; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "demand_id", nullable = true)
	public CourseDemand getCourseDemand() { return iCourseDemand; }
	public void setCourseDemand(CourseDemand courseDemand) { iCourseDemand = courseDemand; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "swap_course_id", nullable = true)
	public CourseOffering getSwapCourseOffering() { return iSwapCourseOffering; }
	public void setSwapCourseOffering(CourseOffering swapCourseOffering) { iSwapCourseOffering = swapCourseOffering; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof WaitList)) return false;
		if (getUniqueId() == null || ((WaitList)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((WaitList)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "WaitList["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "WaitList[" +
			"\n	ChangedBy: " + getChangedBy() +
			"\n	CourseDemand: " + getCourseDemand() +
			"\n	CourseOffering: " + getCourseOffering() +
			"\n	EnrolledCourse: " + getEnrolledCourse() +
			"\n	Enrollment: " + getEnrollment() +
			"\n	Request: " + getRequest() +
			"\n	Student: " + getStudent() +
			"\n	SwapCourseOffering: " + getSwapCourseOffering() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	WaitListed: " + getWaitListed() +
			"\n	WaitListedTimeStamp: " + getWaitListedTimeStamp() +
			"]";
	}
}
