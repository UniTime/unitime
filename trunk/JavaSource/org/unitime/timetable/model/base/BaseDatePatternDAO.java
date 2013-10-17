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

import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.DatePatternDAO;

/**
 * @author Tomas Muller
 */
public abstract class BaseDatePatternDAO extends _RootDAO<DatePattern,Long> {

	private static DatePatternDAO sInstance;

	public static DatePatternDAO getInstance() {
		if (sInstance == null) sInstance = new DatePatternDAO();
		return sInstance;
	}

	public Class<DatePattern> getReferenceClass() {
		return DatePattern.class;
	}

	@SuppressWarnings("unchecked")
	public List<DatePattern> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from DatePattern x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
