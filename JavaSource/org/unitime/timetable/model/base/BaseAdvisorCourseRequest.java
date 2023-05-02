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
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.AdvisorSectioningPref;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.Student;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseAdvisorCourseRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iPriority;
	private Boolean iSubstitute;
	private Integer iAlternative;
	private Date iTimestamp;
	private String iChangedBy;
	private String iCredit;
	private String iCourse;
	private String iNotes;
	private Integer iCritical;
	private Boolean iWaitlist;
	private Boolean iNoSub;

	private Student iStudent;
	private CourseOffering iCourseOffering;
	private FreeTime iFreeTime;
	private Set<AdvisorSectioningPref> iPreferences;

	public BaseAdvisorCourseRequest() {
	}

	public BaseAdvisorCourseRequest(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "advisor_crsreq_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "advisor_crsreq_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "priority", nullable = false, length = 10)
	public Integer getPriority() { return iPriority; }
	public void setPriority(Integer priority) { iPriority = priority; }

	@Column(name = "substitute", nullable = false)
	public Boolean isSubstitute() { return iSubstitute; }
	@Transient
	public Boolean getSubstitute() { return iSubstitute; }
	public void setSubstitute(Boolean substitute) { iSubstitute = substitute; }

	@Column(name = "alternative", nullable = false, length = 10)
	public Integer getAlternative() { return iAlternative; }
	public void setAlternative(Integer alternative) { iAlternative = alternative; }

	@Column(name = "time_stamp", nullable = false)
	public Date getTimestamp() { return iTimestamp; }
	public void setTimestamp(Date timestamp) { iTimestamp = timestamp; }

	@Column(name = "changed_by", nullable = true, length = 40)
	public String getChangedBy() { return iChangedBy; }
	public void setChangedBy(String changedBy) { iChangedBy = changedBy; }

	@Column(name = "credit", nullable = true, length = 10)
	public String getCredit() { return iCredit; }
	public void setCredit(String credit) { iCredit = credit; }

	@Column(name = "course", nullable = true, length = 1024)
	public String getCourse() { return iCourse; }
	public void setCourse(String course) { iCourse = course; }

	@Column(name = "notes", nullable = true, length = 2048)
	public String getNotes() { return iNotes; }
	public void setNotes(String notes) { iNotes = notes; }

	@Column(name = "critical", nullable = true)
	public Integer getCritical() { return iCritical; }
	public void setCritical(Integer critical) { iCritical = critical; }

	@Column(name = "waitlist", nullable = true)
	public Boolean isWaitlist() { return iWaitlist; }
	@Transient
	public Boolean getWaitlist() { return iWaitlist; }
	public void setWaitlist(Boolean waitlist) { iWaitlist = waitlist; }

	@Column(name = "nosub", nullable = true)
	public Boolean isNoSub() { return iNoSub; }
	@Transient
	public Boolean getNoSub() { return iNoSub; }
	public void setNoSub(Boolean noSub) { iNoSub = noSub; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	public Student getStudent() { return iStudent; }
	public void setStudent(Student student) { iStudent = student; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "course_offering_id", nullable = true)
	public CourseOffering getCourseOffering() { return iCourseOffering; }
	public void setCourseOffering(CourseOffering courseOffering) { iCourseOffering = courseOffering; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "free_time_id", nullable = true)
	public FreeTime getFreeTime() { return iFreeTime; }
	public void setFreeTime(FreeTime freeTime) { iFreeTime = freeTime; }

	@OneToMany(mappedBy = "courseRequest", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<AdvisorSectioningPref> getPreferences() { return iPreferences; }
	public void setPreferences(Set<AdvisorSectioningPref> preferences) { iPreferences = preferences; }
	public void addToPreferences(AdvisorSectioningPref advisorSectioningPref) {
		if (iPreferences == null) iPreferences = new HashSet<AdvisorSectioningPref>();
		iPreferences.add(advisorSectioningPref);
	}
	@Deprecated
	public void addTopreferences(AdvisorSectioningPref advisorSectioningPref) {
		addToPreferences(advisorSectioningPref);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof AdvisorCourseRequest)) return false;
		if (getUniqueId() == null || ((AdvisorCourseRequest)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((AdvisorCourseRequest)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "AdvisorCourseRequest["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "AdvisorCourseRequest[" +
			"\n	Alternative: " + getAlternative() +
			"\n	ChangedBy: " + getChangedBy() +
			"\n	Course: " + getCourse() +
			"\n	CourseOffering: " + getCourseOffering() +
			"\n	Credit: " + getCredit() +
			"\n	Critical: " + getCritical() +
			"\n	FreeTime: " + getFreeTime() +
			"\n	NoSub: " + getNoSub() +
			"\n	Notes: " + getNotes() +
			"\n	Priority: " + getPriority() +
			"\n	Student: " + getStudent() +
			"\n	Substitute: " + getSubstitute() +
			"\n	Timestamp: " + getTimestamp() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Waitlist: " + getWaitlist() +
			"]";
	}
}
