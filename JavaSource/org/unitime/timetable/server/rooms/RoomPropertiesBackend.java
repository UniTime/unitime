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

import java.util.TreeSet;

import org.cpsolver.ifs.util.DistanceMetric;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.ExamTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureTypeInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomPropertiesRequest;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomTypeInterface;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(RoomPropertiesRequest.class)
public class RoomPropertiesBackend implements GwtRpcImplementation<RoomPropertiesRequest, RoomPropertiesInterface> {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public RoomPropertiesInterface execute(RoomPropertiesRequest request, SessionContext context) {
		context.checkPermission(Right.Rooms);
		
		RoomPropertiesInterface response = new RoomPropertiesInterface();
		
		response.setAcademicSessionId(context.getUser() == null ? null : context.getUser().getCurrentAcademicSessionId());
		
		response.setCanEditDepartments(context.hasPermission(Right.EditRoomDepartments));
		response.setCanExportCsv(context.hasPermission(Right.RoomsExportCsv));
		response.setCanExportPdf(context.hasPermission(Right.RoomsExportPdf));
		response.setCanEditRoomExams(context.hasPermission(Right.EditRoomDepartmentsExams));
		response.setCanAddRoom(context.hasPermission(Right.AddRoom) || context.hasPermission(Right.AddNonUnivLocation));
		
		response.setCanSeeCourses(context.hasPermission(Right.InstructionalOfferings) || context.hasPermission(Right.Classes));
		response.setCanSeeExams(context.hasPermission(Right.Examinations));
		response.setCanSeeEvents(context.hasPermission(Right.Events));
		
		for (RoomType type: RoomType.findAll())
			response.addRoomType(new RoomTypeInterface(type.getUniqueId(), type.getLabel(), type.isRoom()));
		
		for (RoomFeatureType type: new TreeSet<RoomFeatureType>(RoomFeatureTypeDAO.getInstance().findAll()))
			response.addFeatureType(new FeatureTypeInterface(type.getUniqueId(), type.getReference(), type.getLabel(), type.isShowInEventManagement()));
		
		for (ExamType type: ExamType.findAll())
			response.addExamType(new ExamTypeInterface(type.getUniqueId(), type.getReference(), type.getLabel(), type.getType() == ExamType.sExamTypeFinal));
		
		for (Department d: Department.getUserDepartments(context.getUser())) {
			DepartmentInterface department = new DepartmentInterface();
			department.setId(d.getUniqueId());
			department.setDeptCode(d.getDeptCode());
			department.setAbbreviation(d.getAbbreviation());
			department.setLabel(d.getName());
			department.setExternal(d.isExternalManager());
			department.setExtAbbreviation(d.getExternalMgrAbbv());
			department.setExtLabel(d.getExternalMgrLabel());
			department.setTitle(d.getLabel());
			response.addDepartment(department);
		}
		
		response.setHorizontal(context.getUser() == null ? false : CommonValues.HorizontalGrid.eq(context.getUser().getProperty(UserProperty.GridOrientation)));
		response.setGridAsText(context.getUser() == null ? false : CommonValues.TextGrid.eq(context.getUser().getProperty(UserProperty.GridOrientation)));
		
		for (int i = 0; true; i++) {
			String mode = ApplicationProperty.RoomSharingMode.value(String.valueOf(1 + i), i < CONSTANTS.roomSharingModes().length ? CONSTANTS.roomSharingModes()[i] : null);
			if (mode == null || mode.isEmpty()) break;
			response.addMode(new RoomInterface.RoomSharingDisplayMode(mode));
		}
		
		DistanceMetric.Ellipsoid ellipsoid = DistanceMetric.Ellipsoid.valueOf(ApplicationProperty.DistanceEllipsoid.value());
		response.setEllipsoid(ellipsoid.getEclipsoindName());
		
		return response;
	}

}
