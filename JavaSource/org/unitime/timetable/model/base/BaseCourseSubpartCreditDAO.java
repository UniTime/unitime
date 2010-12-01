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

import org.unitime.timetable.model.CourseSubpartCredit;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseSubpartCreditDAO;

public abstract class BaseCourseSubpartCreditDAO extends _RootDAO<CourseSubpartCredit,Long> {

	private static CourseSubpartCreditDAO sInstance;

	public static CourseSubpartCreditDAO getInstance() {
		if (sInstance == null) sInstance = new CourseSubpartCreditDAO();
		return sInstance;
	}

	public Class<CourseSubpartCredit> getReferenceClass() {
		return CourseSubpartCredit.class;
	}

	@SuppressWarnings("unchecked")
	public List<CourseSubpartCredit> findByCourseCatalog(org.hibernate.Session hibSession, Long courseCatalogId) {
		return hibSession.createQuery("from CourseSubpartCredit x where x.courseCatalog.uniqueId = :courseCatalogId").setLong("courseCatalogId", courseCatalogId).list();
	}
}
