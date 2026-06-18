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
package org.unitime.timetable.server.instructor;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.InstructorInterface.ManageInstructorListRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.ManageInstructorListResponse;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(ManageInstructorListRequest.class)
public class ManageInstructorListBackend implements GwtRpcImplementation<ManageInstructorListRequest, ManageInstructorListResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public ManageInstructorListResponse execute(ManageInstructorListRequest request, SessionContext context) {
		context.checkPermission(request.getDepartmentId(), Right.ManageInstructors);
		
		ManageInstructorListResponse response = new ManageInstructorListResponse();
		String nameFormat = context.getUser().getProperty(UserProperty.NameFormat);
		
		Set<PositionType> positions = new TreeSet<PositionType>();
		boolean hasNoPosition = false;
		
		Department dept = DepartmentDAO.getInstance().get(request.getDepartmentId());
		response.setDepartmentId(request.getDepartmentId());
		response.setDepartmentName(dept.getLabel());
		Set<String> externalIds = new HashSet<String>();
		for (DepartmentalInstructor instructor: DepartmentalInstructor.findInstructorsForDepartment(dept.getUniqueId())) {
			response.addAssigned(instructor.getUniqueId(),
					instructor.getExternalUniqueId(),
					instructor.getName(nameFormat),
					instructor.getPositionType() == null ? -1l : instructor.getPositionType().getUniqueId(),
					context.hasPermission(instructor, Right.InstructorDelete));
			if (instructor.getPositionType() == null)
				hasNoPosition = true;
			else
				positions.add(instructor.getPositionType());
			if (instructor.getExternalUniqueId() != null)
				externalIds.add(instructor.getExternalUniqueId());
		}
		
		for (Staff staff: Staff.getStaffByDept(dept.getDeptCode(), dept.getSessionId())) {
			if (staff.getExternalUniqueId() == null || !externalIds.add(staff.getExternalUniqueId())) continue;
			response.addAvailable(
					staff.getExternalUniqueId(),
					staff.getName(nameFormat),
					staff.getPositionType() == null ? -1l : staff.getPositionType().getUniqueId());
			if (staff.getPositionType() == null)
				hasNoPosition = true;
			else
				positions.add(staff.getPositionType());
		}
		
		for (PositionType pos: positions)
			response.addPosition(pos.getUniqueId(), pos.getLabel());
		if (hasNoPosition)
			response.addPosition(-1l, MSG.positionNotSet());
		
		return response;
	}

}
