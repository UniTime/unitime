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
import java.util.List;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.EventServiceProviderInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomHintRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomHintResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.model.AttachmentType;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.LocationPicture;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.server.rooms.RoomDetailsBackend.UrlSigner;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(RoomHintRequest.class)
public class RoomHintBackend implements GwtRpcImplementation<RoomHintRequest, RoomHintResponse> {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

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
	    	if (minimap != null && location.getCoordinateX() != null && location.getCoordinateY() != null) {
	    		minimap = minimap
	    				.replace("%x", location.getCoordinateX().toString())
	    				.replace("%y", location.getCoordinateY().toString())
	    				.replace("%n", location.getLabel())
	    				.replace("%i", location.getExternalUniqueId() == null ? "" : location.getExternalUniqueId());
		    	String apikey = ApplicationProperty.RoomMapStaticApiKey.value();
		    	if (apikey != null && !apikey.isEmpty()) {
		    		minimap += "&key=" + apikey;
			    	String secret = ApplicationProperty.RoomMapStaticSecret.value();
		    		if (secret != null && !secret.isEmpty()) {
		    			try {
		    				minimap += "&signature=" + new UrlSigner(secret).signRequest(minimap);
						} catch (Exception e) {}
		    		}
		    	}
		    	response.setMiniMapUrl(minimap);
	    	}
	    	
	    	response.setCapacity(location.getCapacity());
	    	if (location.getExamCapacity() != null && location.getExamCapacity() > 0 && !location.getExamCapacity().equals(location.getCapacity()) && !location.getExamTypes().isEmpty()) {
	    		response.setExamCapacity(location.getExamCapacity());
	    		if (location.getExamTypes().size() == 1)
	    			response.setExamType(location.getExamTypes().iterator().next().getLabel().toLowerCase());
	    	}
	    	
	    	if (location.getArea() != null)
	    		response.setArea(new DecimalFormat(CONSTANTS.roomAreaFormat()).format(location.getArea()) + " " + (ApplicationProperty.RoomAreaUnitsMetric.isTrue() ? MSG.roomAreaMetricUnitsShort() : MSG.roomAreaUnitsShort()));

	    	for (GlobalRoomFeature f: location.getGlobalRoomFeatures()) {
	    		FeatureInterface feature = new FeatureInterface(f.getUniqueId(), f.getAbbv(), f.getLabel());
	    		feature.setDescription(f.getDescription());
	    		RoomFeatureType t = f.getFeatureType();
	    		if (t != null)
	    			feature.setType(new FeatureTypeInterface(t.getUniqueId(), t.getReference(), t.getLabel(), t.isShowInEventManagement()));
	    		response.addFeature(feature);
	    	}
	    	
	    	for (RoomGroup g: location.getGlobalRoomGroups()) {
	    		GroupInterface group = new GroupInterface(g.getUniqueId(), g.getAbbv(), g.getName());
	    		group.setDescription(g.getDescription());
	    		response.addGroup(group);
	    	}
	    	
	    	response.setEventStatus(location.getEventDepartment() == null ? null : location.getEffectiveEventStatus().toString());
	    	response.setEventDepartment(location.getEventDepartment() == null ? MESSAGES.noEventDepartment() : location.getEventDepartment().getDeptCode() + " - " + location.getEventDepartment().getName());
	    	
	    	response.setNote(location.getEventMessage());
	    	
	    	if (ApplicationProperty.RoomHintShowBreakTime.isTrue())
	    		response.setBreakTime(location.getEffectiveBreakTime());
	    	
	    	response.setIgnoreRoomCheck(location.isIgnoreRoomCheck());
	    	
	    	for (LocationPicture picture: new TreeSet<LocationPicture>(location.getPictures())) {
	    		if (picture.getType() != null && (
	    				!AttachmentType.VisibilityFlag.IS_IMAGE.in(picture.getType().getVisibility()) ||
	    				!AttachmentType.VisibilityFlag.SHOW_ROOM_TOOLTIP.in(picture.getType().getVisibility())
	    				)) continue;
	    		response.addPicture(new RoomPictureInterface(picture.getUniqueId(), picture.getFileName(), picture.getContentType(), picture.getTimeStamp().getTime(), RoomPicturesBackend.getPictureType(picture.getType())));
	    	}
	    	
	    	if (location.getEventDepartment() != null) {
		    	for (EventServiceProvider p: location.getAllowedServices()) {
		    		if (!p.isVisible() || p.isAllRooms()) continue;
		    		EventServiceProviderInterface provider = new EventServiceProviderInterface();
		    		provider.setId(p.getUniqueId());
					provider.setReference(p.getReference());
					provider.setLabel(p.getLabel());
					provider.setMessage(p.getNote());
					provider.setEmail(p.getEmail());
		    		if (p.getDepartment() != null)
		    			provider.setDepartmentId(p.getDepartment().getUniqueId());
		    		response.addService(provider);
		    	}
		    	for (EventServiceProvider p: (List<EventServiceProvider>)LocationDAO.getInstance().getSession().createQuery(
		    		"from EventServiceProvider where visible = true and allRooms = true and (session is null or session = :sessionId) and (department is null or department = :departmentId)"
		    		).setLong("sessionId", location.getSession().getUniqueId()).setLong("departmentId", location.getEventDepartment().getUniqueId()).setCacheable(true).list()) {
		    		EventServiceProviderInterface provider = new EventServiceProviderInterface();
		    		provider.setId(p.getUniqueId());
					provider.setReference(p.getReference());
					provider.setLabel(p.getLabel());
					provider.setMessage(p.getNote());
					provider.setEmail(p.getEmail());
		    		if (p.getDepartment() != null)
		    			provider.setDepartmentId(p.getDepartment().getUniqueId());
		    		response.addService(provider);
		    	}
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
