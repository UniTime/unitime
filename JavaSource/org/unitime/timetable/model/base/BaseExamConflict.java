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
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.Student;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseExamConflict implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iConflictType;
	private Double iDistance;
	private Integer iNrStudents;
	private Integer iNrInstructors;

	private Set<Exam> iExams;
	private Set<Student> iStudents;
	private Set<DepartmentalInstructor> iInstructors;

	public BaseExamConflict() {
	}

	public BaseExamConflict(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "pref_group_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "conflict_type", nullable = false, length = 10)
	public Integer getConflictType() { return iConflictType; }
	public void setConflictType(Integer conflictType) { iConflictType = conflictType; }

	@Column(name = "distance", nullable = true)
	public Double getDistance() { return iDistance; }
	public void setDistance(Double distance) { iDistance = distance; }

	@Formula("(select count(xs.student_id) from %SCHEMA%.xconflict_student xs where xs.conflict_id = uniqueid)")
	public Integer getNrStudents() { return iNrStudents; }
	public void setNrStudents(Integer nrStudents) { iNrStudents = nrStudents; }

	@Formula("(select count(xi.instructor_id) from %SCHEMA%.xconflict_instructor xi where xi.conflict_id = uniqueid)")
	public Integer getNrInstructors() { return iNrInstructors; }
	public void setNrInstructors(Integer nrInstructors) { iNrInstructors = nrInstructors; }

	@ManyToMany(fetch = FetchType.EAGER, mappedBy = "conflicts")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<Exam> getExams() { return iExams; }
	public void setExams(Set<Exam> exams) { iExams = exams; }
	public void addToExams(Exam exam) {
		if (iExams == null) iExams = new HashSet<Exam>();
		iExams.add(exam);
	}
	@Deprecated
	public void addToexams(Exam exam) {
		addToExams(exam);
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "xconflict_student",
		joinColumns = { @JoinColumn(name = "conflict_id") },
		inverseJoinColumns = { @JoinColumn(name = "student_id") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<Student> getStudents() { return iStudents; }
	public void setStudents(Set<Student> students) { iStudents = students; }
	public void addToStudents(Student student) {
		if (iStudents == null) iStudents = new HashSet<Student>();
		iStudents.add(student);
	}
	@Deprecated
	public void addTostudents(Student student) {
		addToStudents(student);
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "xconflict_instructor",
		joinColumns = { @JoinColumn(name = "conflict_id") },
		inverseJoinColumns = { @JoinColumn(name = "instructor_id") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<DepartmentalInstructor> getInstructors() { return iInstructors; }
	public void setInstructors(Set<DepartmentalInstructor> instructors) { iInstructors = instructors; }
	public void addToInstructors(DepartmentalInstructor departmentalInstructor) {
		if (iInstructors == null) iInstructors = new HashSet<DepartmentalInstructor>();
		iInstructors.add(departmentalInstructor);
	}
	@Deprecated
	public void addToinstructors(DepartmentalInstructor departmentalInstructor) {
		addToInstructors(departmentalInstructor);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExamConflict)) return false;
		if (getUniqueId() == null || ((ExamConflict)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExamConflict)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ExamConflict["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ExamConflict[" +
			"\n	ConflictType: " + getConflictType() +
			"\n	Distance: " + getDistance() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
