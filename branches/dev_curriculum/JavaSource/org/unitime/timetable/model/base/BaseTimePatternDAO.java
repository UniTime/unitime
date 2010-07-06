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

import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;

public abstract class BaseTimePatternDAO extends _RootDAO {

	private static TimePatternDAO sInstance;

	public static TimePatternDAO getInstance () {
		if (sInstance == null) sInstance = new TimePatternDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return TimePattern.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public TimePattern get(Long uniqueId) {
		return (TimePattern) get(getReferenceClass(), uniqueId);
	}

	public TimePattern get(Long uniqueId, org.hibernate.Session hibSession) {
		return (TimePattern) get(getReferenceClass(), uniqueId, hibSession);
	}

	public TimePattern load(Long uniqueId) {
		return (TimePattern) load(getReferenceClass(), uniqueId);
	}

	public TimePattern load(Long uniqueId, org.hibernate.Session hibSession) {
		return (TimePattern) load(getReferenceClass(), uniqueId, hibSession);
	}

	public TimePattern loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		TimePattern timePattern = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(timePattern)) Hibernate.initialize(timePattern);
		return timePattern;
	}

	public void save(TimePattern timePattern) {
		save((Object) timePattern);
	}

	public void save(TimePattern timePattern, org.hibernate.Session hibSession) {
		save((Object) timePattern, hibSession);
	}

	public void saveOrUpdate(TimePattern timePattern) {
		saveOrUpdate((Object) timePattern);
	}

	public void saveOrUpdate(TimePattern timePattern, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) timePattern, hibSession);
	}


	public void update(TimePattern timePattern) {
		update((Object) timePattern);
	}

	public void update(TimePattern timePattern, org.hibernate.Session hibSession) {
		update((Object) timePattern, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(TimePattern timePattern) {
		delete((Object) timePattern);
	}

	public void delete(TimePattern timePattern, org.hibernate.Session hibSession) {
		delete((Object) timePattern, hibSession);
	}

	public void refresh(TimePattern timePattern, org.hibernate.Session hibSession) {
		refresh((Object) timePattern, hibSession);
	}

	public List<TimePattern> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from TimePattern").list();
	}

	public List<TimePattern> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from TimePattern x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
