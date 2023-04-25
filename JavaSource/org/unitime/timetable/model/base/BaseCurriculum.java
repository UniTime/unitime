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
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PosMajor;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
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

	public BaseCurriculum() {
	}

	public BaseCurriculum(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "curriculum_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "curriculum_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "abbv", nullable = false, length = 40)
	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	@Column(name = "name", nullable = false, length = 100)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "multiple_majors", nullable = false)
	public Boolean isMultipleMajors() { return iMultipleMajors; }
	@Transient
	public Boolean getMultipleMajors() { return iMultipleMajors; }
	public void setMultipleMajors(Boolean multipleMajors) { iMultipleMajors = multipleMajors; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "acad_area_id", nullable = false)
	public AcademicArea getAcademicArea() { return iAcademicArea; }
	public void setAcademicArea(AcademicArea academicArea) { iAcademicArea = academicArea; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "dept_id", nullable = false)
	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "curriculum_major",
		joinColumns = { @JoinColumn(name = "curriculum_id") },
		inverseJoinColumns = { @JoinColumn(name = "major_id") })
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<PosMajor> getMajors() { return iMajors; }
	public void setMajors(Set<PosMajor> majors) { iMajors = majors; }
	public void addTomajors(PosMajor posMajor) {
		if (iMajors == null) iMajors = new HashSet<PosMajor>();
		iMajors.add(posMajor);
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "curriculum", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<CurriculumClassification> getClassifications() { return iClassifications; }
	public void setClassifications(Set<CurriculumClassification> classifications) { iClassifications = classifications; }
	public void addToclassifications(CurriculumClassification curriculumClassification) {
		if (iClassifications == null) iClassifications = new HashSet<CurriculumClassification>();
		iClassifications.add(curriculumClassification);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Curriculum)) return false;
		if (getUniqueId() == null || ((Curriculum)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Curriculum)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
