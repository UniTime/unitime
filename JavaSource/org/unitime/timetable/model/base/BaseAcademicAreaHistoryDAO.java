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

import org.unitime.timetable.model.AcademicAreaHistory;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AcademicAreaHistoryDAO;

public abstract class BaseAcademicAreaHistoryDAO extends _RootDAO {

	private static AcademicAreaHistoryDAO sInstance;

	public static AcademicAreaHistoryDAO getInstance () {
		if (sInstance == null) sInstance = new AcademicAreaHistoryDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return AcademicAreaHistory.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public AcademicAreaHistory get(Long uniqueId) {
		return (AcademicAreaHistory) get(getReferenceClass(), uniqueId);
	}

	public AcademicAreaHistory get(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcademicAreaHistory) get(getReferenceClass(), uniqueId, hibSession);
	}

	public AcademicAreaHistory load(Long uniqueId) {
		return (AcademicAreaHistory) load(getReferenceClass(), uniqueId);
	}

	public AcademicAreaHistory load(Long uniqueId, org.hibernate.Session hibSession) {
		return (AcademicAreaHistory) load(getReferenceClass(), uniqueId, hibSession);
	}

	public AcademicAreaHistory loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		AcademicAreaHistory academicAreaHistory = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(academicAreaHistory)) Hibernate.initialize(academicAreaHistory);
		return academicAreaHistory;
	}

	public void save(AcademicAreaHistory academicAreaHistory) {
		save((Object) academicAreaHistory);
	}

	public void save(AcademicAreaHistory academicAreaHistory, org.hibernate.Session hibSession) {
		save((Object) academicAreaHistory, hibSession);
	}

	public void saveOrUpdate(AcademicAreaHistory academicAreaHistory) {
		saveOrUpdate((Object) academicAreaHistory);
	}

	public void saveOrUpdate(AcademicAreaHistory academicAreaHistory, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) academicAreaHistory, hibSession);
	}


	public void update(AcademicAreaHistory academicAreaHistory) {
		update((Object) academicAreaHistory);
	}

	public void update(AcademicAreaHistory academicAreaHistory, org.hibernate.Session hibSession) {
		update((Object) academicAreaHistory, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(AcademicAreaHistory academicAreaHistory) {
		delete((Object) academicAreaHistory);
	}

	public void delete(AcademicAreaHistory academicAreaHistory, org.hibernate.Session hibSession) {
		delete((Object) academicAreaHistory, hibSession);
	}

	public void refresh(AcademicAreaHistory academicAreaHistory, org.hibernate.Session hibSession) {
		refresh((Object) academicAreaHistory, hibSession);
	}

	public List<AcademicAreaHistory> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from AcademicAreaHistory").list();
	}
}
