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

import org.unitime.timetable.model.JointEnrollment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.JointEnrollmentDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseJointEnrollmentDAO extends _RootDAO<JointEnrollment,Long> {

	private static JointEnrollmentDAO sInstance;

	public static JointEnrollmentDAO getInstance() {
		if (sInstance == null) sInstance = new JointEnrollmentDAO();
		return sInstance;
	}

	public Class<JointEnrollment> getReferenceClass() {
		return JointEnrollment.class;
	}

	@SuppressWarnings("unchecked")
	public List<JointEnrollment> findBySolution(org.hibernate.Session hibSession, Long solutionId) {
		return hibSession.createQuery("from JointEnrollment x where x.solution.uniqueId = :solutionId").setLong("solutionId", solutionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<JointEnrollment> findByClass1(org.hibernate.Session hibSession, Long class1Id) {
		return hibSession.createQuery("from JointEnrollment x where x.class1.uniqueId = :class1Id").setLong("class1Id", class1Id).list();
	}

	@SuppressWarnings("unchecked")
	public List<JointEnrollment> findByClass2(org.hibernate.Session hibSession, Long class2Id) {
		return hibSession.createQuery("from JointEnrollment x where x.class2.uniqueId = :class2Id").setLong("class2Id", class2Id).list();
	}
}
