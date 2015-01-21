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

import org.unitime.timetable.model.JointEnrollment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.JointEnrollmentDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseJointEnrollmentDAO extends _RootDAO<JointEnrollment,Long> {

	private static JointEnrollmentDAO sInstance;

	public static JointEnrollmentDAO getInstance() {
		if (sInstance == null) sInstance = new JointEnrollmentDAO();
		return sInstance;
	}

	public Class<JointEnrollment> getReferenceClass() {
		return JointEnrollment.class;
	}

	@SuppressWarnings("unchecked")
	public List<JointEnrollment> findBySolution(org.hibernate.Session hibSession, Long solutionId) {
		return hibSession.createQuery("from JointEnrollment x where x.solution.uniqueId = :solutionId").setLong("solutionId", solutionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<JointEnrollment> findByClass1(org.hibernate.Session hibSession, Long class1Id) {
		return hibSession.createQuery("from JointEnrollment x where x.class1.uniqueId = :class1Id").setLong("class1Id", class1Id).list();
	}

	@SuppressWarnings("unchecked")
	public List<JointEnrollment> findByClass2(org.hibernate.Session hibSession, Long class2Id) {
		return hibSession.createQuery("from JointEnrollment x where x.class2.uniqueId = :class2Id").setLong("class2Id", class2Id).list();
	}
}
