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

import org.unitime.timetable.model.CourseCreditFormat;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseCreditFormatDAO;

public abstract class BaseCourseCreditFormatDAO extends _RootDAO {

	private static CourseCreditFormatDAO sInstance;

	public static CourseCreditFormatDAO getInstance () {
		if (sInstance == null) sInstance = new CourseCreditFormatDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseCreditFormat.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseCreditFormat get(Long uniqueId) {
		return (CourseCreditFormat) get(getReferenceClass(), uniqueId);
	}

	public CourseCreditFormat get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseCreditFormat) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseCreditFormat load(Long uniqueId) {
		return (CourseCreditFormat) load(getReferenceClass(), uniqueId);
	}

	public CourseCreditFormat load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseCreditFormat) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseCreditFormat loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseCreditFormat courseCreditFormat = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseCreditFormat)) Hibernate.initialize(courseCreditFormat);
		return courseCreditFormat;
	}

	public void save(CourseCreditFormat courseCreditFormat) {
		save((Object) courseCreditFormat);
	}

	public void save(CourseCreditFormat courseCreditFormat, org.hibernate.Session hibSession) {
		save((Object) courseCreditFormat, hibSession);
	}

	public void saveOrUpdate(CourseCreditFormat courseCreditFormat) {
		saveOrUpdate((Object) courseCreditFormat);
	}

	public void saveOrUpdate(CourseCreditFormat courseCreditFormat, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseCreditFormat, hibSession);
	}


	public void update(CourseCreditFormat courseCreditFormat) {
		update((Object) courseCreditFormat);
	}

	public void update(CourseCreditFormat courseCreditFormat, org.hibernate.Session hibSession) {
		update((Object) courseCreditFormat, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseCreditFormat courseCreditFormat) {
		delete((Object) courseCreditFormat);
	}

	public void delete(CourseCreditFormat courseCreditFormat, org.hibernate.Session hibSession) {
		delete((Object) courseCreditFormat, hibSession);
	}

	public void refresh(CourseCreditFormat courseCreditFormat, org.hibernate.Session hibSession) {
		refresh((Object) courseCreditFormat, hibSession);
	}

	public List<CourseCreditFormat> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseCreditFormat").list();
	}
}
