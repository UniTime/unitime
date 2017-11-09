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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.events.EventLookupBackend;
import org.unitime.timetable.events.ResourceLookupBackend;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.events.EventDetailBackend;
import org.unitime.timetable.gwt.command.server.GwtRpcServlet;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApproveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventDetailRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.SaveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.SaveOrApproveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.SaveOrApproveEventRpcResponse;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.EventContactDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.MeetingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
@Service("/api/events")
public class EventsConnector extends ApiConnector {
	private @Autowired ApplicationContext applicationContext;

	@Override
	public void doGet(ApiHelper helper) throws IOException {
		if (helper.getParameter("eventId") != null) {
			Event event = EventDAO.getInstance().get(helper.getRequiredParameterLong("eventId"));
			if (event == null)
				throw new IllegalArgumentException("Given event no longer exists.");
			
			Long sessionId = helper.getAcademicSessionId();
			if (sessionId == null) {
				if (event.getSession() != null)
					sessionId = event.getSession().getUniqueId();
				else {
					for (Meeting m: event.getMeetings()) {
						if (m.getLocation() != null) {
							sessionId = m.getLocation().getSession().getUniqueId();
							break;
						}
					}
				}
			}
			if (sessionId == null)
				throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
			
			helper.getSessionContext().checkPermissionAnyAuthority(sessionId, "Session", Right.ApiRetrieveEvents);
			
			EventDetailRpcRequest request = new EventDetailRpcRequest();
			request.setEventId(event.getUniqueId());
			request.setSessionId(sessionId);
			
			EventContext context = new EventContext(helper.getSessionContext(), helper.getSessionContext().getUser(), sessionId);
			helper.setResponse(new EventDetailBackend().execute(request, context));
			
			return;
		}

		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId, helper.getHibSession());
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");
		
		helper.getSessionContext().checkPermissionAnyAuthority(session, Right.ApiRetrieveEvents);
		EventLookupRpcRequest request = new EventLookupRpcRequest();
    	request.setSessionId(sessionId);
    	String id = helper.getParameter("id");
    	if (id != null) request.setResourceId(Long.valueOf(id));
    	String ext = helper.getParameter("ext");
    	if (ext != null) request.setResourceExternalId(ext);
    	String type = helper.getParameter("type");
    	if (type == null)
    		type = ResourceType.ROOM.name();
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
		
		if (request.getResourceType() != ResourceType.ROOM && request.getResourceType() != ResourceType.PERSON && request.getResourceId() == null) {
			String name = helper.getParameter("name");
			if (name != null) {
				ResourceInterface resource = new ResourceLookupBackend().findResource(request.getSessionId(), request.getResourceType(), name);
				if (resource != null)
					request.setResourceId(resource.getId());
			}
		}
    	
    	EventContext context = new EventContext(helper.getSessionContext(), helper.getSessionContext().getUser(), sessionId);
    	
    	List<EventInterface> events = new EventLookupBackend().findEvents(request, context);
    	
    	if (!"1".equals(helper.getParameter("ua"))) {
    		for (Iterator<EventInterface> i = events.iterator(); i.hasNext();) {
    			EventInterface event = i.next();
    			if (event.getType() == EventType.Unavailabile) i.remove();
    		}
    	}
    	
