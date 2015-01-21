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

import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseLastLikeCourseDemand implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iCourseNbr;
	private Integer iPriority;
	private String iCoursePermId;

	private Student iStudent;
	private SubjectArea iSubjectArea;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_COURSE_NBR = "courseNbr";
	public static String PROP_PRIORITY = "priority";
	public static String PROP_COURSE_PERM_ID = "coursePermId";

	public BaseLastLikeCourseDemand() {
		initialize();
	}

	public BaseLastLikeCourseDemand(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getCourseNbr() { return iCourseNbr; }
	public void setCourseNbr(String courseNbr) { iCourseNbr = courseNbr; }

	public Integer getPriority() { return iPriority; }
	public void setPriority(Integer priority) { iPriority = priority; }

	public String getCoursePermId() { return iCoursePermId; }
	public void setCoursePermId(String coursePermId) { iCoursePermId = coursePermId; }

	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	public SubjectArea getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(SubjectArea subjectArea) { iSubjectArea = subjectArea; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof LastLikeCourseDemand)) return false;
		if (getUniqueId() == null || ((LastLikeCourseDemand)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((LastLikeCourseDemand)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "LastLikeCourseDemand["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "LastLikeCourseDemand[" +
			"\n	CourseNbr: " + getCourseNbr() +
			"\n	CoursePermId: " + getCoursePermId() +
			"\n	Priority: " + getPriority() +
			"\n	Student: " + getStudent() +
			"\n	SubjectArea: " + getSubjectArea() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
