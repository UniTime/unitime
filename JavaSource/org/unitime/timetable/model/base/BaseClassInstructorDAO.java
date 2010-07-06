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

import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ClassInstructorDAO;

public abstract class BaseClassInstructorDAO extends _RootDAO {

	private static ClassInstructorDAO sInstance;

	public static ClassInstructorDAO getInstance () {
		if (sInstance == null) sInstance = new ClassInstructorDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ClassInstructor.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ClassInstructor get(Long uniqueId) {
		return (ClassInstructor) get(getReferenceClass(), uniqueId);
	}

	public ClassInstructor get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ClassInstructor) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ClassInstructor load(Long uniqueId) {
		return (ClassInstructor) load(getReferenceClass(), uniqueId);
	}

	public ClassInstructor load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ClassInstructor) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ClassInstructor loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ClassInstructor classInstructor = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(classInstructor)) Hibernate.initialize(classInstructor);
		return classInstructor;
	}

	public void save(ClassInstructor classInstructor) {
		save((Object) classInstructor);
	}

	public void save(ClassInstructor classInstructor, org.hibernate.Session hibSession) {
		save((Object) classInstructor, hibSession);
	}

	public void saveOrUpdate(ClassInstructor classInstructor) {
		saveOrUpdate((Object) classInstructor);
	}

	public void saveOrUpdate(ClassInstructor classInstructor, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) classInstructor, hibSession);
	}


	public void update(ClassInstructor classInstructor) {
		update((Object) classInstructor);
	}

	public void update(ClassInstructor classInstructor, org.hibernate.Session hibSession) {
		update((Object) classInstructor, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ClassInstructor classInstructor) {
		delete((Object) classInstructor);
	}

	public void delete(ClassInstructor classInstructor, org.hibernate.Session hibSession) {
		delete((Object) classInstructor, hibSession);
	}

	public void refresh(ClassInstructor classInstructor, org.hibernate.Session hibSession) {
		refresh((Object) classInstructor, hibSession);
	}

	public List<ClassInstructor> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ClassInstructor").list();
	}

	public List<ClassInstructor> findByClassInstructing(org.hibernate.Session hibSession, Long classInstructingId) {
		return hibSession.createQuery("from ClassInstructor x where x.classInstructing.uniqueId = :classInstructingId").setLong("classInstructingId", classInstructingId).list();
	}

	public List<ClassInstructor> findByInstructor(org.hibernate.Session hibSession, Long instructorId) {
		return hibSession.createQuery("from ClassInstructor x where x.instructor.uniqueId = :instructorId").setLong("instructorId", instructorId).list();
	}
}
