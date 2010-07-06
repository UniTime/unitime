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

import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StudentEnrollmentMessageDAO;

public abstract class BaseStudentEnrollmentMessageDAO extends _RootDAO {

	private static StudentEnrollmentMessageDAO sInstance;

	public static StudentEnrollmentMessageDAO getInstance () {
		if (sInstance == null) sInstance = new StudentEnrollmentMessageDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return StudentEnrollmentMessage.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public StudentEnrollmentMessage get(Long uniqueId) {
		return (StudentEnrollmentMessage) get(getReferenceClass(), uniqueId);
	}

	public StudentEnrollmentMessage get(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentEnrollmentMessage) get(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentEnrollmentMessage load(Long uniqueId) {
		return (StudentEnrollmentMessage) load(getReferenceClass(), uniqueId);
	}

	public StudentEnrollmentMessage load(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentEnrollmentMessage) load(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentEnrollmentMessage loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		StudentEnrollmentMessage studentEnrollmentMessage = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(studentEnrollmentMessage)) Hibernate.initialize(studentEnrollmentMessage);
		return studentEnrollmentMessage;
	}

	public void save(StudentEnrollmentMessage studentEnrollmentMessage) {
		save((Object) studentEnrollmentMessage);
	}

	public void save(StudentEnrollmentMessage studentEnrollmentMessage, org.hibernate.Session hibSession) {
		save((Object) studentEnrollmentMessage, hibSession);
	}

	public void saveOrUpdate(StudentEnrollmentMessage studentEnrollmentMessage) {
		saveOrUpdate((Object) studentEnrollmentMessage);
	}

	public void saveOrUpdate(StudentEnrollmentMessage studentEnrollmentMessage, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) studentEnrollmentMessage, hibSession);
	}


	public void update(StudentEnrollmentMessage studentEnrollmentMessage) {
		update((Object) studentEnrollmentMessage);
	}

	public void update(StudentEnrollmentMessage studentEnrollmentMessage, org.hibernate.Session hibSession) {
		update((Object) studentEnrollmentMessage, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(StudentEnrollmentMessage studentEnrollmentMessage) {
		delete((Object) studentEnrollmentMessage);
	}

	public void delete(StudentEnrollmentMessage studentEnrollmentMessage, org.hibernate.Session hibSession) {
		delete((Object) studentEnrollmentMessage, hibSession);
	}

	public void refresh(StudentEnrollmentMessage studentEnrollmentMessage, org.hibernate.Session hibSession) {
		refresh((Object) studentEnrollmentMessage, hibSession);
	}

	public List<StudentEnrollmentMessage> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from StudentEnrollmentMessage").list();
	}

	public List<StudentEnrollmentMessage> findByCourseDemand(org.hibernate.Session hibSession, Long courseDemandId) {
		return hibSession.createQuery("from StudentEnrollmentMessage x where x.courseDemand.uniqueId = :courseDemandId").setLong("courseDemandId", courseDemandId).list();
	}
}
