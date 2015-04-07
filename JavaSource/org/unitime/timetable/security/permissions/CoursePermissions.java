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
import org.springframework.stereotype.Service;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;

/**
 * @author Tomas Muller
 */
public class CoursePermissions {
	
	@Service("permissionOfferingLockNeeded")
	public static class OfferingLockNeeded implements Permission<InstructionalOffering> {
		@Autowired PermissionSession permissionSession;
		
		@Autowired SolverServerService solverServerService;
		
		protected OnlineSectioningServer getInstance(Long sessionId) {
			if (sessionId == null) return null;
			return solverServerService.getOnlineStudentSchedulingContainer().getSolver(sessionId.toString());
		}

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (source.isNotOffered()) return false;
			
			if (!permissionSession.check(
					user,
					source.getSession(),
					DepartmentStatusType.Status.StudentsAssistant, DepartmentStatusType.Status.StudentsOnline))
				return false;
			
			OnlineSectioningServer server = getInstance(user.getCurrentAcademicSessionId());
			
			return server != null && !server.isOfferingLocked(source.getUniqueId());
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
	
	@Service("permissionOfferingLockNeededLimitedEdit")
	public static class OfferingLockNeededLimitedEdit implements Permission<InstructionalOffering> {
		@Autowired PermissionSession permissionSession;

		@Autowired SolverServerService solverServerService;
		
		protected OnlineSectioningServer getInstance(Long sessionId) {
			if (sessionId == null) return null;
			return solverServerService.getOnlineStudentSchedulingContainer().getSolver(sessionId.toString());
		}

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (source.isNotOffered()) return false;
			
			if (!permissionSession.check(
					user,
					source.getSession(),
					DepartmentStatusType.Status.StudentsOnline))
				return false;
			
			OnlineSectioningServer server = getInstance(user.getCurrentAcademicSessionId());
			
			return server != null && server.getAcademicSession().isSectioningEnabled() && !server.isOfferingLocked(source.getUniqueId());
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
	}
	
	@Service("permissionOfferingEdit")
	public static class OfferingEdit implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			// Owner can edit one of the course offerings
			for (CourseOffering course: source.getCourseOfferings()) {
				if (permissionDepartment.check(user, course.getDepartment(), DepartmentStatusType.Status.OwnerLimitedEdit))
					return true;
			}
			
			// Manager can edit external department
			Set<Department> externals = new HashSet<Department>();
			for (InstrOfferingConfig config: source.getInstrOfferingConfigs()) {
				for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
					for (Class_ clazz: subpart.getClasses()) {
						if (clazz.getManagingDept() != null && clazz.getManagingDept().isExternalManager()) {
							if (externals.add(clazz.getManagingDept()) &&
								permissionDepartment.check(user, clazz.getManagingDept(), DepartmentStatusType.Status.ManagerLimitedEdit))
								return true;
						}
					}
				}
			}
			
			return false;
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
	
	@PermissionForRight(Right.OfferingCanLock)
	public static class OfferingCanLock implements Permission<InstructionalOffering> {
		@Autowired Permission<InstructionalOffering> permissionOfferingEdit;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (!permissionOfferingLockNeeded.check(user, source))
				return false; // locking not need (e.g., bad status or already locked)
			
			if (!permissionOfferingEdit.check(user, source))
				return false; // user is not able to edit the offering -> no need to lock

			return true;
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
	
	@PermissionForRight(Right.OfferingCanUnlock)
	public static class OfferingCanUnlock implements Permission<InstructionalOffering> {
		@Autowired Permission<InstructionalOffering> permissionOfferingEdit;

		@Autowired SolverServerService solverServerService;
		
		protected OnlineSectioningServer getInstance(Long sessionId) {
			if (sessionId == null) return null;
			return solverServerService.getOnlineStudentSchedulingContainer().getSolver(sessionId.toString());
		}

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (!permissionOfferingEdit.check(user, source))
				return false; // user is not able to edit the offering -> no need to lock

