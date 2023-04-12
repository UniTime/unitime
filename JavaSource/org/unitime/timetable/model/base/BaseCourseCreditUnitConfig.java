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

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.CourseCreditFormat;
import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.SchedulingSubpart;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseCourseCreditUnitConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Boolean iDefinesCreditAtCourseLevel;

	private CourseCreditFormat iCourseCreditFormat;
	private CourseCreditType iCreditType;
	private CourseCreditUnitType iCreditUnitType;
	private SchedulingSubpart iSubpartOwner;
	private CourseOffering iCourseOwner;

	public BaseCourseCreditUnitConfig() {
	}

	public BaseCourseCreditUnitConfig(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "course_credit_unit_config_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "crs_credit_unig_cfg_seq")
	})
	@GeneratedValue(generator = "course_credit_unit_config_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "defines_credit_at_course_level", nullable = false)
	public Boolean isDefinesCreditAtCourseLevel() { return iDefinesCreditAtCourseLevel; }
	@Transient
	public Boolean getDefinesCreditAtCourseLevel() { return iDefinesCreditAtCourseLevel; }
	public void setDefinesCreditAtCourseLevel(Boolean definesCreditAtCourseLevel) { iDefinesCreditAtCourseLevel = definesCreditAtCourseLevel; }

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinFormula("(select f.uniqueid from %SCHEMA%.crse_credit_format f where f.reference = credit_format)")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public CourseCreditFormat getCourseCreditFormat() { return iCourseCreditFormat; }
	public void setCourseCreditFormat(CourseCreditFormat courseCreditFormat) { iCourseCreditFormat = courseCreditFormat; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "credit_type", nullable = false)
	public CourseCreditType getCreditType() { return iCreditType; }
	public void setCreditType(CourseCreditType creditType) { iCreditType = creditType; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "credit_unit_type", nullable = false)
	public CourseCreditUnitType getCreditUnitType() { return iCreditUnitType; }
	public void setCreditUnitType(CourseCreditUnitType creditUnitType) { iCreditUnitType = creditUnitType; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "owner_id", nullable = true)
	public SchedulingSubpart getSubpartOwner() { return iSubpartOwner; }
	public void setSubpartOwner(SchedulingSubpart subpartOwner) { iSubpartOwner = subpartOwner; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "course_id", nullable = true)
	public CourseOffering getCourseOwner() { return iCourseOwner; }
	public void setCourseOwner(CourseOffering courseOwner) { iCourseOwner = courseOwner; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseCreditUnitConfig)) return false;
		if (getUniqueId() == null || ((CourseCreditUnitConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseCreditUnitConfig)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "CourseCreditUnitConfig["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseCreditUnitConfig[" +
			"\n	CourseOwner: " + getCourseOwner() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	DefinesCreditAtCourseLevel: " + getDefinesCreditAtCourseLevel() +
			"\n	SubpartOwner: " + getSubpartOwner() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
