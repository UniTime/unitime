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

import org.unitime.timetable.model.WaitList;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.WaitListDAO;

public abstract class BaseWaitListDAO extends _RootDAO {

	private static WaitListDAO sInstance;

	public static WaitListDAO getInstance () {
		if (sInstance == null) sInstance = new WaitListDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return WaitList.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public WaitList get(Long uniqueId) {
		return (WaitList) get(getReferenceClass(), uniqueId);
	}

	public WaitList get(Long uniqueId, org.hibernate.Session hibSession) {
		return (WaitList) get(getReferenceClass(), uniqueId, hibSession);
	}

	public WaitList load(Long uniqueId) {
		return (WaitList) load(getReferenceClass(), uniqueId);
	}

	public WaitList load(Long uniqueId, org.hibernate.Session hibSession) {
		return (WaitList) load(getReferenceClass(), uniqueId, hibSession);
	}

	public WaitList loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		WaitList waitList = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(waitList)) Hibernate.initialize(waitList);
		return waitList;
	}

	public void save(WaitList waitList) {
		save((Object) waitList);
	}

	public void save(WaitList waitList, org.hibernate.Session hibSession) {
		save((Object) waitList, hibSession);
	}

	public void saveOrUpdate(WaitList waitList) {
		saveOrUpdate((Object) waitList);
	}

	public void saveOrUpdate(WaitList waitList, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) waitList, hibSession);
	}


	public void update(WaitList waitList) {
		update((Object) waitList);
	}

	public void update(WaitList waitList, org.hibernate.Session hibSession) {
		update((Object) waitList, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(WaitList waitList) {
		delete((Object) waitList);
	}

	public void delete(WaitList waitList, org.hibernate.Session hibSession) {
		delete((Object) waitList, hibSession);
	}

	public void refresh(WaitList waitList, org.hibernate.Session hibSession) {
		refresh((Object) waitList, hibSession);
	}

	public List<WaitList> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from WaitList").list();
	}

	public List<WaitList> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from WaitList x where x.student.uniqueId = :studentId").setLong("studentId", studentId).list();
	}

	public List<WaitList> findByCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId) {
		return hibSession.createQuery("from WaitList x where x.courseOffering.uniqueId = :courseOfferingId").setLong("courseOfferingId", courseOfferingId).list();
	}
}
