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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("/api/instructors")
public class InstructorsConnector extends ApiConnector {
	
	@Override
	public void doGet(ApiHelper helper) throws IOException {
		Long departmentId = helper.getOptinalParameterLong("id", null);
		List<DepartmentInfo> response = new ArrayList<DepartmentInfo>();
		if (departmentId != null) {
			Department department = DepartmentDAO.getInstance().get(departmentId, helper.getHibSession());
			if (department == null)
				throw new IllegalArgumentException("Department " + departmentId + " does not exist.");
			helper.getSessionContext().checkPermissionAnyAuthority(department.getSession(), Right.ApiRetrieveInstructors);
			response.add(new DepartmentInfo(department));
		} else {
			Long sessionId = helper.getAcademicSessionId();
			if (sessionId == null)
				throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
			String deptCode = helper.getOptinalParameter("code", null);
			if (deptCode != null) {
				Department department = Department.findByDeptCode(deptCode, sessionId);
				if (department == null)
					throw new IllegalArgumentException("Department " + deptCode + " does not exist.");
				helper.getSessionContext().checkPermissionAnyAuthority(department.getSession(), Right.ApiRetrieveInstructors);
				response.add(new DepartmentInfo(department));
			} else {
				helper.getSessionContext().checkPermissionAnyAuthority(sessionId, "Session", Right.ApiRetrieveInstructors);
				for (Department department: Department.findAllBeingUsed(sessionId))
					response.add(new DepartmentInfo(department));
			}
		}

		helper.setResponse(response);
	}
	
	class DepartmentInfo {
		Long iDepartmentId;
		String iExternalId;
		String iDeptCode;
		String iAbbreviation;
		String iName;
		Boolean iExternallyManaged;
		String iExternalName, iExternalAbbreviation;
		List<InstructorInfo> iInstructors = new ArrayList<InstructorInfo>();
		
		DepartmentInfo(Department d) {
			iDepartmentId = d.getUniqueId();
			iExternalId = d.getExternalUniqueId();
			iDeptCode = d.getDeptCode();
			iAbbreviation = d.getAbbreviation();
			iName = d.getName();
			iExternallyManaged = d.isExternalManager();
			if (d.isExternalManager()) {
				iExternalName = d.getExternalMgrLabel();
				iExternalAbbreviation = d.getExternalMgrAbbv();
			}
			for (DepartmentalInstructor i: d.getInstructors())
				iInstructors.add(new InstructorInfo(i));
		}
	}
	
	static class InstructorInfo {
		Long iInstructorId;
		String iExternalId;
		String iFirstName;
		String iMiddleName;
		String iLastName;
		String iTitle;
		PositionInfo iPosition;
		String iEmail;
		String iAcademicTitle;
		
		InstructorInfo(DepartmentalInstructor instructor) {
			iInstructorId = instructor.getUniqueId();
			iExternalId = instructor.getExternalUniqueId();
			iFirstName = instructor.getFirstName();
			iMiddleName = instructor.getMiddleName();
			iLastName = instructor.getLastName();
			iTitle = instructor.getAcademicTitle();
			if (instructor.getPositionType() != null)
				iPosition = new PositionInfo(instructor.getPositionType());
			iEmail = instructor.getEmail();
			iAcademicTitle = instructor.getAcademicTitle();
		}
	}
	
	static class PositionInfo {
		String iReference;
		String iLabel;
		
		PositionInfo(PositionType type) {
			iReference = type.getReference();
			iLabel = type.getLabel();
		}
	}

	@Override
	protected String getName() {
		return "instructors";
	}

}
