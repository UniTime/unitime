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

import org.unitime.timetable.model.ManagerSettings;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ManagerSettingsDAO;

public abstract class BaseManagerSettingsDAO extends _RootDAO {

	private static ManagerSettingsDAO sInstance;

	public static ManagerSettingsDAO getInstance () {
		if (sInstance == null) sInstance = new ManagerSettingsDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ManagerSettings.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ManagerSettings get(Long uniqueId) {
		return (ManagerSettings) get(getReferenceClass(), uniqueId);
	}

	public ManagerSettings get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ManagerSettings) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ManagerSettings load(Long uniqueId) {
		return (ManagerSettings) load(getReferenceClass(), uniqueId);
	}

	public ManagerSettings load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ManagerSettings) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ManagerSettings loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ManagerSettings managerSettings = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(managerSettings)) Hibernate.initialize(managerSettings);
		return managerSettings;
	}

	public void save(ManagerSettings managerSettings) {
		save((Object) managerSettings);
	}

	public void save(ManagerSettings managerSettings, org.hibernate.Session hibSession) {
		save((Object) managerSettings, hibSession);
	}

	public void saveOrUpdate(ManagerSettings managerSettings) {
		saveOrUpdate((Object) managerSettings);
	}

	public void saveOrUpdate(ManagerSettings managerSettings, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) managerSettings, hibSession);
	}


	public void update(ManagerSettings managerSettings) {
		update((Object) managerSettings);
	}

	public void update(ManagerSettings managerSettings, org.hibernate.Session hibSession) {
		update((Object) managerSettings, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ManagerSettings managerSettings) {
		delete((Object) managerSettings);
	}

	public void delete(ManagerSettings managerSettings, org.hibernate.Session hibSession) {
		delete((Object) managerSettings, hibSession);
	}

	public void refresh(ManagerSettings managerSettings, org.hibernate.Session hibSession) {
		refresh((Object) managerSettings, hibSession);
	}

	public List<ManagerSettings> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ManagerSettings").list();
	}

	public List<ManagerSettings> findByKey(org.hibernate.Session hibSession, Long keyId) {
		return hibSession.createQuery("from ManagerSettings x where x.key.uniqueId = :keyId").setLong("keyId", keyId).list();
	}

	public List<ManagerSettings> findByManager(org.hibernate.Session hibSession, Long managerId) {
		return hibSession.createQuery("from ManagerSettings x where x.manager.uniqueId = :managerId").setLong("managerId", managerId).list();
	}
}
