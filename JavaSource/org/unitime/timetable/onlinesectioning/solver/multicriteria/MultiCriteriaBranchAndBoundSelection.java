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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.onlinesectioning.solver.OnlineSectioningSelection;

import net.sf.cpsolver.ifs.model.GlobalConstraint;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.JProf;
import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.constraint.LinkedSections;
import net.sf.cpsolver.studentsct.heuristics.selection.BranchBoundSelection.BranchBoundNeighbour;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

public class MultiCriteriaBranchAndBoundSelection implements OnlineSectioningSelection {
    protected int iTimeout = 1000;
    protected StudentSectioningModel iModel = null;
    protected SelectionCriterion iComparator = null;
    private boolean iPriorityWeighting = true;
    
    /** Student */
    protected Student iStudent;
    /** Start time */
    protected long iT0;
    /** End time */
    protected long iT1;
    /** Was timeout reached */
    protected boolean iTimeoutReached;
    /** Current assignment */
    protected Enrollment[] iAssignment;
    /** Best assignment */
    protected Enrollment[] iBestAssignment;
    /** Value cache */
    protected HashMap<CourseRequest, List<Enrollment>> iValues;
    
	private Set<FreeTimeRequest> iRequiredFreeTimes;
	private Hashtable<CourseRequest, Set<Section>> iPreferredSections;
	private Hashtable<CourseRequest, Config> iRequiredConfig = new Hashtable<CourseRequest, Config>();
	private Hashtable<CourseRequest, Hashtable<Subpart, Section>> iRequiredSection = new Hashtable<CourseRequest, Hashtable<Subpart,Section>>();
    
    public MultiCriteriaBranchAndBoundSelection(DataProperties config) {
        iTimeout = config.getPropertyInt("Neighbour.BranchAndBoundTimeout", iTimeout);
        iPriorityWeighting = config.getPropertyBoolean("StudentWeights.PriorityWeighting", iPriorityWeighting);
    }
    
	@Override
	public void setModel(StudentSectioningModel model) {
		iModel = model;
	}

	@Override
	public void setPreferredSections(Hashtable<CourseRequest, Set<Section>> preferredSections) {
		iPreferredSections = preferredSections;
	}
	
	public void setTimeout(int timeout) {
		iTimeout = timeout;
	}

	@Override
	public void setRequiredSections(Hashtable<CourseRequest, Set<Section>> requiredSections) {
    	if (requiredSections != null) {
    		for (Map.Entry<CourseRequest, Set<Section>> entry: requiredSections.entrySet()) {
    			Hashtable<Subpart, Section> subSection = new Hashtable<Subpart, Section>();
    			iRequiredSection.put(entry.getKey(), subSection);
    			for (Section section: entry.getValue()) {
    				if (subSection.isEmpty())
    					iRequiredConfig.put(entry.getKey(), section.getSubpart().getConfig());
    				subSection.put(section.getSubpart(), section);
    			}
    		}
    	}
	}

	@Override
	public void setRequiredFreeTimes(Set<FreeTimeRequest> requiredFreeTimes) {
    	iRequiredFreeTimes = requiredFreeTimes;
	}

	public BranchBoundNeighbour select(Student student, SelectionCriterion comparator) {
        iStudent = student;
		iComparator = comparator;
        return select();
	}
	
	@Override
	public BranchBoundNeighbour select(Student student) {
		SelectionCriterion comparator = null;
		if (iPriorityWeighting)
			comparator = new OnlineSectioningCriterion(student, iModel, iPreferredSections);
		else
			comparator = new EqualWeightCriterion(student, iModel, iPreferredSections);
		return select(student, comparator);
	}

    /**
     * Execute branch & bound, return the best found schedule for the
     * selected student.
     */
    public BranchBoundNeighbour select() {
        iT0 = JProf.currentTimeMillis();
        iTimeoutReached = false;
        iAssignment = new Enrollment[iStudent.getRequests().size()];
        iBestAssignment = null;
        
        int i = 0;
        for (Request r: iStudent.getRequests())
            iAssignment[i++] = r.getAssignment();
        saveBest();
        for (int j = 0; j < iAssignment.length; j++)
            iAssignment[j] = null;
        
        iValues = new HashMap<CourseRequest, List<Enrollment>>();
        backTrack(0);
        iT1 = JProf.currentTimeMillis();
        if (iBestAssignment == null)
            return null;
        
        return new BranchBoundNeighbour(iStudent, iComparator.getTotalWeight(iBestAssignment), iBestAssignment);
    }

    /** Was timeout reached */
    public boolean isTimeoutReached() {
        return iTimeoutReached;
    }

    /** Time (in milliseconds) the branch & bound did run */
    public long getTime() {
        return iT1 - iT0;
    }

    /** Save the current schedule as the best */
    public void saveBest() {
        if (iBestAssignment == null)
            iBestAssignment = new Enrollment[iAssignment.length];
        for (int i = 0; i < iAssignment.length; i++)
            iBestAssignment[i] = iAssignment[i];
    }
    
