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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.cpsolver.ifs.heuristics.ValueSelection;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;

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
	
	public CurValue selectValueFast(
			Solution<CurVariable, CurValue> solution,
			CurVariable selectedVariable) {
		CurModel m = (CurModel)solution.getModel();
		if (selectedVariable.getAssignment() != null && selectedVariable.getAssignment().getStudent().getCourses().size() <= m.getStudentLimit().getMinLimit()) return null;
		int size = selectedVariable.values().size();
		int i = ToolBox.random(size);
		for (int j = 0; j < size; j++) {
			CurValue student = selectedVariable.values().get((i + j) % size);
			if (student.equals(selectedVariable.getAssignment())) continue;
			if (selectedVariable.getCourse().getStudents().contains(student.getStudent())) continue;
			if (student.getStudent().getCourses().size() >= m.getStudentLimit().getMaxLimit()) continue;
			if (selectedVariable.getCourse().getSize() + student.getStudent().getWeight() - 
				(selectedVariable.getAssignment() == null ? 0.0 : selectedVariable.getAssignment().getStudent().getWeight()) >
				selectedVariable.getCourse().getMaxSize()) continue;
			if (selectedVariable.getAssignment() != null && 
				selectedVariable.getCourse().getSize() + student.getStudent().getWeight() - 
				selectedVariable.getAssignment().getStudent().getWeight() <
				selectedVariable.getCourse().getMaxSize() - m.getMinStudentWidth()) continue;
			return student;
		}
		return null;
	}

	public CurValue selectValueSlow(
			Solution<CurVariable, CurValue> solution,
			CurVariable selectedVariable) {
		CurModel m = (CurModel)solution.getModel();
		if (selectedVariable.getAssignment() != null && selectedVariable.getAssignment().getStudent().getCourses().size() <= m.getStudentLimit().getMinLimit()) return null;
		List<CurValue> bestStudents = new ArrayList<CurValue>();
		List<CurValue> allImprovingStudents = new ArrayList<CurValue>();
		List<CurValue> allStudents = new ArrayList<CurValue>();
		double bestValue = 0;
		for (CurValue student: selectedVariable.values()) {
			if (student.equals(selectedVariable.getAssignment())) continue;
			if (selectedVariable.getCourse().getStudents().contains(student.getStudent())) continue;
			if (student.getStudent().getCourses().size() >= m.getStudentLimit().getMaxLimit()) continue;
/*
			if (selectedVariable.getCourse().getSize() + student.getStudent().getWeight() - 
					(selectedVariable.getAssignment() == null ? 0.0 : selectedVariable.getAssignment().getStudent().getWeight()) >
					selectedVariable.getCourse().getMaxSize()) continue;
			if (selectedVariable.getAssignment() != null && 
					selectedVariable.getCourse().getSize() + student.getStudent().getWeight() - 
					selectedVariable.getAssignment().getStudent().getWeight() <
					selectedVariable.getCourse().getMaxSize() - m.getMinStudentWidth()) continue;
*/
			double value = student.toDouble();
			Set<CurValue> conflicts = m.conflictValues(student);
			for (CurValue conf: conflicts) {
				value -= conf.toDouble();
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
		if (selectedVariable.getAssignment() != null) {
			double rnd = ToolBox.random();
			if (rnd < 0.01) return ToolBox.random(allStudents);
			if (rnd < 0.10) return ToolBox.random(allImprovingStudents);
		}
		return ToolBox.random(bestStudents);
	}
}
