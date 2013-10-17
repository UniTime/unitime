/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.Student;

/**
 * @author Tomas Muller
 */
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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CONFLICT_TYPE = "conflictType";
	public static String PROP_DISTANCE = "distance";

	public BaseExamConflict() {
		initialize();
	}

	public BaseExamConflict(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getConflictType() { return iConflictType; }
	public void setConflictType(Integer conflictType) { iConflictType = conflictType; }

	public Double getDistance() { return iDistance; }
	public void setDistance(Double distance) { iDistance = distance; }

	public Integer getNrStudents() { return iNrStudents; }
	public void setNrStudents(Integer nrStudents) { iNrStudents = nrStudents; }

	public Integer getNrInstructors() { return iNrInstructors; }
	public void setNrInstructors(Integer nrInstructors) { iNrInstructors = nrInstructors; }

	public Set<Exam> getExams() { return iExams; }
	public void setExams(Set<Exam> exams) { iExams = exams; }
	public void addToexams(Exam exam) {
		if (iExams == null) iExams = new HashSet<Exam>();
		iExams.add(exam);
	}

	public Set<Student> getStudents() { return iStudents; }
	public void setStudents(Set<Student> students) { iStudents = students; }
	public void addTostudents(Student student) {
		if (iStudents == null) iStudents = new HashSet<Student>();
		iStudents.add(student);
	}

	public Set<DepartmentalInstructor> getInstructors() { return iInstructors; }
	public void setInstructors(Set<DepartmentalInstructor> instructors) { iInstructors = instructors; }
	public void addToinstructors(DepartmentalInstructor departmentalInstructor) {
		if (iInstructors == null) iInstructors = new HashSet<DepartmentalInstructor>();
		iInstructors.add(departmentalInstructor);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExamConflict)) return false;
		if (getUniqueId() == null || ((ExamConflict)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExamConflict)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
