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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.events.RoomFilterBackend;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomDetailInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFilterRpcRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPictureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomTypeInterface;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.LocationPicture;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(RoomFilterRpcRequest.class)
public class RoomDetailsBackend extends RoomFilterBackend {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	public static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Override
	public FilterRpcResponse execute(org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest request, EventContext context) {
		context.checkPermission(Right.Rooms);
		
		return super.execute(request, context);
	}

	@Override
	protected boolean checkEventStatus() { return false; }
	
	@Override
	protected boolean showRoomFeature(RoomFeatureType type) { return true; }
	
	@Override
	public RoomQuery getQuery(Long sessionId, Map<String, Set<String>> options) {
		RoomQuery query = super.getQuery(sessionId, options);
		
		Set<String> flags = (options == null ? null : options.get("flag"));
		boolean fetch = (flags != null && flags.contains("fetch"));
		if (fetch) {
			query.addFrom("fetch", "left join fetch l.roomDepts Frd " +
				" left join fetch l.examTypes Fxt" +
				" left join fetch l.features Ff" +
				" left join fetch l.roomGroups Fg" +
				" left join fetch l.roomType Ft");
		}
		
		return query;
	}
	
	@Override
	public void enumarate(org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest request, FilterRpcResponse response, EventContext context) {
		fixRoomFeatureTypes(request);
		request.addOption("flag", "fetch");

		String department = request.getOption("department");
    	boolean filterDepartments = !context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
    	List<ExamType> types = ExamType.findAllApplicable(context.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable);
    	boolean gridAsText = CommonValues.TextGrid.eq(UserProperty.GridOrientation.get(context.getUser()));
    	boolean html = true;
    	if (request.hasOptions("flag") && request.getOptions("flag").contains("gridAsText")) {
    		gridAsText = true;
    		html = false;
    	}

		Map<Long, Double> distances = new HashMap<Long, Double>();
		for (Location location: locations(request.getSessionId(), request.getOptions(), new Query(request.getText()), -1, distances, null)) {
			Double dist = distances.get(location.getUniqueId());
			Entity e = load(location, department, gridAsText, html, context, filterDepartments, types);
			e.setProperty("permId", location.getPermanentId().toString());
			if (dist != null)
				e.setProperty("distance", String.valueOf(dist == null ? 0l : Math.round(dist)));
			e.setProperty("overbook", context.hasPermission(location, Right.EventLocationOverbook) ? "1" : "0");
			response.addResult(e);
		}
	}
	
