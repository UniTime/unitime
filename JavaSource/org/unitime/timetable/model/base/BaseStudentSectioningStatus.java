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
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseStudentSectioningStatus extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iStatus;
	private String iMessage;
	private Date iEffectiveStartDate;
	private Date iEffectiveStopDate;
	private Integer iEffectiveStartPeriod;
	private Integer iEffectiveStopPeriod;

	private StudentSectioningStatus iFallBackStatus;
	private Session iSession;
	private Set<CourseType> iTypes;

	public BaseStudentSectioningStatus() {
	}

	public BaseStudentSectioningStatus(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "status", nullable = false)
	public Integer getStatus() { return iStatus; }
	public void setStatus(Integer status) { iStatus = status; }

	@Column(name = "message", nullable = true, length = 500)
	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	@Column(name = "start_date", nullable = true)
	public Date getEffectiveStartDate() { return iEffectiveStartDate; }
	public void setEffectiveStartDate(Date effectiveStartDate) { iEffectiveStartDate = effectiveStartDate; }

	@Column(name = "stop_date", nullable = true)
	public Date getEffectiveStopDate() { return iEffectiveStopDate; }
	public void setEffectiveStopDate(Date effectiveStopDate) { iEffectiveStopDate = effectiveStopDate; }

	@Column(name = "start_slot", nullable = true)
	public Integer getEffectiveStartPeriod() { return iEffectiveStartPeriod; }
	public void setEffectiveStartPeriod(Integer effectiveStartPeriod) { iEffectiveStartPeriod = effectiveStartPeriod; }

	@Column(name = "stop_slot", nullable = true)
	public Integer getEffectiveStopPeriod() { return iEffectiveStopPeriod; }
	public void setEffectiveStopPeriod(Integer effectiveStopPeriod) { iEffectiveStopPeriod = effectiveStopPeriod; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "fallback_id", nullable = true)
	public StudentSectioningStatus getFallBackStatus() { return iFallBackStatus; }
	public void setFallBackStatus(StudentSectioningStatus fallBackStatus) { iFallBackStatus = fallBackStatus; }

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "sectioning_course_types",
		joinColumns = { @JoinColumn(name = "sectioning_status_id") },
		inverseJoinColumns = { @JoinColumn(name = "course_type_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<CourseType> getTypes() { return iTypes; }
	public void setTypes(Set<CourseType> types) { iTypes = types; }
	public void addToTypes(CourseType courseType) {
		if (iTypes == null) iTypes = new HashSet<CourseType>();
		iTypes.add(courseType);
	}
	@Deprecated
	public void addTotypes(CourseType courseType) {
		addToTypes(courseType);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentSectioningStatus)) return false;
		if (getUniqueId() == null || ((StudentSectioningStatus)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentSectioningStatus)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "StudentSectioningStatus["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "StudentSectioningStatus[" +
			"\n	EffectiveStartDate: " + getEffectiveStartDate() +
			"\n	EffectiveStartPeriod: " + getEffectiveStartPeriod() +
			"\n	EffectiveStopDate: " + getEffectiveStopDate() +
			"\n	EffectiveStopPeriod: " + getEffectiveStopPeriod() +
			"\n	FallBackStatus: " + getFallBackStatus() +
			"\n	Label: " + getLabel() +
			"\n	Message: " + getMessage() +
			"\n	Reference: " + getReference() +
			"\n	Session: " + getSession() +
			"\n	Status: " + getStatus() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
