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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.type.BigDecimalType;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseLookup;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;

/**
 * @author Tomas Muller
 */
public class UCCCoursesLookup implements CustomCourseLookup {
	private static Logger sLog = Logger.getLogger(UCCCoursesLookup.class);
	
	private ExternalTermProvider iExternalTermProvider;
	
	public UCCCoursesLookup() {
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
	
	protected String getBannerTerm(AcademicSessionInfo session) {
		return iExternalTermProvider.getExternalTerm(session);
	}
	
	protected String getCourseLookupSQL() {
		return ApplicationProperties.getProperty("banner.ucc.lookupSQL",
				"select co.uniqueid " +
				"from timetable.szv_utm_attr a, course_offering co, instructional_offering io, subject_area sa " +
				"where a.term_start <= :term and a.term_end >= :term and " +
				"(lower(a.attribute_description) like concat(concat('uc-', :query), '%') or lower(a.attribute_description) like concat(concat('% ', :query), '%') or lower(a.course_attribute) = :query) and "+
				"co.instr_offr_id = io.uniqueid and co.subject_area_id = sa.uniqueid and io.session_id = :sessionId and " +
				"io.not_offered = 0 and sa.subject_area_abbreviation = a.subject and co.course_nbr like concat(a.course_number, '%') " +
				"order by sa.subject_area_abbreviation, co.course_nbr");
	}

	@Override
	public List<XCourseId> getCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, String query) {
		if ("oc".equalsIgnoreCase(query)) query = "oral communication";
		if ("wc".equalsIgnoreCase(query)) query = "written communication";
		if (query == null || query.length() <= 2) return null;
		List<XCourseId> ret = new ArrayList<XCourseId>();
		for (Object courseId: helper.getHibSession().createSQLQuery(getCourseLookupSQL())
				.setLong("sessionId", server.getAcademicSession().getUniqueId())
				.setString("term", getBannerTerm(server.getAcademicSession()))
				.setString("query", query.toLowerCase()).list()) {
			XCourse course = server.getCourse(((Number)courseId).longValue());
			if (course != null)
				ret.add(course);
		}
		return ret;
	}

	@Override
	public void dispose() {
	}

	@Override
	public List<CourseOffering> getCourses(AcademicSessionInfo session, Session hibSession, String query) {
		if ("oc".equalsIgnoreCase(query)) query = "oral communication";
		if ("wc".equalsIgnoreCase(query)) query = "written communication";
		if (query == null || query.length() <= 2) return null;
		List courseIds = hibSession.createSQLQuery(getCourseLookupSQL())
				.setLong("sessionId", session.getUniqueId())
				.setString("term", getBannerTerm(session))
				.setString("query", query.toLowerCase()).list();
		if (courseIds == null || courseIds.isEmpty()) return null;
		return (List<CourseOffering>)hibSession.createQuery("from CourseOffering where uniqueId in :courseIds order by subjectAreaAbbv, courseNbr")
				.setParameterList("courseIds", courseIds, BigDecimalType.INSTANCE).setCacheable(true).list();
	}

}
