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
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.DemandOfferingType;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.OverrideType;
import org.unitime.timetable.model.SubjectArea;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
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
	private Department iFundingDept;
	private CourseOffering iParentOffering;
	private Set<CourseCreditUnitConfig> iCreditConfigs;
	private Set<OverrideType> iDisabledOverrides;

	public BaseCourseOffering() {
	}

	public BaseCourseOffering(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "crs_offr_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

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

	@Formula("(select count(distinct e.student_id) from %SCHEMA%.student_class_enrl e where e.course_offering_id = uniqueid)")
	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	@Column(name = "reservation", nullable = true, length = 10)
	public Integer getReservation() { return iReservation; }
	public void setReservation(Integer reservation) { iReservation = reservation; }

	@Formula(" ( select sa.subject_area_abbreviation from %SCHEMA%.subject_area sa where sa.uniqueid = subject_area_id ) ")
	public String getSubjectAreaAbbv() { return iSubjectAreaAbbv; }
	public void setSubjectAreaAbbv(String subjectAreaAbbv) { iSubjectAreaAbbv = subjectAreaAbbv; }

	@Column(name = "course_nbr", nullable = false, length = 40)
	public String getCourseNbr() { return iCourseNbr; }
	public void setCourseNbr(String courseNbr) { iCourseNbr = courseNbr; }

	@Column(name = "title", nullable = true, length = 200)
	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }

	@Column(name = "schedule_book_note", nullable = true, length = 1000)
	public String getScheduleBookNote() { return iScheduleBookNote; }
	public void setScheduleBookNote(String scheduleBookNote) { iScheduleBookNote = scheduleBookNote; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@Column(name = "snapshot_proj_demand", nullable = true, length = 5)
	public Integer getSnapshotProjectedDemand() { return iSnapshotProjectedDemand; }
	public void setSnapshotProjectedDemand(Integer snapshotProjectedDemand) { iSnapshotProjectedDemand = snapshotProjectedDemand; }

	@Column(name = "snapshot_prj_dmd_date", nullable = true)
	public Date getSnapshotProjectedDemandDate() { return iSnapshotProjectedDemandDate; }
	public void setSnapshotProjectedDemandDate(Date snapshotProjectedDemandDate) { iSnapshotProjectedDemandDate = snapshotProjectedDemandDate; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "subject_area_id", nullable = false)
	public SubjectArea getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(SubjectArea subjectArea) { iSubjectArea = subjectArea; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "instr_offr_id", nullable = false)
	public InstructionalOffering getInstructionalOffering() { return iInstructionalOffering; }
	public void setInstructionalOffering(InstructionalOffering instructionalOffering) { iInstructionalOffering = instructionalOffering; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "demand_offering_id", nullable = true)
	public CourseOffering getDemandOffering() { return iDemandOffering; }
	public void setDemandOffering(CourseOffering demandOffering) { iDemandOffering = demandOffering; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "demand_offering_type", nullable = true)
	public DemandOfferingType getDemandOfferingType() { return iDemandOfferingType; }
	public void setDemandOfferingType(DemandOfferingType demandOfferingType) { iDemandOfferingType = demandOfferingType; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "course_type_id", nullable = true)
	public CourseType getCourseType() { return iCourseType; }
	public void setCourseType(CourseType courseType) { iCourseType = courseType; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "consent_type", nullable = true)
	public OfferingConsentType getConsentType() { return iConsentType; }
	public void setConsentType(OfferingConsentType consentType) { iConsentType = consentType; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "alternative_offering_id", nullable = true)
	public CourseOffering getAlternativeOffering() { return iAlternativeOffering; }
	public void setAlternativeOffering(CourseOffering alternativeOffering) { iAlternativeOffering = alternativeOffering; }

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "funding_dept_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Department getFundingDept() { return iFundingDept; }
	public void setFundingDept(Department fundingDept) { iFundingDept = fundingDept; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_offering_id", nullable = true)
	public CourseOffering getParentOffering() { return iParentOffering; }
	public void setParentOffering(CourseOffering parentOffering) { iParentOffering = parentOffering; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "courseOwner", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<CourseCreditUnitConfig> getCreditConfigs() { return iCreditConfigs; }
	public void setCreditConfigs(Set<CourseCreditUnitConfig> creditConfigs) { iCreditConfigs = creditConfigs; }
	public void addToCreditConfigs(CourseCreditUnitConfig courseCreditUnitConfig) {
		if (iCreditConfigs == null) iCreditConfigs = new HashSet<CourseCreditUnitConfig>();
		iCreditConfigs.add(courseCreditUnitConfig);
	}
	@Deprecated
	public void addTocreditConfigs(CourseCreditUnitConfig courseCreditUnitConfig) {
		addToCreditConfigs(courseCreditUnitConfig);
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "disabled_override",
		joinColumns = { @JoinColumn(name = "course_id") },
		inverseJoinColumns = { @JoinColumn(name = "type_id") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<OverrideType> getDisabledOverrides() { return iDisabledOverrides; }
	public void setDisabledOverrides(Set<OverrideType> disabledOverrides) { iDisabledOverrides = disabledOverrides; }
	public void addToDisabledOverrides(OverrideType overrideType) {
		if (iDisabledOverrides == null) iDisabledOverrides = new HashSet<OverrideType>();
		iDisabledOverrides.add(overrideType);
	}
	@Deprecated
	public void addTodisabledOverrides(OverrideType overrideType) {
		addToDisabledOverrides(overrideType);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseOffering)) return false;
		if (getUniqueId() == null || ((CourseOffering)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseOffering)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
			"\n	FundingDept: " + getFundingDept() +
			"\n	InstructionalOffering: " + getInstructionalOffering() +
			"\n	IsControl: " + getIsControl() +
			"\n	NbrExpectedStudents: " + getNbrExpectedStudents() +
			"\n	ParentOffering: " + getParentOffering() +
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
