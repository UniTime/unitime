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

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseRequestsHolder;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseRequestsValidationHolder;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class GetRequest implements OnlineSectioningAction<CourseRequestInterface> {
	protected static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static final long serialVersionUID = 1L;
	
	private Long iStudentId;
	private boolean iSectioning;
	private boolean iCustomValidation = false;
	
	public GetRequest forStudent(Long studentId, boolean sectioning) {
		iStudentId = studentId;
		iSectioning = sectioning;
		return this;
	}
	
	public GetRequest forStudent(Long studentId) {
		return forStudent(studentId, true);
	}
	
	public GetRequest withCustomValidation(boolean validation) {
		iCustomValidation = validation; return this;
	}

	@Override
	public CourseRequestInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (iStudentId == null) {
			if (CustomCourseRequestsHolder.hasProvider()) {
				CourseRequestInterface request = CustomCourseRequestsHolder.getProvider().getCourseRequests(
						server, helper, new XStudent(null, helper.getStudentExternalId(), helper.getUser().getName()));
				if (request != null) return request;
			}
			throw new SectioningException(MSG.exceptionNoStudent());
		}
		Lock lock = server.readLock();
		try {
			OnlineSectioningLog.Action.Builder action = helper.getAction();
			action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(iStudentId));
			XStudent student = server.getStudent(iStudentId);
			if (student == null) return null;
			action.getStudentBuilder().setExternalId(student.getExternalId());
			action.getStudentBuilder().setName(student.getName());

			if (student.getRequests().isEmpty() && CustomCourseRequestsHolder.hasProvider()) {
				CourseRequestInterface request = CustomCourseRequestsHolder.getProvider().getCourseRequests(server, helper, student);
				if (request != null) return request;
			}
			
			CourseRequestInterface request = new CourseRequestInterface();
			request.setStudentId(iStudentId);
			request.setSaved(true);
			request.setAcademicSessionId(server.getAcademicSession().getUniqueId());
			request.setMaxCredit(student.getMaxCredit());
			if (student.getMaxCreditOverride() != null) {
				request.setMaxCreditOverride(student.getMaxCreditOverride().getValue());
				request.setMaxCreditOverrideExternalId(student.getMaxCreditOverride().getExternalId());
				request.setMaxCreditOverrideTimeStamp(student.getMaxCreditOverride().getTimeStamp());
				Integer status = student.getMaxCreditOverride().getStatus();
				if (status == null)
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_PENDING);
				else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.APPROVED.ordinal())
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_APPROVED);
				else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.REJECTED.ordinal())
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_REJECTED);
				else if (status == org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus.CANCELLED.ordinal())
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_CANCELLED);
				else
					request.setMaxCreditOverrideStatus(RequestedCourseStatus.OVERRIDE_PENDING);
			}
			CourseRequestInterface.Request lastRequest = null;
			int lastRequestPriority = -1;
			boolean setReadOnly = ApplicationProperty.OnlineSchedulingMakeAssignedRequestReadOnly.isTrue();
			boolean setReadOnlyWhenReserved = ApplicationProperty.OnlineSchedulingMakeReservedRequestReadOnly.isTrue();
			if (helper.getUser() != null && helper.getUser().getType() == OnlineSectioningLog.Entity.EntityType.MANAGER) {
				setReadOnly = ApplicationProperty.OnlineSchedulingMakeAssignedRequestReadOnlyIfAdmin.isTrue();
				setReadOnlyWhenReserved = ApplicationProperty.OnlineSchedulingMakeReservedRequestReadOnlyIfAdmin.isTrue();
			}
			boolean reservedNoPriority = ApplicationProperty.OnlineSchedulingReservedRequestNoPriorityChanges.isTrue();
			boolean reservedNoAlternatives = ApplicationProperty.OnlineSchedulingReservedRequestNoAlternativeChanges.isTrue();
			boolean enrolledNoPriority = ApplicationProperty.OnlineSchedulingAssignedRequestNoPriorityChanges.isTrue();
			boolean enrolledNoAlternatives = ApplicationProperty.OnlineSchedulingAssignedRequestNoAlternativeChanges.isTrue();
			for (XRequest cd: student.getRequests()) {
				CourseRequestInterface.Request r = null;
				if (cd instanceof XFreeTimeRequest) {
					XFreeTimeRequest ftr = (XFreeTimeRequest)cd;
					CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
					ft.setStart(ftr.getTime().getSlot());
					ft.setLength(ftr.getTime().getLength());
					for (DayCode day : DayCode.toDayCodes(ftr.getTime().getDays()))
						ft.addDay(day.getIndex());
					if (lastRequest != null && lastRequestPriority == cd.getPriority() && lastRequest.hasRequestedCourse() && lastRequest.getRequestedCourse(0).isFreeTime()) {
						lastRequest.getRequestedCourse(0).addFreeTime(ft);
					} else {
						r = new CourseRequestInterface.Request();
						RequestedCourse rc = new RequestedCourse();
						r.addRequestedCourse(rc);
						rc.addFreeTime(ft);
						if (cd.isAlternative())
							request.getAlternatives().add(r);
						else
							request.getCourses().add(r);
						lastRequest = r;
						lastRequestPriority = cd.getPriority();
						rc.setStatus(RequestedCourseStatus.SAVED);
					}
				} else if (cd instanceof XCourseRequest) {
					r = new CourseRequestInterface.Request();
					for (XCourseId courseId: ((XCourseRequest)cd).getCourseIds()) {
						XCourse c = server.getCourse(courseId.getCourseId());
						if (c == null) continue;
						RequestedCourse rc = new RequestedCourse();
						rc.setCourseId(c.getCourseId());
						rc.setCourseName(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() && !CONSTANTS.showCourseTitle() ? "" : " - " + c.getTitle()));
						rc.setCourseTitle(c.getTitle());
						rc.setCredit(c.getMinCredit(), c.getMaxCredit());
						boolean isEnrolled = ((XCourseRequest)cd).getEnrollment() != null && c.getCourseId().equals(((XCourseRequest)cd).getEnrollment().getCourseId());
						if (setReadOnly && isEnrolled)
							rc.setReadOnly(true);
						if (!iSectioning && isEnrolled) {
							rc.setReadOnly(true);
							rc.setCanDelete(false);
							if (enrolledNoAlternatives) rc.setCanChangeAlternatives(false);
							if (enrolledNoPriority) rc.setCanChangePriority(false);
						}
						if (!iSectioning && setReadOnlyWhenReserved) {
							XOffering offering = server.getOffering(c.getOfferingId());
							if (offering != null && (offering.hasIndividualReservation(student, c) || offering.hasGroupReservation(student, c))) {
								rc.setReadOnly(true);
								rc.setCanDelete(false);
								if (reservedNoAlternatives) rc.setCanChangeAlternatives(false);
								if (reservedNoPriority) rc.setCanChangePriority(false);
							}
						}
						if (isEnrolled)
							rc.setStatus(RequestedCourseStatus.ENROLLED);
						else {
							Integer status = ((XCourseRequest)cd).getOverrideStatus(courseId);
							if (status == null)
								rc.setStatus(RequestedCourseStatus.SAVED);
							else if (status == CourseRequest.CourseRequestOverrideStatus.APPROVED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_APPROVED);
							else if (status == CourseRequest.CourseRequestOverrideStatus.REJECTED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_REJECTED);
							else if (status == CourseRequest.CourseRequestOverrideStatus.CANCELLED.ordinal())
								rc.setStatus(RequestedCourseStatus.OVERRIDE_CANCELLED);
							else
								rc.setStatus(RequestedCourseStatus.OVERRIDE_PENDING);
						}
						rc.setOverrideExternalId(((XCourseRequest)cd).getOverrideExternalId(courseId));
						rc.setOverrideTimeStamp(((XCourseRequest)cd).getOverrideTimeStamp(courseId));
						((XCourseRequest)cd).fillPreferencesIn(rc, courseId);
						r.addRequestedCourse(rc);
					}
					r.setWaitList(((XCourseRequest)cd).isWaitlist());
					r.setCritical(((XCourseRequest)cd).isCritical());
					r.setTimeStamp(((XCourseRequest)cd).getTimeStamp());
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
			
			if (iCustomValidation && CustomCourseRequestsValidationHolder.hasProvider())
				CustomCourseRequestsValidationHolder.getProvider().check(server, helper, request);
			
			return request;
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "get-request";
	}
	

}
