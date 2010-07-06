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

import org.unitime.timetable.model.History;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.HistoryDAO;

public abstract class BaseHistoryDAO extends _RootDAO {

	private static HistoryDAO sInstance;

	public static HistoryDAO getInstance () {
		if (sInstance == null) sInstance = new HistoryDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return History.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public History get(Long uniqueId) {
		return (History) get(getReferenceClass(), uniqueId);
	}

	public History get(Long uniqueId, org.hibernate.Session hibSession) {
		return (History) get(getReferenceClass(), uniqueId, hibSession);
	}

	public History load(Long uniqueId) {
		return (History) load(getReferenceClass(), uniqueId);
	}

	public History load(Long uniqueId, org.hibernate.Session hibSession) {
		return (History) load(getReferenceClass(), uniqueId, hibSession);
	}

	public History loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		History history = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(history)) Hibernate.initialize(history);
		return history;
	}

	public void save(History history) {
		save((Object) history);
	}

	public void save(History history, org.hibernate.Session hibSession) {
		save((Object) history, hibSession);
	}

	public void saveOrUpdate(History history) {
		saveOrUpdate((Object) history);
	}

	public void saveOrUpdate(History history, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) history, hibSession);
	}


	public void update(History history) {
		update((Object) history);
	}

	public void update(History history, org.hibernate.Session hibSession) {
		update((Object) history, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(History history) {
		delete((Object) history);
	}

	public void delete(History history, org.hibernate.Session hibSession) {
		delete((Object) history, hibSession);
	}

	public void refresh(History history, org.hibernate.Session hibSession) {
		refresh((Object) history, hibSession);
	}

	public List<History> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from History").list();
	}

	public List<History> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from History x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
