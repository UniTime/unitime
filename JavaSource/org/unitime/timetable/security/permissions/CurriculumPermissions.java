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
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class CurriculumPermissions {

	@PermissionForRight(Right.CurriculumView)
	public static class CurriculumView implements Permission<Session> {
		@Autowired
		PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source, DepartmentStatusType.Status.OwnerView, DepartmentStatusType.Status.ManagerView);
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.CurriculumAdd)
	public static class CurriculumAdd implements Permission<Department> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return source != null && permissionDepartment.check(user, source, DepartmentStatusType.Status.OwnerEdit, DepartmentStatusType.Status.ManagerEdit); 
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.CurriculumEdit)
	public static class CurriculumEdit implements Permission<Curriculum> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Curriculum source) {
			Department department = (source == null ? null : source.getDepartment());
			return department != null && permissionDepartment.check(user, department, DepartmentStatusType.Status.OwnerEdit, DepartmentStatusType.Status.ManagerEdit); 
		}

		@Override
		public Class<Curriculum> type() { return Curriculum.class; }
	}
	
	@PermissionForRight(Right.CurriculumDetail)
	public static class CurriculumDetail implements Permission<Curriculum> {
		@Autowired
		PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Curriculum source) {
			return source != null && permissionSession.check(user, source.getDepartment().getSession()); 
		}

		@Override
		public Class<Curriculum> type() { return Curriculum.class; }
	}
	
	@PermissionForRight(Right.CurriculumDelete)
	public static class CurriculumDelete extends CurriculumEdit { }
	
	@PermissionForRight(Right.CurriculumMerge)
	public static class CurriculumMerge extends CurriculumEdit { }
	
	@PermissionForRight(Right.CurriculumAdmin)
	public static class CurriculumAdmin implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) && permissionSession.check(user, source);
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.CurriculumProjectionRulesDetail)
	public static class CurriculumProjectionRulesDetail extends CurriculumView {}
	
	@PermissionForRight(Right.CurriculumProjectionRulesEdit)
	public static class CurriculumProjectionRulesEdit extends CurriculumAdmin {}
}
