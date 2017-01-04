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
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.PitCourseOffering;
import org.unitime.timetable.model.PitInstructionalOffering;
import org.unitime.timetable.model.SubjectArea;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_COURSE_NBR = "courseNbr";
	public static String PROP_IS_CONTROL = "isControl";
	public static String PROP_PERM_ID = "permId";
	public static String PROP_PROJ_DEMAND = "projectedDemand";
	public static String PROP_NBR_EXPECTED_STDENTS = "nbrExpectedStudents";
	public static String PROP_LASTLIKE_DEMAND = "demand";
	public static String PROP_TITLE = "title";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	public BasePitCourseOffering() {
		initialize();
	}

	public BasePitCourseOffering(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getSubjectAreaAbbv() { return iSubjectAreaAbbv; }
	public void setSubjectAreaAbbv(String subjectAreaAbbv) { iSubjectAreaAbbv = subjectAreaAbbv; }

	public String getCourseNbr() { return iCourseNbr; }
	public void setCourseNbr(String courseNbr) { iCourseNbr = courseNbr; }

	public Boolean isIsControl() { return iIsControl; }
	public Boolean getIsControl() { return iIsControl; }
	public void setIsControl(Boolean isControl) { iIsControl = isControl; }

	public String getPermId() { return iPermId; }
	public void setPermId(String permId) { iPermId = permId; }

	public Integer getProjectedDemand() { return iProjectedDemand; }
	public void setProjectedDemand(Integer projectedDemand) { iProjectedDemand = projectedDemand; }

	public Integer getNbrExpectedStudents() { return iNbrExpectedStudents; }
	public void setNbrExpectedStudents(Integer nbrExpectedStudents) { iNbrExpectedStudents = nbrExpectedStudents; }

	public Integer getDemand() { return iDemand; }
	public void setDemand(Integer demand) { iDemand = demand; }

	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public SubjectArea getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(SubjectArea subjectArea) { iSubjectArea = subjectArea; }

	public CourseOffering getCourseOffering() { return iCourseOffering; }
	public void setCourseOffering(CourseOffering courseOffering) { iCourseOffering = courseOffering; }

	public PitInstructionalOffering getPitInstructionalOffering() { return iPitInstructionalOffering; }
	public void setPitInstructionalOffering(PitInstructionalOffering pitInstructionalOffering) { iPitInstructionalOffering = pitInstructionalOffering; }

	public CourseType getCourseType() { return iCourseType; }
	public void setCourseType(CourseType courseType) { iCourseType = courseType; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitCourseOffering)) return false;
		if (getUniqueId() == null || ((PitCourseOffering)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitCourseOffering)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
