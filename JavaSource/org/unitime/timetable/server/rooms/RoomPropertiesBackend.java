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
import java.util.TreeSet;

import org.cpsolver.ifs.util.DistanceMetric;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventServiceProviderInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.AcademicSessionInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.BuildingInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.GroupInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomTypeInterface;
import org.unitime.timetable.model.AttachmentType;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(RoomPropertiesRequest.class)
public class RoomPropertiesBackend implements GwtRpcImplementation<RoomPropertiesRequest, RoomPropertiesInterface> {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public RoomPropertiesInterface execute(RoomPropertiesRequest request, SessionContext context) {
		if (request.hasSessionId())
			context = new EventContext(context, request.getSessionId());
		
		context.checkPermission(Right.Rooms);
		
		RoomPropertiesInterface response = new RoomPropertiesInterface();
		UserAuthority authority = null;
		
		if (context.getUser() != null) {
			Session session = SessionDAO.getInstance().get(request.hasSessionId() ? request.getSessionId() : context.getUser().getCurrentAcademicSessionId());
			response.setAcademicSession(new AcademicSessionInterface(session.getUniqueId(), session.getAcademicTerm() + " " + session.getAcademicYear()));
			authority = context.getUser().getCurrentAuthority();
		}
		
		response.setCanEditDepartments(context.hasPermission(Right.EditRoomDepartments));
		response.setCanExportCsv(context.hasPermission(Right.RoomsExportCsv));
		response.setCanExportPdf(context.hasPermission(Right.RoomsExportPdf));
		response.setCanEditRoomExams(context.hasPermission(Right.EditRoomDepartmentsExams));
		response.setCanAddRoom(context.hasPermission(Right.AddRoom));
		response.setCanAddNonUniversity(context.hasPermission(Right.AddNonUnivLocation));
		
		response.setCanSeeCourses(context.hasPermission(Right.InstructionalOfferings) || context.hasPermission(Right.Classes) ||
				(authority != null && (authority.hasRight(Right.RoomEditChangeRoomProperties) || authority.hasRight(Right.RoomEditChangeControll) || authority.hasRight(Right.RoomDetailAvailability) || authority.hasRight(Right.RoomEditAvailability))));
		response.setCanSeeExams(context.hasPermission(Right.Examinations) ||
				(authority != null && (authority.hasRight(Right.RoomEditChangeExaminationStatus) || authority.hasRight(Right.RoomDetailPeriodPreferences))));
		response.setCanSeeEvents(context.hasPermission(Right.Events) ||
				(authority != null && (authority.hasRight(Right.RoomEditChangeEventProperties) || authority.hasRight(Right.RoomDetailEventAvailability ) || authority.hasRight(Right.RoomEditEventAvailability))));
		response.setCanExportRoomGroups(context.hasPermission(Right.RoomGroupsExportPdf));
		response.setCanExportRoomFeatures(context.hasPermission(Right.RoomFeaturesExportPdf));
		response.setCanAddGlobalRoomGroup(context.hasPermission(Right.GlobalRoomGroupAdd));
		response.setCanAddDepartmentalRoomGroup(context.hasPermission(Right.DepartmentRoomGroupAdd));
		response.setCanAddGlobalRoomFeature(context.hasPermission(Right.GlobalRoomFeatureAdd));
		response.setCanAddDepartmentalRoomFeature(context.hasPermission(Right.DepartmentRoomFeatureAdd));
		
		if (context.getUser() != null) {
			response.setCanChangeAvailability(context.getUser().getCurrentAuthority().hasRight(Right.RoomEditAvailability));
			response.setCanChangeControll(context.getUser().getCurrentAuthority().hasRight(Right.RoomEditChangeControll));
			response.setCanChangeEventAvailability(context.getUser().getCurrentAuthority().hasRight(Right.RoomEditEventAvailability));
			response.setCanChangeEventProperties(context.getUser().getCurrentAuthority().hasRight(Right.RoomEditChangeEventProperties));
			response.setCanChangeExamStatus(context.getUser().getCurrentAuthority().hasRight(Right.RoomEditChangeExaminationStatus));
			response.setCanChangeExternalId(context.getUser().getCurrentAuthority().hasRight(Right.RoomEditChangeExternalId));
			response.setCanChangeFeatures(context.getUser().getCurrentAuthority().hasRight(Right.RoomEditFeatures) || context.getUser().getCurrentAuthority().hasRight(Right.RoomEditGlobalFeatures));
			response.setCanChangeGroups(context.getUser().getCurrentAuthority().hasRight(Right.RoomEditGroups) || context.getUser().getCurrentAuthority().hasRight(Right.RoomEditGlobalGroups));
			response.setCanChangePicture(context.getUser().getCurrentAuthority().hasRight(Right.RoomEditChangePicture));
			response.setCanChangePreferences(context.getUser().getCurrentAuthority().hasRight(Right.RoomEditPreference));
			response.setCanChangeDefaultGroup(context.getUser().getCurrentAuthority().hasRight(Right.GlobalRoomGroupEditSetDefault));
			if (!response.isCanSeeEvents() && response.isCanChangeEventProperties())
				response.setCanSeeEvents(true);
		}
		
