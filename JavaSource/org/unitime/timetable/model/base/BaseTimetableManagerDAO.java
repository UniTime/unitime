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

import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;

public abstract class BaseTimetableManagerDAO extends _RootDAO {

	private static TimetableManagerDAO sInstance;

	public static TimetableManagerDAO getInstance () {
		if (sInstance == null) sInstance = new TimetableManagerDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return TimetableManager.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public TimetableManager get(Long uniqueId) {
		return (TimetableManager) get(getReferenceClass(), uniqueId);
	}

	public TimetableManager get(Long uniqueId, org.hibernate.Session hibSession) {
		return (TimetableManager) get(getReferenceClass(), uniqueId, hibSession);
	}

	public TimetableManager load(Long uniqueId) {
		return (TimetableManager) load(getReferenceClass(), uniqueId);
	}

	public TimetableManager load(Long uniqueId, org.hibernate.Session hibSession) {
		return (TimetableManager) load(getReferenceClass(), uniqueId, hibSession);
	}

	public TimetableManager loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		TimetableManager timetableManager = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(timetableManager)) Hibernate.initialize(timetableManager);
		return timetableManager;
	}

	public void save(TimetableManager timetableManager) {
		save((Object) timetableManager);
	}

	public void save(TimetableManager timetableManager, org.hibernate.Session hibSession) {
		save((Object) timetableManager, hibSession);
	}

	public void saveOrUpdate(TimetableManager timetableManager) {
		saveOrUpdate((Object) timetableManager);
	}

	public void saveOrUpdate(TimetableManager timetableManager, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) timetableManager, hibSession);
	}


	public void update(TimetableManager timetableManager) {
		update((Object) timetableManager);
	}

	public void update(TimetableManager timetableManager, org.hibernate.Session hibSession) {
		update((Object) timetableManager, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(TimetableManager timetableManager) {
		delete((Object) timetableManager);
	}

	public void delete(TimetableManager timetableManager, org.hibernate.Session hibSession) {
		delete((Object) timetableManager, hibSession);
	}

	public void refresh(TimetableManager timetableManager, org.hibernate.Session hibSession) {
		refresh((Object) timetableManager, hibSession);
	}

	public List<TimetableManager> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from TimetableManager").list();
	}
}
