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

import org.unitime.timetable.model.CourseHistory;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseHistoryDAO;

public abstract class BaseCourseHistoryDAO extends _RootDAO {

	private static CourseHistoryDAO sInstance;

	public static CourseHistoryDAO getInstance () {
		if (sInstance == null) sInstance = new CourseHistoryDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseHistory.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseHistory get(Long uniqueId) {
		return (CourseHistory) get(getReferenceClass(), uniqueId);
	}

	public CourseHistory get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseHistory) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseHistory load(Long uniqueId) {
		return (CourseHistory) load(getReferenceClass(), uniqueId);
	}

	public CourseHistory load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseHistory) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseHistory loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseHistory courseHistory = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseHistory)) Hibernate.initialize(courseHistory);
		return courseHistory;
	}

	public void save(CourseHistory courseHistory) {
		save((Object) courseHistory);
	}

	public void save(CourseHistory courseHistory, org.hibernate.Session hibSession) {
		save((Object) courseHistory, hibSession);
	}

	public void saveOrUpdate(CourseHistory courseHistory) {
		saveOrUpdate((Object) courseHistory);
	}

	public void saveOrUpdate(CourseHistory courseHistory, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseHistory, hibSession);
	}


	public void update(CourseHistory courseHistory) {
		update((Object) courseHistory);
	}

	public void update(CourseHistory courseHistory, org.hibernate.Session hibSession) {
		update((Object) courseHistory, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseHistory courseHistory) {
		delete((Object) courseHistory);
	}

	public void delete(CourseHistory courseHistory, org.hibernate.Session hibSession) {
		delete((Object) courseHistory, hibSession);
	}

	public void refresh(CourseHistory courseHistory, org.hibernate.Session hibSession) {
		refresh((Object) courseHistory, hibSession);
	}

	public List<CourseHistory> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseHistory").list();
	}
}
