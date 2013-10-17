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

import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;
import net.sf.cpsolver.studentsct.weights.EqualStudentWeights;
import net.sf.cpsolver.studentsct.weights.PriorityStudentWeights;
import net.sf.cpsolver.studentsct.weights.StudentWeights;

/**
 * @author Tomas Muller
 */
public class StudentSchedulingAssistantWeights implements StudentWeights {
	/** deduction for section with no time assignment */
    private double iNoTimeFactor = 0.050;
    /** deduction for sections that are not preferred (different time & instructor) */
    private double iSelectionFactor = 0.125;
    /** deduction for over expected sections */
    private double iPenaltyFactor = 0.250;
    /** similar to balancing factor on {@link PriorityStudentWeights} */
    private double iAvailabilityFactor = 0.050;
	/** negative penalty means there is space available */
	private double iAvgPenaltyFactor = 0.001;
	
	private Hashtable<CourseRequest, double[]> iCache = new Hashtable<CourseRequest, double[]>();
	
	private boolean iPriorityWeighting = true;

	private StudentWeights iParent;
	
	public StudentSchedulingAssistantWeights(DataProperties properties) {
		iNoTimeFactor = properties.getPropertyDouble("StudentWeights.NoTimeFactor", iNoTimeFactor);
		iSelectionFactor = properties.getPropertyDouble("StudentWeights.SelectionFactor", iSelectionFactor);
		iPenaltyFactor = properties.getPropertyDouble("StudentWeights.PenaltyFactor", iPenaltyFactor);
		iAvgPenaltyFactor = properties.getPropertyDouble("StudentWeights.AvgPenaltyFactor", iAvgPenaltyFactor);
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
	
	private double[] best(CourseRequest cr) {
		double[] cached = iCache.get(cr);
		if (cached != null) return cached;
		double bestTime = 0;
		double bestPenalty = 1.0;
		Double bestAvgPenalty = null;
		double bestSelected = 0.0;
		for (Course course: cr.getCourses()) {
			for (Config config: course.getOffering().getConfigs()) {
				int size = config.getSubparts().size();
				double sectionsWithTime = 0;
				double sectionsWithoutPenalty = 0;
				double penalty = 0;
				double selectedSections = 0;
				for (Subpart subpart: config.getSubparts()) {
					boolean hasTime = false;
					boolean noPenalty = false;
					Double sectionPenalty = null;
					boolean hasSelection = false;
					for (Section section: subpart.getSections()) {
						if (section.getLimit() == 0) continue;
						if (section.getTime() != null) hasTime = true;
						if (section.getPenalty() < 0.0) noPenalty = true;
						if (!cr.getSelectedChoices().isEmpty() && cr.getSelectedChoices().contains(section.getChoice())) hasSelection = true;
						if (sectionPenalty == null || sectionPenalty > section.getPenalty()) sectionPenalty = section.getPenalty();
					}
					if (hasTime) sectionsWithTime ++;
					if (noPenalty) sectionsWithoutPenalty ++;
					if (sectionPenalty != null) penalty += sectionPenalty;
					if (hasSelection) selectedSections ++;
				}
				if (sectionsWithTime / size > bestTime) bestTime = sectionsWithTime / size;
				double sectionsWithPenalty = size - sectionsWithoutPenalty;
				if (sectionsWithPenalty / size < bestPenalty) bestPenalty = sectionsWithPenalty / size;
				if (bestAvgPenalty == null || penalty / size < bestAvgPenalty) bestAvgPenalty = penalty / size;
				if (selectedSections / size > bestSelected) bestSelected = selectedSections / size;
			}
		}
		cached = new double[] { bestTime, bestPenalty, (bestAvgPenalty == null ? 0.0 : bestAvgPenalty), bestSelected };
		iCache.put(cr, cached);
		return cached;
	}
	
	public double getBaseWeight(Enrollment enrollment) {
		return iParent.getWeight(enrollment);
	}
	
	@Override
	public double getWeight(Enrollment enrollment) {
		if (!enrollment.isCourseRequest()) return getBaseWeight(enrollment);
		if (enrollment.getAssignments().isEmpty()) return 0;
		
		double base = getBaseWeight(enrollment);
		double weight = base;
		
		int size = enrollment.getAssignments().size();
		
		CourseRequest cr = (CourseRequest)enrollment.getRequest();
		double[] best = best(cr);
		
		double hasTime = 0;
		double penalty = 0;
		double totalPenalty = 0.0;
		for (Section section: enrollment.getSections()) {
    		if (section.getTime() != null) hasTime++;
    		if (section.getPenalty() >= 0.0) penalty++;
    		totalPenalty += section.getPenalty();
    	}
    	double noTime = best[0] - (hasTime / size);
    	double penaltyFraction = (penalty / size) - best[1];
    	double avgPenalty = (totalPenalty / size) - best[2];

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
		
		weight -= penaltyFraction * base * iPenaltyFactor;
		
		weight -= unselectedFraction * base * iSelectionFactor;
		
		weight -= noTime * base * iNoTimeFactor;
		
		weight -= unavailableSizeFraction * base * iAvailabilityFactor;
		
		weight -= avgPenalty * iAvgPenaltyFactor;
		
		return round(weight);
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
	public double getDistanceConflictWeight(DistanceConflict.Conflict distanceConflict) {
		return iParent.getDistanceConflictWeight(distanceConflict);
	}

	@Override
	public double getTimeOverlapConflictWeight(Enrollment enrollment, TimeOverlapsCounter.Conflict timeOverlap) {
		return iParent.getTimeOverlapConflictWeight(enrollment, timeOverlap);
	}

	@Override
	public boolean isFreeTimeAllowOverlaps() {
		return iParent.isFreeTimeAllowOverlaps();
	}
}