	protected static DepartmentInterface wrap(Department d, Location location, PreferenceLevel pref) {
		if (d == null) return null;
		DepartmentInterface department = new DepartmentInterface();
		department.setId(d.getUniqueId());
		department.setDeptCode(d.getDeptCode());
		department.setAbbreviation(d.getAbbreviation());
		department.setLabel(d.getName());
		department.setExternal(d.isExternalManager());
		department.setExtAbbreviation(d.getExternalMgrAbbv());
		department.setExtLabel(d.getExternalMgrLabel());
		department.setTitle(d.getLabel());
		if (pref != null && !PreferenceLevel.sNeutral.equals(pref.getPrefProlog()))
			department.setPreference(new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2color(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), false));
		if (location != null)
			department.setColor("#" + d.getRoomSharingColor(location.getRoomDepts()));
		return department;
	}
	
	protected RoomDetailInterface load(Location location, String department, boolean gridAsText, boolean html, SessionContext context, boolean filterDepartments, List<ExamType> types) {
		RoomDetailInterface response = new RoomDetailInterface(location.getUniqueId(), location.getDisplayName(), location.getLabel());
		
		response.setCanShowDetail(context.hasPermission(location, Right.RoomDetail));
		response.setCanSeeAvailability(context.hasPermission(location, Right.RoomDetailAvailability));
		response.setCanSeeEventAvailability(context.hasPermission(location, Right.RoomDetailEventAvailability));
		response.setCanSeePeriodPreferences(context.hasPermission(location, Right.RoomDetailPeriodPreferences));
		response.setCanChange(context.hasPermission(location, Right.RoomEdit));
		if (response.isCanChange()) {
			response.setCanChangeAvailability(context.hasPermission(location, Right.RoomEditAvailability));
			response.setCanChangeCapacity(context.hasPermission(location, Right.RoomEditChangeCapacity));
			response.setCanChangeControll(context.hasPermission(location, Right.RoomEditChangeControll));
			response.setCanChangeEventAvailability(context.hasPermission(location, Right.RoomEditEventAvailability));
			response.setCanChangeEventProperties(context.hasPermission(location, Right.RoomEditChangeEventProperties));
			response.setCanChangeExamStatus(context.hasPermission(location, Right.RoomEditChangeExaminationStatus));
			response.setCanChangeExternalId(context.hasPermission(location, Right.RoomEditChangeExternalId));
			response.setCanChangeFeatures(context.hasPermission(location, Right.RoomEditFeatures) || context.hasPermission(location, Right.RoomEditGlobalFeatures));
			response.setCanChangeGroups(context.hasPermission(location, Right.RoomEditGroups) || context.hasPermission(location, Right.RoomEditGlobalGroups));
			response.setCanChangePicture(context.hasPermission(location, Right.RoomEditChangePicture));
			response.setCanChangePreferences(context.hasPermission(location, Right.RoomEditPreference));
			response.setCanChangeRoomProperties(context.hasPermission(location, Right.RoomEditChangeRoomProperties));
			response.setCanChangeType(context.hasPermission(location, Right.RoomEditChangeType));
		}
		response.setCanDelete(context.hasPermission(location, Right.RoomDelete));
		
		response.setRoomType(new RoomTypeInterface(location.getRoomType().getUniqueId(), location.getRoomType().getLabel(), location.getRoomType().isRoom()));
		response.setX(location.getCoordinateX());
		response.setY(location.getCoordinateY());
		response.setArea(location.getArea());;
    	response.setCapacity(location.getCapacity());
    	response.setExamCapacity(location.getExamCapacity());
    	response.setIgnoreRoomCheck(location.isIgnoreRoomCheck());
    	response.setIgnoreTooFar(location.isIgnoreTooFar());
    	
    	for (RoomFeature f: location.getFeatures()) {
    		FeatureInterface feature = new FeatureInterface(f.getUniqueId(), f.getAbbv(), f.getLabel());
    		if (f.getFeatureType() != null)
    			feature.setType(new FeatureTypeInterface(f.getFeatureType().getUniqueId(), f.getFeatureType().getReference(), f.getFeatureType().getLabel(), f.getFeatureType().isShowInEventManagement()));
    		if (f instanceof DepartmentRoomFeature) {
    			Department d = ((DepartmentRoomFeature)f).getDepartment();
    			if (filterDepartments && !context.getUser().getCurrentAuthority().hasQualifier(d)) continue; 
    			feature.setDepartment(wrap(d, location, null));
    			feature.setTitle(f.getLabel() + " (" + d.getName() + (f.getFeatureType() == null ? "" : ", " + f.getFeatureType().getLabel()) + ")");
    		} else {
    			feature.setTitle(f.getLabel() + (f.getFeatureType() == null ? "" : " (" + f.getFeatureType().getLabel() + ")"));
    		}
    		response.addFeature(feature);
    	}
    	for (RoomGroup g: location.getRoomGroups()) {
    		GroupInterface group = new GroupInterface(g.getUniqueId(), g.getAbbv(), g.getName());
    		if (g.getDepartment() != null) {
    			if (filterDepartments && !context.getUser().getCurrentAuthority().hasQualifier(g.getDepartment())) continue;
    			group.setDepartment(wrap(g.getDepartment(), location, null));
    			group.setTitle((g.getDescription() == null || g.getDescription().isEmpty() ? g.getName() : g.getDescription()) + " (" + g.getDepartment().getName() + ")");
    		}
    		response.addGroup(group);
    	}
    	for (RoomDept rd: location.getRoomDepts()) {
    		DepartmentInterface d = wrap(rd.getDepartment(), location, location.getRoomPreferenceLevel(rd.getDepartment()));
    		response.addDepartment(d);
    		if (rd.isControl())
    			response.setControlDepartment(d);
    	}
    	if (gridAsText && response.isCanSeeAvailability()) {
    		if (html)
    			response.setAvailability(location.getRoomSharingTable().getModel().toString().replaceAll(", ","<br>"));
    		else
    			response.setAvailability(location.getRoomSharingTable().getModel().toString().replaceAll(", ","\n"));
    	}
    	if (gridAsText && response.isCanSeeEventAvailability()) {
    		if (html)
    			response.setEventAvailability(location.getEventAvailabilityTable().getModel().toString().replaceAll(", ", "<br>"));
    		else
    			response.setEventAvailability(location.getEventAvailabilityTable().getModel().toString().replaceAll(", ", "\n"));
    	}
    	
    	for (ExamType xt: location.getExamTypes()) {
    		if (types != null && !types.contains(xt)) continue;
    		if (xt.getReference().equals(department) && response.isCanSeePeriodPreferences()) {
    			if (xt.getType() == ExamType.sExamTypeMidterm || gridAsText) {
    				if (html)
    					response.setPeriodPreference(location.getExamPreferencesAbbreviationHtml(xt));
    				else
    					response.setPeriodPreference(location.getExamPreferencesAbbreviation(xt));
    			}
    		}
    		response.addExamRype(new ExamTypeInterface(xt.getUniqueId(), xt.getReference(), xt.getLabel(), xt.getType() == ExamType.sExamTypeFinal));
    	}
    	
    	if (location.getEventDepartment() != null)
    		response.setEventDepartment(wrap(location.getEventDepartment(), location, null));
    	
    	response.setRoomSharingNote(location.getShareNote());

    	response.setEventNote(location.getEventMessage());
    	response.setBreakTime(location.getBreakTime());
    	if (location.getEventDepartment() != null) {
        	response.setEventStatus(location.getEventStatus());
        	RoomTypeOption rto = location.getRoomType().getOption(location.getEventDepartment());
        	response.setDefaultEventStatus(rto.getStatus());
        	response.setDefaultBreakTime(rto.getBreakTime());
        	response.setDefaultEventNote(rto.getMessage());
    	} else {
    		response.setDefaultEventStatus(RoomTypeOption.Status.NoEventManagement.ordinal());
    	}
    	
		String minimap = ApplicationProperty.RoomHintMinimapUrl.value();
    	if (minimap != null && location.getCoordinateX() != null && location.getCoordinateY() != null)
    		response.setMiniMapUrl(minimap
    				.replace("%x", location.getCoordinateX().toString())
    				.replace("%y", location.getCoordinateY().toString())
    				.replace("%n", location.getLabel())
    				.replace("%i", location.getExternalUniqueId() == null ? "" : location.getExternalUniqueId()));
    	
    	for (LocationPicture picture: new TreeSet<LocationPicture>(location.getPictures()))
    		response.addPicture(new RoomPictureInterface(picture.getUniqueId(), picture.getFileName(), picture.getContentType()));
    	
    	return response;
	}
}
