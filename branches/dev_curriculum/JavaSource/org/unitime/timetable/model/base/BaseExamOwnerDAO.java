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

import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamOwnerDAO;

public abstract class BaseExamOwnerDAO extends _RootDAO {

	private static ExamOwnerDAO sInstance;

	public static ExamOwnerDAO getInstance () {
		if (sInstance == null) sInstance = new ExamOwnerDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ExamOwner.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ExamOwner get(Long uniqueId) {
		return (ExamOwner) get(getReferenceClass(), uniqueId);
	}

	public ExamOwner get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamOwner) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamOwner load(Long uniqueId) {
		return (ExamOwner) load(getReferenceClass(), uniqueId);
	}

	public ExamOwner load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExamOwner) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ExamOwner loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ExamOwner examOwner = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(examOwner)) Hibernate.initialize(examOwner);
		return examOwner;
	}

	public void save(ExamOwner examOwner) {
		save((Object) examOwner);
	}

	public void save(ExamOwner examOwner, org.hibernate.Session hibSession) {
		save((Object) examOwner, hibSession);
	}

	public void saveOrUpdate(ExamOwner examOwner) {
		saveOrUpdate((Object) examOwner);
	}

	public void saveOrUpdate(ExamOwner examOwner, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) examOwner, hibSession);
	}


	public void update(ExamOwner examOwner) {
		update((Object) examOwner);
	}

	public void update(ExamOwner examOwner, org.hibernate.Session hibSession) {
		update((Object) examOwner, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ExamOwner examOwner) {
		delete((Object) examOwner);
	}

	public void delete(ExamOwner examOwner, org.hibernate.Session hibSession) {
		delete((Object) examOwner, hibSession);
	}

	public void refresh(ExamOwner examOwner, org.hibernate.Session hibSession) {
		refresh((Object) examOwner, hibSession);
	}

	public List<ExamOwner> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ExamOwner").list();
	}

	public List<ExamOwner> findByExam(org.hibernate.Session hibSession, Long examId) {
		return hibSession.createQuery("from ExamOwner x where x.exam.uniqueId = :examId").setLong("examId", examId).list();
	}

	public List<ExamOwner> findByCourse(org.hibernate.Session hibSession, Long courseId) {
		return hibSession.createQuery("from ExamOwner x where x.course.uniqueId = :courseId").setLong("courseId", courseId).list();
	}
}
