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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.type.BigDecimalType;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.dao._RootDAO;
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
	
	private List<CourseAttribute> iCache = null;
	private Long iCacheTS = null;
	
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
				"select distinct co.uniqueid " +
				"from timetable.szv_utm_attr a, course_offering co, instructional_offering io, subject_area sa " +
				"where a.term_start <= :term and a.term_end >= :term and " +
				"(lower(a.attribute_description) like concat(concat('uc-', :query), '%') or lower(a.attribute_description) like concat(concat('% ', :query), '%') or lower(a.course_attribute) = :query) and "+
				"co.instr_offr_id = io.uniqueid and co.subject_area_id = sa.uniqueid and io.session_id = :sessionId and " +
				"io.not_offered = 0 and sa.subject_area_abbreviation = a.subject and co.course_nbr like concat(a.course_number, '%')"
				);
	}
	
	protected String getPlaceHolderRegExp() {
		return ApplicationProperties.getProperty("banner.ucc.placeholder.regExp", " ?UCC:? ?([^-]*[^- ]+)( ?-.*)?");
	}
	
	protected String getPlaceHolderRenames() {
		return ApplicationProperties.getProperty("banner.ucc.placeholder.replacements", "(?i:Behavioral/Social Science)|Behavior/Social Science\n(?i:Science,? Tech \\& Society( Selective)?)|Science, Tech & Society");
	}
	
	protected String getListAttributesSQL() {
		return ApplicationProperties.getProperty("banner.ucc.cachedSQL",
				"select subject, course_number, course_attribute, attribute_description, term_start, term_end from timetable.szv_utm_attr"
				);
	}
	
	protected boolean useCache() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.ucc.useCache", "false"));
	}
	
	protected long getCourseAttributesTTL() {
		return 1000l * Long.valueOf(ApplicationProperties.getProperty("banner.ucc.ttlSeconds", "900"));
	}
	
	protected synchronized List<CourseAttribute> getCourseAttributes() {
		if (iCache == null || (System.currentTimeMillis() - iCacheTS) > getCourseAttributesTTL()) {
			iCache = new ArrayList<CourseAttribute>();
			iCacheTS = System.currentTimeMillis();
			org.hibernate.Session hibSession = new _RootDAO().createNewSession();
			try {
				for (Object[] data: (List<Object[]>)hibSession.createSQLQuery(getListAttributesSQL()).list()) {
					iCache.add(new CourseAttribute(
							(String)data[0], (String)data[1], (String)data[2],
							(String)data[3], (String)data[4], (String)data[5]));
				}
			} finally {
				hibSession.close();
			}
		}
		return iCache;
	}

	@Override
	public List<XCourseId> getCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, String query) {
		if ("oc".equalsIgnoreCase(query)) query = "oral communication";
		if ("wc".equalsIgnoreCase(query)) query = "written communication";
		if (query == null || query.length() <= 2) return null;
		String regExp = getPlaceHolderRegExp();
		if (regExp != null && !regExp.isEmpty()) {
			Matcher m = Pattern.compile(regExp).matcher(query);
			if (m.matches())
				query = m.group(1);
		}
		String replacements = getPlaceHolderRenames();
		if (replacements != null && !replacements.isEmpty()) {
			for (String rep: replacements.split("\n")) {
				int idx = rep.indexOf('|');
				if (idx <= 0) continue;
				if (query.matches(rep.substring(0, idx))) {
					query = rep.substring(idx + 1);
					break;
				}
			}
		}
		List<XCourseId> ret = new ArrayList<XCourseId>();
		if (useCache()) {
			String term = getBannerTerm(server.getAcademicSession());
			String q = query.toLowerCase();
			for (CourseAttribute ca: getCourseAttributes()) {
				if (ca.isApplicable(term) && ca.isMatching(q)) {
					Collection<? extends XCourseId> courses = server.findCourses(ca.getSubjectArea() + " " + ca.getCourseNumber(), -1, null);
					if (courses != null)
						ret.addAll(courses);
				}
			}
		} else {
			for (Object courseId: helper.getHibSession().createSQLQuery(getCourseLookupSQL())
					.setLong("sessionId", server.getAcademicSession().getUniqueId())
					.setString("term", getBannerTerm(server.getAcademicSession()))
					.setString("query", query.toLowerCase()).list()) {
				XCourse course = server.getCourse(((Number)courseId).longValue());
				if (course != null)
					ret.add(course);
			}
		}
		Collections.sort(ret);
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
		String regExp = getPlaceHolderRegExp();
		if (regExp != null && !regExp.isEmpty()) {
			Matcher m = Pattern.compile(regExp).matcher(query);
			if (m.matches())
				query = m.group(1);
		}
		String replacements = getPlaceHolderRenames();
		if (replacements != null && !replacements.isEmpty()) {
			for (String rep: replacements.split("\n")) {
				int idx = rep.indexOf('|');
				if (idx <= 0) continue;
				if (query.matches(rep.substring(0, idx))) {
					query = rep.substring(idx + 1);
					break;
				}
			}
		}

		if (useCache()) {
			String term = getBannerTerm(session);
			String q = query.toLowerCase();
			List<CourseOffering> courses = new ArrayList<CourseOffering>();
			for (CourseAttribute ca: getCourseAttributes()) {
				if (ca.isApplicable(term) && ca.isMatching(q)) {
					courses.addAll(
							hibSession.createQuery(
									"select co from CourseOffering co where " +
									"co.instructionalOffering.session = :sessionId and co.instructionalOffering.notOffered = false and " +
									"co.subjectAreaAbbv = :subject and co.courseNbr like :course"
							).setLong("sessionId", session.getUniqueId())
							.setString("subject", ca.getSubjectArea())
							.setString("course", ca.getCourseNumber() + "%")
							.setCacheable(true).list());
				}
			}
			return courses;
		} else {
			List courseIds = hibSession.createSQLQuery(getCourseLookupSQL())
					.setLong("sessionId", session.getUniqueId())
					.setString("term", getBannerTerm(session))
					.setString("query", query.toLowerCase()).list();
			if (courseIds == null || courseIds.isEmpty()) return null;
			return (List<CourseOffering>)hibSession.createQuery("from CourseOffering where uniqueId in :courseIds order by subjectAreaAbbv, courseNbr")
					.setParameterList("courseIds", courseIds, BigDecimalType.INSTANCE).setCacheable(true).list();
		}
	}

	private static class CourseAttribute {
		String iSubjectArea, iCourseNumber;
		String iCourseAttribute, iAttributeDescription;
		String iStartTerm, iEndTerm;

		private  CourseAttribute(String subject, String courseNumber, String courseAttribute, String attributeDescription, String startTerm, String endTerm) {
			iSubjectArea = subject;
			iCourseNumber = courseNumber;
			iCourseAttribute = courseAttribute;
			iAttributeDescription = attributeDescription.toLowerCase();
			iStartTerm = startTerm;
			iEndTerm = endTerm;
		}

		public String getSubjectArea() { return iSubjectArea; }
		public String getCourseNumber() { return iCourseNumber; }
		public boolean isApplicable(String term) {
			return iStartTerm.compareTo(term) <= 0 && term.compareTo(iEndTerm) <= 0;
		}
		public boolean isMatching(String query) {
			return	iAttributeDescription.startsWith("uc-" + query) ||
					iAttributeDescription.contains(" " + query) ||
					iCourseAttribute.equalsIgnoreCase(query);
		}
	}
}
