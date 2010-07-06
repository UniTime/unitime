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

import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StudentDAO;

public abstract class BaseStudentDAO extends _RootDAO {

	private static StudentDAO sInstance;

	public static StudentDAO getInstance () {
		if (sInstance == null) sInstance = new StudentDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Student.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Student get(Long uniqueId) {
		return (Student) get(getReferenceClass(), uniqueId);
	}

	public Student get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Student) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Student load(Long uniqueId) {
		return (Student) load(getReferenceClass(), uniqueId);
	}

	public Student load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Student) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Student loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Student student = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(student)) Hibernate.initialize(student);
		return student;
	}

	public void save(Student student) {
		save((Object) student);
	}

	public void save(Student student, org.hibernate.Session hibSession) {
		save((Object) student, hibSession);
	}

	public void saveOrUpdate(Student student) {
		saveOrUpdate((Object) student);
	}

	public void saveOrUpdate(Student student, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) student, hibSession);
	}


	public void update(Student student) {
		update((Object) student);
	}

	public void update(Student student, org.hibernate.Session hibSession) {
		update((Object) student, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Student student) {
		delete((Object) student);
	}

	public void delete(Student student, org.hibernate.Session hibSession) {
		delete((Object) student, hibSession);
	}

	public void refresh(Student student, org.hibernate.Session hibSession) {
		refresh((Object) student, hibSession);
	}

	public List<Student> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Student").list();
	}

	public List<Student> findByStatus(org.hibernate.Session hibSession, Long statusId) {
		return hibSession.createQuery("from Student x where x.status.uniqueId = :statusId").setLong("statusId", statusId).list();
	}

	public List<Student> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from Student x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
