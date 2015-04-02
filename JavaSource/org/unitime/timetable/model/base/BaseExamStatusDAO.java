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

import java.io.Serializable;
import java.util.List;

import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ExamStatusDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseExamStatusDAO extends _RootDAO<ExamStatus,Serializable> {

	private static ExamStatusDAO sInstance;

	public static ExamStatusDAO getInstance() {
		if (sInstance == null) sInstance = new ExamStatusDAO();
		return sInstance;
	}

	public Class<ExamStatus> getReferenceClass() {
		return ExamStatus.class;
	}

	@SuppressWarnings("unchecked")
	public List<ExamStatus> findByStatus(org.hibernate.Session hibSession, Long statusId) {
		return hibSession.createQuery("from ExamStatus x where x.status.uniqueId = :statusId").setLong("statusId", statusId).list();
	}
}
