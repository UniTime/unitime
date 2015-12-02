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
package org.unitime.localization.messages;

import org.unitime.timetable.action.ClassSearchAction;
import org.unitime.timetable.action.InstructionalOfferingSearchAction;


/**
 * @author Tomas Muller, Zuzana Mullerova
 */
public interface CourseMessages extends Messages {
	
	@DefaultMessage("Filter")
	String filter();
	
	@DefaultMessage("Optional Columns:")
	String filterOptionalColumns();
	
	@DefaultMessage("Manager:")
	String filterManager();
	
	@DefaultMessage("Instructional Type:")
	String filterInstructionalType();

	@DefaultMessage("Instructor:")
	String filterInstructor();
	
	@DefaultMessage("Assigned Time:")
	String filterAssignedTime();
	
	@DefaultMessage("Assigned Room:")
	String filterAssignedRoom();
	
	@DefaultMessage("from")
	String filterTimeFrom();
	
	@DefaultMessage("for")
	String filterTimeFor();
	
	@DefaultMessage("minutes")
	String filterTimeMinutes();
	
	@DefaultMessage("Parent Class:")
	String propertyParentClass();
	
	@DefaultMessage("Parent Scheduling Subpart:")
	String propertyParentSchedulingSubpart();
	
	@DefaultMessage("External Id:")
	String propertyExternalId();
	
	@DefaultMessage("Enrollment:")
	String propertyEnrollment();
	
	@DefaultMessage("Class Limit:")
	String propertyClassLimit();
	
	@DefaultMessage("Minimum Class Limit:")
	String propertyMinimumClassLimit();
	
	@DefaultMessage("Maximum Class Limit:")
	String propertyMaximumClassLimit();
	
	@DefaultMessage("Number of Rooms:")
	String propertyNumberOfRooms();
	
	@DefaultMessage("Room Ratio:")
	String propertyRoomRatio();
	
	@DefaultMessage("Minimum Room Capacity:")
	String propertyMinimumRoomCapacity();
	
	@DefaultMessage("Date Pattern:")
	String propertyDatePattern();
	
	@DefaultMessage("Date:")
	String propertyDate();
	
	@DefaultMessage("Display Instructors:")
	String propertyDisplayInstructors();
	
	@DefaultMessage("Student Scheduling:")
	String propertyEnabledForStudentScheduling();
	
	@DefaultMessage("Student Schedule Note:")
	String propertyStudentScheduleNote();
	
	@DefaultMessage("Requests / Notes:")
	String propertyRequestsNotes();
	
	@DefaultMessage("Instructors:")
	String propertyInstructors();
	
	@DefaultMessage("Instructor:")
	String propertyInstructor();
	
	@DefaultMessage("Time:")
	String propertyTime();
	
	@DefaultMessage("Examination Periods:")
	String propertyExaminationPeriods();
	
	@DefaultMessage("Room Groups:")
	String propertyRoomGroups();
	
	@DefaultMessage("Rooms:")
	String propertyRooms();
	
	@DefaultMessage("Room:")
	String propertyRoom();
	
	@DefaultMessage("Buildings:")
	String propertyBuildings();
	
	@DefaultMessage("Room Features:")
	String propertyRoomFeatures();
	
	@DefaultMessage("Available Rooms:")
	String propertyAvailableRooms();
	
	@DefaultMessage("Distribution:")
	String propertyDistribution();
	
	@DefaultMessage("Initial Assignment:")
	String propertyInitialAssignment();
	
	@DefaultMessage("Student Conflicts:")
	String propertyStudentConflicts();
	
	@DefaultMessage("Violated Constraints:")
	String propertyViolatedConstraints();
	
	@DefaultMessage("Room Locations:")
	String propertyRoomLocations();
	
	@DefaultMessage("Time Locations:")
	String propertyTimeLocations();
	
	@DefaultMessage("Date Patterns:")
	String propertyDatePatterns();
	
	@DefaultMessage("Minimum Room Size:")
	String propertyMinimumRoomSize();
	
	@DefaultMessage("Note:")
	String propertyNote();
	
	@DefaultMessage("Automatic Spread In Time:")
	String propertyAutomaticSpreadInTime();
	
	@DefaultMessage("Student Overlaps:")
	String propertyStudentOverlaps();
	
	@DefaultMessage("Credit:")
	String propertyCredit();
	
	@DefaultMessage("Subpart Credit:")
	String propertySubpartCredit();
	
	@DefaultMessage("Credit Type:")
	String propertyCreditType();
	
	@DefaultMessage("Credit Unit Type:")
	String propertyCreditUnitType();
	
	@DefaultMessage("Units:")
	String propertyUnits();
	
	@DefaultMessage("Max Units:")
	String propertyMaxUnits();
	
	@DefaultMessage("Fractional Increments Allowed:")
	String propertyFractionalIncrementsAllowed();
	
	@DefaultMessage("Configuration Name:")
	String propertyConfigurationName();
	
	@DefaultMessage("Unlimited Enrollment:")
	String propertyUnlimitedEnrollment();
	
	@DefaultMessage("Configuration Limit:")
	String propertyConfigurationLimit();
	
	@DefaultMessage("Course Catalog:")
	String propertyCourseCatalog();
	
	@DefaultMessage("Title:")
	String propertyCourseTitle();
	
	@DefaultMessage("Type:")
	String propertyCourseType();
	
	@DefaultMessage("Schedule of Classes Note:")
	String propertyScheduleOfClassesNote();
	
	@DefaultMessage("Consent:")
	String propertyConsent();
	
	@DefaultMessage("Coordinators:")
	String propertyCoordinators();
	
	@DefaultMessage("Take Course Demands from Offering:")
	String propertyTakeCourseDemandsFromOffering();	
	
	@DefaultMessage("Instructional Offering Limit:")
	String propertyIOLimit();
	
	@DefaultMessage("Course Offerings:")
	String propertyCourseOfferings();
	
	@DefaultMessage("Scheduling Subpart Limits:")
	String propertySchedulingSubpartLimits();
	
	@DefaultMessage("All:") //used in Multiple Class Setup, Assign Instructors
	String propertyAll();

	@DefaultMessage("By Reservation Only:")
	String propertyByReservationOnly();
	
	@DefaultMessage("Last Enrollment:")
	String propertyLastEnrollment();
	
	@DefaultMessage("Projected Demand:")
	String propertyProjectedDemand();
	
	@DefaultMessage("Offering Limit:")
	String propertyOfferingLimit();
	
	@DefaultMessage("Department:")
	String propertyDepartment();
	
	@DefaultMessage("Account Name:")
	String propertyAccountName();
	
	@DefaultMessage("First Name:")
	String propertyFirstName();
	
	@DefaultMessage("Middle Name:")
	String propertyMiddleName();
	
	@DefaultMessage("Last Name:")
	String propertyLastName();
	
	@DefaultMessage("Academic Title:")
	String propertyAcademicTitle();
	
	@DefaultMessage("Email:")
	String propertyEmail();
	
	@DefaultMessage("Position:")
	String propertyInstructorPosition();
	
	@DefaultMessage("Note:")
	String propertyNotes();
	
	@DefaultMessage("Ignore Too Far:")
	String propertyIgnoreTooFar();
	
	@DefaultMessage("Student Accommodations:")
	String propertyAccommodations();
	
	@DefaultMessage("this one")
	String messageThisOne(); //used in getAssignmentTable - if the initial assignment is "this one"
	
//	@DefaultMessage("Parent Class:")
//	String propertyParentClass();
	
	@DefaultMessage("Normal")
	String examSeatingTypeNormal();
	
	@DefaultMessage("Exam")
	String examSeatingTypeExam();
	
	@DefaultMessage("examinations")
	String examinations(); //used in ExamsAction.java, in PdfWebTable getTable
		
	@DefaultMessage("Sort classes only within scheduling subparts")
	String checkSortWithinSubparts();
	
	@DefaultMessage("All")
	String dropDeptAll();
	
	@DefaultMessage("All")
	String dropManagerAll();
	
	@DefaultMessage("Department")
	String dropDeptDepartment();
	
	@DefaultMessage("All")
	String dropITypeAll();
	
	@DefaultMessage("Default")
	String dropDefaultDatePattern();
	
	@DefaultMessage("External Id")
	String columnExternalId();

	@DefaultMessage("Enrollment Information")
	String columnEnrollmentInformation();
	
