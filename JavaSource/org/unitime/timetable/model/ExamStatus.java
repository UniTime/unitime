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
package org.unitime.timetable.model;

import java.util.List;

import org.unitime.timetable.model.base.BaseExamStatus;
import org.unitime.timetable.model.dao.ExamStatusDAO;

public class ExamStatus extends BaseExamStatus {
	private static final long serialVersionUID = 1L;

	public ExamStatus() {
		super();
	}
	
	public static ExamStatus findStatus(Long sessionId, Long typeId) {
		return findStatus(null, sessionId, typeId);
	}
	
	public static ExamStatus findStatus(org.hibernate.Session hibSession, Long sessionId, Long typeId) {
		return (ExamStatus)(hibSession != null ? hibSession : ExamStatusDAO.getInstance().getSession()).createQuery(
				"from ExamStatus where session.uniqueId = :sessionId and type.uniqueId = :typeId")
				.setLong("sessionId", sessionId)
				.setLong("typeId", typeId)
				.setCacheable(true)
				.uniqueResult();
	}
	
	public static List<ExamStatus> findAll(Long sessionId) {
		return findAll(null, sessionId);
	}
	
	public static List<ExamStatus> findAll(org.hibernate.Session hibSession, Long sessionId) {
		return (List<ExamStatus>)(hibSession != null ? hibSession : ExamStatusDAO.getInstance().getSession()).createQuery(
				"from ExamStatus where session.uniqueId = :sessionId and type.uniqueId = :typeId")
				.setLong("sessionId", sessionId)
				.setCacheable(true)
				.list();
	}
	
	public DepartmentStatusType effectiveStatus() {
		return getStatus() == null ? getSession().getStatusType() : getStatus();
	}
}
