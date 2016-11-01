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

import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentStatusType.Status;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.permissions.Permission.PermissionDepartment;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("permissionDepartment")
public class SimpleDepartmentPermission implements PermissionDepartment {

	@Override
	public boolean check(UserContext user, Department department) {
		return check(user, department, new DepartmentStatusType.Status[] {}) && checkStatus(department.effectiveStatusType());
	}
	
	@Override
	public boolean check(UserContext user, Department department, DepartmentStatusType.Status... status) {
		// Not authenticated or no authority -> no permission
		if (user == null || user.getCurrentAuthority() == null || department == null) return false;
		
		UserAuthority authority = user.getCurrentAuthority();
		
		// Academic session check
		if (!authority.hasRight(Right.SessionIndependent) && !authority.hasQualifier(department.getSession()))
			return false;
		
		// Department check
		if (!authority.hasRight(Right.DepartmentIndependent) && !authority.hasQualifier(department))
			return false;

		// Check department status
		if (status.length > 0 && !authority.hasRight(Right.StatusIndependent)) {
			DepartmentStatusType type = department.effectiveStatusType();
			if (type == null) return false;
			for (DepartmentStatusType.Status s: status) {
				if (type.can(s)) return true;
			}
			return false;
		}
		
		return true;
	}

	@Override
	public Class<Department> type() {
		return Department.class;
	}
	
	public boolean checkStatus(DepartmentStatusType status) { return true; }

	@Override
	public boolean check(UserContext user, Department controllingDepartment, Status ownerStatus, Department managingDepartment, Status managerStatus) {
		// Not authenticated or no authority -> no permission
		if (user == null || user.getCurrentAuthority() == null || controllingDepartment == null) return false;
		if (managingDepartment == null) managingDepartment = controllingDepartment;
		
		UserAuthority authority = user.getCurrentAuthority();
		
		// Academic session check
		if (!authority.hasRight(Right.SessionIndependent) && !authority.hasQualifier(controllingDepartment.getSession()))
			return false;
		
		// Department check
		if (!authority.hasRight(Right.DepartmentIndependent) && !authority.hasQualifier(controllingDepartment) && !authority.hasQualifier(managingDepartment))
			return false;

		// Check department status
		if ((ownerStatus != null || managerStatus != null) && !authority.hasRight(Right.StatusIndependent)) {
			DepartmentStatusType type = managingDepartment.effectiveStatusType(controllingDepartment);
			if (ownerStatus != null && authority.hasQualifier(controllingDepartment) && type.can(ownerStatus))
				return true;
			if (managerStatus != null && authority.hasQualifier(managingDepartment) && type.can(managerStatus))
				return true;
			return false;
		}
		
		return true;
	}
}
