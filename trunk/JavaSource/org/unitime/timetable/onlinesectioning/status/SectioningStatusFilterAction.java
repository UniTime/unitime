/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.onlinesectioning.status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.model.CourseOffering;
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
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class SectioningStatusFilterAction implements OnlineSectioningAction<FilterRpcResponse> {
	private static final long serialVersionUID = 1L;
	
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
				.exclude("area").exclude("major").query(helper.getHibSession()).list()) {
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
			for (Object[] o: (List<Object[]>)query.select("m.uniqueId, m.code, m.name, count(distinct s)")
					.from("PosMajor m").where("m in elements(s.posMajors)")
					.order("m.code, m.name").group("m.uniqueId, m.code, m.name")
					.exclude("major").query(helper.getHibSession()).list()) {
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
				.exclude("classification").query(helper.getHibSession()).list()) {
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
				.exclude("group").query(helper.getHibSession()).list()) {
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
				.exclude("accommodation").query(helper.getHibSession()).list()) {
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
		assignment.add(new Entity(0l, "Assigned", "Assigned"));
		assignment.add(new Entity(1l, "Reserved", "Reserved"));
		assignment.add(new Entity(2l, "Not Assigned", "Not Assigned"));
		assignment.add(new Entity(3l, "Wait-Listed", "Wait-Listed"));
		response.add("assignment", assignment);
		
		List<Entity> consent = new ArrayList<Entity>();
		consent.add(new Entity(-1l, "Consent", "Any Consent Needed"));
		for (OfferingConsentType type: OfferingConsentType.getConsentTypeList())
			consent.add(new Entity(type.getUniqueId(), type.getAbbv(), type.getLabel()));
		consent.add(new Entity(-2l, "No Consent", "Consent Not Needed"));
		consent.add(new Entity(-3l, "Waiting", "Consent Waiting Approval"));
		consent.add(new Entity(-4l, "Approved", "Consent Approved"));
		consent.add(new Entity(-5l, "To Do", "Waiting My Approval"));
		response.add("consent", consent);
		
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
						"lower(s.subjectAreaAbbreviation || '.' || c.courseNbr) like :name or lower(' ' || c.title) like :title) and c.instructionalOffering.notOffered = false " +
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
			StudentQuery.StudentInstance instance = query.select("distinct s").exclude("student").order("s.lastName, s.firstName, s.middleName");
			
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
				instance.set("id", iRequest.getText().trim());
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
					"select distinct i from CourseOffering c inner join c.instructionalOffering.coordinators i where " +
					"c.subjectArea.session.uniqueId = :sessionId and c.consentType.reference != :reference and " +
					"(lower(i.externalUniqueId) like :q || '%' or lower(i.email) like :q || '%' or lower(i.lastName) || ' ' || lower(i.firstName) like :q || '%') " +
					"order by i.lastName, i.firstName, i.middleName"
					).setString("q", iRequest.getText().toLowerCase()).setString("reference", "IN").setLong("sessionId", server.getAcademicSession().getUniqueId()).setMaxResults(20).list()) {
				response.addSuggestion(coordinator.getNameLastFirst(), coordinator.getNameLastFirst(), "Approved by", "approver");
			}
		}
		
		if (iRequest.getText().length() > 1 && (response.getSuggestions() == null || response.getSuggestions().size() < 20)) {
			StudentQuery.StudentInstance instance = query.select("distinct l.operation").exclude("operation").order("l.operation")
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
			query.addFrom("major", "PosMajor m");
			String major = "";
			int id = 0;
			for (String m: request.getOptions("major")) {
				major += (major.isEmpty() ? "" : ",") + ":Xmj" + id;
				query.addParameter("major", "Xmj" + id, m);
				id++;
			}
			query.addWhere("major", "m in elements(s.posMajors) and m.code in (" + major + ")");
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
				status = (status.isEmpty() ? "" : ",") + ":Xst" + id;
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
				query.addParameter("student", "Xsid", request.getOption("student").trim());
				query.addWhere("student", student);
			}
		}

		return query;
	}
	
	public static class StudentQuery {
		private Long iSessionId;
		private Map<String, String> iFrom = new HashMap<String, String>();
		private Map<String, String> iWhere = new HashMap<String, String>();
		private Map<String, Map<String, Object>> iParams = new HashMap<String, Map<String,Object>>();
		
		public StudentQuery(Long sessionId) {
			iSessionId = sessionId;
		}
		
		public void addFrom(String option, String from) { iFrom.put(option, from); }
		public void addWhere(String option, String where) { iWhere.put(option, where); }

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
		
		public StudentInstance select(String select) {
			return new StudentInstance(select);
		}
		
		
		public class StudentInstance {
			private String iSelect = null, iFrom = null, iWhere = null, iOrderBy = null, iGroupBy = null, iType = "Student";
			private Integer iLimit = null;
			private Set<String> iExclude = new HashSet<String>();
			private Map<String, Object> iParams = new HashMap<String, Object>();
			
			private StudentInstance(String select) {
				iSelect = select;
			}
			
			public StudentInstance from(String from) { iFrom = from; return this; }
			public StudentInstance where(String where) { 
				if (iWhere == null)
					iWhere = "(" + where + ")";
				else
					iWhere += " and (" + where + ")";
				return this;
			}
			public StudentInstance type(String type) { iType = type; return this; }
			public StudentInstance order(String orderBy) { iOrderBy = orderBy; return this; }
			public StudentInstance group(String groupBy) { iGroupBy = groupBy; return this; }
			public StudentInstance exclude(String excludeOption) { iExclude.add(excludeOption); return this; }
			public StudentInstance set(String param, Object value) { iParams.put(param, value); return this; }
			public StudentInstance limit(Integer limit) { iLimit = (limit == null || limit <= 0 ? null : limit); return this; }
			
			public String query() {
				return
					"select " + (iSelect == null ? "distinct s" : iSelect) +
					" from " + iType + " s left outer join s.academicAreaClassifications aac " + 
					(iFrom == null ? "" : iFrom.trim().toLowerCase().startsWith("inner join") ? " " + iFrom :
						", " + iFrom) + getFrom(iExclude) +
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

}
