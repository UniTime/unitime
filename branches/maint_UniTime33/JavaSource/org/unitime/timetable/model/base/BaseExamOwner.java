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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;

public abstract class BaseExamOwner implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iOwnerId;
	private Integer iOwnerType;

	private Exam iExam;
	private CourseOffering iCourse;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_OWNER_ID = "ownerId";
	public static String PROP_OWNER_TYPE = "ownerType";

	public BaseExamOwner() {
		initialize();
	}

	public BaseExamOwner(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getOwnerId() { return iOwnerId; }
	public void setOwnerId(Long ownerId) { iOwnerId = ownerId; }

	public Integer getOwnerType() { return iOwnerType; }
	public void setOwnerType(Integer ownerType) { iOwnerType = ownerType; }

	public Exam getExam() { return iExam; }
	public void setExam(Exam exam) { iExam = exam; }

	public CourseOffering getCourse() { return iCourse; }
	public void setCourse(CourseOffering course) { iCourse = course; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExamOwner)) return false;
		if (getUniqueId() == null || ((ExamOwner)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExamOwner)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ExamOwner["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ExamOwner[" +
			"\n	Course: " + getCourse() +
			"\n	Exam: " + getExam() +
			"\n	OwnerId: " + getOwnerId() +
			"\n	OwnerType: " + getOwnerType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
