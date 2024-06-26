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
package org.unitime.timetable.server.reservation;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.springframework.web.util.HtmlUtils;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideType;
import org.unitime.timetable.gwt.shared.ReservationInterface.ReservationFilterRpcRequest;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseReservation;
import org.unitime.timetable.model.CurriculumOverrideReservation;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.GroupOverrideReservation;
import org.unitime.timetable.model.IndividualOverrideReservation;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.LearningCommunityReservation;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.UniversalOverrideReservation;
import org.unitime.timetable.model.dao.ReservationDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.FilterBoxBackend;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ReservationFilterRpcRequest.class)
public class ReservationFilterBackend extends FilterBoxBackend<ReservationFilterRpcRequest> {
	public static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	public static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public FilterRpcResponse execute(ReservationFilterRpcRequest request, SessionContext context) {
		context.checkPermission(Right.Reservations);
		return super.execute(request, context);
	}

	@Override
	public void load(ReservationFilterRpcRequest request, FilterRpcResponse response, SessionContext context) {
		ReservationQuery query = getQuery(request, context);
		
		Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date today = cal.getTime();

		org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();
		
		Map<String, Integer> type2count = new HashMap<String, Integer>();
		for (Object[] o: (List<Object[]>)query.select("type(r), count(distinct r)").group("type(r)").exclude("type").exclude("override").query(hibSession).list()) {
			String type = ((Class)o[0]).getSimpleName();
			int count = ((Number)o[1]).intValue();
			type2count.put(type, count);
		}
		Entity individualType = new Entity(Long.valueOf(0), "Individual", MESSAGES.reservationIndividualAbbv(), "translated-value", MESSAGES.reservationIndividualAbbv());
		Integer individualCnt = type2count.get(IndividualReservation.class.getSimpleName());
		if (individualCnt != null)
			individualType.setCount(individualCnt);
		Integer individualOverrideCnt = type2count.get(IndividualOverrideReservation.class.getSimpleName());
		if (individualOverrideCnt != null)
			individualType.setCount(individualType.getCount() + individualOverrideCnt);
		response.add("type", individualType);
		Entity groupType = new Entity(Long.valueOf(0), "Group", MESSAGES.reservationStudentGroupAbbv(), "translated-value", MESSAGES.reservationStudentGroupAbbv());
		Integer groupCnt = type2count.get(StudentGroupReservation.class.getSimpleName());
		if (groupCnt != null)
			groupType.setCount(groupCnt);
		Integer groupOverrideCnt = type2count.get(GroupOverrideReservation.class.getSimpleName());
		if (groupOverrideCnt != null)
			groupType.setCount(groupType.getCount() + groupOverrideCnt);
		response.add("type", groupType);
		Entity lcType = new Entity(Long.valueOf(0), "LC", MESSAGES.reservationLearningCommunityAbbv(), "translated-value", MESSAGES.reservationLearningCommunityAbbv());
		Integer lcCnt = type2count.get(LearningCommunityReservation.class.getSimpleName());
		if (lcCnt != null)
			lcType.setCount(lcCnt);
		response.add("type", lcType);
		Entity curriculumType = new Entity(Long.valueOf(0), "Curriculum", MESSAGES.reservationCurriculumAbbv(), "translated-value", MESSAGES.reservationCurriculumAbbv());
		Integer curriculumCnt = type2count.get(CurriculumReservation.class.getSimpleName());
		if (curriculumCnt != null)
			curriculumType.setCount(curriculumCnt);
		Integer curriculumOverrideCnt = type2count.get(CurriculumOverrideReservation.class.getSimpleName());
		if (curriculumOverrideCnt != null)
			curriculumType.setCount(curriculumType.getCount() + curriculumOverrideCnt);
		response.add("type", curriculumType);
		Entity courseType = new Entity(Long.valueOf(0), "Course", MESSAGES.reservationCourseAbbv(), "translated-value", MESSAGES.reservationCourseAbbv());
		Integer courseCnt = type2count.get(CourseReservation.class.getSimpleName());
		if (courseCnt != null)
			courseType.setCount(courseCnt);
		Entity overrideType = new Entity(Long.valueOf(0), "Override", MESSAGES.reservationOverrideAbbv(), "translated-value", MESSAGES.reservationOverrideAbbv());
		Integer overrideCnt = type2count.get(OverrideReservation.class.getSimpleName());
		if (overrideCnt != null)
			overrideType.setCount(overrideCnt);
		response.add("type", overrideType);
		Entity universalType = new Entity(Long.valueOf(0), "Universal", MESSAGES.reservationUniversalAbbv(), "translated-value", MESSAGES.reservationUniversalAbbv());
		Integer univestalCnt = type2count.get(UniversalOverrideReservation.class.getSimpleName());
		if (univestalCnt != null)
			universalType.setCount(univestalCnt);
		response.add("type", universalType);

		Map<Long, Integer> dept2count = new HashMap<Long, Integer>();
		for (Object[] o: (List<Object[]>)query.select("co.subjectArea.department.uniqueId, count(distinct r)").group("co.subjectArea.department.uniqueId").exclude("department").exclude("subject").query(hibSession).list()) {
			Long type = (Long)o[0];
			int count = ((Number)o[1]).intValue();
			dept2count.put(type, count);
		}
		TreeSet<Entity> depts = new TreeSet<Entity>();
		for (Department department: Department.getUserDepartments(context.getUser())) {
			Integer count = dept2count.get(department.getUniqueId());
			if (count == null) continue;
			Entity dept = new Entity(department.getUniqueId(), department.getDeptCode(), department.getDeptCode() + " - " + department.getName() + (department.isExternalManager() ? " (" + department.getExternalMgrLabel() + ")" : ""));
			dept.setCount(count);
			depts.add(dept);
		}
		response.add("department", depts);

		Map<Long, Integer> subject2count = new HashMap<Long, Integer>();
		for (Object[] o: (List<Object[]>)query.select("co.subjectArea.uniqueId, count(distinct r)").group("co.subjectArea.uniqueId").exclude("department").exclude("subject").query(hibSession).list()) {
			Long type = (Long)o[0];
			int count = ((Number)o[1]).intValue();
			subject2count.put(type, count);
		}
		TreeSet<Entity> subjects = new TreeSet<Entity>();
		for (SubjectArea area: SubjectArea.getUserSubjectAreas(context.getUser())) {
			Integer count = subject2count.get(area.getUniqueId());
			if (count == null) continue;
			Entity subject = new Entity(area.getUniqueId(), area.getSubjectAreaAbbreviation(), area.getSubjectAreaAbbreviation() + " - " + HtmlUtils.htmlUnescape(area.getTitle()));
			subject.setCount(count);
			subjects.add(subject);
		}
		response.add("subject", subjects);
		
		Entity all = new Entity(0l, "All", CONSTANTS.reservationModeLabel()[0], "translated-value", CONSTANTS.reservationModeAbbv()[0]);
		all.setCount(((Number)query.select("count(distinct r)").exclude("mode").query(hibSession).uniqueResult()).intValue());
		response.add("mode", all);
		Entity expired = new Entity(1l, "Expired", CONSTANTS.reservationModeLabel()[1], "translated-value", CONSTANTS.reservationModeAbbv()[1]);
		expired.setCount(((Number)query.select("count(distinct r)").exclude("mode").where("r.expirationDate < :today or :today < r.startDate").set("today", today).query(hibSession).uniqueResult()).intValue());
		response.add("mode", expired);
		Entity notExpired = new Entity(2l, "Not Expired", CONSTANTS.reservationModeLabel()[2], "translated-value", CONSTANTS.reservationModeAbbv()[2]);
		notExpired.setCount(all.getCount() - expired.getCount());
		response.add("mode", notExpired);
		
		if (request.hasOptions("type") && request.getOptions("type").contains("Curriculum")) {
			Map<Long, Entity> areas = new HashMap<Long, Entity>();
			for (Reservation reservation: (List<Reservation>)query.select("distinct r").where("type(r) in (CurriculumReservation, CurriculumOverrideReservation)").exclude("area").query(hibSession).list()) {
				for (AcademicArea academicArea: ((CurriculumReservation)reservation).getAreas()) {
					Entity area = areas.get(academicArea.getUniqueId());
					if (area == null) {
						area = new Entity(academicArea.getUniqueId(), academicArea.getAcademicAreaAbbreviation(),
								Constants.curriculaToInitialCase(academicArea.getTitle()));
						areas.put(area.getUniqueId(), area);
					}
					area.incCount();	
				}
			}
			response.add("area", new TreeSet<Entity>(areas.values()));
		}
		
		if (request.hasOptions("type") && request.getOptions("type").contains("Override")) {
			List<Entity> types = new ArrayList<Entity>();
			for (Object[] typeAndCount: (List<Object[]>)query.select("r.type, count(distinct r)").where("type(r) = OverrideReservation").group("r.type").order("r.type").exclude("override").query(hibSession).list()) {
				OverrideType type = OverrideType.values()[((Number)typeAndCount[0]).intValue()];
				Entity e = new Entity(Long.valueOf(type.ordinal()), type.getReference(), CONSTANTS.reservationOverrideTypeAbbv()[type.ordinal()]);
				e.setCount(((Number)typeAndCount[1]).intValue());
				types.add(e);
			}
			response.add("override", types);
		}
		
		if (request.hasOptions("type") && (request.getOptions("type").contains("Group") || request.getOptions("type").contains("LC"))) {
			boolean gr = request.getOptions("type").contains("Group");
			boolean lc = request.getOptions("type").contains("LC");
			Map<Long, Entity> groups = new HashMap<Long, Entity>();
			for (Reservation reservation: (List<Reservation>)query.select("distinct r").where(lc ? (gr ? "type(r) in (StudentGroupReservation, LearningCommunityReservation)" : "type(r) = LearningCommunityReservation") : "type(r) = StudentGroupReservation").exclude("group").query(hibSession).list()) {
				StudentGroup studentGroup = ((StudentGroupReservation)reservation).getGroup();
				Entity group = groups.get(studentGroup.getUniqueId());
				if (group == null) {
					group = new Entity(studentGroup.getUniqueId(), studentGroup.getGroupAbbreviation(), Constants.curriculaToInitialCase(studentGroup.getGroupName()));
					groups.put(group.getUniqueId(), group);
				}
				group.incCount();
			}
			response.add("group", new TreeSet<Entity>(groups.values()));
		}
	}

