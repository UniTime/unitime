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

import org.unitime.timetable.model.AcadAreaPosReservation;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AcadAreaPosReservationDAO;

public abstract class BaseAcadAreaPosReservationDAO extends _RootDAO {

	private static AcadAreaPosReservationDAO sInstance;

	public static AcadAreaPosReservationDAO getInstance () {
		if (sInstance == null) sInstance = new AcadAreaPosReservationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return AcadAreaPosReservation.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public AcadAreaPosReservation get(Long uniqueId) {
		return (AcadAreaPosReservation) get(getReferenceClass(), uniqueId);
	}

	public AcadAreaPosReservation get(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcadAreaPosReservation) get(getReferenceClass(), uniqueId, hibSession);
	}

	public AcadAreaPosReservation load(Long uniqueId) {
		return (AcadAreaPosReservation) load(getReferenceClass(), uniqueId);
	}

	public AcadAreaPosReservation load(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcadAreaPosReservation) load(getReferenceClass(), uniqueId, hibSession);
	}

	public AcadAreaPosReservation loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		AcadAreaPosReservation acadAreaPosReservation = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(acadAreaPosReservation)) Hibernate.initialize(acadAreaPosReservation);
		return acadAreaPosReservation;
	}

	public void save(AcadAreaPosReservation acadAreaPosReservation) {
		save((Object) acadAreaPosReservation);
	}

	public void save(AcadAreaPosReservation acadAreaPosReservation, org.hibernate.Session hibSession) {
		save((Object) acadAreaPosReservation, hibSession);
	}

	public void saveOrUpdate(AcadAreaPosReservation acadAreaPosReservation) {
		saveOrUpdate((Object) acadAreaPosReservation);
	}

	public void saveOrUpdate(AcadAreaPosReservation acadAreaPosReservation, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) acadAreaPosReservation, hibSession);
	}


	public void update(AcadAreaPosReservation acadAreaPosReservation) {
		update((Object) acadAreaPosReservation);
	}

	public void update(AcadAreaPosReservation acadAreaPosReservation, org.hibernate.Session hibSession) {
		update((Object) acadAreaPosReservation, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(AcadAreaPosReservation acadAreaPosReservation) {
		delete((Object) acadAreaPosReservation);
	}

	public void delete(AcadAreaPosReservation acadAreaPosReservation, org.hibernate.Session hibSession) {
		delete((Object) acadAreaPosReservation, hibSession);
	}

	public void refresh(AcadAreaPosReservation acadAreaPosReservation, org.hibernate.Session hibSession) {
		refresh((Object) acadAreaPosReservation, hibSession);
	}

	public List<AcadAreaPosReservation> findByAcademicClassification(org.hibernate.Session hibSession, Long academicClassificationId) {
		return hibSession.createQuery("from AcadAreaPosReservation x where x.academicClassification.uniqueId = :academicClassificationId").setLong("academicClassificationId", academicClassificationId).list();
	}
}
