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

import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.FixedCreditUnitConfigDAO;

public abstract class BaseFixedCreditUnitConfigDAO extends _RootDAO {

	private static FixedCreditUnitConfigDAO sInstance;

	public static FixedCreditUnitConfigDAO getInstance () {
		if (sInstance == null) sInstance = new FixedCreditUnitConfigDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return FixedCreditUnitConfig.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public FixedCreditUnitConfig get(Long uniqueId) {
		return (FixedCreditUnitConfig) get(getReferenceClass(), uniqueId);
	}

	public FixedCreditUnitConfig get(Long uniqueId, org.hibernate.Session hibSession) {
		return (FixedCreditUnitConfig) get(getReferenceClass(), uniqueId, hibSession);
	}

	public FixedCreditUnitConfig load(Long uniqueId) {
		return (FixedCreditUnitConfig) load(getReferenceClass(), uniqueId);
	}

	public FixedCreditUnitConfig load(Long uniqueId, org.hibernate.Session hibSession) {
		return (FixedCreditUnitConfig) load(getReferenceClass(), uniqueId, hibSession);
	}

	public FixedCreditUnitConfig loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		FixedCreditUnitConfig fixedCreditUnitConfig = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(fixedCreditUnitConfig)) Hibernate.initialize(fixedCreditUnitConfig);
		return fixedCreditUnitConfig;
	}

	public void save(FixedCreditUnitConfig fixedCreditUnitConfig) {
		save((Object) fixedCreditUnitConfig);
	}

	public void save(FixedCreditUnitConfig fixedCreditUnitConfig, org.hibernate.Session hibSession) {
		save((Object) fixedCreditUnitConfig, hibSession);
	}

	public void saveOrUpdate(FixedCreditUnitConfig fixedCreditUnitConfig) {
		saveOrUpdate((Object) fixedCreditUnitConfig);
	}

	public void saveOrUpdate(FixedCreditUnitConfig fixedCreditUnitConfig, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) fixedCreditUnitConfig, hibSession);
	}


	public void update(FixedCreditUnitConfig fixedCreditUnitConfig) {
		update((Object) fixedCreditUnitConfig);
	}

	public void update(FixedCreditUnitConfig fixedCreditUnitConfig, org.hibernate.Session hibSession) {
		update((Object) fixedCreditUnitConfig, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(FixedCreditUnitConfig fixedCreditUnitConfig) {
		delete((Object) fixedCreditUnitConfig);
	}

	public void delete(FixedCreditUnitConfig fixedCreditUnitConfig, org.hibernate.Session hibSession) {
		delete((Object) fixedCreditUnitConfig, hibSession);
	}

	public void refresh(FixedCreditUnitConfig fixedCreditUnitConfig, org.hibernate.Session hibSession) {
		refresh((Object) fixedCreditUnitConfig, hibSession);
	}

	public List<FixedCreditUnitConfig> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from FixedCreditUnitConfig").list();
	}
}
