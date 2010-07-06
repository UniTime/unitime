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

import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamConflictDAO;

public abstract class BaseExamConflictDAO extends _RootDAO {

	private static ExamConflictDAO sInstance;

	public static ExamConflictDAO getInstance () {
		if (sInstance == null) sInstance = new ExamConflictDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ExamConflict.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ExamConflict get(Long uniqueId) {
		return (ExamConflict) get(getReferenceClass(), uniqueId);
	}

	public ExamConflict get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamConflict) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamConflict load(Long uniqueId) {
		return (ExamConflict) load(getReferenceClass(), uniqueId);
	}

	public ExamConflict load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamConflict) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamConflict loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ExamConflict examConflict = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(examConflict)) Hibernate.initialize(examConflict);
		return examConflict;
	}

	public void save(ExamConflict examConflict) {
		save((Object) examConflict);
	}

	public void save(ExamConflict examConflict, org.hibernate.Session hibSession) {
		save((Object) examConflict, hibSession);
	}

	public void saveOrUpdate(ExamConflict examConflict) {
		saveOrUpdate((Object) examConflict);
	}

	public void saveOrUpdate(ExamConflict examConflict, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) examConflict, hibSession);
	}


	public void update(ExamConflict examConflict) {
		update((Object) examConflict);
	}

	public void update(ExamConflict examConflict, org.hibernate.Session hibSession) {
		update((Object) examConflict, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ExamConflict examConflict) {
		delete((Object) examConflict);
	}

	public void delete(ExamConflict examConflict, org.hibernate.Session hibSession) {
		delete((Object) examConflict, hibSession);
	}

	public void refresh(ExamConflict examConflict, org.hibernate.Session hibSession) {
		refresh((Object) examConflict, hibSession);
	}

	public List<ExamConflict> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ExamConflict").list();
	}
}
