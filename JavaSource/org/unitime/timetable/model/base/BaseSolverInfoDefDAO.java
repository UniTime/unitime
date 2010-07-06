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

import org.unitime.timetable.model.SolverInfoDef;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SolverInfoDefDAO;

public abstract class BaseSolverInfoDefDAO extends _RootDAO {

	private static SolverInfoDefDAO sInstance;

	public static SolverInfoDefDAO getInstance () {
		if (sInstance == null) sInstance = new SolverInfoDefDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SolverInfoDef.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SolverInfoDef get(Long uniqueId) {
		return (SolverInfoDef) get(getReferenceClass(), uniqueId);
	}

	public SolverInfoDef get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverInfoDef) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverInfoDef load(Long uniqueId) {
		return (SolverInfoDef) load(getReferenceClass(), uniqueId);
	}

	public SolverInfoDef load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverInfoDef) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverInfoDef loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SolverInfoDef solverInfoDef = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(solverInfoDef)) Hibernate.initialize(solverInfoDef);
		return solverInfoDef;
	}

	public void save(SolverInfoDef solverInfoDef) {
		save((Object) solverInfoDef);
	}

	public void save(SolverInfoDef solverInfoDef, org.hibernate.Session hibSession) {
		save((Object) solverInfoDef, hibSession);
	}

	public void saveOrUpdate(SolverInfoDef solverInfoDef) {
		saveOrUpdate((Object) solverInfoDef);
	}

	public void saveOrUpdate(SolverInfoDef solverInfoDef, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) solverInfoDef, hibSession);
	}


	public void update(SolverInfoDef solverInfoDef) {
		update((Object) solverInfoDef);
	}

	public void update(SolverInfoDef solverInfoDef, org.hibernate.Session hibSession) {
		update((Object) solverInfoDef, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SolverInfoDef solverInfoDef) {
		delete((Object) solverInfoDef);
	}

	public void delete(SolverInfoDef solverInfoDef, org.hibernate.Session hibSession) {
		delete((Object) solverInfoDef, hibSession);
	}

	public void refresh(SolverInfoDef solverInfoDef, org.hibernate.Session hibSession) {
		refresh((Object) solverInfoDef, hibSession);
	}

	public List<SolverInfoDef> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SolverInfoDef").list();
	}
}
