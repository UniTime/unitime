/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.model.base;

import java.util.List;

import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseRequestDAO;

public abstract class BaseCourseRequestDAO extends _RootDAO<CourseRequest,Long> {

	private static CourseRequestDAO sInstance;

	public static CourseRequestDAO getInstance() {
		if (sInstance == null) sInstance = new CourseRequestDAO();
		return sInstance;
	}

	public Class<CourseRequest> getReferenceClass() {
		return CourseRequest.class;
	}

	@SuppressWarnings("unchecked")
	public List<CourseRequest> findByCourseDemand(org.hibernate.Session hibSession, Long courseDemandId) {
		return hibSession.createQuery("from CourseRequest x where x.courseDemand.uniqueId = :courseDemandId").setLong("courseDemandId", courseDemandId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseRequest> findByCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId) {
		return hibSession.createQuery("from CourseRequest x where x.courseOffering.uniqueId = :courseOfferingId").setLong("courseOfferingId", courseOfferingId).list();
	}
}
