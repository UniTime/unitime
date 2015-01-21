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
