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
package org.unitime.timetable.server.instructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.HtmlUtils;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsFilterRpcRequest;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstructorAttributeTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.FilterBoxBackend;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(TeachingRequestsFilterRpcRequest.class)
public class TeachingRequestsFilterBackend extends FilterBoxBackend<TeachingRequestsFilterRpcRequest> {
	protected static GwtMessages MSG = Localization.create(GwtMessages.class);
	
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;
	
	@Override
	public FilterRpcResponse execute(TeachingRequestsFilterRpcRequest request, SessionContext context) {
		context.checkPermission(Right.InstructorScheduling);
		return super.execute(request, context);
	}
	
	@Override
	public void load(TeachingRequestsFilterRpcRequest request, FilterRpcResponse response, SessionContext context) {
		InstructorSchedulingProxy solver = instructorSchedulingSolverService.getSolver();
		Long ownerId = null;
		if (solver != null) {
			ownerId = solver.getProperties().getPropertyLong("General.SolverGroupId", null);
			if (ownerId != null)
				request.setOption("owner", ownerId.toString());
		}
		
		for (SubjectArea sa: SubjectArea.getUserSubjectAreas(context.getUser(), true)) {
			if (ownerId != null && (sa.getDepartment().getSolverGroup() == null || !ownerId.equals(sa.getDepartment().getSolverGroup().getUniqueId())))
				continue;
			boolean hasTeachingPreference = false;
			for (DepartmentalInstructor di: sa.getDepartment().getInstructors())
				if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
					hasTeachingPreference = true;
					break;
				}
			if (!hasTeachingPreference) continue;
			response.add("subject", new Entity(sa.getUniqueId(), sa.getSubjectAreaAbbreviation(), sa.getSubjectAreaAbbreviation() + " - " + HtmlUtils.htmlUnescape(sa.getTitle()),
					"hint", sa.getSubjectAreaAbbreviation() + " - " + HtmlUtils.htmlUnescape(sa.getTitle())));
		}
		
