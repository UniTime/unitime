/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.model.base;

import java.util.List;

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SessionDAO;

public abstract class BaseSessionDAO extends _RootDAO<Session,Long> {

	private static SessionDAO sInstance;

	public static SessionDAO getInstance() {
		if (sInstance == null) sInstance = new SessionDAO();
		return sInstance;
	}

	public Class<Session> getReferenceClass() {
		return Session.class;
	}

	@SuppressWarnings("unchecked")
	public List<Session> findByStatusType(org.hibernate.Session hibSession, Long statusTypeId) {
		return hibSession.createQuery("from Session x where x.statusType.uniqueId = :statusTypeId").setLong("statusTypeId", statusTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Session> findByDefaultDatePattern(org.hibernate.Session hibSession, Long defaultDatePatternId) {
		return hibSession.createQuery("from Session x where x.defaultDatePattern.uniqueId = :defaultDatePatternId").setLong("defaultDatePatternId", defaultDatePatternId).list();
	}
}
