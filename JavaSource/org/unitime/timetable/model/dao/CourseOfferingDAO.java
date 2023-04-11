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
package org.unitime.timetable.model.dao;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
import java.util.List;
import org.unitime.timetable.model.CourseOffering;

public class CourseOfferingDAO extends _RootDAO<CourseOffering,Long> {
	private static CourseOfferingDAO sInstance;

	public CourseOfferingDAO() {}

	public static CourseOfferingDAO getInstance() {
		if (sInstance == null) sInstance = new CourseOfferingDAO();
		return sInstance;
	}

	public Class<CourseOffering> getReferenceClass() {
		return CourseOffering.class;
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findBySubjectArea(org.hibernate.Session hibSession, Long subjectAreaId) {
		return hibSession.createQuery("from CourseOffering x where x.subjectArea.uniqueId = :subjectAreaId").setParameter("subjectAreaId", subjectAreaId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByInstructionalOffering(org.hibernate.Session hibSession, Long instructionalOfferingId) {
		return hibSession.createQuery("from CourseOffering x where x.instructionalOffering.uniqueId = :instructionalOfferingId").setParameter("instructionalOfferingId", instructionalOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByDemandOffering(org.hibernate.Session hibSession, Long demandOfferingId) {
		return hibSession.createQuery("from CourseOffering x where x.demandOffering.uniqueId = :demandOfferingId").setParameter("demandOfferingId", demandOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByDemandOfferingType(org.hibernate.Session hibSession, Long demandOfferingTypeId) {
		return hibSession.createQuery("from CourseOffering x where x.demandOfferingType.uniqueId = :demandOfferingTypeId").setParameter("demandOfferingTypeId", demandOfferingTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByCourseType(org.hibernate.Session hibSession, Long courseTypeId) {
		return hibSession.createQuery("from CourseOffering x where x.courseType.uniqueId = :courseTypeId").setParameter("courseTypeId", courseTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByConsentType(org.hibernate.Session hibSession, Long consentTypeId) {
		return hibSession.createQuery("from CourseOffering x where x.consentType.uniqueId = :consentTypeId").setParameter("consentTypeId", consentTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByAlternativeOffering(org.hibernate.Session hibSession, Long alternativeOfferingId) {
		return hibSession.createQuery("from CourseOffering x where x.alternativeOffering.uniqueId = :alternativeOfferingId").setParameter("alternativeOfferingId", alternativeOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseOffering> findByFundingDept(org.hibernate.Session hibSession, Long fundingDeptId) {
		return hibSession.createQuery("from CourseOffering x where x.fundingDept.uniqueId = :fundingDeptId").setParameter("fundingDeptId", fundingDeptId).list();
	}
}
