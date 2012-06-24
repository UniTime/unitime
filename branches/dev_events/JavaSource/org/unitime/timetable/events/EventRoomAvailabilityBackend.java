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

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConglictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcResponse;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;

public class EventRoomAvailabilityBackend extends EventAction<EventRoomAvailabilityRpcRequest, EventRoomAvailabilityRpcResponse> {
	
	@Override
	public EventRoomAvailabilityRpcResponse execute(EventRoomAvailabilityRpcRequest request, GwtRpcHelper helper, EventRights rights) {
		EventRoomAvailabilityRpcResponse response = new EventRoomAvailabilityRpcResponse();
		
		Session session = SessionDAO.getInstance().get(request.getSessionId());
		
		if (request.hasDates() && request.hasLocations()) {
			for (int idx = 0; idx < request.getLocations().size(); idx += 1000) {
			
				String dates = "";
				for (int i = 0; i < request.getDates().size(); i++)
					dates += (dates.isEmpty() ? "" : ",") + ":d" + i;
				
				String locations = "";
				for (int i = idx; i + idx < request.getLocations().size() && i < 1000; i++)
					locations += (locations.isEmpty() ? "" : ",") + ":l" + i;
				
				Query query = EventDAO.getInstance().getSession().createQuery(
						"select m from Meeting m " +
						"where m.startPeriod<:stopTime and m.stopPeriod>:startTime and " +
						"m.locationPermanentId in (" + locations + ") and m.meetingDate in ("+dates+")");
				
				query.setInteger("startTime", request.getStartSlot());
				query.setInteger("stopTime", request.getEndSlot());
				for (int i = 0; i < request.getDates().size(); i++) {
					Date date = CalendarUtils.dateOfYear2date(session.getSessionStartYear(), request.getDates().get(i));
					query.setDate("d" + i, date);
				}
				for (int i = idx; i + idx < request.getLocations().size() && i < 1000; i++)
					query.setLong("l" + i, request.getLocations().get(idx + i));
				
				for (Meeting m: (List<Meeting>)query.list()) {
					MeetingConglictInterface conflict = new MeetingConglictInterface();

					conflict.setEventId(m.getEvent().getUniqueId());
					conflict.setName(m.getEvent().getEventName());
					conflict.setType(EventInterface.EventType.values()[m.getEvent().getEventType()]);
					
					conflict.setId(m.getUniqueId());
					conflict.setMeetingDate(m.getMeetingDate());
					conflict.setDayOfYear(CalendarUtils.date2dayOfYear(session.getSessionStartYear(), m.getMeetingDate()));
					conflict.setStartOffset(m.getStartOffset() == null ? 0 : m.getStartOffset());
					conflict.setEndOffset(m.getStopOffset() == null ? 0 : m.getStopOffset());
					conflict.setStartSlot(m.getStartPeriod());
					conflict.setEndSlot(m.getStopPeriod());
					if (m.isApproved())
						conflict.setApprovalDate(m.getApprovedDate());
					
					response.addOverlap(CalendarUtils.date2dayOfYear(session.getSessionStartYear(), m.getMeetingDate()), m.getLocationPermanentId(), conflict);
				}
				
			}
		}
		
		if (request.hasMeetings()) {
			response.setMeetings(request.getMeetings());
			
			for (MeetingInterface meeting: response.getMeetings()) {
				if (meeting.getMeetingDate() == null) {
					meeting.setMeetingDate(CalendarUtils.dateOfYear2date(session.getSessionStartYear(), meeting.getDayOfYear()));
					meeting.setDayOfWeek(Constants.getDayOfWeek(meeting.getMeetingDate()));
				}
				
				if (rights.isPastOrOutside(meeting.getMeetingDate())) {
					MeetingConglictInterface conflict = new MeetingConglictInterface();
					conflict.setName(MESSAGES.conflictPastOrOutside(session.getLabel()));
					conflict.setType(EventInterface.EventType.Unavailabile);
					conflict.setMeetingDate(meeting.getMeetingDate());
					conflict.setDayOfYear(meeting.getDayOfYear());
					conflict.setStartOffset(0);
					conflict.setEndOffset(0);
					conflict.setStartSlot(0);
					conflict.setEndSlot(288);
					conflict.setPast(true);
					meeting.addConflict(conflict);
				}
				
				if (!meeting.hasLocation()) continue;
				
				if (!rights.canCreate(meeting.getLocation().getId())) {
					MeetingConglictInterface conflict = new MeetingConglictInterface();
					conflict.setName(MESSAGES.conflictNotEventRoom(meeting.getLocationName()));
					conflict.setType(EventInterface.EventType.Unavailabile);
					conflict.setMeetingDate(meeting.getMeetingDate());
					conflict.setDayOfYear(meeting.getDayOfYear());
					conflict.setStartOffset(0);
					conflict.setEndOffset(0);
					conflict.setStartSlot(0);
					conflict.setEndSlot(288);
					meeting.addConflict(conflict);
				}

				meeting.setEndOffset(-10);
				
				for (Meeting m: (List<Meeting>)EventDAO.getInstance().getSession().createQuery(
						"select m from Meeting m, Location l "+
						"where m.startPeriod < :stopTime and m.stopPeriod > :startTime and " +
						"m.locationPermanentId = l.permanentId and l.uniqueId = :locationdId and m.meetingDate = :meetingDate and m.uniqueId != :meetingId")
						.setInteger("startTime", meeting.getStartSlot())
						.setInteger("stopTime", meeting.getEndSlot())
						.setDate("meetingDate", meeting.getMeetingDate())
						.setLong("locationdId", meeting.getLocation().getId())
						.setLong("meetingId", meeting.getId() == null ? -1 : meeting.getId())
						.list()) {
					
					MeetingConglictInterface conflict = new MeetingConglictInterface();

					conflict.setEventId(m.getEvent().getUniqueId());
					conflict.setName(m.getEvent().getEventName());
					conflict.setType(EventInterface.EventType.values()[m.getEvent().getEventType()]);
					
					conflict.setId(m.getUniqueId());
					conflict.setMeetingDate(m.getMeetingDate());
					conflict.setDayOfYear(meeting.getDayOfYear());
					conflict.setStartSlot(m.getStartPeriod());
					conflict.setEndSlot(m.getStopPeriod());
					conflict.setStartOffset(m.getStartOffset() == null ? 0 : m.getStartOffset());
					conflict.setEndOffset(m.getStopOffset() == null ? 0 : m.getStopOffset());
					if (m.isApproved())
						conflict.setApprovalDate(m.getApprovedDate());
					
					meeting.addConflict(conflict);
				}
			}
		}

		
		return response;
	}
}
