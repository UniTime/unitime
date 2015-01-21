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
package org.unitime.timetable.model.base;

import java.util.List;

import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.model.dao.EventDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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
