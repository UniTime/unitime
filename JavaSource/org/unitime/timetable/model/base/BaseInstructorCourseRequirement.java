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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorCourseRequirementNote;
import org.unitime.timetable.model.InstructorSurvey;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseInstructorCourseRequirement implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iCourse;

	private InstructorSurvey iInstructorSurvey;
	private CourseOffering iCourseOffering;
	private Set<InstructorCourseRequirementNote> iNotes;

	public BaseInstructorCourseRequirement() {
	}

	public BaseInstructorCourseRequirement(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "instr_crsreq_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "instr_crsreq_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "course", nullable = true, length = 1024)
	public String getCourse() { return iCourse; }
	public void setCourse(String course) { iCourse = course; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "survey_id", nullable = false)
	public InstructorSurvey getInstructorSurvey() { return iInstructorSurvey; }
	public void setInstructorSurvey(InstructorSurvey instructorSurvey) { iInstructorSurvey = instructorSurvey; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "course_offering_id", nullable = true)
	public CourseOffering getCourseOffering() { return iCourseOffering; }
	public void setCourseOffering(CourseOffering courseOffering) { iCourseOffering = courseOffering; }

	@OneToMany(mappedBy = "requirement", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<InstructorCourseRequirementNote> getNotes() { return iNotes; }
	public void setNotes(Set<InstructorCourseRequirementNote> notes) { iNotes = notes; }
	public void addTonotes(InstructorCourseRequirementNote instructorCourseRequirementNote) {
		if (iNotes == null) iNotes = new HashSet<InstructorCourseRequirementNote>();
		iNotes.add(instructorCourseRequirementNote);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructorCourseRequirement)) return false;
		if (getUniqueId() == null || ((InstructorCourseRequirement)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructorCourseRequirement)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
