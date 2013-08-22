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

import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.LastLikeCourseDemandDAO;

public abstract class BaseLastLikeCourseDemandDAO extends _RootDAO<LastLikeCourseDemand,Long> {

	private static LastLikeCourseDemandDAO sInstance;

	public static LastLikeCourseDemandDAO getInstance() {
		if (sInstance == null) sInstance = new LastLikeCourseDemandDAO();
		return sInstance;
	}

	public Class<LastLikeCourseDemand> getReferenceClass() {
		return LastLikeCourseDemand.class;
	}

	@SuppressWarnings("unchecked")
	public List<LastLikeCourseDemand> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from LastLikeCourseDemand x where x.student.uniqueId = :studentId").setLong("studentId", studentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<LastLikeCourseDemand> findBySubjectArea(org.hibernate.Session hibSession, Long subjectAreaId) {
		return hibSession.createQuery("from LastLikeCourseDemand x where x.subjectArea.uniqueId = :subjectAreaId").setLong("subjectAreaId", subjectAreaId).list();
	}
}
