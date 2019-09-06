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

public class TestCriticalCourseProvider implements CriticalCoursesProvider {
	CriticalCoursesProvider iFile, iDGW;
	Query iStudentQuery;
	
	public TestCriticalCourseProvider() throws ServletException, IOException {
		iFile = new CriticalCoursesFile();
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
		return iFile.getCriticalCourses(server, helper, student, action);
	}

	@Override
	public void dispose() {
		iFile.dispose(); iDGW.dispose();
	}

}
