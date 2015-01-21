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

import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ChangeLogDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseChangeLogDAO extends _RootDAO<ChangeLog,Long> {

	private static ChangeLogDAO sInstance;

	public static ChangeLogDAO getInstance() {
		if (sInstance == null) sInstance = new ChangeLogDAO();
		return sInstance;
	}

	public Class<ChangeLog> getReferenceClass() {
		return ChangeLog.class;
	}

	@SuppressWarnings("unchecked")
	public List<ChangeLog> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from ChangeLog x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ChangeLog> findByManager(org.hibernate.Session hibSession, Long managerId) {
		return hibSession.createQuery("from ChangeLog x where x.manager.uniqueId = :managerId").setLong("managerId", managerId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ChangeLog> findBySubjectArea(org.hibernate.Session hibSession, Long subjectAreaId) {
		return hibSession.createQuery("from ChangeLog x where x.subjectArea.uniqueId = :subjectAreaId").setLong("subjectAreaId", subjectAreaId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ChangeLog> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from ChangeLog x where x.department.uniqueId = :departmentId").setLong("departmentId", departmentId).list();
	}
}
