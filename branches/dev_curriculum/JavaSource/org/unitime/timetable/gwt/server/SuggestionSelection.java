/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.util.Iterator;
import java.util.Hashtable;
import java.util.Set;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.heuristics.selection.BranchBoundSelection;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

public class SuggestionSelection extends BranchBoundSelection {
	protected double iPreferenceWeight = 100000.0;
	protected double iNoTimeWeight = 1.0;
	protected double iNotAssignedWeight = 10000.0;
	protected double iOnlinePenaltyWeight = 10.0;
	protected double iOverlappingFreeTimeWeight = 10.0;
	protected double iPriorotyFactor = 0.90;
	protected Hashtable<CourseRequest, Set<Section>> iPreferredSections, iRequiredSections;
	protected Set<FreeTimeRequest> iRequiredFreeTimes;
	
    public SuggestionSelection(DataProperties properties, Hashtable<CourseRequest, Set<Section>> preferredSections,
    		Hashtable<CourseRequest, Set<Section>> requiredSections, Set<FreeTimeRequest> requiredFreeTimes) {
    	super(properties);
    	iPreferenceWeight = properties.getPropertyDouble("Suggestions.PreferredSectionWeight", iPreferenceWeight);
    	iNoTimeWeight = properties.getPropertyDouble("Suggestions.NoTimeWeight", iNoTimeWeight);
    	iNotAssignedWeight = properties.getPropertyDouble("Suggestions.NotAssignedWeight", iNotAssignedWeight);
    	iOnlinePenaltyWeight = properties.getPropertyDouble("Suggestions.OnlinePenaltyWeight", iOnlinePenaltyWeight);
        iDistConfWeight = properties.getPropertyDouble("Suggestions.DistanceConflictWeight", 25.0);
        iOverlappingFreeTimeWeight = properties.getPropertyDouble("Suggestions.OverlappingFreeTimeWeight", iOverlappingFreeTimeWeight);
        iPriorotyFactor = properties.getPropertyDouble("Suggestions.PriorityFactor", iPriorotyFactor);
    	iPreferredSections = preferredSections;
    	iRequiredSections = requiredSections;
    	iMinimizePenalty = true;
    	iRequiredFreeTimes = requiredFreeTimes;
    }
    
    public Selection getSelection(Student student) {
        return new Selection(student);
    }

   
    public class Selection extends BranchBoundSelection.Selection {
    	private Hashtable<CourseRequest, Double> iBestTime = new Hashtable<CourseRequest, Double>();
    	
        public Selection(Student student) {
        	super(student);
        }
        
        @SuppressWarnings("unchecked")
    	private double bestTime(Request r) {
        	if (r instanceof FreeTimeRequest) return 1.0;
        	CourseRequest cr = (CourseRequest)r;
        	Double cached = iBestTime.get(cr);
        	if (cached != null) return cached.doubleValue();
        	double bestTime = 0;
        	for (Iterator<Course> e = cr.getCourses().iterator(); e.hasNext();) {
        		Course course = e.next();
        		for (Iterator<Config> f = course.getOffering().getConfigs().iterator(); f.hasNext();) {
        			Config config = f.next();
        			int nrSubpartsWithTime = 0;
        			subparts: for (Iterator<Subpart> g = config.getSubparts().iterator(); g.hasNext(); ) {
        				Subpart subpart = g.next();
        				for (Iterator<Section> h = subpart.getSections().iterator(); h.hasNext(); ) {
        					Section section = h.next();
        					if (section.getTime() != null) { nrSubpartsWithTime++; continue subparts; }
        				}
        			}
        			double time = ((double)nrSubpartsWithTime / config.getSubparts().size());
        			if (time > bestTime) bestTime = time;
        		}
        	}
        	iBestTime.put(cr, bestTime);
        	return bestTime;
        }
        
        @Override
        public double getPenalty() {
            double bestPenalty = 0;
            for (int i = 0; i < iAssignment.length; i++)
                if (iAssignment[i] != null)
                    bestPenalty += getAssignmentPenalty(i);
                else
                	bestPenalty += iNotAssignedWeight * Math.pow(iPriorotyFactor, iStudent.getRequests().get(i).getPriority());
            return bestPenalty;
        }
        
        @SuppressWarnings("unchecked")
    	protected double getAssignmentPenalty(int i) {
        	double preferredFraction = 0.0;
        	int hasTime = 0;
        	double noTime = 0;
        	int penalty = 0;
        	int freeTimeOverlap = 0;
        	if (iAssignment[i].getAssignments() != null && iAssignment[i].isCourseRequest()) {
        		CourseRequest cr = (CourseRequest)iAssignment[i].getRequest();
            	int nrPreferred = 0;
            	int nrSelected = 0;
            	Set<Section> preferredSections = iPreferredSections.get(cr);
            	if (preferredSections != null)
                	for (Iterator<Section> j = iAssignment[i].getSections().iterator(); j.hasNext();) {
                		Section section = j.next();
                		if (preferredSections.contains(section)) nrPreferred++;
                	}
            	if (!cr.getSelectedChoices().isEmpty()) {
                	for (Iterator<Section> j = iAssignment[i].getSections().iterator(); j.hasNext();) {
                		Section section = j.next();
                		if (cr.getSelectedChoices().contains(section.getChoice())) nrSelected++;
                	}
            	}
            	preferredFraction = (0.67 * nrPreferred + 0.33 * nrSelected) / iAssignment[i].getAssignments().size();
            	for (Iterator<Section> j = iAssignment[i].getSections().iterator(); j.hasNext();) {
            		Section section = j.next();
            		if (section.getTime() != null) hasTime++;
            		if (section.getPenalty() > 0.0) penalty++;
            	}
            	noTime = bestTime(iAssignment[i].getRequest()) - (((double)hasTime) / iAssignment[i].getAssignments().size());
            	freeTimeOverlap = iStudent.countFreeTimeOverlaps(iAssignment[i]);
            }
        	return
        		iOnlinePenaltyWeight * penalty + 
        		iDistConfWeight * getNrDistanceConflicts(i) +
        		(1.0 - preferredFraction) * iPreferenceWeight +
        		noTime * iNoTimeWeight +
        		freeTimeOverlap * iOverlappingFreeTimeWeight;
        }
        
        public boolean isAllowed(int idx, Enrollment enrollment) {
        	if (enrollment.isCourseRequest()) {
        		Set<Section> reqSections = iRequiredSections.get((CourseRequest)enrollment.getRequest());
        		if (reqSections != null && !reqSections.isEmpty()) {
        			if (enrollment.getAssignments() == null || enrollment.getAssignments().isEmpty()) return false;
        			for (Section req: reqSections) {
        				if (!enrollment.getAssignments().contains(req)) return false;
        			}
        		}
        	} else if (iRequiredFreeTimes.contains(enrollment.getRequest())) {
        		if (enrollment.getAssignments() == null || enrollment.getAssignments().isEmpty()) return false;
        	}
        	return true;
        }
        
        public Enrollment firstConflict(int idx, Enrollment enrollment) {
            Enrollment conflict = super.firstConflict(idx, enrollment);
            if (conflict!=null) return conflict;
            return (isAllowed(idx, enrollment)?null:enrollment);
        }
        
        protected boolean canLeaveUnassigned(Request request) {
        	if (request instanceof CourseRequest) {
        		Set<Section> reqSections = iRequiredSections.get(request);
        		if (reqSections != null && !reqSections.isEmpty()) return false;
        	} else if (iRequiredFreeTimes.contains(request)) return false;
        	return true;
        }
    }
}
