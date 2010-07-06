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

import org.unitime.timetable.model.BuildingAbbreviationHistory;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.BuildingAbbreviationHistoryDAO;

public abstract class BaseBuildingAbbreviationHistoryDAO extends _RootDAO {

	private static BuildingAbbreviationHistoryDAO sInstance;

	public static BuildingAbbreviationHistoryDAO getInstance () {
		if (sInstance == null) sInstance = new BuildingAbbreviationHistoryDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return BuildingAbbreviationHistory.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public BuildingAbbreviationHistory get(Long uniqueId) {
		return (BuildingAbbreviationHistory) get(getReferenceClass(), uniqueId);
	}

	public BuildingAbbreviationHistory get(Long uniqueId, org.hibernate.Session hibSession) {
		return (BuildingAbbreviationHistory) get(getReferenceClass(), uniqueId, hibSession);
	}

	public BuildingAbbreviationHistory load(Long uniqueId) {
		return (BuildingAbbreviationHistory) load(getReferenceClass(), uniqueId);
	}

	public BuildingAbbreviationHistory load(Long uniqueId, org.hibernate.Session hibSession) {
		return (BuildingAbbreviationHistory) load(getReferenceClass(), uniqueId, hibSession);
	}

	public BuildingAbbreviationHistory loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		BuildingAbbreviationHistory buildingAbbreviationHistory = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(buildingAbbreviationHistory)) Hibernate.initialize(buildingAbbreviationHistory);
		return buildingAbbreviationHistory;
	}

	public void save(BuildingAbbreviationHistory buildingAbbreviationHistory) {
		save((Object) buildingAbbreviationHistory);
	}

	public void save(BuildingAbbreviationHistory buildingAbbreviationHistory, org.hibernate.Session hibSession) {
		save((Object) buildingAbbreviationHistory, hibSession);
	}

	public void saveOrUpdate(BuildingAbbreviationHistory buildingAbbreviationHistory) {
		saveOrUpdate((Object) buildingAbbreviationHistory);
	}

	public void saveOrUpdate(BuildingAbbreviationHistory buildingAbbreviationHistory, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) buildingAbbreviationHistory, hibSession);
	}


	public void update(BuildingAbbreviationHistory buildingAbbreviationHistory) {
		update((Object) buildingAbbreviationHistory);
	}

	public void update(BuildingAbbreviationHistory buildingAbbreviationHistory, org.hibernate.Session hibSession) {
		update((Object) buildingAbbreviationHistory, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(BuildingAbbreviationHistory buildingAbbreviationHistory) {
		delete((Object) buildingAbbreviationHistory);
	}

	public void delete(BuildingAbbreviationHistory buildingAbbreviationHistory, org.hibernate.Session hibSession) {
		delete((Object) buildingAbbreviationHistory, hibSession);
	}

	public void refresh(BuildingAbbreviationHistory buildingAbbreviationHistory, org.hibernate.Session hibSession) {
		refresh((Object) buildingAbbreviationHistory, hibSession);
	}

	public List<BuildingAbbreviationHistory> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from BuildingAbbreviationHistory").list();
	}
}
