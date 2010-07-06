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

import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.BuildingPrefDAO;

public abstract class BaseBuildingPrefDAO extends _RootDAO {

	private static BuildingPrefDAO sInstance;

	public static BuildingPrefDAO getInstance () {
		if (sInstance == null) sInstance = new BuildingPrefDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return BuildingPref.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public BuildingPref get(Long uniqueId) {
		return (BuildingPref) get(getReferenceClass(), uniqueId);
	}

	public BuildingPref get(Long uniqueId, org.hibernate.Session hibSession) {
		return (BuildingPref) get(getReferenceClass(), uniqueId, hibSession);
	}

	public BuildingPref load(Long uniqueId) {
		return (BuildingPref) load(getReferenceClass(), uniqueId);
	}

	public BuildingPref load(Long uniqueId, org.hibernate.Session hibSession) {
		return (BuildingPref) load(getReferenceClass(), uniqueId, hibSession);
	}

	public BuildingPref loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		BuildingPref buildingPref = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(buildingPref)) Hibernate.initialize(buildingPref);
		return buildingPref;
	}

	public void save(BuildingPref buildingPref) {
		save((Object) buildingPref);
	}

	public void save(BuildingPref buildingPref, org.hibernate.Session hibSession) {
		save((Object) buildingPref, hibSession);
	}

	public void saveOrUpdate(BuildingPref buildingPref) {
		saveOrUpdate((Object) buildingPref);
	}

	public void saveOrUpdate(BuildingPref buildingPref, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) buildingPref, hibSession);
	}


	public void update(BuildingPref buildingPref) {
		update((Object) buildingPref);
	}

	public void update(BuildingPref buildingPref, org.hibernate.Session hibSession) {
		update((Object) buildingPref, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(BuildingPref buildingPref) {
		delete((Object) buildingPref);
	}

	public void delete(BuildingPref buildingPref, org.hibernate.Session hibSession) {
		delete((Object) buildingPref, hibSession);
	}

	public void refresh(BuildingPref buildingPref, org.hibernate.Session hibSession) {
		refresh((Object) buildingPref, hibSession);
	}

	public List<BuildingPref> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from BuildingPref").list();
	}

	public List<BuildingPref> findByBuilding(org.hibernate.Session hibSession, Long buildingId) {
		return hibSession.createQuery("from BuildingPref x where x.building.uniqueId = :buildingId").setLong("buildingId", buildingId).list();
	}
}
