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

import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.CourseCreditUnitConfigDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseCourseCreditUnitConfigDAO extends _RootDAO<CourseCreditUnitConfig,Long> {

	private static CourseCreditUnitConfigDAO sInstance;

	public static CourseCreditUnitConfigDAO getInstance() {
		if (sInstance == null) sInstance = new CourseCreditUnitConfigDAO();
		return sInstance;
	}

	public Class<CourseCreditUnitConfig> getReferenceClass() {
		return CourseCreditUnitConfig.class;
	}

	@SuppressWarnings("unchecked")
	public List<CourseCreditUnitConfig> findByCourseCreditFormat(org.hibernate.Session hibSession, Long courseCreditFormatId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.courseCreditFormat.uniqueId = :courseCreditFormatId").setLong("courseCreditFormatId", courseCreditFormatId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseCreditUnitConfig> findByCreditType(org.hibernate.Session hibSession, Long creditTypeId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.creditType.uniqueId = :creditTypeId").setLong("creditTypeId", creditTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseCreditUnitConfig> findByCreditUnitType(org.hibernate.Session hibSession, Long creditUnitTypeId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.creditUnitType.uniqueId = :creditUnitTypeId").setLong("creditUnitTypeId", creditUnitTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseCreditUnitConfig> findBySubpartOwner(org.hibernate.Session hibSession, Long subpartOwnerId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.subpartOwner.uniqueId = :subpartOwnerId").setLong("subpartOwnerId", subpartOwnerId).list();
	}

	@SuppressWarnings("unchecked")
	public List<CourseCreditUnitConfig> findByCourseOwner(org.hibernate.Session hibSession, Long courseOwnerId) {
		return hibSession.createQuery("from CourseCreditUnitConfig x where x.courseOwner.uniqueId = :courseOwnerId").setLong("courseOwnerId", courseOwnerId).list();
	}
}
