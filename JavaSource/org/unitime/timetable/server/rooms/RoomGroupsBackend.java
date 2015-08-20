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
package org.unitime.timetable.server.rooms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.events.RoomFilterBackend;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.SearchRoomGroupsRequest;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SearchRoomGroupsRequest.class)
public class RoomGroupsBackend implements GwtRpcImplementation<SearchRoomGroupsRequest, GwtRpcResponseList<GroupInterface>>{

	@Override
	public GwtRpcResponseList<GroupInterface> execute(SearchRoomGroupsRequest request, SessionContext context) {
		context.checkPermission(Right.RoomGroups);
		GwtRpcResponseList<GroupInterface> list = new GwtRpcResponseList<GroupInterface>();
		
		FilterRpcResponse filterResponse = new FilterRpcResponse();
		new RoomFilterBackend().enumarate(request.getFilter(), filterResponse, new EventContext(context, context.getUser().getCurrentAcademicSessionId()));
		Map<Long, Entity> locations = new HashMap<Long, Entity>();
		if (filterResponse.hasResults())
			for (Entity entity: filterResponse.getResults())
				locations.put(entity.getUniqueId(), entity);
		
		boolean filterDepartments = context.getUser() != null && !context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
		
		for (RoomGroup g: RoomGroup.getAllRoomGroupsForSession(context.getUser().getCurrentAcademicSessionId())) {
			GroupInterface group = new GroupInterface(g.getUniqueId(), g.getAbbv(), g.getName());
			group.setDescription(g.getDescription());
			group.setDefault(g.isDefaultGroup());
			if (g.getDepartment() != null) {
				if (filterDepartments && !context.getUser().getCurrentAuthority().hasQualifier(g.getDepartment())) continue;
				group.setDepartment(RoomDetailsBackend.wrap(g.getDepartment(), null, null));
				group.setCanEdit(context.hasPermission(g, Right.DepartmenalRoomGroupEdit));
				group.setCanDelete(context.hasPermission(g, Right.DepartmenalRoomGroupDelete));
			} else {
				group.setCanEdit(context.hasPermission(g, Right.GlobalRoomGroupEdit));
				group.setCanDelete(context.hasPermission(g, Right.GlobalRoomGroupDelete));
			}
			
			for (Location location: g.getRooms()) {
				Entity e = locations.get(location.getUniqueId());
				if (e != null)
					group.addRoom(e);
			}
			if (group.hasRooms())
				Collections.sort(group.getRooms());
			
			list.add(group);
		}
		
		return list;
	}

}
