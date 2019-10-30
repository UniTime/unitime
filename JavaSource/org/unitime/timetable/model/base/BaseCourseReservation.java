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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseReservation;
import org.unitime.timetable.model.Reservation;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCourseReservation extends Reservation implements Serializable {
	private static final long serialVersionUID = 1L;

	private CourseOffering iCourse;


	public BaseCourseReservation() {
		initialize();
	}

	public BaseCourseReservation(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public CourseOffering getCourse() { return iCourse; }
	public void setCourse(CourseOffering course) { iCourse = course; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseReservation)) return false;
		if (getUniqueId() == null || ((CourseReservation)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseReservation)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CourseReservation["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseReservation[" +
			"\n	Course: " + getCourse() +
			"\n	ExpirationDate: " + getExpirationDate() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	Limit: " + getLimit() +
			"\n	StartDate: " + getStartDate() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
