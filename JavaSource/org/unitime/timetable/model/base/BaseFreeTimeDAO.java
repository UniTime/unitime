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

import org.unitime.timetable.model.FreeTime;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.FreeTimeDAO;

public abstract class BaseFreeTimeDAO extends _RootDAO {

	private static FreeTimeDAO sInstance;

	public static FreeTimeDAO getInstance () {
		if (sInstance == null) sInstance = new FreeTimeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return FreeTime.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public FreeTime get(Long uniqueId) {
		return (FreeTime) get(getReferenceClass(), uniqueId);
	}

	public FreeTime get(Long uniqueId, org.hibernate.Session hibSession) {
		return (FreeTime) get(getReferenceClass(), uniqueId, hibSession);
	}

	public FreeTime load(Long uniqueId) {
		return (FreeTime) load(getReferenceClass(), uniqueId);
	}

	public FreeTime load(Long uniqueId, org.hibernate.Session hibSession) {
		return (FreeTime) load(getReferenceClass(), uniqueId, hibSession);
	}

	public FreeTime loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		FreeTime freeTime = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(freeTime)) Hibernate.initialize(freeTime);
		return freeTime;
	}

	public void save(FreeTime freeTime) {
		save((Object) freeTime);
	}

	public void save(FreeTime freeTime, org.hibernate.Session hibSession) {
		save((Object) freeTime, hibSession);
	}

	public void saveOrUpdate(FreeTime freeTime) {
		saveOrUpdate((Object) freeTime);
	}

	public void saveOrUpdate(FreeTime freeTime, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) freeTime, hibSession);
	}


	public void update(FreeTime freeTime) {
		update((Object) freeTime);
	}

	public void update(FreeTime freeTime, org.hibernate.Session hibSession) {
		update((Object) freeTime, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(FreeTime freeTime) {
		delete((Object) freeTime);
	}

	public void delete(FreeTime freeTime, org.hibernate.Session hibSession) {
		delete((Object) freeTime, hibSession);
	}

	public void refresh(FreeTime freeTime, org.hibernate.Session hibSession) {
		refresh((Object) freeTime, hibSession);
	}

	public List<FreeTime> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from FreeTime").list();
	}

	public List<FreeTime> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from FreeTime x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
