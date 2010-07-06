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

import org.hibernate.Hibernate;
import org.hibernate.criterion.Order;

import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RefTableEntryDAO;

public abstract class BaseRefTableEntryDAO extends _RootDAO {

	private static RefTableEntryDAO sInstance;

	public static RefTableEntryDAO getInstance () {
		if (sInstance == null) sInstance = new RefTableEntryDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return RefTableEntry.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public RefTableEntry get(Long uniqueId) {
		return (RefTableEntry) get(getReferenceClass(), uniqueId);
	}

	public RefTableEntry get(Long uniqueId, org.hibernate.Session hibSession) {
		return (RefTableEntry) get(getReferenceClass(), uniqueId, hibSession);
	}

	public RefTableEntry load(Long uniqueId) {
		return (RefTableEntry) load(getReferenceClass(), uniqueId);
	}

	public RefTableEntry load(Long uniqueId, org.hibernate.Session hibSession) {
		return (RefTableEntry) load(getReferenceClass(), uniqueId, hibSession);
	}

	public RefTableEntry loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		RefTableEntry refTableEntry = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(refTableEntry)) Hibernate.initialize(refTableEntry);
		return refTableEntry;
	}

	public void save(RefTableEntry refTableEntry) {
		save((Object) refTableEntry);
	}

	public void save(RefTableEntry refTableEntry, org.hibernate.Session hibSession) {
		save((Object) refTableEntry, hibSession);
	}

	public void saveOrUpdate(RefTableEntry refTableEntry) {
		saveOrUpdate((Object) refTableEntry);
	}

	public void saveOrUpdate(RefTableEntry refTableEntry, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) refTableEntry, hibSession);
	}


	public void update(RefTableEntry refTableEntry) {
		update((Object) refTableEntry);
	}

	public void update(RefTableEntry refTableEntry, org.hibernate.Session hibSession) {
		update((Object) refTableEntry, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(RefTableEntry refTableEntry) {
		delete((Object) refTableEntry);
	}

	public void delete(RefTableEntry refTableEntry, org.hibernate.Session hibSession) {
		delete((Object) refTableEntry, hibSession);
	}

	public void refresh(RefTableEntry refTableEntry, org.hibernate.Session hibSession) {
		refresh((Object) refTableEntry, hibSession);
	}
}
