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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitClassEvent;
import org.unitime.timetable.model.PitClassInstructor;
import org.unitime.timetable.model.PitSchedulingSubpart;
import org.unitime.timetable.model.PitStudentClassEnrollment;
import org.unitime.timetable.model.TimePattern;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BasePitClass implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iLimit;
	private Integer iNbrRooms;
	private Boolean iEnabledForStudentScheduling;
	private Integer iSectionNumber;
	private String iClassSuffix;
	private Long iUniqueIdRolledForwardFrom;
	private String iExternalUniqueId;
	private Integer iEnrollment;

	private Department iControllingDept;
	private Class_ iClazz;
	private Department iManagingDept;
	private PitSchedulingSubpart iPitSchedulingSubpart;
	private PitClass iPitParentClass;
	private DatePattern iDatePattern;
	private TimePattern iTimePattern;
	private Department iFundingDept;
	private Set<PitClass> iPitChildClasses;
	private Set<PitClassInstructor> iPitClassInstructors;
	private Set<PitStudentClassEnrollment> iStudentEnrollments;
	private Set<PitClassEvent> iPitClassEvents;

	public BasePitClass() {
	}

	public BasePitClass(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "pit_class_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "point_in_time_seq")
	})
	@GeneratedValue(generator = "pit_class_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "class_limit", nullable = true, length = 10)
	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	@Column(name = "nbr_rooms", nullable = true, length = 4)
	public Integer getNbrRooms() { return iNbrRooms; }
	public void setNbrRooms(Integer nbrRooms) { iNbrRooms = nbrRooms; }

	@Column(name = "enabled_for_stu_sched", nullable = false)
	public Boolean isEnabledForStudentScheduling() { return iEnabledForStudentScheduling; }
	@Transient
	public Boolean getEnabledForStudentScheduling() { return iEnabledForStudentScheduling; }
	public void setEnabledForStudentScheduling(Boolean enabledForStudentScheduling) { iEnabledForStudentScheduling = enabledForStudentScheduling; }

	@Column(name = "section_number", nullable = true, length = 5)
	public Integer getSectionNumber() { return iSectionNumber; }
	public void setSectionNumber(Integer sectionNumber) { iSectionNumber = sectionNumber; }

	@Column(name = "class_suffix", nullable = true, length = 40)
	public String getClassSuffix() { return iClassSuffix; }
	public void setClassSuffix(String classSuffix) { iClassSuffix = classSuffix; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Formula("(select count(e.pit_student_id) from %SCHEMA%.pit_student_class_enrl e where e.pit_class_id = uniqueid)")
	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	@ManyToOne
	@JoinFormula(" ( select sa.department_uniqueid from %SCHEMA%.pit_sched_subpart ss, %SCHEMA%.pit_instr_offer_config ioc, %SCHEMA%.pit_instr_offering io, %SCHEMA%.pit_course_offering co, %SCHEMA%.subject_area sa where ss.uniqueid = pit_subpart_id and ioc.uniqueid = ss.pit_config_id and io.uniqueid = ioc.pit_instr_offr_id and co.pit_instr_offr_id = io.uniqueid and co.is_control = %TRUE% and sa.uniqueid = co.subject_area_id ) ")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Department getControllingDept() { return iControllingDept; }
	public void setControllingDept(Department controllingDept) { iControllingDept = controllingDept; }

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "class_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Class_ getClazz() { return iClazz; }
	public void setClazz(Class_ clazz) { iClazz = clazz; }

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "managing_dept", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Department getManagingDept() { return iManagingDept; }
	public void setManagingDept(Department managingDept) { iManagingDept = managingDept; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "pit_subpart_id", nullable = false)
	public PitSchedulingSubpart getPitSchedulingSubpart() { return iPitSchedulingSubpart; }
	public void setPitSchedulingSubpart(PitSchedulingSubpart pitSchedulingSubpart) { iPitSchedulingSubpart = pitSchedulingSubpart; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "pit_parent_id", nullable = true)
	public PitClass getPitParentClass() { return iPitParentClass; }
	public void setPitParentClass(PitClass pitParentClass) { iPitParentClass = pitParentClass; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "date_pattern_id", nullable = true)
	public DatePattern getDatePattern() { return iDatePattern; }
	public void setDatePattern(DatePattern datePattern) { iDatePattern = datePattern; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "time_pattern_id", nullable = true)
	public TimePattern getTimePattern() { return iTimePattern; }
	public void setTimePattern(TimePattern timePattern) { iTimePattern = timePattern; }

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "funding_dept_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Department getFundingDept() { return iFundingDept; }
	public void setFundingDept(Department fundingDept) { iFundingDept = fundingDept; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitParentClass")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	@Cascade(value = org.hibernate.annotations.CascadeType.SAVE_UPDATE)
	public Set<PitClass> getPitChildClasses() { return iPitChildClasses; }
	public void setPitChildClasses(Set<PitClass> pitChildClasses) { iPitChildClasses = pitChildClasses; }
	public void addTopitChildClasses(PitClass pitClass) {
		if (iPitChildClasses == null) iPitChildClasses = new HashSet<PitClass>();
		iPitChildClasses.add(pitClass);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitClassInstructing", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<PitClassInstructor> getPitClassInstructors() { return iPitClassInstructors; }
	public void setPitClassInstructors(Set<PitClassInstructor> pitClassInstructors) { iPitClassInstructors = pitClassInstructors; }
	public void addTopitClassInstructors(PitClassInstructor pitClassInstructor) {
		if (iPitClassInstructors == null) iPitClassInstructors = new HashSet<PitClassInstructor>();
		iPitClassInstructors.add(pitClassInstructor);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitClass", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<PitStudentClassEnrollment> getStudentEnrollments() { return iStudentEnrollments; }
	public void setStudentEnrollments(Set<PitStudentClassEnrollment> studentEnrollments) { iStudentEnrollments = studentEnrollments; }
	public void addTostudentEnrollments(PitStudentClassEnrollment pitStudentClassEnrollment) {
		if (iStudentEnrollments == null) iStudentEnrollments = new HashSet<PitStudentClassEnrollment>();
		iStudentEnrollments.add(pitStudentClassEnrollment);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "pitClass", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<PitClassEvent> getPitClassEvents() { return iPitClassEvents; }
	public void setPitClassEvents(Set<PitClassEvent> pitClassEvents) { iPitClassEvents = pitClassEvents; }
	public void addTopitClassEvents(PitClassEvent pitClassEvent) {
		if (iPitClassEvents == null) iPitClassEvents = new HashSet<PitClassEvent>();
		iPitClassEvents.add(pitClassEvent);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PitClass)) return false;
		if (getUniqueId() == null || ((PitClass)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((PitClass)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "PitClass["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "PitClass[" +
			"\n	ClassSuffix: " + getClassSuffix() +
			"\n	Clazz: " + getClazz() +
			"\n	DatePattern: " + getDatePattern() +
			"\n	EnabledForStudentScheduling: " + getEnabledForStudentScheduling() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FundingDept: " + getFundingDept() +
			"\n	Limit: " + getLimit() +
			"\n	ManagingDept: " + getManagingDept() +
			"\n	NbrRooms: " + getNbrRooms() +
			"\n	PitParentClass: " + getPitParentClass() +
			"\n	PitSchedulingSubpart: " + getPitSchedulingSubpart() +
			"\n	SectionNumber: " + getSectionNumber() +
			"\n	TimePattern: " + getTimePattern() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
