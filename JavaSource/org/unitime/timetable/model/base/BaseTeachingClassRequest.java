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
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TeachingRequest;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseTeachingClassRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iPercentShare;
	private Boolean iLead;
	private Boolean iCanOverlap;
	private Boolean iAssignInstructor;
	private Boolean iCommon;

	private TeachingRequest iTeachingRequest;
	private Class_ iTeachingClass;

	public BaseTeachingClassRequest() {
	}

	public BaseTeachingClassRequest(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "teachreq_class_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "teachreq_class_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "percent_share", nullable = false)
	public Integer getPercentShare() { return iPercentShare; }
	public void setPercentShare(Integer percentShare) { iPercentShare = percentShare; }

	@Column(name = "is_lead", nullable = false)
	public Boolean isLead() { return iLead; }
	@Transient
	public Boolean getLead() { return iLead; }
	public void setLead(Boolean lead) { iLead = lead; }

	@Column(name = "can_overlap", nullable = false)
	public Boolean isCanOverlap() { return iCanOverlap; }
	@Transient
	public Boolean getCanOverlap() { return iCanOverlap; }
	public void setCanOverlap(Boolean canOverlap) { iCanOverlap = canOverlap; }

	@Column(name = "assign_instructor", nullable = false)
	public Boolean isAssignInstructor() { return iAssignInstructor; }
	@Transient
	public Boolean getAssignInstructor() { return iAssignInstructor; }
	public void setAssignInstructor(Boolean assignInstructor) { iAssignInstructor = assignInstructor; }

	@Column(name = "common", nullable = false)
	public Boolean isCommon() { return iCommon; }
	@Transient
	public Boolean getCommon() { return iCommon; }
	public void setCommon(Boolean common) { iCommon = common; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "request_id", nullable = false)
	public TeachingRequest getTeachingRequest() { return iTeachingRequest; }
	public void setTeachingRequest(TeachingRequest teachingRequest) { iTeachingRequest = teachingRequest; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "class_id", nullable = false)
	public Class_ getTeachingClass() { return iTeachingClass; }
	public void setTeachingClass(Class_ teachingClass) { iTeachingClass = teachingClass; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TeachingClassRequest)) return false;
		if (getUniqueId() == null || ((TeachingClassRequest)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TeachingClassRequest)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "TeachingClassRequest["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TeachingClassRequest[" +
			"\n	AssignInstructor: " + getAssignInstructor() +
			"\n	CanOverlap: " + getCanOverlap() +
			"\n	Common: " + getCommon() +
			"\n	Lead: " + getLead() +
			"\n	PercentShare: " + getPercentShare() +
			"\n	TeachingClass: " + getTeachingClass() +
			"\n	TeachingRequest: " + getTeachingRequest() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
