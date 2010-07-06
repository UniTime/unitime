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

import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PreferenceDAO;

public abstract class BasePreferenceDAO extends _RootDAO {

	private static PreferenceDAO sInstance;

	public static PreferenceDAO getInstance () {
		if (sInstance == null) sInstance = new PreferenceDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Preference.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Preference get(Long uniqueId) {
		return (Preference) get(getReferenceClass(), uniqueId);
	}

	public Preference get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Preference) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Preference load(Long uniqueId) {
		return (Preference) load(getReferenceClass(), uniqueId);
	}

	public Preference load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Preference) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Preference loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Preference preference = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(preference)) Hibernate.initialize(preference);
		return preference;
	}

	public void save(Preference preference) {
		save((Object) preference);
	}

	public void save(Preference preference, org.hibernate.Session hibSession) {
		save((Object) preference, hibSession);
	}

	public void saveOrUpdate(Preference preference) {
		saveOrUpdate((Object) preference);
	}

	public void saveOrUpdate(Preference preference, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) preference, hibSession);
	}


	public void update(Preference preference) {
		update((Object) preference);
	}

	public void update(Preference preference, org.hibernate.Session hibSession) {
		update((Object) preference, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Preference preference) {
		delete((Object) preference);
	}

	public void delete(Preference preference, org.hibernate.Session hibSession) {
		delete((Object) preference, hibSession);
	}

	public void refresh(Preference preference, org.hibernate.Session hibSession) {
		refresh((Object) preference, hibSession);
	}

	public List<Preference> findByOwner(org.hibernate.Session hibSession, Long ownerId) {
		return hibSession.createQuery("from Preference x where x.owner.uniqueId = :ownerId").setLong("ownerId", ownerId).list();
	}

	public List<Preference> findByPrefLevel(org.hibernate.Session hibSession, Long prefLevelId) {
		return hibSession.createQuery("from Preference x where x.prefLevel.uniqueId = :prefLevelId").setLong("prefLevelId", prefLevelId).list();
	}
}
