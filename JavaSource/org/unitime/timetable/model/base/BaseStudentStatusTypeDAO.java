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

import org.unitime.timetable.model.StudentStatusType;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StudentStatusTypeDAO;

public abstract class BaseStudentStatusTypeDAO extends _RootDAO {

	private static StudentStatusTypeDAO sInstance;

	public static StudentStatusTypeDAO getInstance () {
		if (sInstance == null) sInstance = new StudentStatusTypeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return StudentStatusType.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public StudentStatusType get(Long uniqueId) {
		return (StudentStatusType) get(getReferenceClass(), uniqueId);
	}

	public StudentStatusType get(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentStatusType) get(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentStatusType load(Long uniqueId) {
		return (StudentStatusType) load(getReferenceClass(), uniqueId);
	}

	public StudentStatusType load(Long uniqueId, org.hibernate.Session hibSession) {
		return (StudentStatusType) load(getReferenceClass(), uniqueId, hibSession);
	}

	public StudentStatusType loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		StudentStatusType studentStatusType = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(studentStatusType)) Hibernate.initialize(studentStatusType);
		return studentStatusType;
	}

	public void save(StudentStatusType studentStatusType) {
		save((Object) studentStatusType);
	}

	public void save(StudentStatusType studentStatusType, org.hibernate.Session hibSession) {
		save((Object) studentStatusType, hibSession);
	}

	public void saveOrUpdate(StudentStatusType studentStatusType) {
		saveOrUpdate((Object) studentStatusType);
	}

	public void saveOrUpdate(StudentStatusType studentStatusType, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) studentStatusType, hibSession);
	}


	public void update(StudentStatusType studentStatusType) {
		update((Object) studentStatusType);
	}

	public void update(StudentStatusType studentStatusType, org.hibernate.Session hibSession) {
		update((Object) studentStatusType, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(StudentStatusType studentStatusType) {
		delete((Object) studentStatusType);
	}

	public void delete(StudentStatusType studentStatusType, org.hibernate.Session hibSession) {
		delete((Object) studentStatusType, hibSession);
	}

	public void refresh(StudentStatusType studentStatusType, org.hibernate.Session hibSession) {
		refresh((Object) studentStatusType, hibSession);
	}

	public List<StudentStatusType> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from StudentStatusType").list();
	}
}
