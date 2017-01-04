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

import org.unitime.timetable.model.PitStudentClassEnrollment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PitStudentClassEnrollmentDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitStudentClassEnrollmentDAO extends _RootDAO<PitStudentClassEnrollment,Long> {

	private static PitStudentClassEnrollmentDAO sInstance;

	public static PitStudentClassEnrollmentDAO getInstance() {
		if (sInstance == null) sInstance = new PitStudentClassEnrollmentDAO();
		return sInstance;
	}

	public Class<PitStudentClassEnrollment> getReferenceClass() {
		return PitStudentClassEnrollment.class;
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentClassEnrollment> findByPitStudent(org.hibernate.Session hibSession, Long pitStudentId) {
		return hibSession.createQuery("from PitStudentClassEnrollment x where x.pitStudent.uniqueId = :pitStudentId").setLong("pitStudentId", pitStudentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentClassEnrollment> findByPitCourseOffering(org.hibernate.Session hibSession, Long pitCourseOfferingId) {
		return hibSession.createQuery("from PitStudentClassEnrollment x where x.pitCourseOffering.uniqueId = :pitCourseOfferingId").setLong("pitCourseOfferingId", pitCourseOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitStudentClassEnrollment> findByPitClass(org.hibernate.Session hibSession, Long pitClassId) {
		return hibSession.createQuery("from PitStudentClassEnrollment x where x.pitClass.uniqueId = :pitClassId").setLong("pitClassId", pitClassId).list();
	}
}
