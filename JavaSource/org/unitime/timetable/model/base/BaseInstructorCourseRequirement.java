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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorCourseRequirementNote;
import org.unitime.timetable.model.InstructorSurvey;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseInstructorCourseRequirement implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iCourse;

	private InstructorSurvey iInstructorSurvey;
	private CourseOffering iCourseOffering;
	private Set<InstructorCourseRequirementNote> iNotes;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_COURSE = "course";

	public BaseInstructorCourseRequirement() {
		initialize();
	}

	public BaseInstructorCourseRequirement(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getCourse() { return iCourse; }
	public void setCourse(String course) { iCourse = course; }

	public InstructorSurvey getInstructorSurvey() { return iInstructorSurvey; }
	public void setInstructorSurvey(InstructorSurvey instructorSurvey) { iInstructorSurvey = instructorSurvey; }

	public CourseOffering getCourseOffering() { return iCourseOffering; }
	public void setCourseOffering(CourseOffering courseOffering) { iCourseOffering = courseOffering; }

	public Set<InstructorCourseRequirementNote> getNotes() { return iNotes; }
	public void setNotes(Set<InstructorCourseRequirementNote> notes) { iNotes = notes; }
	public void addTonotes(InstructorCourseRequirementNote instructorCourseRequirementNote) {
		if (iNotes == null) iNotes = new HashSet<InstructorCourseRequirementNote>();
		iNotes.add(instructorCourseRequirementNote);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructorCourseRequirement)) return false;
		if (getUniqueId() == null || ((InstructorCourseRequirement)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructorCourseRequirement)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "InstructorCourseRequirement["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "InstructorCourseRequirement[" +
			"\n	Course: " + getCourse() +
			"\n	CourseOffering: " + getCourseOffering() +
			"\n	InstructorSurvey: " + getInstructorSurvey() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
