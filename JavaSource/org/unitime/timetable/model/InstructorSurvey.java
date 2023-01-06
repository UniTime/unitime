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
}
