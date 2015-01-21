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
package org.unitime.timetable.server.curricula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CurriculumFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.FilterBoxBackend;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(CurriculumFilterRpcRequest.class)
public class CurriculumFilterBackend extends FilterBoxBackend<CurriculumFilterRpcRequest> {
	
	@Override
	public FilterRpcResponse execute(CurriculumFilterRpcRequest request, SessionContext context) {
		context.checkPermission(Right.CurriculumView);
		return super.execute(request, context);
	}
	
	@Override
	public void load(CurriculumFilterRpcRequest request, FilterRpcResponse response, SessionContext context) {
		Set<Department> userDepts = Department.getUserDepartments(context.getUser());
		
		Map<Long, Entity> areas = new HashMap<Long, Entity>();
		for (Curriculum curriculum: curricula(request.getSessionId(), request.getOptions(), null, -1, "area", userDepts)) {
			Entity area = areas.get(curriculum.getAcademicArea().getUniqueId());
			if (area == null) {
				area = new Entity(curriculum.getAcademicArea().getUniqueId(), curriculum.getAcademicArea().getAcademicAreaAbbreviation(),
						Constants.curriculaToInitialCase(curriculum.getAcademicArea().getTitle()));
				areas.put(area.getUniqueId(), area);
			}
			area.incCount();
		}
		response.add("area", new TreeSet<Entity>(areas.values()));
		
		if (request.hasOption("area")) {
			Map<Long, Entity> majors = new HashMap<Long, Entity>();
			for (Curriculum curriculum: curricula(request.getSessionId(), request.getOptions(), null, -1, "major", userDepts)) {
				for (PosMajor m: curriculum.getMajors()) {
					Entity major = majors.get(m.getUniqueId());
					if (major == null) {
						major = new Entity(m.getUniqueId(), m.getCode(), Constants.curriculaToInitialCase(m.getName()));
						majors.put(major.getUniqueId(), major);
					}
				}
			}
			response.add("major", new TreeSet<Entity>(majors.values()));
		}
		
		Map<Long, Entity> classifications = new HashMap<Long, Entity>();
		for (Curriculum curriculum: curricula(request.getSessionId(), request.getOptions(), null, -1, "classification", userDepts)) {
			for (CurriculumClassification cc: curriculum.getClassifications()) {
				Entity classification = classifications.get(cc.getAcademicClassification().getUniqueId());
				if (classification == null) {
					classification = new Entity(cc.getAcademicClassification().getUniqueId(), cc.getAcademicClassification().getCode(), Constants.curriculaToInitialCase(cc.getAcademicClassification().getName()));
					classifications.put(classification.getUniqueId(), classification);
				}
				classification.incCount();
			}
		}
		response.add("classification", new TreeSet<Entity>(classifications.values()));
		
		Entity managed = new Entity(-1l, "Managed", "Managed Curricula");
		Map<Long, Entity> departments = new HashMap<Long, Entity>();
		for (Curriculum curriculum: curricula(request.getSessionId(), request.getOptions(), null, -1, "department", userDepts)) {
			Entity department = departments.get(curriculum.getDepartment().getUniqueId());
			if (department == null) {
				department = new Entity(curriculum.getDepartment().getUniqueId(), curriculum.getDepartment().getDeptCode(), 
						curriculum.getDepartment().getDeptCode() + " - " + curriculum.getDepartment().getName() + (curriculum.getDepartment().isExternalManager() ? " (" + curriculum.getDepartment().getExternalMgrLabel() + ")" : ""));
				departments.put(department.getUniqueId(), department);
			}
			department.incCount();
			if (userDepts.contains(curriculum.getDepartment())) managed.incCount();
		}
		if (managed.getCount() > 0) response.add("department", managed);
		response.add("department", new TreeSet<Entity>(departments.values()));
	}
	
