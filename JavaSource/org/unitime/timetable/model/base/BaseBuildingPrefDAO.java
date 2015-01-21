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

import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.BuildingPrefDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseBuildingPrefDAO extends _RootDAO<BuildingPref,Long> {

	private static BuildingPrefDAO sInstance;

	public static BuildingPrefDAO getInstance() {
		if (sInstance == null) sInstance = new BuildingPrefDAO();
		return sInstance;
	}

	public Class<BuildingPref> getReferenceClass() {
		return BuildingPref.class;
	}

	@SuppressWarnings("unchecked")
	public List<BuildingPref> findByBuilding(org.hibernate.Session hibSession, Long buildingId) {
		return hibSession.createQuery("from BuildingPref x where x.building.uniqueId = :buildingId").setLong("buildingId", buildingId).list();
	}
}
