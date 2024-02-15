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
import jakarta.persistence.JoinColumn;
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
import org.hibernate.annotations.JoinFormula;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TeachingClassRequest;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseClass_ extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iExpectedCapacity;
	private String iNotes;
	private Integer iNbrRooms;
	private Boolean iRoomsSplitAttendance;
	private Integer iSectionNumberCache;
	private Boolean iDisplayInstructor;
	private String iSchedulePrintNote;
	private String iClassSuffix;
	private Boolean iEnabledForStudentScheduling;
	private Integer iMaxExpectedCapacity;
	private Float iRoomRatio;
	private Long iUniqueIdRolledForwardFrom;
	private String iExternalUniqueId;
	private Integer iEnrollment;
	private Boolean iCancelled;
	private Integer iSnapshotLimit;
	private Date iSnapshotLimitDate;

	private Department iControllingDept;
	private Department iManagingDept;
	private SchedulingSubpart iSchedulingSubpart;
	private Class_ iParentClass;
	private DatePattern iDatePattern;
	private Assignment iCommittedAssignment;
	private LearningManagementSystemInfo iLmsInfo;
	private Department iFundingDept;
	private Set<Class_> iChildClasses;
	private Set<ClassInstructor> iClassInstructors;
	private Set<Assignment> iAssignments;
	private Set<StudentClassEnrollment> iStudentEnrollments;
	private Set<TeachingClassRequest> iTeachingRequests;

	public BaseClass_() {
	}

	public BaseClass_(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "expected_capacity", nullable = false, length = 4)
	public Integer getExpectedCapacity() { return iExpectedCapacity; }
	public void setExpectedCapacity(Integer expectedCapacity) { iExpectedCapacity = expectedCapacity; }

	@Column(name = "notes", nullable = true, length = 1000)
	public String getNotes() { return iNotes; }
	public void setNotes(String notes) { iNotes = notes; }

	@Column(name = "nbr_rooms", nullable = true, length = 4)
	public Integer getNbrRooms() { return iNbrRooms; }
	public void setNbrRooms(Integer nbrRooms) { iNbrRooms = nbrRooms; }

	@Column(name = "rooms_split_att", nullable = true)
	public Boolean isRoomsSplitAttendance() { return iRoomsSplitAttendance; }
	@Transient
	public Boolean getRoomsSplitAttendance() { return iRoomsSplitAttendance; }
	public void setRoomsSplitAttendance(Boolean roomsSplitAttendance) { iRoomsSplitAttendance = roomsSplitAttendance; }

	@Column(name = "section_number", nullable = true, length = 5)
	public Integer getSectionNumberCache() { return iSectionNumberCache; }
	public void setSectionNumberCache(Integer sectionNumberCache) { iSectionNumberCache = sectionNumberCache; }

	@Column(name = "display_instructor", nullable = false)
	public Boolean isDisplayInstructor() { return iDisplayInstructor; }
	@Transient
	public Boolean getDisplayInstructor() { return iDisplayInstructor; }
	public void setDisplayInstructor(Boolean displayInstructor) { iDisplayInstructor = displayInstructor; }

	@Column(name = "sched_print_note", nullable = true, length = 2000)
	public String getSchedulePrintNote() { return iSchedulePrintNote; }
	public void setSchedulePrintNote(String schedulePrintNote) { iSchedulePrintNote = schedulePrintNote; }

	@Column(name = "class_suffix", nullable = true, length = 40)
	public String getClassSuffix() { return iClassSuffix; }
	public void setClassSuffix(String classSuffix) { iClassSuffix = classSuffix; }

	@Column(name = "display_in_sched_book", nullable = false)
	public Boolean isEnabledForStudentScheduling() { return iEnabledForStudentScheduling; }
	@Transient
	public Boolean getEnabledForStudentScheduling() { return iEnabledForStudentScheduling; }
	public void setEnabledForStudentScheduling(Boolean enabledForStudentScheduling) { iEnabledForStudentScheduling = enabledForStudentScheduling; }

	@Column(name = "max_expected_capacity", nullable = false, length = 4)
	public Integer getMaxExpectedCapacity() { return iMaxExpectedCapacity; }
	public void setMaxExpectedCapacity(Integer maxExpectedCapacity) { iMaxExpectedCapacity = maxExpectedCapacity; }

	@Column(name = "room_ratio", nullable = false)
	public Float getRoomRatio() { return iRoomRatio; }
	public void setRoomRatio(Float roomRatio) { iRoomRatio = roomRatio; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Formula("(select count(e.student_id) from %SCHEMA%.student_class_enrl e where e.class_id = uniqueid)")
	public Integer getEnrollment() { return iEnrollment; }
	public void setEnrollment(Integer enrollment) { iEnrollment = enrollment; }

	@Column(name = "cancelled", nullable = false)
	public Boolean isCancelled() { return iCancelled; }
	@Transient
	public Boolean getCancelled() { return iCancelled; }
	public void setCancelled(Boolean cancelled) { iCancelled = cancelled; }

	@Column(name = "snapshot_limit", nullable = true, length = 10)
	public Integer getSnapshotLimit() { return iSnapshotLimit; }
	public void setSnapshotLimit(Integer snapshotLimit) { iSnapshotLimit = snapshotLimit; }

	@Column(name = "snapshot_limit_date", nullable = true)
	public Date getSnapshotLimitDate() { return iSnapshotLimitDate; }
	public void setSnapshotLimitDate(Date snapshotLimitDate) { iSnapshotLimitDate = snapshotLimitDate; }

	@ManyToOne
	@JoinFormula(" ( select sa.department_uniqueid from %SCHEMA%.scheduling_subpart ss, %SCHEMA%.instr_offering_config ioc, %SCHEMA%.instructional_offering io, %SCHEMA%.course_offering co, %SCHEMA%.subject_area sa where ss.uniqueid = subpart_id and ioc.uniqueid = ss.config_id and io.uniqueid = ioc.instr_offr_id and co.instr_offr_id = io.uniqueid and co.is_control = %TRUE% and sa.uniqueid = co.subject_area_id ) ")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Department getControllingDept() { return iControllingDept; }
	public void setControllingDept(Department controllingDept) { iControllingDept = controllingDept; }

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "managing_dept", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Department getManagingDept() { return iManagingDept; }
	public void setManagingDept(Department managingDept) { iManagingDept = managingDept; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "subpart_id", nullable = false)
	public SchedulingSubpart getSchedulingSubpart() { return iSchedulingSubpart; }
	public void setSchedulingSubpart(SchedulingSubpart schedulingSubpart) { iSchedulingSubpart = schedulingSubpart; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_class_id", nullable = true)
	public Class_ getParentClass() { return iParentClass; }
	public void setParentClass(Class_ parentClass) { iParentClass = parentClass; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "date_pattern_id", nullable = true)
	public DatePattern getDatePattern() { return iDatePattern; }
	public void setDatePattern(DatePattern datePattern) { iDatePattern = datePattern; }

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinFormula(" (       select a.uniqueid from        %SCHEMA%.assignment a,        %SCHEMA%.solution s,        %SCHEMA%.department d,       %SCHEMA%.solver_group g      where a.class_id=uniqueid and        a.solution_id=s.uniqueid and        s.commited = %TRUE% and        d.uniqueid=managing_dept and        s.owner_id=g.uniqueid and       d.solver_group_id=g.uniqueid ) ")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Assignment getCommittedAssignment() { return iCommittedAssignment; }
	public void setCommittedAssignment(Assignment committedAssignment) { iCommittedAssignment = committedAssignment; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "lms_info_id", nullable = true)
	public LearningManagementSystemInfo getLmsInfo() { return iLmsInfo; }
	public void setLmsInfo(LearningManagementSystemInfo lmsInfo) { iLmsInfo = lmsInfo; }

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "funding_dept_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Department getFundingDept() { return iFundingDept; }
	public void setFundingDept(Department fundingDept) { iFundingDept = fundingDept; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "parentClass")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<Class_> getChildClasses() { return iChildClasses; }
	public void setChildClasses(Set<Class_> childClasses) { iChildClasses = childClasses; }
	public void addToChildClasses(Class_ class_) {
		if (iChildClasses == null) iChildClasses = new HashSet<Class_>();
		iChildClasses.add(class_);
	}
	@Deprecated
	public void addTochildClasses(Class_ class_) {
		addToChildClasses(class_);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "classInstructing", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<ClassInstructor> getClassInstructors() { return iClassInstructors; }
	public void setClassInstructors(Set<ClassInstructor> classInstructors) { iClassInstructors = classInstructors; }
	public void addToClassInstructors(ClassInstructor classInstructor) {
		if (iClassInstructors == null) iClassInstructors = new HashSet<ClassInstructor>();
		iClassInstructors.add(classInstructor);
	}
	@Deprecated
	public void addToclassInstructors(ClassInstructor classInstructor) {
		addToClassInstructors(classInstructor);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "clazz", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToAssignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet<Assignment>();
		iAssignments.add(assignment);
	}
	@Deprecated
	public void addToassignments(Assignment assignment) {
		addToAssignments(assignment);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "clazz", orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<StudentClassEnrollment> getStudentEnrollments() { return iStudentEnrollments; }
	public void setStudentEnrollments(Set<StudentClassEnrollment> studentEnrollments) { iStudentEnrollments = studentEnrollments; }
	public void addToStudentEnrollments(StudentClassEnrollment studentClassEnrollment) {
		if (iStudentEnrollments == null) iStudentEnrollments = new HashSet<StudentClassEnrollment>();
		iStudentEnrollments.add(studentClassEnrollment);
	}
	@Deprecated
	public void addTostudentEnrollments(StudentClassEnrollment studentClassEnrollment) {
		addToStudentEnrollments(studentClassEnrollment);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "teachingClass", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<TeachingClassRequest> getTeachingRequests() { return iTeachingRequests; }
	public void setTeachingRequests(Set<TeachingClassRequest> teachingRequests) { iTeachingRequests = teachingRequests; }
	public void addToTeachingRequests(TeachingClassRequest teachingClassRequest) {
		if (iTeachingRequests == null) iTeachingRequests = new HashSet<TeachingClassRequest>();
		iTeachingRequests.add(teachingClassRequest);
	}
	@Deprecated
	public void addToteachingRequests(TeachingClassRequest teachingClassRequest) {
		addToTeachingRequests(teachingClassRequest);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Class_)) return false;
		if (getUniqueId() == null || ((Class_)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Class_)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Class_["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Class_[" +
			"\n	Cancelled: " + getCancelled() +
			"\n	ClassSuffix: " + getClassSuffix() +
			"\n	DatePattern: " + getDatePattern() +
			"\n	DisplayInstructor: " + getDisplayInstructor() +
			"\n	EnabledForStudentScheduling: " + getEnabledForStudentScheduling() +
			"\n	ExpectedCapacity: " + getExpectedCapacity() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FundingDept: " + getFundingDept() +
			"\n	LmsInfo: " + getLmsInfo() +
			"\n	ManagingDept: " + getManagingDept() +
			"\n	MaxExpectedCapacity: " + getMaxExpectedCapacity() +
			"\n	NbrRooms: " + getNbrRooms() +
			"\n	Notes: " + getNotes() +
			"\n	ParentClass: " + getParentClass() +
			"\n	RoomRatio: " + getRoomRatio() +
			"\n	RoomsSplitAttendance: " + getRoomsSplitAttendance() +
			"\n	SchedulePrintNote: " + getSchedulePrintNote() +
			"\n	SchedulingSubpart: " + getSchedulingSubpart() +
			"\n	SectionNumberCache: " + getSectionNumberCache() +
			"\n	SnapshotLimit: " + getSnapshotLimit() +
			"\n	SnapshotLimitDate: " + getSnapshotLimitDate() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