	@DefaultMessage("Enrollment")
	String columnDemand();
	
	@DefaultMessage("Last Enrollment")
	String columnLastDemand();

	@DefaultMessage("Projected Demand")
	String columnProjectedDemand();

	@DefaultMessage("Limit")
	String columnLimit();

	@DefaultMessage("Room Ratio")
	String columnRoomRatio();
	
	@DefaultMessage("Room<br>Ratio")
	String columnRoomRatioBr();

	@DefaultMessage("Manager")
	String columnManager();

	@DefaultMessage("Date/Time Information")
	String columnDateTimeInformation();

	@DefaultMessage("Date Pattern")
	String columnDatePattern();
	
	@DefaultMessage("Minutes Per Week")
	String columnMinPerWk();
	
	@DefaultMessage("Time Pattern")
	String columnTimePattern();
	
	@DefaultMessage("Preferences")
	String columnPreferences();
	
	@DefaultMessage("Instructor")
	String columnInstructor();
	
	@DefaultMessage("Timetable")
	String columnTimetable();
	
	@DefaultMessage("Catalog Information")
	String columnCatalogInformation();
	
	@DefaultMessage("Title")
	String columnTitle();

	@DefaultMessage("Course Credit")
	String columnOfferingCredit();

	@DefaultMessage("Subpart Credit")
	String columnSubpartCredit();

	@DefaultMessage("Consent")
	String columnConsent();
	
	@DefaultMessage("Catalog")
	String columnCourseCatalog();

	@DefaultMessage("Schedule Print Note")
	String columnSchedulePrintNote();
	
	@DefaultMessage("Student Schedule Note")
	String columnStudentScheduleNote();

	@DefaultMessage("Note to Schedule Manager")
	String columnNote();
	
	@DefaultMessage("Examinations")
	String columnExams();

	@DefaultMessage("Examination")
	String columnExam();
	
	@DefaultMessage("Class")
	String columnClass();

	@DefaultMessage("{0} {1} Examination")
	String tooltipExam(String label, String type);

	@DefaultMessage("Name")
	String columnExamName();

	@DefaultMessage("Period")
	String columnExamPeriod();

	@DefaultMessage("Room")
	String columnExamRoom();
	
	@DefaultMessage("Time")
	String columnTimePref();
	
	@DefaultMessage("Room&nbsp;Group")
	String columnRoomGroupPref();
	
	@DefaultMessage("Bldg")
	String columnBuildingPref();
	
	@DefaultMessage("Room")
	String columnRoomPref();
	
	@DefaultMessage("Features")
	String columnRoomFeaturePref();
	
	@DefaultMessage("Distribution")
	String columnDistributionPref();
	
	@DefaultMessage("Room")
	String columnAllRoomPref();
	
	@DefaultMessage("Time")
	String columnAssignedTime();
	
	@DefaultMessage("Room")
	String columnAssignedRoom();
	
	@DefaultMessage("Room Cap")
	String columnAssignedRoomCapacity();
	
	@DefaultMessage("Name")
	String columnInstructorName();

	@DefaultMessage("% Share")
	String columnInstructorShare();
	
	@DefaultMessage("Check Conflicts")
	String columnInstructorCheckConflicts();
	
	@DefaultMessage("&nbsp;Check<br>Conflicts")
	String columnInstructorCheckConflictsBr();
	
	@DefaultMessage("Position")
	String columnInstructorPosition();
	
	@DefaultMessage("Note")
	String columnInstructorNote();
	
	@DefaultMessage("Class<BR>Assignments")
	String columnInstructorClassAssignments();

	@DefaultMessage("Class\nAssignments")
	String columnInstructorClassAssignmentsPDF(); //has a new line ("\n") for printing out pdf
	
	@DefaultMessage("Exam<BR>Assignments")
	String columnInstructorExamAssignments();

	@DefaultMessage("Exam\nAssignments")
	String columnInstructorExamAssignmentsPDF(); //has a new line ("\n") for printing out pdf
	
	@DefaultMessage("Ignore<BR>Too Far")
	String columnInstructorIgnoreTooFar();
	
	@DefaultMessage("Ignore\nToo Far")
	String columnInstructorIgnoreTooFarPDF();
	
	@DefaultMessage("Classes / Courses")
	String columnExamClassesCourses();
	
	@DefaultMessage("Type")
	String columnExamType();
	
	@DefaultMessage("Length")
	String columnExamLength();
	
	@DefaultMessage("Seating<br>Type")
	String columnExamSeatingType();
	
	@DefaultMessage("Size")
	String columnExamSize();
	
	@DefaultMessage("Max<br>Rooms")
	String columnExamMaxRooms();
	
	@DefaultMessage("Instructor")
	String columnExamInstructor();
	
	@DefaultMessage("Period<br>Preferences")
	String columnExamPeriodPreferences();

	@DefaultMessage("Room<br>Preferences")
	String columnExamRoomPreferences();
	
	@DefaultMessage("Distribution<br>Preferences")
	String columnExamDistributionPreferences();
	
	@DefaultMessage("Assigned<br>Period")
	String columnExamAssignedPeriod();
	
	@DefaultMessage("Assigned<br>Room")
	String columnExamAssignedRoom();
	
	@DefaultMessage("Student<br>Conflicts")
	String columnExamStudentConflicts();
	
	@DefaultMessage("Subject")
	String columnExamSubject();
	
	@DefaultMessage("Course")
	String columnExamCourse();
	
	@DefaultMessage("External Id")
	String columnExamExternalId();
	
	@DefaultMessage("Instructional Type")
	String columnExamInstructionalType();

	@DefaultMessage("Section")
	String columnExamSection();
	
	@DefaultMessage("Date")
	String columnExamDate();

	@DefaultMessage("Time")
	String columnExamTime();
	
	@DefaultMessage(" Type ")
	String columnDistrPrefType();
	
	@DefaultMessage(" Structure ")
	String columnDistrPrefStructure();

	@DefaultMessage(" Owner ")
	String columnDistrPrefOwner();
	
	@DefaultMessage(" Class ")
	String columnDistrPrefClass();
	
	@DefaultMessage("Min Limit<br>per Class")
	String columnSubpartMinLimitPerClass();
	
	@DefaultMessage("Max Limit<br>per Class")
	String columnSubpartMaxLimitPerClass();
	
	@DefaultMessage("Number<br>of Classes")
	String columnSubpartNumberOfClasses();
	
	@DefaultMessage("Minutes<br>per Week")
	String columnSubpartMinutesPerWeek();
	
	@DefaultMessage("Number<br>of Rooms")
	String columnSubpartNumberOfRooms();
	
	@DefaultMessage("Room<br>Ratio")
	String columnSubpartRoomRatio();
	
	@DefaultMessage("Managing<br>Department")
	String columnSubpartManagingDepartment();
	
	@DefaultMessage("Limit<br>per Class")
	String columnSubpartLimitPerClass();
	
	@DefaultMessage("Offering")
	String columnCrossListsOffering();
	
	@DefaultMessage("Controlling")
	String columnCrossListsControlling();
	
	@DefaultMessage("Reserved")
	String columnCrossListsReserved();

	@DefaultMessage("Projected")
	String columnCrossListsProjected();
	
	@DefaultMessage("Last Term")
	String columnCrossListsLastTerm();
	
	@DefaultMessage("Allow<br>variable limits")
	String columnAllowVariableLimits();
	
	@DefaultMessage("Enroll")
	String columnEnroll();
	
	@DefaultMessage("Nbr<br>Rms")
	String columnNbrRms();
	
	@DefaultMessage("Managing Department")
	String columnManagingDepartment();
	
	@DefaultMessage("Display&nbsp;<br>Instr")
	String columnDisplayInstr();
	
	@DefaultMessage("Student&nbsp;<br>Scheduling")
	String columnStudentScheduling();
	
	@DefaultMessage("Instructors")
	String columnInstructors();
	
	@DefaultMessage("Display")
	String columnDisplay();
	
	@DefaultMessage("Min")
	String columnMin();
	
	@DefaultMessage("Max")
	String columnMax();
	
	@DefaultMessage("Reserved")
	String columnReserved();
	
	@DefaultMessage("Schedule of Classes Note")
	String columnScheduleOfClassesNote();
		
	@DefaultMessage("Demands From")
	String columnDemandsFrom();
	
	@DefaultMessage("Login")
	String columnLogin();
	
