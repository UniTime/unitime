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
package org.unitime.timetable.export;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.List;

import org.unitime.commons.User;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.events.EventLookupBackend;
import org.unitime.timetable.events.EventRights;
import org.unitime.timetable.events.SimpleEventRights;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MultiMeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;

public class EventsExportEventsToCSV implements Exporter {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Override
	public String reference() {
		return "events.csv";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new RuntimeException("No academic session provided.");

		EventLookupRpcRequest request = new EventLookupRpcRequest();
    	request.setSessionId(sessionId);
    	String id = helper.getParameter("id");
    	if (id != null) request.setResourceId(Long.valueOf(id));
    	String ext = helper.getParameter("ext");
    	if (ext != null) request.setResourceExternalId(ext);
    	String type = helper.getParameter("type");
    	request.setResourceType(ResourceType.valueOf(type.toUpperCase()));
    	EventFilterRpcRequest eventFilter = new EventFilterRpcRequest();
    	eventFilter.setSessionId(sessionId);
    	request.setEventFilter(eventFilter);
    	RoomFilterRpcRequest roomFilter = new RoomFilterRpcRequest();
    	roomFilter.setSessionId(sessionId);
    	boolean hasRoomFilter = false;
    	for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
    		String command = e.nextElement();
    		if (command.equals("e:text")) {
    			eventFilter.setText(helper.getParameter("e:text"));
    	} else if (command.startsWith("e:")) {
    			for (String value: helper.getParameterValues(command))
    				eventFilter.addOption(command.substring(2), value);
    		} else if (command.equals("r:text")) {
    			hasRoomFilter = true;
    			roomFilter.setText(helper.getParameter("r:text"));
    		} else if (command.startsWith("r:")) {
    			hasRoomFilter = true;
    			for (String value: helper.getParameterValues(command))
    				roomFilter.addOption(command.substring(2), value);
    		}
    	}
    	if (hasRoomFilter)
    		request.setRoomFilter(roomFilter);
    	String user = helper.getParameter("user");
    	User u = null;
    	if (user != null) {
    		u = new User();
    		u.setId(user);
    		eventFilter.setOption("user", user);
    		roomFilter.setOption("user", user);
    		String role = helper.getParameter("role");
    		if (role != null) {
    			eventFilter.setOption("role", role);
    			roomFilter.setOption("role", role);
    			u.setRole(role);
    		}
    	}
    	
    	EventRights rights = null;
    	if (checkRights()) {
    		rights = new SimpleEventRights(helper.getUser(), false, sessionId);
    		rights.checkAccess();
    		if (request.getResourceType() == ResourceType.PERSON && !rights.canSeeSchedule(request.getResourceExternalId()))
    			throw rights.getException();
    	} else {
    		rights = new SimpleEventRights(u, false, sessionId);
    	}
    	
    	print(helper, new EventLookupBackend().findEvents(request, rights));
	}
	
	protected boolean checkRights() {
		return true;
	}
	
	protected void print(ExportHelper helper, List<EventInterface> events) throws IOException {
		helper.setup("text/csv", reference(), false);
    	print(new CSVPrinter(helper.getWriter(), !"false".equals(helper.getParameter("smart"))), events);
	}
	
	protected void print(Printer out, List<EventInterface> events) {
		out.println(MESSAGES.colName(), MESSAGES.colSection(), MESSAGES.colType(), MESSAGES.colDayOfWeek(), MESSAGES.colFirstDate(), MESSAGES.colLastDate(),
				MESSAGES.colPublishedStartTime(), MESSAGES.colPublishedEndTime(), MESSAGES.colAllocatedStartTime(), MESSAGES.colAllocatedEndTime(),
				MESSAGES.colLocation(), MESSAGES.colLimit(), MESSAGES.colEnrollment(),
				MESSAGES.colSponsorOrInstructor(), MESSAGES.colEmail(),
				MESSAGES.colMainContact(), MESSAGES.colEmail(), MESSAGES.colApproval());
		
		DateFormat df = new SimpleDateFormat(CONSTANTS.eventDateFormat(), Localization.getJavaLocale());
		
		for (EventInterface event: events) {
			for (MultiMeetingInterface multi: EventInterface.getMultiMeetings(event.getMeetings(), true, false)) {
				MeetingInterface meeting = multi.getMeetings().first();
				out.print(
					event.getName(),
					event.getSectionNumber(),
					event.hasInstruction() ? event.getInstruction() : event.getType().getAbbreviation(),
					multi.getDays(CONSTANTS),
					df.format(multi.getFirstMeetingDate()),
					multi.getNrMeetings() == 1 ? null : df.format(multi.getLastMeetingDate()),
					meeting.getStartTime(CONSTANTS, true),
					meeting.getEndTime(CONSTANTS, true),
					meeting.getStartTime(CONSTANTS, false),
					meeting.getEndTime(CONSTANTS, false),
					meeting.getLocationName(),
					meeting.hasLocation() && meeting.getLocation().hasSize() ? meeting.getLocation().getSize().toString() : null,
					event.hasInstructors() ? event.getInstructorNames("\n") : event.hasSponsor() ? event.getSponsor().getName() : null,
							event.hasInstructors() ? event.getInstructorEmails("\n") : event.hasSponsor() ? event.getSponsor().getEmail() : null,
					event.hasMaxCapacity() ? event.getMaxCapacity().toString() : null,
					event.hasContact() ? event.getContact().getName() : null,
					event.hasContact() ? event.getContact().getEmail() : null,
					multi.isApproved() ? df.format(multi.getApprovalDate()) : MESSAGES.approvalNotApproved()
					);
			}
			out.println();
		}
	}
	
	public static interface Printer {
		public void print(String... fields);
		public void println(String... fields);
	}
		
	public static class CSVPrinter implements Printer {
		private PrintWriter iOut;
		private String[] iLastLine = null;
		private boolean iCheckLast = false;
		
		public CSVPrinter(PrintWriter writer, boolean checkLast) {
			iOut = writer;
			iCheckLast = checkLast;
		}
		
		public void print(String... fields) {
			int idx = 0;
			for (String f: fields) {
				if (f != null && !f.isEmpty()) {
					if (!iCheckLast || !f.equals(iLastLine == null || idx >= iLastLine.length ? null : iLastLine[idx]))
						iOut.print("\"" + f.replace("\"", "\"\"") + "\"");
				}
				iOut.print(","); idx ++;
			}
			iOut.println();
			iLastLine = fields;
		}
		
		public void println(String... fields) {
			if (fields != null && fields.length > 0) print(fields);
			iLastLine = null;
		}
	}
}
