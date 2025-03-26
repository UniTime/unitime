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

import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorsFilterRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorsFilterResponse;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.dao.PositionTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(InstructorsFilterRequest.class)
public class InstructorsFilterBackend implements GwtRpcImplementation<InstructorsFilterRequest, InstructorsFilterResponse> {
	protected static CourseMessages MESSAGES = Localization.create(CourseMessages.class);

	@Override
	public InstructorsFilterResponse execute(InstructorsFilterRequest request, SessionContext context) {
		context.checkPermission(Right.Instructors);
		InstructorsFilterResponse filter = new InstructorsFilterResponse();
		
		Set<Department> departments = Department.getUserDepartments(context.getUser());
		
		if (departments.isEmpty())
			throw new GwtRpcException(MESSAGES.exceptionNoDepartmentToManage());
		String defaultDeptId = (String)context.getAttribute(SessionAttribute.DepartmentId);
		if (departments.size() == 1) {
			defaultDeptId = departments.iterator().next().getUniqueId().toString();
			context.setAttribute(SessionAttribute.DepartmentId, defaultDeptId);
		}
		
		FilterParameterInterface positions = new FilterParameterInterface();
		positions.setName("positions");
		positions.setLabel(MESSAGES.propertyInstructorPosition());
		positions.setType("list");
		positions.setMultiSelect(true);
		positions.addOption("-1", MESSAGES.instructorPositionNotSpecified());
		for (PositionType pos: new TreeSet<PositionType>(PositionTypeDAO.getInstance().findAll())) {
			positions.addOption(pos.getUniqueId().toString(), pos.getLabel());
		}
		positions.setMaxLinesToShow(Math.min(positions.getOptions().size(), 5));
		positions.setDefaultValue(context.getUser().getProperty("Instructoros.positions"));
		filter.addParameter(positions);

		FilterParameterInterface depts = new FilterParameterInterface();
		depts.setName("deptId");
		depts.setLabel(MESSAGES.filterDepartment());
		depts.setType("list");
		depts.setCollapsible(false);
		if (defaultDeptId != null)
			depts.setDefaultValue(defaultDeptId);
		for (Department d: departments)
			depts.addOption(d.getUniqueId().toString(), d.getDeptCode() + " - " + d.getName());
		filter.addParameter(depts);
		
		filter.setSticky(CommonValues.Yes.eq(UserProperty.StickyTables.get(context.getUser())));
		filter.setSessionId(context.getUser().getCurrentAcademicSessionId());
		
		return filter;
	}

}
