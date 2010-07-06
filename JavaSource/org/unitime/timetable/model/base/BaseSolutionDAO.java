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

import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SolutionDAO;

public abstract class BaseSolutionDAO extends _RootDAO {

	private static SolutionDAO sInstance;

	public static SolutionDAO getInstance () {
		if (sInstance == null) sInstance = new SolutionDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Solution.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Solution get(Long uniqueId) {
		return (Solution) get(getReferenceClass(), uniqueId);
	}

	public Solution get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Solution) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Solution load(Long uniqueId) {
		return (Solution) load(getReferenceClass(), uniqueId);
	}

	public Solution load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Solution) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Solution loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Solution solution = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(solution)) Hibernate.initialize(solution);
		return solution;
	}

	public void save(Solution solution) {
		save((Object) solution);
	}

	public void save(Solution solution, org.hibernate.Session hibSession) {
		save((Object) solution, hibSession);
	}

	public void saveOrUpdate(Solution solution) {
		saveOrUpdate((Object) solution);
	}

	public void saveOrUpdate(Solution solution, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) solution, hibSession);
	}


	public void update(Solution solution) {
		update((Object) solution);
	}

	public void update(Solution solution, org.hibernate.Session hibSession) {
		update((Object) solution, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Solution solution) {
		delete((Object) solution);
	}

	public void delete(Solution solution, org.hibernate.Session hibSession) {
		delete((Object) solution, hibSession);
	}

	public void refresh(Solution solution, org.hibernate.Session hibSession) {
		refresh((Object) solution, hibSession);
	}

	public List<Solution> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Solution").list();
	}

	public List<Solution> findByOwner(org.hibernate.Session hibSession, Long ownerId) {
		return hibSession.createQuery("from Solution x where x.owner.uniqueId = :ownerId").setLong("ownerId", ownerId).list();
	}
}
