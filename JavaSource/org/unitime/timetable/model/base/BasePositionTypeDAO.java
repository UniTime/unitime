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

import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PositionTypeDAO;

public abstract class BasePositionTypeDAO extends _RootDAO {

	private static PositionTypeDAO sInstance;

	public static PositionTypeDAO getInstance () {
		if (sInstance == null) sInstance = new PositionTypeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return PositionType.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public PositionType get(Long uniqueId) {
		return (PositionType) get(getReferenceClass(), uniqueId);
	}

	public PositionType get(Long uniqueId, org.hibernate.Session hibSession) {
		return (PositionType) get(getReferenceClass(), uniqueId, hibSession);
	}

	public PositionType load(Long uniqueId) {
		return (PositionType) load(getReferenceClass(), uniqueId);
	}

	public PositionType load(Long uniqueId, org.hibernate.Session hibSession) {
		return (PositionType) load(getReferenceClass(), uniqueId, hibSession);
	}

	public PositionType loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		PositionType positionType = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(positionType)) Hibernate.initialize(positionType);
		return positionType;
	}

	public void save(PositionType positionType) {
		save((Object) positionType);
	}

	public void save(PositionType positionType, org.hibernate.Session hibSession) {
		save((Object) positionType, hibSession);
	}

	public void saveOrUpdate(PositionType positionType) {
		saveOrUpdate((Object) positionType);
	}

	public void saveOrUpdate(PositionType positionType, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) positionType, hibSession);
	}


	public void update(PositionType positionType) {
		update((Object) positionType);
	}

	public void update(PositionType positionType, org.hibernate.Session hibSession) {
		update((Object) positionType, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(PositionType positionType) {
		delete((Object) positionType);
	}

	public void delete(PositionType positionType, org.hibernate.Session hibSession) {
		delete((Object) positionType, hibSession);
	}

	public void refresh(PositionType positionType, org.hibernate.Session hibSession) {
		refresh((Object) positionType, hibSession);
	}

	public List<PositionType> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from PositionType").list();
	}
}
