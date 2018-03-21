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
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CourseMessage;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseRequestsValidationHolder;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class CheckCourses implements OnlineSectioningAction<CheckCoursesResponse> {
	private static final long serialVersionUID = 1L;
	protected static final StudentSectioningMessages MESSAGES = Localization.create(StudentSectioningMessages.class);
	private CourseRequestInterface iRequest;
	private CourseMatcher iMatcher;
	private boolean iCustomValidation = false;
	
	public CheckCourses forRequest(CourseRequestInterface request) {
		iRequest = request; return this;
	}
	
	public CheckCourses withMatcher(CourseMatcher matcher) {
		iMatcher = matcher; return this;
	}
	
	public CheckCourses withCustomValidation(boolean validation) {
		iCustomValidation = validation; return this;
	}

	@Override
	public CheckCoursesResponse execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		OnlineSectioningLog.Action.Builder action = helper.getAction();
		if (iMatcher != null) iMatcher.setServer(server);
		CheckCoursesResponse response = new CheckCoursesResponse();
		if (iRequest.getStudentId() != null)
			action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(iRequest.getStudentId()));
		XStudent student = (iRequest.getStudentId() == null ? null : server.getStudent(iRequest.getStudentId()));
		if (student != null) {
			action.getStudentBuilder().setExternalId(student.getExternalId());
			action.getStudentBuilder().setName(student.getName());
		}
		for (OnlineSectioningLog.Request r: OnlineSectioningHelper.toProto(iRequest))
			action.addRequest(r);
		
		for (CourseRequestInterface.Request cr: iRequest.getCourses()) {
			if (cr.hasRequestedCourse()) {
				for (RequestedCourse rc: cr.getRequestedCourse())
					if (rc.isCourse() && lookup(server, student, rc) == null) {
						response.addError(rc.getCourseId(), rc.getCourseName(), "NOT_FOUND", MESSAGES.validationCourseNotExists(rc.getCourseName()));
						response.setErrorMessage(MESSAGES.validationCourseNotExists(rc.getCourseName()));
					}
			}
		}
		for (CourseRequestInterface.Request cr: iRequest.getAlternatives()) {
			if (cr.hasRequestedCourse()) {
				for (RequestedCourse rc: cr.getRequestedCourse())
					if (rc.isCourse() && lookup(server, student, rc) == null) {
						response.addError(rc.getCourseId(), rc.getCourseName(), "NOT_FOUND", MESSAGES.validationCourseNotExists(rc.getCourseName()));
						response.setErrorMessage(MESSAGES.validationCourseNotExists(rc.getCourseName()));
					}
			}
		}
		
		if (iCustomValidation && CustomCourseRequestsValidationHolder.hasProvider())
			CustomCourseRequestsValidationHolder.getProvider().validate(server, helper, iRequest, response);
		
		if (response.hasMessages())
			for (CourseMessage m: response.getMessages())
				if (m.hasCourse())
					action.addMessageBuilder().setText(m.toString()).setLevel(m.isError() ? OnlineSectioningLog.Message.Level.ERROR : m.isConfirm() ? OnlineSectioningLog.Message.Level.WARN : OnlineSectioningLog.Message.Level.INFO);
		if (response.isError())
			action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
		else
			action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);
		
		return response;
	}
	
	public XCourseId lookup(OnlineSectioningServer server, XStudent student, RequestedCourse course) {
		XCourseId c = server.getCourse(course.getCourseId(), course.getCourseName());
		if (c != null && iMatcher != null && !iMatcher.match(c)) {
			if (student != null) {
				for (XRequest r: student.getRequests())
					if (r instanceof XCourseRequest) {
						if (((XCourseRequest)r).hasCourse(c.getCourseId()))
							return c; // already requested
					}
			}
			return null;
		}
		return c;
	}

	@Override
	public String name() {
		return "check-courses";
	}

}
