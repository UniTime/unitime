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

import org.unitime.timetable.model.PitDepartmentalInstructor;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PitDepartmentalInstructorDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitDepartmentalInstructorDAO extends _RootDAO<PitDepartmentalInstructor,Long> {

	private static PitDepartmentalInstructorDAO sInstance;

	public static PitDepartmentalInstructorDAO getInstance() {
		if (sInstance == null) sInstance = new PitDepartmentalInstructorDAO();
		return sInstance;
	}

	public Class<PitDepartmentalInstructor> getReferenceClass() {
		return PitDepartmentalInstructor.class;
	}

	@SuppressWarnings("unchecked")
	public List<PitDepartmentalInstructor> findByPositionType(org.hibernate.Session hibSession, Long positionTypeId) {
		return hibSession.createQuery("from PitDepartmentalInstructor x where x.positionType.uniqueId = :positionTypeId").setLong("positionTypeId", positionTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitDepartmentalInstructor> findByDepartment(org.hibernate.Session hibSession, Long departmentId) {
		return hibSession.createQuery("from PitDepartmentalInstructor x where x.department.uniqueId = :departmentId").setLong("departmentId", departmentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitDepartmentalInstructor> findByPointInTimeData(org.hibernate.Session hibSession, Long pointInTimeDataId) {
		return hibSession.createQuery("from PitDepartmentalInstructor x where x.pointInTimeData.uniqueId = :pointInTimeDataId").setLong("pointInTimeDataId", pointInTimeDataId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitDepartmentalInstructor> findByDepartmentalInstructor(org.hibernate.Session hibSession, Long departmentalInstructorId) {
		return hibSession.createQuery("from PitDepartmentalInstructor x where x.departmentalInstructor.uniqueId = :departmentalInstructorId").setLong("departmentalInstructorId", departmentalInstructorId).list();
	}
}
