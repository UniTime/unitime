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

import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RoomTypeDAO;

public abstract class BaseRoomTypeDAO extends _RootDAO {

	private static RoomTypeDAO sInstance;

	public static RoomTypeDAO getInstance () {
		if (sInstance == null) sInstance = new RoomTypeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return RoomType.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public RoomType get(Long uniqueId) {
		return (RoomType) get(getReferenceClass(), uniqueId);
	}

	public RoomType get(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomType) get(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomType load(Long uniqueId) {
		return (RoomType) load(getReferenceClass(), uniqueId);
	}

	public RoomType load(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomType) load(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomType loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		RoomType roomType = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(roomType)) Hibernate.initialize(roomType);
		return roomType;
	}

	public void save(RoomType roomType) {
		save((Object) roomType);
	}

	public void save(RoomType roomType, org.hibernate.Session hibSession) {
		save((Object) roomType, hibSession);
	}

	public void saveOrUpdate(RoomType roomType) {
		saveOrUpdate((Object) roomType);
	}

	public void saveOrUpdate(RoomType roomType, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) roomType, hibSession);
	}


	public void update(RoomType roomType) {
		update((Object) roomType);
	}

	public void update(RoomType roomType, org.hibernate.Session hibSession) {
		update((Object) roomType, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(RoomType roomType) {
		delete((Object) roomType);
	}

	public void delete(RoomType roomType, org.hibernate.Session hibSession) {
		delete((Object) roomType, hibSession);
	}

	public void refresh(RoomType roomType, org.hibernate.Session hibSession) {
		refresh((Object) roomType, hibSession);
	}

	public List<RoomType> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from RoomType").list();
	}
}
