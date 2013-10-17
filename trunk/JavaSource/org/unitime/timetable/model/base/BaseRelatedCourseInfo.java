/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.RelatedCourseInfo;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public abstract class BaseRelatedCourseInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iOwnerId;
	private Integer iOwnerType;

	private CourseEvent iEvent;
	private CourseOffering iCourse;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_OWNER_ID = "ownerId";
	public static String PROP_OWNER_TYPE = "ownerType";

	public BaseRelatedCourseInfo() {
		initialize();
	}

	public BaseRelatedCourseInfo(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getOwnerId() { return iOwnerId; }
	public void setOwnerId(Long ownerId) { iOwnerId = ownerId; }

	public Integer getOwnerType() { return iOwnerType; }
	public void setOwnerType(Integer ownerType) { iOwnerType = ownerType; }

	public CourseEvent getEvent() { return iEvent; }
	public void setEvent(CourseEvent event) { iEvent = event; }

	public CourseOffering getCourse() { return iCourse; }
	public void setCourse(CourseOffering course) { iCourse = course; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof RelatedCourseInfo)) return false;
		if (getUniqueId() == null || ((RelatedCourseInfo)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((RelatedCourseInfo)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "RelatedCourseInfo["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "RelatedCourseInfo[" +
			"\n	Course: " + getCourse() +
			"\n	Event: " + getEvent() +
			"\n	OwnerId: " + getOwnerId() +
			"\n	OwnerType: " + getOwnerType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
