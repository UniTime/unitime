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
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.StudentEnrollmentMessage;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseStudentEnrollmentMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iMessage;
	private Integer iLevel;
	private Integer iType;
	private Date iTimestamp;
	private Integer iOrder;

	private CourseDemand iCourseDemand;

	public BaseStudentEnrollmentMessage() {
	}

	public BaseStudentEnrollmentMessage(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "student_enrl_msg_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "student_enrl_msg_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "message", nullable = false, length = 255)
	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	@Column(name = "msg_level", nullable = false, length = 10)
	public Integer getLevel() { return iLevel; }
	public void setLevel(Integer level) { iLevel = level; }

	@Column(name = "type", nullable = false, length = 10)
	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	@Column(name = "timestamp", nullable = false)
	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	@Column(name = "ord", nullable = false, length = 10)
	public Integer getOrder() { return iOrder; }
	public void setOrder(Integer order) { iOrder = order; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "course_demand_id", nullable = false)
	public CourseDemand getCourseDemand() { return iCourseDemand; }
	public void setCourseDemand(CourseDemand courseDemand) { iCourseDemand = courseDemand; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentEnrollmentMessage)) return false;
		if (getUniqueId() == null || ((StudentEnrollmentMessage)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentEnrollmentMessage)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "StudentEnrollmentMessage["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentEnrollmentMessage[" +
			"\n	CourseDemand: " + getCourseDemand() +
			"\n	Level: " + getLevel() +
			"\n	Message: " + getMessage() +
			"\n	Order: " + getOrder() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
