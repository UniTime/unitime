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

import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ReservationDAO;

public abstract class BaseReservationDAO extends _RootDAO {

	private static ReservationDAO sInstance;

	public static ReservationDAO getInstance () {
		if (sInstance == null) sInstance = new ReservationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Reservation.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Reservation get(Long uniqueId) {
		return (Reservation) get(getReferenceClass(), uniqueId);
	}

	public Reservation get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Reservation) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Reservation load(Long uniqueId) {
		return (Reservation) load(getReferenceClass(), uniqueId);
	}

	public Reservation load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Reservation) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Reservation loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Reservation reservation = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(reservation)) Hibernate.initialize(reservation);
		return reservation;
	}

	public void save(Reservation reservation) {
		save((Object) reservation);
	}

	public void save(Reservation reservation, org.hibernate.Session hibSession) {
		save((Object) reservation, hibSession);
	}

	public void saveOrUpdate(Reservation reservation) {
		saveOrUpdate((Object) reservation);
	}

	public void saveOrUpdate(Reservation reservation, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) reservation, hibSession);
	}


	public void update(Reservation reservation) {
		update((Object) reservation);
	}

	public void update(Reservation reservation, org.hibernate.Session hibSession) {
		update((Object) reservation, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Reservation reservation) {
		delete((Object) reservation);
	}

	public void delete(Reservation reservation, org.hibernate.Session hibSession) {
		delete((Object) reservation, hibSession);
	}

	public void refresh(Reservation reservation, org.hibernate.Session hibSession) {
		refresh((Object) reservation, hibSession);
	}

	public List<Reservation> findByReservationType(org.hibernate.Session hibSession, Long reservationTypeId) {
		return hibSession.createQuery("from Reservation x where x.reservationType.uniqueId = :reservationTypeId").setLong("reservationTypeId", reservationTypeId).list();
	}
}
