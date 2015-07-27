/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.onlinesectioning.basic;

import java.util.ArrayList;
import java.util.Collection;

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
	
	private String iQuery = null;
	private Integer iLimit = null;
	private CourseMatcher iMatcher = null; 
	
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
			ArrayList<ClassAssignmentInterface.CourseAssignment> ret = new ArrayList<ClassAssignmentInterface.CourseAssignment>();
			for (XCourseId id: server.findCourses(iQuery, iLimit, iMatcher)) {
				XCourse c = server.getCourse(id.getCourseId());
				if (c == null) continue;
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
				if (requests != null) {
					int enrl = 0;
					for (XCourseRequest r: requests)
						if (r.getEnrollment() != null && r.getEnrollment().getCourseId().equals(course.getCourseId()))
							enrl ++;
					course.setEnrollment(enrl);
				}
				ret.add(course);
			}
			return ret;
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "list-courses";
	}
}
