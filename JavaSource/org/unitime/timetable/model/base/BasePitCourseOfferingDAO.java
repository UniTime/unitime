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

import org.unitime.timetable.model.PitCourseOffering;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PitCourseOfferingDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitCourseOfferingDAO extends _RootDAO<PitCourseOffering,Long> {

	private static PitCourseOfferingDAO sInstance;

	public static PitCourseOfferingDAO getInstance() {
		if (sInstance == null) sInstance = new PitCourseOfferingDAO();
		return sInstance;
	}

	public Class<PitCourseOffering> getReferenceClass() {
		return PitCourseOffering.class;
	}

	@SuppressWarnings("unchecked")
	public List<PitCourseOffering> findBySubjectArea(org.hibernate.Session hibSession, Long subjectAreaId) {
		return hibSession.createQuery("from PitCourseOffering x where x.subjectArea.uniqueId = :subjectAreaId").setLong("subjectAreaId", subjectAreaId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitCourseOffering> findByCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId) {
		return hibSession.createQuery("from PitCourseOffering x where x.courseOffering.uniqueId = :courseOfferingId").setLong("courseOfferingId", courseOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitCourseOffering> findByPitInstructionalOffering(org.hibernate.Session hibSession, Long pitInstructionalOfferingId) {
		return hibSession.createQuery("from PitCourseOffering x where x.pitInstructionalOffering.uniqueId = :pitInstructionalOfferingId").setLong("pitInstructionalOfferingId", pitInstructionalOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitCourseOffering> findByCourseType(org.hibernate.Session hibSession, Long courseTypeId) {
		return hibSession.createQuery("from PitCourseOffering x where x.courseType.uniqueId = :courseTypeId").setLong("courseTypeId", courseTypeId).list();
	}
}
