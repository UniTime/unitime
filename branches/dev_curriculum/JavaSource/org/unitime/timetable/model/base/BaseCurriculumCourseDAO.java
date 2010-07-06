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

import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CurriculumCourseDAO;

public abstract class BaseCurriculumCourseDAO extends _RootDAO {

	private static CurriculumCourseDAO sInstance;

	public static CurriculumCourseDAO getInstance () {
		if (sInstance == null) sInstance = new CurriculumCourseDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CurriculumCourse.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CurriculumCourse get(Long uniqueId) {
		return (CurriculumCourse) get(getReferenceClass(), uniqueId);
	}

	public CurriculumCourse get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CurriculumCourse) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CurriculumCourse load(Long uniqueId) {
		return (CurriculumCourse) load(getReferenceClass(), uniqueId);
	}

	public CurriculumCourse load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CurriculumCourse) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CurriculumCourse loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CurriculumCourse curriculumCourse = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(curriculumCourse)) Hibernate.initialize(curriculumCourse);
		return curriculumCourse;
	}

	public void save(CurriculumCourse curriculumCourse) {
		save((Object) curriculumCourse);
	}

	public void save(CurriculumCourse curriculumCourse, org.hibernate.Session hibSession) {
		save((Object) curriculumCourse, hibSession);
	}

	public void saveOrUpdate(CurriculumCourse curriculumCourse) {
		saveOrUpdate((Object) curriculumCourse);
	}

	public void saveOrUpdate(CurriculumCourse curriculumCourse, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) curriculumCourse, hibSession);
	}


	public void update(CurriculumCourse curriculumCourse) {
		update((Object) curriculumCourse);
	}

	public void update(CurriculumCourse curriculumCourse, org.hibernate.Session hibSession) {
		update((Object) curriculumCourse, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CurriculumCourse curriculumCourse) {
		delete((Object) curriculumCourse);
	}

	public void delete(CurriculumCourse curriculumCourse, org.hibernate.Session hibSession) {
		delete((Object) curriculumCourse, hibSession);
	}

	public void refresh(CurriculumCourse curriculumCourse, org.hibernate.Session hibSession) {
		refresh((Object) curriculumCourse, hibSession);
	}

	public List<CurriculumCourse> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CurriculumCourse").list();
	}

	public List<CurriculumCourse> findByClassification(org.hibernate.Session hibSession, Long classificationId) {
		return hibSession.createQuery("from CurriculumCourse x where x.classification.uniqueId = :classificationId").setLong("classificationId", classificationId).list();
	}

	public List<CurriculumCourse> findByCourse(org.hibernate.Session hibSession, Long courseId) {
		return hibSession.createQuery("from CurriculumCourse x where x.course.uniqueId = :courseId").setLong("courseId", courseId).list();
	}
}
