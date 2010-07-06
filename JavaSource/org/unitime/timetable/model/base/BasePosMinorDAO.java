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

import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PosMinorDAO;

public abstract class BasePosMinorDAO extends _RootDAO {

	private static PosMinorDAO sInstance;

	public static PosMinorDAO getInstance () {
		if (sInstance == null) sInstance = new PosMinorDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return PosMinor.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public PosMinor get(Long uniqueId) {
		return (PosMinor) get(getReferenceClass(), uniqueId);
	}

	public PosMinor get(Long uniqueId, org.hibernate.Session hibSession) {
		return (PosMinor) get(getReferenceClass(), uniqueId, hibSession);
	}

	public PosMinor load(Long uniqueId) {
		return (PosMinor) load(getReferenceClass(), uniqueId);
	}

	public PosMinor load(Long uniqueId, org.hibernate.Session hibSession) {
		return (PosMinor) load(getReferenceClass(), uniqueId, hibSession);
	}

	public PosMinor loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		PosMinor posMinor = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(posMinor)) Hibernate.initialize(posMinor);
		return posMinor;
	}

	public void save(PosMinor posMinor) {
		save((Object) posMinor);
	}

	public void save(PosMinor posMinor, org.hibernate.Session hibSession) {
		save((Object) posMinor, hibSession);
	}

	public void saveOrUpdate(PosMinor posMinor) {
		saveOrUpdate((Object) posMinor);
	}

	public void saveOrUpdate(PosMinor posMinor, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) posMinor, hibSession);
	}


	public void update(PosMinor posMinor) {
		update((Object) posMinor);
	}

	public void update(PosMinor posMinor, org.hibernate.Session hibSession) {
		update((Object) posMinor, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(PosMinor posMinor) {
		delete((Object) posMinor);
	}

	public void delete(PosMinor posMinor, org.hibernate.Session hibSession) {
		delete((Object) posMinor, hibSession);
	}

	public void refresh(PosMinor posMinor, org.hibernate.Session hibSession) {
		refresh((Object) posMinor, hibSession);
	}

	public List<PosMinor> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from PosMinor").list();
	}

	public List<PosMinor> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from PosMinor x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
