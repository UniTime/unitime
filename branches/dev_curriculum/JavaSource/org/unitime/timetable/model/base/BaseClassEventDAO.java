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

import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ClassEventDAO;

public abstract class BaseClassEventDAO extends _RootDAO {

	private static ClassEventDAO sInstance;

	public static ClassEventDAO getInstance () {
		if (sInstance == null) sInstance = new ClassEventDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ClassEvent.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ClassEvent get(Long uniqueId) {
		return (ClassEvent) get(getReferenceClass(), uniqueId);
	}

	public ClassEvent get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ClassEvent) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ClassEvent load(Long uniqueId) {
		return (ClassEvent) load(getReferenceClass(), uniqueId);
	}

	public ClassEvent load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ClassEvent) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ClassEvent loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ClassEvent classEvent = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(classEvent)) Hibernate.initialize(classEvent);
		return classEvent;
	}

	public void save(ClassEvent classEvent) {
		save((Object) classEvent);
	}

	public void save(ClassEvent classEvent, org.hibernate.Session hibSession) {
		save((Object) classEvent, hibSession);
	}

	public void saveOrUpdate(ClassEvent classEvent) {
		saveOrUpdate((Object) classEvent);
	}

	public void saveOrUpdate(ClassEvent classEvent, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) classEvent, hibSession);
	}


	public void update(ClassEvent classEvent) {
		update((Object) classEvent);
	}

	public void update(ClassEvent classEvent, org.hibernate.Session hibSession) {
		update((Object) classEvent, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ClassEvent classEvent) {
		delete((Object) classEvent);
	}

	public void delete(ClassEvent classEvent, org.hibernate.Session hibSession) {
		delete((Object) classEvent, hibSession);
	}

	public void refresh(ClassEvent classEvent, org.hibernate.Session hibSession) {
		refresh((Object) classEvent, hibSession);
	}

	public List<ClassEvent> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ClassEvent").list();
	}

	public List<ClassEvent> findByClazz(org.hibernate.Session hibSession, Long clazzId) {
		return hibSession.createQuery("from ClassEvent x where x.clazz.uniqueId = :clazzId").setLong("clazzId", clazzId).list();
	}
}
