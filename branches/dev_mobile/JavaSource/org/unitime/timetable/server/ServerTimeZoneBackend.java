/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
