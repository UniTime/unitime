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
package org.unitime.timetable.server.departments;


import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentOption;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentPropertiesInterface;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentPropertiesRequest;
import org.unitime.timetable.gwt.shared.DepartmentInterface.StatusOption;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(DepartmentPropertiesRequest.class)
public class DepartmentPropertiesBackend implements GwtRpcImplementation<DepartmentPropertiesRequest, DepartmentPropertiesInterface>{

	public List<StatusOption> getStatusOptions() {
		List<StatusOption> ret = new ArrayList<StatusOption>();
		for (DepartmentStatusType status: DepartmentStatusType.findAllForDepartment())
			ret.add(new StatusOption(status.getUniqueId(), status.getReference(), status.getLabel()));
		return ret;
	}
	
	public List<DepartmentOption> getAllDependentDepartments(SessionContext context) {
		List<DepartmentOption> ret = new ArrayList<DepartmentOption>();
		for (Department d: Department.findAllNonExternal(context.getUser().getCurrentAcademicSessionId()))
			ret.add(new DepartmentOption(d.getUniqueId(), d.getLabel()));
		return ret;
	}

	@Override
	public DepartmentPropertiesInterface execute(DepartmentPropertiesRequest request, SessionContext context) {
		DepartmentPropertiesInterface d = new DepartmentPropertiesInterface();	
		d.setStatusOptions(getStatusOptions());
		d.setExtDepartmentOptions(getAllDependentDepartments(context));
		d.setAcademicSessionName(context.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel());
		d.setCoursesFundingDepartmentsEnabled(ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue());
		Department dept = null;
		if (request.getDepartmentId() != null) {
			dept = DepartmentDAO.getInstance().get(request.getDepartmentId());
		}
				
		if (dept != null) {
			d.setCanDelete(context.hasPermission(dept, Right.DepartmentDelete));
			d.setCanChangeExtManager(context.hasPermission(dept, Right.DepartmentEditChangeExternalManager));
			d.setCanEdit(context.hasPermission(dept, Right.DepartmentEdit));
		} else {
			d.setCanDelete(false);
			d.setCanChangeExtManager(true);
			d.setCanEdit(true);			
		}
		
		return d;
	}
}

