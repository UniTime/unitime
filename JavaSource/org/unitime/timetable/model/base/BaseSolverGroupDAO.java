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

import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;

public abstract class BaseSolverGroupDAO extends _RootDAO {

	private static SolverGroupDAO sInstance;

	public static SolverGroupDAO getInstance () {
		if (sInstance == null) sInstance = new SolverGroupDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SolverGroup.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SolverGroup get(Long uniqueId) {
		return (SolverGroup) get(getReferenceClass(), uniqueId);
	}

	public SolverGroup get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverGroup) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverGroup load(Long uniqueId) {
		return (SolverGroup) load(getReferenceClass(), uniqueId);
	}

	public SolverGroup load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SolverGroup) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SolverGroup loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SolverGroup solverGroup = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(solverGroup)) Hibernate.initialize(solverGroup);
		return solverGroup;
	}

	public void save(SolverGroup solverGroup) {
		save((Object) solverGroup);
	}

	public void save(SolverGroup solverGroup, org.hibernate.Session hibSession) {
		save((Object) solverGroup, hibSession);
	}

	public void saveOrUpdate(SolverGroup solverGroup) {
		saveOrUpdate((Object) solverGroup);
	}

	public void saveOrUpdate(SolverGroup solverGroup, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) solverGroup, hibSession);
	}


	public void update(SolverGroup solverGroup) {
		update((Object) solverGroup);
	}

	public void update(SolverGroup solverGroup, org.hibernate.Session hibSession) {
		update((Object) solverGroup, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SolverGroup solverGroup) {
		delete((Object) solverGroup);
	}

	public void delete(SolverGroup solverGroup, org.hibernate.Session hibSession) {
		delete((Object) solverGroup, hibSession);
	}

	public void refresh(SolverGroup solverGroup, org.hibernate.Session hibSession) {
		refresh((Object) solverGroup, hibSession);
	}

	public List<SolverGroup> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SolverGroup").list();
	}

	public List<SolverGroup> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from SolverGroup x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
