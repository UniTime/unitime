/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.rooms;

import java.text.DecimalFormat;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomHintRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomHintResponse;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.security.SessionContext;

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
			
			String minimap = ApplicationProperties.getProperty("unitime.minimap.hint");
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
	    		response.setArea(new DecimalFormat(ApplicationProperties.getProperty("unitime.room.area.units.format", "#,##0.00")).format(location.getArea()) + " " + MSG.roomAreaUnitsShort());

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
	    	
	    	if ("true".equals(ApplicationProperties.getProperty("unitime.roomHint.showBreakTime", "false")))
	    		response.setBreakTime(location.getEffectiveBreakTime());
	    	
	    	return response;
		} else {
			Building building = BuildingDAO.getInstance().get(-request.getLocationId());
			if (building == null) return null;
			
			response.setId(-building.getUniqueId());
			response.setLabel(building.getName());
			
	    	String minimap = ApplicationProperties.getProperty("unitime.minimap.hint");
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
