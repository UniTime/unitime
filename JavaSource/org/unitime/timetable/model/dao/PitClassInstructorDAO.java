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
import org.unitime.timetable.model.PitClassInstructor;

public class PitClassInstructorDAO extends _RootDAO<PitClassInstructor,Long> {
	private static PitClassInstructorDAO sInstance;

	public PitClassInstructorDAO() {}

	public static PitClassInstructorDAO getInstance() {
		if (sInstance == null) sInstance = new PitClassInstructorDAO();
		return sInstance;
	}

	public Class<PitClassInstructor> getReferenceClass() {
		return PitClassInstructor.class;
	}

	@SuppressWarnings("unchecked")
	public List<PitClassInstructor> findByPitClassInstructing(org.hibernate.Session hibSession, Long pitClassInstructingId) {
		return hibSession.createQuery("from PitClassInstructor x where x.pitClassInstructing.uniqueId = :pitClassInstructingId").setParameter("pitClassInstructingId", pitClassInstructingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitClassInstructor> findByPitDepartmentalInstructor(org.hibernate.Session hibSession, Long pitDepartmentalInstructorId) {
		return hibSession.createQuery("from PitClassInstructor x where x.pitDepartmentalInstructor.uniqueId = :pitDepartmentalInstructorId").setParameter("pitDepartmentalInstructorId", pitDepartmentalInstructorId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitClassInstructor> findByResponsibility(org.hibernate.Session hibSession, Long responsibilityId) {
		return hibSession.createQuery("from PitClassInstructor x where x.responsibility.uniqueId = :responsibilityId").setParameter("responsibilityId", responsibilityId).list();
	}
}
