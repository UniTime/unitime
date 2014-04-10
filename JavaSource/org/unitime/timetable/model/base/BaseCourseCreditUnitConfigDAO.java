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

import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseCreditUnitConfigDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCourseCreditUnitConfigDAO extends _RootDAO<CourseCreditUnitConfig,Long> {

	private static CourseCreditUnitConfigDAO sInstance;

	public static CourseCreditUnitConfigDAO getInstance() {
		if (sInstance == null) sInstance = new CourseCreditUnitConfigDAO();
		return sInstance;
	}

	public Class<CourseCreditUnitConfig> getReferenceClass() {
		return CourseCreditUnitConfig.class;
	}

	@SuppressWarnings("unchecked")
	public List<CourseCreditUnitConfig> findByCourseCreditFormat(org.hibernate.Session hibSession, Long courseCreditFormatId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.courseCreditFormat.uniqueId = :courseCreditFormatId").setLong("courseCreditFormatId", courseCreditFormatId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseCreditUnitConfig> findByCreditType(org.hibernate.Session hibSession, Long creditTypeId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.creditType.uniqueId = :creditTypeId").setLong("creditTypeId", creditTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseCreditUnitConfig> findByCreditUnitType(org.hibernate.Session hibSession, Long creditUnitTypeId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.creditUnitType.uniqueId = :creditUnitTypeId").setLong("creditUnitTypeId", creditUnitTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseCreditUnitConfig> findBySubpartOwner(org.hibernate.Session hibSession, Long subpartOwnerId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.subpartOwner.uniqueId = :subpartOwnerId").setLong("subpartOwnerId", subpartOwnerId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseCreditUnitConfig> findByInstructionalOfferingOwner(org.hibernate.Session hibSession, Long instructionalOfferingOwnerId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.instructionalOfferingOwner.uniqueId = :instructionalOfferingOwnerId").setLong("instructionalOfferingOwnerId", instructionalOfferingOwnerId).list();
	}
}
