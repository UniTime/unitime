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

import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;

public abstract class BaseSolverPredefinedSettingDAO extends _RootDAO {

	private static SolverPredefinedSettingDAO sInstance;

	public static SolverPredefinedSettingDAO getInstance () {
		if (sInstance == null) sInstance = new SolverPredefinedSettingDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SolverPredefinedSetting.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SolverPredefinedSetting get(Long uniqueId) {
		return (SolverPredefinedSetting) get(getReferenceClass(), uniqueId);
	}

	public SolverPredefinedSetting get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverPredefinedSetting) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverPredefinedSetting load(Long uniqueId) {
		return (SolverPredefinedSetting) load(getReferenceClass(), uniqueId);
	}

	public SolverPredefinedSetting load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverPredefinedSetting) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverPredefinedSetting loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SolverPredefinedSetting solverPredefinedSetting = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(solverPredefinedSetting)) Hibernate.initialize(solverPredefinedSetting);
		return solverPredefinedSetting;
	}

	public void save(SolverPredefinedSetting solverPredefinedSetting) {
		save((Object) solverPredefinedSetting);
	}

	public void save(SolverPredefinedSetting solverPredefinedSetting, org.hibernate.Session hibSession) {
		save((Object) solverPredefinedSetting, hibSession);
	}

	public void saveOrUpdate(SolverPredefinedSetting solverPredefinedSetting) {
		saveOrUpdate((Object) solverPredefinedSetting);
	}

	public void saveOrUpdate(SolverPredefinedSetting solverPredefinedSetting, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) solverPredefinedSetting, hibSession);
	}


	public void update(SolverPredefinedSetting solverPredefinedSetting) {
		update((Object) solverPredefinedSetting);
	}

	public void update(SolverPredefinedSetting solverPredefinedSetting, org.hibernate.Session hibSession) {
		update((Object) solverPredefinedSetting, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SolverPredefinedSetting solverPredefinedSetting) {
		delete((Object) solverPredefinedSetting);
	}

	public void delete(SolverPredefinedSetting solverPredefinedSetting, org.hibernate.Session hibSession) {
		delete((Object) solverPredefinedSetting, hibSession);
	}

	public void refresh(SolverPredefinedSetting solverPredefinedSetting, org.hibernate.Session hibSession) {
		refresh((Object) solverPredefinedSetting, hibSession);
	}

	public List<SolverPredefinedSetting> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SolverPredefinedSetting").list();
	}
}
