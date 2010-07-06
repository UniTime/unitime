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

import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.NonUniversityLocationDAO;

public abstract class BaseNonUniversityLocationDAO extends _RootDAO {

	private static NonUniversityLocationDAO sInstance;

	public static NonUniversityLocationDAO getInstance () {
		if (sInstance == null) sInstance = new NonUniversityLocationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return NonUniversityLocation.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public NonUniversityLocation get(Long uniqueId) {
		return (NonUniversityLocation) get(getReferenceClass(), uniqueId);
	}

	public NonUniversityLocation get(Long uniqueId, org.hibernate.Session hibSession) {
		return (NonUniversityLocation) get(getReferenceClass(), uniqueId, hibSession);
	}

	public NonUniversityLocation load(Long uniqueId) {
		return (NonUniversityLocation) load(getReferenceClass(), uniqueId);
	}

	public NonUniversityLocation load(Long uniqueId, org.hibernate.Session hibSession) {
		return (NonUniversityLocation) load(getReferenceClass(), uniqueId, hibSession);
	}

	public NonUniversityLocation loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		NonUniversityLocation nonUniversityLocation = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(nonUniversityLocation)) Hibernate.initialize(nonUniversityLocation);
		return nonUniversityLocation;
	}

	public void save(NonUniversityLocation nonUniversityLocation) {
		save((Object) nonUniversityLocation);
	}

	public void save(NonUniversityLocation nonUniversityLocation, org.hibernate.Session hibSession) {
		save((Object) nonUniversityLocation, hibSession);
	}

	public void saveOrUpdate(NonUniversityLocation nonUniversityLocation) {
		saveOrUpdate((Object) nonUniversityLocation);
	}

	public void saveOrUpdate(NonUniversityLocation nonUniversityLocation, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) nonUniversityLocation, hibSession);
	}


	public void update(NonUniversityLocation nonUniversityLocation) {
		update((Object) nonUniversityLocation);
	}

	public void update(NonUniversityLocation nonUniversityLocation, org.hibernate.Session hibSession) {
		update((Object) nonUniversityLocation, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(NonUniversityLocation nonUniversityLocation) {
		delete((Object) nonUniversityLocation);
	}

	public void delete(NonUniversityLocation nonUniversityLocation, org.hibernate.Session hibSession) {
		delete((Object) nonUniversityLocation, hibSession);
	}

	public void refresh(NonUniversityLocation nonUniversityLocation, org.hibernate.Session hibSession) {
		refresh((Object) nonUniversityLocation, hibSession);
	}

	public List<NonUniversityLocation> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from NonUniversityLocation").list();
	}

	public List<NonUniversityLocation> findByRoomType(org.hibernate.Session hibSession, Long roomTypeId) {
		return hibSession.createQuery("from NonUniversityLocation x where x.roomType.uniqueId = :roomTypeId").setLong("roomTypeId", roomTypeId).list();
	}
}
