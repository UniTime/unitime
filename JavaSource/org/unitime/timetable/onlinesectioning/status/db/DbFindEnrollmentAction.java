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
package org.unitime.timetable.onlinesectioning.status.db;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface.HasGradableSubpart;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseReservation;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LearningCommunityReservation;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.UniversalOverrideReservation;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideIntent;
import org.unitime.timetable.model.StudentSectioningStatus.Option;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.status.FindEnrollmentAction;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.CourseLookup;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentInfoAction.DbCourseRequestMatcher;

/**
 * @author Tomas Muller
 */
public class DbFindEnrollmentAction extends FindEnrollmentAction {
	private static final long serialVersionUID = 1L;
	
	public boolean isMyStudent(Student student) {
		return iMyStudents != null && iMyStudents.contains(student.getUniqueId());
	}
	
	public boolean isCanSelect(Student student) {
		if (iIsAdmin) return true;
		if (iIsAdvisor) {
			if (iCanEditOtherStudents || (iCanEditMyStudents && isMyStudent(student))) return true;
		} else {
			if (iCanSelect) return true;
		}
		return false;
	}

	@Override
	public List<ClassAssignmentInterface.Enrollment> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (iFilter == null) return super.execute(server, helper);
		
		List<ClassAssignmentInterface.Enrollment> ret = new ArrayList<ClassAssignmentInterface.Enrollment>();
		
		AcademicSessionInfo session = server.getAcademicSession();
		CourseLookup lookup = new CourseLookup(session);
		OverExpectedCriterion overExp = server.getOverExpectedCriterion();
		
		CourseOffering course = CourseOfferingDAO.getInstance().get(courseId(), helper.getHibSession());
		if (course == null) return ret;
		InstructionalOffering offering = course.getInstructionalOffering();
		if (offering == null) return ret;
		HasGradableSubpart gs = null;
		if (ApplicationProperty.OnlineSchedulingGradableIType.isTrue() && Class_.getExternalClassNameHelper() != null && Class_.getExternalClassNameHelper() instanceof HasGradableSubpart)
			gs = (HasGradableSubpart) Class_.getExternalClassNameHelper();
		
