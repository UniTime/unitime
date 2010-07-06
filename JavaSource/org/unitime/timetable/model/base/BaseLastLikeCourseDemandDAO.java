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

import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.LastLikeCourseDemandDAO;

public abstract class BaseLastLikeCourseDemandDAO extends _RootDAO {

	private static LastLikeCourseDemandDAO sInstance;

	public static LastLikeCourseDemandDAO getInstance () {
		if (sInstance == null) sInstance = new LastLikeCourseDemandDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return LastLikeCourseDemand.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public LastLikeCourseDemand get(Long uniqueId) {
		return (LastLikeCourseDemand) get(getReferenceClass(), uniqueId);
	}

	public LastLikeCourseDemand get(Long uniqueId, org.hibernate.Session hibSession) {
		return (LastLikeCourseDemand) get(getReferenceClass(), uniqueId, hibSession);
	}

	public LastLikeCourseDemand load(Long uniqueId) {
		return (LastLikeCourseDemand) load(getReferenceClass(), uniqueId);
	}

	public LastLikeCourseDemand load(Long uniqueId, org.hibernate.Session hibSession) {
		return (LastLikeCourseDemand) load(getReferenceClass(), uniqueId, hibSession);
	}

	public LastLikeCourseDemand loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		LastLikeCourseDemand lastLikeCourseDemand = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(lastLikeCourseDemand)) Hibernate.initialize(lastLikeCourseDemand);
		return lastLikeCourseDemand;
	}

	public void save(LastLikeCourseDemand lastLikeCourseDemand) {
		save((Object) lastLikeCourseDemand);
	}

	public void save(LastLikeCourseDemand lastLikeCourseDemand, org.hibernate.Session hibSession) {
		save((Object) lastLikeCourseDemand, hibSession);
	}

	public void saveOrUpdate(LastLikeCourseDemand lastLikeCourseDemand) {
		saveOrUpdate((Object) lastLikeCourseDemand);
	}

	public void saveOrUpdate(LastLikeCourseDemand lastLikeCourseDemand, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) lastLikeCourseDemand, hibSession);
	}


	public void update(LastLikeCourseDemand lastLikeCourseDemand) {
		update((Object) lastLikeCourseDemand);
	}

	public void update(LastLikeCourseDemand lastLikeCourseDemand, org.hibernate.Session hibSession) {
		update((Object) lastLikeCourseDemand, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(LastLikeCourseDemand lastLikeCourseDemand) {
		delete((Object) lastLikeCourseDemand);
	}

	public void delete(LastLikeCourseDemand lastLikeCourseDemand, org.hibernate.Session hibSession) {
		delete((Object) lastLikeCourseDemand, hibSession);
	}

	public void refresh(LastLikeCourseDemand lastLikeCourseDemand, org.hibernate.Session hibSession) {
		refresh((Object) lastLikeCourseDemand, hibSession);
	}

	public List<LastLikeCourseDemand> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from LastLikeCourseDemand").list();
	}

	public List<LastLikeCourseDemand> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from LastLikeCourseDemand x where x.student.uniqueId = :studentId").setLong("studentId", studentId).list();
	}

	public List<LastLikeCourseDemand> findBySubjectArea(org.hibernate.Session hibSession, Long subjectAreaId) {
		return hibSession.createQuery("from LastLikeCourseDemand x where x.subjectArea.uniqueId = :subjectAreaId").setLong("subjectAreaId", subjectAreaId).list();
	}
}
