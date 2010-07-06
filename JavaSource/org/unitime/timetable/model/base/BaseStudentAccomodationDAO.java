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

import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StudentAccomodationDAO;

public abstract class BaseStudentAccomodationDAO extends _RootDAO {

	private static StudentAccomodationDAO sInstance;

	public static StudentAccomodationDAO getInstance () {
		if (sInstance == null) sInstance = new StudentAccomodationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return StudentAccomodation.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public StudentAccomodation get(Long uniqueId) {
		return (StudentAccomodation) get(getReferenceClass(), uniqueId);
	}

	public StudentAccomodation get(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentAccomodation) get(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentAccomodation load(Long uniqueId) {
		return (StudentAccomodation) load(getReferenceClass(), uniqueId);
	}

	public StudentAccomodation load(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentAccomodation) load(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentAccomodation loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		StudentAccomodation studentAccomodation = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(studentAccomodation)) Hibernate.initialize(studentAccomodation);
		return studentAccomodation;
	}

	public void save(StudentAccomodation studentAccomodation) {
		save((Object) studentAccomodation);
	}

	public void save(StudentAccomodation studentAccomodation, org.hibernate.Session hibSession) {
		save((Object) studentAccomodation, hibSession);
	}

	public void saveOrUpdate(StudentAccomodation studentAccomodation) {
		saveOrUpdate((Object) studentAccomodation);
	}

	public void saveOrUpdate(StudentAccomodation studentAccomodation, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) studentAccomodation, hibSession);
	}


	public void update(StudentAccomodation studentAccomodation) {
		update((Object) studentAccomodation);
	}

	public void update(StudentAccomodation studentAccomodation, org.hibernate.Session hibSession) {
		update((Object) studentAccomodation, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(StudentAccomodation studentAccomodation) {
		delete((Object) studentAccomodation);
	}

	public void delete(StudentAccomodation studentAccomodation, org.hibernate.Session hibSession) {
		delete((Object) studentAccomodation, hibSession);
	}

	public void refresh(StudentAccomodation studentAccomodation, org.hibernate.Session hibSession) {
		refresh((Object) studentAccomodation, hibSession);
	}

	public List<StudentAccomodation> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from StudentAccomodation").list();
	}

	public List<StudentAccomodation> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from StudentAccomodation x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
