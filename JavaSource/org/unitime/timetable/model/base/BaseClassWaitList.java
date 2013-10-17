/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.Student;

/**
 * @author Tomas Muller
 */
public abstract class BaseClassWaitList implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iType;
	private Date iTimestamp;

	private Student iStudent;
	private CourseRequest iCourseRequest;
	private Class_ iClazz;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TYPE = "type";
	public static String PROP_TIMESTAMP = "timestamp";

	public BaseClassWaitList() {
		initialize();
	}

	public BaseClassWaitList(Long uniqueId) {
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

	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	public CourseRequest getCourseRequest() { return iCourseRequest; }
	public void setCourseRequest(CourseRequest courseRequest) { iCourseRequest = courseRequest; }

	public Class_ getClazz() { return iClazz; }
	public void setClazz(Class_ clazz) { iClazz = clazz; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ClassWaitList)) return false;
		if (getUniqueId() == null || ((ClassWaitList)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ClassWaitList)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ClassWaitList["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ClassWaitList[" +
			"\n	Clazz: " + getClazz() +
			"\n	CourseRequest: " + getCourseRequest() +
			"\n	Student: " + getStudent() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
