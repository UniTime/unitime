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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFilterRpcRequest;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.server.rooms.RoomDetailsBackend;

/**
 * @author Tomas Muller
 */
@Service("/api/rooms")
public class RoomsConnector extends ApiConnector {
	
	@Override
	@PreAuthorize("checkPermission(#helper.academicSessionId, 'Session', 'ApiRetrieveRooms')")
	public void doGet(ApiHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Session session = SessionDAO.getInstance().get(sessionId);
		if (session == null)
			throw new IllegalArgumentException("Given academic session no longer exists.");

		RoomFilterRpcRequest request = new RoomFilterRpcRequest();
		request.setCommand(FilterRpcRequest.Command.ENUMERATE);
    	request.setSessionId(sessionId);
    	for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
    		String command = e.nextElement();
    		if (command.equals("r:text")) {
    			request.setText(helper.getParameter("r:text"));
    		} else if (command.startsWith("r:")) {
    			for (String value: helper.getParameterValues(command))
    				request.addOption(command.substring(2), value);
    		}
    	}
    	request.setOption("flag", "plain");
    	
    	EventContext context = new EventContext(helper.getSessionContext(), helper.getSessionContext().getUser(), sessionId);
    	
    	FilterRpcResponse response = new FilterRpcResponse();
    	new RoomDetailsBackend().enumarate(request, response, context); 
    	
    	List<RoomDetailInterface> rooms = new ArrayList<RoomDetailInterface>();
    	if (response.hasResults()) {
    		for (Entity e: response.getResults())
    			rooms.add((RoomDetailInterface)e);
    	}
    	
    	helper.setResponse(rooms);
	}
}
