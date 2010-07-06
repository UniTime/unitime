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

import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.BuildingDAO;

public abstract class BaseBuildingDAO extends _RootDAO {

	private static BuildingDAO sInstance;

	public static BuildingDAO getInstance () {
		if (sInstance == null) sInstance = new BuildingDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Building.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Building get(Long uniqueId) {
		return (Building) get(getReferenceClass(), uniqueId);
	}

	public Building get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Building) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Building load(Long uniqueId) {
		return (Building) load(getReferenceClass(), uniqueId);
	}

	public Building load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Building) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Building loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Building building = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(building)) Hibernate.initialize(building);
		return building;
	}

	public void save(Building building) {
		save((Object) building);
	}

	public void save(Building building, org.hibernate.Session hibSession) {
		save((Object) building, hibSession);
	}

	public void saveOrUpdate(Building building) {
		saveOrUpdate((Object) building);
	}

	public void saveOrUpdate(Building building, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) building, hibSession);
	}


	public void update(Building building) {
		update((Object) building);
	}

	public void update(Building building, org.hibernate.Session hibSession) {
		update((Object) building, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Building building) {
		delete((Object) building);
	}

	public void delete(Building building, org.hibernate.Session hibSession) {
		delete((Object) building, hibSession);
	}

	public void refresh(Building building, org.hibernate.Session hibSession) {
		refresh((Object) building, hibSession);
	}

	public List<Building> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Building").list();
	}

	public List<Building> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from Building x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
