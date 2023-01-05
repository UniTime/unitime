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
package org.unitime.timetable.model.base;

import java.util.List;

import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.InstructorCourseRequirementDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseInstructorCourseRequirementDAO extends _RootDAO<InstructorCourseRequirement,Long> {

	private static InstructorCourseRequirementDAO sInstance;

	public static InstructorCourseRequirementDAO getInstance() {
		if (sInstance == null) sInstance = new InstructorCourseRequirementDAO();
		return sInstance;
	}

	public Class<InstructorCourseRequirement> getReferenceClass() {
		return InstructorCourseRequirement.class;
	}

	@SuppressWarnings("unchecked")
	public List<InstructorCourseRequirement> findByInstructorSurvey(org.hibernate.Session hibSession, Long instructorSurveyId) {
		return hibSession.createQuery("from InstructorCourseRequirement x where x.instructorSurvey.uniqueId = :instructorSurveyId").setLong("instructorSurveyId", instructorSurveyId).list();
	}

	@SuppressWarnings("unchecked")
	public List<InstructorCourseRequirement> findByCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId) {
		return hibSession.createQuery("from InstructorCourseRequirement x where x.courseOffering.uniqueId = :courseOfferingId").setLong("courseOfferingId", courseOfferingId).list();
	}
}
