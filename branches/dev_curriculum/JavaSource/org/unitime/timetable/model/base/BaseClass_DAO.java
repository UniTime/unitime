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

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.Class_DAO;

public abstract class BaseClass_DAO extends _RootDAO {

	private static Class_DAO sInstance;

	public static Class_DAO getInstance () {
		if (sInstance == null) sInstance = new Class_DAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Class_.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Class_ get(Long uniqueId) {
		return (Class_) get(getReferenceClass(), uniqueId);
	}

	public Class_ get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Class_) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Class_ load(Long uniqueId) {
		return (Class_) load(getReferenceClass(), uniqueId);
	}

	public Class_ load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Class_) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Class_ loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Class_ class_ = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(class_)) Hibernate.initialize(class_);
		return class_;
	}

	public void save(Class_ class_) {
		save((Object) class_);
	}

	public void save(Class_ class_, org.hibernate.Session hibSession) {
		save((Object) class_, hibSession);
	}

	public void saveOrUpdate(Class_ class_) {
		saveOrUpdate((Object) class_);
	}

	public void saveOrUpdate(Class_ class_, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) class_, hibSession);
	}


	public void update(Class_ class_) {
		update((Object) class_);
	}

	public void update(Class_ class_, org.hibernate.Session hibSession) {
		update((Object) class_, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Class_ class_) {
		delete((Object) class_);
	}

	public void delete(Class_ class_, org.hibernate.Session hibSession) {
		delete((Object) class_, hibSession);
	}

	public void refresh(Class_ class_, org.hibernate.Session hibSession) {
		refresh((Object) class_, hibSession);
	}

	public List<Class_> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Class_").list();
	}

	public List<Class_> findByManagingDept(org.hibernate.Session hibSession, Long managingDeptId) {
		return hibSession.createQuery("from Class_ x where x.managingDept.uniqueId = :managingDeptId").setLong("managingDeptId", managingDeptId).list();
	}

	public List<Class_> findBySchedulingSubpart(org.hibernate.Session hibSession, Long schedulingSubpartId) {
		return hibSession.createQuery("from Class_ x where x.schedulingSubpart.uniqueId = :schedulingSubpartId").setLong("schedulingSubpartId", schedulingSubpartId).list();
	}

	public List<Class_> findByParentClass(org.hibernate.Session hibSession, Long parentClassId) {
		return hibSession.createQuery("from Class_ x where x.parentClass.uniqueId = :parentClassId").setLong("parentClassId", parentClassId).list();
	}

	public List<Class_> findByDatePattern(org.hibernate.Session hibSession, Long datePatternId) {
		return hibSession.createQuery("from Class_ x where x.datePattern.uniqueId = :datePatternId").setLong("datePatternId", datePatternId).list();
	}
}
