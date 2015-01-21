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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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
