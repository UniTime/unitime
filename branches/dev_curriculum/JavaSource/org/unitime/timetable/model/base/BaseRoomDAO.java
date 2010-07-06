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

import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RoomDAO;

public abstract class BaseRoomDAO extends _RootDAO {

	private static RoomDAO sInstance;

	public static RoomDAO getInstance () {
		if (sInstance == null) sInstance = new RoomDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Room.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Room get(Long uniqueId) {
		return (Room) get(getReferenceClass(), uniqueId);
	}

	public Room get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Room) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Room load(Long uniqueId) {
		return (Room) load(getReferenceClass(), uniqueId);
	}

	public Room load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Room) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Room loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Room room = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(room)) Hibernate.initialize(room);
		return room;
	}

	public void save(Room room) {
		save((Object) room);
	}

	public void save(Room room, org.hibernate.Session hibSession) {
		save((Object) room, hibSession);
	}

	public void saveOrUpdate(Room room) {
		saveOrUpdate((Object) room);
	}

	public void saveOrUpdate(Room room, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) room, hibSession);
	}


	public void update(Room room) {
		update((Object) room);
	}

	public void update(Room room, org.hibernate.Session hibSession) {
		update((Object) room, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Room room) {
		delete((Object) room);
	}

	public void delete(Room room, org.hibernate.Session hibSession) {
		delete((Object) room, hibSession);
	}

	public void refresh(Room room, org.hibernate.Session hibSession) {
		refresh((Object) room, hibSession);
	}

	public List<Room> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Room").list();
	}

	public List<Room> findByRoomType(org.hibernate.Session hibSession, Long roomTypeId) {
		return hibSession.createQuery("from Room x where x.roomType.uniqueId = :roomTypeId").setLong("roomTypeId", roomTypeId).list();
	}

	public List<Room> findByBuilding(org.hibernate.Session hibSession, Long buildingId) {
		return hibSession.createQuery("from Room x where x.building.uniqueId = :buildingId").setLong("buildingId", buildingId).list();
	}
}
