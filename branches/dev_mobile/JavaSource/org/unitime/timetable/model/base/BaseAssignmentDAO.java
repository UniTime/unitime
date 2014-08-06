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

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.AssignmentDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseAssignmentDAO extends _RootDAO<Assignment,Long> {

	private static AssignmentDAO sInstance;

	public static AssignmentDAO getInstance() {
		if (sInstance == null) sInstance = new AssignmentDAO();
		return sInstance;
	}

	public Class<Assignment> getReferenceClass() {
		return Assignment.class;
	}

	@SuppressWarnings("unchecked")
	public List<Assignment> findByTimePattern(org.hibernate.Session hibSession, Long timePatternId) {
		return hibSession.createQuery("from Assignment x where x.timePattern.uniqueId = :timePatternId").setLong("timePatternId", timePatternId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Assignment> findByDatePattern(org.hibernate.Session hibSession, Long datePatternId) {
		return hibSession.createQuery("from Assignment x where x.datePattern.uniqueId = :datePatternId").setLong("datePatternId", datePatternId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Assignment> findBySolution(org.hibernate.Session hibSession, Long solutionId) {
		return hibSession.createQuery("from Assignment x where x.solution.uniqueId = :solutionId").setLong("solutionId", solutionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Assignment> findByClazz(org.hibernate.Session hibSession, Long clazzId) {
		return hibSession.createQuery("from Assignment x where x.clazz.uniqueId = :clazzId").setLong("clazzId", clazzId).list();
	}
}
