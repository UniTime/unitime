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

import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamLocationPrefDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
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
