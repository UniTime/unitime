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

import org.unitime.timetable.model.SubjectHistory;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SubjectHistoryDAO;

public abstract class BaseSubjectHistoryDAO extends _RootDAO {

	private static SubjectHistoryDAO sInstance;

	public static SubjectHistoryDAO getInstance () {
		if (sInstance == null) sInstance = new SubjectHistoryDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SubjectHistory.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SubjectHistory get(Long uniqueId) {
		return (SubjectHistory) get(getReferenceClass(), uniqueId);
	}

	public SubjectHistory get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SubjectHistory) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SubjectHistory load(Long uniqueId) {
		return (SubjectHistory) load(getReferenceClass(), uniqueId);
	}

	public SubjectHistory load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SubjectHistory) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SubjectHistory loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SubjectHistory subjectHistory = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(subjectHistory)) Hibernate.initialize(subjectHistory);
		return subjectHistory;
	}

	public void save(SubjectHistory subjectHistory) {
		save((Object) subjectHistory);
	}

	public void save(SubjectHistory subjectHistory, org.hibernate.Session hibSession) {
		save((Object) subjectHistory, hibSession);
	}

	public void saveOrUpdate(SubjectHistory subjectHistory) {
		saveOrUpdate((Object) subjectHistory);
	}

	public void saveOrUpdate(SubjectHistory subjectHistory, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) subjectHistory, hibSession);
	}


	public void update(SubjectHistory subjectHistory) {
		update((Object) subjectHistory);
	}

	public void update(SubjectHistory subjectHistory, org.hibernate.Session hibSession) {
		update((Object) subjectHistory, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SubjectHistory subjectHistory) {
		delete((Object) subjectHistory);
	}

	public void delete(SubjectHistory subjectHistory, org.hibernate.Session hibSession) {
		delete((Object) subjectHistory, hibSession);
	}

	public void refresh(SubjectHistory subjectHistory, org.hibernate.Session hibSession) {
		refresh((Object) subjectHistory, hibSession);
	}

	public List<SubjectHistory> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SubjectHistory").list();
	}
}
