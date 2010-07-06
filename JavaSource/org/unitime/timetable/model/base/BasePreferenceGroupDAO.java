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

import org.hibernate.Hibernate;
import org.hibernate.criterion.Order;

import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PreferenceGroupDAO;

public abstract class BasePreferenceGroupDAO extends _RootDAO {

	private static PreferenceGroupDAO sInstance;

	public static PreferenceGroupDAO getInstance () {
		if (sInstance == null) sInstance = new PreferenceGroupDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return PreferenceGroup.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public PreferenceGroup get(Long uniqueId) {
		return (PreferenceGroup) get(getReferenceClass(), uniqueId);
	}

	public PreferenceGroup get(Long uniqueId, org.hibernate.Session hibSession) {
		return (PreferenceGroup) get(getReferenceClass(), uniqueId, hibSession);
	}

	public PreferenceGroup load(Long uniqueId) {
		return (PreferenceGroup) load(getReferenceClass(), uniqueId);
	}

	public PreferenceGroup load(Long uniqueId, org.hibernate.Session hibSession) {
		return (PreferenceGroup) load(getReferenceClass(), uniqueId, hibSession);
	}

	public PreferenceGroup loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		PreferenceGroup preferenceGroup = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(preferenceGroup)) Hibernate.initialize(preferenceGroup);
		return preferenceGroup;
	}

	public void save(PreferenceGroup preferenceGroup) {
		save((Object) preferenceGroup);
	}

	public void save(PreferenceGroup preferenceGroup, org.hibernate.Session hibSession) {
		save((Object) preferenceGroup, hibSession);
	}

	public void saveOrUpdate(PreferenceGroup preferenceGroup) {
		saveOrUpdate((Object) preferenceGroup);
	}

	public void saveOrUpdate(PreferenceGroup preferenceGroup, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) preferenceGroup, hibSession);
	}


	public void update(PreferenceGroup preferenceGroup) {
		update((Object) preferenceGroup);
	}

	public void update(PreferenceGroup preferenceGroup, org.hibernate.Session hibSession) {
		update((Object) preferenceGroup, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(PreferenceGroup preferenceGroup) {
		delete((Object) preferenceGroup);
	}

	public void delete(PreferenceGroup preferenceGroup, org.hibernate.Session hibSession) {
		delete((Object) preferenceGroup, hibSession);
	}

	public void refresh(PreferenceGroup preferenceGroup, org.hibernate.Session hibSession) {
		refresh((Object) preferenceGroup, hibSession);
	}
}
