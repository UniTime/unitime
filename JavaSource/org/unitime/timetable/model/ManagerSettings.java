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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseManagerSettings;
import org.unitime.timetable.model.dao.ManagerSettingsDAO;
import org.unitime.timetable.model.dao.SettingsDAO;



/**
 * @author Tomas Muller
 */
public class ManagerSettings extends BaseManagerSettings {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ManagerSettings () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ManagerSettings (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	
	public static String getValue(TimetableManager mgr, String name, String defaultValue) {
		if (mgr == null) {
			Settings s = (Settings)SettingsDAO.getInstance().getSession().createQuery(
					"select s from Settings s where s.key = :key"
					).setString("key", name).setCacheable(true).uniqueResult();
			return (s == null ? defaultValue : s.getDefaultValue());
		}
		return getValue(mgr.getUniqueId(), name, defaultValue);
	}

	public static String getValue(TimetableManager mgr, String name) {
		return getValue(mgr, name, null);
	}
	
	public static String getValue(Long managerId, String name, String defaultValue) {
		if (managerId == null) return defaultValue;
		Settings s = (Settings)SettingsDAO.getInstance().getSession().createQuery(
				"select s from Settings s where s.key = :key"
				).setString("key", name).setCacheable(true).uniqueResult();
		if (s == null) return defaultValue;
		ManagerSettings m = (ManagerSettings)ManagerSettingsDAO.getInstance().getSession().createQuery(
				"select m from ManagerSettings m where m.manager.uniqueId = :managerId and m.key.uniqueId = :settingsId"
				).setLong("managerId", managerId).setLong("settingsId", s.getUniqueId()).setCacheable(true).uniqueResult();
		return (m == null? s.getDefaultValue() : m.getValue());
	}

	public static String getValue(Long managerId, String name) {
		return getValue(managerId, name, null);
	}
}
