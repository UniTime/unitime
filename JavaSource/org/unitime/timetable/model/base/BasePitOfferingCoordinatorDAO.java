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

import org.unitime.timetable.model.PitOfferingCoordinator;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PitOfferingCoordinatorDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitOfferingCoordinatorDAO extends _RootDAO<PitOfferingCoordinator,Long> {

	private static PitOfferingCoordinatorDAO sInstance;

	public static PitOfferingCoordinatorDAO getInstance() {
		if (sInstance == null) sInstance = new PitOfferingCoordinatorDAO();
		return sInstance;
	}

	public Class<PitOfferingCoordinator> getReferenceClass() {
		return PitOfferingCoordinator.class;
	}

	@SuppressWarnings("unchecked")
	public List<PitOfferingCoordinator> findByPitInstructionalOffering(org.hibernate.Session hibSession, Long pitInstructionalOfferingId) {
		return hibSession.createQuery("from PitOfferingCoordinator x where x.pitInstructionalOffering.uniqueId = :pitInstructionalOfferingId").setLong("pitInstructionalOfferingId", pitInstructionalOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitOfferingCoordinator> findByPitDepartmentalInstructor(org.hibernate.Session hibSession, Long pitDepartmentalInstructorId) {
		return hibSession.createQuery("from PitOfferingCoordinator x where x.pitDepartmentalInstructor.uniqueId = :pitDepartmentalInstructorId").setLong("pitDepartmentalInstructorId", pitDepartmentalInstructorId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitOfferingCoordinator> findByResponsibility(org.hibernate.Session hibSession, Long responsibilityId) {
		return hibSession.createQuery("from PitOfferingCoordinator x where x.responsibility.uniqueId = :responsibilityId").setLong("responsibilityId", responsibilityId).list();
	}
}
