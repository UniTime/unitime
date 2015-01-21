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
package org.unitime.timetable.solver.curricula.students;

import java.util.ArrayList;
import java.util.List;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.heuristics.RouletteWheelSelection;
import org.cpsolver.ifs.heuristics.VariableSelection;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;


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
		Assignment<CurVariable, CurValue> assignment = solution.getAssignment();
		if (m.nrUnassignedVariables(assignment) > 0) {
			List<CurVariable> best = new ArrayList<CurVariable>();
			double bestValue = 0.0;
			for (CurVariable course: m.unassignedVariables(assignment)) {
				if (course.getCourse().isComplete(assignment)) continue;
				double value = course.getCourse().getMaxSize() - course.getCourse().getSize(assignment);
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
			for (CurVariable course: m.assignedVariables(assignment)) {
				double penalty = assignment.getValue(course).toDouble(assignment);
				if (course.getCourse().getStudents(assignment).size() == m.getStudents().size()) continue;
				if (penalty != 0) iWheel.add(course, penalty);
			}
		}
		return iWheel.nextElement();
	}

}
