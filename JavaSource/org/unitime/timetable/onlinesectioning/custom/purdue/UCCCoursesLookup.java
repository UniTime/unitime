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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
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

/**
 * @author Tomas Muller
 */
public class UCCCoursesLookup implements CustomCourseLookup {
	private static Log sLog = LogFactory.getLog(UCCCoursesLookup.class);
	
	private ExternalTermProvider iExternalTermProvider;
	
	private Map<Long, CourseAttributeCache> iCache = new HashMap<Long, CourseAttributeCache>();
	
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
				"from timetable.szv_utm_attr a, timetable.course_offering co, timetable.instructional_offering io, timetable.subject_area sa " +
				"where a.term_start <= :term and a.term_end >= :term and " +
				"(lower(a.attribute_description) = concat('uc-', :query) or lower(a.course_attribute) = :query) and "+
				"co.instr_offr_id = io.uniqueid and co.subject_area_id = sa.uniqueid and io.session_id = :sessionId and " +
				"io.not_offered = 0 and (sa.subject_area_abbreviation = a.subject or sa.subject_area_abbreviation like concat('% - ', a.subject)) and co.course_nbr like concat(a.course_number, '%')"
				);
	}
	
	protected String getCourseLookupPartialSQL() {
		return ApplicationProperties.getProperty("banner.ucc.lookupSQLpartial",
				"select distinct co.uniqueid " +
				"from timetable.szv_utm_attr a, timetable.course_offering co, timetable.instructional_offering io, timetable.subject_area sa " +
				"where a.term_start <= :term and a.term_end >= :term and " +
				"(lower(a.attribute_description) like concat(concat('uc-', :query), '%') or lower(a.attribute_description) like concat(concat('% ', :query), '%')) and "+
				"co.instr_offr_id = io.uniqueid and co.subject_area_id = sa.uniqueid and io.session_id = :sessionId and " +
				"io.not_offered = 0 and (sa.subject_area_abbreviation = a.subject or sa.subject_area_abbreviation like concat('% - ', a.subject)) and co.course_nbr like concat(a.course_number, '%')"
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
		return ApplicationProperties.getProperty("banner.ucc.placeholder.regExp", "( ?UCC:? ?)?([^-]*[^- ]+)( ?-.*)?");
	}
	
	protected String getPlaceHolderRenames() {
		return ApplicationProperties.getProperty("banner.ucc.placeholder.replacements",
				"(?i:Behavioral/Social Science)|Behavior/Social Science\n" + 
				"(?i:Science,? Tech \\& Society( Selective)?)|Science, Tech & Society\n" +
				"(?i:Oral Communication)|Oral Communications\n" +
				"(?i:Written Communications)|Written Communication\n" +
				"(?i:Oral/Written Communications)|Communication"
				);
	}
	
	protected String getListAttributesSQL() {
		return ApplicationProperties.getProperty("banner.ucc.cachedSQL",
				"select distinct a.course_attribute, a.attribute_description, co.uniqueId " + 
				"from timetable.szv_utm_attr a, timetable.course_offering co, timetable.instructional_offering io, timetable.subject_area sa " +
				"where a.term_start <= :term and a.term_end >= :term and " +
				"co.instr_offr_id = io.uniqueid and co.subject_area_id = sa.uniqueid and io.session_id = :sessionId and " +
				"io.not_offered = 0 and sa.subject_area_abbreviation = a.subject and co.course_nbr like concat(a.course_number, '%')"
				);
	}
	
	protected boolean useCache() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.ucc.useCache", "false"));
	}
	
	protected long getCourseAttributesTTL() {
		return 1000l * Long.valueOf(ApplicationProperties.getProperty("banner.ucc.ttlSeconds", "900"));
	}
	
	protected synchronized Collection<CourseAttribute> getCourseAttributes(AcademicSessionInfo session, org.hibernate.Session hibSession) {
		CourseAttributeCache cache = iCache.get(session.getUniqueId());
		if (cache == null || !cache.isActive(getCourseAttributesTTL())) {
			Map<String, CourseAttribute> attributes = new HashMap<String, CourseAttribute>();
			String term = getBannerTerm(session);
			for (Object[] data: (hibSession == null ? new _RootDAO().getSession() : hibSession).createNativeQuery(getListAttributesSQL(), Object[].class)
					.setParameter("sessionId", session.getUniqueId())
					.setParameter("term", term)
					.list()) {
				String course_attribute = (String)data[0];
				String attribute_description = (String)data[1];
				Long courseId = ((Number)data[2]).longValue();
				CourseAttribute att = attributes.get(course_attribute);
				if (att == null) {
					att = new CourseAttribute(course_attribute, attribute_description);
					attributes.put(course_attribute, att);
				}
				att.addCourse(courseId);
			}
			cache = new CourseAttributeCache(attributes.values());
			iCache.put(session.getUniqueId(), cache);
			sLog.info("UCC Cache [" + session + "]: " + ToolBox.col2string(cache.getAttributes(), 2));
		}
		return cache.getAttributes();
	}
	
	protected String fixQuery(String query) {
		if ("oc".equalsIgnoreCase(query)) query = "oral communications";
		if ("wc".equalsIgnoreCase(query)) query = "written communication";
		if (query == null || query.length() <= 3) return null;
		String regExp = getPlaceHolderRegExp();
		if (regExp != null && !regExp.isEmpty()) {
			Matcher m = Pattern.compile(regExp).matcher(query);
			if (m.matches())
				query = m.group(2);
		}
		String replacements = getPlaceHolderRenames();
		if (replacements != null && !replacements.isEmpty()) {
			for (String rep: replacements.split("[\r\n]")) {
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
	public List<XCourse> getCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, String query, boolean allowPartialMatch) {
		String q = fixQuery(query);
		if (q == null) return null;
		List<XCourse> ret = new ArrayList<XCourse>();
		if (useCache()) {
			boolean fullMatch = false;
			for (CourseAttribute ca: getCourseAttributes(server.getAcademicSession(), helper.getHibSession())) {
				if (ca.isFullMatch(q)) {
					if (!fullMatch) { fullMatch = true; ret.clear(); }
					for (Long courseId: ca.getCourseIds()) {
						XCourse course = server.getCourse(courseId);
						if (course != null)
							ret.add(course);
					}
				} else if (allowPartialMatch && !fullMatch && ca.isPartialMatch(q)) {
					for (Long courseId: ca.getCourseIds()) {
						XCourse course = server.getCourse(courseId);
						if (course != null)
							ret.add(course);
					}
				}
			}
		} else {
			for (Long courseId: helper.getHibSession().createNativeQuery(getCourseLookupFullSQL(), Long.class)
					.setParameter("sessionId", server.getAcademicSession().getUniqueId())
					.setParameter("term", getBannerTerm(server.getAcademicSession()))
					.setParameter("query", q).list()) {
				XCourse course = server.getCourse(courseId);
				if (course != null)
					ret.add(course);
			}
			if (allowPartialMatch && ret.isEmpty()) {
				for (Long courseId: helper.getHibSession().createNativeQuery(getCourseLookupPartialSQL(), Long.class)
						.setParameter("sessionId", server.getAcademicSession().getUniqueId())
						.setParameter("term", getBannerTerm(server.getAcademicSession()))
						.setParameter("query", q).list()) {
					XCourse course = server.getCourse(courseId);
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
	public List<CourseOffering> getCourses(AcademicSessionInfo session, Session hibSession, String query, boolean allowPartialMatch) {
		String q = fixQuery(query);
		if (q == null) return null;
		if (useCache()) {
			List<Long> courseIds = new ArrayList<Long>();
			boolean fullMatch = false;
			for (CourseAttribute ca: getCourseAttributes(session, hibSession)) {
				if (ca.isFullMatch(q)) {
					if (!fullMatch) { fullMatch = true; courseIds.clear(); }
					courseIds.addAll(ca.getCourseIds());
				} else if (allowPartialMatch && !fullMatch && ca.isPartialMatch(q)) {
					courseIds.addAll(ca.getCourseIds());
				}
			}
			if (courseIds == null || courseIds.isEmpty()) return null;
			return hibSession.createQuery("from CourseOffering where uniqueId in :courseIds order by subjectAreaAbbv, courseNbr", CourseOffering.class)
					.setParameterList("courseIds", courseIds, Long.class).setCacheable(true).list();
		} else {
			List<Long> courseIds = hibSession.createNativeQuery(getCourseLookupFullSQL(), Long.class)
					.setParameter("sessionId", session.getUniqueId())
					.setParameter("term", getBannerTerm(session))
					.setParameter("query", q).list();
			if (allowPartialMatch && (courseIds == null || courseIds.isEmpty())) {
				courseIds = hibSession.createNativeQuery(getCourseLookupPartialSQL(), Long.class)
						.setParameter("sessionId", session.getUniqueId())
						.setParameter("term", getBannerTerm(session))
						.setParameter("query", q).list();
			}
			if (courseIds == null || courseIds.isEmpty()) return null;
			return hibSession.createQuery("from CourseOffering where uniqueId in :courseIds order by subjectAreaAbbv, courseNbr", CourseOffering.class)
					.setParameterList("courseIds", courseIds, Long.class).setCacheable(true).list();
		}
	}
	
	@Override
	public Set<Long> getCourseIds(AcademicSessionInfo session, org.hibernate.Session hibSession, String query, boolean allowPartialMatch) {
		String q = fixQuery(query);
		if (q == null) return null;
		if (useCache()) {
			Set<Long> courseIds = new HashSet<Long>();
			boolean fullMatch = false;
			for (CourseAttribute ca: getCourseAttributes(session, hibSession)) {
				if (ca.isFullMatch(q)) {
					if (!fullMatch) { fullMatch = true; courseIds.clear(); }
					courseIds.addAll(ca.getCourseIds());
				} else if (allowPartialMatch && !fullMatch && ca.isPartialMatch(q)) {
					courseIds.addAll(ca.getCourseIds());
				}
			}
			return courseIds;
		} else {
			if (hibSession == null)
				hibSession = new _RootDAO().getSession();
			List<Long> courseIds = hibSession.createNativeQuery(getCourseLookupFullSQL(), Long.class)
					.setParameter("sessionId", session.getUniqueId())
					.setParameter("term", getBannerTerm(session))
					.setParameter("query", q).list();
			if (allowPartialMatch && (courseIds == null || courseIds.isEmpty())) {
				courseIds = hibSession.createNativeQuery(getCourseLookupPartialSQL(), Long.class)
						.setParameter("sessionId", session.getUniqueId())
						.setParameter("term", getBannerTerm(session))
						.setParameter("query", q).list();
			}
			if (courseIds == null || courseIds.isEmpty()) return null;
			Set<Long> ret = new HashSet<Long>(courseIds);
			return ret;
		}
	}
	
	private static class CourseAttributeCache {
		Set<CourseAttribute> iAttributes;
		long iTimeStamp;
		
		public CourseAttributeCache(Collection<CourseAttribute> attributes) {
			iAttributes = new TreeSet<CourseAttribute>(attributes);
			iTimeStamp = System.currentTimeMillis();
		}
		
		public Collection<CourseAttribute> getAttributes() { return iAttributes; }
		public boolean isActive(long ttl) {
			return (System.currentTimeMillis() - iTimeStamp) <= ttl;
		}
	}
	
	private static class CourseAttribute implements Comparable<CourseAttribute> {
		String iCourseAttribute, iAttributeDescription, iAttributeDescriptionLowCase;
		List<Long> iCourseIds = new ArrayList<Long>();

		private CourseAttribute(String courseAttribute, String attributeDescription) {
			iCourseAttribute = courseAttribute;
			iAttributeDescription = attributeDescription;
			iAttributeDescriptionLowCase = attributeDescription.toLowerCase();
		}
		
		void addCourse(Long courseId) { iCourseIds.add(courseId); }
		List<Long> getCourseIds() { return iCourseIds; }
		
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
			return iCourseAttribute + " - " + iAttributeDescription + ": " + iCourseIds.size() + " courses";
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
			for (CourseAttribute ca: getCourseAttributes(server.getAcademicSession(), helper.getHibSession())) {
				if (ca.isFullMatch(q) ||  ca.isPartialMatch(q)) {
					filter.addSuggestion(ca.getAttributeDescription(), ca.getCourseAttribute(), "UCC Attribute", "lookup", true);
				}
			}
		} else {
			for (Object[] data: helper.getHibSession().createNativeQuery(getSuggestionSQL(), Object[].class)
					.setParameter("term", getBannerTerm(server.getAcademicSession()))
					.setParameter("query", q).list()) {
				String course_attribute = (String)data[0];
				String attribute_description = (String)data[1];
				filter.addSuggestion(attribute_description, course_attribute, "UCC Attribute", "lookup", true);
			}
		}
	}
}
