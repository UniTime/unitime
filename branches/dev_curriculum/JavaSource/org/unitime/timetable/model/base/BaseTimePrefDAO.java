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

import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.TimePrefDAO;

public abstract class BaseTimePrefDAO extends _RootDAO {

	private static TimePrefDAO sInstance;

	public static TimePrefDAO getInstance () {
		if (sInstance == null) sInstance = new TimePrefDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return TimePref.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public TimePref get(Long uniqueId) {
		return (TimePref) get(getReferenceClass(), uniqueId);
	}

	public TimePref get(Long uniqueId, org.hibernate.Session hibSession) {
		return (TimePref) get(getReferenceClass(), uniqueId, hibSession);
	}

	public TimePref load(Long uniqueId) {
		return (TimePref) load(getReferenceClass(), uniqueId);
	}

	public TimePref load(Long uniqueId, org.hibernate.Session hibSession) {
		return (TimePref) load(getReferenceClass(), uniqueId, hibSession);
	}

	public TimePref loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		TimePref timePref = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(timePref)) Hibernate.initialize(timePref);
		return timePref;
	}

	public void save(TimePref timePref) {
		save((Object) timePref);
	}

	public void save(TimePref timePref, org.hibernate.Session hibSession) {
		save((Object) timePref, hibSession);
	}

	public void saveOrUpdate(TimePref timePref) {
		saveOrUpdate((Object) timePref);
	}

	public void saveOrUpdate(TimePref timePref, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) timePref, hibSession);
	}


	public void update(TimePref timePref) {
		update((Object) timePref);
	}

	public void update(TimePref timePref, org.hibernate.Session hibSession) {
		update((Object) timePref, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(TimePref timePref) {
		delete((Object) timePref);
	}

	public void delete(TimePref timePref, org.hibernate.Session hibSession) {
		delete((Object) timePref, hibSession);
	}

	public void refresh(TimePref timePref, org.hibernate.Session hibSession) {
		refresh((Object) timePref, hibSession);
	}

	public List<TimePref> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from TimePref").list();
	}

	public List<TimePref> findByTimePattern(org.hibernate.Session hibSession, Long timePatternId) {
		return hibSession.createQuery("from TimePref x where x.timePattern.uniqueId = :timePatternId").setLong("timePatternId", timePatternId).list();
	}
}
