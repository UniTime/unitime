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
package org.unitime.timetable.onlinesectioning.status;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;


import org.cpsolver.ifs.util.DistanceMetric;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XAcademicAreaCode;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.solver.expectations.OverExpectedCriterion;

/**
 * @author Tomas Muller
 */
public class FindEnrollmentAction implements OnlineSectioningAction<List<ClassAssignmentInterface.Enrollment>> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Query iQuery;
	private Long iCourseId, iClassId;
	private boolean iConsentToDoCourse;
	
	public FindEnrollmentAction withParams(String query, Long courseId, Long classId, boolean isConsentToDoCourse) {
		iQuery = new Query(query);
		iCourseId = courseId;
		iClassId = classId;
		iConsentToDoCourse = isConsentToDoCourse;
		return this;
	}
	
	public Query query() { return iQuery; }

	public Long courseId() { return iCourseId; }
	
	public Long classId() { return iClassId; }
	
	public boolean isConsentToDoCourse() { return iConsentToDoCourse; }

	@Override
	public List<ClassAssignmentInterface.Enrollment> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<ClassAssignmentInterface.Enrollment> ret = new ArrayList<ClassAssignmentInterface.Enrollment>();
		XCourse course = server.getCourse(courseId());
		if (course == null) return ret;
		XOffering offering = server.getOffering(course.getOfferingId());
		if (offering == null) return ret;
		XEnrollments enrollments = server.getEnrollments(course.getOfferingId());
		DistanceMetric m = server.getDistanceMetric();
		XExpectations expectations = server.getExpectations(offering.getOfferingId());
		OverExpectedCriterion overExp = server.getOverExpectedCriterion();
		
		for (XCourseRequest request: enrollments.getRequests()) {
			if (request.getEnrollment() != null && !request.getEnrollment().getCourseId().equals(courseId())) continue;
			if (classId() != null && request.getEnrollment() != null && !request.getEnrollment().getSectionIds().contains(classId())) continue;
			if (request.getEnrollment() == null && !request.getCourseIds().contains(course)) continue;
			XStudent student = server.getStudent(request.getStudentId());
			if (student == null) continue;
			if (request.getEnrollment() == null && !student.canAssign(request)) continue;
			if (!query().match(new StatusPageSuggestionsAction.CourseRequestMatcher(server, course, student, offering, request, isConsentToDoCourse()))) continue;
			
			ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
			st.setId(student.getStudentId());
			st.setSessionId(server.getAcademicSession().getUniqueId());
			st.setExternalId(student.getExternalId());
			st.setName(student.getName());
			for (XAcademicAreaCode ac: student.getAcademicAreaClasiffications()) {
				st.addArea(ac.getArea());
				st.addClassification(ac.getCode());
			}
			for (XAcademicAreaCode ac: student.getMajors()) {
				st.addMajor(ac.getCode());
			}
			for (String gr: student.getGroups()) {
				st.addGroup(gr);
			}
			for (String acc: student.getAccomodations()) {
				st.addAccommodation(acc);
			}
			
			ClassAssignmentInterface.Enrollment e = new ClassAssignmentInterface.Enrollment();
			e.setStudent(st);
			e.setPriority(1 + request.getPriority());
			CourseAssignment c = new CourseAssignment();
			c.setCourseId(course.getCourseId());
			c.setSubject(course.getSubjectArea());
			c.setCourseNbr(course.getCourseNumber());
			e.setCourse(c);
			e.setWaitList(request.isWaitlist());
			if (!request.getCourseIds().get(0).equals(course))
				e.setAlternative(request.getCourseIds().get(0).getCourseName());
			if (request.isAlternative()) {
				for (XRequest r: student.getRequests()) {
					if (r instanceof XCourseRequest && !r.isAlternative() && ((XCourseRequest)r).getEnrollment() == null) {
						e.setAlternative(((XCourseRequest)r).getCourseIds().get(0).getCourseName());
					}
				}
			}
			if (request.getTimeStamp() != null)
				e.setRequestedDate(request.getTimeStamp());
			if (request.getEnrollment() != null) {
				if (request.getEnrollment().getReservation() != null) {
					switch (request.getEnrollment().getReservation().getType()) {
					case Individual:
						e.setReservation(MSG.reservationIndividual());
						break;
					case Group:
						e.setReservation(MSG.reservationGroup());
						break;
					case Course:
						e.setReservation(MSG.reservationCourse());
						break;
					case Curriculum:
						e.setReservation(MSG.reservationCurriculum());
						break;
					}
				}
				if (request.getEnrollment().getTimeStamp() != null)
					e.setEnrolledDate(request.getEnrollment().getTimeStamp());
				if (request.getEnrollment().getApproval() != null) {
					e.setApprovedDate(request.getEnrollment().getApproval().getTimeStamp());
					e.setApprovedBy(request.getEnrollment().getApproval().getName());
				}
				
				for (XSection section: offering.getSections(request.getEnrollment())) {
					ClassAssignmentInterface.ClassAssignment a = e.getCourse().addClassAssignment();
					a.setAlternative(request.isAlternative());
					a.setClassId(section.getSectionId());
					a.setSubpart(section.getSubpartName());
					a.setSection(section.getName(course.getCourseId()));
					a.setClassNumber(section.getName(-1l));
					a.setLimit(new int[] {enrollments.countEnrollmentsForSection(section.getSectionId()), section.getLimit()});
					if (section.getTime() != null) {
						for (DayCode d : DayCode.toDayCodes(section.getTime().getDays()))
							a.addDay(d.getIndex());
						a.setStart(section.getTime().getSlot());
						a.setLength(section.getTime().getLength());
						a.setBreakTime(section.getTime().getBreakTime());
						a.setDatePattern(section.getTime().getDatePatternName());
					}
					if (section.getNrRooms() > 0) {
						for (XRoom rm: section.getRooms()) {
							a.addRoom(rm.getName());
						}
					}
					if (section.getInstructors() != null) {
						for (XInstructor instructor: section.getInstructors()) {
							a.addInstructor(instructor.getName());
							a.addInstructoEmail(instructor.getEmail());
						}
					}
					if (section.getParentId()!= null)
						a.setParentSection(offering.getSection(section.getParentId()).getName(course.getCourseId()));
					a.setSubpartId(section.getSubpartId());
					a.addNote(course.getNote());
					a.addNote(section.getNote());
					XSubpart subpart = offering.getSubpart(section.getSubpartId());
					a.setCredit(subpart.getCredit(course.getCourseId()));
					int dist = 0;
					String from = null;
					TreeSet<String> overlap = new TreeSet<String>();
					for (XRequest q: student.getRequests()) {
						if (q instanceof XCourseRequest) {
							XEnrollment otherEnrollment = ((XCourseRequest)q).getEnrollment();
							if (otherEnrollment == null) continue;
							XOffering otherOffering = server.getOffering(otherEnrollment.getOfferingId());
							for (XSection otherSection: otherOffering.getSections(otherEnrollment)) {
								if (otherSection.equals(section) || otherSection.getTime() == null) continue;
								int d = otherSection.getDistanceInMinutes(section, m);
								if (d > dist) {
									dist = d;
									from = "";
									for (Iterator<XRoom> k = otherSection.getRooms().iterator(); k.hasNext();)
										from += k.next().getName() + (k.hasNext() ? ", " : "");
								}
								if (d > otherSection.getTime().getBreakTime()) {
									a.setDistanceConflict(true);
								}
								if (section.getTime() != null && section.getTime().hasIntersection(otherSection.getTime()) && !section.isToIgnoreStudentConflictsWith(offering.getDistributions(), otherSection.getSectionId())) {
									XCourse otherCourse = otherOffering.getCourse(otherEnrollment.getCourseId());
									XSubpart otherSubpart = otherOffering.getSubpart(otherSection.getSubpartId());
									overlap.add(MSG.clazz(otherCourse.getSubjectArea(), otherCourse.getCourseNumber(), otherSubpart.getName(), otherSection.getName(otherCourse.getCourseId())));
								}
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
						a.setParentSection(course.getConsentLabel());
					a.setExpected(overExp.getExpected(section, expectations));
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
