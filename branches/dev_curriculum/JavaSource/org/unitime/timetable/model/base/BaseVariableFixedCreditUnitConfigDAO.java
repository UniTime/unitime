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

import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.VariableFixedCreditUnitConfigDAO;

public abstract class BaseVariableFixedCreditUnitConfigDAO extends _RootDAO {

	private static VariableFixedCreditUnitConfigDAO sInstance;

	public static VariableFixedCreditUnitConfigDAO getInstance () {
		if (sInstance == null) sInstance = new VariableFixedCreditUnitConfigDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return VariableFixedCreditUnitConfig.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public VariableFixedCreditUnitConfig get(Long uniqueId) {
		return (VariableFixedCreditUnitConfig) get(getReferenceClass(), uniqueId);
	}

	public VariableFixedCreditUnitConfig get(Long uniqueId, org.hibernate.Session hibSession) {
		return (VariableFixedCreditUnitConfig) get(getReferenceClass(), uniqueId, hibSession);
	}

	public VariableFixedCreditUnitConfig load(Long uniqueId) {
		return (VariableFixedCreditUnitConfig) load(getReferenceClass(), uniqueId);
	}

	public VariableFixedCreditUnitConfig load(Long uniqueId, org.hibernate.Session hibSession) {
		return (VariableFixedCreditUnitConfig) load(getReferenceClass(), uniqueId, hibSession);
	}

	public VariableFixedCreditUnitConfig loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		VariableFixedCreditUnitConfig variableFixedCreditUnitConfig = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(variableFixedCreditUnitConfig)) Hibernate.initialize(variableFixedCreditUnitConfig);
		return variableFixedCreditUnitConfig;
	}

	public void save(VariableFixedCreditUnitConfig variableFixedCreditUnitConfig) {
		save((Object) variableFixedCreditUnitConfig);
	}

	public void save(VariableFixedCreditUnitConfig variableFixedCreditUnitConfig, org.hibernate.Session hibSession) {
		save((Object) variableFixedCreditUnitConfig, hibSession);
	}

	public void saveOrUpdate(VariableFixedCreditUnitConfig variableFixedCreditUnitConfig) {
		saveOrUpdate((Object) variableFixedCreditUnitConfig);
	}

	public void saveOrUpdate(VariableFixedCreditUnitConfig variableFixedCreditUnitConfig, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) variableFixedCreditUnitConfig, hibSession);
	}


	public void update(VariableFixedCreditUnitConfig variableFixedCreditUnitConfig) {
		update((Object) variableFixedCreditUnitConfig);
	}

	public void update(VariableFixedCreditUnitConfig variableFixedCreditUnitConfig, org.hibernate.Session hibSession) {
		update((Object) variableFixedCreditUnitConfig, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(VariableFixedCreditUnitConfig variableFixedCreditUnitConfig) {
		delete((Object) variableFixedCreditUnitConfig);
	}

	public void delete(VariableFixedCreditUnitConfig variableFixedCreditUnitConfig, org.hibernate.Session hibSession) {
		delete((Object) variableFixedCreditUnitConfig, hibSession);
	}

	public void refresh(VariableFixedCreditUnitConfig variableFixedCreditUnitConfig, org.hibernate.Session hibSession) {
		refresh((Object) variableFixedCreditUnitConfig, hibSession);
	}

	public List<VariableFixedCreditUnitConfig> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from VariableFixedCreditUnitConfig").list();
	}
}
