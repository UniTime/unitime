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

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AssignmentDAO;

public abstract class BaseAssignmentDAO extends _RootDAO {

	private static AssignmentDAO sInstance;

	public static AssignmentDAO getInstance () {
		if (sInstance == null) sInstance = new AssignmentDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Assignment.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Assignment get(Long uniqueId) {
		return (Assignment) get(getReferenceClass(), uniqueId);
	}

	public Assignment get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Assignment) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Assignment load(Long uniqueId) {
		return (Assignment) load(getReferenceClass(), uniqueId);
	}

	public Assignment load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Assignment) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Assignment loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Assignment assignment = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(assignment)) Hibernate.initialize(assignment);
		return assignment;
	}

	public void save(Assignment assignment) {
		save((Object) assignment);
	}

	public void save(Assignment assignment, org.hibernate.Session hibSession) {
		save((Object) assignment, hibSession);
	}

	public void saveOrUpdate(Assignment assignment) {
		saveOrUpdate((Object) assignment);
	}

	public void saveOrUpdate(Assignment assignment, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) assignment, hibSession);
	}


	public void update(Assignment assignment) {
		update((Object) assignment);
	}

	public void update(Assignment assignment, org.hibernate.Session hibSession) {
		update((Object) assignment, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Assignment assignment) {
		delete((Object) assignment);
	}

	public void delete(Assignment assignment, org.hibernate.Session hibSession) {
		delete((Object) assignment, hibSession);
	}

	public void refresh(Assignment assignment, org.hibernate.Session hibSession) {
		refresh((Object) assignment, hibSession);
	}

	public List<Assignment> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Assignment").list();
	}

	public List<Assignment> findByTimePattern(org.hibernate.Session hibSession, Long timePatternId) {
		return hibSession.createQuery("from Assignment x where x.timePattern.uniqueId = :timePatternId").setLong("timePatternId", timePatternId).list();
	}

	public List<Assignment> findBySolution(org.hibernate.Session hibSession, Long solutionId) {
		return hibSession.createQuery("from Assignment x where x.solution.uniqueId = :solutionId").setLong("solutionId", solutionId).list();
	}

	public List<Assignment> findByClazz(org.hibernate.Session hibSession, Long clazzId) {
		return hibSession.createQuery("from Assignment x where x.clazz.uniqueId = :clazzId").setLong("clazzId", clazzId).list();
	}
}
