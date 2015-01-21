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
public abstract class BaseCourseCreditUnitConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Boolean iDefinesCreditAtCourseLevel;

	private CourseCreditFormat iCourseCreditFormat;
	private CourseCreditType iCreditType;
	private CourseCreditUnitType iCreditUnitType;
	private SchedulingSubpart iSubpartOwner;
	private CourseOffering iCourseOwner;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_DEFINES_CREDIT_AT_COURSE_LEVEL = "definesCreditAtCourseLevel";

	public BaseCourseCreditUnitConfig() {
		initialize();
	}

	public BaseCourseCreditUnitConfig(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Boolean isDefinesCreditAtCourseLevel() { return iDefinesCreditAtCourseLevel; }
	public Boolean getDefinesCreditAtCourseLevel() { return iDefinesCreditAtCourseLevel; }
	public void setDefinesCreditAtCourseLevel(Boolean definesCreditAtCourseLevel) { iDefinesCreditAtCourseLevel = definesCreditAtCourseLevel; }

	public CourseCreditFormat getCourseCreditFormat() { return iCourseCreditFormat; }
	public void setCourseCreditFormat(CourseCreditFormat courseCreditFormat) { iCourseCreditFormat = courseCreditFormat; }

	public CourseCreditType getCreditType() { return iCreditType; }
	public void setCreditType(CourseCreditType creditType) { iCreditType = creditType; }

	public CourseCreditUnitType getCreditUnitType() { return iCreditUnitType; }
	public void setCreditUnitType(CourseCreditUnitType creditUnitType) { iCreditUnitType = creditUnitType; }

	public SchedulingSubpart getSubpartOwner() { return iSubpartOwner; }
	public void setSubpartOwner(SchedulingSubpart subpartOwner) { iSubpartOwner = subpartOwner; }

	public CourseOffering getCourseOwner() { return iCourseOwner; }
	public void setCourseOwner(CourseOffering courseOwner) { iCourseOwner = courseOwner; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof CourseCreditUnitConfig)) return false;
		if (getUniqueId() == null || ((CourseCreditUnitConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CourseCreditUnitConfig)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "CourseCreditUnitConfig["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "CourseCreditUnitConfig[" +
			"\n	CourseCreditFormat: " + getCourseCreditFormat() +
			"\n	CourseOwner: " + getCourseOwner() +
			"\n	CreditType: " + getCreditType() +
			"\n	CreditUnitType: " + getCreditUnitType() +
			"\n	DefinesCreditAtCourseLevel: " + getDefinesCreditAtCourseLevel() +
			"\n	SubpartOwner: " + getSubpartOwner() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
