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
package org.unitime.timetable.solver.curricula.students;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.heuristics.ValueSelection;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;


/**
 * @author Tomas Muller
 */
public class CurValueSelection implements ValueSelection<CurVariable, CurValue> {
	
	public CurValueSelection(DataProperties cfg) {}

	public void init(Solver<CurVariable, CurValue> solver) {
	}
	
	public CurValue selectValue(
			Solution<CurVariable, CurValue> solution,
			CurVariable selectedVariable) {
		/*
		if (ToolBox.random() < 0.2)
			return selectValueSlow(solution, selectedVariable);
		return selectValueFast(solution, selectedVariable);
		*/
		return selectValueSlow(solution, selectedVariable);
	}
	
	public CurValue selectValueFast(Solution<CurVariable, CurValue> solution, CurVariable selectedVariable) {
		CurModel m = (CurModel)solution.getModel();
		Assignment<CurVariable, CurValue> assignment = solution.getAssignment();
		CurValue currentValue = assignment.getValue(selectedVariable);
		if (currentValue != null && currentValue.getStudent().getCourses(assignment).size() <= m.getStudentLimit().getMinLimit()) return null;
		int size = selectedVariable.values(solution.getAssignment()).size();
		int i = ToolBox.random(size);
		for (int j = 0; j < size; j++) {
			CurValue student = selectedVariable.values(solution.getAssignment()).get((i + j) % size);
			if (student.equals(currentValue)) continue;
			if (selectedVariable.getCourse().getStudents(assignment).contains(student.getStudent())) continue;
			if (student.getStudent().getCourses(assignment).size() >= m.getStudentLimit().getMaxLimit()) continue;
			if (selectedVariable.getCourse().getSize(assignment) + student.getStudent().getWeight() - 
				(currentValue == null ? 0.0 : currentValue.getStudent().getWeight()) >
				selectedVariable.getCourse().getMaxSize()) continue;
			if (currentValue != null && 
				selectedVariable.getCourse().getSize(assignment) + student.getStudent().getWeight() - 
				currentValue.getStudent().getWeight() <
				selectedVariable.getCourse().getMaxSize() - m.getMinStudentWidth()) continue;
			return student;
		}
		return null;
	}

	public CurValue selectValueSlow(
			Solution<CurVariable, CurValue> solution,
			CurVariable selectedVariable) {
		CurModel m = (CurModel)solution.getModel();
		Assignment<CurVariable, CurValue> assignment = solution.getAssignment();
		CurValue currentValue = assignment.getValue(selectedVariable);
		if (currentValue != null && currentValue.getStudent().getCourses(assignment).size() <= m.getStudentLimit().getMinLimit()) return null;
		List<CurValue> bestStudents = new ArrayList<CurValue>();
		List<CurValue> allImprovingStudents = new ArrayList<CurValue>();
		List<CurValue> allStudents = new ArrayList<CurValue>();
		double bestValue = 0;
		for (CurValue student: selectedVariable.values(solution.getAssignment())) {
			if (student.equals(currentValue)) continue;
			if (selectedVariable.getCourse().getStudents(assignment).contains(student.getStudent())) continue;
			if (student.getStudent().getCourses(assignment).size() >= m.getStudentLimit().getMaxLimit()) continue;
/*
			if (selectedVariable.getCourse().getSize() + student.getStudent().getWeight() - 
					(currentValue == null ? 0.0 : currentValue.getStudent().getWeight()) >
					selectedVariable.getCourse().getMaxSize()) continue;
			if (currentValue != null && 
					selectedVariable.getCourse().getSize() + student.getStudent().getWeight() - 
					currentValue.getStudent().getWeight() <
					selectedVariable.getCourse().getMaxSize() - m.getMinStudentWidth()) continue;
*/
			double value = student.toDouble(assignment);
			Set<CurValue> conflicts = m.conflictValues(assignment, student);
			for (CurValue conf: conflicts) {
				value -= conf.toDouble(assignment);
			}
			if (bestStudents.isEmpty() || bestValue > value) {
				bestValue = value;
				bestStudents.clear();
				bestStudents.add(student);
			} else if (bestValue == value) {
				bestStudents.add(student);
			}
			if (value != 0)
				allImprovingStudents.add(student);
			allStudents.add(student);
		}
		if (currentValue != null) {
			double rnd = ToolBox.random();
			if (rnd < 0.01) return ToolBox.random(allStudents);
			if (rnd < 0.10) return ToolBox.random(allImprovingStudents);
		}
		return ToolBox.random(bestStudents);
	}
}
