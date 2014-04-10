/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
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
