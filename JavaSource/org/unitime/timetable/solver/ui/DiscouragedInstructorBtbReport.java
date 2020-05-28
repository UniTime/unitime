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

import org.cpsolver.coursett.constraint.InstructorConstraint;
import org.cpsolver.coursett.constraint.SoftInstructorConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solver.Solver;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.util.Constants;


/**
 * @author Tomas Muller
 */
public class DiscouragedInstructorBtbReport implements Serializable {

	private static final long serialVersionUID = 1L;
	private HashSet iGroups = new HashSet();

	public DiscouragedInstructorBtbReport(Solver solver) {
		TimetableModel model = (TimetableModel)solver.currentSolution().getModel();
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		for (InstructorConstraint ic: model.getInstructorConstraints()) {
			InstructorConstraint.InstructorConstraintContext context = ic.getContext(assignment);
			HashSet checked = new HashSet();
	        for (int slot=1;slot<Constants.SLOTS_PER_DAY * Constants.NR_DAYS;slot++) {
	        	if ((slot%Constants.SLOTS_PER_DAY)==0) continue;
	            for (Placement placement: context.getPlacements(slot)) {
	            	for (Placement prevPlacement: context.getPlacements(slot-1,placement)) {
	            		if (prevPlacement.equals(placement)) continue;
	            		if (!checked.add(prevPlacement+"."+placement)) continue; 
	            		if (placement.getTimeLocation().hasIntersection(prevPlacement.getTimeLocation())) continue;
	            		double dist = Placement.getDistanceInMeters(model.getDistanceMetric(), prevPlacement,placement);
	            		if (dist>model.getDistanceMetric().getInstructorNoPreferenceLimit() && dist<=model.getDistanceMetric().getInstructorDiscouragedLimit())
	            			iGroups.add(new DiscouragedBtb(solver, ic, dist, prevPlacement, placement, PreferenceLevel.sDiscouraged));
	            		if (dist>model.getDistanceMetric().getInstructorDiscouragedLimit() && dist<=model.getDistanceMetric().getInstructorProhibitedLimit()) 
	            			iGroups.add(new DiscouragedBtb(solver, ic, dist, prevPlacement, placement, PreferenceLevel.sStronglyDiscouraged));
	            		if (dist>model.getDistanceMetric().getInstructorProhibitedLimit())
	            			iGroups.add(new DiscouragedBtb(solver, ic, dist, prevPlacement, placement, ic.isIgnoreDistances() ? PreferenceLevel.sStronglyDiscouraged : PreferenceLevel.sProhibited));
	            	}
	            }
	        }
	        if (model.getDistanceMetric().doComputeDistanceConflictsBetweenNonBTBClasses()) {
	            for (Lecture l1: ic.variables()) {
	            	Placement p1 = assignment.getValue(l1);
	                TimeLocation t1 = (p1 == null ? null : p1.getTimeLocation());
	                if (t1 == null) continue;
	                Placement before = null;
	                for (Lecture l2: ic.variables()) {
	                	Placement p2 = assignment.getValue(l2);
	                    if (p2 == null || l2.equals(l1)) continue;
	                    TimeLocation t2 = p2.getTimeLocation();
	                    if (t2 == null || !t1.shareDays(t2) || !t1.shareWeeks(t2)) continue;
	                    if (t2.getStartSlot() + t2.getLength() < t1.getStartSlot()) {
	                        int distanceInMinutes = Placement.getDistanceInMinutes(model.getDistanceMetric(), p1, p2);
	                        if (distanceInMinutes >  t2.getBreakTime() + Constants.SLOT_LENGTH_MIN * (t1.getStartSlot() - t2.getStartSlot() - t2.getLength()))
	                        	iGroups.add(new DiscouragedBtb(solver, ic, Placement.getDistanceInMeters(model.getDistanceMetric(), p1, p2), p1, p2, ic.isIgnoreDistances() ? PreferenceLevel.sStronglyDiscouraged : PreferenceLevel.sProhibited));
	                        else if (distanceInMinutes > Constants.SLOT_LENGTH_MIN * (t1.getStartSlot() - t2.getStartSlot() - t2.getLength()))
	                        	iGroups.add(new DiscouragedBtb(solver, ic, Placement.getDistanceInMeters(model.getDistanceMetric(), p1, p2), p1, p2, PreferenceLevel.sDiscouraged));
	                    }
	                    if (t2.getStartSlot() + t2.getLength() <= t1.getStartSlot()) {
	                        if (before == null || before.getTimeLocation().getStartSlot() < t2.getStartSlot())
	                            before = p2;
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
	                if (before == null) continue;
	                TimeLocation t0 = before.getTimeLocation();
	                if (t0.getStartSlot() + t0.getLength() < t1.getStartSlot()) { // not back-to-back
		                if (Placement.getDistanceInMinutes(model.getDistanceMetric(), before, p1) > t0.getBreakTime() + Constants.SLOT_LENGTH_MIN * (t1.getStartSlot() - t0.getStartSlot() - t0.getLength())) {
		                	// too far apart
		                	iGroups.add(new DiscouragedBtb(solver, ic, - Placement.getDistanceInMinutes(model.getDistanceMetric(), before, p1), before, p1, ic.isIgnoreDistances() ? PreferenceLevel.sStronglyDiscouraged : PreferenceLevel.sProhibited));
		                } else if (before != null && Placement.getDistanceInMinutes(model.getDistanceMetric(), before, p1) > model.getDistanceMetric().getInstructorLongTravelInMinutes()) {
		                	// long travel
		                	iGroups.add(new DiscouragedBtb(solver, ic, - Placement.getDistanceInMinutes(model.getDistanceMetric(), before, p1), before, p1, PreferenceLevel.sStronglyDiscouraged));
		                } else if (Placement.getDistanceInMinutes(model.getDistanceMetric(), before, p1) > Constants.SLOT_LENGTH_MIN * (t1.getStartSlot() - t0.getStartSlot() - t0.getLength())) {
		                	// too far if no break time
		                	iGroups.add(new DiscouragedBtb(solver, ic, - Placement.getDistanceInMinutes(model.getDistanceMetric(), before, p1), before, p1, PreferenceLevel.sDiscouraged));
		                }
	                }
	            }
	        }
	        if (ic instanceof SoftInstructorConstraint) {
	        	for (Lecture l1: ic.variables()) {
	            	Placement p1 = assignment.getValue(l1);
	                TimeLocation t1 = (p1 == null ? null : p1.getTimeLocation());
	                if (t1 == null) continue;
	                for (Lecture l2: ic.variables()) {
	                	if (l1.compareTo(l2) <= 0) continue;
	                	Placement p2 = assignment.getValue(l2);
	                    if (p2 == null) continue;
	                    if (p1.canShareRooms(p2) && p1.sameRooms(p2)) continue;
	                    TimeLocation t2 = p2.getTimeLocation();
	                    if (t1 == null || t2 == null || !t1.hasIntersection(t2)) continue;
	                    iGroups.add(new DiscouragedBtb(solver, ic, 0, p1, p2, PreferenceLevel.sProhibited));
	                }
	                if (ic.getUnavailabilities() != null) {
	                    for (Placement c: ic.getUnavailabilities()) {
	                    	if (c.getTimeLocation().hasIntersection(t1) && (!l1.canShareRoom(c.variable()) || !p1.sameRooms(c))) {
	                    		if (checked.add(p1+"."+c))
	                    			iGroups.add(new DiscouragedBtb(solver, ic, 0, p1, c, PreferenceLevel.sProhibited));
	                    	} else if (!ic.isIgnoreDistances()) {
	                    		TimeLocation t2 = c.getTimeLocation();
	                    		if (t1.shareDays(t2) && t1.shareWeeks(t2) && (t1.getStartSlot() + t1.getLength() == t2.getStartSlot() || t2.getStartSlot() + t2.getLength() == t1.getStartSlot()) && Placement.getDistanceInMeters(model.getDistanceMetric(), p1, c) > model.getDistanceMetric().getInstructorProhibitedLimit()) {
	                    			if (checked.add(p1+"."+c))
	                    				iGroups.add(new DiscouragedBtb(solver, ic, Placement.getDistanceInMeters(model.getDistanceMetric(), p1, c), p1, c, PreferenceLevel.sProhibited));
	                    		}
	                    	}
	                    }
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
