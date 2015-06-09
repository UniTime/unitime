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
package org.unitime.timetable.security.rights;

import java.util.Date;

import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;

/**
 * @author Tomas Muller
 */
public enum Right {
	/** Session default: current session */
	SessionDefaultCurrent, // -- DEFAULT SESSION SELECTION
	/** Session default: first future session */
	SessionDefaultFirstFuture,
	/** Session default: first examination session */
	SessionDefaultFirstExamination,
	
	/** Session dependency -- if independent the role applies to all academic session */
	SessionIndependent,
	SessionIndependentIfNoSessionGiven,
	/** Session dependency -- test sessions are allowed */
	AllowTestSessions,
	
	/** Department dependency -- department must match */
	DepartmentIndependent,
	
	/** Status dependency -- session / department status must match */
	StatusIndependent,
	
	/** For some old (backward compatible) checks */
	HasRole,
	IsAdmin,
	
	/** Can register UniTime */
    Registration,

	/** Individual page rights: Courses Input Data */
	
	InstructionalOfferings(Department.class),
	InstructionalOfferingsExportPDF(Department.class),
	InstructionalOfferingsWorksheetPDF(Department.class),

	Classes(Department.class),
	ClassesExportPDF(Department.class),
	
	ClassAssignments(Session.class),
	ClassAssignmentsExportPdf(Session.class),
	ClassAssignmentsExportCsv(Session.class),

	InstructionalOfferingDetail(InstructionalOffering.class),
	
	AddCourseOffering(SubjectArea.class),
	EditCourseOffering(CourseOffering.class),
	EditCourseOfferingNote(CourseOffering.class),
	EditCourseOfferingCoordinators(CourseOffering.class),
	
	OfferingCanLock(InstructionalOffering.class),
	OfferingCanUnlock(InstructionalOffering.class),
	OfferingMakeNotOffered(InstructionalOffering.class),
	OfferingMakeOffered(InstructionalOffering.class),
	OfferingDelete(InstructionalOffering.class),
	
	InstructionalOfferingCrossLists(InstructionalOffering.class),

	InstrOfferingConfigAdd(InstructionalOffering.class),
	InstrOfferingConfigEdit(InstrOfferingConfig.class),
	InstrOfferingConfigEditDepartment(Department.class),
	InstrOfferingConfigEditSubpart(SchedulingSubpart.class),
	InstrOfferingConfigDelete(InstrOfferingConfig.class),

	MultipleClassSetup(InstrOfferingConfig.class),
	MultipleClassSetupDepartment(Department.class),
	MultipleClassSetupClass(Class_.class),
	
	AssignInstructors(InstrOfferingConfig.class),
	AssignInstructorsClass(Class_.class),

	SchedulingSubpartDetail(SchedulingSubpart.class),
	SchedulingSubpartDetailClearClassPreferences(SchedulingSubpart.class),
	SchedulingSubpartEdit(SchedulingSubpart.class),
	SchedulingSubpartEditClearPreferences(SchedulingSubpart.class),

	ClassDetail(Class_.class),
	ClassEdit(Class_.class),
	ClassEditClearPreferences(Class_.class),
	
    ExtendedDatePatterns,
    ExtendedTimePatterns,

    CanUseHardTimePrefs(PreferenceGroup.class),
    CanUseHardRoomPrefs(PreferenceGroup.class),
    CanUseHardDistributionPrefs(PreferenceGroup.class),
    CanUseHardPeriodPrefs(PreferenceGroup.class),
    
	ClassAssignment(Class_.class),

	CurriculumView(Session.class),
    CurriculumDetail(Curriculum.class),
    CurriculumAdd(Department.class),
    CurriculumEdit(Curriculum.class),
    CurriculumDelete(Curriculum.class),
    CurriculumMerge(Curriculum.class),
    CurriculumAdmin(Session.class),
    CurriculumProjectionRulesDetail(Session.class),
    CurriculumProjectionRulesEdit(Session.class),
    
    Instructors(Department.class),
    InstructorsExportPdf(Department.class),
    ManageInstructors(Department.class),
    InstructorDetail(DepartmentalInstructor.class),
    InstructorAdd(Department.class),
    InstructorEdit(DepartmentalInstructor.class),
    InstructorEditClearPreferences(DepartmentalInstructor.class),
    InstructorDelete(DepartmentalInstructor.class),
    InstructorPreferences(DepartmentalInstructor.class),
    
