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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.AdvisorCourseRequest;
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
@MappedSuperclass
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
	private Float iMaxCredit;
	private Float iMinCredit;
	private Float iOverrideMaxCredit;
	private Integer iOverrideStatus;
	private String iOverrideExternalId;
	private Date iOverrideTimeStamp;
	private Integer iOverrideIntent;
	private String iPin;
	private Boolean iPinReleased;
	private Date iClassStartDate;
	private Date iClassEndDate;
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
	private Set<AdvisorCourseRequest> iAdvisorCourseRequests;

	public BaseStudent() {
	}

	public BaseStudent(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "student_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "student_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "first_name", nullable = false, length = 100)
	public String getFirstName() { return iFirstName; }
	public void setFirstName(String firstName) { iFirstName = firstName; }

	@Column(name = "middle_name", nullable = true, length = 100)
	public String getMiddleName() { return iMiddleName; }
	public void setMiddleName(String middleName) { iMiddleName = middleName; }

	@Column(name = "last_name", nullable = false, length = 100)
	public String getLastName() { return iLastName; }
	public void setLastName(String lastName) { iLastName = lastName; }

	@Column(name = "email", nullable = true, length = 200)
	public String getEmail() { return iEmail; }
	public void setEmail(String email) { iEmail = email; }

	@Column(name = "free_time_cat", nullable = false, length = 10)
	public Integer getFreeTimeCategory() { return iFreeTimeCategory; }
	public void setFreeTimeCategory(Integer freeTimeCategory) { iFreeTimeCategory = freeTimeCategory; }

	@Column(name = "schedule_preference", nullable = false, length = 10)
	public Integer getSchedulePreference() { return iSchedulePreference; }
	public void setSchedulePreference(Integer schedulePreference) { iSchedulePreference = schedulePreference; }

	@Column(name = "max_credit", nullable = true)
	public Float getMaxCredit() { return iMaxCredit; }
	public void setMaxCredit(Float maxCredit) { iMaxCredit = maxCredit; }

	@Column(name = "min_credit", nullable = true)
	public Float getMinCredit() { return iMinCredit; }
	public void setMinCredit(Float minCredit) { iMinCredit = minCredit; }

	@Column(name = "req_credit", nullable = true)
	public Float getOverrideMaxCredit() { return iOverrideMaxCredit; }
	public void setOverrideMaxCredit(Float overrideMaxCredit) { iOverrideMaxCredit = overrideMaxCredit; }

	@Column(name = "req_status", nullable = true, length = 10)
	public Integer getOverrideStatus() { return iOverrideStatus; }
	public void setOverrideStatus(Integer overrideStatus) { iOverrideStatus = overrideStatus; }

	@Column(name = "req_extid", nullable = true, length = 40)
	public String getOverrideExternalId() { return iOverrideExternalId; }
	public void setOverrideExternalId(String overrideExternalId) { iOverrideExternalId = overrideExternalId; }

	@Column(name = "req_ts", nullable = true)
	public Date getOverrideTimeStamp() { return iOverrideTimeStamp; }
	public void setOverrideTimeStamp(Date overrideTimeStamp) { iOverrideTimeStamp = overrideTimeStamp; }

	@Column(name = "req_intent", nullable = true, length = 10)
	public Integer getOverrideIntent() { return iOverrideIntent; }
	public void setOverrideIntent(Integer overrideIntent) { iOverrideIntent = overrideIntent; }

	@Column(name = "pin", nullable = true, length = 40)
	public String getPin() { return iPin; }
	public void setPin(String pin) { iPin = pin; }

	@Column(name = "pin_released", nullable = true)
	public Boolean isPinReleased() { return iPinReleased; }
	@Transient
	public Boolean getPinReleased() { return iPinReleased; }
	public void setPinReleased(Boolean pinReleased) { iPinReleased = pinReleased; }

	@Column(name = "class_start", nullable = true)
	public Date getClassStartDate() { return iClassStartDate; }
	public void setClassStartDate(Date classStartDate) { iClassStartDate = classStartDate; }

	@Column(name = "class_end", nullable = true)
	public Date getClassEndDate() { return iClassEndDate; }
	public void setClassEndDate(Date classEndDate) { iClassEndDate = classEndDate; }

	@Column(name = "schedule_emailed", nullable = true)
	public Date getScheduleEmailedDate() { return iScheduleEmailedDate; }
	public void setScheduleEmailedDate(Date scheduleEmailedDate) { iScheduleEmailedDate = scheduleEmailedDate; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "sect_status", nullable = true)
	public StudentSectioningStatus getSectioningStatus() { return iSectioningStatus; }
	public void setSectioningStatus(StudentSectioningStatus sectioningStatus) { iSectioningStatus = sectioningStatus; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "student", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<StudentAreaClassificationMajor> getAreaClasfMajors() { return iAreaClasfMajors; }
	public void setAreaClasfMajors(Set<StudentAreaClassificationMajor> areaClasfMajors) { iAreaClasfMajors = areaClasfMajors; }
	public void addToAreaClasfMajors(StudentAreaClassificationMajor studentAreaClassificationMajor) {
		if (iAreaClasfMajors == null) iAreaClasfMajors = new HashSet<StudentAreaClassificationMajor>();
		iAreaClasfMajors.add(studentAreaClassificationMajor);
	}
	@Deprecated
	public void addToareaClasfMajors(StudentAreaClassificationMajor studentAreaClassificationMajor) {
		addToAreaClasfMajors(studentAreaClassificationMajor);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "student", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<StudentAreaClassificationMinor> getAreaClasfMinors() { return iAreaClasfMinors; }
	public void setAreaClasfMinors(Set<StudentAreaClassificationMinor> areaClasfMinors) { iAreaClasfMinors = areaClasfMinors; }
	public void addToAreaClasfMinors(StudentAreaClassificationMinor studentAreaClassificationMinor) {
		if (iAreaClasfMinors == null) iAreaClasfMinors = new HashSet<StudentAreaClassificationMinor>();
		iAreaClasfMinors.add(studentAreaClassificationMinor);
	}
	@Deprecated
	public void addToareaClasfMinors(StudentAreaClassificationMinor studentAreaClassificationMinor) {
		addToAreaClasfMinors(studentAreaClassificationMinor);
	}

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "students")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<StudentAccomodation> getAccomodations() { return iAccomodations; }
	public void setAccomodations(Set<StudentAccomodation> accomodations) { iAccomodations = accomodations; }
	public void addToAccomodations(StudentAccomodation studentAccomodation) {
		if (iAccomodations == null) iAccomodations = new HashSet<StudentAccomodation>();
		iAccomodations.add(studentAccomodation);
	}
	@Deprecated
	public void addToaccomodations(StudentAccomodation studentAccomodation) {
		addToAccomodations(studentAccomodation);
	}

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "students")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<StudentGroup> getGroups() { return iGroups; }
	public void setGroups(Set<StudentGroup> groups) { iGroups = groups; }
	public void addToGroups(StudentGroup studentGroup) {
		if (iGroups == null) iGroups = new HashSet<StudentGroup>();
		iGroups.add(studentGroup);
	}
	@Deprecated
	public void addTogroups(StudentGroup studentGroup) {
		addToGroups(studentGroup);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "student", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<WaitList> getWaitlists() { return iWaitlists; }
	public void setWaitlists(Set<WaitList> waitlists) { iWaitlists = waitlists; }
	public void addToWaitlists(WaitList waitList) {
		if (iWaitlists == null) iWaitlists = new HashSet<WaitList>();
		iWaitlists.add(waitList);
	}
	@Deprecated
	public void addTowaitlists(WaitList waitList) {
		addToWaitlists(waitList);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "student", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<CourseDemand> getCourseDemands() { return iCourseDemands; }
	public void setCourseDemands(Set<CourseDemand> courseDemands) { iCourseDemands = courseDemands; }
	public void addToCourseDemands(CourseDemand courseDemand) {
		if (iCourseDemands == null) iCourseDemands = new HashSet<CourseDemand>();
		iCourseDemands.add(courseDemand);
	}
	@Deprecated
	public void addTocourseDemands(CourseDemand courseDemand) {
		addToCourseDemands(courseDemand);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "student", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<StudentClassEnrollment> getClassEnrollments() { return iClassEnrollments; }
	public void setClassEnrollments(Set<StudentClassEnrollment> classEnrollments) { iClassEnrollments = classEnrollments; }
	public void addToClassEnrollments(StudentClassEnrollment studentClassEnrollment) {
		if (iClassEnrollments == null) iClassEnrollments = new HashSet<StudentClassEnrollment>();
		iClassEnrollments.add(studentClassEnrollment);
	}
	@Deprecated
	public void addToclassEnrollments(StudentClassEnrollment studentClassEnrollment) {
		addToClassEnrollments(studentClassEnrollment);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "student", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<LastLikeCourseDemand> getLastLikeCourseDemands() { return iLastLikeCourseDemands; }
	public void setLastLikeCourseDemands(Set<LastLikeCourseDemand> lastLikeCourseDemands) { iLastLikeCourseDemands = lastLikeCourseDemands; }
	public void addToLastLikeCourseDemands(LastLikeCourseDemand lastLikeCourseDemand) {
		if (iLastLikeCourseDemands == null) iLastLikeCourseDemands = new HashSet<LastLikeCourseDemand>();
		iLastLikeCourseDemands.add(lastLikeCourseDemand);
	}
	@Deprecated
	public void addTolastLikeCourseDemands(LastLikeCourseDemand lastLikeCourseDemand) {
		addToLastLikeCourseDemands(lastLikeCourseDemand);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "student", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<StudentNote> getNotes() { return iNotes; }
	public void setNotes(Set<StudentNote> notes) { iNotes = notes; }
	public void addToNotes(StudentNote studentNote) {
		if (iNotes == null) iNotes = new HashSet<StudentNote>();
		iNotes.add(studentNote);
	}
	@Deprecated
	public void addTonotes(StudentNote studentNote) {
		addToNotes(studentNote);
	}

	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "students")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<Advisor> getAdvisors() { return iAdvisors; }
	public void setAdvisors(Set<Advisor> advisors) { iAdvisors = advisors; }
	public void addToAdvisors(Advisor advisor) {
		if (iAdvisors == null) iAdvisors = new HashSet<Advisor>();
		iAdvisors.add(advisor);
	}
	@Deprecated
	public void addToadvisors(Advisor advisor) {
		addToAdvisors(advisor);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "student")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<AdvisorCourseRequest> getAdvisorCourseRequests() { return iAdvisorCourseRequests; }
	public void setAdvisorCourseRequests(Set<AdvisorCourseRequest> advisorCourseRequests) { iAdvisorCourseRequests = advisorCourseRequests; }
	public void addToAdvisorCourseRequests(AdvisorCourseRequest advisorCourseRequest) {
		if (iAdvisorCourseRequests == null) iAdvisorCourseRequests = new HashSet<AdvisorCourseRequest>();
		iAdvisorCourseRequests.add(advisorCourseRequest);
	}
	@Deprecated
	public void addToadvisorCourseRequests(AdvisorCourseRequest advisorCourseRequest) {
		addToAdvisorCourseRequests(advisorCourseRequest);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Student)) return false;
		if (getUniqueId() == null || ((Student)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Student)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "Student["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Student[" +
			"\n	ClassEndDate: " + getClassEndDate() +
			"\n	ClassStartDate: " + getClassStartDate() +
			"\n	Email: " + getEmail() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FirstName: " + getFirstName() +
			"\n	FreeTimeCategory: " + getFreeTimeCategory() +
			"\n	LastName: " + getLastName() +
			"\n	MaxCredit: " + getMaxCredit() +
			"\n	MiddleName: " + getMiddleName() +
			"\n	MinCredit: " + getMinCredit() +
			"\n	OverrideExternalId: " + getOverrideExternalId() +
			"\n	OverrideIntent: " + getOverrideIntent() +
			"\n	OverrideMaxCredit: " + getOverrideMaxCredit() +
			"\n	OverrideStatus: " + getOverrideStatus() +
			"\n	OverrideTimeStamp: " + getOverrideTimeStamp() +
			"\n	Pin: " + getPin() +
			"\n	PinReleased: " + getPinReleased() +
			"\n	ScheduleEmailedDate: " + getScheduleEmailedDate() +
			"\n	SchedulePreference: " + getSchedulePreference() +
			"\n	SectioningStatus: " + getSectioningStatus() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
