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

import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AdvisorCourseRequestDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseAdvisorCourseRequestDAO extends _RootDAO<AdvisorCourseRequest,Long> {

	private static AdvisorCourseRequestDAO sInstance;

	public static AdvisorCourseRequestDAO getInstance() {
		if (sInstance == null) sInstance = new AdvisorCourseRequestDAO();
		return sInstance;
	}

	public Class<AdvisorCourseRequest> getReferenceClass() {
		return AdvisorCourseRequest.class;
	}

	@SuppressWarnings("unchecked")
	public List<AdvisorCourseRequest> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from AdvisorCourseRequest x where x.student.uniqueId = :studentId").setLong("studentId", studentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<AdvisorCourseRequest> findByCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId) {
		return hibSession.createQuery("from AdvisorCourseRequest x where x.courseOffering.uniqueId = :courseOfferingId").setLong("courseOfferingId", courseOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<AdvisorCourseRequest> findByFreeTime(org.hibernate.Session hibSession, Long freeTimeId) {
		return hibSession.createQuery("from AdvisorCourseRequest x where x.freeTime.uniqueId = :freeTimeId").setLong("freeTimeId", freeTimeId).list();
	}
}
