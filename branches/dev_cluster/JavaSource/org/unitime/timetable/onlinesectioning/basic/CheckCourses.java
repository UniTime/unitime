/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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

import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Student;

import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.CourseInfoMatcher;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

public class CheckCourses implements OnlineSectioningAction<Collection<String>> {
	private static final long serialVersionUID = 1L;
	private CourseRequestInterface iRequest;
	private CourseInfoMatcher iMatcher;
	
	public CheckCourses(CourseRequestInterface request, CourseInfoMatcher matcher) {
		iRequest = request; iMatcher = matcher;
	}

	@Override
	public Collection<String> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		ArrayList<String> notFound = new ArrayList<String>();
		Lock lock = (iRequest.getStudentId() == null ? null : server.lockStudent(iRequest.getStudentId(), null, true));
		try {
			Student student = (iRequest.getStudentId() == null ? null : server.getStudent(iRequest.getStudentId()));
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
		} finally {
			if (lock != null)
				lock.release();
		}
	}
	
	public CourseInfo lookup(OnlineSectioningServer server, Student student, String course) {
		CourseInfo c = server.getCourseInfo(course);
		if (c != null && iMatcher != null && !iMatcher.match(c)) {
			if (student != null) {
				for (Request r: student.getRequests())
					if (r instanceof CourseRequest) {
						for (Course x: ((CourseRequest)r).getCourses()) {
							if (x.getId() == c.getUniqueId()) return c; // already requested
						}
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
