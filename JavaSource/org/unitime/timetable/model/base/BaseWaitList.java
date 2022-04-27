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

import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.WaitList;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TYPE = "type";
	public static String PROP_TIMESTAMP = "timestamp";
	public static String PROP_WAITLISTED = "waitListed";
	public static String PROP_CHANGED_BY = "changedBy";
	public static String PROP_REQUEST = "request";
	public static String PROP_ENROLLMENT = "enrollment";
	public static String PROP_WAITLIST_TS = "waitListedTimeStamp";

	public BaseWaitList() {
		initialize();
	}

	public BaseWaitList(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	public Boolean isWaitListed() { return iWaitListed; }
	public Boolean getWaitListed() { return iWaitListed; }
	public void setWaitListed(Boolean waitListed) { iWaitListed = waitListed; }

	public String getChangedBy() { return iChangedBy; }
	public void setChangedBy(String changedBy) { iChangedBy = changedBy; }

	public String getRequest() { return iRequest; }
	public void setRequest(String request) { iRequest = request; }

	public String getEnrollment() { return iEnrollment; }
	public void setEnrollment(String enrollment) { iEnrollment = enrollment; }

	public Date getWaitListedTimeStamp() { return iWaitListedTimeStamp; }
	public void setWaitListedTimeStamp(Date waitListedTimeStamp) { iWaitListedTimeStamp = waitListedTimeStamp; }

	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	public CourseOffering getCourseOffering() { return iCourseOffering; }
	public void setCourseOffering(CourseOffering courseOffering) { iCourseOffering = courseOffering; }

	public CourseOffering getEnrolledCourse() { return iEnrolledCourse; }
	public void setEnrolledCourse(CourseOffering enrolledCourse) { iEnrolledCourse = enrolledCourse; }

	public CourseDemand getCourseDemand() { return iCourseDemand; }
	public void setCourseDemand(CourseDemand courseDemand) { iCourseDemand = courseDemand; }

	public CourseOffering getSwapCourseOffering() { return iSwapCourseOffering; }
	public void setSwapCourseOffering(CourseOffering swapCourseOffering) { iSwapCourseOffering = swapCourseOffering; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof WaitList)) return false;
		if (getUniqueId() == null || ((WaitList)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((WaitList)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
