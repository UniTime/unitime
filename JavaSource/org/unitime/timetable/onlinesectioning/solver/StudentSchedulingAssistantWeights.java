/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
import java.util.Set;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.weights.EqualStudentWeights;
import org.cpsolver.studentsct.weights.PriorityStudentWeights;
import org.cpsolver.studentsct.weights.StudentWeights;
import org.unitime.timetable.onlinesectioning.solver.expectations.MoreSpaceThanExpected;


/**
 * @author Tomas Muller
 */
public class StudentSchedulingAssistantWeights implements StudentWeights {
	/** deduction for section with no time assignment */
    private double iNoTimeFactor = 0.050;
    /** deduction for sections that are not preferred (different time & instructor) */
    private double iSelectionFactor = 0.125;
    /** deduction for over expected sections */
    private double iOverExpectedFactor = 0.250;
    /** similar to balancing factor on {@link PriorityStudentWeights} */
    private double iAvailabilityFactor = 0.050;
	/** negative penalty means there is space available */
	private double iPenaltyFactor = 0.001;
	
	private Hashtable<CourseRequest, double[]> iCache = new Hashtable<CourseRequest, double[]>();
	
	private boolean iPriorityWeighting = true;

	private StudentWeights iParent;
	
	public StudentSchedulingAssistantWeights(DataProperties properties) {
		iNoTimeFactor = properties.getPropertyDouble("StudentWeights.NoTimeFactor", iNoTimeFactor);
		iSelectionFactor = properties.getPropertyDouble("StudentWeights.SelectionFactor", iSelectionFactor);
		iOverExpectedFactor = properties.getPropertyDouble("StudentWeights.PenaltyFactor", iOverExpectedFactor);
		iPenaltyFactor = properties.getPropertyDouble("StudentWeights.AvgPenaltyFactor", iPenaltyFactor);
		iAvailabilityFactor = properties.getPropertyDouble("StudentWeights.AvailabilityFactor", iAvailabilityFactor);
		iPriorityWeighting = properties.getPropertyBoolean("StudentWeights.PriorityWeighting", iPriorityWeighting);
		if (iPriorityWeighting)
			iParent = new PriorityStudentWeights(properties);
		else
			iParent = new EqualStudentWeights(properties);
	}
	
	public void clearBestCache() {
		iCache.clear();
	}
	
	private double getOverExpected(Assignment<Request, Enrollment> assignment, Section section, Request request) {
		if (request.getModel() == null || !(request.getModel() instanceof OnlineSectioningModel))
			return new MoreSpaceThanExpected().getOverExpected(assignment, section, request);
		return ((OnlineSectioningModel)request.getModel()).getOverExpected(assignment, section, request);
	}
	
	private double[] best(Assignment<Request, Enrollment> assignment, CourseRequest cr) {
		double[] cached = iCache.get(cr);
		if (cached != null) return cached;
		double bestTime = 0;
		Double bestOverExpected = null;
		Double bestAvgPenalty = null;
		double bestSelected = 0.0;
		for (Course course: cr.getCourses()) {
			for (Config config: course.getOffering().getConfigs()) {
				int size = config.getSubparts().size();
				double sectionsWithTime = 0;
				double overExpected = 0;
				double penalty = 0;
				double selectedSections = 0;
				for (Subpart subpart: config.getSubparts()) {
					boolean hasTime = false;
					Double sectionPenalty = null;
					Double sectionOverExpected = null;
					boolean hasSelection = false;
					for (Section section: subpart.getSections()) {
						if (section.getLimit() == 0) continue;
						if (section.getTime() != null) hasTime = true;
						if (!cr.getSelectedChoices().isEmpty() && cr.getSelectedChoices().contains(section.getChoice())) hasSelection = true;
						if (sectionPenalty == null || sectionPenalty > section.getPenalty()) sectionPenalty = section.getPenalty();
						double oexp = getOverExpected(assignment, section, cr);
						if (sectionOverExpected == null || sectionOverExpected > oexp) sectionOverExpected = oexp;
					}
					if (hasTime) sectionsWithTime ++;
					if (sectionPenalty != null) penalty += sectionPenalty;
					if (hasSelection) selectedSections ++;
					if (sectionOverExpected != null) overExpected += sectionOverExpected;
				}
				if (sectionsWithTime / size > bestTime) bestTime = sectionsWithTime / size;
				if (bestOverExpected == null || overExpected < bestOverExpected) bestOverExpected = overExpected;
				if (bestAvgPenalty == null || penalty / size < bestAvgPenalty) bestAvgPenalty = penalty / size;
				if (selectedSections / size > bestSelected) bestSelected = selectedSections / size;
			}
		}
		cached = new double[] { bestTime, (bestOverExpected == null ? 0.0 : bestOverExpected), (bestAvgPenalty == null ? 0.0 : bestAvgPenalty), bestSelected };
		iCache.put(cr, cached);
		return cached;
	}
	
