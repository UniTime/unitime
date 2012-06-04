/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.events;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.EventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConglictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.model.Meeting;

public abstract class EventAction<T extends EventRpcRequest<R>, R extends GwtRpcResponse> implements GwtRpcImplementation<T, R> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static DateFormat sMeetingDateFormat = new SimpleDateFormat(CONSTANTS.eventDateFormatShort(), Localization.getJavaLocale());

	@Override
	public R execute(T request, GwtRpcHelper helper) {
		// Create event rights
		EventRights rights = createEventRights(request, helper);
		
		// Check basic access
		rights.checkAccess();
		
		// Execute action
		return execute(request, helper, rights);
	}
	
	public abstract R execute(T request, GwtRpcHelper helper, EventRights rights);
	
	protected EventRights createEventRights(T request, GwtRpcHelper helper) {
		return new SimpleEventRights(helper, request.getSessionId());
	}

	protected static String toString(MeetingInterface meeting) {
		return (meeting instanceof MeetingConglictInterface ? ((MeetingConglictInterface)meeting).getName() + " " : "") +
				(meeting.getMeetingDate() == null ? "" : sMeetingDateFormat.format(meeting.getMeetingDate()) + " ") +
				meeting.getAllocatedTime(CONSTANTS) + (meeting.hasLocation() ? " " + meeting.getLocationName() : "");
	}
	
	protected static String toString(Meeting meeting) {
		return (meeting.getMeetingDate() == null ? "" : sMeetingDateFormat.format(meeting.getMeetingDate()) + " ") +
				time2string(meeting.getStartPeriod(), 0) + " - " + time2string(meeting.getStopPeriod(), 0) +
				(meeting.getLocation() == null ? " " + meeting.getLocation().getLabel() : "");
	}
	
	protected static String time2string(int slot, int offset) {
		int min = 5 * slot + offset;
		if (min == 0 || min == 1440) return CONSTANTS.timeMidnitgh();
		if (min == 720) return CONSTANTS.timeNoon();
		int h = min / 60;
        int m = min % 60;
        if (CONSTANTS.useAmPm()) {
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
		} else {
			return h + ":" + (m < 10 ? "0" : "") + m;
		}
	}

}
