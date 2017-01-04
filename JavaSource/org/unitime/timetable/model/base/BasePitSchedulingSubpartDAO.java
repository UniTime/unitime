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

import org.unitime.timetable.model.PitSchedulingSubpart;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PitSchedulingSubpartDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BasePitSchedulingSubpartDAO extends _RootDAO<PitSchedulingSubpart,Long> {

	private static PitSchedulingSubpartDAO sInstance;

	public static PitSchedulingSubpartDAO getInstance() {
		if (sInstance == null) sInstance = new PitSchedulingSubpartDAO();
		return sInstance;
	}

	public Class<PitSchedulingSubpart> getReferenceClass() {
		return PitSchedulingSubpart.class;
	}

	@SuppressWarnings("unchecked")
	public List<PitSchedulingSubpart> findByCreditType(org.hibernate.Session hibSession, Long creditTypeId) {
		return hibSession.createQuery("from PitSchedulingSubpart x where x.creditType.uniqueId = :creditTypeId").setLong("creditTypeId", creditTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitSchedulingSubpart> findByCreditUnitType(org.hibernate.Session hibSession, Long creditUnitTypeId) {
		return hibSession.createQuery("from PitSchedulingSubpart x where x.creditUnitType.uniqueId = :creditUnitTypeId").setLong("creditUnitTypeId", creditUnitTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitSchedulingSubpart> findByItype(org.hibernate.Session hibSession, Integer itypeId) {
		return hibSession.createQuery("from PitSchedulingSubpart x where x.itype.itype = :itypeId").setInteger("itypeId", itypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitSchedulingSubpart> findBySchedulingSubpart(org.hibernate.Session hibSession, Long schedulingSubpartId) {
		return hibSession.createQuery("from PitSchedulingSubpart x where x.schedulingSubpart.uniqueId = :schedulingSubpartId").setLong("schedulingSubpartId", schedulingSubpartId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitSchedulingSubpart> findByPitParentSubpart(org.hibernate.Session hibSession, Long pitParentSubpartId) {
		return hibSession.createQuery("from PitSchedulingSubpart x where x.pitParentSubpart.uniqueId = :pitParentSubpartId").setLong("pitParentSubpartId", pitParentSubpartId).list();
	}

	@SuppressWarnings("unchecked")
	public List<PitSchedulingSubpart> findByPitInstrOfferingConfig(org.hibernate.Session hibSession, Long pitInstrOfferingConfigId) {
		return hibSession.createQuery("from PitSchedulingSubpart x where x.pitInstrOfferingConfig.uniqueId = :pitInstrOfferingConfigId").setLong("pitInstrOfferingConfigId", pitInstrOfferingConfigId).list();
	}
}
