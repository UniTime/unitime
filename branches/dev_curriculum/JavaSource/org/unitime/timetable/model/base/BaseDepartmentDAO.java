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

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;

public abstract class BaseDepartmentDAO extends _RootDAO {

	private static DepartmentDAO sInstance;

	public static DepartmentDAO getInstance () {
		if (sInstance == null) sInstance = new DepartmentDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Department.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Department get(Long uniqueId) {
		return (Department) get(getReferenceClass(), uniqueId);
	}

	public Department get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Department) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Department load(Long uniqueId) {
		return (Department) load(getReferenceClass(), uniqueId);
	}

	public Department load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Department) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Department loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Department department = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(department)) Hibernate.initialize(department);
		return department;
	}

	public void save(Department department) {
		save((Object) department);
	}

	public void save(Department department, org.hibernate.Session hibSession) {
		save((Object) department, hibSession);
	}

	public void saveOrUpdate(Department department) {
		saveOrUpdate((Object) department);
	}

	public void saveOrUpdate(Department department, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) department, hibSession);
	}


	public void update(Department department) {
		update((Object) department);
	}

	public void update(Department department, org.hibernate.Session hibSession) {
		update((Object) department, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Department department) {
		delete((Object) department);
	}

	public void delete(Department department, org.hibernate.Session hibSession) {
		delete((Object) department, hibSession);
	}

	public void refresh(Department department, org.hibernate.Session hibSession) {
		refresh((Object) department, hibSession);
	}

	public List<Department> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Department").list();
	}

	public List<Department> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from Department x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	public List<Department> findByStatusType(org.hibernate.Session hibSession, Long statusTypeId) {
		return hibSession.createQuery("from Department x where x.statusType.uniqueId = :statusTypeId").setLong("statusTypeId", statusTypeId).list();
	}

	public List<Department> findBySolverGroup(org.hibernate.Session hibSession, Long solverGroupId) {
		return hibSession.createQuery("from Department x where x.solverGroup.uniqueId = :solverGroupId").setLong("solverGroupId", solverGroupId).list();
	}
}