	Rooms(Department.class),
	RoomsExportPdf(Department.class),
	RoomsExportCsv(Department.class),
	RoomDetail(Location.class),
	RoomEdit(Room.class),
	RoomEditChangeControll(Location.class),
	RoomEditChangeExternalId(Location.class),
	RoomEditChangeType(Location.class),
	RoomEditChangeCapacity(Location.class),
	RoomEditChangeExaminationStatus(Location.class),
	RoomEditChangeRoomProperties(Location.class),
	RoomEditChangeEventProperties(Location.class),
	RoomEditChangePicture(Location.class),
	RoomAvailability(Session.class),
	RoomDepartments(Department.class),
	EditRoomDepartments(Department.class),
	EditRoomDepartmentsExams(Session.class),
	AddRoom(Department.class),
	AddSpecialUseRoom(Department.class),
	AddSpecialUseRoomExternalRoom(ExternalRoom.class),
    RoomDelete(Room.class),
	RoomDetailAvailability(Location.class),
	RoomDetailPeriodPreferences(Location.class),
	RoomDetailEventAvailability(Location.class),
	RoomEditAvailability(Location.class),
	RoomEditPreference(Location.class),
	RoomEditGroups(Location.class),
	RoomEditGlobalGroups(Location.class),
	RoomEditFeatures(Location.class),
	RoomEditGlobalFeatures(Location.class),
	RoomEditEventAvailability(Location.class),
	AddNonUnivLocation(Department.class),
	NonUniversityLocationEdit(NonUniversityLocation.class),
    NonUniversityLocationDelete(NonUniversityLocation.class),
	
	RoomFeatures(Department.class),
	RoomFeaturesExportPdf(Department.class),
	GlobalRoomFeatureAdd(Session.class),
	DepartmentRoomFeatureAdd(Department.class),
	DepartmenalRoomFeatureEdit(DepartmentRoomFeature.class),
	GlobalRoomFeatureEdit(GlobalRoomFeature.class),
	DepartmenalRoomFeatureDelete(DepartmentRoomFeature.class),
	GlobalRoomFeatureDelete(GlobalRoomFeature.class),
	
	RoomGroups(Department.class),
	RoomGroupsExportPdf(Department.class),
	GlobalRoomGroupAdd(Session.class),
	DepartmentRoomGroupAdd(Department.class),
	DepartmenalRoomGroupEdit(RoomGroup.class),
	GlobalRoomGroupEdit(RoomGroup.class),
	GlobalRoomGroupEditSetDefault(RoomGroup.class),
	DepartmenalRoomGroupDelete(RoomGroup.class),
	GlobalRoomGroupDelete(RoomGroup.class),
    
    TravelTimesLoad(Session.class),
    TravelTimesSave(Session.class),
	
	DistributionPreferences(Department.class),
	DistributionPreferenceAdd(Department.class),
	DistributionPreferenceDetail(DistributionPref.class),
	DistributionPreferenceEdit(DistributionPref.class),
	DistributionPreferenceDelete(DistributionPref.class),
	DistributionPreferenceClass(Class_.class),
	DistributionPreferenceSubpart(SchedulingSubpart.class),
	DistributionPreferenceExam(Exam.class),

	Reservations(Department.class),
	ReservationOffering(InstructionalOffering.class),
	ReservationAdd(Department.class),
	ReservationEdit(Reservation.class),
	ReservationDelete(Reservation.class),
	
	/** Individual page rights: Course Timetabling */
	
	CourseTimetabling(SolverGroup.class),
	CourseTimetablingAudit(SolverGroup.class),
	
	Timetables(SolverGroup.class),
	TimetablesSolutionExportCsv(Solution.class),
	TimetablesSolutionChangeNote(Solution.class),
	TimetablesSolutionCommit(SolverGroup.class),
	TimetablesSolutionLoad(Solution.class),
	TimetablesSolutionLoadEmpty(SolverGroup.class),
	TimetablesSolutionDelete(Solution.class),
	Solver(SolverGroup.class),
	SolverSolutionSave(SolverGroup.class),
	SolverSolutionExportCsv(SolverGroup.class),
	SolverSolutionExportXml(SolverGroup.class),
    CanSelectSolverServer,
	Suggestions(SolverGroup.class),
	TimetableGrid(SolverGroup.class),
	AssignedClasses(SolverGroup.class),
	NotAssignedClasses(SolverGroup.class),
	SolutionChanges(SolverGroup.class),
	AssignmentHistory(SolverGroup.class),
	ConflictStatistics(SolverGroup.class),
	SolverLog(SolverGroup.class),
	SolutionReports(SolverGroup.class),
	
