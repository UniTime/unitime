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

import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.IndividualReservationDAO;

public abstract class BaseIndividualReservationDAO extends _RootDAO {

	private static IndividualReservationDAO sInstance;

	public static IndividualReservationDAO getInstance () {
		if (sInstance == null) sInstance = new IndividualReservationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return IndividualReservation.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public IndividualReservation get(Long uniqueId) {
		return (IndividualReservation) get(getReferenceClass(), uniqueId);
	}

	public IndividualReservation get(Long uniqueId, org.hibernate.Session hibSession) {
		return (IndividualReservation) get(getReferenceClass(), uniqueId, hibSession);
	}

	public IndividualReservation load(Long uniqueId) {
		return (IndividualReservation) load(getReferenceClass(), uniqueId);
	}

	public IndividualReservation load(Long uniqueId, org.hibernate.Session hibSession) {
		return (IndividualReservation) load(getReferenceClass(), uniqueId, hibSession);
	}

	public IndividualReservation loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		IndividualReservation individualReservation = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(individualReservation)) Hibernate.initialize(individualReservation);
		return individualReservation;
	}

	public void save(IndividualReservation individualReservation) {
		save((Object) individualReservation);
	}

	public void save(IndividualReservation individualReservation, org.hibernate.Session hibSession) {
		save((Object) individualReservation, hibSession);
	}

	public void saveOrUpdate(IndividualReservation individualReservation) {
		saveOrUpdate((Object) individualReservation);
	}

	public void saveOrUpdate(IndividualReservation individualReservation, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) individualReservation, hibSession);
	}


	public void update(IndividualReservation individualReservation) {
		update((Object) individualReservation);
	}

	public void update(IndividualReservation individualReservation, org.hibernate.Session hibSession) {
		update((Object) individualReservation, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(IndividualReservation individualReservation) {
		delete((Object) individualReservation);
	}

	public void delete(IndividualReservation individualReservation, org.hibernate.Session hibSession) {
		delete((Object) individualReservation, hibSession);
	}

	public void refresh(IndividualReservation individualReservation, org.hibernate.Session hibSession) {
		refresh((Object) individualReservation, hibSession);
	}

	public List<IndividualReservation> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from IndividualReservation").list();
	}
}
