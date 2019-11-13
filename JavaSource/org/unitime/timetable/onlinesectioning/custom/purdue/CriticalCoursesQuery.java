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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.Builder;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseLookupHolder;
import org.unitime.timetable.onlinesectioning.model.XAreaClassificationMajor;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;

/**
 * @author Tomas Muller
 */
public class CriticalCoursesQuery implements CriticalCoursesProvider {
	private static Logger sLog = Logger.getLogger(CriticalCoursesFile.class);
	private ExternalTermProvider iExternalTermProvider;
	
	public CriticalCoursesQuery() throws ServletException, IOException {
		try {
			String clazz = ApplicationProperty.CustomizationExternalTerm.value();
			if (clazz == null || clazz.isEmpty())
				iExternalTermProvider = new BannerTermProvider();
			else
				iExternalTermProvider = (ExternalTermProvider)Class.forName(clazz).getConstructor().newInstance();
		} catch (Exception e) {
			sLog.error("Failed to create external term provider, using the default one instead.", e);
			iExternalTermProvider = new BannerTermProvider();
		}
	}
	
	public String getCatalogYear(OnlineSectioningServer server) {
		return iExternalTermProvider.getExternalTerm(server.getAcademicSession()).substring(0, 4) + "10";
	}
	
	protected String getCriticalCoursesSQL() {
		return ApplicationProperties.getProperty("banner.dgw.criticalCoursesSQL",
				"select (course_discipline || ' ' || course_number) as course from timetable.tmpl_course_view where " +
				"is_critical='Y' and tmpl_description not like '%Statewide%' and tmpl_college=:area and tmpl_major=:major and tmpl_catyear=:catyear"
				);
	}
	
	protected String getCriticalPlaceholdersSQL() {
		return ApplicationProperties.getProperty("banner.dgw.criticalPlaceholdersSQL",
				"select placeholder_value from timetable.tmpl_placeholder_view where " +
				"placeholder_value like '%*' and placeholder_type = 'UNIV-CORE' and tmpl_description not like '%Statewide%' and tmpl_college=:area and tmpl_major=:major and tmpl_catyear=:catyear"
				);
	}
	
	protected boolean getCriticalPlaceHolderAllowPartialMatch() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.dgw.placeHolderPartialMatch", "true"));
	}
	
	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId student) {
		return getCriticalCourses(server, helper, student, helper.getAction());
	}

	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId studentId, Builder action) {
		XStudent student = (studentId instanceof XStudent ? (XStudent)studentId : server.getStudent(studentId.getStudentId()));
		if (student == null) return null;
		String catyear = getCatalogYear(server);
		String sqlCourses = getCriticalCoursesSQL();
		String sqlPlaceholders = getCriticalPlaceholdersSQL();
		CriticalCoursesImpl cc = new CriticalCoursesImpl();
		for (XAreaClassificationMajor acm: student.getMajors()) {
			org.hibernate.Query query = helper.getHibSession().createSQLQuery(sqlCourses);
			query.setString("area", acm.getArea());
			if (action != null) action.addOptionBuilder().setKey("area").setValue(acm.getArea());
			query.setString("major", acm.getMajor());
			if (action != null) action.addOptionBuilder().setKey("major").setValue(acm.getMajor());
			if (sqlCourses.contains(":catyear")) {
				query.setString("catyear", catyear);
				if (action != null) action.addOptionBuilder().setKey("catyear").setValue(catyear);
			}
			cc.addCourses((List<String>)query.list());
			if (action != null) action.addOptionBuilder().setKey("courses").setValue(cc.toString());
			if (sqlPlaceholders != null && !sqlPlaceholders.isEmpty() && CustomCourseLookupHolder.hasProvider()) {
				query = helper.getHibSession().createSQLQuery(sqlPlaceholders);
				query.setString("area", acm.getArea());
				query.setString("major", acm.getMajor());
				if (sqlPlaceholders.contains(":catyear"))
					query.setString("catyear", catyear);
				List<String> placeholders = (List<String>)query.list();
				if (placeholders != null && !placeholders.isEmpty()) {
					for (String ph: placeholders) {
						cc.addCourseIds(CustomCourseLookupHolder.getProvider().getCourseIds(server.getAcademicSession(), helper.getHibSession(), ph, getCriticalPlaceHolderAllowPartialMatch()));
					}
				}
				if (action != null) action.addOptionBuilder().setKey("placeholders").setValue(placeholders.toString());
			}
		}
		return cc;
	}

	@Override
	public void dispose() {
	}
	
	protected static class CriticalCoursesImpl implements CriticalCourses {
		private Set<String> iCriticalCourses = new TreeSet<String>();
		private Set<Long> iCourseIds = new HashSet<Long>();
		
		public void addCourses(Collection<String> courses) {
			if (courses != null && !courses.isEmpty())
				iCriticalCourses.addAll(courses);
		}
		public void addCourseIds(Collection<Long> courseIds) {
			if (courseIds != null && !courseIds.isEmpty())
				iCourseIds.addAll(courseIds);
		}
		
		@Override
		public boolean isEmpty() { return iCriticalCourses.isEmpty() && iCourseIds.isEmpty(); }

		@Override
		public boolean isCritical(CourseOffering course) {
			if (iCourseIds.contains(course.getUniqueId())) return true;
			for (String c: iCriticalCourses)
				if (course.getCourseName().startsWith(c)) return true;
			return false;
		}
		
		@Override
		public String toString() {
			return iCriticalCourses.toString();
		}
	}
}