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

import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.RelatedCourseInfo;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCourseEvent extends Event implements Serializable {
	private static final long serialVersionUID = 1L;

	private Boolean iReqAttendance;

	private Set<RelatedCourseInfo> iRelatedCourses;

	public static String PROP_REQ_ATTD = "reqAttendance";

	public BaseCourseEvent() {
		initialize();
	}

	public BaseCourseEvent(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Boolean isReqAttendance() { return iReqAttendance; }
	public Boolean getReqAttendance() { return iReqAttendance; }
	public void setReqAttendance(Boolean reqAttendance) { iReqAttendance = reqAttendance; }

	public Set<RelatedCourseInfo> getRelatedCourses() { return iRelatedCourses; }
	public void setRelatedCourses(Set<RelatedCourseInfo> relatedCourses) { iRelatedCourses = relatedCourses; }
	public void addTorelatedCourses(RelatedCourseInfo relatedCourseInfo) {
		if (iRelatedCourses == null) iRelatedCourses = new HashSet<RelatedCourseInfo>();
		iRelatedCourses.add(relatedCourseInfo);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseEvent)) return false;
		if (getUniqueId() == null || ((CourseEvent)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseEvent)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
