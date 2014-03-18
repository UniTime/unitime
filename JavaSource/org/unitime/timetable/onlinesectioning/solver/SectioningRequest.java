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
package org.unitime.timetable.onlinesectioning.solver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.ifs.assignment.DefaultSingleAssignment;
import org.cpsolver.ifs.util.ToolBox;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XIndividualReservation;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.onlinesectioning.solver.ResectioningWeights.LastSectionProvider;


/**
 * @author Tomas Muller
 */
public class SectioningRequest implements Comparable<SectioningRequest>, LastSectionProvider {
	private XCourseRequest iRequest;
	private XStudent iOldStudent;
	private XEnrollment iLastEnrollment;
	private XOffering iOffering;
	private boolean iHasIndividualReservation;
	private OnlineSectioningLog.Action.Builder iAction;
	private OnlineSectioningLog.CourseRequestOption iOriginal;
	private List<Section> iLastSections = new ArrayList<Section>();

	public SectioningRequest(XOffering offering, XCourseRequest request, XStudent oldStudent, XEnrollment lastEnrollment,
			OnlineSectioningLog.Action.Builder action, OnlineSectioningLog.CourseRequestOption original) {
		iRequest = request;
		iOldStudent = oldStudent;
		iLastEnrollment = lastEnrollment;
		iOffering = offering;
		iHasIndividualReservation = false;
		for (XReservation reservation: iOffering.getReservations())
			if (reservation instanceof XIndividualReservation && ((XIndividualReservation)reservation).getStudentIds().contains(request.getStudentId())) {
				iHasIndividualReservation = true; break;
			}
		iAction = action;
		iOriginal = original;
	}
	
