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

import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PosMajorDAO;

public abstract class BasePosMajorDAO extends _RootDAO {

	private static PosMajorDAO sInstance;

	public static PosMajorDAO getInstance () {
		if (sInstance == null) sInstance = new PosMajorDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return PosMajor.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public PosMajor get(Long uniqueId) {
		return (PosMajor) get(getReferenceClass(), uniqueId);
	}

	public PosMajor get(Long uniqueId, org.hibernate.Session hibSession) {
		return (PosMajor) get(getReferenceClass(), uniqueId, hibSession);
	}

	public PosMajor load(Long uniqueId) {
		return (PosMajor) load(getReferenceClass(), uniqueId);
	}

	public PosMajor load(Long uniqueId, org.hibernate.Session hibSession) {
		return (PosMajor) load(getReferenceClass(), uniqueId, hibSession);
	}

	public PosMajor loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		PosMajor posMajor = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(posMajor)) Hibernate.initialize(posMajor);
		return posMajor;
	}

	public void save(PosMajor posMajor) {
		save((Object) posMajor);
	}

	public void save(PosMajor posMajor, org.hibernate.Session hibSession) {
		save((Object) posMajor, hibSession);
	}

	public void saveOrUpdate(PosMajor posMajor) {
		saveOrUpdate((Object) posMajor);
	}

	public void saveOrUpdate(PosMajor posMajor, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) posMajor, hibSession);
	}


	public void update(PosMajor posMajor) {
		update((Object) posMajor);
	}

	public void update(PosMajor posMajor, org.hibernate.Session hibSession) {
		update((Object) posMajor, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(PosMajor posMajor) {
		delete((Object) posMajor);
	}

	public void delete(PosMajor posMajor, org.hibernate.Session hibSession) {
		delete((Object) posMajor, hibSession);
	}

	public void refresh(PosMajor posMajor, org.hibernate.Session hibSession) {
		refresh((Object) posMajor, hibSession);
	}

	public List<PosMajor> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from PosMajor").list();
	}

	public List<PosMajor> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from PosMajor x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
