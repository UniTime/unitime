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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.DemandOfferingType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.SubjectArea;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCourseOffering implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Boolean iIsControl;
	private String iPermId;
	private Integer iProjectedDemand;
	private Integer iNbrExpectedStudents;
	private Integer iDemand;
	private Integer iEnrollment;
	private Integer iReservation;
	private String iSubjectAreaAbbv;
	private String iCourseNbr;
	private String iTitle;
	private String iScheduleBookNote;
	private String iExternalUniqueId;
	private Long iUniqueIdRolledForwardFrom;
	private Integer iSnapshotProjectedDemand;
	private Date iSnapshotProjectedDemandDate;

	private SubjectArea iSubjectArea;
	private InstructionalOffering iInstructionalOffering;
	private CourseOffering iDemandOffering;
	private DemandOfferingType iDemandOfferingType;
	private CourseType iCourseType;
	private OfferingConsentType iConsentType;
	private CourseOffering iAlternativeOffering;
	private Set<CourseCreditUnitConfig> iCreditConfigs;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_IS_CONTROL = "isControl";
	public static String PROP_PERM_ID = "permId";
	public static String PROP_PROJ_DEMAND = "projectedDemand";
	public static String PROP_NBR_EXPECTED_STDENTS = "nbrExpectedStudents";
	public static String PROP_LASTLIKE_DEMAND = "demand";
	public static String PROP_RESERVATION = "reservation";
	public static String PROP_COURSE_NBR = "courseNbr";
	public static String PROP_TITLE = "title";
	public static String PROP_SCHEDULE_BOOK_NOTE = "scheduleBookNote";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_SNAPSHOT_PROJ_DEMAND = "snapshotProjectedDemand";
	public static String PROP_SNAPSHOT_PRJ_DMD_DATE = "snapshotProjectedDemandDate";

	public BaseCourseOffering() {
		initialize();
	}

	public BaseCourseOffering(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

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

	public Integer getReservation() { return iReservation; }
	public void setReservation(Integer reservation) { iReservation = reservation; }

	public String getSubjectAreaAbbv() { return iSubjectAreaAbbv; }
	public void setSubjectAreaAbbv(String subjectAreaAbbv) { iSubjectAreaAbbv = subjectAreaAbbv; }

	public String getCourseNbr() { return iCourseNbr; }
	public void setCourseNbr(String courseNbr) { iCourseNbr = courseNbr; }

	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }

	public String getScheduleBookNote() { return iScheduleBookNote; }
	public void setScheduleBookNote(String scheduleBookNote) { iScheduleBookNote = scheduleBookNote; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public Integer getSnapshotProjectedDemand() { return iSnapshotProjectedDemand; }
	public void setSnapshotProjectedDemand(Integer snapshotProjectedDemand) { iSnapshotProjectedDemand = snapshotProjectedDemand; }

	public Date getSnapshotProjectedDemandDate() { return iSnapshotProjectedDemandDate; }
	public void setSnapshotProjectedDemandDate(Date snapshotProjectedDemandDate) { iSnapshotProjectedDemandDate = snapshotProjectedDemandDate; }

	public SubjectArea getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(SubjectArea subjectArea) { iSubjectArea = subjectArea; }

	public InstructionalOffering getInstructionalOffering() { return iInstructionalOffering; }
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) { iInstructionalOffering = instructionalOffering; }

	public CourseOffering getDemandOffering() { return iDemandOffering; }
	public void setDemandOffering(CourseOffering demandOffering) { iDemandOffering = demandOffering; }

	public DemandOfferingType getDemandOfferingType() { return iDemandOfferingType; }
	public void setDemandOfferingType(DemandOfferingType demandOfferingType) { iDemandOfferingType = demandOfferingType; }

	public CourseType getCourseType() { return iCourseType; }
	public void setCourseType(CourseType courseType) { iCourseType = courseType; }

	public OfferingConsentType getConsentType() { return iConsentType; }
	public void setConsentType(OfferingConsentType consentType) { iConsentType = consentType; }

	public CourseOffering getAlternativeOffering() { return iAlternativeOffering; }
	public void setAlternativeOffering(CourseOffering alternativeOffering) { iAlternativeOffering = alternativeOffering; }

	public Set<CourseCreditUnitConfig> getCreditConfigs() { return iCreditConfigs; }
	public void setCreditConfigs(Set<CourseCreditUnitConfig> creditConfigs) { iCreditConfigs = creditConfigs; }
	public void addTocreditConfigs(CourseCreditUnitConfig courseCreditUnitConfig) {
		if (iCreditConfigs == null) iCreditConfigs = new HashSet<CourseCreditUnitConfig>();
		iCreditConfigs.add(courseCreditUnitConfig);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseOffering)) return false;
		if (getUniqueId() == null || ((CourseOffering)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseOffering)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CourseOffering["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseOffering[" +
			"\n	AlternativeOffering: " + getAlternativeOffering() +
			"\n	ConsentType: " + getConsentType() +
			"\n	CourseNbr: " + getCourseNbr() +
			"\n	CourseType: " + getCourseType() +
			"\n	Demand: " + getDemand() +
			"\n	DemandOffering: " + getDemandOffering() +
			"\n	DemandOfferingType: " + getDemandOfferingType() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	IsControl: " + getIsControl() +
			"\n	NbrExpectedStudents: " + getNbrExpectedStudents() +
			"\n	PermId: " + getPermId() +
			"\n	ProjectedDemand: " + getProjectedDemand() +
			"\n	Reservation: " + getReservation() +
			"\n	ScheduleBookNote: " + getScheduleBookNote() +
			"\n	SnapshotProjectedDemand: " + getSnapshotProjectedDemand() +
			"\n	SnapshotProjectedDemandDate: " + getSnapshotProjectedDemandDate() +
			"\n	SubjectArea: " + getSubjectArea() +
			"\n	Title: " + getTitle() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
