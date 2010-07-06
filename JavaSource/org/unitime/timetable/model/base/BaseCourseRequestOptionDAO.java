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

import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseRequestOptionDAO;

public abstract class BaseCourseRequestOptionDAO extends _RootDAO {

	private static CourseRequestOptionDAO sInstance;

	public static CourseRequestOptionDAO getInstance () {
		if (sInstance == null) sInstance = new CourseRequestOptionDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseRequestOption.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseRequestOption get(Long uniqueId) {
		return (CourseRequestOption) get(getReferenceClass(), uniqueId);
	}

	public CourseRequestOption get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseRequestOption) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseRequestOption load(Long uniqueId) {
		return (CourseRequestOption) load(getReferenceClass(), uniqueId);
	}

	public CourseRequestOption load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseRequestOption) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseRequestOption loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseRequestOption courseRequestOption = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseRequestOption)) Hibernate.initialize(courseRequestOption);
		return courseRequestOption;
	}

	public void save(CourseRequestOption courseRequestOption) {
		save((Object) courseRequestOption);
	}

	public void save(CourseRequestOption courseRequestOption, org.hibernate.Session hibSession) {
		save((Object) courseRequestOption, hibSession);
	}

	public void saveOrUpdate(CourseRequestOption courseRequestOption) {
		saveOrUpdate((Object) courseRequestOption);
	}

	public void saveOrUpdate(CourseRequestOption courseRequestOption, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseRequestOption, hibSession);
	}


	public void update(CourseRequestOption courseRequestOption) {
		update((Object) courseRequestOption);
	}

	public void update(CourseRequestOption courseRequestOption, org.hibernate.Session hibSession) {
		update((Object) courseRequestOption, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseRequestOption courseRequestOption) {
		delete((Object) courseRequestOption);
	}

	public void delete(CourseRequestOption courseRequestOption, org.hibernate.Session hibSession) {
		delete((Object) courseRequestOption, hibSession);
	}

	public void refresh(CourseRequestOption courseRequestOption, org.hibernate.Session hibSession) {
		refresh((Object) courseRequestOption, hibSession);
	}

	public List<CourseRequestOption> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseRequestOption").list();
	}

	public List<CourseRequestOption> findByCourseRequest(org.hibernate.Session hibSession, Long courseRequestId) {
		return hibSession.createQuery("from CourseRequestOption x where x.courseRequest.uniqueId = :courseRequestId").setLong("courseRequestId", courseRequestId).list();
	}
}
