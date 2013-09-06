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

import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.solver.ResectioningWeights.LastSectionProvider;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.ifs.util.ToolBox;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.model.Course;
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
	private OnlineSectioningLog.Action.Builder iAction;
	private OnlineSectioningLog.CourseRequestOption iOriginal;

	public SectioningRequest(Offering offering, CourseRequest request, Student oldStudent, Enrollment lastEnrollment,
			OnlineSectioningLog.Action.Builder action, OnlineSectioningLog.CourseRequestOption original) {
		iRequest = request;
		iOldStudent = oldStudent;
		iLastEnrollment = lastEnrollment;
		iOffering = (offering != null ? offering : iRequest.getCourses().get(0).getOffering());
		iHasIndividualReservation = false;
		for (Reservation reservation: iOffering.getReservations())
			if (reservation instanceof IndividualReservation && reservation.isApplicable(iRequest.getStudent())) {
				iHasIndividualReservation = true; break;
			}
		iAction = action;
		iOriginal = original;
	}
	
	public CourseRequest getRequest() { return iRequest; }
	public Student getOldStudent() { return iOldStudent; }
	public Enrollment getLastEnrollment() { return iLastEnrollment; }
	public Offering getOffering() { return iOffering; }
	public boolean hasIndividualReservation() { return iHasIndividualReservation; }
	public OnlineSectioningLog.Action.Builder getAction() { return iAction; }
	public OnlineSectioningLog.CourseRequestOption getOriginalEnrollment() { return iOriginal; }
	public void setOriginalEnrollment(OnlineSectioningLog.CourseRequestOption original) { iOriginal = original; }
	
	
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

	public Enrollment resection(OnlineSectioningServer server, ResectioningWeights w, DistanceConflict dc, TimeOverlapsCounter toc) {
		w.setLastSectionProvider(this);
		
		List<Enrollment> enrollments = new ArrayList<Enrollment>();
		double bestValue = 0.0;
		
		enrollments: for (Enrollment e: getRequest().getAvaiableEnrollments()) {
			for (Request other: getRequest().getStudent().getRequests())
				if (other.getAssignment() != null && !other.equals(getRequest()) && other.getAssignment().isOverlapping(e))
					continue enrollments;
			
			for (Section s: e.getSections()) {
				if (getLastEnrollment() == null) {
					if (!server.checkDeadline(s, OnlineSectioningServer.Deadline.NEW)) continue enrollments;
				} else {
					if (!getLastEnrollment().getSections().contains(s) && !server.checkDeadline(s, OnlineSectioningServer.Deadline.CHANGE)) continue enrollments;
				}
			}
			
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

	@Override
	public boolean sameLastChoice(Section current) {
		if (getLastEnrollment() != null)
			for (Section section: getLastEnrollment().getSections())
				if (section.getSubpart().getInstructionalType().equals(current.getSubpart().getInstructionalType()))
					if (ResectioningWeights.sameChoice(current, section.getChoice()))
						return true;
		
		if (getOriginalEnrollment() != null)
			for (OnlineSectioningLog.Section section: getOriginalEnrollment().getSectionList()) {
				
				if (!section.hasSubpart()) continue;
				if (section.getSubpart().hasExternalId()) {
					if (!section.getSubpart().getExternalId().equals(current.getSubpart().getInstructionalType())) continue;
				} else if (section.getSubpart().hasName()) {
					if (!section.getSubpart().getName().equals(current.getSubpart().getName())) continue;
				} else if (section.getSubpart().hasUniqueId()) {
					if (section.getSubpart().getUniqueId() != current.getSubpart().getId()) continue;
				}
				
				if (section.hasTime()) {
					if (current.getTime() == null) continue;
					if (section.getTime().getDays() != current.getTime().getDayCode()) continue;
					if (section.getTime().getStart() != current.getTime().getStartSlot()) continue;
					if (section.getTime().getLength() != current.getTime().getLength()) continue;
					if (section.getTime().hasPattern() && !ToolBox.equals(section.getTime().getPattern(), current.getTime().getDatePatternName())) continue;
				} else {
					if (current.getTime() != null) continue;
				}
				String instructorNames = "";
				String instructorIds = "";
				for (OnlineSectioningLog.Entity instructor: section.getInstructorList()) {
					if (instructor.hasUniqueId()) {
						if (!instructorIds.isEmpty()) instructorIds += ":";
						instructorIds += instructor.getUniqueId();
					}
					if (instructor.hasName()) {
						if (!instructorNames.isEmpty()) instructorNames += ":";
						instructorNames += instructor.getName() + "|" + (instructor.hasExternalId() ? instructor.getExternalId() : "");
					}
				}
				if (!instructorIds.equals(current.getChoice().getInstructorIds()) && !instructorNames.equals(current.getChoice().getInstructorNames()))
					continue;

				return true;
			}
		return false;
	}

	@Override
	public boolean sameLastTime(Section current) {
		if (getLastEnrollment() != null)
			for (Section section: getLastEnrollment().getSections())
				if (section.getSubpart().getInstructionalType().equals(current.getSubpart().getInstructionalType()))
					if (ResectioningWeights.sameTime(current, section.getTime()))
						return true;
		
		if (getOriginalEnrollment() != null)
			for (OnlineSectioningLog.Section section: getOriginalEnrollment().getSectionList()) {
				
				if (!section.hasSubpart()) continue;
				if (section.getSubpart().hasExternalId()) {
					if (!section.getSubpart().getExternalId().equals(current.getSubpart().getInstructionalType())) continue;
				} else if (section.getSubpart().hasName()) {
					if (!section.getSubpart().getName().equals(current.getSubpart().getName())) continue;
				} else if (section.getSubpart().hasUniqueId()) {
					if (section.getSubpart().getUniqueId() != current.getSubpart().getId()) continue;
				}
				
				if (section.hasTime()) {
					if (current.getTime() == null) continue;
					if (section.getTime().getDays() != current.getTime().getDayCode()) continue;
					if (section.getTime().getStart() != current.getTime().getStartSlot()) continue;
					if (section.getTime().getLength() != current.getTime().getLength()) continue;
					if (section.getTime().hasPattern() && !ToolBox.equals(section.getTime().getPattern(), current.getTime().getDatePatternName())) continue;
				} else {
					if (current.getTime() != null) continue;
				}
				
				return true;
			}
		return false;
	}

	@Override
	public boolean sameLastRoom(Section current) {
		if (getLastEnrollment() != null)
			for (Section section: getLastEnrollment().getSections())
				if (section.getSubpart().getInstructionalType().equals(current.getSubpart().getInstructionalType()))
					if (ResectioningWeights.sameRooms(current, section.getRooms()))
						return true;

		if (getOriginalEnrollment() != null)
			sections: for (OnlineSectioningLog.Section section: getOriginalEnrollment().getSectionList()) {
				
				if (!section.hasSubpart()) continue;
				if (section.getSubpart().hasExternalId()) {
					if (!section.getSubpart().getExternalId().equals(current.getSubpart().getInstructionalType())) continue;
				} else if (section.getSubpart().hasName()) {
					if (!section.getSubpart().getName().equals(current.getSubpart().getName())) continue;
				} else if (section.getSubpart().hasUniqueId()) {
					if (section.getSubpart().getUniqueId() != current.getSubpart().getId()) continue;
				}
				
				if (section.getLocationCount() > 0) {
					if (current.getRooms() == null || current.getRooms().isEmpty()) continue;
					rooms: for (OnlineSectioningLog.Entity room: section.getLocationList()) {
						for (RoomLocation loc: current.getRooms()) {
							if (room.hasUniqueId() && room.getUniqueId() == loc.getId()) continue rooms;
							if (room.hasName() && room.getName().equals(loc.getName())) continue rooms;
						}
						continue sections;
					}
				} else {
					if (current.getRooms() != null && !current.getRooms().isEmpty()) continue;
				}
				
				return true;
			}
		return false;
	}

	@Override
	public boolean sameLastName(Section current, Course course) {
		if (getLastEnrollment() != null)
			for (Section section: getLastEnrollment().getSections())
				if (section.getSubpart().getId() == current.getSubpart().getId())
					return ResectioningWeights.sameName(course.getId(), current, section);
		
		if (getOriginalEnrollment() != null)
			for (OnlineSectioningLog.Section section: getOriginalEnrollment().getSectionList()) {
				
				if (!section.hasSubpart()) continue;
				if (section.getSubpart().hasExternalId()) {
					if (!section.getSubpart().getExternalId().equals(current.getSubpart().getInstructionalType())) continue;
				} else if (section.getSubpart().hasName()) {
					if (!section.getSubpart().getName().equals(current.getSubpart().getName())) continue;
				} else if (section.getSubpart().hasUniqueId()) {
					if (section.getSubpart().getUniqueId() != current.getSubpart().getId()) continue;
				}

				if (!section.hasClazz()) continue;
				
				if (section.getClazz().hasName() && !section.getClazz().getName().equals(current.getName(-1l))) continue;

				if (section.getClazz().hasExternalId() && !section.getClazz().getExternalId().equals(current.getName(course.getId()))) continue;
				
				return false;
			}
		return false;
	}
}
