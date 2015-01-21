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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.cpsolver.coursett.constraint.JenrlConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solver.Solver;


/**
 * @author Tomas Muller
 */
public class StudentConflictsReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private HashSet iGroups = new HashSet();

	public StudentConflictsReport(Solver solver) {
		TimetableModel model = (TimetableModel)solver.currentSolution().getModel();
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		for (JenrlConstraint jenrl: model.getJenrlConstraints()) {
			if (jenrl.isInConflict(assignment) && !jenrl.isToBeIgnored())
				iGroups.add(new JenrlInfo(solver, jenrl));
		}
		for (Lecture lecture: assignment.assignedVariables()) {
			iGroups.addAll(JenrlInfo.getCommitedJenrlInfos(solver, lecture).values());
		}
		if (model.constantVariables() != null)
			for (Lecture lecture: model.constantVariables()) {
				if (assignment.getValue(lecture) != null)
					iGroups.addAll(JenrlInfo.getCommitedJenrlInfos(solver, lecture).values());
			}
	}
	
	public Set getGroups() {
		return iGroups;
	}
}
