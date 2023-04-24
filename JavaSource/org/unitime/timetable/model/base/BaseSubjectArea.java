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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseSubjectArea implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iSubjectAreaAbbreviation;
	private String iTitle;

	private Session iSession;
	private Department iDepartment;
	private Department iFundingDept;
	private Set<CourseOffering> iCourseOfferings;

	public BaseSubjectArea() {
	}

	public BaseSubjectArea(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "subject_area_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "subject_area_seq")
	})
	@GeneratedValue(generator = "subject_area_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "subject_area_abbreviation", nullable = false, length = 20)
	public String getSubjectAreaAbbreviation() { return iSubjectAreaAbbreviation; }
	public void setSubjectAreaAbbreviation(String subjectAreaAbbreviation) { iSubjectAreaAbbreviation = subjectAreaAbbreviation; }

	@Column(name = "long_title", nullable = false, length = 100)
	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "department_uniqueid", nullable = false)
	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	@ManyToOne(optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "funding_dept_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Department getFundingDept() { return iFundingDept; }
	public void setFundingDept(Department fundingDept) { iFundingDept = fundingDept; }

	@OneToMany(mappedBy = "subjectArea", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<CourseOffering> getCourseOfferings() { return iCourseOfferings; }
	public void setCourseOfferings(Set<CourseOffering> courseOfferings) { iCourseOfferings = courseOfferings; }
	public void addTocourseOfferings(CourseOffering courseOffering) {
		if (iCourseOfferings == null) iCourseOfferings = new HashSet<CourseOffering>();
		iCourseOfferings.add(courseOffering);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SubjectArea)) return false;
		if (getUniqueId() == null || ((SubjectArea)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SubjectArea)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "SubjectArea["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SubjectArea[" +
			"\n	Department: " + getDepartment() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FundingDept: " + getFundingDept() +
			"\n	Session: " + getSession() +
			"\n	SubjectAreaAbbreviation: " + getSubjectAreaAbbreviation() +
			"\n	Title: " + getTitle() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
