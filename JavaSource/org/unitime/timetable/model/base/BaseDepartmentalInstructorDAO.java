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

import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;

public abstract class BaseDepartmentalInstructorDAO extends _RootDAO {

	private static DepartmentalInstructorDAO sInstance;

	public static DepartmentalInstructorDAO getInstance () {
		if (sInstance == null) sInstance = new DepartmentalInstructorDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return DepartmentalInstructor.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public DepartmentalInstructor get(Long uniqueId) {
		return (DepartmentalInstructor) get(getReferenceClass(), uniqueId);
	}

	public DepartmentalInstructor get(Long uniqueId, org.hibernate.Session hibSession) {
		return (DepartmentalInstructor) get(getReferenceClass(), uniqueId, hibSession);
	}

	public DepartmentalInstructor load(Long uniqueId) {
		return (DepartmentalInstructor) load(getReferenceClass(), uniqueId);
	}

	public DepartmentalInstructor load(Long uniqueId, org.hibernate.Session hibSession) {
		return (DepartmentalInstructor) load(getReferenceClass(), uniqueId, hibSession);
	}

	public DepartmentalInstructor loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		DepartmentalInstructor departmentalInstructor = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(departmentalInstructor)) Hibernate.initialize(departmentalInstructor);
		return departmentalInstructor;
	}

	public void save(DepartmentalInstructor departmentalInstructor) {
		save((Object) departmentalInstructor);
	}

	public void save(DepartmentalInstructor departmentalInstructor, org.hibernate.Session hibSession) {
		save((Object) departmentalInstructor, hibSession);
	}

	public void saveOrUpdate(DepartmentalInstructor departmentalInstructor) {
		saveOrUpdate((Object) departmentalInstructor);
	}

	public void saveOrUpdate(DepartmentalInstructor departmentalInstructor, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) departmentalInstructor, hibSession);
	}


	public void update(DepartmentalInstructor departmentalInstructor) {
		update((Object) departmentalInstructor);
	}

	public void update(DepartmentalInstructor departmentalInstructor, org.hibernate.Session hibSession) {
		update((Object) departmentalInstructor, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(DepartmentalInstructor departmentalInstructor) {
		delete((Object) departmentalInstructor);
	}

	public void delete(DepartmentalInstructor departmentalInstructor, org.hibernate.Session hibSession) {
		delete((Object) departmentalInstructor, hibSession);
	}

	public void refresh(DepartmentalInstructor departmentalInstructor, org.hibernate.Session hibSession) {
		refresh((Object) departmentalInstructor, hibSession);
	}

	public List<DepartmentalInstructor> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from DepartmentalInstructor").list();
	}

	public List<DepartmentalInstructor> findByPositionType(org.hibernate.Session hibSession, Long positionTypeId) {
		return hibSession.createQuery("from DepartmentalInstructor x where x.positionType.uniqueId = :positionTypeId").setLong("positionTypeId", positionTypeId).list();
	}

	public List<DepartmentalInstructor> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from DepartmentalInstructor x where x.department.uniqueId = :departmentId").setLong("departmentId", departmentId).list();
	}
}