		for (CourseRequest request: helper.getHibSession().createQuery(
				"from CourseRequest where courseOffering.uniqueId = :courseId", CourseRequest.class
				).setParameter("courseId", course.getUniqueId()).setCacheable(true).list()) {
			DbCourseRequestMatcher crm = new DbCourseRequestMatcher(session, request, isConsentToDoCourse(), isMyStudent(request.getCourseDemand().getStudent()), helper.getStudentNameFormat(), lookup);
			if (classId() != null) {
				boolean match = false;
				for (StudentClassEnrollment e: crm.enrollment()) {
					if (e.getClazz().getUniqueId().equals(classId())) { match = true; break; }
				}
				if (!match && !crm.enrollment().isEmpty()) continue;
			}
			if (!query().match(crm)) continue;
			if (crm.enrollment().isEmpty() && !crm.canAssign()) continue;
			
			Student student = request.getCourseDemand().getStudent();
			
			ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
			st.setId(student.getUniqueId());
			st.setSessionId(session.getUniqueId());
			st.setExternalId(student.getExternalUniqueId());
			st.setCanShowExternalId(iCanShowExtIds);
			StudentSectioningStatus status = student.getEffectiveStatus();
			if (status == null || status.hasOption(Option.waitlist)) {
				st.setWaitListMode(WaitListMode.WaitList);
			} else if (status != null && status.hasOption(Option.nosubs)) {
				st.setWaitListMode(WaitListMode.NoSubs);
			} else {
				st.setWaitListMode(WaitListMode.None);
			}
			st.setCanRegister(iCanRegister && (status == null
					|| status.hasOption(StudentSectioningStatus.Option.regenabled)
					|| (iIsAdmin && status.hasOption(StudentSectioningStatus.Option.regadmin))
					|| (iIsAdvisor && status.hasOption(StudentSectioningStatus.Option.regadvisor))
					));
			st.setCanUseAssistant(iCanUseAssistant && (status == null
					|| status.hasOption(StudentSectioningStatus.Option.enabled)
					|| (iIsAdmin && status.hasOption(StudentSectioningStatus.Option.admin))
					|| (iIsAdvisor && status.hasOption(StudentSectioningStatus.Option.advisor))
					));
			st.setCanSelect(isCanSelect(student));
			st.setName(helper.getStudentNameFormat().format(student));
			for (StudentAreaClassificationMajor acm: new TreeSet<StudentAreaClassificationMajor>(student.getAreaClasfMajors())) {
				st.addArea(acm.getAcademicArea().getAcademicAreaAbbreviation(), acm.getAcademicArea().getTitle());
				st.addClassification(acm.getAcademicClassification().getCode(), acm.getAcademicClassification().getName());
				st.addMajor(acm.getMajor().getCode(), acm.getMajor().getName());
				st.addConcentration(acm.getConcentration() == null ? null : acm.getConcentration().getCode(), acm.getConcentration() == null ? null : acm.getConcentration().getName());
				st.addDegree(acm.getDegree() == null ? null : acm.getDegree().getReference(), acm.getDegree() == null ? null : acm.getDegree().getLabel());
				st.addProgram(acm.getProgram() == null ? null : acm.getProgram().getReference(), acm.getProgram() == null ? null : acm.getProgram().getLabel());
				st.addCampus(acm.getCampus() == null ? null : acm.getCampus().getReference(), acm.getCampus() == null ? null : acm.getCampus().getLabel());
			}
			st.setDefaultCampus(server.getAcademicSession().getCampus());
			for (StudentAreaClassificationMinor acm: new TreeSet<StudentAreaClassificationMinor>(student.getAreaClasfMinors())) {
				st.addMinor(acm.getMinor().getCode(), acm.getMinor().getName());
			}
			for (StudentAccomodation acc: student.getAccomodations()) {
				st.addAccommodation(acc.getAbbreviation(), acc.getName());
			}
			for (StudentGroup gr: student.getGroups()) {
				if (gr.getType() == null)
					st.addGroup(gr.getGroupAbbreviation(), gr.getGroupName());
				else
					st.addGroup(gr.getType().getReference(), gr.getGroupAbbreviation(), gr.getGroupName());
			}
			for (Advisor a: student.getAdvisors()) {
				if (a.getLastName() != null)
					st.addAdvisor(helper.getInstructorNameFormat().format(a));
			}
			
			ClassAssignmentInterface.Enrollment e = new ClassAssignmentInterface.Enrollment();
			e.setStudent(st);
			e.setPriority(1 + request.getCourseDemand().getPriority());
			CourseAssignment c = new CourseAssignment();
			c.setCourseId(course.getUniqueId());
			c.setSubject(course.getSubjectAreaAbbv());
			c.setCourseNbr(course.getCourseNbr());
			c.setParentCourseId(course.getParentOffering() == null ? null : course.getParentOffering().getUniqueId());
			c.setTitle(course.getTitle());
			c.setHasCrossList(course.getInstructionalOffering().hasCrossList());
			c.setCanWaitList(course.getInstructionalOffering().effectiveWaitList());
			e.setCourse(c);
			e.setWaitList(request.getCourseDemand().effectiveWaitList());
			e.setNoSub(request.getCourseDemand().effectiveNoSub());
			if (crm.enrollment().isEmpty()) {
				if (request.getCourseDemand().getEnrollmentMessages() != null) {
		        	StudentEnrollmentMessage message = null;
		        	for (StudentEnrollmentMessage m: request.getCourseDemand().getEnrollmentMessages()) {
		        		if (message == null || message.getOrder() < m.getOrder() || (message.getOrder() == m.getOrder() && message.getTimestamp().before(m.getTimestamp()))) {
		        			message = m;
		        		}
		        	}
		        	if (message != null)
		        		e.setEnrollmentMessage(message.getMessage());
				}
				if (request.getOverrideStatus() != null) {
					switch (request.getCourseRequestOverrideStatus()) {
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
				if (student.getOverrideStatus() != null && student.getMaxCreditOverrideIntent() == CourseRequestOverrideIntent.WAITLIST) {
					switch (student.getMaxCreditOverrideStatus()) {
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
			CourseRequest alt = null;
			for (CourseRequest r: request.getCourseDemand().getCourseRequests()) {
				if (alt == null || alt.getOrder() < r.getOrder()) alt = r;
			}
			if (alt != null && alt.getOrder() < request.getOrder())
				e.setAlternative(alt.getCourseOffering().getCourseName());
			if (request.getCourseDemand().isAlternative()) {
				alt = null;
				demands: for (CourseDemand demand: student.getCourseDemands()) {
					if (!demand.getCourseRequests().isEmpty() && !demand.isAlternative() && !demand.effectiveWaitList() && !demand.effectiveNoSub()) {
						for (CourseRequest r: demand.getCourseRequests()) {
							if (!r.getClassEnrollments().isEmpty()) continue demands;
						}
						for (CourseRequest r: demand.getCourseRequests()) {
							if (alt == null || demand.getPriority() < alt.getCourseDemand().getPriority() || (demand.getPriority() == alt.getCourseDemand().getPriority() && r.getOrder() < alt.getOrder()))
								alt = r;
						}
					}
				}
				if (alt != null)
					e.setAlternative(alt.getCourseOffering().getCourseName());
			}
			if (request.getCourseDemand().getTimestamp() != null)
				e.setRequestedDate(request.getCourseDemand().getTimestamp());
			if (request.getCourseDemand().getWaitlistedTimeStamp() != null && e.isWaitList())
				e.setWaitListedDate(request.getCourseDemand().getWaitlistedTimeStamp());
			if (student.isEnrolled(request.getCourseDemand().getWaitListSwapWithCourseOffering()))
				e.setWaitListedReplacement(request.getCourseDemand().getWaitListSwapWithCourseOffering().getCourseName());
			e.setCritical(request.getCourseDemand().getEffectiveCritical().ordinal());
			if (!crm.enrollment().isEmpty()) {
				if (crm.reservation() != null) {
					if (crm.reservation() instanceof IndividualReservation)
						e.setReservation(MSG.reservationIndividual());
					else if (crm.reservation() instanceof LearningCommunityReservation)
						e.setReservation(MSG.reservationLearningCommunity());
					else if (crm.reservation() instanceof StudentGroupReservation)
						e.setReservation(MSG.reservationGroup());
					else if (crm.reservation() instanceof CourseReservation)
						e.setReservation(MSG.reservationCourse());
					else if (crm.reservation() instanceof CurriculumReservation)
						e.setReservation(MSG.reservationCurriculum());
					else if (crm.reservation() instanceof UniversalOverrideReservation)
						e.setReservation(MSG.reservationUniversal());
				}
				for (StudentClassEnrollment x: crm.enrollment()) {
					if (x.getTimestamp() != null) {
						if (e.getEnrolledDate() == null)
							e.setEnrolledDate(x.getTimestamp());
						else if (x.getTimestamp().after(e.getEnrolledDate()))
							e.setEnrolledDate(x.getTimestamp());
					}
				}
				if (crm.approval() != null) {
					for (StudentClassEnrollment x: crm.enrollment()) {
						if (x.getApprovedDate() != null) {
							if (x.getApprovedDate() == null) {
								e.setApprovedDate(x.getApprovedDate());
								e.setApprovedBy(x.getApprovedBy());
							} else if (x.getApprovedDate().after(e.getApprovedDate())) {
								e.setApprovedDate(x.getApprovedDate());
								e.setApprovedBy(x.getApprovedBy());
							}
						}
					}
				}
				
				for (StudentClassEnrollment enrollment: crm.enrollment()) {
					Class_ section = enrollment.getClazz();
					SchedulingSubpart subpart = section.getSchedulingSubpart();
					
					ClassAssignmentInterface.ClassAssignment a = e.getCourse().addClassAssignment();
					a.setAlternative(request.getCourseDemand().isAlternative());
					a.setClassId(section.getUniqueId());
					a.setSubpart(subpart.getItype().getAbbv().trim());
					if (subpart.getInstrOfferingConfig().getInstructionalMethod() != null)
						a.setSubpart(a.getSubpart() + " (" + subpart.getInstrOfferingConfig().getInstructionalMethod().getLabel() + ")");
					a.setClassNumber(section.getSectionNumberString());
					a.setSection(section.getClassSuffix(course));
					a.setExternalId(section.getExternalId(course));
					a.setCancelled(section.isCancelled());
					a.setLimit(new int[] { section.getEnrollment(), section.getSectioningLimit()});
					Assignment assignment = section.getCommittedAssignment();
					if (assignment != null) {
						for (DayCode d : DayCode.toDayCodes(assignment.getDays()))
							a.addDay(d.getIndex());
						a.setStart(assignment.getStartSlot());
						a.setLength(assignment.getSlotPerMtg());
						a.setBreakTime(assignment.getBreakTime());
						a.setDatePattern(assignment.getDatePattern().getName());
					}
					if (assignment != null && !assignment.getRooms().isEmpty()) {
						for (Location rm: assignment.getRooms()) {
							a.addRoom(rm.getUniqueId(), rm.getLabelWithDisplayName());
						}
					}
					if (section.isDisplayInstructor() && !section.getClassInstructors().isEmpty()) {
						for (ClassInstructor instructor: section.getClassInstructors()) {
							a.addInstructor(helper.getInstructorNameFormat().format(instructor.getInstructor()));
							a.addInstructoEmail(instructor.getInstructor().getEmail());
						}
					}
					if (section.getParentClass()!= null)
						a.setParentSection(section.getParentClass().getClassSuffix(course));
					a.setSubpartId(section.getSchedulingSubpart().getUniqueId());
					a.addNote(course.getScheduleBookNote());
					a.addNote(section.getSchedulePrintNote());
					if (section.getSchedulingSubpart().getCredit() != null) {
						a.setCredit(section.getSchedulingSubpart().getCredit().creditAbbv() + "|" + section.getSchedulingSubpart().getCredit().creditText());
						a.setCreditRange(section.getSchedulingSubpart().getCredit().getMinCredit(), section.getSchedulingSubpart().getCredit().getMaxCredit());
					} else if (gs != null && gs.isGradableSubpart(section.getSchedulingSubpart(), enrollment.getCourseOffering(), helper.getHibSession()) && course.getCredit() != null) {
						a.setCredit(course.getCredit().creditAbbv() + "|" + course.getCredit().creditText());
						a.setCreditRange(course.getCredit().getMinCredit(), course.getCredit().getMaxCredit());
					} else if (gs == null && section.getParentClass() != null && course.getCredit() != null) {
						a.setCredit(course.getCredit().creditAbbv() + "|" + course.getCredit().creditText());
						a.setCreditRange(course.getCredit().getMinCredit(), course.getCredit().getMaxCredit());
					}
					Float creditOverride = section.getCredit(course);
					if (creditOverride != null) a.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
					if (a.getParentSection() == null) {
						String consent = (course.getConsentType() == null ? null : course.getConsentType().getLabel());
						if (consent != null)
							a.setParentSection(consent);
					}
					a.setExpected(overExp.getExpected(section.getSectioningLimit(), section.getSectioningInfo() == null ? 0.0 : section.getSectioningInfo().getNbrExpectedStudents()));
				}
			}
			ret.add(e);
		}
		return ret;
	}

}
