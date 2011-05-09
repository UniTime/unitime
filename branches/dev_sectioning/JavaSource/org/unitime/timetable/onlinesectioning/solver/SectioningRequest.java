/*
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
package org.unitime.timetable.onlinesectioning.solver;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.onlinesectioning.solver.ResectioningWeights.LastSectionProvider;

import net.sf.cpsolver.ifs.util.ToolBox;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.reservation.IndividualReservation;
import net.sf.cpsolver.studentsct.reservation.Reservation;

public class SectioningRequest implements Comparable<SectioningRequest>, LastSectionProvider {
	private CourseRequest iRequest;
	private Student iOldStudent;
	private Enrollment iLastEnrollment;
	private Offering iOffering;
	private boolean iHasIndividualReservation;

	public SectioningRequest(Offering offering, CourseRequest request, Student oldStudent, Enrollment lastEnrollment) {
		iRequest = request;
		iOldStudent = oldStudent;
		iLastEnrollment = lastEnrollment;
		iOffering = (offering != null ? offering : iRequest.getCourses().get(0).getOffering());
		iHasIndividualReservation = false;
		for (Reservation reservation: iOffering.getReservations())
			if (reservation instanceof IndividualReservation && reservation.isApplicable(iRequest.getStudent())) {
				iHasIndividualReservation = true; break;
			}
	}
	
	public CourseRequest getRequest() { return iRequest; }
	public Student getOldStudent() { return iOldStudent; }
	public Enrollment getLastEnrollment() { return iLastEnrollment; }
	public Offering getOffering() { return iOffering; }
	public boolean hasIndividualReservation() { return iHasIndividualReservation; }
	
	public int hashCode() { return new Long(getRequest().getStudent().getId()).hashCode(); }
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SectioningRequest)) return false;
		return getRequest().getStudent().getId() == ((SectioningRequest)o).getRequest().getStudent().getId();
	}
	
	public int compareTo(SectioningRequest r) {
		// Requests with last enrollment (recently unassigned requests) have priority
		if (getLastEnrollment() == null && r.getLastEnrollment() != null) return 1;
		if (getLastEnrollment() != null && r.getLastEnrollment() == null) return -1;
		
		// Check individual reservations
		if (hasIndividualReservation() && !r.hasIndividualReservation()) return -1;
		if (!hasIndividualReservation() && r.hasIndividualReservation()) return 1;

		if (getLastEnrollment() == null) {
			// Use time stamp
			int cmp = (getRequest().getTimeStamp() != null ? getRequest().getTimeStamp() : new Long(Long.MAX_VALUE)).compareTo(
					(r.getRequest().getTimeStamp() != null ? r.getRequest().getTimeStamp() : Long.MAX_VALUE));
			if (cmp != 0) return cmp;
		}
		
		// Alternative requests last
		if (getRequest().isAlternative() && !r.getRequest().isAlternative()) return 1;
		if (!getRequest().isAlternative() && r.getRequest().isAlternative()) return -1;
		
		// Use priority
		int cmp = new Integer(getRequest().getPriority()).compareTo(r.getRequest().getPriority());
		if (cmp != 0) return cmp;

		if (getLastEnrollment() != null) {
			// Use time stamp
			cmp = (getRequest().getTimeStamp() != null ? getRequest().getTimeStamp() : new Long(Long.MAX_VALUE)).compareTo(
					(r.getRequest().getTimeStamp() != null ? r.getRequest().getTimeStamp() : Long.MAX_VALUE));
			if (cmp != 0) return cmp;
		}
		
		return new Long(getRequest().getStudent().getId()).compareTo(r.getRequest().getStudent().getId());
	}

	@Override
	public Section getLastSection(Section current) {
		if (getLastEnrollment() == null) return null;
		for (Section section: getLastEnrollment().getSections())
			if (section.getSubpart().getId() == current.getSubpart().getId()) return section;
		return null;
	}

	public Enrollment resection(ResectioningWeights w, DistanceConflict dc, TimeOverlapsCounter toc) {
		w.setLastSectionProvider(this);
		
		List<Enrollment> enrollments = new ArrayList<Enrollment>();
		double bestValue = 0.0;
		
		enrollments: for (Enrollment e: getRequest().getAvaiableEnrollments()) {
			for (Request other: getRequest().getStudent().getRequests())
				if (other.getAssignment() != null && !other.equals(getRequest()) && other.getAssignment().isOverlapping(e))
					continue enrollments;
			double value = w.getWeight(e, dc.allConflicts(e), toc.allConflicts(e));
			if (enrollments.isEmpty() || value > bestValue) {
				enrollments.clear();
				enrollments.add(e); bestValue = value;
			} else if (value == bestValue) {
				enrollments.add(e); 
			}
		}
		
		return (enrollments.isEmpty() ? null : ToolBox.random(enrollments));
	}
}
