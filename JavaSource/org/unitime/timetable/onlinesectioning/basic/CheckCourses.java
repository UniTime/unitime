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

import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class CheckCourses implements OnlineSectioningAction<Collection<String>> {
	private static final long serialVersionUID = 1L;
	private CourseRequestInterface iRequest;
	private CourseMatcher iMatcher;
	
	public CheckCourses forRequest(CourseRequestInterface request) {
		iRequest = request; return this;
	}
	
	public CheckCourses withMatcher(CourseMatcher matcher) {
		iMatcher = matcher; return this;
	}

	@Override
	public Collection<String> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (iMatcher != null) iMatcher.setServer(server);
		ArrayList<String> notFound = new ArrayList<String>();
		XStudent student = (iRequest.getStudentId() == null ? null : server.getStudent(iRequest.getStudentId()));
		for (CourseRequestInterface.Request cr: iRequest.getCourses()) {
			if (!cr.hasRequestedFreeTime() && cr.hasRequestedCourse() && lookup(server, student, cr.getRequestedCourse()) == null)
				notFound.add(cr.getRequestedCourse());
			if (cr.hasFirstAlternative() && lookup(server, student, cr.getFirstAlternative()) == null)
				notFound.add(cr.getFirstAlternative());
			if (cr.hasSecondAlternative() && lookup(server, student, cr.getSecondAlternative()) == null)
				notFound.add(cr.getSecondAlternative());
		}
		for (CourseRequestInterface.Request cr: iRequest.getAlternatives()) {
			if (cr.hasRequestedCourse() && lookup(server, student, cr.getRequestedCourse()) == null)
				notFound.add(cr.getRequestedCourse());
			if (cr.hasFirstAlternative() && lookup(server, student, cr.getFirstAlternative()) == null)
				notFound.add(cr.getFirstAlternative());
			if (cr.hasSecondAlternative() && lookup(server, student, cr.getSecondAlternative()) == null)
				notFound.add(cr.getSecondAlternative());
		}
		return notFound;
	}
	
	public XCourseId lookup(OnlineSectioningServer server, XStudent student, String course) {
		XCourseId c = server.getCourse(course);
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
