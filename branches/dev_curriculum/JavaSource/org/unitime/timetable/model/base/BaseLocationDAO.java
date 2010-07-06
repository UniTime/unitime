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

import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.LocationDAO;

public abstract class BaseLocationDAO extends _RootDAO {

	private static LocationDAO sInstance;

	public static LocationDAO getInstance () {
		if (sInstance == null) sInstance = new LocationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Location.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Location get(Long uniqueId) {
		return (Location) get(getReferenceClass(), uniqueId);
	}

	public Location get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Location) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Location load(Long uniqueId) {
		return (Location) load(getReferenceClass(), uniqueId);
	}

	public Location load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Location) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Location loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Location location = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(location)) Hibernate.initialize(location);
		return location;
	}

	public void save(Location location) {
		save((Object) location);
	}

	public void save(Location location, org.hibernate.Session hibSession) {
		save((Object) location, hibSession);
	}

	public void saveOrUpdate(Location location) {
		saveOrUpdate((Object) location);
	}

	public void saveOrUpdate(Location location, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) location, hibSession);
	}


	public void update(Location location) {
		update((Object) location);
	}

	public void update(Location location, org.hibernate.Session hibSession) {
		update((Object) location, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Location location) {
		delete((Object) location);
	}

	public void delete(Location location, org.hibernate.Session hibSession) {
		delete((Object) location, hibSession);
	}

	public void refresh(Location location, org.hibernate.Session hibSession) {
		refresh((Object) location, hibSession);
	}

	public List<Location> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from Location x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
