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

import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;

public abstract class BaseSubjectAreaDAO extends _RootDAO {

	private static SubjectAreaDAO sInstance;

	public static SubjectAreaDAO getInstance () {
		if (sInstance == null) sInstance = new SubjectAreaDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return SubjectArea.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public SubjectArea get(Long uniqueId) {
		return (SubjectArea) get(getReferenceClass(), uniqueId);
	}

	public SubjectArea get(Long uniqueId, org.hibernate.Session hibSession) {
		return (SubjectArea) get(getReferenceClass(), uniqueId, hibSession);
	}

	public SubjectArea load(Long uniqueId) {
		return (SubjectArea) load(getReferenceClass(), uniqueId);
	}

	public SubjectArea load(Long uniqueId, org.hibernate.Session hibSession) {
		return (SubjectArea) load(getReferenceClass(), uniqueId, hibSession);
	}

	public SubjectArea loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		SubjectArea subjectArea = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(subjectArea)) Hibernate.initialize(subjectArea);
		return subjectArea;
	}

	public void save(SubjectArea subjectArea) {
		save((Object) subjectArea);
	}

	public void save(SubjectArea subjectArea, org.hibernate.Session hibSession) {
		save((Object) subjectArea, hibSession);
	}

	public void saveOrUpdate(SubjectArea subjectArea) {
		saveOrUpdate((Object) subjectArea);
	}

	public void saveOrUpdate(SubjectArea subjectArea, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) subjectArea, hibSession);
	}


	public void update(SubjectArea subjectArea) {
		update((Object) subjectArea);
	}

	public void update(SubjectArea subjectArea, org.hibernate.Session hibSession) {
		update((Object) subjectArea, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(SubjectArea subjectArea) {
		delete((Object) subjectArea);
	}

	public void delete(SubjectArea subjectArea, org.hibernate.Session hibSession) {
		delete((Object) subjectArea, hibSession);
	}

	public void refresh(SubjectArea subjectArea, org.hibernate.Session hibSession) {
		refresh((Object) subjectArea, hibSession);
	}

	public List<SubjectArea> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from SubjectArea").list();
	}

	public List<SubjectArea> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from SubjectArea x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	public List<SubjectArea> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from SubjectArea x where x.department.uniqueId = :departmentId").setLong("departmentId", departmentId).list();
	}
}
