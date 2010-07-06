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

import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExternalRoomDAO;

public abstract class BaseExternalRoomDAO extends _RootDAO {

	private static ExternalRoomDAO sInstance;

	public static ExternalRoomDAO getInstance () {
		if (sInstance == null) sInstance = new ExternalRoomDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ExternalRoom.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ExternalRoom get(Long uniqueId) {
		return (ExternalRoom) get(getReferenceClass(), uniqueId);
	}

	public ExternalRoom get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExternalRoom) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ExternalRoom load(Long uniqueId) {
		return (ExternalRoom) load(getReferenceClass(), uniqueId);
	}

	public ExternalRoom load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExternalRoom) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ExternalRoom loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ExternalRoom externalRoom = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(externalRoom)) Hibernate.initialize(externalRoom);
		return externalRoom;
	}

	public void save(ExternalRoom externalRoom) {
		save((Object) externalRoom);
	}

	public void save(ExternalRoom externalRoom, org.hibernate.Session hibSession) {
		save((Object) externalRoom, hibSession);
	}

	public void saveOrUpdate(ExternalRoom externalRoom) {
		saveOrUpdate((Object) externalRoom);
	}

	public void saveOrUpdate(ExternalRoom externalRoom, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) externalRoom, hibSession);
	}


	public void update(ExternalRoom externalRoom) {
		update((Object) externalRoom);
	}

	public void update(ExternalRoom externalRoom, org.hibernate.Session hibSession) {
		update((Object) externalRoom, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ExternalRoom externalRoom) {
		delete((Object) externalRoom);
	}

	public void delete(ExternalRoom externalRoom, org.hibernate.Session hibSession) {
		delete((Object) externalRoom, hibSession);
	}

	public void refresh(ExternalRoom externalRoom, org.hibernate.Session hibSession) {
		refresh((Object) externalRoom, hibSession);
	}

	public List<ExternalRoom> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ExternalRoom").list();
	}

	public List<ExternalRoom> findByRoomType(org.hibernate.Session hibSession, Long roomTypeId) {
		return hibSession.createQuery("from ExternalRoom x where x.roomType.uniqueId = :roomTypeId").setLong("roomTypeId", roomTypeId).list();
	}

	public List<ExternalRoom> findByBuilding(org.hibernate.Session hibSession, Long buildingId) {
		return hibSession.createQuery("from ExternalRoom x where x.building.uniqueId = :buildingId").setLong("buildingId", buildingId).list();
	}
}