	public XCourseRequest getRequest() { return iRequest; }
	public void setRequest(XCourseRequest request) { iRequest = request; }
	public XStudent getOldStudent() { return iOldStudent; }
	public XEnrollment getLastEnrollment() { return iLastEnrollment; }
	public XOffering getOffering() { return iOffering; }
	public boolean hasIndividualReservation() { return iHasIndividualReservation; }
	public OnlineSectioningLog.Action.Builder getAction() { return iAction; }
	public OnlineSectioningLog.CourseRequestOption getOriginalEnrollment() { return iOriginal; }
	public void setOriginalEnrollment(OnlineSectioningLog.CourseRequestOption original) { iOriginal = original; }
	
	
	public int hashCode() { return new Long(getRequest().getStudentId()).hashCode(); }
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SectioningRequest)) return false;
		return getRequest().getStudentId().equals(((SectioningRequest)o).getRequest().getStudentId());
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
			if (getRequest().getTimeStamp() != null) {
				if (r.getRequest().getTimeStamp() != null) {
					int cmp = getRequest().getTimeStamp().compareTo(r.getRequest().getTimeStamp());
					if (cmp != 0) return cmp;
				} else {
					return 1;
				}
			} else if (r.getRequest().getTimeStamp() != null) {
				return -1;
			}
		}
		
		// Alternative requests last
		if (getRequest().isAlternative() && !r.getRequest().isAlternative()) return 1;
		if (!getRequest().isAlternative() && r.getRequest().isAlternative()) return -1;
		
		// Use priority
		int cmp = new Integer(getRequest().getPriority()).compareTo(r.getRequest().getPriority());
		if (cmp != 0) return cmp;

		if (getLastEnrollment() != null) {
			// Use time stamp
			if (getRequest().getTimeStamp() != null) {
				if (r.getRequest().getTimeStamp() != null) {
					cmp = getRequest().getTimeStamp().compareTo(r.getRequest().getTimeStamp());
					if (cmp != 0) return cmp;
				} else {
					return 1;
				}
			} else if (r.getRequest().getTimeStamp() != null) {
				return -1;
			}
		}
		
		return new Long(getRequest().getStudentId()).compareTo(r.getRequest().getStudentId());
	}

	public XEnrollment resection(OnlineSectioningServer server, ResectioningWeights w, DistanceConflict dc, TimeOverlapsCounter toc) {
		w.setLastSectionProvider(this);
		
		List<Enrollment> enrollments = new ArrayList<Enrollment>();
		double bestValue = 0.0;
		
		Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
		CourseRequest request = convert(assignment, getRequest(), server);
		if (request == null) return null;
		
		if (getLastEnrollment() != null)
			for (Long sectionId: getLastEnrollment().getSectionIds()) {
				for (Course course: request.getCourses()) {
					Section section = course.getOffering().getSection(sectionId);
					if (section != null) iLastSections.add(section);
				}
			}
		
		enrollments: for (Enrollment e: request.getAvaiableEnrollments(assignment)) {
			// only consider enrollments of the offering that is being checked
			if (e.getOffering().getId() != getOffering().getOfferingId()) continue;
			
			for (Request other: request.getStudent().getRequests())
				if (assignment.getValue(other) != null && !other.equals(getRequest()) && assignment.getValue(other).isOverlapping(e))
					continue enrollments;
			
			for (Section s: e.getSections()) {
				if (getLastEnrollment() == null) {
					if (!server.checkDeadline(e.getCourse().getId(), s.getTime() == null ? null : new XTime(s.getTime()), OnlineSectioningServer.Deadline.NEW)) continue enrollments;
				} else {
					if (!getLastEnrollment().getSectionIds().contains(s.getId()) &&
						!server.checkDeadline(e.getCourse().getId(), s.getTime() == null ? null : new XTime(s.getTime()), OnlineSectioningServer.Deadline.CHANGE)) continue enrollments;
				}
			}
			
			double value = w.getWeight(assignment, e, dc.allConflicts(assignment, e), toc.allConflicts(assignment, e));
			if (enrollments.isEmpty() || value > bestValue) {
				enrollments.clear();
				enrollments.add(e); bestValue = value;
			} else if (value == bestValue) {
				enrollments.add(e); 
			}
		}
		
		return (enrollments.isEmpty() ? null : new XEnrollment(ToolBox.random(enrollments)));
	}

	@Override
	public boolean sameLastChoice(Section current) {
		if (getLastEnrollment() != null)
			for (Section section: iLastSections)
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
			for (Section section: iLastSections)
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
			for (Section section: iLastSections)
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
			for (Section section: iLastSections)
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
	
	public static CourseRequest convert(Assignment<Request, Enrollment> assignment, XCourseRequest request, OnlineSectioningServer server) {
		return convert(assignment, server.getStudent(request.getStudentId()), request, server, null, null);
	}
	
	public static Enrollment convert(XCourseRequest request, OnlineSectioningServer server) {
		Assignment<Request, Enrollment> assignment = new DefaultSingleAssignment<Request, Enrollment>();
		CourseRequest cr = convert(assignment, server.getStudent(request.getStudentId()), request, server, null, null);
		return assignment.getValue(cr);
	}
	
	public static CourseRequest convert(Assignment<Request, Enrollment> assignment, XStudent student, XCourseRequest request, OnlineSectioningServer server, XOffering oldOffering, XEnrollment oldEnrollment) {
		Student clonnedStudent = new Student(request.getStudentId());
		CourseRequest ret = null;
		for (XRequest r: student.getRequests()) {
			if (r instanceof XFreeTimeRequest) {
				XFreeTimeRequest ft = (XFreeTimeRequest)r;
				FreeTimeRequest ftr = new FreeTimeRequest(r.getRequestId(), r.getPriority(), r.isAlternative(), clonnedStudent,
						new TimeLocation(ft.getTime().getDays(), ft.getTime().getSlot(), ft.getTime().getLength(), 0, 0.0,
								-1l, "Free Time", server.getAcademicSession().getFreeTimePattern(), 0));
				assignment.assign(0, ftr.createEnrollment());
			} else {
				XCourseRequest cr = (XCourseRequest)r;
				List<Course> courses = new ArrayList<Course>();
				for (XCourseId c: cr.getCourseIds()) {
					XOffering offering = server.getOffering(c.getOfferingId());
					if (oldOffering != null && oldOffering.getOfferingId().equals(c.getOfferingId()))
						offering = oldOffering;
					courses.add(offering.toCourse(c.getCourseId(), student, server.getExpectations(c.getOfferingId()), offering.getDistributions(), server.getEnrollments(c.getOfferingId())));
				}
				CourseRequest clonnedRequest = new CourseRequest(r.getRequestId(), r.getPriority(), r.isAlternative(), clonnedStudent, courses, cr.isWaitlist(), cr.getTimeStamp() == null ? null : cr.getTimeStamp().getTime());
				cr.fillChoicesIn(clonnedRequest);
				XEnrollment enrollment = cr.getEnrollment();
				if (oldEnrollment != null && cr.getCourseIdByOfferingId(oldOffering.getOfferingId()) != null)
					enrollment = oldEnrollment;
				if (enrollment != null) {
					Config config = null;
					Set<Section> assignments = new HashSet<Section>();
					for (Course c: clonnedRequest.getCourses()) {
						if (enrollment.getCourseId().equals(c.getId()))
							for (Config g: c.getOffering().getConfigs()) {
								if (enrollment.getConfigId().equals(g.getId())) {
									config = g;
									for (Subpart s: g.getSubparts())
										for (Section x: s.getSections())
											if (enrollment.getSectionIds().contains(x.getId()))
												assignments.add(x);
								}
							}
					}
					if (config != null)
						assignment.assign(0, new Enrollment(clonnedRequest, 0, config, assignments, assignment));
				}
					
				if (request.equals(r)) ret = clonnedRequest;
			}
		}
		return ret;
	}
	
	public static Enrollment convert(XStudent student, XCourseRequest request, OnlineSectioningServer server, XOffering oldOffering, XEnrollment oldEnrollment) {
		Assignment<Request, Enrollment> assignment = new DefaultSingleAssignment<Request, Enrollment>();
		CourseRequest cr = convert(assignment, student, request, server, oldOffering, oldEnrollment);
		return assignment.getValue(cr);
	}
}
