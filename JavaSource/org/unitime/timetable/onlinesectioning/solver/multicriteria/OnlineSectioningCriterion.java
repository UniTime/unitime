/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.weights.StudentWeights;
import org.unitime.timetable.onlinesectioning.solver.OnlineSectioningModel;
import org.unitime.timetable.onlinesectioning.solver.multicriteria.MultiCriteriaBranchAndBoundSelection.SelectionCriterion;


/**
 * @author Tomas Muller
 */
public class OnlineSectioningCriterion implements SelectionCriterion {
	private Hashtable<CourseRequest, Set<Section>> iPreferredSections = null;
	private List<TimeToAvoid> iTimesToAvoid = null;
	private OnlineSectioningModel iModel;
	private Student iStudent;
	
	public OnlineSectioningCriterion(Student student, OnlineSectioningModel model, Assignment<Request, Enrollment> assignment, Hashtable<CourseRequest, Set<Section>> preferredSections) {
		iStudent = student;
		iModel = model;
		iPreferredSections = preferredSections;
    	if (model.getProperties().getPropertyBoolean("OnlineStudentSectioning.TimesToAvoidHeuristics", true)) {
        	iTimesToAvoid = new ArrayList<TimeToAvoid>();
        	for (Request r: iStudent.getRequests()) {
        		if (r instanceof CourseRequest) {
        			List<Enrollment> enrollments = ((CourseRequest)r).getAvaiableEnrollmentsSkipSameTime(assignment);
        			if (enrollments.size() <= 5) {
        				int penalty = (7 - enrollments.size()) * (r.isAlternative() ? 1 : 7 - enrollments.size());
        				for (Enrollment enrollment: enrollments)
            				for (Section section: enrollment.getSections())
            					if (section.getTime() != null)
            						iTimesToAvoid.add(new TimeToAvoid(section.getTime(), penalty, r.getPriority()));
        			}
        		} else if (r instanceof FreeTimeRequest) {
        			iTimesToAvoid.add(new TimeToAvoid(((FreeTimeRequest)r).getTime(), 1, r.getPriority()));
        		}
        	}
    	}
	}
	
	protected OnlineSectioningModel getModel() {
		return iModel;
	}
	
	protected Student getStudent() {
		return iStudent;
	}
	
	protected Set<Section> getPreferredSections(Request request) {
		return iPreferredSections.get(request);
	}
	
	protected List<TimeToAvoid> getTimesToAvoid() {
		return iTimesToAvoid;
	}
	
    /**
     * Distance conflicts of idx-th assignment of the current
     * schedule
     */
    public Set<DistanceConflict.Conflict> getDistanceConflicts(Enrollment[] assignment, int idx) {
        if (getModel().getDistanceConflict() == null || assignment[idx] == null)
            return null;
        Set<DistanceConflict.Conflict> dist = getModel().getDistanceConflict().conflicts(assignment[idx]);
        for (int x = 0; x < idx; x++)
            if (assignment[x] != null)
                dist.addAll(getModel().getDistanceConflict().conflicts(assignment[x], assignment[idx]));
        return dist;
    }
    
    /**
     * Time overlapping conflicts of idx-th assignment of the current
     * schedule
     */
    public Set<TimeOverlapsCounter.Conflict> getTimeOverlappingConflicts(Enrollment[] assignment, int idx) {
        if (getModel().getTimeOverlaps() == null || assignment[idx] == null)
            return null;
        Set<TimeOverlapsCounter.Conflict> overlaps = new HashSet<TimeOverlapsCounter.Conflict>();
        for (int x = 0; x < idx; x++)
            if (assignment[x] != null)
                overlaps.addAll(getModel().getTimeOverlaps().conflicts(assignment[x], assignment[idx]));
            else if (getStudent().getRequests().get(x) instanceof FreeTimeRequest)
                overlaps.addAll(getModel().getTimeOverlaps().conflicts(((FreeTimeRequest)getStudent().getRequests().get(x)).createEnrollment(), assignment[idx]));
        return overlaps;
    }
    
    /**
     * Weight of an assignment. Unlike {@link StudentWeights#getWeight(Enrollment, Set, Set)}, only count this side of distance conflicts and time overlaps.
     **/
    protected double getWeight(Assignment<Request, Enrollment> assignment, Enrollment enrollment, Set<DistanceConflict.Conflict> distanceConflicts, Set<TimeOverlapsCounter.Conflict> timeOverlappingConflicts) {
        double weight = - getModel().getStudentWeights().getWeight(assignment, enrollment);
        if (distanceConflicts != null)
            for (DistanceConflict.Conflict c: distanceConflicts) {
                Enrollment other = (c.getE1().equals(enrollment) ? c.getE2() : c.getE1());
                if (other.getRequest().getPriority() <= enrollment.getRequest().getPriority())
                    weight += getModel().getStudentWeights().getDistanceConflictWeight(assignment, c);
            }
        if (timeOverlappingConflicts != null)
            for (TimeOverlapsCounter.Conflict c: timeOverlappingConflicts) {
                weight += getModel().getStudentWeights().getTimeOverlapConflictWeight(assignment, enrollment, c);
            }
        return enrollment.getRequest().getWeight() * weight;
    }
    
