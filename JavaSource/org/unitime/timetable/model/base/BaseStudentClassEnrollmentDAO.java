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

import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.StudentClassEnrollmentDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseStudentClassEnrollmentDAO extends _RootDAO<StudentClassEnrollment,Long> {

	private static StudentClassEnrollmentDAO sInstance;

	public static StudentClassEnrollmentDAO getInstance() {
		if (sInstance == null) sInstance = new StudentClassEnrollmentDAO();
		return sInstance;
	}

	public Class<StudentClassEnrollment> getReferenceClass() {
		return StudentClassEnrollment.class;
	}

	@SuppressWarnings("unchecked")
	public List<StudentClassEnrollment> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from StudentClassEnrollment x where x.student.uniqueId = :studentId").setLong("studentId", studentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentClassEnrollment> findByCourseRequest(org.hibernate.Session hibSession, Long courseRequestId) {
		return hibSession.createQuery("from StudentClassEnrollment x where x.courseRequest.uniqueId = :courseRequestId").setLong("courseRequestId", courseRequestId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentClassEnrollment> findByCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId) {
		return hibSession.createQuery("from StudentClassEnrollment x where x.courseOffering.uniqueId = :courseOfferingId").setLong("courseOfferingId", courseOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<StudentClassEnrollment> findByClazz(org.hibernate.Session hibSession, Long clazzId) {
		return hibSession.createQuery("from StudentClassEnrollment x where x.clazz.uniqueId = :clazzId").setLong("clazzId", clazzId).list();
	}
}