	@DefaultMessage("Department")
	String columnDepartment();
	
	@DefaultMessage("Type")
	String columnCourseType();
	
	@DefaultMessage("Credit")
	String columnCredit();
	
	@DefaultMessage("Total")
	String rowCrossListsTotal();
	
	@DefaultMessage("Not assigned.")
	String messageNotAssigned();

	@DefaultMessage("{0} - Disabled For Student Scheduling.")
	String tooltipDisabledForStudentScheduling(String classLabelWithTitle);
	
	@DefaultMessage("Has Schedule Print Note")
	String altHasSchedulePrintNote();
	
	@DefaultMessage("Has Course Offering Note")
	String altHasCourseOfferingNote();
	
	@DefaultMessage("Has Note to Mgr")
	String altHasNoteToMgr();
	
	@DefaultMessage("Delete")
	String altDelete();
	
	@DefaultMessage("Move Up")
	String altMoveUp();
	
	@DefaultMessage("Move Down")
	String altMoveDown();
	
	@DefaultMessage("Limits do not match")
	String altCrossListsLimitsDoNotMatch();
	
	@DefaultMessage("Limits do not match")
	String altLimitsDoNotMatch();
	
	@DefaultMessage("Controlling Course")
	String altControllingCourse();
	
	@DefaultMessage("Not Available")
	String altNotAvailableExternalId();
	
	@DefaultMessage("Yes")
	String altYes(); //an alt text to a check (as in Ignore Too Far - Checked)
		
	@DefaultMessage("Configuration {0}")
	String labelConfiguration(String name);
	
	@DefaultMessage("Configuration {0} ({1})")
	String labelConfigurationWithInstructionalMethod(String name, String instructionalMethod);
	
	@DefaultMessage("{0} Not Offered Courses")
	String labelNotOfferedCourses(String subjectAreaAbbv);
	
	@DefaultMessage("{0} Offered Courses")
	String labelOfferedCourses(String subjectAreaAbbv);
	
	@DefaultMessage("There are no courses currently offered for {0} subject.")
	String errorNoCoursesOffered(String subjectAreaAbbv);

	@DefaultMessage("All courses are currently being offered for {0} subject.")
	String errorAllCoursesOffered(String subjectAreaAbbv);
	
	@DefaultMessage("Sort By:")
	String filterSortBy();
	
	@DefaultMessage("Cross Lists:")
	String filterCrossList();
	
	@DefaultMessage("Show cross-listed classes")
	String showCrossListedClasses();
	
	@DefaultMessage("Subject:")
	String filterSubject();

	@DefaultMessage("Course Number:")
	String filterCourseNumber();

	@DefaultMessage("Search")
	@StrutsAction(
		value = "searchInstructionalOfferings",
		apply = InstructionalOfferingSearchAction.class
	)
	String actionSearchInstructionalOfferings();
	
	@DefaultMessage("Search")
	@StrutsAction(
		value = "searchClasses",
		apply = ClassSearchAction.class
	)
	String actionSearchClasses();
	
	@DefaultMessage("Search")
	String actionSearchInstructors();

	@DefaultMessage("Export PDF")
	@StrutsAction("exportPdf")
	String actionExportPdf();

	@DefaultMessage("Worksheet PDF")
	@StrutsAction("worksheetPdf")
	String actionWorksheetPdf();

	@DefaultMessage("Add New")
	@StrutsAction("addInstructionalOfferings")
	String actionAddNewInstructionalOffering();
	
	@DefaultMessage("Edit Class")
	String actionEditClass();
	
	@DefaultMessage("Add Distribution Preference")
	String actionAddDistributionPreference();
	
	@DefaultMessage("Assign")
	String actionOpenClassAssignmentDialog();
		
	@DefaultMessage("Previous")
	String actionPreviousClass();
	
	@DefaultMessage("Previous")
	String actionPreviousSubpart();
	
	@DefaultMessage("Previous")
	String actionPreviousInstructor();
	
	@DefaultMessage("Previous")
	String actionPreviousExamination();
	
	@DefaultMessage("Previous")
	String actionPreviousIO();
	
	@DefaultMessage("Next")
	String actionNextClass();

	@DefaultMessage("Next")
	String actionNextSubpart();
	
	@DefaultMessage("Next")
	String actionNextInstructor();
	
	@DefaultMessage("Next")
	String actionNextExamination();
	
	@DefaultMessage("Next")
	String actionNextIO();
	
	@DefaultMessage("Back")
	String actionBackClassDetail();
	
	@DefaultMessage("Back")
	String actionBackSubpartDetail();
	
	@DefaultMessage("Back")
	String actionBackToDetail();
	
	@DefaultMessage("Back")
	String actionBackToIODetail();
	
	@DefaultMessage("Update")
	String actionUpdatePreferences();
	
	@DefaultMessage("Update")
	String actionUpdateExamination();
	
	@DefaultMessage("Update")
	String actionUpdateConfiguration();
	
	@DefaultMessage("Update")
	String actionUpdateCourseOffering();
	
	@DefaultMessage("Save")
	String actionSaveCourseOffering();
	
	@DefaultMessage("Update")
	String actionUpdateCrossLists();
	
	@DefaultMessage("Update")
	String actionUpdateMultipleClassSetup();
		
	@DefaultMessage("Clear Class Preferences")
	String actionClearClassPreferences();
	
	@DefaultMessage("Clear Subpart Preferences")
	String actionClearSubpartPreferences();

	@DefaultMessage("Clear Instructor Preferences")
	String actionClearInstructorPreferences();

	@DefaultMessage("Add Time Preference")
	String actionAddTimePreference();
	
	@DefaultMessage("Delete")
	String actionRemoveTimePattern();
	
	@DefaultMessage("Add Room Group Preference")
	String actionAddRoomGroupPreference();
	
	@DefaultMessage("Delete")
	String actionRemoveRoomGroupPreference();
	
	@DefaultMessage("Delete")
	String titleRemoveDatePatternPreference();
	
	@DefaultMessage("Add Room Preference")
	String actionAddRoomPreference();
	
	@DefaultMessage("Add Date Pattern Preference")
	String actionAddDatePatternPreference();
	
	@DefaultMessage("Delete")
	String actionRemoveRoomPreference();
	
	@DefaultMessage("Delete")
	String actionRemoveDatePatternPreference();
	
	@DefaultMessage("Add Building Preference")
	String actionAddBuildingPreference();
	
	@DefaultMessage("Delete")
	String actionRemoveBuildingPreference();

	@DefaultMessage("Add Room Feature Preference")
	String actionAddRoomFeaturePreference();
	
	@DefaultMessage("Delete")
	String actionRemoveRoomFeaturePreference();
	
	@DefaultMessage("Delete")
	String actionRemoveDistributionPreference();
	
	@DefaultMessage("Delete")
	String actionDeleteConfiguration();
	
	@DefaultMessage("Save")
	String actionSaveExamination();
	
	@DefaultMessage("Save")
	String actionSaveConfiguration();
	
	@DefaultMessage("Add Instructor")
	String actionAddInstructor();
	
	@DefaultMessage("Add New Instructor")
	String actionAddNewInstructor();
	
	@DefaultMessage("Delete")
	String actionRemoveInstructor();
	
	@DefaultMessage("Add Examination")
	String actionAddExamination();
	
	@DefaultMessage("Edit Subpart")
	String actionEditSubpart();
	
	@DefaultMessage("Clear Class Preferences")
	String actionClearClassPreferencesOnSubpart();
	
	@DefaultMessage("Add")
	String actionAddInstructionalTypeToConfig();
	
	@DefaultMessage("Add")
	String actionAddCourseToCrossList();

	@DefaultMessage("Add Coordinator")
	String actionAddCoordinator();
	
	@DefaultMessage("Delete")
	String actionRemoveCoordinator();
	
	@DefaultMessage("Unassign All")
	String actionUnassignAllInstructorsFromConfig();
	
	@DefaultMessage("Update")
	String actionUpdateClassInstructorsAssignment();
	
	@DefaultMessage("Add Configuration")
	String actionAddConfiguration();

	@DefaultMessage("Cross Lists")
	String actionCrossLists();
	
	@DefaultMessage("Make Offered")
	String actionMakeOffered();

	@DefaultMessage("Make NOT Offered")
	String actionMakeNotOffered();
	
	@DefaultMessage("Delete")
	String actionDeleteIO();
	
