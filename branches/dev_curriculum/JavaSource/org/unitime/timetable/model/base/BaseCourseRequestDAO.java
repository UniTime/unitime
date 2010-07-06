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

import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseRequestDAO;

public abstract class BaseCourseRequestDAO extends _RootDAO {

	private static CourseRequestDAO sInstance;

	public static CourseRequestDAO getInstance () {
		if (sInstance == null) sInstance = new CourseRequestDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseRequest.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseRequest get(Long uniqueId) {
		return (CourseRequest) get(getReferenceClass(), uniqueId);
	}

	public CourseRequest get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseRequest) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseRequest load(Long uniqueId) {
		return (CourseRequest) load(getReferenceClass(), uniqueId);
	}

	public CourseRequest load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseRequest) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseRequest loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseRequest courseRequest = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseRequest)) Hibernate.initialize(courseRequest);
		return courseRequest;
	}

	public void save(CourseRequest courseRequest) {
		save((Object) courseRequest);
	}

	public void save(CourseRequest courseRequest, org.hibernate.Session hibSession) {
		save((Object) courseRequest, hibSession);
	}

	public void saveOrUpdate(CourseRequest courseRequest) {
		saveOrUpdate((Object) courseRequest);
	}

	public void saveOrUpdate(CourseRequest courseRequest, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseRequest, hibSession);
	}


	public void update(CourseRequest courseRequest) {
		update((Object) courseRequest);
	}

	public void update(CourseRequest courseRequest, org.hibernate.Session hibSession) {
		update((Object) courseRequest, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseRequest courseRequest) {
		delete((Object) courseRequest);
	}

	public void delete(CourseRequest courseRequest, org.hibernate.Session hibSession) {
		delete((Object) courseRequest, hibSession);
	}

	public void refresh(CourseRequest courseRequest, org.hibernate.Session hibSession) {
		refresh((Object) courseRequest, hibSession);
	}

	public List<CourseRequest> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseRequest").list();
	}

	public List<CourseRequest> findByCourseDemand(org.hibernate.Session hibSession, Long courseDemandId) {
		return hibSession.createQuery("from CourseRequest x where x.courseDemand.uniqueId = :courseDemandId").setLong("courseDemandId", courseDemandId).list();
	}

	public List<CourseRequest> findByCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId) {
		return hibSession.createQuery("from CourseRequest x where x.courseOffering.uniqueId = :courseOfferingId").setLong("courseOfferingId", courseOfferingId).list();
	}
}
