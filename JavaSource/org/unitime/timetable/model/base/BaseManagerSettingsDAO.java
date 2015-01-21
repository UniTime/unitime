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

import org.unitime.timetable.model.ManagerSettings;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.ManagerSettingsDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseManagerSettingsDAO extends _RootDAO<ManagerSettings,Long> {

	private static ManagerSettingsDAO sInstance;

	public static ManagerSettingsDAO getInstance() {
		if (sInstance == null) sInstance = new ManagerSettingsDAO();
		return sInstance;
	}

	public Class<ManagerSettings> getReferenceClass() {
		return ManagerSettings.class;
	}

	@SuppressWarnings("unchecked")
	public List<ManagerSettings> findByKey(org.hibernate.Session hibSession, Long keyId) {
		return hibSession.createQuery("from ManagerSettings x where x.key.uniqueId = :keyId").setLong("keyId", keyId).list();
	}

	@SuppressWarnings("unchecked")
	public List<ManagerSettings> findByManager(org.hibernate.Session hibSession, Long managerId) {
		return hibSession.createQuery("from ManagerSettings x where x.manager.uniqueId = :managerId").setLong("managerId", managerId).list();
	}
}
