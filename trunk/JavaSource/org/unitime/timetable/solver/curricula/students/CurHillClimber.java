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

import net.sf.cpsolver.ifs.heuristics.NeighbourSelection;
import net.sf.cpsolver.ifs.model.Neighbour;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;

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
	public Neighbour<CurVariable, CurValue> selectNeighbour(
			Solution<CurVariable, CurValue> solution) {
		CurModel model = (CurModel)solution.getModel();
		List<CurValue> best = new ArrayList<CurValue>();
		double bestValue = 0;
		int ix = ToolBox.random(model.variables().size());
		for (int i = 0; i < model.variables().size(); i++) {
			CurVariable course = model.variables().get((ix + i) % model.variables().size());
			if (!course.getCourse().isComplete() && course.getAssignment() != null) continue;
			int jx = ToolBox.random(course.values().size());
			if (course.getAssignment() != null && course.getAssignment().getStudent().getCourses().size() <= model.getStudentLimit().getMinLimit()) continue;
			for (int j = 0; j < course.values().size(); j++) {
				CurValue student = course.values().get((j + jx) % course.values().size());
				if (course.getCourse().getStudents().contains(student.getStudent())) continue;
				if (student.getStudent().getCourses().size() >= model.getStudentLimit().getMaxLimit()) continue;
				if (course.getCourse().getSize() + student.getStudent().getWeight() - (course.getAssignment() == null ? 0.0 : course.getAssignment().getStudent().getWeight())
						> course.getCourse().getMaxSize()) continue;
				if (course.getAssignment() != null && course.getCourse().getSize() + student.getStudent().getWeight() - course.getAssignment().getStudent().getWeight()
						< course.getCourse().getMaxSize() - model.getMinStudentWidth()) continue;
				double value = student.toDouble();
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
		if (bestValue > 0.0 && !student.variable().getCourse().isComplete()) return null;
		return (student == null ? null : new CurSimpleAssignment(student));
	}
}
