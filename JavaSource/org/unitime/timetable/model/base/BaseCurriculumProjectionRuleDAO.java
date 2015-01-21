/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.model.base;

import java.util.List;

import org.unitime.timetable.model.CurriculumProjectionRule;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CurriculumProjectionRuleDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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
