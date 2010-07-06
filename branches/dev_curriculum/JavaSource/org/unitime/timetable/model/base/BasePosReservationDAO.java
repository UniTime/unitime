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

import org.unitime.timetable.model.PosReservation;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PosReservationDAO;

public abstract class BasePosReservationDAO extends _RootDAO {

	private static PosReservationDAO sInstance;

	public static PosReservationDAO getInstance () {
		if (sInstance == null) sInstance = new PosReservationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return PosReservation.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public PosReservation get(Long uniqueId) {
		return (PosReservation) get(getReferenceClass(), uniqueId);
	}

	public PosReservation get(Long uniqueId, org.hibernate.Session hibSession) {
		return (PosReservation) get(getReferenceClass(), uniqueId, hibSession);
	}

	public PosReservation load(Long uniqueId) {
		return (PosReservation) load(getReferenceClass(), uniqueId);
	}

	public PosReservation load(Long uniqueId, org.hibernate.Session hibSession) {
		return (PosReservation) load(getReferenceClass(), uniqueId, hibSession);
	}

	public PosReservation loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		PosReservation posReservation = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(posReservation)) Hibernate.initialize(posReservation);
		return posReservation;
	}

	public void save(PosReservation posReservation) {
		save((Object) posReservation);
	}

	public void save(PosReservation posReservation, org.hibernate.Session hibSession) {
		save((Object) posReservation, hibSession);
	}

	public void saveOrUpdate(PosReservation posReservation) {
		saveOrUpdate((Object) posReservation);
	}

	public void saveOrUpdate(PosReservation posReservation, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) posReservation, hibSession);
	}


	public void update(PosReservation posReservation) {
		update((Object) posReservation);
	}

	public void update(PosReservation posReservation, org.hibernate.Session hibSession) {
		update((Object) posReservation, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(PosReservation posReservation) {
		delete((Object) posReservation);
	}

	public void delete(PosReservation posReservation, org.hibernate.Session hibSession) {
		delete((Object) posReservation, hibSession);
	}

	public void refresh(PosReservation posReservation, org.hibernate.Session hibSession) {
		refresh((Object) posReservation, hibSession);
	}

	public List<PosReservation> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from PosReservation").list();
	}

	public List<PosReservation> findByPosMajor(org.hibernate.Session hibSession, Long posMajorId) {
		return hibSession.createQuery("from PosReservation x where x.posMajor.uniqueId = :posMajorId").setLong("posMajorId", posMajorId).list();
	}
}
