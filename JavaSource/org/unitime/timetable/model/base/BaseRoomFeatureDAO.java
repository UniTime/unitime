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

import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;

public abstract class BaseRoomFeatureDAO extends _RootDAO {

	private static RoomFeatureDAO sInstance;

	public static RoomFeatureDAO getInstance () {
		if (sInstance == null) sInstance = new RoomFeatureDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return RoomFeature.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public RoomFeature get(Long uniqueId) {
		return (RoomFeature) get(getReferenceClass(), uniqueId);
	}

	public RoomFeature get(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomFeature) get(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomFeature load(Long uniqueId) {
		return (RoomFeature) load(getReferenceClass(), uniqueId);
	}

	public RoomFeature load(Long uniqueId, org.hibernate.Session hibSession) {
		return (RoomFeature) load(getReferenceClass(), uniqueId, hibSession);
	}

	public RoomFeature loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		RoomFeature roomFeature = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(roomFeature)) Hibernate.initialize(roomFeature);
		return roomFeature;
	}

	public void save(RoomFeature roomFeature) {
		save((Object) roomFeature);
	}

	public void save(RoomFeature roomFeature, org.hibernate.Session hibSession) {
		save((Object) roomFeature, hibSession);
	}

	public void saveOrUpdate(RoomFeature roomFeature) {
		saveOrUpdate((Object) roomFeature);
	}

	public void saveOrUpdate(RoomFeature roomFeature, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) roomFeature, hibSession);
	}


	public void update(RoomFeature roomFeature) {
		update((Object) roomFeature);
	}

	public void update(RoomFeature roomFeature, org.hibernate.Session hibSession) {
		update((Object) roomFeature, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(RoomFeature roomFeature) {
		delete((Object) roomFeature);
	}

	public void delete(RoomFeature roomFeature, org.hibernate.Session hibSession) {
		delete((Object) roomFeature, hibSession);
	}

	public void refresh(RoomFeature roomFeature, org.hibernate.Session hibSession) {
		refresh((Object) roomFeature, hibSession);
	}

	public List<RoomFeature> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from RoomFeature").list();
	}
}
