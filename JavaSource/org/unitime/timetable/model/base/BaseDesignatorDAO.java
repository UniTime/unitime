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

import org.unitime.timetable.model.Designator;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DesignatorDAO;

public abstract class BaseDesignatorDAO extends _RootDAO<Designator,Long> {

	private static DesignatorDAO sInstance;

	public static DesignatorDAO getInstance() {
		if (sInstance == null) sInstance = new DesignatorDAO();
		return sInstance;
	}

	public Class<Designator> getReferenceClass() {
		return Designator.class;
	}

	@SuppressWarnings("unchecked")
	public List<Designator> findBySubjectArea(org.hibernate.Session hibSession, Long subjectAreaId) {
		return hibSession.createQuery("from Designator x where x.subjectArea.uniqueId = :subjectAreaId").setLong("subjectAreaId", subjectAreaId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Designator> findByInstructor(org.hibernate.Session hibSession, Long instructorId) {
		return hibSession.createQuery("from Designator x where x.instructor.uniqueId = :instructorId").setLong("instructorId", instructorId).list();
	}
}
