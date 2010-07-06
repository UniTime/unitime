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

import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExternalRoomDepartmentDAO;

public abstract class BaseExternalRoomDepartmentDAO extends _RootDAO {

	private static ExternalRoomDepartmentDAO sInstance;

	public static ExternalRoomDepartmentDAO getInstance () {
		if (sInstance == null) sInstance = new ExternalRoomDepartmentDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ExternalRoomDepartment.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ExternalRoomDepartment get(Long uniqueId) {
		return (ExternalRoomDepartment) get(getReferenceClass(), uniqueId);
	}

	public ExternalRoomDepartment get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExternalRoomDepartment) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ExternalRoomDepartment load(Long uniqueId) {
		return (ExternalRoomDepartment) load(getReferenceClass(), uniqueId);
	}

	public ExternalRoomDepartment load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExternalRoomDepartment) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ExternalRoomDepartment loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ExternalRoomDepartment externalRoomDepartment = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(externalRoomDepartment)) Hibernate.initialize(externalRoomDepartment);
		return externalRoomDepartment;
	}

	public void save(ExternalRoomDepartment externalRoomDepartment) {
		save((Object) externalRoomDepartment);
	}

	public void save(ExternalRoomDepartment externalRoomDepartment, org.hibernate.Session hibSession) {
		save((Object) externalRoomDepartment, hibSession);
	}

	public void saveOrUpdate(ExternalRoomDepartment externalRoomDepartment) {
		saveOrUpdate((Object) externalRoomDepartment);
	}

	public void saveOrUpdate(ExternalRoomDepartment externalRoomDepartment, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) externalRoomDepartment, hibSession);
	}


	public void update(ExternalRoomDepartment externalRoomDepartment) {
		update((Object) externalRoomDepartment);
	}

	public void update(ExternalRoomDepartment externalRoomDepartment, org.hibernate.Session hibSession) {
		update((Object) externalRoomDepartment, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ExternalRoomDepartment externalRoomDepartment) {
		delete((Object) externalRoomDepartment);
	}

	public void delete(ExternalRoomDepartment externalRoomDepartment, org.hibernate.Session hibSession) {
		delete((Object) externalRoomDepartment, hibSession);
	}

	public void refresh(ExternalRoomDepartment externalRoomDepartment, org.hibernate.Session hibSession) {
		refresh((Object) externalRoomDepartment, hibSession);
	}

	public List<ExternalRoomDepartment> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ExternalRoomDepartment").list();
	}

	public List<ExternalRoomDepartment> findByRoom(org.hibernate.Session hibSession, Long roomId) {
		return hibSession.createQuery("from ExternalRoomDepartment x where x.room.uniqueId = :roomId").setLong("roomId", roomId).list();
	}
}
