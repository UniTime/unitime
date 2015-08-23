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
package org.unitime.timetable.export.events;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.events.EventLookupBackend;
import org.unitime.timetable.events.ResourceLookupBackend;
import org.unitime.timetable.events.EventAction.EventContext;
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
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public abstract class EventsExporter implements Exporter {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public void export(ExportHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId);
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");

		EventLookupRpcRequest request = new EventLookupRpcRequest();
    	request.setSessionId(sessionId);
    	String id = helper.getParameter("id");
    	if (id != null) request.setResourceId(Long.valueOf(id));
    	String ext = helper.getParameter("ext");
    	if (ext != null) request.setResourceExternalId(ext);
    	String type = helper.getParameter("type");
    	if (type == null)
    		throw new IllegalArgumentException("Resource type not provided, please set the type parameter.");
    	request.setResourceType(ResourceType.valueOf(type.toUpperCase()));
    	EventFilterRpcRequest eventFilter = new EventFilterRpcRequest();
    	eventFilter.setSessionId(sessionId);
    	request.setEventFilter(eventFilter);
    	RoomFilterRpcRequest roomFilter = new RoomFilterRpcRequest();
    	roomFilter.setSessionId(sessionId);
    	for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
    		String command = e.nextElement();
    		if (command.equals("e:text")) {
    			eventFilter.setText(helper.getParameter("e:text"));
    	} else if (command.startsWith("e:")) {
    			for (String value: helper.getParameterValues(command))
    				eventFilter.addOption(command.substring(2), value);
    		} else if (command.equals("r:text")) {
    			roomFilter.setText(helper.getParameter("r:text"));
    		} else if (command.startsWith("r:")) {
    			for (String value: helper.getParameterValues(command))
    				roomFilter.addOption(command.substring(2), value);
    		}
    	}
		request.setRoomFilter(roomFilter);
    	
    	UserContext u = helper.getSessionContext().getUser();
    	String user = helper.getParameter("user");
    	if (u == null && user != null && !checkRights()) {
    		u = new UniTimeUserContext(user, null, null, null);
    		String role = helper.getParameter("role");
    		if (role != null) {
    			for (UserAuthority a: u.getAuthorities()) {
    				if (a.getAcademicSession() != null && a.getAcademicSession().getQualifierId().equals(sessionId) && role.equals(a.getRole())) {
    					u.setCurrentAuthority(a); break;
    				}
    			}
    		}
    	}
    	EventContext context = new EventContext(helper.getSessionContext(), u, sessionId);

    	if (checkRights()) {
    		context.checkPermission(Right.Events);
    		if (request.getResourceType() == ResourceType.PERSON && !context.getUser().getExternalUserId().equals(request.getResourceExternalId()))
    			context.checkPermission(Right.EventLookupSchedule);
    	} else if (!helper.isRequestEncoded() && request.getResourceType() == ResourceType.PERSON) {
    		throw new PageAccessException("Request parameters must be encrypted.");
    	}
    	
		if (request.getResourceType() != ResourceType.ROOM && request.getResourceType() != ResourceType.PERSON && request.getResourceId() == null) {
			String name = helper.getParameter("name");
			if (name != null) {
				ResourceInterface resource = new ResourceLookupBackend().findResource(request.getSessionId(), request.getResourceType(), name);
				if (resource != null)
					request.setResourceId(resource.getId());
			}
		}
    	
    	List<EventInterface> events = new EventLookupBackend().findEvents(request, context);
    	
    	String sortBy = helper.getParameter("sort");
    	EventMeetingSortBy sort = null;
    	boolean asc = true;
    	if (sortBy == null || sortBy.isEmpty()) {
    		sort = null; asc = true;
		} else if (sortBy.startsWith("+")) {
			asc = true;
			sort = EventMeetingSortBy.values()[Integer.parseInt(sortBy.substring(1))];
		} else if (sortBy.startsWith("-")) {
			asc = false;
			sort = EventMeetingSortBy.values()[Integer.parseInt(sortBy.substring(1))];
		} else {
			asc = true;
			sort = (sortBy == null ? null : EventMeetingSortBy.values()[Integer.parseInt(sortBy)]);	
		}
    	
    	int eventCookieFlags = (helper.getParameter("flags") == null ? EventInterface.sDefaultEventFlags : Integer.parseInt(helper.getParameter("flags")));
    	if (!context.hasPermission(Right.EventLookupContact)) {
    		eventCookieFlags = EventFlag.SHOW_MAIN_CONTACT.clear(eventCookieFlags);
    		eventCookieFlags = EventFlag.SHOW_LAST_CHANGE.clear(eventCookieFlags);
    	}
    	eventCookieFlags = EventFlag.SHOW_SECTION.set(eventCookieFlags);
    	
    	if (!"1".equals(helper.getParameter("ua"))) {
    		for (Iterator<EventInterface> i = events.iterator(); i.hasNext();) {
    			EventInterface event = i.next();
    			if (event.getType() == EventType.Unavailabile) i.remove();
    		}
    	}
    	
    	print(helper, events, eventCookieFlags, sort, asc);
	}
	
	protected abstract void print(ExportHelper helper, List<EventInterface> events, int eventCookieFlags, EventMeetingSortBy sort, boolean asc) throws IOException;
	
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
	
	protected void sort(List<EventInterface> events, final EventMeetingSortBy sort, boolean asc) {
		if (sort != null) {
    		Collections.sort(events, new ReverseComparator<EventInterface>(new Comparator<EventInterface>() {
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
			}, !asc));
    	} else {
    		Collections.sort(events);
    	}
	}
	
	protected Set<EventMeeting> meetings(List<EventInterface> events, final EventMeetingSortBy sort, boolean asc) {
		TreeSet<EventMeeting> meetings = new TreeSet<EventMeeting>(new ReverseComparator<EventMeeting>(new Comparator<EventMeeting>() {
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
		}, !asc));
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
	
	public String getTitle(EventInterface event) {
		if (event.hasCourseTitles()) {
			String title = "";
			String last = null;
			for (String ct: event.getCourseTitles()) {
				if (last != null && !last.isEmpty() && last.equals(ct))
					ct = "";
				else
					last = ct;
				if (title.isEmpty()) {
					title += ct;
				} else if (event.getInstruction() != null || event.getType() == EventType.Course) {
					title += "\n  " + ct;
				} else {
					title += "\n" + ct;
				}
			}
			return title;
		} else {
			return "";
		}
	}
	
	public String getSection(EventInterface event) {
		if (event.hasCourseNames()) {
			String section = "";
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
	
	public static class ReverseComparator<T> implements Comparator<T> {
		private Comparator<T> iComparator = null;
		private boolean iReverse;
		
		public ReverseComparator(Comparator<T> comparator, boolean reverse) {
			iComparator = comparator; iReverse = reverse;
		}

		@Override
		public int compare(T o1, T o2) {
			if (iReverse)
				return iComparator.compare(o2, o1);
			else
				return iComparator.compare(o1, o2);
		}
	}
}
