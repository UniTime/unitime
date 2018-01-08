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

import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentNote;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.WaitList;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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
	private Set<StudentAreaClassificationMajor> iAreaClasfMajors;
	private Set<StudentAreaClassificationMinor> iAreaClasfMinors;
	private Set<StudentAccomodation> iAccomodations;
	private Set<StudentGroup> iGroups;
	private Set<WaitList> iWaitlists;
	private Set<CourseDemand> iCourseDemands;
	private Set<StudentClassEnrollment> iClassEnrollments;
	private Set<LastLikeCourseDemand> iLastLikeCourseDemands;
	private Set<StudentNote> iNotes;
	private Set<Advisor> iAdvisors;

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

	public Set<StudentAreaClassificationMajor> getAreaClasfMajors() { return iAreaClasfMajors; }
	public void setAreaClasfMajors(Set<StudentAreaClassificationMajor> areaClasfMajors) { iAreaClasfMajors = areaClasfMajors; }
	public void addToareaClasfMajors(StudentAreaClassificationMajor studentAreaClassificationMajor) {
		if (iAreaClasfMajors == null) iAreaClasfMajors = new HashSet<StudentAreaClassificationMajor>();
		iAreaClasfMajors.add(studentAreaClassificationMajor);
	}

	public Set<StudentAreaClassificationMinor> getAreaClasfMinors() { return iAreaClasfMinors; }
	public void setAreaClasfMinors(Set<StudentAreaClassificationMinor> areaClasfMinors) { iAreaClasfMinors = areaClasfMinors; }
	public void addToareaClasfMinors(StudentAreaClassificationMinor studentAreaClassificationMinor) {
		if (iAreaClasfMinors == null) iAreaClasfMinors = new HashSet<StudentAreaClassificationMinor>();
		iAreaClasfMinors.add(studentAreaClassificationMinor);
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

	public Set<StudentNote> getNotes() { return iNotes; }
	public void setNotes(Set<StudentNote> notes) { iNotes = notes; }
	public void addTonotes(StudentNote studentNote) {
		if (iNotes == null) iNotes = new HashSet<StudentNote>();
		iNotes.add(studentNote);
	}

	public Set<Advisor> getAdvisors() { return iAdvisors; }
	public void setAdvisors(Set<Advisor> advisors) { iAdvisors = advisors; }
	public void addToadvisors(Advisor advisor) {
		if (iAdvisors == null) iAdvisors = new HashSet<Advisor>();
		iAdvisors.add(advisor);
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
