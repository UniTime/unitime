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

import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DepartmentStatusTypeDAO;

public abstract class BaseDepartmentStatusTypeDAO extends _RootDAO {

	private static DepartmentStatusTypeDAO sInstance;

	public static DepartmentStatusTypeDAO getInstance () {
		if (sInstance == null) sInstance = new DepartmentStatusTypeDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return DepartmentStatusType.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public DepartmentStatusType get(Long uniqueId) {
		return (DepartmentStatusType) get(getReferenceClass(), uniqueId);
	}

	public DepartmentStatusType get(Long uniqueId, org.hibernate.Session hibSession) {
		return (DepartmentStatusType) get(getReferenceClass(), uniqueId, hibSession);
	}

	public DepartmentStatusType load(Long uniqueId) {
		return (DepartmentStatusType) load(getReferenceClass(), uniqueId);
	}

	public DepartmentStatusType load(Long uniqueId, org.hibernate.Session hibSession) {
		return (DepartmentStatusType) load(getReferenceClass(), uniqueId, hibSession);
	}

	public DepartmentStatusType loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		DepartmentStatusType departmentStatusType = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(departmentStatusType)) Hibernate.initialize(departmentStatusType);
		return departmentStatusType;
	}

	public void save(DepartmentStatusType departmentStatusType) {
		save((Object) departmentStatusType);
	}

	public void save(DepartmentStatusType departmentStatusType, org.hibernate.Session hibSession) {
		save((Object) departmentStatusType, hibSession);
	}

	public void saveOrUpdate(DepartmentStatusType departmentStatusType) {
		saveOrUpdate((Object) departmentStatusType);
	}

	public void saveOrUpdate(DepartmentStatusType departmentStatusType, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) departmentStatusType, hibSession);
	}


	public void update(DepartmentStatusType departmentStatusType) {
		update((Object) departmentStatusType);
	}

	public void update(DepartmentStatusType departmentStatusType, org.hibernate.Session hibSession) {
		update((Object) departmentStatusType, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(DepartmentStatusType departmentStatusType) {
		delete((Object) departmentStatusType);
	}

	public void delete(DepartmentStatusType departmentStatusType, org.hibernate.Session hibSession) {
		delete((Object) departmentStatusType, hibSession);
	}

	public void refresh(DepartmentStatusType departmentStatusType, org.hibernate.Session hibSession) {
		refresh((Object) departmentStatusType, hibSession);
	}

	public List<DepartmentStatusType> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from DepartmentStatusType").list();
	}
}
