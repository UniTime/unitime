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

import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SolverParameterDefDAO;

public abstract class BaseSolverParameterDefDAO extends _RootDAO {

	private static SolverParameterDefDAO sInstance;

	public static SolverParameterDefDAO getInstance () {
		if (sInstance == null) sInstance = new SolverParameterDefDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SolverParameterDef.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SolverParameterDef get(Long uniqueId) {
		return (SolverParameterDef) get(getReferenceClass(), uniqueId);
	}

	public SolverParameterDef get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverParameterDef) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverParameterDef load(Long uniqueId) {
		return (SolverParameterDef) load(getReferenceClass(), uniqueId);
	}

	public SolverParameterDef load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverParameterDef) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverParameterDef loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SolverParameterDef solverParameterDef = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(solverParameterDef)) Hibernate.initialize(solverParameterDef);
		return solverParameterDef;
	}

	public void save(SolverParameterDef solverParameterDef) {
		save((Object) solverParameterDef);
	}

	public void save(SolverParameterDef solverParameterDef, org.hibernate.Session hibSession) {
		save((Object) solverParameterDef, hibSession);
	}

	public void saveOrUpdate(SolverParameterDef solverParameterDef) {
		saveOrUpdate((Object) solverParameterDef);
	}

	public void saveOrUpdate(SolverParameterDef solverParameterDef, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) solverParameterDef, hibSession);
	}


	public void update(SolverParameterDef solverParameterDef) {
		update((Object) solverParameterDef);
	}

	public void update(SolverParameterDef solverParameterDef, org.hibernate.Session hibSession) {
		update((Object) solverParameterDef, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SolverParameterDef solverParameterDef) {
		delete((Object) solverParameterDef);
	}

	public void delete(SolverParameterDef solverParameterDef, org.hibernate.Session hibSession) {
		delete((Object) solverParameterDef, hibSession);
	}

	public void refresh(SolverParameterDef solverParameterDef, org.hibernate.Session hibSession) {
		refresh((Object) solverParameterDef, hibSession);
	}

	public List<SolverParameterDef> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SolverParameterDef").list();
	}

	public List<SolverParameterDef> findByGroup(org.hibernate.Session hibSession, Long groupId) {
		return hibSession.createQuery("from SolverParameterDef x where x.group.uniqueId = :groupId").setLong("groupId", groupId).list();
	}
}