    	helper.setResponse(events);
	}
	
	public void fixContact(Long sessionId, ContactInterface c) {
		if (c.getExternalId() != null) {
			org.hibernate.Session hibSession = EventContactDAO.getInstance().getSession();
			EventContact contact = (EventContact)hibSession.createQuery(
					"from EventContact where externalUniqueId = :userId"
					).setString("userId", c.getExternalId()).setMaxResults(1).uniqueResult();
			if (contact != null) {
				c.setFirstName(contact.getFirstName());
				c.setMiddleName(contact.getMiddleName());
				c.setLastName(contact.getLastName());
				c.setAcademicTitle(contact.getAcademicTitle());
				c.setEmail(contact.getEmailAddress());
				c.setPhone(contact.getPhone());
				return;
			}
			TimetableManager manager = (TimetableManager)hibSession.createQuery(
					"from TimetableManager where externalUniqueId = :userId"
					).setString("userId", c.getExternalId()).setMaxResults(1).uniqueResult();
			if (manager != null) {
				c.setExternalId(manager.getExternalUniqueId());
				c.setFirstName(manager.getFirstName());
				c.setMiddleName(manager.getMiddleName());
				c.setLastName(manager.getLastName());
				c.setAcademicTitle(manager.getAcademicTitle());
				c.setEmail(manager.getEmailAddress());
				return;
			}
			DepartmentalInstructor instructor = (DepartmentalInstructor)hibSession.createQuery(
					"from DepartmentalInstructor where department.session.uniqueId = :sessionId and externalUniqueId = :userId"
					).setLong("sessionId", sessionId).setString("userId", c.getExternalId()).setMaxResults(1).uniqueResult();
			if (instructor != null) {
				c.setExternalId(instructor.getExternalUniqueId());
				c.setFirstName(instructor.getFirstName());
				c.setMiddleName(instructor.getMiddleName());
				c.setLastName(instructor.getLastName());
				c.setAcademicTitle(instructor.getAcademicTitle());
				c.setEmail(instructor.getEmail());
				return;
			}
			Staff staff = (Staff)hibSession.createQuery(
					"from Staff where externalUniqueId = :userId"
					).setString("userId", c.getExternalId()).setMaxResults(1).uniqueResult();
			if (staff != null) {
				c.setExternalId(staff.getExternalUniqueId());
				c.setFirstName(staff.getFirstName());
				c.setMiddleName(staff.getMiddleName());
				c.setLastName(staff.getLastName());
				c.setAcademicTitle(staff.getAcademicTitle());
				c.setEmail(staff.getEmail());
				return;
			}
			Student student = (Student)hibSession.createQuery(
					"from Student where session.uniqueId = :sessionId and externalUniqueId = :userId"
					).setLong("sessionId", sessionId).setString("userId", c.getExternalId()).setMaxResults(1).uniqueResult();
			if (student != null) {
				c.setExternalId(student.getExternalUniqueId());
				c.setFirstName(student.getFirstName());
				c.setMiddleName(student.getMiddleName());
				c.setLastName(student.getLastName());
				c.setAcademicTitle(student.getAcademicTitle());
				c.setEmail(student.getEmail());
				return;
			}
		} else if (c.hasEmail()) {
			List<PersonInterface> people = GwtRpcServlet.execute(new PersonInterface.LookupRequest(c.getEmail(), "mustHaveExternalId,session=" + sessionId), applicationContext, null);
			if (people != null) {
				for (PersonInterface person: people) {
					c.setFirstName(person.getFirstName());
					c.setMiddleName(person.getMiddleName());
					c.setLastName(person.getLastName());
					c.setAcademicTitle(person.getAcademicTitle());
					c.setEmail(person.getEmail());
					c.setPhone(person.getPhone());
					c.setExternalId(person.getId());
					return;
				}
			}
		}
	}
	
	@Override
	public void doPost(ApiHelper helper) throws IOException {
		EventInterface event = helper.getRequest(EventInterface.class);
		if (event == null)
			throw new IllegalArgumentException("No event data provided.");
		
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null && event.getId() != null) {
			Event e = EventDAO.getInstance().get(event.getId());
			if (e != null) {
				if (e.getSession() != null)
					sessionId = e.getSession().getUniqueId();
				else {
					for (Meeting m: e.getMeetings()) {
						if (m.getLocation() != null) {
							sessionId = m.getLocation().getSession().getUniqueId();
							break;
						}
					}
				}
			}
		}
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		if (event.hasMeetings())
			for (MeetingInterface meeting: event.getMeetings()) {
				if (meeting.getId() != null && meeting.getMeetingDate() == null) {
					Meeting m = MeetingDAO.getInstance().get(meeting.getId());
					if (m == null) continue;
					meeting.setMeetingDate(m.getMeetingDate());
					meeting.setDayOfWeek(Constants.getDayOfWeek(m.getMeetingDate()));
					meeting.setStartTime(m.getStartTime().getTime());
					meeting.setStopTime(m.getStopTime().getTime());
					meeting.setStartSlot(m.getStartPeriod());
					meeting.setEndSlot(m.getStopPeriod());
					meeting.setStartOffset(m.getStartOffset() == null ? 0 : m.getStartOffset());
					meeting.setEndOffset(m.getStopOffset() == null ? 0 : m.getStopOffset());
					meeting.setApprovalDate(m.getApprovalDate());
					meeting.setApprovalStatus(m.getApprovalStatus());
					if (m.getLocation() != null) {
						ResourceInterface location = new ResourceInterface();
						location.setType(ResourceType.ROOM);
						location.setId(m.getLocation().getUniqueId());
						location.setName(m.getLocation().getLabel());
						location.setSize(m.getLocation().getCapacity());
						location.setRoomType(m.getLocation().getRoomTypeLabel());
						location.setBreakTime(m.getLocation().getEffectiveBreakTime());
						location.setMessage(m.getLocation().getEventMessage());
						location.setIgnoreRoomCheck(m.getLocation().isIgnoreRoomCheck());
						location.setDisplayName(m.getLocation().getDisplayName());
						meeting.setLocation(location);
					}
				}
				if (meeting.hasLocation() && meeting.getLocation().getId() == null && meeting.getLocationName() != null) {
					Location l = Location.findByName(LocationDAO.getInstance().getSession(), sessionId, meeting.getLocationName());
					if (l != null) {
						ResourceInterface location = new ResourceInterface();
						location.setType(ResourceType.ROOM);
						location.setId(l.getUniqueId());
						location.setName(l.getLabel());
						location.setSize(l.getCapacity());
						location.setRoomType(l.getRoomTypeLabel());
						location.setBreakTime(l.getEffectiveBreakTime());
						location.setMessage(l.getEventMessage());
						location.setIgnoreRoomCheck(l.isIgnoreRoomCheck());
						location.setDisplayName(l.getDisplayName());
						meeting.setLocation(location);
					}
				}
			}
		if (event.hasContact())
			fixContact(sessionId, event.getContact());
		if (event.hasAdditionalContacts())
			for (ContactInterface c: event.getAdditionalContacts())
				fixContact(sessionId, c);
		
		String op = helper.getParameter("operation");
		SaveOrApproveEventRpcRequest.Operation operation = null;
		if (op != null)
			operation = SaveOrApproveEventRpcRequest.Operation.valueOf(op.toUpperCase());
		
		Session session = SessionDAO.getInstance().get(sessionId, helper.getHibSession());
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");
		
		helper.getSessionContext().checkPermissionAnyAuthority(session, Right.ApiRetrieveEvents);
		
		EventContext context = new EventContext(helper.getSessionContext(), helper.getSessionContext().getUser(), sessionId);
		
		if (event.getContact() == null)
			event.setContact(GwtRpcServlet.execute(new EventPropertiesRpcRequest(sessionId), applicationContext, context).getMainContact());
		
		SaveOrApproveEventRpcRequest save = null;
		if (op == null)
			save = new SaveEventRpcRequest();
		else {
			switch (operation) {
			case CREATE:
			case UPDATE:
			case DELETE:
				save = new SaveEventRpcRequest();
				break;
			default:
				ApproveEventRpcRequest approve = new ApproveEventRpcRequest();
				approve.setOperation(operation);
				if (event.hasMeetings())
					for (MeetingInterface m: event.getMeetings())
						approve.addMeeting(m);
				save = approve;
				break;
			}
		}
		save.setEvent(event);
		save.setEmailConfirmation(helper.getOptinalParameterBoolean("email", true));
		save.setMessage(helper.getParameter("message"));
		save.setSessionId(sessionId);
		
		SaveOrApproveEventRpcResponse response = GwtRpcServlet.execute(save, applicationContext, context);
		helper.setResponse(response);
	}
	
	@Override
	public void doDelete(ApiHelper helper) throws IOException {
		EventInterface event = new EventInterface();
		event.setId(helper.getRequiredParameterLong("eventId"));
		event.setMeetings(new TreeSet<MeetingInterface>());
		Event e = EventDAO.getInstance().get(event.getId());
		if (e == null)
			throw new IllegalArgumentException("Given event no longer exists.");
		
		event.setType(EventInterface.EventType.values()[e.getEventType()]);
		if (e.getMainContact() != null) {
			ContactInterface contact = new ContactInterface();
			contact.setFirstName(e.getMainContact().getFirstName());
			contact.setMiddleName(e.getMainContact().getMiddleName());
			contact.setLastName(e.getMainContact().getLastName());
			contact.setAcademicTitle(e.getMainContact().getAcademicTitle());
			contact.setEmail(e.getMainContact().getEmailAddress());
			event.setContact(contact);
		}
		
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null) {
			if (e.getSession() != null)
				sessionId = e.getSession().getUniqueId();
			else {
				for (Meeting m: e.getMeetings()) {
					if (m.getLocation() != null) {
						sessionId = m.getLocation().getSession().getUniqueId();
						break;
					}
				}
			}
		}
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId, helper.getHibSession());
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");
		
		helper.getSessionContext().checkPermissionAnyAuthority(session, Right.ApiRetrieveEvents);
		
		SaveEventRpcRequest save = new SaveEventRpcRequest();
		save.setEvent(event);
		save.setEmailConfirmation(helper.getOptinalParameterBoolean("email", true));
		save.setMessage(helper.getParameter("message"));
		save.setSessionId(sessionId);
		
		EventContext context = new EventContext(helper.getSessionContext(), helper.getSessionContext().getUser(), sessionId);
		
		SaveOrApproveEventRpcResponse response = GwtRpcServlet.execute(save, applicationContext, context);
		helper.setResponse(response);
	}

	@Override
	protected String getName() {
		return "events";
	}
}
