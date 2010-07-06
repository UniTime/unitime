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

import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StaffDAO;

public abstract class BaseStaffDAO extends _RootDAO {

	private static StaffDAO sInstance;

	public static StaffDAO getInstance () {
		if (sInstance == null) sInstance = new StaffDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Staff.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Staff get(Long uniqueId) {
		return (Staff) get(getReferenceClass(), uniqueId);
	}

	public Staff get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Staff) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Staff load(Long uniqueId) {
		return (Staff) load(getReferenceClass(), uniqueId);
	}

	public Staff load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Staff) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Staff loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Staff staff = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(staff)) Hibernate.initialize(staff);
		return staff;
	}

	public void save(Staff staff) {
		save((Object) staff);
	}

	public void save(Staff staff, org.hibernate.Session hibSession) {
		save((Object) staff, hibSession);
	}

	public void saveOrUpdate(Staff staff) {
		saveOrUpdate((Object) staff);
	}

	public void saveOrUpdate(Staff staff, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) staff, hibSession);
	}


	public void update(Staff staff) {
		update((Object) staff);
	}

	public void update(Staff staff, org.hibernate.Session hibSession) {
		update((Object) staff, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Staff staff) {
		delete((Object) staff);
	}

	public void delete(Staff staff, org.hibernate.Session hibSession) {
		delete((Object) staff, hibSession);
	}

	public void refresh(Staff staff, org.hibernate.Session hibSession) {
		refresh((Object) staff, hibSession);
	}

	public List<Staff> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Staff").list();
	}

	public List<Staff> findByPositionCode(org.hibernate.Session hibSession, String positionCodeId) {
		return hibSession.createQuery("from Staff x where x.positionCode.positionCode = :positionCodeId").setString("positionCodeId", positionCodeId).list();
	}
}
