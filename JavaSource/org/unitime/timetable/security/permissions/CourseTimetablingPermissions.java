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
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.service.AssignmentService;

public class CourseTimetablingPermissions {
	
	@PermissionForRight(Right.CourseTimetabling)
	public static class CourseTimetabling implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return source != null && source.getSolverGroup() != null &&
				permissionDepartment.check(user, source, DepartmentStatusType.Status.Timetable);
		}

		@Override
		public Class<Department> type() { return Department.class; }
		
	}
	
	@PermissionForRight(Right.AssignedClasses)
	public static class AssignedClasses implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source, DepartmentStatusType.Status.Timetable);
		}

		@Override
		public Class<Department> type() { return Department.class; }
		
	}
	
	@PermissionForRight(Right.AssignmentHistory)
	public static class AssignmentHistory extends CourseTimetabling {}
	
	@PermissionForRight(Right.Suggestions)
	public static class Suggestions extends CourseTimetabling {}

	@PermissionForRight(Right.SolutionChanges)
	public static class SolutionChanges extends CourseTimetabling {}

	@PermissionForRight(Right.ClassAssignments)
	public static class ClassAssignments implements Permission<Session> {
		@Autowired PermissionSession permissionSession;
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Session source) {
			if (!permissionSession.check(user, source)) return false;
			
			// Check for a department with a committed solution or for my department with a solution
			for (Department department: source.getDepartments()) {
				if (department.getSolverGroup() == null) continue;
				
				if (department.getSolverGroup().getCommittedSolution() != null) return true;
				
				if (permissionDepartment.check(user, department, DepartmentStatusType.Status.Timetable) && !department.getSolverGroup().getSolutions().isEmpty())
					return true;
			}
			
			return false;
		}

		@Override
		public Class<Session> type() { return Session.class; }
		
	}
	
	@PermissionForRight(Right.ClassAssignmentsExportCSV)
	public static class ClassAssignmentsExportCSV extends ClassAssignments {}
	
	@PermissionForRight(Right.ClassAssignmentsExportPDF)
	public static class ClassAssignmentsExportPDF extends ClassAssignments {}

	@PermissionForRight(Right.ClassAssignment)
	public static class ClassAssignment implements Permission<Class_> {
		
		@Autowired SessionContext sessionContext;
		
		@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;

		@Autowired PermissionDepartment permissionDepartment;
		
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeededLimitedEdit;
		
		@Override
		public boolean check(UserContext user, Class_ source) {
			// Must have a committed solution (not the class per se, but the managing department)
			if (source.getManagingDept() == null || source.getManagingDept().getSolverGroup() == null || source.getManagingDept().getSolverGroup().getCommittedSolution() == null)
				return false;
			
			// No date or time pattern
			if (source.effectiveDatePattern() == null || source.effectiveTimePatterns().isEmpty()) return false;
			
			// Showing an in-memory or uncommitted solution
			try {
				Assignment assignment = (classAssignmentService.getAssignment() == null ? null : classAssignmentService.getAssignment().getAssignment(source));
				if (assignment.getUniqueId() == null || assignment.getSolution() == null || !assignment.getSolution().isCommited())
					return false;
			} catch (Exception e) {
				return false;
			}
			
			// Need an offering lock
			if (permissionOfferingLockNeededLimitedEdit.check(user, source.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering()))
				return false;
			
			// Check departmental permissions
			return permissionDepartment.check(user, source.getManagingDept(), DepartmentStatusType.Status.Timetable);
		}

		@Override
		public Class<Class_> type() { return Class_.class; }
		
	}
}
