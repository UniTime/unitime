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

import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.MeetingDAO;

public abstract class BaseMeetingDAO extends _RootDAO {

	private static MeetingDAO sInstance;

	public static MeetingDAO getInstance () {
		if (sInstance == null) sInstance = new MeetingDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Meeting.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Meeting get(Long uniqueId) {
		return (Meeting) get(getReferenceClass(), uniqueId);
	}

	public Meeting get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Meeting) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Meeting load(Long uniqueId) {
		return (Meeting) load(getReferenceClass(), uniqueId);
	}

	public Meeting load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Meeting) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Meeting loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Meeting meeting = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(meeting)) Hibernate.initialize(meeting);
		return meeting;
	}

	public void save(Meeting meeting) {
		save((Object) meeting);
	}

	public void save(Meeting meeting, org.hibernate.Session hibSession) {
		save((Object) meeting, hibSession);
	}

	public void saveOrUpdate(Meeting meeting) {
		saveOrUpdate((Object) meeting);
	}

	public void saveOrUpdate(Meeting meeting, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) meeting, hibSession);
	}


	public void update(Meeting meeting) {
		update((Object) meeting);
	}

	public void update(Meeting meeting, org.hibernate.Session hibSession) {
		update((Object) meeting, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Meeting meeting) {
		delete((Object) meeting);
	}

	public void delete(Meeting meeting, org.hibernate.Session hibSession) {
		delete((Object) meeting, hibSession);
	}

	public void refresh(Meeting meeting, org.hibernate.Session hibSession) {
		refresh((Object) meeting, hibSession);
	}

	public List<Meeting> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Meeting").list();
	}

	public List<Meeting> findByEvent(org.hibernate.Session hibSession, Long eventId) {
		return hibSession.createQuery("from Meeting x where x.event.uniqueId = :eventId").setLong("eventId", eventId).list();
	}
}
