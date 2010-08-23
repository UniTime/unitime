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

import java.util.ArrayList;
import java.util.List;

import net.sf.cpsolver.ifs.heuristics.NeighbourSelection;
import net.sf.cpsolver.ifs.model.Neighbour;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;

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
		boolean isComplete = model.unassignedVariables().isEmpty();
		int ix = ToolBox.random(model.variables().size());
		for (int i = 0; i < model.variables().size(); i++) {
			CurVariable course = model.variables().get((ix + i) % model.variables().size());
			if (!isComplete && course.getAssignment() != null) continue;
			int jx = ToolBox.random(course.values().size());
			if (course.getAssignment() != null && course.getAssignment().getStudent().getCourses().size() <= model.getStudentLimit().getMinLimit()) continue;
			for (int j = 0; j < course.values().size(); j++) {
				CurValue student = course.values().get((j + jx) % course.values().size());
				if (course.getCouse().getStudents().contains(student.getStudent())) continue;
				if (student.getStudent().getCourses().size() >= model.getStudentLimit().getMaxLimit()) continue;
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
		if (isComplete && bestValue > 0.0) return null;
		CurValue student = ToolBox.random(best);
		return (student == null ? null : new CurSimpleAssignment(student));
	}
}
