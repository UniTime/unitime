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

import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RolesDAO;

public abstract class BaseRolesDAO extends _RootDAO {

	private static RolesDAO sInstance;

	public static RolesDAO getInstance () {
		if (sInstance == null) sInstance = new RolesDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Roles.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Roles get(Long roleId) {
		return (Roles) get(getReferenceClass(), roleId);
	}

	public Roles get(Long roleId, org.hibernate.Session hibSession) {
		return (Roles) get(getReferenceClass(), roleId, hibSession);
	}

	public Roles load(Long roleId) {
		return (Roles) load(getReferenceClass(), roleId);
	}

	public Roles load(Long roleId, org.hibernate.Session hibSession) {
		return (Roles) load(getReferenceClass(), roleId, hibSession);
	}

	public Roles loadInitialize(Long roleId, org.hibernate.Session hibSession) {
		Roles roles = load(roleId, hibSession);
		if (!Hibernate.isInitialized(roles)) Hibernate.initialize(roles);
		return roles;
	}

	public void save(Roles roles) {
		save((Object) roles);
	}

	public void save(Roles roles, org.hibernate.Session hibSession) {
		save((Object) roles, hibSession);
	}

	public void saveOrUpdate(Roles roles) {
		saveOrUpdate((Object) roles);
	}

	public void saveOrUpdate(Roles roles, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) roles, hibSession);
	}


	public void update(Roles roles) {
		update((Object) roles);
	}

	public void update(Roles roles, org.hibernate.Session hibSession) {
		update((Object) roles, hibSession);
	}

	public void delete(Long roleId) {
		delete(load(roleId));
	}

	public void delete(Long roleId, org.hibernate.Session hibSession) {
		delete(load(roleId, hibSession), hibSession);
	}

	public void delete(Roles roles) {
		delete((Object) roles);
	}

	public void delete(Roles roles, org.hibernate.Session hibSession) {
		delete((Object) roles, hibSession);
	}

	public void refresh(Roles roles, org.hibernate.Session hibSession) {
		refresh((Object) roles, hibSession);
	}

	public List<Roles> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Roles").list();
	}
}
