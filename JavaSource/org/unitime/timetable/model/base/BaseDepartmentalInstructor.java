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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Roles;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseDepartmentalInstructor extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iExternalUniqueId;
	private String iCareerAcct;
	private String iFirstName;
	private String iMiddleName;
	private String iLastName;
	private String iAcademicTitle;
	private String iNote;
	private String iEmail;
	private Boolean iIgnoreToFar;
	private Float iMaxLoad;
	private String iUnavailableDays;
	private Integer iUnavailableOffset;

	private PositionType iPositionType;
	private Department iDepartment;
	private Roles iRole;
	private PreferenceLevel iTeachingPreference;
	private Set<ClassInstructor> iClasses;
	private Set<Exam> iExams;
	private Set<Assignment> iAssignments;
	private Set<OfferingCoordinator> iOfferingCoordinators;
	private Set<InstructorAttribute> iAttributes;

	public BaseDepartmentalInstructor() {
	}

	public BaseDepartmentalInstructor(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "career_acct", nullable = true, length = 20)
	public String getCareerAcct() { return iCareerAcct; }
	public void setCareerAcct(String careerAcct) { iCareerAcct = careerAcct; }

	@Column(name = "fname", nullable = true, length = 100)
	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	@Column(name = "mname", nullable = true, length = 100)
	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	@Column(name = "lname", nullable = false, length = 100)
	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	@Column(name = "acad_title", nullable = true, length = 50)
	public String getAcademicTitle() { return iAcademicTitle; }
	public void setAcademicTitle(String academicTitle) { iAcademicTitle = academicTitle; }

	@Column(name = "note", nullable = true, length = 2048)
	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	@Column(name = "email", nullable = true, length = 200)
	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	@Column(name = "ignore_too_far", nullable = false)
	public Boolean isIgnoreToFar() { return iIgnoreToFar; }
	@Transient
	public Boolean getIgnoreToFar() { return iIgnoreToFar; }
	public void setIgnoreToFar(Boolean ignoreToFar) { iIgnoreToFar = ignoreToFar; }

	@Column(name = "max_load", nullable = true)
	public Float getMaxLoad() { return iMaxLoad; }
	public void setMaxLoad(Float maxLoad) { iMaxLoad = maxLoad; }

	@Column(name = "unavailable_days", nullable = true, length = 366)
	public String getUnavailableDays() { return iUnavailableDays; }
	public void setUnavailableDays(String unavailableDays) { iUnavailableDays = unavailableDays; }

	@Column(name = "unavailable_offset", nullable = true)
	public Integer getUnavailableOffset() { return iUnavailableOffset; }
	public void setUnavailableOffset(Integer unavailableOffset) { iUnavailableOffset = unavailableOffset; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "pos_code_type", nullable = true)
	public PositionType getPositionType() { return iPositionType; }
	public void setPositionType(PositionType positionType) { iPositionType = positionType; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "department_uniqueid", nullable = false)
	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "role_id", nullable = true)
	public Roles getRole() { return iRole; }
	public void setRole(Roles role) { iRole = role; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "teaching_pref_id", nullable = true)
	public PreferenceLevel getTeachingPreference() { return iTeachingPreference; }
	public void setTeachingPreference(PreferenceLevel teachingPreference) { iTeachingPreference = teachingPreference; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instructor", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<ClassInstructor> getClasses() { return iClasses; }
	public void setClasses(Set<ClassInstructor> classes) { iClasses = classes; }
	public void addToclasses(ClassInstructor classInstructor) {
		if (iClasses == null) iClasses = new HashSet<ClassInstructor>();
		iClasses.add(classInstructor);
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "exam_instructor",
		joinColumns = { @JoinColumn(name = "instructor_id") },
		inverseJoinColumns = { @JoinColumn(name = "exam_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<Exam> getExams() { return iExams; }
	public void setExams(Set<Exam> exams) { iExams = exams; }
	public void addToexams(Exam exam) {
		if (iExams == null) iExams = new HashSet<Exam>();
		iExams.add(exam);
	}

	@ManyToMany(mappedBy = "instructors")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToassignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet<Assignment>();
		iAssignments.add(assignment);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instructor")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<OfferingCoordinator> getOfferingCoordinators() { return iOfferingCoordinators; }
	public void setOfferingCoordinators(Set<OfferingCoordinator> offeringCoordinators) { iOfferingCoordinators = offeringCoordinators; }
	public void addToofferingCoordinators(OfferingCoordinator offeringCoordinator) {
		if (iOfferingCoordinators == null) iOfferingCoordinators = new HashSet<OfferingCoordinator>();
		iOfferingCoordinators.add(offeringCoordinator);
	}

	@ManyToMany
	@JoinTable(name = "instructor_attributes",
		joinColumns = { @JoinColumn(name = "instructor_id") },
		inverseJoinColumns = { @JoinColumn(name = "attribute_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<InstructorAttribute> getAttributes() { return iAttributes; }
	public void setAttributes(Set<InstructorAttribute> attributes) { iAttributes = attributes; }
	public void addToattributes(InstructorAttribute instructorAttribute) {
		if (iAttributes == null) iAttributes = new HashSet<InstructorAttribute>();
		iAttributes.add(instructorAttribute);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof DepartmentalInstructor)) return false;
		if (getUniqueId() == null || ((DepartmentalInstructor)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((DepartmentalInstructor)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "DepartmentalInstructor["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "DepartmentalInstructor[" +
			"\n	AcademicTitle: " + getAcademicTitle() +
			"\n	CareerAcct: " + getCareerAcct() +
			"\n	Department: " + getDepartment() +
			"\n	Email: " + getEmail() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FirstName: " + getFirstName() +
			"\n	IgnoreToFar: " + getIgnoreToFar() +
			"\n	LastName: " + getLastName() +
			"\n	MaxLoad: " + getMaxLoad() +
			"\n	MiddleName: " + getMiddleName() +
			"\n	Note: " + getNote() +
			"\n	PositionType: " + getPositionType() +
			"\n	Role: " + getRole() +
			"\n	TeachingPreference: " + getTeachingPreference() +
			"\n	UnavailableDays: " + getUnavailableDays() +
			"\n	UnavailableOffset: " + getUnavailableOffset() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
