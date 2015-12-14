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
import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.permissions.Permission.PermissionExamination;
import org.unitime.timetable.security.rights.Right;

@Service("permissionExaminationStatus")
public class SimpleExaminationPermission implements PermissionExamination {

	@Override
	public boolean check(UserContext user, Session session) {
		return check(user, session, null, new DepartmentStatusType.Status[] {});
	}
	
	@Override
	public boolean check(UserContext user, Session session, ExamType examType, DepartmentStatusType.Status... status) {
		// Not authenticated or no authority -> no permission
		if (user == null || user.getCurrentAuthority() == null || session == null) return false;
		
		UserAuthority authority = user.getCurrentAuthority();
		
		// Academic session check
		if (!authority.hasRight(Right.SessionIndependent) && !authority.hasQualifier(session))
			return false;
		
		// Test session check
		if (!authority.hasRight(Right.AllowTestSessions) && (session.getStatusType() == null || session.getStatusType().isTestSession()))
			return false;
		
		// Check examination status
		if (examType == null) {
			// Check all examination types
			boolean checkDefaultStatus = false;
			for (ExamType et: ExamType.findAllUsed(session.getUniqueId())) {
				ExamStatus examStatus = ExamStatus.findStatus(session.getUniqueId(), et.getUniqueId());
				if (examStatus != null) {
					if (checkManager(authority, examStatus, status) && checkStatus(authority, examStatus.effectiveStatus(), status))
						return true;
				} else {
					checkDefaultStatus = true;
				}
			}
			// All examination types have a status associated with -> fail
			if (!checkDefaultStatus) return false;
		}
		
		// Check examination status of the given type (or the default one when null)
		ExamStatus examStatus = (examType == null ? null : ExamStatus.findStatus(session.getUniqueId(), examType.getUniqueId()));
		DepartmentStatusType type = (examStatus == null ? session.getStatusType() : examStatus.effectiveStatus());
		return checkManager(authority, examStatus, status) && checkStatus(authority, type, status);
	}
	
	@Override
	public boolean check(UserContext user, Department department, ExamType examType, DepartmentStatusType.Status... status) {
		if (!check(user, department.getSession(), examType, status))
			return false;
		
		UserAuthority authority = user.getCurrentAuthority();
		
		// Department check
		if (!authority.hasRight(Right.DepartmentIndependent) && !authority.hasQualifier(department))
			return false;

		// Check department status
		if (status.length > 0 && !authority.hasRight(Right.StatusIndependent)) {
			DepartmentStatusType type = department.getStatusType();
			if (type != null) {
				for (DepartmentStatusType.Status s: status) {
					if (type.can(s)) return true;
				}
				return false;
			}
		}
		
		return true;
	}

	@Override
	public Class<Session> type() {
		return Session.class;
	}
	
	public boolean checkManager(UserAuthority authority, ExamStatus examStatus, DepartmentStatusType.Status... status) {
		if (examStatus == null || authority.hasRight(Right.StatusIndependent) || !authority.hasRight(Right.ExaminationSolver)) return true;
		
		/*
		// skip check for view permissions
		if (status.length > 0)
			for (DepartmentStatusType.Status s: status) {
				if (s == DepartmentStatusType.Status.ExamView)
					return true;
			}
		*/
		
		if (!examStatus.getManagers().isEmpty()) {
			for (TimetableManager m: examStatus.getManagers()) {
				if (authority.hasQualifier(m))
					return true;
			}
			return false;
		}
		
		return true;
	}
	
	public boolean checkStatus(UserAuthority authority, DepartmentStatusType type, DepartmentStatusType.Status... status) {
		if (authority.hasRight(Right.StatusIndependent) || status.length == 0) return true;
		
		if (type != null) {
			for (DepartmentStatusType.Status s: status) {
				if (type.can(s)) return true;
			}
		}
		
		return false;
	}
}