	@DefaultMessage("Back")
	String actionBackToIOList();
	
	@DefaultMessage("Edit Course Offering")
	String actionEditCourseOffering();
	
	@DefaultMessage("Add Course Offering")
	String actionAddCourseOffering();
	
	@DefaultMessage("Lock")
	String actionLockIO();
	
	@DefaultMessage("Unlock")
	String actionUnlockIO();
	
	@DefaultMessage("Back")
	String actionBackIODetail();
	
	@DefaultMessage("Edit Configuration")
	String actionEditConfiguration();
	
	@DefaultMessage("Class Setup")
	String actionClassSetup();
	
	@DefaultMessage("Assign Instructors")
	String actionAssignInstructors();
	
	@DefaultMessage("Manage Instructor List")
	String actionManageInstructorList();
	
	@DefaultMessage("Select Instructor")
	String actionSelectInstructor();
	
	@DefaultMessage("Cancel")
	String actionCancel();
	
	@DefaultMessage("Save")
	String actionSaveInstructor();
	
	@DefaultMessage("Update")
	String actionUpdateInstructor();
	
	@DefaultMessage("Delete")
	String actionDeleteInstructor();
	
	@DefaultMessage("Lookup")
	String actionLookupInstructor();
	
	@DefaultMessage("Back")
	String actionBackToInstructors();
	
	@DefaultMessage("S")
	String accessSearchInstructionalOfferings();
	
	@DefaultMessage("S")
	String accessSearchClasses();
	
	@DefaultMessage("S")
	String accessSearchInstructors();

	@DefaultMessage("P")
	String accessExportPdf();
	
	@DefaultMessage("W")
	String accessWorksheetPdf();

	@DefaultMessage("A")
	String accessAddNewInstructionalOffering();

	@DefaultMessage("E")
	String accessEditClass();
	
	@DefaultMessage("A")
	String accessAddDistributionPreference();
	
	@DefaultMessage("X")
	String accessOpenClassAssignmentDialog();

	@DefaultMessage("P")
	String accessPreviousClass();

	@DefaultMessage("P")
	String accessPreviousSubpart();
	
	@DefaultMessage("P")
	String accessPreviousInstructor();
	
	@DefaultMessage("P")
	String accessPreviousExamination();
	
	@DefaultMessage("P")
	String accessPreviousIO();

	@DefaultMessage("N")
	String accessNextClass();
	
	@DefaultMessage("N")
	String accessNextSubpart();
	
	@DefaultMessage("N")
	String accessNextInstructor();
	
	@DefaultMessage("N")
	String accessNextExamination();
	
	@DefaultMessage("N")
	String accessNextIO();
	
	@DefaultMessage("B")
	String accessBackClassDetail();
	
	@DefaultMessage("B")
	String accessBackSubpartDetail();
	
	@DefaultMessage("B")
	String accessBackToDetail();
	
	@DefaultMessage("B")
	String accessBackToIODetail();
	
	@DefaultMessage("I")
	String accessInstructionalOfferingDetail();
	
	@DefaultMessage("S")
	String accessSchedulingSubpartDetail();
	
	@DefaultMessage("U")
	String accessUpdatePreferences();
	
	@DefaultMessage("U")
	String accessUpdateExamination();
	
	@DefaultMessage("U")
	String accessUpdateConfiguration();
	
	@DefaultMessage("U")
	String accessUpdateCourseOffering();
	
	@DefaultMessage("S")
	String accessSaveCourseOffering();
	
	@DefaultMessage("U")
	String accessUpdateCrossLists();
	
	@DefaultMessage("U")
	String accessUpdateMultipleClassSetup();
	
	@DefaultMessage("C")
	String accessClearClassPreferences();

	@DefaultMessage("C")
	String accessClearSubpartPreferences();
	
	@DefaultMessage("C")
	String accessClearInstructorPreferences();
	
	@DefaultMessage("T")
	String accessAddTimePreference();
	
	@DefaultMessage("G")
	String accessAddRoomGroupPreference();
	
	@DefaultMessage("R")
	String accessAddRoomPreference();
	
	@DefaultMessage("D")
	String accessAddBuildingPreference();

	@DefaultMessage("F")
	String accessAddRoomFeaturePreference();
	
	@DefaultMessage("S")
	String accessSaveExamination();
	
	@DefaultMessage("S")
	String accessSaveConfiguration();
	
	@DefaultMessage("I")
	String accessAddInstructor();
	
	@DefaultMessage("A")
	String accessAddNewInstructor();
	
	@DefaultMessage("X")
	String accessAddExamination();
	
	@DefaultMessage("E")
	String accessEditSubpart();
	
	@DefaultMessage("D")
	String accessDeleteConfiguration();
	
	@DefaultMessage("A")
	String accessAddInstructionalTypeToConfig();
	
	@DefaultMessage("A")
	String accessAddCourseToCrossList();
	
	@DefaultMessage("I")
	String accessBackToIOList();
	
	@DefaultMessage("C")
	String accessAddCoordinator();
	
	@DefaultMessage("U")
	String accessUpdateClassInstructorsAssignment();
	
	@DefaultMessage("C")
	String accessAddConfiguration();
	
	@DefaultMessage("L")
	String accessCrossLists();
	
	@DefaultMessage("F")
	String accessMakeOffered();

	@DefaultMessage("F")
	String accessMakeNotOffered();
	
	@DefaultMessage("B")
	String accessBackToIOListButton();
	
	@DefaultMessage("D")
	String accessDeleteIO();
	
	@DefaultMessage("X")
	String accessLockIO();
	
	@DefaultMessage("X")
	String accessUnlockIO();
	
	@DefaultMessage("B")
	String accessBackIODetail();
	
	@DefaultMessage("M")
	String accessManageInstructorList();
	
	@DefaultMessage("E")
	String accessSelectInstructor();
	
	@DefaultMessage("C")
	String accessCancel();
	
	@DefaultMessage("S")
	String accessSaveInstructor();
	
	@DefaultMessage("U")
	String accessUpdateInstructor();
	
	@DefaultMessage("D")
	String accessDeleteInstructor();
	
	@DefaultMessage("L")
	String accessLookupInstructor();
	
	@DefaultMessage("B")
	String accessBackToInstructors();
	
	@DefaultMessage("Search/Display Offerings (Alt+{0})")
	String titleSearchInstructionalOfferings(String accessKey);

	@DefaultMessage("Search/Display Classes (Alt+{0})")
	String titleSearchClasses(String accessKey);
	
	@DefaultMessage("Search/Display Instructors (Alt+{0})")
	String titleSearchInstructors(String accessKey);
	
	@DefaultMessage("Export PDF (Alt+{0})")
	String titleExportPdf(String accessKey);

	@DefaultMessage("Export Worksheet PDF (Alt+{0})")
	String titleWorksheetPdf(String accessKey);

	@DefaultMessage("Add New Offering (Alt+{0})")
	String titleAddNewInstructionalOffering(String accessKey);

	@DefaultMessage("Edit Class (Alt+{0})")
	String titleEditClass(String accessKey);
	
	@DefaultMessage("Add Distribution Preference (Alt+{0})")
	String titleAddDistributionPreference(String accessKey);
	
	@DefaultMessage("Open Class Assignment Dialog (Alt+{0})")
	String titleOpenClassAssignmentDialog(String accessKey);
		
	@DefaultMessage("Previous Class (Alt+{0})")
	String titlePreviousClass(String accessKey);
	
	@DefaultMessage("Update and go to previous Class (Alt+{0})")
	String titlePreviousClassWithUpdate(String accessKey);
	
	@DefaultMessage("Previous Scheduling Subpart (Alt+{0}")
	String titlePreviousSubpart(String accessKey);
	
	@DefaultMessage("Update and go to previous Scheduling Subpart (Alt+{0})")
	String titlePreviousSubpartWithUpdate(String accessKey);

	@DefaultMessage("Previous Instructional Offering (Alt+{0})")
	String titlePreviousIO(String accessKey);
	
	@DefaultMessage("Update and go to previous Instructional Offering (Alt+{0})")
	String titlePreviousIOWithUpdate(String accessKey);
	
	@DefaultMessage("Update and go to previous Instructor (Alt+{0})")
	String titlePreviousInstructorWithUpdate(String accessKey);
	
	@DefaultMessage("Update and go to previous Examination (Alt+{0})")
	String titlePreviousExaminationWithUpdate(String accessKey);

