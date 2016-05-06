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
package org.unitime.timetable.security.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class InstructorSchedulingPermissions {
	
	@PermissionForRight(Right.InstructorScheduling)
	public static class InstructorScheduling implements Permission<SolverGroup> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, SolverGroup source) {
			for (Department department: source.getDepartments()) {
				if (!permissionDepartment.check(user, department, DepartmentStatusType.Status.InstructorScheduling, DepartmentStatusType.Status.OwnerLimitedEdit))
					return false;
			}
			for (Department department: source.getDepartments()) {
				for (DepartmentalInstructor di: department.getInstructors())
					if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog()))
						return true;
			}
			return false;
		}

		@Override
		public Class<SolverGroup> type() { return SolverGroup.class; }
	}
	
	@PermissionForRight(Right.InstructorSchedulingSolver)
	public static class InstructorSchedulingSolver extends InstructorScheduling {}
	
	@PermissionForRight(Right.InstructorSchedulingSolverLog)
	public static class InstructorSchedulingSolverLog extends InstructorSchedulingSolver {}

	@PermissionForRight(Right.InstructorSchedulingSolutionExportXml)
	public static class InstructorSchedulingSolutionExportXml extends InstructorSchedulingSolver {}
}
