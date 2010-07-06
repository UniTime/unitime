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

import org.unitime.timetable.model.CourseCreditUnitType;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseCreditUnitTypeDAO;

public abstract class BaseCourseCreditUnitTypeDAO extends _RootDAO {

	private static CourseCreditUnitTypeDAO sInstance;

	public static CourseCreditUnitTypeDAO getInstance () {
		if (sInstance == null) sInstance = new CourseCreditUnitTypeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseCreditUnitType.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseCreditUnitType get(Long uniqueId) {
		return (CourseCreditUnitType) get(getReferenceClass(), uniqueId);
	}

	public CourseCreditUnitType get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseCreditUnitType) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseCreditUnitType load(Long uniqueId) {
		return (CourseCreditUnitType) load(getReferenceClass(), uniqueId);
	}

	public CourseCreditUnitType load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseCreditUnitType) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseCreditUnitType loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseCreditUnitType courseCreditUnitType = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseCreditUnitType)) Hibernate.initialize(courseCreditUnitType);
		return courseCreditUnitType;
	}

	public void save(CourseCreditUnitType courseCreditUnitType) {
		save((Object) courseCreditUnitType);
	}

	public void save(CourseCreditUnitType courseCreditUnitType, org.hibernate.Session hibSession) {
		save((Object) courseCreditUnitType, hibSession);
	}

	public void saveOrUpdate(CourseCreditUnitType courseCreditUnitType) {
		saveOrUpdate((Object) courseCreditUnitType);
	}

	public void saveOrUpdate(CourseCreditUnitType courseCreditUnitType, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseCreditUnitType, hibSession);
	}


	public void update(CourseCreditUnitType courseCreditUnitType) {
		update((Object) courseCreditUnitType);
	}

	public void update(CourseCreditUnitType courseCreditUnitType, org.hibernate.Session hibSession) {
		update((Object) courseCreditUnitType, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseCreditUnitType courseCreditUnitType) {
		delete((Object) courseCreditUnitType);
	}

	public void delete(CourseCreditUnitType courseCreditUnitType, org.hibernate.Session hibSession) {
		delete((Object) courseCreditUnitType, hibSession);
	}

	public void refresh(CourseCreditUnitType courseCreditUnitType, org.hibernate.Session hibSession) {
		refresh((Object) courseCreditUnitType, hibSession);
	}

	public List<CourseCreditUnitType> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseCreditUnitType").list();
	}
}
