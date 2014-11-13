/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.PreferenceGroup;
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

	private PositionType iPositionType;
	private Department iDepartment;
	private Roles iRole;
	private Set<ClassInstructor> iClasses;
	private Set<Exam> iExams;
	private Set<Assignment> iAssignments;
	private Set<InstructionalOffering> iOfferings;

	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_CAREER_ACCT = "careerAcct";
	public static String PROP_FNAME = "firstName";
	public static String PROP_MNAME = "middleName";
	public static String PROP_LNAME = "lastName";
	public static String PROP_ACAD_TITLE = "academicTitle";
	public static String PROP_NOTE = "note";
	public static String PROP_EMAIL = "email";
	public static String PROP_IGNORE_TOO_FAR = "ignoreToFar";

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

	public PositionType getPositionType() { return iPositionType; }
	public void setPositionType(PositionType positionType) { iPositionType = positionType; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public Roles getRole() { return iRole; }
	public void setRole(Roles role) { iRole = role; }

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

	public Set<InstructionalOffering> getOfferings() { return iOfferings; }
	public void setOfferings(Set<InstructionalOffering> offerings) { iOfferings = offerings; }
	public void addToofferings(InstructionalOffering instructionalOffering) {
		if (iOfferings == null) iOfferings = new HashSet<InstructionalOffering>();
		iOfferings.add(instructionalOffering);
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
			"\n	MiddleName: " + getMiddleName() +
			"\n	Note: " + getNote() +
			"\n	PositionType: " + getPositionType() +
			"\n	Role: " + getRole() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
