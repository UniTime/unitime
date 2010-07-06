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

import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RoomFeaturePrefDAO;

public abstract class BaseRoomFeaturePrefDAO extends _RootDAO {

	private static RoomFeaturePrefDAO sInstance;

	public static RoomFeaturePrefDAO getInstance () {
		if (sInstance == null) sInstance = new RoomFeaturePrefDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return RoomFeaturePref.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public RoomFeaturePref get(Long uniqueId) {
		return (RoomFeaturePref) get(getReferenceClass(), uniqueId);
	}

	public RoomFeaturePref get(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomFeaturePref) get(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomFeaturePref load(Long uniqueId) {
		return (RoomFeaturePref) load(getReferenceClass(), uniqueId);
	}

	public RoomFeaturePref load(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomFeaturePref) load(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomFeaturePref loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		RoomFeaturePref roomFeaturePref = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(roomFeaturePref)) Hibernate.initialize(roomFeaturePref);
		return roomFeaturePref;
	}

	public void save(RoomFeaturePref roomFeaturePref) {
		save((Object) roomFeaturePref);
	}

	public void save(RoomFeaturePref roomFeaturePref, org.hibernate.Session hibSession) {
		save((Object) roomFeaturePref, hibSession);
	}

	public void saveOrUpdate(RoomFeaturePref roomFeaturePref) {
		saveOrUpdate((Object) roomFeaturePref);
	}

	public void saveOrUpdate(RoomFeaturePref roomFeaturePref, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) roomFeaturePref, hibSession);
	}


	public void update(RoomFeaturePref roomFeaturePref) {
		update((Object) roomFeaturePref);
	}

	public void update(RoomFeaturePref roomFeaturePref, org.hibernate.Session hibSession) {
		update((Object) roomFeaturePref, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(RoomFeaturePref roomFeaturePref) {
		delete((Object) roomFeaturePref);
	}

	public void delete(RoomFeaturePref roomFeaturePref, org.hibernate.Session hibSession) {
		delete((Object) roomFeaturePref, hibSession);
	}

	public void refresh(RoomFeaturePref roomFeaturePref, org.hibernate.Session hibSession) {
		refresh((Object) roomFeaturePref, hibSession);
	}

	public List<RoomFeaturePref> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from RoomFeaturePref").list();
	}

	public List<RoomFeaturePref> findByRoomFeature(org.hibernate.Session hibSession, Long roomFeatureId) {
		return hibSession.createQuery("from RoomFeaturePref x where x.roomFeature.uniqueId = :roomFeatureId").setLong("roomFeatureId", roomFeatureId).list();
	}
}
