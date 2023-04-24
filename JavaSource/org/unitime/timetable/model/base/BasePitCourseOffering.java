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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.io.Serializable;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.PitCourseOffering;
import org.unitime.timetable.model.PitInstructionalOffering;
import org.unitime.timetable.model.SubjectArea;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitCourseOffering implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iSubjectAreaAbbv;
	private String iCourseNbr;
	private Boolean iIsControl;
	private String iPermId;
	private Integer iProjectedDemand;
	private Integer iNbrExpectedStudents;
	private Integer iDemand;
	private Integer iEnrollment;
	private String iTitle;
	private String iExternalUniqueId;
	private Long iUniqueIdRolledForwardFrom;

	private SubjectArea iSubjectArea;
	private CourseOffering iCourseOffering;
	private PitInstructionalOffering iPitInstructionalOffering;
	private CourseType iCourseType;

	public BasePitCourseOffering() {
	}

	public BasePitCourseOffering(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "pit_course_offering_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "point_in_time_seq")
	})
	@GeneratedValue(generator = "pit_course_offering_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Formula(" ( select sa.subject_area_abbreviation from %SCHEMA%.subject_area sa where sa.uniqueid = subject_area_id ) ")
	public String getSubjectAreaAbbv() { return iSubjectAreaAbbv; }
	public void setSubjectAreaAbbv(String subjectAreaAbbv) { iSubjectAreaAbbv = subjectAreaAbbv; }

	@Column(name = "course_nbr", nullable = false, length = 10)
	public String getCourseNbr() { return iCourseNbr; }
	public void setCourseNbr(String courseNbr) { iCourseNbr = courseNbr; }

	@Column(name = "is_control", nullable = false)
	public Boolean isIsControl() { return iIsControl; }
	@Transient
	public Boolean getIsControl() { return iIsControl; }
	public void setIsControl(Boolean isControl) { iIsControl = isControl; }

	@Column(name = "perm_id", nullable = true, length = 20)
	public String getPermId() { return iPermId; }
	public void setPermId(String permId) { iPermId = permId; }

	@Column(name = "proj_demand", nullable = true, length = 5)
	public Integer getProjectedDemand() { return iProjectedDemand; }
	public void setProjectedDemand(Integer projectedDemand) { iProjectedDemand = projectedDemand; }

	@Column(name = "nbr_expected_stdents", nullable = false, length = 10)
	public Integer getNbrExpectedStudents() { return iNbrExpectedStudents; }
	public void setNbrExpectedStudents(Integer nbrExpectedStudents) { iNbrExpectedStudents = nbrExpectedStudents; }

	@Column(name = "lastlike_demand", nullable = false, length = 10)
	public Integer getDemand() { return iDemand; }
	public void setDemand(Integer demand) { iDemand = demand; }

	@Formula("(select count(distinct e.pit_student_id) from %SCHEMA%.pit_student_class_enrl e where e.pit_course_offering_id = uniqueid)")
	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	@Column(name = "title", nullable = true, length = 200)
	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "subject_area_id", nullable = false)
	public SubjectArea getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(SubjectArea subjectArea) { iSubjectArea = subjectArea; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "course_offering_id", nullable = true)
	public CourseOffering getCourseOffering() { return iCourseOffering; }
	public void setCourseOffering(CourseOffering courseOffering) { iCourseOffering = courseOffering; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_instr_offr_id", nullable = false)
	public PitInstructionalOffering getPitInstructionalOffering() { return iPitInstructionalOffering; }
	public void setPitInstructionalOffering(PitInstructionalOffering pitInstructionalOffering) { iPitInstructionalOffering = pitInstructionalOffering; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "course_type_id", nullable = true)
	public CourseType getCourseType() { return iCourseType; }
	public void setCourseType(CourseType courseType) { iCourseType = courseType; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitCourseOffering)) return false;
		if (getUniqueId() == null || ((PitCourseOffering)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitCourseOffering)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PitCourseOffering["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitCourseOffering[" +
			"\n	CourseNbr: " + getCourseNbr() +
			"\n	CourseOffering: " + getCourseOffering() +
			"\n	CourseType: " + getCourseType() +
			"\n	Demand: " + getDemand() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	IsControl: " + getIsControl() +
			"\n	NbrExpectedStudents: " + getNbrExpectedStudents() +
			"\n	PermId: " + getPermId() +
			"\n	PitInstructionalOffering: " + getPitInstructionalOffering() +
			"\n	ProjectedDemand: " + getProjectedDemand() +
			"\n	SubjectArea: " + getSubjectArea() +
			"\n	Title: " + getTitle() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