	@DefaultMessage("Next Class (Alt+{0})")
	String titleNextClass(String accessKey);
	
	@DefaultMessage("Update and go to next Class (Alt+{0})")
	String titleNextClassWithUpdate(String accessKey);
	
	@DefaultMessage("Next Scheduling Subpart (Alt+{0})")
	String titleNextSubpart(String accessKey);
	
	@DefaultMessage("Update and go to next Scheduling Subpart (Alt+{0})")
	String titleNextSubpartWithUpdate(String accessKey);
	
	@DefaultMessage("Next Instructional Offering (Alt+{0})")
	String titleNextIO(String accessKey);
	
	@DefaultMessage("Update and go to next Instructional Offering (Alt+{0})")
	String titleNextIOWithUpdate(String accessKey);
	
	@DefaultMessage("Update and go to next Instructor (Alt+{0})")
	String titleNextInstructorWithUpdate(String accessKey);
	
	@DefaultMessage("Update and go to next Examination (Alt+{0})")
	String titleNextExaminationWithUpdate(String accessKey);
	
	@DefaultMessage("Update Examination (Alt+{0})")
	String titleUpdateExamination(String accessKey);
	
	@DefaultMessage("Update Configuration (Alt+{0})")
	String titleUpdateConfiguration(String accessKey);
	
	@DefaultMessage("Update Course Offering (Alt+{0})")
	String titleUpdateCourseOffering(String accessKey);
	
	@DefaultMessage("Save Course Offering (Alt+{0})")
	String titleSaveCourseOffering(String accessKey);
	
	@DefaultMessage("Update Cross Lists (Alt+{0})")
	String titleUpdateCrossLists(String accessKey);
	
	@DefaultMessage("Update Class Setup (Alt+{0})")
	String titleUpdateMultipleClassSetup(String accessKey);
	
	@DefaultMessage("Return to %% (Alt+{0})")
	String titleBackClassDetail(String accessKey);
	
	@DefaultMessage("Return to %% (Alt+{0})")
	String titleBackSubpartDetail(String accessKey);
	
	@DefaultMessage("Do not commit any change. Return to Detail Screen (Alt+{0})")
	String titleBackToDetail(String accessKey);
	
	@DefaultMessage("Back to Instructional Offering Detail (Alt+{0})")
	String titleBackToIODetail (String accessKey);
	
	@DefaultMessage("Instructional Offering Detail (Alt+{0})")
	String titleInstructionalOfferingDetail(String accessKey);
	
	@DefaultMessage("Scheduling Subpart Detail (Alt+{0})")
	String titleSchedulingSubpartDetail(String accessKey);
	
	@DefaultMessage("Commit changes to database (Alt+{0})")
	String titleUpdatePreferences(String accessKey);
	
	@DefaultMessage("Delete all Class Preferences. Preferences will be inherited from the subpart (Alt+{0})")
	String titleClearClassPreferences(String accessKey);
	
	@DefaultMessage("Delete all Subpart Preferences (Alt+{0})")
	String titleClearSubpartPreferences(String accessKey);
	
	@DefaultMessage("Delete all Instructor Preferences (Alt+{0})")
	String titleClearInstructorPreferences(String accessKey);
	
	@DefaultMessage("Add Time Pattern Preference (Alt+{0})")
	String titleAddTimePreference(String accessKey);
	
	@DefaultMessage("Remove Time Pattern")
	String titleRemoveTimePattern();
	
	@DefaultMessage("Add Room Group Preference (Alt+{0})")
	String titleAddRoomGroupPreference(String accessKey);
	
	@DefaultMessage("Remove Room Group Preference")
	String titleRemoveRoomGroupPreference();
	
	@DefaultMessage("Add Room Preference (Alt+{0})")
	String titleAddRoomPreference(String accessKey);
	
	@DefaultMessage("Add Date Pattern Preference (Alt+{0})")
	String titleAddDatePatternPreference(String accessKey);
	
	@DefaultMessage("Remove Room Preference")
	String titleRemoveRoomPreference();
	
	@DefaultMessage("Add Building Preference (Alt+{0})")
	String titleAddBuildingPreference(String accessKey);
	
	@DefaultMessage("Remove Building Preference")
	String titleRemoveBuildingPreference();
	
	@DefaultMessage("Add Room Feature Preference (Alt+{0})")
	String titleAddRoomFeaturePreference(String accessKey);
	
	@DefaultMessage("Remove Room Feature Preference")
	String titleRemoveRoomFeaturePreference();
	
	@DefaultMessage("Remove Distribution Preference")
	String titleRemoveDistributionPreference();
	
	@DefaultMessage("Delete Configuration (Alt+{0})")
	String titleDeleteConfiguration(String accessKey);
	
	@DefaultMessage("Save Examination (Alt+{0})")
	String titleSaveExamination(String accessKey);
	
	@DefaultMessage("Save Configuration (Alt+{0})")
	String titleSaveConfiguration(String accessKey);
	
	@DefaultMessage("Add Instructor (Alt+{0})")
	String titleAddInstructor(String accessKey);
	
	@DefaultMessage("Add New Instructor (Alt+{0})")
	String titleAddNewInstructor(String accessKey);
	
	@DefaultMessage("Add Instructor to Class")
	String titleAddInstructorToClass();
	
	@DefaultMessage("Remove Instructor")
	String titleRemoveInstructor();
	
	@DefaultMessage("Remove Instructor from Class")
	String titleRemoveInstructorFromClass();
	
	@DefaultMessage("Add Examination (Alt+{0})")
	String titleAddExamination(String accessKey);
	
	@DefaultMessage("Edit Scheduling Supbart (Alt+{0})")
	String titleEditSubpart(String accessKey);
	
	@DefaultMessage("Delete all Class Preferences. Preferences will be inherited from the subpart.")
	String titleClearClassPreferencesOnSubpart();

	@DefaultMessage("Add the selected instructional type to the configuration (Alt+{0})")
	String titleAddInstructionalTypeToConfig(String accessKey);
	
	@DefaultMessage("Add course offering to the instructional offering (Alt+{0})")
	String titleAddCourseToCrossList(String accessKey);
	
	@DefaultMessage("Move to Child Level")
	String titleMoveToChildLevel();
	
	@DefaultMessage("Move to Parent Level")
	String titleMoveToParentLevel();
	
	@DefaultMessage("Delete Instructional Type")
	String titleDeleteInstructionalType();		
	
	@DefaultMessage("Back to Instructional Offering List (Alt+{0})")
	String titleBackToIOList(String accessKey);
	
	@DefaultMessage("Limits do not match")
	String titleCrossListsLimitsDoNotMatch();
	
	@DefaultMessage("Limits do not match")
	String titleLimitsDoNotMatch();
	
	@DefaultMessage("Remove course from instructional offering & mark it as not offered.")
	String titleRemoveCourseFromCrossList();
	
	@DefaultMessage("Display all instructors of this subpart in the schedule book.")
	String titleDisplayAllInstrForSubpartInSchedBook();
	
	@DefaultMessage("Enable all classes of this subpart for student scheduling.")
	String titleEnableAllClassesOfSubpartForStudentScheduling();
	
	@DefaultMessage("Enable these classes for student scheduling.")
	String titleEnableTheseClassesForStudentScheduling();
	
	@DefaultMessage("Move Class Up")
	String titleMoveClassUp();
	
	@DefaultMessage("Move Class Down")
	String titleMoveClassDown();
	
	@DefaultMessage("Remove Class from Instructional Offering")
	String titleRemoveClassFromIO();
	
	@DefaultMessage("Add a class of this type to Instructional Offering")
	String titleAddClassToIO();
	
	@DefaultMessage("Add Coordinator (Alt+{0})")
	String titleAddCoordinator(String accessKey);
	
	@DefaultMessage("Unassign All Instructors")
	String titleUnassignAllInstructorsFromConfig();

	@DefaultMessage("Update Class Instructors (Alt+{0})")
	String titleUpdateClassInstructorsAssignment(String accessKey);
	
	@DefaultMessage("Add Configuration (Alt+{0})")
	String titleAddConfiguration(String accessKey);
	
	@DefaultMessage("Add/Delete cross-listed courses and change controlling course Alt+{0})")
	String titleCrossLists(String accessKey);
	
	@DefaultMessage("Make this offering 'Offered' (Alt+{0})")
	String titleMakeOffered(String accessKey);

