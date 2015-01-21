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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solver.Solver;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;


/**
 * @author Tomas Muller
 */
public class ViolatedDistrPreferencesReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private HashSet iGroups = new HashSet();

	public ViolatedDistrPreferencesReport(Solver solver) {
		TimetableModel model = (TimetableModel)solver.currentSolution().getModel();
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		for (GroupConstraint gc: model.getGroupConstraints()) {
			if (!gc.isSatisfied(assignment))
				iGroups.add(new ViolatedDistrPreference(solver, gc));
		}
	}
	
	public Set getGroups() {
		return iGroups;
	}
	
	public class ViolatedDistrPreference implements Serializable {
		private static final long serialVersionUID = 1L;
		Vector iClasses = new Vector();
		int iPreference = 0;
		String iType = null;
		String iName = null;
		
		public ViolatedDistrPreference(Solver solver, GroupConstraint gc) {
			Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
			iPreference = gc.getPreference();
			iType = gc.getType().reference();
			iName = gc.getName();
			for (Lecture lecture: gc.variables()) {
				if (assignment.getValue(lecture)==null) continue;
				iClasses.add(new ClassAssignmentDetails(solver,lecture,false));
			}
			Collections.sort(iClasses);
		}
		
		public int getPreference() { return iPreference; }
		public String getName() { return iName; }
		public String getType() { return iType; }
		public Vector getClasses() { return iClasses; }
	}
}
