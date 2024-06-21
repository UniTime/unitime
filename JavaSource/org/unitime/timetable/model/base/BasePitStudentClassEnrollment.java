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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitCourseOffering;
import org.unitime.timetable.model.PitStudent;
import org.unitime.timetable.model.PitStudentClassEnrollment;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitStudentClassEnrollment implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Date iTimestamp;
	private String iChangedBy;

	private PitStudent iPitStudent;
	private PitCourseOffering iPitCourseOffering;
	private PitClass iPitClass;

	public BasePitStudentClassEnrollment() {
	}

	public BasePitStudentClassEnrollment(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "point_in_time_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "timestamp", nullable = false)
	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	@Column(name = "changed_by", nullable = true, length = 40)
	public String getChangedBy() { return iChangedBy; }
	public void setChangedBy(String changedBy) { iChangedBy = changedBy; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_student_id", nullable = false)
	public PitStudent getPitStudent() { return iPitStudent; }
	public void setPitStudent(PitStudent pitStudent) { iPitStudent = pitStudent; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_course_offering_id", nullable = false)
	public PitCourseOffering getPitCourseOffering() { return iPitCourseOffering; }
	public void setPitCourseOffering(PitCourseOffering pitCourseOffering) { iPitCourseOffering = pitCourseOffering; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_class_id", nullable = false)
	public PitClass getPitClass() { return iPitClass; }
	public void setPitClass(PitClass pitClass) { iPitClass = pitClass; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitStudentClassEnrollment)) return false;
		if (getUniqueId() == null || ((PitStudentClassEnrollment)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitStudentClassEnrollment)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PitStudentClassEnrollment["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitStudentClassEnrollment[" +
			"\n	ChangedBy: " + getChangedBy() +
			"\n	PitClass: " + getPitClass() +
			"\n	PitCourseOffering: " + getPitCourseOffering() +
			"\n	PitStudent: " + getPitStudent() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