	@DefaultMessage("Make this offering 'Not Offered' (Alt+{0})")
	String titleMakeNotOffered(String accessKey);

	@DefaultMessage("Delete Instructional Offering (Alt+{0})")
	String titleDeleteIO(String accessKey);
	
	@DefaultMessage("Lock Offering (Alt+{0})")
	String titleLockIO(String accessKey);
	
	@DefaultMessage("Unlock Offering (Alt+{0})")
	String titleUnlockIO(String accessKey);
	
	@DefaultMessage("Return to %% (Alt+{0})")
	String titleBackIODetail(String accessKey);	
	
	@DefaultMessage("Controlling Course")
	String titleControllingCourse();
	
	@DefaultMessage("Edit Course Offering")
	String titleEditCourseOffering();
	
	@DefaultMessage("Unlimited Enrollment")
	String titleUnlimitedEnrollment();
	
	@DefaultMessage("Set Up Configuration")
	String titleEditConfiguration();
	
	@DefaultMessage("Multiple Class Setup")
	String titleClassSetup();
	
	@DefaultMessage("Class Instructor Assignment")
	String titleAssignInstructors();
	
	@DefaultMessage("Add Instructor from Staff / Remove from Dept List (Alt+{0})")
	String titleManageInstructorList(String accessKey);
	
	@DefaultMessage("Instructor ExternalId not supplied")
	String titleInstructorExternalIdNotSupplied();
	
	@DefaultMessage("Do Not Display Instructor.")
	String titleDoNotDisplayInstructor();
	
	@DefaultMessage("check conflicts")
	String titleCheckConflicts();
	
	@DefaultMessage("Midterm Examination")
	String titleMidtermExamination();
	
	@DefaultMessage("Final Examination")
	String titleFinalExamination();
	
	@DefaultMessage("Ignore too far distances")
	String titleIgnoreTooFarDistances();
	
	@DefaultMessage("Select Instructor (Alt+{0})")
	String titleSelectInstructor(String accessKey);
	
	@DefaultMessage("Cancel (Alt+{0})")
	String titleCancel(String accessKey);
	
	@DefaultMessage("Save Instructor Information (Alt+{0})")
	String titleSaveInstructor(String accessKey);
	
	@DefaultMessage("Update Instructor (Alt+{0})")
	String titleUpdateInstructor(String accessKey);
	
	@DefaultMessage("Delete Instructor (Alt+{0})")
	String titleDeleteInstructor(String accessKey);
	
	@DefaultMessage("Look for matches in Staff List and Directory (Alt+{0})")
	String titleLookupInstructor(String accessKey);
	
	@DefaultMessage("Back to Instructors (Alt+{0})")
	String titleBackToInstructors(String accessKey);
	
	@DefaultMessage("Cancel this class.")
	String titleCancelClass();
	
	@DefaultMessage("Reopen this class.")
	String titleReopenClass();
	
	@DefaultMessage("Course numbers can be specified using wildcard (*). E.g. 2*")
	String tooltipCourseNumber();
	
	@DefaultMessage("Subject Area")
	String labelSubjectArea();
	
	@DefaultMessage("No records matching the search criteria were found.")
	String errorNoRecords();
	
	@DefaultMessage("Instructional Offerings")
	String labelInstructionalOfferings();
	
	@DefaultMessage("Unable to create PDF file: {0}.")
	String errorUnableToCreatePdf(String reason);
	
	@DefaultMessage("Unable to create worksheet PDF file: nothing to export.")
	String errorUnableToCreateWorksheetPdfNoData();

	@DefaultMessage("Unable to create worksheet PDF file: {0}.")
	String errorUnableToCreateWorksheetPdf(String reason);
	
	@DefaultMessage("Course Number cannot be matched to regular expression: {0}. Reason: {1}")
	String errorCourseDoesNotMatchRegEx(String regEx, String reason);
	
	@DefaultMessage("ERRORS") //preferable to use this than errors for each page
	String errors();
	
	@DefaultMessage("ERRORS")
	String errorsClassDetail();
	
	@DefaultMessage("ERRORS")
	String errorsSubpartDetail();
	
	@DefaultMessage("ERRORS")
	String errorsClassEdit();
	
	@DefaultMessage("ERRORS")
	String errorsSubpartEdit();
	
	@DefaultMessage("ERRORS")
	String errorsConfigurationEdit();
	
	@DefaultMessage("ERRORS")
	String errorsIOCrossLists();
	
	@DefaultMessage("ERRORS")
	String errorsMultipleClassSetup();
	
	@DefaultMessage("Invalid room group: Check for duplicate / blank selection. ")
	String errorInvalidRoomGroup();
	
	@DefaultMessage("Invalid room group level. ")
	String errorInvalidRoomGroupLevel();
	
	@DefaultMessage("Invalid building preference: Check for duplicate / blank selection. ")
	String errorInvalidBuildingPreference();
	
	@DefaultMessage("Invalid building preference level. ")
	String errorInvalidBuildingPreferenceLevel();
	
	@DefaultMessage("Invalid distribution preference: Check for duplicate / blank selection. ")
	String errorInvalidDistributionPreference();
	
	@DefaultMessage("Invalid date pattern preference: Check for duplicate / blank selection. ")
	String errorInvalidDatePatternPreference();

	@DefaultMessage("Invalid distribution preference level.")
	String errorInvalidDistributionPreferenceLevel();
	
	@DefaultMessage("Invalid room feature preference: Check for duplicate / blank selection. ")
	String errorInvalidRoomFeaturePreference();
	
	@DefaultMessage("Invalid room feature preference level. ")
	String errorInvalidRoomFeaturePreferenceLevel();

	@DefaultMessage("Invalid instructor preference: Check for duplicate / blank selection. ")
	String errorInvalidInstructorPreference();
	
	@DefaultMessage("Time pattern not selected. ")
	String errorTimePatternNotSelected();
	
	@DefaultMessage("Invalid room preference: Check for duplicate / blank selection. ")
	String errorInvalidRoomPreference();
	
	@DefaultMessage("Invalid room preference level. ")
	String errorInvalidRoomPreferenceLevel();
	
	@DefaultMessage("Null Operation not supported.")
	String errorNullOperationNotSupported();
	
	@DefaultMessage("Class Info not supplied.")
	String errorClassInfoNotSupplied();
	
	@DefaultMessage("Subpart Info not supplied.")
	String errorSubpartInfoNotSupplied();
	
	@DefaultMessage("Number of Rooms cannot be less than 0.")
	String errorNumberOfRoomsNegative();
	
	@DefaultMessage("Room Ratio cannot be less than 0.")
	String errorRoomRatioNegative();
	
	@DefaultMessage("Minimum Expected Capacity cannot be less than 0.")
	String errorMinimumExpectedCapacityNegative();

	@DefaultMessage("Maximum Expected Capacity cannot be less than 0.")
	String errorMaximumExpectedCapacityNegative();

	@DefaultMessage("Maximum Expected Capacity cannot be less than Minimum Expected Capacity.")
	String errorMaximumExpectedCapacityLessThanMinimum();

	@DefaultMessage("Class Owner is required.")
	String errorRequiredClassOwner();

	@DefaultMessage("Notes to schedule manager cannot exceed 999 characters.")
	String errorNotesLongerThan999();
	
	@DefaultMessage("Schedule print note cannot exceed 1999 characters.")
	String errorSchedulePrintNoteLongerThan1999();
	
	@DefaultMessage("Required room {0} (capacity: {1}) cannot accommodate this class (capacity: {2})")
	String errorRequiredRoomTooSmall(String room, int roomCapacity, int requiredCapacity);
	
	@DefaultMessage("User temporarily locked out - Exceeded maximum failed login attempts.")
	String errorUserTemporarilyLockedOut();
	
	@DefaultMessage("Authentication failed")
	String errorAuthenticationFailed();
	
	@DefaultMessage("Operation could not be interpreted: ")
	String errorOperationNotInterpreted();
	
	@DefaultMessage("Course Offering data was not correct: ")
	String errorCourseDataNotCorrect();
	
	@DefaultMessage("Config ID is not valid: ")
	String errorConfigIDNotValid();
	
	@DefaultMessage("Subject is required.")
	String errorSubjectRequired();
	
	@DefaultMessage("Course Number is required.")
	String errorCourseNumberRequired();
	