	/** Individual page rights: Examinations */
	
	Examinations(Session.class),

    ExaminationDetail(Exam.class),
    ExaminationEdit(Exam.class),
    ExaminationEditClearPreferences(Exam.class),
    ExaminationDelete(Exam.class),
    ExaminationClone(Exam.class),
    ExaminationAdd(Session.class),
    ExaminationAssignment(Exam.class),
    
    ExaminationDistributionPreferences(Session.class),
    ExaminationDistributionPreferenceAdd(Session.class),
    ExaminationDistributionPreferenceDetail(DistributionPref.class),
    ExaminationDistributionPreferenceEdit(DistributionPref.class),
    ExaminationDistributionPreferenceDelete(DistributionPref.class),
    
	ExaminationSchedule(Session.class),

	/** Individual page rights: Examination Timetabling */

	ExaminationTimetabling(Session.class),
	ExaminationSolver(Session.class),
	ExaminationTimetable(Session.class),
	AssignedExaminations(Session.class),
	NotAssignedExaminations(Session.class),
	ExaminationAssignmentChanges(Session.class),
	ExaminationConflictStatistics(Session.class),
	ExaminationSolverLog(Session.class),
	ExaminationReports(Session.class),
	ExaminationPdfReports(Session.class),
	ExaminationSolutionExportXml(Session.class),
	
	/** Individual page rights: Students Scheduling */
	
    StudentScheduling(Session.class),
    EnrollmentAuditPDFReports(Session.class),
    StudentSectioningSolver(Session.class),
    StudentSectioningSolverLog(Session.class),
    StudentSectioningSolverDashboard(Session.class),
    StudentSectioningSolutionExportXml(Session.class),
    
    /** Individual page rights: Online Students Scheduling */

    CourseRequests(Session.class),
    SchedulingAssistant(Session.class),
    SchedulingDashboard(Session.class),

    ConsentApproval(CourseOffering.class),
    StudentSchedulingAdvisor,
    StudentSchedulingAdmin,
    StudentSchedulingMassCancel(Session.class),
    StudentSchedulingEmailStudent(Session.class),
    StudentSchedulingChangeStudentStatus(Session.class),
    StudentSchedulingRequestStudentUpdate(Session.class),
    
    OfferingEnrollments(InstructionalOffering.class),
    StudentEnrollments(Student.class),
    EnrollmentsShowExternalId,

    /** Individual page rights: Events */
    
    Events(Session.class),
    EventAddSpecial(Session.class),
    EventAddCourseRelated(Session.class),
    EventAddUnavailable(Session.class),
    EventLookupContact,
    EventLookupContactAdditional,
    EventLookupSchedule,
    EventDetail(Event.class),
    EventEdit(Event.class),
    EventEditClass(ClassEvent.class),
    EventEditExam(ExamEvent.class),
    EventDate(Date.class),
    EventLocation(Location.class),
    EventLocationApprove(Location.class),
    EventLocationOverbook(Location.class),
    EventLocationUnavailable(Location.class),
    EventMeetingEdit(Meeting.class),
    EventMeetingDelete(Meeting.class),
    EventMeetingInquire(Meeting.class),
    EventMeetingInquireClass(Meeting.class),
    EventMeetingInquireExam(Meeting.class),
    EventMeetingApprove(Meeting.class),
    EventMeetingCancel(Meeting.class),
    EventMeetingCancelClass(Meeting.class),
    EventMeetingCancelExam(Meeting.class),
    EventApprovePast,
    EventAnyLocation,
    EventEditPast,
    EventSetExpiration,
    EventCanEditAcademicTitle,
    
    /** Administration: Academic Sessions */
    
    AcademicSessions,
    AcademicSessionAdd,
    AcademicSessionEdit(Session.class),
    AcademicSessionDelete(Session.class),
    
    TimetableManagers,
    TimetableManagerAdd,
    TimetableManagerEdit(TimetableManager.class),
    TimetableManagerDelete(TimetableManager.class),

    Departments(Session.class),
    DepartmentAdd(Session.class),
    DepartmentEdit(Department.class),
    DepartmentEditChangeExternalManager(Department.class),
    DepartmentDelete(Department.class),

    SolverGroups(Session.class),
    
    SubjectAreas(Session.class),
    SubjectAreaAdd(Session.class),
    SubjectAreaEdit(SubjectArea.class),
    SubjectAreaDelete(SubjectArea.class),
    SubjectAreaChangeDepartment(SubjectArea.class),

