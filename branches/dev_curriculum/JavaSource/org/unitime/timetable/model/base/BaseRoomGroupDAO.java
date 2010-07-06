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

import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;

public abstract class BaseRoomGroupDAO extends _RootDAO {

	private static RoomGroupDAO sInstance;

	public static RoomGroupDAO getInstance () {
		if (sInstance == null) sInstance = new RoomGroupDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return RoomGroup.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public RoomGroup get(Long uniqueId) {
		return (RoomGroup) get(getReferenceClass(), uniqueId);
	}

	public RoomGroup get(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomGroup) get(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomGroup load(Long uniqueId) {
		return (RoomGroup) load(getReferenceClass(), uniqueId);
	}

	public RoomGroup load(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomGroup) load(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomGroup loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		RoomGroup roomGroup = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(roomGroup)) Hibernate.initialize(roomGroup);
		return roomGroup;
	}

	public void save(RoomGroup roomGroup) {
		save((Object) roomGroup);
	}

	public void save(RoomGroup roomGroup, org.hibernate.Session hibSession) {
		save((Object) roomGroup, hibSession);
	}

	public void saveOrUpdate(RoomGroup roomGroup) {
		saveOrUpdate((Object) roomGroup);
	}

	public void saveOrUpdate(RoomGroup roomGroup, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) roomGroup, hibSession);
	}


	public void update(RoomGroup roomGroup) {
		update((Object) roomGroup);
	}

	public void update(RoomGroup roomGroup, org.hibernate.Session hibSession) {
		update((Object) roomGroup, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(RoomGroup roomGroup) {
		delete((Object) roomGroup);
	}

	public void delete(RoomGroup roomGroup, org.hibernate.Session hibSession) {
		delete((Object) roomGroup, hibSession);
	}

	public void refresh(RoomGroup roomGroup, org.hibernate.Session hibSession) {
		refresh((Object) roomGroup, hibSession);
	}

	public List<RoomGroup> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from RoomGroup").list();
	}

	public List<RoomGroup> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from RoomGroup x where x.department.uniqueId = :departmentId").setLong("departmentId", departmentId).list();
	}

	public List<RoomGroup> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from RoomGroup x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
