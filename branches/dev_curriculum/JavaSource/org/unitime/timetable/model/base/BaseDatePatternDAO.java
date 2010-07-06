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

import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DatePatternDAO;

public abstract class BaseDatePatternDAO extends _RootDAO {

	private static DatePatternDAO sInstance;

	public static DatePatternDAO getInstance () {
		if (sInstance == null) sInstance = new DatePatternDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return DatePattern.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public DatePattern get(Long uniqueId) {
		return (DatePattern) get(getReferenceClass(), uniqueId);
	}

	public DatePattern get(Long uniqueId, org.hibernate.Session hibSession) {
		return (DatePattern) get(getReferenceClass(), uniqueId, hibSession);
	}

	public DatePattern load(Long uniqueId) {
		return (DatePattern) load(getReferenceClass(), uniqueId);
	}

	public DatePattern load(Long uniqueId, org.hibernate.Session hibSession) {
		return (DatePattern) load(getReferenceClass(), uniqueId, hibSession);
	}

	public DatePattern loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		DatePattern datePattern = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(datePattern)) Hibernate.initialize(datePattern);
		return datePattern;
	}

	public void save(DatePattern datePattern) {
		save((Object) datePattern);
	}

	public void save(DatePattern datePattern, org.hibernate.Session hibSession) {
		save((Object) datePattern, hibSession);
	}

	public void saveOrUpdate(DatePattern datePattern) {
		saveOrUpdate((Object) datePattern);
	}

	public void saveOrUpdate(DatePattern datePattern, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) datePattern, hibSession);
	}


	public void update(DatePattern datePattern) {
		update((Object) datePattern);
	}

	public void update(DatePattern datePattern, org.hibernate.Session hibSession) {
		update((Object) datePattern, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(DatePattern datePattern) {
		delete((Object) datePattern);
	}

	public void delete(DatePattern datePattern, org.hibernate.Session hibSession) {
		delete((Object) datePattern, hibSession);
	}

	public void refresh(DatePattern datePattern, org.hibernate.Session hibSession) {
		refresh((Object) datePattern, hibSession);
	}

	public List<DatePattern> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from DatePattern").list();
	}

	public List<DatePattern> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from DatePattern x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
