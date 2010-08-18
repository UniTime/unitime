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

import org.unitime.timetable.model.CourseOfferingReservation;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseOfferingReservationDAO;

public abstract class BaseCourseOfferingReservationDAO extends _RootDAO<CourseOfferingReservation,Long> {

	private static CourseOfferingReservationDAO sInstance;

	public static CourseOfferingReservationDAO getInstance() {
		if (sInstance == null) sInstance = new CourseOfferingReservationDAO();
		return sInstance;
	}

	public Class<CourseOfferingReservation> getReferenceClass() {
		return CourseOfferingReservation.class;
	}

	@SuppressWarnings("unchecked")
	public List<CourseOfferingReservation> findByCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId) {
		return hibSession.createQuery("from CourseOfferingReservation x where x.courseOffering.uniqueId = :courseOfferingId").setLong("courseOfferingId", courseOfferingId).list();
	}
}