    public Request getRequest(int index) {
    	return (index < 0 || index >= getStudent().getRequests().size() ? null : getStudent().getRequests().get(index));
    }
    
    public boolean isFreeTime(int index) {
    	Request r = getRequest(index);
    	return r != null && r instanceof FreeTimeRequest;
    }
    
	@Override
	public int compare(Assignment<Request, Enrollment> assignment, Enrollment[] current, Enrollment[] best) {
		if (best == null) return -1;
		
		// 0. best priority & alternativity ignoring free time requests
		boolean ft = false;
		for (int idx = 0; idx < current.length; idx++) {
			if (isFreeTime(idx)) { ft = true; continue; }
			if (best[idx] != null && best[idx].getAssignments() != null) {
				if (current[idx] == null || current[idx].getSections() == null) return 1; // higher priority request assigned
				if (best[idx].getPriority() < current[idx].getPriority()) return 1; // less alternative request assigned
			} else {
				if (current[idx] != null && current[idx].getAssignments() != null) return -1; // higher priority request assigned
			}
		}
		
		// 1. minimize number of penalties
		double bestPenalties = 0, currentPenalties = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null && best[idx].getAssignments() != null && best[idx].isCourseRequest()) {
				for (Section section: best[idx].getSections())
					bestPenalties += getModel().getOverExpected(assignment, section, best[idx].getRequest());
				for (Section section: current[idx].getSections())
					currentPenalties += getModel().getOverExpected(assignment, section, current[idx].getRequest());
			}
		}
		if (currentPenalties < bestPenalties) return -1;
		if (bestPenalties < currentPenalties) return 1;

		// 2. best priority & alternativity including free time requests
		if (ft) {
			for (int idx = 0; idx < current.length; idx++) {
				if (best[idx] != null && best[idx].getAssignments() != null) {
					if (current[idx] == null || current[idx].getSections() == null) return 1; // higher priority request assigned
					if (best[idx].getPriority() < current[idx].getPriority()) return 1; // less alternative request assigned
				} else {
					if (current[idx] != null && current[idx].getAssignments() != null) return -1; // higher priority request assigned
				}
			}			
		}
		
		// 3. maximize selection
    	int bestSelected = 0, currentSelected = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null && best[idx].getAssignments() != null && best[idx].isCourseRequest()) {
				Set<Section> preferred = getPreferredSections(best[idx].getRequest());
        		if (preferred != null && !preferred.isEmpty()) {
        			for (Section section: best[idx].getSections())
        				if (preferred.contains(section)) bestSelected ++;
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
				for (Section section: current[idx].getSections())
					currentPenalty += section.getPenalty();
			}
		}
		if (currentPenalty < bestPenalty) return -1;
		if (bestPenalty < currentPenalty) return 1;
		
		return 0;
	}

	@Override
	public boolean canImprove(Assignment<Request, Enrollment> assignment, int maxIdx, Enrollment[] current, Enrollment[] best) {
		// 0. best priority & alternativity ignoring free time requests
		int alt = 0;
		boolean ft = false;
		for (int idx = 0; idx < current.length; idx++) {
			if (isFreeTime(idx)) { ft = true; continue; }
			Request request = getRequest(idx);
			if (idx < maxIdx) {
				if (best[idx] != null) {
					if (current[idx] == null) return false; // higher priority request assigned
					if (best[idx].getPriority() < current[idx].getPriority()) return false; // less alternative request assigned
					if (request.isAlternative()) alt--;
				} else {
					if (current[idx] != null) return true; // higher priority request assigned
					if (!request.isAlternative()) alt++;
				}
			} else {
				if (best[idx] != null) {
					if (best[idx].getPriority() > 0) return true; // alternativity can be improved
				} else {
					if (!request.isAlternative() || alt > 0) return true; // priority can be improved
				}
			}
		}
		
		// 1. maximize number of penalties
		double bestPenalties = 0, currentPenalties = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null) {
				for (Section section: best[idx].getSections())
					bestPenalties += getModel().getOverExpected(assignment, section, best[idx].getRequest());
			}
			if (current[idx] != null && idx < maxIdx) {
				for (Section section: current[idx].getSections())
					currentPenalties += getModel().getOverExpected(assignment, section, current[idx].getRequest());
			}
		}
		if (currentPenalties < bestPenalties) return true;
		if (bestPenalties < currentPenalties) return false;
		
		// 2. best priority & alternativity including free times
		if (ft) {
			alt = 0;
			for (int idx = 0; idx < current.length; idx++) {
				Request request = getStudent().getRequests().get(idx);
				if (idx < maxIdx) {
					if (best[idx] != null) {
						if (current[idx] == null) return false; // higher priority request assigned
						if (best[idx].getPriority() < current[idx].getPriority()) return false; // less alternative request assigned
						if (request.isAlternative()) alt--;
					} else {
						if (current[idx] != null) return true; // higher priority request assigned
						if (request instanceof CourseRequest && !request.isAlternative()) alt++;
					}
				} else {
					if (best[idx] != null) {
						if (best[idx].getPriority() > 0) return true; // alternativity can be improved
					} else {
						if (!request.isAlternative() || alt > 0) return true; // priority can be improved
					}
				}
			}			
		}

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

	@Override
	public double getTotalWeight(Assignment<Request, Enrollment> assignment, Enrollment[] enrollemnts) {
		if (enrollemnts == null) return 0.0;
		double value = 0.0;
		for (int idx = 0; idx < enrollemnts.length; idx++) {
			if (enrollemnts[idx] != null)
				value += getWeight(assignment, enrollemnts[idx], getDistanceConflicts(enrollemnts, idx), getTimeOverlappingConflicts(enrollemnts, idx));
		}
		return value;
	}
	
	public int compare(Assignment<Request, Enrollment> assignment, Enrollment e1, Enrollment e2) {
		// 1. alternativity
		if (e1.getPriority() < e2.getPriority()) return -1;
		if (e1.getPriority() > e2.getPriority()) return 1;
		
		// 2. maximize number of penalties
		double p1 = 0, p2 = 0;
		for (Section section: e1.getSections())
			p1 += getModel().getOverExpected(assignment, section, e1.getRequest());
		for (Section section: e2.getSections())
			p2 += getModel().getOverExpected(assignment, section, e2.getRequest());
		if (p1 < p2) return -1;
		if (p2 < p1) return 1;

		// 3. maximize selection
		if (e1.isCourseRequest()) {
			Set<Section> preferred = getPreferredSections(e1.getRequest());
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
		if (getTimesToAvoid() == null) {
			if (getModel().getTimeOverlaps() != null) {
				int o1 = getModel().getTimeOverlaps().nrFreeTimeConflicts(e1);
				int o2 = getModel().getTimeOverlaps().nrFreeTimeConflicts(e2);
				if (o1 < o2) return -1;
				if (o2 < o1) return 1;
			}
		} else {
			if (e1.getRequest().equals(e2.getRequest()) && e1.isCourseRequest()) {
				double o1 = 0.0, o2 = 0.0;
				for (Section s: e1.getSections()) {
					if (s.getTime() != null)
						for (TimeToAvoid avoid: getTimesToAvoid()) {
							if (avoid.priority() > e1.getPriority())
								o1 += avoid.overlap(s.getTime());
						}
				}
				for (Section s: e2.getSections()) {
					if (s.getTime() != null)
						for (TimeToAvoid avoid: getTimesToAvoid()) {
							if (avoid.priority() > e2.getPriority())
								o2 += avoid.overlap(s.getTime());
						}
				}
				if (o1 < o2) return -1;
				if (o2 < o1) return 1;
			}
		}
		
		// 5. avoid distance conflicts
		if (getModel().getDistanceConflict() != null) {
			int c1 = getModel().getDistanceConflict().nrConflicts(e1);
			int c2 = getModel().getDistanceConflict().nrConflicts(e2);
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
	
	protected static class TimeToAvoid {
		private TimeLocation iTime;
		private double iPenalty;
		private int iPriority;
		
		public TimeToAvoid(TimeLocation time, int penalty, int priority) {
			iTime = time; iPenalty = penalty; iPriority = priority;
		}
		
		public int priority() { return iPriority; }
		
		public double overlap(TimeLocation time) {
			if (time.hasIntersection(iTime)) {
				return iPenalty * (time.nrSharedDays(iTime) * time.nrSharedDays(iTime)) / (iTime.getNrMeetings() * iTime.getLength()); 
			} else {
				return 0.0;
			}
		}
		
		public String toString() {
			return iTime.getLongName() + " (" + iPriority + "/" + iPenalty + ")";
		}
	}
}
