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

import java.text.DecimalFormat;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomHintRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomHintResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.model.AttachementType;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.LocationPicture;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(RoomHintRequest.class)
public class RoomHintBackend implements GwtRpcImplementation<RoomHintRequest, RoomHintResponse> {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	public static final CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public RoomHintResponse execute(RoomHintRequest request, SessionContext context) {
		RoomHintResponse response = new RoomHintResponse();
		
		if (request.getLocationId() >= 0) {
			Location location = LocationDAO.getInstance().get(request.getLocationId());
			if (location == null) return null;
			
			response.setId(location.getUniqueId());
			response.setLabel(location.getLabel());
			response.setDisplayName(location.getDisplayName());
			response.setRoomTypeLabel(location.getRoomTypeLabel());
			
			String minimap = ApplicationProperty.RoomHintMinimapUrl.value();
	    	if (minimap != null && location.getCoordinateX() != null && location.getCoordinateY() != null)
	    		response.setMiniMapUrl(minimap
	    				.replace("%x", location.getCoordinateX().toString())
	    				.replace("%y", location.getCoordinateY().toString())
	    				.replace("%n", location.getLabel())
	    				.replace("%i", location.getExternalUniqueId() == null ? "" : location.getExternalUniqueId()));
	    	
	    	response.setCapacity(location.getCapacity());
	    	if (location.getExamCapacity() != null && location.getExamCapacity() > 0 && !location.getExamCapacity().equals(location.getCapacity()) && !location.getExamTypes().isEmpty()) {
	    		response.setExamCapacity(location.getExamCapacity());
	    		if (location.getExamTypes().size() == 1)
	    			response.setExamType(location.getExamTypes().iterator().next().getLabel().toLowerCase());
	    	}
	    	
	    	if (location.getArea() != null)
	    		response.setArea(new DecimalFormat(ApplicationProperty.RoomAreaUnitsFormat.value()).format(location.getArea()) + " " + MSG.roomAreaUnitsShort());

	    	for (GlobalRoomFeature f: location.getGlobalRoomFeatures()) {
	    		String type = (f.getFeatureType() == null ? MESSAGES.roomFeatures() : f.getFeatureType().getReference());
	    		response.addFeature(type, f.getLabel());
	    	}
	    	String groups = "";
	    	for (RoomGroup g: location.getGlobalRoomGroups()) {
	    		if (!groups.isEmpty()) groups += ", ";
	    		groups += g.getName();
	    	}
	    	if (!groups.isEmpty())
	    		response.setGroups(groups);
	    	
	    	response.setEventStatus(location.getEventDepartment() == null ? null : location.getEffectiveEventStatus().toString());
	    	response.setEventDepartment(location.getEventDepartment() == null ? MESSAGES.noEventDepartment() : location.getEventDepartment().getDeptCode() + " - " + location.getEventDepartment().getName());
	    	
	    	response.setNote(location.getEventMessage());
	    	
	    	if (ApplicationProperty.RoomHintShowBreakTime.isTrue())
	    		response.setBreakTime(location.getEffectiveBreakTime());
	    	
	    	response.setIgnoreRoomCheck(location.isIgnoreRoomCheck());
	    	
	    	for (LocationPicture picture: new TreeSet<LocationPicture>(location.getPictures())) {
	    		if (picture.getType() != null && (
	    				!AttachementType.VisibilityFlag.IS_IMAGE.in(picture.getType().getVisibility()) ||
	    				!AttachementType.VisibilityFlag.SHOW_ROOM_TOOLTIP.in(picture.getType().getVisibility())
	    				)) continue;
	    		response.addPicture(new RoomPictureInterface(picture.getUniqueId(), picture.getFileName(), picture.getContentType(), picture.getTimeStamp().getTime(), RoomPicturesBackend.getPictureType(picture.getType())));
	    	}
	    	
	    	return response;
		} else {
			Building building = BuildingDAO.getInstance().get(-request.getLocationId());
			if (building == null) return null;
			
			response.setId(-building.getUniqueId());
			response.setLabel(building.getName());
			
	    	String minimap = ApplicationProperty.RoomHintMinimapUrl.value();
	    	if (minimap != null && building.getCoordinateX() != null && building.getCoordinateY() != null)
	    		response.setMiniMapUrl(minimap
	    				.replace("%x", building.getCoordinateX().toString())
	    				.replace("%y", building.getCoordinateY().toString())
	    				.replace("%n", building.getAbbreviation())
	    				.replace("%i", building.getExternalUniqueId() == null ? "" : building.getExternalUniqueId()));
	    	
	    	return response;
		}
	}
}
