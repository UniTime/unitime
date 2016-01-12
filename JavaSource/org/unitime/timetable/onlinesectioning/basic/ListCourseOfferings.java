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
import java.util.List;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;

/**
 * @author Tomas Muller
 */
public class ListCourseOfferings implements OnlineSectioningAction<Collection<ClassAssignmentInterface.CourseAssignment>> {
	private static final long serialVersionUID = 1L;
	
	protected String iQuery = null;
	protected Integer iLimit = null;
	protected CourseMatcher iMatcher = null; 
	
	public ListCourseOfferings forQuery(String query) {
		iQuery = query; return this;
	}
	
	public ListCourseOfferings withLimit(Integer limit) {
		iLimit = limit; return this;
	}
	
	public ListCourseOfferings withMatcher(CourseMatcher matcher) {
		iMatcher = matcher; return this;
	}
	
	@Override
	public Collection<CourseAssignment> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.readLock();
		try {
			return listCourses(server, helper);
		} finally {
			lock.release();
		}
	}
	
	protected List<CourseAssignment> listCourses(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<CourseAssignment> ret = new ArrayList<CourseAssignment>();
		for (XCourseId id: server.findCourses(iQuery, iLimit, iMatcher)) {
			XCourse course = server.getCourse(id.getCourseId());
			if (course != null)
				ret.add(convert(course, server));
		}
		return ret;
	}
	
	protected CourseAssignment convert(XCourse c, OnlineSectioningServer server) {
		CourseAssignment course = new CourseAssignment();
		course.setCourseId(c.getCourseId());
		course.setSubject(c.getSubjectArea());
		course.setCourseNbr(c.getCourseNumber());
		course.setTitle(c.getTitle());
		course.setNote(c.getNote());
		course.setCreditAbbv(c.getCreditAbbv());
		course.setCreditText(c.getCreditText());
		course.setTitle(c.getTitle());
		course.setHasUniqueName(c.hasUniqueName());
		course.setLimit(c.getLimit());
		Collection<XCourseRequest> requests = server.getRequests(c.getOfferingId());
		int enrl = 0;
		if (requests != null) {
			for (XCourseRequest r: requests)
				if (r.getEnrollment() != null && r.getEnrollment().getCourseId().equals(course.getCourseId()))
					enrl ++;
		}
		course.setEnrollment(enrl);
		return course;
	}

	@Override
	public String name() {
		return "list-courses";
	}
}
