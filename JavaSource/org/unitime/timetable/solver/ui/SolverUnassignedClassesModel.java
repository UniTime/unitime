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
package org.unitime.timetable.solver.ui;

import java.util.ArrayList;
import java.util.List;

import org.cpsolver.coursett.constraint.InstructorConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.unitime.timetable.solver.TimetableSolver;



/**
 * @author Tomas Muller
 */
public class SolverUnassignedClassesModel extends UnassignedClassesModel {
	
	private static final long serialVersionUID = -6094708695678612559L;

	public SolverUnassignedClassesModel(TimetableSolver solver, String... prefix) {
		super();
		for (Lecture lecture: solver.currentSolution().getModel().unassignedVariables(solver.currentSolution().getAssignment())) {
			String name = lecture.getName();
			if (prefix != null && prefix.length > 0) {
				boolean hasPrefix = false;
				for (String p: prefix)
					if (p == null || name.startsWith(p)) { hasPrefix = true; break; }
				if (!hasPrefix) continue;
			}
			List<String> instructors = new ArrayList<String>();
			for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
				instructors.add(ic.getName());
			}
			int nrStudents = lecture.students().size();
			String initial = "";
			if (lecture.getInitialAssignment()!=null)
				initial = lecture.getInitialAssignment().getName();
			rows().add(new UnassignedClassRow(lecture.getClassId(), name, instructors, nrStudents, initial, lecture.getOrd()));
		}
	}
}
