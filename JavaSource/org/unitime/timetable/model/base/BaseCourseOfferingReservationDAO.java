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

import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseOfferingReservationDAO;

public abstract class BaseCourseOfferingReservationDAO extends _RootDAO {

	private static CourseOfferingReservationDAO sInstance;

	public static CourseOfferingReservationDAO getInstance () {
		if (sInstance == null) sInstance = new CourseOfferingReservationDAO();
		return sInstance;
	}

	public Class getReferenceClass () {
		return CourseOfferingReservation.class;
	}

	public Order getDefaultOrder () {
		return null;
	}

	public CourseOfferingReservation get(Long uniqueId) {
		return (CourseOfferingReservation) get(getReferenceClass(), uniqueId);
	}

	public CourseOfferingReservation get(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseOfferingReservation) get(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseOfferingReservation load(Long uniqueId) {
		return (CourseOfferingReservation) load(getReferenceClass(), uniqueId);
	}

	public CourseOfferingReservation load(Long uniqueId, org.hibernate.Session hibSession) {
		return (CourseOfferingReservation) load(getReferenceClass(), uniqueId, hibSession);
	}

	public CourseOfferingReservation loadInitialize(Long uniqueId, org.hibernate.Session hibSession) {
		CourseOfferingReservation courseOfferingReservation = load(uniqueId, hibSession);
		if (!Hibernate.isInitialized(courseOfferingReservation)) Hibernate.initialize(courseOfferingReservation);
		return courseOfferingReservation;
	}

	public void save(CourseOfferingReservation courseOfferingReservation) {
		save((Object) courseOfferingReservation);
	}

	public void save(CourseOfferingReservation courseOfferingReservation, org.hibernate.Session hibSession) {
		save((Object) courseOfferingReservation, hibSession);
	}

	public void saveOrUpdate(CourseOfferingReservation courseOfferingReservation) {
		saveOrUpdate((Object) courseOfferingReservation);
	}

	public void saveOrUpdate(CourseOfferingReservation courseOfferingReservation, org.hibernate.Session hibSession) {
		saveOrUpdate((Object) courseOfferingReservation, hibSession);
	}


	public void update(CourseOfferingReservation courseOfferingReservation) {
		update((Object) courseOfferingReservation);
	}

	public void update(CourseOfferingReservation courseOfferingReservation, org.hibernate.Session hibSession) {
		update((Object) courseOfferingReservation, hibSession);
	}

	public void delete(Long uniqueId) {
		delete(load(uniqueId));
	}

	public void delete(Long uniqueId, org.hibernate.Session hibSession) {
		delete(load(uniqueId, hibSession), hibSession);
	}

	public void delete(CourseOfferingReservation courseOfferingReservation) {
		delete((Object) courseOfferingReservation);
	}

	public void delete(CourseOfferingReservation courseOfferingReservation, org.hibernate.Session hibSession) {
		delete((Object) courseOfferingReservation, hibSession);
	}

	public void refresh(CourseOfferingReservation courseOfferingReservation, org.hibernate.Session hibSession) {
		refresh((Object) courseOfferingReservation, hibSession);
	}

	public List<CourseOfferingReservation> findAll(org.hibernate.Session hibSession) {
		return hibSession.createQuery("from CourseOfferingReservation").list();
	}

	public List<CourseOfferingReservation> findByCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId) {
		return hibSession.createQuery("from CourseOfferingReservation x where x.courseOffering.uniqueId = :courseOfferingId").setLong("courseOfferingId", courseOfferingId).list();
	}
}
