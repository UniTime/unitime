/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Designator;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.PreferenceGroup;

public abstract class BaseDepartmentalInstructor extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iExternalUniqueId;
	private String iCareerAcct;
	private String iFirstName;
	private String iMiddleName;
	private String iLastName;
	private String iNote;
	private String iEmail;
	private Boolean iIgnoreToFar;

	private PositionType iPositionType;
	private Department iDepartment;
	private Set<ClassInstructor> iClasses;
	private Set<Exam> iExams;
	private Set<Designator> iDesignatorSubjectAreas;
	private Set<Assignment> iAssignments;

	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_CAREER_ACCT = "careerAcct";
	public static String PROP_FNAME = "firstName";
	public static String PROP_MNAME = "middleName";
	public static String PROP_LNAME = "lastName";
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

	public Set<Designator> getDesignatorSubjectAreas() { return iDesignatorSubjectAreas; }
	public void setDesignatorSubjectAreas(Set<Designator> designatorSubjectAreas) { iDesignatorSubjectAreas = designatorSubjectAreas; }
	public void addTodesignatorSubjectAreas(Designator designator) {
		if (iDesignatorSubjectAreas == null) iDesignatorSubjectAreas = new HashSet<Designator>();
		iDesignatorSubjectAreas.add(designator);
	}

	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToassignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet<Assignment>();
		iAssignments.add(assignment);
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
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
