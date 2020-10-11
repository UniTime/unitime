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

import org.unitime.timetable.model.TeachingScheduleDivision;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.TeachingScheduleDivisionDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseTeachingScheduleDivisionDAO extends _RootDAO<TeachingScheduleDivision,Long> {

	private static TeachingScheduleDivisionDAO sInstance;

	public static TeachingScheduleDivisionDAO getInstance() {
		if (sInstance == null) sInstance = new TeachingScheduleDivisionDAO();
		return sInstance;
	}

	public Class<TeachingScheduleDivision> getReferenceClass() {
		return TeachingScheduleDivision.class;
	}

	@SuppressWarnings("unchecked")
	public List<TeachingScheduleDivision> findByOffering(org.hibernate.Session hibSession, Long offeringId) {
		return hibSession.createQuery("from TeachingScheduleDivision x where x.offering.uniqueId = :offeringId").setLong("offeringId", offeringId).list();
	}

	@SuppressWarnings("unchecked")
	public List<TeachingScheduleDivision> findByConfig(org.hibernate.Session hibSession, Long configId) {
		return hibSession.createQuery("from TeachingScheduleDivision x where x.config.uniqueId = :configId").setLong("configId", configId).list();
	}

	@SuppressWarnings("unchecked")
	public List<TeachingScheduleDivision> findByItype(org.hibernate.Session hibSession, Integer itypeId) {
		return hibSession.createQuery("from TeachingScheduleDivision x where x.itype.itype = :itypeId").setInteger("itypeId", itypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<TeachingScheduleDivision> findByAttribute(org.hibernate.Session hibSession, Long attributeId) {
		return hibSession.createQuery("from TeachingScheduleDivision x where x.attribute.uniqueId = :attributeId").setLong("attributeId", attributeId).list();
	}
}
