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
		int ix = ToolBox.random(model.variables().size());
		for (int i = 0; i < model.variables().size(); i++) {
			CurVariable course = model.variables().get((ix + i) % model.variables().size());
			CurValue current = assignment.getValue(course);
			if (!course.getCourse().isComplete(assignment) && current != null) continue;
			int jx = ToolBox.random(course.values(solution.getAssignment()).size());
			if (current != null && current.getStudent().getCourses(assignment).size() <= model.getStudentLimit().getMinLimit()) continue;
			for (int j = 0; j < course.values(solution.getAssignment()).size(); j++) {
				CurValue student = course.values(solution.getAssignment()).get((j + jx) % course.values(solution.getAssignment()).size());
				if (course.getCourse().getStudents(assignment).contains(student.getStudent())) continue;
				if (student.getStudent().getCourses(assignment).size() >= model.getStudentLimit().getMaxLimit()) continue;
				if (course.getCourse().getSize(assignment) + student.getStudent().getWeight() - (current == null ? 0.0 : current.getStudent().getWeight())
						> course.getCourse().getMaxSize()) continue;
				if (current != null && course.getCourse().getSize(assignment) + student.getStudent().getWeight() - current.getStudent().getWeight()
						< course.getCourse().getMaxSize() - model.getMinStudentWidth()) continue;
				return new CurSimpleAssignment(student);
			}
		}
		return null;
	}
}
