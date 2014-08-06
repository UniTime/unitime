/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseDepartmentalInstructorDAO extends _RootDAO<DepartmentalInstructor,Long> {

	private static DepartmentalInstructorDAO sInstance;

	public static DepartmentalInstructorDAO getInstance() {
		if (sInstance == null) sInstance = new DepartmentalInstructorDAO();
		return sInstance;
	}

	public Class<DepartmentalInstructor> getReferenceClass() {
		return DepartmentalInstructor.class;
	}

	@SuppressWarnings("unchecked")
	public List<DepartmentalInstructor> findByPositionType(org.hibernate.Session hibSession, Long positionTypeId) {
		return hibSession.createQuery("from DepartmentalInstructor x where x.positionType.uniqueId = :positionTypeId").setLong("positionTypeId", positionTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<DepartmentalInstructor> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from DepartmentalInstructor x where x.department.uniqueId = :departmentId").setLong("departmentId", departmentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<DepartmentalInstructor> findByRole(org.hibernate.Session hibSession, Long roleId) {
		return hibSession.createQuery("from DepartmentalInstructor x where x.role.roleId = :roleId").setLong("roleId", roleId).list();
	}
}
