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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeCourseInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreeGroupInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface.DegreePlaceHolderInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.Builder;
import org.unitime.timetable.onlinesectioning.custom.CriticalCoursesProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseLookupHolder;
import org.unitime.timetable.onlinesectioning.custom.DegreePlansProvider;
import org.unitime.timetable.onlinesectioning.model.XAreaClassificationMajor;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;

/**
 * @author Tomas Muller
 */
public class CriticalCoursesQuery implements CriticalCoursesProvider, DegreePlansProvider {
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
		return ApplicationProperties.getProperty("banner.dgw.catalogYear." + server.getAcademicSession().getTerm() + server.getAcademicSession().getYear(),
				ApplicationProperties.getProperty("banner.dgw.catalogYear", iExternalTermProvider.getExternalTerm(server.getAcademicSession()).substring(0, 4) + "10"));
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
				"placeholder_value like '%*' and placeholder_type = '" + getUccPlaceHolderType() + "' and tmpl_description not like '%Statewide%' and tmpl_college=:area and tmpl_major=:major and tmpl_catyear=:catyear"
				);
	}
	
	protected boolean getCriticalPlaceHolderAllowPartialMatch() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.dgw.placeHolderPartialMatch", "true"));
	}
	
	protected String getUccPlaceHolderType() {
		return ApplicationProperties.getProperty("banner.dgw.uccPlaceHolderCodeRegExp", "UNIV-CORE");
	}
	
	protected String getCriticalPlaceHolderRegExp() {
		return ApplicationProperties.getProperty("banner.dgw.criticalPlaceHolderRegExp", ".* ?\\* ?");
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
		for (XAreaClassificationMajor acm: getAreaClasfMajors(server, helper, student)) {
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
			if (action != null) action.addOptionBuilder().setKey("critical").setValue(cc.toString());
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
		public int isCritical(CourseOffering course) {
			if (iCourseIds.contains(course.getUniqueId())) return CourseDemand.Critical.IMPORTANT.ordinal();
			for (String c: iCriticalCourses)
				if (course.getCourseName().startsWith(c)) return CourseDemand.Critical.IMPORTANT.ordinal();
			return CourseDemand.Critical.NORMAL.ordinal();
		}

		@Override
		public int isCritical(XCourseId course) {
			if (iCourseIds.contains(course.getCourseId())) return CourseDemand.Critical.IMPORTANT.ordinal();
			for (String c: iCriticalCourses)
				if (course.getCourseName().startsWith(c)) return CourseDemand.Critical.IMPORTANT.ordinal();
			return CourseDemand.Critical.NORMAL.ordinal();
		}
		
		@Override
		public String toString() {
			return iCriticalCourses.toString();
		}
	}
	
	public String getCatalogYear(OnlineSectioningServer server, XStudent student, XAreaClassificationMajor acm) {
		int year = Integer.parseInt(iExternalTermProvider.getExternalTerm(server.getAcademicSession()).substring(0, 4));
		if ("01".equals(acm.getClassification()) || "02".equals(acm.getClassification()))
			return year + "10";
		if ("03".equals(acm.getClassification()) || "04".equals(acm.getClassification()))
			return (year - 1) + "10";
		if ("05".equals(acm.getClassification()) || "06".equals(acm.getClassification()))
			return (year - 2) + "10";
		if ("07".equals(acm.getClassification()) || "08".equals(acm.getClassification()))
			return (year - 3) + "10";
		if ("09".equals(acm.getClassification()) || "10".equals(acm.getClassification()))
			return (year - 4) + "10";
		return year + "10";
	}
	
	public int getTermSequence(OnlineSectioningServer server, XStudent student, XAreaClassificationMajor acm) {
		String term = iExternalTermProvider.getExternalTerm(server.getAcademicSession());
		int seq = (term.endsWith("20") ? 1 : term.endsWith("30") ? 2 : 0);
		if ("01".equals(acm.getClassification()) || "02".equals(acm.getClassification()))
			return seq;
		if ("03".equals(acm.getClassification()) || "04".equals(acm.getClassification()))
			return seq + 3;
		if ("05".equals(acm.getClassification()) || "06".equals(acm.getClassification()))
			return seq + 6;
		if ("07".equals(acm.getClassification()) || "08".equals(acm.getClassification()))
			return seq + 9;
		if ("09".equals(acm.getClassification()) || "10".equals(acm.getClassification()))
			return seq + 9; // most plans are only for four years
		return seq;
	}
	
	protected String getPlannedCoursesSQL() {
		return ApplicationProperties.getProperty("banner.dgw.plannedCoursesSQL",
				"select tmpl_id, tmpl_description, tmpl_conc, course_discipline, course_number, choice_group_id, trim(is_critical) from timetable.tmpl_course_view where " +
				"tmpl_description not like '%Statewide%' and tmpl_college=:area and tmpl_major=:major and tmpl_catyear=:catyear and term_seq=:term " +
				"order by tmpl_conc, choice_group_id desc, course_discipline, course_number"
				);
	}
	
	protected String getPlannedPlaceholdersSQL() {
		return ApplicationProperties.getProperty("banner.dgw.criticalPlaceholdersSQL",
				"select tmpl_id, tmpl_description, tmpl_conc, placeholder_type, placeholder_value from timetable.tmpl_placeholder_view where " +
				"tmpl_description not like '%Statewide%' and tmpl_college=:area and tmpl_major=:major and tmpl_catyear=:catyear and term_seq=:term " +
				"order by tmpl_conc, placeholder_value"
				);
	}
	
	protected List<XAreaClassificationMajor> getAreaClasfMajors(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) {
		return student.getMajors();
	}
	
	protected DegreeCourseInterface getCourse(OnlineSectioningServer server, String subjectArea, String courseNbr, boolean critical) {
		DegreeCourseInterface course = new DegreeCourseInterface();
		course.setSubject(subjectArea);
		course.setCourse(courseNbr);
		course.setId(subjectArea + " " + courseNbr);
		course.setCritical(critical);
		course.setSelected(false);
		Collection<? extends XCourseId> ids = server.findCourses(subjectArea + " " + courseNbr, -1, null);
		if (ids != null) {
			for (XCourseId id: ids) {
				XCourse xc = (id instanceof XCourse ? (XCourse) id : server.getCourse(id.getCourseId()));
				if (xc == null) continue;
				if (!id.getCourseName().startsWith(subjectArea + " " + courseNbr)) continue;
				if (course.getTitle() == null || id.getCourseName().equals(subjectArea + " " + courseNbr))
					course.setTitle(xc.getTitle());
				CourseAssignment ca = new CourseAssignment();
				ca.setCourseId(xc.getCourseId());
				ca.setSubject(xc.getSubjectArea());
				ca.setCourseNbr(xc.getCourseNumber());
				ca.setTitle(xc.getTitle());
				ca.setNote(xc.getNote());
				ca.setCreditAbbv(xc.getCreditAbbv());
				ca.setCreditText(xc.getCreditText());
				ca.setTitle(xc.getTitle());
				ca.setHasUniqueName(xc.hasUniqueName());
				ca.setLimit(xc.getLimit());
				int firstChoiceReqs = 0;
				int enrl = 0;
				Collection<XCourseRequest> requests = server.getRequests(id.getOfferingId());
				if (requests != null)
					for (XCourseRequest r: requests) {
						if (r.getEnrollment() != null && r.getEnrollment().getCourseId().equals(id.getCourseId())) enrl ++;
						if (!r.isAlternative() && r.getEnrollment() == null && r.getCourseIds().get(0).equals(id)) firstChoiceReqs ++;
					}
				ca.setEnrollment(enrl);
				ca.setProjected(firstChoiceReqs);
				course.addCourse(ca);
			}
		}
		if (course.hasCourses()) {
			for (CourseAssignment ca: course.getCourses())
				if (ca.getSubject().equals(course.getSubject()) && ca.getCourseNbr().equals(course.getCourse()))
					course.setCourseId(ca.getCourseId());
		}
		return course;
	}
	
	protected List<XCourse> getPlaceHolderCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, String phType, String phName) {
		// check provider
		if (!CustomCourseLookupHolder.hasProvider()) return null;
		// check placeholder type code
		if (phType == null || !phType.matches(getUccPlaceHolderType()))
			return null;
		return CustomCourseLookupHolder.getProvider().getCourses(server, helper, phName, getCriticalPlaceHolderAllowPartialMatch());
	}
	
	protected boolean isCriticalPlaceholder(String phValue) {
		return phValue != null && phValue.matches(getCriticalPlaceHolderRegExp());
	}

	@Override
	public List<DegreePlanInterface> getDegreePlans(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) throws SectioningException {
		String sqlCourses = getPlannedCoursesSQL();
		String sqlPlaceholders = getPlannedPlaceholdersSQL();
		Builder action = helper.getAction();
		Map<String, DegreePlanInterface> plans = new HashMap<String, DegreePlanInterface>();
		for (XAreaClassificationMajor acm: getAreaClasfMajors(server, helper, student)) {
			String catyear = getCatalogYear(server, student, acm);
			int term = getTermSequence(server, student, acm);
			org.hibernate.Query query = helper.getHibSession().createSQLQuery(sqlCourses);
			query.setString("area", acm.getArea());
			if (action != null) action.addOptionBuilder().setKey("area").setValue(acm.getArea());
			query.setString("major", acm.getMajor());
			if (action != null) action.addOptionBuilder().setKey("major").setValue(acm.getMajor());
			if (sqlCourses.contains(":catyear")) {
				query.setString("catyear", catyear);
				if (action != null) action.addOptionBuilder().setKey("catyear").setValue(catyear);
			}
			if (sqlCourses.contains(":term")) {
				query.setInteger("term", term);
				if (action != null) action.addOptionBuilder().setKey("term").setValue(String.valueOf(term));
			}
			for (Object[] o: (List<Object[]>)query.list()) {
				String progCode = (String)o[0];
				String progName = (String)o[1];
				String progConc = (String)o[2];
				String subject = (String)o[3];
				String courseNbr = (String)o[4];
				String choiceGroupId = (String)o[5];
				boolean isCritical = "Y".equals(o[6]);
				DegreePlanInterface plan = plans.get(progCode);
				if (plan == null) {
					plan = new DegreePlanInterface();
					plan.setId(progCode);
					plan.setName(progName);
					plan.setSchool(acm.getArea());
					plan.setDegree(acm.getMajor());
					plan.setGroup(new DegreeGroupInterface());
					plan.getGroup().setChoice(false);
					if (progConc != null && !progConc.isEmpty())
						plan.setDegree(acm.getMajor() + "/" + progConc);
					else
						plan.setActive(true);
					plans.put(progCode, plan);
				}
				if (choiceGroupId == null || choiceGroupId.isEmpty()) {
					plan.getGroup().addCourse(getCourse(server, subject, courseNbr, isCritical));
				} else {
					DegreeGroupInterface group = plan.getGroup().getGroup(choiceGroupId);
					if (group == null) {
						group = new DegreeGroupInterface();
						group.setChoice(true);
						group.setId(choiceGroupId);
						group.setCritical(isCritical);
						plan.getGroup().addGroup(group);
					}
					group.addCourse(getCourse(server, subject, courseNbr, isCritical));
				}
			}
			if (sqlPlaceholders != null && !sqlPlaceholders.isEmpty()) {
				query = helper.getHibSession().createSQLQuery(sqlPlaceholders);
				query.setString("area", acm.getArea());
				query.setString("major", acm.getMajor());
				if (sqlCourses.contains(":catyear"))
					query.setString("catyear", catyear);
				if (sqlPlaceholders.contains(":term"))
					query.setInteger("term", term);
				for (Object[] o: (List<Object[]>)query.list()) {
					String progCode = (String)o[0];
					String progName = (String)o[1];
					String progConc = (String)o[2];
					String placType = (String)o[3];
					String placName = (String)o[4];
					DegreePlanInterface plan = plans.get(progCode);
					if (plan == null) {
						plan = new DegreePlanInterface();
						plan.setId(progCode);
						plan.setName(progName);
						plan.setSchool(acm.getArea());
						plan.setDegree(acm.getMajor());
						plan.setGroup(new DegreeGroupInterface());
						plan.getGroup().setChoice(false);
						if (progConc != null && !progConc.isEmpty())
							plan.setDegree(acm.getMajor() + "/" + progConc);
						else
							plan.setActive(true);
						plans.put(progCode, plan);
					}
					List<XCourse> phc = getPlaceHolderCourses(server, helper, placType, placName);
					if (phc != null && !phc.isEmpty()) {
						DegreePlanInterface.DegreeGroupInterface phg = new DegreePlanInterface.DegreeGroupInterface();
						phg.setChoice(true);
						phg.setPlaceHolder(true);
						phg.setDescription(placName);
						phg.setId(placName);
						phg.setCritical(isCriticalPlaceholder(placName));
						DegreePlanInterface.DegreeCourseInterface course = null;
						for (XCourse xc: phc) {
							if (course == null || !course.getSubject().equals(xc.getSubjectArea()) || !xc.getCourseNumber().startsWith(course.getCourse())) {
								course = new DegreePlanInterface.DegreeCourseInterface();
								course.setSubject(iExternalTermProvider.getExternalSubject(server.getAcademicSession(), xc.getSubjectArea(), xc.getCourseNumber()));
								course.setCourse(iExternalTermProvider.getExternalCourseNumber(server.getAcademicSession(), xc.getSubjectArea(), xc.getCourseNumber()));
								course.setTitle(xc.getTitle());
								course.setId(placName + "-" + xc.getCourseId());
								course.setCourseId(xc.getCourseId());
								course.setSelected(false);
								phg.addCourse(course);
							}
							CourseAssignment ca = new CourseAssignment();
							ca.setCourseId(xc.getCourseId());
							ca.setSubject(xc.getSubjectArea());
							ca.setCourseNbr(xc.getCourseNumber());
							ca.setTitle(xc.getTitle());
							ca.setNote(xc.getNote());
							ca.setCreditAbbv(xc.getCreditAbbv());
							ca.setCreditText(xc.getCreditText());
							ca.setTitle(xc.getTitle());
							ca.setHasUniqueName(xc.hasUniqueName());
							ca.setLimit(xc.getLimit());
							int firstChoiceReqs = 0;
							int enrl = 0;
							Collection<XCourseRequest> requests = server.getRequests(xc.getOfferingId());
							if (requests != null)
								for (XCourseRequest r: requests) {
									if (r.getEnrollment() != null && r.getEnrollment().getCourseId().equals(xc.getCourseId())) enrl ++;
									if (!r.isAlternative() && r.getEnrollment() == null && r.getCourseIds().get(0).equals(xc)) firstChoiceReqs ++;
								}
							ca.setEnrollment(enrl);
							ca.setProjected(firstChoiceReqs);
							course.addCourse(ca);
						}
						plan.getGroup().addGroup(phg);
					} else {
						DegreePlaceHolderInterface placeholder = new DegreePlaceHolderInterface();
						placeholder.setType(placType);
						placeholder.setName(placName);
						plan.getGroup().addPlaceHolder(placeholder);
					}
				}
			}
		}
		List<DegreePlanInterface> ret = new ArrayList<DegreePlanInterface>(plans.values());
		Collections.sort(ret, new Comparator<DegreePlanInterface>(){
			@Override
			public int compare(DegreePlanInterface p1, DegreePlanInterface p2) {
				return p1.getName().compareToIgnoreCase(p2.getName());
			}
		});
		if (action != null) 
			for (DegreePlanInterface plan: ret) {
				String value = "";
				if (plan.getGroup().hasCourses())
					for (DegreeCourseInterface c: plan.getGroup().getCourses()) {
						value += (value.isEmpty() ? "" : "\n") + c.getCourseName();
					}
				if (plan.getGroup().hasGroups())
					for (DegreeGroupInterface g: plan.getGroup().getGroups()) {
						value += (value.isEmpty() ? "" : "\n") + g;
					}
				if (plan.getGroup().hasPlaceHolders())
					for (DegreePlaceHolderInterface p: plan.getGroup().getPlaceHolders()) {
						value += (value.isEmpty() ? "" : "\n") + p.getName();
					}
				if (plan.isActive())
					helper.info(value);
				action.addOptionBuilder().setKey(plan.getDegree()).setValue(value);
			}
		return ret;
	}
}