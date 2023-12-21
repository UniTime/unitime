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

import java.util.List;

import org.unitime.timetable.model.base.BaseInstructorCourseRequirement;
import org.unitime.timetable.model.dao.InstructorCourseRequirementDAO;

public class InstructorCourseRequirement extends BaseInstructorCourseRequirement implements Comparable<InstructorCourseRequirement> {
	private static final long serialVersionUID = 3076787808984760805L;

	public InstructorCourseRequirement() {
		super();
	}

	public int compareTo(InstructorCourseRequirement req) { 
		if (getInstructorSurvey().equals(req.getInstructorSurvey())) {
			// same survey
			int cmp = getCourse().compareTo(req.getCourse());
			if (cmp != 0) return cmp;
			InstructorCourseRequirementNote n1 = getFirstNote();
			InstructorCourseRequirementNote n2 = req.getFirstNote();
			if (n1 == null) {
				if (n2 == null) return getUniqueId().compareTo(req.getUniqueId());
				else return 1;
			} else if (n2 == null) {
				return -1;
			}
			cmp = n1.getType().getSortOrder().compareTo(n2.getType().getSortOrder());
			if (cmp != 0) return cmp;
			cmp = (n1.getNote() == null ? "" : n1.getNote()).compareTo(n2.getNote() == null ? "" : n2.getNote());
			if (cmp != 0) return cmp;
		} else {
			int cmp = getInstructorSurvey().getExternalUniqueId().compareTo(req.getInstructorSurvey().getExternalUniqueId());
			if (cmp != 0) return cmp;
		}
		return getUniqueId().compareTo(req.getUniqueId());
	}
	
	public InstructorCourseRequirementNote getFirstNote() {
		InstructorCourseRequirementNote ret = null;
		for (InstructorCourseRequirementNote note: getNotes()) {
			if (ret == null || note.getType().getSortOrder() < ret.getType().getSortOrder())
				ret = note;
		}
		return ret;
	}
	
	public InstructorCourseRequirementNote getNote(InstructorCourseRequirementType type) {
		for (InstructorCourseRequirementNote note: getNotes()) {
			if (note.getType().equals(type)) return note;
		}
		return null;
	}
	
	public static List<InstructorCourseRequirement> getRequirementsForOffering(InstructionalOffering io) {
		return (List<InstructorCourseRequirement>)InstructorCourseRequirementDAO.getInstance().getSession().createQuery(
				"select r from InstructorCourseRequirement r, CourseOffering co " +
				"where co.instructionalOffering = :offeringId and " +
				"r.instructorSurvey.session = co.instructionalOffering.session and " +
				"(r.courseOffering = co or r.course = (co.subjectAreaAbbv || ' ' || co.courseNbr)) and " +
				"r.instructorSurvey.submitted is not null"
				).setLong("offeringId", io.getUniqueId())
				.setCacheable(true).list();
	}
	
	public static boolean hasRequirementsForOffering(InstructionalOffering io) {
		int reqs = ((Number)InstructorCourseRequirementDAO.getInstance().getSession().createQuery(
				"select count(r) from InstructorCourseRequirement r, CourseOffering co " +
				"where co.instructionalOffering = :offeringId and " +
				"r.instructorSurvey.session = co.instructionalOffering.session and " +
				"(r.courseOffering = co or r.course = (co.subjectAreaAbbv || ' ' || co.courseNbr)) and " +
				"r.instructorSurvey.submitted is not null"
				).setLong("offeringId", io.getUniqueId())
				.setCacheable(true).uniqueResult()).intValue();
		if (reqs > 0) return true;
		int survs = ((Number)InstructorCourseRequirementDAO.getInstance().getSession().createQuery(
				"select count(s) from InstructorSurvey s where s.submitted is not null and s.externalUniqueId in " +
				"(select ci.instructor.externalUniqueId from ClassInstructor ci where ci.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId = :offeringId and ci.instructor.externalUniqueId is not null)" +
				" and s.session.uniqueId = :sessionId and (s.preferences is not empty or length(s.note) > 0)"
				).setLong("sessionId", io.getSessionId()).setLong("offeringId", io.getUniqueId())
				.setCacheable(true).uniqueResult()).intValue();
		if (survs > 0) return true;
		return false;
	}
}
