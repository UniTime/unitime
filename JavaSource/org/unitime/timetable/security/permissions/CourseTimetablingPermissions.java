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
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;

/**
 * @author Tomas Muller
 */
public class CourseTimetablingPermissions {
	
	@PermissionForRight(Right.CourseTimetabling)
	public static class CourseTimetabling implements Permission<SolverGroup> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, SolverGroup source) {
			for (Department department: source.getDepartments())
					if (!permissionDepartment.check(user, department, DepartmentStatusType.Status.Timetable))
						return false;
			return true;
		}

		@Override
		public Class<SolverGroup> type() { return SolverGroup.class; }
		
	}
	
	
	@PermissionForRight(Right.CourseTimetablingAudit)
	public static class CourseTimetablingAudit extends CourseTimetabling {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, SolverGroup source) {
			if (super.check(user, source)) return false;
			for (Department department: source.getDepartments())
				if (!permissionDepartment.check(user, department, DepartmentStatusType.Status.Audit, DepartmentStatusType.Status.Timetable))
					return false;
			return true;
		}
		
	}
	
	public static class CourseTimetablingOrAudit extends CourseTimetabling {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, SolverGroup source) {
			for (Department department: source.getDepartments())
				if (!permissionDepartment.check(user, department, DepartmentStatusType.Status.Audit, DepartmentStatusType.Status.Timetable))
					return false;
			return true;
		}
		
	}
	
	@PermissionForRight(Right.Timetables)
	public static class Timetables implements Permission<SolverGroup> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, SolverGroup source) {
			if (source.getCommittedSolution() != null) return true;
			
			for (Department department: source.getDepartments())
					if (!permissionDepartment.check(user, department, DepartmentStatusType.Status.Timetable))
						return false;
			return true;
		}

		@Override
		public Class<SolverGroup> type() { return SolverGroup.class; }
		
	}

	@PermissionForRight(Right.Solver)
	public static class Solver extends CourseTimetablingOrAudit {}

	@PermissionForRight(Right.AssignedClasses)
	public static class AssignedClasses extends CourseTimetabling {}
	
	@PermissionForRight(Right.AssignmentHistory)
	public static class AssignmentHistory extends CourseTimetabling {}
	
	@PermissionForRight(Right.Suggestions)
	public static class Suggestions extends CourseTimetabling {}

	@PermissionForRight(Right.SolutionChanges)
	public static class SolutionChanges extends CourseTimetabling {}

	@PermissionForRight(Right.ConflictStatistics)
	public static class ConflictStatistics extends CourseTimetablingOrAudit {}

	@PermissionForRight(Right.SolverLog)
	public static class SolverLog extends CourseTimetablingOrAudit {}

	@PermissionForRight(Right.NotAssignedClasses)
	public static class NotAssignedClasses extends CourseTimetabling {}

	@PermissionForRight(Right.SolutionReports)
	public static class SolutionReports extends CourseTimetabling {}

	@PermissionForRight(Right.TimetableGrid)
	public static class TimetableGrid extends Timetables {}

	@PermissionForRight(Right.ClassAssignments)
	public static class ClassAssignments implements Permission<Session> {
		@Autowired PermissionSession permissionSession;
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
		
		@Override
		public boolean check(UserContext user, Session source) {
			if (!permissionSession.check(user, source)) return false;
			
			// Has a solver running -> can see assignments
			if (courseTimetablingSolverService.getSolver() != null) return true;
			
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
	
	@PermissionForRight(Right.ClassAssignmentsExportCsv)
	public static class ClassAssignmentsExportCsv extends ClassAssignments {}
	
	@PermissionForRight(Right.ClassAssignmentsExportPdf)
	public static class ClassAssignmentsExportPdf extends ClassAssignments {}

	@PermissionForRight(Right.ClassAssignment)
	public static class ClassAssignment implements Permission<Class_> {
		
		@Autowired SessionContext sessionContext;
		
		@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

		@Autowired PermissionDepartment permissionDepartment;
		
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeededLimitedEdit;
		
		@Override
		public boolean check(UserContext user, Class_ source) {
			// cancelled classes cannot be assigned
			if (source.isCancelled()) return false;

			// Must have a committed solution (not the class per se, but the managing department)
			if (source.getManagingDept() == null || source.getManagingDept().getSolverGroup() == null || source.getManagingDept().getSolverGroup().getCommittedSolution() == null)
				return false;
			
			// No date or time pattern
			if (source.effectiveDatePattern() == null || source.effectiveTimePatterns().isEmpty()) return false;
			
			// Showing an in-memory solution
			if (courseTimetablingSolverService.getSolver() != null) return false;
			
			// Showing a selected solution
			String solutionIdsStr = (String)sessionContext.getAttribute(SessionAttribute.SelectedSolution);
			if (solutionIdsStr != null && !solutionIdsStr.isEmpty()) return false;
			
			// Need an offering lock
			if (permissionOfferingLockNeededLimitedEdit.check(user, source.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering()))
				return false;
			
			// Check departmental permissions
			return permissionDepartment.check(user, source.getManagingDept(), DepartmentStatusType.Status.Timetable);
		}

		@Override
		public Class<Class_> type() { return Class_.class; }
		
	}
	
	@PermissionForRight(Right.TimetablesSolutionCommit)
	public static class TimetablesSolutionCommit implements Permission<SolverGroup> {
		@Autowired PermissionDepartment permissionDepartment;
		
		@Autowired SolverServerService solverServerService;
		
		private boolean hasInstance(Long sessionId) {
			if (sessionId == null) return false;
			return solverServerService.getOnlineStudentSchedulingContainer().hasSolver(sessionId.toString());
		}

		@Override
		public boolean check(UserContext user, SolverGroup source) {
			if (hasInstance(user.getCurrentAcademicSessionId())) return false;
			
			for (Department department: source.getDepartments())
				if (!permissionDepartment.check(user, department, DepartmentStatusType.Status.Commit))
					return false;
			return true;
		}

		@Override
		public Class<SolverGroup> type() { return SolverGroup.class;}
	}
	
	@PermissionForRight(Right.TimetablesSolutionLoad)
	public static class TimetablesSolutionLoad implements Permission<Solution> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Solution source) {
			for (Department department: source.getOwner().getDepartments())
				if (!permissionDepartment.check(user, department, DepartmentStatusType.Status.Timetable))
					return false;
			return true;
		}

		@Override
		public Class<Solution> type() { return Solution.class;}
	}
	
	@PermissionForRight(Right.TimetablesSolutionDelete)
	public static class TimetablesSolutionDelete extends TimetablesSolutionLoad {}
	
	@PermissionForRight(Right.TimetablesSolutionLoadEmpty)
	public static class TimetablesSolutionLoadEmpty implements Permission<SolverGroup> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, SolverGroup source) {
			for (Department department: source.getDepartments())
				if (!permissionDepartment.check(user, department, DepartmentStatusType.Status.Timetable, DepartmentStatusType.Status.Audit))
					return false;
			return true;
		}


		@Override
		public Class<SolverGroup> type() { return SolverGroup.class;}
	}
	
	@PermissionForRight(Right.TimetablesSolutionChangeNote)
	public static class TimetablesSolutionChangeNote extends TimetablesSolutionLoad {}

	@PermissionForRight(Right.TimetablesSolutionExportCsv)
	public static class TimetablesSolutionExportCsv extends TimetablesSolutionLoad {}
	
	@PermissionForRight(Right.SolverSolutionSave)
	public static class SolverSolutionSave implements Permission<SolverGroup> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, SolverGroup source) {
			for (Department department: source.getDepartments())
				if (!permissionDepartment.check(user, department, DepartmentStatusType.Status.Timetable))
					return false;
			return true;
		}

		@Override
		public Class<SolverGroup> type() { return SolverGroup.class;}
	}
	
	@PermissionForRight(Right.SolverSolutionExportCsv)
	public static class SolverSolutionExportCsv extends SolverSolutionSave {}

	@PermissionForRight(Right.SolverSolutionExportXml)
	public static class SolverSolutionExportXml extends SolverSolutionSave {}
	
	@PermissionForRight(Right.ManageSolvers)
	public static class ManageSolvers implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source);
		}

		@Override
		public Class<Session> type() { return Session.class; }
		
	}
}
