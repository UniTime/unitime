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

import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DepartmentRoomFeatureDAO;

public abstract class BaseDepartmentRoomFeatureDAO extends _RootDAO {

	private static DepartmentRoomFeatureDAO sInstance;

	public static DepartmentRoomFeatureDAO getInstance () {
		if (sInstance == null) sInstance = new DepartmentRoomFeatureDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return DepartmentRoomFeature.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public DepartmentRoomFeature get(Long uniqueId) {
		return (DepartmentRoomFeature) get(getReferenceClass(), uniqueId);
	}

	public DepartmentRoomFeature get(Long uniqueId, org.hibernate.Session hibSession) {
		return (DepartmentRoomFeature) get(getReferenceClass(), uniqueId, hibSession);
	}

	public DepartmentRoomFeature load(Long uniqueId) {
		return (DepartmentRoomFeature) load(getReferenceClass(), uniqueId);
	}

	public DepartmentRoomFeature load(Long uniqueId, org.hibernate.Session hibSession) {
		return (DepartmentRoomFeature) load(getReferenceClass(), uniqueId, hibSession);
	}

	public DepartmentRoomFeature loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		DepartmentRoomFeature departmentRoomFeature = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(departmentRoomFeature)) Hibernate.initialize(departmentRoomFeature);
		return departmentRoomFeature;
	}

	public void save(DepartmentRoomFeature departmentRoomFeature) {
		save((Object) departmentRoomFeature);
	}

	public void save(DepartmentRoomFeature departmentRoomFeature, org.hibernate.Session hibSession) {
		save((Object) departmentRoomFeature, hibSession);
	}

	public void saveOrUpdate(DepartmentRoomFeature departmentRoomFeature) {
		saveOrUpdate((Object) departmentRoomFeature);
	}

	public void saveOrUpdate(DepartmentRoomFeature departmentRoomFeature, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) departmentRoomFeature, hibSession);
	}


	public void update(DepartmentRoomFeature departmentRoomFeature) {
		update((Object) departmentRoomFeature);
	}

	public void update(DepartmentRoomFeature departmentRoomFeature, org.hibernate.Session hibSession) {
		update((Object) departmentRoomFeature, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(DepartmentRoomFeature departmentRoomFeature) {
		delete((Object) departmentRoomFeature);
	}

	public void delete(DepartmentRoomFeature departmentRoomFeature, org.hibernate.Session hibSession) {
		delete((Object) departmentRoomFeature, hibSession);
	}

	public void refresh(DepartmentRoomFeature departmentRoomFeature, org.hibernate.Session hibSession) {
		refresh((Object) departmentRoomFeature, hibSession);
	}

	public List<DepartmentRoomFeature> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from DepartmentRoomFeature").list();
	}

	public List<DepartmentRoomFeature> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from DepartmentRoomFeature x where x.department.uniqueId = :departmentId").setLong("departmentId", departmentId).list();
	}
}
