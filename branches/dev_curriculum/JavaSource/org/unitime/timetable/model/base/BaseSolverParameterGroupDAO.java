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

import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SolverParameterGroupDAO;

public abstract class BaseSolverParameterGroupDAO extends _RootDAO {

	private static SolverParameterGroupDAO sInstance;

	public static SolverParameterGroupDAO getInstance () {
		if (sInstance == null) sInstance = new SolverParameterGroupDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SolverParameterGroup.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SolverParameterGroup get(Long uniqueId) {
		return (SolverParameterGroup) get(getReferenceClass(), uniqueId);
	}

	public SolverParameterGroup get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverParameterGroup) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverParameterGroup load(Long uniqueId) {
		return (SolverParameterGroup) load(getReferenceClass(), uniqueId);
	}

	public SolverParameterGroup load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverParameterGroup) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverParameterGroup loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SolverParameterGroup solverParameterGroup = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(solverParameterGroup)) Hibernate.initialize(solverParameterGroup);
		return solverParameterGroup;
	}

	public void save(SolverParameterGroup solverParameterGroup) {
		save((Object) solverParameterGroup);
	}

	public void save(SolverParameterGroup solverParameterGroup, org.hibernate.Session hibSession) {
		save((Object) solverParameterGroup, hibSession);
	}

	public void saveOrUpdate(SolverParameterGroup solverParameterGroup) {
		saveOrUpdate((Object) solverParameterGroup);
	}

	public void saveOrUpdate(SolverParameterGroup solverParameterGroup, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) solverParameterGroup, hibSession);
	}


	public void update(SolverParameterGroup solverParameterGroup) {
		update((Object) solverParameterGroup);
	}

	public void update(SolverParameterGroup solverParameterGroup, org.hibernate.Session hibSession) {
		update((Object) solverParameterGroup, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SolverParameterGroup solverParameterGroup) {
		delete((Object) solverParameterGroup);
	}

	public void delete(SolverParameterGroup solverParameterGroup, org.hibernate.Session hibSession) {
		delete((Object) solverParameterGroup, hibSession);
	}

	public void refresh(SolverParameterGroup solverParameterGroup, org.hibernate.Session hibSession) {
		refresh((Object) solverParameterGroup, hibSession);
	}

	public List<SolverParameterGroup> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SolverParameterGroup").list();
	}
}
