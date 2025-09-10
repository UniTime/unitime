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

import java.util.ArrayList;
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
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XAreaClassificationMajor;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XExpectations;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XOverride;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;
import org.unitime.timetable.onlinesectioning.updates.WaitlistedOnlineSectioningAction;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class ListEnrollments extends WaitlistedOnlineSectioningAction<List<ClassAssignmentInterface.Enrollment>> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	private Long iOfferingId, iSectionId;
	private boolean iCanShowExtIds = false, iCanRegister = false, iCanUseAssistant = false;
	protected boolean iIsAdmin = false, iIsAdvisor = false, iCanEditMyStudents = false, iCanEditOtherStudents = false;
	
	public ListEnrollments forOffering(Long offeringId) {
		iOfferingId = offeringId;
		return this;
	}
	
	public ListEnrollments withSection(Long sectionId) {
		iSectionId = sectionId;
		return this;
	}
	
	public ListEnrollments canShowExternalIds(boolean canShowExtIds) {
		iCanShowExtIds = canShowExtIds;
		return this;
	}
	
	public ListEnrollments canRegister(boolean canRegister) {
		iCanRegister = canRegister;
		return this;
	}
	
	public ListEnrollments canUseAssistant(boolean canUseAssistant) {
		iCanUseAssistant = canUseAssistant;
		return this;
	}
	
	public ListEnrollments withPermissions(boolean isAdmin, boolean isAdvisor, boolean canEditMyStudents, boolean canEditOtherStudents) {
		iIsAdmin = isAdmin; iIsAdvisor = isAdvisor;
		iCanEditMyStudents = canEditMyStudents; iCanEditOtherStudents = canEditOtherStudents;
		return this;
	}
	
	@Override
	public List<ClassAssignmentInterface.Enrollment> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.readLock();
		try {
			List<ClassAssignmentInterface.Enrollment> enrollments = new ArrayList<ClassAssignmentInterface.Enrollment>();
			XOffering offering = server.getOffering(iOfferingId);
			DistanceMetric m = server.getDistanceMetric();
			OverExpectedCriterion overExp = server.getOverExpectedCriterion();
			Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_REQUEST);
			XExpectations expectations = server.getExpectations(iOfferingId);
			
			Set<String> regStates = new HashSet<String>();
			Set<String> assStates = new HashSet<String>();
			Set<String> wlStates = new HashSet<String>();
			Set<String> noSubStates = new HashSet<String>();
			AcademicSessionInfo session = server.getAcademicSession();
			Session dbSession = SessionDAO.getInstance().get(session.getUniqueId());
			for (StudentSectioningStatus status: StudentSectioningStatusDAO.getInstance().findAll(helper.getHibSession())) {
				if (StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.enabled)
					|| (iIsAdmin && StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.admin))
					|| (iIsAdvisor && StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.advisor))
					) assStates.add(status.getReference());
				if (StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.regenabled)
					|| (iIsAdmin && StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.regadmin))
					|| (iIsAdvisor && StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.regadvisor))
					) regStates.add(status.getReference());
				if (StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.waitlist))
					wlStates.add(status.getReference());
				else if (StudentSectioningStatus.hasEffectiveOption(status, dbSession, StudentSectioningStatus.Option.nosubs))
					noSubStates.add(status.getReference());
			}

			XEnrollments requests = server.getEnrollments(iOfferingId);
			for (XCourseRequest request: requests.getRequests()) {
				XEnrollment enrollment = request.getEnrollment();
				if (iSectionId != null && (enrollment == null || !enrollment.getSectionIds().contains(iSectionId))) continue;
				
				for (XCourse course: offering.getCourses()) {
					if (!request.getCourseIds().contains(course)) continue;
					if (enrollment != null && !course.getCourseId().equals(enrollment.getCourseId())) continue;
					
					XStudent student = server.getStudent(request.getStudentId());
					String status = student.getStatus();
					if (status == null) status = session.getDefaultSectioningStatus();
					WaitListMode wl = WaitListMode.None;
					if (status == null || wlStates.contains(status))
						wl = WaitListMode.WaitList;
					else if (noSubStates.contains(status))
						wl = WaitListMode.NoSubs;

					if (enrollment == null && !student.canAssign(request, wl)) continue;

					ClassAssignmentInterface.Enrollment e = new ClassAssignmentInterface.Enrollment();

					// fill student information in
					ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
					st.setId(student.getStudentId());
					st.setSessionId(server.getAcademicSession().getUniqueId());
					st.setExternalId(student.getExternalId());
					st.setCanShowExternalId(iCanShowExtIds);
					st.setWaitListMode(wl);
					st.setCanRegister(iCanRegister && (status == null || regStates.contains(status)));
					st.setCanUseAssistant(iCanUseAssistant && (status == null || assStates.contains(status)));
					st.setName(student.getName());
					for (XAreaClassificationMajor acm: student.getMajors()) {
						st.addArea(acm.getArea(), acm.getAreaLabel());
						st.addClassification(acm.getClassification(), acm.getClassificationLabel());
						st.addMajor(acm.getMajor(), acm.getMajorLabel());
						st.addConcentration(acm.getConcentration(), acm.getConcentrationLabel());
						st.addDegree(acm.getDegree(), acm.getDegreeLabel());
						st.addProgram(acm.getProgram(), acm.getProgramLabel());
						st.addCampus(acm.getCampus(), acm.getCampusLabel());
					}
					st.setDefaultCampus(server.getAcademicSession().getCampus());
					for (XAreaClassificationMajor acm: student.getMinors()) {
						st.addMinor(acm.getMajor(), acm.getMajorLabel());
					}
					for (XStudent.XGroup gr: student.getGroups()) {
						st.addGroup(gr.getType(), gr.getAbbreviation(), gr.getTitle());
					}
					for (XStudent.XGroup acc: student.getAccomodations()) {
						st.addAccommodation(acc.getAbbreviation(), acc.getTitle());
					}
					for (XStudent.XAdvisor a: student.getAdvisors()) {
						if (a.getName() != null) st.addAdvisor(a.getName());
					}
					e.setStudent(st);
					
					// fill course request information in
					e.setPriority(1 + request.getPriority());
					CourseAssignment c = new CourseAssignment();
					c.setCourseId(course.getCourseId());
					c.setSubject(course.getSubjectArea());
					c.setCourseNbr(course.getCourseNumber());
					c.setTitle(course.getTitle());
					c.setParentCourseId(course.getParentCourseId());
					c.setHasCrossList(offering.hasCrossList());
					c.setCanWaitList(offering.isWaitList());
					e.setCourse(c);
					e.setWaitList(request.isWaitlist());
					e.setNoSub(request.isNoSub());
					if (!request.getCourseIds().get(0).equals(course))
						e.setAlternative(request.getCourseIds().get(0).getCourseName());
					if (request.isAlternative()) {
						for (XRequest r: student.getRequests()) {
							if (r instanceof XCourseRequest && !r.isAlternative() && ((XCourseRequest) r).getEnrollment() == null) {
								e.setAlternative(((XCourseRequest)r).getCourseIds().get(0).getCourseName());
							}
						}
					}
					if (request.getTimeStamp() != null)
						e.setRequestedDate(request.getTimeStamp());
					e.setCritical(request.getCritical());
					if (request.getWaitListedTimeStamp() != null && request.getEnrollment() == null)
						e.setWaitListedDate(request.getWaitListedTimeStamp());
					e.setWaitListedPosition(getWaitListPosition(offering, student, request, course, server, helper));
					if (student.isEnrolled(request.getWaitListSwapWithCourseOffering()))
						e.setWaitListedReplacement(request.getWaitListSwapWithCourseOffering().getCourseName());
					if (enrollment == null) {
						e.setEnrollmentMessage(request.getEnrollmentMessage());
						if (request.hasOverrides()) {
							XOverride override = request.getOverride(course);
							if (override != null && override.getStatus() != null) {
								switch (CourseRequestOverrideStatus.values()[override.getStatus()]) {
								case PENDING:
									e.addEnrollmentMessage(MSG.overridePendingShort(course.getCourseName())); break;
								case REJECTED:
									e.addEnrollmentMessage(MSG.overrideRejectedWaitList(course.getCourseName())); break;
								case CANCELLED:
									e.addEnrollmentMessage(MSG.overrideCancelledWaitList(course.getCourseName())); break;
								case NOT_CHECKED:
									e.addEnrollmentMessage(MSG.overrideNotRequested()); break;
								case NOT_NEEDED:
									e.addEnrollmentMessage(MSG.overrideNotNeeded(course.getCourseName())); break;
								}
							}
						}
						if (student.getMaxCreditOverride() != null && student.getMaxCreditOverride().getStatus() != null && student.getMaxCredit() != null && course.hasCredit()) {
							float credit = 0f;
							for (XRequest r: student.getRequests()) {
								if (r instanceof XCourseRequest && ((XCourseRequest)r).getEnrollment() != null) {
									credit += ((XCourseRequest)r).getEnrollment().getCredit(server);
								}
							}
							if (credit + course.getMinCredit() > student.getMaxCredit()) {
								switch (CourseRequestOverrideStatus.values()[student.getMaxCreditOverride().getStatus()]) {
								case PENDING:
									e.addEnrollmentMessage(MSG.creditStatusPendingShort()); break;
								case REJECTED:
									e.addEnrollmentMessage(MSG.creditStatusDenied()); break;
								case CANCELLED:
									e.addEnrollmentMessage(MSG.creditStatusCancelledWaitList()); break;
								case NOT_CHECKED:
									e.addEnrollmentMessage(MSG.overrideNotRequested()); break;
								}
							}
						}
						if (request.isWaitlist(wl) && offering.isWaitList()) {
							Assignment<Request, Enrollment> assignment = new AssignmentMap<Request, Enrollment>();
							org.cpsolver.studentsct.model.CourseRequest courseRequest = SectioningRequest.convert(assignment, request, server,  wl, helper);
							Collection<Enrollment> enrls = courseRequest.getEnrollmentsSkipSameTime(assignment);
							TreeSet<Enrollment> overlap = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
								@Override
								public int compare(Enrollment o1, Enrollment o2) {
									return o1.getRequest().compareTo(o2.getRequest());
								}
							});
							Hashtable<org.cpsolver.studentsct.model.CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<org.cpsolver.studentsct.model.CourseRequest, TreeSet<Section>>();
							Enrollment noConfEnrl = null;
							for (Iterator<Enrollment> f = enrls.iterator(); f.hasNext();) {
								Enrollment enrl = f.next();
								if (!course.getCourseId().equals(enrl.getCourse().getId())) continue;
								boolean overlaps = false;
								for (Request q: enrl.getStudent().getRequests()) {
									if (q.equals(request)) continue;
									Enrollment x = assignment.getValue(q);
									if (q instanceof FreeTimeRequest) {
										if (GetAssignment.isFreeTimeOverlapping((FreeTimeRequest)q, enrl)) {
											overlaps = true;
											overlap.add(((FreeTimeRequest)q).createEnrollment());
										}
									} else if (x != null && x.getAssignments() != null && !x.getAssignments().isEmpty()) {
										for (Iterator<SctAssignment> i = x.getAssignments().iterator(); i.hasNext();) {
								        	SctAssignment a = i.next();
											if (a.isOverlapping(enrl.getAssignments())) {
												overlaps = true;
												overlap.add(x);
												if (x.getRequest() instanceof org.cpsolver.studentsct.model.CourseRequest) {
													org.cpsolver.studentsct.model.CourseRequest cr = (org.cpsolver.studentsct.model.CourseRequest)x.getRequest();
													TreeSet<Section> ss = overlapingSections.get(cr);
													if (ss == null) { ss = new TreeSet<Section>(new AssignmentComparator<Section, Request, Enrollment>(assignment)); overlapingSections.put(cr, ss); }
													ss.add((Section)a);
												}
											}
								        }
									}
								}
								if (!overlaps && noConfEnrl == null)
									noConfEnrl = enrl;
							}
							if (noConfEnrl == null) {
								Set<String> overlaps = new TreeSet<String>();
								for (Enrollment q: overlap) {
									if (q.getRequest() instanceof FreeTimeRequest) {
										overlaps.add(OnlineSectioningHelper.toString((FreeTimeRequest)q.getRequest()));
									} else {
										org.cpsolver.studentsct.model.CourseRequest cr = (org.cpsolver.studentsct.model.CourseRequest)q.getRequest();
										Course o = q.getCourse();
										String ov = MSG.course(o.getSubjectArea(), o.getCourseNumber());
										if (overlapingSections.get(cr).size() == 1)
											for (Iterator<Section> i = overlapingSections.get(cr).iterator(); i.hasNext();) {
												Section s = i.next();
												ov += " " + s.getSubpart().getName();
												if (i.hasNext()) ov += ",";
											}
										overlaps.add(ov);
									}
								}
								if (overlaps != null && !overlaps.isEmpty()) {
									String message = null;
									for (Iterator<String> i = overlaps.iterator(); i.hasNext();) {
										String ov = i.next();
										if (message == null)
											message = MSG.conflictWithFirst(ov);
										else if (i.hasNext())
											message += MSG.conflictWithMiddle(ov);
										else
											message += MSG.conflictWithLast(ov);
									}
									e.addEnrollmentMessage(message + ".");
								}
							}
						}
					}
					
					// fill enrollment information in
					if (enrollment != null) {
						if (enrollment.getReservation() != null) {
							switch (enrollment.getReservation().getType()) {
							case Course:
								e.setReservation(MSG.reservationCourse());
								break;
							case Curriculum:
							case CurriculumOverride:
								e.setReservation(MSG.reservationCurriculum());
								break;
							case Group:
							case GroupOverride:
								e.setReservation(MSG.reservationGroup());
								break;
							case Individual:
							case IndividualOverride:
								e.setReservation(MSG.reservationIndividual());
								break;
							case LearningCommunity:
								e.setReservation(MSG.reservationLearningCommunity());
								break;
							case Universal:
								e.setReservation(MSG.reservationUniversal());
								break;
							}
						}
						e.setEnrolledDate(request.getEnrollment().getTimeStamp());
						if (request.getEnrollment().getApproval() != null) {
							e.setApprovedDate(request.getEnrollment().getApproval().getTimeStamp());
							e.setApprovedBy(request.getEnrollment().getApproval().getName());
						}
						
						for (Long sectionId: request.getEnrollment().getSectionIds()) {
							XSection section = offering.getSection(sectionId);
							ClassAssignmentInterface.ClassAssignment a = e.getCourse().addClassAssignment();
							a.setAlternative(request.isAlternative());
							a.setClassId(section.getSectionId());
							XSubpart subpart = offering.getSubpart(section.getSubpartId());
							a.setSubpart(subpart.getName());
							a.setSection(section.getName(course.getCourseId()));
							a.setExternalId(section.getExternalId(c.getCourseId()));
							a.setClassNumber(section.getName(-1l));
							a.setCancelled(section.isCancelled());
							a.setLimit(new int[] {requests.countEnrollmentsForSection(section.getSectionId()), section.getLimit()});
							if (section.getTime() != null) {
								for (DayCode d : DayCode.toDayCodes(section.getTime().getDays()))
									a.addDay(d.getIndex());
								a.setStart(section.getTime().getSlot());
								a.setLength(section.getTime().getLength());
								a.setBreakTime(section.getTime().getBreakTime());
								a.setDatePattern(section.getTime().getDatePatternName());
							}
							for (XRoom rm: section.getRooms())
								a.addRoom(rm.getUniqueId(), rm.getName());
							for (XInstructor instructor: section.getInstructors()) {
								a.addInstructor(instructor.getName());
								a.addInstructor(instructor.getEmail() == null ? "" : instructor.getEmail());
							}
							if (section.getParentId() != null)
								a.setParentSection(offering.getSection(section.getParentId()).getName(course.getCourseId()));
							a.setSubpartId(section.getSubpartId());
							a.addNote(course.getNote());
							a.addNote(section.getNote());
							a.setCredit(subpart.getCredit(course.getCourseId()));
							a.setCreditRange(subpart.getCreditMin(course.getCourseId()), subpart.getCreditMax(course.getCourseId()));
							Float creditOverride = section.getCreditOverride(c.getCourseId());
							if (creditOverride != null) a.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
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
											from += " (" + otherEnrollment.getCourseName() + " " + otherSection.getSubpartName() + " " + otherSection.getName(otherEnrollment.getCourseId()) + ")";
										}
										if (otherSection.isDistanceConflict(student, section, m)) {
											a.setDistanceConflict(true);
											a.setLongDistanceConflict(otherSection.isLongDistanceConflict(student, section, m));
										}
										if (section.getTime() != null && section.getTime().hasIntersection(otherSection.getTime()) && !section.isToIgnoreStudentConflictsWith(offering.getDistributions(), otherSection.getSectionId())) {
											XCourse otherCourse = otherOffering.getCourse(otherEnrollment.getCourseId());
											XSubpart otherSubpart = otherOffering.getSubpart(otherSection.getSubpartId());
											overlap.add(MSG.clazz(otherCourse.getSubjectArea(), otherCourse.getCourseNumber(), otherSubpart.getName(), otherSection.getName(otherCourse.getCourseId())));
										}
										if (otherSection.isHardDistanceConflict(student, section, m)) {
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
									if (request.getEnrollment().getApproval() != null) {
										a.setParentSection(MSG.consentApproved(df.format(request.getEnrollment().getApproval().getTimeStamp())));
									} else
										a.setParentSection(MSG.consentWaiting(consent.toLowerCase()));
								}
							}
							a.setExpected(overExp.getExpected(section.getLimit(), expectations.getExpectedSpace(section.getSectionId())));
						}
					}
					enrollments.add(e);
				}
			}
			return enrollments;
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "list-enrollments";
	}

}
