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

import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ChangeLogDAO;

/**
 * @author Tomas Muller
 */
public abstract class BaseChangeLogDAO extends _RootDAO<ChangeLog,Long> {

	private static ChangeLogDAO sInstance;

	public static ChangeLogDAO getInstance() {
		if (sInstance == null) sInstance = new ChangeLogDAO();
		return sInstance;
	}

	public Class<ChangeLog> getReferenceClass() {
		return ChangeLog.class;
	}

	@SuppressWarnings("unchecked")
	public List<ChangeLog> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from ChangeLog x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ChangeLog> findByManager(org.hibernate.Session hibSession, Long managerId) {
		return hibSession.createQuery("from ChangeLog x where x.manager.uniqueId = :managerId").setLong("managerId", managerId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ChangeLog> findBySubjectArea(org.hibernate.Session hibSession, Long subjectAreaId) {
		return hibSession.createQuery("from ChangeLog x where x.subjectArea.uniqueId = :subjectAreaId").setLong("subjectAreaId", subjectAreaId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ChangeLog> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from ChangeLog x where x.department.uniqueId = :departmentId").setLong("departmentId", departmentId).list();
	}
}
