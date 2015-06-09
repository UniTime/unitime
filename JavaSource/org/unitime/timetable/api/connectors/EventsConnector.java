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

import org.springframework.security.access.prepost.PreAuthorize;
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

/**
 * @author Tomas Muller
 */
@Service("/api/events")
public class EventsConnector extends ApiConnector {

	@Override
	@PreAuthorize("checkPermission(#helper.academicSessionId, 'Session', 'ApiRetrieveEvents')")
	public void doGet(ApiHelper helper) throws IOException {
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

}