    BuildingList(Session.class),
    BuildingAdd(Session.class),
    BuildingEdit(Building.class),
    BuildingDelete(Building.class),
    BuildingUpdateData(Session.class),
    BuildingExportPdf(Session.class),

    DatePatterns(Session.class),

    TimePatterns(Session.class),

    ExactTimes,

    AcademicAreas(Session.class),
    AcademicAreaEdit(Session.class),
    
    AcademicClassifications(Session.class),
    AcademicClassificationEdit(Session.class),

    Majors(Session.class),
    MajorEdit(Session.class),

    Minors(Session.class),
    MinorEdit(Session.class),
    
    StudentGroups(Session.class),
    StudentGroupEdit(Session.class),
    
    StudentAccommodations(Session.class),
    StudentAccommodationEdit(Session.class),
    
    ExaminationPeriods(Session.class),

    DataExchange(Session.class),

    SessionRollForward,

    LastChanges(Session.class),
    
    /** Administration: Solver */
    
	ManageSolvers(Session.class),

	SolverParameterGroups,
    SolverParameters,
    SolverConfigurations,
    DistributionTypes,
    DistributionTypeEdit(Session.class),

    /** Administration: Other */
    
    InstructionalTypes,
    InstructionalTypeAdd,
    InstructionalTypeEdit(ItypeDesc.class),
    InstructionalTypeDelete(ItypeDesc.class),

    StatusTypes,

    RoomTypes,

    SponsoringOrganizations,
    SponsoringOrganizationAdd,
    SponsoringOrganizationEdit(SponsoringOrganization.class),
    SponsoringOrganizationDelete(SponsoringOrganization.class),

    StandardEventNotes,
    StandardEventNotesGlobalEdit,
    StandardEventNotesSessionEdit(Session.class),
    StandardEventNotesDepartmentEdit(Department.class),

    Users,
    ChangePassword(Session.class),
    
    OfferingConsentTypes,
    OfferingConsentTypeEdit,

    CourseCreditFormats,
    CourseCreditFormatEdit,

    CourseCreditTypes,
	CourseCreditTypeEdit,

	CourseCreditUnits,
	CourseCreditUnitEdit,

	PositionTypes,
	PositionTypeEdit,

	StudentSchedulingStatusTypes,
	StudentSchedulingStatusTypeEdit,
    
	Roles,
	RoleEdit,

	Permissions,
	PermissionEdit,
	
	ExamTypes,
	ExamTypeEdit,
	
	EventStatuses(Department.class),
	EventStatusEdit(Department.class),
	
	RoomFeatureTypes,
	RoomFeatureTypeEdit,
	
	InstructorRoles(Department.class),
	InstructorRoleEdit(Department.class),
	
	EventDateMappings(Session.class),
	EventDateMappingEdit(Session.class),
	
	CourseTypes,
	CourseTypeEdit,
	
	EventDefaults,
	EventDefaultsEditOther,
	
	/** Administration: Defaults */
	
	ApplicationConfig,
	ApplicationConfigEdit,
	
    SettingsAdmin,

    /** Administration: Utilities */
    
    PageStatistics,

    HibernateStatistics,
    
    TestHQL,
    ClearHibernateCache,
    
    /** Preferences */

	Chameleon(Session.class),
    SettingsUser,
    
    /** Help */
    
    Inquiry,

    /** Other */
	
	PersonalSchedule(Session.class),
	PersonalScheduleLookup(Session.class),
	
	/** Reports */
	
    HQLReports(Session.class),
    HQLReportAdd(Session.class),
    HQLReportEdit(SavedHQL.class),
    HQLReportDelete(SavedHQL.class),
    HQLReportsCourses(Session.class),
    HQLReportsExaminations(Session.class),
    HQLReportsStudents(Session.class),
    HQLReportsEvents(Session.class),
    HQLReportsAdministration(Session.class),
    HQLReportsAdminOnly(Session.class),
    
    /** Scripting */
    
    Scripts,
    ScriptEdit,
    
    /** API */
    ApiRetrieveEvents(Session.class),
    ApiRetrieveRooms(Session.class),
    ApiJsonConnector,
    ApiDataExchangeConnector,

	/** Obsolete */
    
	SolutionInformationDefinitions,

	;
	
	private Class<?> iType;
	Right(Class<?> type) { iType = type; }
	Right() { this(null); }
	
	public Class<?> type() { return iType; }
	public boolean hasType() { return iType != null; }
	@Override
	public String toString() { return name().replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2"); }
}
