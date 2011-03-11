/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.solver;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;
import net.sf.cpsolver.studentsct.weights.PriorityStudentWeights;

/**
 * @author Tomas Muller
 */
public class StudentSchedulingAssistantWeights extends PriorityStudentWeights {
	/** deduction for section with no time assignment */
    private double iNoTimeFactor = 0.050;
    /** deduction for sections that are not preferred (different time & instructor) */
    private double iPreferenceFactor = 0.125;
    /** deduction for over expected sections */
    private double iPenaltyFactor = 0.250;
    /** similar to balancing factor on {@link PriorityStudentWeights} */
    private double iAvailabilityFactor;
	private Hashtable<CourseRequest, Double> iBestTime = new Hashtable<CourseRequest, Double>();
	
	public StudentSchedulingAssistantWeights(DataProperties properties) {
		super(properties);
		iNoTimeFactor = properties.getPropertyDouble("StudentWeights.NoTimeFactor", iNoTimeFactor);
		iPreferenceFactor = properties.getPropertyDouble("StudentWeights.PreferenceFactor", iPreferenceFactor);
		iPenaltyFactor = properties.getPropertyDouble("StudentWeights.PenaltyFactor", iPenaltyFactor);
		iAvailabilityFactor = iBalancingFactor; iBalancingFactor = 0.0;
	}
	
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
	public double getWeight(Enrollment enrollment) {
		if (enrollment.getAssignments().isEmpty()) return 0;
		
		double base = super.getWeight(enrollment);
		double weight = base;
		
		int size = enrollment.getAssignments().size();
		
		double hasTime = 0;
		double penalty = 0;
		if (enrollment.isCourseRequest() && enrollment.getAssignments() != null) {
    		for (Section section: enrollment.getSections()) {
        		if (section.getTime() != null) hasTime++;
        		if (section.getPenalty() > 0.0) penalty++;
        	}
		} else {
			hasTime = 1.0;
		}
    	double noTime = bestTime(enrollment.getRequest()) - (hasTime / size);
    	double penaltyFraction = penalty / size;

    	double selectedFraction = 1.0;
		if (enrollment.isCourseRequest() && enrollment.getAssignments() != null) {
    		CourseRequest cr = (CourseRequest)enrollment.getRequest();
        	int nrSelected = 0;
        	if (!cr.getSelectedChoices().isEmpty()) {
            	for (Section section: enrollment.getSections()) {
            		if (cr.getSelectedChoices().contains(section.getChoice())) nrSelected++;
            	}
        	}
        	selectedFraction = (size - nrSelected) / size;
		}
		
		double unavailableSizeFraction = 0.0;
		if (enrollment.isCourseRequest() && enrollment.getAssignments() != null) {
            double unavailableSize = 0;
            double total = 0;
            for (Section section: enrollment.getSections()) {
                Subpart subpart = section.getSubpart();
                // skip unlimited and single section subparts
                if (subpart.getSections().size() <= 1 || subpart.getLimit() <= 0) continue;
                // average size
                double averageSize = ((double)subpart.getLimit()) / subpart.getSections().size();
                // section is below average
                if (section.getLimit() < averageSize)
                    unavailableSize += (averageSize - section.getLimit()) / averageSize;
                total ++;
            }
            if (unavailableSize > 0)
            	unavailableSizeFraction = unavailableSize / total;
		}
		
		weight -= penaltyFraction * base * iPenaltyFactor;
		
		weight -= selectedFraction * base * iPreferenceFactor;
		
		weight -= noTime * base * iNoTimeFactor;
		
		weight -= unavailableSizeFraction * base * iAvailabilityFactor;
		
		return weight;
		
	}
	
	@Override
	public double getWeight(Enrollment enrollment, Set<DistanceConflict.Conflict> distanceConflicts, Set<TimeOverlapsCounter.Conflict> timeOverlappingConflicts) {
		if (enrollment.getAssignments().isEmpty()) return 0;
		
		double weight = getWeight(enrollment);
		
        if (distanceConflicts != null)
            for (DistanceConflict.Conflict c: distanceConflicts) {
                Enrollment other = (c.getE1().equals(enrollment) ? c.getE2() : c.getE1());
                if (other.getRequest().getPriority() <= enrollment.getRequest().getPriority())
                	weight -= getDistanceConflictWeight(c);
            }
        
        if (timeOverlappingConflicts != null)
            for (TimeOverlapsCounter.Conflict c: timeOverlappingConflicts) {
            	weight -= getTimeOverlapConflictWeight(enrollment, c);
            }
		
		return weight;
		
	}
}
