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

import org.unitime.timetable.model.EventNote;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.EventNoteDAO;

public abstract class BaseEventNoteDAO extends _RootDAO {

	private static EventNoteDAO sInstance;

	public static EventNoteDAO getInstance () {
		if (sInstance == null) sInstance = new EventNoteDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return EventNote.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public EventNote get(Long uniqueId) {
		return (EventNote) get(getReferenceClass(), uniqueId);
	}

	public EventNote get(Long uniqueId, org.hibernate.Session hibSession) {
		return (EventNote) get(getReferenceClass(), uniqueId, hibSession);
	}

	public EventNote load(Long uniqueId) {
		return (EventNote) load(getReferenceClass(), uniqueId);
	}

	public EventNote load(Long uniqueId, org.hibernate.Session hibSession) {
		return (EventNote) load(getReferenceClass(), uniqueId, hibSession);
	}

	public EventNote loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		EventNote eventNote = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(eventNote)) Hibernate.initialize(eventNote);
		return eventNote;
	}

	public void save(EventNote eventNote) {
		save((Object) eventNote);
	}

	public void save(EventNote eventNote, org.hibernate.Session hibSession) {
		save((Object) eventNote, hibSession);
	}

	public void saveOrUpdate(EventNote eventNote) {
		saveOrUpdate((Object) eventNote);
	}

	public void saveOrUpdate(EventNote eventNote, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) eventNote, hibSession);
	}


	public void update(EventNote eventNote) {
		update((Object) eventNote);
	}

	public void update(EventNote eventNote, org.hibernate.Session hibSession) {
		update((Object) eventNote, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(EventNote eventNote) {
		delete((Object) eventNote);
	}

	public void delete(EventNote eventNote, org.hibernate.Session hibSession) {
		delete((Object) eventNote, hibSession);
	}

	public void refresh(EventNote eventNote, org.hibernate.Session hibSession) {
		refresh((Object) eventNote, hibSession);
	}

	public List<EventNote> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from EventNote").list();
	}

	public List<EventNote> findByEvent(org.hibernate.Session hibSession, Long eventId) {
		return hibSession.createQuery("from EventNote x where x.event.uniqueId = :eventId").setLong("eventId", eventId).list();
	}
}
