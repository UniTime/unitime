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
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.TeachingResponsibility;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseTeachingRequest extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iNbrInstructors;
	private Float iTeachingLoad;
	private Boolean iAssignCoordinator;
	private Integer iPercentShare;

	private InstructionalOffering iOffering;
	private PreferenceLevel iSameCoursePreference;
	private PreferenceLevel iSameCommonPart;
	private TeachingResponsibility iResponsibility;
	private Set<DepartmentalInstructor> iAssignedInstructors;
	private Set<TeachingClassRequest> iClassRequests;

	public BaseTeachingRequest() {
	}

	public BaseTeachingRequest(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "nbr_instructors", nullable = false)
	public Integer getNbrInstructors() { return iNbrInstructors; }
	public void setNbrInstructors(Integer nbrInstructors) { iNbrInstructors = nbrInstructors; }

	@Column(name = "teaching_load", nullable = false)
	public Float getTeachingLoad() { return iTeachingLoad; }
	public void setTeachingLoad(Float teachingLoad) { iTeachingLoad = teachingLoad; }

	@Column(name = "assign_coordinator", nullable = false)
	public Boolean isAssignCoordinator() { return iAssignCoordinator; }
	@Transient
	public Boolean getAssignCoordinator() { return iAssignCoordinator; }
	public void setAssignCoordinator(Boolean assignCoordinator) { iAssignCoordinator = assignCoordinator; }

	@Column(name = "percent_share", nullable = false, length = 3)
	public Integer getPercentShare() { return iPercentShare; }
	public void setPercentShare(Integer percentShare) { iPercentShare = percentShare; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "offering_id", nullable = false)
	public InstructionalOffering getOffering() { return iOffering; }
	public void setOffering(InstructionalOffering offering) { iOffering = offering; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "same_course_pref", nullable = true)
	public PreferenceLevel getSameCoursePreference() { return iSameCoursePreference; }
	public void setSameCoursePreference(PreferenceLevel sameCoursePreference) { iSameCoursePreference = sameCoursePreference; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "same_common_pref", nullable = true)
	public PreferenceLevel getSameCommonPart() { return iSameCommonPart; }
	public void setSameCommonPart(PreferenceLevel sameCommonPart) { iSameCommonPart = sameCommonPart; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "responsibility_id", nullable = true)
	public TeachingResponsibility getResponsibility() { return iResponsibility; }
	public void setResponsibility(TeachingResponsibility responsibility) { iResponsibility = responsibility; }

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "teachreq_instructor",
		joinColumns = { @JoinColumn(name = "request_id") },
		inverseJoinColumns = { @JoinColumn(name = "instructor_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<DepartmentalInstructor> getAssignedInstructors() { return iAssignedInstructors; }
	public void setAssignedInstructors(Set<DepartmentalInstructor> assignedInstructors) { iAssignedInstructors = assignedInstructors; }
	public void addToassignedInstructors(DepartmentalInstructor departmentalInstructor) {
		if (iAssignedInstructors == null) iAssignedInstructors = new HashSet<DepartmentalInstructor>();
		iAssignedInstructors.add(departmentalInstructor);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "teachingRequest", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<TeachingClassRequest> getClassRequests() { return iClassRequests; }
	public void setClassRequests(Set<TeachingClassRequest> classRequests) { iClassRequests = classRequests; }
	public void addToclassRequests(TeachingClassRequest teachingClassRequest) {
		if (iClassRequests == null) iClassRequests = new HashSet<TeachingClassRequest>();
		iClassRequests.add(teachingClassRequest);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof TeachingRequest)) return false;
		if (getUniqueId() == null || ((TeachingRequest)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((TeachingRequest)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "TeachingRequest["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "TeachingRequest[" +
			"\n	AssignCoordinator: " + getAssignCoordinator() +
			"\n	NbrInstructors: " + getNbrInstructors() +
			"\n	Offering: " + getOffering() +
			"\n	PercentShare: " + getPercentShare() +
			"\n	Responsibility: " + getResponsibility() +
			"\n	SameCommonPart: " + getSameCommonPart() +
			"\n	SameCoursePreference: " + getSameCoursePreference() +
			"\n	TeachingLoad: " + getTeachingLoad() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
