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

import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.TimePatternDaysDAO;

public abstract class BaseTimePatternDaysDAO extends _RootDAO {

	private static TimePatternDaysDAO sInstance;

	public static TimePatternDaysDAO getInstance () {
		if (sInstance == null) sInstance = new TimePatternDaysDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return TimePatternDays.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public TimePatternDays get(Long uniqueId) {
		return (TimePatternDays) get(getReferenceClass(), uniqueId);
	}

	public TimePatternDays get(Long uniqueId, org.hibernate.Session hibSession) {
		return (TimePatternDays) get(getReferenceClass(), uniqueId, hibSession);
	}

	public TimePatternDays load(Long uniqueId) {
		return (TimePatternDays) load(getReferenceClass(), uniqueId);
	}

	public TimePatternDays load(Long uniqueId, org.hibernate.Session hibSession) {
		return (TimePatternDays) load(getReferenceClass(), uniqueId, hibSession);
	}

	public TimePatternDays loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		TimePatternDays timePatternDays = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(timePatternDays)) Hibernate.initialize(timePatternDays);
		return timePatternDays;
	}

	public void save(TimePatternDays timePatternDays) {
		save((Object) timePatternDays);
	}

	public void save(TimePatternDays timePatternDays, org.hibernate.Session hibSession) {
		save((Object) timePatternDays, hibSession);
	}

	public void saveOrUpdate(TimePatternDays timePatternDays) {
		saveOrUpdate((Object) timePatternDays);
	}

	public void saveOrUpdate(TimePatternDays timePatternDays, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) timePatternDays, hibSession);
	}


	public void update(TimePatternDays timePatternDays) {
		update((Object) timePatternDays);
	}

	public void update(TimePatternDays timePatternDays, org.hibernate.Session hibSession) {
		update((Object) timePatternDays, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(TimePatternDays timePatternDays) {
		delete((Object) timePatternDays);
	}

	public void delete(TimePatternDays timePatternDays, org.hibernate.Session hibSession) {
		delete((Object) timePatternDays, hibSession);
	}

	public void refresh(TimePatternDays timePatternDays, org.hibernate.Session hibSession) {
		refresh((Object) timePatternDays, hibSession);
	}

	public List<TimePatternDays> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from TimePatternDays").list();
	}
}
