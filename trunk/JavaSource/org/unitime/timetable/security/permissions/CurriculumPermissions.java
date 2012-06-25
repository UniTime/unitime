/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.security.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

public class CurriculumPermissions {

	@Service("permissionCurriculumView")
	public static class CanView implements Permission<Session> {
		@Autowired
		PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(
					user,
					source,
					Right.CurriculumView);
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@Service("permissionCurriculumAdd")
	public static class CanAdd implements Permission<Department> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return source != null && permissionDepartment.check(
					user,
					source,
					Right.CurriculumAdd,
					(source.isExternalManager() ? DepartmentStatusType.Status.ManagerEdit : DepartmentStatusType.Status.OwnerEdit)); 
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@Service("permissionCurriculumEdit")
	public static class CanEdit implements Permission<Curriculum> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Curriculum source) {
			Department department = (source == null ? null : source.getDepartment());
			return department != null && permissionDepartment.check(
					user,
					department,
					Right.CurriculumEdit,
					(department.isExternalManager() ? DepartmentStatusType.Status.ManagerEdit : DepartmentStatusType.Status.OwnerEdit)); 
		}

		@Override
		public Class<Curriculum> type() { return Curriculum.class; }
	}
	
	@Service("permissionCurriculumDetail")
	public static class CanDetail implements Permission<Curriculum> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Curriculum source) {
			return source != null && permissionDepartment.check(
					user,
					source.getDepartment(),
					Right.CurriculumDetail); 
		}

		@Override
		public Class<Curriculum> type() { return Curriculum.class; }
	}
	
	@Service("permissionCurriculumDelete")
	public static class CanDelete implements Permission<Curriculum> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Curriculum source) {
			Department department = (source == null ? null : source.getDepartment());
			return department != null && permissionDepartment.check(
					user,
					department,
					Right.CurriculumDelete,
					(department.isExternalManager() ? DepartmentStatusType.Status.ManagerEdit : DepartmentStatusType.Status.OwnerEdit)); 
		}

		@Override
		public Class<Curriculum> type() { return Curriculum.class; }
	}
	
	@Service("permissionCurriculumMerge")
	public static class CanMerge implements Permission<Curriculum> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Curriculum source) {
			Department department = (source == null ? null : source.getDepartment());
			return department != null && permissionDepartment.check(
					user,
					department,
					Right.CurriculumMerge,
					(department.isExternalManager() ? DepartmentStatusType.Status.ManagerEdit : DepartmentStatusType.Status.OwnerEdit)); 
		}

		@Override
		public Class<Curriculum> type() { return Curriculum.class; }
	}
}
