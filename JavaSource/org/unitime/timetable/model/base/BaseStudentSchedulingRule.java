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

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.StudentSchedulingRule;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
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


	public BaseStudentSchedulingRule() {
	}

	public BaseStudentSchedulingRule(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "std_sched_rules_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "std_sched_rules_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "ord", nullable = false)
	public Integer getOrd() { return iOrd; }
	public void setOrd(Integer ord) { iOrd = ord; }

	@Column(name = "name", nullable = false, length = 255)
	public String getRuleName() { return iRuleName; }
	public void setRuleName(String ruleName) { iRuleName = ruleName; }

	@Column(name = "student_filter", nullable = true, length = 2048)
	public String getStudentFilter() { return iStudentFilter; }
	public void setStudentFilter(String studentFilter) { iStudentFilter = studentFilter; }

	@Column(name = "initiative", nullable = true, length = 1024)
	public String getFilterInitiative() { return iFilterInitiative; }
	public void setFilterInitiative(String filterInitiative) { iFilterInitiative = filterInitiative; }

	@Column(name = "term", nullable = true, length = 1024)
	public String getFilterTerm() { return iFilterTerm; }
	public void setFilterTerm(String filterTerm) { iFilterTerm = filterTerm; }

	@Column(name = "first_year", nullable = true, length = 4)
	public Integer getFirstYear() { return iFirstYear; }
	public void setFirstYear(Integer firstYear) { iFirstYear = firstYear; }

	@Column(name = "last_year", nullable = true, length = 4)
	public Integer getLastYear() { return iLastYear; }
	public void setLastYear(Integer lastYear) { iLastYear = lastYear; }

	@Column(name = "instr_method", nullable = true, length = 2048)
	public String getInstructonalMethod() { return iInstructonalMethod; }
	public void setInstructonalMethod(String instructonalMethod) { iInstructonalMethod = instructonalMethod; }

	@Column(name = "course_name", nullable = true, length = 2048)
	public String getCourseName() { return iCourseName; }
	public void setCourseName(String courseName) { iCourseName = courseName; }

	@Column(name = "course_type", nullable = true, length = 2048)
	public String getCourseType() { return iCourseType; }
	public void setCourseType(String courseType) { iCourseType = courseType; }

	@Column(name = "disjunctive", nullable = false)
	public Boolean isDisjunctive() { return iDisjunctive; }
	@Transient
	public Boolean getDisjunctive() { return iDisjunctive; }
	public void setDisjunctive(Boolean disjunctive) { iDisjunctive = disjunctive; }

	@Column(name = "apply_filter", nullable = false)
	public Boolean isAppliesToFilter() { return iAppliesToFilter; }
	@Transient
	public Boolean getAppliesToFilter() { return iAppliesToFilter; }
	public void setAppliesToFilter(Boolean appliesToFilter) { iAppliesToFilter = appliesToFilter; }

	@Column(name = "apply_online", nullable = false)
	public Boolean isAppliesToOnline() { return iAppliesToOnline; }
	@Transient
	public Boolean getAppliesToOnline() { return iAppliesToOnline; }
	public void setAppliesToOnline(Boolean appliesToOnline) { iAppliesToOnline = appliesToOnline; }

	@Column(name = "apply_batch", nullable = false)
	public Boolean isAppliesToBatch() { return iAppliesToBatch; }
	@Transient
	public Boolean getAppliesToBatch() { return iAppliesToBatch; }
	public void setAppliesToBatch(Boolean appliesToBatch) { iAppliesToBatch = appliesToBatch; }

	@Column(name = "admin_override", nullable = false)
	public Boolean isAdminOverride() { return iAdminOverride; }
	@Transient
	public Boolean getAdminOverride() { return iAdminOverride; }
	public void setAdminOverride(Boolean adminOverride) { iAdminOverride = adminOverride; }

	@Column(name = "advisor_override", nullable = false)
	public Boolean isAdvisorOverride() { return iAdvisorOverride; }
	@Transient
	public Boolean getAdvisorOverride() { return iAdvisorOverride; }
	public void setAdvisorOverride(Boolean advisorOverride) { iAdvisorOverride = advisorOverride; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof StudentSchedulingRule)) return false;
		if (getUniqueId() == null || ((StudentSchedulingRule)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((StudentSchedulingRule)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
