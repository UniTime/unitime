/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.WaitList;

public abstract class BaseStudent implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iFirstName;
	private String iMiddleName;
	private String iLastName;
	private String iEmail;
	private Integer iFreeTimeCategory;
	private Integer iSchedulePreference;
	private Date iScheduleEmailedDate;

	private Session iSession;
	private StudentSectioningStatus iSectioningStatus;
	private Set<AcademicAreaClassification> iAcademicAreaClassifications;
	private Set<PosMajor> iPosMajors;
	private Set<PosMinor> iPosMinors;
	private Set<StudentAccomodation> iAccomodations;
	private Set<StudentGroup> iGroups;
	private Set<WaitList> iWaitlists;
	private Set<CourseDemand> iCourseDemands;
	private Set<StudentClassEnrollment> iClassEnrollments;
	private Set<LastLikeCourseDemand> iLastLikeCourseDemands;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_EXTERNAL_UID = "externalUniqueId";
	public static String PROP_FIRST_NAME = "firstName";
	public static String PROP_MIDDLE_NAME = "middleName";
	public static String PROP_LAST_NAME = "lastName";
	public static String PROP_EMAIL = "email";
	public static String PROP_FREE_TIME_CAT = "freeTimeCategory";
	public static String PROP_SCHEDULE_PREFERENCE = "schedulePreference";
	public static String PROP_SCHEDULE_EMAILED = "scheduleEmailedDate";

	public BaseStudent() {
		initialize();
	}

	public BaseStudent(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	public Integer getFreeTimeCategory() { return iFreeTimeCategory; }
	public void setFreeTimeCategory(Integer freeTimeCategory) { iFreeTimeCategory = freeTimeCategory; }

	public Integer getSchedulePreference() { return iSchedulePreference; }
	public void setSchedulePreference(Integer schedulePreference) { iSchedulePreference = schedulePreference; }

	public Date getScheduleEmailedDate() { return iScheduleEmailedDate; }
	public void setScheduleEmailedDate(Date scheduleEmailedDate) { iScheduleEmailedDate = scheduleEmailedDate; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public StudentSectioningStatus getSectioningStatus() { return iSectioningStatus; }
	public void setSectioningStatus(StudentSectioningStatus sectioningStatus) { iSectioningStatus = sectioningStatus; }

	public Set<AcademicAreaClassification> getAcademicAreaClassifications() { return iAcademicAreaClassifications; }
	public void setAcademicAreaClassifications(Set<AcademicAreaClassification> academicAreaClassifications) { iAcademicAreaClassifications = academicAreaClassifications; }
	public void addToacademicAreaClassifications(AcademicAreaClassification academicAreaClassification) {
		if (iAcademicAreaClassifications == null) iAcademicAreaClassifications = new HashSet<AcademicAreaClassification>();
		iAcademicAreaClassifications.add(academicAreaClassification);
	}

	public Set<PosMajor> getPosMajors() { return iPosMajors; }
	public void setPosMajors(Set<PosMajor> posMajors) { iPosMajors = posMajors; }
	public void addToposMajors(PosMajor posMajor) {
		if (iPosMajors == null) iPosMajors = new HashSet<PosMajor>();
		iPosMajors.add(posMajor);
	}

	public Set<PosMinor> getPosMinors() { return iPosMinors; }
	public void setPosMinors(Set<PosMinor> posMinors) { iPosMinors = posMinors; }
	public void addToposMinors(PosMinor posMinor) {
		if (iPosMinors == null) iPosMinors = new HashSet<PosMinor>();
		iPosMinors.add(posMinor);
	}

	public Set<StudentAccomodation> getAccomodations() { return iAccomodations; }
	public void setAccomodations(Set<StudentAccomodation> accomodations) { iAccomodations = accomodations; }
	public void addToaccomodations(StudentAccomodation studentAccomodation) {
		if (iAccomodations == null) iAccomodations = new HashSet<StudentAccomodation>();
		iAccomodations.add(studentAccomodation);
	}

	public Set<StudentGroup> getGroups() { return iGroups; }
	public void setGroups(Set<StudentGroup> groups) { iGroups = groups; }
	public void addTogroups(StudentGroup studentGroup) {
		if (iGroups == null) iGroups = new HashSet<StudentGroup>();
		iGroups.add(studentGroup);
	}

	public Set<WaitList> getWaitlists() { return iWaitlists; }
	public void setWaitlists(Set<WaitList> waitlists) { iWaitlists = waitlists; }
	public void addTowaitlists(WaitList waitList) {
		if (iWaitlists == null) iWaitlists = new HashSet<WaitList>();
		iWaitlists.add(waitList);
	}

	public Set<CourseDemand> getCourseDemands() { return iCourseDemands; }
	public void setCourseDemands(Set<CourseDemand> courseDemands) { iCourseDemands = courseDemands; }
	public void addTocourseDemands(CourseDemand courseDemand) {
		if (iCourseDemands == null) iCourseDemands = new HashSet<CourseDemand>();
		iCourseDemands.add(courseDemand);
	}

	public Set<StudentClassEnrollment> getClassEnrollments() { return iClassEnrollments; }
	public void setClassEnrollments(Set<StudentClassEnrollment> classEnrollments) { iClassEnrollments = classEnrollments; }
	public void addToclassEnrollments(StudentClassEnrollment studentClassEnrollment) {
		if (iClassEnrollments == null) iClassEnrollments = new HashSet<StudentClassEnrollment>();
		iClassEnrollments.add(studentClassEnrollment);
	}

	public Set<LastLikeCourseDemand> getLastLikeCourseDemands() { return iLastLikeCourseDemands; }
	public void setLastLikeCourseDemands(Set<LastLikeCourseDemand> lastLikeCourseDemands) { iLastLikeCourseDemands = lastLikeCourseDemands; }
	public void addTolastLikeCourseDemands(LastLikeCourseDemand lastLikeCourseDemand) {
		if (iLastLikeCourseDemands == null) iLastLikeCourseDemands = new HashSet<LastLikeCourseDemand>();
		iLastLikeCourseDemands.add(lastLikeCourseDemand);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Student)) return false;
		if (getUniqueId() == null || ((Student)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Student)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Student["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Student[" +
			"\n	Email: " + getEmail() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FirstName: " + getFirstName() +
			"\n	FreeTimeCategory: " + getFreeTimeCategory() +
			"\n	LastName: " + getLastName() +
			"\n	MiddleName: " + getMiddleName() +
			"\n	ScheduleEmailedDate: " + getScheduleEmailedDate() +
			"\n	SchedulePreference: " + getSchedulePreference() +
			"\n	SectioningStatus: " + getSectioningStatus() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
