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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.StudentSectioningPref;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseCourseRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iOrder;
	private Boolean iAllowOverlap;
	private Integer iCredit;
	private Integer iOverrideStatus;
	private String iOverrideExternalId;
	private Date iOverrideTimeStamp;
	private Integer iOverrideIntent;

	private CourseDemand iCourseDemand;
	private CourseOffering iCourseOffering;
	private Set<CourseRequestOption> iCourseRequestOptions;
	private Set<ClassWaitList> iClassWaitLists;
	private Set<StudentSectioningPref> iPreferences;

	public BaseCourseRequest() {
	}

	public BaseCourseRequest(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "course_request_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "course_request_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "ord", nullable = false, length = 10)
	public Integer getOrder() { return iOrder; }
	public void setOrder(Integer order) { iOrder = order; }

	@Column(name = "allow_overlap", nullable = false)
	public Boolean isAllowOverlap() { return iAllowOverlap; }
	@Transient
	public Boolean getAllowOverlap() { return iAllowOverlap; }
	public void setAllowOverlap(Boolean allowOverlap) { iAllowOverlap = allowOverlap; }

	@Column(name = "credit", nullable = false, length = 10)
	public Integer getCredit() { return iCredit; }
	public void setCredit(Integer credit) { iCredit = credit; }

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

	@ManyToOne(optional = false)
	@JoinColumn(name = "course_demand_id", nullable = false)
	public CourseDemand getCourseDemand() { return iCourseDemand; }
	public void setCourseDemand(CourseDemand courseDemand) { iCourseDemand = courseDemand; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "course_offering_id", nullable = false)
	public CourseOffering getCourseOffering() { return iCourseOffering; }
	public void setCourseOffering(CourseOffering courseOffering) { iCourseOffering = courseOffering; }

	@OneToMany(mappedBy = "courseRequest", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<CourseRequestOption> getCourseRequestOptions() { return iCourseRequestOptions; }
	public void setCourseRequestOptions(Set<CourseRequestOption> courseRequestOptions) { iCourseRequestOptions = courseRequestOptions; }
	public void addTocourseRequestOptions(CourseRequestOption courseRequestOption) {
		if (iCourseRequestOptions == null) iCourseRequestOptions = new HashSet<CourseRequestOption>();
		iCourseRequestOptions.add(courseRequestOption);
	}

	@OneToMany(mappedBy = "courseRequest", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<ClassWaitList> getClassWaitLists() { return iClassWaitLists; }
	public void setClassWaitLists(Set<ClassWaitList> classWaitLists) { iClassWaitLists = classWaitLists; }
	public void addToclassWaitLists(ClassWaitList classWaitList) {
		if (iClassWaitLists == null) iClassWaitLists = new HashSet<ClassWaitList>();
		iClassWaitLists.add(classWaitList);
	}

	@OneToMany(mappedBy = "courseRequest", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<StudentSectioningPref> getPreferences() { return iPreferences; }
	public void setPreferences(Set<StudentSectioningPref> preferences) { iPreferences = preferences; }
	public void addTopreferences(StudentSectioningPref studentSectioningPref) {
		if (iPreferences == null) iPreferences = new HashSet<StudentSectioningPref>();
		iPreferences.add(studentSectioningPref);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseRequest)) return false;
		if (getUniqueId() == null || ((CourseRequest)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseRequest)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "CourseRequest["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseRequest[" +
			"\n	AllowOverlap: " + getAllowOverlap() +
			"\n	CourseDemand: " + getCourseDemand() +
			"\n	CourseOffering: " + getCourseOffering() +
			"\n	Credit: " + getCredit() +
			"\n	Order: " + getOrder() +
			"\n	OverrideExternalId: " + getOverrideExternalId() +
			"\n	OverrideIntent: " + getOverrideIntent() +
			"\n	OverrideStatus: " + getOverrideStatus() +
			"\n	OverrideTimeStamp: " + getOverrideTimeStamp() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
