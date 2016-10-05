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
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseRequestsHolder;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
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
	
	public GetRequest forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
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
			CourseRequestInterface.Request lastRequest = null;
			int lastRequestPriority = -1;
			boolean setReadOnly = ApplicationProperty.OnlineSchedulingMakeAssignedRequestReadOnly.isTrue();
			if (!setReadOnly && helper.getUser() != null && helper.getUser().getType() == OnlineSectioningLog.Entity.EntityType.MANAGER)
				setReadOnly = ApplicationProperty.OnlineSchedulingMakeAssignedRequestReadOnlyIfAdmin.isTrue();
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
					}
				} else if (cd instanceof XCourseRequest) {
					r = new CourseRequestInterface.Request();
					for (XCourseId courseId: ((XCourseRequest)cd).getCourseIds()) {
						XCourse c = server.getCourse(courseId.getCourseId());
						if (c == null) continue;
						RequestedCourse rc = new RequestedCourse();
						rc.setCourseId(c.getCourseId());
						rc.setCourseName(c.getSubjectArea() + " " + c.getCourseNumber() + (c.hasUniqueName() && !CONSTANTS.showCourseTitle() ? "" : " - " + c.getTitle()));
						if (setReadOnly && ((XCourseRequest)cd).getEnrollment() != null && c.getCourseId().equals(((XCourseRequest)cd).getEnrollment().getCourseId()))
							rc.setReadOnly(true);
						r.addRequestedCourse(rc);
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
