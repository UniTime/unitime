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

import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RoomDeptDAO;

public abstract class BaseRoomDeptDAO extends _RootDAO {

	private static RoomDeptDAO sInstance;

	public static RoomDeptDAO getInstance () {
		if (sInstance == null) sInstance = new RoomDeptDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return RoomDept.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public RoomDept get(Long uniqueId) {
		return (RoomDept) get(getReferenceClass(), uniqueId);
	}

	public RoomDept get(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomDept) get(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomDept load(Long uniqueId) {
		return (RoomDept) load(getReferenceClass(), uniqueId);
	}

	public RoomDept load(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomDept) load(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomDept loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		RoomDept roomDept = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(roomDept)) Hibernate.initialize(roomDept);
		return roomDept;
	}

	public void save(RoomDept roomDept) {
		save((Object) roomDept);
	}

	public void save(RoomDept roomDept, org.hibernate.Session hibSession) {
		save((Object) roomDept, hibSession);
	}

	public void saveOrUpdate(RoomDept roomDept) {
		saveOrUpdate((Object) roomDept);
	}

	public void saveOrUpdate(RoomDept roomDept, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) roomDept, hibSession);
	}


	public void update(RoomDept roomDept) {
		update((Object) roomDept);
	}

	public void update(RoomDept roomDept, org.hibernate.Session hibSession) {
		update((Object) roomDept, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(RoomDept roomDept) {
		delete((Object) roomDept);
	}

	public void delete(RoomDept roomDept, org.hibernate.Session hibSession) {
		delete((Object) roomDept, hibSession);
	}

	public void refresh(RoomDept roomDept, org.hibernate.Session hibSession) {
		refresh((Object) roomDept, hibSession);
	}

	public List<RoomDept> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from RoomDept").list();
	}

	public List<RoomDept> findByRoom(org.hibernate.Session hibSession, Long roomId) {
		return hibSession.createQuery("from RoomDept x where x.room.uniqueId = :roomId").setLong("roomId", roomId).list();
	}

	public List<RoomDept> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from RoomDept x where x.department.uniqueId = :departmentId").setLong("departmentId", departmentId).list();
	}
}
