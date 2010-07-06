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

import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AcademicAreaClassificationDAO;

public abstract class BaseAcademicAreaClassificationDAO extends _RootDAO {

	private static AcademicAreaClassificationDAO sInstance;

	public static AcademicAreaClassificationDAO getInstance () {
		if (sInstance == null) sInstance = new AcademicAreaClassificationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return AcademicAreaClassification.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public AcademicAreaClassification get(Long uniqueId) {
		return (AcademicAreaClassification) get(getReferenceClass(), uniqueId);
	}

	public AcademicAreaClassification get(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcademicAreaClassification) get(getReferenceClass(), uniqueId, hibSession);
	}

	public AcademicAreaClassification load(Long uniqueId) {
		return (AcademicAreaClassification) load(getReferenceClass(), uniqueId);
	}

	public AcademicAreaClassification load(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcademicAreaClassification) load(getReferenceClass(), uniqueId, hibSession);
	}

	public AcademicAreaClassification loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		AcademicAreaClassification academicAreaClassification = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(academicAreaClassification)) Hibernate.initialize(academicAreaClassification);
		return academicAreaClassification;
	}

	public void save(AcademicAreaClassification academicAreaClassification) {
		save((Object) academicAreaClassification);
	}

	public void save(AcademicAreaClassification academicAreaClassification, org.hibernate.Session hibSession) {
		save((Object) academicAreaClassification, hibSession);
	}

	public void saveOrUpdate(AcademicAreaClassification academicAreaClassification) {
		saveOrUpdate((Object) academicAreaClassification);
	}

	public void saveOrUpdate(AcademicAreaClassification academicAreaClassification, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) academicAreaClassification, hibSession);
	}


	public void update(AcademicAreaClassification academicAreaClassification) {
		update((Object) academicAreaClassification);
	}

	public void update(AcademicAreaClassification academicAreaClassification, org.hibernate.Session hibSession) {
		update((Object) academicAreaClassification, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(AcademicAreaClassification academicAreaClassification) {
		delete((Object) academicAreaClassification);
	}

	public void delete(AcademicAreaClassification academicAreaClassification, org.hibernate.Session hibSession) {
		delete((Object) academicAreaClassification, hibSession);
	}

	public void refresh(AcademicAreaClassification academicAreaClassification, org.hibernate.Session hibSession) {
		refresh((Object) academicAreaClassification, hibSession);
	}

	public List<AcademicAreaClassification> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from AcademicAreaClassification").list();
	}

	public List<AcademicAreaClassification> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from AcademicAreaClassification x where x.student.uniqueId = :studentId").setLong("studentId", studentId).list();
	}

	public List<AcademicAreaClassification> findByAcademicClassification(org.hibernate.Session hibSession, Long academicClassificationId) {
		return hibSession.createQuery("from AcademicAreaClassification x where x.academicClassification.uniqueId = :academicClassificationId").setLong("academicClassificationId", academicClassificationId).list();
	}

	public List<AcademicAreaClassification> findByAcademicArea(org.hibernate.Session hibSession, Long academicAreaId) {
		return hibSession.createQuery("from AcademicAreaClassification x where x.academicArea.uniqueId = :academicAreaId").setLong("academicAreaId", academicAreaId).list();
	}
}
