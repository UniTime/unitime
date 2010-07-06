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

import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StudentClassEnrollmentDAO;

public abstract class BaseStudentClassEnrollmentDAO extends _RootDAO {

	private static StudentClassEnrollmentDAO sInstance;

	public static StudentClassEnrollmentDAO getInstance () {
		if (sInstance == null) sInstance = new StudentClassEnrollmentDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return StudentClassEnrollment.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public StudentClassEnrollment get(Long uniqueId) {
		return (StudentClassEnrollment) get(getReferenceClass(), uniqueId);
	}

	public StudentClassEnrollment get(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentClassEnrollment) get(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentClassEnrollment load(Long uniqueId) {
		return (StudentClassEnrollment) load(getReferenceClass(), uniqueId);
	}

	public StudentClassEnrollment load(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentClassEnrollment) load(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentClassEnrollment loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		StudentClassEnrollment studentClassEnrollment = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(studentClassEnrollment)) Hibernate.initialize(studentClassEnrollment);
		return studentClassEnrollment;
	}

	public void save(StudentClassEnrollment studentClassEnrollment) {
		save((Object) studentClassEnrollment);
	}

	public void save(StudentClassEnrollment studentClassEnrollment, org.hibernate.Session hibSession) {
		save((Object) studentClassEnrollment, hibSession);
	}

	public void saveOrUpdate(StudentClassEnrollment studentClassEnrollment) {
		saveOrUpdate((Object) studentClassEnrollment);
	}

	public void saveOrUpdate(StudentClassEnrollment studentClassEnrollment, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) studentClassEnrollment, hibSession);
	}


	public void update(StudentClassEnrollment studentClassEnrollment) {
		update((Object) studentClassEnrollment);
	}

	public void update(StudentClassEnrollment studentClassEnrollment, org.hibernate.Session hibSession) {
		update((Object) studentClassEnrollment, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(StudentClassEnrollment studentClassEnrollment) {
		delete((Object) studentClassEnrollment);
	}

	public void delete(StudentClassEnrollment studentClassEnrollment, org.hibernate.Session hibSession) {
		delete((Object) studentClassEnrollment, hibSession);
	}

	public void refresh(StudentClassEnrollment studentClassEnrollment, org.hibernate.Session hibSession) {
		refresh((Object) studentClassEnrollment, hibSession);
	}

	public List<StudentClassEnrollment> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from StudentClassEnrollment").list();
	}

	public List<StudentClassEnrollment> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from StudentClassEnrollment x where x.student.uniqueId = :studentId").setLong("studentId", studentId).list();
	}

	public List<StudentClassEnrollment> findByCourseRequest(org.hibernate.Session hibSession, Long courseRequestId) {
		return hibSession.createQuery("from StudentClassEnrollment x where x.courseRequest.uniqueId = :courseRequestId").setLong("courseRequestId", courseRequestId).list();
	}

	public List<StudentClassEnrollment> findByCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId) {
		return hibSession.createQuery("from StudentClassEnrollment x where x.courseOffering.uniqueId = :courseOfferingId").setLong("courseOfferingId", courseOfferingId).list();
	}

	public List<StudentClassEnrollment> findByClazz(org.hibernate.Session hibSession, Long clazzId) {
		return hibSession.createQuery("from StudentClassEnrollment x where x.clazz.uniqueId = :clazzId").setLong("clazzId", clazzId).list();
	}
}
