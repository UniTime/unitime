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

import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamLocationPrefDAO;

/**
 * @author Tomas Muller
 */
public abstract class BaseExamLocationPrefDAO extends _RootDAO<ExamLocationPref,Long> {

	private static ExamLocationPrefDAO sInstance;

	public static ExamLocationPrefDAO getInstance() {
		if (sInstance == null) sInstance = new ExamLocationPrefDAO();
		return sInstance;
	}

	public Class<ExamLocationPref> getReferenceClass() {
		return ExamLocationPref.class;
	}

	@SuppressWarnings("unchecked")
	public List<ExamLocationPref> findByLocation(org.hibernate.Session hibSession, Long locationId) {
		return hibSession.createQuery("from ExamLocationPref x where x.location.uniqueId = :locationId").setLong("locationId", locationId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ExamLocationPref> findByPrefLevel(org.hibernate.Session hibSession, Long prefLevelId) {
		return hibSession.createQuery("from ExamLocationPref x where x.prefLevel.uniqueId = :prefLevelId").setLong("prefLevelId", prefLevelId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ExamLocationPref> findByExamPeriod(org.hibernate.Session hibSession, Long examPeriodId) {
		return hibSession.createQuery("from ExamLocationPref x where x.examPeriod.uniqueId = :examPeriodId").setLong("examPeriodId", examPeriodId).list();
	}
}
