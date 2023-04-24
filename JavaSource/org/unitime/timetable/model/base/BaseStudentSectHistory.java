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
import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectHistory;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseStudentSectHistory implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private byte[] iData;
	private Integer iType;
	private Date iTimestamp;

	private Student iStudent;

	public BaseStudentSectHistory() {
	}

	public BaseStudentSectHistory(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "student_sect_hist_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "student_sect_hist_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "data", nullable = false)
	public byte[] getData() { return iData; }
	public void setData(byte[] data) { iData = data; }

	@Column(name = "type", nullable = false, length = 10)
	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	@Column(name = "timestamp", nullable = false)
	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentSectHistory)) return false;
		if (getUniqueId() == null || ((StudentSectHistory)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentSectHistory)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "StudentSectHistory["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentSectHistory[" +
			"\n	Data: " + getData() +
			"\n	Student: " + getStudent() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
