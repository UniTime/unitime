/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.curricula.students;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import net.sf.cpsolver.ifs.model.Constraint;

public class CurCourse extends Constraint<CurVariable, CurValue> {
	private String iCourseName;
	private Long iCourseId;
	private Set<CurStudent> iStudents = new HashSet<CurStudent>();
	private Hashtable<Long, Integer> iTargetShare = new Hashtable<Long, Integer>();
	private CurModel iModel;
	
	public CurCourse(CurModel model, Long course, String courseName, int nrStudents) {
		iModel = model;
		iCourseId = course;
		iCourseName = courseName;
		for (int i = 0; i < nrStudents; i++) {
			CurVariable c = new CurVariable(model, this, 0, model.getStudents().size());
			model.addVariable(c);
			addVariable(c);
		}
		model.addConstraint(this);
	}
	
	public void computeConflicts(CurValue value, Set<CurValue> conflicts) {
		if (getStudents().contains(value.getStudent()))
			for (CurVariable sc: assignedVariables()) {
				if (sc.getAssignment().getStudent().equals(value.getStudent())) {
					conflicts.add(sc.getAssignment());
				}
			}
	}

	public Set<CurStudent> getStudents() { return iStudents; }
	
	public Long getCourseId() { return iCourseId; }
	
	public String getCourseName() { return iCourseName; }
	
	public int getNrStudents() { return variables().size(); }
	
	public CurValue getValue(CurStudent student) {
		for (CurVariable var: variables()) {
			if (var.getAssignment() != null && var.getAssignment().getStudent().equals(student))
				return var.getAssignment();
		}
		return null;
	}
	
	public int share(CurCourse course) {
		int share = 0;
		for (CurStudent s: getStudents())
			if (course.getStudents().contains(s)) share++;
		return share;
	}
	
	public int penalty(CurCourse course) {
		return Math.abs(share(course) - getTargetShare(course.getCourseId()));
	}
	
	public double penalty(CurStudent student) {
		double penalty = 0;
		for (CurCourse course: iModel.getCourses()) {
			if (course.getCourseId().equals(getCourseId())) continue;
			int target = getTargetShare(course.getCourseId());
			int share = share(course);
			boolean contains = course.getStudents().contains(student);
			int size = course.getStudents().size();
			if (!getStudents().contains(student)) {
				size++;
				if (contains) share++;
			}
			if ((share < target && !contains) || (share > target && contains))
				penalty += ((double)Math.abs(share - target)) / (contains ? share : size - share);
		}
		return penalty;
	}

	public double penalty(CurStudent newStudent, CurStudent oldStudent) {
		if (oldStudent != null && oldStudent.equals(newStudent)) return penalty(newStudent);
		double penalty = 0;
		for (CurCourse course: iModel.getCourses()) {
			if (course.getCourseId().equals(getCourseId())) continue;
			boolean containsNew = (newStudent != null && course.getStudents().contains(newStudent));
			boolean containsOld = (oldStudent != null && course.getStudents().contains(oldStudent));
			if (containsNew == containsOld) continue;
			int target = getTargetShare(course.getCourseId());
			int share = share(course);
			boolean add = containsNew && !containsOld;
			if ((share <= target && !add) || (share >= target && add))
				penalty ++;
			else
				penalty --;
			/*
			if ((share <= target && !add) || (share >= target && add))
				penalty += ((double)(1 + Math.abs(share - target))) / (add ? 1 + share : 1 + course.getStudents().size() - share);
				*/
		}
		return penalty;
	}

	public void setTargetShare(Long course, int targetShare) {
		iTargetShare.put(course, targetShare);
	}
	
	public int getTargetShare(Long course) {
		Integer targetShare = iTargetShare.get(course);
		return (targetShare == null ? 0 : targetShare);
	}

	@Override
    public void assigned(long iteration, CurValue value) {
		super.assigned(iteration, value);
		iStudents.add(value.getStudent());
		value.getStudent().getCourses().add(this);
		if (value.getStudent().getCourses().size() > ((CurModel)value.variable().getModel()).getStudentLimit().getMaxLimit())
			throw new RuntimeException("Student max limit breached for " + value.getStudent() + ".");
	}

	@Override
	public void unassigned(long iteration, CurValue value) {
		super.unassigned(iteration, value);
		iStudents.remove(value.getStudent());
		value.getStudent().getCourses().remove(this);
		if (value.getStudent().getCourses().size() < ((CurModel)value.variable().getModel()).getStudentLimit().getMinLimit())
			throw new RuntimeException("Student min limit breached for " + value.getStudent() + ".");
	}
	
	public int hashCode() {
		return getCourseId().hashCode();
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CurCourse)) return false;
		return getCourseId().equals(((CurCourse)o).getCourseId());
	}
	
	public String toString() {
		return getCourseName();
	}
}
