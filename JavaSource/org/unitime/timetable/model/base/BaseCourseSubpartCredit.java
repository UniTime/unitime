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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.CourseCatalog;
import org.unitime.timetable.model.CourseSubpartCredit;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseCourseSubpartCredit implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iSubpartId;
	private String iCreditType;
	private String iCreditUnitType;
	private String iCreditFormat;
	private Float iFixedMinimumCredit;
	private Float iMaximumCredit;
	private Boolean iFractionalCreditAllowed;

	private CourseCatalog iCourseCatalog;

	public BaseCourseSubpartCredit() {
	}

	public BaseCourseSubpartCredit(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "course_subpart_credit_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "course_subpart_credit_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "subpart_id", nullable = false, length = 10)
	public String getSubpartId() { return iSubpartId; }
	public void setSubpartId(String subpartId) { iSubpartId = subpartId; }

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
	@JoinColumn(name = "course_catalog_id", nullable = false)
	public CourseCatalog getCourseCatalog() { return iCourseCatalog; }
	public void setCourseCatalog(CourseCatalog courseCatalog) { iCourseCatalog = courseCatalog; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseSubpartCredit)) return false;
		if (getUniqueId() == null || ((CourseSubpartCredit)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseSubpartCredit)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "CourseSubpartCredit["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseSubpartCredit[" +
			"\n	CourseCatalog: " + getCourseCatalog() +
			"\n	CreditFormat: " + getCreditFormat() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	FixedMinimumCredit: " + getFixedMinimumCredit() +
			"\n	FractionalCreditAllowed: " + getFractionalCreditAllowed() +
			"\n	MaximumCredit: " + getMaximumCredit() +
			"\n	SubpartId: " + getSubpartId() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
