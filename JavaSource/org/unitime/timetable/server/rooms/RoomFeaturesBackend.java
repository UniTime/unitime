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
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.SearchRoomFeaturesRequest;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SearchRoomFeaturesRequest.class)
public class RoomFeaturesBackend implements GwtRpcImplementation<SearchRoomFeaturesRequest, GwtRpcResponseList<FeatureInterface>>{

	@Override
	public GwtRpcResponseList<FeatureInterface> execute(SearchRoomFeaturesRequest request, SessionContext context) {
		if (request.hasSessionId())
			context = new EventContext(context, request.getSessionId());

		if (context.isAuthenticated())
			request.getFilter().setOption("user", context.getUser().getExternalUserId());

		context.checkPermission(Right.RoomFeatures);
		GwtRpcResponseList<FeatureInterface> list = new GwtRpcResponseList<FeatureInterface>();
		
		FilterRpcResponse filterResponse = new FilterRpcResponse();
		new RoomFilterBackend().enumarate(request.getFilter(), filterResponse, new EventContext(context, context.getUser().getCurrentAcademicSessionId()));
		Map<Long, Entity> locations = new HashMap<Long, Entity>();
		if (filterResponse.hasResults())
			for (Entity entity: filterResponse.getResults())
				locations.put(entity.getUniqueId(), entity);
		
		boolean filterDepartments = context.getUser() != null && !context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
		
		for (GlobalRoomFeature f: GlobalRoomFeature.getAllGlobalRoomFeatures(context.getUser().getCurrentAcademicSessionId())) {
			FeatureInterface feature = new FeatureInterface(f.getUniqueId(), f.getAbbv(), f.getLabel());
			if (f.getFeatureType() != null)
				feature.setType(new FeatureTypeInterface(f.getFeatureType().getUniqueId(), f.getFeatureType().getReference(), f.getFeatureType().getLabel(), f.getFeatureType().isShowInEventManagement()));
			feature.setCanEdit(context.hasPermission(f, Right.GlobalRoomFeatureEdit));
			feature.setCanDelete(context.hasPermission(f, Right.GlobalRoomFeatureDelete));
			feature.setSessionId(f.getSession().getUniqueId());
			feature.setSessionName(f.getSession().getLabel());
			feature.setDescription(f.getDescription());
			
			for (Location location: f.getRooms()) {
				Entity e = locations.get(location.getUniqueId());
				if (e != null)
					feature.addRoom(e);
			}
			if (feature.hasRooms())
				Collections.sort(feature.getRooms());
			
			list.add(feature);
		}
		
		for (DepartmentRoomFeature f: DepartmentRoomFeature.getAllDepartmentRoomFeaturesInSession(context.getUser().getCurrentAcademicSessionId())) {
			if (filterDepartments && !context.getUser().getCurrentAuthority().hasQualifier(f.getDepartment())) continue;
			FeatureInterface feature = new FeatureInterface(f.getUniqueId(), f.getAbbv(), f.getLabel());
			if (f.getFeatureType() != null)
				feature.setType(new FeatureTypeInterface(f.getFeatureType().getUniqueId(), f.getFeatureType().getReference(), f.getFeatureType().getLabel(), f.getFeatureType().isShowInEventManagement()));
			feature.setDepartment(RoomDetailsBackend.wrap(f.getDepartment(), null, null));
			feature.setCanEdit(context.hasPermission(f, Right.DepartmenalRoomFeatureEdit));
			feature.setCanDelete(context.hasPermission(f, Right.DepartmenalRoomFeatureDelete));
			feature.setSessionId(f.getDepartment().getSession().getUniqueId());
			feature.setSessionName(f.getDepartment().getSession().getLabel());
			feature.setDescription(f.getDescription());

			for (Location location: f.getRooms()) {
				Entity e = locations.get(location.getUniqueId());
				if (e != null)
					feature.addRoom(e);
			}
			if (feature.hasRooms())
				Collections.sort(feature.getRooms());
			
			list.add(feature);
		}
		
		return list;
	}

}
