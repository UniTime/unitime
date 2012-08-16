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
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

public class StudentSchedulingPermissions {
	
	@PermissionForRight(Right.StudentScheduling)
	public static class StudentScheduling extends SimpleSessionPermission {}

	@PermissionForRight(Right.StudentSectioningSolver)
	public static class StudentSectioningSolver extends StudentScheduling {}
	
	@PermissionForRight(Right.StudentSectioningSolverLog)
	public static class StudentSectioningSolverLog extends StudentScheduling {}

	@PermissionForRight(Right.EnrollmentAuditPDFReports)
	public static class EnrollmentAuditPDFReports extends SimpleSessionPermission {}

	@PermissionForRight(Right.ConsentApproval)
	public static class ConsentApproval implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (source.getConsentType() == null) return false;
			
			if ("Instructor".equals(user.getCurrentAuthority().getRole())) {
				if (!"IN".equals(source.getConsentType().getReference())) return false;
				
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
	
	@PermissionForRight(Right.OfferingEnrollments)
	public static class OfferingEnrollments implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if ("Instructor".equals(user.getCurrentAuthority().getRole())) {
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
			if ("Student".equals(user.getCurrentAuthority().getRole()))
				return source.getExternalUniqueId().equals(user.getExternalUserId());
			
			return true;
		}

		@Override
		public Class<Student> type() { return Student.class; }
		
	}
}
