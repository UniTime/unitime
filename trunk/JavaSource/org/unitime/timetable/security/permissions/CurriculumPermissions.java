/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
