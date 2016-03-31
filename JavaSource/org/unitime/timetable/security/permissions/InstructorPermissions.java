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

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructorAttribute;
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


	@PermissionForRight(Right.InstructorAssignmentPreferences)
	public static class InstructorAssignmentPreferences extends InstructorPreferences {}
	
	@PermissionForRight(Right.InstructorClearAssignmentPreferences)
	public static class InstructorClearAssignmentPreferences extends InstructorEditClearPreferences {}

	@PermissionForRight(Right.InstructorAttributes)
	public static class InstructorAttributes extends Instructors {}
	
	@PermissionForRight(Right.InstructorAttributeAdd)
	public static class InstructorAttributeAdd extends Instructors {}
	
	@PermissionForRight(Right.InstructorAttributeEdit)
	public static class InstructorAttributeEdit implements Permission<InstructorAttribute> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, InstructorAttribute source) {
			if (source.getDepartment() != null)
				return permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit);
			else
				return user.getCurrentAuthority().hasRight(Right.InstructorGlobalAttributeEdit) && permissionSession.check(user, source.getSession(), DepartmentStatusType.Status.OwnerEdit);
		}

		@Override
		public Class<InstructorAttribute> type() { return InstructorAttribute.class; }
	}
	
	@PermissionForRight(Right.InstructorAttributeDelete)
	public static class InstructorAttributeDelete extends InstructorAttributeEdit {}
	
	@PermissionForRight(Right.InstructorAttributeAssign)
	public static class InstructorAttributeAssign implements Permission<InstructorAttribute> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, InstructorAttribute source) {
			if (source.getDepartment() != null)
				return permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerLimitedEdit);
			else
				return permissionSession.check(user, source.getSession(), DepartmentStatusType.Status.OwnerLimitedEdit);
		}

		@Override
		public Class<InstructorAttribute> type() { return InstructorAttribute.class; }
	}
	
	
}
