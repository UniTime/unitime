/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.hibernate.criterion.Order;
import org.unitime.timetable.model.base.BaseRoles;
import org.unitime.timetable.model.dao.RolesDAO;
import org.unitime.timetable.security.rights.HasRights;
import org.unitime.timetable.security.rights.Right;




public class Roles extends BaseRoles implements HasRights {

/**
	 *
	 */
	private static final long serialVersionUID = 3256722879445154100L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Roles () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Roles (java.lang.Long roleId) {
		super(roleId);
	}

/*[CONSTRUCTOR MARKER END]*/

	@Deprecated
	public static String ADMIN_ROLE = "Administrator";
	@Deprecated
	public static String DEPT_SCHED_MGR_ROLE = "Dept Sched Mgr";
	@Deprecated
	public static String VIEW_ALL_ROLE = "View All";
	@Deprecated
	public static String EXAM_MGR_ROLE = "Exam Mgr";
	public static String EVENT_MGR_ROLE = "Event Mgr";
	@Deprecated
	public static String CURRICULUM_MGR_ROLE = "Curriculum Mgr";
	@Deprecated
	public static String STUDENT_ADVISOR = "Advisor";
	
    public static String USER_ROLES_ATTR_NAME = "userRoles";
    public static String ROLES_ATTR_NAME = "rolesList";

	/**
	 * Define Admin and non - admin roles
	 */
    @Deprecated
    private static String[] adminRoles = new String[] { 
    	Roles.ADMIN_ROLE };
    
    @Deprecated
    private static String[] nonAdminRoles = new String[] { 
    	Roles.DEPT_SCHED_MGR_ROLE, 
    	Roles.VIEW_ALL_ROLE, 
    	Roles.EXAM_MGR_ROLE,
    	Roles.EVENT_MGR_ROLE };
    
    /**
     * Retrieve admin roles
     * @return String Array of admin roles (defined in Roles class) 
     * @see Roles
     */
    @Deprecated
	public static String[] getAdminRoles() {
	    return adminRoles;
	}
	
    /**
     * Retrieve non-admin roles
     * @return String Array of admin roles (defined in Roles class) 
     * @see Roles
     */
    @Deprecated
	public static String[] getNonAdminRoles() {
	    return nonAdminRoles;
	}

    /** Roles List **/
    private static Vector rolesList = null;
    
	/**
	 * Retrieves all roles in the database
	 * ordered by column label
	 * @param refresh true - refreshes the list from database
	 * @return Vector of Roles objects
	 */
    public static synchronized Vector getRolesList(boolean refresh) {
        
        if(rolesList!=null && !refresh)
            return rolesList;
        

        RolesDAO rdao = new RolesDAO();

        List l = rdao.findAll(Order.asc("abbv"));
        rolesList = new Vector(l);
        return rolesList;
    }
    
    /**
     * Get icon file name corresponding to role
     * @param roleRef
     * @return icon file name
     */
    public static String getRoleIcon(String roleRef) {
        if (roleRef.equals(Roles.ADMIN_ROLE))
            return "admin-icon.gif";
        if (roleRef.equals(Roles.DEPT_SCHED_MGR_ROLE))
            return "dept-mgr-icon.gif";
        if (roleRef.equals(Roles.VIEW_ALL_ROLE))
            return "view-all-icon.gif";
        
        return "other-role-icon.gif";
                
    }
    
    public static Roles getRole(String roleRef) {
        for (Enumeration e=getRolesList(false).elements();e.hasMoreElements();) {
            Roles role = (Roles)e.nextElement();
            if (roleRef.equals(role.getReference())) return role;
        }
        return null;
    }

