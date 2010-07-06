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

import org.unitime.timetable.model.TimePatternTime;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.TimePatternTimeDAO;

public abstract class BaseTimePatternTimeDAO extends _RootDAO {

	private static TimePatternTimeDAO sInstance;

	public static TimePatternTimeDAO getInstance () {
		if (sInstance == null) sInstance = new TimePatternTimeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return TimePatternTime.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public TimePatternTime get(Long uniqueId) {
		return (TimePatternTime) get(getReferenceClass(), uniqueId);
	}

	public TimePatternTime get(Long uniqueId, org.hibernate.Session hibSession) {
		return (TimePatternTime) get(getReferenceClass(), uniqueId, hibSession);
	}

	public TimePatternTime load(Long uniqueId) {
		return (TimePatternTime) load(getReferenceClass(), uniqueId);
	}

	public TimePatternTime load(Long uniqueId, org.hibernate.Session hibSession) {
		return (TimePatternTime) load(getReferenceClass(), uniqueId, hibSession);
	}

	public TimePatternTime loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		TimePatternTime timePatternTime = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(timePatternTime)) Hibernate.initialize(timePatternTime);
		return timePatternTime;
	}

	public void save(TimePatternTime timePatternTime) {
		save((Object) timePatternTime);
	}

	public void save(TimePatternTime timePatternTime, org.hibernate.Session hibSession) {
		save((Object) timePatternTime, hibSession);
	}

	public void saveOrUpdate(TimePatternTime timePatternTime) {
		saveOrUpdate((Object) timePatternTime);
	}

	public void saveOrUpdate(TimePatternTime timePatternTime, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) timePatternTime, hibSession);
	}


	public void update(TimePatternTime timePatternTime) {
		update((Object) timePatternTime);
	}

	public void update(TimePatternTime timePatternTime, org.hibernate.Session hibSession) {
		update((Object) timePatternTime, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(TimePatternTime timePatternTime) {
		delete((Object) timePatternTime);
	}

	public void delete(TimePatternTime timePatternTime, org.hibernate.Session hibSession) {
		delete((Object) timePatternTime, hibSession);
	}

	public void refresh(TimePatternTime timePatternTime, org.hibernate.Session hibSession) {
		refresh((Object) timePatternTime, hibSession);
	}

	public List<TimePatternTime> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from TimePatternTime").list();
	}
}
