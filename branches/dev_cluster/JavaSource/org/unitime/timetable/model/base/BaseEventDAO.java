/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.EventDAO;

public abstract class BaseEventDAO extends _RootDAO<Event,Long> {

	private static EventDAO sInstance;

	public static EventDAO getInstance() {
		if (sInstance == null) sInstance = new EventDAO();
		return sInstance;
	}

	public Class<Event> getReferenceClass() {
		return Event.class;
	}

	@SuppressWarnings("unchecked")
	public List<Event> findByMainContact(org.hibernate.Session hibSession, Long mainContactId) {
		return hibSession.createQuery("from Event x where x.mainContact.uniqueId = :mainContactId").setLong("mainContactId", mainContactId).list();
	}

	@SuppressWarnings("unchecked")
	public List<Event> findBySponsoringOrganization(org.hibernate.Session hibSession, Long sponsoringOrganizationId) {
		return hibSession.createQuery("from Event x where x.sponsoringOrganization.uniqueId = :sponsoringOrganizationId").setLong("sponsoringOrganizationId", sponsoringOrganizationId).list();
	}
}
