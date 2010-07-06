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

import org.unitime.timetable.model.StudentEnrollment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StudentEnrollmentDAO;

public abstract class BaseStudentEnrollmentDAO extends _RootDAO {

	private static StudentEnrollmentDAO sInstance;

	public static StudentEnrollmentDAO getInstance () {
		if (sInstance == null) sInstance = new StudentEnrollmentDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return StudentEnrollment.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public StudentEnrollment get(Long uniqueId) {
		return (StudentEnrollment) get(getReferenceClass(), uniqueId);
	}

	public StudentEnrollment get(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentEnrollment) get(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentEnrollment load(Long uniqueId) {
		return (StudentEnrollment) load(getReferenceClass(), uniqueId);
	}

	public StudentEnrollment load(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentEnrollment) load(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentEnrollment loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		StudentEnrollment studentEnrollment = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(studentEnrollment)) Hibernate.initialize(studentEnrollment);
		return studentEnrollment;
	}

	public void save(StudentEnrollment studentEnrollment) {
		save((Object) studentEnrollment);
	}

	public void save(StudentEnrollment studentEnrollment, org.hibernate.Session hibSession) {
		save((Object) studentEnrollment, hibSession);
	}

	public void saveOrUpdate(StudentEnrollment studentEnrollment) {
		saveOrUpdate((Object) studentEnrollment);
	}

	public void saveOrUpdate(StudentEnrollment studentEnrollment, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) studentEnrollment, hibSession);
	}


	public void update(StudentEnrollment studentEnrollment) {
		update((Object) studentEnrollment);
	}

	public void update(StudentEnrollment studentEnrollment, org.hibernate.Session hibSession) {
		update((Object) studentEnrollment, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(StudentEnrollment studentEnrollment) {
		delete((Object) studentEnrollment);
	}

	public void delete(StudentEnrollment studentEnrollment, org.hibernate.Session hibSession) {
		delete((Object) studentEnrollment, hibSession);
	}

	public void refresh(StudentEnrollment studentEnrollment, org.hibernate.Session hibSession) {
		refresh((Object) studentEnrollment, hibSession);
	}

	public List<StudentEnrollment> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from StudentEnrollment").list();
	}

	public List<StudentEnrollment> findBySolution(org.hibernate.Session hibSession, Long solutionId) {
		return hibSession.createQuery("from StudentEnrollment x where x.solution.uniqueId = :solutionId").setLong("solutionId", solutionId).list();
	}

	public List<StudentEnrollment> findByClazz(org.hibernate.Session hibSession, Long clazzId) {
		return hibSession.createQuery("from StudentEnrollment x where x.clazz.uniqueId = :clazzId").setLong("clazzId", clazzId).list();
	}
}