	public static List<Curriculum> curricula(Long sessionId, Map<String, Set<String>> options, Query query, int limit, String ignoreCommand, Set<Department> userDepartments) {
		org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();

		Set<String> area = (options == null || "area".equals(ignoreCommand) ? null : options.get("area"));
		Set<String> major = (options == null || "major".equals(ignoreCommand) ? null : options.get("major"));
		Set<String> department = (options == null || "department".equals(ignoreCommand) ? null : options.get("department"));
		Set<String> classification = (options == null || "classification".equals(ignoreCommand) ? null : options.get("classification"));
		
		Set<String> nameOrAbbv = (options == null || "curriculum".equals(ignoreCommand) ? null : options.get("curriculum"));
		
		List<Curriculum> curricula = hibSession.createQuery("select distinct c from Curriculum c where c.department.session.uniqueId = :sessionId").setLong("sessionId", sessionId).setCacheable(true).list();
		
		List<Curriculum> ret = new ArrayList<Curriculum>();
		curricula: for (Curriculum curriculum: curricula) {
			if (nameOrAbbv != null && !nameOrAbbv.isEmpty()) {
				for (String fragment: nameOrAbbv) {
					if (fragment.isEmpty()) continue;
					if (fragment.equalsIgnoreCase(curriculum.getAbbv()) || fragment.equalsIgnoreCase(curriculum.getName())) continue;
					words: for (String word: fragment.split(" ")) {
						for (String term: curriculum.getName().split(" "))
							if (word.equalsIgnoreCase(term)) continue words;
						continue curricula;
					}
				}
			}
			if (query != null && !query.match(new CurriculumMatcher(curriculum, userDepartments))) continue;
			
			if (area != null && !area.isEmpty() && !area.contains(curriculum.getAcademicArea().getAcademicAreaAbbreviation()) &&
					!area.contains(Constants.curriculaToInitialCase(curriculum.getAcademicArea().getTitle()))) continue;
			if (department != null && !department.isEmpty()) {
				if (department.contains("Managed")) {
					if (!userDepartments.contains(curriculum.getDepartment())) continue;
				} else {
					if (!department.contains(curriculum.getDepartment().getDeptCode())) continue;
				}
			}
			
			if (major != null && !major.isEmpty()) {
				boolean found = false;
				for (PosMajor m: curriculum.getMajors()) {
					if (major.contains(m.getCode()) || major.contains(Constants.curriculaToInitialCase(m.getName()))) { found = true; break; }
				}
				if (!found) continue;
			}
			
			if (classification != null && !classification.isEmpty()) {
				boolean found = false;
				for (CurriculumClassification cc: curriculum.getClassifications()) {
					if (classification.contains(cc.getName()) || classification.contains(cc.getAcademicClassification().getCode()) || classification.contains(Constants.curriculaToInitialCase(cc.getAcademicClassification().getName()))) { found = true; break; }
				}
				if (!found) continue;
			}
			
			ret.add(curriculum);
		}
		
		return ret;
	}
	
	public static class CurriculumMatcher implements Query.TermMatcher {
		private Curriculum iCurriculum;
		private Set<Department> iManaged;
		
		public CurriculumMatcher(Curriculum c, Set<Department> managed) {
			iCurriculum = c;
			iManaged = managed;
		}
		
		public boolean match(String attr, String term) {
			if (term.isEmpty()) return true;
			if (attr == null || "dept".equals(attr) || "department".equals(attr)) {
				if (("dept".equals(attr) || "department".equals(attr)) && "managed".equalsIgnoreCase(term)) {
					return iManaged.contains(iCurriculum.getDepartment());
				} else {
					if (eq(iCurriculum.getDepartment().getDeptCode(), term) ||
						eq(iCurriculum.getDepartment().getAbbreviation(), term) ||
						has(iCurriculum.getDepartment().getName(), term)) return true;
				}
			}
			if (attr == null || "abbv".equals(attr) || "curriculum".equals(attr) || "abbreviation".equals(attr)) {
				if (eq(iCurriculum.getAbbv(), term)) return true;
			}
			if (attr == null || "name".equals(attr) || "curriculum".equals(attr)) {
				if (has(iCurriculum.getName(), term)) return true;
			}
			if ("starts".equals(attr)) {
				return iCurriculum.getName().startsWith(term) || iCurriculum.getAbbv().startsWith(term);
			}
			if (attr == null || "area".equals(attr)) {
				if (eq(iCurriculum.getAcademicArea().getAcademicAreaAbbreviation(), term) ||
					has(iCurriculum.getAcademicArea().getTitle(), term)) return true;
			}
			if (attr == null || "major".equals(attr)) {
				for (Iterator<PosMajor> i = iCurriculum.getMajors().iterator(); i.hasNext(); ) {
					PosMajor m = i.next();
					if (eq(m.getCode(), term) || has(m.getName(), term)) return true;
				}
			}
			if (attr == null || "clasf".equals(attr) || "classification".equals(attr)) {
				for (CurriculumClassification cc: iCurriculum.getClassifications()) {
					if (eq(cc.getName(), term) || eq(cc.getAcademicClassification().getCode(), term) || has(cc.getAcademicClassification().getName(), term)) return true;
				}
			}
			return false;
		}
		
		private boolean eq(String name, String term) {
			if (name == null) return false;
			return name.equalsIgnoreCase(term);
		}

		private boolean has(String name, String term) {
			if (name == null) return false;
			if (eq(name, term)) return true;
			for (String t: name.split(" "))
				if (t.equalsIgnoreCase(term)) return true;
			return false;
		}
	
	}
	
	private String suggestionQuery(String query) {
		if (query == null || query.isEmpty()) return query;
		if (!query.contains(":") && !query.contains("\""))
			return query + " || starts:\"" + query + "\"";
		return query;
	}

	@Override
	public void suggestions(CurriculumFilterRpcRequest request, FilterRpcResponse response, SessionContext context) {
		for (Curriculum curriculum: curricula(request.getSessionId(), request.getOptions(), new Query(suggestionQuery(request.getText())), 20, null, Department.getUserDepartments(context.getUser())))
			response.addSuggestion(curriculum.getAbbv(), curriculum.getAbbv(), "(" + curriculum.getName() + ")");
	}

	@Override
	public void enumarate(CurriculumFilterRpcRequest request, FilterRpcResponse response, SessionContext context) {
		for (Curriculum curriculum: curricula(request.getSessionId(), request.getOptions(), new Query(request.getText()), -1, null, Department.getUserDepartments(context.getUser()))) {
			response.addResult(new Entity(
					curriculum.getUniqueId(),
					curriculum.getAbbv(),
					curriculum.getName(),
					"department", curriculum.getDepartment().getDeptCode()
					));
		}
	}
}