		for (RoomType type: RoomType.findAll())
			response.addRoomType(new RoomTypeInterface(type.getUniqueId(), type.getReference(), type.getLabel(), type.isRoom(), type.getOrd()));
		
		for (Building b: Building.findAll(response.getAcademicSessionId())) {
			BuildingInterface building = new BuildingInterface(b.getUniqueId(), b.getAbbreviation(), b.getName());
			building.setX(b.getCoordinateX()); building.setY(b.getCoordinateY());
			building.setExternalId(b.getExternalUniqueId());
			response.addBuilding(building);
		}
		
		for (RoomFeatureType type: new TreeSet<RoomFeatureType>(RoomFeatureTypeDAO.getInstance().findAll()))
			response.addFeatureType(new FeatureTypeInterface(type.getUniqueId(), type.getReference(), type.getLabel(), type.isShowInEventManagement()));
		
		if (context.getUser() != null) {
			for (ExamType type: ExamType.findAllApplicable(context.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable))
				response.addExamType(new ExamTypeInterface(type.getUniqueId(), type.getReference(), type.getLabel(), type.getType() == ExamType.sExamTypeFinal));
		}
		
		for (Department d: Department.getUserDepartments(context.getUser())) {
			DepartmentInterface department = new DepartmentInterface();
			department.setId(d.getUniqueId());
			department.setDeptCode(d.getDeptCode());
			department.setAbbreviation(d.getAbbreviation());
			department.setLabel(d.getName());
			department.setExternal(d.isExternalManager());
			department.setEvent(d.isAllowEvents());
			department.setExtAbbreviation(d.getExternalMgrAbbv());
			department.setExtLabel(d.getExternalMgrLabel());
			department.setTitle(d.getLabel());
			department.setCanEditRoomSharing(context.hasPermission(d, Right.EditRoomDepartments));
			response.addDepartment(department);
		}
		response.setNrDepartments(Department.findAllBeingUsed(response.getAcademicSessionId()).size());
		
		response.setHorizontal(context.getUser() == null ? false : CommonValues.HorizontalGrid.eq(context.getUser().getProperty(UserProperty.GridOrientation)));
		response.setGridAsText(context.getUser() == null ? false : CommonValues.TextGrid.eq(context.getUser().getProperty(UserProperty.GridOrientation)));
		
		for (int i = 0; true; i++) {
			String mode = ApplicationProperty.RoomSharingMode.value(String.valueOf(1 + i), i < CONSTANTS.roomSharingModes().length ? CONSTANTS.roomSharingModes()[i] : null);
			if (mode == null || mode.isEmpty()) break;
			response.addMode(new RoomInterface.RoomSharingDisplayMode(mode));
		}
		
		boolean filterDepartments = context.getUser() != null && !context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
		boolean includeGlobalGroups = context.getUser() != null && context.getUser().getCurrentAuthority().hasRight(Right.RoomEditGlobalGroups);
		boolean includeDeptGroups = context.getUser() != null && context.getUser().getCurrentAuthority().hasRight(Right.RoomEditGroups);

		for (RoomGroup g: RoomGroup.getAllRoomGroupsForSession(context.getUser().getCurrentAcademicSessionId())) {
			GroupInterface group = new GroupInterface(g.getUniqueId(), g.getAbbv(), g.getName());
			if (g.getDepartment() != null) {
				if (!includeDeptGroups) continue;
				if (filterDepartments && !context.getUser().getCurrentAuthority().hasQualifier(g.getDepartment())) continue;
				group.setDepartment(response.getDepartment(g.getDepartment().getUniqueId()));
				group.setTitle((g.getDescription() == null || g.getDescription().isEmpty() ? g.getName() : g.getDescription()) + " (" + g.getDepartment().getName() + ")");
			} else {
				if (!includeGlobalGroups) continue;
				group.setTitle((g.getDescription() == null || g.getDescription().isEmpty() ? g.getName() : g.getDescription()));
			}
			response.addGroup(group);
		}
		
		if (context.getUser() != null && context.getUser().getCurrentAuthority().hasRight(Right.RoomEditGlobalFeatures)) {
			for (GlobalRoomFeature f: RoomFeature.getAllGlobalRoomFeatures(context.getUser().getCurrentAcademicSessionId())) {
				FeatureInterface feature = new FeatureInterface(f.getUniqueId(), f.getAbbv(), f.getLabel());
				if (f.getFeatureType() != null)
					feature.setType(response.getFeatureType(f.getFeatureType().getUniqueId()));
				feature.setTitle((f.getDescription() == null || f.getDescription().isEmpty() ? f.getLabel() : f.getDescription()));
				response.addFeature(feature);
			}
		}
		
