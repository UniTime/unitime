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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentEnrollmentMessage;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseCourseDemand implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iPriority;
	private Boolean iWaitlist;
	private Boolean iNoSub;
	private Boolean iAlternative;
	private Date iTimestamp;
	private Integer iCritical;
	private Integer iCriticalOverride;
	private Date iWaitlistedTimeStamp;
	private String iChangedBy;

	private Student iStudent;
	private CourseOffering iWaitListSwapWithCourseOffering;
	private FreeTime iFreeTime;
	private Set<CourseRequest> iCourseRequests;
	private Set<StudentEnrollmentMessage> iEnrollmentMessages;

	public BaseCourseDemand() {
	}

	public BaseCourseDemand(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "course_demand_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "course_demand_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "priority", nullable = false, length = 10)
	public Integer getPriority() { return iPriority; }
	public void setPriority(Integer priority) { iPriority = priority; }

	@Column(name = "waitlist", nullable = false)
	public Boolean isWaitlist() { return iWaitlist; }
	@Transient
	public Boolean getWaitlist() { return iWaitlist; }
	public void setWaitlist(Boolean waitlist) { iWaitlist = waitlist; }

	@Column(name = "nosub", nullable = true)
	public Boolean isNoSub() { return iNoSub; }
	@Transient
	public Boolean getNoSub() { return iNoSub; }
	public void setNoSub(Boolean noSub) { iNoSub = noSub; }

	@Column(name = "is_alternative", nullable = false)
	public Boolean isAlternative() { return iAlternative; }
	@Transient
	public Boolean getAlternative() { return iAlternative; }
	public void setAlternative(Boolean alternative) { iAlternative = alternative; }

	@Column(name = "timestamp", nullable = false)
	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	@Column(name = "critical", nullable = true)
	public Integer getCritical() { return iCritical; }
	public void setCritical(Integer critical) { iCritical = critical; }

	@Column(name = "critical_override", nullable = true)
	public Integer getCriticalOverride() { return iCriticalOverride; }
	public void setCriticalOverride(Integer criticalOverride) { iCriticalOverride = criticalOverride; }

	@Column(name = "waitlist_ts", nullable = true)
	public Date getWaitlistedTimeStamp() { return iWaitlistedTimeStamp; }
	public void setWaitlistedTimeStamp(Date waitlistedTimeStamp) { iWaitlistedTimeStamp = waitlistedTimeStamp; }

	@Column(name = "changed_by", nullable = true, length = 40)
	public String getChangedBy() { return iChangedBy; }
	public void setChangedBy(String changedBy) { iChangedBy = changedBy; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "wl_course_swap_id", nullable = true)
	public CourseOffering getWaitListSwapWithCourseOffering() { return iWaitListSwapWithCourseOffering; }
	public void setWaitListSwapWithCourseOffering(CourseOffering waitListSwapWithCourseOffering) { iWaitListSwapWithCourseOffering = waitListSwapWithCourseOffering; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "free_time_id", nullable = true)
	public FreeTime getFreeTime() { return iFreeTime; }
	public void setFreeTime(FreeTime freeTime) { iFreeTime = freeTime; }

	@OneToMany(mappedBy = "courseDemand", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<CourseRequest> getCourseRequests() { return iCourseRequests; }
	public void setCourseRequests(Set<CourseRequest> courseRequests) { iCourseRequests = courseRequests; }
	public void addTocourseRequests(CourseRequest courseRequest) {
		if (iCourseRequests == null) iCourseRequests = new HashSet<CourseRequest>();
		iCourseRequests.add(courseRequest);
	}

	@OneToMany(mappedBy = "courseDemand", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<StudentEnrollmentMessage> getEnrollmentMessages() { return iEnrollmentMessages; }
	public void setEnrollmentMessages(Set<StudentEnrollmentMessage> enrollmentMessages) { iEnrollmentMessages = enrollmentMessages; }
	public void addToenrollmentMessages(StudentEnrollmentMessage studentEnrollmentMessage) {
		if (iEnrollmentMessages == null) iEnrollmentMessages = new HashSet<StudentEnrollmentMessage>();
		iEnrollmentMessages.add(studentEnrollmentMessage);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseDemand)) return false;
		if (getUniqueId() == null || ((CourseDemand)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseDemand)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "CourseDemand["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseDemand[" +
			"\n	Alternative: " + getAlternative() +
			"\n	ChangedBy: " + getChangedBy() +
			"\n	Critical: " + getCritical() +
			"\n	CriticalOverride: " + getCriticalOverride() +
			"\n	FreeTime: " + getFreeTime() +
			"\n	NoSub: " + getNoSub() +
			"\n	Priority: " + getPriority() +
			"\n	Student: " + getStudent() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	WaitListSwapWithCourseOffering: " + getWaitListSwapWithCourseOffering() +
			"\n	Waitlist: " + getWaitlist() +
			"\n	WaitlistedTimeStamp: " + getWaitlistedTimeStamp() +
			"]";
	}
}
