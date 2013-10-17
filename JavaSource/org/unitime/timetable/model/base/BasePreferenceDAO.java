/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.PreferenceDAO;

/**
 * @author Tomas Muller
 */
public abstract class BasePreferenceDAO extends _RootDAO<Preference,Long> {

	private static PreferenceDAO sInstance;

	public static PreferenceDAO getInstance() {
		if (sInstance == null) sInstance = new PreferenceDAO();
		return sInstance;
	}

	public Class<Preference> getReferenceClass() {
		return Preference.class;
	}

	@SuppressWarnings("unchecked")
	public List<Preference> findByOwner(org.hibernate.Session hibSession, Long ownerId) {
		return hibSession.createQuery("from Preference x where x.owner.uniqueId = :ownerId").setLong("ownerId", ownerId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Preference> findByPrefLevel(org.hibernate.Session hibSession, Long prefLevelId) {
		return hibSession.createQuery("from Preference x where x.prefLevel.uniqueId = :prefLevelId").setLong("prefLevelId", prefLevelId).list();
	}
}
