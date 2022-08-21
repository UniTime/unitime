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

import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.RoomInterface.GetRoomsOfABuildingRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(GetRoomsOfABuildingRequest.class)
public class GetRoomsOfABuildingBackend implements GwtRpcImplementation<GetRoomsOfABuildingRequest, GwtRpcResponseList<RoomDetailInterface>>{

	@Override
	public GwtRpcResponseList<RoomDetailInterface> execute(GetRoomsOfABuildingRequest request, SessionContext context) {
		context.checkPermission(Right.Rooms);
		
		GwtRpcResponseList response = new GwtRpcResponseList();
		for (Room room: (List<Room>)
				RoomDAO.getInstance().getSession().createQuery(
						"from Room r where r.building = :buildingId order by r.roomNumber")
					.setLong("buildingId", request.getBuildingId())
					.setCacheable(true).list()) {
			response.add(
					new RoomDetailInterface(
							room.getUniqueId(),
							room.getDisplayName(),
							room.getLabel(),
							"parent", room.getParentRoom() == null ? null : room.getParentRoom().getUniqueId().toString())
					);
		}
		
		return response;
	}

}
