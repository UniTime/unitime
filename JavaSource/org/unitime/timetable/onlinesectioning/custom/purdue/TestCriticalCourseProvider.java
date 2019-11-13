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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.io.IOException;

import javax.servlet.ServletException;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.Builder;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;

/**
 * @author Tomas Muller
 */
public class TestCriticalCourseProvider implements CriticalCoursesProvider {
	CriticalCoursesProvider iSQL, iDGW;
	Query iStudentQuery;
	
	public TestCriticalCourseProvider() throws ServletException, IOException {
		iSQL = new CriticalCoursesQuery();
		iDGW = new DegreeWorksCourseRequests();
		iStudentQuery = new Query(ApplicationProperties.getProperty("purdue.critical.filter", "group:STAR group:VSTAR group:PREREG"));
	}

	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId student) {
		return getCriticalCourses(server, helper, student, helper.getAction());
	}
	
	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId studentId, Builder action) {
		XStudent student = (studentId instanceof XStudent ? (XStudent)studentId : server.getStudent(studentId.getStudentId()));
		if (student == null) return null;
		if (iStudentQuery.match(new StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
			CriticalCourses cc = iDGW.getCriticalCourses(server, helper, student, action);
			if (cc != null && !cc.isEmpty()) return cc;
		}
		return iSQL.getCriticalCourses(server, helper, student, action);
	}

	@Override
	public void dispose() {
		iSQL.dispose(); iDGW.dispose();
	}

}
