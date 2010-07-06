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

import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ManagerRoleDAO;

public abstract class BaseManagerRoleDAO extends _RootDAO {

	private static ManagerRoleDAO sInstance;

	public static ManagerRoleDAO getInstance () {
		if (sInstance == null) sInstance = new ManagerRoleDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ManagerRole.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ManagerRole get(Long uniqueId) {
		return (ManagerRole) get(getReferenceClass(), uniqueId);
	}

	public ManagerRole get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ManagerRole) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ManagerRole load(Long uniqueId) {
		return (ManagerRole) load(getReferenceClass(), uniqueId);
	}

	public ManagerRole load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ManagerRole) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ManagerRole loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ManagerRole managerRole = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(managerRole)) Hibernate.initialize(managerRole);
		return managerRole;
	}

	public void save(ManagerRole managerRole) {
		save((Object) managerRole);
	}

	public void save(ManagerRole managerRole, org.hibernate.Session hibSession) {
		save((Object) managerRole, hibSession);
	}

	public void saveOrUpdate(ManagerRole managerRole) {
		saveOrUpdate((Object) managerRole);
	}

	public void saveOrUpdate(ManagerRole managerRole, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) managerRole, hibSession);
	}


	public void update(ManagerRole managerRole) {
		update((Object) managerRole);
	}

	public void update(ManagerRole managerRole, org.hibernate.Session hibSession) {
		update((Object) managerRole, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ManagerRole managerRole) {
		delete((Object) managerRole);
	}

	public void delete(ManagerRole managerRole, org.hibernate.Session hibSession) {
		delete((Object) managerRole, hibSession);
	}

	public void refresh(ManagerRole managerRole, org.hibernate.Session hibSession) {
		refresh((Object) managerRole, hibSession);
	}

	public List<ManagerRole> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ManagerRole").list();
	}

	public List<ManagerRole> findByRole(org.hibernate.Session hibSession, Long roleId) {
		return hibSession.createQuery("from ManagerRole x where x.role.roleId = :roleId").setLong("roleId", roleId).list();
	}

	public List<ManagerRole> findByTimetableManager(org.hibernate.Session hibSession, Long timetableManagerId) {
		return hibSession.createQuery("from ManagerRole x where x.timetableManager.uniqueId = :timetableManagerId").setLong("timetableManagerId", timetableManagerId).list();
	}
}
