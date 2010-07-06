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

import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ChangeLogDAO;

public abstract class BaseChangeLogDAO extends _RootDAO {

	private static ChangeLogDAO sInstance;

	public static ChangeLogDAO getInstance () {
		if (sInstance == null) sInstance = new ChangeLogDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ChangeLog.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ChangeLog get(Long uniqueId) {
		return (ChangeLog) get(getReferenceClass(), uniqueId);
	}

	public ChangeLog get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ChangeLog) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ChangeLog load(Long uniqueId) {
		return (ChangeLog) load(getReferenceClass(), uniqueId);
	}

	public ChangeLog load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ChangeLog) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ChangeLog loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ChangeLog changeLog = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(changeLog)) Hibernate.initialize(changeLog);
		return changeLog;
	}

	public void save(ChangeLog changeLog) {
		save((Object) changeLog);
	}

	public void save(ChangeLog changeLog, org.hibernate.Session hibSession) {
		save((Object) changeLog, hibSession);
	}

	public void saveOrUpdate(ChangeLog changeLog) {
		saveOrUpdate((Object) changeLog);
	}

	public void saveOrUpdate(ChangeLog changeLog, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) changeLog, hibSession);
	}


	public void update(ChangeLog changeLog) {
		update((Object) changeLog);
	}

	public void update(ChangeLog changeLog, org.hibernate.Session hibSession) {
		update((Object) changeLog, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ChangeLog changeLog) {
		delete((Object) changeLog);
	}

	public void delete(ChangeLog changeLog, org.hibernate.Session hibSession) {
		delete((Object) changeLog, hibSession);
	}

	public void refresh(ChangeLog changeLog, org.hibernate.Session hibSession) {
		refresh((Object) changeLog, hibSession);
	}

	public List<ChangeLog> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ChangeLog").list();
	}

	public List<ChangeLog> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from ChangeLog x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	public List<ChangeLog> findByManager(org.hibernate.Session hibSession, Long managerId) {
		return hibSession.createQuery("from ChangeLog x where x.manager.uniqueId = :managerId").setLong("managerId", managerId).list();
	}

	public List<ChangeLog> findBySubjectArea(org.hibernate.Session hibSession, Long subjectAreaId) {
		return hibSession.createQuery("from ChangeLog x where x.subjectArea.uniqueId = :subjectAreaId").setLong("subjectAreaId", subjectAreaId).list();
	}

	public List<ChangeLog> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from ChangeLog x where x.department.uniqueId = :departmentId").setLong("departmentId", departmentId).list();
	}
}
