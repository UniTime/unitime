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

import org.unitime.timetable.model.FinalExamEvent;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.FinalExamEventDAO;

public abstract class BaseFinalExamEventDAO extends _RootDAO {

	private static FinalExamEventDAO sInstance;

	public static FinalExamEventDAO getInstance () {
		if (sInstance == null) sInstance = new FinalExamEventDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return FinalExamEvent.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public FinalExamEvent get(Long uniqueId) {
		return (FinalExamEvent) get(getReferenceClass(), uniqueId);
	}

	public FinalExamEvent get(Long uniqueId, org.hibernate.Session hibSession) {
		return (FinalExamEvent) get(getReferenceClass(), uniqueId, hibSession);
	}

	public FinalExamEvent load(Long uniqueId) {
		return (FinalExamEvent) load(getReferenceClass(), uniqueId);
	}

	public FinalExamEvent load(Long uniqueId, org.hibernate.Session hibSession) {
		return (FinalExamEvent) load(getReferenceClass(), uniqueId, hibSession);
	}

	public FinalExamEvent loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		FinalExamEvent finalExamEvent = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(finalExamEvent)) Hibernate.initialize(finalExamEvent);
		return finalExamEvent;
	}

	public void save(FinalExamEvent finalExamEvent) {
		save((Object) finalExamEvent);
	}

	public void save(FinalExamEvent finalExamEvent, org.hibernate.Session hibSession) {
		save((Object) finalExamEvent, hibSession);
	}

	public void saveOrUpdate(FinalExamEvent finalExamEvent) {
		saveOrUpdate((Object) finalExamEvent);
	}

	public void saveOrUpdate(FinalExamEvent finalExamEvent, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) finalExamEvent, hibSession);
	}


	public void update(FinalExamEvent finalExamEvent) {
		update((Object) finalExamEvent);
	}

	public void update(FinalExamEvent finalExamEvent, org.hibernate.Session hibSession) {
		update((Object) finalExamEvent, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(FinalExamEvent finalExamEvent) {
		delete((Object) finalExamEvent);
	}

	public void delete(FinalExamEvent finalExamEvent, org.hibernate.Session hibSession) {
		delete((Object) finalExamEvent, hibSession);
	}

	public void refresh(FinalExamEvent finalExamEvent, org.hibernate.Session hibSession) {
		refresh((Object) finalExamEvent, hibSession);
	}

	public List<FinalExamEvent> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from FinalExamEvent").list();
	}
}
