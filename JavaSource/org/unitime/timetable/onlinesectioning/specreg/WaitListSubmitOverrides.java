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
package org.unitime.timetable.onlinesectioning.specreg;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CourseMessage;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideIntent;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.WaitListValidationProvider;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.updates.ReloadAllData;

/**
 * @author Tomas Muller
 */
public class WaitListSubmitOverrides implements OnlineSectioningAction<CourseRequestInterface>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private CourseRequestInterface iRequest = null;
	private Float iNeededCredit = null;
	
	public WaitListSubmitOverrides withRequest(CourseRequestInterface request) {
		iRequest = request;
		return this;
	}
	
	public WaitListSubmitOverrides withCredit(Float neededCredit) {
		iNeededCredit = neededCredit;
		return this;
	}
	
	public Long getStudentId() { return iRequest.getStudentId(); }
	
	public CourseRequestInterface getRequest() { return iRequest; }

	@Override
	public CourseRequestInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (!server.getAcademicSession().isSectioningEnabled() || !Customization.WaitListValidationProvider.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());

		Lock lock = server.lockStudent(getStudentId(), null, name());
		try {
			helper.beginTransaction();
			try {
				Student student = StudentDAO.getInstance().get(getStudentId(), helper.getHibSession());
				if (student == null) throw new SectioningException(MSG.exceptionBadStudentId());

				
				OnlineSectioningLog.Action.Builder action = helper.getAction();
				
				action.setStudent(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(getStudentId())
						.setExternalId(student.getExternalUniqueId())
						.setName(helper.getStudentNameFormat().format(student)));

				WaitListValidationProvider provider = Customization.WaitListValidationProvider.getProvider();
				provider.submit(server, helper, iRequest, iNeededCredit);
				
				for (CourseDemand cd: student.getCourseDemands()) {
					if (!Boolean.TRUE.equals(cd.getWaitlist()) || Boolean.TRUE.equals(cd.isAlternative()) || cd.isEnrolled()) {
						for (CourseRequest cr: cd.getCourseRequests())
							if (cr.getCourseRequestOverrideIntent() == CourseRequestOverrideIntent.WAITLIST) {
								cr.setOverrideExternalId(null);
								cr.setOverrideTimeStamp(null);
								cr.setOverrideIntent(null);
								cr.setOverrideStatus(null);
								helper.getHibSession().update(cr);
							}
					}
				}
				
				for (Request r: getRequest().getCourses()) {
					if (r.hasRequestedCourse() && r.isWaitList()) {
						for (RequestedCourse rc: r.getRequestedCourse()) {
							XCourseId course = server.getCourse(rc.getCourseId(), rc.getCourseName());
							if (course == null) continue;
							CourseRequest courseRequest = null;
							cd: for (CourseDemand cd: student.getCourseDemands()) {
								for (CourseRequest cr: cd.getCourseRequests())
									if (cr.getCourseOffering().getUniqueId().equals(course.getCourseId())) { courseRequest = cr; break cd; }
							}
							if (courseRequest != null) {
								if (RequestedCourseStatus.OVERRIDE_NEEDED == rc.getStatus()) {
									courseRequest.setOverrideExternalId(null);
									courseRequest.setOverrideTimeStamp(null);
									courseRequest.setCourseRequestOverrideIntent(null);
									courseRequest.setCourseRequestOverrideStatus(null);
								} else {
									courseRequest.setOverrideExternalId(rc.getOverrideExternalId());
									courseRequest.setOverrideTimeStamp(rc.getOverrideTimeStamp());
									courseRequest.setCourseRequestOverrideIntent(CourseRequestOverrideIntent.WAITLIST);
									courseRequest.setCourseRequestOverrideStatus(
										RequestedCourseStatus.OVERRIDE_APPROVED == rc.getStatus() ? CourseRequestOverrideStatus.APPROVED :
										RequestedCourseStatus.OVERRIDE_PENDING == rc.getStatus() ? CourseRequestOverrideStatus.PENDING :
										RequestedCourseStatus.OVERRIDE_CANCELLED == rc.getStatus() ? CourseRequestOverrideStatus.CANCELLED :
										RequestedCourseStatus.OVERRIDE_REJECTED == rc.getStatus() ? CourseRequestOverrideStatus.REJECTED : null);
								}
								helper.getHibSession().update(courseRequest);
							}
						}
					}
				}

				student.setOverrideExternalId(getRequest().getMaxCreditOverrideExternalId());
				student.setOverrideTimeStamp(getRequest().getMaxCreditOverrideTimeStamp());
				student.setMaxCreditOverrideStatus(
					RequestedCourseStatus.OVERRIDE_APPROVED == getRequest().getMaxCreditOverrideStatus() ? CourseRequestOverrideStatus.APPROVED :
					RequestedCourseStatus.OVERRIDE_PENDING == getRequest().getMaxCreditOverrideStatus() ? CourseRequestOverrideStatus.PENDING :
					RequestedCourseStatus.OVERRIDE_CANCELLED == getRequest().getMaxCreditOverrideStatus() ? CourseRequestOverrideStatus.CANCELLED :
					RequestedCourseStatus.OVERRIDE_REJECTED == getRequest().getMaxCreditOverrideStatus() ? CourseRequestOverrideStatus.REJECTED : null);
				student.setOverrideMaxCredit(getRequest().getMaxCreditOverride());				
				helper.getHibSession().update(student);
				
				// Reload student
				XStudent newStudent = null;
				try {
					newStudent = ReloadAllData.loadStudentNoCheck(student, server, helper);
					server.update(newStudent, true);
					
					for (XRequest r: newStudent.getRequests())
						if (r instanceof XCourseRequest) {
							XCourseRequest cr = (XCourseRequest)r;
							if (cr.getEnrollment() == null && cr.isWaitlist() && !cr.isAlternative())
								action.addRequest(OnlineSectioningHelper.toProto(r));
						}
						
				} catch (Exception e) {
					if (e instanceof RuntimeException)
						throw (RuntimeException)e;
					throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
				}
				
				if (getRequest().hasConfirmations())
					for (CourseMessage m: getRequest().getConfirmations())
						if (m.hasCourse())
							action.addMessageBuilder().setText(m.toString()).setLevel(m.isError() ? OnlineSectioningLog.Message.Level.ERROR : m.isConfirm() ? OnlineSectioningLog.Message.Level.WARN : OnlineSectioningLog.Message.Level.INFO);
				if (getRequest().hasCreditNote())
					action.addMessageBuilder().setText(getRequest().getCreditNote()).setLevel(OnlineSectioningLog.Message.Level.INFO);
				if (getRequest().hasCreditWarning())
					action.addMessageBuilder().setText(getRequest().getCreditWarning()).setLevel(OnlineSectioningLog.Message.Level.WARN);
				if (getRequest().hasErrorMessage())
					action.addMessageBuilder().setText(getRequest().getErrorMessaeg()).setLevel(OnlineSectioningLog.Message.Level.ERROR);
				
				return iRequest;
			} catch (Exception e) {
				helper.error("Failed to save wait-lists: " + e.getMessage(), e);
				helper.rollbackTransaction();
				if (e instanceof SectioningException) throw (SectioningException)e;
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
		} finally {
			lock.release();
		}
	}
	
	@Override
	public String name() {
		return "wait-submit";
	}
}
