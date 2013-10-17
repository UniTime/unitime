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

import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.TravelTimeDAO;

/**
 * @author Tomas Muller
 */
public abstract class BaseTravelTimeDAO extends _RootDAO<TravelTime,Long> {

	private static TravelTimeDAO sInstance;

	public static TravelTimeDAO getInstance() {
		if (sInstance == null) sInstance = new TravelTimeDAO();
		return sInstance;
	}

	public Class<TravelTime> getReferenceClass() {
		return TravelTime.class;
	}

	@SuppressWarnings("unchecked")
	public List<TravelTime> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from TravelTime x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
