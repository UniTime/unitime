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

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.Class_DAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseClass_DAO extends _RootDAO<Class_,Long> {

	private static Class_DAO sInstance;

	public static Class_DAO getInstance() {
		if (sInstance == null) sInstance = new Class_DAO();
		return sInstance;
	}

	public Class<Class_> getReferenceClass() {
		return Class_.class;
	}

	@SuppressWarnings("unchecked")
	public List<Class_> findByManagingDept(org.hibernate.Session hibSession, Long managingDeptId) {
		return hibSession.createQuery("from Class_ x where x.managingDept.uniqueId = :managingDeptId").setLong("managingDeptId", managingDeptId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Class_> findBySchedulingSubpart(org.hibernate.Session hibSession, Long schedulingSubpartId) {
		return hibSession.createQuery("from Class_ x where x.schedulingSubpart.uniqueId = :schedulingSubpartId").setLong("schedulingSubpartId", schedulingSubpartId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Class_> findByParentClass(org.hibernate.Session hibSession, Long parentClassId) {
		return hibSession.createQuery("from Class_ x where x.parentClass.uniqueId = :parentClassId").setLong("parentClassId", parentClassId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Class_> findByDatePattern(org.hibernate.Session hibSession, Long datePatternId) {
		return hibSession.createQuery("from Class_ x where x.datePattern.uniqueId = :datePatternId").setLong("datePatternId", datePatternId).list();
	}
}