	@Override
	//TODO: get this information from the database
	public boolean hasRight(Right right) {
		switch (right) {
		/* session defaults */
		case SessionDefaultFirstFuture:
			return DEPT_SCHED_MGR_ROLE.equals(getReference());
		case SessionDefaultFirstExamination:
			return EXAM_MGR_ROLE.equals(getReference());
		case SessionDefaultCurrent:
			return !DEPT_SCHED_MGR_ROLE.equals(getReference()) && !EXAM_MGR_ROLE.equals(getReference());
		
		/* session / department / status dependency */
		case SessionIndependent:
			return ADMIN_ROLE.equals(getReference());
		case SessionIndependentIfNoSessionGiven:
			return VIEW_ALL_ROLE.equals(getReference());
		case AllowTestSessions:
			return ADMIN_ROLE.equals(getReference());
		case DepartmentIndependent:
			return !DEPT_SCHED_MGR_ROLE.equals(getReference()) && !EVENT_MGR_ROLE.equals(getReference());
		case StatusIndependent:
			return ADMIN_ROLE.equals(getReference());
			
		case AddNonUnivLocation:
		case AddSpecialUseRoom:
		case AddSpecialUseRoomExternalRoom:
		case NonUniversityLocationDelete:
			return ADMIN_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference()) || EVENT_MGR_ROLE.endsWith(getReference());
			
		case RoomDelete: 
		case ApplicationConfig:
			return ADMIN_ROLE.equals(getReference());
			
		case CourseTimetabling:
		case CourseTimetablingAudit:
		case Timetables:
		case TimetablesSolutionCommit:
		case TimetablesSolutionDelete:
		case TimetablesSolutionLoad:
		case TimetablesSolutionLoadEmpty:
		case TimetablesSolutionChangeNote:
		case TimetablesSolutionExportCsv:
		case Solver:
		case SolverSolutionSave:
		case SolverSolutionExportCsv:
		case SolverLog:
		case TimetableGrid:
		case AssignedClasses:
		case NotAssignedClasses:
		case AssignmentHistory:
		case ConflictStatistics:
		case SolutionReports:
		case AddCourseOffering:
		case ClassEdit:
		case ClassEditClearPreferences:
		case OfferingCanLock:
		case OfferingCanUnlock:
		case AssignInstructors:
		case AssignInstructorsClass:
		case MultipleClassSetup:
		case MultipleClassSetupClass:
		case MultipleClassSetupDepartment:
		case InstrOfferingConfigAdd:
		case InstrOfferingConfigDelete:
		case InstrOfferingConfigEdit:
		case InstrOfferingConfigEditDepartment:
		case InstrOfferingConfigEditSubpart:
		case AddReservation:
		case EditCourseOffering:
		case InstructionalOfferingCrossLists:
		case OfferingMakeNotOffered:
		case OfferingMakeOffered:
		case Reservations:
		case SchedulingSubpartEdit:
		case SchedulingSubpartEditClearPreferences:
		case SchedulingSubpartDetailClearClassPreferences:
		case InstructorPreferences:
		case InstructorDelete:
		case InstructorEdit:
		case InstructorAdd:
		case ManageInstructors:
		case InstructorEditClearPreferences: 
		case SolutionChanges:
		case Suggestions:
		case DistributionPreferenceClass:
		case DistributionPreferenceSubpart:
		case DistributionPreferenceAdd:
		case DistributionPreferenceEdit:
		case DistributionPreferenceDelete:
			return ADMIN_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference());
			
		case SolverSolutionExportXml:
		case ManageSolvers:
			return ADMIN_ROLE.equals(getReference());
			
		case ExaminationTimetabling:
		case ExaminationSolver:
		case ExaminationTimetable:
		case AssignedExaminations:
		case NotAssignedExaminations:
		case ExaminationAssignmentChanges:
		case ExaminationConflictStatistics:
		case ExaminationSolverLog:
		case ExaminationReports:
		case ExaminationAssignment:
			return ADMIN_ROLE.equals(getReference()) || EXAM_MGR_ROLE.equals(getReference());
			
