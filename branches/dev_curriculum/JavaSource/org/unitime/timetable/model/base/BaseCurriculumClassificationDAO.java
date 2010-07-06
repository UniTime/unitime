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

import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CurriculumClassificationDAO;

public abstract class BaseCurriculumClassificationDAO extends _RootDAO {

	private static CurriculumClassificationDAO sInstance;

	public static CurriculumClassificationDAO getInstance () {
		if (sInstance == null) sInstance = new CurriculumClassificationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CurriculumClassification.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CurriculumClassification get(Long uniqueId) {
		return (CurriculumClassification) get(getReferenceClass(), uniqueId);
	}

	public CurriculumClassification get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CurriculumClassification) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CurriculumClassification load(Long uniqueId) {
		return (CurriculumClassification) load(getReferenceClass(), uniqueId);
	}

	public CurriculumClassification load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CurriculumClassification) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CurriculumClassification loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CurriculumClassification curriculumClassification = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(curriculumClassification)) Hibernate.initialize(curriculumClassification);
		return curriculumClassification;
	}

	public void save(CurriculumClassification curriculumClassification) {
		save((Object) curriculumClassification);
	}

	public void save(CurriculumClassification curriculumClassification, org.hibernate.Session hibSession) {
		save((Object) curriculumClassification, hibSession);
	}

	public void saveOrUpdate(CurriculumClassification curriculumClassification) {
		saveOrUpdate((Object) curriculumClassification);
	}

	public void saveOrUpdate(CurriculumClassification curriculumClassification, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) curriculumClassification, hibSession);
	}


	public void update(CurriculumClassification curriculumClassification) {
		update((Object) curriculumClassification);
	}

	public void update(CurriculumClassification curriculumClassification, org.hibernate.Session hibSession) {
		update((Object) curriculumClassification, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CurriculumClassification curriculumClassification) {
		delete((Object) curriculumClassification);
	}

	public void delete(CurriculumClassification curriculumClassification, org.hibernate.Session hibSession) {
		delete((Object) curriculumClassification, hibSession);
	}

	public void refresh(CurriculumClassification curriculumClassification, org.hibernate.Session hibSession) {
		refresh((Object) curriculumClassification, hibSession);
	}

	public List<CurriculumClassification> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CurriculumClassification").list();
	}

	public List<CurriculumClassification> findByCurriculum(org.hibernate.Session hibSession, Long curriculumId) {
		return hibSession.createQuery("from CurriculumClassification x where x.curriculum.uniqueId = :curriculumId").setLong("curriculumId", curriculumId).list();
	}

	public List<CurriculumClassification> findByAcademicClassification(org.hibernate.Session hibSession, Long academicClassificationId) {
		return hibSession.createQuery("from CurriculumClassification x where x.academicClassification.uniqueId = :academicClassificationId").setLong("academicClassificationId", academicClassificationId).list();
	}
}
