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

import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamPeriodPrefDAO;

public abstract class BaseExamPeriodPrefDAO extends _RootDAO {

	private static ExamPeriodPrefDAO sInstance;

	public static ExamPeriodPrefDAO getInstance () {
		if (sInstance == null) sInstance = new ExamPeriodPrefDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ExamPeriodPref.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ExamPeriodPref get(Long uniqueId) {
		return (ExamPeriodPref) get(getReferenceClass(), uniqueId);
	}

	public ExamPeriodPref get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamPeriodPref) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamPeriodPref load(Long uniqueId) {
		return (ExamPeriodPref) load(getReferenceClass(), uniqueId);
	}

	public ExamPeriodPref load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamPeriodPref) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamPeriodPref loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ExamPeriodPref examPeriodPref = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(examPeriodPref)) Hibernate.initialize(examPeriodPref);
		return examPeriodPref;
	}

	public void save(ExamPeriodPref examPeriodPref) {
		save((Object) examPeriodPref);
	}

	public void save(ExamPeriodPref examPeriodPref, org.hibernate.Session hibSession) {
		save((Object) examPeriodPref, hibSession);
	}

	public void saveOrUpdate(ExamPeriodPref examPeriodPref) {
		saveOrUpdate((Object) examPeriodPref);
	}

	public void saveOrUpdate(ExamPeriodPref examPeriodPref, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) examPeriodPref, hibSession);
	}


	public void update(ExamPeriodPref examPeriodPref) {
		update((Object) examPeriodPref);
	}

	public void update(ExamPeriodPref examPeriodPref, org.hibernate.Session hibSession) {
		update((Object) examPeriodPref, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ExamPeriodPref examPeriodPref) {
		delete((Object) examPeriodPref);
	}

	public void delete(ExamPeriodPref examPeriodPref, org.hibernate.Session hibSession) {
		delete((Object) examPeriodPref, hibSession);
	}

	public void refresh(ExamPeriodPref examPeriodPref, org.hibernate.Session hibSession) {
		refresh((Object) examPeriodPref, hibSession);
	}

	public List<ExamPeriodPref> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ExamPeriodPref").list();
	}

	public List<ExamPeriodPref> findByExamPeriod(org.hibernate.Session hibSession, Long examPeriodId) {
		return hibSession.createQuery("from ExamPeriodPref x where x.examPeriod.uniqueId = :examPeriodId").setLong("examPeriodId", examPeriodId).list();
	}
}
