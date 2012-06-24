/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.solver.multicriteria;

import java.util.Hashtable;
import java.util.Set;

import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

public class EqualWeightCriterion extends OnlineSectioningCriterion {
	
	public EqualWeightCriterion(Student student, StudentSectioningModel model, Hashtable<CourseRequest, Set<Section>> preferredSections) {
		super(student, model, preferredSections);
	}
	
    	@Override
	public int compare(Enrollment[] current, Enrollment[] best) {
		if (best == null) return -1;
		
		// 0. best number of assigned course requests (including alternativity & priority)
		int currentAssignedCourseReq = 0, bestAssignedCourseReq = 0;
		int currentAssignedRequests = 0, bestAssignedRequests = 0;
		int currentAssignedPriority = 0, bestAssignedPriority = 0;
		int currentAssignedAlternativity = 0, bestAssignedAlternativity = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (current[idx] != null && current[idx].getAssignments() != null) {
				currentAssignedRequests ++;
				if (current[idx].isCourseRequest())
					currentAssignedCourseReq ++;
				currentAssignedPriority += current[idx].getPriority() * current[idx].getPriority();
				currentAssignedAlternativity += (current[idx].getRequest().isAlternative() ? 1 : 0);
			}
			if (best[idx] != null && best[idx].getAssignments() != null) {
				bestAssignedRequests ++;
				if (best[idx].isCourseRequest())
					bestAssignedCourseReq ++;
				bestAssignedPriority += best[idx].getPriority() * best[idx].getPriority();
				bestAssignedAlternativity += (best[idx].getRequest().isAlternative() ? 1 : 0);
			}
		}
		if (currentAssignedCourseReq > bestAssignedCourseReq) return -1;
		if (bestAssignedCourseReq > currentAssignedCourseReq) return 1;
		if (currentAssignedPriority < bestAssignedPriority) return -1;
		if (bestAssignedPriority < currentAssignedPriority) return 1;
		if (currentAssignedAlternativity < bestAssignedAlternativity) return -1;
		if (bestAssignedAlternativity < currentAssignedAlternativity) return 1;
		
