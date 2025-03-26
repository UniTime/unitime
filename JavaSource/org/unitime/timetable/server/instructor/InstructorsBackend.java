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

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorsRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorsResponse;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(InstructorsRequest.class)
public class InstructorsBackend implements GwtRpcImplementation<InstructorsRequest, InstructorsResponse>{
	protected static CourseMessages MESSAGES = Localization.create(CourseMessages.class);

	@Override
	public InstructorsResponse execute(InstructorsRequest request, SessionContext context) {
		String deptId = request.getFilter().getParameterValue("deptId");
		if (deptId == null || deptId.isEmpty())
			throw new GwtRpcException(MESSAGES.errorRequiredDepartment());
		Department department = DepartmentDAO.getInstance().get(Long.valueOf(deptId));
		context.checkPermission(department, Right.Instructors);
		
		context.setAttribute(SessionAttribute.DepartmentId, deptId);
		context.getUser().setProperty("Instructoros.positions", request.getFilter().getParameterValue("positions"));
		
		InstructorsResponse response = new InstructorsResponse();
		response.setDepartmentId(Long.valueOf(deptId));
		InstructorsTableBuilder builder = new InstructorsTableBuilder(context, request.getBackType(), request.getBackId());
		response.setTable(builder.generateTableForDepartment(department, request.getFilter(), context));
		
		if (response.getTable() != null && response.getTable().hasLines() && context.hasPermission(department, Right.InstructorsExportPdf))
			response.addOperation("export");
		
		if (context.hasPermission(department, Right.InstructorSurveyAdmin) && InstructorSurvey.hasInstructorSurveys(department.getUniqueId()))
			response.addOperation("export-surveys");
		
		if (context.hasPermission(department, Right.ManageInstructors))
			response.addOperation("manage-instructors");
			
		if (context.hasPermission(department, Right.InstructorAdd))
			response.addOperation("add-instructor");
		
		BackTracker.markForBack(
				context,
				"instructors?deptId="+department.getUniqueId(),
				MESSAGES.backInstructors(department.getDeptCode() + " - " + department.getName()),
				true, true
				);
		
		return response;
	}
}
