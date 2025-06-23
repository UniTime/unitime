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
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.TeachingResponsibility;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseClassInstructor implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iPercentShare;
	private Boolean iLead;

	private Class_ iClassInstructing;
	private DepartmentalInstructor iInstructor;
	private TeachingResponsibility iResponsibility;
	private TeachingRequest iTeachingRequest;

	public BaseClassInstructor() {
	}

	public BaseClassInstructor(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "class_instructor_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "percent_share", nullable = false, length = 3)
	public Integer getPercentShare() { return iPercentShare; }
	public void setPercentShare(Integer percentShare) { iPercentShare = percentShare; }

	@Column(name = "is_lead", nullable = false)
	public Boolean isLead() { return iLead; }
	@Transient
	public Boolean getLead() { return iLead; }
	public void setLead(Boolean lead) { iLead = lead; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "class_id", nullable = false)
	public Class_ getClassInstructing() { return iClassInstructing; }
	public void setClassInstructing(Class_ classInstructing) { iClassInstructing = classInstructing; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "instructor_id", nullable = false)
	public DepartmentalInstructor getInstructor() { return iInstructor; }
	public void setInstructor(DepartmentalInstructor instructor) { iInstructor = instructor; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "responsibility_id", nullable = true)
	public TeachingResponsibility getResponsibility() { return iResponsibility; }
	public void setResponsibility(TeachingResponsibility responsibility) { iResponsibility = responsibility; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "request_id", nullable = true)
	public TeachingRequest getTeachingRequest() { return iTeachingRequest; }
	public void setTeachingRequest(TeachingRequest teachingRequest) { iTeachingRequest = teachingRequest; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ClassInstructor)) return false;
		if (getUniqueId() == null || ((ClassInstructor)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ClassInstructor)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ClassInstructor["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ClassInstructor[" +
			"\n	ClassInstructing: " + getClassInstructing() +
			"\n	Instructor: " + getInstructor() +
			"\n	Lead: " + getLead() +
			"\n	PercentShare: " + getPercentShare() +
			"\n	Responsibility: " + getResponsibility() +
			"\n	TeachingRequest: " + getTeachingRequest() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
