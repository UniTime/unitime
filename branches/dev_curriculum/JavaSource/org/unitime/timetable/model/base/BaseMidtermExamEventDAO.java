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

import org.unitime.timetable.model.MidtermExamEvent;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.MidtermExamEventDAO;

public abstract class BaseMidtermExamEventDAO extends _RootDAO {

	private static MidtermExamEventDAO sInstance;

	public static MidtermExamEventDAO getInstance () {
		if (sInstance == null) sInstance = new MidtermExamEventDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return MidtermExamEvent.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public MidtermExamEvent get(Long uniqueId) {
		return (MidtermExamEvent) get(getReferenceClass(), uniqueId);
	}

	public MidtermExamEvent get(Long uniqueId, org.hibernate.Session hibSession) {
		return (MidtermExamEvent) get(getReferenceClass(), uniqueId, hibSession);
	}

	public MidtermExamEvent load(Long uniqueId) {
		return (MidtermExamEvent) load(getReferenceClass(), uniqueId);
	}

	public MidtermExamEvent load(Long uniqueId, org.hibernate.Session hibSession) {
		return (MidtermExamEvent) load(getReferenceClass(), uniqueId, hibSession);
	}

	public MidtermExamEvent loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		MidtermExamEvent midtermExamEvent = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(midtermExamEvent)) Hibernate.initialize(midtermExamEvent);
		return midtermExamEvent;
	}

	public void save(MidtermExamEvent midtermExamEvent) {
		save((Object) midtermExamEvent);
	}

	public void save(MidtermExamEvent midtermExamEvent, org.hibernate.Session hibSession) {
		save((Object) midtermExamEvent, hibSession);
	}

	public void saveOrUpdate(MidtermExamEvent midtermExamEvent) {
		saveOrUpdate((Object) midtermExamEvent);
	}

	public void saveOrUpdate(MidtermExamEvent midtermExamEvent, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) midtermExamEvent, hibSession);
	}


	public void update(MidtermExamEvent midtermExamEvent) {
		update((Object) midtermExamEvent);
	}

	public void update(MidtermExamEvent midtermExamEvent, org.hibernate.Session hibSession) {
		update((Object) midtermExamEvent, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(MidtermExamEvent midtermExamEvent) {
		delete((Object) midtermExamEvent);
	}

	public void delete(MidtermExamEvent midtermExamEvent, org.hibernate.Session hibSession) {
		delete((Object) midtermExamEvent, hibSession);
	}

	public void refresh(MidtermExamEvent midtermExamEvent, org.hibernate.Session hibSession) {
		refresh((Object) midtermExamEvent, hibSession);
	}

	public List<MidtermExamEvent> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from MidtermExamEvent").list();
	}
}
