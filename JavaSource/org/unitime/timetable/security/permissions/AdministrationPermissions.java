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
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

public class AdministrationPermissions {
	
	@PermissionForRight(Right.Chameleon)
	public static class Chameleon extends SimpleSessionPermission {}
	
	@PermissionForRight(Right.DatePatterns)
	public static class DatePatterns extends SimpleSessionPermission {}
	
	@PermissionForRight(Right.ExaminationPeriods)
	public static class ExaminationPeriods extends SimpleSessionPermission {}

	@PermissionForRight(Right.DataExchange)
	public static class DataExchange extends SimpleSessionPermission {}

	@PermissionForRight(Right.SessionRollForward)
	public static class SessionRollForward extends SimpleSessionPermission {}
	
	@PermissionForRight(Right.Departments)
	public static class Departments extends SimpleSessionPermission {}
	
	@PermissionForRight(Right.DepartmentAdd)
	public static class DepartmentAdd extends SimpleSessionPermission {}
	
	@PermissionForRight(Right.DepartmentEdit)
	public static class DepartmentEdit extends SimpleDepartmentPermission {}
	
	@PermissionForRight(Right.DepartmentDelete)
	public static class DepartmentDelete implements Permission<Department> {
		@Autowired Permission<Department> permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			if (!permissionDepartment.check(user, source))
				return false;
			
			if (source.getSolverGroup() != null)
				return false;
			
			int nrOffered = ((Number)DepartmentDAO.getInstance().getSession().
                    createQuery("select count(io) from CourseOffering co inner join co.instructionalOffering io " +
                    		"where co.subjectArea.department.uniqueId=:deptId and io.notOffered = 0").
                    setLong("deptId", source.getUniqueId()).uniqueResult()).intValue();
            
			return nrOffered == 0;
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.DepartmentEditChangeExternalManager)
	public static class DepartmentEditChangeExternalManager implements Permission<Department> {
		@Autowired Permission<Department> permissionDepartmentEdit;

		@Override
		public boolean check(UserContext user, Department source) {
			if (!permissionDepartmentEdit.check(user, source))
				return false;
			
			if (source.isExternalManager()) {
	            int nrExtManaged = ((Number)DepartmentDAO.getInstance().getSession().
	                    createQuery("select count(c) from Class_ c where c.managingDept.uniqueId=:deptId").
	                    setLong("deptId", source.getUniqueId()).uniqueResult()).intValue();
	            
	            return nrExtManaged == 0;
			} else {
				return source.getSubjectAreas().isEmpty();
			}

		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.AcademicSessionEdit)
	public static class AcademicSessionEdit extends SimpleSessionPermission {}
	
	@PermissionForRight(Right.AcademicSessionDelete)
	public static class AcademicSessionDelete implements Permission<Session> {
		@Autowired Permission<Session> permissionSession;
		
		@Override
		public boolean check(UserContext user, Session source) {
			if (!permissionSession.check(user, source)) return false;
			
			return source.getStatusType() == null || !source.getStatusType().isActive() || source.getStatusType().isTestSession();
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.TimetableManagerEdit)
	public static class TimetableManagerEdit implements Permission<TimetableManager> {
		@Autowired Permission<Session> permissionSession;

		@Override
		public boolean check(UserContext user, TimetableManager source) {
			for (Department d: source.getDepartments()) {
				if (d.getSessionId().equals(user.getCurrentAcademicSessionId())) {
					return permissionSession.check(user, d.getSession());
				}
			}
			
			return true;
		}

		@Override
		public Class<TimetableManager> type() { return TimetableManager.class; }
	}
	
	@PermissionForRight(Right.TimetableManagerDelete)
	public static class TimetableManagerDelete extends TimetableManagerEdit {
		@Override
		public boolean check(UserContext user, TimetableManager source) {
			for (Department d: source.getDepartments())
				if (!permissionSession.check(user, d.getSession()))
					return false;
			
			return true;
		}
	}
	
	@PermissionForRight(Right.SolverGroups)
	public static class SolverGroups extends SimpleSessionPermission {}

	@PermissionForRight(Right.SubjectAreas)
	public static class SubjectAreas extends SimpleSessionPermission {}

	@PermissionForRight(Right.SubjectAreaAdd)
	public static class SubjectAreaAdd extends SubjectAreas {}

	@PermissionForRight(Right.SubjectAreaEdit)
	public static class SubjectAreaEdit implements Permission<SubjectArea> {
		@Autowired Permission<Session> permissionSession;

		@Override
		public boolean check(UserContext user, SubjectArea source) {
			return permissionSession.check(user, source.getSession());
		}

		@Override
		public Class<SubjectArea> type() { return SubjectArea.class; }
	}
	
	@PermissionForRight(Right.SubjectAreaDelete)
	public static class SubjectAreaDelete extends SubjectAreaEdit {
		@Override
		public boolean check(UserContext user, SubjectArea source) {
			if (!super.check(user, source)) return false;
			
			return !source.hasOfferedCourses();
		}
	}
	
	@PermissionForRight(Right.SubjectAreaChangeDepartment)
	public static class SubjectAreaChangeDepartment extends SubjectAreaEdit {
		@Override
		public boolean check(UserContext user, SubjectArea source) {
			if (!super.check(user, source)) return false;
			
			return !source.hasOfferedCourses() || source.getDepartment() == null || source.getDepartment().getSolverGroup() == null ||
					source.getDepartment().getSolverGroup().getCommittedSolution() == null;
		}
	}
}
