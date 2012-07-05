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
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

public class CurriculumPermissions {

	@PermissionForRight(Right.CurriculumView)
	public static class CanView implements Permission<Session> {
		@Autowired
		PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(
					user,
					source);
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.CurriculumAdd)
	public static class CanAdd implements Permission<Department> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return source != null && permissionDepartment.check(
					user,
					source,
					(source.isExternalManager() ? DepartmentStatusType.Status.ManagerEdit : DepartmentStatusType.Status.OwnerEdit)); 
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.CurriculumEdit)
	public static class CanEdit implements Permission<Curriculum> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Curriculum source) {
			Department department = (source == null ? null : source.getDepartment());
			return department != null && permissionDepartment.check(
					user,
					department,
					(department.isExternalManager() ? DepartmentStatusType.Status.ManagerEdit : DepartmentStatusType.Status.OwnerEdit)); 
		}

		@Override
		public Class<Curriculum> type() { return Curriculum.class; }
	}
	
	@PermissionForRight(Right.CurriculumDetail)
	public static class CanDetail implements Permission<Curriculum> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Curriculum source) {
			return source != null && permissionDepartment.check(
					user,
					source.getDepartment()); 
		}

		@Override
		public Class<Curriculum> type() { return Curriculum.class; }
	}
	
	@PermissionForRight(Right.CurriculumDelete)
	public static class CanDelete implements Permission<Curriculum> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Curriculum source) {
			Department department = (source == null ? null : source.getDepartment());
			return department != null && permissionDepartment.check(
					user,
					department,
					(department.isExternalManager() ? DepartmentStatusType.Status.ManagerEdit : DepartmentStatusType.Status.OwnerEdit)); 
		}

		@Override
		public Class<Curriculum> type() { return Curriculum.class; }
	}
	
	@PermissionForRight(Right.CurriculumMerge)
	public static class CanMerge implements Permission<Curriculum> {
		@Autowired
		PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Curriculum source) {
			Department department = (source == null ? null : source.getDepartment());
			return department != null && permissionDepartment.check(
					user,
					department,
					(department.isExternalManager() ? DepartmentStatusType.Status.ManagerEdit : DepartmentStatusType.Status.OwnerEdit)); 
		}

		@Override
		public Class<Curriculum> type() { return Curriculum.class; }
	}
}
