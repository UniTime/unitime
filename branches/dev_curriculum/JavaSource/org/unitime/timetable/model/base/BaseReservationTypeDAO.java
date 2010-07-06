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

import org.unitime.timetable.model.ReservationType;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ReservationTypeDAO;

public abstract class BaseReservationTypeDAO extends _RootDAO {

	private static ReservationTypeDAO sInstance;

	public static ReservationTypeDAO getInstance () {
		if (sInstance == null) sInstance = new ReservationTypeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ReservationType.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ReservationType get(Long uniqueId) {
		return (ReservationType) get(getReferenceClass(), uniqueId);
	}

	public ReservationType get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ReservationType) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ReservationType load(Long uniqueId) {
		return (ReservationType) load(getReferenceClass(), uniqueId);
	}

	public ReservationType load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ReservationType) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ReservationType loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ReservationType reservationType = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(reservationType)) Hibernate.initialize(reservationType);
		return reservationType;
	}

	public void save(ReservationType reservationType) {
		save((Object) reservationType);
	}

	public void save(ReservationType reservationType, org.hibernate.Session hibSession) {
		save((Object) reservationType, hibSession);
	}

	public void saveOrUpdate(ReservationType reservationType) {
		saveOrUpdate((Object) reservationType);
	}

	public void saveOrUpdate(ReservationType reservationType, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) reservationType, hibSession);
	}


	public void update(ReservationType reservationType) {
		update((Object) reservationType);
	}

	public void update(ReservationType reservationType, org.hibernate.Session hibSession) {
		update((Object) reservationType, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ReservationType reservationType) {
		delete((Object) reservationType);
	}

	public void delete(ReservationType reservationType, org.hibernate.Session hibSession) {
		delete((Object) reservationType, hibSession);
	}

	public void refresh(ReservationType reservationType, org.hibernate.Session hibSession) {
		refresh((Object) reservationType, hibSession);
	}

	public List<ReservationType> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ReservationType").list();
	}
}
