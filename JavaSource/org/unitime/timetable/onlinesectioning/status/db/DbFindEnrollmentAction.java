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

import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseReservation;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.status.FindEnrollmentAction;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentInfoAction.DbCourseRequestMatcher;

/**
 * @author Tomas Muller
 */
public class DbFindEnrollmentAction extends FindEnrollmentAction {
	private static final long serialVersionUID = 1L;

	@Override
	public List<ClassAssignmentInterface.Enrollment> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (iFilter == null) return super.execute(server, helper);
		
		List<ClassAssignmentInterface.Enrollment> ret = new ArrayList<ClassAssignmentInterface.Enrollment>();
		
		AcademicSessionInfo session = server.getAcademicSession();
		OverExpectedCriterion overExp = server.getOverExpectedCriterion();
		
		CourseOffering course = CourseOfferingDAO.getInstance().get(courseId(), helper.getHibSession());
		if (course == null) return ret;
		InstructionalOffering offering = course.getInstructionalOffering();
		if (offering == null) return ret;
		
		for (CourseRequest request: (List<CourseRequest>)helper.getHibSession().createQuery(
				"from CourseRequest where courseOffering.uniqueId = :courseId"
				).setLong("courseId", course.getUniqueId()).setCacheable(true).list()) {
			DbCourseRequestMatcher crm = new DbCourseRequestMatcher(session, request, isConsentToDoCourse(), helper.getStudentNameFormat());
			if (classId() != null) {
				boolean match = false;
				for (StudentClassEnrollment e: crm.enrollment()) {
					if (e.getClazz().getUniqueId().equals(classId())) { match = true; break; }
				}
				if (!match) continue;
			}
			if (!query().match(crm)) continue;
			if (crm.enrollment().isEmpty() && !crm.canAssign()) continue;
			
			Student student = request.getCourseDemand().getStudent();
			
			ClassAssignmentInterface.Student st = new ClassAssignmentInterface.Student();
			st.setId(student.getUniqueId());
			st.setSessionId(session.getUniqueId());
			st.setExternalId(student.getExternalUniqueId());
			st.setCanShowExternalId(iCanShowExtIds);
			st.setCanRegister(iCanRegister);
			st.setCanUseAssistant(iCanUseAssistant);
			st.setName(helper.getStudentNameFormat().format(student));
			for (AcademicAreaClassification ac: student.getAcademicAreaClassifications()) {
				st.addArea(ac.getAcademicArea().getAcademicAreaAbbreviation());
				st.addClassification(ac.getAcademicClassification().getCode());
			}
			for (PosMajor mj: student.getPosMajors()) {
				st.addMajor(mj.getCode());
			}
			for (StudentAccomodation acc: student.getAccomodations()) {
				st.addAccommodation(acc.getAbbreviation());
			}
			for (StudentGroup gr: student.getGroups()) {
				st.addGroup(gr.getGroupAbbreviation());
			}
			
			ClassAssignmentInterface.Enrollment e = new ClassAssignmentInterface.Enrollment();
			e.setStudent(st);
			e.setPriority(1 + request.getCourseDemand().getPriority());
			CourseAssignment c = new CourseAssignment();
			c.setCourseId(course.getUniqueId());
			c.setSubject(course.getSubjectAreaAbbv());
			c.setCourseNbr(course.getCourseNbr());
			c.setTitle(course.getTitle());
			e.setCourse(c);
			e.setWaitList(request.getCourseDemand().isWaitlist());
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
					if (!demand.getCourseRequests().isEmpty() && !demand.isAlternative() && !demand.isWaitlist()) {
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
			if (!crm.enrollment().isEmpty()) {
				if (crm.reservation() != null) {
					if (crm.reservation() instanceof IndividualReservation)
						e.setReservation(MSG.reservationIndividual());
					else if (crm.reservation() instanceof StudentGroupReservation)
						e.setReservation(MSG.reservationGroup());
					else if (crm.reservation() instanceof CourseReservation)
						e.setReservation(MSG.reservationCourse());
					else if (crm.reservation() instanceof CurriculumReservation)
						e.setReservation(MSG.reservationCurriculum());
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
							a.addRoom(rm.getLabel());
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
					} else if (section.getParentClass() != null && course.getCredit() != null) {
						a.setCredit(course.getCredit().creditAbbv() + "|" + course.getCredit().creditText());
					}
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
