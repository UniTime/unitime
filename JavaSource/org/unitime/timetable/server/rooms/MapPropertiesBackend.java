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

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.RoomInterface.MapPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.MapPropertiesRequest;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(MapPropertiesRequest.class)
public class MapPropertiesBackend implements GwtRpcImplementation<MapPropertiesRequest, MapPropertiesInterface> {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public MapPropertiesInterface execute(MapPropertiesRequest request, SessionContext context) {
		MapPropertiesInterface response = new MapPropertiesInterface();
		
		response.setGoogleMap(ApplicationProperty.RoomUseGoogleMap.isTrue());
		response.setGoogleMapApiKey(ApplicationProperty.GoogleMapsApiKey.value());
		
		response.setLeafletMap(!response.isGoogleMap() && ApplicationProperty.RoomUseLeafletMap.isTrue());
		response.setLeafletMapTiles(ApplicationProperty.RoomUseLeafletMapTiles.value());
		response.setLeafletMapAttribution(ApplicationProperty.RoomUseLeafletMapAttribution.value());
		
		return response;
	}

}
