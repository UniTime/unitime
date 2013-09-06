/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.curricula.students;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.sf.cpsolver.ifs.model.Constraint;
import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class CurCourse extends Constraint<CurVariable, CurValue> {
	private static DecimalFormat sDF = new DecimalFormat("0.###");
	private String iCourseName;
	private Long iCourseId;
	private Set<CurStudent> iStudents = new HashSet<CurStudent>();
	private Hashtable<Long, Double> iTargetShare = new Hashtable<Long, Double>();
	private CurModel iModel;
	private double iMaxSize;
	private double iSize = 0.0;
	private Double iPriority = null;
	
	public CurCourse(CurModel model, Long course, String courseName, int maxNrStudents, double maxSize, Double priority) {
		iModel = model;
		iCourseId = course;
		iCourseName = courseName;
		iMaxSize = maxSize;
		iPriority = priority;
		for (int i = 0; i < maxNrStudents; i++) {
			CurVariable c = new CurVariable(model, this, 0, model.getStudents().size());
			model.addVariable(c);
			addVariable(c);
		}
		model.addConstraint(this);
	}
	
	public void computeConflicts(CurValue value, Set<CurValue> conflicts) {
		if (getSize() + value.getStudent().getWeight() > getMaxSize()) {
			double excess = getSize() + value.getStudent().getWeight() - getMaxSize();
			for (CurValue conf: conflicts)
				if (conf.variable().getCourse().equals(this))
					excess -= conf.getStudent().getWeight();
			/*
			if (value.variable().getAssignment() != null && !conflicts.contains(value.variable().getAssignment()))
				excess -= value.variable().getAssignment().getStudent().getWeight();
				*/
			while (excess > 0.0) {
				List<CurValue> adepts = new ArrayList<CurValue>();
				double best = 0;
				for (CurVariable assigned: assignedVariables()) {
					if (assigned.equals(value.variable())) continue;
					CurValue adept = assigned.getAssignment();
					if (conflicts.contains(adept)) continue;
					double p = adept.toDouble();
					if (adepts.isEmpty() || p < best) {
						best = p; adepts.clear(); adepts.add(adept);
					} else if (p == best) {
						adepts.add(adept);
					}
				}
				if (adepts.isEmpty()) {
					conflicts.add(value); break;
				}
				CurValue conf = ToolBox.random(adepts);
				conflicts.add(conf);
				excess -= conf.getStudent().getWeight();
			}
		}
		if (getStudents().contains(value.getStudent()))
			for (CurVariable sc: assignedVariables()) {
				if (sc.getAssignment().getStudent().equals(value.getStudent())) {
					conflicts.add(sc.getAssignment());
				}
			}
	}

	public Set<CurStudent> getStudents() { return iStudents; }
	
	public double getMaxSize() { return iMaxSize + iModel.getMinStudentWidth() / 2f; }
	
	public double getOriginalMaxSize() { return iMaxSize; }
	
	public Long getCourseId() { return iCourseId; }
	
	public String getCourseName() { return iCourseName; }
	
	public int getNrStudents() { return variables().size(); }
	
	public double getSize() { return iSize; }
	
	public CurValue getValue(CurStudent student) {
		for (CurVariable var: variables()) {
			if (var.getAssignment() != null && var.getAssignment().getStudent().equals(student))
				return var.getAssignment();
		}
		return null;
	}
	
	public double share(CurCourse course) {
		double share = 0;
		for (CurStudent s: getStudents())
			if (course.getStudents().contains(s)) share += s.getWeight();
		return share;
	}
	
	public double penalty(CurCourse course) {
		double target = getTargetShare(course.getCourseId());
		return Math.abs(share(course) - target) * (target == 0.0 ? 10.0 : 1.0);
	}
	
	public double penalty(CurStudent student) {
		return penalty(student, null);
		/*
		if (student == null) return 0.0;
		double penalty = 0;
		for (CurCourse course: iModel.getCourses()) {
			if (course.getCourseId().equals(getCourseId())) continue;
			double target = getTargetShare(course.getCourseId());
			double share = share(course);
			boolean contains = course.getStudents().contains(student);
			double size = course.getSize();
			if (!getStudents().contains(student)) {
				size += student.getWeight();
				if (contains) share += student.getWeight();
			}
			if ((share < target && !contains) || (share > target && contains))
				penalty += ((double)Math.abs(share - target)) / (contains ? share : size - share);
		}
		return penalty;
		*/
	}

	public double penalty(CurStudent newStudent, CurStudent oldStudent) {
		if (oldStudent != null && oldStudent.equals(newStudent))
			return penalty(newStudent, null);
		double penalty = 0;
		for (CurCourse course: iModel.getCourses()) {
			if (course.getCourseId().equals(getCourseId())) continue;
			double target = getTargetShare(course.getCourseId());
			double share = share(course);
			double oldPenalty = Math.abs(share - target);
			if (newStudent != null && course.getStudents().contains(newStudent))
				share += newStudent.getWeight();
			if (oldStudent != null && course.getStudents().contains(oldStudent))
				share -= oldStudent.getWeight();
			double newPenalty = Math.abs(share - target);
			penalty += (newPenalty - oldPenalty) * (target == 0.0 ? 10.0 : 1.0);
		}
		return penalty;
	}

	public void setTargetShare(Long course, double targetShare) {
		iTargetShare.put(course, targetShare);
	}
	
	public double getTargetShare(Long course) {
		Double targetShare = iTargetShare.get(course);
		return (targetShare == null ? 0 : targetShare);
	}

	@Override
    public void assigned(long iteration, CurValue value) {
		super.assigned(iteration, value);
		iStudents.add(value.getStudent());
		value.getStudent().getCourses().add(this);
		iSize += value.getStudent().getWeight();
		/*
		if (iSize > getMaxSize())
			throw new RuntimeException("Maximal number of students in a course exceeded " + "(" + iSize + " > " + getMaxSize() + ")");
		if (value.getStudent().getCourses().size() > ((CurModel)value.variable().getModel()).getStudentLimit().getMaxLimit())
			throw new RuntimeException("Student max limit breached for " + value.getStudent() + " (" + value.getStudent().getCourses().size() + " > " + ((CurModel)value.variable().getModel()).getStudentLimit().getMaxLimit() + ".");
			*/
	}

	@Override
	public void unassigned(long iteration, CurValue value) {
		super.unassigned(iteration, value);
		iStudents.remove(value.getStudent());
		iSize -= value.getStudent().getWeight();
		value.getStudent().getCourses().remove(this);
		/*
		if (value.getStudent().getCourses().size() < ((CurModel)value.variable().getModel()).getStudentLimit().getMinLimit())
			throw new RuntimeException("Student min limit breached for " + value.getStudent() + ".");
			*/
	}
	
	public int hashCode() {
		return getCourseId().hashCode();
	}
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof CurCourse)) return false;
		return getCourseId().equals(((CurCourse)o).getCourseId());
	}
	
	public String toString() {
		return "Course<" + getCourseName() + ", size: " + sDF.format(getSize()) + "/" + sDF.format(getOriginalMaxSize()) + ">";
	}
	
	public boolean isComplete() {
		return getSize() + iModel.getMinStudentWidth() > getMaxSize();
	}
	
	public Double getPriority() { return iPriority; }
}
