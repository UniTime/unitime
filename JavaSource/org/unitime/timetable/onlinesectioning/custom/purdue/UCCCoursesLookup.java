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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.hibernate.type.BigDecimalType;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
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
	
	protected String getCourseLookupFullSQL() {
		return ApplicationProperties.getProperty("banner.ucc.lookupSQLfull",
				"select distinct co.uniqueid " +
				"from timetable.szv_utm_attr a, course_offering co, instructional_offering io, subject_area sa " +
				"where a.term_start <= :term and a.term_end >= :term and " +
				"(lower(a.attribute_description) = concat('uc-', :query) or lower(a.course_attribute) = :query) and "+
				"co.instr_offr_id = io.uniqueid and co.subject_area_id = sa.uniqueid and io.session_id = :sessionId and " +
				"io.not_offered = 0 and sa.subject_area_abbreviation = a.subject and co.course_nbr like concat(a.course_number, '%')"
				);
	}
	
	protected String getCourseLookupPartialSQL() {
		return ApplicationProperties.getProperty("banner.ucc.lookupSQLpartial",
				"select distinct co.uniqueid " +
				"from timetable.szv_utm_attr a, course_offering co, instructional_offering io, subject_area sa " +
				"where a.term_start <= :term and a.term_end >= :term and " +
				"(lower(a.attribute_description) like concat(concat('uc-', :query), '%') or lower(a.attribute_description) like concat(concat('% ', :query), '%')) and "+
				"co.instr_offr_id = io.uniqueid and co.subject_area_id = sa.uniqueid and io.session_id = :sessionId and " +
				"io.not_offered = 0 and sa.subject_area_abbreviation = a.subject and co.course_nbr like concat(a.course_number, '%')"
				);
	}
	
	protected String getCourseNamesFullSQL() {
		return ApplicationProperties.getProperty("banner.ucc.coursesSQLfull",
				"select a.subject, a.course_number " +
				"from timetable.szv_utm_attr a where a.term_start <= :term and a.term_end >= :term and " +
				"(lower(a.attribute_description) = concat('uc-', :query) or lower(a.course_attribute) = :query)"
				);
	}
	
	protected String getCourseNamesPartialSQL() {
		return ApplicationProperties.getProperty("banner.ucc.coursesSQLpartial",
				"select a.subject, a.course_number " +
				"from timetable.szv_utm_attr a where a.term_start <= :term and a.term_end >= :term and " +
				"(lower(a.attribute_description) like concat(concat('uc-', :query), '%') or lower(a.attribute_description) like concat(concat('% ', :query), '%'))"
				);
	}
	
	protected String getSuggestionSQL() {
		return ApplicationProperties.getProperty("banner.ucc.suggestionsSQL",
				"select distinct a.course_attribute, a.attribute_description " +
				"from timetable.szv_utm_attr a where a.term_start <= :term and a.term_end >= :term and " +
				"(lower(a.attribute_description) like concat(concat('uc-', :query), '%') or lower(a.attribute_description) like concat(concat('% ', :query), '%') or lower(a.course_attribute) = :query)"
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
			Map<String, CourseAttribute> attributes = new HashMap<String, CourseAttribute>();
			org.hibernate.Session hibSession = new _RootDAO().createNewSession();
			try {
				for (Object[] data: (List<Object[]>)hibSession.createSQLQuery(getListAttributesSQL()).list()) {
					String subject = (String)data[0];
					String course_number = (String)data[1];
					String course_attribute = (String)data[2];
					String attribute_description = (String)data[3];
					String term_start = (String)data[4];
					String term_end = (String)data[5];
					CourseAttribute att = attributes.get(course_attribute);
					if (att == null) {
						att = new CourseAttribute(course_attribute, attribute_description);
						attributes.put(course_attribute, att);
					}
					att.addCourse(new Course(subject, course_number, term_start, term_end));
				}
			} finally {
				hibSession.close();
			}
			iCache = new ArrayList<CourseAttribute>(attributes.values());
			iCacheTS = System.currentTimeMillis();
			sLog.info("UCC Cache: " + ToolBox.col2string(iCache, 2));
		}
		return iCache;
	}
	
	protected String fixQuery(String query) {
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
		return query.toLowerCase();
	}

	@Override
	public List<XCourseId> getCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, String query) {
		String q = fixQuery(query);
		if (q == null) return null;
		List<XCourseId> ret = new ArrayList<XCourseId>();
		if (useCache()) {
			String term = getBannerTerm(server.getAcademicSession());
			boolean fullMatch = false;
			for (CourseAttribute ca: getCourseAttributes()) {
				if (ca.isFullMatch(q)) {
					if (!fullMatch) { fullMatch = true; ret.clear(); }
					for (Course c: ca.getCourses()) {
						if (c.isApplicable(term)) {
							Collection<? extends XCourseId> courses = server.findCourses(c.getSubjectArea() + " " + c.getCourseNumber(), -1, null);
							if (courses != null)
								ret.addAll(courses);
						}
					}
				} else if (!fullMatch && ca.isPartialMatch(q)) {
					for (Course c: ca.getCourses()) {
						if (c.isApplicable(term)) {
							Collection<? extends XCourseId> courses = server.findCourses(c.getSubjectArea() + " " + c.getCourseNumber(), -1, null);
							if (courses != null)
								ret.addAll(courses);
						}
					}
				}
			}
		} else {
			for (Object courseId: helper.getHibSession().createSQLQuery(getCourseLookupFullSQL())
					.setLong("sessionId", server.getAcademicSession().getUniqueId())
					.setString("term", getBannerTerm(server.getAcademicSession()))
					.setString("query", q).list()) {
				XCourse course = server.getCourse(((Number)courseId).longValue());
				if (course != null)
					ret.add(course);
			}
			if (ret.isEmpty()) {
				for (Object courseId: helper.getHibSession().createSQLQuery(getCourseLookupPartialSQL())
						.setLong("sessionId", server.getAcademicSession().getUniqueId())
						.setString("term", getBannerTerm(server.getAcademicSession()))
						.setString("query", q).list()) {
					XCourse course = server.getCourse(((Number)courseId).longValue());
					if (course != null)
						ret.add(course);
				}
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
		String q = fixQuery(query);
		if (q == null) return null;
		if (useCache()) {
			String term = getBannerTerm(session);
			List<CourseOffering> courses = new ArrayList<CourseOffering>();
			boolean fullMatch = false;
			for (CourseAttribute ca: getCourseAttributes()) {
				if (ca.isFullMatch(q)) {
					if (!fullMatch) { fullMatch = true; courses.clear(); }
					for (Course c: ca.getCourses()) {
						if (c.isApplicable(term)) {
							courses.addAll(
									hibSession.createQuery(
											"select co from CourseOffering co where " +
											"co.instructionalOffering.session = :sessionId and co.instructionalOffering.notOffered = false and " +
											"co.subjectAreaAbbv = :subject and co.courseNbr like :course"
									).setLong("sessionId", session.getUniqueId())
									.setString("subject", c.getSubjectArea())
									.setString("course", c.getCourseNumber() + "%")
									.setCacheable(true).list());
						}
					}
				} else if (!fullMatch && ca.isPartialMatch(q)) {
					for (Course c: ca.getCourses()) {
						if (c.isApplicable(term)) {
							courses.addAll(
									hibSession.createQuery(
											"select co from CourseOffering co where " +
											"co.instructionalOffering.session = :sessionId and co.instructionalOffering.notOffered = false and " +
											"co.subjectAreaAbbv = :subject and co.courseNbr like :course"
									).setLong("sessionId", session.getUniqueId())
									.setString("subject", c.getSubjectArea())
									.setString("course", c.getCourseNumber() + "%")
									.setCacheable(true).list());
						}
					}
				}
			}
			return courses;
		} else {
			List courseIds = hibSession.createSQLQuery(getCourseLookupFullSQL())
					.setLong("sessionId", session.getUniqueId())
					.setString("term", getBannerTerm(session))
					.setString("query", q).list();
			if (courseIds == null || courseIds.isEmpty()) {
				courseIds = hibSession.createSQLQuery(getCourseLookupPartialSQL())
						.setLong("sessionId", session.getUniqueId())
						.setString("term", getBannerTerm(session))
						.setString("query", q).list();
			}
			if (courseIds == null || courseIds.isEmpty()) return null;
			return (List<CourseOffering>)hibSession.createQuery("from CourseOffering where uniqueId in :courseIds order by subjectAreaAbbv, courseNbr")
					.setParameterList("courseIds", courseIds, BigDecimalType.INSTANCE).setCacheable(true).list();
		}
	}
	
	private static class Course {
		String iSubjectArea, iCourseNumber;
		String iStartTerm, iEndTerm;

		private Course(String subject, String courseNumber, String startTerm, String endTerm) {
			iSubjectArea = subject;
			iCourseNumber = courseNumber;
			iStartTerm = startTerm;
			iEndTerm = endTerm;
		}
		
		public String getSubjectArea() { return iSubjectArea; }
		public String getCourseNumber() { return iCourseNumber; }

		public boolean isApplicable(String term) {
			return iStartTerm.compareTo(term) <= 0 && term.compareTo(iEndTerm) <= 0;
		}
		
		@Override
		public String toString() {
			return iSubjectArea + " " + iCourseNumber;
		}
	}

	private static class CourseAttribute implements Comparable<CourseAttribute> {
		String iCourseAttribute, iAttributeDescription, iAttributeDescriptionLowCase;
		List<Course> iCourses = new ArrayList<Course>();

		private CourseAttribute(String courseAttribute, String attributeDescription) {
			iCourseAttribute = courseAttribute;
			iAttributeDescription = attributeDescription;
			iAttributeDescriptionLowCase = attributeDescription.toLowerCase();
		}
		
		void addCourse(Course course) { iCourses.add(course); }
		List<Course> getCourses() { return iCourses; }
		
		public boolean isFullMatch(String query) {
			return iCourseAttribute.equalsIgnoreCase(query) || iAttributeDescription.equalsIgnoreCase("uc-" + query);
		}

		public boolean isPartialMatch(String query) {
			return	iAttributeDescriptionLowCase.startsWith("uc-" + query) || iAttributeDescriptionLowCase.contains(" " + query);
		}
		
		public String getCourseAttribute() {
			return iCourseAttribute;
		}
		
		public String getAttributeDescription() {
			return iAttributeDescription;
		}
		
		@Override
		public String toString() {
			return iCourseAttribute + " - " + iAttributeDescription + ": " + iCourses.size() + "/" + iCourses;
		}

		@Override
		public int compareTo(CourseAttribute ca) {
			return iCourseAttribute.compareTo(ca.iCourseAttribute);
		}
		
		@Override
		public int hashCode() {
			return iCourseAttribute.hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof CourseAttribute)) return false;
			return iCourseAttribute.equals(((CourseAttribute)o).iCourseAttribute);
		}
	}

	@Override
	public void addSuggestions(OnlineSectioningServer server, OnlineSectioningHelper helper, String query, FilterRpcResponse filter) {
		String q = fixQuery(query);
		if (q == null) return;
		if (useCache()) {
			for (CourseAttribute ca: getCourseAttributes()) {
				if (ca.isFullMatch(q) ||  ca.isPartialMatch(q)) {
					filter.addSuggestion(ca.getAttributeDescription(), ca.getCourseAttribute(), "UCC Attribute", "lookup", true);
				}
			}
		} else {
			for (Object[] data: (List<Object[]>)helper.getHibSession().createSQLQuery(getSuggestionSQL())
					.setString("term", getBannerTerm(server.getAcademicSession()))
					.setString("query", q).list()) {
				String course_attribute = (String)data[0];
				String attribute_description = (String)data[1];
				filter.addSuggestion(attribute_description, course_attribute, "UCC Attribute", "lookup", true);
			}
		}
		
	}

	@Override
	public List<String> getCourses(AcademicSessionInfo session, String query) {
		String q = fixQuery(query);
		if (q == null) return null;
		List<String> ret = new ArrayList<String>();
		if (useCache()) {
			String term = getBannerTerm(session);
			boolean fullMatch = false;
			for (CourseAttribute ca: getCourseAttributes()) {
				if (ca.isFullMatch(q)) {
					if (!fullMatch) { fullMatch = true; ret.clear(); }
					for (Course c: ca.getCourses()) {
						if (c.isApplicable(term))
							ret.add(c.getSubjectArea() + " " + c.getCourseNumber());
					}
				} else if (!fullMatch && ca.isPartialMatch(q)) {
					for (Course c: ca.getCourses()) {
						if (c.isApplicable(term))
							ret.add(c.getSubjectArea() + " " + c.getCourseNumber());
					}
				}
			}
		} else {
			org.hibernate.Session hibSession = new _RootDAO().createNewSession();
			try {
				for (Object[] course: (List<Object[]>)hibSession.createSQLQuery(getCourseNamesFullSQL())
						.setString("term", getBannerTerm(session))
						.setString("query", q).list()) {
					ret.add(((String)course[0]) + " " + ((String)course[1]));
				}
				if (ret.isEmpty()) {
					for (Object[] course: (List<Object[]>)hibSession.createSQLQuery(getCourseNamesPartialSQL())
							.setString("term", getBannerTerm(session))
							.setString("query", q).list()) {
						ret.add(((String)course[0]) + " " + ((String)course[1]));
					}	
				}
			} finally {
				hibSession.close();
			}
		}
		Collections.sort(ret);
		return ret;
	}
}
