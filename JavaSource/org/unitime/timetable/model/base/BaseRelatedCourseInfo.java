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

import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.RelatedCourseInfo;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
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
