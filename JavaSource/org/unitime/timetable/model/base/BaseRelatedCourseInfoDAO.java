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

import org.unitime.timetable.model.RelatedCourseInfo;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.RelatedCourseInfoDAO;

public abstract class BaseRelatedCourseInfoDAO extends _RootDAO {

	private static RelatedCourseInfoDAO sInstance;

	public static RelatedCourseInfoDAO getInstance () {
		if (sInstance == null) sInstance = new RelatedCourseInfoDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return RelatedCourseInfo.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public RelatedCourseInfo get(Long uniqueId) {
		return (RelatedCourseInfo) get(getReferenceClass(), uniqueId);
	}

	public RelatedCourseInfo get(Long uniqueId, org.hibernate.Session hibSession) {
		return (RelatedCourseInfo) get(getReferenceClass(), uniqueId, hibSession);
	}

	public RelatedCourseInfo load(Long uniqueId) {
		return (RelatedCourseInfo) load(getReferenceClass(), uniqueId);
	}

	public RelatedCourseInfo load(Long uniqueId, org.hibernate.Session hibSession) {
		return (RelatedCourseInfo) load(getReferenceClass(), uniqueId, hibSession);
	}

	public RelatedCourseInfo loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		RelatedCourseInfo relatedCourseInfo = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(relatedCourseInfo)) Hibernate.initialize(relatedCourseInfo);
		return relatedCourseInfo;
	}

	public void save(RelatedCourseInfo relatedCourseInfo) {
		save((Object) relatedCourseInfo);
	}

	public void save(RelatedCourseInfo relatedCourseInfo, org.hibernate.Session hibSession) {
		save((Object) relatedCourseInfo, hibSession);
	}

	public void saveOrUpdate(RelatedCourseInfo relatedCourseInfo) {
		saveOrUpdate((Object) relatedCourseInfo);
	}

	public void saveOrUpdate(RelatedCourseInfo relatedCourseInfo, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) relatedCourseInfo, hibSession);
	}


	public void update(RelatedCourseInfo relatedCourseInfo) {
		update((Object) relatedCourseInfo);
	}

	public void update(RelatedCourseInfo relatedCourseInfo, org.hibernate.Session hibSession) {
		update((Object) relatedCourseInfo, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(RelatedCourseInfo relatedCourseInfo) {
		delete((Object) relatedCourseInfo);
	}

	public void delete(RelatedCourseInfo relatedCourseInfo, org.hibernate.Session hibSession) {
		delete((Object) relatedCourseInfo, hibSession);
	}

	public void refresh(RelatedCourseInfo relatedCourseInfo, org.hibernate.Session hibSession) {
		refresh((Object) relatedCourseInfo, hibSession);
	}

	public List<RelatedCourseInfo> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from RelatedCourseInfo").list();
	}

	public List<RelatedCourseInfo> findByEvent(org.hibernate.Session hibSession, Long eventId) {
		return hibSession.createQuery("from RelatedCourseInfo x where x.event.uniqueId = :eventId").setLong("eventId", eventId).list();
	}

	public List<RelatedCourseInfo> findByCourse(org.hibernate.Session hibSession, Long courseId) {
		return hibSession.createQuery("from RelatedCourseInfo x where x.course.uniqueId = :courseId").setLong("courseId", courseId).list();
	}
}
