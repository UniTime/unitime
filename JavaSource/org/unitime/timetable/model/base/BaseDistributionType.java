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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.RefTableEntry;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseDistributionType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Boolean iSequencingRequired;
	private Integer iRequirementId;
	private String iAllowedPref;
	private String iDescr;
	private String iAbbreviation;
	private Boolean iInstructorPref;
	private Boolean iExamPref;

	private Set<Department> iDepartments;

	public static String PROP_SEQUENCING_REQUIRED = "sequencingRequired";
	public static String PROP_REQ_ID = "requirementId";
	public static String PROP_ALLOWED_PREF = "allowedPref";
	public static String PROP_DESCRIPTION = "descr";
	public static String PROP_ABBREVIATION = "abbreviation";
	public static String PROP_INSTRUCTOR_PREF = "instructorPref";
	public static String PROP_EXAM_PREF = "examPref";

	public BaseDistributionType() {
		initialize();
	}

	public BaseDistributionType(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Boolean isSequencingRequired() { return iSequencingRequired; }
	public Boolean getSequencingRequired() { return iSequencingRequired; }
	public void setSequencingRequired(Boolean sequencingRequired) { iSequencingRequired = sequencingRequired; }

	public Integer getRequirementId() { return iRequirementId; }
	public void setRequirementId(Integer requirementId) { iRequirementId = requirementId; }

	public String getAllowedPref() { return iAllowedPref; }
	public void setAllowedPref(String allowedPref) { iAllowedPref = allowedPref; }

	public String getDescr() { return iDescr; }
	public void setDescr(String descr) { iDescr = descr; }

	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }

	public Boolean isInstructorPref() { return iInstructorPref; }
	public Boolean getInstructorPref() { return iInstructorPref; }
	public void setInstructorPref(Boolean instructorPref) { iInstructorPref = instructorPref; }

	public Boolean isExamPref() { return iExamPref; }
	public Boolean getExamPref() { return iExamPref; }
	public void setExamPref(Boolean examPref) { iExamPref = examPref; }

	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addTodepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet<Department>();
		iDepartments.add(department);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof DistributionType)) return false;
		if (getUniqueId() == null || ((DistributionType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((DistributionType)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "DistributionType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "DistributionType[" +
			"\n	Abbreviation: " + getAbbreviation() +
			"\n	AllowedPref: " + getAllowedPref() +
			"\n	Descr: " + getDescr() +
			"\n	ExamPref: " + getExamPref() +
			"\n	InstructorPref: " + getInstructorPref() +
			"\n	Label: " + getLabel() +
			"\n	Reference: " + getReference() +
			"\n	RequirementId: " + getRequirementId() +
			"\n	SequencingRequired: " + getSequencingRequired() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
