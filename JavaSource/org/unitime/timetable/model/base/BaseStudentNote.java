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

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentNote;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseStudentNote implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iTextNote;
	private Date iTimeStamp;
	private String iUserId;

	private Student iStudent;

	public BaseStudentNote() {
	}

	public BaseStudentNote(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "student_note_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "student_note_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "text_note", nullable = true, length = 1000)
	public String getTextNote() { return iTextNote; }
	public void setTextNote(String textNote) { iTextNote = textNote; }

	@Column(name = "time_stamp", nullable = false)
	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	@Column(name = "user_id", nullable = true, length = 40)
	public String getUserId() { return iUserId; }
	public void setUserId(String userId) { iUserId = userId; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentNote)) return false;
		if (getUniqueId() == null || ((StudentNote)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentNote)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "StudentNote["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentNote[" +
			"\n	Student: " + getStudent() +
			"\n	TextNote: " + getTextNote() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UserId: " + getUserId() +
			"]";
	}
}
