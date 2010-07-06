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

import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.EventDAO;

public abstract class BaseEventDAO extends _RootDAO {

	private static EventDAO sInstance;

	public static EventDAO getInstance () {
		if (sInstance == null) sInstance = new EventDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Event.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Event get(Long uniqueId) {
		return (Event) get(getReferenceClass(), uniqueId);
	}

	public Event get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Event) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Event load(Long uniqueId) {
		return (Event) load(getReferenceClass(), uniqueId);
	}

	public Event load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Event) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Event loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Event event = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(event)) Hibernate.initialize(event);
		return event;
	}

	public void save(Event event) {
		save((Object) event);
	}

	public void save(Event event, org.hibernate.Session hibSession) {
		save((Object) event, hibSession);
	}

	public void saveOrUpdate(Event event) {
		saveOrUpdate((Object) event);
	}

	public void saveOrUpdate(Event event, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) event, hibSession);
	}


	public void update(Event event) {
		update((Object) event);
	}

	public void update(Event event, org.hibernate.Session hibSession) {
		update((Object) event, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Event event) {
		delete((Object) event);
	}

	public void delete(Event event, org.hibernate.Session hibSession) {
		delete((Object) event, hibSession);
	}

	public void refresh(Event event, org.hibernate.Session hibSession) {
		refresh((Object) event, hibSession);
	}

	public List<Event> findByMainContact(org.hibernate.Session hibSession, Long mainContactId) {
		return hibSession.createQuery("from Event x where x.mainContact.uniqueId = :mainContactId").setLong("mainContactId", mainContactId).list();
	}

	public List<Event> findBySponsoringOrganization(org.hibernate.Session hibSession, Long sponsoringOrganizationId) {
		return hibSession.createQuery("from Event x where x.sponsoringOrganization.uniqueId = :sponsoringOrganizationId").setLong("sponsoringOrganizationId", sponsoringOrganizationId).list();
	}
}
