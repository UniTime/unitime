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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.RelatedCourseInfo;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseCourseEvent extends Event implements Serializable {
	private static final long serialVersionUID = 1L;

	private Boolean iReqAttendance;

	private Set<RelatedCourseInfo> iRelatedCourses;

	public BaseCourseEvent() {
	}

	public BaseCourseEvent(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "req_attd", nullable = false)
	public Boolean isReqAttendance() { return iReqAttendance; }
	@Transient
	public Boolean getReqAttendance() { return iReqAttendance; }
	public void setReqAttendance(Boolean reqAttendance) { iReqAttendance = reqAttendance; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "event", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<RelatedCourseInfo> getRelatedCourses() { return iRelatedCourses; }
	public void setRelatedCourses(Set<RelatedCourseInfo> relatedCourses) { iRelatedCourses = relatedCourses; }
	public void addTorelatedCourses(RelatedCourseInfo relatedCourseInfo) {
		if (iRelatedCourses == null) iRelatedCourses = new HashSet<RelatedCourseInfo>();
		iRelatedCourses.add(relatedCourseInfo);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseEvent)) return false;
		if (getUniqueId() == null || ((CourseEvent)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseEvent)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "CourseEvent["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseEvent[" +
			"\n	Email: " + getEmail() +
			"\n	EventName: " + getEventName() +
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	MainContact: " + getMainContact() +
			"\n	MaxCapacity: " + getMaxCapacity() +
			"\n	MinCapacity: " + getMinCapacity() +
			"\n	ReqAttendance: " + getReqAttendance() +
			"\n	SponsoringOrganization: " + getSponsoringOrganization() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
