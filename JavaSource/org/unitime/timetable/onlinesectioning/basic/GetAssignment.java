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
package org.unitime.timetable.onlinesectioning.basic;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;


import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentComparator;
import org.cpsolver.ifs.assignment.AssignmentMap;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;

import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class GetAssignment implements OnlineSectioningAction<ClassAssignmentInterface>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	private Long iStudentId;
	
	public GetAssignment(Long studentId) {
		iStudentId = studentId;
	}

	@Override
	public ClassAssignmentInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.readLock();
		try {
			Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_REQUEST);
			DistanceMetric m = server.getDistanceMetric();
			XStudent student = server.getStudent(iStudentId);
			if (student == null) return null;
	        ClassAssignmentInterface ret = new ClassAssignmentInterface();
			int nrUnassignedCourses = 0, nrAssignedAlt = 0;
			for (XRequest request: student.getRequests()) {
				ClassAssignmentInterface.CourseAssignment ca = new ClassAssignmentInterface.CourseAssignment();
				if (request instanceof XCourseRequest) {
					XCourseRequest r = (XCourseRequest)request;
					
					XEnrollment enrollment = r.getEnrollment();
					XCourseId courseId = (enrollment == null ? r.getCourseIds().get(0) : enrollment);
					XOffering offering = server.getOffering(courseId.getOfferingId());
					XExpectations expectations = server.getExpectations(courseId.getOfferingId());
					XCourse course = offering.getCourse(courseId);
					
					if (request.isAlternative() && nrAssignedAlt >= nrUnassignedCourses && enrollment == null) continue;
					if (request.isAlternative() && enrollment != null) nrAssignedAlt++;

					if (server.isOfferingLocked(course.getOfferingId()))
						ca.setLocked(true);
					ca.setAssigned(enrollment != null);
					ca.setCourseId(course.getCourseId());
					ca.setSubject(course.getSubjectArea());
					ca.setWaitListed(r.isWaitlist());
					ca.setCourseNbr(course.getCourseNumber());
					if (enrollment == null) {
						TreeSet<Enrollment> overlap = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
							@Override
							public int compare(Enrollment o1, Enrollment o2) {
								return o1.getRequest().compareTo(o2.getRequest());
							}
						});
						Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
						Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
						Collection<Enrollment> avEnrls = SectioningRequest.convert(assignment, r, server).getAvaiableEnrollmentsSkipSameTime(assignment);
						for (Iterator<Enrollment> e = avEnrls.iterator(); e.hasNext();) {
							Enrollment enrl = e.next();
							for (Request q: enrl.getStudent().getRequests()) {
								if (q.equals(request)) continue;
								Enrollment x = assignment.getValue(q);
								if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
						        for (Iterator<SctAssignment> i = x.getAssignments().iterator(); i.hasNext();) {
						        	SctAssignment a = i.next();
									if (a.isOverlapping(enrl.getAssignments())) {
										overlap.add(x);
										if (x.getRequest() instanceof CourseRequest) {
											CourseRequest cr = (CourseRequest)x.getRequest();
											TreeSet<Section> ss = overlapingSections.get(cr);
											if (ss == null) { ss = new TreeSet<Section>(new AssignmentComparator<Section, Request, Enrollment>(assignment)); overlapingSections.put(cr, ss); }
											ss.add((Section)a);
										}
									}
						        }
							}
						}
						for (Enrollment q: overlap) {
							if (q.getRequest() instanceof FreeTimeRequest) {
								ca.addOverlap(OnlineSectioningHelper.toString((FreeTimeRequest)q.getRequest()));
							} else {
								CourseRequest cr = (CourseRequest)q.getRequest();
								Course o = q.getCourse();
								String ov = MSG.course(o.getSubjectArea(), o.getCourseNumber());
								if (overlapingSections.get(cr).size() == 1)
									for (Iterator<Section> i = overlapingSections.get(cr).iterator(); i.hasNext();) {
										Section s = i.next();
										ov += " " + s.getSubpart().getName();
										if (i.hasNext()) ov += ",";
									}
								ca.addOverlap(ov);
							}
						}
						if (avEnrls.isEmpty()) ca.setNotAvailable(true);
						if (!r.isWaitlist()) nrUnassignedCourses++;
						int alt = nrUnassignedCourses;
						for (XRequest q: student.getRequests()) {
							if (q instanceof XCourseRequest && !q.equals(request)) {
								XEnrollment otherEnrollment = ((XCourseRequest)q).getEnrollment();
								if (otherEnrollment == null) continue;
								if (q.isAlternative()) {
									if (--alt == 0) {
										XOffering otherOffering = server.getOffering(otherEnrollment.getOfferingId());
										XCourse otherCourse = otherOffering.getCourse(otherEnrollment.getCourseId());
										ca.setInstead(MSG.course(otherCourse.getSubjectArea(), otherCourse.getCourseNumber()));
										break;
									}
								}
							}
						}
					} else {
						List<XSection> sections = offering.getSections(enrollment);
						boolean hasAlt = false;
						if (r.getCourseIds().size() > 1) {
							hasAlt = true;
						} else if (offering.getConfigs().size() > 1) {
							hasAlt = true;
						} else {
							for (XSubpart subpart: offering.getConfigs().get(0).getSubparts()) {
								if (subpart.getSections().size() > 1) { hasAlt = true; break; }
							}
						}
						XEnrollments enrollments = server.getEnrollments(offering.getOfferingId());
						for (XSection section: sections) {
							ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
							a.setAlternative(r.isAlternative());
							a.setClassId(section.getSectionId());
							XSubpart subpart = offering.getSubpart(section.getSubpartId());
							a.setSubpart(subpart.getName());
							a.setClassNumber(section.getName(-1l));
							a.setSection(section.getName(course.getCourseId()));
							a.setLimit(new int[] {enrollments.countEnrollmentsForSection(section.getSectionId()), section.getLimit()});
							if (section.getTime() != null) {
								for (DayCode d : DayCode.toDayCodes(section.getTime().getDays()))
									a.addDay(d.getIndex());
								a.setStart(section.getTime().getSlot());
								a.setLength(section.getTime().getLength());
								a.setBreakTime(section.getTime().getBreakTime());
								a.setDatePattern(section.getTime().getDatePatternName());
							}
							if (section.getRooms() != null) {
								for (XRoom room: section.getRooms()) {
									a.addRoom(room.getName());
								}
							}
							for (XInstructor instructor: section.getInstructors()) {
								a.addInstructor(instructor.getName());
								a.addInstructoEmail(instructor.getEmail() == null ? "" : instructor.getEmail());
							}
							if (section.getParentId() != null)
								a.setParentSection(offering.getSection(section.getParentId()).getName(course.getCourseId()));
							a.setSubpartId(section.getSubpartId());
							a.setHasAlternatives(hasAlt);
							a.addNote(course.getNote());
							a.addNote(section.getNote());
							a.setCredit(subpart.getCredit());
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
							if (a.getParentSection() == null) {
								String consent = server.getCourse(course.getCourseId()).getConsentLabel();
								if (consent != null) {
									if (enrollment.getApproval() != null) {
										a.setParentSection(MSG.consentApproved(df.format(enrollment.getApproval().getTimeStamp())));
									} else
										a.setParentSection(MSG.consentWaiting(consent.toLowerCase()));
								}
							}
							a.setExpected(Math.round(expectations.getExpectedSpace(section.getSectionId())));
						}
					}
				} else if (request instanceof XFreeTimeRequest) {
					XFreeTimeRequest r = (XFreeTimeRequest)request;
					ca.setCourseId(null);
					for (XRequest q: student.getRequests()) {
						if (q instanceof XCourseRequest) {
							XEnrollment otherEnrollment = ((XCourseRequest)q).getEnrollment();
							if (otherEnrollment == null) continue;
							XOffering otherOffering = server.getOffering(otherEnrollment.getOfferingId());
							for (XSection otherSection: otherOffering.getSections(otherEnrollment)) {
								if (otherSection.getTime() != null && otherSection.getTime().hasIntersection(r.getTime())) {
									XCourse otherCourse = otherOffering.getCourse(otherEnrollment.getCourseId());
									XSubpart otherSubpart = otherOffering.getSubpart(otherSection.getSubpartId());
									ca.addOverlap(MSG.clazz(otherCourse.getSubjectArea(), otherCourse.getCourseNumber(), otherSubpart.getName(), otherSection.getName(otherCourse.getCourseId())));
								}
							}
						}
					}
					ca.setAssigned(ca.getOverlaps() == null);
					ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
					a.setAlternative(r.isAlternative());
					for (DayCode d : DayCode.toDayCodes(r.getTime().getDays()))
						a.addDay(d.getIndex());
					a.setStart(r.getTime().getSlot());
					a.setLength(r.getTime().getLength());
				}
				ret.add(ca);
			}
			return ret;
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "get-assignment";
	}

}
