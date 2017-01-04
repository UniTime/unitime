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

import org.unitime.timetable.model.PitInstrOfferingConfig;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PitInstrOfferingConfigDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitInstrOfferingConfigDAO extends _RootDAO<PitInstrOfferingConfig,Long> {

	private static PitInstrOfferingConfigDAO sInstance;

	public static PitInstrOfferingConfigDAO getInstance() {
		if (sInstance == null) sInstance = new PitInstrOfferingConfigDAO();
		return sInstance;
	}

	public Class<PitInstrOfferingConfig> getReferenceClass() {
		return PitInstrOfferingConfig.class;
	}

	@SuppressWarnings("unchecked")
	public List<PitInstrOfferingConfig> findByInstrOfferingConfig(org.hibernate.Session hibSession, Long instrOfferingConfigId) {
		return hibSession.createQuery("from PitInstrOfferingConfig x where x.instrOfferingConfig.uniqueId = :instrOfferingConfigId").setLong("instrOfferingConfigId", instrOfferingConfigId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitInstrOfferingConfig> findByPitInstructionalOffering(org.hibernate.Session hibSession, Long pitInstructionalOfferingId) {
		return hibSession.createQuery("from PitInstrOfferingConfig x where x.pitInstructionalOffering.uniqueId = :pitInstructionalOfferingId").setLong("pitInstructionalOfferingId", pitInstructionalOfferingId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitInstrOfferingConfig> findByClassDurationType(org.hibernate.Session hibSession, Long classDurationTypeId) {
		return hibSession.createQuery("from PitInstrOfferingConfig x where x.classDurationType.uniqueId = :classDurationTypeId").setLong("classDurationTypeId", classDurationTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitInstrOfferingConfig> findByInstructionalMethod(org.hibernate.Session hibSession, Long instructionalMethodId) {
		return hibSession.createQuery("from PitInstrOfferingConfig x where x.instructionalMethod.uniqueId = :instructionalMethodId").setLong("instructionalMethodId", instructionalMethodId).list();
	}
}
