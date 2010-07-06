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

import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseDemandDAO;

public abstract class BaseCourseDemandDAO extends _RootDAO {

	private static CourseDemandDAO sInstance;

	public static CourseDemandDAO getInstance () {
		if (sInstance == null) sInstance = new CourseDemandDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseDemand.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseDemand get(Long uniqueId) {
		return (CourseDemand) get(getReferenceClass(), uniqueId);
	}

	public CourseDemand get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseDemand) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseDemand load(Long uniqueId) {
		return (CourseDemand) load(getReferenceClass(), uniqueId);
	}

	public CourseDemand load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseDemand) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseDemand loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseDemand courseDemand = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseDemand)) Hibernate.initialize(courseDemand);
		return courseDemand;
	}

	public void save(CourseDemand courseDemand) {
		save((Object) courseDemand);
	}

	public void save(CourseDemand courseDemand, org.hibernate.Session hibSession) {
		save((Object) courseDemand, hibSession);
	}

	public void saveOrUpdate(CourseDemand courseDemand) {
		saveOrUpdate((Object) courseDemand);
	}

	public void saveOrUpdate(CourseDemand courseDemand, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseDemand, hibSession);
	}


	public void update(CourseDemand courseDemand) {
		update((Object) courseDemand);
	}

	public void update(CourseDemand courseDemand, org.hibernate.Session hibSession) {
		update((Object) courseDemand, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseDemand courseDemand) {
		delete((Object) courseDemand);
	}

	public void delete(CourseDemand courseDemand, org.hibernate.Session hibSession) {
		delete((Object) courseDemand, hibSession);
	}

	public void refresh(CourseDemand courseDemand, org.hibernate.Session hibSession) {
		refresh((Object) courseDemand, hibSession);
	}

	public List<CourseDemand> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseDemand").list();
	}

	public List<CourseDemand> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from CourseDemand x where x.student.uniqueId = :studentId").setLong("studentId", studentId).list();
	}

	public List<CourseDemand> findByFreeTime(org.hibernate.Session hibSession, Long freeTimeId) {
		return hibSession.createQuery("from CourseDemand x where x.freeTime.uniqueId = :freeTimeId").setLong("freeTimeId", freeTimeId).list();
	}
}
