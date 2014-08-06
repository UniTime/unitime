/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.match.AnyCourseMatcher;
import org.unitime.timetable.onlinesectioning.match.AnyStudentMatcher;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class CheckEnrollmentConsistencyAction implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.readLock();
		List<String> problems = new ArrayList<String>();
		try {
			for (XCourseId courseId: server.findCourses(new AnyCourseMatcher())) {
				Collection<XCourseRequest> requests = server.getRequests(courseId.getOfferingId());
				if (requests != null)
					for (XCourseRequest request: requests) {
						XStudent student = server.getStudent(request.getStudentId());
						if (!student.getRequests().contains(request)) {
							helper.error("Student " + student + " is missing request " + request);
							problems.add("Student " + student + " is missing request " + request);
						}
					}
			}
			for (XStudentId studentId: server.findStudents(new AnyStudentMatcher())) {
				XStudent student = server.getStudent(studentId.getStudentId());
				for (XRequest request: student.getRequests())
					if (request instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)request;
						for (XCourseId course: cr.getCourseIds()) {
							Collection<XCourseRequest> requests = server.getRequests(course.getOfferingId());
							if (requests == null || !requests.contains(cr)) {
								helper.error("Offering " + course + " is missing request " + request + " from " + student);
								problems.add("Offering " + course + " is missing request " + request + " from " + student);
							}
						}
					}
			}
		} finally {
			lock.release();
		}
		if (!problems.isEmpty())
			throw new SectioningException("Consistency check failed: " + ToolBox.col2string(problems, 2));
		return true;
	}

	@Override
	public String name() {
		return "check-consistency";
	}

}
