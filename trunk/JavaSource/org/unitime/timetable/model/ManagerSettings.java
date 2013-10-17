/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
