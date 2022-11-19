package org.unitime.timetable.onlinesectioning.custom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.Builder;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XAdvisorRequest;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;

public class DefaultCriticalCourses implements CriticalCoursesProvider {
	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId studentId) {
		return getCriticalCourses(server, helper, studentId, helper.getAction());
	}

	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId studentId, Builder action) {
		XStudent student = (studentId instanceof XStudent ? (XStudent)studentId: server.getStudent(studentId.getStudentId()));
		if (student == null) return null;
		
		CourseDemand.Critical critical = CourseDemand.Critical.fromText(ApplicationProperty.AdvisorCourseRequestsAllowCritical.valueOfSession(server.getAcademicSession().getUniqueId()));
		CriticalCoursesImpl cc = new CriticalCoursesImpl(critical);
		if (student.hasAdvisorRequests()) {
			for (XAdvisorRequest ar: student.getAdvisorRequests()) {
				if (ar.isCritical() && !ar.isSubstitute() && ar.hasCourseId() && ar.getAlternative() == 0) {
					cc.addCritical(ar.getCourseId());
					for (XAdvisorRequest alt: student.getAdvisorRequests()) {
						if (alt.getPriority() == ar.getPriority() && alt.getAlternative() > 0 && !alt.isSubstitute() && alt.hasCourseId()) {
							cc.addCritical(alt.getCourseId());
						}
					}
				}
			}
		}
		return cc;
	}

	@Override
	public void dispose() {
	}
	
	protected static class CriticalCoursesImpl implements CriticalCourses, CriticalCoursesProvider.AdvisorCriticalCourses {
		private CourseDemand.Critical iCritical = null;
		private Map<Long, String> iCriticalCourses = new HashMap<Long, String>();
		
		CriticalCoursesImpl(CourseDemand.Critical critical) {
			iCritical = critical;
		}
		public boolean addCritical(XCourseId course) { return iCriticalCourses.put(course.getCourseId(), course.getCourseName()) != null; }
		
		@Override
		public boolean isEmpty() { return iCriticalCourses.isEmpty(); }
		
		@Override
		public int isCritical(CourseOffering course) {
			if (iCriticalCourses.containsKey(course.getUniqueId()))
				return iCritical.ordinal();
			return CourseDemand.Critical.NORMAL.ordinal();
		}

		@Override
		public int isCritical(XCourseId course) {
			if (iCriticalCourses.containsKey(course.getCourseId()))
				return iCritical.ordinal();
			return CourseDemand.Critical.NORMAL.ordinal();
		}
		
		@Override
		public String toString() {
			Set<String> courses = new TreeSet<String>(iCriticalCourses.values());
			return courses.toString();
		}

		@Override
		public int isCritical(AdvisorCourseRequest request) {
			return request.getEffectiveCritical().ordinal();
		}
	}

}
