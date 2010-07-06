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

import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamPeriodDAO;

public abstract class BaseExamPeriodDAO extends _RootDAO {

	private static ExamPeriodDAO sInstance;

	public static ExamPeriodDAO getInstance () {
		if (sInstance == null) sInstance = new ExamPeriodDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ExamPeriod.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ExamPeriod get(Long uniqueId) {
		return (ExamPeriod) get(getReferenceClass(), uniqueId);
	}

	public ExamPeriod get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamPeriod) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamPeriod load(Long uniqueId) {
		return (ExamPeriod) load(getReferenceClass(), uniqueId);
	}

	public ExamPeriod load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamPeriod) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamPeriod loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ExamPeriod examPeriod = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(examPeriod)) Hibernate.initialize(examPeriod);
		return examPeriod;
	}

	public void save(ExamPeriod examPeriod) {
		save((Object) examPeriod);
	}

	public void save(ExamPeriod examPeriod, org.hibernate.Session hibSession) {
		save((Object) examPeriod, hibSession);
	}

	public void saveOrUpdate(ExamPeriod examPeriod) {
		saveOrUpdate((Object) examPeriod);
	}

	public void saveOrUpdate(ExamPeriod examPeriod, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) examPeriod, hibSession);
	}


	public void update(ExamPeriod examPeriod) {
		update((Object) examPeriod);
	}

	public void update(ExamPeriod examPeriod, org.hibernate.Session hibSession) {
		update((Object) examPeriod, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ExamPeriod examPeriod) {
		delete((Object) examPeriod);
	}

	public void delete(ExamPeriod examPeriod, org.hibernate.Session hibSession) {
		delete((Object) examPeriod, hibSession);
	}

	public void refresh(ExamPeriod examPeriod, org.hibernate.Session hibSession) {
		refresh((Object) examPeriod, hibSession);
	}

	public List<ExamPeriod> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ExamPeriod").list();
	}

	public List<ExamPeriod> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from ExamPeriod x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	public List<ExamPeriod> findByPrefLevel(org.hibernate.Session hibSession, Long prefLevelId) {
		return hibSession.createQuery("from ExamPeriod x where x.prefLevel.uniqueId = :prefLevelId").setLong("prefLevelId", prefLevelId).list();
	}
}
