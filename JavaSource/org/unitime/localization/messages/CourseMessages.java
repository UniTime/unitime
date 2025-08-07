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
	
	@DefaultMessage("Department:")
	String filterDepartment();
	
	@DefaultMessage("Subject Area:")
	String filterSubjectArea();
	
	@DefaultMessage("Number of Changes:")
	String filterNumberOfChanges();
	
	@DefaultMessage("Parent Class:")
	String propertyParentClass();
	
	@DefaultMessage("Parent Scheduling Subpart:")
	String propertyParentSchedulingSubpart();
	
	@DefaultMessage("External Id:")
	String propertyExternalId();
	
	@DefaultMessage("Enrollment:")
	String propertyEnrollment();
	
	@DefaultMessage("Snapshot Limit:")
	String propertySnapshotLimit();
	
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
	
	@DefaultMessage("Funding Department:")
	String propertyFundingDepartment();
	
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

	@DefaultMessage("Distribution Type:")
	String propertyDistributionType();

	@DefaultMessage("Structure:")
	String propertyDistributionStructure();

	@DefaultMessage("Preference:")
	String propertyDistributionPreference();
	
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
	
	@DefaultMessage("Default Alternative Course Offering:")
	String propertyAlternativeCourseOffering();
	
	@DefaultMessage("Instructional Offering Limit:")
	String propertyIOLimit();
	
	@DefaultMessage("Course Offerings:")
	String propertyCourseOfferings();
	
	@DefaultMessage("Scheduling Subpart Limits:")
	String propertySchedulingSubpartLimits();
	
	@DefaultMessage("Snapshot Limits:")
	String propertySchedulingSubpartSnapshotLimits();
	
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
	
	@DefaultMessage("Teaching Preference:")
	String propertyTeachingPreference();
	
	@DefaultMessage("Maximal Teaching Load:")
	String propertyMaxLoad();
	
	@DefaultMessage("Courses:")
	String propertyCoursePrefs();

	@DefaultMessage("Instructors:")
	String propertyInstructorPrefs();
	
	@DefaultMessage("Attributes:")
	String propertyAttributePrefs();
	
	@DefaultMessage("Need Instructor Assignment:")
	String propertyNeedInstructorAssignment();
	
	@DefaultMessage("Teaching Load:")
	String propertyTeachingLoad();
	
	@DefaultMessage("Number of Instructors:")
	String propertyNbrInstructors();
	
	@DefaultMessage("units")
	String teachingLoadUnits();
	
	@DefaultMessage("N/A")
	String cellNoInstructorAssignment();
	
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
	String dropDistrPrefAll();
	
	@DefaultMessage("All")
	String dropManagerAll();
	
	@DefaultMessage("Department")
	String dropDeptDepartment();
	
	@DefaultMessage("All")
	String dropITypeAll();
	
	@DefaultMessage("Default")
	String dropDefaultDatePattern();
	
	@DefaultMessage("Default")
	String dropDefaultLearningManagementSystem();
	
	@DefaultMessage("Name")
	String columnName();
	
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
	
	@DefaultMessage("Instructor Scheduling")
	String includeInstructorScheduling();
	
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
	
	@DefaultMessage("Funding Department")
	String columnFundingDepartment();
	
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
	
	@DefaultMessage("Capacity")
	String columnExamRoomCapacity();
	
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
	
	@DefaultMessage("Attribute")
	String columnInstructorAttributePref();
	
	@DefaultMessage("Instructor")
	String columnInstructorPref();
	
	@DefaultMessage("Course")
	String columnCoursePref();

	@DefaultMessage("Room")
	String columnAllRoomPref();
	
	@DefaultMessage("Time")
	String columnAssignedTime();
	
	@DefaultMessage("Room")
	String columnAssignedRoom();
	
	@DefaultMessage("Room Cap")
	String columnAssignedRoomCapacity();
	
	@DefaultMessage("Date")
	String columnAssignedDatePattern();
	
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
	
	@DefaultMessage("Type")
	String columnDistrPrefType();
	
	@DefaultMessage("Structure")
	String columnDistrPrefStructure();

	@DefaultMessage("Owner")
	String columnDistrPrefOwner();
	
	@DefaultMessage("Class")
	String columnDistrPrefClass();
	
	@DefaultMessage("Preference")
	String columnDistrPrefLevel();
	
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
	
	@DefaultMessage("Split<br>Attendance")
	String columnRoomSplitAttendance();
	
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
	
	@DefaultMessage("Enrollment")
	String columnEnrollment();
	
	@DefaultMessage("Snapshot Limit")
	String columnSnapshotLimit();
	
	@DefaultMessage("Snapshot<br>Limit")
	String columnSnapshotLimitBr();
	
	@DefaultMessage("Nbr<br>Rms")
	String columnNbrRms();
	
	@DefaultMessage("Splt<br>Attd")
	String columnSplitAttnd();
	
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
	
	@DefaultMessage("Alternative")
	String columnAlternativeCourse();
	
	@DefaultMessage("Login")
	String columnLogin();
	
	@DefaultMessage("Department")
	String columnDepartment();
	
	@DefaultMessage("Type")
	String columnCourseType();
	
	@DefaultMessage("Credit")
	String columnCredit();
	
	@DefaultMessage("Name")
	String columnEventName();
	
	@DefaultMessage("Type")
	String columnEventType();
	
	@DefaultMessage("Date")
	String columnEventDate();
	
	@DefaultMessage("Time")
	String columnEventTime();
	
	@DefaultMessage("Teaching<BR>Preference")
	String columnTeachingPreference();
	
	@DefaultMessage("Teaching\nPreference")
	String columnTeachingPreferencePDF();
	
	@DefaultMessage("Maximal<BR>Load")
	String columnMaxTeachingLoad();
	
	@DefaultMessage("Maximal\nLoad")
	String columnMaxTeachingLoadPDF();
	
	@DefaultMessage("Teaching Load")
	String columnTeachingLoad();
	
	@DefaultMessage("Responsibility")
	String columnTeachingResponsibility();
	
	@DefaultMessage("Enrollment Conflict")
	String columnEnrollmentConflict();
	
	@DefaultMessage("LMS Integration")
	String columnLms();
	
	@DefaultMessage("Wait-Listing")
	String columnWaitlistMode();

	@DefaultMessage("LMS Integration:")
	String propertyLms();

	@DefaultMessage("Funding Dept")
	String columnFundingDept();

	@DefaultMessage("Date")
	String columnDate();

	@DefaultMessage("Subject")
	String columnSubject();

	@DefaultMessage("Page")
	String columnPage();

	@DefaultMessage("Object")
	String columnObject();

	@DefaultMessage("Operation")
	String columnOperation();

	@DefaultMessage("Funding Dept:")
	String propertyFundingDept();

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
	
	@DefaultMessage("{0} Wait-Listed Courses")
	String labelOfferedWaitListedCourses(String subjectAreaAbbv);
	
	@DefaultMessage("{0} Not Wait-Listed Courses")
	String labelOfferedNotWaitListedCourses(String subjectAreaAbbv);
	
	@DefaultMessage("{0} Courses Allowing Re-Scheduling")
	String labelOfferedCoursesAllowingReScheduling(String subjectAreaAbbv);
	
	@DefaultMessage("{0} Not Wait-Listed Courses Allowing Re-Scheduling")
	String labelOfferedNotWaitListedCoursesAllowingReScheduling(String subjectAreaAbbv);
	
	@DefaultMessage("{0} Courses Not Allowing Re-Scheduling")
	String labelOfferedCoursesNotAllowingReScheduling(String subjectAreaAbbv);
	
	
	@DefaultMessage("There are no courses currently offered for {0} subject.")
	String errorNoCoursesOffered(String subjectAreaAbbv);

	@DefaultMessage("All courses are currently being offered for {0} subject.")
	String errorAllCoursesOffered(String subjectAreaAbbv);
	
	@DefaultMessage("No preferences found")
	String errorNoDistributionPreferencesFound();
	
	@DefaultMessage("Sort By:")
	String filterSortBy();
	
	@DefaultMessage("Cross Lists:")
	String filterCrossList();
	
	@DefaultMessage("Show cross-listed classes")
	String showCrossListedClasses();
	
	@DefaultMessage("Cancelled Classes:")
	String filterCancelledClasses();
	
	@DefaultMessage("Show cancelled classes")
	String showCancelledClasses();
	
	@DefaultMessage("Need Instructor Assignment:")
	String filterNeedInstructorAssignment();
	
	@DefaultMessage("Show only classes that need instructor assignment")
	String showNeedInstructorClasses();
	
	@DefaultMessage("Subject:")
	String filterSubject();

	@DefaultMessage("Course Number:")
	String filterCourseNumber();

	@DefaultMessage("Search")
	String actionSearchInstructionalOfferings();
	
	@DefaultMessage("Search")
	String actionSearchDistributionPreferences();
	
	@DefaultMessage("Search")
	String actionSearchClasses();
	
	@DefaultMessage("Search")
	String actionSearchClassAssignments();

	@DefaultMessage("Search")
	String actionSearchInstructors();

	@DefaultMessage("Export PDF")
	String actionExportPdf();

	@DefaultMessage("Export CSV")
	String actionExportCsv();

	@DefaultMessage("Worksheet PDF")
	String actionWorksheetPdf();
	
	@DefaultMessage("Surveys XLS")
	String actionExportSurveysXLS();

	@DefaultMessage("Add New")
	String actionAddNewInstructionalOffering();
	
	@DefaultMessage("Edit Class")
	String actionEditClass();

	@DefaultMessage("Edit Instructor")
	String actionEditInstructor();	
	
	@DefaultMessage("Edit Preferences")
	String actionEditInstructorPreferences();
	
	@DefaultMessage("Add Distribution Preference")
	String actionAddDistributionPreference();

	@DefaultMessage("Add New Distribution Preference")
	String actionAddNewDistributionPreference();
	
	@DefaultMessage("Add Class")
	String actionAddClassToDistribution();
	
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
	String actionBackDistributionPreference();
	
	@DefaultMessage("Back")
	String actionBackSubpartDetail();

	@DefaultMessage("Back")
	String actionBackInstructorDetail();

	@DefaultMessage("Back")
	String actionBackToDetail();
	
	@DefaultMessage("Back")
	String actionBackToIODetail();
	
	@DefaultMessage("Update")
	String actionUpdatePreferences();
	
	@DefaultMessage("Update")
	String actionUpdateDistributionPreference();
	
	@DefaultMessage("Save")
	String actionSaveNewDistributionPreference();
	
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
	
	@DefaultMessage("Add Course Preference")
	String actionAddCoursePreference();
	
	@DefaultMessage("Delete")
	String actionRemoveCoursePreference();
	
	@DefaultMessage("Add Instructor Preference")
	String actionAddInstructorPreference();
	
	@DefaultMessage("Delete")
	String actionRemoveInstructorPreference();

	@DefaultMessage("Add Attribute Preference")
	String actionAddAttributePreference();
	
	@DefaultMessage("Delete")
	String actionRemoveAttributePreference();

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
	
	@DefaultMessage("Delete")
	String actionDeleteDistributionPreference();
	
	@DefaultMessage("Show Preferences")
	String actionDisplayInstructorPreferences();

	@DefaultMessage("Hide Preferences")
	String actionHideInstructorPreferences();
	
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
	
	@DefaultMessage("Delete")
	String actionDelete();
	
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
	
	@DefaultMessage("Edit Assignment Preferences")
	String actionEditInstructorAssignmentPreferences();
	
	@DefaultMessage("Instructor Assignment Preferences")
	String actionEditClassInstructorAssignmentPreferences();
	
	@DefaultMessage("Instructor Assignment Preferences")
	String actionEditSubpartInstructorAssignmentPreferences();
	
	@DefaultMessage("S")
	String accessSearchInstructionalOfferings();
	
	@DefaultMessage("S")
	String accessSearchClasses();
	
	@DefaultMessage("S")
	String accessSearchInstructors();
	
	@DefaultMessage("S")
	String accessSearchDistributionPreferences();

	@DefaultMessage("P")
	String accessExportPdf();
	
	@DefaultMessage("C")
	String accessExportCsv();

	@DefaultMessage("W")
	String accessWorksheetPdf();

	@DefaultMessage("A")
	String accessAddNewInstructionalOffering();
	
	@DefaultMessage("E")
	String accessEditClass();

	@DefaultMessage("I")
	String accessEditInstructor();

	@DefaultMessage("P")
	String accessEditInstructorPreferences();
	
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
	String accessBackDistributionPreference();
	
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
	
	@DefaultMessage("U")
	String accessUpdateDistributionPreference();

	@DefaultMessage("S")
	String accessSaveNewDistributionPreference();
	
	@DefaultMessage("S")
	String accessSaveCourseOffering();
	
	@DefaultMessage("U")
	String accessUpdateCrossLists();
	
	@DefaultMessage("U")
	String accessUpdateMultipleClassSetup();
	
	@DefaultMessage("C")
	String accessClearClassPreferences();
	
	@DefaultMessage("C")
	String accessAddClassToDistribution();

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

	@DefaultMessage("A")
	String accessAddNewDistributionPreference();
	
	@DefaultMessage("H")
	String accessHideInstructorPreferences();

	@DefaultMessage("S")
	String accessShowInstructorPreferences();

	@DefaultMessage("X")
	String accessAddExamination();
	
	@DefaultMessage("E")
	String accessEditSubpart();
	
	@DefaultMessage("D")
	String accessDeleteConfiguration();
	
	@DefaultMessage("D")
	String accessDeleteDistributionPreference();
	
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
	
	@DefaultMessage("B")
	String accessBackInstructorDetail();
	
	@DefaultMessage("C")
	String accessAddCoursePreference();
	
	@DefaultMessage("A")
	String accessAddAttributePreference();
	
	@DefaultMessage("I")
	String accessAddInstructorPreference();
	
	@DefaultMessage("A")
	String accessEditInstructorAssignmentPreferences();

	@DefaultMessage("I")
	String accessEditClassInstructorAssignmentPreferences();

	@DefaultMessage("I")
	String accessEditSubpartInstructorAssignmentPreferences();

	@DefaultMessage("Search/Display Offerings (Alt+{0})")
	String titleSearchInstructionalOfferings(String accessKey);
	
	@DefaultMessage("Search Distribution Preferences (Alt+{0})")
	String titleSearchDistributionPreferences(String accessKey);

	@DefaultMessage("Search/Display Classes (Alt+{0})")
	String titleSearchClasses(String accessKey);
	
	@DefaultMessage("Search/Display Instructors (Alt+{0})")
	String titleSearchInstructors(String accessKey);
	
	@DefaultMessage("Export PDF (Alt+{0})")
	String titleExportPdf(String accessKey);

	@DefaultMessage("Export CSV (Alt+{0})")
	String titleExportCsv(String accessKey);

	@DefaultMessage("Export Worksheet PDF (Alt+{0})")
	String titleWorksheetPdf(String accessKey);

	@DefaultMessage("Add New Offering (Alt+{0})")
	String titleAddNewInstructionalOffering(String accessKey);
	
	@DefaultMessage("Edit Class (Alt+{0})")
	String titleEditClass(String accessKey);
	
	@DefaultMessage("Add Class (Alt+{0})")
	String titleAddClassToDistribution(String accessKey);

	@DefaultMessage("Edit Instructor (Alt+{0})")
	String titleEditInstructor(String accessKey);
	
	@DefaultMessage("Edit Instructor Preferences(Alt+{0})")
	String titleEditInstructorPreferences(String accessKey);
	
	@DefaultMessage("Move Up")
	String titleMoveUp();
	
	@DefaultMessage("Move Down")
	String titleMoveDown();
	
	@DefaultMessage("Add Distribution Preference (Alt+{0})")
	String titleAddDistributionPreference(String accessKey);
	
	@DefaultMessage("Add New Distribution Preference (Alt+{0})")
	String titleAddNewDistributionPreference(String accessKey);
	
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
	
	@DefaultMessage("Go to previous Instructor (Alt+{0})")
	String titlePreviousInstructor(String accessKey);
	
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

	@DefaultMessage("Go to next Instructor (Alt+{0})")
	String titleNextInstructor(String accessKey);
	
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
	
	@DefaultMessage("Update Distribution Preference (Alt+{0})")
	String titleUpdateDistributionPreference(String accessKey);

	@DefaultMessage("Save New Distribution Preference (Alt+{0})")
	String titleSaveNewDistributionPreference(String accessKey);
	
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
	
	@DefaultMessage("Delete all attribute and instructor preferences that are set directly on this class. Instructor assignment preferences will be inherited from the subpart (Alt+{0})")
	String titleClearClassInstructorAssignmentPreferences(String accessKey);
	
	@DefaultMessage("Delete all Subpart Preferences (Alt+{0})")
	String titleClearSubpartPreferences(String accessKey);
	
	@DefaultMessage("Delete all attribute and instructor preferences that are on this scheduling subpart (Alt+{0})")
	String titleClearSubpartInstructorAssignmentPreferences(String accessKey);
	
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
	
	@DefaultMessage("Delete Distribution Preference (Alt+{0})")
	String titleDeleteDistributionPreference(String accessKey);
	
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
	
	@DefaultMessage("Return to %% (Alt+{0})")
	String titleBackDistributionPreference(String accessKey);	
	
	@DefaultMessage("Controlling Course")
	String titleControllingCourse();
	
	@DefaultMessage("Edit Course Offering")
	String titleEditCourseOffering();
	
	@DefaultMessage("Add Course Offering")
	String titleAddCourseOffering();
	
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
	String titleBackInstructorDetail(String accessKey);
	
	@DefaultMessage("Back to Instructors (Alt+{0})")
	String titleBackToInstructors(String accessKey);
	
	@DefaultMessage("Cancel this class.")
	String titleCancelClass();
	
	@DefaultMessage("Reopen this class.")
	String titleReopenClass();
	
	@DefaultMessage("Add Course Preference (Alt+{0})")
	String titleAddCoursePreference(String accessKey);
	
	@DefaultMessage("Add Attribute Preference (Alt+{0})")
	String titleAddAttributePreference(String accessKey);
	
	@DefaultMessage("Add Instructor Preference (Alt+{0})")
	String titleAddInstructorPreference(String accessKey);

	@DefaultMessage("Remove Course Preference")
	String titleRemoveCoursePreference();
	
	@DefaultMessage("Remove Attribute Preference")
	String titleRemoveAttributePreference();
	
	@DefaultMessage("Remove Instructor Preference")
	String titleRemoveInstructorPreference();
	
	@DefaultMessage("Edit Instructor Assignment Preferences (Alt+{0})")
	String titleEditInstructorAssignmentPreferences(String accessKey);
	
	@DefaultMessage("Edit Instructor Assignment Preferences (Alt+{0})")
	String titleEditClassInstructorAssignmentPreferences(String accessKey);
	
	@DefaultMessage("Edit Instructor Assignment Preferences (Alt+{0})")
	String titleEditSubpartInstructorAssignmentPreferences(String accessKey);
	
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
	
	@DefaultMessage("Select a distribution type.")
	String errorSelectDistributionType();

	@DefaultMessage("Select a preference level.")
	String errorSelectDistributionPreferenceLevel();
	
	@DefaultMessage("Invalid class selections: Check for duplicate / blank selection.")
	String errorInvalidClassSelectionDP();
	
	@DefaultMessage("Invalid class selections: Select at least one subpart.")
	String errorInvalidClassSelectionDPSubpart();
	
	@DefaultMessage("Invalid class selections: Select at least two classes.")
	String errorInvalidClassSelectionDPMinTwoClasses();

	@DefaultMessage("Invalid class selections: An individual class cannot be selected if the entire subpart has been selected.")
	String errorInvalidClassSelectionDPIndividualClass();
	
	@DefaultMessage("ERRORS") //preferable to use this than errors for each page
	String errors();
	
	@DefaultMessage("No classes exist for the given subpart")
	String errorNoClassesExist();
	
	@DefaultMessage("No subparts exist for the given course")
	String errorNoSupbartsExist();
	
	@DefaultMessage("ERRORS")
	String errorsClassDetail();

	@DefaultMessage("ERRORS")
	String errorsInstructorDetail();
	
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
	
	@DefaultMessage("Invalid room group: Check for duplicate / blank selection.")
	String errorInvalidRoomGroup();
	
	@DefaultMessage("Invalid room group level.")
	String errorInvalidRoomGroupLevel();
	
	@DefaultMessage("Invalid building preference: Check for duplicate / blank selection.")
	String errorInvalidBuildingPreference();
	
	@DefaultMessage("Invalid building preference level.")
	String errorInvalidBuildingPreferenceLevel();
	
	@DefaultMessage("Invalid distribution preference: Check for duplicate / blank selection.")
	String errorInvalidDistributionPreference();
	
	@DefaultMessage("Invalid date pattern preference: Check for duplicate / blank selection.")
	String errorInvalidDatePatternPreference();

	@DefaultMessage("Invalid distribution preference level.")
	String errorInvalidDistributionPreferenceLevel();
	
	@DefaultMessage("Invalid room feature preference: Check for duplicate / blank selection.")
	String errorInvalidRoomFeaturePreference();
	
	@DefaultMessage("Invalid room feature preference level.")
	String errorInvalidRoomFeaturePreferenceLevel();

	@DefaultMessage("Invalid instructors: Check for duplicate / blank selection.")
	String errorInvalidInstructors();
	
	@DefaultMessage("Time pattern not selected.")
	String errorTimePatternNotSelected();
	
	@DefaultMessage("Invalid room preference: Check for duplicate / blank selection.")
	String errorInvalidRoomPreference();
	
	@DefaultMessage("Invalid room preference level.")
	String errorInvalidRoomPreferenceLevel();
	
	@DefaultMessage("Invalid course preference: Check for duplicate / blank selection.")
	String errorInvalidCoursePreference();
	
	@DefaultMessage("Invalid course preference level.")
	String errorInvalidCoursePreferenceLevel();
	
	@DefaultMessage("Invalid attribute preference: Check for duplicate / blank selection.")
	String errorInvalidAttributePreference();
	
	@DefaultMessage("Invalid attribute preference level.")
	String errorInvalidAttributePreferenceLevel();
	
	@DefaultMessage("Invalid instructor preference: Check for duplicate / blank selection.")
	String errorInvalidInstructorPreference();
	
	@DefaultMessage("Invalid instructor preference level.")
	String errorInvalidInstructorPreferenceLevel();

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
	
	@DefaultMessage("Unique Id needed for operation.")
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
	
	@DefaultMessage("<b>Teaching Load</b> is required.")
	String errorNoTeachingLoad();
	
	@DefaultMessage("<b>Number of Instructors</b> is required.")
	String errorNoNbrInstructors();
	
	@DefaultMessage("Duplicate coordinator for a course.")
	String errorDuplicateCoordinator();
	
	@DefaultMessage("Wait-listing cannot be enabled when {0} is allowed.")
	String errorWaitListingOverrideMustBeProhibited(String override);
	
	@DefaultMessage("This instructor is allowed to teach two back-to-back classes that are too far away.")
	String descriptionInstructorIgnoreTooFar();
	
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
	
	@DefaultMessage("Course Offering Id need for operation.")
	String exceptionCourseOfferingIdNeeded();
	
	@DefaultMessage("You do not have any department to manage.")
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

	@DefaultMessage("Distribution Preferences")
	String backDistributionPreferences();
		
	@DefaultMessage("Class Assignments ({0})")
	String backClassAssignments(String classes);
	
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
	
	@DefaultMessage("Classes in Distribution")
	String sectionTitleClassesInDistribution();
	
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
	
	@DefaultMessage("Add Distribution Preference")
	String sectionTitleAddDistributionPreference();
	
	@DefaultMessage("Edit Distribution Preference")
	String sectionTitleEditDistributionPreference();
	
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
	
	@DefaultMessage("Configuration")
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
	
	@DefaultMessage("Class Assignments")
	String sectionTitleClassAssignments();
	
	@DefaultMessage("Conflicting Meetings")
	String sectionTitleEventConflicts();
	
	@DefaultMessage("Teaching Properties")
	String sectionTeachingProperties();
	
	@DefaultMessage("Course Preferences")
	String sectionTitleCoursePreferences();

	@DefaultMessage("Attribute Preferences")
	String sectionTitleAttributePreferences();

	@DefaultMessage("Instructor Preferences")
	String sectionTitleInstructorPreferences();

	@DefaultMessage("Instructor Displayed")
	String titleInstructorDisplayed();
	
	@DefaultMessage("Instructor Not Displayed")
	String titleInstructorNotDisplayed();	
	
	@DefaultMessage("Attributes")
	String sectionAttributes();

	@DefaultMessage("Hide Instructor Preferences (Alt+{0})")
	String titleHideInstructorPreferences(String accessKey);	

	@DefaultMessage("Show Instructor Preferences (Alt+{0})")
	String titleShowInstructorPreferences(String accessKey);	
	
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
	String confirmCreateTooManyClasses(int number);
	
	@DefaultMessage("This operation may result in deletion of existing subparts/classes. Continue?")
	String confirmMayDeleteSubpartsClasses();
	
	@DefaultMessage("This operation will delete existing subparts and associated classes. Continue?")
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
	
	@DefaultMessage("Do you really want to delete this distribution preference?")
	String confirmDeleteDistributionPreference();
	
	@DefaultMessage("Select an instructor")
	String alertSelectAnInstructor();

	@DefaultMessage("More Options >>>")
	String selectMoreOptions();
	
	@DefaultMessage("<<< Less Options")
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
	
	@DefaultMessage("If checked, classes from this subpart will be included in the Instructor Scheduling problem.")
	String descriptionSubpartNeedInstructorAssignment();
	
	@DefaultMessage("If checked, this class will be included in the Instructor Scheduling problem.")
	String descriptionClassNeedInstructorAssignment();
	
	@DefaultMessage("Set to Prohibited when disabled for Instructor Scheduling.")
	String descriptionTeachingPreference();
	
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
	
	@DefaultMessage("displayLoading(''Locking {0}...''); return true;")
	String jsSubmitLockIO(String instrOfferingName);

	@DefaultMessage("displayLoading(''Unlocking {0}...''); return true;")
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
	
	@DefaultMessage("{0} uncommitted")
	String classNoteUncommitted(String problem);
	
	@DefaultMessage("{0} committed")
	String classNoteCommitted(String problem);
	
	@DefaultMessage("{0} committed, class was removed or unassigned")
	String classNoteCommittedClassRemoved(String problem);

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
	
	@DefaultMessage("Class {0} - Date Pattern: {1} used in commited solution does not match current date pattern: {2}.  Use the \u201cAssign\u201d button to update the solution.")
	String datePatternCommittedIsDifferent(String name, String commitedDatePattern, String currentDatePattern);

	@DefaultMessage("Instructional Method:")
	String propertyInstructionalMethod();
	
	@DefaultMessage("Not Selected")
	String selectNoInstructionalMethod();
	
	@DefaultMessage("Default ({0})")
	String defaultInstructionalMethod(String defaultInstructionalMethod);
	
	@DefaultMessage("No matching date pattern!")
	String warnNoMatchingDatePattern();
	
	@DefaultMessage("One or more classes of this instructional offering are in a conflict.")
	String warnOfferingHasConflictingClasses();
	
	@DefaultMessage("Managed As {0}")
	String crossListManagedAs(String course);
	
	@DefaultMessage("Class Assignments")
	String classAssignmentsAdditionalNote();
	
	@DefaultMessage("Cancelled")
	String statusCancelled();
	
	@DefaultMessage("The constraint will apply to all classes in the selected distribution set. "+
			"For example, a Back-to-Back constraint among three classes seeks to place all three classes "+
			"sequentially in time such that there are no intervening class times (transition time between "+
			"classes is taken into account, e.g., if the first class ends at 8:20, the second has to start at 8:30).")
	String distributionStructureDescriptionAllClasses();
	
	@DefaultMessage("The distribution constraint is created between classes in one scheduling subpart and the "+
			"appropriate class(es) in one or more other subparts. This structure links child and parent "+
			"classes together if subparts have been grouped. Otherwise the first class in one subpart is "+
			"linked to the the first class in the second subpart, etc.")
	String distributionStructureDescriptionProgressive();
	
	@DefaultMessage("The distribution constraint is applied only on subsets containing two classes in the selected "+
			"distribution set.  A constraint is posted between the first two classes (in the order listed), "+
			"then between the second two classes, etc.")
	String distributionStructureDescriptionGroupsOfTwo();
	
	@DefaultMessage("The distribution constraint is applied only on subsets containing three classes in the selected "+
			"distribution set.  A constraint is posted between the first three classes (in the order listed), "+
			"then between the second three classes, etc.")
	String distributionStructureDescriptionGroupsOfThree();
	
	@DefaultMessage("The distribution constraint is applied only on subsets containing four classes in the selected "+
			"distribution set.  A constraint is posted between the first four classes (in the order listed), "+
			"then between the second four classes, etc.")
	String distributionStructureDescriptionGroupsOfFour();

	@DefaultMessage("The distribution constraint is applied only on subsets containing five classes in the selected "+
			"distribution set.  A constraint is posted between the first five classes (in the order listed), "+
			"then between the second five classes, etc.")
	String distributionStructureDescriptionGroupsOfFive();
	
	@DefaultMessage("The distribution constraint is created between every pair of classes in the selected distribution set. "+
			"Therefore, if n classes are in the set, n(n-1)/2 constraints will be posted among the classes. "+
			"This structure should not be used with \"required\" or \"prohibited\" preferences on sets containing "+
			"more than a few classes.")
	String distributionStructureDescriptionPairwise();
	
	@DefaultMessage("The distribution constraint is created for each combination of classes such that one class is taken from each " +
			"line representing a class or a scheduling subpart. " +
			"For instance, if the constraint is put between three scheduling subparts, a constraint will be posted between " +
			"each combination of three classes, each from one of the three subparts. If a constraint is put between a class and " +
			"a scheduling subpart, there will be a binary constraint posted between the class and each of the classes of the scheduling subpart.")
	String distributionStructureDescriptionOneOfEach();
	
	@DefaultMessage("All Classes")
	String distributionStructureNameAllClasses();
	
	@DefaultMessage("Progressive")
	String distributionStructureNameProgressive();

	@DefaultMessage("Groups of Two")
	String distributionStructureNameGroupsOfTwo();

	@DefaultMessage("Groups of Three")
	String distributionStructureNameGroupsOfThree();

	@DefaultMessage("Groups of Four")
	String distributionStructureNameGroupsOfFour();

	@DefaultMessage("Groups Of Five")
	String distributionStructureNameGroupsOfFive();

	@DefaultMessage("Pairwise")
	String distributionStructureNamePairwise();

	@DefaultMessage("One Of Each")
	String distributionStructureNameOneOfEach();

	@DefaultMessage("{0}")
	String distributionStructureLabelAllClasses(String content);

	@DefaultMessage("{0} Progressive")
	String distributionStructureLabelProgressive(String content);

	@DefaultMessage("{0} Groups of Two")
	String distributionStructureLabelGroupsOfTwo(String content);

	@DefaultMessage("{0} Groups of Three")
	String distributionStructureLabelGroupsOfThree(String content);

	@DefaultMessage("{0} Groups of Four")
	String distributionStructureLabelGroupsOfFour(String content);

	@DefaultMessage("{0} Groups Of Five")
	String distributionStructureLabelGroupsOfFive(String content);

	@DefaultMessage("{0} Pairwise")
	String distributionStructureLabelPairwise(String content);

	@DefaultMessage("{0} One Of Each")
	String distributionStructureLabelOneOfEach(String content);
	
	@DefaultMessage("{0}")
	String distributionStructureAbbreviationAllClasses(String content);

	@DefaultMessage("{0} Prg")
	String distributionStructureAbbreviationProgressive(String content);

	@DefaultMessage("{0} Go2")
	String distributionStructureAbbreviationGroupsOfTwo(String content);

	@DefaultMessage("{0} Go3")
	String distributionStructureAbbreviationGroupsOfThree(String content);

	@DefaultMessage("{0} Go4")
	String distributionStructureAbbreviationGroupsOfFour(String content);

	@DefaultMessage("{0} Go5")
	String distributionStructureAbbreviationGroupsOfFive(String content);

	@DefaultMessage("{0} Pair")
	String distributionStructureAbbreviationPairwise(String content);

	@DefaultMessage("{0} OoE")
	String distributionStructureAbbreviationOneOfEach(String content);
	
	@DefaultMessage("Distribution Preferences")
	String pageTitleDistributionPreferencesPdf();
	
	@DefaultMessage("class")
	String prefOwnerClass();

	@DefaultMessage("scheduling subpart")
	String prefOwnerSchedulingSubpart();
	
	@DefaultMessage("instructor")
	String prefOwnerInstructor();
	
	@DefaultMessage("department")
	String prefOwnerDepartment();
	
	@DefaultMessage("examination")
	String prefOwnerExamination();
	
	@DefaultMessage("session")
	String prefOwnerSession();
	
	@DefaultMessage("combined")
	String prefOwnerCombined();
	
	@DefaultMessage("{0} Room {1}")
	String prefTitleRoom(String preference, String owner);
	
	@DefaultMessage("{0} Room Group {1}")
	String prefTitleRoomGroup(String preference, String owner);
	
	@DefaultMessage("{0} Room Feature {1}")
	String prefTitleRoomFeature(String preference, String owner);
	
	@DefaultMessage("{0} Building {1}")
	String prefTitleBuilding(String preference, String owner);
	
	@DefaultMessage("Room")
	String prefRoom();
	
	@DefaultMessage("Building")
	String prefBuilding();
	
	@DefaultMessage("<font color='red'><B>DISABLED</B></font><i> -- Classes of this subpart may be timetabled during overlapping times.</i>")
	String classDetailNoSpread();
	
	@DefaultMessage("<font color='red'><B>ENABLED</B></font><i> -- Students are allowed to take classes from this subpart even when they overlap with other classes.</i>")
	String classDetailAllowOverlap();
	
	@DefaultMessage("<font color='green'><B>ENABLED</B></font><i> -- Classes from this subpart will be included in the Instructor Scheduling problem.</i>")
	String subpartDetailNeedInstructorAssignment();
	
	@DefaultMessage("<font color='green'><B>ENABLED</B></font><i> -- This class will be included in the Instructor Scheduling problem.</i>")
	String classDetailNeedInstructorAssignment();
	
	@DefaultMessage("<font color='red'><B>DISABLED</B></font><i> -- This class will NOT be included in the Instructor Scheduling problem.</i>")
	String classDetailNoInstructorAssignment();

	@DefaultMessage("(defaults to {0} when blank)")
	String classEditNbrRoomsDefault(String defaultValue);
	
	@DefaultMessage("(defaults to {0} when blank)")
	String classEditTeachingLoadDefault(String defaultValue);
	
	@DefaultMessage("lead")
	String toolTipInstructorLead();
	
	@DefaultMessage(" - Do Not Display Instructor.")
	String toolTipInstructorDoNotDisplay();
	
	@DefaultMessage("No class given.")
	String errorNoClassGiven();
	
	@DefaultMessage("Assign")
	String actionClassAssign();
	
	@DefaultMessage("Apply")
	String actionFilterApply();
	
	@DefaultMessage("Open Class Detail for {0} in a new window.")
	String titleOpenClassDetail(String classNam);
	
	@DefaultMessage("Conflict Checked Instructor(s):")
	String properyConflictCheckedInstructors();
	
	@DefaultMessage("Assigned Dates:")
	String properyAssignedDates();
	
	@DefaultMessage("Selected Dates:")
	String properySelectedDates();
	
	@DefaultMessage("Selected Time:")
	String properySelectedTime();
	
	@DefaultMessage("Selected Room:")
	String properySelectedRoom();
	
	@DefaultMessage("New Assignment(s)")
	String sectionTitleNewAssignments();
	
	@DefaultMessage("Do not unassign conflicting classes:")
	String toggleDoNotUnassignConflictingClasses();
	
	@DefaultMessage("Student Conflicts")
	String sectionTitleStudentConflicts();
	
	@DefaultMessage("There are no students enrolled yet, showing solution conflicts instead.")
	String messageNoStudentsEnrolledYetUsingSolutionConflicts();
	
	@DefaultMessage("Available Dates for {0}")
	String sectionTitleAvailableDatesForClass(String className);
	
	@DefaultMessage("Available Times for {0}")
	String sectionTitleAvailableTimesForClass(String className);
	
	@DefaultMessage("No times available.")
	String messageNoTimesAvailable();
	
	@DefaultMessage("Available Rooms for {0}")
	String sectionTitleAvailableRoomsForClass(String className);
	
	@DefaultMessage("selected size:")
	String messageSelectedSize();
	
	@DefaultMessage("of")
	String messageSelectedSizeOf();
	
	@DefaultMessage("Size:")
	String properyRoomSize();
	
	@DefaultMessage("Filter:")
	String properyRoomFilter();
	
	@DefaultMessage("Allow conflicts:")
	String properyRoomAllowConflicts();
	
	@DefaultMessage("Order:")
	String propertyRoomOrder();
	
	@DefaultMessage("Room Types:")
	String propertyRoomTypes();
	
	@DefaultMessage("No room matching the above criteria was found.")
	String messageNoMatchingRoomFound();
	
	@DefaultMessage("Name [asc]")
	String sortRoomNameAsc();
	
	@DefaultMessage("Name [desc]")
	String sortRoomNameDesc();
	
	@DefaultMessage("Size [asc]")
	String sortRoomSizeAsc();
	
	@DefaultMessage("Size [desc]")
	String sortRoomSizeDesc();
	
	@DefaultMessage("Departmental")
	String roomTypeDepartmental();
	
	@DefaultMessage("Timetabling")
	String roomTypeTimetabling();
	
	@DefaultMessage("All")
	String roomTypeAll();
	
	@DefaultMessage("Nothing to assign.")
	String errorNothingToAssign();
	
	@DefaultMessage("It is not allowed to keep a class unassigned.")
	String errorNotAllowedToKeepClassUnassigned();
	
	@DefaultMessage("Unassignment of {0} failed, reason: {1}")
	String errorUnassignmentFailed(String className, String reason);
	
	@DefaultMessage("Assignment of {0} to {1} failed, reason: {2}")
	String errorAssignmentFailed(String className, String placement, String reason);
	
	@DefaultMessage("The selected assignment will be done directly in the database. Are you sure?")
	String confirmClassAssignment();
	
	@DefaultMessage("N/A")
	String dateNotApplicable();
	
	@DefaultMessage("N/A")
	String timeNotApplicable();
	
	@DefaultMessage("Students")
	String columnStudentConflicts();
	
	@DefaultMessage("There are no student conflicts.")
	String messageNoStudentConflicts();
	
	@DefaultMessage("Class {0} has no date pattern selected.")
	String messageClassHasNoDatePatternSelected(String className);
	
	@DefaultMessage("Class {0} has no time pattern selected.")
	String messageClassHasNoTimePatternSelected(String className);
	
	@DefaultMessage("Class {0} has no available time.")
	String messageClassHasNoAvailableTime(String className);
	
	@DefaultMessage("Room {0} is not available for {1} due to the class {2}.")
	String messageRoomNotAvailable(String room, String time, String className);
	
	@DefaultMessage("Room {0} is not available for {1} due to {2}.")
	String messageRoomNotAvailable2(String room, String time, String conflicts);
	
	@DefaultMessage("Instructor {0} is not available for {1} due to {2}.")
	String messageInstructroNotAvailable(String room, String time, String conflicts);
	
	@DefaultMessage("Date Change")
	String columnDateChange();
	
	@DefaultMessage("Room Change")
	String columnRoomChange();
	
	@DefaultMessage("Time Change")
	String columnTimeChange();
	
	@DefaultMessage("not-assigned")
	String notAssigned();
	
	@DefaultMessage("Not Assigned")
	String assignmentNotAssigned();
	
	@DefaultMessage("Not selected ...")
	String assignmentRoomNotSelected();
	
	@DefaultMessage("Select below ...")
	String assignmentRoomSelectBelow();
	
	@DefaultMessage("Course {0} is not locked. Do you want to lock it?")
	String messageCourseNotLocked(String courseName);
	
	@DefaultMessage("Course {0} is not locked. Click the warning icon to lock it.")
	String titleCourseNotLocked(String courseName);
	
	@DefaultMessage("Prohibited Overrides:")
	String propertyDisabledOverrides();

	@DefaultMessage("Prohibited Overrides")
	String columnDisabledOverrides();
	
	@DefaultMessage("Showing projected student conflicts, click here to change to the actual class enrollments.")
	String studentConflictsShowingSolutionConflicts();
	
	
	@DefaultMessage("Showing the actual student class enrollments, click here to change to projected student conflicts.")
	String studentConflictsShowingActualConflicts();
	
	@DefaultMessage("Unavailable Dates")
	String sectionTitleUnavailableDates();
	
	@DefaultMessage("Unavailable Dates:")
	String propertyUnavailableDates();
	
	@DefaultMessage("Not Available")
	String dateNotAvailable();
	
	@DefaultMessage("Available")
	String dateAvailable();
	
	@DefaultMessage("Unavailable<BR>Dates")
	String columnUnavailableDates();
	
	@DefaultMessage("Unavailable\nDates")
	String columnUnavailableDatesPDF();
	
	@DefaultMessage("Instructor Not Available {0} ({1})")
	String instructorNotAvailableName(String date, String deptCode);
	
	@DefaultMessage("Availability")
	String instructorNotAvailableType();
	
	@DefaultMessage("Wait-Listing:")
	String propertyWaitListing();
	
	@DefaultMessage("Default (Wait-Listing Enabled)")
	String waitListDefaultEnabled();
	
	@DefaultMessage("Default (Wait-Listing Disabled)")
	String waitListDefaultDisabled();
	
	@DefaultMessage("Default (Re-Scheduling Enabled)")
	String waitListDefaultReschedule();
	
	@DefaultMessage("Wait-Listing Enabled")
	String waitListEnabled();
	
	@DefaultMessage("Re-Scheduling Enabled")
	String waitListReschedule();
	
	@DefaultMessage("Wait-Listing Disabled")
	String waitListDisabled();
	
	@DefaultMessage("Wait-Listing")
	String waitListEnabledShort();
	
	@DefaultMessage("Re-Scheduling")
	String waitListRescheduleShort();
	
	@DefaultMessage("Disabled")
	String waitListDisabledShort();
	
	@DefaultMessage("Wait-listing is enabled for this offering.")
	String descWaitListEnabled();
	
	@DefaultMessage("Wait-listing is not enabled for this offering.")
	String descWaitListDisabled();
	
	@DefaultMessage("Wait-listing is not enabled, but students can be automatically re-scheduled for this offering.")
	String descWaitListReschedule();
	
	@DefaultMessage("{0} allows for {1}.")
	String problemWaitListProhibitedOverride(String course, String override);
	
	@DefaultMessage("Wait-Listing:")
	String filterWaitlist();
	
	@DefaultMessage("All Courses")
	String itemWaitListAllCourses();
	
	@DefaultMessage("Wait-Listed Courses")
	String itemWaitListWaitListed();
	
	@DefaultMessage("Not Wait-Listed Courses")
	String itemWaitListNotWaitListed();
	
	@DefaultMessage("Courses Allowing Re-Scheduling")
	String itemWaitListReschedule();
	
	@DefaultMessage("Not Wait-Listed Courses Allowing Re-Scheduling")
	String itemWaitListNotWaitListedReschedule();
	
	@DefaultMessage("Courses Not Allowing Re-Scheduling")
	String itemWaitListNotReschedule();
	
	@DefaultMessage("All Subjects")
	String itemAllSubjects();

	@DefaultMessage("All Departments")
	String itemAllDepartments();

	@DefaultMessage("All Managers")
	String itemAllManagers();

	@DefaultMessage("{0} is required.")
	String errorRequiredField(String fieldName);
	
	@DefaultMessage("Abbreviation")
	String fieldAbbreviation();
	
	@DefaultMessage("Name")
	String fieldName();
	
	@DefaultMessage("Reference")
	String fieldReference();
	
	@DefaultMessage("Type")
	String fieldType();
	
	@DefaultMessage("Organized")
	String fieldOrganized();
	
	@DefaultMessage("Parent")
	String fieldParent();
	
	@DefaultMessage("IType")
	String fieldIType();
	
	@DefaultMessage("{0} already exists.")
	String errorAlreadyExists(String value);
	
	@DefaultMessage("{0} does not exists.")
	String errorDoesNotExists(String value);
	
	@DefaultMessage("{0} must be numeric.")
	String errorNotNumber(String value);
	
	@DefaultMessage("Instructional Types")
	String sectionInstructionalTypes();
	
	@DefaultMessage("Add Instructional Type")
	String sectionAddInstructionalTypes();
	
	@DefaultMessage("Edit Instructional Type")
	String sectionEditInstructionalTypes();
	
	@DefaultMessage("Add IType")
	String actionAddIType();
	
	@DefaultMessage("A")
	String accessAddIType();
	
	@DefaultMessage("Add IType (Alt+{0})")
	String titleAddIType(String access);
	
	@DefaultMessage("Back")
	String actionBackITypes();
	
	@DefaultMessage("Update")
	String actionUpdateIType();
	
	@DefaultMessage("Save")
	String actionSaveIType();
	
	@DefaultMessage("Delete")
	String actionDeleteIType();
	
	@DefaultMessage("B")
	String accessBackITypes();
	
	@DefaultMessage("U")
	String accessUpdateIType();
	
	@DefaultMessage("S")
	String accessSaveIType();
	
	@DefaultMessage("D")
	String accessDeleteIType();
	
	@DefaultMessage("Back (Alt+{0})")
	String titleBackITypes(String access);
	
	@DefaultMessage("Update (Alt+{0})")
	String titleUpdateIType(String access);
	
	@DefaultMessage("Save (Alt+{0})")
	String titleSaveIType(String access);
	
	@DefaultMessage("Delete (Alt+{0})")
	String titleDeleteIType(String access);
	
	@DefaultMessage("File")
	String fieldFile();
	
	@DefaultMessage("Nothing to export.")
	String errorNothingToExport();
	
	@DefaultMessage("Import")
	String actionImport();
	
	@DefaultMessage("Export")
	String actionExport();
	
	@DefaultMessage("R")
	String accessRefreshLog();
	
	@DefaultMessage("Refresh")
	String actionRefreshLog();
	
	@DefaultMessage("Refresh Log (Alt + {0})")
	String titleRefreshLog(String access);
	
	@DefaultMessage("Name")
	String fieldQueueName();
	
	@DefaultMessage("Status")
	String fieldQueueStatus();
	
	@DefaultMessage("Progress")
	String fieldQueueProgress();
	
	@DefaultMessage("Owner")
	String fieldQueueOwner();
	
	@DefaultMessage("Session")
	String fieldQueueSession();
	
	@DefaultMessage("Created")
	String fieldQueueCreated();
	
	@DefaultMessage("Started")
	String fieldQueueStarted();
	
	@DefaultMessage("Finished")
	String fieldQueueFinished();
	
	@DefaultMessage("Output")
	String fieldQueueOutput();
	
	@DefaultMessage("Import File")
	String fieldImportFile();
	
	@DefaultMessage("Export Type")
	String fieldExportType();
	
	@DefaultMessage("Do you really want to remove this data exchange?")
	String questionDeleteDataExchangeItem();
	
	@DefaultMessage("Data Export")
	String sectioDateExport();
	
	@DefaultMessage("Data Import")
	String sectioDateImport();
	
	@DefaultMessage("Options")
	String sectionDataExchangeOptions();
	
	@DefaultMessage("Email (Log, Export XML)")
	String fieldDataExchangeEmail();
	
	@DefaultMessage("Select...")
	String itemSelect();
	
	@DefaultMessage("Log of {0}")
	String sectionDataExchangeLog(String name);
	
	@DefaultMessage("Data exchange in progress")
	String sectionDataExchangeQueue();
	
	@DefaultMessage("Export PDF")
	String buttonExportPDF();
	
	@DefaultMessage("Apply")
	String buttonApply();
	
	@DefaultMessage("Refresh")
	String buttonRefresh();
	
	@DefaultMessage("Export failed: {0}")
	String exportFailed(String error);
	
	@DefaultMessage("Instructor id not provided.")
	String errorNoInstructorId();
	
	@DefaultMessage("There are the following errors:")
	String formValidationErrors();
	
	@DefaultMessage("ENABLED")
	String enabled();
	
	@DefaultMessage("N/A")
	String cellNoPositionType();
	
	@DefaultMessage("Update")
	String actionUpdateInstructorsList();
	
	@DefaultMessage("M")
	String accessUpdateInstructorsList();
	
	@DefaultMessage("Update instructors list (Alt+{0})")
	String titleUpdateInstructorsList(String access);
	
	@DefaultMessage("Apply Filter")
	String actionApplyInstructorFilter();
	
	@DefaultMessage("A")
	String accessApplyInstructorFilter();
	
	@DefaultMessage("Apply Filter (Alt+{0})")
	String titleApplyInstructorFilter(String access);
	
	@DefaultMessage("Display:")
	String propertyFilterDisplay();
	
	@DefaultMessage("Department Assigned Instructors Only")
	String filterDisplayDepartmentAssignedInstructorsOnly();
	
	@DefaultMessage("Available Instructors Only")
	String filterDisplayAvailableInstructorsOnly();
	
	@DefaultMessage("Both")
	String filterDisplayBothDepartmentAssignedAndAvailableInstructors();
	
	@DefaultMessage("Ignore Positions:")
	String propertyFilterIgorePositions();
	
	@DefaultMessage("* applies only to Instructors not in department list")
	String descriptionIgnorePosition();
	
	@DefaultMessage("Department Instructors")
	String sectionDepartmentInstructors();
	
	@DefaultMessage("Instructors not in the Department List")
	String sectionAvailableInstructors();
	
	@DefaultMessage("There are no instructors assigned to this department")
	String messageNoDepartmentalInstructors();
	
	@DefaultMessage("There are no additional instructors that can be assigned to this department")
	String messageNoAvailableInstructors();
	
	@DefaultMessage("Position Type Not Set")
	String positionNotSet();
	
	@DefaultMessage("Term:")
	String filterTerm();
	
	@DefaultMessage("Exams:")
	String filterExams();
	
	@DefaultMessage("All")
	String allSubjects();
	
	@DefaultMessage("Personal Schedule")
	String sectPersonalSchedule();
	
	@DefaultMessage("User:")
	String propUserName();
	
	@DefaultMessage("Password:")
	String propUserPassword();
	
	@DefaultMessage("Log In")
	String buttonLogIn();
	
	@DefaultMessage("Log Out")
	String buttonLogOut();
	
	@DefaultMessage("L")
	String accessLogOut();
	
	@DefaultMessage("Log out (Alt + {0})")
	String titleLogOut(String access);
	
	@DefaultMessage("There are no classes available at the moment.")
	String infoNoClassesAvailable();
	
	@DefaultMessage("No subject area selected.")
    String infoNoSubjectAreaSelected();
    
    @DefaultMessage("There are no classes available for {0} at the moment.")
    String infoNoClassesAvailableForSession(String acadSession);
    
    @DefaultMessage("There are no classes available for {0} subject area at the moment.")
    String infoNoClassesAvailableForSubject(String subjectAreaAbbv);
    
    @DefaultMessage("There are no {0} {1} classes available at the moment.")
    String infoNoClassesAvailableForCourse(String subjectAreaAbbv, String courseNumber);
    
    @DefaultMessage("Arr Hrs")
    String arrHrs();
    
    @DefaultMessage("Arr {0} Hrs")
    String arrHrsN(int n);
    
    @DefaultMessage("Arrange Hours")
    String arrangeHours();
    
    @DefaultMessage("Arrange {0} Hours")
    String arrangeHoursN(int n);
    
    @DefaultMessage("Course")
	String columnCourse();
    
    @DefaultMessage("Instruction\nType")
	String columnInstructionType();
    
    @DefaultMessage("Section")
	String columnSection();
    
    @DefaultMessage("No classes found.")
    String infoNoClassesFound();
    
    @DefaultMessage("No examinations found.")
    String infoNoExaminationsFound();
    
    @DefaultMessage("No schedule found.")
    String infoNoScheduleFound();
    
    @DefaultMessage("No classes found in {0}.")
    String infoNoClassesFoundForSession(String acadSession);
    
    @DefaultMessage("No examinations found in {0}.")
    String infoNoExaminationsFoundForSession(String acadSession);
    
    @DefaultMessage("No classes or examinations found in {0}.")
    String infoNoClassesOrExamsFoundForSession(String acadSession);
    
    @DefaultMessage("No classes or examinations found for {0}.")
    String infoNoClassesOrExamsFoundForUser(String userName);
    
    @DefaultMessage("iCalendar")
	String actionExportIcal();
    
    @DefaultMessage("I")
	String accessExportIcal();
    
    @DefaultMessage("Export iCalendar (Alt + {0})")
	String titleExportIcal(String access);
    
    @DefaultMessage("Available Academic Sessions for {0}")
    String sectAvailableAcademicSessionsForUser(String name);
    
    @DefaultMessage("Term")
    String columnTerm();
    
    @DefaultMessage("Year")
    String columnYear();
    
    @DefaultMessage("Campus")
    String columnCampus();
    
    @DefaultMessage("{0} Examination Schedule for {1}")
    String sectExaminationScheduleForStudent(String acadSession, String name);
    
    @DefaultMessage("{0} Class Schedule for {1}")
    String sectClassScheduleForStudent(String acadSession, String name);
    
    @DefaultMessage("{0} Class Schedule for {1}")
    String sectClassScheduleForInstructor(String acadSession, String name);
    
    @DefaultMessage("Meeting Time")
    String columnMeetingTime();
    
    @DefaultMessage("Meeting Times")
    String columnMeetingTimes();
    
    @DefaultMessage("Distance")
    String columnBackToBackDistance();
    
    @DefaultMessage("{0} Examination Conflicts for {1}")
    String sectExaminationConflictsForStudent(String acadSession, String name);
    
    @DefaultMessage("{0} Examination Conflicts and/or Back-To-Back Examinations for {1}")
    String sectExaminationConflictsOrBackToBacksForStudent(String acadSession, String name);
    
    @DefaultMessage("{0} m")
    String backToBackDistanceInMeters(int distance);
    
    @DefaultMessage("{0} Examination Instructor Schedule for {1}")
    String sectExaminationScheduleForInstructor(String acadSession, String name);
    
    @DefaultMessage("Name")
	String columnStudentName();
    
    @DefaultMessage("Share")
	String columnShare();
    
    @DefaultMessage("{0} Examination Instructor Conflicts for {1}")
    String sectExaminationConflictsForInstructor(String acadSession, String name);
    
    @DefaultMessage("{0} Examination Instructor Conflicts and/or Back-To-Back Examinations for {1}")
    String sectExaminationConflictsOrBackToBacksForInstructor(String acadSession, String name);
    
    @DefaultMessage("Lookup")
	String buttonLookup();

	@DefaultMessage("UniTime {0} failed to start up properly, please check the application log for more details.")
	String errorUniTimeFailedToStart(String version);

	@DefaultMessage("BACK")
	String linkBACK();
	
	@DefaultMessage("LOG IN")
	String linkLOGIN();
	
	@DefaultMessage("Select Academic Session")
	String sectSelectAcademicSession();
	
	@DefaultMessage("Select User Role &amp; Academic Session")
	String sectSelectUserRoleAndSession();
	
	@DefaultMessage("User Role")
	String columnUserRole();
	
	@DefaultMessage("Academic Session")
	String columnAcademicSession();
	
	@DefaultMessage("Academic Initiative")
	String columnAcademicInitiative();
	
	@DefaultMessage("Academic Session Status")
	String columnAcademicSessionStatus();
	
	@DefaultMessage("No user role and/or academic session associated with the user {0}.")
	String warnNoRoleForUser(String user);
	
	@DefaultMessage("A default user role and/or academic session could not be assigned. Please select one of the user role and academic session combinations below to proceed.")
	String infoNoDefaultAuthority();
	
	@DefaultMessage("Log In")
	String pageLogIn();
	
	@DefaultMessage("University Timetabling")
	String pageLogInH1();
	
	@DefaultMessage("Comprehensive Academic Scheduling Solutions")
	String pageLogInH2();
	
	@DefaultMessage("Authentication failed: {0}.")
	String errorAuthenticationFailedWithReason(String reason);
	
	@DefaultMessage("Username:")
	String propertyUsername();
	
	@DefaultMessage("Enter user name")
	String ariaEnterUserName();
	
	@DefaultMessage("Password:")
	String propertyPassword();
	
	@DefaultMessage("Enter password")
	String ariaEnterPassword();
	
	@DefaultMessage("Log In")
	String actionLogIn();
	
	@DefaultMessage("Submit login information.")
	String ariaLogIn();
	
	@DefaultMessage("Forgot your password?")
	String linkForgotYourPassword();
	
	@DefaultMessage("Logging out ...")
	String messageLoggingOut();
	
	@DefaultMessage("Log Out")
	String pageLogOut();
	
	@DefaultMessage("You have been successfully logged out of UniTime, click <a href='logout'>here</a> to log out of all other applications as well.")
	String casLoggedOut();
	
	@DefaultMessage("You have been successfully logged out of UniTime, click <a href='login.action'>here</a> to log in again.")
	String opLoggedOut();
	
	@DefaultMessage("System Messages")
	String sectSystemMessages();
	
	@DefaultMessage("Messages from UniTime")
	String sectRegistrationMessages();
	
	@DefaultMessage("Page is loading, please wait ...")
	String messagePageLoading();
	
	@DefaultMessage("Submit")
	String actionChangeUser();
	
	@DefaultMessage("Switch to another user (Alt + {0})")
	String titleChangeUser(String access);
	
	@DefaultMessage("S")
	String accessChangeUser();
	
	@DefaultMessage("Timetable Manager:")
	String propertyTimetableManager();
	
	@DefaultMessage("Other:")
	String propertyOtherUser();
	
	@DefaultMessage("Lookup")
	String actionLookupUser();
	
	@DefaultMessage("No user has been selected.")
	String warnNoUser();
	
	@DefaultMessage("Back")
	String actionBackToManagerSettings();
	
	@DefaultMessage("Update")
	String actionUpdateManagerSetting();
	
	@DefaultMessage("B")
	String accessBackToManagerSettings();
	
	@DefaultMessage("U")
	String accessUpdateManagerSetting();
	
	@DefaultMessage("Back to Manager Settings (Alt + {0})")
	String titleBackToManagerSettings(String access);
	
	@DefaultMessage("Update Setting (Alt + {0})")
	String titleUpdateManagerSetting(String access);
	
	@DefaultMessage("Manager Settings")
	String sectionManagerSettings();
	
	@DefaultMessage("Setting")
	String columnManagerSettingKey();
	
	@DefaultMessage("Value")
	String columnManagerSettingValue();
	
	@DefaultMessage("(default)")
	String userSettingDefaultIndicator();
	
	@DefaultMessage("<b>{0}</b> must be greater than or equal to <b>{1}</b>.")
	String errorIntegerGtEq(String variable, String value);
	
	@DefaultMessage("<b>{0}</b> must be less than or equal to <b>{1}</b>.")
	String errorIntegerLtEq(String variable, String value);
	
	@DefaultMessage("<b>{0}</b> must have a value greater than <b>{1}</b>.")
	String errorIntegerGt(String variable, String value);
	
	@DefaultMessage("<b>{0}</b> must have a value lesser than <b>{1}</b>.")
	String errorIntegerLt(String variable, String value);
	
	@DefaultMessage("<b>{0}</b> must be equal to <b>{1}</b>.")
	String errorEqual(String variable, String value);
	
	@DefaultMessage("Min Limit")
	String columnMinLimit();
	
	@DefaultMessage("Max Limit")
	String columnMaxLimit();
	
	@DefaultMessage("A configuration with this name already exists in this offering. Use a unique name.")
	String errorConfigurationAlreadyExists();
	
	@DefaultMessage("{0} per class for <u>{1}</u>")
	String messageLimitPerClassForIType(String limitName, String itype);
	
	@DefaultMessage("{0} per class of {1} for <u>{2}</u>")
	String messageLimitPerClassOfLimitForIType(String limitName, int limit, String itype);
	
	@DefaultMessage("Configuration limit of {0}")
	String messageConfigurationLimit(int limit);
	
	@DefaultMessage("Sum of class limits <u>{0}</u>")
	String messageSumClassLimitsForIType(String itype);
	
	@DefaultMessage("Number of classes for <u>{0}</u>")
	String messageNumberOfClassesForIType(String itype);
	
	@DefaultMessage("Number of rooms for <u>{0}</u>")
	String messageNumberOfRoomsForIType(String itype);

	@DefaultMessage("Minutes per week for <u>{0}</u>")
	String messageMinsPerWeekForIType(String itype);
	
	@DefaultMessage("Room ratio for <u>{0}</u>")
	String messageRoomRatioForIType(String itype);
	
	@DefaultMessage("Max limit per class")
	String messageMaxLimitPerClass();
	
	@DefaultMessage("Subparts that are grouped under <u>{0}</u> must <br>&nbsp; &nbsp; &nbsp; have number of classes that is a multiple of {1}.")
	String errorConfigurationNC(String itype, int numClasses);
	
	@DefaultMessage("Subparts that are grouped under <u>{0}</u> must <br>&nbsp; &nbsp; &nbsp; have a {1} per class &lt;= {1} per class of {2}.")
	String errorConfigurationCL(String itype, String limitName, int maxLimitPerClass);
	
	@DefaultMessage("Subparts that are grouped under <u>{0}</u> must <br>&nbsp; &nbsp; &nbsp; not accomodate lesser number of students.")
	String errorConfigurationLS(String itype);
	
	@DefaultMessage("Minutes per week for <u>{0}</u> can be 0 only if number of rooms is 0.")
	String messageMinsPerWeekForITypeCanBeZeroWhenNbrRoomsIsZero(String itype);
	
	@DefaultMessage("Configuration could not be updated. If possible, split your configuration change into 2 or more separate operations.")
	String errorConfigurationCouldNotBeUpdated();
	
	@DefaultMessage("Exception: {0}")
	String errorCaughtException(String errorMessage);
	
	@DefaultMessage("Class {0}")
	String sectClass(String name);
	
	@DefaultMessage("Please specify category of this inquiry.")
	String errorInquiryPleaseSpecifyCategory();
	
	@DefaultMessage("Message is required.")
	String errorInquiryMessageRequired();
	
	@DefaultMessage("Recipient has an invalid email address.")
	String errorInquiryInvalidRecipientAddress();
	
	@DefaultMessage("Invalid email address.")
	String errorInquiryInvalidAddress();
	
	@DefaultMessage("Cancel")
	String actionInquiryCancel();
	
	@DefaultMessage("Back")
	String actionInquiryBack();
	
	@DefaultMessage("Submit")
	String actionInquirySubmit();
	
	@DefaultMessage("Submit Another Inquiry")
	String actionInquirySubmitAnother();
	
	@DefaultMessage("Add Recipient")
	String actionAddRecipient();
	
	@DefaultMessage("Attach File")
	String actionAttachFile();
	
	@DefaultMessage("C")
	String accessInquiryCancel();
	
	@DefaultMessage("B")
	String accessInquiryBack();
	
	@DefaultMessage("S")
	String accessInquirySubmit();
	
	@DefaultMessage("S")
	String accessInquirySubmitAnother();
	
	@DefaultMessage("R")
	String accessAddRecipient();
	
	@DefaultMessage("F")
	String accessAttachFile();
	
	@DefaultMessage("Cancel (Alt + {0})")
	String titleInquiryCancel(String access);
	
	@DefaultMessage("Back (Alt + {0})")
	String titleInquiryBack(String access);
	
	@DefaultMessage("Submit (Alt + {0})")
	String titleInquirySubmit(String access);
	
	@DefaultMessage("Submit Another Inquiry (Alt + {0})")
	String titleInquirySubmitAnother(String access);
	
	@DefaultMessage("Add Recipient (Alt + {0})")
	String titleAddRecipient(String access);
	
	@DefaultMessage("Attach File (Alt + {0})")
	String titleAttachFile(String access);
	
	@DefaultMessage("Delete Recipient")
	String titleDeleteRecipient();
	
	@DefaultMessage("Remove {0}")
	String titleDeleteAttachedFile(String name);
	
	@DefaultMessage("User info -------------- ")
	String emailInquiryUserInfoSection();
	
	@DefaultMessage("Login:")
	String propLogin();
	
	@DefaultMessage("Role:")
	String propertyRole();
	
	@DefaultMessage("Academic Session:")
	String propAcademicSession();
	
	@DefaultMessage("Departments:")
	String propDepartments();
	
	@DefaultMessage("Solver Groups:")
	String propSolverGroups();
	
	@DefaultMessage("Application info -------------- ")
	String emailInquiryApplicationInfoSection();
	
	@DefaultMessage("Version:")
	String propVersion();
	
	@DefaultMessage("TimeStamp:")
	String propTimeStamp();
	
	@DefaultMessage("UniTime ({0}): {1}")
	String emailInquirySubject(String type, String subject);
	
	@DefaultMessage("The following inquiry was submitted on your behalf. We will contact you soon. This email was automatically generated, please do not reply.")
	String emailInquiryCofirmation();
	
	@DefaultMessage("Thank you, \r\n{0}")
	String emailInquiryCofirmationThankYou(String sender);
	
	@DefaultMessage("-- INQUIRY ({0}): {1} ---------- ")
	String emailInquiryBeginSection(String type, String subject);
	
	@DefaultMessage("-- END INQUIRY -------------------------------------------")
	String emailInquiryEndSection();
	
	@DefaultMessage("Your inquiry was successfully submitted. Thank you.")
	String messageInquirySubmitted();
	
	@DefaultMessage("Inquiry")
	String sectionInquiry();
	
	@DefaultMessage("Category:")
	String propCategory();
	
	@DefaultMessage("CC:")
	String propEmailCC();
	
	@DefaultMessage("Subject:")
	String propEmailSubject();
	
	@DefaultMessage("Message:")
	String propEmailMessage();
	
	@DefaultMessage("Attachment:")
	String propEmailAttachment();
	
	@DefaultMessage("Contact Information")
	String sectionContactInformation();
	
	@DefaultMessage("Address:")
	String propContactAddress();
	
	@DefaultMessage("Phone:")
	String propContactPhone();
	
	@DefaultMessage("Office Hours:")
	String propContactOfficeHours();
	
	@DefaultMessage("Email:")
	String propContactEmail();
	
	@DefaultMessage("({0} bytes)")
	String attachmentFileSize(String bytes);
	
	@DefaultMessage("Name")
	String columnAppConfigKey();
	
	@DefaultMessage("Value")
	String columnAppConfigValue();
	
	@DefaultMessage("Description")
	String columnAppConfigDescription();
	
	@DefaultMessage("Name:")
	String propAppConfigKey();
	
	@DefaultMessage("Value:")
	String propAppConfigValue();
	
	@DefaultMessage("Description:")
	String propAppConfigDescription();
	
	@DefaultMessage("Applies To:")
	String propAppConfigAppliesTo();
	
	@DefaultMessage("Type:")
	String propAppConfigType();
	
	@DefaultMessage("Vales:")
	String propAppConfigValues();
	
	@DefaultMessage("Default:")
	String propAppConfigDefault();
	
	@DefaultMessage("All Sessions")
	String checkAppConfigAppliesToAllSessions();
	
	@DefaultMessage("Applies to {0}")
	String hintAppConfigAppliesTo(String session);
	
	@DefaultMessage("s)")
	String supAppConfigSessionOnly();
	
	@DefaultMessage("s) Applies to current academic session.")
	String descAppConfigAppliesToCurrentAcadSession();
	
	@DefaultMessage("Add Setting")
	String actionAddSetting();
	
	@DefaultMessage("Save")
	String actionSaveSetting();
	
	@DefaultMessage("Update")
	String actionUpdateSetting();
	
	@DefaultMessage("Delete")
	String actionDeleteSetting();
	
	@DefaultMessage("Cancel")
	String actionCancelSetting();
	
	@DefaultMessage("A")
	String accessAddSetting();
	
	@DefaultMessage("S")
	String accessSaveSetting();
	
	@DefaultMessage("U")
	String accessUpdateSetting();
	
	@DefaultMessage("D")
	String accessDeleteSetting();
	
	@DefaultMessage("C")
	String accessCancelSetting();
	
	@DefaultMessage("Add Application Setting (Alt + {0})")
	String titleAddSetting(String access);
	
	@DefaultMessage("Save Application Setting (Alt + {0})")
	String titleSaveSetting(String access);
	
	@DefaultMessage("Update Application Setting (Alt + {0})")
	String titleUpdateSetting(String access);
	
	@DefaultMessage("Delete Application Setting (Alt + {0})")
	String titleDeleteSetting(String access);
	
	@DefaultMessage("Cancel Save/Update (Alt + {0})")
	String titleCancelSetting(String access);
	
	@DefaultMessage("No application configuration keys found.")
	String messageNoAppConfKeys();
	
	@DefaultMessage("The application setting will be deleted. Continue?")
	String confirmDeleteAppConfig();
	
	@DefaultMessage("Add Application Setting")
	String sectAddAppSetting();
	
	@DefaultMessage("Edit Application Setting")
	String sectEditAppSetting();
	
	@DefaultMessage("Application Settings")
	String sectAppSettings();
	
	@DefaultMessage("Show all properties")
	String checkShowAllAppSettings();
	
	@DefaultMessage("Update")
	String actionUpdateUser();
	
	@DefaultMessage("Save")
	String actionSaveUser();
	
	@DefaultMessage("Delete")
	String actionDeleteUser();
	
	@DefaultMessage("Back")
	String actionBackToUsers();
	
	@DefaultMessage("Add User")
	String actionAddUser();
	
	@DefaultMessage("Request Password Change")
	String actionRequestPasswordChange();
	
	@DefaultMessage("U")
	String accessUpdateUser();
	
	@DefaultMessage("S")
	String accessSaveUser();
	
	@DefaultMessage("D")
	String accessDeleteUser();
	
	@DefaultMessage("B")
	String accessBackToUsers();
	
	@DefaultMessage("A")
	String accessAddUser();
	
	@DefaultMessage("P")
	String accessRequestPasswordChange();
	
	@DefaultMessage("Update User (Alt + {0})")
	String titleUpdateUser(String access);
	
	@DefaultMessage("Save User (Alt + {0})")
	String titleSaveUser(String access);
	
	@DefaultMessage("Delete User (Alt + {0})")
	String titleDeleteUser(String access);
	
	@DefaultMessage("Return to Users (Alt + {0})")
	String titleBackToUsers(String access);
	
	@DefaultMessage("Add User (Alt + {0})")
	String titleAddUser(String access);
	
	@DefaultMessage("Request Password Change (Alt + {0})")
	String titleRequestPasswordChange(String access);
	
	@DefaultMessage("Username")
	String columnUserName();
	
	@DefaultMessage("Password")
	String columnUserPassword();
	
	@DefaultMessage("Manager")
	String columnTimetableManager();
	
	@DefaultMessage("API Token")
	String columnAPIKey();
	
	@DefaultMessage("No users defined.")
	String messageNoUsers();
	
	@DefaultMessage("The user will be deleted. Continue?")
	String confirmUserDelete();
	
	@DefaultMessage("Add User")
	String sectAddUser();
	
	@DefaultMessage("Edit User")
	String sectEditUser();
	
	@DefaultMessage("Users")
	String sectUsers();
	
	@DefaultMessage("API Token:")
	String propAPIKey();
	
	@DefaultMessage("Query is required.")
	String errorQueryIsRequired();
	
	@DefaultMessage("Unable to set parameter {0}: no available values.")
	String errorCannotSetQueryParameterNoValues(String param);
	
	@DefaultMessage("Clear Cache")
	String actionClearCache();
	
	@DefaultMessage("Next")
	String actionNextQueryResults();
	
	@DefaultMessage("Previous")
	String actionPreviousQueryResults();
	
	@DefaultMessage("Submit")
	String actionSubmitQuery();
	
	@DefaultMessage("C")
	String accessClearCache();
	
	@DefaultMessage("N")
	String accessNextQueryResults();
	
	@DefaultMessage("P")
	String accessPreviousQueryResults();
	
	@DefaultMessage("S")
	String accessSubmitQuery();
	
	@DefaultMessage("Clear Hibernate Cache (Alt + {0})")
	String titleClearCache(String access);
	
	@DefaultMessage("Next Page (Alt + {0})")
	String titleNextQueryResults(String access);
	
	@DefaultMessage("Previous Page (Alt + {0})")
	String titlePreviousQueryResults(String access);
	
	@DefaultMessage("Submit Query (ALt + {0})")
	String titleSubmitQuery(String access);
	
	@DefaultMessage("lines")
	String queryLines();
	
	@DefaultMessage("{0} lines updated")
	String queryLinesUpdated(int number);
	
	@DefaultMessage("HQL")
	String sectHQL();
	
	@DefaultMessage("Generated SQL")
	String sectGeneratedSQL();
	
	@DefaultMessage("Result ({0})")
	String sectQueryResult(String lines);
	
	@DefaultMessage("Error:")
	String propError();
	
	@DefaultMessage("Query:")
	String propQuery();
	
	@DefaultMessage("Trace:")
	String propStackTrace();
	
	@DefaultMessage("Detailed Statistics")
	String sectDetailedStatistics();
	
	@DefaultMessage("Summary Statistics")
	String sectSummaryStatistics();
	
	@DefaultMessage("Enable Statistics")
	String actionEnableStatistics();
	
	@DefaultMessage("Disable Statistics")
	String actionDisableStatistics();
	
	@DefaultMessage("Show Details")
	String actionShowDetails();
	
	@DefaultMessage("Hide Details")
	String actionHideDetails();
	
	@DefaultMessage("Name")
	String columnDatePatternName();
	
	@DefaultMessage("Type")
	String columnDatePatternType();
	
	@DefaultMessage("Used")
	String columnDatePatternUsed();
	
	@DefaultMessage("Weeks")
	String columnDatePatternWeeks();
	
	@DefaultMessage("Dates")
	String columnDatePatternDates();
	
	@DefaultMessage("Dates / Patterns")
	String columnDatePatternDatesOrPatterns();
	
	@DefaultMessage("Pattern Sets")
	String columnDatePatternPatternSets();
	
	@DefaultMessage("Departments")
	String columnDatePatternDepartments();
	
	@DefaultMessage("Pattern")
	String columnDatePatternPattern();
	
	@DefaultMessage("Parent")
	String columnDatePatternParent();
	
	@DefaultMessage("NrDays")
	String columnDatePatternNbrDays();
	
	@DefaultMessage("From")
	String columnDatePatternFrom();
	
	@DefaultMessage("To")
	String columnDatePatternTo();
	
	@DefaultMessage("Classes")
	String columnDatePatternClasses();
	
	@DefaultMessage("Only extended pattern and alternative pattern set can contain relations with departments.")
	String errorOnyExtDatePatternsHaveDepartments();
	
	@DefaultMessage("Number of weeks must be a number.")
	String errorNumberOfWeeksIsNotNumber();
	
	@DefaultMessage("Alternative pattern set date pattern can not have any dates selected.")
	String errorAltPatternSetCannotHaveDates();
	
	@DefaultMessage("Alternative pattern set date pattern can not have a pattern set.")
	String errorAltPatternSetCannotHavePatternSet();
	
	@DefaultMessage("Date Patterns cannot contain more than 1 year.")
	String errorDatePatternCannotContainMoreThanAYear();
	
	@DefaultMessage("No department selected.")
	String errorNoDepartmentSelected();
	
	@DefaultMessage("Department already present in the list of departments.")
	String errorDepartmentAlreadyListed();
	
	@DefaultMessage("Department not present in the list of departments.")
	String errorDepartmentNotListed();
	
	@DefaultMessage("No date pattern selected.")
	String errorNoDatePatternSelected();
	
	@DefaultMessage("Date pattern already present in the list.")
	String errorDatePatternAlreadyListed();
	
	@DefaultMessage("Date pattern not present in the list.")
	String errorDatePatternNotListed();
	
	@DefaultMessage("No date pattern defined for this session.")
	String errorNoDatePatterns();
	
	@DefaultMessage("This date pattern is being used.")
	String infoDatePatternUsed();
	
	@DefaultMessage("Back")
	String actionBackToDatePatterns();
	
	@DefaultMessage("Add Date Pattern")
	String actionAddDatePattern();
	
	@DefaultMessage("Remove Date Pattern")
	String actionRemoveDatePattern();
	
	@DefaultMessage("Create a new date pattern")
	String titleAddDatePattern();
	
	@DefaultMessage("Add Department")
	String actionAddDepartment();
	
	@DefaultMessage("Remove Department")
	String actionRemoveDepartment();
	
	@DefaultMessage("Add Pattern Set")
	String actionAddAltPatternSet();
	
	@DefaultMessage("Remove Pattern Set")
	String actionRemovePatternSet();
	
	@DefaultMessage("Save")
	String actionSaveDatePattern();
	
	@DefaultMessage("Update")
	String actionUpdateDatePattern();
	
	@DefaultMessage("Previous")
	String actionPreviousDatePattern();
	
	@DefaultMessage("Next")
	String actionNextDatePattern();
	
	@DefaultMessage("Make Default")
	String actionMakeDatePatternDefaulf();
	
	@DefaultMessage("Delete")
	String actionDeleteDatePattern();
	
	@DefaultMessage("Push Up")
	String actionPushUpDatePatterns();
	
	@DefaultMessage("Move date patterns from classes to subparts whenever possible")
	String titlePushUpDatePatterns();
	
	@DefaultMessage("Assign Departments")
	String actionAssingDepartmentsToDatePatterns();
	
	@DefaultMessage("Assign departments to extended date patterns")
	String titleAssingDepartmentsToDatePatterns();
	
	@DefaultMessage("Export date patterns to CSV")
	String titleExportDatePatternsCSV();
	
	@DefaultMessage("The date pattern will be deleted. Continue?")
	String confirmDeleteDatePattern();
	
	@DefaultMessage("Standard")
	String datePatternTypeStandard();
	
	@DefaultMessage("Alternate Weeks")
	String datePatternTypeAlternateWeeks();
	
	@DefaultMessage("Non-standard")
	String datePatternTypeNonStandard();
	
	@DefaultMessage("Extended")
	String datePatternTypeExtended();
	
	@DefaultMessage("Alternative Pattern Set")
	String datePatternTypeAltPatternSet();
	
	@DefaultMessage("Add Date Pattern")
	String sectAddDatePattern();
	
	@DefaultMessage("Edit Date Pattern")
	String sectEditDatePattern();
	
	@DefaultMessage("Date Patterns")
	String sectDatePatterns();
	
	@DefaultMessage("Name:")
	String propDatePatternName();
	
	@DefaultMessage("Type:")
	String propDatePatternType();
	
	@DefaultMessage("Number of Weeks:")
	String propDatePatternNbrWeeks();
	
	@DefaultMessage("Visible:")
	String propDatePatternVisible();
	
	@DefaultMessage("Default:")
	String propDatePatternDefault();
	
	@DefaultMessage("Departments:")
	String propDatePatternDepartments();
	
	@DefaultMessage("Alternative Pattern Sets:")
	String propDatePatternAltPatternSets();
	
	@DefaultMessage("Date Patterns:")
	String propDatePatternChildren();
	
	@DefaultMessage("Pattern:")
	String propDatePatternPattern();
	
	@DefaultMessage("The number of weeks will be computed from the pattern when left blank.")
	String infoDatePatternNbrWeeks();
	
	@DefaultMessage("Update")
	String actionUpdateExactTimeMins();
	
	@DefaultMessage("Number of 5 minute time slots per meeting &amp; break times")
	String sectExactTimeMins();
	
	@DefaultMessage("Number of minutes<br>per meeting")
	String columnExactTimeMinuesRange();
	
	@DefaultMessage("Number of slots<br>per meeting")
	String columnExactTimeSlotsPerMeeting();
	
	@DefaultMessage("Break time")
	String columnExactTimeBreakTime();
	
	@DefaultMessage("Name")
	String columnTimePatternName();
	
	@DefaultMessage("Type")
	String columnTimePatternType();
	
	@DefaultMessage("Visible")
	String columnTimePatternVisible();
	
	@DefaultMessage("Used")
	String columnTimePatternUsed();
	
	@DefaultMessage("NbrMtgs")
	String columnTimePatternNbrMtgs();
	
	@DefaultMessage("MinPerMtg")
	String columnTimePatternMinPerMtg();
	
	@DefaultMessage("SlotsPerMtg")
	String columnTimePatternSlotsPerMtg();
	
	@DefaultMessage("Break Time")
	String columnTimePatternBreakTime();
	
	@DefaultMessage("Days")
	String columnTimePatternDays();
	
	@DefaultMessage("Times")
	String columnTimePatternTimes();
	
	@DefaultMessage("Departments")
	String columnTimePatternDepartments();
	
	@DefaultMessage("Classes")
	String columnTimePatternClasses();
	
	@DefaultMessage("Number of slots per meeting is required.")
	String errorNumberOfSlotsPerMeetingRequired();
	
	@DefaultMessage("Minutes per meeting is required.")
	String errorMinutesPerMeetingRequired();
	
	@DefaultMessage("Number of meetings per week is required.")
	String errorNumberOfMeetingsPerWeekRequired();
	
	@DefaultMessage("Only extended or exact time pattern can contain relations with departments.")
	String errorOnlyExtTimePatternHasDepartments();
	
	@DefaultMessage("Save")
	String actionSaveTimePattern();
	
	@DefaultMessage("Update")
	String actionUpdateTimePattern();
	
	@DefaultMessage("Back")
	String actionBackToTimePatterns();
	
	@DefaultMessage("Next")
	String actionNextTimePattern();
	
	@DefaultMessage("Previous")
	String actionPreviousTimePattern();
	
	@DefaultMessage("Delete")
	String actionDeleteTimePattern();
	
	@DefaultMessage("Add Time Pattern")
	String actionAddTimePattern();
	
	@DefaultMessage("Create a new time pattern")
	String titleAddTimePattern();
	
	@DefaultMessage("Exact Times CSV")
	String actionExactTimesCSV();
	
	@DefaultMessage("Generate a CSV report with all classes that are using exact times")
	String titleExactTimesCSV();
	
	@DefaultMessage("Assign Departments")
	String actionAssingDepartmentsToTimePatterns();
	
	@DefaultMessage("Assign departments to extended time patterns")
	String titleAssingDepartmentsToTimePatterns();
	
	@DefaultMessage("Export time patterns to CSV")
	String titleExportTimePatternsCSV();
	
	@DefaultMessage("Standard")
	String timePatterTypeStandard();
	
	@DefaultMessage("Evening")
	String timePatterTypeEvening();
	
	@DefaultMessage("Saturday")
	String timePatterTypeSaturday();
	
	@DefaultMessage("Morning")
	String timePatterTypeMorning();
	
	@DefaultMessage("Extended")
	String timePatterTypeExtended();
	
	@DefaultMessage("Exact Time")
	String timePatterTypeExactTime();
	
	@DefaultMessage("Invalid days ''{0}''.")
	String errorInvalidDaysForToken(String token);
	
	@DefaultMessage("Days ''{0}'' invalid -- wrong number of days.")
	String errorWrongNumberOfDaysForToken(String token);
	
	@DefaultMessage("Days ''{0}'' included more than once.")
	String errorDuplicateDaysToken(String token);
	
	@DefaultMessage("Invalid time ''{0}'' -- hour ({1}) must be between 0 and 23.")
	String errorWrongHoursForTimeToken(String token, int hours);
	
	@DefaultMessage("Invalid time ''{0}'' -- minute ({1}) must be between 0 and 59.")
	String errorWrongMinutesForTimeToken(String token, int min);
	
	@DefaultMessage("Invalid time ''{0}'' -- minute ({1}) must be divisible by 5.")
	String errorMinutesNotDivisibleByFiveForTimeToken(String token, int min);
	
	@DefaultMessage("Invalid time ''{0}'' -- the time cannot go over midnight.")
	String errorTimeGoesOverMidnightForToken(String token);
	
	@DefaultMessage("Invalid time ''{0}'' -- not a number.")
	String errorTimeNotNumberForToken(String token);
	
	@DefaultMessage("Invalid time ''{0}''.")
	String errorNotValidTimeForToken(String token);
	
	@DefaultMessage("Time ''{0}'' included more than once.")
	String errorDiplicateTimeToken(String token);
	
	@DefaultMessage("There is no Exact Time time pattern defined.")
	String errorNoExactTimePatternDefined();
	
	@DefaultMessage("not used")
	String notUsed();
	
	@DefaultMessage("Y")
	String csvTrue();
	
	@DefaultMessage("N")
	String csvFalse();
	
	@DefaultMessage("No time pattern defined for this academic session.")
	String errorNoTimePatternsDefined();
	
	@DefaultMessage("This time pattern is being used.")
	String hintTimePatternUsed();
	
	@DefaultMessage("The time pattern will be deleted. Continue?")
	String confirmTimeDatePattern();
	
	@DefaultMessage("Add Time Pattern")
	String sectAddTimePattern();
	
	@DefaultMessage("Edit Time Pattern")
	String sectEditTimePattern();
	
	@DefaultMessage("Time Patterns")
	String sectTimePatterns();
	
	@DefaultMessage("Name:")
	String propTimePatternName();
	
	@DefaultMessage("Type:")
	String propTimePatternType();
	
	@DefaultMessage("Visible:")
	String propTimePatternVisible();
	
	@DefaultMessage("Number of slots per meeting:")
	String propTimePatternSlotsPerMeeting();
	
	@DefaultMessage("(one slot represent 5 minutes)")
	String hintTimePatternSlotsPerMeeting();
	
	@DefaultMessage("Break time [minutes]:")
	String propTimePatternBreakTime();
	
	@DefaultMessage("Days:")
	String propTimePatternDays();
	
	@DefaultMessage("Start times:")
	String propTimePatternStartTimes();
	
	@DefaultMessage("Departments:")
	String propTimePatternDepartments();
	
	@DefaultMessage("Example:")
	String propTimePatternExample();
	
	@DefaultMessage("Add Session")
	String actionAddAcademicSession();
	
	@DefaultMessage("A")
	String accessAddAcademicSession();
	
	@DefaultMessage("Add New Academic Session (Alt + {0})")
	String titleAddAcademicSession(String access);
	
	@DefaultMessage("Default")
	String columnAcademicSessionDefault();
	
	@DefaultMessage("Academic\nSession")
	String columnAcademicSessionTermYear();
	
	@DefaultMessage("Academic\nInitiative")
	String columnAcademicSessionInitiative();
	
	@DefaultMessage("Session\nBegins")
	String columnAcademicSessionStartDate();
	
	@DefaultMessage("Classes\nEnd")
	String columnAcademicSessionClassesEndDate();
	
	@DefaultMessage("Session\nEnds")
	String columnAcademicSessionEndDate();
	
	@DefaultMessage("Exams\nBegins")
	String columnAcademicSessionExamStartDate();
	
	@DefaultMessage("Date\nPattern")
	String columnAcademicSessionDefaultDatePattern();
	
	@DefaultMessage("Status")
	String columnAcademicSessionCurrentStatus();
	
	@DefaultMessage("Class\nDuration")
	String columnAcademicSessionClassDuration();
	
	@DefaultMessage("Events\nBegins")
	String columnAcademicSessionEventStartDate();
	
	@DefaultMessage("Events\nEnds")
	String columnAcademicSessionEventEndDate();
	
	@DefaultMessage("\nEnrollment")
	String columnAcademicSessionEnrollmentAddDeadline();
	
	@DefaultMessage("Deadline\nChange")
	String columnAcademicSessionEnrollmentChangeDeadline();
	
	@DefaultMessage("\nDrop")
	String columnAcademicSessionEnrollmentDropDeadline();
	
	@DefaultMessage("Scheduling\nStatus")
	String columnAcademicSessionSectioningStatus();
	
	@DefaultMessage("Default\nIM")
	String columnAcademicSessionDefaultInstructionalMethod();
	
	@DefaultMessage("Academic Term")
	String columnAcademicTerm();
	
	@DefaultMessage("Academic Year")
	String columnAcademicYear();
	
	@DefaultMessage("Default Date Pattern")
	String columnDefaultDatePattern();
	
	@DefaultMessage("Session Status")
	String columnSessionStatus();
	
	@DefaultMessage("Session Start Date")
	String columnSessionStartDate();
	
	@DefaultMessage("Session End Date")
	String columnSessionEndDate();
	
	@DefaultMessage("Classes End Date")
	String columnClassesEndDate();
	
	@DefaultMessage("Examination Start Date")
	String columnExamStartDate();
	
	@DefaultMessage("Event Start Date")
	String columnEventStartDate();
	
	@DefaultMessage("Event End Date")
	String columnEventEndDate();
	
	@DefaultMessage("Default Class Duration")
	String columnDefaultClassDuration();
	
	@DefaultMessage("Default Instructional Method")
	String columnDefailtInstructionalMethod();
	
	@DefaultMessage("Holidays")
	String columnHolidays();
	
	@DefaultMessage("An academic session for the initiative, year and term already exists.")
	String errorAcademicSessionAlreadyExists();
	
	@DefaultMessage("Another academic session for the same initiative, year and term already exists.")
	String errorAcademicSessionSameAlreadyExists();
	
	@DefaultMessage("{0} has not a valid date.")
	String errorNotValidDate(String column);
	
	@DefaultMessage("Session End Date must occur AFTER Session Start Date.")
	String errorSessionEndDateNotAfterSessionStartDate();
	
	@DefaultMessage("Classes End Date must occur AFTER Session Start Date.")
	String errorClassesEndDateNotAfterSessionStartDate();
	
	@DefaultMessage("Classes End Date must occur ON or BEFORE Session End Date.")
	String errorClassesEndDateNotOnOrBeforeSessionEndDate();
	
	@DefaultMessage("Event End Date must occur AFTER Event Start Date.")
	String errorEventEndDateNotAfterEventStartDate();
	
	@DefaultMessage("Dates associated with a session cannot cover more than one year.")
	String errorSessionDatesOverAYear();
	
	@DefaultMessage("Current academic session cannot be deleted -- please change your session first.")
	String errorCannotDeleteCurrentAcademicSession();
	
	@DefaultMessage("Save")
	String actionSaveAcademicSession();
	
	@DefaultMessage("Update")
	String actionUpdateAcademicSession();
	
	@DefaultMessage("Delete")
	String actionDeleteAcademicSession();
	
	@DefaultMessage("Back")
	String actionBackToAcademicSessions();
	
	@DefaultMessage("S")
	String accessSaveAcademicSession();
	
	@DefaultMessage("U")
	String accessUpdateAcademicSession();
	
	@DefaultMessage("D")
	String accessDeleteAcademicSession();
	
	@DefaultMessage("B")
	String accessBackToAcademicSessions();
	
	@DefaultMessage("Save Academic Session (Alt + {0})")
	String titleSaveAcademicSession(String access);
	
	@DefaultMessage("Update Academic Session (Alt + {0})")
	String titleUpdateAcademicSession(String access);
	
	@DefaultMessage("Delete Academic Session (Alt + {0})")
	String titleDeleteAcademicSession(String access);
	
	@DefaultMessage("Back to Academic Sessions (Alt + {0})")
	String titleBackToAcademicSessions(String access);
	
	@DefaultMessage("Default date pattern not set")
	String infoNoDefaultDatePattern();
	
	@DefaultMessage("The academic session and all associated data will be deleted. Continue?")
	String confirmDeleteAcademicSession();
	
	@DefaultMessage("Add Academic Session")
	String sectAddAcademicSession();
	
	@DefaultMessage("Edit Academic Session")
	String sectEditAcademicSession();
	
	@DefaultMessage("Online Student Scheduling Default Settings")
	String sectOnlineStudentSchedulingDefaultSettings();
	
	@DefaultMessage("No date patterns are available for this academic session.")
	String infoNoDatePatternsAvailable();
	
	@DefaultMessage("System Default (Minutes per Week)")
	String itemDefaultClassDuration();
	
	@DefaultMessage("No Default")
	String itemNoDefault();
	
	@DefaultMessage("System Default (No Restrictions)")
	String itemDefaultStudentStatus();
	
	@DefaultMessage("New Enrollment Deadline:")
	String propNewEnrollmentDeadline();
	
	@DefaultMessage("Number of weeks during which students are allowed to enroll to a new course.\n"+
			"Weeks start on the day of session start date, number of weeks is relative to class start.\n"+
			"For instance, 1 means that new enrollments will be allowed during the first week of classes.")
	String descNewEnrollmentDeadline();
	
	@DefaultMessage("Class Changes Deadline:")
	String propClassChangesDeadline();
	
	@DefaultMessage("Number of weeks during which students are allowed to change existing enrollments.\n"+
			"If smaller than new enrollment deadline, they will not be able to add a new course to their schedule during the weeks between the two.")
	String descClassChangesDeadline();
	
	@DefaultMessage("Course Drop Deadline:")
	String propCourseDropDeadline();
	
	@DefaultMessage("Number of weeks during which students are allowed to drop from courses they are enrolled into.")
	String descCourseDropDeadline();
	
	@DefaultMessage("Default Student Scheduling Status:")
	String propDefaultStudentStatus();
	
	@DefaultMessage("Appearance")
	String fieldAppearance();
	
	@DefaultMessage("Courses Saved Timetables")
	String solverConfigAppearanceTimetables();
	
	@DefaultMessage("Course Timetabling Solver")
	String solverConfigAppearanceSolver();
	
	@DefaultMessage("Examination Timetabling Solver")
	String solverConfigAppearanceExamSolver();
	
	@DefaultMessage("Student Scheduling Solver")
	String solverConfigAppearanceStudentSolver();
	
	@DefaultMessage("Instructor Scheduling Solver")
	String solverConfigAppearanceInstructorSolver();
	
	@DefaultMessage("Add Solver Configuration")
	String actionAddNewSolverConfig();
	
	@DefaultMessage("A")
	String accessAddNewSolverConfig();
	
	@DefaultMessage("Create New Solver Configuration (Alt + {0})")
	String titleAddNewSolverConfig(String access);
	
	@DefaultMessage("Save")
	String actionSaveSolverConfig();
	
	@DefaultMessage("S")
	String accessSaveSolverConfig();
	
	@DefaultMessage("Save Solver Configuration (Alt + {0})")
	String titleSaveSolverConfig(String access);
	
	@DefaultMessage("Update")
	String actionUpdateSolverConfig();
	
	@DefaultMessage("U")
	String accessUpdateSolverConfig();
	
	@DefaultMessage("Update Solver Configuration (Alt + {0})")
	String titleUpdateSolverConfig(String access);
	
	@DefaultMessage("Delete")
	String actionDeleteSolverConfig();
	
	@DefaultMessage("D")
	String accessDeleteSolverConfig();
	
	@DefaultMessage("Delete Solver Configuration (Alt + {0})")
	String titleDeleteSolverConfig(String access);
	
	@DefaultMessage("Export")
	String actionExportSolverConfig();
	
	@DefaultMessage("X")
	String accessExportSolverConfig();
	
	@DefaultMessage("Export Solver Configuration (Alt + {0})")
	String titleExportSolverConfig(String access);
	
	@DefaultMessage("Back")
	String actionBackToSolverConfigs();
	
	@DefaultMessage("B")
	String accessBackToSolverConfigs();
	
	@DefaultMessage("Back to Solver Configurations (Alt + {0})")
	String titleBackToSolverConfigs(String access);
	
	@DefaultMessage("No solver configurations defined.")
	String infoNoSolverConfigs();
	
	@DefaultMessage("The solver configuration will be deleted. Continue?")
	String confirmDeleteSolverConfig();
	
	@DefaultMessage("Solver Configurations")
	String sectSolverConfigurations();
	
	@DefaultMessage("Add Solver Configuration")
	String sectAddSolverConfiguration();
	
	@DefaultMessage("Edit Solver Configuration")
	String sectEditSolverConfiguration();
	
	@DefaultMessage("Distribution Types")
	String sectDistributionTypes();
	
	@DefaultMessage("Id")
	String fieldId();
	
	@DefaultMessage("Visible")
	String fieldVisible();
	
	@DefaultMessage("Allow Instructor Preference")
	String fieldAllowInstructorPreference();
	
	@DefaultMessage("Instructor Survey")
	String fieldAllowInstructorSurvey();
	
	@DefaultMessage("Sequencing Required")
	String fieldSequencingRequired();
	
	@DefaultMessage("Allow Preferences")
	String fieldAllowPreferences();
	
	@DefaultMessage("Departments")
	String fieldDepartments();
	
	@DefaultMessage("Description")
	String fieldDescription();
	
	@DefaultMessage("None")
	String itemNone();
	
	@DefaultMessage("All")
	String itemAll();
	
	@DefaultMessage("Examination")
	String itemDistTypeExams();
	
	@DefaultMessage("Course")
	String itemDistTypeCourses();
	
	@DefaultMessage("N/A")
	String notApplicable();
	
	@DefaultMessage("Update")
	String actionUpdateDistributionType();
	
	@DefaultMessage("Back")
	String actionBackToDistributionTypes();
	
	@DefaultMessage("Restrict Access")
	String sectRestrictAccess();
	
	@DefaultMessage("Back")
	String actionBackToSolverGroups();
	
	@DefaultMessage("Save")
	String actionSaveSolverGroup();
	
	@DefaultMessage("Update")
	String actionUpdateSolverGroup();
	
	@DefaultMessage("Delete")
	String actionDeleteSolverGroup();
	
	@DefaultMessage("Add Solver Group")
	String actionAddSolverGroup();
	
	@DefaultMessage("Delete All")
	String actionDeleteAllSolverGroups();
	
	@DefaultMessage("Delete all solver groups that have no solutions saved.")
	String titleDeleteAllSolverGroups();
	
	@DefaultMessage("Auto Setup")
	String actionAutoSetupSolverGroups();
	
	@DefaultMessage("Automatically setup solver groups.")
	String titleAutoSetupSolverGroups();
	
	@DefaultMessage("Solver Groups - {0}")
	String sectSolverGroupsForSession(String session);
	
	@DefaultMessage("Abbv")
	String fieldAbbv();
	
	@DefaultMessage("Managers")
	String fieldManagers();
	
	@DefaultMessage("Committed")
	String fieldCommitted();
	
	@DefaultMessage("No solver groups defined for this academic session.")
	String infoNoSolverGroupInThisSession();
	
	@DefaultMessage("The solver group will be deleted. Continue?")
	String confirmDeleteSolverGroup();
	
	@DefaultMessage("All solver groups that have no solutions saved will be deleted. Continue?")
	String confirmDeleteAllSolverGroups();
	
	@DefaultMessage("New solver groups may be created. Continue?")
	String confirmCreateNewSolverGroups();
	
	@DefaultMessage("Add Solver Group")
	String sectAddSolverGroup();
	
	@DefaultMessage("Edit Solver Group")
	String sectEditSolverGroup();
	
	@DefaultMessage("Assigned Departments")
	String sectAssignedDepartments();
	
	@DefaultMessage("Assigned Managers")
	String sectAssignedManagers();
	
	@DefaultMessage("Not Assigned Departments")
	String sectNotAssignedDepartments();
	
	@DefaultMessage("Not Assigned Managers")
	String sectNotAssignedManagers();
	
	@DefaultMessage("Label")
	String fieldLabel();
	
	@DefaultMessage("Apply")
	String fieldApply();
	
	@DefaultMessage("Rights")
	String fieldRights();
	
	@DefaultMessage("Add Status Type")
	String actionAddStatusType();
	
	@DefaultMessage("Save")
	String actionSaveStatusType();
	
	@DefaultMessage("Update")
	String actionUpdateStatusType();
	
	@DefaultMessage("Delete")
	String actionDeleteStatusType();
	
	@DefaultMessage("Back")
	String actionBackToStatusTypes();
	
	@DefaultMessage("Session")
	String applyToSession();
	
	@DefaultMessage("Department")
	String applyToDepartment();
	
	@DefaultMessage("Examinations")
	String applyToExaminations();
	
	@DefaultMessage("Session & Department")
	String applyToSessionAndDepartment();
	
	@DefaultMessage("All")
	String applyToAll();
	
	@DefaultMessage("No status defined.")
	String infoNoStatusTypes();
	
	@DefaultMessage("roll-forward")
	String rightRollFoward();
	
	@DefaultMessage("owner can {0}")
	String rightOwnerCan(String what);
	
	@DefaultMessage("manager can {0}")
	String rightManagerCan(String what);
	
	@DefaultMessage("do all")
	String rightViewAndEdit();
	
	@DefaultMessage("do all")
	String rightEditAndTimetable();
	
	@DefaultMessage("view")
	String rightView();
	
	@DefaultMessage("edit")
	String rightEdit();
	
	@DefaultMessage("limited edit")
	String rightLimitedEdit();
	
	@DefaultMessage("{0} and {1}")
	String rightAnd(String r1, String r2);
	
	@DefaultMessage("audit")
	String rightAudit();
	
	@DefaultMessage("timetable")
	String rightTimetable();
	
	@DefaultMessage("commit")
	String rightCommit();
	
	@DefaultMessage("exam {0}")
	String rightExam(String what);
	
	@DefaultMessage("sectioning")
	String rightSectioning();
	
	@DefaultMessage("assistant")
	String rightAssitant();
	
	@DefaultMessage("registration")
	String rightRegistration();
	
	@DefaultMessage("events")
	String rightEvents();
	
	@DefaultMessage("no-role {0}")
	String rightNoRoleCan(String what);
	
	@DefaultMessage("all")
	String rightSeeAllEvents();
	
	@DefaultMessage("classes")
	String rightSeeClasses();
	
	@DefaultMessage("exams")
	String rightSeeExams();
	
	@DefaultMessage("final exams")
	String rightSeeFinalExams();
	
	@DefaultMessage("midterm exams")
	String rightSeeMidtermExams();
	
	@DefaultMessage("test session")
	String rightTestSession();
	
	@DefaultMessage("instructor survey")
	String rightInstructorSurvey();
	
	@DefaultMessage("The status will be deleted. Continue?")
	String confirmStatusTypeDelete();
	
	@DefaultMessage("Add Status Type")
	String sectAddStatusType();
	
	@DefaultMessage("Edit Status Type")
	String sectEditStatusType();
	
	@DefaultMessage("Status Types")
	String sectStatusTypes();
	
	@DefaultMessage("Course Timetabling")
	String sectCourseTimetabling();
	
	@DefaultMessage("Examination Timetabling")
	String sectExaminationTimetabling();
	
	@DefaultMessage("Student Scheduling")
	String sectStudentSectioning();
	
	@DefaultMessage("Event Management")
	String sectEventManagement();
	
	@DefaultMessage("Other")
	String sectOther();
	
	@DefaultMessage("Owner View:")
	String propOwnerView();
	@DefaultMessage("The manager of the department of the subject area of a class can view class data but cannot change it.")
	String descOwnerView();
	
	@DefaultMessage("Owner Limited Edit:")
	String propOwnerLimitedEdit();
	@DefaultMessage("The manager of the department of the subject area of a class can assign instructors.")
	String descOwnerLimitedEdit();
	
	@DefaultMessage("Owner Edit:")
	String propOwnerEdit();
	@DefaultMessage("The manager of the department of the subject area of a class can edit the class data.")
	String descOwnerEdit();
	
	@DefaultMessage("Manager View:")
	String propManagerView();
	@DefaultMessage("The schedule manager of the class (manager of the department that is managing the class, if externally managed) can view class data but cannot change it.")
	String descManagerView();

	@DefaultMessage("Manager Limited Edit:")
	String propManagerLimitedEdit();
	@DefaultMessage("The schedule manager of the class can assign instructors.")
	String descManagerLimitedEdit();

	@DefaultMessage("Manager Edit:")
	String propManagerEdit();
	@DefaultMessage("The schedule manager of the class can edit class data.")
	String descManagerEdit();

	@DefaultMessage("Audit:")
	String propAudit();
	@DefaultMessage("The schedule manager can run the solver, but cannot save timetables.")
	String descAudit();

	@DefaultMessage("Timetable:")
	String propTimetable();
	@DefaultMessage("The schedule manager can create timetables and save them, but cannot commit.")
	String descTimetable();

	@DefaultMessage("Commit:")
	String propCommit();
	@DefaultMessage("The schedule manager can commit a timetable.")
	String descCommit();
	
	@DefaultMessage("Instructor Survey:")
	String propInstructorSurvey();
	@DefaultMessage("Instructors can fill in their requirements using the Instructor Survey page.")
	String descInstructorSurvey();

	@DefaultMessage("Exam View:")
	String propExamView();
	@DefaultMessage("The schedule manager of the department can view examination data of the appropriate subject areas.")
	String descExamView();

	@DefaultMessage("Exam Edit:")
	String propExamEdit();
	@DefaultMessage("The schedule manager of the department can edit examination data of the appropriate subject areas.")
	String descExamEdit();

	@DefaultMessage("Exam Timetable:")
	String propExamTimetable();
	@DefaultMessage("The examination timetabling manager can edit all examination data and create examination schedule.")
	String descExamTimetable();

	@DefaultMessage("Course Requests:")
	String propRegistration();
	@DefaultMessage("Students are able to use the Student Course Requests page to fill in their course and free time requests.")
	String descRegistration();

	@DefaultMessage("Scheduling Assistant:")
	String propAssistant();
	@DefaultMessage("Unauthenticated users and students are able to use the Student Scheduling Assistant.")
	String descAssistant();

	@DefaultMessage("Online Scheduling:")
	String propOnlineSectioning();
	@DefaultMessage("Online student sectioning is fully enabled (e.g., courses must be locked before a change, conflicting students are automatically re-sectioned).")
	String descOnlineSectioning();

	@DefaultMessage("Events:")
	String propEvents();
	@DefaultMessage("Event management is available to non-administrative users (when not set, all rooms are treated as with No Event Management status).")
	String descEvents();

	@DefaultMessage("Class Schedule:")
	String propClassSchedule();
	@DefaultMessage("Class schedule can be presented to unauthenticated users or authenticated users without a role.")
	String descClassSchedule();

	@DefaultMessage("Final Examination Schedule:")
	String propFinalExaminationSchedule();
	@DefaultMessage("Final examination schedule can be presented to unauthenticated users or authenticated users without a role.")
	String descFinalExaminationSchedule();

	@DefaultMessage("Midterm Examination Schedule:")
	String propMidtermExaminationSchedule();
	@DefaultMessage("Midterm examination schedule can be presented to unauthenticated users or authenticated users without a role.")
	String descMidtermExaminationSchedule();

	@DefaultMessage("Allow Roll Forward:")
	String propAllowRollForward();
	@DefaultMessage("An academic session can only be rolled forwarded into an academic session of a status that allows for roll forward.")
	String descAllowRollForward();

	@DefaultMessage("Allow No Role:")
	String propAllowNoRole();
	@DefaultMessage("If the No Role role and this toggle are enabled: Users without any role are allowed to select an academic session of this status using the No Role role (e.g., on the Events page).")
	String descAllowNoRole();

	@DefaultMessage("Test Session:")
	String propTestSession();
	@DefaultMessage("Academic session is marked as a test session. Such a session is not available to no-role users. Also, classes and examinations do not generate any events.")
	String descTestSession();
	
	@DefaultMessage("Add Timetable Manager")
	String actionAddTimetableManager();
	
	@DefaultMessage("T")
	String accessAddTimetableManager();
	
	@DefaultMessage("Add Timetable Manager (Alt + {0})")
	String titleAddTimetableManager(String access);
	
	@DefaultMessage("Manager List - {0}")
	String sectManagerList(String name);
	
	@DefaultMessage("Roles")
	String columnRoles();
	
	@DefaultMessage("Name")
	String columnManagerName();
	
	@DefaultMessage("Email Address")
	String columnEmailAddress();
	
	@DefaultMessage("Subject Area")
	String columnSubjectArea();
	
	@DefaultMessage("Solver Group")
	String columnSolverGroup();
	
	@DefaultMessage("Last Change")
	String columnLastChange();
	
	@DefaultMessage("Last Changes")
	String columnLastChanges();
	
	@DefaultMessage("{0} ({1}) on {2}")
	String formatLastChange(String page, String action, String date);
	
	@DefaultMessage("Last {0} of {1} was made by {2} at {3}.")
	String formatLongLastChange(String operation, String object, String manager, String date);
	
	@DefaultMessage("Last {0} was made by {1} at {2}.")
	String formatShortLastChange(String operation, String manager, String date);
	
	@DefaultMessage("Primary Role")
	String flagPrimaryRole();
	
	@DefaultMessage("* No Email for this Role")
	String explNoEmailForThisRole();
	
	@DefaultMessage("Show all managers")
	String checkShowAllManagers();

	@DefaultMessage("Add Role")
	String actionAddRole();
	
	@DefaultMessage("Role")
	String fieldRole();
	
	@DefaultMessage("Primary Role")
	String fieldPrimaryRole();
	
	@DefaultMessage("First Name")
	String fieldFirstName();

	@DefaultMessage("Middle Name")
	String fieldMiddleName();

	@DefaultMessage("Last Name")
	String fieldLastName();

	@DefaultMessage("Academic Title")
	String fieldAcademicTitle();

	@DefaultMessage("Save")
	String actionSaveManager();
	
	@DefaultMessage("Update")
	String actionUpdateManager();
	
	@DefaultMessage("Delete")
	String actionDeleteManager();
	
	@DefaultMessage("Back")
	String actionBackToManagers();
	
	@DefaultMessage("S")
	String accessSaveManager();
	
	@DefaultMessage("U")
	String accessUpdateManager();
	
	@DefaultMessage("D")
	String accessDeleteManager();
	
	@DefaultMessage("B")
	String accessBackToManagers();
	
	@DefaultMessage("Save Manager (Alt + {0})")
	String titleSaveManager(String access);
	
	@DefaultMessage("Update Manager (Alt + {0})")
	String titleUpdateManager(String access);
	
	@DefaultMessage("Delete Manager (Alt + {0})")
	String titleDeleteManager(String access);
	
	@DefaultMessage("Back to Managers (Alt + {0})")
	String titleBackToManagers(String access);
	
	@DefaultMessage("At least one role must be assigned")
	String errorManagerHasNoRoles();
	
	@DefaultMessage("Duplicate Record - This manager already exists")
	String errorManagerDuplicate();
	
	@DefaultMessage("Lookup")
	String actionLookupManager();
	
	@DefaultMessage("Failed to lookup manager: {0}")
	String errorLookupManager(String error);
	
	@DefaultMessage("The manager and all associated settings will be deleted. Continue?")
	String confirmDeleteManager();
	
	@DefaultMessage("Solver Groups")
	String sectSolverGroups();
	
	@DefaultMessage("Primary")
	String columnPrimaryRole();
	
	@DefaultMessage("Receive Emails")
	String columnReceiveEmails();
	
	@DefaultMessage("Roll {0} Forward Error: To session {1} already contains {0} data.")
	String errorRollForwardNoData(String type, String session);
	
	@DefaultMessage("Roll {0} Forward Error: Must select a session from which to roll forward.")
	String errorRollForwardMissingFromSession(String type);
	
	@DefaultMessage("Roll {0} Forward Error: From session and to session cannot be the same: {1}.")
	String errorRollForwardSessionsMustBeDifferent(String type, String session);
	
	@DefaultMessage("Roll Forward Error: Must select a session to which to roll forward.")
	String errorRollForwardMissingToSession();
	
	@DefaultMessage("Roll {0} Forward Error: {1}")
	String errorRollForwardGeneric(String type, String error);
	
	@DefaultMessage("Invalid subpart location preference roll forward action: {0}")
	String errorRollForwardInvalidSubpartLocationAction(String action);
	
	@DefaultMessage("Invalid subpart time preference roll forward action: {0}")
	String errorRollForwardInvalidSubpartTimeAction(String action);
	
	@DefaultMessage("Invalid class preference roll forward action: {0}")
	String errorRollForwardInvalidClassAction(String action);
	
	@DefaultMessage("Invalid roll forward distribution preferences action: {0}")
	String errorRollForwardInvalidDistributionAction(String action);
	
	@DefaultMessage("Invalid cancelled class roll forward action: {0}")
	String errorRollForwardInvalidCancelAction(String action);
	
	@DefaultMessage("Invalid last like course demand roll forward action: {0}")
	String errorRollForwardInvalidCourseDemandAction(String action);
	
	@DefaultMessage("Error Rolling {0} Forward From Term {1} To Term {2}: {3}")
	String errorRollingForward(String type, String fromSession, String toSession, String message);
	
	@DefaultMessage("Error Rolling {0} Forward To Term {1}: {2}")
	String errorRollingForwardTo(String type, String toSession, String message);
	
	@DefaultMessage("Failed to roll {0} forward.")
	String errorRollForwardFailedAll(String type);
	
	@DefaultMessage("Learning Management System Info")
	String rollForwardLMSInfo();
	
	@DefaultMessage("LMS")
	String rollForwardLMS();
	
	@DefaultMessage("Date Patterns")
	String rollForwardDatePatterns();
	
	@DefaultMessage("Time Patterns")
	String rollForwardTimePatterns();
	
	@DefaultMessage("Departments")
	String rollForwardDepartments();
	
	@DefaultMessage("Configuration")
	String rollForwardConfiguration();
	
	@DefaultMessage("Session Configuration")
	String rollForwardSessionConfiguration();
	
	@DefaultMessage("Managers")
	String rollForwardManagers();
	
	@DefaultMessage("Timetable Managers")
	String rollForwardTimetableManagers();
	
	@DefaultMessage("Buildings")
	String rollForwardBuildings();
	
	@DefaultMessage("Rooms")
	String rollForwardRooms();
	
	@DefaultMessage("Non University Locations")
	String rollForwardNonUniversityLocations();
	
	@DefaultMessage("Room Features")
	String rollForwardRoomsFeatures();
	
	@DefaultMessage("Room Groups")
	String rollForwardRoomsGroups();
	
	@DefaultMessage("Subject Areas")
	String rollForwardSubjectAreas();
	
	@DefaultMessage("Instructors")
	String rollForwardInstructors();
	
	@DefaultMessage("Course Offerings")
	String rollForwardCourseOfferings();
	
	@DefaultMessage("Class Instructors")
	String rollForwardClassInstructors();
	
	@DefaultMessage("Offering Coordinators")
	String rollForwardOfferingCoordinators();
	
	@DefaultMessage("Exam Configuration")
	String rollForwardExamConfiguration();
	
	@DefaultMessage("Midterm Exams")
	String rollForwardMidtermExams();
	
	@DefaultMessage("Final Exams")
	String rollForwardFinalExams();
	
	@DefaultMessage("Last-like Student Course Requests")
	String rollForwardLastLikeStudentCourseRequests();
	
	@DefaultMessage("Student Class Enrollments")
	String rollForwardStudentClassEnrollments();
	
	@DefaultMessage("Course Requests")
	String rollForwardCourseRequests();
	
	@DefaultMessage("Point In Time Data Student Class Enrollments")
	String rollForwardPITStudentClassEnrollments();
	
	@DefaultMessage("Curricula")
	String rollForwardCurricula();
	
	@DefaultMessage("Scheduled Tasks")
	String rollForwardScheduledTasks();
	
	@DefaultMessage("Teaching Requests")
	String rollForwardTeachingRequests();
	
	@DefaultMessage("New Courses")
	String rollForwardNewCourses();
	
	@DefaultMessage("Students")
	String rollForwardStudents();
	
	@DefaultMessage("Reservations")
	String rollForwardReservations();
	
	@DefaultMessage("Roll Forward")
	String actionRollForward();
	
	@DefaultMessage("R")
	String accessRollForward();
	
	@DefaultMessage("Roll Forward (Alt + {0})")
	String titleRollForward(String access);
	
	@DefaultMessage("Roll Forward(s) In Progress")
	String sectRollForwardsInProgress();
	
	@DefaultMessage("Log of {0}")
	String sectionRollForwardLog(String name);
	
	@DefaultMessage("Roll Forward Actions")
	String sectRollForwardActions();
	
	@DefaultMessage("All done.")
	String logAllDone();
	
	@DefaultMessage("Session To Roll Forward To:")
	String propSessionToRollForwardTo();
	
	@DefaultMessage("Roll Departments Forward From Session:")
	String propRollDepartmentsForwardFromSession();
	
	@DefaultMessage("Roll Session Configuration Forward From Session:")
	String propRollSessionConfigFromSession();
	
	@DefaultMessage("Session configuration contains application configuration, standard notes, event room notes, and break times that are applied directly to the session and/or its department(s).\n"
			+ "Individual room notes and break times are rolled forward with the rooms.")
	String infoRollSessionConfigFromSession();
	
	@DefaultMessage("Roll Manager Data Forward From Session:")
	String propRollManagersFromSession();
	
	@DefaultMessage("Roll Building and Room Data Forward From Session:")
	String propRollRoomsFromSession();
	
	@DefaultMessage("Roll Date Pattern Data Forward From Session:")
	String propRollDatePatternsFromSession();
	
	@DefaultMessage("Roll Time Pattern Data Forward From Session:")
	String propRollTimePatternsFromSession();
	
	@DefaultMessage("Roll Learning Management System Data Forward From Session:")
	String propRollLMSFromSession();
	
	@DefaultMessage("Roll Subject Areas Forward From Session:")
	String propRollSubjectsFromSession();
	
	@DefaultMessage("Roll Instructor Data Forward From Session:")
	String propRollInstructorsFromSession();
	
	@DefaultMessage("Roll Course Offerings Forward From Session:")
	String propRollCoursesFormSession();
	
	@DefaultMessage("For Departments:")
	String propForDepartments();
	
	@DefaultMessage("For Subject Areas:")
	String propForSubjectAreas();
	
	@DefaultMessage("Include Wait-Listing and Prohibited Overrides")
	String checkIncludeWaitListAndOverrides();
	
	@DefaultMessage("Scheduling Subpart Level Time Preference Options:")
	String propSubpartLevelTimePrefs();
	
	@DefaultMessage("Roll forward scheduling subpart time preferences")
	String optRollSubpartTimePrefs();
	
	@DefaultMessage("Do not roll forward scheduling subpart time preferences")
	String optNotRollSubpartTimePrefs();
	
	@DefaultMessage("Scheduling Subpart Level Location Preference Options:")
	String propSubpartLevelRoomPrefs();
	
	@DefaultMessage("Roll forward scheduling subpart location preferences")
	String optRollSubpartRoomPrefs();
	
	@DefaultMessage("Do not roll forward scheduling subpart location preferences")
	String optNotRollSubpartRoomPrefs();
	
	@DefaultMessage("Class Level Preference Options:")
	String propClassLevelPrefs();
	
	@DefaultMessage("Ignore all class level preferences")
	String optNoRollClassPrefs();
	
	@DefaultMessage("Promote appropriate class level preferences to subparts")
	String optPushClassPrefsUp();
	
	@DefaultMessage("Roll forward class level preferences")
	String optRollClassPrefs();
	
	@DefaultMessage("Distribution Preferences:")
	String propDistributionPrefs();
	
	@DefaultMessage("Roll forward all distribution preferences")
	String optRollDistPrefsAll();
	
	@DefaultMessage("Roll forward all distribution preferences, except those that are put solely on classes")
	String optRollDistPrefsMixed();
	
	@DefaultMessage("Roll forward only distribution preferences that are put solely on subparts")
	String optRollDistPrefsSubparts();
	
	@DefaultMessage("Do not roll forward distribution preferences")
	String optRollDistPrefsNone();
	
	@DefaultMessage("Cancelled Classes:")
	String propCancelledClasses();
	
	@DefaultMessage("Roll forward cancelled classes as they are (keep)")
	String optCancelledClassesKeep();
	
	@DefaultMessage("Roll forward cancelled classes as offered (reopen)")
	String optCancelledClassesReopen();
	
	@DefaultMessage("Do not roll forward cancelled classes (skip)")
	String optCancelledClassesSkip();
	
	@DefaultMessage("Roll Forward Class Instructors For Subject Areas:")
	String propRollClassInstructorsForSubjects();
	
	@DefaultMessage("Roll Forward Offering Coordinators For Subject Areas:")
	String propRollOfferingCoordinatorsForSubjects();
	
	@DefaultMessage("Roll Forward Teaching Request For Subject Areas:")
	String propRollTeachingRequestsForSubjects();
	
	@DefaultMessage("Add New Course Offerings For Subject Areas:")
	String propAddNewCoursesForSubjects();
	
	@DefaultMessage("Note: Only use this after all existing course\nofferings have been rolled forward to avoid\nerrors with cross lists.")
	String infoAddNewCoursesForSubjects();
	
	@DefaultMessage("Roll Exam Configuration Data Forward From Session:")
	String propRollExamConfigFromSession();
	
	@DefaultMessage("Roll Midterm Exams Forward")
	String propRollMidtermExams();
	
	@DefaultMessage("Roll Final Exams Forward")
	String propRollFinalExams();
	
	@DefaultMessage("Preferences:")
	String propPreferences();
	
	@DefaultMessage("Roll forward all midterm examination preferences")
	String prefMidtermExamsAll();
	
	@DefaultMessage("Roll forward building, room feature and room group preferences (exclude period and individual room preferences)")
	String prefMidtermExamsRoom();
	
	@DefaultMessage("Do not roll forward any midterm examination preferences")
	String prefMidtermExamsNone();
	
	@DefaultMessage("Roll forward all final examination preferences")
	String prefFinalExamsAll();
	
	@DefaultMessage("Roll forward building, room feature and room group preferences (exclude period and individual room preferences)")
	String prefFinalExamsRoom();
	
	@DefaultMessage("Do not roll forward any final examination preferences")
	String prefFinalExamsNone();
	
	@DefaultMessage("Import Last-Like Course Demands")
	String propImportLastLikes();
	
	@DefaultMessage("Copy Last-like Course Demands From Previous Session")
	String optLastLikeCopy();
	
	@DefaultMessage("Import Last-like Course Demands From Student Class Enrollments Of Previous Session")
	String optLastLikeEnrls();
	
	@DefaultMessage("Import Last-like Course Demands From Course Requests Of Previous Session")
	String optLastLikeCourseReqs();
	
	@DefaultMessage("Import Last-like Course Demands From a Point In Time Snapshot of Student Class Enrollments Of Previous Session")
	String optLastLikePIT();
	
	@DefaultMessage("Point In Time Data Snapshot To Use:")
	String propPointInTimeSnapshot();
	
	@DefaultMessage("Roll Curricula Forward From Session:")
	String propRollCurriculaFromSession();
	
	@DefaultMessage("This will also roll academic areas, classifications, majors, minors, and projection rules forward (if these are not already present in the target academic session).")
	String infoRollCurriculaFromSession();
	
	@DefaultMessage("Roll Reservations Forward From Session:")
	String propRollReservationsFromSession();
	
	@DefaultMessage("Include Course Reservations")
	String optIncludeCourseReservations();
	
	@DefaultMessage("Include Curriculum Reservations")
	String optIncludeCurriculumReservations();
	
	@DefaultMessage("Include Student Group Reservations")
	String optIncludeStudentGroupReservations();
	
	@DefaultMessage("Include Student Filter Reservations")
	String optIncludeStudentUniversalReservations();
	
	@DefaultMessage("New Start Date:")
	String propNewStartDate();
	
	@DefaultMessage("New Expiration Date:")
	String propNewExpirationDate();
	
	@DefaultMessage("Applies to course reservations with a start date filled in.")
	String infoNewStartDateCourse();
	
	@DefaultMessage("Applies to course reservations with an expiration date filled in.")
	String infoNewExpirationDateCourse();
	
	@DefaultMessage("Applies to curriculum reservations with a start date filled in.")
	String infoNewStartDateCurriculum();
	
	@DefaultMessage("Applies to curriculum reservations with an expiration date filled in.")
	String infoNewExpirationDateCurriculum();
	
	@DefaultMessage("Applies to student group reservations with a start date filled in.")
	String infoNewStartDateGroup();
	
	@DefaultMessage("Applies to student group reservations with an expiration date filled in.")
	String infoNewExpirationDateGroup();
	
	@DefaultMessage("Applies to student filter reservations with a start date filled in.")
	String infoNewStartDateUniversal();
	
	@DefaultMessage("Applies to student filter reservations with an expiration date filled in.")
	String infoNewExpirationDateUniversal();
	
	@DefaultMessage("Create student groups that do not exist (with no students). Ignore group reservations that do not match otherwise.")
	String optCreateStudentGroupsForReservations();
	
	@DefaultMessage("Roll Scheduled Tasks Forward From Session:")
	String propRollScheduledTasksFromSession();
	
	@DefaultMessage("Preview of {0}")
	String sectPreviewOfDatePattern(String name);
	
	@DefaultMessage("Not Available")
	String legendNotAvailable();
	
	@DefaultMessage("Classes Offered")
	String legendClassesOffered();
	
	@DefaultMessage("Classes Not Offered")
	String legendClassesNotOffered();
	
	@DefaultMessage("No Holiday")
	String legendNoHoliday();
	
	@DefaultMessage("Holiday")
	String legendHoliday();
	
	@DefaultMessage("(Spring/October/Thanksgiving) Break")
	String legendBreak();
	
	@DefaultMessage("Break")
	String legendBreakShort();
	
	@DefaultMessage("Start / End")
	String legendClassesStartEnd();
	
	@DefaultMessage("Examination Start")
	String legenExaminationStart();
	
	@DefaultMessage("Assignment")
	String legendAssignment();
	
	@DefaultMessage("Room Features")
	String labelRoomfeatures();
	
	@DefaultMessage("Allow variable limits")
	String labelAllowVariableLimits();
	
	@DefaultMessage("WARNING: Application of this preference will remove all required preferences.")
	String alertReqPrefsWillBeRemoved();
	
	@DefaultMessage("WARNING: Application of required preference will remove all not required preferences.")
	String alertNotReqPrefsWillBeRemoved();
	
	@DefaultMessage("Instructor Survey")
	String actionInstructorSurvey();
	
	@DefaultMessage("Open Instructor Survey (Alt + {0})")
	String titleInstructorSurvey(String access);
	
	@DefaultMessage("S")
	String accessInstructorSurvey();
	
	@DefaultMessage("Survey<br>Submitted")
	String columnInstrSurvey();
	
	@DefaultMessage("Survey\nSubmitted")
	String columnInstrSurveyPDF();
	
	@DefaultMessage("Not Submitted")
	String instrSurveyNotSubmitted();
	
	@DefaultMessage("Not Filled")
	String instrSurveyNotFilled();
	
	@DefaultMessage("~~ No Requirements ~~")
	String courseOtherInstructorClasses();
	
	@DefaultMessage("Notifications Begin Date")
	String columnNotificationsBeginDate();
	
	@DefaultMessage("Notifications End Date")
	String columnNotificationsEndDate();
	
	@DefaultMessage("Notifications End Date must occur AFTER Notifications Start Date.")
	String errorNotificationsEndDateNotAfterNotificationsStartDate();
	
	@DefaultMessage("Email\nNotifications")
	String columnAcademicSessionNotificationsDates();
	
	@DefaultMessage("Update Academic Sessions")
	String columnAcademicSessionsToUpdate();
	
	@DefaultMessage("From {0}")
	String notificationDatesFrom(String date);
	
	@DefaultMessage("To {0}")
	String notificationDatesTo(String date);
	
	@DefaultMessage("Between {0} and {1}")
	String notificationDatesBetween(String fromDate, String toDate);
	
	@DefaultMessage("{0} \u00d7 {1}")
	String cellNbrRoomsAndRoomRatio(int nbrRooms, String roomRatio);
	
	@DefaultMessage("{0} @ {1}")
	String cellNbrRoomsAndRoomRatioSlitAttendance(int nbrRooms, String roomRatio);
	
	@DefaultMessage("{0} at {1} each room")
	String titleNbrRoomsAndRoomRatio(int nbrRooms, String roomRatio);
	
	@DefaultMessage("{0} at {1} total capacity")
	String titleNbrRoomsAndRoomRatioSlitAttendance(int nbrRooms, String roomRatio);
	
	@DefaultMessage("Total")
	String descClassMultipleRoomsSplitAttendance();
	
	@DefaultMessage("Each Room")
	String descClassMultipleRoomsAlternativeAttendance();
	
	@DefaultMessage("Attendance:")
	String propertyRoomSplitAttendance();
	
	@DefaultMessage("<b>Split</b> -- Class is split between multiple rooms.")
	String descriptionClassMultipleRoomsSplitAttendance();
	
	@DefaultMessage("<b>Alternative</b> -- Class must fit each room.")
	String descriptionClassMultipleRoomsAlternativeAttendance();
	
	@DefaultMessage("All Rooms")
	String itemAllRooms();
	
	@DefaultMessage("{0}. Room")
	String itemOnlyRoom(int roomNumber);
	
	@DefaultMessage("No rooms are available.")
	String warnNoRoomsAreAvaliable();
	
	@DefaultMessage("Not enough rooms are available:")
	String warnNotEnoughtRoomsAreAvaliable();
	
	@DefaultMessage("... {0} more")
	String moreAvailableRooms(int more);
	
	@DefaultMessage("Page:")
	String filterPage();
	
	@DefaultMessage("Type:")
	String filterChartType();
	
	@DefaultMessage("Interval:")
	String filterChartInterval();
	
	@DefaultMessage("From:")
	String filterChartFrom();
	
	@DefaultMessage("To:")
	String filterChartTo();
	
	@DefaultMessage("Basic Information")
	String chartModeBasic();
	
	@DefaultMessage("Active Users")
	String chartModeActive();
	
	@DefaultMessage("Average Times")
	String chartModeTimes();
	
	@DefaultMessage("Last Day")
	String chartIntervalLastDay();
	
	@DefaultMessage("Last Three Hours")
	String chartIntervalLast3Hours();
	
	@DefaultMessage("Last Hour")
	String chartIntervalLastHour();

	@DefaultMessage("Last Week")
	String chartIntervalLastWeek();

	@DefaultMessage("Last Month")
	String chartIntervalLastMonth();
	
	@DefaultMessage("Custom")
	String chartIntervalCustom();
	
	@DefaultMessage("Date")
	String chartBasicDate();

	@DefaultMessage("Opened")
	String chartBasicOpened();
	
	@DefaultMessage("Access")
	String chartBasicAccess();
	
	@DefaultMessage("Active")
	String chartBasicActive();
	
	@DefaultMessage("Waiting")
	String chartBasicWaiting();
	
	@DefaultMessage("Got In")
	String chartBasicGotIn();

	@DefaultMessage("Left")
	String chartBasicLeft();

	@DefaultMessage("Gave Up")
	String chartBasicGaveUp();
	
	@DefaultMessage("1 min")
	String chartActive1m();

	@DefaultMessage("2 mins")
	String chartActive2m();

	@DefaultMessage("5 mins")
	String chartActive5m();

	@DefaultMessage("10 mins")
	String chartActive10m();

	@DefaultMessage("15 mins")
	String chartActive15m();
	
	@DefaultMessage("Access Time [m]")
	String chartTimesAccess();
	
	@DefaultMessage("Wait Time [m]")
	String chartTimesWait();

	@DefaultMessage("Access when left [m]")
	String chartTimesAccessLeft();
	
	@DefaultMessage("Wait when got in [m]")
	String chartTimesWaitGotIn();
	
	@DefaultMessage("Multiple Departments")
	String subpartMultipleManagers();
	
	@DefaultMessage("Cap")
	String columnAssignedRoomCap();
	
	@DefaultMessage("Are you sure you want to change the default date pattern?")
	String confirmDefaultDatePatternChange();
}