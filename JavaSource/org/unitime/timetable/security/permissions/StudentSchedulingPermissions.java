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
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingCoordinator;
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
	
	@PermissionForRight(Right.StudentSectioningSolverSave)
	public static class StudentSectioningSolverSave extends StudentScheduling {}

	@PermissionForRight(Right.StudentSectioningSolutionExportXml)
	public static class StudentSectioningSolutionExportXml extends StudentSectioningSolver {}
	
	@PermissionForRight(Right.EnrollmentAuditPDFReports)
	public static class EnrollmentAuditPDFReports extends SimpleSessionPermission {}
	
	@PermissionForRight(Right.SchedulingAssistant)
	public static class SchedulingAssistant implements Permission<Session> {
		@Autowired PermissionSession permissionSession;
		
		@Autowired SolverServerService solverServerService;
		
		protected boolean hasInstance(Long sessionId) {
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
	public static class SchedulingDashboard extends SchedulingAssistant {
		@Override
		public boolean check(UserContext user, Session source) {
			return super.check(user, source) || permissionSession.check(user, source, DepartmentStatusType.Status.StudentsPreRegister);
		}
	}
	
	@PermissionForRight(Right.SchedulingReports)
	public static class SchedulingReports extends SchedulingAssistant {}
	
	@PermissionForRight(Right.CourseRequests)
	public static class CourseRequests implements Permission<Session> {
		@Autowired PermissionSession permissionSession;
		
		@Override
		public boolean check(UserContext user, Session source) {
			DepartmentStatusType status = source.getStatusType();
			return status != null && status.can(DepartmentStatusType.Status.StudentsPreRegister);
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
				
				for (OfferingCoordinator coordinator: source.getInstructionalOffering().getOfferingCoordinators()) {
					if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
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
				for (OfferingCoordinator coordinator: source.getOfferingCoordinators()) {
					if (user.getExternalUserId().equals(coordinator.getInstructor().getExternalUniqueId())) return true;
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
			return super.check(user, source) && (source.getStatusType().can(Status.StudentsOnline) || source.getStatusType().can(Status.StudentsAssistant) || source.getStatusType().can(Status.StudentsPreRegister));
		}
	}
	
	@PermissionForRight(Right.StudentSchedulingRequestStudentUpdate)
	public static class StudentSchedulingRequestStudentUpdate extends SimpleSessionPermission {
		@Override
		public boolean check(UserContext user, Session source) {
			return super.check(user, source) && source.getStatusType().can(Status.StudentsOnline) && ApplicationProperty.CustomizationStudentEnrollments.value() != null;
		}
	}
	
	@PermissionForRight(Right.StudentSchedulingAdvisor)
	public static class StudentSchedulingAdvisor extends StudentScheduling {}
	
	@PermissionForRight(Right.StudentSchedulingAdmin)
	public static class StudentSchedulingAdmin extends StudentScheduling {}
}
