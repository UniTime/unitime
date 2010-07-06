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

import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AcademicAreaDAO;

public abstract class BaseAcademicAreaDAO extends _RootDAO {

	private static AcademicAreaDAO sInstance;

	public static AcademicAreaDAO getInstance () {
		if (sInstance == null) sInstance = new AcademicAreaDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return AcademicArea.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public AcademicArea get(Long uniqueId) {
		return (AcademicArea) get(getReferenceClass(), uniqueId);
	}

	public AcademicArea get(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcademicArea) get(getReferenceClass(), uniqueId, hibSession);
	}

	public AcademicArea load(Long uniqueId) {
		return (AcademicArea) load(getReferenceClass(), uniqueId);
	}

	public AcademicArea load(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcademicArea) load(getReferenceClass(), uniqueId, hibSession);
	}

	public AcademicArea loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		AcademicArea academicArea = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(academicArea)) Hibernate.initialize(academicArea);
		return academicArea;
	}

	public void save(AcademicArea academicArea) {
		save((Object) academicArea);
	}

	public void save(AcademicArea academicArea, org.hibernate.Session hibSession) {
		save((Object) academicArea, hibSession);
	}

	public void saveOrUpdate(AcademicArea academicArea) {
		saveOrUpdate((Object) academicArea);
	}

	public void saveOrUpdate(AcademicArea academicArea, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) academicArea, hibSession);
	}


	public void update(AcademicArea academicArea) {
		update((Object) academicArea);
	}

	public void update(AcademicArea academicArea, org.hibernate.Session hibSession) {
		update((Object) academicArea, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(AcademicArea academicArea) {
		delete((Object) academicArea);
	}

	public void delete(AcademicArea academicArea, org.hibernate.Session hibSession) {
		delete((Object) academicArea, hibSession);
	}

	public void refresh(AcademicArea academicArea, org.hibernate.Session hibSession) {
		refresh((Object) academicArea, hibSession);
	}

	public List<AcademicArea> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from AcademicArea").list();
	}

	public List<AcademicArea> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from AcademicArea x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
