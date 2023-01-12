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
package org.unitime.timetable.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.unitime.timetable.model.base.BaseInstructorSurvey;
import org.unitime.timetable.model.dao.InstructorSurveyDAO;

public class InstructorSurvey extends BaseInstructorSurvey {
	private static final long serialVersionUID = 6737724550632383507L;

	public InstructorSurvey() {
		super();
	}

	@Override
	public String htmlLabel() {
		return null;
	}

	@Override
	public Department getDepartment() {
		return null;
	}
	
	public static InstructorSurvey getInstructorSurvey(String externalUniqueId, Long sessionId) {
		return  (InstructorSurvey)InstructorSurveyDAO.getInstance().getSession().createQuery(
				"from InstructorSurvey where session = :sessionId and externalUniqueId = :externalId"
				).setLong("sessionId", sessionId)
				.setString("externalId", externalUniqueId)
				.setMaxResults(1).uniqueResult();
	}
	
	public static InstructorSurvey getInstructorSurvey(DepartmentalInstructor di) {
		if (di.getExternalUniqueId() == null || di.getExternalUniqueId().isEmpty()) return null;
		return getInstructorSurvey(di.getExternalUniqueId(), di.getDepartment().getSessionId());
	}
	
	public static boolean hasInstructorSurveys(Long departmentId) {
		return ((Number)InstructorSurveyDAO.getInstance().getSession().createQuery(
				"select count(s) from DepartmentalInstructor di, InstructorSurvey s where " +
				"s.session = di.department.session and s.externalUniqueId = di.externalUniqueId and " +
				"di.department = :deptId")
				.setLong("deptId", departmentId).setCacheable(true).uniqueResult()
				).intValue() > 0;
	}
	
	public static Map<String, InstructorSurvey> getInstructorSurveysForDepartment(Long departmentId) {
		Map<String, InstructorSurvey> ret = new HashedMap<String, InstructorSurvey>();
		for (InstructorSurvey is: (List<InstructorSurvey>)InstructorSurveyDAO.getInstance().getSession().createQuery(
				"select s from DepartmentalInstructor di, InstructorSurvey s where " +
				"s.session = di.department.session and s.externalUniqueId = di.externalUniqueId and " +
				"di.department = :deptId")
				.setLong("deptId", departmentId).setCacheable(true).list()) {
			ret.put(is.getExternalUniqueId(), is);
		}
		return ret;
	}
	
	public DepartmentalInstructor getInstructor(InstructionalOffering io) {
		Map<Department, DepartmentalInstructor> ret = new HashMap<Department, DepartmentalInstructor>();
		for (DepartmentalInstructor di: (List<DepartmentalInstructor>)InstructorSurveyDAO.getInstance().getSession().createQuery(
				"from DepartmentalInstructor where externalUniqueId = :extId and department.session = :sessionId")
				.setString("extId", getExternalUniqueId())
				.setLong("sessionId", io.getSessionId())
				.setCacheable(true).list()) {
			if (di.getDepartment().equals(io.getDepartment()))
				return di;
			ret.put(di.getDepartment(), di);
		}
		if (ret.isEmpty()) return null;
		if (ret.size() == 1)
			return ret.entrySet().iterator().next().getValue();
		for (CourseOffering co: io.getCourseOfferings()) {
			DepartmentalInstructor di = ret.get(co.getDepartment());
			if (di != null) return di;
		}
		return ret.entrySet().iterator().next().getValue();
	}
}
