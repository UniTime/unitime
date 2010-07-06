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

import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SettingsDAO;

public abstract class BaseSettingsDAO extends _RootDAO {

	private static SettingsDAO sInstance;

	public static SettingsDAO getInstance () {
		if (sInstance == null) sInstance = new SettingsDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Settings.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Settings get(Long uniqueId) {
		return (Settings) get(getReferenceClass(), uniqueId);
	}

	public Settings get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Settings) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Settings load(Long uniqueId) {
		return (Settings) load(getReferenceClass(), uniqueId);
	}

	public Settings load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Settings) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Settings loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Settings settings = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(settings)) Hibernate.initialize(settings);
		return settings;
	}

	public void save(Settings settings) {
		save((Object) settings);
	}

	public void save(Settings settings, org.hibernate.Session hibSession) {
		save((Object) settings, hibSession);
	}

	public void saveOrUpdate(Settings settings) {
		saveOrUpdate((Object) settings);
	}

	public void saveOrUpdate(Settings settings, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) settings, hibSession);
	}


	public void update(Settings settings) {
		update((Object) settings);
	}

	public void update(Settings settings, org.hibernate.Session hibSession) {
		update((Object) settings, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Settings settings) {
		delete((Object) settings);
	}

	public void delete(Settings settings, org.hibernate.Session hibSession) {
		delete((Object) settings, hibSession);
	}

	public void refresh(Settings settings, org.hibernate.Session hibSession) {
		refresh((Object) settings, hibSession);
	}

	public List<Settings> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Settings").list();
	}
}
