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

import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;

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
