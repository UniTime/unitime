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

import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseSchedulingSubpartDAO extends _RootDAO<SchedulingSubpart,Long> {

	private static SchedulingSubpartDAO sInstance;

	public static SchedulingSubpartDAO getInstance() {
		if (sInstance == null) sInstance = new SchedulingSubpartDAO();
		return sInstance;
	}

	public Class<SchedulingSubpart> getReferenceClass() {
		return SchedulingSubpart.class;
	}

	@SuppressWarnings("unchecked")
	public List<SchedulingSubpart> findByItype(org.hibernate.Session hibSession, Integer itypeId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.itype.itype = :itypeId").setInteger("itypeId", itypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<SchedulingSubpart> findByParentSubpart(org.hibernate.Session hibSession, Long parentSubpartId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.parentSubpart.uniqueId = :parentSubpartId").setLong("parentSubpartId", parentSubpartId).list();
	}

	@SuppressWarnings("unchecked")
	public List<SchedulingSubpart> findByInstrOfferingConfig(org.hibernate.Session hibSession, Long instrOfferingConfigId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.instrOfferingConfig.uniqueId = :instrOfferingConfigId").setLong("instrOfferingConfigId", instrOfferingConfigId).list();
	}

	@SuppressWarnings("unchecked")
	public List<SchedulingSubpart> findByDatePattern(org.hibernate.Session hibSession, Long datePatternId) {
		return hibSession.createQuery("from SchedulingSubpart x where x.datePattern.uniqueId = :datePatternId").setLong("datePatternId", datePatternId).list();
	}
}
