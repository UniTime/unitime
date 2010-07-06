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

import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ApplicationConfigDAO;

public abstract class BaseApplicationConfigDAO extends _RootDAO {

	private static ApplicationConfigDAO sInstance;

	public static ApplicationConfigDAO getInstance () {
		if (sInstance == null) sInstance = new ApplicationConfigDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ApplicationConfig.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ApplicationConfig get(String key) {
		return (ApplicationConfig) get(getReferenceClass(), key);
	}

	public ApplicationConfig get(String key, org.hibernate.Session hibSession) {
		return (ApplicationConfig) get(getReferenceClass(), key, hibSession);
	}

	public ApplicationConfig load(String key) {
		return (ApplicationConfig) load(getReferenceClass(), key);
	}

	public ApplicationConfig load(String key, org.hibernate.Session hibSession) {
		return (ApplicationConfig) load(getReferenceClass(), key, hibSession);
	}

	public ApplicationConfig loadInitialize(String key, org.hibernate.Session hibSession) {
		ApplicationConfig applicationConfig = load(key, hibSession);
		if (!Hibernate.isInitialized(applicationConfig)) Hibernate.initialize(applicationConfig);
		return applicationConfig;
	}

	public void save(ApplicationConfig applicationConfig) {
		save((Object) applicationConfig);
	}

	public void save(ApplicationConfig applicationConfig, org.hibernate.Session hibSession) {
		save((Object) applicationConfig, hibSession);
	}

	public void saveOrUpdate(ApplicationConfig applicationConfig) {
		saveOrUpdate((Object) applicationConfig);
	}

	public void saveOrUpdate(ApplicationConfig applicationConfig, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) applicationConfig, hibSession);
	}


	public void update(ApplicationConfig applicationConfig) {
		update((Object) applicationConfig);
	}

	public void update(ApplicationConfig applicationConfig, org.hibernate.Session hibSession) {
		update((Object) applicationConfig, hibSession);
	}

	public void delete(Object key) {
		if (key instanceof String)
			delete((Object) load((String)key));
		else
		super.delete(key);
	}

	public void delete(Object key, org.hibernate.Session hibSession) {
		if (key instanceof String)
			delete((Object) load((String)key, hibSession), hibSession);
		else
			super.delete(key, hibSession);
	}

	public void delete(ApplicationConfig applicationConfig) {
		delete((Object) applicationConfig);
	}

	public void delete(ApplicationConfig applicationConfig, org.hibernate.Session hibSession) {
		delete((Object) applicationConfig, hibSession);
	}

	public void refresh(ApplicationConfig applicationConfig, org.hibernate.Session hibSession) {
		refresh((Object) applicationConfig, hibSession);
	}

	public List<ApplicationConfig> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ApplicationConfig").list();
	}
}