    /** True if the enrollment is conflicting */
    public boolean inConflict(final int idx, final Enrollment enrollment) {
        for (GlobalConstraint<Request, Enrollment> constraint : enrollment.variable().getModel().globalConstraints())
            if (constraint.inConflict(enrollment))
                return true;
        for (LinkedSections linkedSections: iStudent.getLinkedSections()) {
            if (linkedSections.inConflict(enrollment, new LinkedSections.Assignment() {
                @Override
                public Enrollment getEnrollment(Request request, int index) {
                    return (index == idx ? enrollment : iAssignment[index]);
                }
            }) != null) return true;
        }
        for (int i = 0; i < iAssignment.length; i++)
            if (iAssignment[i] != null && i != idx && iAssignment[i].isOverlapping(enrollment))
                return true;
        return !isAllowed(idx, enrollment);
    }

    /** True if the given request can be assigned */
    public boolean canAssign(Request request, int idx) {
        if (!request.isAlternative() || iAssignment[idx] != null)
            return true;
        int alt = 0;
        int i = 0;
        for (Iterator<Request> e = iStudent.getRequests().iterator(); e.hasNext(); i++) {
            Request r = e.next();
            if (r.equals(request))
                continue;
            if (r.isAlternative()) {
                if (iAssignment[i] != null || (r instanceof CourseRequest && ((CourseRequest) r).isWaitlist()))
                    alt--;
            } else {
                if (r instanceof CourseRequest && !((CourseRequest) r).isWaitlist() && iAssignment[i] == null)
                    alt++;
            }
        }
        return (alt > 0);
    }
    
    public boolean isAllowed(int idx, Enrollment enrollment) {
    	if (enrollment.isCourseRequest()) {
    		CourseRequest request = (CourseRequest)enrollment.getRequest();
    		Config reqConfig = iRequiredConfig.get(request);
    		if (reqConfig != null) {
    			if (!reqConfig.equals(enrollment.getConfig())) return false;
        		Hashtable<Subpart, Section> reqSections = iRequiredSection.get(request);
    			for (Section section: enrollment.getSections()) {
    				Section reqSection = reqSections.get(section.getSubpart());
    				if (reqSection == null) continue;
    				if (!section.equals(reqSection)) return false;
    			}
    		}
    	} else if (iRequiredFreeTimes.contains(enrollment.getRequest())) {
    		if (enrollment.getAssignments() == null || enrollment.getAssignments().isEmpty()) return false;
    	}
    	return true;
    }
    
    /** Returns true if the given request can be left unassigned */
    protected boolean canLeaveUnassigned(Request request) {
    	if (request instanceof CourseRequest) {
    		if (iRequiredConfig.get(request) != null) return false;
    	} else if (iRequiredFreeTimes.contains(request)) return false;
    	return true;
    }

    /** Returns list of available enrollments for a course request */
    protected List<Enrollment> values(final CourseRequest request) {
        List<Enrollment> values = request.getAvaiableEnrollments();
        Collections.sort(values, iComparator);
        return values;
    }

    /** branch & bound search */
    public void backTrack(int idx) {
        if (iTimeout > 0 && (JProf.currentTimeMillis() - iT0) > iTimeout) {
            iTimeoutReached = true;
            return;
        }
        if (idx == iAssignment.length) {
            if (iBestAssignment == null || iComparator.compare(iAssignment, iBestAssignment) < 0)
                saveBest();
            return;
        } else if (iBestAssignment != null && !iComparator.canImprove(idx, iAssignment, iBestAssignment)) {
            return;
        }

        Request request = iStudent.getRequests().get(idx);
        if (!canAssign(request, idx)) {
            backTrack(idx + 1);
            return;
        }
        
        List<Enrollment> values = null;
        if (request instanceof CourseRequest) {
            CourseRequest courseRequest = (CourseRequest) request;
            if (!courseRequest.getSelectedChoices().isEmpty()) {
                values = courseRequest.getSelectedEnrollments(true);
                if (values != null && !values.isEmpty()) {
                    boolean hasNoConflictValue = false;
                    for (Enrollment enrollment : values) {
                        if (inConflict(idx, enrollment))
                            continue;
                        hasNoConflictValue = true;
                        iAssignment[idx] = enrollment;
                        backTrack(idx + 1);
                        iAssignment[idx] = null;
                    }
                    if (hasNoConflictValue)
                        return;
                }
            }
            values = iValues.get(courseRequest);
            if (values == null) {
                values = values(courseRequest);
                iValues.put(courseRequest, values);
            }
        } else {
            values = request.computeEnrollments();
        }
        
        for (Enrollment enrollment : values) {
            if (inConflict(idx, enrollment)) continue;
            iAssignment[idx] = enrollment;
            backTrack(idx + 1);
            iAssignment[idx] = null;
        }
        
        if (canLeaveUnassigned(request))
            backTrack(idx + 1);
    }
    
    public interface SelectionCriterion extends Comparator<Enrollment> {
    	public int compare(Enrollment[] current, Enrollment[] best);
    	public boolean canImprove(int idx, Enrollment[] current, Enrollment[] best);
    	public double getTotalWeight(Enrollment[] assignment);
    }
}