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

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;

/**
 * @author Tomas Muller
 */
public abstract class BaseDepartmentDAO extends _RootDAO<Department,Long> {

	private static DepartmentDAO sInstance;

	public static DepartmentDAO getInstance() {
		if (sInstance == null) sInstance = new DepartmentDAO();
		return sInstance;
	}

	public Class<Department> getReferenceClass() {
		return Department.class;
	}

	@SuppressWarnings("unchecked")
	public List<Department> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from Department x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Department> findByStatusType(org.hibernate.Session hibSession, Long statusTypeId) {
		return hibSession.createQuery("from Department x where x.statusType.uniqueId = :statusTypeId").setLong("statusTypeId", statusTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Department> findBySolverGroup(org.hibernate.Session hibSession, Long solverGroupId) {
		return hibSession.createQuery("from Department x where x.solverGroup.uniqueId = :solverGroupId").setLong("solverGroupId", solverGroupId).list();
	}
}
