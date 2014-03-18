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

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.heuristics.NeighbourSelection;
import org.cpsolver.ifs.model.Neighbour;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;


/**
 * @author Tomas Muller
 */
public class CurHillClimber implements NeighbourSelection<CurVariable, CurValue>{

	protected CurHillClimber(DataProperties	config) {
	}

	@Override
	public void init(Solver<CurVariable, CurValue> solver) {
	}

	@Override
	public Neighbour<CurVariable, CurValue> selectNeighbour(Solution<CurVariable, CurValue> solution) {
		CurModel model = (CurModel)solution.getModel();
		Assignment<CurVariable, CurValue> assignment = solution.getAssignment();
		List<CurValue> best = new ArrayList<CurValue>();
		double bestValue = 0;
		int ix = ToolBox.random(model.variables().size());
		for (int i = 0; i < model.variables().size(); i++) {
			CurVariable course = model.variables().get((ix + i) % model.variables().size());
			CurValue current = assignment.getValue(course);
			if (!course.getCourse().isComplete(assignment) && current != null) continue;
			int jx = ToolBox.random(course.values().size());
			if (current != null && current.getStudent().getCourses(assignment).size() <= model.getStudentLimit().getMinLimit()) continue;
			for (int j = 0; j < course.values().size(); j++) {
				CurValue student = course.values().get((j + jx) % course.values().size());
				if (course.getCourse().getStudents(assignment).contains(student.getStudent())) continue;
				if (student.getStudent().getCourses(assignment).size() >= model.getStudentLimit().getMaxLimit()) continue;
				if (course.getCourse().getSize(assignment) + student.getStudent().getWeight() - (current == null ? 0.0 : current.getStudent().getWeight())
						> course.getCourse().getMaxSize()) continue;
				if (current != null && course.getCourse().getSize(assignment) + student.getStudent().getWeight() - current.getStudent().getWeight()
						< course.getCourse().getMaxSize() - model.getMinStudentWidth()) continue;
				double value = student.toDouble(assignment);
				if (best.isEmpty() || value < bestValue) {
					if (value < 0.0) return new CurSimpleAssignment(student);
					best.clear();
					best.add(student);
					bestValue = value;
				} else if (value == bestValue) {
					best.add(student);
				}
			}
		}
		CurValue student = ToolBox.random(best);
		if (bestValue > 0.0 && !student.variable().getCourse().isComplete(assignment)) return null;
		return (student == null ? null : new CurSimpleAssignment(student));
	}
}
