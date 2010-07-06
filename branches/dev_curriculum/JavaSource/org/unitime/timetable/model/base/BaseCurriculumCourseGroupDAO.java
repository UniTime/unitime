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

import org.unitime.timetable.model.CurriculumCourseGroup;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CurriculumCourseGroupDAO;

public abstract class BaseCurriculumCourseGroupDAO extends _RootDAO {

	private static CurriculumCourseGroupDAO sInstance;

	public static CurriculumCourseGroupDAO getInstance () {
		if (sInstance == null) sInstance = new CurriculumCourseGroupDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CurriculumCourseGroup.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CurriculumCourseGroup get(Long uniqueId) {
		return (CurriculumCourseGroup) get(getReferenceClass(), uniqueId);
	}

	public CurriculumCourseGroup get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CurriculumCourseGroup) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CurriculumCourseGroup load(Long uniqueId) {
		return (CurriculumCourseGroup) load(getReferenceClass(), uniqueId);
	}

	public CurriculumCourseGroup load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CurriculumCourseGroup) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CurriculumCourseGroup loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CurriculumCourseGroup curriculumCourseGroup = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(curriculumCourseGroup)) Hibernate.initialize(curriculumCourseGroup);
		return curriculumCourseGroup;
	}

	public void save(CurriculumCourseGroup curriculumCourseGroup) {
		save((Object) curriculumCourseGroup);
	}

	public void save(CurriculumCourseGroup curriculumCourseGroup, org.hibernate.Session hibSession) {
		save((Object) curriculumCourseGroup, hibSession);
	}

	public void saveOrUpdate(CurriculumCourseGroup curriculumCourseGroup) {
		saveOrUpdate((Object) curriculumCourseGroup);
	}

	public void saveOrUpdate(CurriculumCourseGroup curriculumCourseGroup, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) curriculumCourseGroup, hibSession);
	}


	public void update(CurriculumCourseGroup curriculumCourseGroup) {
		update((Object) curriculumCourseGroup);
	}

	public void update(CurriculumCourseGroup curriculumCourseGroup, org.hibernate.Session hibSession) {
		update((Object) curriculumCourseGroup, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CurriculumCourseGroup curriculumCourseGroup) {
		delete((Object) curriculumCourseGroup);
	}

	public void delete(CurriculumCourseGroup curriculumCourseGroup, org.hibernate.Session hibSession) {
		delete((Object) curriculumCourseGroup, hibSession);
	}

	public void refresh(CurriculumCourseGroup curriculumCourseGroup, org.hibernate.Session hibSession) {
		refresh((Object) curriculumCourseGroup, hibSession);
	}

	public List<CurriculumCourseGroup> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CurriculumCourseGroup").list();
	}

	public List<CurriculumCourseGroup> findByCurriculum(org.hibernate.Session hibSession, Long curriculumId) {
		return hibSession.createQuery("from CurriculumCourseGroup x where x.curriculum.uniqueId = :curriculumId").setLong("curriculumId", curriculumId).list();
	}
}
