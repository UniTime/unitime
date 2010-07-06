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

import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.VariableRangeCreditUnitConfigDAO;

public abstract class BaseVariableRangeCreditUnitConfigDAO extends _RootDAO {

	private static VariableRangeCreditUnitConfigDAO sInstance;

	public static VariableRangeCreditUnitConfigDAO getInstance () {
		if (sInstance == null) sInstance = new VariableRangeCreditUnitConfigDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return VariableRangeCreditUnitConfig.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public VariableRangeCreditUnitConfig get(Long uniqueId) {
		return (VariableRangeCreditUnitConfig) get(getReferenceClass(), uniqueId);
	}

	public VariableRangeCreditUnitConfig get(Long uniqueId, org.hibernate.Session hibSession) {
		return (VariableRangeCreditUnitConfig) get(getReferenceClass(), uniqueId, hibSession);
	}

	public VariableRangeCreditUnitConfig load(Long uniqueId) {
		return (VariableRangeCreditUnitConfig) load(getReferenceClass(), uniqueId);
	}

	public VariableRangeCreditUnitConfig load(Long uniqueId, org.hibernate.Session hibSession) {
		return (VariableRangeCreditUnitConfig) load(getReferenceClass(), uniqueId, hibSession);
	}

	public VariableRangeCreditUnitConfig loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		VariableRangeCreditUnitConfig variableRangeCreditUnitConfig = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(variableRangeCreditUnitConfig)) Hibernate.initialize(variableRangeCreditUnitConfig);
		return variableRangeCreditUnitConfig;
	}

	public void save(VariableRangeCreditUnitConfig variableRangeCreditUnitConfig) {
		save((Object) variableRangeCreditUnitConfig);
	}

	public void save(VariableRangeCreditUnitConfig variableRangeCreditUnitConfig, org.hibernate.Session hibSession) {
		save((Object) variableRangeCreditUnitConfig, hibSession);
	}

	public void saveOrUpdate(VariableRangeCreditUnitConfig variableRangeCreditUnitConfig) {
		saveOrUpdate((Object) variableRangeCreditUnitConfig);
	}

	public void saveOrUpdate(VariableRangeCreditUnitConfig variableRangeCreditUnitConfig, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) variableRangeCreditUnitConfig, hibSession);
	}


	public void update(VariableRangeCreditUnitConfig variableRangeCreditUnitConfig) {
		update((Object) variableRangeCreditUnitConfig);
	}

	public void update(VariableRangeCreditUnitConfig variableRangeCreditUnitConfig, org.hibernate.Session hibSession) {
		update((Object) variableRangeCreditUnitConfig, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(VariableRangeCreditUnitConfig variableRangeCreditUnitConfig) {
		delete((Object) variableRangeCreditUnitConfig);
	}

	public void delete(VariableRangeCreditUnitConfig variableRangeCreditUnitConfig, org.hibernate.Session hibSession) {
		delete((Object) variableRangeCreditUnitConfig, hibSession);
	}

	public void refresh(VariableRangeCreditUnitConfig variableRangeCreditUnitConfig, org.hibernate.Session hibSession) {
		refresh((Object) variableRangeCreditUnitConfig, hibSession);
	}

	public List<VariableRangeCreditUnitConfig> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from VariableRangeCreditUnitConfig").list();
	}
}