			OnlineSectioningServer server = getInstance(user.getCurrentAcademicSessionId());
			
			return user.getCurrentAuthority().hasRight(Right.OfferingCanUnlock) && server != null && server.isOfferingLocked(source.getUniqueId());
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}

	@PermissionForRight(Right.AddCourseOffering)
	public static class AddCourseOffering implements Permission<SubjectArea> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, SubjectArea source) {
			return permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit);
		}

		@Override
		public Class<SubjectArea> type() { return SubjectArea.class; }
		
	}
	
	@PermissionForRight(Right.InstructionalOfferingDetail)
	public static class InstructionalOfferingDetail implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			// Owner can view one of the course offerings
			for (CourseOffering course: source.getCourseOfferings()) {
				if (permissionDepartment.check(user, course.getDepartment(), DepartmentStatusType.Status.OwnerView))
					return true;
			}
			
			/*
			for (Department dept: source.getSession().getDepartments()) {
				if (dept.isExternalManager() && permissionDepartment.check(user, dept, DepartmentStatusType.Status.ManagerView))
					return true;
			}
			*/

			// Manager can view one of the classes
			Set<Department> externals = new HashSet<Department>();
			for (InstrOfferingConfig config: source.getInstrOfferingConfigs()) {
				for (SchedulingSubpart subpart: config.getSchedulingSubparts()) {
					for (Class_ clazz: subpart.getClasses()) {
						if (clazz.getManagingDept() != null && clazz.getManagingDept().isExternalManager()) {
							if (externals.add(clazz.getManagingDept()) &&
								permissionDepartment.check(user, clazz.getManagingDept(), DepartmentStatusType.Status.ManagerView))
								return true;
						}
					}
				}
			}
			
			return false;
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
	
	@PermissionForRight(Right.SchedulingSubpartDetail)
	public static class SchedulingSubpartDetail implements Permission<SchedulingSubpart> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, SchedulingSubpart source) {
			return permissionDepartment.check(user, source.getControllingDept(), DepartmentStatusType.Status.OwnerView,
					source.getManagingDept(), DepartmentStatusType.Status.ManagerView);
		}

		@Override
		public Class<SchedulingSubpart> type() { return SchedulingSubpart.class; }
	}
	
	@PermissionForRight(Right.ClassDetail)
	public static class ClassDetail implements Permission<Class_> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Class_ source) {
			return permissionDepartment.check(user, source.getControllingDept(), DepartmentStatusType.Status.OwnerView,
					source.getManagingDept(), DepartmentStatusType.Status.ManagerView);
		}

		@Override
		public Class<Class_> type() { return Class_.class; }
	}
	
	@PermissionForRight(Right.ClassEdit)
	public static class ClassEdit implements Permission<Class_> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, Class_ source) {
			// cancelled classes cannot be edited
			if (source.isCancelled()) return false;
			
			return !permissionOfferingLockNeeded.check(user, source.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering()) &&
					permissionDepartment.check(user, source.getControllingDept(), DepartmentStatusType.Status.OwnerEdit,
							source.getManagingDept(), DepartmentStatusType.Status.ManagerEdit);
		}

		@Override
		public Class<Class_> type() { return Class_.class; }
	}
	
	@PermissionForRight(Right.SchedulingSubpartEdit)
	public static class SchedulingSubpartEdit implements Permission<SchedulingSubpart> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, SchedulingSubpart source) {
			return !permissionOfferingLockNeeded.check(user, source.getInstrOfferingConfig().getInstructionalOffering()) &&
					permissionDepartment.check(user, source.getControllingDept(), DepartmentStatusType.Status.OwnerEdit,
							source.getManagingDept(), DepartmentStatusType.Status.ManagerEdit);
		}

		@Override
		public Class<SchedulingSubpart> type() { return SchedulingSubpart.class; }
	}

	@PermissionForRight(Right.MultipleClassSetup)
	public static class MultipleClassSetup implements Permission<InstrOfferingConfig> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, InstrOfferingConfig source) {
			if (source.getInstructionalOffering().isNotOffered()) return false;
			
			if (permissionOfferingLockNeeded.check(user, source.getInstructionalOffering())) return false;
			
			if (permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit))
				return true;
			
			// Manager can edit external department
			Set<Department> externals = new HashSet<Department>();
			for (SchedulingSubpart subpart: source.getSchedulingSubparts()) {
				for (Class_ clazz: subpart.getClasses()) {
					if (clazz.getManagingDept() != null && clazz.getManagingDept().isExternalManager()) {
						if (externals.add(clazz.getManagingDept()) &&
							permissionDepartment.check(user, clazz.getManagingDept(), DepartmentStatusType.Status.ManagerEdit))
							return true;
					}
				}
			}
			
			return false;
		}

		@Override
		public Class<InstrOfferingConfig> type() { return InstrOfferingConfig.class; }
	}
	
	@PermissionForRight(Right.InstrOfferingConfigEdit)
	public static class InstrOfferingConfigEdit implements Permission<InstrOfferingConfig> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, InstrOfferingConfig source) {
			if (permissionOfferingLockNeeded.check(user, source.getInstructionalOffering())) return false;
			
			if (source.getInstructionalOffering().isNotOffered()) return false;
			
			if (permissionDepartment.check(user, source.getInstructionalOffering().getDepartment(), DepartmentStatusType.Status.OwnerEdit)) return true;
			
			return false;
		}

		@Override
		public Class<InstrOfferingConfig> type() { return InstrOfferingConfig.class; }
	}
	
	@PermissionForRight(Right.InstrOfferingConfigAdd)
	public static class InstrOfferingConfigAdd implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (permissionOfferingLockNeeded.check(user, source)) return false;
			
			if (source.isNotOffered()) return false;
			
			if (permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit)) return true;
			
			return false;
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
	
	@PermissionForRight(Right.InstrOfferingConfigDelete)
	public static class InstrOfferingConfigDelete implements Permission<InstrOfferingConfig> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, InstrOfferingConfig source) {
			if (source.getInstructionalOffering().isNotOffered()) return false;
			
			if (source.getInstructionalOffering().getInstrOfferingConfigs().size() <= 1) return false;

			if (permissionOfferingLockNeeded.check(user, source.getInstructionalOffering())) return false;
			
			if (source.getInstructionalOffering().isNotOffered()) return false;
			
			// Manager can edit external department
			Set<Department> externals = new HashSet<Department>();
			for (SchedulingSubpart subpart: source.getSchedulingSubparts()) {
				for (Class_ clazz: subpart.getClasses()) {
					if (clazz.getManagingDept() != null && clazz.getManagingDept().isExternalManager()) {
						if (externals.add(clazz.getManagingDept()) &&
							!permissionDepartment.check(user, clazz.getManagingDept(), DepartmentStatusType.Status.ManagerEdit) &&
							!clazz.getManagingDept().effectiveStatusType().can(DepartmentStatusType.Status.OwnerEdit))
							return false;
					}
				}
			}
			
			return permissionDepartment.check(user, source.getInstructionalOffering().getDepartment(), DepartmentStatusType.Status.OwnerEdit);
		}

		@Override
		public Class<InstrOfferingConfig> type() { return InstrOfferingConfig.class; }
		
	}

	@PermissionForRight(Right.InstrOfferingConfigEditDepartment)
	public static class InstrOfferingConfigEditDepartment implements Permission<Department> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			if (source.isExternalManager() && permissionDepartment.check(user, source, DepartmentStatusType.Status.ManagerEdit))
				return true;
			
			if (!source.isExternalManager() && permissionDepartment.check(user, source, DepartmentStatusType.Status.OwnerEdit))
				return true;
			
			if (source.isExternalManager() && source.effectiveStatusType().can(DepartmentStatusType.Status.OwnerEdit))
				return true;
			
			return false;
		}

		@Override
		public Class<Department> type() { return Department.class; }
		
	}
	
	@PermissionForRight(Right.InstrOfferingConfigEditSubpart)
	public static class InstrOfferingConfigEditSubpart extends SchedulingSubpartEdit {}
	
	@PermissionForRight(Right.InstructionalOfferingCrossLists)
	public static class InstructionalOfferingCrossLists implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (permissionOfferingLockNeeded.check(user, source)) return false;
			
			if (permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit)) return true;
			
			return false;
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
	
	@PermissionForRight(Right.MultipleClassSetupDepartment)
	public static class MultipleClassSetupDepartment extends InstrOfferingConfigEditDepartment {}
	
	@PermissionForRight(Right.MultipleClassSetupClass)
	public static class MultipleClassSetupClassEdit extends ClassEdit {}

	@PermissionForRight(Right.OfferingMakeOffered)
	public static class OfferingMakeOffered implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (!source.isNotOffered()) return false;
			
			if (permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit)) return true;
			
			return false;
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
	
	@PermissionForRight(Right.OfferingMakeNotOffered)
	public static class OfferingMakeNotOffered implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (permissionOfferingLockNeeded.check(user, source)) return false;
			
			if (source.isNotOffered()) return false;
			
			if (permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit)) return true;
			
			return false;
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
	
	@PermissionForRight(Right.OfferingDelete)
	public static class OfferingDelete implements Permission<InstructionalOffering> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, InstructionalOffering source) {
			if (!source.isNotOffered()) return false;
			
			if (permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit)) return true;
			
			return false;
		}

		@Override
		public Class<InstructionalOffering> type() { return InstructionalOffering.class; }
		
	}
	
	@PermissionForRight(Right.EditCourseOffering) 
	public static class EditCourseOffering implements Permission<CourseOffering> {
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, CourseOffering source) {
			if (permissionOfferingLockNeeded.check(user, source.getInstructionalOffering())) return false;

			return permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerEdit);
		}

		@Override
		public Class<CourseOffering> type() { return CourseOffering.class; }
		
	}
	
	@PermissionForRight(Right.EditCourseOfferingNote) 
	public static class EditCourseOfferingNote implements Permission<CourseOffering> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, CourseOffering source) {
			return permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerLimitedEdit);
		}

		@Override
		public Class<CourseOffering> type() { return CourseOffering.class; }
		
	}
	
	@PermissionForRight(Right.EditCourseOfferingCoordinators) 
	public static class EditCourseOfferingCoordinators implements Permission<CourseOffering> {
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, CourseOffering source) {
			return source.isIsControl() && permissionDepartment.check(user, source.getDepartment(), DepartmentStatusType.Status.OwnerLimitedEdit);
		}

		@Override
		public Class<CourseOffering> type() { return CourseOffering.class; }
		
	}
	
	@PermissionForRight(Right.CanUseHardPeriodPrefs)
	public static class CanUseHardPeriodPrefs implements Permission<PreferenceGroup> {

		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, PreferenceGroup source) {
			return user.getCurrentAuthority().hasRight(Right.DepartmentIndependent);
		}

		@Override
		public Class<PreferenceGroup> type() { return PreferenceGroup.class; }
	}
	
	@PermissionForRight(Right.CanUseHardTimePrefs)
	public static class CanUseHardTimePrefs implements Permission<PreferenceGroup> {

		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, PreferenceGroup source) {
			if (user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) || source.getDepartment() == null) return true;
			
			Department department = source.getDepartment();
			if (source instanceof Class_)
				department = ((Class_)source).getManagingDept();
			if (source instanceof SchedulingSubpart)
				department = ((SchedulingSubpart)source).getManagingDept();

			return department == null || user.getCurrentAuthority().hasQualifier(department) || source.getDepartment().getAllowReqTime();
		}

		@Override
		public Class<PreferenceGroup> type() { return PreferenceGroup.class; }
	}
	
	@PermissionForRight(Right.CanUseHardRoomPrefs)
	public static class CanUseHardRoomPrefs implements Permission<PreferenceGroup> {

		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, PreferenceGroup source) {
			if (user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) || source.getDepartment() == null) return true;
			
			Department department = source.getDepartment();
			if (source instanceof Class_)
				department = ((Class_)source).getManagingDept();
			if (source instanceof SchedulingSubpart)
				department = ((SchedulingSubpart)source).getManagingDept();

			return department == null || user.getCurrentAuthority().hasQualifier(department) || source.getDepartment().getAllowReqRoom();
		}

		@Override
		public Class<PreferenceGroup> type() { return PreferenceGroup.class; }
	}
	
	@PermissionForRight(Right.CanUseHardDistributionPrefs)
	public static class CanUseHardDistributionPrefs implements Permission<PreferenceGroup> {

		@Autowired PermissionDepartment permissionDepartment;
		
		@Override
		public boolean check(UserContext user, PreferenceGroup source) {
			if (user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) || source.getDepartment() == null) return true;
			
			Department department = source.getDepartment();
			if (source instanceof Class_)
				department = ((Class_)source).getManagingDept();
			if (source instanceof SchedulingSubpart)
				department = ((SchedulingSubpart)source).getManagingDept();

			return department == null || user.getCurrentAuthority().hasQualifier(department) || source.getDepartment().getAllowReqDistribution();
		}

		@Override
		public Class<PreferenceGroup> type() { return PreferenceGroup.class; }
	}
		
	@PermissionForRight(Right.InstructionalOfferings)
	public static class InstructionalOfferings implements Permission<Department> {
		
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source, DepartmentStatusType.Status.OwnerView, DepartmentStatusType.Status.ManagerView);
		}

		@Override
		public Class<Department> type() { return Department.class; }
		
	}
	
	@PermissionForRight(Right.InstructionalOfferingsExportPDF)
	public static class InstructionalOfferingsExportPDF extends InstructionalOfferings {}

	@PermissionForRight(Right.InstructionalOfferingsWorksheetPDF)
	public static class InstructionalOfferingsWorksheetPDF extends InstructionalOfferings {}

	@PermissionForRight(Right.Classes)
	public static class Classes extends InstructionalOfferings {}

	@PermissionForRight(Right.ClassesExportPDF)
	public static class ClassesExportPDF extends InstructionalOfferings {}
			
	@PermissionForRight(Right.DistributionPreferenceClass)
	public static class DistributionPreferenceClass extends ClassEdit {}
	
	@PermissionForRight(Right.ClassEditClearPreferences)
	public static class ClassEditClearPreferences extends ClassEdit {}
	
	@PermissionForRight(Right.DistributionPreferenceSubpart)
	public static class DistributionPreferenceSubpart extends SchedulingSubpartEdit {}
	
	@PermissionForRight(Right.SchedulingSubpartDetailClearClassPreferences)
	public static class SchedulingSubpartDetailClearClassPreferences extends SchedulingSubpartEdit {}
	
	@PermissionForRight(Right.SchedulingSubpartEditClearPreferences)
	public static class SchedulingSubpartEditClearPreferences extends SchedulingSubpartEdit {}
	
	@PermissionForRight(Right.DistributionPreferences)
	public static class DistributionPreferences implements Permission<Department> {
		
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source, source.isExternalManager() ? DepartmentStatusType.Status.ManagerView : DepartmentStatusType.Status.OwnerView);
		}

		@Override
		public Class<Department> type() { return Department.class; }
		
	}
	
	@PermissionForRight(Right.DistributionPreferenceAdd)
	public static class DistributionPreferenceAdd implements Permission<Department> {
		
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Department source) {
			return permissionDepartment.check(user, source, source.isExternalManager() ? DepartmentStatusType.Status.ManagerEdit : DepartmentStatusType.Status.OwnerEdit);
		}

		@Override
		public Class<Department> type() { return Department.class; }
		
	}
	
	@PermissionForRight(Right.DistributionPreferenceEdit)
	public static class DistributionPreferenceEdit implements Permission<DistributionPref> {
		@Autowired PermissionDepartment permissionDepartment;
		
		@Autowired Permission<Class_> permissionClassEdit;
		
		@Autowired Permission<SchedulingSubpart> permissionSchedulingSubpartEdit;

		@Override
		public boolean check(UserContext user, DistributionPref source) {
			// Get owning department
			Department owner = null;
	    	if (source.getOwner() instanceof DepartmentalInstructor) {
	    		owner = ((DepartmentalInstructor)source.getOwner()).getDepartment();
	    	} else {
	    		owner = (Department) source.getOwner();
	    	}
	    	
	    	// No owning department
	    	if (owner == null) return false;

	    	// If departmental dependent role, check distribution type applicability 
	    	if (!user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) && 
	    		!source.getDistributionType().isApplicable(owner))
	    		return false;
	    		    	
	    	// If my department -> check status
	    	if (permissionDepartment.check(user, owner)) {
	    		return permissionDepartment.check(user, owner, owner.isExternalManager() ? DepartmentStatusType.Status.ManagerEdit : DepartmentStatusType.Status.OwnerEdit);
	    	} else {
	    		
	    	}
	    	
	    	// Not my department -- check if it is allowed to require
	    	if (!owner.isAllowReqDistribution()) {
	    		if (source.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sRequired)) {
	    			if (source.getDistributionType().getAllowedPref().indexOf(PreferenceLevel.sCharLevelStronglyPreferred) >= 0)
	    				source.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
	    			else
	    				return false;
    			}
        		if (source.getPrefLevel().getPrefProlog().equals(PreferenceLevel.sProhibited)) {
        			if (source.getDistributionType().getAllowedPref().indexOf(PreferenceLevel.sCharLevelStronglyDiscouraged) >= 0)
        				source.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
        			else
        				return false;
        		}
	    	}
	    	
	    	// Not my department -- must be able to edit all classes and subparts
       		for (DistributionObject distrObj: source.getDistributionObjects()) {
       			if (distrObj.getPrefGroup() instanceof Class_) {
       				if (!permissionClassEdit.check(user,  (Class_)distrObj.getPrefGroup()))
       					return false;
       			} else if (distrObj.getPrefGroup() instanceof SchedulingSubpart) {
       				if (!permissionSchedulingSubpartEdit.check(user,  (SchedulingSubpart)distrObj.getPrefGroup()))
       					return false;
       			} else {
       				return false;
       			}
       		}
       		
       		return true;
		}

		@Override
		public Class<DistributionPref> type() { return DistributionPref.class;}
	}
	
	@PermissionForRight(Right.DistributionPreferenceDelete)
	public static class DistributionPreferenceDelete implements Permission<DistributionPref> {
		@Autowired PermissionDepartment permissionDepartment;
		
		@Autowired Permission<Class_> permissionClassEdit;
		
		@Autowired Permission<SchedulingSubpart> permissionSchedulingSubpartEdit;

		@Override
		public boolean check(UserContext user, DistributionPref source) {
			// Get owning department
			Department owner = null;
	    	if (source.getOwner() instanceof DepartmentalInstructor) {
	    		owner = ((DepartmentalInstructor)source.getOwner()).getDepartment();
	    	} else {
	    		owner = (Department) source.getOwner();
	    	}
	    	
	    	// No owning department
	    	if (owner == null) return true;

	    	// If my department -> check status
	    	if (permissionDepartment.check(user, owner)) {
	    		return permissionDepartment.check(user, owner, owner.isExternalManager() ? DepartmentStatusType.Status.ManagerEdit : DepartmentStatusType.Status.OwnerEdit);
	    	}
	    	
	    	// Not my department -- must be able to edit all classes and subparts
       		for (DistributionObject distrObj: source.getDistributionObjects()) {
       			if (distrObj.getPrefGroup() instanceof Class_) {
       				if (!permissionClassEdit.check(user,  (Class_)distrObj.getPrefGroup()))
       					return false;
       			} else if (distrObj.getPrefGroup() instanceof SchedulingSubpart) {
       				if (!permissionSchedulingSubpartEdit.check(user,  (SchedulingSubpart)distrObj.getPrefGroup()))
       					return false;
       			} else {
       				return false;
       			}
       		}
       		
       		return true;
		}

		@Override
		public Class<DistributionPref> type() { return DistributionPref.class;}
	}
	
	@PermissionForRight(Right.DistributionPreferenceDetail)
	public static class DistributionPreferenceDetail implements Permission<DistributionPref> {
		@Autowired PermissionDepartment permissionDepartment;
		
		@Autowired Permission<Class_> permissionClassDetail;
		
		@Autowired Permission<SchedulingSubpart> permissionSchedulingSubpartDetail;

		@Override
		public boolean check(UserContext user, DistributionPref source) {
			// Get owning department
			Department owner = null;
	    	if (source.getOwner() instanceof DepartmentalInstructor) {
	    		owner = ((DepartmentalInstructor)source.getOwner()).getDepartment();
	    	} else {
	    		owner = (Department) source.getOwner();
	    	}
	    	
	    	// No owning department
	    	if (owner == null) return false;
	    	
	    	// If my department -> check status
	    	if (permissionDepartment.check(user, owner)) {
	    		return permissionDepartment.check(user, owner, owner.isExternalManager() ? DepartmentStatusType.Status.ManagerView : DepartmentStatusType.Status.OwnerView);
	    	}
	    	
	    	// Not my department -- must be able to view at least one class or subpart
       		for (DistributionObject distrObj: source.getDistributionObjects()) {
       			if (distrObj.getPrefGroup() instanceof Class_) {
       				if (permissionClassDetail.check(user,  (Class_)distrObj.getPrefGroup())) return true;
       			} else if (distrObj.getPrefGroup() instanceof SchedulingSubpart) {
       				if (permissionSchedulingSubpartDetail.check(user,  (SchedulingSubpart)distrObj.getPrefGroup())) return true;
       			} else {
       				return false;
       			}
       		}
       		
       		return false;
		}

		@Override
		public Class<DistributionPref> type() { return DistributionPref.class;}
	}
	
	@PermissionForRight(Right.ClassDelete)
	public static class ClassDelete implements Permission<Class_> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, Class_ source) {
			// There is a committed solution -> class with enrollment cannot be edited
			if (source.getManagingDept() != null && source.getManagingDept().getSolverGroup() != null && source.getManagingDept().getSolverGroup().getCommittedSolution() != null) {
				if (source.getEnrollment() > 0) return false;
			}

			return !permissionOfferingLockNeeded.check(user, source.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering()) &&
					permissionDepartment.check(user, source.getControllingDept(), DepartmentStatusType.Status.OwnerEdit,
							source.getManagingDept(), DepartmentStatusType.Status.ManagerEdit);
		}

		@Override
		public Class<Class_> type() { return Class_.class; }
	}

	@PermissionForRight(Right.ClassCancel)
	public static class ClassCancel implements Permission<Class_> {
		@Autowired PermissionDepartment permissionDepartment;
		@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;

		@Override
		public boolean check(UserContext user, Class_ source) {
			// Must have a committed solution (not the class per se, but the managing department)
			if (source.getManagingDept() == null || source.getManagingDept().getSolverGroup() == null || source.getManagingDept().getSolverGroup().getCommittedSolution() == null)
				return false;

			return !permissionOfferingLockNeeded.check(user, source.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering()) &&
					permissionDepartment.check(user, source.getControllingDept(), DepartmentStatusType.Status.OwnerEdit,
							source.getManagingDept(), DepartmentStatusType.Status.ManagerEdit);
		}

		@Override
		public Class<Class_> type() { return Class_.class; }
	}
}
