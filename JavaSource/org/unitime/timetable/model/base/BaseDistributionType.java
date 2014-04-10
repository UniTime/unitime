/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
