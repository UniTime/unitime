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

import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RoomPrefDAO;

public abstract class BaseRoomPrefDAO extends _RootDAO {

	private static RoomPrefDAO sInstance;

	public static RoomPrefDAO getInstance () {
		if (sInstance == null) sInstance = new RoomPrefDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return RoomPref.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public RoomPref get(Long uniqueId) {
		return (RoomPref) get(getReferenceClass(), uniqueId);
	}

	public RoomPref get(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomPref) get(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomPref load(Long uniqueId) {
		return (RoomPref) load(getReferenceClass(), uniqueId);
	}

	public RoomPref load(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomPref) load(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomPref loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		RoomPref roomPref = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(roomPref)) Hibernate.initialize(roomPref);
		return roomPref;
	}

	public void save(RoomPref roomPref) {
		save((Object) roomPref);
	}

	public void save(RoomPref roomPref, org.hibernate.Session hibSession) {
		save((Object) roomPref, hibSession);
	}

	public void saveOrUpdate(RoomPref roomPref) {
		saveOrUpdate((Object) roomPref);
	}

	public void saveOrUpdate(RoomPref roomPref, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) roomPref, hibSession);
	}


	public void update(RoomPref roomPref) {
		update((Object) roomPref);
	}

	public void update(RoomPref roomPref, org.hibernate.Session hibSession) {
		update((Object) roomPref, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(RoomPref roomPref) {
		delete((Object) roomPref);
	}

	public void delete(RoomPref roomPref, org.hibernate.Session hibSession) {
		delete((Object) roomPref, hibSession);
	}

	public void refresh(RoomPref roomPref, org.hibernate.Session hibSession) {
		refresh((Object) roomPref, hibSession);
	}

	public List<RoomPref> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from RoomPref").list();
	}

	public List<RoomPref> findByRoom(org.hibernate.Session hibSession, Long roomId) {
		return hibSession.createQuery("from RoomPref x where x.room.uniqueId = :roomId").setLong("roomId", roomId).list();
	}
}
