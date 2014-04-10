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

import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.SessionDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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

	@SuppressWarnings("unchecked")
	public List<Session> findByDefaultSectioningStatus(org.hibernate.Session hibSession, Long defaultSectioningStatusId) {
		return hibSession.createQuery("from Session x where x.defaultSectioningStatus.uniqueId = :defaultSectioningStatusId").setLong("defaultSectioningStatusId", defaultSectioningStatusId).list();
	}
}
