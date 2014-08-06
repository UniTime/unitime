/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
