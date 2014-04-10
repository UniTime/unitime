/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
}
