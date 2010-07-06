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

import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExternalBuildingDAO;

public abstract class BaseExternalBuildingDAO extends _RootDAO {

	private static ExternalBuildingDAO sInstance;

	public static ExternalBuildingDAO getInstance () {
		if (sInstance == null) sInstance = new ExternalBuildingDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ExternalBuilding.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ExternalBuilding get(Long uniqueId) {
		return (ExternalBuilding) get(getReferenceClass(), uniqueId);
	}

	public ExternalBuilding get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExternalBuilding) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ExternalBuilding load(Long uniqueId) {
		return (ExternalBuilding) load(getReferenceClass(), uniqueId);
	}

	public ExternalBuilding load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ExternalBuilding) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ExternalBuilding loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ExternalBuilding externalBuilding = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(externalBuilding)) Hibernate.initialize(externalBuilding);
		return externalBuilding;
	}

	public void save(ExternalBuilding externalBuilding) {
		save((Object) externalBuilding);
	}

	public void save(ExternalBuilding externalBuilding, org.hibernate.Session hibSession) {
		save((Object) externalBuilding, hibSession);
	}

	public void saveOrUpdate(ExternalBuilding externalBuilding) {
		saveOrUpdate((Object) externalBuilding);
	}

	public void saveOrUpdate(ExternalBuilding externalBuilding, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) externalBuilding, hibSession);
	}


	public void update(ExternalBuilding externalBuilding) {
		update((Object) externalBuilding);
	}

	public void update(ExternalBuilding externalBuilding, org.hibernate.Session hibSession) {
		update((Object) externalBuilding, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ExternalBuilding externalBuilding) {
		delete((Object) externalBuilding);
	}

	public void delete(ExternalBuilding externalBuilding, org.hibernate.Session hibSession) {
		delete((Object) externalBuilding, hibSession);
	}

	public void refresh(ExternalBuilding externalBuilding, org.hibernate.Session hibSession) {
		refresh((Object) externalBuilding, hibSession);
	}

	public List<ExternalBuilding> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ExternalBuilding").list();
	}

	public List<ExternalBuilding> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from ExternalBuilding x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
