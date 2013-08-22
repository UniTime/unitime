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

import org.unitime.timetable.model.CurriculumProjectionRule;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CurriculumProjectionRuleDAO;

public abstract class BaseCurriculumProjectionRuleDAO extends _RootDAO<CurriculumProjectionRule,Long> {

	private static CurriculumProjectionRuleDAO sInstance;

	public static CurriculumProjectionRuleDAO getInstance() {
		if (sInstance == null) sInstance = new CurriculumProjectionRuleDAO();
		return sInstance;
	}

	public Class<CurriculumProjectionRule> getReferenceClass() {
		return CurriculumProjectionRule.class;
	}

	@SuppressWarnings("unchecked")
	public List<CurriculumProjectionRule> findByAcademicArea(org.hibernate.Session hibSession, Long academicAreaId) {
		return hibSession.createQuery("from CurriculumProjectionRule x where x.academicArea.uniqueId = :academicAreaId").setLong("academicAreaId", academicAreaId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CurriculumProjectionRule> findByMajor(org.hibernate.Session hibSession, Long majorId) {
		return hibSession.createQuery("from CurriculumProjectionRule x where x.major.uniqueId = :majorId").setLong("majorId", majorId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CurriculumProjectionRule> findByAcademicClassification(org.hibernate.Session hibSession, Long academicClassificationId) {
		return hibSession.createQuery("from CurriculumProjectionRule x where x.academicClassification.uniqueId = :academicClassificationId").setLong("academicClassificationId", academicClassificationId).list();
	}
}
