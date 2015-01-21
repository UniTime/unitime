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
package org.unitime.timetable.server;

import java.util.Date;

import org.joda.time.DateTimeZone;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat.ServerTimeZoneRequest;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat.ServerTimeZoneResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ServerTimeZoneRequest.class)
public class ServerTimeZoneBackend implements GwtRpcImplementation<ServerTimeZoneRequest, ServerTimeZoneResponse> {

	@Override
	public ServerTimeZoneResponse execute(ServerTimeZoneRequest request, SessionContext context) {
		Date first = null, last = null;
		for (Session session: SessionDAO.getInstance().findAll()) {
			if (first == null || first.after(session.getEventBeginDate()))
				first = session.getEventBeginDate();
			if (last == null || last.before(session.getEventEndDate()))
				last = session.getEventEndDate();
		}
		DateTimeZone zone = DateTimeZone.getDefault();
		int offsetInMinutes = zone.getOffset(first.getTime()) / 60000;
		ServerTimeZoneResponse ret = new ServerTimeZoneResponse();
		ret.setId(zone.getID());
		ret.addName(zone.getName(new Date().getTime()));
		ret.setTimeZoneOffsetInMinutes(offsetInMinutes);
		long time = first.getTime();
		long transition;
		while (time != (transition = zone.nextTransition(time)) && time < last.getTime()) {
			int adjustment = (zone.getOffset(transition) / 60000) - offsetInMinutes;
			ret.addTransition((int)(transition / 3600000), adjustment);
			time = transition;
		}
		return ret;
	}

}
