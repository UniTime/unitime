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

import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StudentGroupReservationDAO;

public abstract class BaseStudentGroupReservationDAO extends _RootDAO {

	private static StudentGroupReservationDAO sInstance;

	public static StudentGroupReservationDAO getInstance () {
		if (sInstance == null) sInstance = new StudentGroupReservationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return StudentGroupReservation.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public StudentGroupReservation get(Long uniqueId) {
		return (StudentGroupReservation) get(getReferenceClass(), uniqueId);
	}

	public StudentGroupReservation get(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentGroupReservation) get(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentGroupReservation load(Long uniqueId) {
		return (StudentGroupReservation) load(getReferenceClass(), uniqueId);
	}

	public StudentGroupReservation load(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentGroupReservation) load(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentGroupReservation loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		StudentGroupReservation studentGroupReservation = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(studentGroupReservation)) Hibernate.initialize(studentGroupReservation);
		return studentGroupReservation;
	}

	public void save(StudentGroupReservation studentGroupReservation) {
		save((Object) studentGroupReservation);
	}

	public void save(StudentGroupReservation studentGroupReservation, org.hibernate.Session hibSession) {
		save((Object) studentGroupReservation, hibSession);
	}

	public void saveOrUpdate(StudentGroupReservation studentGroupReservation) {
		saveOrUpdate((Object) studentGroupReservation);
	}

	public void saveOrUpdate(StudentGroupReservation studentGroupReservation, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) studentGroupReservation, hibSession);
	}


	public void update(StudentGroupReservation studentGroupReservation) {
		update((Object) studentGroupReservation);
	}

	public void update(StudentGroupReservation studentGroupReservation, org.hibernate.Session hibSession) {
		update((Object) studentGroupReservation, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(StudentGroupReservation studentGroupReservation) {
		delete((Object) studentGroupReservation);
	}

	public void delete(StudentGroupReservation studentGroupReservation, org.hibernate.Session hibSession) {
		delete((Object) studentGroupReservation, hibSession);
	}

	public void refresh(StudentGroupReservation studentGroupReservation, org.hibernate.Session hibSession) {
		refresh((Object) studentGroupReservation, hibSession);
	}

	public List<StudentGroupReservation> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from StudentGroupReservation").list();
	}

	public List<StudentGroupReservation> findByStudentGroup(org.hibernate.Session hibSession, Long studentGroupId) {
		return hibSession.createQuery("from StudentGroupReservation x where x.studentGroup.uniqueId = :studentGroupId").setLong("studentGroupId", studentGroupId).list();
	}
}
