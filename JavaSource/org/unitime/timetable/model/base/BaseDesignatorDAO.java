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

import org.unitime.timetable.model.Designator;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DesignatorDAO;

public abstract class BaseDesignatorDAO extends _RootDAO {

	private static DesignatorDAO sInstance;

	public static DesignatorDAO getInstance () {
		if (sInstance == null) sInstance = new DesignatorDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return Designator.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public Designator get(Long uniqueId) {
		return (Designator) get(getReferenceClass(), uniqueId);
	}

	public Designator get(Long uniqueId, org.hibernate.Session hibSession) {
		return (Designator) get(getReferenceClass(), uniqueId, hibSession);
	}

	public Designator load(Long uniqueId) {
		return (Designator) load(getReferenceClass(), uniqueId);
	}

	public Designator load(Long uniqueId, org.hibernate.Session hibSession) {
		return (Designator) load(getReferenceClass(), uniqueId, hibSession);
	}

	public Designator loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		Designator designator = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(designator)) Hibernate.initialize(designator);
		return designator;
	}

	public void save(Designator designator) {
		save((Object) designator);
	}

	public void save(Designator designator, org.hibernate.Session hibSession) {
		save((Object) designator, hibSession);
	}

	public void saveOrUpdate(Designator designator) {
		saveOrUpdate((Object) designator);
	}

	public void saveOrUpdate(Designator designator, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) designator, hibSession);
	}


	public void update(Designator designator) {
		update((Object) designator);
	}

	public void update(Designator designator, org.hibernate.Session hibSession) {
		update((Object) designator, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(Designator designator) {
		delete((Object) designator);
	}

	public void delete(Designator designator, org.hibernate.Session hibSession) {
		delete((Object) designator, hibSession);
	}

	public void refresh(Designator designator, org.hibernate.Session hibSession) {
		refresh((Object) designator, hibSession);
	}

	public List<Designator> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from Designator").list();
	}

	public List<Designator> findBySubjectArea(org.hibernate.Session hibSession, Long subjectAreaId) {
		return hibSession.createQuery("from Designator x where x.subjectArea.uniqueId = :subjectAreaId").setLong("subjectAreaId", subjectAreaId).list();
	}

	public List<Designator> findByInstructor(org.hibernate.Session hibSession, Long instructorId) {
		return hibSession.createQuery("from Designator x where x.instructor.uniqueId = :instructorId").setLong("instructorId", instructorId).list();
	}
}
