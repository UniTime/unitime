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

import org.unitime.timetable.model.CourseSubpartCredit;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseSubpartCreditDAO;

public abstract class BaseCourseSubpartCreditDAO extends _RootDAO {

	private static CourseSubpartCreditDAO sInstance;

	public static CourseSubpartCreditDAO getInstance () {
		if (sInstance == null) sInstance = new CourseSubpartCreditDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseSubpartCredit.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseSubpartCredit get(Long uniqueId) {
		return (CourseSubpartCredit) get(getReferenceClass(), uniqueId);
	}

	public CourseSubpartCredit get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseSubpartCredit) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseSubpartCredit load(Long uniqueId) {
		return (CourseSubpartCredit) load(getReferenceClass(), uniqueId);
	}

	public CourseSubpartCredit load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseSubpartCredit) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseSubpartCredit loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseSubpartCredit courseSubpartCredit = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseSubpartCredit)) Hibernate.initialize(courseSubpartCredit);
		return courseSubpartCredit;
	}

	public void save(CourseSubpartCredit courseSubpartCredit) {
		save((Object) courseSubpartCredit);
	}

	public void save(CourseSubpartCredit courseSubpartCredit, org.hibernate.Session hibSession) {
		save((Object) courseSubpartCredit, hibSession);
	}

	public void saveOrUpdate(CourseSubpartCredit courseSubpartCredit) {
		saveOrUpdate((Object) courseSubpartCredit);
	}

	public void saveOrUpdate(CourseSubpartCredit courseSubpartCredit, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseSubpartCredit, hibSession);
	}


	public void update(CourseSubpartCredit courseSubpartCredit) {
		update((Object) courseSubpartCredit);
	}

	public void update(CourseSubpartCredit courseSubpartCredit, org.hibernate.Session hibSession) {
		update((Object) courseSubpartCredit, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseSubpartCredit courseSubpartCredit) {
		delete((Object) courseSubpartCredit);
	}

	public void delete(CourseSubpartCredit courseSubpartCredit, org.hibernate.Session hibSession) {
		delete((Object) courseSubpartCredit, hibSession);
	}

	public void refresh(CourseSubpartCredit courseSubpartCredit, org.hibernate.Session hibSession) {
		refresh((Object) courseSubpartCredit, hibSession);
	}

	public List<CourseSubpartCredit> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseSubpartCredit").list();
	}

	public List<CourseSubpartCredit> findByCourseCatalog(org.hibernate.Session hibSession, Long courseCatalogId) {
		return hibSession.createQuery("from CourseSubpartCredit x where x.courseCatalog.uniqueId = :courseCatalogId").setLong("courseCatalogId", courseCatalogId).list();
	}
}