		for (Department d: Department.getUserDepartments(context.getUser())) {
			if (ownerId != null && (d.getSolverGroup() == null || !ownerId.equals(d.getSolverGroup().getUniqueId())))
				continue;
			boolean hasTeachingPreference = false;
			for (DepartmentalInstructor di: d.getInstructors())
				if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
					hasTeachingPreference = true;
					break;
				}
			if (!hasTeachingPreference) continue;
			response.add("department", new Entity(d.getUniqueId(), d.getDeptCode(), d.getDeptCode() + " - " + HtmlUtils.htmlUnescape(d.getLabel())));
		}
		
		Department department = null;
		if (request.hasOption("department"))
			department = Department.findByDeptCode(request.getOption("department"), request.getSessionId(), DepartmentDAO.getInstance().getSession());
		else if (request.hasOption("subject")) {
			SubjectArea subject = SubjectArea.findByAbbv(request.getSessionId(), request.getOption("subject"));
			if (subject != null) department = subject.getDepartment();
		}
		
		for (InstructorAttribute a: InstructorAttribute.getAllGlobalAttributes(request.getSessionId())) {
			response.add(a.getType().getReference().replace(' ', '_'), new Entity(a.getUniqueId(), a.getCode(), a.getName(), "hint", a.getNameWithType()));
		}
		
		if (department != null) {
			for (InstructorAttribute a: InstructorAttribute.getAllDepartmentalAttributes(department.getUniqueId())) {
				response.add(a.getType().getReference().replace(' ', '_'), new Entity(a.getUniqueId(), a.getCode(), a.getName(), "hint", a.getNameWithType()));
			}
		}
	}
	
	protected void fixAttributeTypes(TeachingRequestsFilterRpcRequest request) {
		InstructorSchedulingProxy solver = instructorSchedulingSolverService.getSolver();
		if (solver != null) {
			Long ownerId = solver.getProperties().getPropertyLong("General.SolverGroupId", null);
			if (ownerId != null)
				request.setOption("owner", ownerId.toString());
		}

		for (InstructorAttributeType type: InstructorAttributeTypeDAO.getInstance().findAll())
			if (request.hasOptions(type.getReference().replace(' ', '_')))
				for (String option: request.getOptions(type.getReference().replace(' ', '_')))
					request.addOption("attribute", option);
	}
	@Override
	public void suggestions(TeachingRequestsFilterRpcRequest request, FilterRpcResponse response, SessionContext context) {
		fixAttributeTypes(request);
		
		if (!request.getText().isEmpty()) {
			NameFormat nf = NameFormat.fromReference(UserProperty.NameFormat.get(context.getUser()));
			InstructorQuery.QueryInstance instance = getQuery(request, context.getUser()).select("distinct i").exclude("instructor").order("i.lastName, i.firstName, i.middleName");
			int id = 0;
			String where = "";
			for (StringTokenizer s = new StringTokenizer(request.getText().trim(),", "); s.hasMoreTokens(); ) {
				String token = s.nextToken().toUpperCase();
				if (!where.isEmpty())
					where += " and ";
				where += "(upper(i.firstName) like :cn" + id + " || '%' or upper(i.middleName) like :cn" + id + " || '%' or upper(i.lastName) like :cn" + id + " || '%' or upper(i.email) like :cn" + id + ")";
				instance.set("cn" + id, token);
				id++;
            }
			if (id > 0) {
				instance.where("(" + where + ") or upper(trim(trailing ' ' from i.lastName || ', ' || i.firstName || ' ' || i.middleName)) = :name or i.externalUniqueId = :id");
				instance.set("name", request.getText().trim().toUpperCase());
				instance.set("id", request.getText().trim());
				for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)instance.limit(20).query(DepartmentalInstructorDAO.getInstance().getSession()).list())
					response.addSuggestion(nf.format(instructor), instructor.getExternalUniqueId() == null ? nf.format(instructor) : instructor.getExternalUniqueId(), MSG.fieldInstructor(), "instructor");
			}
		}
		
		if (!request.getText().isEmpty()) {
			RequestQuery.QueryInstance instance = getRequestQuery(request, context.getUser()).select("distinct c").exclude("course").order("c.subjectAreaAbbv, c.courseNbr");
			if (request.hasOption("subject"))
				instance.set("name", request.getText().toUpperCase() + "%").where("upper(c.subjectAreaAbbv || ' ' || c.courseNbr) like :name or c.courseNbr like :name");
			else
				instance.set("name", request.getText().toUpperCase() + "%").where("upper(c.subjectAreaAbbv || ' ' || c.courseNbr) like :name");
			for (CourseOffering co: (List<CourseOffering>)instance.limit(20).query(DepartmentalInstructorDAO.getInstance().getSession()).list()) {
				response.addSuggestion(co.getCourseName(), co.getCourseName(), co.getTitle() == null || co.getTitle().isEmpty() ? MSG.fieldCourse() : co.getTitle(), "course");
			}

		}
	}

	@Override
	public void enumarate(TeachingRequestsFilterRpcRequest request, FilterRpcResponse response, SessionContext context) {
	}
	
	public static InstructorQuery getQuery(FilterRpcRequest request, UserContext user) {
		InstructorQuery query = new InstructorQuery(request.getSessionId() == null ? user.getCurrentAcademicSessionId() : request.getSessionId());
		
		if (request.hasOption("instructorId") ) {
			query.addParameter("instructor", "Xiid", Long.valueOf(request.getOption("instructorId")));
			query.addWhere("instructor", "i.uniqueId = :Xiid");
		} else if (request.hasOptions("instructor") ) {
			String instructor = "";
			int id = 0;
			for (StringTokenizer s=new StringTokenizer(request.getOption("instructor").trim(),", ");s.hasMoreTokens();) {
                String token = s.nextToken().toUpperCase();
                instructor += (instructor.isEmpty() ? "" : " and ") + "(upper(i.firstName) like :Xins" + id + " || '%' or " +
                		"upper(i.middleName) like :Xins" + id + " || '%' or upper(i.lastName) like :Xins" + id + " || '%' or upper(i.email) like :Xins" + id + " || '%')";
                query.addParameter("instructor", "Xins" + id, token);
                id++;
            }
			if (id > 0) {
				instructor = "(" + instructor + ") or (upper(trim(trailing ' ' from i.lastName || ', ' || i.firstName || ' ' || i.middleName)) = :Xins) or (i.externalUniqueId = :Xiid)";
				query.addParameter("instructor", "Xins", request.getOption("instructor").trim().toUpperCase());
				query.addParameter("instructor", "Xiid", request.getOption("instructor").trim());
				query.addWhere("instructor", instructor);
			}
		}
		
		query.addWhere("load", "i.maxLoad > 0");
		query.addWhere("preference", "i.teachingPreference.prefProlog != :Xpref");
		query.addParameter("preference", "Xpref", PreferenceLevel.sProhibited);
		
		if (request.hasOption("department")) {
			query.addParameter("department", "Xdpt", request.getOption("department"));
			query.addWhere("department", "i.department.deptCode = :Xdpt");
		} else if (request.hasOption("subject")) {
			query.addFrom("subject", "SubjectArea s");
			query.addParameter("subject", "Xsa", request.getOption("subject"));
			query.addWhere("subject", "s in elements(i.department.subjectAreas) and s.subjectAreaAbbreviation = :Xsa");
		} else if (request.hasOption("owner")) {
			query.addParameter("owner", "Xsg", Long.valueOf(request.getOption("owner")));
			query.addWhere("owner", "i.department.solverGroup.uniqueId = :Xsg");
		} else {
			String department = "";
			int id = 0;
			for (Department d: Department.getUserDepartments(user)) {
				boolean hasTeachingPreference = false;
				for (DepartmentalInstructor di: d.getInstructors())
					if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
						hasTeachingPreference = true;
						break;
					}
				if (!hasTeachingPreference) continue;
				query.addParameter("department", "Xdpt" + id, d.getUniqueId());
				department += (department.isEmpty() ? "" : ",") + ":Xdpt" + id;
				id++;
			}
			if (id > 0)
				query.addWhere("department", "i.department.uniqueId in (" + department + ")");
		}
		
		if (request.hasOptions("attribute")) {
			query.addFrom("attribute", "InstructorAttribute a");
			String attribute = "";
			int id = 0;
			for (String g: request.getOptions("attribute")) {
				attribute += (attribute.isEmpty() ? "" : ",") + ":Xatt" + id;
				query.addParameter("attribute", "Xatt" + id, g);
				id++;
			}
			query.addWhere("attribute", "a in elements(i.attributes) and a.code in (" + attribute + ")");
		}
		
		return query;
	}
	
	public static class InstructorQuery {
		protected Long iSessionId;
		protected Map<String, String> iFrom = new HashMap<String, String>();
		protected Map<String, String> iWhere = new HashMap<String, String>();
		protected Map<String, Map<String, Object>> iParams = new HashMap<String, Map<String,Object>>();
		
		public InstructorQuery(Long sessionId) {
			iSessionId = sessionId;
		}
		
		public InstructorQuery(InstructorQuery q) {
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
			protected String iSelect = null, iFrom = null, iWhere = null, iOrderBy = null, iGroupBy = null, iType = "DepartmentalInstructor";
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
					"select " + (iSelect == null ? "distinct i" : iSelect) +
					" from " + iType + " i " +  (iFrom == null ? "" : iFrom.trim().toLowerCase().startsWith("inner join") ? " " + iFrom : ", " + iFrom) + getFrom(iExclude) +
					" where i.department.session.uniqueId = :sessionId" +
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
	
	public static RequestQuery getRequestQuery(FilterRpcRequest request, UserContext user) {
		RequestQuery query = new RequestQuery(request.getSessionId() == null ? user.getCurrentAcademicSessionId() : request.getSessionId());
		
		if (request.hasOption("subject")) {
			query.addParameter("owner", "Xsa", request.getOption("subject"));
			query.addWhere("owner", "c.subjectArea.subjectAreaAbbreviation = :Xsa");
		} else if (request.hasOption("subjectId")) {
			query.addParameter("owner", "Xsa", request.getOption("subjectId"));
			query.addWhere("owner", "c.subjectArea.uniqueId = :Xsa");
		} else if (request.hasOption("department")) {
			query.addParameter("owner", "Xdpt", request.getOption("department"));
			query.addWhere("owner", "c.subjectArea.department.deptCode = :Xdpt");
		} else if (request.hasOption("departmentId")) {
			query.addParameter("owner", "Xdpt", request.getOption("department"));
			query.addWhere("owner", "c.subjectArea.department.uniqueId = :Xdpt");
		} else if (request.hasOption("owner")) {
			query.addParameter("owner", "Xsg", Long.valueOf(request.getOption("owner")));
			query.addWhere("owner", "c.subjectArea.department.solverGroup.uniqueId = :Xsg");
		} else if (request.hasOption("offeringId")) {
			query.addParameter("owner", "Xoff", request.getOption("offeringId"));
			query.addWhere("owner", "o.uniqueId = :Xoff");
		} else {
			String department = "";
			int id = 0;
			for (Department d: Department.getUserDepartments(user)) {
				boolean hasTeachingPreference = false;
				for (DepartmentalInstructor di: d.getInstructors())
					if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
						hasTeachingPreference = true;
						break;
					}
				if (!hasTeachingPreference) continue;
				query.addParameter("owner", "Xdpt" + id, d.getUniqueId());
				department += (department.isEmpty() ? "" : ",") + ":Xdpt" + id;
				id++;
			}
			if (id > 0)
				query.addWhere("owner", "c.subjectArea.department.uniqueId in (" + department + ")");
		}
		
		
		if (request.hasOption("course")) {
			query.addWhere("course", "c.subjectAreaAbbv || ' ' || c.courseNbr = :Xcrs");
			query.addParameter("course", "Xcrs", request.getOption("course"));
		}
		
		return query;
	}
	
	public static class RequestQuery {
		protected Long iSessionId;
		protected Map<String, String> iFrom = new HashMap<String, String>();
		protected Map<String, String> iWhere = new HashMap<String, String>();
		protected Map<String, Map<String, Object>> iParams = new HashMap<String, Map<String,Object>>();
		
		public RequestQuery(Long sessionId) {
			iSessionId = sessionId;
		}
		
		public RequestQuery(RequestQuery q) {
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
			protected String iSelect = null, iFrom = null, iWhere = null, iOrderBy = null, iGroupBy = null, iType = "TeachingRequest";
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
					"select " + (iSelect == null ? "distinct r" : iSelect) +
					" from " + iType + " r inner join r.offering o inner join o.courseOfferings c " +  (iFrom == null ? "" : iFrom.trim().toLowerCase().startsWith("inner join") ? " " + iFrom : ", " + iFrom) + getFrom(iExclude) +
					" where c.isControl = true and o.session.uniqueId = :sessionId" +
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
	
	public static class TeachingRequestMatcher implements Query.TermMatcher {
		private TeachingRequestInfo iRequest;
		private InstructorInfo iInstructor;
		private FilterRpcRequest iFilter;
		
		public TeachingRequestMatcher(TeachingRequestInfo request, InstructorInfo instructor, FilterRpcRequest filter) {
			iRequest = request;
			iInstructor = instructor;
			iFilter = filter;
		}
		
		public TeachingRequestMatcher(TeachingRequestInfo request, FilterRpcRequest filter) {
			this(request, null, filter);
		}

		public TeachingRequestMatcher(InstructorInfo instructor, FilterRpcRequest filter) {
			this(null, instructor, filter);
		}
		
		@Override
		public boolean match(String attr, String term) {
			if ("department".equals(attr) || "departmentId".equals(attr)) { return true;}
			if ("subject".equals(attr) || "subjectId".equals(attr)) { return true; }
			if ("owner".equals(attr) || "offeringId".equals(attr)) { return true; }
			if (attr == null || "course".equals(attr)) {
				if (iRequest != null) {
					return iRequest.getCourse().getCourseName().equalsIgnoreCase(term);
				} else {
					for (TeachingRequestInfo r: iInstructor.getAssignedRequests()) {
						if (r.getCourse().getCourseName().equalsIgnoreCase(term)) return true;
					}
					return false;
				}
			}
			if ("attribute".equals(attr)) {
				if (iInstructor != null) {
					for (AttributeInterface a: iInstructor.getAttributes()) {
						if (a.getCode().equalsIgnoreCase(term) || a.getName().equalsIgnoreCase(term)) return true;
					}
					return false;
				} else if (iRequest.hasInstructors()) {
					for (InstructorInfo i: iRequest.getInstructors()) {
						for (AttributeInterface a: i.getAttributes()) {
							if (a.getCode().equalsIgnoreCase(term) || a.getName().equalsIgnoreCase(term)) return true;
						}
					}
					return false;
				} else {
					return true;
				}
			}
			if ("instructor".equals(attr)) {
				if (iInstructor != null) {
					return (iInstructor.hasExternalId() && iInstructor.getExternalId().equals(term)) || iInstructor.getInstructorName().equalsIgnoreCase(term);
				} else if (iRequest.hasInstructors()) {
					for (InstructorInfo i: iRequest.getInstructors()) {
						if ((i.hasExternalId() && i.getExternalId().equals(term)) || i.getInstructorName().equalsIgnoreCase(term)) return true;
					}
					return false;
				} else {
					return true;
				}
			}
			if ("instructorId".equals(attr)) {
				if (iInstructor != null) {
					return iInstructor.getInstructorId().toString().equals(term);
				} else if (iRequest.hasInstructors()) {
					for (InstructorInfo i: iRequest.getInstructors()) {
						if (i.getInstructorId().toString().equals(term)) return true;
					}
					return false;
				} else {
					return true;
				}
			}
			if ("assigned".equals(attr)) {
				if (iRequest != null && iInstructor == null) {
					if ("true".equalsIgnoreCase(term) && iRequest.getNrAssignedInstructors() > 0) return true;
					if (!"true".equalsIgnoreCase(term) && iRequest.getNrAssignedInstructors() < iRequest.getNrInstructors()) return true;
				} else {
					return true;
				}
			}
			if (iInstructor != null) {
				for (AttributeInterface a: iInstructor.getAttributes()) {
					if (a.getType().getLabel().equalsIgnoreCase(attr) && a.getName().equalsIgnoreCase(term))
						return true;
				}
				return false;
			} else if (iRequest.hasInstructors()) {
				for (InstructorInfo i: iRequest.getInstructors()) {
					for (AttributeInterface a: i.getAttributes()) {
						if (a.getType().getLabel().equalsIgnoreCase(attr) && a.getName().equalsIgnoreCase(term))
							return true;
					}
				}
				return false;
			} else if (iFilter.hasOption("assigned") && !"true".equals(iFilter.getOption("assigned"))) {
				for (PreferenceInfo p: iRequest.getAttributePreferences()) {
					if (p.getOwnerName().equalsIgnoreCase(term))
						return true;
				}
				return false;
			} else {
				return true;
			}
		}
	}
	
	public static Query toQuery(FilterRpcRequest request) {
		List<Query.Term> ands = new ArrayList<Query.Term>();
		if (request.hasOptions()) {
			for (Map.Entry<String, Set<String>> option: request.getOptions().entrySet()) {
				List<Query.Term> ors = new ArrayList<Query.Term>();
				for (String value: option.getValue()) {
					ors.add(new Query.AtomTerm(option.getKey(), value));
				}
				if (ors.size() > 1)
					ands.add(new Query.OrTerm(ors));
				else if (ors.size() == 1)
					ands.add(ors.get(0));
			}
		}
		if (request.hasText())
			ands.add(new Query(request.getText()).getQuery());
		if (ands.size() == 1)
			return new Query(ands.get(0));
		return new Query(new Query.AndTerm(ands));
	}
}
