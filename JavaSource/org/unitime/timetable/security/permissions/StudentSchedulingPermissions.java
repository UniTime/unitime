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
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.DepartmentStatusType.Status;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;

/**
 * @author Tomas Muller
 */
public class StudentSchedulingPermissions {
	
	@PermissionForRight(Right.StudentScheduling)
	public static class StudentScheduling extends SimpleSessionPermission {}

	@PermissionForRight(Right.StudentSectioningSolver)
	public static class StudentSectioningSolver extends StudentScheduling {}
	
	@PermissionForRight(Right.StudentSectioningSolverLog)
	public static class StudentSectioningSolverLog extends StudentScheduling {}
	
	@PermissionForRight(Right.StudentSectioningSolverDashboard)
	public static class StudentSectioningSolverDashboard extends StudentScheduling {}

	@PermissionForRight(Right.StudentSectioningSolutionExportXml)
	public static class StudentSectioningSolutionExportXml extends StudentSectioningSolver {}
	
	@PermissionForRight(Right.EnrollmentAuditPDFReports)
	public static class EnrollmentAuditPDFReports extends SimpleSessionPermission {}
	
	@PermissionForRight(Right.SchedulingAssistant)
	public static class SchedulingAssistant implements Permission<Session> {
		@Autowired PermissionSession permissionSession;
		
		@Autowired SolverServerService solverServerService;
		
		private boolean hasInstance(Long sessionId) {
			if (sessionId == null) return false;
			return solverServerService.getOnlineStudentSchedulingContainer().hasSolver(sessionId.toString());
		}
		
		@Override
		public boolean check(UserContext user, Session source) {
			if (!permissionSession.check(user, source, DepartmentStatusType.Status.StudentsAssistant, DepartmentStatusType.Status.StudentsOnline))
				return false;
			
			return hasInstance(user.getCurrentAcademicSessionId());
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
	
	@PermissionForRight(Right.SchedulingDashboard)
	public static class SchedulingDashboard extends SchedulingAssistant {}
	
	@PermissionForRight(Right.CourseRequests)
	public static class CourseRequests implements Permission<Session> {
		@Autowired PermissionSession permissionSession;
		
		@Override
		public boolean check(UserContext user, Session source) {
			DepartmentStatusType status = source.getStatusType();
			return status != null && status.can(DepartmentStatusType.Status.StudentsPreRegister) &&
					!status.can(DepartmentStatusType.Status.StudentsAssistant) &&
					!status.can(DepartmentStatusType.Status.StudentsOnline);
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}

	@PermissionForRight(Right.ConsentApproval)
	public static class ConsentApproval implements Permission<CourseOffering> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, CourseOffering source) {
			if (source.getConsentType() == null) return false;
			
			if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
				if (!"IN".equals(source.getConsentType().getReference())) return false;
				
				for (DepartmentalInstructor instructor: source.getInstructionalOffering().getCoordinators()) {
					if (user.getExternalUserId().equals(instructor.getExternalUniqueId())) return true;
				}

				return false;
			} else {
				return permissionDepartment.check(user, source.getInstructionalOffering().getControllingCourseOffering().getSubjectArea().getDepartment());
			}
		}

		@Override
		public Class<CourseOffering> type() { return CourseOffering.class; }
		
	}
	
	@PermissionForRight(Right.OfferingEnrollments)
	public static class OfferingEnrollments implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (Roles.ROLE_INSTRUCTOR.equals(user.getCurrentAuthority().getRole())) {
				for (DepartmentalInstructor instructor: source.getCoordinators()) {
					if (user.getExternalUserId().equals(instructor.getExternalUniqueId())) return true;
				}
				
				return false;
			} else {
				return permissionDepartment.check(user, source.getControllingCourseOffering().getSubjectArea().getDepartment());
			}
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
	
	@PermissionForRight(Right.StudentEnrollments)
	public static class StudentEnrollments implements Permission<Student> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Student source) {
			if (Roles.ROLE_STUDENT.equals(user.getCurrentAuthority().getRole()))
				return source.getExternalUniqueId().equals(user.getExternalUserId());
			
			return true;
		}

		@Override
		public Class<Student> type() { return Student.class; }
		
	}
	
	@PermissionForRight(Right.StudentSchedulingMassCancel)
	public static class StudentSchedulingMassCancel extends SimpleSessionPermission {
		@Override
		public boolean check(UserContext user, Session source) {
			return super.check(user, source) && source.getStatusType().can(Status.StudentsOnline) && CustomStudentEnrollmentHolder.isAllowWaitListing();
		}
	}

	@PermissionForRight(Right.StudentSchedulingEmailStudent)
	public static class StudentSchedulingEmailStudent extends SimpleSessionPermission {
		@Override
		public boolean check(UserContext user, Session source) {
			return super.check(user, source) && source.getStatusType().can(Status.StudentsOnline) && ApplicationProperty.OnlineSchedulingEmailConfirmation.isTrue();
		}
	}

	@PermissionForRight(Right.StudentSchedulingChangeStudentStatus)
	public static class StudentSchedulingChangeStudentStatus extends SimpleSessionPermission {
		@Override
		public boolean check(UserContext user, Session source) {
			return super.check(user, source) && source.getStatusType().can(Status.StudentsOnline);
		}
	}
}
