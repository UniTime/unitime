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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
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
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class ListEnrollments implements OnlineSectioningAction<List<ClassAssignmentInterface.Enrollment>> {
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
			}

			XEnrollments requests = server.getEnrollments(iOfferingId);
			for (XCourseRequest request: requests.getRequests()) {
				XEnrollment enrollment = request.getEnrollment();
				if (iSectionId != null && (enrollment == null || !enrollment.getSectionIds().contains(iSectionId))) continue;
				
				for (XCourse course: offering.getCourses()) {
					if (!request.getCourseIds().contains(course)) continue;
					if (enrollment != null && !course.getCourseId().equals(enrollment.getCourseId())) continue;
					
					XStudent student = server.getStudent(request.getStudentId());
					if (enrollment == null && !student.canAssign(request)) continue;
					String status = student.getStatus();
					if (status == null) status = session.getDefaultSectioningStatus();
					
					ClassAssignmentInterface.Enrollment e = new ClassAssignmentInterface.Enrollment();

					// fill student information in
					ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
					st.setId(student.getStudentId());
					st.setSessionId(server.getAcademicSession().getUniqueId());
					st.setExternalId(student.getExternalId());
					st.setCanShowExternalId(iCanShowExtIds);
					st.setCanRegister(iCanRegister && (status == null || regStates.contains(status)));
					st.setCanUseAssistant(iCanUseAssistant && (status == null || assStates.contains(status)));
					st.setName(student.getName());
					for (XAreaClassificationMajor acm: student.getMajors()) {
						st.addArea(acm.getArea());
						st.addClassification(acm.getClassification());
						st.addMajor(acm.getMajor());
					}
					for (String ac: student.getAccomodations())
						st.addAccommodation(ac);
					for (XStudent.XGroup gr: student.getGroups())
						st.addGroup(gr.getType(), gr.getAbbreviation(), gr.getTitle());
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
					c.setHasCrossList(offering.hasCrossList());
					e.setCourse(c);
					e.setWaitList(request.isWaitlist());
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
					if (enrollment == null)
						e.setEnrollmentMessage(request.getEnrollmentMessage());
					
					// fill enrollment information in
					if (enrollment != null) {
						if (enrollment.getReservation() != null) {
							switch (enrollment.getReservation().getType()) {
							case Course:
								e.setReservation(MSG.reservationCourse());
								break;
							case Curriculum:
								e.setReservation(MSG.reservationCurriculum());
								break;
							case Group:
								e.setReservation(MSG.reservationGroup());
								break;
							case Individual:
								e.setReservation(MSG.reservationIndividual());
								break;
							case LearningCommunity:
								e.setReservation(MSG.reservationLearningCommunity());
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
										}
										if (otherSection.isDistanceConflict(student, section, m))
											a.setDistanceConflict(true);
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
