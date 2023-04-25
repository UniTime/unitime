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
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.CourseCatalog;
import org.unitime.timetable.model.CourseSubpartCredit;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseCourseCatalog implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iExternalUniqueId;
	private String iSubject;
	private String iCourseNumber;
	private String iTitle;
	private String iPermanentId;
	private String iApprovalType;
	private String iPreviousSubject;
	private String iPreviousCourseNumber;
	private String iCreditType;
	private String iCreditUnitType;
	private String iCreditFormat;
	private Float iFixedMinimumCredit;
	private Float iMaximumCredit;
	private Boolean iFractionalCreditAllowed;

	private Session iSession;
	private Set<CourseSubpartCredit> iSubparts;

	public BaseCourseCatalog() {
	}

	public BaseCourseCatalog(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "course_catalog_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "course_catalog_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "external_uid", nullable = true, length = 40)
	public String getExternalUniqueId() { return iExternalUniqueId; }
	public void setExternalUniqueId(String externalUniqueId) { iExternalUniqueId = externalUniqueId; }

	@Column(name = "subject", nullable = false, length = 10)
	public String getSubject() { return iSubject; }
	public void setSubject(String subject) { iSubject = subject; }

	@Column(name = "course_nbr", nullable = false, length = 10)
	public String getCourseNumber() { return iCourseNumber; }
	public void setCourseNumber(String courseNumber) { iCourseNumber = courseNumber; }

	@Column(name = "title", nullable = false, length = 100)
	public String getTitle() { return iTitle; }
	public void setTitle(String title) { iTitle = title; }

	@Column(name = "perm_id", nullable = true, length = 20)
	public String getPermanentId() { return iPermanentId; }
	public void setPermanentId(String permanentId) { iPermanentId = permanentId; }

	@Column(name = "approval_type", nullable = true, length = 20)
	public String getApprovalType() { return iApprovalType; }
	public void setApprovalType(String approvalType) { iApprovalType = approvalType; }

	@Column(name = "prev_subject", nullable = true, length = 10)
	public String getPreviousSubject() { return iPreviousSubject; }
	public void setPreviousSubject(String previousSubject) { iPreviousSubject = previousSubject; }

	@Column(name = "prev_crs_nbr", nullable = true, length = 10)
	public String getPreviousCourseNumber() { return iPreviousCourseNumber; }
	public void setPreviousCourseNumber(String previousCourseNumber) { iPreviousCourseNumber = previousCourseNumber; }

	@Column(name = "credit_type", nullable = false, length = 20)
	public String getCreditType() { return iCreditType; }
	public void setCreditType(String creditType) { iCreditType = creditType; }

	@Column(name = "credit_unit_type", nullable = false, length = 20)
	public String getCreditUnitType() { return iCreditUnitType; }
	public void setCreditUnitType(String creditUnitType) { iCreditUnitType = creditUnitType; }

	@Column(name = "credit_format", nullable = false, length = 20)
	public String getCreditFormat() { return iCreditFormat; }
	public void setCreditFormat(String creditFormat) { iCreditFormat = creditFormat; }

	@Column(name = "fixed_min_credit", nullable = false, length = 10)
	public Float getFixedMinimumCredit() { return iFixedMinimumCredit; }
	public void setFixedMinimumCredit(Float fixedMinimumCredit) { iFixedMinimumCredit = fixedMinimumCredit; }

	@Column(name = "max_credit", nullable = true, length = 10)
	public Float getMaximumCredit() { return iMaximumCredit; }
	public void setMaximumCredit(Float maximumCredit) { iMaximumCredit = maximumCredit; }

	@Column(name = "frac_credit_allowed", nullable = true, length = 10)
	public Boolean isFractionalCreditAllowed() { return iFractionalCreditAllowed; }
	@Transient
	public Boolean getFractionalCreditAllowed() { return iFractionalCreditAllowed; }
	public void setFractionalCreditAllowed(Boolean fractionalCreditAllowed) { iFractionalCreditAllowed = fractionalCreditAllowed; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)
	@JoinColumn(name = "course_catalog_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<CourseSubpartCredit> getSubparts() { return iSubparts; }
	public void setSubparts(Set<CourseSubpartCredit> subparts) { iSubparts = subparts; }
	public void addTosubparts(CourseSubpartCredit courseSubpartCredit) {
		if (iSubparts == null) iSubparts = new HashSet<CourseSubpartCredit>();
		iSubparts.add(courseSubpartCredit);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseCatalog)) return false;
		if (getUniqueId() == null || ((CourseCatalog)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseCatalog)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "CourseCatalog["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseCatalog[" +
			"\n	ApprovalType: " + getApprovalType() +
			"\n	CourseNumber: " + getCourseNumber() +
			"\n	CreditFormat: " + getCreditFormat() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	ExternalUniqueId: " + getExternalUniqueId() +
			"\n	FixedMinimumCredit: " + getFixedMinimumCredit() +
			"\n	FractionalCreditAllowed: " + getFractionalCreditAllowed() +
			"\n	MaximumCredit: " + getMaximumCredit() +
			"\n	PermanentId: " + getPermanentId() +
			"\n	PreviousCourseNumber: " + getPreviousCourseNumber() +
			"\n	PreviousSubject: " + getPreviousSubject() +
			"\n	Session: " + getSession() +
			"\n	Subject: " + getSubject() +
			"\n	Title: " + getTitle() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
