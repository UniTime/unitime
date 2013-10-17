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

import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;

/**
 * @author Tomas Muller
 */
public abstract class BaseTimePatternDAO extends _RootDAO<TimePattern,Long> {

	private static TimePatternDAO sInstance;

	public static TimePatternDAO getInstance() {
		if (sInstance == null) sInstance = new TimePatternDAO();
		return sInstance;
	}

	public Class<TimePattern> getReferenceClass() {
		return TimePattern.class;
	}

	@SuppressWarnings("unchecked")
	public List<TimePattern> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from TimePattern x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
