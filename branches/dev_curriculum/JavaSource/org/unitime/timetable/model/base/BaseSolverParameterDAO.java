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

import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SolverParameterDAO;

public abstract class BaseSolverParameterDAO extends _RootDAO {

	private static SolverParameterDAO sInstance;

	public static SolverParameterDAO getInstance () {
		if (sInstance == null) sInstance = new SolverParameterDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SolverParameter.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SolverParameter get(Long uniqueId) {
		return (SolverParameter) get(getReferenceClass(), uniqueId);
	}

	public SolverParameter get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverParameter) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverParameter load(Long uniqueId) {
		return (SolverParameter) load(getReferenceClass(), uniqueId);
	}

	public SolverParameter load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverParameter) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverParameter loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SolverParameter solverParameter = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(solverParameter)) Hibernate.initialize(solverParameter);
		return solverParameter;
	}

	public void save(SolverParameter solverParameter) {
		save((Object) solverParameter);
	}

	public void save(SolverParameter solverParameter, org.hibernate.Session hibSession) {
		save((Object) solverParameter, hibSession);
	}

	public void saveOrUpdate(SolverParameter solverParameter) {
		saveOrUpdate((Object) solverParameter);
	}

	public void saveOrUpdate(SolverParameter solverParameter, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) solverParameter, hibSession);
	}


	public void update(SolverParameter solverParameter) {
		update((Object) solverParameter);
	}

	public void update(SolverParameter solverParameter, org.hibernate.Session hibSession) {
		update((Object) solverParameter, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SolverParameter solverParameter) {
		delete((Object) solverParameter);
	}

	public void delete(SolverParameter solverParameter, org.hibernate.Session hibSession) {
		delete((Object) solverParameter, hibSession);
	}

	public void refresh(SolverParameter solverParameter, org.hibernate.Session hibSession) {
		refresh((Object) solverParameter, hibSession);
	}

	public List<SolverParameter> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SolverParameter").list();
	}

	public List<SolverParameter> findByDefinition(org.hibernate.Session hibSession, Long definitionId) {
		return hibSession.createQuery("from SolverParameter x where x.definition.uniqueId = :definitionId").setLong("definitionId", definitionId).list();
	}
}
