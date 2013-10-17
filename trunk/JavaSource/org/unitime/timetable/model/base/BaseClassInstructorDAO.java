/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ClassInstructorDAO;

/**
 * @author Tomas Muller
 */
public abstract class BaseClassInstructorDAO extends _RootDAO<ClassInstructor,Long> {

	private static ClassInstructorDAO sInstance;

	public static ClassInstructorDAO getInstance() {
		if (sInstance == null) sInstance = new ClassInstructorDAO();
		return sInstance;
	}

	public Class<ClassInstructor> getReferenceClass() {
		return ClassInstructor.class;
	}

	@SuppressWarnings("unchecked")
	public List<ClassInstructor> findByClassInstructing(org.hibernate.Session hibSession, Long classInstructingId) {
		return hibSession.createQuery("from ClassInstructor x where x.classInstructing.uniqueId = :classInstructingId").setLong("classInstructingId", classInstructingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ClassInstructor> findByInstructor(org.hibernate.Session hibSession, Long instructorId) {
		return hibSession.createQuery("from ClassInstructor x where x.instructor.uniqueId = :instructorId").setLong("instructorId", instructorId).list();
	}
}
