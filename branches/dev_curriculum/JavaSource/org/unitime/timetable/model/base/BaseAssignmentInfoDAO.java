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

import org.unitime.timetable.model.AssignmentInfo;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AssignmentInfoDAO;

public abstract class BaseAssignmentInfoDAO extends _RootDAO {

	private static AssignmentInfoDAO sInstance;

	public static AssignmentInfoDAO getInstance () {
		if (sInstance == null) sInstance = new AssignmentInfoDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return AssignmentInfo.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public AssignmentInfo get(Long uniqueId) {
		return (AssignmentInfo) get(getReferenceClass(), uniqueId);
	}

	public AssignmentInfo get(Long uniqueId, org.hibernate.Session hibSession) {
		return (AssignmentInfo) get(getReferenceClass(), uniqueId, hibSession);
	}

	public AssignmentInfo load(Long uniqueId) {
		return (AssignmentInfo) load(getReferenceClass(), uniqueId);
	}

	public AssignmentInfo load(Long uniqueId, org.hibernate.Session hibSession) {
		return (AssignmentInfo) load(getReferenceClass(), uniqueId, hibSession);
	}

	public AssignmentInfo loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		AssignmentInfo assignmentInfo = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(assignmentInfo)) Hibernate.initialize(assignmentInfo);
		return assignmentInfo;
	}

	public void save(AssignmentInfo assignmentInfo) {
		save((Object) assignmentInfo);
	}

	public void save(AssignmentInfo assignmentInfo, org.hibernate.Session hibSession) {
		save((Object) assignmentInfo, hibSession);
	}

	public void saveOrUpdate(AssignmentInfo assignmentInfo) {
		saveOrUpdate((Object) assignmentInfo);
	}

	public void saveOrUpdate(AssignmentInfo assignmentInfo, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) assignmentInfo, hibSession);
	}


	public void update(AssignmentInfo assignmentInfo) {
		update((Object) assignmentInfo);
	}

	public void update(AssignmentInfo assignmentInfo, org.hibernate.Session hibSession) {
		update((Object) assignmentInfo, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(AssignmentInfo assignmentInfo) {
		delete((Object) assignmentInfo);
	}

	public void delete(AssignmentInfo assignmentInfo, org.hibernate.Session hibSession) {
		delete((Object) assignmentInfo, hibSession);
	}

	public void refresh(AssignmentInfo assignmentInfo, org.hibernate.Session hibSession) {
		refresh((Object) assignmentInfo, hibSession);
	}

	public List<AssignmentInfo> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from AssignmentInfo").list();
	}

	public List<AssignmentInfo> findByAssignment(org.hibernate.Session hibSession, Long assignmentId) {
		return hibSession.createQuery("from AssignmentInfo x where x.assignment.uniqueId = :assignmentId").setLong("assignmentId", assignmentId).list();
	}
}
