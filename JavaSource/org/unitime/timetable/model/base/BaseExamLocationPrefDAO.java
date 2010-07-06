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

import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamLocationPrefDAO;

public abstract class BaseExamLocationPrefDAO extends _RootDAO {

	private static ExamLocationPrefDAO sInstance;

	public static ExamLocationPrefDAO getInstance () {
		if (sInstance == null) sInstance = new ExamLocationPrefDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ExamLocationPref.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ExamLocationPref get(Long uniqueId) {
		return (ExamLocationPref) get(getReferenceClass(), uniqueId);
	}

	public ExamLocationPref get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamLocationPref) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamLocationPref load(Long uniqueId) {
		return (ExamLocationPref) load(getReferenceClass(), uniqueId);
	}

	public ExamLocationPref load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamLocationPref) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamLocationPref loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ExamLocationPref examLocationPref = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(examLocationPref)) Hibernate.initialize(examLocationPref);
		return examLocationPref;
	}

	public void save(ExamLocationPref examLocationPref) {
		save((Object) examLocationPref);
	}

	public void save(ExamLocationPref examLocationPref, org.hibernate.Session hibSession) {
		save((Object) examLocationPref, hibSession);
	}

	public void saveOrUpdate(ExamLocationPref examLocationPref) {
		saveOrUpdate((Object) examLocationPref);
	}

	public void saveOrUpdate(ExamLocationPref examLocationPref, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) examLocationPref, hibSession);
	}


	public void update(ExamLocationPref examLocationPref) {
		update((Object) examLocationPref);
	}

	public void update(ExamLocationPref examLocationPref, org.hibernate.Session hibSession) {
		update((Object) examLocationPref, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ExamLocationPref examLocationPref) {
		delete((Object) examLocationPref);
	}

	public void delete(ExamLocationPref examLocationPref, org.hibernate.Session hibSession) {
		delete((Object) examLocationPref, hibSession);
	}

	public void refresh(ExamLocationPref examLocationPref, org.hibernate.Session hibSession) {
		refresh((Object) examLocationPref, hibSession);
	}

	public List<ExamLocationPref> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ExamLocationPref").list();
	}

	public List<ExamLocationPref> findByLocation(org.hibernate.Session hibSession, Long locationId) {
		return hibSession.createQuery("from ExamLocationPref x where x.location.uniqueId = :locationId").setLong("locationId", locationId).list();
	}

	public List<ExamLocationPref> findByPrefLevel(org.hibernate.Session hibSession, Long prefLevelId) {
		return hibSession.createQuery("from ExamLocationPref x where x.prefLevel.uniqueId = :prefLevelId").setLong("prefLevelId", prefLevelId).list();
	}

	public List<ExamLocationPref> findByExamPeriod(org.hibernate.Session hibSession, Long examPeriodId) {
		return hibSession.createQuery("from ExamLocationPref x where x.examPeriod.uniqueId = :examPeriodId").setLong("examPeriodId", examPeriodId).list();
	}
}
