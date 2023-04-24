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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import org.unitime.timetable.model.base.BaseManagerSettings;
import org.unitime.timetable.model.dao.ManagerSettingsDAO;
import org.unitime.timetable.model.dao.SettingsDAO;

/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
@Table(name = "manager_settings")
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
			Settings s = SettingsDAO.getInstance().getSession().createQuery(
					"select s from Settings s where s.key = :key", Settings.class
					).setParameter("key", name, String.class).setCacheable(true).uniqueResult();
			return (s == null ? defaultValue : s.getDefaultValue());
		}
		return getValue(mgr.getUniqueId(), name, defaultValue);
	}

	public static String getValue(TimetableManager mgr, String name) {
		return getValue(mgr, name, null);
	}
	
	public static String getValue(Long managerId, String name, String defaultValue) {
		if (managerId == null) return defaultValue;
		Settings s = SettingsDAO.getInstance().getSession().createQuery(
				"select s from Settings s where s.key = :key", Settings.class
				).setParameter("key", name, String.class).setCacheable(true).uniqueResult();
		if (s == null) return defaultValue;
		ManagerSettings m = ManagerSettingsDAO.getInstance().getSession().createQuery(
				"select m from ManagerSettings m where m.manager.uniqueId = :managerId and m.key.uniqueId = :settingsId", ManagerSettings.class
				).setParameter("managerId", managerId, Long.class).setParameter("settingsId", s.getUniqueId(), Long.class).setCacheable(true).uniqueResult();
		return (m == null? s.getDefaultValue() : m.getValue());
	}

	public static String getValue(Long managerId, String name) {
		return getValue(managerId, name, null);
	}
}