	@DefaultMessage("Course Number cannot be matched to regular expression: {0} . Reason: {1}")
	String errorCourseNumberCannotBeMatched(String regularExpression, String reason);
	
	@DefaultMessage("The course cannot be renamed. A course with the same course number already exists.")
	String errorCourseCannotBeRenamed();
	
	@DefaultMessage("The course cannot be created. A course with the same course number already exists.")
	String errorCourseCannotBeCreated();
	
	@DefaultMessage("Reserved spaces should total to at least the limit")
	String errorCrossListsLimitsDoNotMatch();
	
	@DefaultMessage("Course Offering is required")
	String errorRequiredCourseOffering();
	
	@DefaultMessage("Controlling Course is required")
	String errorRequiredControllingCourse();
	
	@DefaultMessage("Reserved Space is required")
	String errorRequiredReservedSpace();
	
	@DefaultMessage("Unique Id needed for operation. ")
	String errorUniqueIdNeeded();
	
	@DefaultMessage("Class is required")
	String errorRequiredClass();
	
	@DefaultMessage("Instructional Offering Configuration is required")
	String errorRequiredIOConfiguration();
	
	@DefaultMessage("The configuration requires that <b>{0}</b> have at least one child class.")
	String errorClassMustHaveChildClasses(String parentClass);
	
	@DefaultMessage("The configuration requires that each scheduling subpart have at least one associated class.")
	String errorEachSubpartMustHaveClass();
	
	@DefaultMessage("Maximum limit for class {0} cannot be less than the minimum limit for the class.")
	String errorMaxLessThanMinLimit(String classWithWrongLimits);
	
	@DefaultMessage("Maximum limits for each top level class type must total to greater than or equal to the offering limit.")
	String errorMaxLimitsTotalTooLow();
	
	@DefaultMessage("Limits for each top level class type must total to greater than or equal to the offering limit.")
	String errorLimitsForTopLevelClassesTooLow();
	
	@DefaultMessage("Maximum limits for child classes of the same type at a level must total to at least the parent class maximum limit.")
	String errorTotalMaxChildrenAtLeastMaxParent();
	
	@DefaultMessage("Minimum limits for child classes of the same type at a level must total to greater than or equal to the parent class minimum limit.")
	String errorTotalMinChildrenAtLeastMinParent();
	
	@DefaultMessage("Limits for child classes of the same type at a level must total to greater than or equal to the parent class limit.")
	String errorLimitsChildClasses();
	
	@DefaultMessage("Missing Instructional Offering Configuration.")
	String errorMissingIOConfig();
	
	@DefaultMessage("Instructional Offering Config has not been defined.")
	String errorIOConfigNotDefined();
	
	@DefaultMessage("Initial setup of Instructional Offering Config has not been completed.")
	String errorInitialIOSetupIncomplete();
	
	@DefaultMessage("Duplicate instructor for class.")
	String errorDuplicateInstructorForClass();
	
	@DefaultMessage("Spaces reserved for course offerings ({0}) should total to at least the offering limit.")
	String errorReservedSpacesForOfferingsTotal(String coursesTotal);
	
	@DefaultMessage("Configuration {0} has a higher limit than there is space available in its classes.")
	String errorConfigWithTooHighLimit(String configuration);
	
	@DefaultMessage("Configurations {0} have a higher limit than there is space available in their classes.")
	String errorConfigsWithTooHighLimit(String configurations);

	@DefaultMessage("Department is required.")
	String errorRequiredDepartment();
	
	@DefaultMessage("No instructors for the selected department were found.")
	String errorNoInstructorsFoundForDepartment();
	
	@DefaultMessage("No instructors were found. Use the option 'Manage Instructor List' to add instructors to your list.")
	String errorNoInstructorsFoundInSearch();
	
	@DefaultMessage("Supply one or more of the following information: Account Name / First Name / Last Name")
	String errorSupplyInfoForInstructorLookup();
	
	@DefaultMessage("Last Name is required.")
	String errorRequiredLastName();
	
	@DefaultMessage("This Instructor Id already exists in your instructor list.")
	String errorInstructorIdAlreadyExistsInList();
	
	@DefaultMessage("No matching records found")
	String errorNoMatchingRecordsFound();
	
	@DefaultMessage("No instructor was selected from the list")
	String errorNoInstructorSelectedFromList();
	
	@DefaultMessage("Instructor Info not supplied.")
	String exceptionInstructorInfoNotSupplied();
	
	@DefaultMessage("Access Denied.")
	String exceptionAccessDenied();
	
	@DefaultMessage("Operation could not be interpreted: ")
	String exceptionOperationNotInterpreted();
	
	@DefaultMessage("Missing Instructional Offering Config.")
	String exceptionMissingIOConfig();
	
	@DefaultMessage("Instructional Offering Config has not been defined.")
	String exceptionIOConfigUndefined();
	
	@DefaultMessage("Initial setup of Instructional Offering Config has not been completed.")
	String exceptionInitialIOSetupIncomplete();
	
	@DefaultMessage("Instructional Offering data was not correct: ")
	String exceptionIODataNotCorrect();
	
	@DefaultMessage("Generated method 'validate(...)' not implemented.")
	String exceptionValidateNotImplemented();
	
	@DefaultMessage("Course Offering Id need for operation. ")
	String exceptionCourseOfferingIdNeeded();
	
	@DefaultMessage("You do not have any department to manage. ")
	String exceptionNoDepartmentToManage();
	
	@DefaultMessage("Null Operation not supported.")
	String exceptionNullOperationNotSupported();
	
	@DefaultMessage("Name")
	String sortByName();
	
	@DefaultMessage("External Id")
	String sortByDivSec();
	
	@DefaultMessage("Enrollment")
	String sortByEnrollment();
	
	@DefaultMessage("Limit")
	String sortByLimit();

	@DefaultMessage("Room Size")
	String sortByRoomSize();

	@DefaultMessage("Date Pattern")
	String sortByDatePattern();

	@DefaultMessage("Time Pattern")
	String sortByTimePattern();

	@DefaultMessage("Instructor")
	String sortByInstructor();

	@DefaultMessage("Assigned Time")
	String sortByAssignedTime();

	@DefaultMessage("Assigned Room")
	String sortByAssignedRoom();

	@DefaultMessage("Assigned Room Capacity")
	String sortByAssignedRoomCapacity();

	@DefaultMessage("Classes ({0})")
	String backClasses(String classes);
	
	@DefaultMessage("Class ({0})")
	String backClass(String className);

	@DefaultMessage("Scheduling Subpart ({0})")
	String backSubpart(String subpartName);
	
	@DefaultMessage("Instructional Offering ({0})")
	String backInstructionalOffering(String ioName);
	
	@DefaultMessage("Instructors ({0})")
	String backInstructors(String deptName);
	
	@DefaultMessage("Instructor ({0})")
	String backInstructor(String instructorName);
	
	@DefaultMessage("Instructors")
	String backInstructors2(); //when there is no department selected in the Instructors screen
	
	@DefaultMessage("Not Found")
	String valueInstructorAccountNameNotFound();
	
	@DefaultMessage("Not Set")
	String valueNotSet();
	
	@DefaultMessage("am")
	String timeAm();
	
	@DefaultMessage("pm")
	String timePm();
	
	@DefaultMessage("None Required")
	String noConsentRequired();
	
	@DefaultMessage("No preferences found")
	String noPreferencesFound();
	
	@DefaultMessage("Class Assignment")
	String dialogClassAssignment();
	
	@DefaultMessage("Timetable")
	String sectionTitleTimetable();
	
	@DefaultMessage("Preferences")
	String sectionTitlePreferences();
	
	@DefaultMessage("Time Preferences")
	String sectionTitleTimePreferences();
	
	@DefaultMessage("Room Group Preferences")
	String sectionTitleRoomGroupPreferences();
	
	@DefaultMessage("Room Preferences")
	String sectionTitleRoomPreferences();
	
	@DefaultMessage("Building Preferences")
	String sectionTitleBuildingPreferences();
	
	@DefaultMessage("Room Feature Preferences")
	String sectionTitleRoomFeaturePreferences();
	
	@DefaultMessage("Examination Period Preferences")
	String sectionTitleExaminationPeriodPreferences();
	
	@DefaultMessage("Distribution Preferences")
	String sectionTitleDistributionPreferences();
	
