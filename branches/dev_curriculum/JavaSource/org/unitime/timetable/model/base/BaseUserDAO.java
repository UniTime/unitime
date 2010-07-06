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

import org.unitime.timetable.model.User;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.UserDAO;

public abstract class BaseUserDAO extends _RootDAO {

	private static UserDAO sInstance;

	public static UserDAO getInstance () {
		if (sInstance == null) sInstance = new UserDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return User.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public User get(String username) {
		return (User) get(getReferenceClass(), username);
	}

	public User get(String username, org.hibernate.Session hibSession) {
		return (User) get(getReferenceClass(), username, hibSession);
	}

	public User load(String username) {
		return (User) load(getReferenceClass(), username);
	}

	public User load(String username, org.hibernate.Session hibSession) {
		return (User) load(getReferenceClass(), username, hibSession);
	}

	public User loadInitialize(String username, org.hibernate.Session hibSession) {
		User user = load(username, hibSession);
		if (!Hibernate.isInitialized(user)) Hibernate.initialize(user);
		return user;
	}

	public void save(User user) {
		save((Object) user);
	}

	public void save(User user, org.hibernate.Session hibSession) {
		save((Object) user, hibSession);
	}

	public void saveOrUpdate(User user) {
		saveOrUpdate((Object) user);
	}

	public void saveOrUpdate(User user, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) user, hibSession);
	}


	public void update(User user) {
		update((Object) user);
	}

	public void update(User user, org.hibernate.Session hibSession) {
		update((Object) user, hibSession);
	}

	public void delete(Object username) {
		if (username instanceof String)
			delete((Object) load((String)username));
		else
		super.delete(username);
	}

	public void delete(Object username, org.hibernate.Session hibSession) {
		if (username instanceof String)
			delete((Object) load((String)username, hibSession), hibSession);
		else
			super.delete(username, hibSession);
	}

	public void delete(User user) {
		delete((Object) user);
	}

	public void delete(User user, org.hibernate.Session hibSession) {
		delete((Object) user, hibSession);
	}

	public void refresh(User user, org.hibernate.Session hibSession) {
		refresh((Object) user, hibSession);
	}

	public List<User> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from User").list();
	}
}
