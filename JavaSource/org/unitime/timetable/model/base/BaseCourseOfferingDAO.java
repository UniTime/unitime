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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;

public abstract class BaseCourseOfferingDAO extends _RootDAO {

	private static CourseOfferingDAO sInstance;

	public static CourseOfferingDAO getInstance () {
		if (sInstance == null) sInstance = new CourseOfferingDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseOffering.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseOffering get(Long uniqueId) {
		return (CourseOffering) get(getReferenceClass(), uniqueId);
	}

	public CourseOffering get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseOffering) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseOffering load(Long uniqueId) {
		return (CourseOffering) load(getReferenceClass(), uniqueId);
	}

	public CourseOffering load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseOffering) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseOffering loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseOffering courseOffering = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseOffering)) Hibernate.initialize(courseOffering);
		return courseOffering;
	}

	public void save(CourseOffering courseOffering) {
		save((Object) courseOffering);
	}

	public void save(CourseOffering courseOffering, org.hibernate.Session hibSession) {
		save((Object) courseOffering, hibSession);
	}

	public void saveOrUpdate(CourseOffering courseOffering) {
		saveOrUpdate((Object) courseOffering);
	}

	public void saveOrUpdate(CourseOffering courseOffering, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseOffering, hibSession);
	}


	public void update(CourseOffering courseOffering) {
		update((Object) courseOffering);
	}

	public void update(CourseOffering courseOffering, org.hibernate.Session hibSession) {
		update((Object) courseOffering, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseOffering courseOffering) {
		delete((Object) courseOffering);
	}

	public void delete(CourseOffering courseOffering, org.hibernate.Session hibSession) {
		delete((Object) courseOffering, hibSession);
	}

	public void refresh(CourseOffering courseOffering, org.hibernate.Session hibSession) {
		refresh((Object) courseOffering, hibSession);
	}

	public List<CourseOffering> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseOffering").list();
	}

	public List<CourseOffering> findBySubjectArea(org.hibernate.Session hibSession, Long subjectAreaId) {
		return hibSession.createQuery("from CourseOffering x where x.subjectArea.uniqueId = :subjectAreaId").setLong("subjectAreaId", subjectAreaId).list();
	}

	public List<CourseOffering> findByInstructionalOffering(org.hibernate.Session hibSession, Long instructionalOfferingId) {
		return hibSession.createQuery("from CourseOffering x where x.instructionalOffering.uniqueId = :instructionalOfferingId").setLong("instructionalOfferingId", instructionalOfferingId).list();
	}

	public List<CourseOffering> findByDemandOffering(org.hibernate.Session hibSession, Long demandOfferingId) {
		return hibSession.createQuery("from CourseOffering x where x.demandOffering.uniqueId = :demandOfferingId").setLong("demandOfferingId", demandOfferingId).list();
	}

	public List<CourseOffering> findByDemandOfferingType(org.hibernate.Session hibSession, Long demandOfferingTypeId) {
		return hibSession.createQuery("from CourseOffering x where x.demandOfferingType.uniqueId = :demandOfferingTypeId").setLong("demandOfferingTypeId", demandOfferingTypeId).list();
	}
}
