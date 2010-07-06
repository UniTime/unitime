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

import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamEventDAO;

public abstract class BaseExamEventDAO extends _RootDAO {

	private static ExamEventDAO sInstance;

	public static ExamEventDAO getInstance () {
		if (sInstance == null) sInstance = new ExamEventDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ExamEvent.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ExamEvent get(Long uniqueId) {
		return (ExamEvent) get(getReferenceClass(), uniqueId);
	}

	public ExamEvent get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamEvent) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamEvent load(Long uniqueId) {
		return (ExamEvent) load(getReferenceClass(), uniqueId);
	}

	public ExamEvent load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamEvent) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamEvent loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ExamEvent examEvent = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(examEvent)) Hibernate.initialize(examEvent);
		return examEvent;
	}

	public void save(ExamEvent examEvent) {
		save((Object) examEvent);
	}

	public void save(ExamEvent examEvent, org.hibernate.Session hibSession) {
		save((Object) examEvent, hibSession);
	}

	public void saveOrUpdate(ExamEvent examEvent) {
		saveOrUpdate((Object) examEvent);
	}

	public void saveOrUpdate(ExamEvent examEvent, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) examEvent, hibSession);
	}


	public void update(ExamEvent examEvent) {
		update((Object) examEvent);
	}

	public void update(ExamEvent examEvent, org.hibernate.Session hibSession) {
		update((Object) examEvent, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ExamEvent examEvent) {
		delete((Object) examEvent);
	}

	public void delete(ExamEvent examEvent, org.hibernate.Session hibSession) {
		delete((Object) examEvent, hibSession);
	}

	public void refresh(ExamEvent examEvent, org.hibernate.Session hibSession) {
		refresh((Object) examEvent, hibSession);
	}

	public List<ExamEvent> findByExam(org.hibernate.Session hibSession, Long examId) {
		return hibSession.createQuery("from ExamEvent x where x.exam.uniqueId = :examId").setLong("examId", examId).list();
	}
}
