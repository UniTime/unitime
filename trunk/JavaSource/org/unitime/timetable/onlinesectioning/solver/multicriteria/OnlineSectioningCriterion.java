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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.unitime.timetable.onlinesectioning.solver.multicriteria.MultiCriteriaBranchAndBoundSelection.SelectionCriterion;

import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;
import net.sf.cpsolver.studentsct.weights.StudentWeights;

public class OnlineSectioningCriterion implements SelectionCriterion {
	private Hashtable<CourseRequest, Set<Section>> iPreferredSections = null;

	private StudentSectioningModel iModel;
	private Student iStudent;
	
	public OnlineSectioningCriterion(Student student, StudentSectioningModel model, Hashtable<CourseRequest, Set<Section>> preferredSections) {
		iStudent = student;
		iModel = model;
    	iPreferredSections = preferredSections;
	}
	
    /**
     * Distance conflicts of idx-th assignment of the current
     * schedule
     */
    public Set<DistanceConflict.Conflict> getDistanceConflicts(Enrollment[] assignment, int idx) {
        if (iModel.getDistanceConflict() == null || assignment[idx] == null)
            return null;
        Set<DistanceConflict.Conflict> dist = iModel.getDistanceConflict().conflicts(assignment[idx]);
        for (int x = 0; x < idx; x++)
            if (assignment[x] != null)
                dist.addAll(iModel.getDistanceConflict().conflicts(assignment[x], assignment[idx]));
        return dist;
    }
    
    /**
     * Time overlapping conflicts of idx-th assignment of the current
     * schedule
     */
    public Set<TimeOverlapsCounter.Conflict> getTimeOverlappingConflicts(Enrollment[] assignment, int idx) {
        if (iModel.getTimeOverlaps() == null || assignment[idx] == null)
            return null;
        Set<TimeOverlapsCounter.Conflict> overlaps = new HashSet<TimeOverlapsCounter.Conflict>();
        for (int x = 0; x < idx; x++)
            if (assignment[x] != null)
                overlaps.addAll(iModel.getTimeOverlaps().conflicts(assignment[x], assignment[idx]));
            else if (iStudent.getRequests().get(x) instanceof FreeTimeRequest)
                overlaps.addAll(iModel.getTimeOverlaps().conflicts(((FreeTimeRequest)iStudent.getRequests().get(x)).createEnrollment(), assignment[idx]));
        return overlaps;
    }
    
    /**
     * Weight of an assignment. Unlike {@link StudentWeights#getWeight(Enrollment, Set, Set)}, only count this side of distance conflicts and time overlaps.
     **/
    protected double getWeight(Enrollment enrollment, Set<DistanceConflict.Conflict> distanceConflicts, Set<TimeOverlapsCounter.Conflict> timeOverlappingConflicts) {
        double weight = - iModel.getStudentWeights().getWeight(enrollment);
        if (distanceConflicts != null)
            for (DistanceConflict.Conflict c: distanceConflicts) {
                Enrollment other = (c.getE1().equals(enrollment) ? c.getE2() : c.getE1());
                if (other.getRequest().getPriority() <= enrollment.getRequest().getPriority())
                    weight += iModel.getStudentWeights().getDistanceConflictWeight(c);
            }
        if (timeOverlappingConflicts != null)
            for (TimeOverlapsCounter.Conflict c: timeOverlappingConflicts) {
                weight += iModel.getStudentWeights().getTimeOverlapConflictWeight(enrollment, c);
            }
        return enrollment.getRequest().getWeight() * weight;
    }
    
    

