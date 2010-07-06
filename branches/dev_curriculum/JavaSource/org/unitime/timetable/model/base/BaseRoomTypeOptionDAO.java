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

import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RoomTypeOptionDAO;

public abstract class BaseRoomTypeOptionDAO extends _RootDAO {

	private static RoomTypeOptionDAO sInstance;

	public static RoomTypeOptionDAO getInstance () {
		if (sInstance == null) sInstance = new RoomTypeOptionDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return RoomTypeOption.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public RoomTypeOption get(RoomTypeOption key) {
		return (RoomTypeOption) get(getReferenceClass(), key);
	}

	public RoomTypeOption get(RoomTypeOption key, org.hibernate.Session hibSession) {
		return (RoomTypeOption) get(getReferenceClass(), key, hibSession);
	}

	public RoomTypeOption load(RoomTypeOption key) {
		return (RoomTypeOption) load(getReferenceClass(), key);
	}

	public RoomTypeOption load(RoomTypeOption key, org.hibernate.Session hibSession) {
		return (RoomTypeOption) load(getReferenceClass(), key, hibSession);
	}

	public RoomTypeOption loadInitialize(RoomTypeOption key, org.hibernate.Session hibSession) {
		RoomTypeOption roomTypeOption = load(key, hibSession);
		if (!Hibernate.isInitialized(roomTypeOption)) Hibernate.initialize(roomTypeOption);
		return roomTypeOption;
	}

	public void save(RoomTypeOption roomTypeOption) {
		save((Object) roomTypeOption);
	}

	public void save(RoomTypeOption roomTypeOption, org.hibernate.Session hibSession) {
		save((Object) roomTypeOption, hibSession);
	}

	public void saveOrUpdate(RoomTypeOption roomTypeOption) {
		saveOrUpdate((Object) roomTypeOption);
	}

	public void saveOrUpdate(RoomTypeOption roomTypeOption, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) roomTypeOption, hibSession);
	}


	public void update(RoomTypeOption roomTypeOption) {
		update((Object) roomTypeOption);
	}

	public void update(RoomTypeOption roomTypeOption, org.hibernate.Session hibSession) {
		update((Object) roomTypeOption, hibSession);
	}


	public void delete(RoomTypeOption roomTypeOption) {
		delete((Object) roomTypeOption);
	}

	public void delete(RoomTypeOption roomTypeOption, org.hibernate.Session hibSession) {
		delete((Object) roomTypeOption, hibSession);
	}

	public void refresh(RoomTypeOption roomTypeOption, org.hibernate.Session hibSession) {
		refresh((Object) roomTypeOption, hibSession);
	}

	public List<RoomTypeOption> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from RoomTypeOption").list();
	}
}
