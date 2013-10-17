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

import net.sf.cpsolver.ifs.heuristics.RouletteWheelSelection;
import net.sf.cpsolver.ifs.heuristics.VariableSelection;
import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class CurVariableSelection implements VariableSelection<CurVariable, CurValue>{
	RouletteWheelSelection<CurVariable> iWheel = null;
	
	public CurVariableSelection(DataProperties p) {}

	@Override
	public void init(Solver<CurVariable, CurValue> solver) {
	}

	@Override
	public CurVariable selectVariable(Solution<CurVariable, CurValue> solution) {
		CurModel m = (CurModel)solution.getModel();
		if (!m.unassignedVariables().isEmpty()) {
			List<CurVariable> best = new ArrayList<CurVariable>();
			double bestValue = 0.0;
			for (CurVariable course: m.unassignedVariables()) {
				if (course.getCourse().isComplete()) continue;
				double value = course.getCourse().getMaxSize() - course.getCourse().getSize();
				if (best.isEmpty() || bestValue < value) {
					best.clear();
					best.add(course);
					bestValue = value;
				} else if (bestValue == value) {
					best.add(course);
				}
			}
			if (!best.isEmpty()) return ToolBox.random(best);
		}
		if (iWheel == null || !iWheel.hasMoreElements())  {
			iWheel = new RouletteWheelSelection<CurVariable>();
			for (CurVariable course: m.assignedVariables()) {
				double penalty = course.getAssignment().toDouble();
				if (course.getCourse().getStudents().size() == m.getStudents().size()) continue;
				if (penalty != 0) iWheel.add(course, penalty);
			}
		}
		return iWheel.nextElement();
	}

}
