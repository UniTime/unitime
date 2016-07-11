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

	private PositionType iPositionType;
	private Department iDepartment;
	private Roles iRole;
	private PreferenceLevel iTeachingPreference;
	private Set<ClassInstructor> iClasses;
	private Set<Exam> iExams;
	private Set<Assignment> iAssignments;
	private Set<OfferingCoordinator> iOfferingCoordinators;
	private Set<InstructorAttribute> iAttributes;

	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_CAREER_ACCT = "careerAcct";
	public static String PROP_FNAME = "firstName";
	public static String PROP_MNAME = "middleName";
	public static String PROP_LNAME = "lastName";
	public static String PROP_ACAD_TITLE = "academicTitle";
	public static String PROP_NOTE = "note";
	public static String PROP_EMAIL = "email";
	public static String PROP_IGNORE_TOO_FAR = "ignoreToFar";
	public static String PROP_MAX_LOAD = "maxLoad";

	public BaseDepartmentalInstructor() {
		initialize();
	}

	public BaseDepartmentalInstructor(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getCareerAcct() { return iCareerAcct; }
	public void setCareerAcct(String careerAcct) { iCareerAcct = careerAcct; }

	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	public String getAcademicTitle() { return iAcademicTitle; }
	public void setAcademicTitle(String academicTitle) { iAcademicTitle = academicTitle; }

	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	public Boolean isIgnoreToFar() { return iIgnoreToFar; }
	public Boolean getIgnoreToFar() { return iIgnoreToFar; }
	public void setIgnoreToFar(Boolean ignoreToFar) { iIgnoreToFar = ignoreToFar; }

	public Float getMaxLoad() { return iMaxLoad; }
	public void setMaxLoad(Float maxLoad) { iMaxLoad = maxLoad; }

	public PositionType getPositionType() { return iPositionType; }
	public void setPositionType(PositionType positionType) { iPositionType = positionType; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public Roles getRole() { return iRole; }
	public void setRole(Roles role) { iRole = role; }

	public PreferenceLevel getTeachingPreference() { return iTeachingPreference; }
	public void setTeachingPreference(PreferenceLevel teachingPreference) { iTeachingPreference = teachingPreference; }

	public Set<ClassInstructor> getClasses() { return iClasses; }
	public void setClasses(Set<ClassInstructor> classes) { iClasses = classes; }
	public void addToclasses(ClassInstructor classInstructor) {
		if (iClasses == null) iClasses = new HashSet<ClassInstructor>();
		iClasses.add(classInstructor);
	}

	public Set<Exam> getExams() { return iExams; }
	public void setExams(Set<Exam> exams) { iExams = exams; }
	public void addToexams(Exam exam) {
		if (iExams == null) iExams = new HashSet<Exam>();
		iExams.add(exam);
	}

	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToassignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet<Assignment>();
		iAssignments.add(assignment);
	}

	public Set<OfferingCoordinator> getOfferingCoordinators() { return iOfferingCoordinators; }
	public void setOfferingCoordinators(Set<OfferingCoordinator> offeringCoordinators) { iOfferingCoordinators = offeringCoordinators; }
	public void addToofferingCoordinators(OfferingCoordinator offeringCoordinator) {
		if (iOfferingCoordinators == null) iOfferingCoordinators = new HashSet<OfferingCoordinator>();
		iOfferingCoordinators.add(offeringCoordinator);
	}

	public Set<InstructorAttribute> getAttributes() { return iAttributes; }
	public void setAttributes(Set<InstructorAttribute> attributes) { iAttributes = attributes; }
	public void addToattributes(InstructorAttribute instructorAttribute) {
		if (iAttributes == null) iAttributes = new HashSet<InstructorAttribute>();
		iAttributes.add(instructorAttribute);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof DepartmentalInstructor)) return false;
		if (getUniqueId() == null || ((DepartmentalInstructor)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((DepartmentalInstructor)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
