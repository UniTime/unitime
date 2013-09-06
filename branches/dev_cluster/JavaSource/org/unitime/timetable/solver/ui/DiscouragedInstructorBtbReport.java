/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.util.Constants;

import net.sf.cpsolver.coursett.constraint.InstructorConstraint;
import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.TimeLocation;
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
		for (InstructorConstraint ic: model.getInstructorConstraints()) {
			HashSet checked = new HashSet();
	        for (int slot=1;slot<Constants.SLOTS_PER_DAY * Constants.NR_DAYS;slot++) {
	        	if ((slot%Constants.SLOTS_PER_DAY)==0) continue;
	            for (Placement placement: ic.getResource(slot)) {
	            	for (Placement prevPlacement: ic.getPlacements(slot-1,placement)) {
	            		if (prevPlacement.equals(placement)) continue;
	            		if (!checked.add(prevPlacement+"."+placement)) continue; 
	            		double dist = Placement.getDistanceInMeters(model.getDistanceMetric(), prevPlacement,placement);
	            		if (dist>model.getDistanceMetric().getInstructorNoPreferenceLimit() && dist<=model.getDistanceMetric().getInstructorDiscouragedLimit())
	            			iGroups.add(new DiscouragedBtb(solver, ic, dist, prevPlacement, placement, PreferenceLevel.sDiscouraged));
	            		if (dist>model.getDistanceMetric().getInstructorDiscouragedLimit() && dist<=model.getDistanceMetric().getInstructorProhibitedLimit()) 
	            			iGroups.add(new DiscouragedBtb(solver, ic, dist, prevPlacement, placement, PreferenceLevel.sStronglyDiscouraged));
	            		if (dist>model.getDistanceMetric().getInstructorProhibitedLimit())
	            			iGroups.add(new DiscouragedBtb(solver, ic, dist, prevPlacement, placement, PreferenceLevel.sProhibited));
	            	}
	            }
	        }
	        if (model.getDistanceMetric().doComputeDistanceConflictsBetweenNonBTBClasses()) {
	            for (Lecture p1: ic.assignedVariables()) {
	                TimeLocation t1 = (p1.getAssignment() == null ? null : p1.getAssignment().getTimeLocation());
	                if (t1 == null) continue;
	                Placement before = null;
	                for (Lecture p2: ic.assignedVariables()) {
	                    if (p2.getAssignment() == null || p2.equals(p1)) continue;
	                    TimeLocation t2 = p2.getAssignment().getTimeLocation();
	                    if (t2 == null || !t1.shareDays(t2) || !t1.shareWeeks(t2)) continue;
	                    if (t2.getStartSlot() + t2.getLength() < t1.getStartSlot()) {
	                        int distanceInMinutes = Placement.getDistanceInMinutes(model.getDistanceMetric(), p1.getAssignment(), p2.getAssignment());
	                        if (distanceInMinutes >  t2.getBreakTime() + Constants.SLOT_LENGTH_MIN * (t1.getStartSlot() - t2.getStartSlot() - t2.getLength()))
	                        	iGroups.add(new DiscouragedBtb(solver, ic, Placement.getDistanceInMeters(model.getDistanceMetric(), p1.getAssignment(), p2.getAssignment()), p1.getAssignment(), p2.getAssignment(), ic.isIgnoreDistances() ? PreferenceLevel.sStronglyDiscouraged : PreferenceLevel.sProhibited));
	                        else if (distanceInMinutes > Constants.SLOT_LENGTH_MIN * (t1.getStartSlot() - t2.getStartSlot() - t2.getLength()))
	                        	iGroups.add(new DiscouragedBtb(solver, ic, Placement.getDistanceInMeters(model.getDistanceMetric(), p1.getAssignment(), p2.getAssignment()), p1.getAssignment(), p2.getAssignment(), PreferenceLevel.sDiscouraged));
	                    }
	                    if (t2.getStartSlot() + t2.getLength() <= t1.getStartSlot()) {
	                        if (before == null || before.getTimeLocation().getStartSlot() < t2.getStartSlot())
	                            before = p2.getAssignment();
	                    }
	                }
	                if (ic.getUnavailabilities() != null) {
	                    for (Placement c: ic.getUnavailabilities()) {
	                        TimeLocation t2 = c.getTimeLocation();
	                        if (t1 == null || t2 == null || !t1.shareDays(t2) || !t1.shareWeeks(t2)) continue;
	                        if (t2.getStartSlot() + t2.getLength() <= t1.getStartSlot()) {
	                            if (before == null || before.getTimeLocation().getStartSlot() < t2.getStartSlot())
	                                before = c;
	                        }
	                    }
	                }
	                if (before != null && Placement.getDistanceInMinutes(model.getDistanceMetric(), before, p1.getAssignment()) > model.getDistanceMetric().getInstructorLongTravelInMinutes())
	                	iGroups.add(new DiscouragedBtb(solver, ic, Placement.getDistanceInMeters(model.getDistanceMetric(), before, p1.getAssignment()), before, p1.getAssignment(), PreferenceLevel.sStronglyDiscouraged));
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
