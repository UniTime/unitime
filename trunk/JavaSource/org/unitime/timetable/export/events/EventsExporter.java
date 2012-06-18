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
package org.unitime.timetable.export.events;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.commons.User;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.events.EventLookupBackend;
import org.unitime.timetable.events.EventRights;
import org.unitime.timetable.events.SimpleEventRights;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.client.events.EventComparator;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventFlag;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;

public abstract class EventsExporter implements Exporter {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

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
    	} else if (helper.getUser() != null) {
    		u = new User();
    		u.setId(helper.getUser().getId());
    		eventFilter.setOption("user", helper.getUser().getId());
    		roomFilter.setOption("user", helper.getUser().getId());
    		if (helper.getUser().getRole() != null) {
    			u.setRole(helper.getUser().getRole());
        		eventFilter.setOption("role", helper.getUser().getRole());
        		roomFilter.setOption("role", helper.getUser().getRole());
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
    	
    	List<EventInterface> events = new EventLookupBackend().findEvents(request, rights);
    	
    	EventMeetingSortBy sort = (helper.getParameter("sort") == null ? null :
    		EventMeetingSortBy.values()[Integer.parseInt(helper.getParameter("sort"))]);
    	
    	int eventCookieFlags = (helper.getParameter("flags") == null ? EventInterface.sDefaultEventFlags : Integer.parseInt(helper.getParameter("flags")));
    	if (!rights.canLookupContacts())
    		eventCookieFlags = EventFlag.SHOW_MAIN_CONTACT.clear(eventCookieFlags);
    	eventCookieFlags = EventFlag.SHOW_SECTION.set(eventCookieFlags);
    	
    	print(helper, events, eventCookieFlags, sort);
	}
	
	protected abstract void print(ExportHelper helper, List<EventInterface> events, int eventCookieFlags, EventMeetingSortBy sort) throws IOException;
	
	protected boolean checkRights() {
		return true;
	}
	
	protected void hideColumns(Printer out, List<EventInterface> events, int eventCookieFlags) {
		for (EventFlag flag: EventFlag.values()) {
			if (!flag.in(eventCookieFlags)) hideColumn(out, events, flag);
		}
		boolean hasSection = false;
		for (EventInterface event: events) {
			if (getSection(event) != null) { hasSection = true; break; }
		}
		if (!hasSection) hideColumn(out, events, EventFlag.SHOW_SECTION);
	}
	
	protected void hideColumn(Printer out, List<EventInterface> events, EventFlag flag) {}
	
	protected void sort(List<EventInterface> events, final EventMeetingSortBy sort) {
		if (sort != null) {
    		Collections.sort(events, new Comparator<EventInterface>() {
				@Override
				public int compare(EventInterface e1, EventInterface e2) {
					int cmp = EventComparator.compareEvents(e1, e2, sort);
					if (cmp != 0) return cmp;
					Iterator<MeetingInterface> i1 = e1.getMeetings().iterator(), i2 = e2.getMeetings().iterator();
					while (i1.hasNext() && i2.hasNext()) {
						cmp = EventComparator.compareMeetings(i1.next(), i2.next(), sort);
						if (cmp != 0) return cmp;
					}
					cmp = EventComparator.compareFallback(e1, e2);
					if (cmp != 0) return cmp;
					i1 = e1.getMeetings().iterator(); i2 = e2.getMeetings().iterator();
					while (i1.hasNext() && i2.hasNext()) {
						cmp = EventComparator.compareFallback(i1.next(), i2.next());
						if (cmp != 0) return cmp;
					}
					if (i1.hasNext() && !i2.hasNext()) return 1;
					if (!i1.hasNext() && i2.hasNext()) return -1;
					return e1.compareTo(e2);
				}
			});
    	} else {
    		Collections.sort(events);
    	}
	}
	
	protected Set<EventMeeting> meetings(List<EventInterface> events, final EventMeetingSortBy sort) {
		TreeSet<EventMeeting> meetings = new TreeSet<EventMeeting>(new Comparator<EventMeeting>() {
			@Override
			public int compare(EventMeeting m1, EventMeeting m2) {
				if (sort != null) {
					int cmp = EventComparator.compareEvents(m1.getEvent(), m2.getEvent(), sort);
					if (cmp != 0) return cmp;
					cmp = EventComparator.compareMeetings(m1.getMeeting(), m2.getMeeting(), sort);
					if (cmp != 0) return cmp;
					cmp = EventComparator.compareFallback(m1.getEvent(), m2.getEvent());
					if (cmp != 0) return cmp;
					cmp = EventComparator.compareFallback(m1.getMeeting(), m2.getMeeting());
					if (cmp != 0) return cmp;
				}
				return m1.compareTo(m2);
			}
		});
		for (EventInterface event: events)
			for (MeetingInterface meeting: event.getMeetings())
				meetings.add(new EventMeeting(event, meeting));
		return meetings;
	}

	public static class EventMeeting implements Comparable<EventMeeting> {
		private EventInterface iEvent;
		private MeetingInterface iMeeting;
		
		private EventMeeting(EventInterface event, MeetingInterface meeting) {
			iEvent = event;
			iMeeting = meeting;
		}
		
		public EventInterface getEvent() { return iEvent; }
		public MeetingInterface getMeeting() { return iMeeting; }
		
		@Override
		public int compareTo(EventMeeting m) {
			int cmp = getEvent().compareTo(m.getEvent());
			return (cmp == 0 ? getMeeting().compareTo(m.getMeeting()) : cmp);
		}
	}
	
	public String getName(EventInterface event) {
		if (event.hasCourseNames()) {
			String name = "";
			if (event.getType() == EventType.Course) { name = event.getName(); }
			for (String cn: event.getCourseNames())
				if (name.isEmpty()) {
					name += cn;
				} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
					name += "\n  " + cn;
				} else {
					name += "\n" + cn;
				}
			return name;
		} else {
			return event.getName();
		}
	}
	
	public String getSection(EventInterface event) {
		if (event.hasCourseNames()) {
			String section = "";
			if (event.getType() == EventType.Course) { section = ""; }
			if (event.hasExternalIds())
				for (String ex: event.getExternalIds()) {
					if (section.isEmpty()) {
						section += ex;
					} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
						section += "\n  " + ex;
					} else {
						section += "\n" + ex;
					}
				}
			else if (event.hasSectionNumber()) {
				section = event.getSectionNumber();
			}
			return section;
		} else {
			return event.getSectionNumber();
		}
	}
}
