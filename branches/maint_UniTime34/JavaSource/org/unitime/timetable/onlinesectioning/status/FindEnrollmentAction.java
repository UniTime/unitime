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
package org.unitime.timetable.onlinesectioning.status;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.studentsct.model.AcademicAreaCode;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.reservation.CourseReservation;
import net.sf.cpsolver.studentsct.reservation.CurriculumReservation;
import net.sf.cpsolver.studentsct.reservation.GroupReservation;
import net.sf.cpsolver.studentsct.reservation.IndividualReservation;
import net.sf.cpsolver.studentsct.reservation.Reservation;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;

public class FindEnrollmentAction implements OnlineSectioningAction<List<ClassAssignmentInterface.Enrollment>> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Query iQuery;
	private Long iCourseId, iClassId;
	private boolean iConsentToDoCourse;
	
	public FindEnrollmentAction(String query, Long courseId, Long classId, boolean isConsentToDoCourse) {
		iQuery = new Query(query);
		iCourseId = courseId;
		iClassId = classId;
		iConsentToDoCourse = isConsentToDoCourse;
	}
	
	public Query query() { return iQuery; }

	public Long courseId() { return iCourseId; }
	
	public Long classId() { return iClassId; }
	
	public boolean isConsentToDoCourse() { return iConsentToDoCourse; }

	@Override
	public List<ClassAssignmentInterface.Enrollment> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<ClassAssignmentInterface.Enrollment> ret = new ArrayList<ClassAssignmentInterface.Enrollment>();
		CourseInfo info = server.getCourseInfo(courseId());
		Course course = server.getCourse(courseId());
		if (course == null) return ret;
		Section filterSection = (classId() == null ? null : server.getSection(classId()));
		
		for (CourseRequest request: course.getRequests()) {
			if (request.getAssignment() != null && request.getAssignment().getCourse().getId() != course.getId()) continue;
			if (filterSection != null && request.getAssignment() != null && !request.getAssignment().getSections().contains(filterSection)) continue;
			if (request.getAssignment() == null && !request.getStudent().canAssign(request)) continue;
			if (!query().match(new StatusPageSuggestionsAction.CourseRequestMatcher(helper, server, info, request, isConsentToDoCourse()))) continue;
			
			ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
			st.setId(request.getStudent().getId());
			st.setExternalId(request.getStudent().getExternalId());
			st.setName(request.getStudent().getName());
			for (AcademicAreaCode ac: request.getStudent().getAcademicAreaClasiffications()) {
				st.addArea(ac.getArea());
				st.addClassification(ac.getCode());
			}
			for (AcademicAreaCode ac: request.getStudent().getMajors()) {
				st.addMajor(ac.getCode());
			}
			for (AcademicAreaCode ac: request.getStudent().getMinors()) {
				if ("A".equals(ac.getArea()))
					st.addAccommodation(ac.getCode());
				else
					st.addGroup(ac.getCode());
			}
			
			ClassAssignmentInterface.Enrollment e = new ClassAssignmentInterface.Enrollment();
			e.setStudent(st);
			e.setPriority(1 + request.getPriority());
			CourseAssignment c = new CourseAssignment();
			c.setCourseId(course.getId());
			c.setSubject(course.getSubjectArea());
			c.setCourseNbr(course.getCourseNumber());
			e.setCourse(c);
			e.setWaitList(request.isWaitlist());
			if (!request.getCourses().get(0).equals(course))
				e.setAlternative(request.getCourses().get(0).getName());
			if (request.isAlternative()) {
				for (Request r: request.getStudent().getRequests()) {
					if (r instanceof CourseRequest && !r.isAlternative() && r.getAssignment() == null) {
						e.setAlternative(((CourseRequest)r).getCourses().get(0).getName());
					}
				}
			}
			if (request.getTimeStamp() != null)
				e.setRequestedDate(new Date(request.getTimeStamp()));
			if (request.getAssignment() != null) {
				if (request.getAssignment().getReservation() != null) {
					Reservation r = request.getAssignment().getReservation();
					if (r instanceof GroupReservation) {
						e.setReservation(MSG.reservationGroup());
					} else if (r instanceof IndividualReservation) {
						e.setReservation(MSG.reservationIndividual());
					} else if (r instanceof CourseReservation) {
						e.setReservation(MSG.reservationCourse());
					} else if (r instanceof CurriculumReservation) {
						e.setReservation(MSG.reservationCurriculum());
					}
				}
				if (request.getAssignment().getTimeStamp() != null)
					e.setEnrolledDate(new Date(request.getAssignment().getTimeStamp()));
				if (request.getAssignment().getApproval() != null) {
					String[] approval = request.getAssignment().getApproval().split(":");
					e.setApprovedDate(new Date(Long.parseLong(approval[0])));
					e.setApprovedBy(approval[2]);
				}
				
				for (Section section: request.getAssignment().getSections()) {
					ClassAssignmentInterface.ClassAssignment a = e.getCourse().addClassAssignment();
					a.setAlternative(request.isAlternative());
					a.setClassId(section.getId());
					a.setSubpart(section.getSubpart().getName());
					a.setSection(section.getName(course.getId()));
					a.setClassNumber(section.getName(-1l));
					a.setLimit(new int[] {section.getEnrollments().size(), section.getLimit()});
					if (section.getTime() != null) {
						for (DayCode d : DayCode.toDayCodes(section.getTime().getDayCode()))
							a.addDay(d.getIndex());
						a.setStart(section.getTime().getStartSlot());
						a.setLength(section.getTime().getLength());
						a.setBreakTime(section.getTime().getBreakTime());
						a.setDatePattern(section.getTime().getDatePatternName());
					}
					if (section.getRooms() != null) {
						for (Iterator<RoomLocation> i = section.getRooms().iterator(); i.hasNext(); ) {
							RoomLocation rm = i.next();
							a.addRoom(rm.getName());
						}
					}
					if (section.getChoice().getInstructorNames() != null && !section.getChoice().getInstructorNames().isEmpty()) {
						String[] instructors = section.getChoice().getInstructorNames().split(":");
						for (String instructor: instructors) {
							String[] nameEmail = instructor.split("\\|");
							a.addInstructor(nameEmail[0]);
							a.addInstructoEmailr(nameEmail.length < 2 ? "" : nameEmail[1]);
						}
					}
					if (section.getParent() != null)
						a.setParentSection(section.getParent().getName(course.getId()));
					a.setSubpartId(section.getSubpart().getId());
					a.addNote(course.getNote());
					a.addNote(section.getNote());
					a.setCredit(section.getSubpart().getCredit());
					int dist = 0;
					String from = null;
					TreeSet<String> overlap = new TreeSet<String>();
					for (Request q: request.getStudent().getRequests()) {
						Enrollment x = q.getAssignment();
						if (x == null || !x.isCourseRequest() || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
						for (Iterator<Section> j=x.getSections().iterator(); j.hasNext();) {
							Section s = j.next();
							if (s == section || s.getTime() == null) continue;
							int d = server.distance(s, section);
							if (d > dist) {
								dist = d;
								from = "";
								for (Iterator<RoomLocation> k = s.getRooms().iterator(); k.hasNext();)
									from += k.next().getName() + (k.hasNext() ? ", " : "");
							}
							if (d > s.getTime().getBreakTime()) {
								a.setDistanceConflict(true);
							}
							if (section.getTime() != null && section.getTime().hasIntersection(s.getTime()) && !section.isToIgnoreStudentConflictsWith(s.getId())) {
								overlap.add(MSG.clazz(x.getCourse().getSubjectArea(), x.getCourse().getCourseNumber(), s.getSubpart().getName(), s.getName(x.getCourse().getId())));
							}
						}
					}
					if (!overlap.isEmpty()) {
						String note = null;
						for (Iterator<String> j = overlap.iterator(); j.hasNext(); ) {
							String n = j.next();
							if (note == null)
								note = MSG.noteAllowedOverlapFirst(n);
							else if (j.hasNext())
								note += MSG.noteAllowedOverlapMiddle(n);
							else
								note += MSG.noteAllowedOverlapLast(n);
						}
						a.addNote(note);
					}
					a.setBackToBackDistance(dist);
					a.setBackToBackRooms(from);
					a.setSaved(true);
					if (a.getParentSection() == null)
						a.setParentSection(info.getConsent());
					a.setExpected(Math.round(section.getSpaceExpected()));
				}
			}
			ret.add(e);
		}
		return ret;
	}

	@Override
	public String name() {
		return "find-enrollments";
	}

}
