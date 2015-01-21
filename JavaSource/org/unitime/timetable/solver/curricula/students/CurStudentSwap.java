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
public class CurStudentSwap implements NeighbourSelection<CurVariable, CurValue>{

	protected CurStudentSwap(DataProperties	config) {
	}

	@Override
	public void init(Solver<CurVariable, CurValue> solver) {
	}

	@Override
	public Neighbour<CurVariable, CurValue> selectNeighbour(Solution<CurVariable, CurValue> solution) {
		CurModel model = (CurModel)solution.getModel();
		Assignment<CurVariable, CurValue> assignment = solution.getAssignment();
		CurCourse course = ToolBox.random(model.getSwapCourses());
		if (course == null) return null;
		CurStudent student = null;
		CurValue oldValue = null, newValue = null;
		int idx = ToolBox.random(model.getStudents().size());
		for (int i = 0; i < model.getStudents().size(); i++) {
			student = model.getStudents().get((i + idx) % model.getStudents().size());
			oldValue = course.getValue(assignment, student);
			if (oldValue == null && student.getCourses(assignment).size() < model.getStudentLimit().getMaxLimit()) break;
			if (oldValue != null && student.getCourses(assignment).size() > model.getStudentLimit().getMinLimit()) break;
		}
		if (oldValue == null && student.getCourses(assignment).size() >= model.getStudentLimit().getMaxLimit()) return null;
		if (oldValue != null && student.getCourses(assignment).size() <= model.getStudentLimit().getMinLimit()) return null;
		
		idx = ToolBox.random(model.getStudents().size());
		for (int i = 0; i < model.getStudents().size(); i++) {
			CurStudent newStudent = model.getStudents().get((i + idx) % model.getStudents().size());
			if (oldValue != null) {
				if (course.getStudents(assignment).contains(newStudent)) continue;
				if (newStudent.getCourses(assignment).size() >= model.getStudentLimit().getMaxLimit()) continue;
				if (course.getSize(assignment) + newStudent.getWeight() - student.getWeight() > course.getMaxSize()) continue;
				if (course.getSize(assignment) + newStudent.getWeight() - student.getWeight() < course.getMaxSize() - model.getMinStudentWidth()) continue;
				newValue = new CurValue(oldValue.variable(), newStudent);
				break;
			} else {
				if (newStudent.getCourses(assignment).size() <= model.getStudentLimit().getMinLimit()) continue;
				if (course.getSize(assignment) + student.getWeight() - newStudent.getWeight() > course.getMaxSize()) continue;
				if (course.getSize(assignment) + student.getWeight() - newStudent.getWeight() < course.getMaxSize() - model.getMinStudentWidth()) continue;
				oldValue = course.getValue(assignment, newStudent);
				if (oldValue != null) {
					newValue = new CurValue(oldValue.variable(), student);
					break;
				}
			}
		}
		return (newValue == null ? null : new CurSimpleAssignment(newValue));
	}
}
