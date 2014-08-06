/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
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
