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

import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;

public abstract class BaseAcademicClassificationDAO extends _RootDAO {

	private static AcademicClassificationDAO sInstance;

	public static AcademicClassificationDAO getInstance () {
		if (sInstance == null) sInstance = new AcademicClassificationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return AcademicClassification.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public AcademicClassification get(Long uniqueId) {
		return (AcademicClassification) get(getReferenceClass(), uniqueId);
	}

	public AcademicClassification get(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcademicClassification) get(getReferenceClass(), uniqueId, hibSession);
	}

	public AcademicClassification load(Long uniqueId) {
		return (AcademicClassification) load(getReferenceClass(), uniqueId);
	}

	public AcademicClassification load(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcademicClassification) load(getReferenceClass(), uniqueId, hibSession);
	}

	public AcademicClassification loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		AcademicClassification academicClassification = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(academicClassification)) Hibernate.initialize(academicClassification);
		return academicClassification;
	}

	public void save(AcademicClassification academicClassification) {
		save((Object) academicClassification);
	}

	public void save(AcademicClassification academicClassification, org.hibernate.Session hibSession) {
		save((Object) academicClassification, hibSession);
	}

	public void saveOrUpdate(AcademicClassification academicClassification) {
		saveOrUpdate((Object) academicClassification);
	}

	public void saveOrUpdate(AcademicClassification academicClassification, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) academicClassification, hibSession);
	}


	public void update(AcademicClassification academicClassification) {
		update((Object) academicClassification);
	}

	public void update(AcademicClassification academicClassification, org.hibernate.Session hibSession) {
		update((Object) academicClassification, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(AcademicClassification academicClassification) {
		delete((Object) academicClassification);
	}

	public void delete(AcademicClassification academicClassification, org.hibernate.Session hibSession) {
		delete((Object) academicClassification, hibSession);
	}

	public void refresh(AcademicClassification academicClassification, org.hibernate.Session hibSession) {
		refresh((Object) academicClassification, hibSession);
	}

	public List<AcademicClassification> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from AcademicClassification").list();
	}

	public List<AcademicClassification> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from AcademicClassification x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
