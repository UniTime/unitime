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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.Builder;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider;
import org.unitime.timetable.onlinesectioning.model.XAdvisorRequest;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;

public class AdvisorCriticalCourses implements CriticalCoursesProvider {
	private CriticalCoursesProvider iParent = null;
	private static Log sLog = LogFactory.getLog(AdvisorCriticalCourses.class);
	
	public AdvisorCriticalCourses() throws ServletException, IOException {
		String fallback = ApplicationProperties.getProperty("purdue.advisorCriticalCourses.fallback");
		if (fallback != null && !fallback.isEmpty()) {
			try {
				iParent = (CriticalCoursesProvider)Class.forName(fallback).getConstructor().newInstance(); 
			} catch (Exception e) {
				sLog.error("Failed to create critical courses fallback.", e);
			}
		}
	}
	
	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId studentId) {
		return getCriticalCourses(server, helper, studentId, helper.getAction());
	}

	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId studentId, Builder action) {
		XStudent student = (studentId instanceof XStudent ? (XStudent)studentId: server.getStudent(studentId.getStudentId()));
		if (student == null) return (iParent == null ? null : iParent.getCriticalCourses(server, helper, student, action));
		
		CriticalCoursesImpl cc = new CriticalCoursesImpl(iParent == null ? null : iParent.getCriticalCourses(server, helper, student, action));
		if (student.hasAdvisorRequests()) {
			boolean checkAlts = "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.advisorCriticalCourses.checkAlts", "true"));
			boolean altsAreImportant = "true".equalsIgnoreCase(ApplicationProperties.getProperty("purdue.advisorCriticalCourses.altsAreImportant", "false"));
			for (XAdvisorRequest ar: student.getAdvisorRequests()) {
				if (ar.isNoSub() && !ar.isSubstitute() && ar.hasCourseId() && ar.getAlternative() == 0) {
					cc.addCritical(ar.getCourseId());
					if (checkAlts) for (XAdvisorRequest alt: student.getAdvisorRequests()) {
						if (alt.getPriority() == ar.getPriority() && alt.getAlternative() > 0 && !alt.isSubstitute()) {
							if (altsAreImportant)
								cc.addImportant(alt.getCourseId());
							else
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
		if (iParent != null) iParent.dispose();
	}
	
	protected static class CriticalCoursesImpl implements CriticalCourses, CriticalCoursesProvider.AdvisorCriticalCourses {
		private CriticalCourses iParent = null;
		private Map<Long, String> iCriticalCourses = new HashMap<Long, String>();
		private Map<Long, String> iImportantCourses = new HashMap<Long, String>();
		
		CriticalCoursesImpl(CriticalCourses parent) {
			iParent = parent;
		}
		CriticalCoursesImpl() {
			this(null);
		}
		
		public boolean addCritical(XCourseId course) { return iCriticalCourses.put(course.getCourseId(), course.getCourseName()) != null; }
		public boolean addImportant(XCourseId course) { return iImportantCourses.put(course.getCourseId(), course.getCourseName()) != null; }
		
		@Override
		public boolean isEmpty() { return iCriticalCourses.isEmpty() && (iParent == null || iParent.isEmpty()); }

		@Override
		public int isCritical(CourseOffering course) {
			if (iCriticalCourses.containsKey(course.getUniqueId())) {
				return CourseDemand.Critical.CRITICAL.ordinal();
			}
			if (iImportantCourses.containsKey(course.getUniqueId())) {
				if (iParent != null && iParent.isCritical(course) == CourseDemand.Critical.CRITICAL.ordinal())
					return CourseDemand.Critical.CRITICAL.ordinal();
				return CourseDemand.Critical.IMPORTANT.ordinal();
			}
			if (iParent != null) return iParent.isCritical(course);
			return CourseDemand.Critical.NORMAL.ordinal();
		}

		@Override
		public int isCritical(XCourseId course) {
			if (iCriticalCourses.containsKey(course.getCourseId())) {
				return CourseDemand.Critical.CRITICAL.ordinal();
			}
			if (iImportantCourses.containsKey(course.getCourseId())) {
				if (iParent != null && iParent.isCritical(course) == CourseDemand.Critical.CRITICAL.ordinal())
					return CourseDemand.Critical.CRITICAL.ordinal();
				return CourseDemand.Critical.IMPORTANT.ordinal();
			}
			if (iParent != null) return iParent.isCritical(course);
			return CourseDemand.Critical.NORMAL.ordinal();
		}
		
		@Override
		public String toString() {
			Set<String> courses = new TreeSet<String>(iCriticalCourses.values());
			return courses.toString();
		}

		@Override
		public int isCritical(AdvisorCourseRequest request) {
			if (request.getCourseOffering() == null || request.isSubstitute()) return CourseDemand.Critical.NORMAL.ordinal();
			if (Boolean.TRUE.equals(request.isNoSub())) {
				if (request.getAlternative() == 0) {
					return CourseDemand.Critical.CRITICAL.ordinal();
				} else {
					if (iParent != null && iParent.isCritical(request.getCourseOffering()) == CourseDemand.Critical.CRITICAL.ordinal())
						return CourseDemand.Critical.CRITICAL.ordinal();
					return CourseDemand.Critical.IMPORTANT.ordinal();
				}
			}
			if (iParent != null) return iParent.isCritical(request.getCourseOffering());
			return CourseDemand.Critical.NORMAL.ordinal();
		}
	}

}
