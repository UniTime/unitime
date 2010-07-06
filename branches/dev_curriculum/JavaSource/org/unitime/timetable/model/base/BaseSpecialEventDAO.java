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

import org.unitime.timetable.model.SpecialEvent;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SpecialEventDAO;

public abstract class BaseSpecialEventDAO extends _RootDAO {

	private static SpecialEventDAO sInstance;

	public static SpecialEventDAO getInstance () {
		if (sInstance == null) sInstance = new SpecialEventDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SpecialEvent.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SpecialEvent get(Long uniqueId) {
		return (SpecialEvent) get(getReferenceClass(), uniqueId);
	}

	public SpecialEvent get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SpecialEvent) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SpecialEvent load(Long uniqueId) {
		return (SpecialEvent) load(getReferenceClass(), uniqueId);
	}

	public SpecialEvent load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SpecialEvent) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SpecialEvent loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SpecialEvent specialEvent = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(specialEvent)) Hibernate.initialize(specialEvent);
		return specialEvent;
	}

	public void save(SpecialEvent specialEvent) {
		save((Object) specialEvent);
	}

	public void save(SpecialEvent specialEvent, org.hibernate.Session hibSession) {
		save((Object) specialEvent, hibSession);
	}

	public void saveOrUpdate(SpecialEvent specialEvent) {
		saveOrUpdate((Object) specialEvent);
	}

	public void saveOrUpdate(SpecialEvent specialEvent, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) specialEvent, hibSession);
	}


	public void update(SpecialEvent specialEvent) {
		update((Object) specialEvent);
	}

	public void update(SpecialEvent specialEvent, org.hibernate.Session hibSession) {
		update((Object) specialEvent, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SpecialEvent specialEvent) {
		delete((Object) specialEvent);
	}

	public void delete(SpecialEvent specialEvent, org.hibernate.Session hibSession) {
		delete((Object) specialEvent, hibSession);
	}

	public void refresh(SpecialEvent specialEvent, org.hibernate.Session hibSession) {
		refresh((Object) specialEvent, hibSession);
	}

	public List<SpecialEvent> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SpecialEvent").list();
	}
}
