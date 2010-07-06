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

import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ClassWaitListDAO;

public abstract class BaseClassWaitListDAO extends _RootDAO {

	private static ClassWaitListDAO sInstance;

	public static ClassWaitListDAO getInstance () {
		if (sInstance == null) sInstance = new ClassWaitListDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ClassWaitList.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ClassWaitList get(Long uniqueId) {
		return (ClassWaitList) get(getReferenceClass(), uniqueId);
	}

	public ClassWaitList get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ClassWaitList) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ClassWaitList load(Long uniqueId) {
		return (ClassWaitList) load(getReferenceClass(), uniqueId);
	}

	public ClassWaitList load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ClassWaitList) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ClassWaitList loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ClassWaitList classWaitList = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(classWaitList)) Hibernate.initialize(classWaitList);
		return classWaitList;
	}

	public void save(ClassWaitList classWaitList) {
		save((Object) classWaitList);
	}

	public void save(ClassWaitList classWaitList, org.hibernate.Session hibSession) {
		save((Object) classWaitList, hibSession);
	}

	public void saveOrUpdate(ClassWaitList classWaitList) {
		saveOrUpdate((Object) classWaitList);
	}

	public void saveOrUpdate(ClassWaitList classWaitList, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) classWaitList, hibSession);
	}


	public void update(ClassWaitList classWaitList) {
		update((Object) classWaitList);
	}

	public void update(ClassWaitList classWaitList, org.hibernate.Session hibSession) {
		update((Object) classWaitList, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ClassWaitList classWaitList) {
		delete((Object) classWaitList);
	}

	public void delete(ClassWaitList classWaitList, org.hibernate.Session hibSession) {
		delete((Object) classWaitList, hibSession);
	}

	public void refresh(ClassWaitList classWaitList, org.hibernate.Session hibSession) {
		refresh((Object) classWaitList, hibSession);
	}

	public List<ClassWaitList> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ClassWaitList").list();
	}

	public List<ClassWaitList> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from ClassWaitList x where x.student.uniqueId = :studentId").setLong("studentId", studentId).list();
	}

	public List<ClassWaitList> findByCourseRequest(org.hibernate.Session hibSession, Long courseRequestId) {
		return hibSession.createQuery("from ClassWaitList x where x.courseRequest.uniqueId = :courseRequestId").setLong("courseRequestId", courseRequestId).list();
	}

	public List<ClassWaitList> findByClazz(org.hibernate.Session hibSession, Long clazzId) {
		return hibSession.createQuery("from ClassWaitList x where x.clazz.uniqueId = :clazzId").setLong("clazzId", clazzId).list();
	}
}