	@Override
	public void suggestions(ReservationFilterRpcRequest request, FilterRpcResponse response, SessionContext context) {
		org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();

		ReservationQuery query = getQuery(request, context);
		
		if (request.hasText()) {
			for (CourseOffering course: (List<CourseOffering>)query.select("distinct co")
					//.where("type(r) != CourseReservation or co = r.course")
					.where("lower(co.subjectAreaAbbv || ' ' || co.courseNbr) like :x")
					.set("x", request.getText().toLowerCase() + "%")
					.order("co.subjectAreaAbbv, co.courseNbr")
					.limit(20).query(hibSession).list()) {
				response.addSuggestion(course.getCourseName(), course.getCourseName(), course.getTitle() == null ? MESSAGES.reservationCourseAbbv() : course.getTitle());
			}
		}
		
		if (!response.hasSuggestions()) {
			Set<SubjectArea> subjects = SubjectArea.getUserSubjectAreas(context.getUser()); 
			Query q = new Query(request.getText());
			TreeSet<Entity> suggestions = new TreeSet<Entity>();
			
			for (Reservation reservation: (List<Reservation>)query.select("distinct r").query(hibSession).list()) {
				if (!q.match(new ReservationMatcher(reservation))) continue;
				
				for (CourseOffering course: reservation.getInstructionalOffering().getCourseOfferings()) {
					if (subjects.contains(course.getSubjectArea())) {
						suggestions.add(new Entity(0l, course.getCourseName(), course.getCourseName(), "hint", course.getTitle() == null ? MESSAGES.reservationCourseAbbv() : course.getTitle()));
					}
				}
				
			}
			
			for (Entity suggestion: suggestions) {
				response.addSuggestion(suggestion.getName(), suggestion.getName(), suggestion.getProperty("hint", MESSAGES.reservationCourseAbbv()));
				if (response.getSuggestions().size() == 20) break;
			}
		}
		
		if (!request.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20) && request.hasOptions("type") && (request.getOptions("type").contains("Individual") || request.getOptions("type").contains("Override"))) {
			ReservationQuery.ReservationQueryInstance instance = query.select("distinct s").from("inner join r.instructionalOffering.courseOfferings co inner join r.students s").exclude("student");
			int id = 0;
			for (StringTokenizer s=new StringTokenizer(request.getText().trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken().toUpperCase();
                instance.where("upper(s.firstName) like :cn" + id + " || '%' or upper(s.middleName) like :cn" + id + " || '%' or upper(s.lastName) like :cn" + id + " || '%' or s.externalUniqueId = :cn" + id).set("cn" + id, token);
            }
			for (Student student: (List<Student>)instance.limit(20).query(hibSession).list())
				response.addSuggestion(student.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle), student.getExternalUniqueId(), "Student", "student");
		}
	}

	@Override
	public void enumarate(ReservationFilterRpcRequest request, FilterRpcResponse response, SessionContext context) {
		for (Reservation reservation: reservations(request, context)) {
			CourseOffering course = reservation.getInstructionalOffering().getControllingCourseOffering();
			if (reservation instanceof CourseReservation)
				course = ((CourseReservation)reservation).getCourse();
			response.addResult(new Entity(reservation.getUniqueId(), course.getCourseName(), course.getCourseNameWithTitle()));
		}
	}
	
	public static List<Reservation> reservations(ReservationFilterRpcRequest request, SessionContext context) {
		List<Reservation> ret = new ArrayList<Reservation>();
		
		if (request.getSessionId() == null) request.setSessionId(context.getUser().getCurrentAcademicSessionId());
		
		org.hibernate.Session hibSession = ReservationDAO.getInstance().getSession();
		
		if (request.hasText())
			request.setOption("course", request.getText());
		
		String fetch = "inner join fetch r.instructionalOffering io inner join io.courseOfferings co " +
				"left join fetch r.classes xclz left join fetch r.configurations xcfg";
		
		for (Reservation reservation: (List<Reservation>)getQuery(request, context).select("distinct r").from(fetch).query(hibSession).list())
			ret.add(reservation);
		
		if (ret.isEmpty() && request.hasText()) {
			Query query = new Query(request.getText());
			
			for (Reservation reservation: (List<Reservation>)getQuery(request, context).select("distinct r").from(fetch).exclude("course").query(hibSession).list()) {
				if (query.match(new ReservationMatcher(reservation)))
					ret.add(reservation);
			}	
		}
		
		return ret;
	}
	
	public static ReservationQuery getQuery(ReservationFilterRpcRequest request, SessionContext context) {
		ReservationQuery query = new ReservationQuery(request.getSessionId());
		
		if (request.hasOptions("type")) {
			String type = "";
			for (String t: request.getOptions("type")) {
				if (!type.isEmpty()) type += ",";
				if ("individual".equalsIgnoreCase(t))
					type += "IndividualReservation,IndividualOverrideReservation";
				if ("group".equalsIgnoreCase(t))
					type += "StudentGroupReservation,GroupOverrideReservation";
				if ("course".equalsIgnoreCase(t))
					type += "CourseReservation";
				if ("curriculum".equalsIgnoreCase(t))
					type += "CurriculumReservation,CurriculumOverrideReservation";
				if ("override".equalsIgnoreCase(t))
					type += "OverrideReservation";
				if ("lc".equalsIgnoreCase(t))
					type += "LearningCommunityReservation";
				if ("universal".equalsIgnoreCase(t))
					type += "UniversalOverrideReservation";
					
			}
			query.addWhere("type", "type(r) " + (type.indexOf(',') < 0 ? "= " + type : "in (" + type + ")"));
		}
		
		if (request.hasOptions("override")) {
			String override = "";
			for (String o: request.getOptions("override")) {
				OverrideType type = null;
				for (OverrideType t: OverrideType.values())
					if (t.getReference().equals(o)) { type = t; break; }
				if (type == null) continue;
				if (!override.isEmpty()) override += ",";
				override += type.ordinal();
			}
			query.addWhere("override", "r.type " + (override.indexOf(',') < 0 ? "= " + override : "in (" + override + ")"));
		}
		
		if (request.hasOption("department")) {
			query.addWhere("department", "co.subjectArea.department.deptCode = :deptCode");
			query.addParameter("department", "deptCode", request.getOption("department"));
		} else if (!context.hasPermission(Right.DepartmentIndependent)) {
			boolean external = false;
			Set<Department> departments = Department.getUserDepartments(context.getUser());
			for (Department department: departments) {
				if (department.isExternalManager()) { external = true; break; }
			}
			if (!external) {
				String deptIds = "";
				int id = 0;
				for (Department department: departments) {
					deptIds += (deptIds.isEmpty() ? "" : ",") + ":deptId" + id;
					query.addParameter("department", "deptId" + id, department.getUniqueId());
				}
				if (deptIds.isEmpty()) 
					query.addWhere("department", "1 = 0");
				else
					query.addWhere("department", "co.subjectArea.department.uniqueId " + (id == 1 ? "= " + deptIds : "in (" + deptIds + ")"));
			}
		}
		
		if (request.hasOptions("subject")) {
			String subjects = "";
			int id = 0;
			for (String t: request.getOptions("subject")) {
				subjects += (subjects.isEmpty() ? "" : ",") + ":subject" + id;
				query.addParameter("subject", "subject" + id, t);
				id ++;
			}
			query.addWhere("subject", "co.subjectArea.subjectAreaAbbreviation " + (id == 1 ? "= " + subjects : "in (" + subjects + ")"));
		}
		
		Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date today = cal.getTime();
		
		if (request.hasOption("before")) {
			Date date = null;
			if ("today".equals(request.getOption("before"))) {
				date = today;
			} else {
				try {
					int dayOfYear = Integer.parseInt(request.getOption("before"));
					date = DateUtils.getDate(SessionDAO.getInstance().get(request.getSessionId()).getSessionStartYear(), dayOfYear);
				} catch (NumberFormatException f) {
					try {
						date = Formats.getDateFormat(Formats.Pattern.FILTER_DATE).parse(request.getOption("before"));
					} catch (ParseException p) {}
				}
			}
			if (date != null) {
				query.addParameter("before", "before", date);
				query.addWhere("before", "r.expirationDate < :before");
			}
		}
		if (request.hasOption("after")) {
			Date date = null;
			if ("today".equals(request.getOption("after"))) {
				date = today;
			} else {
				try {
					int dayOfYear = Integer.parseInt(request.getOption("after"));
					date = DateUtils.getDate(SessionDAO.getInstance().get(request.getSessionId()).getSessionStartYear(), dayOfYear);
				} catch (NumberFormatException f) {
					try {
						date = Formats.getDateFormat(Formats.Pattern.FILTER_DATE).parse(request.getOption("after"));
					} catch (ParseException p) {}
				}
			}
			if (date != null) {
				query.addParameter("after", "after", date);
				query.addWhere("after", "r.expirationDate is null or r.expirationDate >= :after");
			}
		}
		
		if (request.hasOption("mode")) {
			String mode = request.getOption("mode");
			if ("Expired".equalsIgnoreCase(mode)) {
				query.addWhere("mode", "r.expirationDate < :today or :today < r.startDate");
				query.addParameter("mode", "today", today);  
			} else if ("Not Expired".equalsIgnoreCase(mode)) {
				query.addWhere("mode", "(r.expirationDate is null or r.expirationDate >= :today) and (r.startDate is null or r.startDate <= :today)");
				query.addParameter("mode", "today", today);  
			}
		}
		
		if (request.hasOptions("area")) {
			String areas = "";
			int id = 0;
			for (String t: request.getOptions("area")) {
				areas += (areas.isEmpty() ? "" : ",") + ":area" + id;
				query.addParameter("area", "area" + id, t);
				id ++;
			}
			query.addWhere("area", "xarea.academicAreaAbbreviation " + (id == 1 ? "= " + areas : "in (" + areas + ")"));
			query.addJoin("area", "r.areas xarea");
		}
		
		if (request.hasOptions("group")) {
			String groups = "";
			int id = 0;
			for (String t: request.getOptions("group")) {
				groups += (groups.isEmpty() ? "" : ",") + ":group" + id;
				query.addParameter("group", "group" + id, t);
				id ++;
			}
			query.addWhere("group", "r.group.groupAbbreviation " + (id == 1 ? "= " + groups : "in (" + groups + ")"));
		}
		
		if (request.hasOptions("student")) {
			query.addJoin("student", "r.students s");			
			String student = "";
			int id = 0;
			for (StringTokenizer s=new StringTokenizer(request.getOption("student").trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken().toUpperCase();
                student += (student.isEmpty() ? "" : " and ") + "(upper(s.firstName) like :std" + id + " || '%' or " + "upper(s.middleName) like :std" + id + " || '%' or upper(s.lastName) like :std" + id + " || '%' or s.externalUniqueId = :std" + id + ")";
                query.addParameter("student", "std" + id, token);
                id++;
            }
			query.addWhere("student", student);
		}
		
		if (request.hasOptions("course")) {
			String courses = "";
			int id = 0;
			for (String t: request.getOptions("course")) {
				courses += (courses.isEmpty() ? "" : ",") + ":course" + id;
				query.addParameter("course", "course" + id, t.toLowerCase());
				id ++;
			}
			query.addWhere("course", "lower(co.subjectArea.subjectAreaAbbreviation || ' ' || co.courseNbr) " + (id == 1 ? "= " + courses : "in (" + courses + ")"));
		}
		
		return query;
	}

	public static class ReservationQuery {
		private Long iSessionId;
		private Map<String, String> iJoin = new HashMap<String, String>();
		private Map<String, String> iFrom = new HashMap<String, String>();
		private Map<String, String> iWhere = new HashMap<String, String>();
		private Map<String, Map<String, Object>> iParams = new HashMap<String, Map<String,Object>>();
		
		public ReservationQuery(Long sessionId) {
			iSessionId = sessionId;
		}
		
		public void addFrom(String option, String from) { iFrom.put(option, from); }
		public void addWhere(String option, String where) { iWhere.put(option, where); }
		public void addJoin(String option, String from) { iJoin.put(option, from); }

		private void addParameter(String option, String name, Object value) {
			Map<String, Object> params = iParams.get(option);
			if (params == null) { params = new HashMap<String, Object>(); iParams.put(option, params); }
			params.put(name, value);
		}
		
		public String getFrom(Collection<String> excludeOption) {
			String from = "";
			for (Map.Entry<String, String> entry: iFrom.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				from += ", " + entry.getValue();
			}
			return from;
		}
		
		public String getJoin(Collection<String> excludeOption) {
			String join = "";
			for (Map.Entry<String, String> entry: iJoin.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				join += " inner join " + entry.getValue();
			}
			return join;
		}
		
		public String getWhere(Collection<String> excludeOption) {
			String where = "";
			for (Map.Entry<String, String> entry: iWhere.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				where += " and (" + entry.getValue() + ")";
			}
			return where;
		}
		
		public org.hibernate.query.Query setParams(org.hibernate.query.Query query, Collection<String> excludeOption) {
			for (Map.Entry<String, Map<String, Object>> entry: iParams.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				for (Map.Entry<String, Object> param: entry.getValue().entrySet()) {
					if (param.getValue() instanceof Integer) {
						query.setParameter(param.getKey(), (Integer)param.getValue());
					} else if (param.getValue() instanceof Long) {
						query.setParameter(param.getKey(), (Long)param.getValue());
					} else if (param.getValue() instanceof String) {
						query.setParameter(param.getKey(), (String)param.getValue());
					} else if (param.getValue() instanceof Boolean) {
						query.setParameter(param.getKey(), (Boolean)param.getValue());
					} else if (param.getValue() instanceof Date) {
						query.setParameter(param.getKey(), (Date)param.getValue());
					} else if (param.getValue() instanceof List) {
						List<?> list = (List<?>)param.getValue();
						if (!list.isEmpty() && list.get(0) instanceof Long)
							query.setParameterList(param.getKey(), list, Long.class);
						else if (!list.isEmpty() && list.get(0) instanceof String)
							query.setParameterList(param.getKey(), list, String.class);
						else
							query.setParameterList(param.getKey(), list);
					} else {
						query.setParameter(param.getKey(), param.getValue().toString());
					}
				}
			}
			return query;
		}
		
		public ReservationQueryInstance select(String select) {
			return new ReservationQueryInstance(select);
		}
		
		public class ReservationQueryInstance {
			private String iSelect = null, iFrom = "inner join r.instructionalOffering.courseOfferings co", iWhere = null, iOrderBy = null, iGroupBy = null, iType = "Reservation";
			private Integer iLimit = null;
			private Set<String> iExclude = new HashSet<String>();
			private Map<String, Object> iParams = new HashMap<String, Object>();
			
			private ReservationQueryInstance(String select) {
				iSelect = select;
			}
			
			public ReservationQueryInstance from(String from) { iFrom = from; return this; }
			public ReservationQueryInstance where(String where) { 
				if (iWhere == null)
					iWhere = "(" + where + ")";
				else
					iWhere += " and (" + where + ")";
				return this;
			}
			public ReservationQueryInstance type(String type) { iType = type; return this; }
			public ReservationQueryInstance order(String orderBy) { iOrderBy = orderBy; return this; }
			public ReservationQueryInstance group(String groupBy) { iGroupBy = groupBy; return this; }
			public ReservationQueryInstance exclude(String excludeOption) { iExclude.add(excludeOption); return this; }
			public ReservationQueryInstance set(String param, Object value) { iParams.put(param, value); return this; }
			public ReservationQueryInstance limit(Integer limit) { iLimit = (limit == null || limit <= 0 ? null : limit); return this; }
			
			public String query() {
				return
					"select " + (iSelect == null ? "distinct r" : iSelect) + " from " + iType + " r " +
					getJoin(iExclude) +
					(iFrom == null ? "" : iFrom.trim().toLowerCase().startsWith("inner join") ? " " + iFrom : ", " + iFrom) + getFrom(iExclude) +
					" where r.instructionalOffering.session.uniqueId = :sessionId" +
					getWhere(iExclude) + (iWhere == null ? "" : " and (" + iWhere + ")") +
					(iGroupBy == null ? "" : " group by " + iGroupBy) +
					(iOrderBy == null ? "" : " order by " + iOrderBy);
			}
			
			public org.hibernate.query.Query query(org.hibernate.Session hibSession) {
				@SuppressWarnings("deprecation")
				org.hibernate.query.Query query = setParams(hibSession.createQuery(query()), iExclude);
				query.setParameter("sessionId", iSessionId);
				query.setCacheable(true);
				for (Map.Entry<String, Object> param: iParams.entrySet()) {
					if (param.getValue() instanceof Integer) {
						query.setParameter(param.getKey(), (Integer)param.getValue());
					} else if (param.getValue() instanceof Long) {
						query.setParameter(param.getKey(), (Long)param.getValue());
					} else if (param.getValue() instanceof String) {
						query.setParameter(param.getKey(), (String)param.getValue());
					} else if (param.getValue() instanceof Boolean) {
						query.setParameter(param.getKey(), (Boolean)param.getValue());
					} else if (param.getValue() instanceof Date) {
						query.setParameter(param.getKey(), (Date)param.getValue());
					} else if (param.getValue() instanceof List) {
						List<?> list = (List<?>)param.getValue();
						if (!list.isEmpty() && list.get(0) instanceof Long)
							query.setParameterList(param.getKey(), list, Long.class);
						else if (!list.isEmpty() && list.get(0) instanceof String)
							query.setParameterList(param.getKey(), list, String.class);
						else
							query.setParameterList(param.getKey(), list);
					} else {
						query.setParameter(param.getKey(), param.getValue().toString());
					}
				}
				if (iLimit != null)
					query.setMaxResults(iLimit);
				return query;
			}
		}
	}
	
	private static class ReservationMatcher implements Query.TermMatcher {
		private Reservation iReservation;
		private Date iExpDate;
		private Formats.Format<Date> iDateFormat = null;
		
		private ReservationMatcher(Reservation r) {
			iReservation = r;
			Calendar c = Calendar.getInstance(Locale.US);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			iExpDate = c.getTime();
		}
		
		private String format(Date date) {
			if (iDateFormat == null)
				iDateFormat = Formats.getDateFormat(Formats.Pattern.FILTER_DATE);
			return iDateFormat.format(date);
		}
		
		private Date parse(String date) throws ParseException {
			if (iDateFormat == null)
				iDateFormat = Formats.getDateFormat(Formats.Pattern.FILTER_DATE);
			return iDateFormat.parse(date);
		}
		
		public boolean match(String attr, String term) {
			if (term.isEmpty()) return true;
			if (attr == null || "course".equals(attr)) {
				for (CourseOffering co: iReservation.getInstructionalOffering().getCourseOfferings()) {
					if (eq(co.getCourseName(), term) || has(co.getCourseName(), term)) return true;
				}
			}
			if ("dept".equals(attr) || "department".equals(attr)) {
				Department d = iReservation.getInstructionalOffering().getDepartment();
				if (eq(d.getDeptCode(), term) || eq(d.getAbbreviation(), term) || has(d.getName(), term)) return true;
			}
			if ("subject".equals(attr) || "subj".equals(attr)) {
				for (CourseOffering co: iReservation.getInstructionalOffering().getCourseOfferings()) {
					if (eq(co.getSubjectAreaAbbv(), term) || has(co.getSubjectArea().getTitle(), term)) return true;
				}
			}
			if ("type".equals(attr)) {
				if (iReservation instanceof OverrideReservation && "override".equalsIgnoreCase(term)) return true;
				if (iReservation instanceof IndividualReservation && !(iReservation instanceof OverrideReservation) && "individual".equalsIgnoreCase(term)) return true;
				if (iReservation instanceof StudentGroupReservation && !(iReservation instanceof LearningCommunityReservation) && "group".equalsIgnoreCase(term)) return true;
				if (iReservation instanceof LearningCommunityReservation && "lc".equalsIgnoreCase(term)) return true;
				if (iReservation instanceof CourseReservation && "course".equalsIgnoreCase(term)) return true;
				if (iReservation instanceof CurriculumReservation && "curriculum".equalsIgnoreCase(term)) return true;
			}
			if ("override".equals(attr)) {
				if (iReservation instanceof OverrideReservation && OverrideType.values()[((OverrideReservation)iReservation).getType()].getReference().equalsIgnoreCase(term)) return true;
			}
			if ("group".equals(attr)) {
				if (iReservation instanceof StudentGroupReservation) {
					StudentGroupReservation gr = (StudentGroupReservation)iReservation;
					if (eq(gr.getGroup().getGroupAbbreviation(), term) || has(gr.getGroup().getGroupName(), term)) return true;
				}
			}
			if ("student".equals(attr)) {
				if (iReservation instanceof IndividualReservation) {
					IndividualReservation ir = (IndividualReservation)iReservation;
					for (Student s: ir.getStudents()) {
						if (has(s.getName(DepartmentalInstructor.sNameFormatFirstMiddleLast), term) || eq(s.getExternalUniqueId(), term)) return true;
					}
				}
			}
			if ("area".equals(attr)) {
				if (iReservation instanceof CurriculumReservation) {
					CurriculumReservation cr = (CurriculumReservation)iReservation;
					for (AcademicArea area: cr.getAreas())
						if (eq(area.getAcademicAreaAbbreviation(), term) || has(area.getTitle(), term))
							return true;
				}
			}
			if ("class".equals(attr)) {
				for (Class_ c: iReservation.getClasses()) {
					if (eq(c.getClassLabel(), term) || has(c.getClassLabel(), term) || eq(c.getClassSuffix(), term)) return true;
				}
			}
			if ("config".equals(attr)) {
				for (InstrOfferingConfig c: iReservation.getConfigurations()) {
					if (eq(c.getName(), term) || has(c.getName(), term)) return true;
				}
			}
			if (attr == null && "expired".equalsIgnoreCase(term)) {
				if (iReservation.getExpirationDate() != null && iReservation.getExpirationDate().before(iExpDate)) {
					return true;
				}
				if (iReservation.getStartDate() != null && iExpDate.before(iReservation.getStartDate())) {
					return true;
				}
			}
			if (attr == null || "expiration".equals(attr) || "exp".equals(attr)) {
				if (iReservation.getExpirationDate() != null && eq(format(iReservation.getExpirationDate()), term)) return true;
			}
			if (attr == null || "start".equals(attr)) {
				if (iReservation.getStartDate() != null && eq(format(iReservation.getStartDate()), term)) return true;
			}
			if ("before".equals(attr)) {
				try {
					Date x = ("today".equalsIgnoreCase(term) ? iExpDate : parse(term));
					if (iReservation.getExpirationDate() != null && iReservation.getExpirationDate().before(x)) return true;
				} catch (Exception e) {}
			}
			if ("after".equals(attr)) {
				try {
					Date x = ("today".equalsIgnoreCase(term) ? iExpDate : parse(term));
					if (iReservation.getExpirationDate() == null || iReservation.getExpirationDate().after(x)) return true;
				} catch (Exception e) {}
			}
			return false;
		}
		
		private boolean eq(String name, String term) {
			if (name == null) return false;
			return name.equalsIgnoreCase(term);
		}

		private boolean has(String name, String term) {
			if (name == null) return false;
			for (String t: name.split(" "))
				if (t.equalsIgnoreCase(term)) return true;
			return false;
		}
	
	}
}
