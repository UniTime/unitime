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

import org.unitime.timetable.model.CourseEvent;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseEventDAO;

public abstract class BaseCourseEventDAO extends _RootDAO {

	private static CourseEventDAO sInstance;

	public static CourseEventDAO getInstance () {
		if (sInstance == null) sInstance = new CourseEventDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseEvent.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseEvent get(Long uniqueId) {
		return (CourseEvent) get(getReferenceClass(), uniqueId);
	}

	public CourseEvent get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseEvent) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseEvent load(Long uniqueId) {
		return (CourseEvent) load(getReferenceClass(), uniqueId);
	}

	public CourseEvent load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseEvent) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseEvent loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseEvent courseEvent = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseEvent)) Hibernate.initialize(courseEvent);
		return courseEvent;
	}

	public void save(CourseEvent courseEvent) {
		save((Object) courseEvent);
	}

	public void save(CourseEvent courseEvent, org.hibernate.Session hibSession) {
		save((Object) courseEvent, hibSession);
	}

	public void saveOrUpdate(CourseEvent courseEvent) {
		saveOrUpdate((Object) courseEvent);
	}

	public void saveOrUpdate(CourseEvent courseEvent, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseEvent, hibSession);
	}


	public void update(CourseEvent courseEvent) {
		update((Object) courseEvent);
	}

	public void update(CourseEvent courseEvent, org.hibernate.Session hibSession) {
		update((Object) courseEvent, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseEvent courseEvent) {
		delete((Object) courseEvent);
	}

	public void delete(CourseEvent courseEvent, org.hibernate.Session hibSession) {
		delete((Object) courseEvent, hibSession);
	}

	public void refresh(CourseEvent courseEvent, org.hibernate.Session hibSession) {
		refresh((Object) courseEvent, hibSession);
	}

	public List<CourseEvent> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseEvent").list();
	}
}
