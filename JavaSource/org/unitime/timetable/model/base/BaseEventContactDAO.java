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

import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.EventContactDAO;

public abstract class BaseEventContactDAO extends _RootDAO {

	private static EventContactDAO sInstance;

	public static EventContactDAO getInstance () {
		if (sInstance == null) sInstance = new EventContactDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return EventContact.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public EventContact get(Long uniqueId) {
		return (EventContact) get(getReferenceClass(), uniqueId);
	}

	public EventContact get(Long uniqueId, org.hibernate.Session hibSession) {
		return (EventContact) get(getReferenceClass(), uniqueId, hibSession);
	}

	public EventContact load(Long uniqueId) {
		return (EventContact) load(getReferenceClass(), uniqueId);
	}

	public EventContact load(Long uniqueId, org.hibernate.Session hibSession) {
		return (EventContact) load(getReferenceClass(), uniqueId, hibSession);
	}

	public EventContact loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		EventContact eventContact = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(eventContact)) Hibernate.initialize(eventContact);
		return eventContact;
	}

	public void save(EventContact eventContact) {
		save((Object) eventContact);
	}

	public void save(EventContact eventContact, org.hibernate.Session hibSession) {
		save((Object) eventContact, hibSession);
	}

	public void saveOrUpdate(EventContact eventContact) {
		saveOrUpdate((Object) eventContact);
	}

	public void saveOrUpdate(EventContact eventContact, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) eventContact, hibSession);
	}


	public void update(EventContact eventContact) {
		update((Object) eventContact);
	}

	public void update(EventContact eventContact, org.hibernate.Session hibSession) {
		update((Object) eventContact, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(EventContact eventContact) {
		delete((Object) eventContact);
	}

	public void delete(EventContact eventContact, org.hibernate.Session hibSession) {
		delete((Object) eventContact, hibSession);
	}

	public void refresh(EventContact eventContact, org.hibernate.Session hibSession) {
		refresh((Object) eventContact, hibSession);
	}

	public List<EventContact> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from EventContact").list();
	}
}
