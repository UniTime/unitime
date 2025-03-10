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
package org.unitime.timetable.onlinesectioning.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroupType;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentGroupTypeDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseLookupHolder;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.solver.studentsct.StudentSolver;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class SectioningStatusFilterAction implements OnlineSectioningAction<FilterRpcResponse> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	private static StudentSectioningMessages MESSAGES = Localization.create(StudentSectioningMessages.class);
	
	private FilterRpcRequest iRequest = null;
	private boolean iPinOps = false;
	
	public SectioningStatusFilterAction forRequest(FilterRpcRequest request) {
		iRequest = request;
		return this;
	}
	
	public SectioningStatusFilterAction withPinOps(boolean canReleasePin) {
		iPinOps = canReleasePin;
		return this;
	}

	@Override
	public FilterRpcResponse execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		switch (iRequest.getCommand()) {
			case LOAD:
				return load(server, helper);
			case SUGGESTIONS:
				return suggestions(server, helper);
			case ENUMERATE:
				return enumarate(server, helper);
		}
		
		return null;
	}
	
	public FilterRpcResponse load(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		FilterRpcResponse response = new FilterRpcResponse();
		
		StudentQuery query = getQuery(iRequest, server, helper);
		CourseQuery courseQuery = getCourseQuery(iRequest, server, helper);
		
		Map<Long, Entity> areas = new HashMap<Long, Entity>();
		for (Object[] o: (List<Object[]>)query.select("aac.academicArea.uniqueId, aac.academicArea.academicAreaAbbreviation, aac.academicArea.title, count(distinct s.uniqueId)")
				.order("aac.academicArea.academicAreaAbbreviation, aac.academicArea.title").group("aac.academicArea.uniqueId, aac.academicArea.academicAreaAbbreviation, aac.academicArea.title")
				.exclude("area").exclude("major").exclude("concentration").exclude("minor").exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
			Entity a = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[2]);
			a.setCount(((Number)o[3]).intValue());
			areas.put(a.getUniqueId(), a);
		}
		for (Object[] o: (List<Object[]>)query.select("aam.academicArea.uniqueId, aam.academicArea.academicAreaAbbreviation, aam.academicArea.title, count(distinct s.uniqueId)")
				.from("StudentAreaClassificationMinor aam")
				.where("aam.student = s")
				.order("aam.academicArea.academicAreaAbbreviation, aam.academicArea.title").group("aam.academicArea.uniqueId, aam.academicArea.academicAreaAbbreviation, aam.academicArea.title")
				.exclude("area").exclude("major").exclude("concentration").exclude("minor").exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
			Entity a = areas.get((Long)o[0]);
			if (a == null) {
				a = new Entity((Long)o[0], (String)o[1], (String)o[2]);
				a.setCount(((Number)o[3]).intValue());
				areas.put(a.getUniqueId(), a);
			} else {
				a.setCount(a.getCount() + ((Number)o[3]).intValue());
			}
			areas.put(a.getUniqueId(), a);
		}
		response.add("area", new TreeSet<Entity>(areas.values()));
		
		if (iRequest.hasOptions("area")) {
			StudentQuery q = new StudentQuery(query);
			int id = 0;
			String area = "";
			for (String a: iRequest.getOptions("area")) {
				area += (area.isEmpty() ? "" : ",") + ":Xar" + id;
				q.addParameter("area", "Xar" + id, a);
				id++;
			}
			q.addWhere("xxx", "aac.academicArea.academicAreaAbbreviation in (" + area + ")");
			List<Entity> majors = new ArrayList<Entity>();
			for (Object[] o: (List<Object[]>)q.select("aac.major.uniqueId, aac.major.code, aac.major.name, count(distinct s)")
					.order("aac.major.code, aac.major.name").group("aac.major.uniqueId, aac.major.code, aac.major.name")
					.exclude("major").exclude("concentration").exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
				Entity m = new Entity(
						(Long)o[0],
						(String)o[1],
						(String)o[2]);
				m.setCount(((Number)o[3]).intValue());
				majors.add(m);
			}
			response.add("major", majors);
		}
		
		if (iRequest.hasOptions("major")) {
			StudentQuery q = new StudentQuery(query);
			int id = 0;
			String area = "";
			if (iRequest.hasOptions("area"))
				for (String a: iRequest.getOptions("area")) {
					area += (area.isEmpty() ? "" : ",") + ":Xar" + id;
					q.addParameter("area", "Xar" + id, a);
					id++;
				}
			String major = "";
			id = 0;
			for (String a: iRequest.getOptions("major")) {
				major += (major.isEmpty() ? "" : ",") + ":Xmj" + id;
				q.addParameter("major", "Xmj" + id, a);
				id++;
			}
			q.addWhere("xxx", (area.isEmpty() ? "" : "aac.academicArea.academicAreaAbbreviation in (" + area + ") and ") + "aac.major.code in (" + major + ")");
			List<Entity> concentrations = new ArrayList<Entity>();
			for (Object[] o: (List<Object[]>)q.select("aac.concentration.uniqueId, aac.concentration.code, aac.concentration.name, count(distinct s)")
					.order("aac.concentration.code, aac.concentration.name").group("aac.concentration.uniqueId, aac.concentration.code, aac.concentration.name")
					.exclude("concentration").exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
				Entity m = new Entity(
						(Long)o[0],
						(String)o[1],
						(String)o[2]);
				m.setCount(((Number)o[3]).intValue());
				concentrations.add(m);
			}
			response.add("concentration", concentrations);
		}
		
		if (iRequest.hasOptions("area")) {
			StudentQuery q = new StudentQuery(query);
			int id = 0;
			String area = "";
			for (String a: iRequest.getOptions("area")) {
				area += (area.isEmpty() ? "" : ",") + ":Xar" + id;
				q.addParameter("area", "Xar" + id, a);
				id++;
			}
			q.addFrom("area", "StudentAreaClassificationMinor aam");
			q.addWhere("area", "aam.student = s and aam.academicArea.academicAreaAbbreviation in (" + area + ")");
			List<Entity> minors = new ArrayList<Entity>();
			for (Object[] o: (List<Object[]>)q.select("aam.minor.uniqueId, aam.minor.code, aam.minor.name, count(distinct s)")
					.order("aam.minor.code, aam.minor.name").group("aam.minor.uniqueId, aam.minor.code, aam.minor.name")
					.exclude("minor").exclude("classification").exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
				Entity m = new Entity(
						(Long)o[0],
						(String)o[1],
						(String)o[2]);
				m.setCount(((Number)o[3]).intValue());
				minors.add(m);
			}
			response.add("minor", minors);
		}
		
		List<Entity> classifications = new ArrayList<Entity>();
		for (Object[] o: (List<Object[]>)query.select("aac.academicClassification.uniqueId, aac.academicClassification.code, aac.academicClassification.name, count(distinct s)")
				.order("aac.academicClassification.code, aac.academicClassification.name").group("aac.academicClassification.uniqueId, aac.academicClassification.code, aac.academicClassification.name")
				.exclude("classification").exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
			Entity c = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[2]);
			c.setCount(((Number)o[3]).intValue());
			classifications.add(c);
		}
		response.add("classification", classifications);
		
		List<Entity> degrees = new ArrayList<Entity>();
		for (Object[] o: (List<Object[]>)query.select("aac.degree.uniqueId, aac.degree.reference, aac.degree.label, count(distinct s)")
				.order("aac.degree.reference, aac.degree.label").group("aac.degree.uniqueId, aac.degree.reference, aac.degree.label")
				.exclude("degree").exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
			Entity c = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[2]);
			c.setCount(((Number)o[3]).intValue());
			degrees.add(c);
		}
		response.add("degree", degrees);
		
		List<Entity> programs = new ArrayList<Entity>();
		for (Object[] o: (List<Object[]>)query.select("aac.program.uniqueId, aac.program.reference, aac.program.label, count(distinct s)")
				.order("aac.program.reference, aac.program.label").group("aac.program.uniqueId, aac.program.reference, aac.program.label")
				.exclude("program").exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
			Entity c = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[2]);
			c.setCount(((Number)o[3]).intValue());
			programs.add(c);
		}
		response.add("program", programs);
		
		List<Entity> campuses = new ArrayList<Entity>();
		boolean hasDefaultCampus = false;
		for (Object[] o: (List<Object[]>)query.select("aac.campus.uniqueId, aac.campus.reference, aac.campus.label, count(distinct s)")
				.order("aac.campus.reference, aac.campus.label").group("aac.campus.uniqueId, aac.campus.reference, aac.campus.label")
				.exclude("campus").exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
			Entity c = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[2]);
			c.setCount(((Number)o[3]).intValue());
			if (server.getAcademicSession().getCampus().equals(o[1])) hasDefaultCampus = true;
			campuses.add(c);
		}
		if (campuses.size() == 1 && hasDefaultCampus) campuses.clear();
		response.add("campus", campuses);
		
		List<Entity> groups = new ArrayList<Entity>();
		for (Object[] o: (List<Object[]>)query.select("g.uniqueId, g.groupAbbreviation, g.groupName, count(distinct s)")
				.from("inner join s.groups g").where("g.type is null")
				.order("g.groupAbbreviation, g.groupName").group("g.uniqueId, g.groupAbbreviation, g.groupName")
				.exclude("group").exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
			Entity c = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[2]);
			c.setCount(((Number)o[3]).intValue());
			groups.add(c);
		}
		response.add("group", groups);
		
		for (StudentGroupType type: StudentGroupTypeDAO.getInstance().findAll(helper.getHibSession())) {
			List<Entity> groupsOfThisType = new ArrayList<Entity>();
			for (Object[] o: (List<Object[]>)query.select("gt.uniqueId, gt.groupAbbreviation, gt.groupName, count(distinct s)")
					.from("inner join s.groups gt").where("gt.type.uniqueId = :groupTypeId")
					.set("groupTypeId", type.getUniqueId())
					.order("gt.groupAbbreviation, gt.groupName").group("gt.uniqueId, gt.groupAbbreviation, gt.groupName")
					.exclude(type.getReference().replace(' ', '_')).exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
				Entity c = new Entity(
						(Long)o[0],
						(String)o[1],
						(String)o[2]);
				c.setCount(((Number)o[3]).intValue());
				groupsOfThisType.add(c);
			}
			response.add(type.getReference().replace(' ', '_'), groupsOfThisType);
			if (type.getLabel() != null)
				response.setTypeLabel(type.getReference().replace(' ', '_'), type.getLabel());
		}
		
		List<Entity> acc = new ArrayList<Entity>();
		for (Object[] o: (List<Object[]>)query.select("a.uniqueId, a.abbreviation, a.name, count(distinct s)")
				.from("inner join s.accomodations a")
				.order("a.abbreviation, a.name").group("a.uniqueId, a.abbreviation, a.name")
				.exclude("accommodation").exclude("course").exclude("lookup").exclude("prefer").exclude("require").exclude("im").exclude("credit").query(helper.getHibSession()).list()) {
			Entity c = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[2]);
			c.setCount(((Number)o[3]).intValue());
			acc.add(c);
		}
		response.add("accommodation", acc);
		
		List<Entity> states = new ArrayList<Entity>();
		int defaultStatus = ((Number)query.select("count(distinct s)").where("s.sectioningStatus is null").exclude("status").exclude("credit").query(helper.getHibSession()).uniqueResult()).intValue();
		if (defaultStatus > 0) {
			Session session = SessionDAO.getInstance().get(server.getAcademicSession().getUniqueId(), helper.getHibSession());
			Entity s;
			if (session.getDefaultSectioningStatus() == null) {
				s = new Entity(0l, "Not Set", "No Restrictions (Default)");
			} else {
				s = new Entity(session.getDefaultSectioningStatus().getUniqueId(), "Not Set", session.getDefaultSectioningStatus().getReference() + " (Default)", "hint", session.getDefaultSectioningStatus().getLabel());
			}
			s.setCount(defaultStatus);
			states.add(s);
		}
		for (Object[] o: (List<Object[]>)query.select("s.sectioningStatus.uniqueId, s.sectioningStatus.reference, s.sectioningStatus.label, count(distinct s)")
				.order("s.sectioningStatus.reference, s.sectioningStatus.label").group("s.sectioningStatus.uniqueId, s.sectioningStatus.reference, s.sectioningStatus.label")
				.exclude("status").exclude("credit").query(helper.getHibSession()).list()) {
			Entity s = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[1],
					"hint", (String)o[2]);
			s.setCount(((Number)o[3]).intValue());
			states.add(s);
		}
		if (states.size() > 1)
			response.add("status", states);

		Map<Integer, Integer> crit2count = new HashMap<Integer, Integer>();
		for (Object[] o: (List<Object[]>)courseQuery.select("cd.critical, count(distinct cd)").where("cd.critical is not null and cd.criticalOverride is null").order("cd.critical").group("cd.critical").exclude("assignment").query(helper.getHibSession()).list()) {
			crit2count.put((Integer)o[0],((Number)o[1]).intValue());
		}
		for (Object[] o: (List<Object[]>)courseQuery.select("cd.criticalOverride, count(distinct cd)").where("cd.criticalOverride is not null").order("cd.criticalOverride").group("cd.criticalOverride").exclude("assignment").query(helper.getHibSession()).list()) {
			Integer pref = crit2count.get((Integer)o[0]);
			crit2count.put((Integer)o[0],((Number)o[1]).intValue() + (pref == null ? 0 : pref.intValue()));
		}
		
		List<Entity> assignment = new ArrayList<Entity>();
		assignment.add(new Entity(0l, "Assigned", CONSTANTS.assignmentType()[0], "translated-value", CONSTANTS.assignmentType()[0]));
		assignment.add(new Entity(1l, "Reserved", CONSTANTS.assignmentType()[1], "translated-value", CONSTANTS.assignmentType()[1]));
		assignment.add(new Entity(2l, "Not Assigned", CONSTANTS.assignmentType()[2], "translated-value", CONSTANTS.assignmentType()[2]));
		if (crit2count.containsKey(CourseDemand.Critical.CRITICAL.ordinal())) {
			if (CONSTANTS.assignmentType().length > 4)
				assignment.add(new Entity(4l, "Critical", CONSTANTS.assignmentType()[4], "translated-value", CONSTANTS.assignmentType()[4]));
			else
				assignment.add(new Entity(4l, "Critical", "Critical"));
			assignment.get(assignment.size() - 1).setCount(crit2count.get(CourseDemand.Critical.CRITICAL.ordinal()));
			if (CONSTANTS.assignmentType().length > 5)
				assignment.add(new Entity(5l, "Assigned Critical", CONSTANTS.assignmentType()[5], "translated-value", CONSTANTS.assignmentType()[5]));
			else
				assignment.add(new Entity(5l, "Assigned Critical", "Assigned Critical"));
			if (CONSTANTS.assignmentType().length > 6)
				assignment.add(new Entity(6l, "Not Assigned Critical", CONSTANTS.assignmentType()[6], "translated-value", CONSTANTS.assignmentType()[6]));
			else
				assignment.add(new Entity(6l, "Not Assigned Critical", "Not Assigned Critical"));
		}
		if (crit2count.containsKey(CourseDemand.Critical.VITAL.ordinal())) {
			if (CONSTANTS.assignmentType().length > 13)
				assignment.add(new Entity(13l, "Vital", CONSTANTS.assignmentType()[13], "translated-value", CONSTANTS.assignmentType()[13]));
			else
				assignment.add(new Entity(13l, "Vital", "Vital"));
			assignment.get(assignment.size() - 1).setCount(crit2count.get(CourseDemand.Critical.VITAL.ordinal()));
			if (CONSTANTS.assignmentType().length > 14)
				assignment.add(new Entity(14l, "Assigned Vital", CONSTANTS.assignmentType()[14], "translated-value", CONSTANTS.assignmentType()[14]));
			else
				assignment.add(new Entity(14l, "Assigned Vital", "Assigned Vital"));
			if (CONSTANTS.assignmentType().length > 15)
				assignment.add(new Entity(15l, "Not Assigned Vital", CONSTANTS.assignmentType()[15], "translated-value", CONSTANTS.assignmentType()[15]));
			else
				assignment.add(new Entity(15l, "Not Assigned Vital", "Not Assigned Vital"));
		}
		if (crit2count.containsKey(CourseDemand.Critical.IMPORTANT.ordinal())) {
			if (CONSTANTS.assignmentType().length > 7)
				assignment.add(new Entity(7l, "Important", CONSTANTS.assignmentType()[7], "translated-value", CONSTANTS.assignmentType()[7]));
			else
				assignment.add(new Entity(7l, "Important", "Important"));
			assignment.get(assignment.size() - 1).setCount(crit2count.get(CourseDemand.Critical.IMPORTANT.ordinal()));
			if (CONSTANTS.assignmentType().length > 8)
				assignment.add(new Entity(8l, "Assigned Important", CONSTANTS.assignmentType()[8], "translated-value", CONSTANTS.assignmentType()[8]));
			else
				assignment.add(new Entity(8l, "Assigned Important", "Assigned Important"));
			if (CONSTANTS.assignmentType().length > 9)
				assignment.add(new Entity(9l, "Not Assigned Important", CONSTANTS.assignmentType()[9], "translated-value", CONSTANTS.assignmentType()[9]));
			else
				assignment.add(new Entity(9l, "Not Assigned Important", "Not Assigned Important"));
		}
		if (CONSTANTS.assignmentType().length > 10)
			assignment.add(new Entity(10l, "No-Substitutes", CONSTANTS.assignmentType()[10], "translated-value", CONSTANTS.assignmentType()[10]));
		else
			assignment.add(new Entity(10l, "No-Substitutes", "No-Substitutes"));
		if (CONSTANTS.assignmentType().length > 11)
			assignment.add(new Entity(11l, "Assigned No-Subs", CONSTANTS.assignmentType()[11], "translated-value", CONSTANTS.assignmentType()[11]));
		else
			assignment.add(new Entity(11l, "Assigned No-Subs", "Assigned No-Subs"));
		if (CONSTANTS.assignmentType().length > 12)
			assignment.add(new Entity(12l, "Not Assigned No-Subs", CONSTANTS.assignmentType()[12], "translated-value", CONSTANTS.assignmentType()[12]));
		else
			assignment.add(new Entity(12l, "Not Assigned No-Subs", "Not Assigned No-Subs"));
		if (server instanceof StudentSolver && "LC".equals(server.getConfig().getProperty("Load.LCRequestPriority"))) {
			if (CONSTANTS.assignmentType().length > 16)
				assignment.add(new Entity(16l, "LC", CONSTANTS.assignmentType()[16], "translated-value", CONSTANTS.assignmentType()[16]));
			else
				assignment.add(new Entity(16l, "LC", "LC"));
			if (CONSTANTS.assignmentType().length > 17)
				assignment.add(new Entity(17l, "Assigned LC", CONSTANTS.assignmentType()[17], "translated-value", CONSTANTS.assignmentType()[17]));
			else
				assignment.add(new Entity(17l, "Assigned LC", "Assigned LC"));
			if (CONSTANTS.assignmentType().length > 18)
				assignment.add(new Entity(18l, "Not Assigned LC", CONSTANTS.assignmentType()[18], "translated-value", CONSTANTS.assignmentType()[18]));
			else
				assignment.add(new Entity(18l, "Not Assigned LC", "Not Assigned LC"));
		} else if (crit2count.containsKey(CourseDemand.Critical.LC.ordinal())) {
			if (CONSTANTS.assignmentType().length > 16)
				assignment.add(new Entity(16l, "LC", CONSTANTS.assignmentType()[16], "translated-value", CONSTANTS.assignmentType()[16]));
			else
				assignment.add(new Entity(16l, "LC", "LC"));
			assignment.get(assignment.size() - 1).setCount(crit2count.get(CourseDemand.Critical.LC.ordinal()));
			if (CONSTANTS.assignmentType().length > 17)
				assignment.add(new Entity(17l, "Assigned LC", CONSTANTS.assignmentType()[17], "translated-value", CONSTANTS.assignmentType()[17]));
			else
				assignment.add(new Entity(17l, "Assigned LC", "Assigned LC"));
			if (CONSTANTS.assignmentType().length > 18)
				assignment.add(new Entity(18l, "Not Assigned LC", CONSTANTS.assignmentType()[18], "translated-value", CONSTANTS.assignmentType()[18]));
			else
				assignment.add(new Entity(18l, "Not Assigned LC", "Not Assigned LC"));
		}
		if (server instanceof StudentSolver) {
			String visitingStudentsFilter = server.getConfig().getProperty("Load.VisitingStudentFilter");
			if (visitingStudentsFilter != null && !visitingStudentsFilter.isEmpty()) {
				if (CONSTANTS.assignmentType().length > 19)
					assignment.add(new Entity(19l, "Visiting F2F", CONSTANTS.assignmentType()[19], "translated-value", CONSTANTS.assignmentType()[19]));
				else
					assignment.add(new Entity(19l, "Visiting F2F", "Visiting F2F"));
				if (CONSTANTS.assignmentType().length > 20)
					assignment.add(new Entity(20l, "Assigned Visiting F2F", CONSTANTS.assignmentType()[20], "translated-value", CONSTANTS.assignmentType()[20]));
				else
					assignment.add(new Entity(20l, "Assigned Visiting F2F", "Assigned Visiting F2F"));
				if (CONSTANTS.assignmentType().length > 21)
					assignment.add(new Entity(21l, "Not Assigned Visiting F2F", CONSTANTS.assignmentType()[21], "translated-value", CONSTANTS.assignmentType()[21]));
				else
					assignment.add(new Entity(21l, "Not Assigned Visiting F2F", "Not Assigned Visiting F2F"));
			}
		}
		if (!(server instanceof StudentSolver))
			assignment.add(new Entity(3l, "Wait-Listed", CONSTANTS.assignmentType()[3], "translated-value", CONSTANTS.assignmentType()[3]));
		response.add("assignment", assignment);
		
		List<Entity> consent = new ArrayList<Entity>();
		consent.add(new Entity(-1l, "Consent", CONSTANTS.consentTypeAbbv()[0], "translated-value", CONSTANTS.consentTypeAbbv()[0]));
		for (OfferingConsentType type: OfferingConsentType.getConsentTypeList())
			consent.add(new Entity(type.getUniqueId(), type.getAbbv(), type.getLabel()));
		consent.add(new Entity(-2l, "No Consent", CONSTANTS.consentTypeAbbv()[1], "translated-value", CONSTANTS.consentTypeAbbv()[1]));
		consent.add(new Entity(-3l, "Waiting", CONSTANTS.consentTypeAbbv()[2], "translated-value", CONSTANTS.consentTypeAbbv()[2]));
		consent.add(new Entity(-4l, "Approved", CONSTANTS.consentTypeAbbv()[3], "translated-value", CONSTANTS.consentTypeAbbv()[3]));
		consent.add(new Entity(-5l, "To Do", CONSTANTS.consentTypeAbbv()[4], "translated-value", CONSTANTS.consentTypeAbbv()[4]));
		response.add("consent", consent);
		
		if ("true".equals(iRequest.getOption("online"))) {
			List<Entity> overrides = new ArrayList<Entity>();
			for (CourseRequest.CourseRequestOverrideStatus status: CourseRequest.CourseRequestOverrideStatus.values()) {
				try {
					overrides.add(new Entity(Long.valueOf(-1 - status.ordinal()), Constants.toInitialCase(status.name()), CONSTANTS.overrideType()[status.ordinal()], "translated-value", CONSTANTS.overrideType()[status.ordinal()])); 
				} catch (ArrayIndexOutOfBoundsException e) {}
			}
			if (!iRequest.hasOptions("assignment"))
				for (Object[] o: (List<Object[]>)query.select("s.overrideStatus, count(distinct s)").where("s.overrideStatus is not null").order("s.overrideStatus").group("s.overrideStatus").exclude("credit").exclude("override").query(helper.getHibSession()).list()) {
					Entity e = overrides.get((Integer)o[0]);
					e.setCount(((Number)o[1]).intValue());
				}
			for (Object[] o: (List<Object[]>)courseQuery.select("cr.overrideStatus, count(distinct cr)").where("cr.overrideStatus is not null").order("cr.overrideStatus").group("cr.overrideStatus").exclude("credit").exclude("override").query(helper.getHibSession()).list()) {
				Entity e = overrides.get((Integer)o[0]);
				e.setCount(e.getCount() + ((Number)o[1]).intValue());
			}
			for (Iterator<Entity> i = overrides.iterator(); i.hasNext(); )
				if (i.next().getCount() == 0) i.remove();
			if (!overrides.isEmpty()) {
				Entity none = new Entity(Long.valueOf(-100), "none", CONSTANTS.noOverride(), "translated-value", CONSTANTS.noOverride());
				// none.setCount(((Number)query.select("count(distinct xcr)").where("xcr.overrideStatus is null").from("inner join s.courseDemands xcd inner join xcd.courseRequests xcr").exclude("override").query(helper.getHibSession()).uniqueResult()).intValue());
				overrides.add(none);
				response.add("override", overrides);
			}
		}
		
		
		List<Entity> modes = new ArrayList<Entity>();
		if (iRequest.hasOption("role")) {
			int myStudents = ((Number)query.select("count(distinct s)")
					.where("s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = :Xuser and adv.role.reference = :Xrole and adv.session.uniqueId = s.session.uniqueId)")
					.set("Xuser", iRequest.getOption("user")).set("Xrole", iRequest.getOption("role"))
					.exclude("mode").exclude("credit").query(helper.getHibSession()).uniqueResult()).intValue();
			if (myStudents > 0) {
				Entity myE = new Entity(-1l, "My Students", MESSAGES.modeMyStudents(), "translated-value", MESSAGES.modeMyStudents());
				myE.setCount(myStudents);
				modes.add(myE);
				if (ApplicationProperty.StudentSchedulingFilterSkipAdvisedCounts.isTrue()) {
					modes.add(new Entity(-1l, "My Advised", MESSAGES.modeMyStudentsAdvised(), "translated-value", MESSAGES.modeMyStudentsAdvised()));
					modes.add(new Entity(-1l, "My Not Advised", MESSAGES.modeMyStudentsNotAdvised(), "translated-value", MESSAGES.modeMyStudentsNotAdvised()));
					if (iPinOps) {
						modes.add(new Entity(-1l, "My PIN Released", MESSAGES.modeMyPinReleased(), "translated-value", MESSAGES.modeMyPinReleased()));
						modes.add(new Entity(-1l, "My PIN Suppressed", MESSAGES.modeMyPinSuppressed(), "translated-value", MESSAGES.modeMyPinSuppressed()));
					}
				} else {
					int myAdvised = ((Number)query.select("count(distinct s)")
							.where("s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = :Xuser and adv.role.reference = :Xrole and adv.session.uniqueId = s.session.uniqueId) and s.advisorCourseRequests is not empty")
							.set("Xuser", iRequest.getOption("user")).set("Xrole", iRequest.getOption("role"))
							.exclude("mode").exclude("credit").query(helper.getHibSession()).uniqueResult()).intValue();
					if (myAdvised > 0) {
						Entity myA = new Entity(-1l, "My Advised", MESSAGES.modeMyStudentsAdvised(), "translated-value", MESSAGES.modeMyStudentsAdvised());
						myA.setCount(myAdvised);
						modes.add(myA);
					}
					if (myAdvised < myStudents) {
						Entity myA = new Entity(-1l, "My Not Advised", MESSAGES.modeMyStudentsNotAdvised(), "translated-value", MESSAGES.modeMyStudentsNotAdvised());
						myA.setCount(myStudents - myAdvised);
						modes.add(myA);
					}
					if (iPinOps) {
						int pinReleased = ((Number)query.select("count(distinct s)")
								.where("s.pinReleased = true")
								.where("s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = :Xuser and adv.role.reference = :Xrole and adv.session.uniqueId = s.session.uniqueId)")
								.exclude("mode").exclude("credit").query(helper.getHibSession()).uniqueResult()).intValue();
						if (pinReleased > 0) {
							Entity pr = new Entity(-1l, "My PIN Released", MESSAGES.modeMyPinReleased(), "translated-value", MESSAGES.modeMyPinReleased());
							pr.setCount(pinReleased);
							modes.add(pr);
						}
						int pinSuppressed = ((Number)query.select("count(distinct s)")
								.where("(s.pinReleased is null or s.pinReleased = false)" + (Customization.StudentPinsProvider.hasProvider() ? "" : " and s.pin is not null"))
								.where("s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = :Xuser and adv.role.reference = :Xrole and adv.session.uniqueId = s.session.uniqueId)")
								.exclude("mode").exclude("credit").query(helper.getHibSession()).uniqueResult()).intValue();
						if (pinSuppressed > 0) {
							Entity ps = new Entity(-1l, "My PIN Suppressed", MESSAGES.modeMyPinSuppressed(), "translated-value", MESSAGES.modeMyPinSuppressed());
							ps.setCount(pinSuppressed);
							modes.add(ps);
						}
					}
				}
			}
		}
		
		if (ApplicationProperty.StudentSchedulingFilterSkipAdvisedCounts.isTrue()) {
			modes.add(new Entity(-1l, "Advised", MESSAGES.modeAdvised(), "translated-value", MESSAGES.modeAdvised()));
			modes.add(new Entity(-1l, "Not Advised", MESSAGES.modeNotAdvised(), "translated-value", MESSAGES.modeNotAdvised()));
			if (iPinOps)
				modes.add(new Entity(-1l, "PIN Released", MESSAGES.modePinReleased(), "translated-value", MESSAGES.modePinReleased()));
				modes.add(new Entity(-1l, "PIN Suppressed", MESSAGES.modePinSuppressed(), "translated-value", MESSAGES.modePinSuppressed()));
		} else {
			int advised = ((Number)query.select("count(distinct s)")
					.where("s.advisorCourseRequests is not empty")
					.exclude("mode").exclude("credit").query(helper.getHibSession()).uniqueResult()).intValue();
			if (advised > 0) {
				Entity adv = new Entity(-1l, "Advised", MESSAGES.modeAdvised(), "translated-value", MESSAGES.modeAdvised());
				adv.setCount(advised);
				modes.add(adv);
				int notAdvised = ((Number)query.select("count(distinct s)")
						.where("s.advisorCourseRequests is empty")
						.exclude("mode").exclude("credit").query(helper.getHibSession()).uniqueResult()).intValue();
				if (notAdvised > 0) {
					Entity notAdv = new Entity(-1l, "Not Advised", MESSAGES.modeNotAdvised(), "translated-value", MESSAGES.modeNotAdvised());
					notAdv.setCount(notAdvised);
					modes.add(notAdv);
				}
			}
			if (iPinOps) {
				int pinReleased = ((Number)query.select("count(distinct s)")
						.where("s.pinReleased = true")
						.exclude("mode").exclude("credit").query(helper.getHibSession()).uniqueResult()).intValue();
				if (pinReleased > 0) {
					Entity pr = new Entity(-1l, "PIN Released", MESSAGES.modePinReleased(), "translated-value", MESSAGES.modePinReleased());
					pr.setCount(pinReleased);
					modes.add(pr);
				}
				int pinSuppressed = ((Number)query.select("count(distinct s)")
						.where("(s.pinReleased is null or s.pinReleased = false)" + (Customization.StudentPinsProvider.hasProvider() ? "" : " and s.pin is not null"))
						.exclude("mode").exclude("credit").query(helper.getHibSession()).uniqueResult()).intValue();
				if (pinSuppressed > 0) {
					Entity ps = new Entity(-1l, "PIN Suppressed", MESSAGES.modePinSuppressed(), "translated-value", MESSAGES.modePinSuppressed());
					ps.setCount(pinSuppressed);
					modes.add(ps);
				}				
			}
		}

		if (!modes.isEmpty())
			response.add("mode", modes);		

		List<Entity> preferences = new ArrayList<Entity>();
		preferences.add(new Entity(0l, "Any Preference", MESSAGES.termAnyPreference(), "translated-value", MESSAGES.termAnyPreference()));
		preferences.add(new Entity(1l, "Met Preference", MESSAGES.termMetPreference(), "translated-value", MESSAGES.termMetPreference()));
		preferences.add(new Entity(2l, "Unmet Preference", MESSAGES.termUnmetPreference(), "translated-value", MESSAGES.termUnmetPreference()));
		response.add("prefer", preferences);
		
		List<Entity> requires = new ArrayList<Entity>();
		requires.add(new Entity(0l, "Any Requirement", MESSAGES.termAnyRequirement(), "translated-value", MESSAGES.termAnyRequirement()));
		requires.add(new Entity(1l, "Met Requirement", MESSAGES.termMetRequirement(), "translated-value", MESSAGES.termMetRequirement()));
		requires.add(new Entity(2l, "Unmet Requirement", MESSAGES.termUnmetRequirement(), "translated-value", MESSAGES.termUnmetRequirement()));
		response.add("require", requires);
		
		List<Entity> instructionalMethods = new ArrayList<Entity>();
		for (InstructionalMethod im: InstructionalMethod.findAll())
			instructionalMethods.add(new Entity(im.getUniqueId(), im.getReference(), im.getLabel()));
		response.add("im", instructionalMethods);
		
		return response;
	}
	
	public FilterRpcResponse suggestions(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		FilterRpcResponse response = new FilterRpcResponse();

		if (CustomCourseLookupHolder.hasProvider() && !iRequest.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
			try {
				CustomCourseLookupHolder.getProvider().addSuggestions(server, helper, iRequest.getText(), response);
			} catch (Exception e) {}
		}
		
		String studentIdPattern = ApplicationProperty.OnlineSchedulingStudentIdPattern.value();
		boolean studentIdMatch = (studentIdPattern != null && !studentIdPattern.isEmpty() && iRequest.getText().trim().matches(studentIdPattern));

		if (!iRequest.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20) && !studentIdMatch) {
			List<SubjectArea> subjects = helper.getHibSession().createQuery("select s from SubjectArea s where s.session.uniqueId = :sessionId and (" +
					"lower(s.subjectAreaAbbreviation) like :name or lower(' ' || s.title) like :title) " +
					"order by s.subjectAreaAbbreviation", SubjectArea.class)
					.setParameter("name", iRequest.getText().toLowerCase() + "%").setParameter("title", "% " + iRequest.getText().toLowerCase() + "%")
					.setParameter("sessionId", server.getAcademicSession().getUniqueId()).setMaxResults(20).list();
			for (SubjectArea subject: subjects)
				response.addSuggestion(subject.getSubjectAreaAbbreviation() + " - " + subject.getTitle(), subject.getSubjectAreaAbbreviation(), "Subject Area", "course", true);
			if (subjects.size() == 1) {
				for (CourseOffering course: new TreeSet<CourseOffering>(subjects.get(0).getCourseOfferings())) {
					if (course.getInstructionalOffering().isNotOffered()) continue;
					response.addSuggestion(course.getCourseName() + (course.getTitle() == null ? "" : " - " + course.getTitle()), course.getCourseName(), "Course Offering", "course");
				}
			} else if (subjects.isEmpty()) {
				List<CourseOffering> courses = helper.getHibSession().createQuery("select c from CourseOffering c inner join c.subjectArea s where s.session.uniqueId = :sessionId and (" +
						"lower(s.subjectAreaAbbreviation || ' ' || c.courseNbr) like :name or lower(' ' || c.title) like :title) and c.instructionalOffering.notOffered = false " +
						"order by s.subjectAreaAbbreviation, c.courseNbr", CourseOffering.class)
						.setParameter("name", iRequest.getText().toLowerCase() + "%").setParameter("title", "% " + iRequest.getText().toLowerCase() + "%")
						.setParameter("sessionId", server.getAcademicSession().getUniqueId()).setMaxResults(20).list();
				for (CourseOffering course: courses) {
					response.addSuggestion(course.getCourseName() + (course.getTitle() == null ? "" : " - " + course.getTitle()), course.getCourseName(), "Course Offering", "course", true);
				}
			}
		}
		
		StudentQuery query = getQuery(iRequest, server, helper);
		if (!iRequest.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
			if (studentIdMatch) {
				StudentQuery.QueryInstance instance = query.select("distinct s").exclude("student").order("s.lastName, s.firstName, s.middleName");
				instance.where("s.externalUniqueId = :id");
				if (ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue()) {
					instance.set("id", iRequest.getText().trim().replaceFirst("^0+(?!$)", ""));
				} else {
					instance.set("id", iRequest.getText().trim());
				}
				for (Student student: (List<Student>)instance.limit(20).query(helper.getHibSession()).list())
					response.addSuggestion(helper.getStudentNameFormat().format(student), student.getExternalUniqueId(), "Student", "student");
			} else {
				StudentQuery.QueryInstance instance = query.select("distinct s").exclude("student").order("s.lastName, s.firstName, s.middleName");
				
				int id = 0;
				String where = "";
				for (StringTokenizer s = new StringTokenizer(iRequest.getText().trim(),", "); s.hasMoreTokens(); ) {
					String token = s.nextToken().toUpperCase();
					if (!where.isEmpty())
						where += " and ";
					where += "(upper(s.firstName) like :cn" + id + " || '%' or upper(s.middleName) like :cn" + id + " || '%' or upper(s.lastName) like :cn" + id + " || '%' or upper(s.email) like :cn" + id + ")";
					instance.set("cn" + id, token);
					id++;
	            }
				if (id > 0) {
					instance.where("(" + where + ") or upper(trim(trailing ' ' from s.lastName || ', ' || s.firstName || ' ' || s.middleName)) = :name or s.externalUniqueId = :id");
					instance.set("name", iRequest.getText().trim().toUpperCase());
					if (ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue()) {
						instance.set("id", iRequest.getText().trim().replaceFirst("^0+(?!$)", ""));
					} else {
						instance.set("id", iRequest.getText().trim());
					}
					for (Student student: (List<Student>)instance.limit(20).query(helper.getHibSession()).list())
						response.addSuggestion(helper.getStudentNameFormat().format(student), student.getExternalUniqueId(), "Student", "student");
				}
			}
		}
		
		if (!iRequest.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20) && "true".equals(iRequest.getOption("approval"))) {
			for (TimetableManager manager: helper.getHibSession().createQuery(
					"select distinct m from TimetableManager m inner join m.managerRoles r inner join m.departments d where " +
					" (lower(m.externalUniqueId) like :q || '%' or lower(m.emailAddress) like :q || '%' or lower(m.lastName) || ' ' || lower(m.firstName) like :q || '%')" +
					" and 'ConsentApproval' in elements(r.role.rights) and d.session.uniqueId = :sessionId order by m.lastName, m.firstName, m.middleName", TimetableManager.class
					).setParameter("q", iRequest.getText().toLowerCase()).setParameter("sessionId", server.getAcademicSession().getUniqueId()).setMaxResults(20).list()) {
				response.addSuggestion(manager.getName(), manager.getName(), "Approved by", "approver");
			}
			
			for (DepartmentalInstructor coordinator: helper.getHibSession().createQuery(
					"select distinct i from CourseOffering c inner join c.instructionalOffering.offeringCoordinators oc inner join oc.instructor i where " +
					"c.subjectArea.session.uniqueId = :sessionId and c.consentType.reference != :reference and " +
					"(lower(i.externalUniqueId) like :q || '%' or lower(i.email) like :q || '%' or lower(i.lastName) || ' ' || lower(i.firstName) like :q || '%') " +
					"order by i.lastName, i.firstName, i.middleName", DepartmentalInstructor.class
					).setParameter("q", iRequest.getText().toLowerCase()).setParameter("reference", "IN").setParameter("sessionId", server.getAcademicSession().getUniqueId()).setMaxResults(20).list()) {
				response.addSuggestion(coordinator.getNameLastFirst(), coordinator.getNameLastFirst(), "Approved by", "approver");
			}
		}
		
		if (iRequest.getText().length() > 1 && (response.getSuggestions() == null || response.getSuggestions().size() < 20) && iRequest.getText().matches(ApplicationProperty.OnlineSchedulingDashboardSuggestionsOperationPattern.value())) {
			int days = ApplicationProperty.OnlineSchedulingDashboardSuggestionsLogDays.intValue();
			if (days > 0) {
				StudentQuery.QueryInstance instance = query.select("distinct l.operation").exclude("operation").order("l.operation")
						.from("OnlineSectioningLog l").where("l.session.uniqueId = :sessionId").where("l.student = s.externalUniqueId")
						.where("l.timeStamp > " + HibernateUtil.addDate("current_date()", ":days")).set("days", -days);
				String q = iRequest.getText();
				if (!"operation".startsWith(q.toLowerCase())) {
					instance.set("q", q);
					instance.where("l.operation like :q || '%'");
				}
				for (String op: (List<String>)instance.limit(20).query(helper.getHibSession()).list()) {
					response.addSuggestion(Constants.toInitialCase(op.replace('-', ' ')), op, "Operation", "operation");
				}
			} else if (days < 0) {
				StudentQuery.QueryInstance instance = query.select("distinct l.operation").exclude("operation").order("l.operation")
						.from("OnlineSectioningLog l").where("l.session.uniqueId = :sessionId").where("l.student = s.externalUniqueId");
				String q = iRequest.getText();
				if (!"operation".startsWith(q.toLowerCase())) {
					instance.set("q", q);
					instance.where("l.operation like :q || '%'");
				}
				for (String op: (List<String>)instance.limit(20).query(helper.getHibSession()).list()) {
					response.addSuggestion(Constants.toInitialCase(op.replace('-', ' ')), op, "Operation", "operation");
				}
			}
		}
		
		if (iRequest.getText().length() > 1 && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
			StudentQuery.QueryInstance instance = query.select("distinct im.label").exclude("prefer").exclude("require")
					.from("inner join s.courseDemands pcd inner join pcd.courseRequests pcr inner join pcr.preferences pp inner join pp.instructionalMethod im")
					.where("lower(im.reference) like :q || '%' or lower(im.label) like :q || '%'").set("q", iRequest.getText().toLowerCase());
			for (String im: (List<String>)instance.limit(20).query(helper.getHibSession()).list()) {
				response.addSuggestion(im, im, "Prefer", "prefer", true);
			}
		}
		
		if (iRequest.getText().length() > 1 && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
			StudentQuery.QueryInstance instance = query.select("distinct im.label").exclude("prefer").exclude("require")
					.from("inner join s.courseDemands pcd inner join pcd.courseRequests pcr inner join pcr.preferences pp inner join pp.instructionalMethod im")
					.where("lower(im.reference) like :q || '%' or lower(im.label) like :q || '%'")
					.where("pp.required = true").set("q", iRequest.getText().toLowerCase());
			for (String im: (List<String>)instance.limit(20).query(helper.getHibSession()).list()) {
				response.addSuggestion(im, im, "Require", "require", true);
			}
		}
		
		if (iRequest.getText().length() > 1 && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
			StudentQuery.QueryInstance instance = query.select("distinct c, pcr.courseOffering").exclude("prefer").exclude("require")
					.from("inner join s.courseDemands pcd inner join pcd.courseRequests pcr inner join pcr.preferences pp inner join pp.clazz c")
					.where("lower(pp.label) like :q || '%'").set("q", iRequest.getText().toLowerCase());
			for (Object[] o: (List<Object[]>)instance.limit(20).query(helper.getHibSession()).list()) {
				Class_ c = (Class_)o[0];
				CourseOffering co = (CourseOffering)o[1];
				response.addSuggestion(c.getClassLabel(co, true), c.getClassPrefLabel(co), "Prefer", "prefer", true);
			}
		}
		
		if (iRequest.getText().length() > 1 && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
			StudentQuery.QueryInstance instance = query.select("distinct c, pcr.courseOffering").exclude("prefer").exclude("require")
					.from("inner join s.courseDemands pcd inner join pcd.courseRequests pcr inner join pcr.preferences pp inner join pp.clazz c")
					.where("lower(pp.label) like :q || '%'")
					.where("pp.required = true").set("q", iRequest.getText().toLowerCase());
			for (Object[] o: (List<Object[]>)instance.limit(20).query(helper.getHibSession()).list()) {
				Class_ c = (Class_)o[0];
				CourseOffering co = (CourseOffering)o[1];
				response.addSuggestion(c.getClassLabel(co, true), c.getClassPrefLabel(co), "Require", "require", true);
			}
		}
		
		if (!iRequest.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
			if (studentIdMatch) {
				StudentQuery.QueryInstance instance = query.select("distinct ax").exclude("advisor").from("inner join s.advisors ax").where("ax.lastName is not null").order("ax.lastName, ax.firstName, ax.middleName");
				instance.where("ax.externalUniqueId = :id");
				if (ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue()) {
					instance.set("id", iRequest.getText().trim().replaceFirst("^0+(?!$)", ""));
				} else {
					instance.set("id", iRequest.getText().trim());
				}
				for (Advisor advisor: (List<Advisor>)instance.limit(20).query(helper.getHibSession()).list())
					response.addSuggestion(helper.getStudentNameFormat().format(advisor), advisor.getExternalUniqueId(), "Advisor", "advisor");
			} else {
				StudentQuery.QueryInstance instance = query.select("distinct ax").exclude("advisor").from("inner join s.advisors ax").where("ax.lastName is not null").order("ax.lastName, ax.firstName, ax.middleName");
				
				int id = 0;
				String where = "";
				for (StringTokenizer s = new StringTokenizer(iRequest.getText().trim(),", "); s.hasMoreTokens(); ) {
					String token = s.nextToken().toUpperCase();
					if (!where.isEmpty())
						where += " and ";
					where += "(upper(ax.firstName) like :cn" + id + " || '%' or upper(ax.middleName) like :cn" + id + " || '%' or upper(ax.lastName) like :cn" + id + " || '%' or upper(ax.email) like :cn" + id + ")";
					instance.set("cn" + id, token);
					id++;
	            }
				if (id > 0) {
					instance.where("(" + where + ") or upper(trim(trailing ' ' from ax.lastName || ', ' || ax.firstName || ' ' || ax.middleName)) = :name or ax.externalUniqueId = :id");
					instance.set("name", iRequest.getText().trim().toUpperCase());
					if (ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue()) {
						instance.set("id", iRequest.getText().trim().replaceFirst("^0+(?!$)", ""));
					} else {
						instance.set("id", iRequest.getText().trim());
					}
					for (Advisor advisor: (List<Advisor>)instance.limit(20).query(helper.getHibSession()).list())
						response.addSuggestion(helper.getStudentNameFormat().format(advisor), advisor.getExternalUniqueId(), "Advisor", "advisor");
				}
			}
		}
		
		return response;
	}
	
	public FilterRpcResponse enumarate(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		FilterRpcResponse response = new FilterRpcResponse();
		
		return response;
	}

	@Override
	public String name() {
		return "filter-" + iRequest.getCommand().name().toLowerCase();
	}
	
	public static StudentQuery getQuery(FilterRpcRequest request, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Set<String> groupTypes = new HashSet<String>();
		for (StudentGroupType type: StudentGroupTypeDAO.getInstance().findAll(helper.getHibSession()))
			groupTypes.add(type.getReference().replace(' ', '_'));
		
		StudentQuery query = new StudentQuery(server.getAcademicSession().getUniqueId());
		
		if (request.getText() != null && !request.getText().isEmpty()) {
			// ?
		}
		
		if (request.hasOption("lookup") && CustomCourseLookupHolder.hasProvider()) {
			try {
				Set<Long> courseIds = CustomCourseLookupHolder.getProvider().getCourseIds(server.getAcademicSession(), helper.getHibSession(), request.getOption("lookup"), true);
				if (courseIds != null && !courseIds.isEmpty()) {
					String where = null;
					String course = "";
					int id = 0;
					for (Long c: courseIds) {
						course += (course.isEmpty() ? "" : ",") + ":Xcx" + id;
						query.addParameter("lookup", "Xcx" + id, c);
						id++;
						if ((id % 1000) == 0) {
							where = (where == null ? "" : where + " or ") + "co.uniqueId in (" + course + ")";
							course = "";
						}
					}
					if (!course.isEmpty())
						where = (where == null ? "" : where + " or ") + "co.uniqueId in (" + course + ")"; 
					query.addWhere("lookup", where);
					query.addFrom("lookup", "inner join s.courseDemands cd inner join cd.courseRequests cr inner join cr.courseOffering co");					
				}
			} catch (Exception e) {}
		} else if (request.hasOptions("lookup") && CustomCourseLookupHolder.hasProvider()) {
			try {
				String course = "";
				int id = 0;
				for (String lookup: request.getOptions("lookup")) {
					Set<Long> courseIds = CustomCourseLookupHolder.getProvider().getCourseIds(server.getAcademicSession(), helper.getHibSession(), lookup, true);
					if (courseIds != null && !courseIds.isEmpty()) {
						for (Long c: courseIds) {
							course += (course.isEmpty() ? "" : ",") + ":Xcx" + id;
							query.addParameter("lookup", "Xcx" + id, c);
							id++;
						}
					}
				}
				if (id > 0) {
					query.addWhere("lookup", "co.uniqueId in (" + course + ")");
					query.addFrom("lookup", "inner join s.courseDemands cd inner join cd.courseRequests cr inner join cr.courseOffering co");					
				}
			} catch (Exception e) {}
		}
		
		String area = "";
		if (request.hasOptions("area")) {
			boolean like = false;
			for (String d: request.getOptions("area")) {
				if (d.indexOf('%') >= 0) { like = true; break; }
			}
			if (like) {
				String q = "";
				int id = 0;
				for (String a: request.getOptions("area")) {
					q += (q.isEmpty() ? "" : " or ") + "aac.academicArea.academicAreaAbbreviation like :Xar" + id;
					query.addParameter("area", "Xar" + id, a);
					id++;
				}
				query.addWhere("area", q);
			} else {
				int id = 0;
				for (String a: request.getOptions("area")) {
					area += (area.isEmpty() ? "" : ",") + ":Xar" + id;
					query.addParameter("area", "Xar" + id, a);
					id++;
				}
				query.addWhere("area", "aac.academicArea.academicAreaAbbreviation in (" + area + ")");
			}
		}

		if (request.hasOptions("classification")) {
			boolean like = false;
			for (String d: request.getOptions("classification")) {
				if (d.indexOf('%') >= 0) { like = true; break; }
			}
			if (like) {
				String q = "";
				int id = 0;
				for (String c: request.getOptions("classification")) {
					q += (q.isEmpty() ? "" : " or ") + "aac.academicClassification.code like :Xcf" + id;
					query.addParameter("classification", "Xcf" + id, c);
					id++;
				}
				query.addWhere("classification", q);
			} else {
				String classf = "";
				int id = 0;
				for (String c: request.getOptions("classification")) {
					classf += (classf.isEmpty() ? "" : ",") + ":Xcf" + id;
					query.addParameter("classification", "Xcf" + id, c);
					id++;
				}
				query.addWhere("classification", "aac.academicClassification.code in (" + classf + ")");
			}
		}
		
		if (request.hasOptions("degree")) {
			boolean like = false;
			for (String d: request.getOptions("degree")) {
				if (d.indexOf('%') >= 0) { like = true; break; }
			}
			if (like) {
				String q = "";
				int id = 0;
				for (String d: request.getOptions("degree")) {
					q += (q.isEmpty() ? "" : " or ") + "aac.degree.reference like :Xdg" + id;
					query.addParameter("degree", "Xdg" + id, d);
					id++;
				}
				query.addWhere("degree", q);
			} else {
				String degr = "";
				int id = 0;
				for (String d: request.getOptions("degree")) {
					degr += (degr.isEmpty() ? "" : ",") + ":Xdg" + id;
					query.addParameter("degree", "Xdg" + id, d);
					id++;
				}
				query.addWhere("degree", "aac.degree.reference in (" + degr + ")");
			}
		}
		
		if (request.hasOptions("program")) {
			boolean like = false;
			for (String d: request.getOptions("program")) {
				if (d.indexOf('%') >= 0) { like = true; break; }
			}
			if (like) {
				String q = "";
				int id = 0;
				for (String d: request.getOptions("program")) {
					q += (q.isEmpty() ? "" : " or ") + "aac.program.reference like :Xpr" + id;
					query.addParameter("program", "Xpr" + id, d);
					id++;
				}
				query.addWhere("program", q);
			} else {
				String prog = "";
				int id = 0;
				for (String d: request.getOptions("program")) {
					prog += (prog.isEmpty() ? "" : ",") + ":Xpr" + id;
					query.addParameter("program", "Xpr" + id, d);
					id++;
				}
				query.addWhere("program", "aac.program.reference in (" + prog + ")");
			}
		}
		
		if (request.hasOptions("campus")) {
			boolean like = false;
			for (String d: request.getOptions("campus")) {
				if (d.indexOf('%') >= 0) { like = true; break; }
			}
			if (like) {
				String q = "";
				int id = 0;
				for (String d: request.getOptions("campus")) {
					q += (q.isEmpty() ? "" : " or ") + "aac.campus.reference like :Xcp" + id;
					query.addParameter("campus", "Xcp" + id, d);
					id++;
				}
				query.addWhere("campus", q);
			} else {
				String prog = "";
				int id = 0;
				for (String d: request.getOptions("campus")) {
					prog += (prog.isEmpty() ? "" : ",") + ":Xcp" + id;
					query.addParameter("campus", "Xcp" + id, d);
					id++;
				}
				query.addWhere("campus", "aac.campus.reference in (" + prog + ")");
			}
		}

		if (request.hasOptions("major")) {
			boolean like = false;
			for (String d: request.getOptions("major")) {
				if (d.indexOf('%') >= 0) { like = true; break; }
			}
			if (like) {
				String q = "";
				int id = 0;
				for (String m: request.getOptions("major")) {
					q += (q.isEmpty() ? "" : " or ") + "aac.major.code like :Xmj" + id;
					query.addParameter("major", "Xmj" + id, m);
					id++;
				}
				query.addWhere("major", q);
			} else {
				String major = "";
				int id = 0;
				for (String m: request.getOptions("major")) {
					major += (major.isEmpty() ? "" : ",") + ":Xmj" + id;
					query.addParameter("major", "Xmj" + id, m);
					id++;
				}
				query.addWhere("major", "aac.major.code in (" + major + ")");
			}
		}
		
		if (request.hasOptions("concentration")) {
			boolean like = false;
			for (String d: request.getOptions("concentration")) {
				if (d.indexOf('%') >= 0) { like = true; break; }
			}
			if (like) {
				String q = "";
				int id = 0;
				for (String m: request.getOptions("concentration")) {
					q += (q.isEmpty() ? "" : " or ") + "aac.concentration.code like :Xcn" + id;
					query.addParameter("concentration", "Xcn" + id, m);
					id++;
				}
				query.addWhere("concentration", q);
			} else {
				String conc = "";
				int id = 0;
				for (String m: request.getOptions("concentration")) {
					conc += (conc.isEmpty() ? "" : ",") + ":Xcn" + id;
					query.addParameter("concentration", "Xcn" + id, m);
					id++;
				}
				query.addWhere("concentration", "aac.concentration.code in (" + conc + ")");
			}
		}
		
		if (request.hasOptions("minor")) {
			boolean like = false;
			for (String d: request.getOptions("minor")) {
				if (d.indexOf('%') >= 0) { like = true; break; }
			}
			if (like) {
				String q = "";
				int id = 0;
				for (String m: request.getOptions("minor")) {
					q += (q.isEmpty() ? "" : " or ") + "aam.minor.code like :Xmn" + id;
					query.addParameter("minor", "Xmn" + id, m);
					id++;
				}
				query.addFrom("minor", "StudentAreaClassificationMinor aam");
				query.addWhere("minor", "aam.student = s and (" + q + ")");
			} else {
				String minor = "";
				int id = 0;
				for (String m: request.getOptions("minor")) {
					minor += (minor.isEmpty() ? "" : ",") + ":Xmn" + id;
					query.addParameter("minor", "Xmn" + id, m);
					id++;
				}
				if (!area.isEmpty()) {
					query.addFrom("area", "StudentAreaClassificationMinor aam");
					query.addWhere("area", "aam.student = s and (aac.academicArea.academicAreaAbbreviation in (" + area + ") or aam.academicArea.academicAreaAbbreviation in (" + area + "))");
					query.addWhere("minor", "aam.minor.code in (" + minor + ")");
				} else {
					query.addFrom("minor", "StudentAreaClassificationMinor aam");
					query.addWhere("minor", "aam.student = s and aam.minor.code in (" + minor + ")");
				}
			}				
		}
		
		int gid = 0;
		if (request.hasOptions("group")) {
			boolean like = false;
			for (String d: request.getOptions("group")) {
				if (d.indexOf('%') >= 0) { like = true; break; }
			}
			query.addFrom("group", "StudentGroup g");
			if (like) {
				String q = "";
				int id = 0;
				for (String g: request.getOptions("group")) {
					q += (q.isEmpty() ? "" : " or ") + "g.groupAbbreviation like :Xgr" + id;
					query.addParameter("group", "Xgr" + gid, g);
					id++;
				}
				query.addWhere("group", "g in elements(s.groups) and (" + q + ")");
			} else {
				String group = "";
				for (String g: request.getOptions("group")) {
					group += (group.isEmpty() ? "" : ",") + ":Xgr" + gid;
					query.addParameter("group", "Xgr" + gid, g);
					gid++;
				}
				query.addWhere("group", "g in elements(s.groups) and g.groupAbbreviation in (" + group + ")");
			}
		}
		
		for (String type: groupTypes) {
			if (request.hasOptions(type)) {
				boolean like = false;
				for (String d: request.getOptions(type)) {
					if (d.indexOf('%') >= 0) { like = true; break; }
				}
				query.addFrom(type, "StudentGroup g_" + type);
				if (like) {
					String q = "";
					for (String g: request.getOptions(type)) {
						q += (q.isEmpty() ? "" : " or ") + "g_" + type + ".groupAbbreviation like :Xgr" + gid;
						query.addParameter(type, "Xgr" + gid, g);
						gid++;
					}
					query.addWhere(type, "g_" + type + " in elements(s.groups) and lower(g_" + type + ".type.reference) = :Xgr" + gid + " and (" + q + ")");
					query.addParameter(type, "Xgr" + gid, type.toLowerCase());
					gid++;
				} else {
					String group = "";
					for (String g: request.getOptions(type)) {
						group += (group.isEmpty() ? "" : ",") + ":Xgr" + gid;
						query.addParameter(type, "Xgr" + gid, g);
						gid++;
					}
					query.addWhere(type, "g_" + type + " in elements(s.groups) and g_" + type + ".groupAbbreviation in (" + group + ")");
				}
			}
		}
		
		if (request.hasOptions("accommodation")) {
			boolean like = false;
			for (String d: request.getOptions("accommodation")) {
				if (d.indexOf('%') >= 0) { like = true; break; }
			}
			query.addFrom("accommodation", "StudentAccomodation a");
			if (like) {
				String q = "";
				int id = 0;
				for (String a: request.getOptions("accommodation")) {
					q += (q.isEmpty() ? "" : " or ") + "a.abbreviation like :Xacc" + id;
					query.addParameter("accommodation", "Xacc" + id, a);
					id++;
				}
				query.addWhere("accommodation", "a in elements(s.accomodations) and (" + q + ")");
			} else {
				String acc = "";
				int id = 0;
				for (String a: request.getOptions("accommodation")) {
					acc += (acc.isEmpty() ? "" : ",") + ":Xacc" + id;
					query.addParameter("accommodation", "Xacc" + id, a);
					id++;
				}
				query.addWhere("accommodation", "a in elements(s.accomodations) and a.abbreviation in (" + acc + ")");
			}
		}

		if (request.hasOptions("status")) {
			boolean like = false;
			for (String d: request.getOptions("status")) {
				if (d.indexOf('%') >= 0) { like = true; break; }
			}
			if (like) {
				String q = "";
				int id = 0;
				boolean hasDefault = false;
				for (String s: request.getOptions("status")) {
					if ("Not Set".equals(s)) { hasDefault = true; continue; }
					q += (q.isEmpty() ? "" : " or ") + "s.sectioningStatus.reference like :Xst" + id;
					query.addParameter("status", "Xst" + id, s);
					id++;
				}
				if (id > 0) {
					if (hasDefault)
						query.addWhere("status", "s.sectioningStatus is null or " + q);
					else
						query.addWhere("status", q);
				} else if (hasDefault) {
					query.addWhere("status", "s.sectioningStatus is null");
				}
			} else {
				String status = "";
				int id = 0;
				boolean hasDefault = false;
				for (String s: request.getOptions("status")) {
					if ("Not Set".equals(s)) { hasDefault = true; continue; }
					status += (status.isEmpty() ? "" : ",") + ":Xst" + id;
					query.addParameter("status", "Xst" + id, s);
					id++;
				}
				if (id > 0) {
					if (hasDefault)
						query.addWhere("status", "s.sectioningStatus is null or s.sectioningStatus.reference in (" + status + ")");
					else
						query.addWhere("status", "s.sectioningStatus.reference in (" + status + ")");
				} else if (hasDefault) {
					query.addWhere("status", "s.sectioningStatus is null");
				}
			}
		}

		if (request.hasOptions("student") ) {
			String student = "";
			int id = 0;
			for (StringTokenizer s=new StringTokenizer(request.getOption("student").trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken().toUpperCase();
                student += (student.isEmpty() ? "" : " and ") + "(upper(s.firstName) like :Xstd" + id + " || '%' or " +
                		"upper(s.middleName) like :Xstd" + id + " || '%' or upper(s.lastName) like :Xstd" + id + " || '%' or upper(s.email) like :Xstd" + id + " || '%')";
                query.addParameter("student", "Xstd" + id, token);
                id++;
            }
			if (id > 0) {
				student = "(" + student + ") or (upper(trim(trailing ' ' from s.lastName || ', ' || s.firstName || ' ' || s.middleName)) = :Xstd) or (s.externalUniqueId = :Xsid)";
				query.addParameter("student", "Xstd", request.getOption("student").trim().toUpperCase());
				if (ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue()) {
					query.addParameter("student", "Xsid", request.getOption("student").trim().replaceFirst("^0+(?!$)", ""));
				} else {
					query.addParameter("student", "Xsid", request.getOption("student").trim());
				}
				query.addWhere("student", student);
			}
		}
		
		if (request.hasOption("advisor") ) {
			query.addParameter("advisor", "Xadv", request.getOption("advisor"));
			query.addWhere("advisor", "s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = :Xadv and adv.session.uniqueId = s.session.uniqueId)");
		}
		
		if (request.hasOption("course")) {
			query.addParameter("course", "Xco", request.getOption("course"));
			query.addWhere("course", "co.subjectAreaAbbv = :Xco or co.subjectAreaAbbv || ' ' || co.courseNbr = :Xco");
			query.addFrom("course", "inner join s.courseDemands cd inner join cd.courseRequests cr inner join cr.courseOffering co");
		} else if (request.hasOptions("course")) {
			String course = "";
			int id = 0;
			for (String c: request.getOptions("course")) {
				course += (course.isEmpty() ? "" : ",") + ":Xco" + id;
				query.addParameter("course", "Xco" + id, c);
				id++;
			}
			query.addWhere("course", "co.subjectAreaAbbv in (" + course + ") or co.subjectAreaAbbv || ' ' || co.courseNbr in (" + course + ")");
			query.addFrom("course", "inner join s.courseDemands cd inner join cd.courseRequests cr inner join cr.courseOffering co");
		}
		
		if (request.hasOption("credit") && !(server instanceof StudentSolver) && (server instanceof DatabaseServer || ApplicationProperty.OnlineSchedulingDashboardCreditFilterUseDatabase.isTrue())) {
			String term = request.getOption("credit");
			float min = 0, max = Float.MAX_VALUE;
			Credit prefix = Credit.eq;
			String number = term;
			if (number.startsWith("<=")) { prefix = Credit.le; number = number.substring(2); }
			else if (number.startsWith(">=")) { prefix =Credit.ge; number = number.substring(2); }
			else if (number.startsWith("<")) { prefix = Credit.lt; number = number.substring(1); }
			else if (number.startsWith(">")) { prefix = Credit.gt; number = number.substring(1); }
			else if (number.startsWith("=")) { prefix = Credit.eq; number = number.substring(1); }
			String im = null;
			try {
				float a = Float.parseFloat(number);
				switch (prefix) {
					case eq: min = max = a; break; // = a
					case le: max = a; break; // <= a
					case ge: min = a; break; // >= a
					case lt: max = a - 1; break; // < a
					case gt: min = a + 1; break; // > a
				}
			} catch (NumberFormatException e) {
				Matcher m = Pattern.compile("([0-9]+\\.?[0-9]*)([^0-9\\.].*)").matcher(number);
				if (m.matches()) {
					float a = Float.parseFloat(m.group(1));
					im = m.group(2).trim();
					switch (prefix) {
						case eq: min = max = a; break; // = a
						case le: max = a; break; // <= a
						case ge: min = a; break; // >= a
						case lt: max = a - 1; break; // < a
						case gt: min = a + 1; break; // > a
					}
				}
			}
			if (term.contains("..")) {
				try {
					String a = term.substring(0, term.indexOf('.'));
					String b = term.substring(term.indexOf("..") + 2);
					min = Float.parseFloat(a); max = Float.parseFloat(b);
				} catch (NumberFormatException e) {
					Matcher m = Pattern.compile("([0-9]+\\.?[0-9]*)\\.\\.([0-9]+\\.?[0-9]*)([^0-9].*)").matcher(term);
					if (m.matches()) {
						min = Float.parseFloat(m.group(1));
						max = Float.parseFloat(m.group(2));
						im = m.group(3).trim();
					}
				}
			}
			String creditTerm = "(select coalesce(sum(fixedUnits),0) + coalesce(sum(minUnits),0) from CourseCreditUnitConfig where courseOwner.uniqueId in (select courseOffering.uniqueId from StudentClassEnrollment where student = s))";
			if ("!".equals(im)) {
				creditTerm = "(select coalesce(sum(fixedUnits),0) + coalesce(sum(minUnits),0) from CourseCreditUnitConfig where courseOwner.uniqueId in (select courseOffering.uniqueId from StudentClassEnrollment where student = s and clazz.schedulingSubpart.instrOfferingConfig.instructionalMethod is null))";
			} else if (im != null && im.equals(server.getAcademicSession().getDefaultInstructionalMethod())) {
				query.addParameter("credit", "Xim", im.toLowerCase());
				creditTerm = "(select coalesce(sum(fixedUnits),0) + coalesce(sum(minUnits),0) from CourseCreditUnitConfig where courseOwner.uniqueId in (select sce.courseOffering.uniqueId from StudentClassEnrollment sce left outer join sce.clazz.schedulingSubpart.instrOfferingConfig.instructionalMethod im where sce.student = s and (im is null or lower(im.reference) = :Xim)))";
			} else if (im != null) {
				creditTerm = "(select coalesce(sum(fixedUnits),0) + coalesce(sum(minUnits),0) from CourseCreditUnitConfig where courseOwner.uniqueId in (select courseOffering.uniqueId from StudentClassEnrollment where student = s and lower(clazz.schedulingSubpart.instrOfferingConfig.instructionalMethod.reference) = :Xim))";
				query.addParameter("credit", "Xim", im.toLowerCase());
			}
			if (min > 0) {
				if (max < Integer.MAX_VALUE) {
					query.addWhere("credit", creditTerm + " between :Xmin and :Xmax");
					query.addParameter("credit", "Xmin", min);
					query.addParameter("credit", "Xmax", max);
				} else {
					query.addWhere("credit", creditTerm + " >= :Xmin");
					query.addParameter("credit", "Xmin", min);
				}
			} else if (max < Integer.MAX_VALUE) {
				query.addWhere("credit", creditTerm + " <= :Xmax");
				query.addParameter("credit", "Xmax", max);
			}
		}
		
		if (request.hasOption("mode") && "My Students".equals(request.getOption("mode")) && request.hasOption("role")) {
			query.addWhere("mode", "s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = :Xuser and adv.role.reference = :Xrole and adv.session.uniqueId = s.session.uniqueId)");
			query.addParameter("mode", "Xuser", request.getOption("user"));
			query.addParameter("mode", "Xrole", request.getOption("role"));
		}
		if (request.hasOption("mode") && "My Advised".equals(request.getOption("mode")) && request.hasOption("role")) {
			query.addWhere("mode", "s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = :Xuser and adv.role.reference = :Xrole and adv.session.uniqueId = s.session.uniqueId) and s.advisorCourseRequests is not empty");
			query.addParameter("mode", "Xuser", request.getOption("user"));
			query.addParameter("mode", "Xrole", request.getOption("role"));
		}
		if (request.hasOption("mode") && "My Not Advised".equals(request.getOption("mode")) && request.hasOption("role")) {
			query.addWhere("mode", "s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = :Xuser and adv.role.reference = :Xrole and adv.session.uniqueId = s.session.uniqueId) and s.advisorCourseRequests is empty");
			query.addParameter("mode", "Xuser", request.getOption("user"));
			query.addParameter("mode", "Xrole", request.getOption("role"));
		}
		if (request.hasOption("mode") && "Advised".equals(request.getOption("mode"))) {
			query.addWhere("mode", "s.advisorCourseRequests is not empty");
		}
		if (request.hasOption("mode") && "Not Advised".equals(request.getOption("mode"))) {
			query.addWhere("mode", "s.advisorCourseRequests is empty");
		}
		if (request.hasOption("mode") && "PIN Released".equals(request.getOption("mode"))) {
			query.addWhere("mode", "s.pinReleased = true");
		}
		if (request.hasOption("mode") && "PIN Suppressed".equals(request.getOption("mode"))) {
			query.addWhere("mode", "(s.pinReleased is null or s.pinReleased = false)" + (Customization.StudentPinsProvider.hasProvider() ? "" : " and s.pin is not null"));
		}
		if (request.hasOption("mode") && "My PIN Released".equals(request.getOption("mode")) && request.hasOption("role")) {
			query.addWhere("mode", "s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = :Xuser and adv.role.reference = :Xrole and adv.session.uniqueId = s.session.uniqueId) " +
					"and s.pinReleased = true");
			query.addParameter("mode", "Xuser", request.getOption("user"));
			query.addParameter("mode", "Xrole", request.getOption("role"));
		}
		if (request.hasOption("mode") && "My PIN Suppressed".equals(request.getOption("mode")) && request.hasOption("role")) {
			query.addWhere("mode", "s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = :Xuser and adv.role.reference = :Xrole and adv.session.uniqueId = s.session.uniqueId) " +
					"and (s.pinReleased is null or s.pinReleased = false)" + (Customization.StudentPinsProvider.hasProvider() ? "" : " and s.pin is not null"));
			query.addParameter("mode", "Xuser", request.getOption("user"));
			query.addParameter("mode", "Xrole", request.getOption("role"));
		}
		
		if (request.hasOptions("override")) {
			String where = "";
			int id = 0;
			boolean none = false;
			for (String o: request.getOptions("override")) {
				if ("none".equalsIgnoreCase(o) || "null".equalsIgnoreCase(o)) {
					none = true; continue;
				}
				CourseRequestOverrideStatus status = null;
				for (CourseRequestOverrideStatus s: CourseRequestOverrideStatus.values())
					if (s.name().equalsIgnoreCase(o)) { status = s; break; }
				if (status != null) {
					where += (where.isEmpty() ? "" : ",") + ":Xstatus" + id;
					query.addParameter("override", "Xstatus" + id, status.ordinal());
					id++;
				}
			}
			if (id > 0) {
				if (none) {
					query.addFrom("override", "CourseRequest xcr");
					query.addWhere("override", "xcr.courseDemand.student = s and (xcr.overrideStatus is null or s.overrideStatus in (" + where + ") or xcr.overrideStatus in (" + where + "))");
				} else {
					query.addFrom("override", "CourseRequest xcr");
					query.addWhere("override", "xcr.courseDemand.student = s and (s.overrideStatus in (" + where + ") or xcr.overrideStatus in (" + where + "))");
				}
			} else if (none) {
				query.addFrom("override", "CourseRequest xcr");
				query.addWhere("override", "xcr.courseDemand.student = s and xcr.overrideStatus is null");
			}
		}
		
		if (request.hasOptions("prefer")) {
			String where = "";
			int id = 0;
			for (String p: request.getOptions("prefer")) {
				if ("Any Preference".equalsIgnoreCase(p) || "Met Preference".equalsIgnoreCase(p) || "Unmet Preference".equalsIgnoreCase(p)) continue;
				where += (where.isEmpty() ? "" : ",") + ":Xprf" + id;
				query.addParameter("prefer", "Xprf" + id, p);
				id++;
			}
			query.addFrom("prefer", "inner join s.courseDemands pcd inner join pcd.courseRequests pcr inner join pcr.preferences pp");
			if (id > 0)
				query.addWhere("prefer", "pp.label in (" + where + ")");
		}
		
		if (request.hasOptions("require")) {
			String where = "";
			int id = 0;
			for (String p: request.getOptions("require")) {
				if ("Any Requirement".equalsIgnoreCase(p) || "Met Requirement".equalsIgnoreCase(p) || "Unmet Requirement".equalsIgnoreCase(p)) continue;
				where += (where.isEmpty() ? "" : ",") + ":Xprf" + id;
				query.addParameter("require", "Xprf" + id, p);
				id++;
			}
			query.addFrom("require", "inner join s.courseDemands rcd inner join rcd.courseRequests rcr inner join rcr.preferences rp");
			if (id > 0)
				query.addWhere("require", "rp.label in (" + where + ") and rp.required = true");
		}
		
		if (request.hasOption("assignment") && "Wait-Listed".equalsIgnoreCase(request.getOption("assignment"))) {
			query.addFrom("assignment", "CourseRequest wcr");
			if (InstructionalOffering.getDefaultWaitListMode().isWaitlist()) {
				query.addWhere("assignment", "wcr.courseDemand.waitlist = true and wcr.courseDemand.student = s and (wcr.courseOffering.instructionalOffering.waitlistMode is null or wcr.courseOffering.instructionalOffering.waitlistMode = 1)");
			} else {
				query.addWhere("assignment", "wcr.courseDemand.waitlist = true and wcr.courseDemand.student = s and wcr.courseOffering.instructionalOffering.waitlistMode = 1");
			}
		}
		
		if (request.hasOptions("im")) {
			String ims = "";
			boolean hasDefault = false;
			int id = 0;
			for (String im: request.getOptions("im")) {
				if (im.equals(server.getAcademicSession().getDefaultInstructionalMethod()))
					hasDefault = true;
				ims += (ims.isEmpty() ? "" : ",") + ":Xim" + id;
				query.addParameter("im", "Xim" + id, im);
				id++;
			}
			if (hasDefault) {
				query.addWhere("im", "imIm is null or imIm.reference in (" + ims + ")");
				query.addFrom("im", "inner join s.courseDemands imCd inner join imCd.courseRequests imCr inner join imCr.courseOffering imCo inner join imCo.instructionalOffering.instrOfferingConfigs imCfg left outer join imCfg.instructionalMethod imIm");
			} else {
				query.addWhere("im", "imCfg.instructionalMethod.reference in (" + ims + ")");
				query.addFrom("im", "inner join s.courseDemands imCd inner join imCd.courseRequests imCr inner join imCr.courseOffering imCo inner join imCo.instructionalOffering.instrOfferingConfigs imCfg");
			}
		}
		
		return query;
	}
	
	public static enum Credit {
		eq, lt, gt, le, ge
	};
	
	public static class StudentQuery {
		protected Long iSessionId;
		protected Map<String, String> iFrom = new HashMap<String, String>();
		protected Map<String, String> iWhere = new HashMap<String, String>();
		protected Map<String, Map<String, Object>> iParams = new HashMap<String, Map<String,Object>>();
		
		public StudentQuery(Long sessionId) {
			iSessionId = sessionId;
		}
		
		public StudentQuery(StudentQuery q) {
			iSessionId = q.iSessionId;
			iFrom.putAll(q.iFrom);
			iWhere.putAll(q.iWhere);
			iParams.putAll(q.iParams);
		}
		
		public void addFrom(String option, String from) {
			if (from == null)
				iFrom.remove(option);
			else
				iFrom.put(option, from);
		}
		public void addWhere(String option, String where) {
			if (where == null)
				iWhere.remove(option);
			else
				iWhere.put(option, where);
		}

		public void addParameter(String option, String name, Object value) {
			Map<String, Object> params = iParams.get(option);
			if (params == null) { params = new HashMap<String, Object>(); iParams.put(option, params); }
			if (value == null)
				params.remove(name);
			else
				params.put(name, value);
		}
		
		public String getFrom(Collection<String> excludeOption) {
			String from = "";
			for (Map.Entry<String, String> entry: iFrom.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				from += (entry.getValue().startsWith("inner join") ? " " : ", ") + entry.getValue();
			}
			return from;
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
					} else if (param.getValue() instanceof Float) {
						query.setParameter(param.getKey(), (Float)param.getValue());
					} else if (param.getValue() instanceof Double) {
						query.setParameter(param.getKey(), (Double)param.getValue());
					} else if (param.getValue() instanceof Number) {
						query.setParameter(param.getKey(), ((Number)param.getValue()).doubleValue());
					} else if (param.getValue() instanceof String) {
						query.setParameter(param.getKey(), (String)param.getValue());
					} else if (param.getValue() instanceof Boolean) {
						query.setParameter(param.getKey(), (Boolean)param.getValue());
					} else if (param.getValue() instanceof Date) {
						query.setParameter(param.getKey(), (Date)param.getValue());
					} else {
						query.setParameter(param.getKey(), param.getValue().toString());
					}
				}
			}
			return query;
		}
		
		public QueryInstance select(String select) {
			return new QueryInstance(select);
		}
		
		
		public class QueryInstance {
			protected String iSelect = null, iFrom = null, iWhere = null, iOrderBy = null, iGroupBy = null, iType = "Student";
			protected Integer iLimit = null;
			protected Set<String> iExclude = new HashSet<String>();
			protected Map<String, Object> iParams = new HashMap<String, Object>();
			
			private QueryInstance(String select) {
				iSelect = select;
			}
			
			public QueryInstance from(String from) { iFrom = from; return this; }
			public QueryInstance where(String where) {
				if (iWhere == null)
					iWhere = "(" + where + ")";
				else
					iWhere += " and (" + where + ")";
				return this;
			}
			public QueryInstance type(String type) { iType = type; return this; }
			public QueryInstance order(String orderBy) { iOrderBy = orderBy; return this; }
			public QueryInstance group(String groupBy) { iGroupBy = groupBy; return this; }
			public QueryInstance exclude(String excludeOption) { iExclude.add(excludeOption); return this; }
			public QueryInstance set(String param, Object value) { iParams.put(param, value); return this; }
			public QueryInstance limit(Integer limit) { iLimit = (limit == null || limit <= 0 ? null : limit); return this; }
			
			public String query() {
				return
					"select " + (iSelect == null ? "distinct s" : iSelect) +
					" from " + iType + " s left outer join s.areaClasfMajors aac " +  (iFrom == null ? "" : iFrom.trim().toLowerCase().startsWith("inner join") ? " " + iFrom : ", " + iFrom) + getFrom(iExclude) +
					" where s.session.uniqueId = :sessionId" +
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
					} else if (param.getValue() instanceof Float) {
						query.setParameter(param.getKey(), (Float)param.getValue());
					} else if (param.getValue() instanceof Double) {
						query.setParameter(param.getKey(), (Double)param.getValue());
					} else if (param.getValue() instanceof Number) {
						query.setParameter(param.getKey(), ((Number)param.getValue()).doubleValue());
					} else if (param.getValue() instanceof String) {
						query.setParameter(param.getKey(), (String)param.getValue());
					} else if (param.getValue() instanceof Boolean) {
						query.setParameter(param.getKey(), (Boolean)param.getValue());
					} else if (param.getValue() instanceof Date) {
						query.setParameter(param.getKey(), (Date)param.getValue());
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
	
	public Set<Long> getStudentIds(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		return new HashSet<Long>((List<Long>)getQuery(iRequest, server, helper).select("distinct s.uniqueId").query(helper.getHibSession()).list());
	}

	public Map<Long, List<AdvisorCourseRequest>> getAdvisorCourseRequests(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Map<Long, List<AdvisorCourseRequest>> ret = new HashMap<Long, List<AdvisorCourseRequest>>();
		for (Object[] o: (List<Object[]>)getQuery(iRequest, server, helper)
				.select("s.uniqueId, acr")
				.from("inner join s.advisorCourseRequests acr")
				.order("s.uniqueId, acr.priority, acr.alternative").query(helper.getHibSession()).list()) {
			Long studentId = (Long)o[0];
			AdvisorCourseRequest acr = (AdvisorCourseRequest)o[1];
			List<AdvisorCourseRequest> acrs = ret.get(studentId);
			if (acrs == null) {
				acrs = new ArrayList<AdvisorCourseRequest>();
				ret.put(studentId, acrs);
			}
			acrs.add(acr);
		}
		return ret;
	}

	public List<XStudent> getStudens(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<XStudent> students = new ArrayList<XStudent>();
		for (Student student: (List<Student>)getQuery(iRequest, server, helper).select("distinct s").query(helper.getHibSession()).list()) {
			students.add(new XStudent(student, helper, server.getAcademicSession().getFreeTimePattern(), server.getAcademicSession().getDatePatternFirstDate()));
		}
		return students;
	}
	
	public static CourseQuery getCourseQuery(FilterRpcRequest request, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		CourseQuery query = new CourseQuery(getQuery(request, server, helper));
		
		if (request.hasOptions("lookup") && CustomCourseLookupHolder.hasProvider()) {
			query.addFrom("lookup", null);
		}
		
		if (request.hasOption("course")) {
			query.addParameter("course", "Xco", request.getOption("course"));
			query.addWhere("course", "co.subjectAreaAbbv = :Xco or co.subjectAreaAbbv || ' ' || co.courseNbr = :Xco");
			query.addFrom("course", null);
		} else if (request.hasOptions("course")) {
			String course = "";
			int id = 0;
			for (String c: request.getOptions("course")) {
				course += (course.isEmpty() ? "" : ",") + ":Xco" + id;
				query.addParameter("course", "Xco" + id, c);
				id++;
			}
			query.addWhere("course", "co.subjectAreaAbbv in (" + course + ") or co.subjectAreaAbbv || ' ' || co.courseNbr in (" + course + ")");
			query.addFrom("course", null);
		}
		
		if (request.hasOptions("prefer")) {
			query.addFrom("prefer", "inner join cr.preferences pp");
		}
		
		if (request.hasOptions("require")) {
			query.addFrom("require", "inner join cr.preferences rp");
		}
		
		if (request.hasOptions("im")) {
			String ims = "";
			boolean hasDefault = false;
			int id = 0;
			for (String im: request.getOptions("im")) {
				if (im.equals(server.getAcademicSession().getDefaultInstructionalMethod()))
					hasDefault = true;
				ims += (ims.isEmpty() ? "" : ",") + ":Xim" + id;
				query.addParameter("im", "Xim" + id, im);
				id++;
			}
			if (hasDefault) {
				query.addWhere("im", "im is null or im.reference in (" + ims + ")");
				query.addFrom("im", "inner join co.instructionalOffering.instrOfferingConfigs cfg left outer join cfg.instructionalMethod im");
			} else {
				query.addWhere("im", "cfg.instructionalMethod.reference in (" + ims + ")");
				query.addFrom("im", "inner join co.instructionalOffering.instrOfferingConfigs cfg");
			}
		}
		
		if (request.hasOption("assignment") && "Wait-Listed".equalsIgnoreCase(request.getOption("assignment"))) {
			query.addFrom("assignment", null);
			if (InstructionalOffering.getDefaultWaitListMode().isWaitlist()) {
				query.addWhere("assignment", "(co.instructionalOffering.waitlistMode is null or co.instructionalOffering.waitlistMode = 1) and cd.waitlist = true");
			} else {
				query.addWhere("assignment", "co.instructionalOffering.waitlistMode = 1 and cd.waitlist = true");
			}
		}
		
		if (request.hasOptions("override")) {
			String where = "";
			int id = 0;
			boolean none = false;
			for (String o: request.getOptions("override")) {
				if ("none".equalsIgnoreCase(o) || "null".equalsIgnoreCase(o)) {
					none = true; continue;
				}
				CourseRequestOverrideStatus status = null;
				for (CourseRequestOverrideStatus s: CourseRequestOverrideStatus.values())
					if (s.name().equalsIgnoreCase(o)) { status = s; break; }
				if (status != null) {
					where += (where.isEmpty() ? "" : ",") + ":Xstatus" + id;
					query.addParameter("override", "Xstatus" + id, status.ordinal());
					id++;
				}
			}
			if (id > 0) {
				if (none) {
					query.addFrom("override", null);
					query.addWhere("override", "cr.overrideStatus is null or cr.overrideStatus in (" + where + ")");
				} else {
					query.addFrom("override", null);
					query.addWhere("override", "cr.overrideStatus in (" + where + ")");
				}
			} else if (none) {
				query.addFrom("override", null);
				query.addWhere("override", "cr.overrideStatus is null");
			}
		}

		return query;
	}
	
	public List<XCourseId> getCourseIds(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<XCourseId> ids = new ArrayList<XCourseId>(); 
		for (Object[] line: (List<Object[]>)getCourseQuery(iRequest, server, helper).select("distinct co.instructionalOffering.uniqueId, co.uniqueId, co.subjectAreaAbbv, co.courseNbr").query(helper.getHibSession()).list()) {
			ids.add(new XCourseId(((Number)line[0]).longValue(), ((Number)line[1]).longValue(), (String)line[2], (String)line[3]));
		}
		return ids;
	}
	
	public List<XCourse> getCourses(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<XCourse> courses = new ArrayList<XCourse>(); 
		for (CourseOffering co: (List<CourseOffering>)getCourseQuery(iRequest, server, helper).select("distinct co").query(helper.getHibSession()).list()) {
			courses.add(new XCourse(co));
		}
		return courses;
	}
	
	public static class CourseQuery extends StudentQuery {
		
		public CourseQuery(Long sessionId) {
			super(sessionId);
		}
		
		public CourseQuery(StudentQuery q) {
			super(q);
		}		
		
		@Override
		public QueryInstance select(String select) {
			return new QueryInstance(select) {
				@Override
				public String query() {
					return
							"select " + (iSelect == null ? "distinct co" : iSelect) +
							" from CourseRequest cr inner join cr.courseOffering co inner join cr.courseDemand cd inner join cd.student s left outer join s.areaClasfMajors aac " + 
							(iFrom == null ? "" : iFrom.trim().toLowerCase().startsWith("inner join") ? " " + iFrom : ", " + iFrom) + getFrom(iExclude) +
							" where s.session.uniqueId = :sessionId" + getWhere(iExclude) + (iWhere == null ? "" : " and (" + iWhere + ")") +
							(iGroupBy == null ? "" : " group by " + iGroupBy) +
							(iOrderBy == null ? "" : " order by " + iOrderBy);
				}
			};
		}
		
		public QueryInstance selectCourses(String select, FilterRpcRequest request) {
			if (request.hasOption("assignment") && "Wait-Listed".equalsIgnoreCase(request.getOption("assignment"))) {
				if (InstructionalOffering.getDefaultWaitListMode().isWaitlist()) {
					addWhere("assignment", "(co.instructionalOffering.waitlistMode is null or co.instructionalOffering.waitlistMode = 1)");
				} else {
					addWhere("assignment", "co.instructionalOffering.waitlistMode = 1");
				}
			}
			return new QueryInstance(select) {
				@Override
				public String query() {
					return
							"select " + (iSelect == null ? "distinct co" : iSelect) +
							" from CourseOffering co " + 
							(iFrom == null ? "" : iFrom.trim().toLowerCase().startsWith("inner join") ? " " + iFrom : ", " + iFrom) + getFrom(iExclude) +
							" where co.instructionalOffering.session.uniqueId = :sessionId and co.instructionalOffering.notOffered = false" + getWhere(iExclude) + (iWhere == null ? "" : " and (" + iWhere + ")") +
							(iGroupBy == null ? "" : " group by " + iGroupBy) +
							(iOrderBy == null ? "" : " order by " + iOrderBy);
				}
			};
		}
	}
	
	public static boolean hasNoMatchCourses(FilterRpcRequest request, OnlineSectioningHelper helper) {
		if (request.hasOptions("prefer") || request.hasOptions("require"))
			return false;
		if (request.hasOptions("area") || request.hasOptions("classification") || request.hasOptions("degree") || request.hasOptions("program") || request.hasOptions("campus") || request.hasOptions("major") || request.hasOptions("concentration") || request.hasOptions("minor"))
			return false;
		if (request.hasOptions("group") || request.hasOptions("accommodation"))
			return false;
		boolean matchType = false;
		for (StudentGroupType type: StudentGroupTypeDAO.getInstance().findAll(helper.getHibSession()))
			if (request.hasOptions(type.getReference().replace(' ', '_'))) {
				matchType = true; break;
			}
		if (matchType) return false;
		if (request.hasOptions("student") || request.hasOption("advisor") || request.hasOption("credit"))
			return false;
		if (request.hasOption("mode") || request.hasOptions("override") || request.hasOptions("prefer") || request.hasOptions("require"))
			return false;
		if (request.hasOption("assignment") && !"Wait-Listed".equalsIgnoreCase(request.getOption("assignment")))
			return false;
		if (request.hasText()) return false;
		if (request.hasOptions("consent"))
			return false;
		if (request.hasOptions("approver") || request.hasOptions("accommodation") || request.hasOptions("operation") || request.hasOptions("overlap"))
			return false;
		return true;
	}

}
