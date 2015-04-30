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

import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesRequest;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(RoomPropertiesRequest.class)
public class RoomPropertiesBackend implements GwtRpcImplementation<RoomPropertiesRequest, RoomPropertiesInterface> {

	@Override
	public RoomPropertiesInterface execute(RoomPropertiesRequest request, SessionContext context) {
		RoomPropertiesInterface response = new RoomPropertiesInterface();
		
		response.setAcademicSessionId(context.getUser() == null ? null : context.getUser().getCurrentAcademicSessionId());
		
		response.setCanEditDepartments(context.hasPermission(Right.EditRoomDepartments));
		response.setCanExportCsv(context.hasPermission(Right.RoomsExportCsv));
		response.setCanExportPdf(context.hasPermission(Right.RoomsExportPdf));
		response.setCanEditRoomExams(context.hasPermission(Right.EditRoomDepartmentsExams));
		response.setCanAddRoom(context.hasPermission(Right.AddRoom) || context.hasPermission(Right.AddNonUnivLocation));
		
		return response;
	}

}
