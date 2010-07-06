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

import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.UserDataDAO;

public abstract class BaseUserDataDAO extends _RootDAO {

	private static UserDataDAO sInstance;

	public static UserDataDAO getInstance () {
		if (sInstance == null) sInstance = new UserDataDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return UserData.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public UserData get(UserData key) {
		return (UserData) get(getReferenceClass(), key);
	}

	public UserData get(UserData key, org.hibernate.Session hibSession) {
		return (UserData) get(getReferenceClass(), key, hibSession);
	}

	public UserData load(UserData key) {
		return (UserData) load(getReferenceClass(), key);
	}

	public UserData load(UserData key, org.hibernate.Session hibSession) {
		return (UserData) load(getReferenceClass(), key, hibSession);
	}

	public UserData loadInitialize(UserData key, org.hibernate.Session hibSession) {
		UserData userData = load(key, hibSession);
		if (!Hibernate.isInitialized(userData)) Hibernate.initialize(userData);
		return userData;
	}

	public void save(UserData userData) {
		save((Object) userData);
	}

	public void save(UserData userData, org.hibernate.Session hibSession) {
		save((Object) userData, hibSession);
	}

	public void saveOrUpdate(UserData userData) {
		saveOrUpdate((Object) userData);
	}

	public void saveOrUpdate(UserData userData, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) userData, hibSession);
	}


	public void update(UserData userData) {
		update((Object) userData);
	}

	public void update(UserData userData, org.hibernate.Session hibSession) {
		update((Object) userData, hibSession);
	}


	public void delete(UserData userData) {
		delete((Object) userData);
	}

	public void delete(UserData userData, org.hibernate.Session hibSession) {
		delete((Object) userData, hibSession);
	}

	public void refresh(UserData userData, org.hibernate.Session hibSession) {
		refresh((Object) userData, hibSession);
	}

	public List<UserData> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from UserData").list();
	}
}
