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
package org.unitime.timetable.model.dao;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
import java.util.List;
import org.unitime.timetable.model.ClassInstructor;

public class ClassInstructorDAO extends _RootDAO<ClassInstructor,Long> {
	private static ClassInstructorDAO sInstance;

	public ClassInstructorDAO() {}

	public static ClassInstructorDAO getInstance() {
		if (sInstance == null) sInstance = new ClassInstructorDAO();
		return sInstance;
	}

	public Class<ClassInstructor> getReferenceClass() {
		return ClassInstructor.class;
	}

	@SuppressWarnings("unchecked")
	public List<ClassInstructor> findByClassInstructing(org.hibernate.Session hibSession, Long classInstructingId) {
		return hibSession.createQuery("from ClassInstructor x where x.classInstructing.uniqueId = :classInstructingId").setParameter("classInstructingId", classInstructingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ClassInstructor> findByInstructor(org.hibernate.Session hibSession, Long instructorId) {
		return hibSession.createQuery("from ClassInstructor x where x.instructor.uniqueId = :instructorId").setParameter("instructorId", instructorId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ClassInstructor> findByResponsibility(org.hibernate.Session hibSession, Long responsibilityId) {
		return hibSession.createQuery("from ClassInstructor x where x.responsibility.uniqueId = :responsibilityId").setParameter("responsibilityId", responsibilityId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ClassInstructor> findByTeachingRequest(org.hibernate.Session hibSession, Long teachingRequestId) {
		return hibSession.createQuery("from ClassInstructor x where x.teachingRequest.uniqueId = :teachingRequestId").setParameter("teachingRequestId", teachingRequestId).list();
	}
}
