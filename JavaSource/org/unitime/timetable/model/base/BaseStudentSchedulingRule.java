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

import org.unitime.timetable.model.StudentSchedulingRule;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseStudentSchedulingRule implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iOrd;
	private String iRuleName;
	private String iStudentFilter;
	private String iFilterInitiative;
	private String iFilterTerm;
	private Integer iFirstYear;
	private Integer iLastYear;
	private String iInstructonalMethod;
	private String iCourseName;
	private String iCourseType;
	private Boolean iDisjunctive;
	private Boolean iAppliesToFilter;
	private Boolean iAppliesToOnline;
	private Boolean iAppliesToBatch;
	private Boolean iAdminOverride;
	private Boolean iAdvisorOverride;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_ORD = "ord";
	public static String PROP_NAME = "ruleName";
	public static String PROP_STUDENT_FILTER = "studentFilter";
	public static String PROP_INITIATIVE = "filterInitiative";
	public static String PROP_TERM = "filterTerm";
	public static String PROP_FIRST_YEAR = "firstYear";
	public static String PROP_LAST_YEAR = "lastYear";
	public static String PROP_INSTR_METHOD = "instructonalMethod";
	public static String PROP_COURSE_NAME = "courseName";
	public static String PROP_COURSE_TYPE = "courseType";
	public static String PROP_DISJUNCTIVE = "disjunctive";
	public static String PROP_APPLY_FILTER = "appliesToFilter";
	public static String PROP_APPLY_ONLINE = "appliesToOnline";
	public static String PROP_APPLY_BATCH = "appliesToBatch";
	public static String PROP_ADMIN_OVERRIDE = "adminOverride";
	public static String PROP_ADVISOR_OVERRIDE = "advisorOverride";

	public BaseStudentSchedulingRule() {
		initialize();
	}

	public BaseStudentSchedulingRule(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getOrd() { return iOrd; }
	public void setOrd(Integer ord) { iOrd = ord; }

	public String getRuleName() { return iRuleName; }
	public void setRuleName(String ruleName) { iRuleName = ruleName; }

	public String getStudentFilter() { return iStudentFilter; }
	public void setStudentFilter(String studentFilter) { iStudentFilter = studentFilter; }

	public String getFilterInitiative() { return iFilterInitiative; }
	public void setFilterInitiative(String filterInitiative) { iFilterInitiative = filterInitiative; }

	public String getFilterTerm() { return iFilterTerm; }
	public void setFilterTerm(String filterTerm) { iFilterTerm = filterTerm; }

	public Integer getFirstYear() { return iFirstYear; }
	public void setFirstYear(Integer firstYear) { iFirstYear = firstYear; }

	public Integer getLastYear() { return iLastYear; }
	public void setLastYear(Integer lastYear) { iLastYear = lastYear; }

	public String getInstructonalMethod() { return iInstructonalMethod; }
	public void setInstructonalMethod(String instructonalMethod) { iInstructonalMethod = instructonalMethod; }

	public String getCourseName() { return iCourseName; }
	public void setCourseName(String courseName) { iCourseName = courseName; }

	public String getCourseType() { return iCourseType; }
	public void setCourseType(String courseType) { iCourseType = courseType; }

	public Boolean isDisjunctive() { return iDisjunctive; }
	public Boolean getDisjunctive() { return iDisjunctive; }
	public void setDisjunctive(Boolean disjunctive) { iDisjunctive = disjunctive; }

	public Boolean isAppliesToFilter() { return iAppliesToFilter; }
	public Boolean getAppliesToFilter() { return iAppliesToFilter; }
	public void setAppliesToFilter(Boolean appliesToFilter) { iAppliesToFilter = appliesToFilter; }

	public Boolean isAppliesToOnline() { return iAppliesToOnline; }
	public Boolean getAppliesToOnline() { return iAppliesToOnline; }
	public void setAppliesToOnline(Boolean appliesToOnline) { iAppliesToOnline = appliesToOnline; }

	public Boolean isAppliesToBatch() { return iAppliesToBatch; }
	public Boolean getAppliesToBatch() { return iAppliesToBatch; }
	public void setAppliesToBatch(Boolean appliesToBatch) { iAppliesToBatch = appliesToBatch; }

	public Boolean isAdminOverride() { return iAdminOverride; }
	public Boolean getAdminOverride() { return iAdminOverride; }
	public void setAdminOverride(Boolean adminOverride) { iAdminOverride = adminOverride; }

	public Boolean isAdvisorOverride() { return iAdvisorOverride; }
	public Boolean getAdvisorOverride() { return iAdvisorOverride; }
	public void setAdvisorOverride(Boolean advisorOverride) { iAdvisorOverride = advisorOverride; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentSchedulingRule)) return false;
		if (getUniqueId() == null || ((StudentSchedulingRule)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentSchedulingRule)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "StudentSchedulingRule["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "StudentSchedulingRule[" +
			"\n	AdminOverride: " + getAdminOverride() +
			"\n	AdvisorOverride: " + getAdvisorOverride() +
			"\n	AppliesToBatch: " + getAppliesToBatch() +
			"\n	AppliesToFilter: " + getAppliesToFilter() +
			"\n	AppliesToOnline: " + getAppliesToOnline() +
			"\n	CourseName: " + getCourseName() +
			"\n	CourseType: " + getCourseType() +
			"\n	Disjunctive: " + getDisjunctive() +
			"\n	FilterInitiative: " + getFilterInitiative() +
			"\n	FilterTerm: " + getFilterTerm() +
			"\n	FirstYear: " + getFirstYear() +
			"\n	InstructonalMethod: " + getInstructonalMethod() +
			"\n	LastYear: " + getLastYear() +
			"\n	Ord: " + getOrd() +
			"\n	RuleName: " + getRuleName() +
			"\n	StudentFilter: " + getStudentFilter() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