	@DefaultMessage("Date Pattern Preferences")
	String sectionTitleDatePatternPreferences();
	
	@DefaultMessage("Instructors")
	String sectionTitleInstructors();
	
	@DefaultMessage("Requests / Notes to Schedule Manager")
	String sectionTitleNotesToScheduleManager();
	
	@DefaultMessage("Classes")
	String sectionTitleClasses();
	
	@DefaultMessage("Examinations")
	String sectionTitleExaminations();

	@DefaultMessage("Examination")
	String sectionTitleExamination();
	
	@DefaultMessage("Configuration ")
	String sectionTitleConfiguration();
	
	@DefaultMessage("Instructor List")
	String sectionTitleInstructorList();
	
	@DefaultMessage("Search Results")
	String sectionTitleSearchResults();
	
	@DefaultMessage("External Lookup Match")
	String sectionTitleExternalLookupMatch();
	
	@DefaultMessage("Staff File Matches")
	String sectionTitleStaffFileMatches();
	
	@DefaultMessage("Conflicting Classes")
	String sectionTitleClassConflicts();
	
	@DefaultMessage("Instructor Displayed")
	String titleInstructorDisplayed();
	
	@DefaultMessage("Instructor Not Displayed")
	String titleInstructorNotDisplayed();	
	
	@DefaultMessage("Enabled for Student Scheduling")
	String titleEnabledForStudentScheduling();

	@DefaultMessage("Disabled for Student Scheduling")
	String titleNotEnabledForStudentScheduling();
	
	@DefaultMessage("Are you sure you want to set room size to a value different from expected capacity? Continue?")
	String confirmRoomSizeDifferentFromCapacity();
	
	@DefaultMessage("Do you want to apply instructor preferences to this class?")
	String confirmApplyInstructorPreferencesToClass();
	
	@DefaultMessage("Do you want to remove any instructor preferences \\nthat may have been applied to this class?")
	String confirmRemoveInstructorPreferencesFromClass();
	
	@DefaultMessage("Do you really want to clear all class preferences?")
	String confirmClearAllClassPreferences();
	
	@DefaultMessage("This will create {0} classes. Continue?")
	String confirmCreateTooManyClasses();
	
	@DefaultMessage("This operation may result in deletion of existing subparts/classes . Continue?")
	String confirmMayDeleteSubpartsClasses();
	
	@DefaultMessage("This operation will delete existing subparts and associated classes . Continue?")
	String confirmDeleteExistingSubpartsClasses();
	
	@DefaultMessage("Do you really want to unassign all instructors?")
	String confirmUnassignAllInstructors();
	
	@DefaultMessage("Do you really want to make this offering offered?")
	String confirmMakeOffered();
	
	@DefaultMessage("Do you really want to make this offering not offered?")
	String confirmMakeNotOffered();
	
	@DefaultMessage("This option will delete all associated course offerings. \\nDo you really want to delete this offering?")
	String confirmDeleteIO();
	
	@DefaultMessage("Any instructor class assignments will be deleted as well. Continue?")
	String confirmDeleteInstructor();
	
	@DefaultMessage("Select an instructor")
	String alertSelectAnInstructor();

	@DefaultMessage("More Options &gt;&gt;&gt;")
	String selectMoreOptions();
	
	@DefaultMessage("&lt;&lt;&lt; Less Options")
	String selectLessOptions();
	
	@DefaultMessage("If checked, spread in time constraint will be automatically posted between classes of this subpart.")
	String descriptionAutomaticSpreadInTime();
	
	@DefaultMessage("If checked, students will be allowed to take classes from this subpart even when they are overlapping with other classes.")
	String descriptionStudentOverlaps();
	
	@DefaultMessage("Only course offerings that are not offered can be added into a cross-list.")
	String hintCrossLists();
	
	@DefaultMessage("If checked, only students meeting reservations will be allowed to enroll into the offering.")
	String descriptionByReservationOnly();
	
	@DefaultMessage("Only students meeting reservations are allowed to enroll into this offering.")
	String descriptionByReservationOnly2();
	
	@DefaultMessage("Yes")
	String yes();

	@DefaultMessage("No")
	String no();
	
	@DefaultMessage("Not Specified")
	String instructorPositionNotSpecified();

	@DefaultMessage("Not Specified")
	String instructorExternalIdNotSpecified();
	
	@DefaultMessage("New Enrollment Deadline:")
	String propertyLastWeekEnrollment();
	
	@DefaultMessage("Number of weeks during which students are allowed to enroll to this course, defaults to {0} when left blank.")
	String descriptionLastWeekEnrollment(String wkEnrollmentDefault);
	
	@DefaultMessage("Student are allowed to enroll to this course up to {0}. week of classes.")
	String textLastWeekEnrollment(String wkEnrollment);

	@DefaultMessage("Weeks start on the day of session start date, number of weeks is relative to class start ({0}).")
	String descriptionEnrollmentDeadlines(String weekStartDayOfWeek);

	@DefaultMessage("Class Changes Deadline:")
	String propertyLastWeekChange();
	
	@DefaultMessage("Number of weeks during which students are allowed to change existing enrollments, defaults to {0} when left blank.")
	String descriptionLastWeekChange(String wkChangeDefault);

	@DefaultMessage("Student are allowed to change existing enrollments up to {0}. week of classes.")
	String textLastWeekChange(String wkChange);

	@DefaultMessage("Course Drop Deadline:")
	String propertyLastWeekDrop();
	
	@DefaultMessage("Number of weeks during which students are allowed to drop from this course, defaults to {0} when left blank.")
	String descriptionLastWeekDrop(String wkDropDefault);
	
	@DefaultMessage("Student are allowed to drop from this course up to {0}. week of classes.")
	String textLastWeekDrop(String wkDrop);
	
	@DefaultMessage("if (showGwtLoading) { showGwtLoading('Locking {0}...'); } return true;")
	String jsSubmitLockIO(String instrOfferingName);

	@DefaultMessage("if (showGwtLoading) { showGwtLoading('Unlocking {0}...'); } return true;")
	String jsSubmitUnlockIO(String instrOfferingName);
	
	@DefaultMessage("Course {0} is locked (students are not able to enroll to this course).")
	String lockedCourse(String course);
	
	@DefaultMessage("Courses {0} and {1} are locked.")
	String lockedCourses(String course1, String course2);
	
	@DefaultMessage("{0} ({1} seats)")
	String labelLocationLabelWithCapacity(String label, int capacity);
	
	@DefaultMessage("Unassigned {0}")
	String classNoteUnassigned(String assignment);

	@DefaultMessage("Assigned {0}")
	String classNoteAssigned(String assignment);

	@DefaultMessage("Reassigned {0} >> {1}")
	String classNoteReassigned(String oldAssignment, String newAssignment);
	
	@DefaultMessage("N/A")
	String classMeetingsNotApplicable();
	
	@DefaultMessage("Area:")
	String propertyRoomArea();
	
	@DefaultMessage("square feet")
	String roomAreaUnitsLong();

	@DefaultMessage("ft&sup2;")
	String roomAreaUnitsShort();

	@DefaultMessage("Area [ft&sup2;]")
	String columnArea();
	
	@DefaultMessage("Area [ft2]")
	String columnAreaPDF();
	
	@DefaultMessage("square meters")
	String roomAreaMetricUnitsLong();

	@DefaultMessage("m&sup2;")
	String roomAreaMetricUnitsShort();

	@DefaultMessage("Area [m&sup2;]")
	String columnAreaMetric();
	
	@DefaultMessage("Area [m2]")
	String columnAreaMetricPDF();

	@DefaultMessage("System Default (Minutes per Week)")
	String systemDefaultDurationType();
	
	@DefaultMessage("Session Default ({0})")
	String sessionDefault(String value);
	
	@DefaultMessage("Class Duration:")
	String propertyClassDurationType();
	
	@DefaultMessage("Class {0} is cancelled.")
	String classNoteCancelled(String name);
	
	@DefaultMessage("Class {0} is reopened.")
	String classNoteReopened(String name);
	
	@DefaultMessage("Class {0} conflicts with {1}.")
	String classIsConflicting(String name, String conflicts);
	
	@DefaultMessage("Instructional Method:")
	String propertyInstructionalMethod();
	
	@DefaultMessage("Not Selected")
	String selectNoInstructionalMethod();
	
	@DefaultMessage("No matching date pattern!")
	String warnNoMatchingDatePattern();
}