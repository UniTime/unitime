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
import org.unitime.timetable.model.WaitList;

public class WaitListDAO extends _RootDAO<WaitList,Long> {
	private static WaitListDAO sInstance;

	public WaitListDAO() {}

	public static WaitListDAO getInstance() {
		if (sInstance == null) sInstance = new WaitListDAO();
		return sInstance;
	}

	public Class<WaitList> getReferenceClass() {
		return WaitList.class;
	}

	@SuppressWarnings("unchecked")
	public List<WaitList> findByStudent(org.hibernate.Session hibSession, Long studentId) {
		return hibSession.createQuery("from WaitList x where x.student.uniqueId = :studentId").setParameter("studentId", studentId).list();
	}

	@SuppressWarnings("unchecked")
	public List<WaitList> findByCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId) {
		return hibSession.createQuery("from WaitList x where x.courseOffering.uniqueId = :courseOfferingId").setParameter("courseOfferingId", courseOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<WaitList> findByEnrolledCourse(org.hibernate.Session hibSession, Long enrolledCourseId) {
		return hibSession.createQuery("from WaitList x where x.enrolledCourse.uniqueId = :enrolledCourseId").setParameter("enrolledCourseId", enrolledCourseId).list();
	}

	@SuppressWarnings("unchecked")
	public List<WaitList> findByCourseDemand(org.hibernate.Session hibSession, Long courseDemandId) {
		return hibSession.createQuery("from WaitList x where x.courseDemand.uniqueId = :courseDemandId").setParameter("courseDemandId", courseDemandId).list();
	}

	@SuppressWarnings("unchecked")
	public List<WaitList> findBySwapCourseOffering(org.hibernate.Session hibSession, Long swapCourseOfferingId) {
		return hibSession.createQuery("from WaitList x where x.swapCourseOffering.uniqueId = :swapCourseOfferingId").setParameter("swapCourseOfferingId", swapCourseOfferingId).list();
	}
}
