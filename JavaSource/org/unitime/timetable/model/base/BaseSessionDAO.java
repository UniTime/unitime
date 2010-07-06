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

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SessionDAO;

public abstract class BaseSessionDAO extends _RootDAO {

	private static SessionDAO sInstance;

	public static SessionDAO getInstance () {
		if (sInstance == null) sInstance = new SessionDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Session.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Session get(Long uniqueId) {
		return (Session) get(getReferenceClass(), uniqueId);
	}

	public Session get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Session) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Session load(Long uniqueId) {
		return (Session) load(getReferenceClass(), uniqueId);
	}

	public Session load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Session) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Session loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Session session = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(session)) Hibernate.initialize(session);
		return session;
	}

	public void save(Session session) {
		save((Object) session);
	}

	public void save(Session session, org.hibernate.Session hibSession) {
		save((Object) session, hibSession);
	}

	public void saveOrUpdate(Session session) {
		saveOrUpdate((Object) session);
	}

	public void saveOrUpdate(Session session, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) session, hibSession);
	}


	public void update(Session session) {
		update((Object) session);
	}

	public void update(Session session, org.hibernate.Session hibSession) {
		update((Object) session, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Session session) {
		delete((Object) session);
	}

	public void delete(Session session, org.hibernate.Session hibSession) {
		delete((Object) session, hibSession);
	}

	public void refresh(Session session, org.hibernate.Session hibSession) {
		refresh((Object) session, hibSession);
	}

	public List<Session> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Session").list();
	}

	public List<Session> findByStatusType(org.hibernate.Session hibSession, Long statusTypeId) {
		return hibSession.createQuery("from Session x where x.statusType.uniqueId = :statusTypeId").setLong("statusTypeId", statusTypeId).list();
	}

	public List<Session> findByDefaultDatePattern(org.hibernate.Session hibSession, Long defaultDatePatternId) {
		return hibSession.createQuery("from Session x where x.defaultDatePattern.uniqueId = :defaultDatePatternId").setLong("defaultDatePatternId", defaultDatePatternId).list();
	}
}
