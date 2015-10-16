/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.events.EventLookupBackend;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("/api/events")
public class EventsConnector extends ApiConnector {

	@Override
	public void doGet(ApiHelper helper) throws IOException {
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

	@Override
	protected String getName() {
		return "events";
	}
}
