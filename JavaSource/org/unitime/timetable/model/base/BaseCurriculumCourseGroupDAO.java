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

import org.unitime.timetable.model.CurriculumCourseGroup;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CurriculumCourseGroupDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCurriculumCourseGroupDAO extends _RootDAO<CurriculumCourseGroup,Long> {

	private static CurriculumCourseGroupDAO sInstance;

	public static CurriculumCourseGroupDAO getInstance() {
		if (sInstance == null) sInstance = new CurriculumCourseGroupDAO();
		return sInstance;
	}

	public Class<CurriculumCourseGroup> getReferenceClass() {
		return CurriculumCourseGroup.class;
	}

	@SuppressWarnings("unchecked")
	public List<CurriculumCourseGroup> findByCurriculum(org.hibernate.Session hibSession, Long curriculumId) {
		return hibSession.createQuery("from CurriculumCourseGroup x where x.curriculum.uniqueId = :curriculumId").setLong("curriculumId", curriculumId).list();
	}
}
