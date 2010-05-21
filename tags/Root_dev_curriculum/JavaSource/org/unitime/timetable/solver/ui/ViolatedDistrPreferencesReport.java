/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.ui;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;

import net.sf.cpsolver.coursett.constraint.GroupConstraint;
import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.TimetableModel;
import net.sf.cpsolver.ifs.solver.Solver;

/**
 * @author Tomas Muller
 */
public class ViolatedDistrPreferencesReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private HashSet iGroups = new HashSet();

	public ViolatedDistrPreferencesReport(Solver solver) {
		TimetableModel model = (TimetableModel)solver.currentSolution().getModel();
		for (Enumeration e=model.getGroupConstraints().elements();e.hasMoreElements(); ) {
			GroupConstraint gc = (GroupConstraint)e.nextElement();
			if (!gc.isSatisfied())
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
			iPreference = gc.getPreference();
			iType = gc.getType();
			iName = gc.getName();
			for (Enumeration e=gc.variables().elements();e.hasMoreElements();) {
				Lecture lecture = (Lecture)e.nextElement();
				if (lecture.getAssignment()==null) continue;
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
