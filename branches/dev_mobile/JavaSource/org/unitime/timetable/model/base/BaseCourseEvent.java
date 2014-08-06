/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
