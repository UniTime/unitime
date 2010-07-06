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

import org.unitime.timetable.model.CourseCreditType;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseCreditTypeDAO;

public abstract class BaseCourseCreditTypeDAO extends _RootDAO {

	private static CourseCreditTypeDAO sInstance;

	public static CourseCreditTypeDAO getInstance () {
		if (sInstance == null) sInstance = new CourseCreditTypeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseCreditType.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseCreditType get(Long uniqueId) {
		return (CourseCreditType) get(getReferenceClass(), uniqueId);
	}

	public CourseCreditType get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseCreditType) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseCreditType load(Long uniqueId) {
		return (CourseCreditType) load(getReferenceClass(), uniqueId);
	}

	public CourseCreditType load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseCreditType) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseCreditType loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseCreditType courseCreditType = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseCreditType)) Hibernate.initialize(courseCreditType);
		return courseCreditType;
	}

	public void save(CourseCreditType courseCreditType) {
		save((Object) courseCreditType);
	}

	public void save(CourseCreditType courseCreditType, org.hibernate.Session hibSession) {
		save((Object) courseCreditType, hibSession);
	}

	public void saveOrUpdate(CourseCreditType courseCreditType) {
		saveOrUpdate((Object) courseCreditType);
	}

	public void saveOrUpdate(CourseCreditType courseCreditType, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseCreditType, hibSession);
	}


	public void update(CourseCreditType courseCreditType) {
		update((Object) courseCreditType);
	}

	public void update(CourseCreditType courseCreditType, org.hibernate.Session hibSession) {
		update((Object) courseCreditType, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseCreditType courseCreditType) {
		delete((Object) courseCreditType);
	}

	public void delete(CourseCreditType courseCreditType, org.hibernate.Session hibSession) {
		delete((Object) courseCreditType, hibSession);
	}

	public void refresh(CourseCreditType courseCreditType, org.hibernate.Session hibSession) {
		refresh((Object) courseCreditType, hibSession);
	}

	public List<CourseCreditType> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseCreditType").list();
	}
}
