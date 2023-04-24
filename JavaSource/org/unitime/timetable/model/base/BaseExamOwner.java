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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseExamOwner implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iOwnerId;
	private Integer iOwnerType;

	private Exam iExam;
	private CourseOffering iCourse;

	public BaseExamOwner() {
	}

	public BaseExamOwner(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "exam_owner_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "exam_owner_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "owner_id", nullable = false, length = 20)
	public Long getOwnerId() { return iOwnerId; }
	public void setOwnerId(Long ownerId) { iOwnerId = ownerId; }

	@Column(name = "owner_type", nullable = false, length = 10)
	public Integer getOwnerType() { return iOwnerType; }
	public void setOwnerType(Integer ownerType) { iOwnerType = ownerType; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "exam_id", nullable = false)
	public Exam getExam() { return iExam; }
	public void setExam(Exam exam) { iExam = exam; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	public CourseOffering getCourse() { return iCourse; }
	public void setCourse(CourseOffering course) { iCourse = course; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExamOwner)) return false;
		if (getUniqueId() == null || ((ExamOwner)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExamOwner)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
