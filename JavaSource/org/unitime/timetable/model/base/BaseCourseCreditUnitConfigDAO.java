/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Order;

import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseCreditUnitConfigDAO;

public abstract class BaseCourseCreditUnitConfigDAO extends _RootDAO {

	private static CourseCreditUnitConfigDAO sInstance;

	public static CourseCreditUnitConfigDAO getInstance () {
		if (sInstance == null) sInstance = new CourseCreditUnitConfigDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseCreditUnitConfig.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseCreditUnitConfig get(Long uniqueId) {
		return (CourseCreditUnitConfig) get(getReferenceClass(), uniqueId);
	}

	public CourseCreditUnitConfig get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseCreditUnitConfig) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseCreditUnitConfig load(Long uniqueId) {
		return (CourseCreditUnitConfig) load(getReferenceClass(), uniqueId);
	}

	public CourseCreditUnitConfig load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseCreditUnitConfig) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseCreditUnitConfig loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseCreditUnitConfig courseCreditUnitConfig = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseCreditUnitConfig)) Hibernate.initialize(courseCreditUnitConfig);
		return courseCreditUnitConfig;
	}

	public void save(CourseCreditUnitConfig courseCreditUnitConfig) {
		save((Object) courseCreditUnitConfig);
	}

	public void save(CourseCreditUnitConfig courseCreditUnitConfig, org.hibernate.Session hibSession) {
		save((Object) courseCreditUnitConfig, hibSession);
	}

	public void saveOrUpdate(CourseCreditUnitConfig courseCreditUnitConfig) {
		saveOrUpdate((Object) courseCreditUnitConfig);
	}

	public void saveOrUpdate(CourseCreditUnitConfig courseCreditUnitConfig, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseCreditUnitConfig, hibSession);
	}


	public void update(CourseCreditUnitConfig courseCreditUnitConfig) {
		update((Object) courseCreditUnitConfig);
	}

	public void update(CourseCreditUnitConfig courseCreditUnitConfig, org.hibernate.Session hibSession) {
		update((Object) courseCreditUnitConfig, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseCreditUnitConfig courseCreditUnitConfig) {
		delete((Object) courseCreditUnitConfig);
	}

	public void delete(CourseCreditUnitConfig courseCreditUnitConfig, org.hibernate.Session hibSession) {
		delete((Object) courseCreditUnitConfig, hibSession);
	}

	public void refresh(CourseCreditUnitConfig courseCreditUnitConfig, org.hibernate.Session hibSession) {
		refresh((Object) courseCreditUnitConfig, hibSession);
	}

	public List<CourseCreditUnitConfig> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseCreditUnitConfig").list();
	}

	public List<CourseCreditUnitConfig> findByCreditType(org.hibernate.Session hibSession, Long creditTypeId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.creditType.uniqueId = :creditTypeId").setLong("creditTypeId", creditTypeId).list();
	}

	public List<CourseCreditUnitConfig> findByCreditUnitType(org.hibernate.Session hibSession, Long creditUnitTypeId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.creditUnitType.uniqueId = :creditUnitTypeId").setLong("creditUnitTypeId", creditUnitTypeId).list();
	}

	public List<CourseCreditUnitConfig> findBySubpartOwner(org.hibernate.Session hibSession, Long subpartOwnerId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.subpartOwner.uniqueId = :subpartOwnerId").setLong("subpartOwnerId", subpartOwnerId).list();
	}

	public List<CourseCreditUnitConfig> findByInstructionalOfferingOwner(org.hibernate.Session hibSession, Long instructionalOfferingOwnerId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.instructionalOfferingOwner.uniqueId = :instructionalOfferingOwnerId").setLong("instructionalOfferingOwnerId", instructionalOfferingOwnerId).list();
	}
}
