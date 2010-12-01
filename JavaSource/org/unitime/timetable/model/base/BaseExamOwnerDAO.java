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

import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamOwnerDAO;

public abstract class BaseExamOwnerDAO extends _RootDAO<ExamOwner,Long> {

	private static ExamOwnerDAO sInstance;

	public static ExamOwnerDAO getInstance() {
		if (sInstance == null) sInstance = new ExamOwnerDAO();
		return sInstance;
	}

	public Class<ExamOwner> getReferenceClass() {
		return ExamOwner.class;
	}

	@SuppressWarnings("unchecked")
	public List<ExamOwner> findByExam(org.hibernate.Session hibSession, Long examId) {
		return hibSession.createQuery("from ExamOwner x where x.exam.uniqueId = :examId").setLong("examId", examId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ExamOwner> findByCourse(org.hibernate.Session hibSession, Long courseId) {
		return hibSession.createQuery("from ExamOwner x where x.course.uniqueId = :courseId").setLong("courseId", courseId).list();
	}
}