		case ExaminationPdfReports:
			return ADMIN_ROLE.equals(getReference()) || EXAM_MGR_ROLE.equals(getReference()) || VIEW_ALL_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference());
			
		case InstructionalOfferings:
		case InstructionalOfferingsExportPDF:
		case InstructionalOfferingsWorksheetPDF:
		case InstructionalOfferingDetail:
		case Classes:
		case ClassesExportPDF:
		case SchedulingSubpartDetail:
		case ClassDetail:
		case DistributionPreferences:
		case DistributionPreferenceDetail:
			return ADMIN_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference()) || VIEW_ALL_ROLE.equals(getReference());
		
		case ClassAssignments:
		case ClassAssignmentsExportCsv:
		case ClassAssignmentsExportPdf:
		case Examinations:
		case ExaminationDetail:
		case InstructorDetail:
		case Instructors:
		case InstructorsExportPdf:
			return ADMIN_ROLE.equals(getReference()) || EXAM_MGR_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference()) || VIEW_ALL_ROLE.equals(getReference());
			
		case ExaminationAdd:
		case ExaminationEdit:
		case ExaminationEditClearPreferences:
		case ExaminationDelete:
		case ExaminationClone:
		case DistributionPreferenceExam:
		case ExaminationDistributionPreferenceAdd:
		case ExaminationDistributionPreferenceEdit:
		case ExaminationDistributionPreferenceDelete:
			return ADMIN_ROLE.equals(getReference()) || EXAM_MGR_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference());
			
		case RoomAvailability:
		case ExaminationDistributionPreferences:
		case ExaminationDistributionPreferenceDetail:
			return ADMIN_ROLE.equals(getReference()) || CURRICULUM_MGR_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference()) || VIEW_ALL_ROLE.equals(getReference());
			
		case OfferingDelete:
		case ClassAssignment:
		case Registration:
		case Chameleon:
			return ADMIN_ROLE.equals(getReference());
			
		case PersonalSchedule:
		case PersonalScheduleLookup:
			return ADMIN_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference()) || STUDENT_ADVISOR.equals(getReference());
			
		/* curriculum rights */
		case CurriculumAdd:
		case CurriculumEdit:
		case CurriculumDelete:
			return ADMIN_ROLE.equals(getReference()) || CURRICULUM_MGR_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference());
		case CurriculumDetail:
		case CurriculumView:
			return ADMIN_ROLE.equals(getReference()) || CURRICULUM_MGR_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference()) || VIEW_ALL_ROLE.equals(getReference());
		case CurriculumMerge:
			return ADMIN_ROLE.equals(getReference()) || CURRICULUM_MGR_ROLE.equals(getReference());
		case CurriculumAdmin:
			return ADMIN_ROLE.equals(getReference());
			
		case ExaminationSchedule:
		case CanUseHardDistributionPrefs:
		case CanUseHardPeriodPrefs:
		case CanUseHardRoomPrefs:
		case CanUseHardTimePrefs:
			return true;
			
		case Rooms:
		case RoomsExportCsv:
		case RoomsExportPdf:
		case RoomFeatures:
		case RoomFeaturesExportPdf:
		case RoomGroups:
		case RoomGroupsExportPdf:
		case RoomDetail:
			return ADMIN_ROLE.equals(getReference()) || EXAM_MGR_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference()) || VIEW_ALL_ROLE.equals(getReference()) || EVENT_MGR_ROLE.equals(getReference());
			
		case RoomDetailAvailability:
			return ADMIN_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference()) || VIEW_ALL_ROLE.equals(getReference());

		case RoomDetailPeriodPreferences:
			return ADMIN_ROLE.equals(getReference()) || EXAM_MGR_ROLE.equals(getReference()) || VIEW_ALL_ROLE.equals(getReference());

		case RoomDepartments:
		case EditRoomDepartments:
		case RoomEditAvailability:
		case RoomEditPreference:
			return ADMIN_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference());
		case EditRoomDepartmentsFinalExams:
		case EditRoomDepartmentsMidtermExams:
			return ADMIN_ROLE.equals(getReference()) || EXAM_MGR_ROLE.equals(getReference());
		case RoomEditFeatures:
		case RoomEditGroups:
			return ADMIN_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference()) || EXAM_MGR_ROLE.equals(getReference());
		case DepartmentRoomFeatureAdd:
		case DepartmenalRoomFeatureEdit:
		case DepartmenalRoomFeatureDelete:
		case DepartmentRoomGroupAdd:
		case DepartmenalRoomGroupEdit:
		case DepartmenalRoomGroupDelete:
			return ADMIN_ROLE.equals(getReference()) || DEPT_SCHED_MGR_ROLE.equals(getReference());
		case RoomEditGlobalFeatures:
		case RoomEditGlobalGroups:
		case GlobalRoomFeatureAdd:
		case GlobalRoomFeatureEdit:
		case GlobalRoomFeatureDelete:
		case GlobalRoomGroupAdd:
		case GlobalRoomGroupEdit:
		case GlobalRoomGroupDelete:
			return ADMIN_ROLE.equals(getReference()) || EXAM_MGR_ROLE.equals(getReference());
			
		case AddRoom:
		case RoomEdit:
		case RoomEditChangeControll:
		case GlobalRoomGroupEditSetDefault:
		case BuildingList:
		case BuildingAdd:
		case BuildingEdit:
		case BuildingDelete:
		case BuildingExportPdf:
		case BuildingUpdateData:
		case TravelTimesLoad:
		case TravelTimesSave:
		case DatePatterns:
		case SessionRollForward:
		case DataExchange:
		case CanSelectSolverServer:
		case ExtendedDatePatterns:
		case ExtendedTimePatterns: 
		case ExaminationPeriods:
		case Departments:
		case DepartmentAdd:
		case DepartmentEdit:
		case DepartmentEditChangeExternalManager:
		case DepartmentDelete:
		case AcademicSessions:
		case AcademicSessionAdd:
		case AcademicSessionDelete:
		case AcademicSessionEdit:
		case TimetableManagers:
		case TimetableManagerAdd:
		case TimetableManagerEdit:
		case TimetableManagerDelete:
		case ExactTimes:
		case StandardEventNotes: 
		case StatusTypes:
		case TestHQL:
		case SolverGroups:
		case SubjectAreas:
		case SubjectAreaAdd:
		case SubjectAreaEdit:
		case SubjectAreaChangeDepartment:
		case SubjectAreaDelete:
		case SettingsAdmin:
		case TimePatterns:
		case LastChanges:
		case InstructionalTypes:
		case InstructionalTypeAdd:
		case InstructionalTypeEdit:
		case InstructionalTypeDelete:
		case RoomTypes:
		case SponsoringOrganizations:
		case SponsoringOrganizationAdd:
		case SponsoringOrganizationEdit:
		case SponsoringOrganizationDelete:
			return ADMIN_ROLE.equals(getReference());
			
		case HasRole:
		case Inquiry:
		case SettingsUser:
			return true;

		case IsAdmin:
			return ADMIN_ROLE.equals(getReference());

		default:
			
			return false;
		}
	}
}
