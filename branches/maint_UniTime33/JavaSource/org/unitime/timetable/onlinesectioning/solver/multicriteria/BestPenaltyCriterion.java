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

import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;

import org.unitime.timetable.onlinesectioning.solver.multicriteria.MultiCriteriaBranchAndBoundSelection.SelectionCriterion;

public class BestPenaltyCriterion implements SelectionCriterion {
	private Student iStudent;
	
	public BestPenaltyCriterion(Student student) {
		iStudent = student;
	}
	
	private Request getRequest(int index) {
    	return (index < 0 || index >= iStudent.getRequests().size() ? null : iStudent.getRequests().get(index));
    }
    
	private boolean isFreeTime(int index) {
    	Request r = getRequest(index);
    	return r != null && r instanceof FreeTimeRequest;
    }

	@Override
	public int compare(Enrollment[] current, Enrollment[] best) {
		if (best == null) return -1;
		
		// 0. best priority & alternativity ignoring free time requests
		for (int idx = 0; idx < current.length; idx++) {
			if (isFreeTime(idx)) continue;
			if (best[idx] != null && best[idx].getAssignments() != null) {
				if (current[idx] == null || current[idx].getSections() == null) return 1; // higher priority request assigned
				if (best[idx].getPriority() < current[idx].getPriority()) return 1; // less alternative request assigned
			} else {
				if (current[idx] != null && current[idx].getAssignments() != null) return -1; // higher priority request assigned
			}
		}
		
		// 1. minimize number of penalties
		int bestPenalties = 0, currentPenalties = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (best[idx] != null && best[idx].getAssignments() != null && best[idx].isCourseRequest()) {
				for (Section section: best[idx].getSections())
		    		if (section.getPenalty() >= 0.0) bestPenalties++;
				for (Section section: current[idx].getSections())
		    		if (section.getPenalty() >= 0.0) currentPenalties++;
			}
		}
		if (currentPenalties < bestPenalties) return -1;
		if (bestPenalties < currentPenalties) return 1;
		
		return 0;
	}

	@Override
	public boolean canImprove(int maxIdx, Enrollment[] current, Enrollment[] best) {
		// 0. best priority & alternativity ignoring free time requests
		int alt = 0;
		for (int idx = 0; idx < current.length; idx++) {
			if (isFreeTime(idx)) continue;
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
		
		return false;
	}

	@Override
	public double getTotalWeight(Enrollment[] assignment) {
		return 0.0;
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

		return 0;
	}
}