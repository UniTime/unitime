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

import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CurriculumClassificationDAO;

public abstract class BaseCurriculumClassificationDAO extends _RootDAO<CurriculumClassification,Long> {

	private static CurriculumClassificationDAO sInstance;

	public static CurriculumClassificationDAO getInstance() {
		if (sInstance == null) sInstance = new CurriculumClassificationDAO();
		return sInstance;
	}

	public Class<CurriculumClassification> getReferenceClass() {
		return CurriculumClassification.class;
	}

	@SuppressWarnings("unchecked")
	public List<CurriculumClassification> findByCurriculum(org.hibernate.Session hibSession, Long curriculumId) {
		return hibSession.createQuery("from CurriculumClassification x where x.curriculum.uniqueId = :curriculumId").setLong("curriculumId", curriculumId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CurriculumClassification> findByAcademicClassification(org.hibernate.Session hibSession, Long academicClassificationId) {
		return hibSession.createQuery("from CurriculumClassification x where x.academicClassification.uniqueId = :academicClassificationId").setLong("academicClassificationId", academicClassificationId).list();
	}
}
