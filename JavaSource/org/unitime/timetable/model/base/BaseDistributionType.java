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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.RefTableEntry;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseDistributionType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Boolean iSequencingRequired;
	private Integer iRequirementId;
	private String iAllowedPref;
	private String iDescr;
	private String iAbbreviation;
	private Boolean iInstructorPref;
	private Boolean iExamPref;
	private Boolean iVisible;

	private Set<Department> iDepartments;

	public BaseDistributionType() {
	}

	public BaseDistributionType(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "sequencing_required", nullable = false)
	public Boolean isSequencingRequired() { return iSequencingRequired; }
	@Transient
	public Boolean getSequencingRequired() { return iSequencingRequired; }
	public void setSequencingRequired(Boolean sequencingRequired) { iSequencingRequired = sequencingRequired; }

	@Column(name = "req_id", nullable = false)
	public Integer getRequirementId() { return iRequirementId; }
	public void setRequirementId(Integer requirementId) { iRequirementId = requirementId; }

	@Column(name = "allowed_pref", nullable = true, length = 10)
	public String getAllowedPref() { return iAllowedPref; }
	public void setAllowedPref(String allowedPref) { iAllowedPref = allowedPref; }

	@Column(name = "description", nullable = true, length = 2048)
	public String getDescr() { return iDescr; }
	public void setDescr(String descr) { iDescr = descr; }

	@Column(name = "abbreviation", nullable = true)
	public String getAbbreviation() { return iAbbreviation; }
	public void setAbbreviation(String abbreviation) { iAbbreviation = abbreviation; }

	@Column(name = "instructor_pref", nullable = false)
	public Boolean isInstructorPref() { return iInstructorPref; }
	@Transient
	public Boolean getInstructorPref() { return iInstructorPref; }
	public void setInstructorPref(Boolean instructorPref) { iInstructorPref = instructorPref; }

	@Column(name = "exam_pref", nullable = false)
	public Boolean isExamPref() { return iExamPref; }
	@Transient
	public Boolean getExamPref() { return iExamPref; }
	public void setExamPref(Boolean examPref) { iExamPref = examPref; }

	@Column(name = "visible", nullable = false)
	public Boolean isVisible() { return iVisible; }
	@Transient
	public Boolean getVisible() { return iVisible; }
	public void setVisible(Boolean visible) { iVisible = visible; }

	@ManyToMany
	@JoinTable(name = "dist_type_dept",
		joinColumns = { @JoinColumn(name = "dist_type_id") },
		inverseJoinColumns = { @JoinColumn(name = "dept_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addTodepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet<Department>();
		iDepartments.add(department);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof DistributionType)) return false;
		if (getUniqueId() == null || ((DistributionType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((DistributionType)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
			"\n	Visible: " + getVisible() +
			"]";
	}
}
