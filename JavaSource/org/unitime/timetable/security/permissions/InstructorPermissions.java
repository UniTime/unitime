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

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class InstructorPermissions {

	@PermissionForRight(Right.AssignInstructors)
	public static class AssignInstructors implements Permission<InstrOfferingConfig> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, InstrOfferingConfig source) {
			if (permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerLimitedEdit))
				return true;
			
			// Manager can edit external department
			Set<Department> externals = new HashSet<Department>();
			for (SchedulingSubpart subpart: source.getSchedulingSubparts()) {
				for (Class_ clazz: subpart.getClasses()) {
					if (clazz.getManagingDept() != null && clazz.getManagingDept().isExternalManager()) {
						if (externals.add(clazz.getManagingDept()) &&
							permissionDepartment.check(user, clazz.getManagingDept(), DepartmentStatusType.Status.ManagerLimitedEdit))
							return true;
					}
				}
			}
			
			return false;
		}

		@Override
		public Class<InstrOfferingConfig> type() { return InstrOfferingConfig.class; }
	}
	
	@PermissionForRight(Right.AssignInstructorsClass)
	public static class AssignInstructorsClass implements Permission<Class_> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Class_ source) {
			return permissionDepartment.check(user, source.getControllingDept(), DepartmentStatusType.Status.OwnerLimitedEdit, 
					source.getManagingDept(), DepartmentStatusType.Status.ManagerLimitedEdit);
		}

		@Override
		public Class<Class_> type() { return Class_.class; }
	}
	
	@PermissionForRight(Right.InstructorDetail)
	public static class InstructorDetail implements Permission<DepartmentalInstructor> {
		
		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, DepartmentalInstructor source) {
			return permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerView);
		}

		@Override
		public Class<DepartmentalInstructor> type() { return DepartmentalInstructor.class; }
		
	}

	@PermissionForRight(Right.InstructorEdit)
	public static class EditInstructor implements Permission<DepartmentalInstructor> {
		
		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, DepartmentalInstructor source) {
			return permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerLimitedEdit);
		}

		@Override
		public Class<DepartmentalInstructor> type() { return DepartmentalInstructor.class; }
		
	}
	
	@PermissionForRight(Right.InstructorDelete)
	public static class DeleteInstructor extends EditInstructor {
		@Override
		public boolean check(UserContext user, DepartmentalInstructor source) {
			if (!source.getClasses().isEmpty()) return false;
			
			if (!source.getExams().isEmpty()) return false;
			
			return super.check(user, source);
		}
	}
	
	@PermissionForRight(Right.InstructorPreferences)
	public static class InstructorPreferences implements Permission<DepartmentalInstructor> {
		
		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, DepartmentalInstructor source) {
			return permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit);
		}

		@Override
		public Class<DepartmentalInstructor> type() { return DepartmentalInstructor.class; }
		
	}
	
	@PermissionForRight(Right.InstructorEditClearPreferences)
	public static class InstructorEditClearPreferences extends InstructorPreferences {}
	
	@PermissionForRight(Right.InstructorAdd)
	public static class InstructorAdd implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source, DepartmentStatusType.Status.OwnerLimitedEdit);
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.Instructors)
	public static class Instructors implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source);
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
	
	@PermissionForRight(Right.InstructorsExportPdf)
	public static class InstructorsExportPdf extends Instructors {}
	
	@PermissionForRight(Right.ManageInstructors)
	public static class ManageInstructors implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source, DepartmentStatusType.Status.OwnerLimitedEdit);
		}

		@Override
		public Class<Department> type() { return Department.class; }
	}
}
