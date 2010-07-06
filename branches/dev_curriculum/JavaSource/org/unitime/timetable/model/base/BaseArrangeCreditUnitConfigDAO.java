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

import org.unitime.timetable.model.ArrangeCreditUnitConfig;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ArrangeCreditUnitConfigDAO;

public abstract class BaseArrangeCreditUnitConfigDAO extends _RootDAO {

	private static ArrangeCreditUnitConfigDAO sInstance;

	public static ArrangeCreditUnitConfigDAO getInstance () {
		if (sInstance == null) sInstance = new ArrangeCreditUnitConfigDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return ArrangeCreditUnitConfig.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public ArrangeCreditUnitConfig get(Long uniqueId) {
		return (ArrangeCreditUnitConfig) get(getReferenceClass(), uniqueId);
	}

	public ArrangeCreditUnitConfig get(Long uniqueId, org.hibernate.Session hibSession) {
		return (ArrangeCreditUnitConfig) get(getReferenceClass(), uniqueId, hibSession);
	}

	public ArrangeCreditUnitConfig load(Long uniqueId) {
		return (ArrangeCreditUnitConfig) load(getReferenceClass(), uniqueId);
	}

	public ArrangeCreditUnitConfig load(Long uniqueId, org.hibernate.Session hibSession) {
		return (ArrangeCreditUnitConfig) load(getReferenceClass(), uniqueId, hibSession);
	}

	public ArrangeCreditUnitConfig loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		ArrangeCreditUnitConfig arrangeCreditUnitConfig = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(arrangeCreditUnitConfig)) Hibernate.initialize(arrangeCreditUnitConfig);
		return arrangeCreditUnitConfig;
	}

	public void save(ArrangeCreditUnitConfig arrangeCreditUnitConfig) {
		save((Object) arrangeCreditUnitConfig);
	}

	public void save(ArrangeCreditUnitConfig arrangeCreditUnitConfig, org.hibernate.Session hibSession) {
		save((Object) arrangeCreditUnitConfig, hibSession);
	}

	public void saveOrUpdate(ArrangeCreditUnitConfig arrangeCreditUnitConfig) {
		saveOrUpdate((Object) arrangeCreditUnitConfig);
	}

	public void saveOrUpdate(ArrangeCreditUnitConfig arrangeCreditUnitConfig, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) arrangeCreditUnitConfig, hibSession);
	}


	public void update(ArrangeCreditUnitConfig arrangeCreditUnitConfig) {
		update((Object) arrangeCreditUnitConfig);
	}

	public void update(ArrangeCreditUnitConfig arrangeCreditUnitConfig, org.hibernate.Session hibSession) {
		update((Object) arrangeCreditUnitConfig, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(ArrangeCreditUnitConfig arrangeCreditUnitConfig) {
		delete((Object) arrangeCreditUnitConfig);
	}

	public void delete(ArrangeCreditUnitConfig arrangeCreditUnitConfig, org.hibernate.Session hibSession) {
		delete((Object) arrangeCreditUnitConfig, hibSession);
	}

	public void refresh(ArrangeCreditUnitConfig arrangeCreditUnitConfig, org.hibernate.Session hibSession) {
		refresh((Object) arrangeCreditUnitConfig, hibSession);
	}

	public List<ArrangeCreditUnitConfig> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from ArrangeCreditUnitConfig").list();
	}
}