	public double getBaseWeight(Assignment<Request, Enrollment> assignment, Enrollment enrollment) {
		return iParent.getWeight(assignment, enrollment);
	}
	
	@Override
	public double getWeight(Assignment<Request, Enrollment> assignment, Enrollment enrollment) {
		if (!enrollment.isCourseRequest()) return getBaseWeight(assignment, enrollment);
		if (enrollment.getAssignments().isEmpty()) return 0;
		
		double base = getBaseWeight(assignment, enrollment);
		double weight = base;
		
		int size = enrollment.getAssignments().size();
		
		CourseRequest cr = (CourseRequest)enrollment.getRequest();
		double[] best = best(assignment, cr);
		
		double hasTime = 0;
		double oexp = 0;
		double penalty = 0.0;
		for (Section section: enrollment.getSections()) {
    		if (section.getTime() != null) hasTime++;
    		oexp += getOverExpected(assignment, section, cr);
    		penalty += section.getPenalty();
    	}
    	double noTime = best[0] - (hasTime / size);
    	double overExpected = oexp - best[1];
    	double avgPenalty = (penalty / size) - best[2];

    	int nrSelected = 0;
    	if (!cr.getSelectedChoices().isEmpty()) {
        	for (Section section: enrollment.getSections())
        		if (cr.getSelectedChoices().contains(section.getChoice())) nrSelected++;
    	}
    	double unselectedFraction = best[3] - (nrSelected / size);
		
        double unavailableSize = 0;
        double altSectionsWithLimit = 0;
        for (Section section: enrollment.getSections()) {
            Subpart subpart = section.getSubpart();
            // skip unlimited and single section subparts
            if (subpart.getSections().size() <= 1 || subpart.getLimit() <= 0) continue;
            // average size
            double averageSize = ((double)subpart.getLimit()) / subpart.getSections().size();
            // section is below average
            if (section.getLimit() < averageSize)
                unavailableSize += (averageSize - section.getLimit()) / averageSize;
            altSectionsWithLimit ++;
        }
        double unavailableSizeFraction = (unavailableSize > 0 ? unavailableSize / altSectionsWithLimit : 0.0);
		
		weight -= overExpected * base * iOverExpectedFactor;
		
		weight -= unselectedFraction * base * iSelectionFactor;
		
		weight -= noTime * base * iNoTimeFactor;
		
		weight -= unavailableSizeFraction * base * iAvailabilityFactor;
		
		weight -= avgPenalty * iPenaltyFactor;
		
		return round(weight);
	}
	
	@Override
	public double getWeight(Assignment<Request, Enrollment> assignment, Enrollment enrollment, Set<DistanceConflict.Conflict> distanceConflicts, Set<TimeOverlapsCounter.Conflict> timeOverlappingConflicts) {
		if (enrollment.getAssignments().isEmpty()) return 0;
		
		double weight = getWeight(assignment, enrollment);
		
        if (distanceConflicts != null)
            for (DistanceConflict.Conflict c: distanceConflicts) {
                Enrollment other = (c.getE1().equals(enrollment) ? c.getE2() : c.getE1());
                if (other.getRequest().getPriority() <= enrollment.getRequest().getPriority())
                	weight -= getDistanceConflictWeight(assignment, c);
            }
        
        if (timeOverlappingConflicts != null)
            for (TimeOverlapsCounter.Conflict c: timeOverlappingConflicts) {
            	weight -= getTimeOverlapConflictWeight(assignment, enrollment, c);
            }
		
		return weight;
		
	}
	
    protected double round(double value) {
        return Math.ceil(10000.0 * value) / 10000.0;
    }

	@Override
	public boolean isBetterThanBestSolution(Solution<Request, Enrollment> currentSolution) {
		return iParent.isBetterThanBestSolution(currentSolution);
	}

	@Override
	public double getBound(Request request) {
		return iParent.getBound(request);
	}

	@Override
	public double getDistanceConflictWeight(Assignment<Request, Enrollment> assignment, DistanceConflict.Conflict distanceConflict) {
		return iParent.getDistanceConflictWeight(assignment, distanceConflict);
	}

	@Override
	public double getTimeOverlapConflictWeight(Assignment<Request, Enrollment> assignment, Enrollment enrollment, TimeOverlapsCounter.Conflict timeOverlap) {
		return iParent.getTimeOverlapConflictWeight(assignment, enrollment, timeOverlap);
	}

	@Override
	public boolean isFreeTimeAllowOverlaps() {
		return iParent.isFreeTimeAllowOverlaps();
	}
}