	@Override
	public boolean isBetter(Enrollment[] current, Enrollment[] best) {
		// 1. best priority & alternativity
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null) {
				if (current[idx] == null) return false; // higher priority request assigned
				if (best[idx].getPriority() < current[idx].getPriority()) return false; // less alternative request assigned
			} else {
				if (current[idx] != null) return true; // higher priority request assigned
			}
		}
		
		// 2. minimize number of penalties
		int bestPenalties = 0, currentPenalties = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null) {
				for (Section section: best[idx].getSections())
		    		if (section.getPenalty() >= 0.0) bestPenalties++;
				for (Section section: current[idx].getSections())
		    		if (section.getPenalty() >= 0.0) currentPenalties++;
			}
		}
		if (currentPenalties < bestPenalties) return true;
		if (bestPenalties < currentPenalties) return false;
		
		// 3. maximize selection
    	int bestSelected = 0, currentSelected = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null && best[idx].isCourseRequest()) {
				Set<Section> preferred = iPreferredSections.get((CourseRequest)best[idx].getRequest());
        		if (preferred != null && !preferred.isEmpty()) {
        			for (Section section: best[idx].getSections())
        				if (preferred.contains(section)) bestSelected ++;
        			for (Section section: current[idx].getSections())
        				if (preferred.contains(section)) currentSelected ++;
        		}
			}
		}
		if (currentSelected > bestSelected) return true;
		if (bestSelected > currentSelected) return false;

		// 4. avoid time overlaps
		if (iModel.getTimeOverlaps() != null) {
			int bestTimeOverlaps = 0, currentTimeOverlaps = 0;
			for (int idx = 0; idx < current.length; idx++) {
				if (best[idx] != null) {
			        for (int x = 0; x < idx; x++) {
			        	if (best[x] != null)
			        		bestTimeOverlaps += iModel.getTimeOverlaps().nrConflicts(best[x], best[idx]);
			        	else if (iStudent.getRequests().get(x) instanceof FreeTimeRequest)
			        		bestTimeOverlaps += iModel.getTimeOverlaps().nrConflicts(((FreeTimeRequest)iStudent.getRequests().get(x)).createEnrollment(), best[idx]);
			        }
			        for (int x = 0; x < idx; x++) {
			        	if (current[x] != null)
			        		currentTimeOverlaps += iModel.getTimeOverlaps().nrConflicts(current[x], current[idx]);
			        	else if (iStudent.getRequests().get(x) instanceof FreeTimeRequest)
			        		currentTimeOverlaps += iModel.getTimeOverlaps().nrConflicts(((FreeTimeRequest)iStudent.getRequests().get(x)).createEnrollment(), current[idx]);
			        }
				}
			}
			if (currentTimeOverlaps < bestTimeOverlaps) return true;
			if (bestTimeOverlaps < currentTimeOverlaps) return false;
		}
		
		// 5. avoid distance conflicts
		if (iModel.getDistanceConflict() != null) {
			int bestDistanceConf = 0, currentDistanceConf = 0;
			for (int idx = 0; idx < current.length; idx++) {
				if (best[idx] != null) {
			        for (int x = 0; x < idx; x++) {
			        	if (best[x] != null)
			        		bestDistanceConf += iModel.getDistanceConflict().nrConflicts(best[x], best[idx]);
			        }
			        for (int x = 0; x < idx; x++) {
			        	if (current[x] != null)
			        		currentDistanceConf += iModel.getDistanceConflict().nrConflicts(current[x], current[idx]);
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
				for (Section section: current[idx].getSections())
					currentPenalty += section.getPenalty();
			}
		}
		if (currentPenalty < bestPenalty) return true;
		if (bestPenalty < currentPenalty) return false;
		
		return false;
	}

	@Override
	public boolean canImprove(int maxIdx, Enrollment[] current, Enrollment[] best) {
		// 1. best priority & alternativity
		int alt = 0;
		for (int idx = 0; idx < maxIdx; idx++) {
			Request request = iStudent.getRequests().get(idx);
			if (best[idx] != null) {
				if (current[idx] == null) return false; // higher priority request assigned
				if (best[idx].getPriority() < current[idx].getPriority()) return false; // less alternative request assigned
				if (request.isAlternative()) alt--;
			} else {
				if (current[idx] != null) return true; // higher priority request assigned
				if (!request.isAlternative()) alt++;
			}
		}
		for (int idx = maxIdx; idx < current.length; idx++) {
			Request request = iStudent.getRequests().get(idx);
			if (best[idx] != null) {
				if (best[idx].getPriority() > 0) return true; // alternativity can be improved
			} else {
				if (!request.isAlternative() || alt > 0) return true; // priority can be improved
			}
		}

		// 2. maximize number of penalties
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
		
		// 3. maximize selection
    	int bestSelected = 0, currentSelected = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null && best[idx].isCourseRequest()) {
				Set<Section> preferred = iPreferredSections.get((CourseRequest)best[idx].getRequest());
        		if (preferred != null && !preferred.isEmpty()) {
        			for (Section section: best[idx].getSections())
        				if (preferred.contains(section)) {
        					if (idx < maxIdx) bestSelected ++;
        				} else if (idx >= maxIdx) bestSelected --;
        		}
			}
			if (current[idx] != null && idx < maxIdx && current[idx].isCourseRequest()) {
				Set<Section> preferred = iPreferredSections.get((CourseRequest)best[idx].getRequest());
        		if (preferred != null && !preferred.isEmpty()) {
        			for (Section section: current[idx].getSections())
        				if (preferred.contains(section)) currentSelected ++;
        		}
			}
		}
		if (currentSelected > bestSelected) return true;
		if (bestSelected > currentSelected) return false;

		// 4. avoid time overlaps
		if (iModel.getTimeOverlaps() != null) {
			int bestTimeOverlaps = 0, currentTimeOverlaps = 0;
			for (int idx = 0; idx < current.length; idx++) {
				if (best[idx] != null) {
			        for (int x = 0; x < idx; x++) {
			        	if (best[x] != null)
			        		bestTimeOverlaps += iModel.getTimeOverlaps().nrConflicts(best[x], best[idx]);
			        	else if (iStudent.getRequests().get(x) instanceof FreeTimeRequest)
			        		bestTimeOverlaps += iModel.getTimeOverlaps().nrConflicts(((FreeTimeRequest)iStudent.getRequests().get(x)).createEnrollment(), best[idx]);
			        }
				}
				if (current[idx] != null && idx < maxIdx) {
			        for (int x = 0; x < idx; x++) {
			        	if (current[x] != null)
			        		currentTimeOverlaps += iModel.getTimeOverlaps().nrConflicts(current[x], current[idx]);
			        	else if (iStudent.getRequests().get(x) instanceof FreeTimeRequest)
			        		currentTimeOverlaps += iModel.getTimeOverlaps().nrConflicts(((FreeTimeRequest)iStudent.getRequests().get(x)).createEnrollment(), current[idx]);
			        }
				}
			}
			if (currentTimeOverlaps < bestTimeOverlaps) return true;
			if (bestTimeOverlaps < currentTimeOverlaps) return false;
		}
				
		// 5. avoid distance conflicts
		if (iModel.getDistanceConflict() != null) {
			int bestDistanceConf = 0, currentDistanceConf = 0;
			for (int idx = 0; idx < current.length; idx++) {
				if (best[idx] != null) {
			        for (int x = 0; x < idx; x++) {
			        	if (best[x] != null)
			        		bestDistanceConf += iModel.getDistanceConflict().nrConflicts(best[x], best[idx]);
			        }
				}
				if (current[idx] != null && idx < maxIdx) {
			        for (int x = 0; x < idx; x++) {
			        	if (current[x] != null)
			        		currentDistanceConf += iModel.getDistanceConflict().nrConflicts(current[x], current[idx]);
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

	@Override
	public double getTotalWeight(Enrollment[] assignment) {
		if (assignment == null) return 0.0;
		double value = 0.0;
		for (int idx = 0; idx < assignment.length; idx++) {
			if (assignment[idx] != null)
				value += getWeight(assignment[idx], getDistanceConflicts(assignment, idx), getTimeOverlappingConflicts(assignment, idx));
		}
		return value;
	}
	
	public int compare(Enrollment e1, Enrollment e2) {
		// 1. alternativity
		if (e1.getPriority() < e2.getPriority()) return -1;
		if (e1.getPriority() > e2.getPriority()) return 1;
		
		// 2. maximize number of penalties
		int p1 = 0, p2 = 0;
		for (Section section: e1.getSections())
    		if (section.getPenalty() >= 0.0) p1++;
		for (Section section: e2.getSections())
    		if (section.getPenalty() >= 0.0) p2++;
		if (p1 < p2) return -1;
		if (p2 < p1) return 1;

		// 3. maximize selection
		if (e1.isCourseRequest()) {
			Set<Section> preferred = iPreferredSections.get((CourseRequest)e1.getRequest());
			if (preferred != null && !preferred.isEmpty()) {
				int s1 = 0, s2 = 0;
				for (Section section: e1.getSections())
    				if (preferred.contains(section)) s1++;
				for (Section section: e2.getSections())
    				if (preferred.contains(section)) s2++;
				if (s2 > s1) return -1;
				if (s1 > s2) return 1;
			}
		}
		
		// 4. avoid time overlaps
		if (iModel.getTimeOverlaps() != null) {
			int o1 = iModel.getTimeOverlaps().nrFreeTimeConflicts(e1);
			int o2 = iModel.getTimeOverlaps().nrFreeTimeConflicts(e2);
			if (o1 < o2) return -1;
			if (o2 < o1) return 1;
		}
		
		// 5. avoid distance conflicts
		if (iModel.getDistanceConflict() != null) {
			int c1 = iModel.getDistanceConflict().nrConflicts(e1);
			int c2 = iModel.getDistanceConflict().nrConflicts(e2);
			if (c1 < c2) return -1;
			if (c2 < c1) return 1;
		}
		
		// 6. avoid no-time sections
    	int n1 = 0, n2 = 0;
		for (Section section: e1.getSections())
			if (section.getTime() == null) n1++;
		for (Section section: e2.getSections())
			if (section.getTime() == null) n2++;
		if (n1 < n2) return -1;
		if (n2 < n1) return 1;
		
		// 7. balance sections
		double u1 = 0.0, u2 = 0.0;
		int a1 = 0, a2 = 0;
		for (Section section: e1.getSections()) {
            Subpart subpart = section.getSubpart();
            // skip unlimited and single section subparts
            if (subpart.getSections().size() <= 1 || subpart.getLimit() <= 0) continue;
            // average size
            double averageSize = ((double)subpart.getLimit()) / subpart.getSections().size();
            // section is below average
            if (section.getLimit() < averageSize)
            	u1 += (averageSize - section.getLimit()) / averageSize;
            a1 ++;
		}
		for (Section section: e2.getSections()) {
            Subpart subpart = section.getSubpart();
            // skip unlimited and single section subparts
            if (subpart.getSections().size() <= 1 || subpart.getLimit() <= 0) continue;
            // average size
            double averageSize = ((double)subpart.getLimit()) / subpart.getSections().size();
            // section is below average
            if (section.getLimit() < averageSize)
            	u2 += (averageSize - section.getLimit()) / averageSize;
            a2 ++;
		}
		double f1 = (u1 > 0 ? u1 / a1 : 0.0);
		double f2 = (u2 > 0 ? u2 / a2 : 0.0);
		if (f1 < f2) return -1;
		if (f2 < f1) return 1;
		
		// 8. average penalty sections
		double x1 = 0.0, x2 = 0.0;
		for (Section section: e1.getSections())
    		x1 += section.getPenalty();
		for (Section section: e2.getSections())
			x2 += section.getPenalty();
		if (x1 < x2) return -1;
		if (x2 < x1) return 1;

		return 0;
	}
}
