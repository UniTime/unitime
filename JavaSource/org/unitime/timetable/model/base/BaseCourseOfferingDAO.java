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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCourseOfferingDAO extends _RootDAO<CourseOffering,Long> {

	private static CourseOfferingDAO sInstance;

	public static CourseOfferingDAO getInstance() {
		if (sInstance == null) sInstance = new CourseOfferingDAO();
		return sInstance;
	}

	public Class<CourseOffering> getReferenceClass() {
		return CourseOffering.class;
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findBySubjectArea(org.hibernate.Session hibSession, Long subjectAreaId) {
		return hibSession.createQuery("from CourseOffering x where x.subjectArea.uniqueId = :subjectAreaId").setLong("subjectAreaId", subjectAreaId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByInstructionalOffering(org.hibernate.Session hibSession, Long instructionalOfferingId) {
		return hibSession.createQuery("from CourseOffering x where x.instructionalOffering.uniqueId = :instructionalOfferingId").setLong("instructionalOfferingId", instructionalOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByDemandOffering(org.hibernate.Session hibSession, Long demandOfferingId) {
		return hibSession.createQuery("from CourseOffering x where x.demandOffering.uniqueId = :demandOfferingId").setLong("demandOfferingId", demandOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByDemandOfferingType(org.hibernate.Session hibSession, Long demandOfferingTypeId) {
		return hibSession.createQuery("from CourseOffering x where x.demandOfferingType.uniqueId = :demandOfferingTypeId").setLong("demandOfferingTypeId", demandOfferingTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByCourseType(org.hibernate.Session hibSession, Long courseTypeId) {
		return hibSession.createQuery("from CourseOffering x where x.courseType.uniqueId = :courseTypeId").setLong("courseTypeId", courseTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByConsentType(org.hibernate.Session hibSession, Long consentTypeId) {
		return hibSession.createQuery("from CourseOffering x where x.consentType.uniqueId = :consentTypeId").setLong("consentTypeId", consentTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByAlternativeOffering(org.hibernate.Session hibSession, Long alternativeOfferingId) {
		return hibSession.createQuery("from CourseOffering x where x.alternativeOffering.uniqueId = :alternativeOfferingId").setLong("alternativeOfferingId", alternativeOfferingId).list();
	}
}
