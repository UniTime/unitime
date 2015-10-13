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

import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PosMajor;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCurriculum implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iAbbv;
	private String iName;
	private Boolean iMultipleMajors;

	private AcademicArea iAcademicArea;
	private Department iDepartment;
	private Set<PosMajor> iMajors;
	private Set<CurriculumClassification> iClassifications;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_ABBV = "abbv";
	public static String PROP_NAME = "name";
	public static String PROP_MULTIPLE_MAJORS = "multipleMajors";

	public BaseCurriculum() {
		initialize();
	}

	public BaseCurriculum(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public Boolean isMultipleMajors() { return iMultipleMajors; }
	public Boolean getMultipleMajors() { return iMultipleMajors; }
	public void setMultipleMajors(Boolean multipleMajors) { iMultipleMajors = multipleMajors; }

	public AcademicArea getAcademicArea() { return iAcademicArea; }
	public void setAcademicArea(AcademicArea academicArea) { iAcademicArea = academicArea; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public Set<PosMajor> getMajors() { return iMajors; }
	public void setMajors(Set<PosMajor> majors) { iMajors = majors; }
	public void addTomajors(PosMajor posMajor) {
		if (iMajors == null) iMajors = new HashSet<PosMajor>();
		iMajors.add(posMajor);
	}

	public Set<CurriculumClassification> getClassifications() { return iClassifications; }
	public void setClassifications(Set<CurriculumClassification> classifications) { iClassifications = classifications; }
	public void addToclassifications(CurriculumClassification curriculumClassification) {
		if (iClassifications == null) iClassifications = new HashSet<CurriculumClassification>();
		iClassifications.add(curriculumClassification);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Curriculum)) return false;
		if (getUniqueId() == null || ((Curriculum)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Curriculum)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Curriculum["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "Curriculum[" +
			"\n	Abbv: " + getAbbv() +
			"\n	AcademicArea: " + getAcademicArea() +
			"\n	Department: " + getDepartment() +
			"\n	MultipleMajors: " + getMultipleMajors() +
			"\n	Name: " + getName() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
