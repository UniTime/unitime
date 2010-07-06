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

import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StudentGroupDAO;

public abstract class BaseStudentGroupDAO extends _RootDAO {

	private static StudentGroupDAO sInstance;

	public static StudentGroupDAO getInstance () {
		if (sInstance == null) sInstance = new StudentGroupDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return StudentGroup.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public StudentGroup get(Long uniqueId) {
		return (StudentGroup) get(getReferenceClass(), uniqueId);
	}

	public StudentGroup get(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentGroup) get(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentGroup load(Long uniqueId) {
		return (StudentGroup) load(getReferenceClass(), uniqueId);
	}

	public StudentGroup load(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentGroup) load(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentGroup loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		StudentGroup studentGroup = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(studentGroup)) Hibernate.initialize(studentGroup);
		return studentGroup;
	}

	public void save(StudentGroup studentGroup) {
		save((Object) studentGroup);
	}

	public void save(StudentGroup studentGroup, org.hibernate.Session hibSession) {
		save((Object) studentGroup, hibSession);
	}

	public void saveOrUpdate(StudentGroup studentGroup) {
		saveOrUpdate((Object) studentGroup);
	}

	public void saveOrUpdate(StudentGroup studentGroup, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) studentGroup, hibSession);
	}


	public void update(StudentGroup studentGroup) {
		update((Object) studentGroup);
	}

	public void update(StudentGroup studentGroup, org.hibernate.Session hibSession) {
		update((Object) studentGroup, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(StudentGroup studentGroup) {
		delete((Object) studentGroup);
	}

	public void delete(StudentGroup studentGroup, org.hibernate.Session hibSession) {
		delete((Object) studentGroup, hibSession);
	}

	public void refresh(StudentGroup studentGroup, org.hibernate.Session hibSession) {
		refresh((Object) studentGroup, hibSession);
	}

	public List<StudentGroup> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from StudentGroup").list();
	}

	public List<StudentGroup> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from StudentGroup x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
