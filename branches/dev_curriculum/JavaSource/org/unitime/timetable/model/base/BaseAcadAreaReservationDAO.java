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

import org.unitime.timetable.model.AcadAreaReservation;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AcadAreaReservationDAO;

public abstract class BaseAcadAreaReservationDAO extends _RootDAO {

	private static AcadAreaReservationDAO sInstance;

	public static AcadAreaReservationDAO getInstance () {
		if (sInstance == null) sInstance = new AcadAreaReservationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return AcadAreaReservation.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public AcadAreaReservation get(Long uniqueId) {
		return (AcadAreaReservation) get(getReferenceClass(), uniqueId);
	}

	public AcadAreaReservation get(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcadAreaReservation) get(getReferenceClass(), uniqueId, hibSession);
	}

	public AcadAreaReservation load(Long uniqueId) {
		return (AcadAreaReservation) load(getReferenceClass(), uniqueId);
	}

	public AcadAreaReservation load(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcadAreaReservation) load(getReferenceClass(), uniqueId, hibSession);
	}

	public AcadAreaReservation loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		AcadAreaReservation acadAreaReservation = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(acadAreaReservation)) Hibernate.initialize(acadAreaReservation);
		return acadAreaReservation;
	}

	public void save(AcadAreaReservation acadAreaReservation) {
		save((Object) acadAreaReservation);
	}

	public void save(AcadAreaReservation acadAreaReservation, org.hibernate.Session hibSession) {
		save((Object) acadAreaReservation, hibSession);
	}

	public void saveOrUpdate(AcadAreaReservation acadAreaReservation) {
		saveOrUpdate((Object) acadAreaReservation);
	}

	public void saveOrUpdate(AcadAreaReservation acadAreaReservation, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) acadAreaReservation, hibSession);
	}


	public void update(AcadAreaReservation acadAreaReservation) {
		update((Object) acadAreaReservation);
	}

	public void update(AcadAreaReservation acadAreaReservation, org.hibernate.Session hibSession) {
		update((Object) acadAreaReservation, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(AcadAreaReservation acadAreaReservation) {
		delete((Object) acadAreaReservation);
	}

	public void delete(AcadAreaReservation acadAreaReservation, org.hibernate.Session hibSession) {
		delete((Object) acadAreaReservation, hibSession);
	}

	public void refresh(AcadAreaReservation acadAreaReservation, org.hibernate.Session hibSession) {
		refresh((Object) acadAreaReservation, hibSession);
	}

	public List<AcadAreaReservation> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from AcadAreaReservation").list();
	}

	public List<AcadAreaReservation> findByAcademicArea(org.hibernate.Session hibSession, Long academicAreaId) {
		return hibSession.createQuery("from AcadAreaReservation x where x.academicArea.uniqueId = :academicAreaId").setLong("academicAreaId", academicAreaId).list();
	}
}