		if (context.getUser() != null && context.getUser().getCurrentAuthority().hasRight(Right.RoomEditFeatures)) {
			for (DepartmentRoomFeature f: RoomFeature.getAllDepartmentRoomFeaturesInSession(context.getUser().getCurrentAcademicSessionId())) {
				if (filterDepartments && !context.getUser().getCurrentAuthority().hasQualifier(f.getDepartment())) continue;
				FeatureInterface feature = new FeatureInterface(f.getUniqueId(), f.getAbbv(), f.getLabel());
				if (f.getFeatureType() != null)
	    			feature.setType(new FeatureTypeInterface(f.getFeatureType().getUniqueId(), f.getFeatureType().getReference(), f.getFeatureType().getLabel(), f.getFeatureType().isShowInEventManagement()));
				feature.setDepartment(response.getDepartment(f.getDepartment().getUniqueId()));
				feature.setTitle((f.getDescription() == null || f.getDescription().isEmpty() ? f.getLabel() : f.getDescription()) + " (" + f.getDepartment().getName() + ")");
				feature.setDescription(f.getDescription());
				response.addFeature(feature);
			}
		}
		
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false)) {
			response.addPreference(new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2bgColor(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), pref.getAbbreviation(), true));
		}

		for (AttachmentType type: AttachmentType.listTypes(AttachmentType.VisibilityFlag.ROOM_PICTURE_TYPE)) {
			response.addPictureType(RoomPicturesBackend.getPictureType(type));
		}
		
		if (request.hasSessionId()) {
			for (EventServiceProvider p: EventServiceProvider.findAll(request.getSessionId())) {
				if (p.isAllRooms()) continue;
				EventServiceProviderInterface provider = new EventServiceProviderInterface();
				provider.setId(p.getUniqueId());
				provider.setReference(p.getReference());
				provider.setLabel(p.getLabel());
				provider.setMessage(p.getNote());
				provider.setEmail(p.getEmail());
				if (p.getDepartment() != null)
					provider.setDepartmentId(p.getDepartment().getUniqueId());
				response.addEventServiceProvider(provider);
			}
		}

		DistanceMetric.Ellipsoid ellipsoid = DistanceMetric.Ellipsoid.valueOf(ApplicationProperty.DistanceEllipsoid.value());
		response.setEllipsoid(ellipsoid.getEclipsoindName());
		
		response.setGoogleMap(ApplicationProperty.RoomUseGoogleMap.isTrue());
		response.setGoogleMapApiKey(ApplicationProperty.GoogleMapsApiKey.value());
		
		response.setLeafletMap(!response.isGoogleMap() && ApplicationProperty.RoomUseLeafletMap.isTrue());
		response.setLeafletMapTiles(ApplicationProperty.RoomUseLeafletMapTiles.value());
		response.setLeafletMapAttribution(ApplicationProperty.RoomUseLeafletMapAttribution.value());
		
		if (response.getAcademicSession() != null) {
			for (Session session: (List<Session>)SessionDAO.getInstance().getSession().createQuery(
					"select f from Session f, Session s where " +
					"s.uniqueId = :sessionId and s.sessionBeginDateTime < f.sessionBeginDateTime and s.academicInitiative = f.academicInitiative " +
					"order by f.sessionBeginDateTime")
					.setLong("sessionId", response.getAcademicSessionId()).list()) {
				AcademicSessionInterface s = new AcademicSessionInterface(session.getUniqueId(), session.getAcademicTerm() + " " + session.getAcademicYear());
				EventContext cx = new EventContext(context, context.getUser(), session.getUniqueId());
				s.setCanAddRoom(cx.hasPermission(Right.AddRoom));
				s.setCanAddNonUniversity(cx.hasPermission(Right.AddNonUnivLocation));
				s.setCanAddGlobalRoomGroup(cx.hasPermission(Right.GlobalRoomGroupAdd));
				s.setCanAddDepartmentalRoomGroup(cx.hasPermission(Right.DepartmentRoomGroupAdd));
				s.setCanAddGlobalRoomFeature(cx.hasPermission(Right.GlobalRoomFeatureAdd));
				s.setCanAddDepartmentalRoomFeature(cx.hasPermission(Right.DepartmentRoomFeatureAdd));
				if (s.isCanAddRoom() || s.isCanAddNonUniversity())
					response.addFutureSession(s);
			}
		}
		
		response.setRoomAreaInMetricUnits(ApplicationProperty.RoomAreaUnitsMetric.isTrue());
		
    	response.setCanSaveFilterDefaults(context.hasPermission(Right.HasRole));
    	if (context.isAuthenticated() && response.isCanSaveFilterDefaults() && request.hasMode()) {
			response.setFilterDefault("filter", context.getUser().getProperty("Default[" + request.getMode() + ".filter]"));
		} else if (request.hasMode()) {
			response.setFilterDefault("filter", "EVENTS".equalsIgnoreCase(request.getMode()) ? "flag:Event" : "");
		}
		
		return response;
	}

}
