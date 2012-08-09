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
package org.unitime.timetable.security.rights;

import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;

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
	
	Chameleon(Session.class),
	PersonalSchedule(Session.class),
	PersonalScheduleLookup(Session.class),
	
	OfferingCanLock(InstructionalOffering.class),
	OfferingCanUnlock(InstructionalOffering.class),
	OfferingMakeNotOffered(InstructionalOffering.class),
	OfferingMakeOffered(InstructionalOffering.class),
	OfferingDelete(InstructionalOffering.class),
	InstrOfferingConfigAdd(InstructionalOffering.class),
	InstrOfferingConfigEdit(InstrOfferingConfig.class),
	InstrOfferingConfigEditDepartment(Department.class),
	InstrOfferingConfigEditSubpart(SchedulingSubpart.class),
	InstrOfferingConfigDelete(InstrOfferingConfig.class),
	InstructionalOfferingCrossLists(InstructionalOffering.class),
	AddReservation(InstructionalOffering.class),
	
	Reservations(Department.class),

	InstructionalOfferings(Department.class),
	InstructionalOfferingsExportPDF(Department.class),
	InstructionalOfferingsWorksheetPDF(Department.class),
	
	EditCourseOffering(CourseOffering.class),
	
	Classes(Department.class),
	ClassesExportPDF(Department.class),
	
	InstructionalOfferingDetail(InstructionalOffering.class),
	MultipleClassSetup(InstrOfferingConfig.class),
	MultipleClassSetupDepartment(Department.class),
	MultipleClassSetupClass(Class_.class),
	
	AssignInstructors(InstrOfferingConfig.class),
	AssignInstructorsClass(Class_.class),
	
	CourseTimetabling(SolverGroup.class),
	CourseTimetablingAudit(SolverGroup.class),
	ClassAssignments(Session.class),
	ClassAssignmentsExportPdf(Session.class),
	ClassAssignmentsExportCsv(Session.class),
	
	Examinations(Session.class),
	ExaminationSchedule(Session.class),
	AddCourseOffering(SubjectArea.class),
	
	AddNonUnivLocation(Department.class),
	AddSpecialUseRoom(Department.class),
	AddSpecialUseRoomExternalRoom(ExternalRoom.class),
	ApplicationConfig,
	
	
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
	TimetableGrid(SolverGroup.class),
	AssignedClasses(SolverGroup.class),
	NotAssignedClasses(SolverGroup.class),
	SolutionChanges(SolverGroup.class),
	AssignmentHistory(SolverGroup.class),
	Suggestions(SolverGroup.class),
	ConflictStatistics(SolverGroup.class),
	SolverLog(SolverGroup.class),
	SolutionReports(SolverGroup.class),
	ManageSolvers(Session.class),
	
	/** Class level rights */
	ClassDetail(Class_.class),
	ClassEdit(Class_.class),
	ClassEditClearPreferences(Class_.class),
	ClassAssignment(Class_.class),
	
	SchedulingSubpartDetail(SchedulingSubpart.class),
	SchedulingSubpartDetailClearClassPreferences(SchedulingSubpart.class),
	SchedulingSubpartEdit(SchedulingSubpart.class),
	SchedulingSubpartEditClearPreferences(SchedulingSubpart.class),
	
	DistributionPreferences(Department.class),
	DistributionPreferenceAdd(Department.class),
	DistributionPreferenceDetail(DistributionPref.class),
	DistributionPreferenceEdit(DistributionPref.class),
	DistributionPreferenceDelete(DistributionPref.class),
	DistributionPreferenceClass(Class_.class),
	DistributionPreferenceSubpart(SchedulingSubpart.class),
	DistributionPreferenceExam(Exam.class),

	/** Curriculum rights */
    CurriculumView(Session.class),
    CurriculumDetail(Curriculum.class),
    CurriculumAdd(Department.class),
    CurriculumEdit(Curriculum.class),
    CurriculumDelete(Curriculum.class),
    CurriculumMerge(Curriculum.class),
    CurriculumAdmin(Session.class),
    
    Instructors(Department.class),
    InstructorsExportPdf(Department.class),
    ManageInstructors(Department.class),
    InstructorDetail(DepartmentalInstructor.class),
    InstructorAdd(Department.class),
    InstructorEdit(DepartmentalInstructor.class),
    InstructorEditClearPreferences(DepartmentalInstructor.class),
    InstructorDelete(DepartmentalInstructor.class),
    InstructorPreferences(DepartmentalInstructor.class),
    
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
	
	Rooms(Department.class),
	RoomsExportPdf(Department.class),
	RoomsExportCsv(Department.class),
	RoomDetail(Location.class),
	RoomEdit(Location.class),
	RoomEditChangeControll(Location.class),
	RoomAvailability(Session.class),
	RoomDepartments(Department.class),
	EditRoomDepartments(Department.class),
	EditRoomDepartmentsFinalExams(Session.class),
	EditRoomDepartmentsMidtermExams(Session.class),
	AddRoom(Department.class),
	RoomDetailAvailability(Location.class),
	RoomDetailPeriodPreferences(Location.class),
	RoomEditAvailability(Location.class),
	RoomEditPreference(Location.class),
	RoomEditGroups(Location.class),
	RoomEditGlobalGroups(Location.class),
	RoomEditFeatures(Location.class),
	RoomEditGlobalFeatures(Location.class),
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

    
    CanUseHardTimePrefs(PreferenceGroup.class),
    CanUseHardRoomPrefs(PreferenceGroup.class),
    CanUseHardDistributionPrefs(PreferenceGroup.class),
    CanUseHardPeriodPrefs(PreferenceGroup.class),
    
    BuildingList(Session.class),
    BuildingAdd(Session.class),
    BuildingEdit(Building.class),
    BuildingDelete(Building.class),
    BuildingUpdateData(Session.class),
    BuildingExportPdf(Session.class),
    
    RoomDelete(Room.class),
    NonUniversityLocationDelete(NonUniversityLocation.class),
    
    TravelTimesLoad(Session.class),
    TravelTimesSave(Session.class),
    
    DatePatterns(Session.class),
    DataExchange(Session.class),
    ExaminationPeriods(Session.class),
    Departments(Session.class),
    DepartmentAdd(Session.class),
    DepartmentEdit(Department.class),
    DepartmentEditChangeExternalManager(Department.class),
    DepartmentDelete(Department.class),
    AcademicSessions,
    AcademicSessionAdd,
    AcademicSessionEdit(Session.class),
    AcademicSessionDelete(Session.class),
    TimetableManagers,
    TimetableManagerAdd,
    TimetableManagerEdit(TimetableManager.class),
    TimetableManagerDelete(TimetableManager.class),
    ExactTimes,
    StandardEventNotes,
    TestHQL,
    StatusTypes,
    SolverGroups(Session.class),
    SubjectAreas(Session.class),
    SubjectAreaAdd(Session.class),
    SubjectAreaEdit(SubjectArea.class),
    SubjectAreaDelete(SubjectArea.class),
    SubjectAreaChangeDepartment(SubjectArea.class),
    TimePatterns(Session.class),
    
    Registration,
    ExtendedTimePatterns,
    ExtendedDatePatterns,
    SessionRollForward,
    CanSelectSolverServer,
    Inquiry,
    SettingsUser,
    SettingsAdmin,

    ;
	
	private Class<?> iType;
	Right(Class<?> type) { iType = type; }
	Right() { this(null); }
	
	public Class<?> type() { return iType; }
	public boolean hasType() { return iType != null; }
	@Override
	public String toString() { return name().replaceAll("(\\p{Ll})(\\p{Lu})","$1 $2"); }
}
