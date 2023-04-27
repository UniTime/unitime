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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseCurriculumClassification implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private Integer iNrStudents;
	private Integer iOrd;
	private String iStudents;
	private Integer iSnapshotNrStudents;
	private Date iSnapshotNrStudentsDate;

	private Curriculum iCurriculum;
	private AcademicClassification iAcademicClassification;
	private Set<CurriculumCourse> iCourses;

	public BaseCurriculumClassification() {
	}

	public BaseCurriculumClassification(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "curriculum_clasf_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "curriculum_clasf_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "name", nullable = false, length = 20)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "nr_students", nullable = false)
	public Integer getNrStudents() { return iNrStudents; }
	public void setNrStudents(Integer nrStudents) { iNrStudents = nrStudents; }

	@Column(name = "ord", nullable = false)
	public Integer getOrd() { return iOrd; }
	public void setOrd(Integer ord) { iOrd = ord; }

	@Column(name = "students", nullable = true)
	public String getStudents() { return iStudents; }
	public void setStudents(String students) { iStudents = students; }

	@Column(name = "snapshot_nr_students", nullable = true)
	public Integer getSnapshotNrStudents() { return iSnapshotNrStudents; }
	public void setSnapshotNrStudents(Integer snapshotNrStudents) { iSnapshotNrStudents = snapshotNrStudents; }

	@Column(name = "snapshot_nr_stu_date", nullable = true)
	public Date getSnapshotNrStudentsDate() { return iSnapshotNrStudentsDate; }
	public void setSnapshotNrStudentsDate(Date snapshotNrStudentsDate) { iSnapshotNrStudentsDate = snapshotNrStudentsDate; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "curriculum_id", nullable = false)
	public Curriculum getCurriculum() { return iCurriculum; }
	public void setCurriculum(Curriculum curriculum) { iCurriculum = curriculum; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "acad_clasf_id", nullable = false)
	public AcademicClassification getAcademicClassification() { return iAcademicClassification; }
	public void setAcademicClassification(AcademicClassification academicClassification) { iAcademicClassification = academicClassification; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "classification", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<CurriculumCourse> getCourses() { return iCourses; }
	public void setCourses(Set<CurriculumCourse> courses) { iCourses = courses; }
	public void addTocourses(CurriculumCourse curriculumCourse) {
		if (iCourses == null) iCourses = new HashSet<CurriculumCourse>();
		iCourses.add(curriculumCourse);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CurriculumClassification)) return false;
		if (getUniqueId() == null || ((CurriculumClassification)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((CurriculumClassification)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "CurriculumClassification["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "CurriculumClassification[" +
			"\n	AcademicClassification: " + getAcademicClassification() +
			"\n	Curriculum: " + getCurriculum() +
			"\n	Name: " + getName() +
			"\n	NrStudents: " + getNrStudents() +
			"\n	Ord: " + getOrd() +
			"\n	SnapshotNrStudents: " + getSnapshotNrStudents() +
			"\n	SnapshotNrStudentsDate: " + getSnapshotNrStudentsDate() +
			"\n	Students: " + getStudents() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
