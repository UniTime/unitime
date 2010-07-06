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

import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;

public abstract class BasePreferenceLevelDAO extends _RootDAO {

	private static PreferenceLevelDAO sInstance;

	public static PreferenceLevelDAO getInstance () {
		if (sInstance == null) sInstance = new PreferenceLevelDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return PreferenceLevel.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public PreferenceLevel get(Long uniqueId) {
		return (PreferenceLevel) get(getReferenceClass(), uniqueId);
	}

	public PreferenceLevel get(Long uniqueId, org.hibernate.Session hibSession) {
		return (PreferenceLevel) get(getReferenceClass(), uniqueId, hibSession);
	}

	public PreferenceLevel load(Long uniqueId) {
		return (PreferenceLevel) load(getReferenceClass(), uniqueId);
	}

	public PreferenceLevel load(Long uniqueId, org.hibernate.Session hibSession) {
		return (PreferenceLevel) load(getReferenceClass(), uniqueId, hibSession);
	}

	public PreferenceLevel loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		PreferenceLevel preferenceLevel = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(preferenceLevel)) Hibernate.initialize(preferenceLevel);
		return preferenceLevel;
	}

	public void save(PreferenceLevel preferenceLevel) {
		save((Object) preferenceLevel);
	}

	public void save(PreferenceLevel preferenceLevel, org.hibernate.Session hibSession) {
		save((Object) preferenceLevel, hibSession);
	}

	public void saveOrUpdate(PreferenceLevel preferenceLevel) {
		saveOrUpdate((Object) preferenceLevel);
	}

	public void saveOrUpdate(PreferenceLevel preferenceLevel, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) preferenceLevel, hibSession);
	}


	public void update(PreferenceLevel preferenceLevel) {
		update((Object) preferenceLevel);
	}

	public void update(PreferenceLevel preferenceLevel, org.hibernate.Session hibSession) {
		update((Object) preferenceLevel, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(PreferenceLevel preferenceLevel) {
		delete((Object) preferenceLevel);
	}

	public void delete(PreferenceLevel preferenceLevel, org.hibernate.Session hibSession) {
		delete((Object) preferenceLevel, hibSession);
	}

	public void refresh(PreferenceLevel preferenceLevel, org.hibernate.Session hibSession) {
		refresh((Object) preferenceLevel, hibSession);
	}

	public List<PreferenceLevel> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from PreferenceLevel").list();
	}
}
