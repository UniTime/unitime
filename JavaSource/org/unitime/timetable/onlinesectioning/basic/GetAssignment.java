/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.onlinesectioning.basic;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.StudentEnrollmentProvider.EnrollmentFailure;
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
	private static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	
	private Long iStudentId;
	private List<EnrollmentFailure> iMessages;
	private boolean iIncludeRequest = false;
	
	public GetAssignment forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}
	
	public GetAssignment withMessages(List<EnrollmentFailure> messages) {
		iMessages = messages;
		return this;
	}
	
	public GetAssignment withRequest(boolean includeRequest) {
		iIncludeRequest = includeRequest;
		return this;
	}

	@Override
	public ClassAssignmentInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.readLock();
		try {
			Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_REQUEST);
			DistanceMetric m = server.getDistanceMetric();
			OverExpectedCriterion overExp = server.getOverExpectedCriterion();
			OnlineSectioningLog.Action.Builder action = helper.getAction();
			action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(iStudentId));
			XStudent student = server.getStudent(iStudentId);
			if (student == null) return null;
			action.getStudentBuilder().setExternalId(student.getExternalId());
			action.getStudentBuilder().setName(student.getName());
	        ClassAssignmentInterface ret = new ClassAssignmentInterface();
			int nrUnassignedCourses = 0, nrAssignedAlt = 0;
			OnlineSectioningLog.Enrollment.Builder stored = OnlineSectioningLog.Enrollment.newBuilder();
			stored.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
			
			for (XRequest request: student.getRequests()) {
				action.addRequest(OnlineSectioningHelper.toProto(request));
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
					ca.setEnrollmentMessage(r.getEnrollmentMessage());
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
							stored.addSection(OnlineSectioningHelper.toProto(section, enrollment));
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
								a.setOverlapNote(note);
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
							a.setExpected(overExp.getExpected(section.getLimit(), expectations.getExpectedSpace(section.getSectionId())));
						}
					}
					
					if (iMessages != null) {
						XEnrollments enrollments = server.getEnrollments(offering.getOfferingId());
						f: for (EnrollmentFailure f: iMessages) {
							XSection section = f.getSection();
							if (!f.getCourse().getCourseId().equals(ca.getCourseId())) continue;
							for (ClassAssignmentInterface.ClassAssignment a: ca.getClassAssignments())
								if (f.getSection().getSectionId().equals(a.getClassId())) {
									a.setError(f.getMessage());
									continue f;
								}
							ca.setAssigned(true);
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
							a.addNote(course.getNote());
							a.addNote(section.getNote());
							a.setCredit(subpart.getCredit(course.getCourseId()));
							int dist = 0;
							String from = null;
							a.setBackToBackDistance(dist);
							a.setBackToBackRooms(from);
							a.setSaved(false);
							a.setDummy(true);
							a.setError(f.getMessage());
							a.setExpected(overExp.getExpected(section.getLimit(), expectations.getExpectedSpace(section.getSectionId())));
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
			action.addEnrollment(stored);
			
			if (iMessages != null) {
				Set<String> added = new HashSet<String>();
				for (EnrollmentFailure f: iMessages) {
					for (String fm: f.getMessage().split("\n")) {
						String message = MSG.clazz(f.getCourse().getSubjectArea(), f.getCourse().getCourseNumber(), f.getSection().getSubpartName(), f.getSection().getName(f.getCourse().getCourseId())) + ": " + fm;
						if (added.add(message))
							ret.addMessage(message);
					}
				}
			}
			
			if (iIncludeRequest) {
				CourseRequestInterface request = new CourseRequestInterface();
				request.setStudentId(iStudentId);
				request.setSaved(true);
				request.setAcademicSessionId(server.getAcademicSession().getUniqueId());
				CourseRequestInterface.Request lastRequest = null;
				int lastRequestPriority = -1;
				for (XRequest cd: student.getRequests()) {
					CourseRequestInterface.Request r = null;
					if (cd instanceof XFreeTimeRequest) {
						XFreeTimeRequest ftr = (XFreeTimeRequest)cd;
						CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
						ft.setStart(ftr.getTime().getSlot());
						ft.setLength(ftr.getTime().getLength());
						for (DayCode day : DayCode.toDayCodes(ftr.getTime().getDays()))
							ft.addDay(day.getIndex());
						if (lastRequest != null && lastRequestPriority == cd.getPriority()) {
							r = lastRequest;
							lastRequest.addRequestedFreeTime(ft);
							lastRequest.setRequestedCourse(lastRequest.getRequestedCourse() + ", " + ft.toString());
						} else {
							r = new CourseRequestInterface.Request();
							r.addRequestedFreeTime(ft);
							r.setRequestedCourse(ft.toString());
							if (cd.isAlternative())
								request.getAlternatives().add(r);
							else
								request.getCourses().add(r);
						}
					} else if (cd instanceof XCourseRequest) {
						r = new CourseRequestInterface.Request();
						int order = 0;
						for (XCourseId courseId: ((XCourseRequest)cd).getCourseIds()) {
							XCourse c = server.getCourse(courseId.getCourseId());
							if (c == null) continue;
							switch (order) {
								case 0: 
									r.setRequestedCourse(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
									break;
								case 1:
									r.setFirstAlternative(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
									break;
								case 2:
									r.setSecondAlternative(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() ? "" : " - " + c.getTitle()));
								}
							order++;
							}
						r.setWaitList(((XCourseRequest)cd).isWaitlist());
						if (r.hasRequestedCourse()) {
							if (cd.isAlternative())
								request.getAlternatives().add(r);
							else
								request.getCourses().add(r);
						}
						lastRequest = r;
						lastRequestPriority = cd.getPriority();
					}
					action.addRequest(OnlineSectioningHelper.toProto(cd));
				}
				ret.setRequest(request);
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
