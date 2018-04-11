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

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XStudent;
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
	
	public SectioningStatusFilterAction forRequest(FilterRpcRequest request) {
		iRequest = request;
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
		
		StudentQuery query = getQuery(iRequest, server);
		
		List<Entity> areas = new ArrayList<Entity>();
		for (Object[] o: (List<Object[]>)query.select("aac.academicArea.uniqueId, aac.academicArea.academicAreaAbbreviation, aac.academicArea.title, count(distinct s.uniqueId)")
				.order("aac.academicArea.academicAreaAbbreviation, aac.academicArea.title").group("aac.academicArea.uniqueId, aac.academicArea.academicAreaAbbreviation, aac.academicArea.title")
				.exclude("area").exclude("major").exclude("course").query(helper.getHibSession()).list()) {
			Entity a = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[2]);
			a.setCount(((Number)o[3]).intValue());
			areas.add(a);
		}
		response.add("area", areas);
		
		if (iRequest.hasOption("area")) {
			List<Entity> majors = new ArrayList<Entity>();
			for (Object[] o: (List<Object[]>)query.select("aac.major.uniqueId, aac.major.code, aac.major.name, count(distinct s)")
					.order("aac.major.code, aac.major.name").group("aac.major.uniqueId, aac.major.code, aac.major.name")
					.exclude("major").exclude("course").query(helper.getHibSession()).list()) {
				Entity m = new Entity(
						(Long)o[0],
						(String)o[1],
						(String)o[2]);
				m.setCount(((Number)o[3]).intValue());
				majors.add(m);
			}
			response.add("major", majors);
		}
		
		List<Entity> classifications = new ArrayList<Entity>();
		for (Object[] o: (List<Object[]>)query.select("aac.academicClassification.uniqueId, aac.academicClassification.code, aac.academicClassification.name, count(distinct s)")
				.order("aac.academicClassification.code, aac.academicClassification.name").group("aac.academicClassification.uniqueId, aac.academicClassification.code, aac.academicClassification.name")
				.exclude("classification").exclude("course").query(helper.getHibSession()).list()) {
			Entity c = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[2]);
			c.setCount(((Number)o[3]).intValue());
			classifications.add(c);
		}
		response.add("classification", classifications);
		
		List<Entity> groups = new ArrayList<Entity>();
		for (Object[] o: (List<Object[]>)query.select("g.uniqueId, g.groupAbbreviation, g.groupName, count(distinct s)")
				.from("StudentGroup g").where("g in elements(s.groups)")
				.order("g.groupAbbreviation, g.groupName").group("g.uniqueId, g.groupAbbreviation, g.groupName")
				.exclude("group").exclude("course").query(helper.getHibSession()).list()) {
			Entity c = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[2]);
			c.setCount(((Number)o[3]).intValue());
			groups.add(c);
		}
		response.add("group", groups);
		
		List<Entity> acc = new ArrayList<Entity>();
		for (Object[] o: (List<Object[]>)query.select("a.uniqueId, a.abbreviation, a.name, count(distinct s)")
				.from("StudentAccomodation a").where("a in elements(s.accomodations)")
				.order("a.abbreviation, a.name").group("a.uniqueId, a.abbreviation, a.name")
				.exclude("accommodation").exclude("course").query(helper.getHibSession()).list()) {
			Entity c = new Entity(
					(Long)o[0],
					(String)o[1],
					(String)o[2]);
			c.setCount(((Number)o[3]).intValue());
			acc.add(c);
		}
		response.add("accommodation", acc);
		
		List<Entity> states = new ArrayList<Entity>();
		int defaultStatus = ((Number)query.select("count(distinct s)").where("s.sectioningStatus is null").exclude("status").query(helper.getHibSession()).uniqueResult()).intValue();
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
				.exclude("status").query(helper.getHibSession()).list()) {
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
		
		List<Entity> assignment = new ArrayList<Entity>();
		assignment.add(new Entity(0l, "Assigned", CONSTANTS.assignmentType()[0], "translated-value", CONSTANTS.assignmentType()[0]));
		assignment.add(new Entity(1l, "Reserved", CONSTANTS.assignmentType()[1], "translated-value", CONSTANTS.assignmentType()[1]));
		assignment.add(new Entity(2l, "Not Assigned", CONSTANTS.assignmentType()[2], "translated-value", CONSTANTS.assignmentType()[2]));
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
				overrides.add(new Entity(new Long(-1 - status.ordinal()), Constants.toInitialCase(status.name()), CONSTANTS.overrideType()[status.ordinal()], "translated-value", CONSTANTS.overrideType()[status.ordinal()])); 
			}
			for (Object[] o: (List<Object[]>)query.select("s.overrideStatus, count(distinct s)").where("s.overrideStatus is not null").order("s.overrideStatus").group("s.overrideStatus").exclude("override").query(helper.getHibSession()).list()) {
				Entity e = overrides.get((Integer)o[0]);
				e.setCount(((Number)o[1]).intValue());
			}
			for (Object[] o: (List<Object[]>)query.select("xcr.overrideStatus, count(distinct xcr)").where("xcr.overrideStatus is not null").order("xcr.overrideStatus").group("xcr.overrideStatus").from("inner join s.courseDemands xcd inner join xcd.courseRequests xcr").exclude("override").query(helper.getHibSession()).list()) {
				Entity e = overrides.get((Integer)o[0]);
				e.setCount(e.getCount() + ((Number)o[1]).intValue());
			}
			for (Iterator<Entity> i = overrides.iterator(); i.hasNext(); )
				if (i.next().getCount() == 0) i.remove();
			if (!overrides.isEmpty())
				response.add("override", overrides);
		}
		
		
		if (iRequest.hasOption("role")) {
			List<Entity> modes = new ArrayList<Entity>();
			int myStudents = ((Number)query.select("count(distinct s)")
					.where("s.uniqueId in (select ads.uniqueId from Advisor adv inner join adv.students ads where adv.externalUniqueId = :Xuser and adv.role.reference = :Xrole and adv.session.uniqueId = s.session.uniqueId)")
					.set("Xuser", iRequest.getOption("user")).set("Xrole", iRequest.getOption("role"))
					.exclude("mode").query(helper.getHibSession()).uniqueResult()).intValue();
			if (myStudents > 0) {
				Entity myE = new Entity(-1l, "My Students", MESSAGES.modeMyStudents(), "translated-value", MESSAGES.modeMyStudents());
				myE.setCount(myStudents);
				modes.add(myE);
			}
			if (!modes.isEmpty())
				response.add("mode", modes);
		}
		
		return response;
	}
	
	public FilterRpcResponse suggestions(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		FilterRpcResponse response = new FilterRpcResponse();

		if (!iRequest.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
			List<SubjectArea> subjects = helper.getHibSession().createQuery("select s from SubjectArea s where s.session.uniqueId = :sessionId and (" +
					"lower(s.subjectAreaAbbreviation) like :name or lower(' ' || s.title) like :title) " +
					"order by s.subjectAreaAbbreviation")
					.setString("name", iRequest.getText().toLowerCase() + "%").setString("title", "% " + iRequest.getText().toLowerCase() + "%")
					.setLong("sessionId", server.getAcademicSession().getUniqueId()).setMaxResults(20).list();
			for (SubjectArea subject: subjects)
				response.addSuggestion(subject.getSubjectAreaAbbreviation() + " - " + subject.getTitle(), subject.getSubjectAreaAbbreviation(), "Subject Area", "course");
			if (subjects.size() == 1) {
				for (CourseOffering course: new TreeSet<CourseOffering>(subjects.get(0).getCourseOfferings())) {
					if (course.getInstructionalOffering().isNotOffered()) continue;
					response.addSuggestion(course.getCourseName() + (course.getTitle() == null ? "" : " - " + course.getTitle()), course.getCourseName(), "Course Offering", "course");
				}
			} else if (subjects.isEmpty()) {
				List<CourseOffering> courses = helper.getHibSession().createQuery("select c from CourseOffering c inner join c.subjectArea s where s.session.uniqueId = :sessionId and (" +
						"lower(s.subjectAreaAbbreviation || ' ' || c.courseNbr) like :name or lower(' ' || c.title) like :title) and c.instructionalOffering.notOffered = false " +
						"order by s.subjectAreaAbbreviation, c.courseNbr")
						.setString("name", iRequest.getText().toLowerCase() + "%").setString("title", "% " + iRequest.getText().toLowerCase() + "%")
						.setLong("sessionId", server.getAcademicSession().getUniqueId()).setMaxResults(20).list();
				for (CourseOffering course: courses) {
					response.addSuggestion(course.getCourseName() + (course.getTitle() == null ? "" : " - " + course.getTitle()), course.getCourseName(), "Course Offering", "course");
				}
			}
		}
		
		StudentQuery query = getQuery(iRequest, server);
		if (!iRequest.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
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
		
		if (!iRequest.getText().isEmpty() && (response.getSuggestions() == null || response.getSuggestions().size() < 20) && "true".equals(iRequest.getOption("approval"))) {
			for (TimetableManager manager: (List<TimetableManager>)helper.getHibSession().createQuery(
					"select distinct m from TimetableManager m inner join m.managerRoles r inner join m.departments d where " +
					" (lower(m.externalUniqueId) like :q || '%' or lower(m.emailAddress) like :q || '%' or lower(m.lastName) || ' ' || lower(m.firstName) like :q || '%')" +
					" and 'ConsentApproval' in elements(r.role.rights) and d.session.uniqueId = :sessionId order by m.lastName, m.firstName, m.middleName"
					).setString("q", iRequest.getText().toLowerCase()).setLong("sessionId", server.getAcademicSession().getUniqueId()).setMaxResults(20).list()) {
				response.addSuggestion(manager.getName(), manager.getName(), "Approved by", "approver");
			}
			
			for (DepartmentalInstructor coordinator: (List<DepartmentalInstructor>)helper.getHibSession().createQuery(
					"select distinct i from CourseOffering c inner join c.instructionalOffering.offeringCoordinators oc inner join oc.instructor i where " +
					"c.subjectArea.session.uniqueId = :sessionId and c.consentType.reference != :reference and " +
					"(lower(i.externalUniqueId) like :q || '%' or lower(i.email) like :q || '%' or lower(i.lastName) || ' ' || lower(i.firstName) like :q || '%') " +
					"order by i.lastName, i.firstName, i.middleName"
					).setString("q", iRequest.getText().toLowerCase()).setString("reference", "IN").setLong("sessionId", server.getAcademicSession().getUniqueId()).setMaxResults(20).list()) {
				response.addSuggestion(coordinator.getNameLastFirst(), coordinator.getNameLastFirst(), "Approved by", "approver");
			}
		}
		
		if (iRequest.getText().length() > 1 && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
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
	
	public static StudentQuery getQuery(FilterRpcRequest request, OnlineSectioningServer server) {
		StudentQuery query = new StudentQuery(server.getAcademicSession().getUniqueId());
		
		if (request.getText() != null && !request.getText().isEmpty()) {
			// ?
		}
		
		if (request.hasOptions("area")) {
			String area = "";
			int id = 0;
			for (String a: request.getOptions("area")) {
				area += (area.isEmpty() ? "" : ",") + ":Xar" + id;
				query.addParameter("area", "Xar" + id, a);
				id++;
			}
			query.addWhere("area", "aac.academicArea.academicAreaAbbreviation in (" + area + ")");
		}

		if (request.hasOptions("classification")) {
			String classf = "";
			int id = 0;
			for (String c: request.getOptions("classification")) {
				classf += (classf.isEmpty() ? "" : ",") + ":Xcf" + id;
				query.addParameter("classification", "Xcf" + id, c);
				id++;
			}
			query.addWhere("classification", "aac.academicClassification.code in (" + classf + ")");
		}

		if (request.hasOptions("major")) {
			String major = "";
			int id = 0;
			for (String m: request.getOptions("major")) {
				major += (major.isEmpty() ? "" : ",") + ":Xmj" + id;
				query.addParameter("major", "Xmj" + id, m);
				id++;
			}
			query.addWhere("major", "aac.major.code in (" + major + ")");
		}
		
		if (request.hasOptions("group")) {
			query.addFrom("group", "StudentGroup g");
			String group = "";
			int id = 0;
			for (String g: request.getOptions("group")) {
				group += (group.isEmpty() ? "" : ",") + ":Xgr" + id;
				query.addParameter("group", "Xgr" + id, g);
				id++;
			}
			query.addWhere("group", "g in elements(s.groups) and g.groupAbbreviation in (" + group + ")");
		}
		
		if (request.hasOptions("accommodation")) {
			query.addFrom("accommodation", "StudentAccomodation a");
			String acc = "";
			int id = 0;
			for (String a: request.getOptions("accommodation")) {
				acc += (acc.isEmpty() ? "" : ",") + ":Xacc" + id;
				query.addParameter("accommodation", "Xacc" + id, a);
				id++;
			}
			query.addWhere("accommodation", "a in elements(s.accomodations) and a.abbreviation in (" + acc + ")");
		}

		if (request.hasOptions("status")) {
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
		
		if (request.hasOption("course")) {
			query.addParameter("course", "Xco", request.getOption("course"));
			query.addWhere("course", "co.subjectAreaAbbv = :Xco or co.subjectAreaAbbv || ' ' || co.courseNbr = :Xco");
			query.addFrom("course", "inner join s.courseDemands cd inner join cd.courseRequests cr inner join cr.courseOffering co");
		}
		
		if (request.hasOption("credit") && !(server instanceof StudentSolver)) {
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
			String creditTerm = "((select coalesce(sum(fixedUnits),0) from FixedCreditUnitConfig where courseOwner in (select courseOffering.uniqueId from StudentClassEnrollment where student = s)) + " +
					// "(select coalesce(sum(fixedUnits),0) from FixedCreditUnitConfig where subpartOwner in (select clazz.schedulingSubpart.uniqueId from StudentClassEnrollment where student = s)) + " +
					// "(select coalesce(sum(minUnits),0) from VariableFixedCreditUnitConfig where subpartOwner in (select clazz.schedulingSubpart.uniqueId from StudentClassEnrollment where student = s)) + " +
					"(select coalesce(sum(minUnits),0) from VariableFixedCreditUnitConfig where courseOwner in (select courseOffering.uniqueId from StudentClassEnrollment where student = s)))";
			if ("!".equals(im)) {
				creditTerm = "((select coalesce(sum(fixedUnits),0) from FixedCreditUnitConfig where courseOwner in (select courseOffering.uniqueId from StudentClassEnrollment where student = s and clazz.schedulingSubpart.instrOfferingConfig.instructionalMethod is not null)) + " +
						"(select coalesce(sum(minUnits),0) from VariableFixedCreditUnitConfig where courseOwner in (select courseOffering.uniqueId from StudentClassEnrollment where student = s and clazz.schedulingSubpart.instrOfferingConfig.instructionalMethod is not null)))";
			} else if (im != null) {
				creditTerm = "((select coalesce(sum(fixedUnits),0) from FixedCreditUnitConfig where courseOwner in (select courseOffering.uniqueId from StudentClassEnrollment where student = s and lower(clazz.schedulingSubpart.instrOfferingConfig.instructionalMethod.reference) = :Xim)) + " +
						"(select coalesce(sum(minUnits),0) from VariableFixedCreditUnitConfig where courseOwner in (select courseOffering.uniqueId from StudentClassEnrollment where student = s and lower(clazz.schedulingSubpart.instrOfferingConfig.instructionalMethod.reference) = :Xim)))";
				query.addParameter("credit", "Xim", im.toLowerCase());
			}
			if (min > 0) {
				if (max < Integer.MAX_VALUE) {
					query.addWhere("credit", creditTerm + " >= :Xmin and " + creditTerm + " <= :Xmax");
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
		
		if (request.hasOption("override")) {
			CourseRequestOverrideStatus status = null;
			for (CourseRequestOverrideStatus s: CourseRequestOverrideStatus.values())
				if (s.name().equalsIgnoreCase(request.getOption("override"))) { status = s; break; }
			if (status != null) {
				query.addFrom("override", "CourseRequest xcr");
				query.addWhere("override", "xcr.courseDemand.student = s and (s.overrideStatus = :Xstatus or xcr.overrideStatus = :Xstatus)");
				query.addParameter("override", "Xstatus", status.ordinal());
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

		protected void addParameter(String option, String name, Object value) {
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
		
		public org.hibernate.Query setParams(org.hibernate.Query query, Collection<String> excludeOption) {
			for (Map.Entry<String, Map<String, Object>> entry: iParams.entrySet()) {
				if (excludeOption != null && excludeOption.contains(entry.getKey())) continue;
				for (Map.Entry<String, Object> param: entry.getValue().entrySet()) {
					if (param.getValue() instanceof Integer) {
						query.setInteger(param.getKey(), (Integer)param.getValue());
					} else if (param.getValue() instanceof Long) {
						query.setLong(param.getKey(), (Long)param.getValue());
					} else if (param.getValue() instanceof String) {
						query.setString(param.getKey(), (String)param.getValue());
					} else if (param.getValue() instanceof Boolean) {
						query.setBoolean(param.getKey(), (Boolean)param.getValue());
					} else if (param.getValue() instanceof Date) {
						query.setDate(param.getKey(), (Date)param.getValue());
					} else {
						query.setString(param.getKey(), param.getValue().toString());
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
			
			public org.hibernate.Query query(org.hibernate.Session hibSession) {
				org.hibernate.Query query = setParams(hibSession.createQuery(query()), iExclude).setLong("sessionId", iSessionId).setCacheable(true);
				for (Map.Entry<String, Object> param: iParams.entrySet()) {
					if (param.getValue() instanceof Integer) {
						query.setInteger(param.getKey(), (Integer)param.getValue());
					} else if (param.getValue() instanceof Long) {
						query.setLong(param.getKey(), (Long)param.getValue());
					} else if (param.getValue() instanceof String) {
						query.setString(param.getKey(), (String)param.getValue());
					} else if (param.getValue() instanceof Boolean) {
						query.setBoolean(param.getKey(), (Boolean)param.getValue());
					} else if (param.getValue() instanceof Date) {
						query.setDate(param.getKey(), (Date)param.getValue());
					} else {
						query.setString(param.getKey(), param.getValue().toString());
					}
				}
				if (iLimit != null)
					query.setMaxResults(iLimit);
				return query;
			}
		}
	}
	
	public Set<Long> getStudentIds(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		return new HashSet<Long>((List<Long>)getQuery(iRequest, server).select("distinct s.uniqueId").query(helper.getHibSession()).list());
	}
	
	public List<XStudent> getStudens(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<XStudent> students = new ArrayList<XStudent>();
		for (XStudent student: (List<XStudent>)getQuery(iRequest, server).select("distinct s").query(helper.getHibSession()).list()) {
			students.add(new XStudent(student));
		}
		return students;
	}
	
	public static CourseQuery getCourseQuery(FilterRpcRequest request, OnlineSectioningServer server) {
		CourseQuery query = new CourseQuery(getQuery(request, server));
		
		if (request.hasOption("course")) {
			query.addParameter("course", "Xco", request.getOption("course"));
			query.addWhere("course", "co.subjectAreaAbbv = :Xco or co.subjectAreaAbbv || ' ' || co.courseNbr = :Xco");
			query.addFrom("course", null);
		}

		return query;
	}
	
	public List<XCourseId> getCourseIds(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<XCourseId> ids = new ArrayList<XCourseId>(); 
		for (Object[] line: (List<Object[]>)getCourseQuery(iRequest, server).select("distinct co.instructionalOffering.uniqueId, co.uniqueId, co.subjectAreaAbbv, co.courseNbr").query(helper.getHibSession()).list()) {
			ids.add(new XCourseId(((Number)line[0]).longValue(), ((Number)line[1]).longValue(), line[2] + " " + line[3]));
		}
		return ids;
	}
	
	public List<XCourse> getCourses(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<XCourse> courses = new ArrayList<XCourse>(); 
		for (CourseOffering co: (List<CourseOffering>)getCourseQuery(iRequest, server).select("distinct co").query(helper.getHibSession()).list()) {
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
	}

}
