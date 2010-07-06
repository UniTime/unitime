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

import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RoomGroupPrefDAO;

public abstract class BaseRoomGroupPrefDAO extends _RootDAO {

	private static RoomGroupPrefDAO sInstance;

	public static RoomGroupPrefDAO getInstance () {
		if (sInstance == null) sInstance = new RoomGroupPrefDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return RoomGroupPref.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public RoomGroupPref get(Long uniqueId) {
		return (RoomGroupPref) get(getReferenceClass(), uniqueId);
	}

	public RoomGroupPref get(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomGroupPref) get(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomGroupPref load(Long uniqueId) {
		return (RoomGroupPref) load(getReferenceClass(), uniqueId);
	}

	public RoomGroupPref load(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomGroupPref) load(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomGroupPref loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		RoomGroupPref roomGroupPref = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(roomGroupPref)) Hibernate.initialize(roomGroupPref);
		return roomGroupPref;
	}

	public void save(RoomGroupPref roomGroupPref) {
		save((Object) roomGroupPref);
	}

	public void save(RoomGroupPref roomGroupPref, org.hibernate.Session hibSession) {
		save((Object) roomGroupPref, hibSession);
	}

	public void saveOrUpdate(RoomGroupPref roomGroupPref) {
		saveOrUpdate((Object) roomGroupPref);
	}

	public void saveOrUpdate(RoomGroupPref roomGroupPref, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) roomGroupPref, hibSession);
	}


	public void update(RoomGroupPref roomGroupPref) {
		update((Object) roomGroupPref);
	}

	public void update(RoomGroupPref roomGroupPref, org.hibernate.Session hibSession) {
		update((Object) roomGroupPref, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(RoomGroupPref roomGroupPref) {
		delete((Object) roomGroupPref);
	}

	public void delete(RoomGroupPref roomGroupPref, org.hibernate.Session hibSession) {
		delete((Object) roomGroupPref, hibSession);
	}

	public void refresh(RoomGroupPref roomGroupPref, org.hibernate.Session hibSession) {
		refresh((Object) roomGroupPref, hibSession);
	}

	public List<RoomGroupPref> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from RoomGroupPref").list();
	}

	public List<RoomGroupPref> findByRoomGroup(org.hibernate.Session hibSession, Long roomGroupId) {
		return hibSession.createQuery("from RoomGroupPref x where x.roomGroup.uniqueId = :roomGroupId").setLong("roomGroupId", roomGroupId).list();
	}
}
