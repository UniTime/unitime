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

import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DistributionObjectDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseDistributionObjectDAO extends _RootDAO<DistributionObject,Long> {

	private static DistributionObjectDAO sInstance;

	public static DistributionObjectDAO getInstance() {
		if (sInstance == null) sInstance = new DistributionObjectDAO();
		return sInstance;
	}

	public Class<DistributionObject> getReferenceClass() {
		return DistributionObject.class;
	}

	@SuppressWarnings("unchecked")
	public List<DistributionObject> findByDistributionPref(org.hibernate.Session hibSession, Long distributionPrefId) {
		return hibSession.createQuery("from DistributionObject x where x.distributionPref.uniqueId = :distributionPrefId").setLong("distributionPrefId", distributionPrefId).list();
	}

	@SuppressWarnings("unchecked")
	public List<DistributionObject> findByPrefGroup(org.hibernate.Session hibSession, Long prefGroupId) {
		return hibSession.createQuery("from DistributionObject x where x.prefGroup.uniqueId = :prefGroupId").setLong("prefGroupId", prefGroupId).list();
	}
}
