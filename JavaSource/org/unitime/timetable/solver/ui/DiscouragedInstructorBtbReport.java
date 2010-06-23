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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.coursett.constraint.InstructorConstraint;
import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.TimetableModel;
import net.sf.cpsolver.ifs.solver.Solver;

/**
 * @author Tomas Muller
 */
public class DiscouragedInstructorBtbReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private HashSet iGroups = new HashSet();

	public DiscouragedInstructorBtbReport(Solver solver) {
		TimetableModel model = (TimetableModel)solver.currentSolution().getModel();
		for (Enumeration e=model.getInstructorConstraints().elements();e.hasMoreElements(); ) {
			InstructorConstraint ic = (InstructorConstraint)e.nextElement();
			HashSet used = new HashSet();
	        for (int slot=1;slot<Constants.SLOTS_PER_DAY * Constants.NR_DAYS;slot++) {
	        	if ((slot%Constants.SLOTS_PER_DAY)==0) continue;
	            for (Placement placement: ic.getResource(slot)) {
	            	List<Placement> prevPlacements = ic.getPlacements(slot-1,placement);
	            	for (Placement prevPlacement: prevPlacements) {
	            		if (prevPlacement.equals(placement)) continue;
	            		if (!used.add(prevPlacement+"."+placement)) continue; 
	            		double dist = Placement.getDistance(prevPlacement,placement);
	            		if (dist>model.getInstructorNoPreferenceLimit() && dist<=model.getInstructorDiscouragedLimit())
	            			iGroups.add(new DiscouragedBtb(solver, ic, dist, prevPlacement, placement, PreferenceLevel.sDiscouraged));
	            		if (dist>model.getInstructorDiscouragedLimit() && dist<=model.getInstructorProhibitedLimit()) 
	            			iGroups.add(new DiscouragedBtb(solver, ic, dist, prevPlacement, placement, PreferenceLevel.sStronglyDiscouraged));
	            		if (dist>model.getInstructorProhibitedLimit())
	            			iGroups.add(new DiscouragedBtb(solver, ic, dist, prevPlacement, placement, PreferenceLevel.sProhibited));
	            			
	            	}
	            }
	        }
		}
	}
	
	public Set getGroups() {
		return iGroups;
	}
	
	public class DiscouragedBtb implements Serializable {
		private static final long serialVersionUID = 1L;
		String iPreference;
		Long iInstructorId;
		String iInstructorName;
		ClassAssignmentDetails iFirst, iSecond;
		double iDistance;
		
		public DiscouragedBtb(Solver solver, InstructorConstraint ic, double distance, Placement first, Placement second, String pref) {
			iPreference = pref;
			iInstructorId = ic.getResourceId();
			iInstructorName = ic.getName();
			iDistance = distance;
			iFirst = new ClassAssignmentDetails(solver,(Lecture)first.variable(),false);
			iSecond = new ClassAssignmentDetails(solver,(Lecture)second.variable(),false);
		}
		
		public String getPreference() { return iPreference; }
		public String getInstructorName() { return iInstructorName; }
		public Long getInstructorId() { return iInstructorId; }
		public ClassAssignmentDetails getFirst() { return iFirst; }
		public ClassAssignmentDetails getSecond() { return iSecond; }
		public double getDistance() { return iDistance; }
	}

}
