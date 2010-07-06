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

import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamDAO;

public abstract class BaseExamDAO extends _RootDAO {

	private static ExamDAO sInstance;

	public static ExamDAO getInstance () {
		if (sInstance == null) sInstance = new ExamDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Exam.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Exam get(Long uniqueId) {
		return (Exam) get(getReferenceClass(), uniqueId);
	}

	public Exam get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Exam) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Exam load(Long uniqueId) {
		return (Exam) load(getReferenceClass(), uniqueId);
	}

	public Exam load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Exam) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Exam loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Exam exam = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(exam)) Hibernate.initialize(exam);
		return exam;
	}

	public void save(Exam exam) {
		save((Object) exam);
	}

	public void save(Exam exam, org.hibernate.Session hibSession) {
		save((Object) exam, hibSession);
	}

	public void saveOrUpdate(Exam exam) {
		saveOrUpdate((Object) exam);
	}

	public void saveOrUpdate(Exam exam, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) exam, hibSession);
	}


	public void update(Exam exam) {
		update((Object) exam);
	}

	public void update(Exam exam, org.hibernate.Session hibSession) {
		update((Object) exam, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Exam exam) {
		delete((Object) exam);
	}

	public void delete(Exam exam, org.hibernate.Session hibSession) {
		delete((Object) exam, hibSession);
	}

	public void refresh(Exam exam, org.hibernate.Session hibSession) {
		refresh((Object) exam, hibSession);
	}

	public List<Exam> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Exam").list();
	}

	public List<Exam> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from Exam x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	public List<Exam> findByAssignedPeriod(org.hibernate.Session hibSession, Long assignedPeriodId) {
		return hibSession.createQuery("from Exam x where x.assignedPeriod.uniqueId = :assignedPeriodId").setLong("assignedPeriodId", assignedPeriodId).list();
	}
}
