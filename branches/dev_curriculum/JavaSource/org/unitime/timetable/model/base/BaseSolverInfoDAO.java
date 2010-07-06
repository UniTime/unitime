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

import org.unitime.timetable.model.SolverInfo;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SolverInfoDAO;

public abstract class BaseSolverInfoDAO extends _RootDAO {

	private static SolverInfoDAO sInstance;

	public static SolverInfoDAO getInstance () {
		if (sInstance == null) sInstance = new SolverInfoDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SolverInfo.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SolverInfo get(Long uniqueId) {
		return (SolverInfo) get(getReferenceClass(), uniqueId);
	}

	public SolverInfo get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverInfo) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverInfo load(Long uniqueId) {
		return (SolverInfo) load(getReferenceClass(), uniqueId);
	}

	public SolverInfo load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverInfo) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverInfo loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SolverInfo solverInfo = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(solverInfo)) Hibernate.initialize(solverInfo);
		return solverInfo;
	}

	public void save(SolverInfo solverInfo) {
		save((Object) solverInfo);
	}

	public void save(SolverInfo solverInfo, org.hibernate.Session hibSession) {
		save((Object) solverInfo, hibSession);
	}

	public void saveOrUpdate(SolverInfo solverInfo) {
		saveOrUpdate((Object) solverInfo);
	}

	public void saveOrUpdate(SolverInfo solverInfo, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) solverInfo, hibSession);
	}


	public void update(SolverInfo solverInfo) {
		update((Object) solverInfo);
	}

	public void update(SolverInfo solverInfo, org.hibernate.Session hibSession) {
		update((Object) solverInfo, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SolverInfo solverInfo) {
		delete((Object) solverInfo);
	}

	public void delete(SolverInfo solverInfo, org.hibernate.Session hibSession) {
		delete((Object) solverInfo, hibSession);
	}

	public void refresh(SolverInfo solverInfo, org.hibernate.Session hibSession) {
		refresh((Object) solverInfo, hibSession);
	}

	public List<SolverInfo> findByDefinition(org.hibernate.Session hibSession, Long definitionId) {
		return hibSession.createQuery("from SolverInfo x where x.definition.uniqueId = :definitionId").setLong("definitionId", definitionId).list();
	}
}