		// 1. minimize number of penalties
		int bestPenalties = 0, currentPenalties = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null && best[idx].getAssignments() != null && best[idx].isCourseRequest()) {
				for (Section section: best[idx].getSections())
		    		if (section.getPenalty() >= 0.0) bestPenalties++;
			}
			if (current[idx] != null && current[idx].getAssignments() != null && current[idx].isCourseRequest()) {
				for (Section section: current[idx].getSections())
		    		if (section.getPenalty() >= 0.0) currentPenalties++;
			}
		}
		if (currentPenalties < bestPenalties) return -1;
		if (bestPenalties < currentPenalties) return 1;
		
		// 2. best number of assigned requests (including free time requests)
		if (currentAssignedRequests > bestAssignedRequests) return -1;
		if (bestAssignedRequests > currentAssignedRequests) return 1;
		
		// 3. maximize selection
    	int bestSelected = 0, currentSelected = 0;
		for (int idx = 0; idx < current.length; idx++) {
			Set<Section> preferred = getPreferredSections(getRequest(idx));
    		if (preferred != null && !preferred.isEmpty()) {
    			if (best[idx] != null && best[idx].getAssignments() != null && best[idx].isCourseRequest()) {
        			for (Section section: best[idx].getSections())
        				if (preferred.contains(section)) bestSelected ++;
    			}
    			if (current[idx] != null && current[idx].getAssignments() != null && current[idx].isCourseRequest()) {
        			for (Section section: current[idx].getSections())
        				if (preferred.contains(section)) currentSelected ++;
        		}
			}
		}
		if (currentSelected > bestSelected) return -1;
		if (bestSelected > currentSelected) return 1;

		// 4. avoid time overlaps
		if (getModel().getTimeOverlaps() != null) {
			int bestTimeOverlaps = 0, currentTimeOverlaps = 0;
			for (int idx = 0; idx < current.length; idx++) {
				if (best[idx] != null && best[idx].getAssignments() != null) {
			        for (int x = 0; x < idx; x++) {
			        	if (best[x] != null && best[x].getAssignments() != null)
			        		bestTimeOverlaps += getModel().getTimeOverlaps().nrConflicts(best[x], best[idx]);
			        	else if (getStudent().getRequests().get(x) instanceof FreeTimeRequest)
			        		bestTimeOverlaps += getModel().getTimeOverlaps().nrConflicts(((FreeTimeRequest)getStudent().getRequests().get(x)).createEnrollment(), best[idx]);
			        }
				}
				if (current[idx] != null && current[idx].getAssignments() != null) {
			        for (int x = 0; x < idx; x++) {
			        	if (current[x] != null && current[x].getAssignments() != null)
			        		currentTimeOverlaps += getModel().getTimeOverlaps().nrConflicts(current[x], current[idx]);
			        	else if (getStudent().getRequests().get(x) instanceof FreeTimeRequest)
			        		currentTimeOverlaps += getModel().getTimeOverlaps().nrConflicts(((FreeTimeRequest)getStudent().getRequests().get(x)).createEnrollment(), current[idx]);
			        }
				}
			}
			if (currentTimeOverlaps < bestTimeOverlaps) return -1;
			if (bestTimeOverlaps < currentTimeOverlaps) return 1;
		}
		
		// 5. avoid distance conflicts
		if (getModel().getDistanceConflict() != null) {
			int bestDistanceConf = 0, currentDistanceConf = 0;
			for (int idx = 0; idx < current.length; idx++) {
				if (best[idx] != null && best[idx].getAssignments() != null) {
			        for (int x = 0; x < idx; x++) {
			        	if (best[x] != null && best[x].getAssignments() != null)
			        		bestDistanceConf += getModel().getDistanceConflict().nrConflicts(best[x], best[idx]);
			        }
				}
				if (current[idx] != null && current[idx].getAssignments() != null) {
			        for (int x = 0; x < idx; x++) {
			        	if (current[x] != null && current[x].getAssignments() != null)
			        		currentDistanceConf += getModel().getDistanceConflict().nrConflicts(current[x], current[idx]);
			        }
				}
			}
			if (currentDistanceConf < bestDistanceConf) return -1;
			if (bestDistanceConf < currentDistanceConf) return 1;
		}
		
		// 6. avoid no-time sections
    	int bestNoTime = 0, currentNoTime = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null && best[idx].getAssignments() != null) {
    			for (Section section: best[idx].getSections())
    				if (section.getTime() == null) bestNoTime++;
			}
			if (current[idx] != null && current[idx].getAssignments() != null) {
    			for (Section section: current[idx].getSections())
    				if (section.getTime() == null) currentNoTime++;
			}
		}
		if (currentNoTime < bestNoTime) return -1;
		if (bestNoTime < currentNoTime) return 1;
		
		// 7. balance sections
		double bestUnavailableSize = 0.0, currentUnavailableSize = 0.0;
		int bestAltSectionsWithLimit = 0, currentAltSectionsWithLimit = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null && best[idx].getAssignments() != null) {
				for (Section section: best[idx].getSections()) {
		            Subpart subpart = section.getSubpart();
		            // skip unlimited and single section subparts
		            if (subpart.getSections().size() <= 1 || subpart.getLimit() <= 0) continue;
		            // average size
		            double averageSize = ((double)subpart.getLimit()) / subpart.getSections().size();
		            // section is below average
		            if (section.getLimit() < averageSize)
		            	bestUnavailableSize += (averageSize - section.getLimit()) / averageSize;
		            bestAltSectionsWithLimit ++;
				}
			}
			if (current[idx] != null && current[idx].getAssignments() != null) {
				for (Section section: current[idx].getSections()) {
		            Subpart subpart = section.getSubpart();
		            // skip unlimited and single section subparts
		            if (subpart.getSections().size() <= 1 || subpart.getLimit() <= 0) continue;
		            // average size
		            double averageSize = ((double)subpart.getLimit()) / subpart.getSections().size();
		            // section is below average
		            if (section.getLimit() < averageSize)
		            	currentUnavailableSize += (averageSize - section.getLimit()) / averageSize;
		            currentAltSectionsWithLimit ++;
				}
			}
		}
		double bestUnavailableSizeFraction = (bestUnavailableSize > 0 ? bestUnavailableSize / bestAltSectionsWithLimit : 0.0);
		double currentUnavailableSizeFraction = (currentUnavailableSize > 0 ? currentUnavailableSize / currentAltSectionsWithLimit : 0.0);
		if (currentUnavailableSizeFraction < bestUnavailableSizeFraction) return -1;
		if (bestUnavailableSizeFraction < currentUnavailableSizeFraction) return 1;
		
		// 8. average penalty sections
		double bestPenalty = 0.0, currentPenalty = 0.0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null && best[idx].getAssignments() != null) {
				for (Section section: best[idx].getSections())
					bestPenalty += section.getPenalty();
			}
			if (current[idx] != null && current[idx].getAssignments() != null) {
				for (Section section: current[idx].getSections())
					currentPenalty += section.getPenalty();
			}
		}
		if (currentPenalty < bestPenalty) return -1;
		if (bestPenalty < currentPenalty) return 1;
		
		return 0;
	}

	@Override
	public boolean canImprove(int maxIdx, Enrollment[] current, Enrollment[] best) {
		// 0. best number of assigned course requests (including alternativity & priority)
		int currentAssignedCourseReq = 0, bestAssignedCourseReq = 0;
		int currentAssignedRequests = 0, bestAssignedRequests = 0;
		int currentAssignedPriority = 0, bestAssignedPriority = 0;
		int currentAssignedAlternativity = 0, bestAssignedAlternativity = 0;
		int alt = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (idx < maxIdx) {
				if (current[idx] != null && current[idx].getAssignments() != null) {
					currentAssignedRequests ++;
					if (current[idx].isCourseRequest())
						currentAssignedCourseReq ++;
					currentAssignedPriority += current[idx].getPriority() * current[idx].getPriority();
					currentAssignedAlternativity += (current[idx].getRequest().isAlternative() ? 1 : 0);
				} else if (!isFreeTime(idx) && !getRequest(idx).isAlternative()) {
					alt ++;
				}
			} else {
				if (!getRequest(idx).isAlternative()) {
					currentAssignedRequests ++;
					if (!isFreeTime(idx))
						currentAssignedCourseReq ++;
				} else if (alt > 0) {
					currentAssignedRequests ++;
					currentAssignedCourseReq ++;
					alt --; currentAssignedAlternativity ++;
				}
			}
			if (best[idx] != null && best[idx].getAssignments() != null) {
				bestAssignedRequests ++;
				if (best[idx].isCourseRequest())
					bestAssignedCourseReq ++;
				bestAssignedPriority += best[idx].getPriority() * best[idx].getPriority();
				bestAssignedAlternativity += (best[idx].getRequest().isAlternative() ? 1 : 0);
			}
		}
		if (currentAssignedCourseReq > bestAssignedCourseReq) return true;
		if (bestAssignedCourseReq > currentAssignedCourseReq) return false;
		if (currentAssignedPriority < bestAssignedPriority) return true;
		if (bestAssignedPriority < currentAssignedPriority) return false;
		if (currentAssignedAlternativity < bestAssignedAlternativity) return true;
		if (bestAssignedAlternativity < currentAssignedAlternativity) return false;
		
		// 1. maximize number of penalties
		int bestPenalties = 0, currentPenalties = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null) {
				for (Section section: best[idx].getSections())
		    		if (section.getPenalty() >= 0.0) bestPenalties++;
			}
			if (current[idx] != null && idx < maxIdx) {
				for (Section section: current[idx].getSections())
		    		if (section.getPenalty() >= 0.0) currentPenalties++;
			}
		}
		if (currentPenalties < bestPenalties) return true;
		if (bestPenalties < currentPenalties) return false;
		
		// 2. best number of assigned requests (including free time requests)
		if (currentAssignedRequests > bestAssignedRequests) return true;
		if (bestAssignedRequests > currentAssignedRequests) return false;

		// 3. maximize selection
    	int bestSelected = 0, currentSelected = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null && best[idx].isCourseRequest()) {
				Set<Section> preferred = getPreferredSections(best[idx].getRequest());
        		if (preferred != null && !preferred.isEmpty()) {
        			for (Section section: best[idx].getSections())
        				if (preferred.contains(section)) {
        					if (idx < maxIdx) bestSelected ++;
        				} else if (idx >= maxIdx) bestSelected --;
        		}
			}
			if (current[idx] != null && idx < maxIdx && current[idx].isCourseRequest()) {
				Set<Section> preferred = getPreferredSections(current[idx].getRequest());
        		if (preferred != null && !preferred.isEmpty()) {
        			for (Section section: current[idx].getSections())
        				if (preferred.contains(section)) currentSelected ++;
        		}
			}
		}
		if (currentSelected > bestSelected) return true;
		if (bestSelected > currentSelected) return false;

		// 4. avoid time overlaps
		if (getModel().getTimeOverlaps() != null) {
			int bestTimeOverlaps = 0, currentTimeOverlaps = 0;
			for (int idx = 0; idx < current.length; idx++) {
				if (best[idx] != null) {
			        for (int x = 0; x < idx; x++) {
			        	if (best[x] != null)
			        		bestTimeOverlaps += getModel().getTimeOverlaps().nrConflicts(best[x], best[idx]);
			        	else if (getStudent().getRequests().get(x) instanceof FreeTimeRequest)
			        		bestTimeOverlaps += getModel().getTimeOverlaps().nrConflicts(((FreeTimeRequest)getStudent().getRequests().get(x)).createEnrollment(), best[idx]);
			        }
				}
				if (current[idx] != null && idx < maxIdx) {
			        for (int x = 0; x < idx; x++) {
			        	if (current[x] != null)
			        		currentTimeOverlaps += getModel().getTimeOverlaps().nrConflicts(current[x], current[idx]);
			        	else if (getStudent().getRequests().get(x) instanceof FreeTimeRequest)
			        		currentTimeOverlaps += getModel().getTimeOverlaps().nrConflicts(((FreeTimeRequest)getStudent().getRequests().get(x)).createEnrollment(), current[idx]);
			        }
				}
			}
			if (currentTimeOverlaps < bestTimeOverlaps) return true;
			if (bestTimeOverlaps < currentTimeOverlaps) return false;
		}
				
		// 5. avoid distance conflicts
		if (getModel().getDistanceConflict() != null) {
			int bestDistanceConf = 0, currentDistanceConf = 0;
			for (int idx = 0; idx < current.length; idx++) {
				if (best[idx] != null) {
			        for (int x = 0; x < idx; x++) {
			        	if (best[x] != null)
			        		bestDistanceConf += getModel().getDistanceConflict().nrConflicts(best[x], best[idx]);
			        }
				}
				if (current[idx] != null && idx < maxIdx) {
			        for (int x = 0; x < idx; x++) {
			        	if (current[x] != null)
			        		currentDistanceConf += getModel().getDistanceConflict().nrConflicts(current[x], current[idx]);
			        }
				}
			}
			if (currentDistanceConf < bestDistanceConf) return true;
			if (bestDistanceConf < currentDistanceConf) return false;
		}
		
		// 6. avoid no-time sections
    	int bestNoTime = 0, currentNoTime = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null) {
    			for (Section section: best[idx].getSections())
    				if (section.getTime() == null) bestNoTime++;
			}
			if (current[idx] != null && idx < maxIdx) {
    			for (Section section: current[idx].getSections())
    				if (section.getTime() == null) currentNoTime++;
			}
		}
		if (currentNoTime < bestNoTime) return true;
		if (bestNoTime < currentNoTime) return false;
		
		// 7. balance sections
		double bestUnavailableSize = 0.0, currentUnavailableSize = 0.0;
		int bestAltSectionsWithLimit = 0, currentAltSectionsWithLimit = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null) {
				for (Section section: best[idx].getSections()) {
		            Subpart subpart = section.getSubpart();
		            // skip unlimited and single section subparts
		            if (subpart.getSections().size() <= 1 || subpart.getLimit() <= 0) continue;
		            // average size
		            double averageSize = ((double)subpart.getLimit()) / subpart.getSections().size();
		            // section is below average
		            if (section.getLimit() < averageSize)
		            	bestUnavailableSize += (averageSize - section.getLimit()) / averageSize;
		            bestAltSectionsWithLimit ++;
				}
			}
			if (current[idx] != null && idx < maxIdx) {
				for (Section section: current[idx].getSections()) {
		            Subpart subpart = section.getSubpart();
		            // skip unlimited and single section subparts
		            if (subpart.getSections().size() <= 1 || subpart.getLimit() <= 0) continue;
		            // average size
		            double averageSize = ((double)subpart.getLimit()) / subpart.getSections().size();
		            // section is below average
		            if (section.getLimit() < averageSize)
		            	currentUnavailableSize += (averageSize - section.getLimit()) / averageSize;
		            currentAltSectionsWithLimit ++;
				}
			}
		}
		double bestUnavailableSizeFraction = (bestUnavailableSize > 0 ? bestUnavailableSize / bestAltSectionsWithLimit : 0.0);
		double currentUnavailableSizeFraction = (currentUnavailableSize > 0 ? currentUnavailableSize / currentAltSectionsWithLimit : 0.0);
		if (currentUnavailableSizeFraction < bestUnavailableSizeFraction) return true;
		if (bestUnavailableSizeFraction < currentUnavailableSizeFraction) return false;
		
		// 8. average penalty sections
		double bestPenalty = 0.0, currentPenalty = 0.0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null) {
				for (Section section: best[idx].getSections())
					bestPenalty += section.getPenalty();
				if (idx >= maxIdx && best[idx].isCourseRequest())
					bestPenalty -= ((CourseRequest)best[idx].getRequest()).getMinPenalty();
			}
			if (current[idx] != null && idx < maxIdx) {
				for (Section section: current[idx].getSections())
					currentPenalty += section.getPenalty();
			}
		}
		if (currentPenalty < bestPenalty) return true;
		if (bestPenalty < currentPenalty) return false;
		
		return true;
	}
}