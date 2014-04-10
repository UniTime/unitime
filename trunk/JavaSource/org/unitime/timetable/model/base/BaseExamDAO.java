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

import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseExamDAO extends _RootDAO<Exam,Long> {

	private static ExamDAO sInstance;

	public static ExamDAO getInstance() {
		if (sInstance == null) sInstance = new ExamDAO();
		return sInstance;
	}

	public Class<Exam> getReferenceClass() {
		return Exam.class;
	}

	@SuppressWarnings("unchecked")
	public List<Exam> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from Exam x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Exam> findByAssignedPeriod(org.hibernate.Session hibSession, Long assignedPeriodId) {
		return hibSession.createQuery("from Exam x where x.assignedPeriod.uniqueId = :assignedPeriodId").setLong("assignedPeriodId", assignedPeriodId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Exam> findByExamType(org.hibernate.Session hibSession, Long examTypeId) {
		return hibSession.createQuery("from Exam x where x.examType.uniqueId = :examTypeId").setLong("examTypeId", examTypeId).list();
	}
}
