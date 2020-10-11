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

import org.unitime.timetable.model.TeachingScheduleAssignment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.TeachingScheduleAssignmentDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseTeachingScheduleAssignmentDAO extends _RootDAO<TeachingScheduleAssignment,Long> {

	private static TeachingScheduleAssignmentDAO sInstance;

	public static TeachingScheduleAssignmentDAO getInstance() {
		if (sInstance == null) sInstance = new TeachingScheduleAssignmentDAO();
		return sInstance;
	}

	public Class<TeachingScheduleAssignment> getReferenceClass() {
		return TeachingScheduleAssignment.class;
	}

	@SuppressWarnings("unchecked")
	public List<TeachingScheduleAssignment> findByDivision(org.hibernate.Session hibSession, Long divisionId) {
		return hibSession.createQuery("from TeachingScheduleAssignment x where x.division.uniqueId = :divisionId").setLong("divisionId", divisionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<TeachingScheduleAssignment> findByMeeting(org.hibernate.Session hibSession, Long meetingId) {
		return hibSession.createQuery("from TeachingScheduleAssignment x where x.meeting.uniqueId = :meetingId").setLong("meetingId", meetingId).list();
	}
}
