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
package org.unitime.timetable.gwt.resources;

/**
 * @author Tomas Muller
 */
public interface GwtMessages extends Messages {
	
	@DefaultMessage("{0} Help")
	String pageHelp(String pageTitle);
	
	@DefaultMessage("Close {0}")
	String pageClose(String pageTitle);

	@DefaultMessage("Version {0} built on {1}")
	String pageVersion(String version, String buildDate);
	
	@DefaultMessage("UniTime {0}")
	String unitimeVersion(String version);
	
	@DefaultMessage("&copy; 2008 - 2025 The Apereo Foundation,<br>distributed under the Apache License, Version 2.")
	String pageCopyright();
	
	@DefaultMessage("UniTime {0}, \u00A9 2008 - 2025 The Apereo Foundation, distributed under the Apache License.")
	String pdfCopyright(String version);
	
	@DefaultMessage("Oooops, the loading is taking too much time... Something probably went wrong. You may need to reload this page.")
	String warnLoadingTooLong();
	
	@DefaultMessage("The operation may take a lot of time...<br>Click this message to cancel the operation.")
	String warnLoadingTooLongCanCancel();

	@DefaultMessage("Login is required to access this page.")
	String authenticationRequired();
	
	@DefaultMessage("Your timetabling session has expired. Please log in again.")
	String authenticationExpired();

	@DefaultMessage("Insufficient user privileges.")
	String authenticationInsufficient();
	
	@DefaultMessage("No academic session selected.")
	String authenticationNoSession();

	@DefaultMessage("Export in iCalendar format.")
	String exportICalendar();
	
	@DefaultMessage("Select All")
	String opSelectAll();

	@DefaultMessage("Select All Conflicting")
	String opSelectAllConflicting();

	@DefaultMessage("Clear Selection")
	String opClearSelection();
	
	@DefaultMessage("&#10007; R<u>e</u>move")
	String opDeleteSelectedMeetings();
	
	@DefaultMessage("&#10008 R<u>e</u>move All")
	String opDeleteNewMeetings();
	
	@DefaultMessage("Setup <u>T</u>imes ...")
	String opChangeOffsets();
	
	@DefaultMessage("<u>M</u>odify ...")
	String opModifyMeetings();

	@DefaultMessage("<b><i>+</i></b> Add Meeti<u>n</u>gs ...")
	String opAddMeetings();

	@DefaultMessage("Sort by {0}")
	String opSortBy(String column);
	
	@DefaultMessage("&#9744; {0}")
	String opShow(String column);

	@DefaultMessage("&#9746; {0}")
	String opHide(String column);
	
	@DefaultMessage("&#9744; {0}")
	String opCheck(String column);

	@DefaultMessage("&#9746; {0}")
	String opUncheck(String column);

	@DefaultMessage("&#10003; <u>A</u>pprove ...")
	String opApproveSelectedMeetings();

	@DefaultMessage("&#10004; <u>A</u>pprove All ...")
	String opApproveAllMeetings();

	@DefaultMessage("&#10007; <u>R</u>eject ...")
	String opRejectSelectedMeetings();

	@DefaultMessage("&#10008; <u>R</u>eject All ...")
	String opRejectAllMeetings();

	@DefaultMessage("&#10007; <u>C</u>ancel ...")
	String opCancelSelectedMeetings();

	@DefaultMessage("&#10008; <u>C</u>ancel All ...")
	String opCancelAllMeetings();

	@DefaultMessage("&#10007; <u>C</u>ancel")
	String opCancelSelectedMeetingsNoPopup();

	@DefaultMessage("&#10008; <u>C</u>ancel All")
	String opCancelAllMeetingsNoPopup();

	@DefaultMessage("<i>?</i> <u>I</u>nquire ...")
	String opInquireSelectedMeetings();

	@DefaultMessage("<b><i>?</i></b> <u>I</u>nquire ...")
	String opInquireAllMeetings();
	
	@DefaultMessage("<u>A</u>pprove Meetings")
	String opApproveMeetings();

	@DefaultMessage("<u>R</u>eject Meetings")
	String opRejectMeetings();

	@DefaultMessage("<u>I</u>nquire")
	String opInquireMeetings();

	@DefaultMessage("<u>C</u>ancel Meetings")
	String opCancelMeetings();
	
	@DefaultMessage("<u>B</u>ack")
	String opBack();

	@DefaultMessage("Export PDF")
	String opExportPDF();

	@DefaultMessage("Export CSV")
	String opExportCSV();
	
	@DefaultMessage("Export XLS")
	String opExportXLS();

	@DefaultMessage("Export iCalendar")
	String opExportICalendar();
	
	@DefaultMessage("Export XML")
	String opExportXML();
		
	@DefaultMessage("Expand All")
	String opExpandAll();
	
	@DefaultMessage("Collapse All")
	String opCollapseAll();
	
	@DefaultMessage("Sort by default")
	String opSortDefault();
	
	@DefaultMessage("Hide All")
	String opHideAll();
	
	@DefaultMessage("Show All")
	String opShowAll();
	
	@DefaultMessage("Hide {0}")
	String opHideItem(String item);
	
	@DefaultMessage("Show {0}")
	String opShowItem(String item);
	
	@DefaultMessage("Hide Details")
	String opHideDetails();
	
	@DefaultMessage("Show Details")
	String opShowDetails();
	
	@DefaultMessage("Show All Courses")
	String opShowAllCourses();
	
	@DefaultMessage("Show Empty Courses")
	String opShowEmptyCourses();
	
	@DefaultMessage("Hide Empty Courses")
	String opHideEmptyCourses();
	
	@DefaultMessage("Select All Courses")
	String opSelectAllCourses();
	
	@DefaultMessage("Remove Selected Courses")
	String opRemoveSelectedCourses();

	@DefaultMessage("Hide Empty Classifications")
	String opHideEmptyClassifications();
	
	@DefaultMessage("Show All Classifications")
	String opShowAllClassifications();
	
	@DefaultMessage("Show {0} Enrollment")
	String opShowEnrollmentByType(String type);
	
	@DefaultMessage("Populate Course Projected Demands")
	String opPopulateCourseProjectedDemands();
	
	@DefaultMessage("Populate Course Projected Demands (Include Other Students)")
	String opPopulateCourseProjectedDemandsIncludeOther();
	
	@DefaultMessage("Show Numbers")
	String opShowNumbers();
	
	@DefaultMessage("Show Percentages")
	String opShowPercentages();
	
	@DefaultMessage("New group...")
	String opNewGroup();
	
	@DefaultMessage("Clear Requested Enrollments")
	String opClearRequestedEnrollment();
	
	@DefaultMessage("Clear Requested Enrollments (Selected Courses Only)")
	String opClearRequestedEnrollmentSelectedCoursesOnly();
	
	@DefaultMessage("Clear Requested Enrollments (All Classifications)")
	String opClearRequestedEnrollmentAllClassifications();
	
	@DefaultMessage("Clear Requested Enrollments (All Classifications, Selected Courses Only)")
	String opClearRequestedEnrollmentAllClassificationsSelectedCoursesOnly();
	
	@DefaultMessage("Copy Last-Like &rarr; Requested")
	String opCopyLastLikeToRequested();
	
	@DefaultMessage("Copy Last-Like &rarr; Requested (Selected Courses Only)")
	String opCopyLastLikeToRequestedSelectedCoursesOnly();
	
	@DefaultMessage("Copy Last-Like &rarr; Requested (All Classifications)")
	String opCopyLastLikeToRequestedAllClassifications();
	
	@DefaultMessage("Copy Last-Like &rarr; Requested (All Classifications, Selected Courses Only)")
	String opCopyLastLikeToRequestedAllClassificationsSelectedCoursesOnly();
	
	@DefaultMessage("Copy Current &rarr; Requested")
	String opCopyCurrentToRequested();
	
	@DefaultMessage("Copy Current &rarr; Requested (Selected Courses Only)")
	String opCopyCurrentToRequestedSelectedCoursesOnly();
	
	@DefaultMessage("Copy Current &rarr; Requested (All Classifications)")
	String opCopyCurrentToRequestedAllClassifications();
	
	@DefaultMessage("Copy Current &rarr; Requested (All Classifications, Selected Courses Only)")
	String opCopyCurrentToRequestedAllClassificationsSelectedCoursesOnly();
	
	@DefaultMessage("Copy Projection &rarr; Requested")
	String opCopyProjectionToRequested();
	
	@DefaultMessage("Copy Projection &rarr; Requested (Selected Courses Only)")
	String opCopyProjectionToRequestedSelectedCoursesOnly();
	
	@DefaultMessage("Copy Projection &rarr; Requested (All Classifications)")
	String opCopyProjectionToRequestedAllClassifications();
	
	@DefaultMessage("Copy Projection &rarr; Requested (All Classifications, Selected Courses Only)")
	String opCopyProjectionToRequestedAllClassificationsSelectedCoursesOnly();
	
	@DefaultMessage("Copy Registered &rarr; Requested")
	String opCopyCourseRequestsToRequested();
	
	@DefaultMessage("Copy Registered &rarr; Requested (Selected Courses Only)")
	String opCopyCourseRequestsToRequestedSelectedCoursesOnly();
	
	@DefaultMessage("Copy Registered &rarr; Requested (All Classifications)")
	String opCopyCourseRequestsToRequestedAllClassifications();
	
	@DefaultMessage("Copy Registered &rarr; Requested (All Classifications, Selected Courses Only)")
	String opCopyCourseRequestsToRequestedAllClassificationsSelectedCoursesOnly();
	
	@DefaultMessage("Assign")
	String opGroupAssign();
	
	@DefaultMessage("Update")
	String opGroupUpdate();
	
	@DefaultMessage("Delete")
	String opGroupDelete();
	
	@DefaultMessage("Save")
	String opQuerySave();
	
	@DefaultMessage("Delete")
	String opQueryDelete();
	
	@DefaultMessage("Test")
	String opQueryTest();
	
	@DefaultMessage("Back")
	String opQueryBack();
	
	@DefaultMessage("Export")
	String opQueryExport();

	@DefaultMessage("Save")
	String opScriptSave();
	
	@DefaultMessage("Delete")
	String opScriptDelete();
	
	@DefaultMessage("Update")
	String opScriptUpdate();
	
	@DefaultMessage("Export")
	String opScriptExport();
	
	@DefaultMessage("Back")
	String opScriptBack();
	
	@DefaultMessage("Send Email")
	String opSendEmail();
	
	@DefaultMessage("Edit Requested Enrollments")
	String opEditRequestedEnrollments();
	
	@DefaultMessage("Delete Selected Curricula")
	String opDeleteSelectedCurricula();
	
	@DefaultMessage("Merge Selected Curricula")
	String opMergeSelectedCurricula();
	
	@DefaultMessage("Curriculum Projection Rules")
	String opCurriculumProjectionRules();
	
	@DefaultMessage("Update Requested Enrollment by Projection Rules")
	String opUpdateRequestedEnrollmentByProjectionRules();
	
	@DefaultMessage("Update Requested Enrollment And Course Projections")
	String opUpdateRequestedEnrollmentAndCourseProjections();
	
	@DefaultMessage("Create Curricula from Last-Like Enrollments &amp; Projections")
	String opCreateCurriculaFromLastLike();
	
	@DefaultMessage("Re-Create Curricula from Last-Like Enrollments &amp; Projections")
	String opRecreateCurriculaFromLastLike();
	
	@DefaultMessage("Create Curricula from Course Registrations")
	String opCreateCurriculaFromCourseRequests();
	
	@DefaultMessage("Re-Create Curricula from Course Registrations")
	String opRecreateCurriculaFromCourseRequests();
	
	@DefaultMessage("Show Names")
	String opShowNames();
	
	@DefaultMessage("Show Abbreviations")
	String opShowAbbreviations();
	
	@DefaultMessage("Swap Axes")
	String opSwapAxes();
	
	@DefaultMessage("Approve New Meetings")
	String opAutomaticApproval();
	
	@DefaultMessage("Condense Repetitions")
	String opHideRepeatingInformation();
	
	@DefaultMessage("Columns")
	String opColumns();
	
	@DefaultMessage("Sort By")
	String opSort();
	
	@DefaultMessage("Department")
	String opDepartmentFormat();
	
	@DefaultMessage("Availability")
	String opOrientation();
	
	@DefaultMessage("Show as Text")
	String opOrientationAsText();
	
	@DefaultMessage("Vertical")
	String opOrientationVertical();
	
	@DefaultMessage("Show as Table")
	String opOrientationAsGrid();

	@DefaultMessage("Horizontal")
	String opOrientationHorizontal();
	
	@DefaultMessage("Load")
	String opSolverLoad();
	
	@DefaultMessage("Start")
	String opSolverStart();
	
	@DefaultMessage("Stop")
	String opSolverStop();
	
	@DefaultMessage("Student Sectioning")
	String opSolverStudentSectioning();
	
	@DefaultMessage("Reload Input Data")
	String opSolverReload();
	
	@DefaultMessage("Validate")
	String opSolverValidate();
	
	@DefaultMessage("Clear")
	String opSolverClear();
	
	@DefaultMessage("Export Solution")
	String opSolverExportCSV();
	
	@DefaultMessage("Unload")
	String opSolverUnload();
	
	@DefaultMessage("<u>R</u>efresh")
	String opSolverRefresh();
	
	@DefaultMessage("Save")
	String opSolverSave();

	@DefaultMessage("Save As New")
	String opSolverSaveAsNew();

	@DefaultMessage("Save & Commit")
	String opSolverSaveCommit();

	@DefaultMessage("Save As New & Commit")
	String opSolverSaveAsNewCommit();
	
	@DefaultMessage("Save & Uncommit")
	String opSolverSaveUncommit();
	
	@DefaultMessage("Export XML")
	String opSolverExportXML();
	
	@DefaultMessage("Save To Best")
	String opSolverSaveBest();
	
	@DefaultMessage("Restore From Best")
	String opSolverRestorBest();
	
	@DefaultMessage("<u>R</u>efresh")
	String opSolverLogRefresh();
	
	@DefaultMessage("Publish")
	String opSolverPublish();
	
	@DefaultMessage("Clone")
	String opSolverClone();
	
	@DefaultMessage("Deselect")
	String opSolutionDeselect();
	
	@DefaultMessage("Update Note")
	String opSolutionUpdateNote();
	
	@DefaultMessage("Commit")
	String opSolutionCommit();
	
	@DefaultMessage("Uncommit")
	String opSolutionUncommit();
	
	@DefaultMessage("Export Solution")
	String opSolutionExport();
	
	@DefaultMessage("Delete")
	String opSolutionDelete();
	
	@DefaultMessage("Add Meeting Contact ...")
	String opAddMeetingContact();
	
	@DefaultMessage("Remove Meeting Contact ...")
	String opRemoveMeetingContact();
 
	@DefaultMessage("Load Empty Solution")
	String opSolverLoadEmptySolution();
	
	@DefaultMessage("Save")
	String opTaskSave();
	
	@DefaultMessage("Delete")
	String opTaskDelete();
	
	@DefaultMessage("Update")
	String opTaskUpdate();
	
	@DefaultMessage("Back")
	String opTaskBack();
	
	@DefaultMessage("Press Ctrl + C to copy the selected URL, and Escape to hide this dialog.")
	String hintCtrlCToCopy();

	@DefaultMessage("Date")
	String colDate();

	@DefaultMessage("Day Of Week")
	String colDayOfWeek();

	@DefaultMessage("First Date")
	String colFirstDate();

	@DefaultMessage("Last Date")
	String colLastDate();

	@DefaultMessage("Published Time")
	String colPublishedTime();

	@DefaultMessage("Published Start")
	String colPublishedStartTime();

	@DefaultMessage("Published End")
	String colPublishedEndTime();

	@DefaultMessage("Allocated Time")
	String colAllocatedTime();

	@DefaultMessage("Allocated Start")
	String colAllocatedStartTime();

	@DefaultMessage("Allocated End")
	String colAllocatedEndTime();

	@DefaultMessage("Setup")
	String colSetupTimeShort();

	@DefaultMessage("Setup Time")
	String colSetupTime();

	@DefaultMessage("Teardown")
	String colTeardownTimeShort();

	@DefaultMessage("Teardown Time")
	String colTeardownTime();

	@DefaultMessage("Location")
	String colLocation();
	
	@DefaultMessage("Capacity")
	String colCapacity();
	
	@DefaultMessage("Exam<br>Capacity")
	String colExaminationCapacity();

	@DefaultMessage("Approved")
	String colApproval();
	
	@DefaultMessage("Status")
	String colStatus();

	@DefaultMessage("Name")
	String colName();
	
	@DefaultMessage("Name")
	String colNamePerson();
	
	@DefaultMessage("Course")
	String colCourse();
	
	@DefaultMessage("Offering")
	String colOffering();

	@DefaultMessage("Configuration")
	String colConfig();

	@DefaultMessage("Section")
	String colSection();
	
	@DefaultMessage("Type")
	String colType();
	
	@DefaultMessage("Content Type")
	String colContentType();
	
	@DefaultMessage("Picture Type")
	String colPictureType();
	
	@DefaultMessage("Title")
	String colTitle();
	
	@DefaultMessage("Time")
	String colTime();

	@DefaultMessage("Instructor")
	String colInstructor();

	@DefaultMessage("Instructor / Sponsor")
	String colSponsorOrInstructor();

	@DefaultMessage("Main Contact")
	String colMainContact();
	
	@DefaultMessage("Limit")
	String colLimit();

	@DefaultMessage("Enrollment")
	String colEnrollment();
	
	@DefaultMessage("Email")
	String colEmail();
	
	@DefaultMessage("Phone")
	String colPhone();
	
	@DefaultMessage("Subject")
	String colSubject();
	
	@DefaultMessage("Subjects")
	String colSubjects();

	@DefaultMessage("Course Number")
	String colCourseNumber();
	
	@DefaultMessage("Courses")
	String colCourses();
	
	@DefaultMessage("Config / Subpart")
	String colConfigOrSubpart();
	
	@DefaultMessage("Configs / Subparts")
	String colConfigsOrSubparts();
	
	@DefaultMessage("Configuration / Class")
	String colConfigOrClass();
	
	@DefaultMessage("Class Number")
	String colClassNumber();
	
	@DefaultMessage("Classes")
	String colClasses();

	@DefaultMessage("User")
	String colUser();
	
	@DefaultMessage("Action")
	String colAction();
	
	@DefaultMessage("Meetings")
	String colMeetings();
	
	@DefaultMessage("Note")
	String colNote();

	@DefaultMessage("Attachment")
	String colAttachment();
	
	@DefaultMessage("Distance")
	String colRoomDistance();

	@DefaultMessage("Availability")
	String colRoomAvailability();
	
	@DefaultMessage("Curriculum")
	String colCurriculum();
	
	@DefaultMessage("Academic Area")
	String colAcademicArea();
	
	@DefaultMessage("Major(s)")
	String colMajors();
	
	@DefaultMessage("Total")
	String colTotal();
	
	@DefaultMessage("Total {0}")
	String colTotalOf(String what);
	
	@DefaultMessage("Other Students")
	String colOtherStudents();
	
	@DefaultMessage("Group")
	String colGroup();
	
	@DefaultMessage("Last&#8209;Like")
	String colLastLike();
	
	@DefaultMessage("Projected")
	String colProjected();
	
	@DefaultMessage("Enrolled")
	String colEnrolled();
	
	@DefaultMessage("Department")
	String colDepartment();
	
	@DefaultMessage("Last-Like<br>Enrollment")
	String colLastLikeEnrollment();
	
	@DefaultMessage("Projected<br>by&nbsp;Rule")
	String colProjectedByRule();
	
	@DefaultMessage("Requested<br>Enrollment")
	String colRequestedEnrollment();
	
	@DefaultMessage("Current<br>Enrollment")
	String colCurrentEnrollment();
	
	@DefaultMessage("Course<br>Registrations")
	String colCourseRequests();
	
	@DefaultMessage("Snapshot<br>Projected<br>by&nbsp;Rule")
	String colSnapshotProjectedByRule();

	@DefaultMessage("Snapshot<br>Requested<br>Enrollment")
	String colSnapshotRequestedEnrollment();

	@DefaultMessage("Snapshot Requested Enrollment:")
	String propSnapshotRequestedEnrollment();

	@DefaultMessage("Snapshot Projected by Rule:")
	String propSnapshotProjectedByRule();
	
	@DefaultMessage("Instructional<br>Offering")
	String colInstructionalOffering();
	
	@DefaultMessage("Reservation<br>Type")
	String colReservationType();
	
	@DefaultMessage("Owner")
	String colOwner();
	
	@DefaultMessage("Configuration")
	String colSolverConfig();
	
	@DefaultMessage("Restrictions")
	String colRestrictions();
	
	@DefaultMessage("Reserved<br>Space")
	String colReservedSpace();
	
	@DefaultMessage("Expiration<br>Date")
	String colExpirationDate();
	
	@DefaultMessage("Start<br>Date")
	String colStartDate();
	
	@DefaultMessage("Source")
	String colSource();
	
	@DefaultMessage("Label")
	String colLabel();
	
	@DefaultMessage("Default")
	String colDefaultValue();
	
	@DefaultMessage("Progress")
	String colProgress();
	
	@DefaultMessage("Session")
	String colSession();
	
	@DefaultMessage("Created")
	String colCreated();
	
	@DefaultMessage("Started")
	String colStarted();
	
	@DefaultMessage("Finished")
	String colFinished();
	
	@DefaultMessage("Output")
	String colOutput();
	
	@DefaultMessage("Conflict")
	String colConflict();

	@DefaultMessage("Picture")
	String colPicture();
	
	@DefaultMessage("Building")
	String colBuilding();
	
	@DefaultMessage("Room")
	String colRoom();
	
	@DefaultMessage("Area [{0}]")
	String colArea(String units);
	
	@DefaultMessage("Coordinates")
	String colCoordinates();
	
	@DefaultMessage("Longitude")
	String colCoordinateX();
	
	@DefaultMessage("Latitude")
	String colCoordinateY();
	
	@DefaultMessage("Distances")
	String colDistances();
	
	@DefaultMessage("Distance")
	String colDistance();
	
	@DefaultMessage("Room<br>Check")
	String colRoomCheck();
	
	@DefaultMessage("Preference")
	String colPreference();
	
	@DefaultMessage("Availability")
	String colAvailability();
	
	@DefaultMessage("Departments")
	String colDepartments();
	
	@DefaultMessage("Control")
	String colControl();
	
	@DefaultMessage("Examination<br>Types")
	String colExamTypes();
	
	@DefaultMessage("Period<br>Preferences")
	String colPeriodPreferences();
	
	@DefaultMessage("Event<br>Department")
	String colEventDepartment();
	
	@DefaultMessage("Event<br>Availability")
	String colEventAvailability();
	
	@DefaultMessage("Event<br>Status")
	String colEventStatus();
	
	@DefaultMessage("Event<br>Message")
	String colEventMessage();
	
	@DefaultMessage("Event<br>Email")
	String colEventEmail();
	
	@DefaultMessage("Break<br>Time")
	String colBreakTime();
	
	@DefaultMessage("Groups")
	String colGroups();
	
	@DefaultMessage("Features")
	String colFeatures();
	
	@DefaultMessage("Map")
	String colMap();
	
	@DefaultMessage("Pictures")
	String colPictures();
	
	@DefaultMessage("Last Change")
	String colLastChange();

	@DefaultMessage("Conflicts with {0}")
	String conflictWith(String event);
	
	@DefaultMessage("External Id")
	String colExternalId();
	
	@DefaultMessage("Room<br>Properties")
	String colChangeRoomProperties();
	
	@DefaultMessage("Examination<br>Properties")
	String colChangeExamProperties();
	
	@DefaultMessage("Event<br>Properties")
	String colChangeEventProperties();
	
	@DefaultMessage("Room<br>Groups")
	String colChangeRoomGroups();
	
	@DefaultMessage("Room<br>Features")
	String colChangeRoomFeatures();
	
	@DefaultMessage("Room<br>Sharing")
	String colChangeRoomSharing();
	
	@DefaultMessage("Examination<br>Preferences")
	String colChangeRoomPeriodPreferences();
	
	@DefaultMessage("Event<br>Availability")
	String colChangeRoomEventAvailability();
	
	@DefaultMessage("Room<br>Pictures")
	String colChangeRoomPictures();
	
	@DefaultMessage("&otimes;")
	String colSelection();
	
	@DefaultMessage("Abbreviation")
	String colAbbreviation();
	
	@DefaultMessage("Default")
	String colDefault();
	
	@DefaultMessage("Rooms")
	String colRooms();
	
	@DefaultMessage("Description")
	String colDescription();
	
	@DefaultMessage("Instructors")
	String colInstructors();
	
	@DefaultMessage("Position")
	String colPosition();

	@DefaultMessage("Preference")
	String colTeachingPreference();

	@DefaultMessage("Maximal Load")
	String colMaxLoad();
	
	@DefaultMessage("Parent")
	String colParentAttribute();
	
	@DefaultMessage("Load")
	String colTeachingLoad();
	
	@DefaultMessage("Attributes")
	String colAttributes();
		
	@DefaultMessage("Instructor<br>Preferences")
	String colInstructorPreferences();
	
	@DefaultMessage("Attribute<br>Preferences")
	String colAttributePreferences();
	
	@DefaultMessage("Time<br>Preferences")
	String colTimePreferences();
	
	@DefaultMessage("Room<br>Preferences")
	String colRoomPreferences();
	
	@DefaultMessage("Course<br>Preferences")
	String colCoursePreferences();
	
	@DefaultMessage("Distribution<br>Preferences")
	String colDistributionPreferences();
	
	@DefaultMessage("Assigned<br>Load")
	String colAssignedLoad();
	
	@DefaultMessage("Objectives")
	String colObjectives();
	
	@DefaultMessage("Idx")
	String colIndex();
	
	@DefaultMessage("Assigned<br>Instructors")
	String colAssignedInstructors();
	
	@DefaultMessage("Conflicts")
	String colConflictingRequests();
	
	@DefaultMessage("Score")
	String colScore();
	
	@DefaultMessage("Role")
	String colRole();
	
	@DefaultMessage("Class")
	String colClass();
	
	@DefaultMessage("Instructional Type")
	String colInstructionalType();
	
	@DefaultMessage("Assign")
	String colAssignInstructor();
	
	@DefaultMessage("Share")
	String colPercentShare();
	
	@DefaultMessage("% Share")
	String colPercentShareInstructor();
	
	@DefaultMessage("Responsibility")
	String colTeachingResponsibility();
	
	@DefaultMessage("Lead")
	String colInstructorLead();
	
	@DefaultMessage("Can Overlap")
	String colCanOverlap();
	
	@DefaultMessage("Common")
	String colCommonPart();
	
	@DefaultMessage("Students")
	String colNrStudentConflicts();
	
	@DefaultMessage("Std")
	String colShortStudentConflicts();
	
	@DefaultMessage("Tm")
	String colShortTimePref();
	
	@DefaultMessage("Rm")
	String colShortRoomPref();
	
	@DefaultMessage("Gr")
	String colShortDistPref();
	
	@DefaultMessage("Ins")
	String colShortInstructorBtbPref();
	
	@DefaultMessage("Usl")
	String colShortUselessHalfHours();
	
	@DefaultMessage("Big")
	String colShortTooBigRooms();
	
	@DefaultMessage("Dept")
	String colShortDepartmentBalance();
	
	@DefaultMessage("Subp")
	String colShortSameSubpartBalance();
	
	@DefaultMessage("Pert")
	String colShortPerturbations();
	
	@DefaultMessage("Conf")
	String colShortUnassignments();
	
	@DefaultMessage("Students")
	String colNrAssignedStudents();
	
	@DefaultMessage("Assign")
	String colShortAssignedVariables();
	
	@DefaultMessage("Total")
	String colShortTotalValue();
	
	@DefaultMessage("Initial Assignment")
	String colInitialAssignment();
	
	@DefaultMessage("Meeting Contact")
	String colMeetingContacts();
	
	@DefaultMessage("Requested Services")
	String colRequestedServices();
	
	@DefaultMessage("Available<br>Services")
	String colAvailableServices();
	
	@DefaultMessage("Constraint")
	String colConflictingConstraint();
	
	@DefaultMessage("Student Conflicts")
	String colStudentConflicts();
	
	@DefaultMessage("Distribution Conflicts")
	String colDistributionConflicts();

	@DefaultMessage("Distribution")
	String colDistribution();
	
	@DefaultMessage("Time")
	String colTimeStamp();

	@DefaultMessage("Configuration")
	String colSolverConfiguration();
	
	@DefaultMessage("Committed")
	String colCommitted();
	
	@DefaultMessage("Group")
	String colRoomReportGroup();
	
	@DefaultMessage("Size")
	String colRoomReportActualSizes();
	
	@DefaultMessage("NbrRooms")
	String colRoomReportNbrRooms();
	
	@DefaultMessage("ClUse")
	String colRoomReportClassUse();
	
	@DefaultMessage("ClShould")
	String colRoomReportClassShould();
	
	@DefaultMessage("ClMust")
	String colRoomReportClassMust();
	
	@DefaultMessage("HrUse")
	String colRoomReportHourUse();
	
	@DefaultMessage("HrShould")
	String colRoomReportHourShould();
	
	@DefaultMessage("HrMust")
	String colRoomReportHourMust();
	
	@DefaultMessage("Penalty")
	String colPenalty();
	
	@DefaultMessage("Type")
	String colDistrubutionType();
	
	@DefaultMessage("Violations")
	String colViolations();
	
	@DefaultMessage("Conflicts")
	String colNrConflicts();
	
	@DefaultMessage("Committed")
	String colStudentConflictCommitted();

	@DefaultMessage("Fixed")
	String colStudentConflictFixed();

	@DefaultMessage("Hard")
	String colStudentConflictHard();

	@DefaultMessage("Distance")
	String colStudentConflictDistance();

	@DefaultMessage("Important")
	String colStudentConflictImportant();
	
	@DefaultMessage("Workday")
	String colStudentConflictWorkday();

	@DefaultMessage("Instructor")
	String colStudentConflictInstructor();
	
	@DefaultMessage("Dist")
	String colShortDist();
	
	@DefaultMessage("St")
	String colPerturbationStudents();
	
	@DefaultMessage("StT")
	String colPerturbationStudentsTime();
	
	@DefaultMessage("StR")
	String colPerturbationStudentsRoom();
	
	@DefaultMessage("StB")
	String colPerturbationStudentsBuilding();
	
	@DefaultMessage("In")
	String colPerturbationInstructor();
	
	@DefaultMessage("InT")
	String colPerturbationInstructorTime();
	
	@DefaultMessage("InR")
	String colPerturbationInstructorRoom();
	
	@DefaultMessage("InB")
	String colPerturbationInstructorBuilding();
	
	@DefaultMessage("Rm")
	String colPerturbationRoom();
	
	@DefaultMessage("Bld")
	String colPerturbationBuilding();
	
	@DefaultMessage("Tm")
	String colPerturbationTime();
	
	@DefaultMessage("Day")
	String colPerturbationDay();

	@DefaultMessage("Hr")
	String colPerturbationHour();

	@DefaultMessage("TFSt")
	String colPerturbationTooFarStudent();

	@DefaultMessage("TFInst")
	String colPerturbationTooFarInstructor();

	@DefaultMessage("DStC")
	String colPerturbationDeltaStudentConflicts();

	@DefaultMessage("NStC")
	String colPerturbationNewStudentConflicts();

	@DefaultMessage("DTPr")
	String colPerturbationDeltaTimePref();

	@DefaultMessage("DRPr")
	String colPerturbationDeltaRoomPref();

	@DefaultMessage("DInsB")
	String colPerturbationDeltaInstructorBTB();
	
	@DefaultMessage("Students")
	String colStudents();
	
	@DefaultMessage("Task Name")
	String colTaskName();
	
	@DefaultMessage("Owner")
	String colTaskOwner();
	
	@DefaultMessage("Script")
	String colTaskScript();
	
	@DefaultMessage("Parameters")
	String colTaskParameters();
	
	@DefaultMessage("Date")
	String colTaskScheduleDate();
	
	@DefaultMessage("Time")
	String colTaskScheduleTime();
	
	@DefaultMessage("Status")
	String colTaskStatus();
	
	@DefaultMessage("Queued")
	String colTaskQueued();
	
	@DefaultMessage("Started")
	String colTaskStarted();
	
	@DefaultMessage("Finished")
	String colTaskFinished();
	
	@DefaultMessage("Status Message")
	String colTaskStatusMessage();
	
	@DefaultMessage("Output")
	String colTaskOutput();
	
	@DefaultMessage("pending")
	String approvalNotApproved();
	
	@DefaultMessage("expire {0}")
	String approvalExpire(String date);

	@DefaultMessage("not approved")
	String approvalNotApprovedPast();

	@DefaultMessage("new meeting")
	String approvalNewMeeting();
	
	@DefaultMessage("new approval")
	String approvelNewApprovedMeeting();
	
	@DefaultMessage("cancelled")
	String approvalCancelled();
	
	@DefaultMessage("deleted")
	String approvalDeleted();
	
	@DefaultMessage("rejected")
	String approvalRejected();

	@DefaultMessage("approved")
	String approvalApproved();

	@DefaultMessage("new unavailability")
	String approvalNewUnavailabiliyMeeting();
	
	@DefaultMessage("Setup / Teardown Times")
	String dlgChangeOffsets();
	
	@DefaultMessage("Remove Meeting Contacts")
	String dlgRemoveMeetingContacts();
	
	@DefaultMessage("Setup Time:")
	String propSetupTime();

	@DefaultMessage("Teardown Time:")
	String propTeardownTime();
	
	@DefaultMessage("Academic Session:")
	String propAcademicSession();
	
	@DefaultMessage("Event Filter:")
	String propEventFilter();
	
	@DefaultMessage("Room Filter:")
	String propRoomFilter();
	
	@DefaultMessage("Resource Type:")
	String propResourceType();
	
	@DefaultMessage("Resource:")
	String propResource();
	
	@DefaultMessage("Event Name:")
	String propEventName();
	
	@DefaultMessage("Sponsoring Organization:")
	String propSponsor();
	
	@DefaultMessage("Enrollment:")
	String propEnrollment();
	
	@DefaultMessage("Limit:")
	String propLimit();
	
	@DefaultMessage("Student Conflicts:")
	String propStudentConflicts();
	
	@DefaultMessage("Conflicts with:")
	String propConflicts();
	
	@DefaultMessage("Event Type:")
	String propEventType();
	
	@DefaultMessage("Expected Attendance:")
	String propAttendance();
	
	@DefaultMessage("Main Contact:")
	String propMainContact();
	
	@DefaultMessage("First Name:")
	String propFirstName();
	
	@DefaultMessage("Middle Name:")
	String propMiddleName();

	@DefaultMessage("Last Name:")
	String propLastName();
	
	@DefaultMessage("Academic Title:")
	String propAcademicTitle();

	@DefaultMessage("Email:")
	String propEmail();

	@DefaultMessage("Phone:")
	String propPhone();

	@DefaultMessage("Additional Contacts:")
	String propAdditionalContacts();
	
	@DefaultMessage("Contacts")
	String propContacts();
	
	@DefaultMessage("Additional Emails:")
	String propAdditionalEmails();

	@DefaultMessage("Additional Information:")
	String propAdditionalInformation();
	
	@DefaultMessage("Last Change:")
	String propLastChange();
	
	@DefaultMessage("Dates:")
	String propDates();
	
	@DefaultMessage("Date:")
	String propDate();
	
	@DefaultMessage("Times:")
	String propTimes();
	
	@DefaultMessage("Published Time:")
	String propPublishedTime();
	
	@DefaultMessage("Allocated Time:")
	String propAllocatedTime();
	
	@DefaultMessage("Locations:")
	String propLocations();
	
	@DefaultMessage("Location:")
	String propLocation();
	
	@DefaultMessage("Requested By:")
	String propRequestedBy();
	
	@DefaultMessage("After:")
	String propAfter();
	
	@DefaultMessage("Before:")
	String propBefore();
	
	@DefaultMessage("Min:")
	String propMin();

	@DefaultMessage("Max:")
	String propMax();
	
	@DefaultMessage("From:")
	String propFrom();
	
	@DefaultMessage("To:")
	String propTo();
	
	@DefaultMessage("Notes:")
	String propNotes();
	
	@DefaultMessage("Note:")
	String propNote();

	@DefaultMessage("Standard Notes:")
	String propStandardNotes();
	
	@DefaultMessage("Attachment:")
	String propAttachment();
	
	@DefaultMessage("Meetings:")
	String propMeetings();

	@DefaultMessage("Events:")
	String propEvents();
	
	@DefaultMessage("Curriculum:")
	String propCurriculum();
	
	@DefaultMessage("Academic Area:")
	String propAcademicArea();
	
	@DefaultMessage("Multiple Majors:")
	String propMultipleMajors();
	
	@DefaultMessage("Major:")
	String propMajor();
	
	@DefaultMessage("Major(s):")
	String propMajorOrMajors();
	
	@DefaultMessage("Majors:")
	String propMajors();
	
	@DefaultMessage("Minors:")
	String propMinors();
	
	@DefaultMessage("Concentrations:")
	String propConcentrations();
	
	@DefaultMessage("Academic Classification:")
	String propAcademicClassification();
	
	@DefaultMessage("Name:")
	String propName();
	
	@DefaultMessage("Classification:")
	String propClassification();
	
	@DefaultMessage("Classifications:")
	String propClassifications();
	
	@DefaultMessage("Last-Like Enrollment:")
	String propLastLikeEnrollment();
	
	@DefaultMessage("Projected by Rule:")
	String propProjectedByRule();
	
	@DefaultMessage("Requested Enrollment:")
	String propRequestedEnrollment();
	
	@DefaultMessage("Current Enrollment:")
	String propCurrentEnrollment();
	
	@DefaultMessage("Course Registrations:")
	String propCourseRequests();
	
	@DefaultMessage("Owner:")
	String propTaskOwner();
	
	@DefaultMessage("Dates:")
	String propTaskExecutionDates();
	
	@DefaultMessage("Time:")
	String propTaskStartTime();
	
	@DefaultMessage("Snap Proj")
	String abbvSnapshotProjectedByRule();

	@DefaultMessage("Snapshot Projected")
	String shortSnapshotProjectedByRule();

	@DefaultMessage("Snapshot Projected by Rule")
	String fieldSnapshotProjectedByRule();

	@DefaultMessage("Snap Req")
	String abbvSnapshotRequestedEnrollment();

	@DefaultMessage("Snapshot Requested")
	String shortSnapshotRequestedEnrollment();

	@DefaultMessage("Snapshot Requested Enrollment")
	String fieldSnapshotRequestedEnrollment();

	@DefaultMessage("Filter:")
	String propFilter();
	
	@DefaultMessage("Note:")
	String propRoomAvailabilityNote();
	
	@DefaultMessage("Abbreviation:")
	String propAbbreviation();
	
	@DefaultMessage("Department:")
	String propDepartment();
	
	@DefaultMessage("Section:")
	String propSection();
	
	@DefaultMessage("Approved:")
	String propApproved();
	
	@DefaultMessage("Title:")
	String propTitle();
	
	@DefaultMessage("Title:")
	String propCourseTitle();
	
	@DefaultMessage("Instructor:")
	String propInstructor();
	
	@DefaultMessage("New Picture:")
	String propNewPicture();
	
	@DefaultMessage("Applies To:")
	String propAppliesTo();
	
	@DefaultMessage("Room Type:")
	String propRoomType();
	
	@DefaultMessage("Building:")
	String propBuilding();
	
	@DefaultMessage("Name:")
	String propRoomName();
	
	@DefaultMessage("Room Number:")
	String propRoomNumber();
	
	@DefaultMessage("Display Name:")
	String propDisplayName();
	
	@DefaultMessage("Input Data Loaded:")
	String propSolverLoadDate();
	
	@DefaultMessage("Status:")
	String propSolverStatus();
	
	@DefaultMessage("Progress:")
	String propSolverProgress();
	
	@DefaultMessage("Solver Configuration:")
	String propSolverConfiguration();
	
	@DefaultMessage("Owner:")
	String propSolverOwner();
	
	@DefaultMessage("Examination Problem:")
	String propExamSolverOwner();
	
	@DefaultMessage("Host:")
	String propSolverHost();
	
	@DefaultMessage("Message Level:")
	String propSolverLogLevel();
	
	@DefaultMessage("Subject Area:")
	String propSubjectArea();
	
	@DefaultMessage("Sections:")
	String propSections();
	
	@DefaultMessage("Load:")
	String propRequestLoad();
	
	@DefaultMessage("Attribute Preferences:")
	String propAttributePrefs();
	
	@DefaultMessage("{0} Preferences:")
	String propAttributeOfTypePrefs(String type);
	
	@DefaultMessage("Instructor Preferences:")
	String propInstructorPrefs();
	
	@DefaultMessage("Assigned Instructors:")
	String propAssignedInstructors();
	
	@DefaultMessage("Score:")
	String propSuggestionScore();
	
	@DefaultMessage("Objectives:")
	String propSuggestionObjectives();
	
	@DefaultMessage("Objectives:")
	String propObjectives();
	
	@DefaultMessage("Enrollments:")
	String propEnrollments();
	
	@DefaultMessage("Assignments:")
	String propAssignments();
	
	@DefaultMessage("Compare with:")
	String propAssignmentChangesBase();
	
	@DefaultMessage("Number of Instructors:")
	String propNbrInstructors();
	
	@DefaultMessage("Teaching Load:")
	String propTeachingLoad();
	
	@DefaultMessage("Select Scheduling Subpart:")
	String propSetupTeachingRequestMulti();
	
	@DefaultMessage("Scheduling Subpart:")
	String propSchedulingSubpart();
	
	@DefaultMessage("Same Course Preference:")
	String propSameCoursePreference();
	
	@DefaultMessage("Same Common Part:")
	String propSameCommonPreference();
	
	@DefaultMessage("Classes:")
	String propClasses();
	
	@DefaultMessage("Assign Coordinator:")
	String propAssignCoordinator();
	
	@DefaultMessage("Coordinator Percent Share:")
	String propCoordinatorPercentShare();
	
	@DefaultMessage("Include Subparts:")
	String propIncludeSubparts();
	
	@DefaultMessage("Responsibility:")
	String propTeachingResponsibility();
	
	@DefaultMessage("Assigned Instructor:")
	String propAssignedInstructor();
	
	@DefaultMessage("Meeting Contact:")
	String propMeetingContacts();
	
	@DefaultMessage("Requested Services:")
	String propEventRequestedServices();

	@DefaultMessage("Date:")
	String propAssignedDate();
	
	@DefaultMessage("Time:")
	String propAssignedTime();
	
	@DefaultMessage("Room:")
	String propAssignedRooms();
	
	@DefaultMessage("Initial Assignment:")
	String propInitialAssignment();
	
	@DefaultMessage("Violated Constraints:")
	String propViolatedConstraints();
	
	@DefaultMessage("Minimum Room Size:")
	String propMinimumRoomSize();
	
	@DefaultMessage("Available Rooms:")
	String propRoomLocations();
	
	@DefaultMessage("Available Times:")
	String propTimeLocations();
	
	@DefaultMessage("Available Dates:")
	String propDateLocations();
	
	@DefaultMessage("Not-Assigned Classes:")
	String propNotAssignedClasses();
	
	@DefaultMessage("Overall Solution Value:")
	String propOverallSolutionValue();
	
	@DefaultMessage("Mode:")
	String propConflictStatisticsMode();
	
	@DefaultMessage("Limit:")
	String propConflictStatisticsLimit();
	
	@DefaultMessage("Compare with:")
	String propCompareSolutionWith();
	
	@DefaultMessage("Reversed mode (current \u2192 compared solution):")
	String propCompareSolutionReversed();
	
	@DefaultMessage("Configuration:")
	String propSolverConfig();
	
	@DefaultMessage("Load into interactive solver:")
	String propLoadInteractiveSolver();
	
	@DefaultMessage("Time:")
	String propTimeStamp();
	
	@DefaultMessage("Owner:")
	String propOwner();
	
	@DefaultMessage("Configuration:")
	String propSolutionConfig();
	
	@DefaultMessage("<u>O</u>k")
	String buttonOk();

	@DefaultMessage("<u>C</u>ancel")
	String buttonCancel();
	
	@DefaultMessage("<u>C</u>ancel Event")
	String buttonCancelEvent();

	@DefaultMessage("<u>S</u>earch")
	String buttonSearch();
	
	@DefaultMessage("Add Coordinator")
	String buttonAddCoordinator();
	
	@DefaultMessage("<u>C</u>lear")
	String buttonClear();
	
	@DefaultMessage("<u>L</u>ookup")
	String buttonLookup();
	
	@DefaultMessage("<u>A</u>dd&nbsp;Event")
	String buttonAddEvent();
	
	@DefaultMessage("<u>A</u>dd Meetings")
	String buttonAddMeetings();
	
	@DefaultMessage("Prin<u>t</u>")
	String buttonPrint();
	
	@DefaultMessage("E<u>x</u>port")
	String buttonExport();
	
	@DefaultMessage("M<u>o</u>re<span class='unitime-ButtonArrow'>&#9660;</span>")
	String buttonMoreOperations();

	@DefaultMessage("<u>U</u>pdate")
	String buttonUpdate();

	@DefaultMessage("<u>B</u>ack")
	String buttonBack();
	
	@DefaultMessage("<u>C</u>reate Event")
	String buttonCreateEvent();

	@DefaultMessage("<u>U</u>pdate Event")
	String buttonUpdateEvent();

	@DefaultMessage("<u>D</u>elete Event")
	String buttonDeleteEvent();

	@DefaultMessage("<u>E</u>dit Event")
	String buttonEditEvent();

	@DefaultMessage("<u>L</u>ookup")
	String buttonLookupMainContact();
	
	@DefaultMessage("More <u>C</u>ontacts...")
	String buttonLookupAdditionalContact();
	
	@DefaultMessage("Reset")
	String buttonResetMainContact();
	
	@DefaultMessage("Standard&nbsp;<u>N</u>otes...")
	String buttonStandardNotes();
	
	@DefaultMessage("<u>N</u>ext")
	String buttonNext();
	
	@DefaultMessage("<u>P</u>revious")
	String buttonPrevious();
	
	@DefaultMessage("&laquo;")
	String buttonLeft();
	
	@DefaultMessage("&raquo;")
	String buttonRight();
	
	@DefaultMessage("<u>S</u>elect")
	String buttonSelect();
	
	@DefaultMessage("S<u>o</u>rt&nbsp;by<span class='unitime-ButtonArrow'>&#9660;</span>")
	String buttonSortBy();
	
	@DefaultMessage("<u>A</u>dd")
	String buttonAdd();
	
	@DefaultMessage("<u>A</u>dd New")
	String buttonAddNew();
	
	@DefaultMessage("<u>E</u>dit")
	String buttonEdit();
	
	@DefaultMessage("<u>S</u>ave")
	String buttonSave();
	
	@DefaultMessage("<u>D</u>elete")
	String buttonDelete();
	
	@DefaultMessage("<u>C</u>lose")
	String buttonClose();
	
	@DefaultMessage("Curricula&nbsp;<u>O</u>perations<span class='unitime-ButtonArrow'>&#9660;</span>")
	String buttonCurriculaOperations();
	
	@DefaultMessage("E<u>x</u>ecute")
	String buttonExecute();
	
	@DefaultMessage("Export&nbsp;<u>C</u>SV")
	String buttonExportCSV();
	
	@DefaultMessage("Export&nbsp;<u>P</u>DF")
	String buttonExportPDF();
	
	@DefaultMessage("Export&nbsp;<u>X</u>LS")
	String buttonExportXLS();
	
	@DefaultMessage("Add&nbsp;<u>R</u>eservation")
	String buttonAddReservation();
	
	@DefaultMessage("Edit Course Offering New")
	String buttonEditCourseOffering();
	
	@DefaultMessage("Show")
	String buttonShow();
	
	@DefaultMessage("<u>R</u>efresh")
	String buttonRefresh();
	
	@DefaultMessage("<u>E</u>dit Room")
	String buttonEditRoom();
	
	@DefaultMessage("<u>C</u>reate Room")
	String buttonCreateRoom();

	@DefaultMessage("<u>U</u>pdate Room")
	String buttonUpdateRoom();

	@DefaultMessage("<u>D</u>elete Room")
	String buttonDeleteRoom();

	@DefaultMessage("Geocode")
	String buttonGeocode();
	
	@DefaultMessage("<u>A</u>dd New")
	String buttonAddNewRoom();
	
	@DefaultMessage("<u>E</u>dit Room Sharing")
	String buttonEditRoomSharing();
	
	@DefaultMessage("<u>A</u>dd New")
	String buttonAddNewRoomGroup();
	
	@DefaultMessage("<u>A</u>dd New")
	String buttonAddNewRoomFeature();
	
	@DefaultMessage("<u>C</u>reate Room Group")
	String buttonCreateRoomGroup();

	@DefaultMessage("<u>U</u>pdate Room Group")
	String buttonUpdateRoomGroup();

	@DefaultMessage("<u>D</u>elete Room Group")
	String buttonDeleteRoomGroup();
	
	@DefaultMessage("<u>C</u>reate Room Feature")
	String buttonCreateRoomFeature();

	@DefaultMessage("<u>U</u>pdate Room Feature")
	String buttonUpdateRoomFeature();

	@DefaultMessage("<u>D</u>elete Room Feature")
	String buttonDeleteRoomFeature();

	@DefaultMessage("<u>A</u>dd New")
	String buttonAddNewInstructorAttribute();

	@DefaultMessage("<u>C</u>reate Attribute")
	String buttonCreateInstructorAttribute();

	@DefaultMessage("<u>U</u>pdate Attribute")
	String buttonUpdateInstructorAttribute();

	@DefaultMessage("<u>D</u>elete Attribute")
	String buttonDeleteInstructorAttribute();
	
	@DefaultMessage("OK")
	String buttonConfirmOK();
	
	@DefaultMessage("Yes")
	String buttonConfirmYes();

	@DefaultMessage("No")
	String buttonConfirmNo();
	
	@DefaultMessage("Search <u>D</u>eeper")
	String buttonSearchDeeper();
	
	@DefaultMessage("Search <u>L</u>onger")
	String buttonSearchLonger();
	
	@DefaultMessage("Show <u>M</u>ore")
	String buttonMoreAssignments();
	
	@DefaultMessage("<u>A</u>ssign")
	String buttonAssign();
	
	@DefaultMessage("<u>U</u>nassign")
	String buttonUnassign();
	
	@DefaultMessage("<u>A</u>dd Request")
	String buttonAddTeachingRequest();
	
	@DefaultMessage("<u>R</u>emove Request")
	String buttonRemoveTeachingRequest();
	
	@DefaultMessage("<u>U</u>pdate")
	String buttonSaveTeachingRequests();
	
	@DefaultMessage("Setup Teaching Requests")
	String buttonSetupTeachingRequests();
	
	@DefaultMessage("Apply")
	String buttonApply();
	
	@DefaultMessage("<u>A</u>dd New")
	String buttonAddNewTask();
	
	@DefaultMessage("<u>E</u>dit Task")
	String buttonEditTask();
	
	@DefaultMessage("<u>D</u>elete Task")
	String buttonDeleteTask();

	@DefaultMessage("Filter")
	String sectFilter();
	
	@DefaultMessage("Event")
	String sectEvent();
	
	@DefaultMessage("Meetings")
	String sectMeetings();
	
	@DefaultMessage("Courses / Classes")
	String sectRelatedCourses();
	
	@DefaultMessage("Enrollments")
	String sectEnrollments();
	
	@DefaultMessage("Notes")
	String sectNotes();
	
	@DefaultMessage("Relations")
	String sectRelations();
	
	@DefaultMessage("{0} time grid for {1}")
	String sectTimetable(String resource, String session);

	@DefaultMessage("{0} events for {1}")
	String sectEventList(String resource, String session);
	
	@DefaultMessage("{0} meetings for {1}")
	String sectMeetingList(String resource, String session);
	
	@DefaultMessage("Results")
	String sectResults();
	
	@DefaultMessage("Reservation Details")
	String sectReservationDetails();
	
	@DefaultMessage("Reservations")
	String sectReservations();
	
	@DefaultMessage("Travel time in minutes")
	String sectTravelTimesInMintes();
	
	@DefaultMessage("Script")
	String sectScript();
	
	@DefaultMessage("Scripts in progress")
	String sectScriptQueue();
	
	@DefaultMessage("Log of {0}")
	String sectScriptLog(String name);
	
	@DefaultMessage("Current Timetable")
	String sectSolverCurrentSolution();
	
	@DefaultMessage("Loaded Timetable")
	String sectListSolutionsCurrentSolution();
	
	@DefaultMessage("Current Student Schedule")
	String sectStudentSolverCurrentSolution();
	
	@DefaultMessage("Best Timetable")
	String sectSolverBestSolution();
	
	@DefaultMessage("Best Student Schedule Found So Far")
	String sectStudentSolverBestSolution();
	
	@DefaultMessage("Selected Timetable - {0}")
	String sectSolverSelectedSolution(String name);
	
	@DefaultMessage("Problems - {0}")
	String sectSolverSelectedWarnings(String name);
	
	@DefaultMessage("Problems")
	String sectSolverWarnings();
	
	@DefaultMessage("Solver Log")
	String sectSolverLog();
	
	@DefaultMessage("{0}. Teaching Request")
	String sectTeachingRequest(int index);
	
	@DefaultMessage("Teaching Requests")
	String sectTeachingRequests();
	
	@DefaultMessage("Teaching Assignments")
	String sectTeachingAssignments();
	
	@DefaultMessage("Timetables")
	String sectTimetables();
	
	@DefaultMessage("Assigned Classes")
	String sectAssignedClasses();
	
	@DefaultMessage("Not-Assigned Classes")
	String sectNotAssignedClasses();
	
	@DefaultMessage("Legend")
	String sectLegend();
	
	@DefaultMessage("Changes")
	String sectSolutionChanges();
	
	@DefaultMessage("History")
	String sectAssignmentHistory();
	
	@DefaultMessage("Saved Timetables")
	String sectSavedSolutions();
	
	@DefaultMessage("Scheduled Tasks for {0}")
	String sectScheduledTasks(String session);
	
	@DefaultMessage("Scheduled Executions")
	String sectTaskExecutions();
	
	@DefaultMessage("Loading {0}...")
	String waitLoading(String name);
	
	@DefaultMessage("Creating {0}...")
	String waitCreate(String name);
	
	@DefaultMessage("Updating {0}...")
	String waitUpdate(String name);

	@DefaultMessage("Deleting {0}...")
	String waitDelete(String name);

	@DefaultMessage("Cancelling {0}...")
	String waitCancel(String name);

	@DefaultMessage("Loading data for {0} ...")
	String waitLoadingDataForSession(String session);
	
	@DefaultMessage("Loading {0} schedule for {1} ...")
	String waitLoadingTimetable(String name, String session);
	
	@DefaultMessage("Checking room availability...")
	String waitCheckingRoomAvailability();
	
	@DefaultMessage("Loading academic sessions ...")
	String waitLoadingSessions();
	
	@DefaultMessage("Approving meetings of {0} ...")
	String waitForApproval(String name);
	
	@DefaultMessage("Rejecting meetings of {0} ...")
	String waitForRejection(String name);
	
	@DefaultMessage("Cancelling meetings of {0} ...")
	String waitForCancellation(String name);

	@DefaultMessage("Inquiring about {0} ...")
	String waitForInquiry(String name);
	
	@DefaultMessage("Loading room availability...")
	String waitLoadingRoomAvailability();
	
	@DefaultMessage("Saving data...")
	String waitSavingData();
	
	@DefaultMessage("Saving order...")
	String waitSavingOrder();

	@DefaultMessage("Saving record...")
	String waitSavingRecord();

	@DefaultMessage("Deleting record...")
	String waitDeletingRecord();
	
	@DefaultMessage("Loading data ...")
	String waitLoadingData();
	
	@DefaultMessage("Loading record ...")
	String waitLoadingRecord();
	
	@DefaultMessage("Saving curricula ...")
	String waitSavingCurricula();
	
	@DefaultMessage("Saving curriculum {0} ...")
	String waitSavingCurriculum(String name);
	
	@DefaultMessage("Deleting curriculum {0} ...")
	String waitDeletingCurriculum(String name);
	
	@DefaultMessage("Loading curricula ...")
	String waitLoadingCurricula();
	
	@DefaultMessage("Loading curriculum ...")
	String waitLoadingCurriculum();
	
	@DefaultMessage("Loading curriculum {0} ...")
	String waitLoadingCurriculumWithName(String name);
	
	@DefaultMessage("Populating projected demands for this offering ...")
	String waitPopulateCourseProjectedDemands();
	
	@DefaultMessage("Loading details for {0} ...")
	String waitLoadingDetailsOf(String name);
	
	@DefaultMessage("Deleting selected curricula ...")
	String waitDeletingSelectedCurricula();
	
	@DefaultMessage("Merging selected curricula ...")
	String waitMergingSelectedCurricula();
	
	@DefaultMessage("Updating curricula ... &nbsp;&nbsp;&nbsp;&nbsp;This could take a while ...")
	String waitUpdatingCurricula();
	
	@DefaultMessage("Populating projected demands for all courses ...")
	String waitPopulatingProjectedDemands();
	
	@DefaultMessage("Creating all curricula ... &nbsp;&nbsp;&nbsp;&nbsp;You may also go grab a coffee ... &nbsp;&nbsp;&nbsp;&nbsp;This will take a while ...")
	String waitCreatingAllCurricula();
	
	@DefaultMessage("Loading course enrollments ...")
	String waitLoadingCourseEnrollments();
	
	@DefaultMessage("Saving curriculum projection rules ...")
	String waitSavingCurriculumProjectionRules();
	
	@DefaultMessage("Loading curriculum projection rules ...")
	String waitLoadingCurriculumProjectionRules();
	
	@DefaultMessage("Loading reports ...")
	String waitLoadingReports();
	
	@DefaultMessage("Testing query ...")
	String waitTestingQuery();
	
	@DefaultMessage("Executing {0} ...")
	String waitExecuting(String operation);
	
	@DefaultMessage("Saving reservation...")
	String waitSavingReservation();
	
	@DefaultMessage("Deleting reservation...")
	String waitDeletingReservation();
		
	@DefaultMessage("Loading reservation...")
	String waitLoadingReservation();

	@DefaultMessage("Loading reservations...")
	String waitLoadingReservations();
	
	@DefaultMessage("Loading travel times...")
	String waitLoadingTravelTimes();
	
	@DefaultMessage("Loading page ...")
	String waitLoadingPage();
	
	@DefaultMessage("Loading room pictures...")
	String waitLoadingRoomPictures();
	
	@DefaultMessage("Loading rooms...")
	String waitLoadingRooms();
	
	@DefaultMessage("Loading room details...")
	String waitLoadingRoomDetails();
	
	@DefaultMessage("Storing snapshot...")
	String waitStoringSnapshot();

	@DefaultMessage("Saving room ...")
	String waitSavingRoom();
	
	@DefaultMessage("Updating room ...")
	String waitUpdatingRoom();
	
	@DefaultMessage("Deleting room ...")
	String waitDeletingRoom();
	
	@DefaultMessage("Updating room departments ...")
	String waitUpdatingRoomDepartments();

	@DefaultMessage("Loading room groups...")
	String waitLoadingRoomGroups();
	
	@DefaultMessage("Saving room group ...")
	String waitSavingRoomGroup();
	
	@DefaultMessage("Updating room group ...")
	String waitUpdatingRoomGroup();
	
	@DefaultMessage("Deleting room group ...")
	String waitDeletingRoomGroup();
	
	@DefaultMessage("Loading room features...")
	String waitLoadingRoomFeatures();
	
	@DefaultMessage("Saving room feature ...")
	String waitSavingRoomFeature();
	
	@DefaultMessage("Updating room feature ...")
	String waitUpdatingRoomFeature();
	
	@DefaultMessage("Deleting room feature ...")
	String waitDeletingRoomFeature();
	
	@DefaultMessage("Loading instructor attributes...")
	String waitLoadingInstructorAttributes();

	@DefaultMessage("Saving instructor attribute ...")
	String waitSavingInstructorAttribute();
	
	@DefaultMessage("Updating instructor attribute ...")
	String waitUpdatingInstructorAttribute();
	
	@DefaultMessage("Deleting instructor attribute ...")
	String waitDeletingInstructorAttribute();
	
	@DefaultMessage("Loading instructors...")
	String waitLoadingInstructors();
	
	@DefaultMessage("Please wait ...")
	String waitSolverExecution();
	
	@DefaultMessage("Loading teaching requests ...")
	String waitLoadingTeachingRequests();
	
	@DefaultMessage("Loading teaching assignments ...")
	String waitLoadingTeachingAssignments();
	
	@DefaultMessage("Loading teaching request detail ...")
	String waitLoadTeachingRequestDetail();
	
	@DefaultMessage("Loading class details ...")
	String waitLoadClassDetails();
	
	@DefaultMessage("Saving teaching requests for {0} ...")
	String waitSaveTeachingRequests(String offering);

	@DefaultMessage("Failed to load {0}: {1}")
	String failedLoad(String name, String reason);
	
	@DefaultMessage("Failed to load details of {0}: {1}")
	String failedLoadDetails(String name, String reason);
	
	@DefaultMessage("Failed to create {0}: {1}")
	String failedCreate(String name, String reason);
	
	@DefaultMessage("Failed to update {0}: {1}")
	String failedUpdate(String name, String reason);

	@DefaultMessage("Failed to delete {0}: {1}")
	String failedDelete(String name, String reason);

	@DefaultMessage("Failed to cancel {0}: {1}")
	String failedCancel(String name, String reason);

	@DefaultMessage("Failed to load academic sessions: {0}")
	String failedLoadSessions(String reason);

	@DefaultMessage("No events matching the given criteria were found.")
	String failedNoEvents();
	
	@DefaultMessage("Failed to load enrollments: {0}.")
	String failedNoEnrollments(String message);
	
	@DefaultMessage("Validation failed: {0}")
	String failedValidation(String reason);
	
	@DefaultMessage("Validation failed, see errors below.")
	String failedValidationSeeBelow();
	
	@DefaultMessage("Validation failed, please check the form for warnings.")
	String failedValidationCheckForm();
	
	@DefaultMessage("Room availability failed: {0}")
	String failedRoomAvailability(String reason);

	@DefaultMessage("Add meetings failed: {0}")
	String failedAddMeetings(String reason);
	
	@DefaultMessage("Failed to modify meetings: {0}")
	String failedChangeMeetings(String reason);

	@DefaultMessage("{0} cannot be created through the event interface.")
	String failedSaveEventWrongType(String eventType);
	
	@DefaultMessage("Meeting {0} has no location.")
	String failedSaveEventNoLocation(String meeting);
	
	@DefaultMessage("{0} is not managed in UniTime or disabled for events at the moment.")
	String failedSaveEventWrongLocation(String location);
	
	@DefaultMessage("Requested meeting date {0} is in the past or outside of the academic session.")
	String failedSaveEventPastOrOutside(String meetingDate);
	
	@DefaultMessage("Meeting {0} is conflicting with {1}.")
	String failedSaveEventConflict(String meeting, String conflict);
	
	@DefaultMessage("Meeting {0} cannot be edited by the user.")
	String failedSaveEventCanNotEditMeeting(String meeting);
	
	@DefaultMessage("Meeting {0} cannot be deleted by the user.")
	String failedSaveEventCanNotDeleteMeeting(String meeting);

	@DefaultMessage("Meeting {0} cannot be cancelled by the user.")
	String failedSaveEventCanNotCancelMeeting(String meeting);
	
	@DefaultMessage("Not enough permissions to make {0} not available.")
	String failedSaveCannotMakeUnavailable(String location);

	@DefaultMessage("The event does no longer exist.")
	String failedApproveEventNoEvent();

	@DefaultMessage("No meetings were selected.")
	String failedApproveEventNoMeetings();

	@DefaultMessage("Insufficient rights to approve meeting {0}.")
	String failedApproveEventNoRightsToApprove(String meeting);
	
	@DefaultMessage("Insufficient rights to reject meeting {0}.")
	String failedApproveEventNoRightsToReject(String meeting);
	
	@DefaultMessage("Insufficient rights to cancel meeting {0}.")
	String failedApproveEventNoRightsToCancel(String meeting);

	@DefaultMessage("Failed to hide academic session info: {0}")
	String failedToHideSessionInfo(String reason);
	
	@DefaultMessage("Failed to load room availability: {0}")
	String failedToLoadRoomAvailability(String reason);

	@DefaultMessage("Failed to update room availability: {0}")
	String failedToSaveRoomAvailability(String reason);
	
	@DefaultMessage("Save failed: {0}")
	String failedSave(String reason);
		
	@DefaultMessage("Failed to load data: {0}")
	String failedLoadData(String reason);
	
	@DefaultMessage("Edit type not recognized: {0}")
	String failedWrongEditType(String reason);
	
	@DefaultMessage("Attempted to delete an examination type {0} that is being used.")
	String failedDeleteUsedExaminationType(String reference);
	
	@DefaultMessage("Role {0} cannot be deleted.")
	String failedDeleteRole(String role);
	
	@DefaultMessage("Failed to save curricula: {0}")
	String failedToSaveCurricula(String reason);
	
	@DefaultMessage("Unable to populate course projected demands: {0}")
	String failedPopulateCourseProjectedDemands(String reason);
	
	@DefaultMessage("Failed to load academic areas: {0}")
	String failedToLoadAcademicAreas(String reason);
	
	@DefaultMessage("Failed to load departments: {0}")
	String failedToLoadDepartments(String reason);
	
	@DefaultMessage("Failed to load classifications: {0}")
	String failedToLoadClassifications(String reason);
	
	@DefaultMessage("Failed to load curricula: {0}")
	String failedToLoadCurricula(String reason);
	
	@DefaultMessage("Failed to delete selected curricula: {0}")
	String failedToDeleteSelectedCurricula(String reason);
	
	@DefaultMessage("Failed to merge selected curricula: {0}")
	String failedToMergeSelectedCurricula(String reason);
	
	@DefaultMessage("Failed to update curricula: {0}")
	String failedToUpdateCurricula(String reason);

	@DefaultMessage("Failed to populate course projected demands: {0}")
	String failedToPopulateProjectedDemands(String reason);
	
	@DefaultMessage("Failed to create curricula: {0}")
	String failedToCreateCurricula(String reason);
	
	@DefaultMessage("Failed to open curriculum projection rules: {0}")
	String failedToOpenCurriculumProjectionRules(String reason);
	
	@DefaultMessage("Failed to save curriculum projection rules: {0}")
	String failedToSaveCurriculumProjectionRules(String reason);
	
	@DefaultMessage("Failed to load curriculum projection rules: {0}")
	String failedToLoadCurriculumProjectionRules(String reason);
	
	@DefaultMessage("Test failed: {0}")
	String failedTest(String reason);
	
	@DefaultMessage("Test failed.")
	String failedTestNoReason();
	
	@DefaultMessage("Execution failed: {0}")
	String failedExecution(String reason);
	
	@DefaultMessage("Failed to send email: {0}")
	String failedEmail(String reason);
	
	@DefaultMessage("Failed to load reservations: {0}")
	String failedToLoadReservations(String reason);
	
	@DefaultMessage("Failed to save the matrix: {0}")
	String failedToSaveMatrix(String reason);
	
	@DefaultMessage("Failed to load the matrix: {0}")
	String failedToLoadMatrix(String reason);
	
	@DefaultMessage("Failed to load the matrix: there are no rooms.")
	String failedToLoadMatrixNoRooms();
	
	@DefaultMessage("Failed to load rooms: {0}")
	String failedToLoadRooms(String reason);
	
	@DefaultMessage("Failed to load room details: {0}")
	String failedToLoadRoomDetails(String reason);
	
	@DefaultMessage("Uncaught exception: {0}")
	String failedUncaughtException(String reason);
	
	@DefaultMessage("Failed to load the page: {0}")
	String failedToLoadPage(String reason);
	
	@DefaultMessage("Failed to load the page: page not provided.")
	String failedToLoadPageNotProvided();
	
	@DefaultMessage("Failed to load the page: page {0} not registered.")
	String failedToLoadPageNotRegistered(String page);
	
	@DefaultMessage("Failed to lookup a person: {0}")
	String failedLookup(String reason);
	
	@DefaultMessage("Failed to save as default: {0}")
	String failedSaveAsDefault(String reason);
	
	@DefaultMessage("Failed to load room pictures: {0}")
	String failedToLoadRoomPictures(String reason);
	
	@DefaultMessage("Failed to update room pictures: {0}")
	String failedToSaveRoomPictures(String reason);
	
	@DefaultMessage("Failed to upload room picture: {0}")
	String failedToUploadRoomPicture(String reason);
	
	@DefaultMessage("Failed to load period preferences: {0}")
	String failedToLoadPeriodPreferences(String reason);
	
	@DefaultMessage("Failed to initialize: {0}")
	String failedToInitialize(String reason);
	
	@DefaultMessage("Failed to create {0} in {1}: {2}")
	String failedCreateLocation(String name, String session, String reason);
	
	@DefaultMessage("Failed to update {0} in {1}: {2}")
	String failedUpdateLocation(String name, String session, String reason);

	@DefaultMessage("Failed to delete {0} in {1}: {2}")
	String failedDeleteLocation(String name, String session, String reason);
	
	@DefaultMessage("Failed to load room groups: {0}")
	String failedToLoadRoomGroups(String reason);
	
	@DefaultMessage("Failed to load room features: {0}")
	String failedToLoadRoomFeatures(String reason);
	
	@DefaultMessage("Failed to load instructor attributes: {0}")
	String failedToLoadInstructorAttributes(String reason);
	
	@DefaultMessage("Failed to load teaching requests: {0}")
	String failedToLoadTeachingRequests(String reason);
	
	@DefaultMessage("Failed to load teaching assignments: {0}")
	String failedToLoadTeachingAssignments(String reason);
	
	@DefaultMessage("Failed to load teaching request: {0}")
	String failedToLoadTeachingRequestDetail(String reason);
	
	@DefaultMessage("Failed to compute suggestions: {0}")
	String failedToComputeSuggestions(String reason);
	
	@DefaultMessage("Selected placement not valid: {0}")
	String failedToComputeSelectedAssignment(String reason);
	
	@DefaultMessage("Failed to load timetables: {0}")
	String failedToLoadTimetableGrid(String reason);
	
	@DefaultMessage("Failed to load assigned classes: {0}")
	String failedToLoadAssignedClasses(String reason);
	
	@DefaultMessage("Failed to load unassigned classes: {0}")
	String failedToLoadNotAssignedClasses(String reason);

	@DefaultMessage("Failed to store snapshot: {0}")
	String failedToStoreSnapshot(String reason);
	
	@DefaultMessage("Failed to load class details: {0}.")
	String failedToLoadClassDetails(String message);
	
	@DefaultMessage("Failed to assing: {0}")
	String failedToAssign(String reason);
	
	@DefaultMessage("Failed to compute conflicts: {0}")
	String failedToComputeConflicts(String reason);
	
	@DefaultMessage("Failed to load conflict-based statistics: {0}")
	String failedToLoadConflictStatistics(String reason);
	
	@DefaultMessage("Failed to load solution changes: {0}")
	String failedToLoadSolutionChanges(String reason);
	
	@DefaultMessage("Failed to load assignment history: {0}")
	String failedToLoadAssignmentHistory(String reason);
	
	@DefaultMessage("ERROR: {0}")
	String failedToComputeReport(String reason);

	@DefaultMessage("There are more than {0} meetings matching the filter. Only {0} meetings are loaded.")
	String warnTooManyMeetings(int maximum);
	
	@DefaultMessage("No academic session selected.")
	String warnNoSession();
	
	@DefaultMessage("It is not allowed to create a new event in {0}.")
	String warnCannotAddEvent(String session);

	@DefaultMessage("No resource type selected.")
	String warnNoResourceType();

	@DefaultMessage("Please, enter a {0}.")
	String warnNoResourceName(String resourceName);

	@DefaultMessage("Waiting for the academic session {0} to load...")
	String warnNoEventProperties(String session);
	
	@DefaultMessage("Waiting for the room filter to load...")
	String warnRoomFilterNotInitialized();
	
	@DefaultMessage("Waiting for the event filter to load...")
	String warnEventFilterNotInitialized();
	
	@DefaultMessage("Meeting {0} overlaps with an existing meeting {1}.")
	String warnNewMeetingOverlaps(String m1, String m2);
	
	@DefaultMessage("The requested time {0} is unusual, please look at it closely.")
	String warnMeetingTooEarly(String time);
	
	@DefaultMessage("{0} has no external id.")
	String warnInstructorHasNoExternalId(String name);
	
	@DefaultMessage("Selected meetings have not meeting contacts.")
	String warnSelectedMeetingsHaveNoMeetingContacts();

	@DefaultMessage("No date is selected.")
	String errorNoDateSelected();
	
	@DefaultMessage("No start time is entered.")
	String errorNoStartTime();
	
	@DefaultMessage("No end time is entered.")
	String errorNoEndTime();
	
	@DefaultMessage("No rooms are matching the filter.")
	String errorNoMatchingRooms();
	
	@DefaultMessage("Wrong event id provided.")
	String errorBadEventId();
	
	@DefaultMessage("Edit type is not provided.")
	String errorNoEditType();
	
	@DefaultMessage("{0} must be set.")
	String errorMustBeSet(String field);
	
	@DefaultMessage("{0} must be unique.")
	String errorMustBeUnique(String field);

	@DefaultMessage("{0} is not a valid date.")
	String errorNotValidDate(String value);
	
	@DefaultMessage("{0} is too long.")
	String errorTooLong(String field);
	
	@DefaultMessage("Operation not supported.")
	String errorOperationNotSupported();
	
	@DefaultMessage("Duplicate course {0}")
	String errorDuplicateCourse(String course);
	
	@DefaultMessage("{0} cannot form a cycle.")
	String errorCanNotCycle(String field);
	
	@DefaultMessage("No data.")
	String errorNoData();
	
	@DefaultMessage("No results.")
	String errorNoResults();

	@DefaultMessage("No curricula matching the above filter found.")
	String errorNoMatchingCurriculaFound();
	
	@DefaultMessage("Next curriculum not provided.")
	String errorNoNextCurriculum();
	
	@DefaultMessage("Previous curriculum not provided.")
	String errorNoPreviousCurriculum();
	
	@DefaultMessage("No academic areas defined.")
	String errorNoAcademicAreasDefined();
	
	@DefaultMessage("No academic classifications defined.")
	String errorNoAcademicClassificationsDefined();
	
	@DefaultMessage("No last-like enrollments.")
	String errorNoLastLikeEnrollemnts();
	
	@DefaultMessage("Curriculum {0} does not exist anymore, please refresh your data.")
	String errorCurriculumDoesNotExist(String id);
	
	@DefaultMessage("Course {0} does not exist.")
	String errorCourseDoesNotExist(String name);
	
	@DefaultMessage("Offering {0} does not exist.")
	String errorOfferingDoesNotExist(String name);
	
	@DefaultMessage("Unsaved curriculum cannot be deleted.")
	String errorCannotDeleteUnsavedCurriculum();
	
	@DefaultMessage("Unsaved curriculum cannot be merged.")
	String errorCannotMergeUnsavedCurriculum();
	
	@DefaultMessage("Selected curricula have different academic areas.")
	String errorCannotMergeDifferentAcademicAreas();
	
	@DefaultMessage("Selected curricula have different departments.")
	String errorCannotMergeDifferentDepartments();
	
	@DefaultMessage("Course detail interface not provided.")
	String errorCourseDetailsInterfaceNotProvided();
	
	@DefaultMessage("No report is selected.")
	String errorNoReportSelected();
	
	@DefaultMessage("No reports are avaialable.")
	String errorNoReportsAvailable();
	
	@DefaultMessage("No report provided.")
	String errorNoReportProvided();
	
	@DefaultMessage("{0} not selected")
	String errorItemNotSelected(String item);
	
	@DefaultMessage("At least one appearance must be selected.")
	String errorNoAppearanceSelected();
	
	@DefaultMessage("Name is required.")
	String errorNameIsRequired();
	
	@DefaultMessage("Query is required.")
	String errorQueryIsRequired();
	
	@DefaultMessage("Unable to set parameter {0}: no available values.")
	String errorUnableToSetParameterNoValues(String parameter);
	
	@DefaultMessage("Reservation or instructional offering id not provided.")
	String errorReservationOrOfferingIdNotProvided();
	
	@DefaultMessage("Cannot delete unsaved reservation.")
	String errorCannotDeleteUnsavedReservation();
	
	@DefaultMessage("No reservation matching the above filter found.")
	String errorNoMatchingReservation();
	
	@DefaultMessage("Unknown reservation type {0}.")
	String errorUnknownReservationType(String tpe);
	
	@DefaultMessage("There are no rooms are matching the filter.")
	String errorNoRoomsMatchingFilter();
	
	@DefaultMessage("There is only one room matching the filter.")
	String errorOnlyOneRoomIsMatchingFilter();
	
	@DefaultMessage("No person matching the query {0} was found.")
	String errorNoPersonMatchingQuery(String query);
	
	@DefaultMessage("Engine is required.")
	String errorEngineIsRequired();
	
	@DefaultMessage("Script is required.")
	String errorScriptIsRequired();
	
	@DefaultMessage("Name is not unique.")
	String errorNameNotUnique();
	
	@DefaultMessage("Parameter name {0} is not unique.")
	String errorParameterNameNotUnique(String name);
	
	@DefaultMessage("Parameter {0} has no type.")
	String errorParameterTypeRequired(String name);
	
	@DefaultMessage("Expiration date is in the past.")
	String errorExpirationDateInPast();
	
	@DefaultMessage("Room {0} does not exist.")
	String errorRoomDoesNotExist(String name);
	
	@DefaultMessage("Room type must be selected.")
	String errorRoomTypeMustBeSelected();
	
	@DefaultMessage("Building must be selected for room type {0}.")
	String errorBuildingMustBeSelected(String roomType);
	
	@DefaultMessage("Room number is required.")
	String errorRoomNumberIsEmpty();
	
	@DefaultMessage("Location name is required.")
	String errorLocationNameIsEmpty();
	
	@DefaultMessage("Room capacity is required.")
	String errorRoomCapacityIsEmpty();
	
	@DefaultMessage("Room examination capacity is required.")
	String errorRoomExamCapacityIsEmpty();
	
	@DefaultMessage("Event note is too long.")
	String errorEventNoteTooLong();
	
	@DefaultMessage("At least one department must be selected.")
	String errorRoomHasNoDepartment();
	
	@DefaultMessage("Controlling department must be included in the room sharing.")
	String errorControllingDepartmentNotAmongRoomSharing();
	
	@DefaultMessage("Failed to delete the room: {0}")
	String errorFailedToDeleteRoom(String message);
	
	@DefaultMessage("Failed to save the room: {0}")
	String errorFailedToSaveRoom(String message);
	
	@DefaultMessage("Failed to update the room: {0}")
	String errorFailedToUpdateRoom(String message);
	
	@DefaultMessage("Failed to update room departments: {0}")
	String errorFailedToUpdateRoomDepartments(String message);
	
	@DefaultMessage("Failed to delete the room group: {0}")
	String errorFailedToDeleteRoomGroup(String message);
	
	@DefaultMessage("Failed to save the room group: {0}")
	String errorFailedToSaveRoomGroup(String message);
	
	@DefaultMessage("Failed to update the room group: {0}")
	String errorFailedToUpdateRoomGroup(String message);
	
	@DefaultMessage("Failed to delete the room feature: {0}")
	String errorFailedToDeleteRoomFeature(String message);
	
	@DefaultMessage("Failed to save the room feature: {0}")
	String errorFailedToSaveRoomFeature(String message);
	
	@DefaultMessage("Failed to update the room feature: {0}")
	String errorFailedToUpdateRoomFeature(String message);

	@DefaultMessage("Failed to delete the instructor attribute: {0}")
	String errorFailedToDeleteInstructorAttribute(String message);

	@DefaultMessage("Failed to save the instructor attribute: {0}")
	String errorFailedToSaveInstructorAttribute(String message);
	
	@DefaultMessage("Failed to update the instructor attribute: {0}")
	String errorFailedToUpdateInstructorAttribute(String message);

	@DefaultMessage("Building {0} does not exist.")
	String errorBuildingNotExist(String bldgAbbv);
	
	@DefaultMessage("Room {0} already exists.")
	String errorRoomAlreadyExists(String roomName);
	
	@DefaultMessage("Location name {0} does not meet the required pattern {1}.")
	String errorLocationNameDoesNotMeetRequiredPattern(String locationName, String regExp);
	
	@DefaultMessage("Location name {0} does not meet the required pattern {1}: {2}.")
	String errorLocationNameDoesNotMeetRequiredPatternWithReason(String locationName, String regExp, String reason);
	
	@DefaultMessage("There are no room groups created.")
	String errorNoRoomGroups();
	
	@DefaultMessage("There are no room features created.")
	String errorNoRoomFeatures();
	
	@DefaultMessage("There are no instructor attributes created.")
	String errorNoInstructorAttributes();

	@DefaultMessage("Name is required.")
	String errorNameIsEmpty();

	@DefaultMessage("<b>{0}</b> can not be greater than <b>{1}</b> characters.")
	String errorMaxlength(String name, String length);

	@DefaultMessage("<b>{0}</b>")
	String errorGeneric(String msg);

	@DefaultMessage("<b>{0}</b> is required")
	String errorRequired(String name);
	
	@DefaultMessage("Title is required.")
	String errorTitleIsEmpty();
	
	@DefaultMessage("Course Number is required.")
	String errorCourseNumberIsEmpty();
	
	@DefaultMessage("Course Number cannot be matched to regular expression: {0} . Reason: {1}")
	String errorCourseNumberCannotBeMatched(String regularExpression, String reason);
	
	@DefaultMessage("Abbreviation is required.")
	String errorAbbreviationIsEmpty();
	
	@DefaultMessage("Abbreviation must be unique.")
	String errorAbbreviationMustBeUnique();

	@DefaultMessage("Department Code is required.")
	String errorDeptCodeIsEmpty();
	
	@DefaultMessage("Department Code must be unique.")
	String errorDeptCodeMustBeUnique();
	
	@DefaultMessage("Attribute type must be selected.")
	String errorNoAttributeTypeSelected();
	
	@DefaultMessage("Department must be selected.")
	String errorNoDepartmentSelected();
	
	@DefaultMessage("Description is too long.")
	String errorDescriptionTooLong();
	
	@DefaultMessage("Room group {0} does not exist.")
	String errorRoomGroupDoesNotExist(Long id);
	
	@DefaultMessage("Room group {0} already exists in {1}.")
	String errorRoomGroupAlreadyExists(String name, String session);
	
	@DefaultMessage("Room feature {0} does not exist.")
	String errorRoomFeatureDoesNotExist(Long id);
	
	@DefaultMessage("Room feature {0} already exists in {1}.")
	String errorRoomFeatureAlreadyExists(String name, String session);
	
	@DefaultMessage("Instructor attribute {0} does not exist.")
	String errorInstructorAttributeDoesNotExist(Long id);
	
	@DefaultMessage("Instructor attribute {0} already exists in {1}.")
	String errorInstructorAttributeAlreadyExists(String name, String session);
	
	@DefaultMessage("At least one owner must be selected.")
	String errorSolverNoOwnerSelected();
	
	@DefaultMessage("Invalid solver type {0}")
	String errorSolverInvalidType(String type);
	
	@DefaultMessage("Neither a solver is started nor solution is selected.")
	String errorTimetableGridNoSolution();
	
	@DefaultMessage("Neither a solver is started nor solution is selected.")
	String errorAssignedClassesNoSolution();
	
	@DefaultMessage("Neither a solver is started nor solution is selected.")
	String errorNotAssignedClassesNoSolution();
	
	@DefaultMessage("No resource matches the above criteria (or there is no resource at all).")
	String errorTimetableGridNoDataReturned();
	
	@DefaultMessage("No assigned class.")
	String errorAssignedClassesNoDataReturned();
	
	@DefaultMessage("All classes are assigned.")
	String errorNotAssignedClassesNoDataReturned();
	
	@DefaultMessage("Conflict-based statistics is not available at the moment.")
	String errorConflictStatisticsNoDataReturned();
	
	@DefaultMessage("No timetable is loaded. However, you can load one <a href='gwt.jsp?page=listSolutions'>here</a>.")
	String errorNoSolverLoaded();
	
	@DefaultMessage("No best solution saved so far.")
	String errorNoBestSolutionSaved();
	
	@DefaultMessage("No solution selected. However, you can select one <a href='gwt.jsp?page=listSolutions'>here")
	String errorNoSolutionSelected();
	
	@DefaultMessage("No solution committed so far.")
	String errorListSolutionsNoCommitted();
	
	@DefaultMessage("No solution saved so far.")
	String errorListSolutionsNoSaved();
	
	@DefaultMessage("No changes.")
	String errorSolutionChangesNoDataReturned();
	
	@DefaultMessage("No history.")
	String errorAssignmentHistoryNoDataReturned();
	
	@DefaultMessage("No solutions.")
	String errorListSolutionsNoDataReturned();
	
	@DefaultMessage("No solution selected.")
	String errorListSolutionsNoSolutionSelected();
	
	@DefaultMessage("No solver group selected.")
	String errorListSolutionsNoOwnerSelected();
	
	@DefaultMessage("Class {0} does not exist anymore, please refresh your data.")
	String errorClassDoesNotExist(Long id);
	
	@DefaultMessage("Solution {0} does not exist.")
	String errorSolutionDoesNotExist(String id);
		
	@DefaultMessage("Class {0} {1} overlaps with {2} {3} (room {4}).")
	String failedCommitRoomConflict(String class1, String time1, String class2, String time2, String room);
	
	@DefaultMessage("Class {0} {1} overlaps with {2} {3} (instructor {4}).")
	String failedCommitInstructorConflict(String class1, String time1, String class2, String time2, String room);
	
	@DefaultMessage("Solver is not started.")
	String warnSolverNotStarted();
	
	@DefaultMessage("Neither a solver is started nor solution is selected.")
	String warnSolverNotStartedSolutionNotSelected();
	
	@DefaultMessage("Solver is working, stop it first.")
	String warnSolverIsWorking();
	
	@DefaultMessage("Room availability is not available for {0} examinations.")
	String warnExamSolverNoRoomAvailability(String examType); 
	
	@DefaultMessage("Room availability for {0} examination solver was updated on {1}.")
	String infoExamSolverRoomAvailabilityLastUpdated(String examType, String timeStamp);

	@DefaultMessage("Room availability is not available for classes.")
	String warnCourseSolverNoRoomAvailability(); 
	
	@DefaultMessage("Room availability for course timetabling solver was updated on {0}.")
	String infoCourseSolverRoomAvailabilityLastUpdated(String timeStamp);
	
	@DefaultMessage("Showing an in-memory solution for {0} Examinations.")
	String infoExamSolverShowingSolution(String examType);
	
	@DefaultMessage("Showing an in-memory solution for {0}.")
	String infoSolverShowingSolution(String owners);
	
	@DefaultMessage("Showing a selected solution for {0}.")
	String infoSolverShowingSelectedSolution(String owner);

	@DefaultMessage("Showing selected solutions for {0}.")
	String infoSolverShowingSelectedSolutions(String owners);
	
	@DefaultMessage("Showing published schedule run from {0}.")
	String infoSolverShowingPublishedSectioningSolution(String timeStamp);
	
	@DefaultMessage("There is no {0} solution committed, {1} classes are not considered.")
	String warnSolverNoCommittedSolutionExternal(String owner, String ext);
	
	@DefaultMessage("There is no {0} solution committed, {1} departmental classes are not considered.")
	String warnSolverNoCommittedSolutionDepartmental(String owner, String subjects);
	
	@DefaultMessage("Solver is not loaded in memory.")
	String warnSolverNotLoaded();

	@DefaultMessage("Success (no row returned)")
	String infoTestSucceededNoResults();
	
	@DefaultMessage("Success ({0} rows returned)")
	String infoTestSucceededWithRows(int rows);
	
	@DefaultMessage("Success (100+ rows returned)")
	String infoTestSucceededWith100OrMoreRows();
	
	@DefaultMessage("Showing all {0} lines.")
	String infoShowingAllLines(int lines);
	
	@DefaultMessage("Showing all lines {0} -- {1}.")
	String infoShowingLines(int firstLine, int lastLine);
	
	@DefaultMessage("Room")
	String resourceRoom();
	
	@DefaultMessage("<u>T</u>ime Grid")
	String tabGrid();
	
	@DefaultMessage("List of <u>E</u>vents")
	String tabEventTable();
	
	@DefaultMessage("List of <u>M</u>eetings")
	String tabMeetingTable();
	
	@DefaultMessage("<u>C</u>urricula")
	String tabCurricula();
	
	@DefaultMessage("Students are required to attend this event.")
	String checkRequiredAttendance();
	
	@DefaultMessage("Display Conflicts")
	String checkDisplayConflicts();
	
	@DefaultMessage("All Sessions")
	String checkSpanMultipleSessions();

	@DefaultMessage("Include close by locations")
	String checkIncludeNearby();
	
	@DefaultMessage("Only event locations")
	String checkOnlyEventLocations();

	@DefaultMessage("All locations")
	String checkAllLocations();
	
	@DefaultMessage("Send email confirmation")
	String checkSendEmailConfirmation();
	
	@DefaultMessage("Please confirm the change in your contact information.")
	String checkYourContactChange();
	
	@DefaultMessage("Do not unassign conflicting assignments.")
	String checkIgnoreInstructorAssignmentConflicts();
	
	@DefaultMessage("Your contact information will be updated.")
	String confirmYourContactChange();
	
	@DefaultMessage("Please confirm the change in the contact information for {0}.")
	String checkMainContactChange(String name);
	
	@DefaultMessage("Change future sessions as well.")
	String checkApplyToFutureSessions();
	
	@DefaultMessage("The contact information for {0} will be updated.")
	String confirmMainContactChange(String name);
	
	@DefaultMessage("Please confirm you wish to remove all instructors from this course configuration.")
	String confirmRemoveClassInstructors();	

	@DefaultMessage("One email per line please.")
	String hintAdditionalEmails();
	
	@DefaultMessage("No academic session is selected.")
	String hintNoSession();
	
	@DefaultMessage("Showing {0} Enrollment")
	String hintEnrollmentOfType(String type);
	
	@DefaultMessage("Curriculum abbreviation must be filled in.")
	String hintCurriculumAbbreviationNotSet();
	
	@DefaultMessage("Curriculum name must be filled in.")
	String hintCurriculumNameNotSet();
	
	@DefaultMessage("An academic area must be selected.")
	String hintAcademicAreaNotSelected();
	
	@DefaultMessage("Selected academic area has no majors without a curriculum.")
	String hintAcademicAreaHasNoMajors();
	
	@DefaultMessage("A controlling department must be selected.")
	String hintControllingDepartmentNotSelected();
	
	@DefaultMessage("At least some students must be expected.")
	String hintNoStudentExpectations();
	
	@DefaultMessage("Show all columns.")
	String hintShowAllColumns();
	
	@DefaultMessage("Hide empty columns.")
	String hintHideEmptyColumns();
	
	@DefaultMessage("Click here or outside of the dialog to close.")
	String hintCloseDialog();
	
	@DefaultMessage("Reservation type must be selected.")
	String hintReservationTypeNotSelected();
	
	@DefaultMessage("No students provided.")
	String hintNoStudentsProvided();
	
	@DefaultMessage("Line {0} is not a valid student record.")
	String hintLineXIsNotValidStudent(String x);
	
	@DefaultMessage("Line {0} is not a valid student record: {1}")
	String hintLineXIsNotValidStudentException(String x, String reason);
	
	@DefaultMessage("A student group must be provided.")
	String hintStudentGroupNotProvided();
	
	@DefaultMessage("A course must be provided.")
	String hintCourseNotProvided();
	
	@DefaultMessage("An offering must be provided.")
	String hintOfferingNotProvided();
	
	@DefaultMessage("An academic area must be provided.")
	String hintAcademicAreaNotProvided();
	
	@DefaultMessage("Reservation type {0} not supported.")
	String hintReservationTypeNotSupported(String type);
	
	@DefaultMessage("Expiration date is not valid.")
	String hintExpirationDateNotValid();
	
	@DefaultMessage("Start date is not valid.")
	String hintStartDateNotValid();
	
	@DefaultMessage("Expiration date must be after the start date.")
	String hintExpirationDateNotAfterStartDate();
	
	@DefaultMessage("Reservation limit is not valid.")
	String hintReservationLimitNotValid();
	
	@DefaultMessage("No space selected")
	String hintNoSpaceSelected();
	
	@DefaultMessage("Only 1 space selected")
	String hintOnlyOneSpaceSelected();
	
	@DefaultMessage("Only {0} spaces selected")
	String hintOnlyNSpacesSelected(int n);
	
	@DefaultMessage("{0} not offered")
	String hintCourseNotOffered(String course);
	
	@DefaultMessage("No space in {0}")
	String hintNoSpaceInCourse(String course);
	
	@DefaultMessage("Only 1 space in {0}")
	String hintOnlyOneSpaceInCourse(String course);
	
	@DefaultMessage("Only {0} spaces in {1}")
	String hintOnlyNSpacesInCourse(int n, String course);

	@DefaultMessage("The selected offering has no reservations.")
	String hintOfferingHasNoReservations();
	
	@DefaultMessage("{0} is locked, it must be unlocked first.")
	String hintOfferingIsLocked(String course);
	
	@DefaultMessage("Configuration {0} ({1})")
	String selectionConfiguration(String name, String limit);
	
	@DefaultMessage("Configuration {0}")
	String labelConfiguration(String name);
	
	@DefaultMessage("unlimited")
	String configUnlimited();
	
	@DefaultMessage("Total {0} Enrollment")
	String totalEnrollmentOfType(String type);
	
	@DefaultMessage("Add Event")
	@DoNotTranslate
	String pageAddEvent();
	
	@DefaultMessage("Edit Event")
	@DoNotTranslate
	String pageEditEvent();

	@DefaultMessage("Event Detail")
	@DoNotTranslate
	String pageEventDetail();
	
	@DefaultMessage("Edit Room Event Availability")
	@DoNotTranslate
	String pageEditRoomEventAvailability();
	
	@DefaultMessage("Edit {0}")
	@DoNotTranslate
	String pageEdit(String name);
	
	@DefaultMessage("Add {0}")
	@DoNotTranslate
	String pageAdd(String name);
	
	@DefaultMessage("Academic Area")
	@DoNotTranslate
	String pageAcademicArea();
	
	@DefaultMessage("Academic Areas")
	@DoNotTranslate
	String pageAcademicAreas();
	
	@DefaultMessage("Academic Classification")
	@DoNotTranslate
	String pageAcademicClassification();

	@DefaultMessage("Academic Classifications")
	@DoNotTranslate
	String pageAcademicClassifications();
	
	@DefaultMessage("Course Credit Format")
	@DoNotTranslate
	String pageCourseCreditFormat();

	@DefaultMessage("Course Credit Formats")
	@DoNotTranslate
	String pageCourseCreditFormats();
	
	@DefaultMessage("Course Credit Type")
	@DoNotTranslate
	String pageCourseCreditType();

	@DefaultMessage("Course Credit Types")
	@DoNotTranslate
	String pageCourseCreditTypes();
	
	@DefaultMessage("Course Credit Unit")
	@DoNotTranslate
	String pageCourseCreditUnit();

	@DefaultMessage("Course Credit Units")
	@DoNotTranslate
	String pageCourseCreditUnits();
	
	@DefaultMessage("Course Type")
	@DoNotTranslate
	String pageCourseType();
	
	@DefaultMessage("Course Types")
	@DoNotTranslate
	String pageCourseTypes();
	
	@DefaultMessage("Override Type")
	@DoNotTranslate
	String pageOverrideType();
	
	@DefaultMessage("Override Types")
	@DoNotTranslate
	String pageOverrideTypes();

	@DefaultMessage("Concact Category")
	@DoNotTranslate
	String pageContactCategory();
	
	@DefaultMessage("Concact Categories")
	@DoNotTranslate
	String pageContactCategories();

	@DefaultMessage("Event Date Mapping")
	@DoNotTranslate
	String pageEventDateMapping();
	
	@DefaultMessage("Event Date Mappings")
	@DoNotTranslate
	String pageEventDateMappings();
	
	@DefaultMessage("Event Status")
	@DoNotTranslate
	String pageEventStatus();

	@DefaultMessage("Event Statuses")
	@DoNotTranslate
	String pageEventStatuses();
	
	@DefaultMessage("Event Default")
	@DoNotTranslate
	String pageEventDefault();

	@DefaultMessage("Event Defaults")
	@DoNotTranslate
	String pageEventDefaults();
	
	@DefaultMessage("Examination Type")
	@DoNotTranslate
	String pageExaminationType();

	@DefaultMessage("Examination Types")
	@DoNotTranslate
	String pageExaminationTypes();
	
	@DefaultMessage("Instructor Role")
	@DoNotTranslate
	String pageInstructorRole();
	
	@DefaultMessage("Instructor Roles")
	@DoNotTranslate
	String pageInstructorRoles();
	
	@DefaultMessage("Logging Level")
	@DoNotTranslate
	String pageLoggingLevel();
	
	@DefaultMessage("Logging Levels")
	@DoNotTranslate
	String pageLoggingLevels();
	
	@DefaultMessage("Major")
	@DoNotTranslate
	String pageMajor();

	@DefaultMessage("Majors")
	@DoNotTranslate
	String pageMajors();

	@DefaultMessage("Minor")
	@DoNotTranslate
	String pageMinor();

	@DefaultMessage("Minors")
	@DoNotTranslate
	String pageMinors();
	
	@DefaultMessage("Concentration")
	@DoNotTranslate
	String pageConcentration();

	@DefaultMessage("Concentrations")
	@DoNotTranslate
	String pageConcentrations();
	
	@DefaultMessage("Degree")
	@DoNotTranslate
	String pageDegree();

	@DefaultMessage("Degrees")
	@DoNotTranslate
	String pageDegrees();
	
	@DefaultMessage("Program")
	@DoNotTranslate
	String pageProgram();

	@DefaultMessage("Programs")
	@DoNotTranslate
	String pagePrograms();
	
	@DefaultMessage("Campus")
	@DoNotTranslate
	String pageCampus();

	@DefaultMessage("Campuses")
	@DoNotTranslate
	String pageCampuses();
	
	@DefaultMessage("Offering Consent Type")
	@DoNotTranslate
	String pageOfferingConsentType();
	
	@DefaultMessage("Offering Consent Types")
	@DoNotTranslate
	String pageOfferingConsentTypes();
	
	@DefaultMessage("Permission")
	@DoNotTranslate
	String pagePermission();
	
	@DefaultMessage("Permissions")
	@DoNotTranslate
	String pagePermissions();
	
	@DefaultMessage("Position Type")
	@DoNotTranslate
	String pagePositionType();
	
	@DefaultMessage("Position Types")
	@DoNotTranslate
	String pagePositionTypes();
	
	@DefaultMessage("Room Feature Type")
	@DoNotTranslate
	String pageRoomFeatureType();
	
	@DefaultMessage("Room Feature Types")
	@DoNotTranslate
	String pageRoomFeatureTypes();
	
	@DefaultMessage("Standard Event Note")
	@DoNotTranslate
	String pageStandardEventNote();
	
	@DefaultMessage("Standard Event Notes")
	@DoNotTranslate
	String pageStandardEventNotes();
	
	@DefaultMessage("Student Group")
	@DoNotTranslate
	String pageStudentGroup();
	
	@DefaultMessage("Student Groups")
	@DoNotTranslate
	String pageStudentGroups();
	
	@DefaultMessage("Student Scheduling Status Type")
	@DoNotTranslate
	String pageStudentSchedulingStatusType();
	
	@DefaultMessage("Student Scheduling Status Types")
	@DoNotTranslate
	String pageStudentSchedulingStatusTypes();
	
	@DefaultMessage("Role")
	@DoNotTranslate
	String pageRole();
	
	@DefaultMessage("Roles")
	@DoNotTranslate
	String pageRoles();
	
	@DefaultMessage("Class Duration Type")
	@DoNotTranslate
	String pageDurationType();

	@DefaultMessage("Class Duration Types")
	@DoNotTranslate
	String pageDurationTypes();
	
	@DefaultMessage("Examination Status")
	@DoNotTranslate
	String pageExaminationStatus();

	@DefaultMessage("Examination Statuses")
	@DoNotTranslate
	String pageExaminationStatuses();
	
	@DefaultMessage("Add Curriculum")
	@DoNotTranslate
	String pageAddCurriculum();
	
	@DefaultMessage("Edit Curriculum")
	@DoNotTranslate
	String pageEditCurriculum();
	
	@DefaultMessage("Curriculum Details")
	@DoNotTranslate
	String pageCurriculumDetails();
	
	@DefaultMessage("Curricula")
	@DoNotTranslate
	String pageCurricula();
	
	@DefaultMessage("Course Reports")
	@DoNotTranslate
	String pageCourseReports();
	
	@DefaultMessage("Examination Reports")
	@DoNotTranslate
	String pageExaminationReports();
	
	@DefaultMessage("Student Scheduling Reports")
	@DoNotTranslate
	String pageStudentSectioningReports();
	
	@DefaultMessage("Event Reports")
	@DoNotTranslate
	String pageEventReports();
	
	@DefaultMessage("Administration Reports")
	@DoNotTranslate
	String pageAdministrationReports();
	
	@DefaultMessage("Curriculum Requested Enrollments")
	@DoNotTranslate
	String pageCurriculumRequestedEnrollments();
	
	@DefaultMessage("Reservations")
	@DoNotTranslate
	String pageReservations();
	
	@DefaultMessage("Add Reservation")
	@DoNotTranslate
	String pageAddReservation();
	
	@DefaultMessage("Edit Reservation")
	@DoNotTranslate
	String pageEditReservation();
	
	@DefaultMessage("Edit Course Offering")
	@DoNotTranslate
	String pageEditCourseOffering();
	
	@DefaultMessage("Add Course Offering")
	@DoNotTranslate
	String pageAddCourseOffering();
	
	@DefaultMessage("Curriculum Projection Rules")
	@DoNotTranslate
	String pageCurriculumProjectionRules();
	
	@DefaultMessage("Student Scheduling Assistant")
	@DoNotTranslate
	String pageStudentSchedulingAssistant();
	
	@DefaultMessage("Student Course Requests")
	@DoNotTranslate
	String pageStudentCourseRequests();
	
	@DefaultMessage("Administration")
	@DoNotTranslate
	String pageAdministration();
	
	@DefaultMessage("Events")
	@DoNotTranslate
	String pageEvents();
	
	@DefaultMessage("Event Timetable")
	@DoNotTranslate
	String pageEventTimetable();
	
	@DefaultMessage("Room Timetable")
	@DoNotTranslate
	String pageRoomTimetable();
	
	@DefaultMessage("Online Student Sectioning Test")
	@DoNotTranslate
	String pageOnlineStudentSectioningTest();
	
	@DefaultMessage("Online Student Scheduling Dashboard")
	@DoNotTranslate
	String pageOnlineStudentSchedulingDashboard();
	
	@DefaultMessage("Batch Student Solver Dashboard")
	@DoNotTranslate
	String pageStudentSectioningDashboard();
	
	@DefaultMessage("Travel Times")
	@DoNotTranslate
	String pageTravelTimes();
	
	@DefaultMessage("Lookup Classes")
	@DoNotTranslate
	String pageClasses();
	
	@DefaultMessage("Lookup Examinations")
	@DoNotTranslate
	String pageExaminations();
	
	@DefaultMessage("Personal Schedule")
	@DoNotTranslate
	String pagePersonalTimetable();
	
	@DefaultMessage("Edit Room Availability")
	@DoNotTranslate
	String pageEditRoomAvailability();
	
	@DefaultMessage("Scripts")
	@DoNotTranslate
	String pageScripts();
	
	@DefaultMessage("Event Room Availability")
	@DoNotTranslate
	String pageEventRoomAvailability();
	
	@DefaultMessage("Student Accommodation")
	@DoNotTranslate
	String pageStudentAccommodation();
	
	@DefaultMessage("Student Accommodations")
	@DoNotTranslate
	String pageStudentAccommodations();
	
	@DefaultMessage("Batch Student Solver Reports")
	@DoNotTranslate
	String pageBatchSectioningReports();
	
	@DefaultMessage("Online Student Scheduling Reports")
	@DoNotTranslate
	String pageOnlineSectioningReports();
	
	@DefaultMessage("Room Pictures")
	@DoNotTranslate
	String pageRoomPictures();
	
	@DefaultMessage("Rooms")
	@DoNotTranslate
	String pageRooms();
	
	@DefaultMessage("Room Detail")
	@DoNotTranslate
	String pageRoomDetail();
	
	@DefaultMessage("Add Room")
	@DoNotTranslate
	String pageAddRoom();
	
	@DefaultMessage("Edit Room")
	@DoNotTranslate
	String pageEditRoom();
	
	@DefaultMessage("Attachment Type")
	@DoNotTranslate
	String pageAttachmentType();
	
	@DefaultMessage("Attachment Types")
	@DoNotTranslate
	String pageAttachmentTypes();
	
	@DefaultMessage("InstructionalMethod")
	@DoNotTranslate
	String pageInstructionalMethod();
	
	@DefaultMessage("Instructional Methods")
	@DoNotTranslate
	String pageInstructionalMethods();
	
	@DefaultMessage("Edit Room Departments")
	@DoNotTranslate
	String pageEditRoomsDepartments();
	
	@DefaultMessage("Add Room Group")
	@DoNotTranslate
	String pageAddRoomGroup();
	
	@DefaultMessage("Edit Room Group")
	@DoNotTranslate
	String pageEditRoomGroup();
	
	@DefaultMessage("Room Groups")
	@DoNotTranslate
	String pageRoomGroups();
	
	@DefaultMessage("Add Room Feature")
	@DoNotTranslate
	String pageAddRoomFeature();
	
	@DefaultMessage("Edit Room Feature")
	@DoNotTranslate
	String pageEditRoomFeature();
	
	@DefaultMessage("Room Features")
	@DoNotTranslate
	String pageRoomFeatures();
	
	@DefaultMessage("Preference Level")
	@DoNotTranslate
	String pagePreferenceLevel();

	@DefaultMessage("Preference Levels")
	@DoNotTranslate
	String pagePreferenceLevels();
	
	@DefaultMessage("Instructor Attribute Type")
	@DoNotTranslate
	String pageInstructorAttributeType();
	
	@DefaultMessage("Instructor Attribute Types")
	@DoNotTranslate
	String pageInstructorAttributeTypes();
	
	@DefaultMessage("Add Instructor Attribute")
	@DoNotTranslate
	String pageAddInstructorAttribute();
	
	@DefaultMessage("Edit Instructor Attribute")
	@DoNotTranslate
	String pageEditInstructorAttribute();
	
	@DefaultMessage("Instructor Attributes")
	@DoNotTranslate
	String pageInstructorAttributes();
	
	@DefaultMessage("Solver")
	@DoNotTranslate
	String pageSolver();
	
	@DefaultMessage("Solver Log")
	@DoNotTranslate
	String pageSolverLog();
	
	@DefaultMessage("Course Timetabling Solver")
	@DoNotTranslate
	String pageCourseTimetablingSolver();

	@DefaultMessage("Examination Timetabling Solver")
	@DoNotTranslate
	String pageExaminationTimetablingSolver();

	@DefaultMessage("Student Scheduling Solver")
	@DoNotTranslate
	String pageStudentSchedulingSolver();

	@DefaultMessage("Instructor Scheduling Solver")
	@DoNotTranslate
	String pageInstructorSchedulingSolver();
	
	@DefaultMessage("Course Timetabling Solver Log")
	@DoNotTranslate
	String pageCourseTimetablingSolverLog();

	@DefaultMessage("Examination Timetabling Solver Log")
	@DoNotTranslate
	String pageExaminationTimetablingSolverLog();

	@DefaultMessage("Student Scheduling Solver Log")
	@DoNotTranslate
	String pageStudentSchedulingSolverLog();

	@DefaultMessage("Instructor Scheduling Solver Log")
	@DoNotTranslate
	String pageInstructorSchedulingSolverLog();
	
	@DefaultMessage("Assigned Teaching Requests")
	@DoNotTranslate
	String pageAssignedTeachingRequests();
	
	@DefaultMessage("Not-Assigned Teaching Requests")
	@DoNotTranslate
	String pageUnassignedTeachingRequests();
	
	@DefaultMessage("Teaching Assignments")
	@DoNotTranslate
	String pageTeachingAssignments();
	
	@DefaultMessage("Teaching Assignment Changes")
	@DoNotTranslate
	String pageTeachingAssignmentChanges();
	
	@DefaultMessage("Teaching Responsibility")
	@DoNotTranslate
	String pageTeachingResponsibility();
	
	@DefaultMessage("Teaching Responsibilities")
	@DoNotTranslate
	String pageTeachingResponsibilities();
	
	@DefaultMessage("Instructor Survey Note Type")
	@DoNotTranslate
	String pageInstructorSurveyNoteType();
	
	@DefaultMessage("Instructor Survey Note Types")
	@DoNotTranslate
	String pageInstructorSurveyNoteTypes();
	
	@DefaultMessage("Setup Teaching Requests")
	@DoNotTranslate
	String pageSetupTeachingRequests();
	
	@DefaultMessage("Point In Time Data Snapshot")
	@DoNotTranslate
	String pagePointInTimeDataSnapshot();
	
	@DefaultMessage("Point In Time Data Snapshots")
	@DoNotTranslate
	String pagePointInTimeDataSnapshots();
	
	@DefaultMessage("Point In Time Data Reports")
	@DoNotTranslate
	String pagePointInTimeDataReports();

	@DefaultMessage("Timetable")
	@DoNotTranslate
	String pageTimetableGrid();
	
	@DefaultMessage("Assigned Classes")
	@DoNotTranslate
	String pageAssignedClasses();
	
	@DefaultMessage("Not-Assigned Classes")
	@DoNotTranslate
	String pageNotAssignedClasses();
	
	@DefaultMessage("Limit and Projection Snapshot")
	@DoNotTranslate
	String pageLimitAndProjectionSnapshot();
	
	@DefaultMessage("Event Service Provider")
	@DoNotTranslate
	String pageServiceProvider();
	
	@DefaultMessage("Event Service Providers")
	@DoNotTranslate
	String pageServiceProviders();

	@DefaultMessage("Suggestions")
	@DoNotTranslate
	String pageSuggestions();
	
	@DefaultMessage("Conflict-Based Statistics")
	@DoNotTranslate
	String pageConflictBasedStatistics();
	
	@DefaultMessage("Changes")
	@DoNotTranslate
	String pageSolutionChanges();
	
	@DefaultMessage("Assignment History")
	@DoNotTranslate
	String pageAssignmentHistory();
	
	@DefaultMessage("Saved Timetables")
	@DoNotTranslate
	String pageListSolutions();
	
	@DefaultMessage("Solution Reports")
	@DoNotTranslate
	String pageSolutionReports();
	
	@DefaultMessage("Student Advisor")
	@DoNotTranslate
	String pageStudentAdvisor();
	
	@DefaultMessage("Student Advisors")
	@DoNotTranslate
	String pageStudentAdvisors();
	
	@DefaultMessage("Student Group Type")
	@DoNotTranslate
	String pageStudentGroupType();
	
	@DefaultMessage("Student Group Types")
	@DoNotTranslate
	String pageStudentGroupTypes(); 
	
	@DefaultMessage("Task Scheduler")
	@DoNotTranslate
	String pageTasks();
	
	@DefaultMessage("Task Details")
	@DoNotTranslate
	String pageTaskDetails();
	
	@DefaultMessage("Published Schedule Runs")
	@DoNotTranslate
	String pagePublishedSectioningSolutions();
	
	@DefaultMessage("Learning Management System")
	@DoNotTranslate
	String pageLearningManagementSystemInfo();

	@DefaultMessage("Learning Management Systems")
	@DoNotTranslate
	String pageLearningManagementSystemInfos();
	
	@DefaultMessage("Advisor Course Recommendations")
	@DoNotTranslate
	String pageAdvisorCourseRequests();
	
	@DefaultMessage("Sponsoring Organization")
	@DoNotTranslate
	String pageSponsoringOrganization();
	
	@DefaultMessage("Sponsoring Organizations")
	@DoNotTranslate
	String pageSponsoringOrganizations();
	
	@DefaultMessage("Room Type")
	@DoNotTranslate
	String pageRoomType();
	
	@DefaultMessage("Room Types")
	@DoNotTranslate
	String pageRoomTypes();
	
	@DefaultMessage("Default Manager Setting")
	@DoNotTranslate
	String pageDefaultSetting();
	
	@DefaultMessage("Default Manager Settings")
	@DoNotTranslate
	String pageDefaultSettings();
	
	@DefaultMessage("Buildings")
	@DoNotTranslate
	String pageBuildings();
	
	@DefaultMessage("Add Building")
	@DoNotTranslate
	String pageAddBuilding();
	
	@DefaultMessage("Edit Building")
	@DoNotTranslate
	String pageEditBuilding();
	
	@DefaultMessage("Solution Information Definition")
	@DoNotTranslate
	String pageSolverInfoDef();
	
	@DefaultMessage("Solution Information Definitions")
	@DoNotTranslate
	String pageSolverInfoDefs();
	
	@DefaultMessage("Solver Parameter Group")
	@DoNotTranslate
	String pageSolverParamGroup();
	
	@DefaultMessage("Solver Parameter Groups")
	@DoNotTranslate
	String pageSolverParamGroups();
	
	@DefaultMessage("Solver Parameter")
	@DoNotTranslate
	String pageSolverParam();
	
	@DefaultMessage("Solver Parameters")
	@DoNotTranslate
	String pageSolverParams();
	
	@DefaultMessage("Instructor Survey")
	@DoNotTranslate
	String pageInstructorSurvey();
	
	@DefaultMessage("Multiple Class Setup")
	@DoNotTranslate
	String pageMultipleClassSetup();
	
	@DefaultMessage("Instructional Offering Configuration")
	@DoNotTranslate
	String pageInstrOfferingConfig();
	
	@DefaultMessage("N/A")
	String itemNotApplicable();
	
	@DefaultMessage("All Departments")
	String itemAllDepartments();
	
	@DefaultMessage("All Subjects")
	String itemAllSubjectAreas();
	
	@DefaultMessage("None")
	String itemNone();
	
	@DefaultMessage("No Type")
	String itemNoFeatureType();
	
	@DefaultMessage("No Type")
	String itemNoStudentGroupType();

	@DefaultMessage("Add Meetings...")
	String dialogAddMeetings();
	
	@DefaultMessage("Modify Meetings...")
	String dialogModifyMeetings();

	@DefaultMessage("Approve Meetings...")
	String dialogApprove();

	@DefaultMessage("Reject Meetings...")
	String dialogReject();

	@DefaultMessage("Cancel Meetings...")
	String dialogCancel();

	@DefaultMessage("Inquire...")
	String dialogInquire();
	
	@DefaultMessage("Standard Notes")
	String dialogStandardNotes();
	
	@DefaultMessage("Curriculum Projection Rules")
	String dialogCurriculumProjectionRules();
	
	@DefaultMessage("New Report")
	String dialogNewReport();
	
	@DefaultMessage("Edit {0}")
	String dialogEditReport(String name);
	
	@DefaultMessage("People Lookup")
	String dialogPeopleLookup();
	
	@DefaultMessage("Add Script")
	String dialogAddScript();
	
	@DefaultMessage("Edit Script")
	String dialogEditScript();
	
	@DefaultMessage("Confirmation")
	String dialogConfirmation();
	
	@DefaultMessage("Warning")
	String dialogAlert();
	
	@DefaultMessage("Information")
	String dialogInfo();
	
	@DefaultMessage("Details of {0}")
	String dialogDetailsOf(String item);
	
	@DefaultMessage("Details of {0} schedule run made by {1}")
	String dialogDetailsOfPublishedScheduleRun(String time, String owner);
	
	@DefaultMessage("Details of {0} {1}")
	String dialogTeachingRequestDetail(String course, String section);

	@DefaultMessage("{0}<br>{1}<br>{2} seats")
	String singleRoomSelection(String name, String type, String capacity);
	
	@DefaultMessage("{0}<br>{1}<br>{2} - {3}")
	String dateTimeHeader(String dow, String date, String start, String end);
	
	@DefaultMessage("{0}, {1} {2} - {3}")
	String dateTimeHint(String dow, String date, String start, String end);
	
	@DefaultMessage("New Group")
	String dialogNewGroup();
	
	@DefaultMessage("Edit Group")
	String dialogEditGroup();
	
	@DefaultMessage("Suggestions")
	String dialogSuggestions();
	
	@DefaultMessage("Selected")
	String legendSelected();
	
	@DefaultMessage("Not Selected")
	String legendNotSelected();
	
	@DefaultMessage("Not in Session")
	String legendNotInSession();
	
	@DefaultMessage("Classes Start/End")
	String legendClassesStartOrEnd();
	
	@DefaultMessage("Classes Start")
	String legendClassesStart();

	@DefaultMessage("Classes End")
	String legendClassesEnd();

	@DefaultMessage("Finals")
	String legendFinals();
	
	@DefaultMessage("Midterms")
	String legendMidterms();
	
	@DefaultMessage("Holiday")
	String legendHoliday();
	
	@DefaultMessage("Break")
	String legendBreak();
	
	@DefaultMessage("Today")
	String legendToday();
	
	@DefaultMessage("In The Past")
	String legendPast();
	
	@DefaultMessage("Mapped Class Date")
	String legendDateMappingClassDate();

	@DefaultMessage("Mapped Event Date")
	String legendDateMappingEventDate();

	@DefaultMessage("Finals")
	String hintFinals();
	
	@DefaultMessage("Holiday")
	String hintHoliday();
	
	@DefaultMessage("Break")
	String hintBreak();

	@DefaultMessage("Weekend")
	String hintWeekend();
	
	@DefaultMessage("Too Early?")
	String hintTooEarly();
	
	@DefaultMessage("Midterms")
	String hintMidterms();
	
	@DefaultMessage("<span title='Conflicting event' style='font-style:normal;' aria-label='Conflicting event'>&#9785;</span>")
	String signConflict();
	
	@DefaultMessage("<span title='Selected event' style='font-style:normal;' aria-label='Selected event'>&#9745;</span>")
	String signSelected();

	@DefaultMessage("<span title='Warning' style='font-style:normal;' aria-label='Warning'>&#9888;</span>")
	String signMessage();

	@DefaultMessage("Event name is required.")
	String reqEventName();
	
	@DefaultMessage("Expected attendance is required.")
	String reqAttendance();
	
	@DefaultMessage("Main contact last name is required.")
	String reqMainContactLastName();

	@DefaultMessage("Main contact email is required.")
	String reqMainContactEmail();

	@DefaultMessage("No meetings were defined.")
	String reqMeetings();

	@DefaultMessage("One or more meetings is overlapping with an existing event.")
	String reqNoOverlaps();
	
	@DefaultMessage("Sponsoring organization is required.")
	String reqSponsoringOrg();
	
	@DefaultMessage("No courses / clases were defined.")
	String reqCoursesOrClasses();
	
	@DefaultMessage("Additional Information is too long.")
	String eventNoteTooLong();
	
	@DefaultMessage("an event")
	String anEvent();
	
	@DefaultMessage("Requested meeting is in the past or outside of {0}.")
	String conflictPastOrOutside(String academicSessionName);
	
	@DefaultMessage("{0} is not managed in UniTime or it is disabled for events.")
	String conflictNotEventRoom(String locationName);
	
	@DefaultMessage("{0} is currently not available for event scheduling.")
	String conflictRoomDenied(String locationName);

	@DefaultMessage("Not enough permissions to make {0} not available.")
	String conflictCannotMakeUnavailable(String locationName);

	@DefaultMessage("<i>File {0} attached.</i>")
	String noteAttachment(String fileName);
	
	@DefaultMessage("Confirmation emails are disabled.")
	String emailDisabled();
	
	@DefaultMessage("Event {0} created.")
	String emailSubjectCreate(String eventName);

	@DefaultMessage("Event {0} updated.")
	String emailSubjectUpdate(String eventName);

	@DefaultMessage("Event {0} deleted.")
	String emailSubjectDelete(String eventName);

	@DefaultMessage("Event {0} approved.")
	String emailSubjectApprove(String eventName);

	@DefaultMessage("Event {0} rejected.")
	String emailSubjectReject(String eventName);

	@DefaultMessage("Event {0} cancelled.")
	String emailSubjectCancel(String eventName);

	@DefaultMessage("Event {0} inquiry.")
	String emailSubjectInquire(String eventName);
	
	@DefaultMessage("Event {0} expired.")
	String emailSubjectExpired(String eventName);
	
	@DefaultMessage("Confirmation email sent to {0}.")
	String infoConfirmationEmailSent(String name);
	
	@DefaultMessage("Failed to send confirmation email: {0}")
	String failedToSendConfirmationEmail(String reason);

	@DefaultMessage("Following meetings were requested by you or on your behalf")
	String emailCreatedMeetings();
	
	@DefaultMessage("Following meetings were deleted by you or on your behalf")
	String emailDeletedMeetings();
	
	@DefaultMessage("Following meetings were cancelled by you or on your behalf")
	String emailCancelledMeetingsInEdit();

	@DefaultMessage("Following meetings are in question")
	String emailInquiredMeetings();

	@DefaultMessage("Following meetings were approved")
	String emailApprovedMeetings();

	@DefaultMessage("Following meetings were rejected")
	String emailRejectedMeetings();
	
	@DefaultMessage("Following meetings were cancelled")
	String emailCancelledMeetings();

	@DefaultMessage("Following meetings were updated by you or on your behalf")
	String emailUpdatedMeetings();
	
	@DefaultMessage("Additional Information")
	String emailMessageCreate();
	
	@DefaultMessage("Additional Information")
	String emailMessageUpdate();

	@DefaultMessage("Additional Information")
	String emailMessageDelete();

	@DefaultMessage("Notes")
	String emailMessageCancel();

	@DefaultMessage("Notes")
	String emailMessageApproval();

	@DefaultMessage("Notes")
	String emailMessageReject();

	@DefaultMessage("Inquiry")
	String emailMessageInquiry();
	
	@DefaultMessage("Current status of {0}")
	String emailAllMeetings(String eventName);

	@DefaultMessage("No meeting left, the event {0} was deleted as well.")
	String emailEventDeleted(String eventName);
	
	@DefaultMessage("There are no pending or approved meetings in the event.")
	String emailEventNoMeetings();

	@DefaultMessage("All Notes of {0}")
	String emailNotes(String eventName);
	
	@DefaultMessage("N/A")
	String notApplicable();
	
	@DefaultMessage("Not set")
	String notSet();
	
	@DefaultMessage("Not Selected")
	String notSelected();
	
	@DefaultMessage("All")
	String itemAll();
	
	@DefaultMessage("All Matching")
	String itemAllWithFilter();
	
	@DefaultMessage("All Weeks")
	String itemAllWeeks();
	
	@DefaultMessage("All Matching Weeks")
	String itemAllWeeksWithFilter();
	
	@DefaultMessage("Week {0} - {1}")
	String itemWeek(String first, String last);
	
	@DefaultMessage("Weeks {0} - {1}")
	String itemWeeks(String first, String last);
	
	@DefaultMessage("All Rooms")
	String itemAllRooms();
	
	@DefaultMessage("All Matching Rooms")
	String itemAllRoomsWithFilter();
	
	@DefaultMessage("Select...")
	String itemSelect();
	
	@DefaultMessage("None Required")
	String consentNone();
	
	@DefaultMessage("Consent of Department")
	String consentDepartment();
	
	@DefaultMessage("Consent of Instructor")
	String consentInstructor();
	
	@DefaultMessage("This academic session only")
	String itemThisSessionOnly();

	@DefaultMessage("This academic session and all future sessions")
	String itemAllFutureSessions();

	@DefaultMessage("All academic sessions")
	String itemAllSessions();
	
	@DefaultMessage("No controlling department")
	String itemNoControlDepartment();
	
	@DefaultMessage("No event department")
	String itemNoEventDepartment();
	
	@DefaultMessage("Default")
	String itemDefault();
	
	@DefaultMessage("No Parent")
	String itemInstructorAttributeNoParent();
	
	@DefaultMessage("{0} seats")
	String hintRoomCapacity(String size);
	
	@DefaultMessage("{0} m")
	String hintRoomDistance(String distanceInMeters);
	
	@DefaultMessage("{0}: {1}")
	String hintRoomFeatureWithDescription(String label, String description);
	
	@DefaultMessage("{0}: {1}")
	String hintRoomGroupWithDescription(String label, String description);
	
	@DefaultMessage("Press ENTER or double click a standard note to add it to additional information.")
	String hintStandardNoteDoubleClickToSelect();
	
	@DefaultMessage("Defaults to {0} when empty.")
	String hintDefaultsToWhenEmpty(String value);
	
	@DefaultMessage("{2}, {0} {1}")
	String formatName(String first, String middle, String last);
	
	@DefaultMessage("from:<br><font color='gray'>to:</font>")
	String roomSharingCorner();
	
	@DefaultMessage("{0}<br><font color='gray'>{1}</font>")
	String roomSharingTimeHeader(String from, String to);
	
	@DefaultMessage("Horizontal")
	String roomSharingHorizontal();
	
	@DefaultMessage("Horizontal")
	String periodPreferenceHorizontal();
	
	@DefaultMessage("Free For All")
	String legendFreeForAll();
	
	@DefaultMessage("")
	@DoNotTranslate
	String codeFreeForAll();
	
	@DefaultMessage("Not Available")
	String legendNotAvailable();
	
	@DefaultMessage("N/A")
	String codeNotAvailable();
	
	@DefaultMessage("Available")
	String legendAvailable();
	
	@DefaultMessage("")
	@DoNotTranslate
	String codeAvailable();
	
	@DefaultMessage("<u>A</u>dd Department")
	String buttonAddDepartment();

	@DefaultMessage("<u>A</u>dd Department...")
	String buttonRoomSharingAddDepartment();
	
	@DefaultMessage("<u>R</u>emove All")
	String buttonRemoveAll();
	
	@DefaultMessage("Remove <u>S</u>elected")
	String buttonRemoveSelected();
	
	@DefaultMessage("Add Department...")
	String dialogAddDepartment();
	
	@DefaultMessage("-- Add Department --")
	String separatorAddDepartment();
	
	@DefaultMessage("Add All Matching Departments")
	String buttonAddAllDepartments();
	
	@DefaultMessage("No academic session is available.")
	String noSessionAvailable();
	
	@DefaultMessage("Not Available")
	String unavailableEventDefaultName();
	
	@DefaultMessage("Room Note History")
	String sectRoomNoteHistory();
	
	@DefaultMessage("The selected room has no room note changes.")
	String noRoomNoteChanges();
	
	@DefaultMessage("Failed to load change logs: {0}")
	String failedLoadRoomNoteChanges(String cause);
	
	@DefaultMessage("Session")
	String colAcademicSession();
	
	@DefaultMessage("Manager")
	String colManager();
	
	@DefaultMessage("not set")
	String emptyNote();
	
	@DefaultMessage("Display changes across all academic sessions.")
	String checkAllSessions();
	
	@DefaultMessage("Show deleted, cancelled, and rejected meetings.")
	String showDeletedMeetings();
	
	@DefaultMessage("Capacity:")
	String propRoomCapacity();
	
	@DefaultMessage("Area:")
	String propRoomArea();
	
	@DefaultMessage("Groups:")
	String propRoomGroups();

	@DefaultMessage("Events:")
	String propRoomEventStatus();

	@DefaultMessage("Department:")
	String propRoomEventDepartment();

	@DefaultMessage("Distance:")
	String propRoomDistance();

	@DefaultMessage("Break Time:")
	String propRoomBreakTime();
	
	@DefaultMessage("Report:")
	String propReport();
	
	@DefaultMessage("Description:")
	String propDescription();
	
	@DefaultMessage("Query:")
	String propQuery();
	
	@DefaultMessage("Flags:")
	String propFlags();
	
	@DefaultMessage("Instructional Offering:")
	String propInstructionalOffering();
	
	@DefaultMessage("Reserved Space:")
	String propReservedSpace();
	
	@DefaultMessage("Course Number:")
	String propCourseNumber();
	
	@DefaultMessage("By Reservation Only:")
	String propByReservationOnly();
	
	@DefaultMessage("New Enrollment Deadline:")
	String propNewEnrollmentDeadline();
	
	@DefaultMessage("Class Changes Deadline:")
	String propClassChangesDeadline();
	
	@DefaultMessage("Course Drop Deadline:")
	String propCourseDropDeadline();
	
	@DefaultMessage("Schedule of Classes Note:")
	String propScheduleNote();
	
	@DefaultMessage("Requests/Notes:")
	String propRequestsNotes();
	
	@DefaultMessage("Consent:")
	String propConsent();
	
	@DefaultMessage("Credit:")
	String propCredit();
	
	@DefaultMessage("Credit Type:")
	String propCreditType();
	
	@DefaultMessage("Credit Unit Type:")
	String propCreditUnitType();
	
	@DefaultMessage("Units:")
	String propUnits();
	
	@DefaultMessage("Max Units:")
	String propMaxUnits();
	
	@DefaultMessage("Fractional Increments Allowed:")
	String propFractional();
	
	@DefaultMessage("Take Course Demands from Offering:")
	String propCourseDemands();
	
	@DefaultMessage("Default Alternative Course Offering:")
	String propAlternativeCourseOffering();
	
	@DefaultMessage("Coordinators:")
	String propCoordinators();
	
	@DefaultMessage("Funding Department:")
	String propFundingDepartment();
	
	@DefaultMessage("Wait-Listing:")
	String propWaitListing();

	@DefaultMessage("Course Catalog:")
	String propertyCourseCatalog();
	
	@DefaultMessage("Subject:")
	String propSubject();
	
	@DefaultMessage("Subject is required.")
	String errorSubjectRequired();
	
	@DefaultMessage("Course Number is required.")
	String errorCourseNumberRequired();
	
	@DefaultMessage("The course cannot be created. A course with the same course number already exists.")
	String errorCourseCannotBeCreated();
	
	@DefaultMessage("The course cannot be renamed. A course with the same course number already exists.")
	String errorCourseCannotBeRenamed();
	
	@DefaultMessage("Duplicate coordinator for a course.")
	String errorDuplicateCoordinator();
	
	@DefaultMessage("Expiration Date:")
	String propExpirationDate();
	
	@DefaultMessage("Start Date:")
	String propStartDate();
	
	@DefaultMessage("Restrictions:")
	String propRestrictions();
	
	@DefaultMessage("Mode:")
	String propInclusive();
	
	@DefaultMessage("Type:")
	String propType();
	
	@DefaultMessage("Students:")
	String propStudents();
	
	@DefaultMessage("Student Group:")
	String propStudentGroup();
	
	@DefaultMessage("Course:")
	String propCourse();
	
	@DefaultMessage("Engine:")
	String propEngine();
	
	@DefaultMessage("Permission:")
	String propPermission();
	
	@DefaultMessage("Script:")
	String propScript();
	
	@DefaultMessage("Parameters:")
	String propParameters();
	
	@DefaultMessage("External Id:")
	String propExternalId();
	
	@DefaultMessage("Capacity:")
	String propCapacity();
	
	@DefaultMessage("Examination Rooms:")
	String propExamRooms();

	@DefaultMessage("Examination Seating Capacity:")
	String propExamCapacity();
	
	@DefaultMessage("Controlling Department:")
	String propControllingDepartment();
	
	@DefaultMessage("Controlling Department")
	String colControllingDepartment();

	@DefaultMessage("Status for classes managed by {0}")
	String propStatusManagedBy(String managedBy);
	
	@DefaultMessage("Coordinates:")
	String propCoordinates();
	
	@DefaultMessage("Distance Check:")
	String propDistanceCheck();
	
	@DefaultMessage("Room Check:")
	String propRoomCheck();
	
	@DefaultMessage("Event Department:")
	String propEventDepartment();
	
	@DefaultMessage("Event Status:")
	String propEventStatus();
	
	@DefaultMessage("Event Message:")
	String propEventNote();
	
	@DefaultMessage("Event Email:")
	String propEventEmail();
	
	@DefaultMessage("Break Time:")
	String propBreakTime();
	
	@DefaultMessage("Groups:")
	String propGlobalGroups();
	
	@DefaultMessage("Department Groups:")
	String propDepartmenalGroups();
	
	@DefaultMessage("Features:")
	String propFeatures();
	
	@DefaultMessage("Preference:")
	String propPreference();
	
	@DefaultMessage("{0} Preferences:")
	String propExaminationPreferences(String problem);
	
	@DefaultMessage("Room Sharing Note:")
	String propRoomSharingNote();
	
	@DefaultMessage("Global:")
	String propGlobalGroup();
	
	@DefaultMessage("Default:")
	String propDefaultGroup();
	
	@DefaultMessage("Type:")
	String propFeatureType();
	
	@DefaultMessage("Global:")
	String propGlobalFeature();
	
	@DefaultMessage("Applies To:")
	String propApplyToFutureSessions();

	@DefaultMessage("Type:")
	String propInstructorAttributeType();

	@DefaultMessage("Parent:")
	String propInstructorAttributeParent();

	@DefaultMessage("Global:")
	String propGlobalInstructorAttribute();
	
	@DefaultMessage("Name:")
	String propInstructorName();
	
	@DefaultMessage("Assigned Load:")
	String propAssignedLoad();
	
	@DefaultMessage("Attributes:")
	String propInstructorAttributes();
	
	@DefaultMessage("Course Preferences:")
	String propCoursePreferences();
	
	@DefaultMessage("Time Preferences:")
	String propTimePreferences();
	
	@DefaultMessage("Distribution Preferences:")
	String propDistributionPreferences();
	
	@DefaultMessage("Week:")
	String propTimeGridWeek();
	
	@DefaultMessage("Resource:")
	String propTimeGridResource();
	
	@DefaultMessage("Filter:")
	String propTimeGridFilter();
	
	@DefaultMessage("Class Filter:")
	String propTimeGridClassFilter();
	
	@DefaultMessage("Days:")
	String propTimeGridDays();
	
	@DefaultMessage("Times:")
	String propTimeGridTimes();
	
	@DefaultMessage("Display Mode:")
	String propTimeGridDisplayMode();
	
	@DefaultMessage("Background:")
	String propTimeGridBackground();
	
	@DefaultMessage("Show Discouraged Free Times:")
	String propTimeGridShowFreeTimes();
	
	@DefaultMessage("Show Preferences:")
	String propTimeGridShowPreferences();
	
	@DefaultMessage("Show Instructors:")
	String propTimeGridShowInstructors();
	
	@DefaultMessage("Show Events:")
	String propTimeGridShowEvents();
	
	@DefaultMessage("Show Times:")
	String propTimeGridShowTimes();
	
	@DefaultMessage("Show Course Titles:")
	String propTimeGridShowCourseTitles();
	
	@DefaultMessage("Order By:")
	String propTimeGridOrderBy();
	
	@DefaultMessage("Time:")
	String propTimeGridTime();
	
	@DefaultMessage("Date:")
	String propTimeGridDate();
	
	@DefaultMessage("Room:")
	String propTimeGridLocation();
	
	@DefaultMessage("Instructor:")
	String propTimeGridInstructor();
	
	@DefaultMessage("Student Conflicts:")
	String propTimeGridStudentConflicts();
	
	@DefaultMessage("{0} [committed: {1}, distance: {2}, hard: {3}]")
	String formatStudentConflicts(String total, String committed, String distance, String hard);
	
	@DefaultMessage("Room Preferences:")
	String propTimeGridRoomPreferences();
	
	@DefaultMessage("Time Preferences:")
	String propTimeGridTimePreferences();
	
	@DefaultMessage("Distribution Preferences:")
	String propTimeGridDistributionPreferences();
	
	@DefaultMessage("Initial Assignment:")
	String propTimeGridInitialAssignment();
	
	@DefaultMessage("Perturbation Penalty:")
	String propTimeGridPerturbationPenalty();
	
	@DefaultMessage("Department Balance:")
	String propTimeGridDepartmentBalance();
	
	@DefaultMessage("Non-Conflicting Placements:")
	String propTimeGridNonConflictingPlacements();
	
	@DefaultMessage("Simplified Mode:")
	String propCourseTimetablingSolverSimplifiedMode();
	
	@DefaultMessage("Available Services:")
	String propAvailableServices();
	
	@DefaultMessage("Created:")
	String propSolutionCreated();
	
	@DefaultMessage("Owner:")
	String propSolutionOwner();
	
	@DefaultMessage("Committed:")
	String propSolutionCommitted();
	
	@DefaultMessage("Note:")
	String propSolutionNote();
	
	@DefaultMessage("Problems:")
	String propSolutionLog();
	
	@DefaultMessage("{0} ({1})")
	String label(String name, String type);

	@DefaultMessage("{0}")
	String capacity(String capacity);

	@DefaultMessage("{0} ({1} for examinations)")
	String capacityWithExam(String capacity, String examCapacity);

	@DefaultMessage("{0} ({1} for {2} examinations)")
	String capacityWithExamType(String capacity, String examCapacity, String examType);
	
	@DefaultMessage("Features")
	String roomFeatures();
	
	@DefaultMessage("No Event Department")
	String noEventDepartment();
	
	@DefaultMessage("No Room Check")
	String ignoreRoomCheck();
	
	@DefaultMessage("{0} m")
	String roomDistance(String distanceInMeters);
	
	@DefaultMessage("{0} minutes")
	String breakTime(String breakTimeInMinutes);
	
	@DefaultMessage("Insert a new row above this row.")
	String titleInsertRowAbove();
	
	@DefaultMessage("Delete this row.")
	String titleDeleteRow();
	
	@DefaultMessage("Add another row.")
	String titleAddRow();
	
	@DefaultMessage("External Id")
	String fieldExternalId();
	
	@DefaultMessage("Abbreviation")
	String fieldAbbreviation();
	
	@DefaultMessage("Abbv")
	String fieldAbbv();
	
	@DefaultMessage("Short Title")
	String fieldShortTitle();
	
	@DefaultMessage("Long Title")
	String fieldLongTitle();
	
	@DefaultMessage("Title")
	String fieldTitle();

	@DefaultMessage("Code")
	String fieldCode();

	@DefaultMessage("Name")
	String fieldName();

	@DefaultMessage("Reference")
	String fieldReference();
	
	@DefaultMessage("Note")
	String fieldNote();

	@DefaultMessage("Class Date")
	String fieldClassDate();

	@DefaultMessage("Event Date")
	String fieldEventDate();
	
	@DefaultMessage("Room Type")
	String fieldRoomType();
	
	@DefaultMessage("Room")
	String fieldRoom();
	
	@DefaultMessage("Event Status")
	String fieldEventStatus();
	
	@DefaultMessage("Event Email")
	String fieldEventEmail();
	
	@DefaultMessage("Room Note")
	String fieldRoomNote();
	
	@DefaultMessage("Break Time")
	String fieldBreakTime();
	
	@DefaultMessage("Sort Order")
	String fieldSortOrder();
	
	@DefaultMessage("Type")
	String fieldType();
	
	@DefaultMessage("Department")
	String fieldDepartment();
	
	@DefaultMessage("Instructor")
	String fieldInstructor();

	@DefaultMessage("Role")
	String fieldRole();
	
	@DefaultMessage("Logger")
	String fieldLogger();
	
	@DefaultMessage("Level")
	String fieldLevel();
	
	@DefaultMessage("Academic Area")
	String fieldAcademicArea();
	
	@DefaultMessage("Event Management")
	String fieldEventManagement();
	
	@DefaultMessage("Applies To")
	String fieldAppliesTo();
	
	@DefaultMessage("Students")
	String fieldStudents();
	
	@DefaultMessage("Message")
	String fieldMessage();
	
	@DefaultMessage("Enabled")
	String fieldEnabled();
	
	@DefaultMessage("Instructional Offering")
	String fieldInstructionalOffering();
	
	@DefaultMessage("Reservation Type")
	String fieldReservationType();
	
	@DefaultMessage("Reserved Space")
	String fieldReservedSpace();
	
	@DefaultMessage("Expiration Date")
	String fieldExpirationDate();
	
	@DefaultMessage("Start Date")
	String fieldStartDate();
	
	@DefaultMessage("Additional Emails")
	String fieldAdditionalEmails();
	
	@DefaultMessage("Session")
	String fieldSession();

	@DefaultMessage("Database")
	String fieldDatabase();

	@DefaultMessage("Status")
	String fieldStatus();

	@DefaultMessage("Manager")
	String fieldManager();
	
	@DefaultMessage("Solver")
	String fieldSolver();
	
	@DefaultMessage("Phase")
	String fieldPhase();

	@DefaultMessage("Progress")
	String fieldProgress();

	@DefaultMessage("Owner")
	String fieldOwner();

	@DefaultMessage("Host")
	String fieldHost();

	@DefaultMessage("Version")
	String fieldVersion();
	
	@DefaultMessage("Implementation")
	String fieldImplementation();
	
	@DefaultMessage("Parameters")
	String fieldParameters();
	
	@DefaultMessage("Visible")
	String fieldVisible();
	
	@DefaultMessage("Highlight In Events")
	String fieldHighlightInEvents();
	
	@DefaultMessage("Conjunctive")
	String fieldConjunctive();

	@DefaultMessage("Required Attribute")
	String fieldRequiredAttribute();
	
	@DefaultMessage("Coordinator")
	String fieldCoordinator();
	
	@DefaultMessage("Course")
	String fieldCourse();
	
	@DefaultMessage("Auxiliary (No Report)")
	String fieldAuxiliaryNoReport();
	
	@DefaultMessage("No Export")
	String fieldNoExport();
	
	@DefaultMessage("Hide in Events")
	String fieldHideInEvents();
	
	@DefaultMessage("Expected Size")
	String fieldExpectedSize();
	
	@DefaultMessage("Default")
	String fieldDefault();
	
	@DefaultMessage("Email")
	String fieldEmailAddress();
	
	@DefaultMessage("All Rooms")
	String fieldAllRooms();
	
	@DefaultMessage("First Name")
	String fieldFirstName();
	
	@DefaultMessage("Middle Name")
	String fieldMiddleName();
	
	@DefaultMessage("Last Name")
	String fieldLastName();
	
	@DefaultMessage("Title")
	String fieldAcademicTitle();
	
	@DefaultMessage("Keep Students Together")
	String fieldKeepTogether();
	
	@DefaultMessage("Disabled Sections")
	String fieldAllowDisabledSections();
	
	@DefaultMessage("Advisors Can Set")
	String fieldAdvisorsCanSet();
	
	@DefaultMessage("Start Date")
	String fieldStudentStatusEffectiveStartDate();
	
	@DefaultMessage("Start Time")
	String fieldStudentStatusEffectiveStartTime();
	
	@DefaultMessage("End Date")
	String fieldStudentStatusEffectiveEndDate();
	
	@DefaultMessage("End Time")
	String fieldStudentStatusEffectiveEndTime();
	
	@DefaultMessage("Fallback Status")
	String fieldStudentStatusFallback();
	
	@DefaultMessage("Restricted")
	String fieldHasRole();
	
	@DefaultMessage("Major")
	String fieldMajor();
	
	@DefaultMessage("Not Allowed")
	String itemAllowDisabledSectionsNotAllowed();
	
	@DefaultMessage("Allowed With Group Reservation")
	String itemAllowDisabledSectionsAllowedReservation();
	
	@DefaultMessage("Always Allowed")
	String itemAllowDisabledSectionsAlwaysAllowed();
	
	@DefaultMessage("Type")
	String fieldStudentGroupType();
	
	@DefaultMessage("Assistant")
	String toggleAccess();
	
	@DefaultMessage("Student Register")
	String toggleRegistration();
		
	@DefaultMessage("Student Enroll")
	String toggleEnrollment();
	
	@DefaultMessage("Advisor Enroll")
	String toggleAdvisor();
	
	@DefaultMessage("Admin Enroll")
	String toggleAdmin();
	
	@DefaultMessage("Email")
	String toggleEmail();
	
	@DefaultMessage("Wait-Listing")
	String toggleWaitList();
	
	@DefaultMessage("No-Subs")
	String toggleNoSubs();
	
	@DefaultMessage("No Batch")
	String toggleNoBatch();
	
	@DefaultMessage("Registration")
	String toggleRegAccess();
	
	@DefaultMessage("Advisor Register")
	String toggleRegAdvisor();
	
	@DefaultMessage("Admin Register")
	String toggleRegAdmin();
	
	@DefaultMessage("Advisor Can Set Status")
	String toggleAdvisorCanSetStatus();
	
	@DefaultMessage("Course Request Validation")
	String toggleCourseRequestValidation();
	
	@DefaultMessage("Approval Requests")
	String toggleSpecialRequests();
	
	@DefaultMessage("Can Require")
	String toggleCanRequire();
	
	@DefaultMessage("No Personal Schedule")
	String toggleNoSchedule();
	
	@DefaultMessage("Final Examinations")
	String finalExaminations();
	
	@DefaultMessage("Midterm Examinations")
	String midtermExaminations();
	
	@DefaultMessage("No Role")
	String noRole();
	
	@DefaultMessage("All")
	String levelAll();
	
	@DefaultMessage("Trace")
	String levelTrace();
	
	@DefaultMessage("Debug")
	String levelDebug();
	
	@DefaultMessage("Info")
	String levelInfo();
	
	@DefaultMessage("Warning")
	String levelWarning();
	
	@DefaultMessage("Error")
	String levelError();
	
	@DefaultMessage("Fatal")
	String levelFatal();
	
	@DefaultMessage("Off")
	String levelOff();
	
	@DefaultMessage("Global")
	String levelGlobal();
	
	@DefaultMessage("Logging level for {0}.")
	String descriptionLoggingLevelFor(String logger);
	
	@DefaultMessage("Last")
	String abbvLastLikeEnrollment();
	
	@DefaultMessage("Last-Like")
	String shortLastLikeEnrollment();
	
	@DefaultMessage("Last-Like Enrollment")
	String fieldLastLikeEnrollment();
	
	@DefaultMessage("Proj")
	String abbvProjectedByRule();
	
	@DefaultMessage("Projected")
	String shortProjectedByRule();
	
	@DefaultMessage("Projected by Rule")
	String fieldProjectedByRule();
	
	@DefaultMessage("Curr")
	String abbvCurrentEnrollment();
	
	@DefaultMessage("Current")
	String shortCurrentEnrollment();
	
	@DefaultMessage("Current Enrollment")
	String fieldCurrentEnrollment();
	
	@DefaultMessage("Reg")
	String abbvCourseRequests();
	
	@DefaultMessage("Registration")
	String shortCourseRequests();

	@DefaultMessage("Course Registrations")
	String fieldCourseRequests();
	
	@DefaultMessage("Req")
	String abbvRequestedEnrollment();
	
	@DefaultMessage("Requested")
	String shortRequestedEnrollment();
	
	@DefaultMessage("Requested Enrollment")
	String fieldRequestedEnrollment();
	
	@DefaultMessage("Curricula")
	String headerCurricula();
	
	@DefaultMessage("Curriculum Details")
	String headerCurriculumDetails();
	
	@DefaultMessage("Curriculum Classifications")
	String headerCurriculumClassifications();
	
	@DefaultMessage("Course Projections")
	String headerCourseProjections();
	
	@DefaultMessage("Room Sharing")
	String headerRoomSharing();
	
	@DefaultMessage("Examination Period Preferences")
	String headerExaminationPeriodPreferences();
	
	@DefaultMessage("Event Availability")
	String headerEventAvailability();
	
	@DefaultMessage("Room Pictures")
	String headerRoomPictures();
	
	@DefaultMessage("Room Features")
	String headerRoomFeatures();
	
	@DefaultMessage("Room Groups")
	String headerRoomGroups();
	
	@DefaultMessage("Update Future Academic Sessions")
	String headerRoomApplyToFutureRooms();
	
	@DefaultMessage("Global Room Groups")
	String headerGlobalRoomGroups();
	
	@DefaultMessage("Departmental Room Groups")
	String headerDepartmentalRoomGroups();
	
	@DefaultMessage("Global Room Features")
	String headerGlobalRoomFeatures();
	
	@DefaultMessage("Departmental Room Features")
	String headerDepartmentalRoomFeatures();
	
	@DefaultMessage("Rooms")
	String headerRooms();
	
	@DefaultMessage("Global Instructor Attributes")
	String headerGlobalInstructorAttributes();
	
	@DefaultMessage("Departmental Instructor Attributes")
	String headerDepartmentalInstructorAttributes();
	
	@DefaultMessage("Instructors")
	String headerInstructors();
	
	@DefaultMessage("Teaching Request")
	String headerTeachingRequest();
	
	@DefaultMessage("Available Instructors")
	String headerAvailableInstructors();
	
	@DefaultMessage("Suggestions")
	String headerSuggestions();
	
	@DefaultMessage("Selected Assignment")
	String headerSelectedAssignment();
	
	@DefaultMessage("Conflicting Assignments")
	String headerConflictingAssignments();
	
	@DefaultMessage("Instructor")
	String headerInstructor();
	
	@DefaultMessage("Available Assignments")
	String headerAvailableAssignments();
	
	@DefaultMessage("Current Assignment of {0}")
	String headerCurrentAssignment(String className);
	
	@DefaultMessage("Conflicts")
	String headerConflicts();
	
	@DefaultMessage("Conflict Statistics")
	String headerCBS();
	
	@DefaultMessage("The selected offering has no curricula.")
	String offeringHasNoCurricula();
	
	@DefaultMessage("Comparing {0} students with the other selected courses:")
	String hintComparingStudentsWithOtherCourses(String students);
	
	@DefaultMessage("Students in at least 1 other course")
	String hintStudentsInOneOtherCourse();
	
	@DefaultMessage("Students in at least 2 other courses")
	String hintStudentsInTwoOtherCourses();
	
	@DefaultMessage("Students in at least 3 other courses")
	String hintStudentsInThreeOtherCourses();
	
	@DefaultMessage("Students in all other courses")
	String hintStudentsInAllOtherCourses();
	
	@DefaultMessage("Students not in any other course")
	String hintStudentsNotInAnyOtherCourse();
	
	@DefaultMessage("Students shared with {0}")
	String hinStudentsSharedWith(String course);
	
	@DefaultMessage("No conflict (different students)")
	String groupDifferentStudents();
	
	@DefaultMessage("Conflict (same students)")
	String groupSameStudents();

	@DefaultMessage("Do you realy want to delete the selected curriculum?")
	String confirmDeleteSelectedCurriculum();
	
	@DefaultMessage("Do you realy want to delete this curriculum?")
	String confirmDeleteThisCurriculum();
	
	@DefaultMessage("Do you realy want to delete the selected curricula?")
	String confirmDeleteSelectedCurricula();
	
	@DefaultMessage("Do you realy want to merge the selected curriculum?")
	String confirmMergeSelectedCurriculum();
	
	@DefaultMessage("Do you realy want to merge the selected curricula?")
	String confirmMergeSelectedCurricula();
	
	@DefaultMessage("Do you realy want to update all curricula?")
	String confirmUpdateAllCurricula();
	
	@DefaultMessage("Do you realy want to update all your curricula?")
	String confirmUpdateYourCurricula();
	
	@DefaultMessage("Do you realy want to update selected curricula?")
	String confirmUpdateSelectedCurriculum();
	
	@DefaultMessage("Do you realy want to update selected curriculum?")
	String confirmUpdateSelectedCurricula();
	
	@DefaultMessage("Do you really want to populate projected demands for all courses?")
	String confirmPopulateProjectedDemands();
	
	@DefaultMessage("This will delete all existing curricula and create them from scratch. Are you sure you want to do it?")
	String confirmDeleteAllCurricula();
	
	@DefaultMessage("Are you REALLY sure you want to recreate all curricula?")
	String confirmDeleteAllCurriculaSecondWarning();
	
	@DefaultMessage("This event including ALL its meetings will be deleted. Are you sure you want to do this?\n\nTo delete just a particular meeting, select the meeting and click on the Remove option under the More button. Do not forget to click the Update Event afterwards.")
	String confirmDeleteEvent();
	
	@DefaultMessage("ALL pending or approved meetings of this event will be cancelled. Are you sure you want to do this?\n\nTo cancel just a particular meeting, select the meeting and click on the Cancel option under the More button. Do not forget to click the Update Event afterwards.")
	String confirmCancelEvent();

	@DefaultMessage("Do you really want to execute script {0}?")
	String confirmScriptExecution(String name);
	
	@DefaultMessage("Do you realy want to delete this room?")
	String confirmDeleteRoom();
		
	@DefaultMessage("Do you really want to create the room in {0} as well?")
	String confirmCreateRoomInFutureSessions(String futureSessions);
	
	@DefaultMessage("Do you really want to update the room in {0} as well?")
	String confirmUpdateRoomInFutureSessions(String futureSessions);
	
	@DefaultMessage("Do you realy want to delete this room (including {0} as well)?")
	String confirmDeleteRoomInFutureSessions(String futureSessions);
	
	@DefaultMessage("Do you realy want to delete this room group?")
	String confirmDeleteRoomGroup();
	
	@DefaultMessage("Do you realy want to delete this room feature?")
	String confirmDeleteRoomFeature();
	
	@DefaultMessage("Do you really want to create the room feature in {0} as well?")
	String confirmCreateRoomFeatureInFutureSessions(String futureSessions);
	
	@DefaultMessage("Do you really want to update the room feature in {0} as well?")
	String confirmUpdateRoomFeatureInFutureSessions(String futureSessions);
	
	@DefaultMessage("Do you realy want to delete this room feature (including {0} as well)?")
	String confirmDeleteRoomFeatureInFutureSessions(String futureSessions);
	
	@DefaultMessage("Do you really want to create the room group in {0} as well?")
	String confirmCreateRoomGroupInFutureSessions(String futureSessions);
	
	@DefaultMessage("Do you really want to update the room group in {0} as well?")
	String confirmUpdateRoomGroupInFutureSessions(String futureSessions);
	
	@DefaultMessage("Do you realy want to delete this room group (including {0} as well)?")
	String confirmDeleteRoomGroupInFutureSessions(String futureSessions);
	
	@DefaultMessage("Do you realy want to delete this reservation?")
	String confirmDeleteReservation();
	
	@DefaultMessage("Do you really want to unload your current timetable? You may lose this timetable if you did not save it.")
	String confirmSolverUnload();
	
	@DefaultMessage("Do you really want to unload your current student schedule? You may lose this student schedule if you did not save it.")
	String confirmStudentSolverUnload();
	
	@DefaultMessage("Do you really want to clear your current timetable? You may lose this timetable if you did not save it.")
	String confirmSolverClear();
	
	@DefaultMessage("Do you really want to clear your current student schedule? You may lose this student schedule if you did not save it.")
	String confirmStudentSolverClear();
	
	@DefaultMessage("Do you really want to save your current timetable?")
	String confirmSolverSaveAsNew();
	
	@DefaultMessage("Do you really want to save your current student schedule?")
	String confirmStudentSolverSaveAsNew();
	
	@DefaultMessage("Do you really want to save your current timetable? This will overwrite your previous solution.")
	String confirmSolverSave();
	
	@DefaultMessage("Do you really want to save your current student schedule? This will overwrite your previous student schedule.")
	String confirmStudentSolverSave();
	
	@DefaultMessage("Do you really want to save and commit your current timetable? This will overwrite your previous solution. It may also uncommit your currently committed solution.")
	String confirmSolverSaveCommit();
	
	@DefaultMessage("Do you really want to save and uncommit your current timetable? This will uncommit and overwrite your previous solution.")
	String confirmSolverSaveUncommit();
	
	@DefaultMessage("Do you really want to save and commit your current timetable? This may uncommit your currently committed solution.")
	String confirmSolverSaveAsNewCommit();
	
	@DefaultMessage("Do you really want to uncommit this solution?")
	String confirmSolverUncommit();
	
	@DefaultMessage("Do you really want to commit this solution?")
	String confirmSolverCommit();
	
	@DefaultMessage("Do you really want to delete this solution?")
	String confirmSolverDelete();
	
	@DefaultMessage("The selected assignment will be done directly in the database. Are you sure?")
	String confirmInstructorAssignmentChangesNoSolver();
	
	@DefaultMessage("Do you realy want to delete this task?")
	String confirmDeleteTask();
	
	@DefaultMessage("&nbsp;(of&nbsp;{0})")
	String curriculumProjectionRulesOfTotal(int total);
	
	@DefaultMessage("{0}&nbsp;&rarr;&nbsp;")
	String curriculumProjectionRulesOldValue(int lastLike);
	
	@DefaultMessage("Appearance: Courses")
	String flagAppearanceCourses();
	
	@DefaultMessage("Appearance: Examinations")
	String flagAppearanceExaminations();
	
	@DefaultMessage("Appearance: Student Scheduling")
	String flagAppearanceStudentSectioning();
	
	@DefaultMessage("Appearance: Events")
	String flagAppearanceEvents();
	
	@DefaultMessage("Appearance: Administration")
	String flagAppearanceAdministration();
	
	@DefaultMessage("Restrictions: Administrator Only")
	String flagRestrictionsAdministratorOnly();
	
	@DefaultMessage("Academic Session")
	String optionAcademicSession();
	
	@DefaultMessage("Department")
	String optionDepartment();
	
	@DefaultMessage("Departments")
	String optionDepartments();
	
	@DefaultMessage("Subject Area")
	String optionSubjectArea();
	
	@DefaultMessage("Subject Areas")
	String optionSubjectAreas();
	
	@DefaultMessage("Building")
	String optionBuilding();
	
	@DefaultMessage("Buildings")
	String optionBuildings();
	
	@DefaultMessage("Room")
	String optionRoom();
	
	@DefaultMessage("Rooms")
	String optionRooms();
	
	@DefaultMessage("Point In Time Data")
	String optionPointInTimeData();

	@DefaultMessage("Point In Time Data Comparison")
	String optionPointInTimeDataComparison();

	@DefaultMessage("Minutes in Reporting Hour")
	String optionMinutesInReportingHour();

	@DefaultMessage("Weeks in Reporting Term")
	String optionWeeksInReportingTerm();

	@DefaultMessage("Minimum Location Capacity")
	String optionMinimumLocationCapacity();

	@DefaultMessage("Maximum Location Capacity")
	String optionMaximumLocationCapacity();

	@DefaultMessage("Distribution Type")
	String optionDistributionType();

	@DefaultMessage("Distribution Types")
	String optionDistributionTypes();

	@DefaultMessage("Demand Offering Type")
	String optionDemandOfferingType();

	@DefaultMessage("Demand Offering Types")
	String optionDemandOfferingTypes();

	@DefaultMessage("Offering Consent Type")
	String optionOfferingConsentType();

	@DefaultMessage("Offering Consent Types")
	String optionOfferingConsentTypes();

	@DefaultMessage("Course Credit Format")
	String optionCourseCreditFormat();

	@DefaultMessage("Course Credit Formats")
	String optionCourseCreditFormats();

	@DefaultMessage("Course Credit Type")
	String optionCourseCreditType();

	@DefaultMessage("Course Credit Types")
	String optionCourseCreditTypes();

	@DefaultMessage("Course Credit Unit Type")
	String optionCourseCreditUnitType();

	@DefaultMessage("Course Credit Unit Types")
	String optionCourseCreditUnitTypes();

	@DefaultMessage("Position Type")
	String optionPositionType();

	@DefaultMessage("Position Types")
	String optionPositionTypes();

	@DefaultMessage("Department Status")
	String optionDepartmentStatus();
	
	@DefaultMessage("Department Status Type")
	String optionDepartmentStatusType();

	@DefaultMessage("Department Status Types")
	String optionDepartmentStatusTypes();

	@DefaultMessage("Room Type")
	String optionRoomType();

	@DefaultMessage("Room Types")
	String optionRoomTypes();

	@DefaultMessage("Student Scheduling Status")
	String optionStudentSectioningStatus();

	@DefaultMessage("Student Scheduling Statuses")
	String optionStudentSectioningStatuses();

	@DefaultMessage("Exam Type")
	String optionExamType();

	@DefaultMessage("Exam Types")
	String optionExamTypes();

	@DefaultMessage("Room Feature Type")
	String optionRoomFeatureType();

	@DefaultMessage("Room Feature Types")
	String optionRoomFeatureTypes();

	@DefaultMessage("Course Type")
	String optionCourseType();

	@DefaultMessage("Course Types")
	String optionCourseTypes();
	
	@DefaultMessage("Saved Successfully")
	String fieldSavedSuccessfully();

	@DefaultMessage("Snapshot Timestamp")
	String fieldSnapshotTimestamp();
	
	@DefaultMessage("Individual Reservation")
	String reservationIndividual();
	
	@DefaultMessage("Individual Override")
	String reservationIndividualOverride();
	
	@DefaultMessage("Student Group Reservation")
	String reservationStudentGroup();
	
	@DefaultMessage("Student Group Override")
	String reservationStudentGroupOverride();
	
	@DefaultMessage("Curriculum Reservation")
	String reservationCurriculum();
	
	@DefaultMessage("Curriculum Override")
	String reservationCurriculumOverride();
	
	@DefaultMessage("Course Reservation")
	String reservationCourse();
	
	@DefaultMessage("Learning Community Reservation")
	String reservationLearningCommunity();
	
	@DefaultMessage("Student Filter")
	String reservationUniversalOverride();
	
	@DefaultMessage("Individual")
	String reservationIndividualAbbv();
	
	@DefaultMessage("Student Group")
	String reservationStudentGroupAbbv();
	
	@DefaultMessage("Individual Override")
	String reservationIndividualOverrideAbbv();
	
	@DefaultMessage("Student Group Override")
	String reservationStudentGroupOverrideAbbv();
	
	@DefaultMessage("Curriculum")
	String reservationCurriculumAbbv();
	
	@DefaultMessage("Course")
	String reservationCourseAbbv();
	
	@DefaultMessage("Override")
	String reservationOverrideAbbv();
	
	@DefaultMessage("Filter")
	String reservationUniversalAbbv();
	
	@DefaultMessage("Student Filter Override")
	String reservationUniversalOverrideAbbv();
	
	@DefaultMessage("Unknown")
	String reservationUnknownAbbv();
	
	@DefaultMessage("Learning Community")
	String reservationLearningCommunityAbbv();
	
	@DefaultMessage("Total Reserved Space")
	String totalReservedSpace();
	
	@DefaultMessage("&infin;")
	String infinity();
	
	@DefaultMessage("{0} enrolled")
	String eventGridEnrolled(int enrolled);
	
	@DefaultMessage("{0} limit")
	String eventGridLimit(int limit);
	
	@DefaultMessage("{1} of {0}")
	String addMeetingsLimitAndType(int limit, String type);
	
	@DefaultMessage("{2} {1} / {0}")
	String addMeetingsLimitEnrollmentAndType(int limit, int enrollment, String type);
	
	@DefaultMessage("{0} of {1}")
	String enrollmentOfLimit(int enrollment, int limit);
	
	@DefaultMessage("Event expired, all pending meetings have been cancelled.")
	String noteEventExpired();
	
	@DefaultMessage("Clear filter")
	String altClearFilter();
	
	@DefaultMessage("Open filter")
	String altOpenFilter();
	
	@DefaultMessage("Close filter")
	String altCloseFilter();
	
	@DefaultMessage("Click here to save the current filter value as default for this page.")
	String altStarFilter();
	
	@DefaultMessage("The current filter value is the default.")
	String altStarFilterSelected();
	
	@DefaultMessage("Clear course request")
	String altClearCourseRequest();
	
	@DefaultMessage("Password")
	@DoNotTranslate
	String pageChangePassword();
	
	@DefaultMessage("Change Password")
	String headerChangePassword();
	
	@DefaultMessage("Request Password Change")
	String headerResetPassword();
	
	@DefaultMessage("<u>C</u>hange")
	String buttonChange();
	
	@DefaultMessage("<u>R</u>equest")
	String buttonReset();
	
	@DefaultMessage("Username:")
	String fieldUsername();
	
	@DefaultMessage("Email:")
	String fieldEmail();
	
	@DefaultMessage("Old Password:")
	String fieldOldPassword();
	
	@DefaultMessage("New Password:")
	String fieldNewPassword();
	
	@DefaultMessage("Retype Password:")
	String fieldRetypePassword();
	
	@DefaultMessage("The password was successfully changed.")
	String messagePasswordChanged();
	
	@DefaultMessage("A password change request was generated and sent to the user.")
	String messagePasswordReset();
	
	@DefaultMessage("No teaching requests defined for {0}.")
	String messageNoTeachingRequests(String offering);
	
	@DefaultMessage("Authentication failed: username and / or password is wrong.")
	String errorBadCredentials();
	
	@DefaultMessage("The password change request has already expired.")
	String errorPasswordResetExpired();
	
	@DefaultMessage("There is no user with the given email.")
	String errorEmailNotValid();
	
	@DefaultMessage("No matching user was found.")
	String errorNoMatchingUser();
	
	@DefaultMessage("The old password is not valid.")
	String errorOldPasswordNotValid();
	
	@DefaultMessage("The password cannot be empty.")
	String errorEnterNewPassword();
	
	@DefaultMessage("The two passwords do not match.")
	String errorNewPasswordMismatch();
	
	@DefaultMessage("Failed to change password: {0}")
	String failedToChangePassword(String reason);
	
	@DefaultMessage("Failed to request password change: {0}")
	String failedToResetPassword(String reason);
	
	@DefaultMessage("UniTime password change")
	String emailPasswordChange();
	
	@DefaultMessage("Generating password change request...")
	String waitPasswordReset();
	
	@DefaultMessage("Clearing hiberante cache...")
	String waitClearHiberanteCache();
	
	@DefaultMessage("Hibernate cache cleared.")
	String hibernateCacheCleared();
	
	@DefaultMessage("Failed to clear hibernate cache: {0}")
	String failedToClearHiberanteCache(String reason);
	
	@DefaultMessage("&nbsp;&nbsp;&nbsp;\u2307")
	String repeatingSymbol();
	
	@DefaultMessage("Bad additional email address {0}: {1}.")
	String badEmailAddress(String address, String reason);
	
	@DefaultMessage("Instructor")
	String eventContactInstructorPhone();
	
	@DefaultMessage("Coordinator")
	String eventContactCoordinatorPhone();
	
	@DefaultMessage("{0} Examinations for {1}")
	String dialogExaminations(String type, String student);
	
	@DefaultMessage("View")
	String courseCatalogLink();
	
	@DefaultMessage("Details of {0}")
	String courseCatalogDialog(String course);
	
	@DefaultMessage("Student Scheduling Solver")
	String solverStudent();
	
	@DefaultMessage("Examinations Solver")
	String solverExamination();
	
	@DefaultMessage("Course Timetabling Solver")
	String solverCourse();
	
	@DefaultMessage("Instructor Scheduling Solver")
	String solverInstructor();
	
	@DefaultMessage("Navigation")
	String navigation();
	
	@DefaultMessage("Click here to change the session / role.")
	String hintClickToChangeSession();
	
	@DefaultMessage("Page generated in {0} sec.")
	String pageGeneratedIn(String seconds);
	
	@DefaultMessage("If the pop-up window was blocked, you can follow this link to retrieve the exported file.")
	String pageBlockedPopup();
	
	@DefaultMessage("<u>D</u>ownload")
	String buttonDownload();
	
	@DefaultMessage("You can subscribe to the following iCalendar URL in your favorite calendar application. It will always return your up-to-date schedule.")
	String exportICalendarDescriptionPersonal();
	
	@DefaultMessage("You can subscribe to the following iCalendar URL in your favorite calendar application. It will always return an up-to-date calendar for {0} and your current filter selection.")
	String exportICalendarDescriptionOther(String term);
	
	@DefaultMessage("Or you can download an iCalendar file by clicking the {0} button below. While it is often easier to import an iCalendar file, such a&nbsp;calendar will not get updated automatically.")
	String exportICalendarDownload(String button);
	
	@DefaultMessage("Session Default")
	String examStatusDefault();
	
	@DefaultMessage("<i>{0} -- not approved</i>")
	String gridEventHeaderNotApproved(String header);
	
	@DefaultMessage("{0,number,#,##0.##}")
	String roomArea(Double area);
	
	@DefaultMessage("{0,number,#0.0###}, {1,number,#0.0###}")
	String coordinates(Double x, Double y);
	
	@DefaultMessage("{0,number,#0.0###}, {1,number,#0.0###}&nbsp;&nbsp;&nbsp;<i>{2}</i>")
	String coordinatesWithEllipsoid(Double x, Double y, String ellipsoid);
	
	@DefaultMessage("{0} Examination Rooms")
	String examinationRooms(String examTypeName);
	
	@DefaultMessage("No")
	String exportFalse();
	
	@DefaultMessage("Yes")
	String exportTrue();
	
	@DefaultMessage("Default")
	String exportDefaultRoomGroup();
	
	@DefaultMessage("Global")
	String exportGlobalRoomGroup();
	
	@DefaultMessage("Location of {0}.")
	String titleRoomMap(String roomLabel);
	
	@DefaultMessage("Default break time is used when left empty.")
	String useDefaultBreakTimeWhenEmpty();
	
	@DefaultMessage("{0}, {1}")
	String itemSeparatorMiddle(String list, String item);
	
	@DefaultMessage("{0}, and {1}")
	String itemSeparatorLast(String list, String item);
	
	@DefaultMessage("{0} and {1}")
	String itemSeparatorPair(String firstItem, String lastItem);
	
	@DefaultMessage("Image")
	String attachmentFlagIsImage();
	
	@DefaultMessage("Room Picture Type")
	String attachmentTypeFlagRoomPicture();
	
	@DefaultMessage("Show in Rooms Table")
	String attachmentTypeFlagShowRoomsTable();
	
	@DefaultMessage("Show in Room Tooltip")
	String attachmentTypeFlagShowRoomTooltip();
	
	@DefaultMessage("Distance conflicts will be checked.")
	String infoDistanceCheckOn();
	
	@DefaultMessage("Distance conflict checking is disabled.")
	String infoDistanceCheckOff();
	
	@DefaultMessage("Room conflicts are not allowed.")
	String infoRoomCheckOn();
	
	@DefaultMessage("Room conflict checking is disabled.")
	String infoRoomCheckOff();
	
	@DefaultMessage("link")
	String roomPictureLink();
	
	@DefaultMessage("Event status changes apply only to {0}.")
	String eventStatusHint(String session);
	
	@DefaultMessage("A student must have all of the selected majors.")
	String infoMultipleMajorsOn();
	
	@DefaultMessage("A student must have at least one of the selected majors.")
	String infoMultipleMajorsOff();
	
	@DefaultMessage("UniTime Schedule")
	String scheduleNameDefault();
	
	@DefaultMessage("{0} Schedule")
	String scheduleNameForResource(String resource);
	
	@DefaultMessage("{0} for {1} {2}")
	String scheduleNameForSession(String scheduleName, String acadTerm, String acadYear);
	
	@DefaultMessage("Common")
	String abbvCommonTemplate();
	
	@DefaultMessage("Defaults to {0} ({1})")
	String hintDefaultPercentShare(String defaultSharing, String template);
	
	@DefaultMessage("Taken from {0}")
	String hintTakenFromTemplate(String template);
	
	@DefaultMessage("Type in {0} to confirm the action.")
	String confirmationWrongAnswer(String answer);
	
	@DefaultMessage("department")
	String tagDepartment();
	
	@DefaultMessage("Managed")
	String attrDepartmentManagedRooms();
	
	@DefaultMessage("Managed Rooms")
	String labelDepartmentManagedRooms();
	
	@DefaultMessage("user")
	String tagUser();
	
	@DefaultMessage("type")
	String tagRoomType();
	
	@DefaultMessage("feature")
	String tagRoomFeature();
	
	@DefaultMessage("group")
	String tagRoomGroup();
	
	@DefaultMessage("room")
	String tagRoom();
	
	@DefaultMessage("building")
	String tagBuilding();
	
	@DefaultMessage("flag")
	String tagRoomFlag();
	
	@DefaultMessage("All")
	String attrFlagAllRooms();
	
	@DefaultMessage("Event")
	String attrFlagEventRooms();
	
	@DefaultMessage("Nearby")
	String attrFlagNearbyRooms();
	
	@DefaultMessage("size")
	String tagRoomSize();
	
	@DefaultMessage("starts")
	String tagStarts();
	
	@DefaultMessage("contains")
	String tagContains();
	
	@DefaultMessage("other")
	String tagOther();
	
	@DefaultMessage("limit")
	String tagLimit();
	
	@DefaultMessage("type")
	String tagEventType();
	
	@DefaultMessage("sponsor")
	String tagSponsor();
	
	@DefaultMessage("mode")
	String tagEventMode();
	
	@DefaultMessage("requested")
	String tagRequested();
	
	@DefaultMessage("flag")
	String tagEventFlag();
	
	@DefaultMessage("role")
	String tagEventRole();
	
	@DefaultMessage("date")
	String tagDate();
	
	@DefaultMessage("day")
	String tagDayOfWeek();

	@DefaultMessage("From")
	String tagDateFrom();

	@DefaultMessage("To")
	String tagDateTo();
	
	@DefaultMessage("After")
	String tagDateAfter();
	
	@DefaultMessage("Before")
	String tagDateBefore();
	
	@DefaultMessage("time")
	String tagTime();
	
	@DefaultMessage("After")
	String tagTimeAfter();
	
	@DefaultMessage("Before")
	String tagTimeBefore();
	
	@DefaultMessage("area")
	String tagAcademicArea();
	
	@DefaultMessage("major")
	String tagMajor();
	
	@DefaultMessage("concentration")
	String tagConcentration();
	
	@DefaultMessage("degree")
	String tagDegree();
	
	@DefaultMessage("program")
	String tagProgram();
	
	@DefaultMessage("campus")
	String tagCampus();
	
	@DefaultMessage("minor")
	String tagMinor();
	
	@DefaultMessage("classification")
	String tagClassification();
	
	@DefaultMessage("curriculum")
	String tagCurriculum();
	
	@DefaultMessage("mode")
	String tagReservationMode();
	
	@DefaultMessage("type")
	String tagReservationType();
	
	@DefaultMessage("override")
	String tagReservationOverride();
	
	@DefaultMessage("subject")
	String tagSubjectArea();
	
	@DefaultMessage("group")
	String tagStudentGroup();
	
	@DefaultMessage("accommodation")
	String tagStudentAccommodation();

	@DefaultMessage("student")
	String tagStudent();
	
	@DefaultMessage("mode")
	String tagSectioningMode();
	
	@DefaultMessage("type")
	String tagSectioningType();
	
	@DefaultMessage("status")
	String tagSectioningStatus();
	
	@DefaultMessage("approver")
	String tagApprover();
	
	@DefaultMessage("assignment")
	String tagSectioningAssignment();
	
	@DefaultMessage("consent")
	String tagSectioningConsent();
	
	@DefaultMessage("operation")
	String tagSectioningOperation();
	
	@DefaultMessage("course")
	String tagCourse();
	
	@DefaultMessage("instructor")
	String tagInstructor();
	
	@DefaultMessage("service")
	String tagService();
	
	@DefaultMessage("flag")
	String tagFlag();
	
	@DefaultMessage("mode")
	String tagMode();
	
	@DefaultMessage("depth")
	String tagSuggestionsDepth();
	
	@DefaultMessage("timeout")
	String tagSuggestionsTimeLimit();
	
	@DefaultMessage("results")
	String tagSuggestionsResults();
	
	@DefaultMessage("credit")
	String tagCredit();
	
	@DefaultMessage("overlap")
	String tagOverlap();
	
	@DefaultMessage("prefer")
	String tagPrefer();
	
	@DefaultMessage("require")
	String tagRequire();
	
	@DefaultMessage("instructional method")
	String tagInstructionalMethod();
	
	@DefaultMessage("lookup")
	String tagLookup();
	
	@DefaultMessage("advisor")
	String tagAdvisor();
	
	@DefaultMessage("All Sessions")
	String attrFlagAllSessions();
	
	@DefaultMessage("Conflicts")
	String attrFlagShowConflicts();
	
	@DefaultMessage("For an up to date version of the {0} event, please visit <a href=\"{1}/selectPrimaryRole.action?target=gwt.jsp%3Fpage%3Devents%23event%3D{2}%26term%3D{3}\" style='color: inherit; background-color : transparent;'>{1}</a>.")
	String emailOpenEventOnline(String eventName, String uniTimeUrl, Long eventId, String sessionId);
	
	@DefaultMessage("{0} ({1})")
	@DoNotTranslate
	String roomPreference(String department, String preference);

	@DefaultMessage("{1} {0}")
	@DoNotTranslate
	String roomPreferenceShort(String department, String preference);
	
	@DefaultMessage("Application of required preference will remove all not required preferences.")
	String warnPreferenceUseRequired();
	
	@DefaultMessage("Application of this preference will remove all required preferences.")
	String warnPreferenceUseNotRequired();
	
	@DefaultMessage("When multiple rooms are listed, please consult your instructor regarding your class or examination room location.")
	String warnMultiRoomClassOrExam();
	
	@DefaultMessage("Cancelled")
	String reservationCancelledClass();
	
	@DefaultMessage("All")
	String departmentsAllLabel();

	@DefaultMessage("All")
	String colAll();
	
	@DefaultMessage("All Departments")
	String departmentsAllTitle();
	
	@DefaultMessage("Solver not started.")
	String solverStatusNotStarted();
	
	@DefaultMessage("No instructor assigned.")
	String notAssignedInstructor();
	
	@DefaultMessage("No suggestions found.")
	String noSuggestions();
	
	@DefaultMessage("Not Assigned")
	String notAssigned();
	
	@DefaultMessage("No External Id")
	String noExternalId();
	
	@DefaultMessage("&rarr;")
	String assignmentArrow();
	
	@DefaultMessage("(all {0} possibilities up to {1} changes were considered, no suggestion found)")
	String suggestionsNoteNoTimeoutNoResults(int nrCombinations, int depth);
	
	@DefaultMessage("({0}s timeout reached, {1} possibilities up to {2} changes were considered, no suggestion found)")
	String suggestionsNoteTimeoutNoResults(int timeout, int nrCombinations, int depth);

	@DefaultMessage("(all {0} possibilities of up to {1} changes were considered, top {2} of {3} suggestions displayed)")
	String suggestionsNoteNoTimeoutNResults(int nrCombinations, int depth, int nrResults, int nrSolutions);
	
	@DefaultMessage("({0}s timeout reached, {1} possibilities of up to {2} changes were considered, top {3} of {4} suggestions displayed)")
	String suggestionsNoteTimeoutNResults(int timeout, int nrCombinations, int depth, int nrResults, int nrSolutions);
	
	@DefaultMessage("(all {0} possibilities of up to {1} changes were considered, {2} suggestions displayed)")
	String suggestionsNoteNoTimeoutAllResults(int nrCombinations, int depth, int nrResults);
	
	@DefaultMessage("({0}s timeout reached, {1} possibilities of up to {2} changes were considered, {3} suggestions displayed)")
	String suggestionsNoteTimeoutAllResults(int timeout, int nrCombinations, int depth, int nrResults);
	
	@DefaultMessage("(best {0} available instructor assignments out of {1} displayed)")
	String domainNinstructors(int nrResults, int domainSize);
	
	@DefaultMessage("(best {0} available teaching assignments out of {1} displayed)")
	String domainNassignments(int nrResults, int domainSize);
	
	@DefaultMessage("Student")
	String enrollmentRoleStudent();
	
	@DefaultMessage("Instructor")
	String enrollmentRoleInstructor();
	
	@DefaultMessage("No Class Assignments")
	String teachingRequestNoSubpart();
	
	@DefaultMessage("")
	String noTeachingResponsiblitySelected();
	
	@DefaultMessage("{0} (1 parent class)")
	String subpartNameParent(String subpart);
	
	@DefaultMessage("{0} (1 child class)")
	String subpartNameOneChildClass(String subpart);
	
	@DefaultMessage("{0} ({1} children classes)")
	String subpartNameChildrenClases(String subpart, int children);
	
	@DefaultMessage("{0} ({1} - {2} children classes)")
	String subpartNameChildrenClasesRange(String subpart, int min, int max);
	
	@DefaultMessage("{0} (1 class)")
	String subpartNameNoRelationSingleClass(String subpart);
	
	@DefaultMessage("{0} ({1} classes)")
	String subpartNameNoRelationClasses(String subpart, int classes);
	
	@DefaultMessage("\u2630")
	@DoNotTranslate
	String mobileMenuSymbol();
	
	@DefaultMessage("Please wait ...")
	String waitPlease();
	
	@DefaultMessage("All weeks")
	String weekAll();
	
	@DefaultMessage("Assigned Classes:")
	String legendAssignedClasses();
	
	@DefaultMessage("Free Times:")
	String legendFreeTimes();
	
	@DefaultMessage("Required time")
	String legendRequiredTime();
	
	@DefaultMessage("Strongly preferred time")
	String legendStronglyPreferredTime();
	
	@DefaultMessage("Preferred time")
	String legendPreferredTime();
	
	@DefaultMessage("No time preference")
	String legendNoTimePreference();
	
	@DefaultMessage("Discouraged time")
	String legendDiscouragedTime();
	
	@DefaultMessage("Stringly discouraged time")
	String legendStronglyDiscouragedTime();
	
	@DefaultMessage("Prohibited time")
	String legendProhibitedTime();
	
	@DefaultMessage("Time not available")
	String legendTimeNotAvailable();
	
	@DefaultMessage("No preference")
	String legendNoPreference();
	
	@DefaultMessage("Required room")
	String legendRequiredRoom();
	
	@DefaultMessage("Strongly preferred room")
	String legendStronglyPreferredRoom();
	
	@DefaultMessage("Preferred room")
	String legendPreferredRoom();
	
	@DefaultMessage("No room preference")
	String legendNoRoomPreference();
	
	@DefaultMessage("Discouraged room")
	String legendDiscouragedRoom();
	
	@DefaultMessage("Stringly discouraged room")
	String legendStronglyDiscouragedRoom();
	
	@DefaultMessage("Prohibited room")
	String legendProhibitedRoom();	
	
	@DefaultMessage("{0} student conflicts")
	String legendStudentConflicts(String number);
	
	@DefaultMessage("{0} or more student conflicts")
	String legendStudentConflictsOrMore(String number);
	
	@DefaultMessage("No instructor back-to-back preference <it>(distance = 0m)</it>")
	String legendInstructorBTBNoPreference();
	
	@DefaultMessage("Discouraged back-to-back <it>(0m < distance <= 50m)</it>")
	String legendInstructorBTBDiscouraged();
	
	@DefaultMessage("Strongly discouraged back-to-back <it>(50m < distance <= 200m)</it>")
	String legendInstructorBTBStronglyDiscouraged();
	
	@DefaultMessage("Prohibited back-to-back <it>(200m < distance)</it>")
	String legendInstructorBTBProhibited();
	
	@DefaultMessage("No violated constraint")
	String legendDistributionNoViolation();
	
	@DefaultMessage("Discouraged/preferred constraint violated")
	String legendDistributionDiscouraged();
	
	@DefaultMessage("Strongly discouraged/preferred constraint violated")
	String legendDistributionStronglyDiscouraged();
	
	@DefaultMessage("Required/prohibited constraint violated")
	String legendDistributionProhibited();
	
	@DefaultMessage("No change")
	String legendPerturbationNoChange();
	
	@DefaultMessage("No initial assignment")
	String legendPerturbationNoInitial();
	
	@DefaultMessage("Room changed")
	String legendPerturbationRoomChanged();
	
	@DefaultMessage("Time changed")
	String legendPerturbationTimeChanged();
	
	@DefaultMessage("Both time and room changed")
	String legendPerturbationBothChanged();
	
	@DefaultMessage("Zero perturbation penalty")
	String legendPerturbationNoPenalty();
	
	@DefaultMessage("Perturbation penalty below or equal to {0}")
	String legendPerturbationPenaltyBelow(String number);
	
	@DefaultMessage("Perturbation penalty above {0}")
	String legendPerturbationPenaltyAbove(String number);
	
	@DefaultMessage("Required time and room")
	String legendBothRequired();
	
	@DefaultMessage("Required time and room")
	String legendHardRequired();
	
	@DefaultMessage("Can be moved in room with no hard conflict")
	String legendHardStronglyPreferred();
	
	@DefaultMessage("Can be moved in room (but there is a hard conflict), can be moved in time with no conflict")
	String legendHardPreferred();
	
	@DefaultMessage("Can be moved in room (but there is a hard conflict)")
	String legendHardNeutral();
	
	@DefaultMessage("Can be moved in time with no hard conflict, cannot be moved in room")
	String legendHardDiscouraged();
	
	@DefaultMessage("Can be moved in time (but there is a hard conflict), cannot be moved in room")
	String legendHardStronglyDiscouraged();

	@DefaultMessage("Zero penalty")
	String legendNoPenalty();
	
	@DefaultMessage("Penalty equal to {0}")
	String legendPenaltyEqual(String number);
	
	@DefaultMessage("Penalty equal or above {0}")
	String legendPenaltyEqualAbove(String number);

	@DefaultMessage("Assigned room is smaller than room limit of a class")
	String legendTooBigRoomsRequired();
	
	@DefaultMessage("Assigned room is not more than 25% bigger than the smallest avaialable room")
	String legendTooBigRoomsNeutral();
	
	@DefaultMessage("Assigned room is not more than 50% bigger than the smallest avaialable room")
	String legendTooBigRoomsDiscouraged();
	
	@DefaultMessage("Assigned room is more than 50% bigger than the smallest avaialable room")
	String legendTooBigRoomsStronglyDiscouraged();
	
	@DefaultMessage("{0}% students of the group are in the same class")
	String legendStudentGroups(String percentage);
	
	@DefaultMessage("Standard (MWF or TTh) time pattern is broken (time cannot be used for MW, WF, MF or TTh class)")
	String legendFreeTimeDiscouraged();
	
	@DefaultMessage("Useless half-hour")
	String legendFreeTimeStronglyDiscouraged();
	
	@DefaultMessage("Useless half-hour and broken standard time pattern")
	String legendFreeTimeProhibited();
	
	@DefaultMessage("current assignment")
	String initialAssignmentCurrent();
	
	@DefaultMessage("{0} ({1})")
	String roomLabelWithDisplayName(String label, String displayName);
	
	@DefaultMessage("{0} ({1})")
	String roomLabelWithSize(String label, Integer size);
	
	@DefaultMessage("{0} ({1}, {2})")
	String roomLabelWithDisplayNameAndSize(String label, String displayName, Integer size);
	
	@DefaultMessage("NOTE: Only classes that are loaded into the solver are displayed in the below list. This means that classes that are assigned to other " +
			"timetabling managers (e.g., LLR or LAB) as well as classes that are not loaded into the solver (e.g., Arrange Hours classes) are excluded. " +
			"For the full list of classes see <a href='classSearch.action'>Classes</a> or <a href='classAssignmentsReportSearch.action'>Class Assignments</a> page.")
	String notAssignedClassesNote();
	
	@DefaultMessage("{0} has been deleted.")
	String eventNoteRoomDeleted(String room);
	
	@DefaultMessage("Current Snapshot Date:")
	String labelCurrentSnapshotDate();

	@DefaultMessage("<u>T</u>ake New Snapshot")
	String buttonTakeNewSnapshot();
	
	@DefaultMessage("; ")
	String meetingContactSeparator();
	
	@DefaultMessage("Departmental")
	String serviceProviderDepartmental();
	
	@DefaultMessage("Waiting...")
	String scriptStatusWaiting();
	
	@DefaultMessage("All done.")
	String scriptStatusAllDone();
	
	@DefaultMessage("Killed.")
	String scriptStatusKilled();
	
	@DefaultMessage("Failed: {0}")
	String scriptStatusFailed(String message);
	
	@DefaultMessage("Starting up...")
	String scriptStatusStartingUp();
	
	@DefaultMessage("Compiling script...")
	String scriptStatusCompiling();
	
	@DefaultMessage("Running script...")
	String scriptStatusRunning();
	
	@DefaultMessage("Execution Stopped.")
	String scriptLogExecutionStopped();
	
	@DefaultMessage("Execution Failed.")
	String scriptLogExecutionFailed();
	
	@DefaultMessage("Output already created.")
	String scriptErrorOutputAlreadyCreated();
	
	@DefaultMessage("Send email when finished")
	String scriptSendEmailCheckbox();

	@DefaultMessage("Not assigned.")
	String classNotAssigned();
	
	@DefaultMessage("committed")
	String studentConflictCommitted();

	@DefaultMessage("fixed")
	String studentConflictFixed();

	@DefaultMessage("hard")
	String studentConflictHard();

	@DefaultMessage("distance")
	String studentConflictDistance();

	@DefaultMessage("important")
	String studentConflictImportant();
	
	@DefaultMessage("workday")
	String studentConflictWorkday();

	@DefaultMessage("instructor")
	String studentConflictInstructor();

	@DefaultMessage("Back-To-Back Instructor")
	String btbInstructorConflictConstraint();
	
	@DefaultMessage("not-assigned")
	String unassignment();
	
	@DefaultMessage("Remove {0} class assignment from the selection.")
	String titleRemoveSelectedClassAssignment(String className);
	
	@DefaultMessage("Same Time")
	String suggestionsSameTime();
	
	@DefaultMessage("Same Room")
	String suggestionsSameRoom();
	
	@DefaultMessage("Allow Break Hard")
	String suggestionsAllowBreakHard();
	
	@DefaultMessage("Placements")
	String suggestionsPlacements();
	
	@DefaultMessage("Suggestions")
	String suggestionsSuggestions();
	
	@DefaultMessage("Show All Conflicts")
	String opShowAllConflicts();
	
	@DefaultMessage("Hide All Conflicts")
	String opHideAllConflicts();
	
	@DefaultMessage("Balancing of department {0}")
	String constraintDeptSpread(String department);
	
	@DefaultMessage("Same subpart spread {0}")
	String constraintSameSubpartSpread(String course);
	
	@DefaultMessage("Distribution {0}")
	String constraintDistribution(String constraint);
	
	@DefaultMessage("Instructor {0}")
	String constraintInstructor(String instructor);
	
	@DefaultMessage("Room {0}")
	String constraintRoom(String room);
	
	@DefaultMessage("Class limit {0}")
	String constraintClassLimit(String clazz);
	
	@DefaultMessage("Unknown")
	String constraintUnknown();
	
	@DefaultMessage("Variable - oriented")
	String modeCBSVariables();
	
	@DefaultMessage("Constraints - oriented")
	String modeCBSConstraints();
	
	@DefaultMessage("Best Solution")
	String compareWithBestSolution();
	
	@DefaultMessage("Initial Solution")
	String compareWithInitialSolution();
	
	@DefaultMessage("Selected Solution")
	String compareWithSelectedSolution();
	
	@DefaultMessage("Committed Solution")
	String compareWithCommittedSolution();
	
	@DefaultMessage("?")
	String listSolutionsUnknown();
	
	@DefaultMessage("Open Class Detail for {0} in a new window.")
	String titleOpenClassDetail(String clazz);
	
	@DefaultMessage("Room Allocation - Non University Locations")
	String reportRoomAllocationNonUnivLocs();
	
	@DefaultMessage("Room Allocation - {0}")
	String reportRoomAllocation(String roomType);
	
	@DefaultMessage("Departmental Balancing")
	String reportDepartmentalBalancing();
	
	@DefaultMessage("Violated Distribution Preferences")
	String reportViolatedDistributionPreferences();
	
	@DefaultMessage("Instructor Back-to-Back Preferences")
	String reportInstructorBackToBackPreferences();
	
	@DefaultMessage("Student Conflicts")
	String reportStudentConflicts();
	
	@DefaultMessage("Section Balancing")
	String reportSectionBalancing();
	
	@DefaultMessage("Perturbations")
	String reportPerturbations();
	
	@DefaultMessage("{0} ... {1}")
	String reportRoomRange(String low, String high);
	
	@DefaultMessage("Total")
	String reportTotal();
	
	@DefaultMessage("{0}m")
	String reportDistanceInMeter(Integer distance);
	
	@DefaultMessage("group size <minimum, maximum)")
	String reportRoomAlocDescGroup();
	
	@DefaultMessage("actual group size (size of the smallest and the biggest room in the group)")
	String reportRoomAlocDescSize();
	
	@DefaultMessage("number of rooms in the group (cumulative numbers are displayed in parentheses)")
	String reportRoomAlocDescNbrRooms();
	
	@DefaultMessage("number of classes that are using a room from the group (actual solution)")
	String reportRoomAlocDescClassUse();
	
	@DefaultMessage("number of classes that &quot;should&quot; use a room of the group (smallest available room of a class is in this group)")
	String reportRoomAlocDescClassShould();
	
	@DefaultMessage("number of classes that must use a room of the group (all available rooms of a class are in this group; cumulative numbers are displayed in parentheses)")
	String reportRoomAlocDescClassMust();
	
	@DefaultMessage("average hours a room of the group is used (actual solution)")
	String reportRoomAlocDescHourUse();
	
	@DefaultMessage("average hours a room of the group should be used (smallest available room of a class is in this group)")
	String reportRoomAlocDescHourShould();
	
	@DefaultMessage("average hours a room of this group must be used (all available rooms of a class are in this group; cumulative numbers are displayed in parentheses)")
	String reportRoomAlocDescHourMust();
	
	@DefaultMessage("Class name")
	String reportPertClass();
	
	@DefaultMessage("Date (initial \u2192 assigned)")
	String reportPertDate();

	@DefaultMessage("Time (initial \u2192 assigned)")
	String reportPertTime();
	
	@DefaultMessage("Room (initial \u2192 assigned)")
	String reportPertRoom();
	
	@DefaultMessage("Distance between assignments (if different are used buildings)")
	String reportPertDistance();
	
	@DefaultMessage("Number of affected students")
	String reportPertStudents();
	
	@DefaultMessage("Number of affected students by time change")
	String reportPertStudentsTime();
	
	@DefaultMessage("Number of affected students by room change")
	String reportPertStudentsRoom();
	
	@DefaultMessage("Number of affected students by building change")
	String reportPertStudentsBuilding();
	
	@DefaultMessage("Number of affected instructors")
	String reportPertInstructor();
	
	@DefaultMessage("Number of affected instructors by time change")
	String reportPertInstructorTime();
	
	@DefaultMessage("Number of affected instructors by room change")
	String reportPertInstructorRoom();
	
	@DefaultMessage("Number of affected instructors by building change")
	String reportPertInstructorBuilding();
	
	@DefaultMessage("Number of rooms changed")
	String reportPertRoomChange();
	
	@DefaultMessage("Number of buildings changed")
	String reportPertBuildingChange();
	
	@DefaultMessage("Number of times changed")
	String reportPertTimeChange();
	
	@DefaultMessage("Number of days changed")
	String reportPertDayChange();
	
	@DefaultMessage("Number of hours changed")
	String reportPertHourChange();
	
	@DefaultMessage("Assigned building too far for students (from the initial one)")
	String reportPertTooFarStudents();
	
	@DefaultMessage("Assigned building too far for instructor (from the initial one)")
	String reportPertTooFarInstructor();
	
	@DefaultMessage("Difference in student conflicts")
	String reportPertDeltaStudentConf();
	
	@DefaultMessage("Number of new student conflicts")
	String reportPertNewStudentConf();
	
	@DefaultMessage("Difference in time preferences")
	String reportPertDeltaTimePref();
	
	@DefaultMessage("Difference in room preferences")
	String reportPertDeltaRoomPref();
	
	@DefaultMessage("Difference in back-to-back instructor preferences")
	String reportPertDeltaInstructorBTBPref();
	
	@DefaultMessage("Session Default")
	String studentStatusDefault();
	
	@DefaultMessage("Add Task...")
	String dialogAddTask();
	
	@DefaultMessage("Edit {0}")
	String dialogEditTask(String name);
	
	@DefaultMessage("Course<br>Requests")
	String colAssignedCourseRequests();
	
	@DefaultMessage("1st Choice<br>Assigned")
	String colAssignedPriorityCourseRequests();
	
	@DefaultMessage("Critical<br>Assignments")
	String colAssignedCriticalCourseRequests();
	
	@DefaultMessage("Important<br>Assignments")
	String colAssignedImportantCourseRequests();
	
	@DefaultMessage("Vital<br>Assignments")
	String colAssignedVitalCourseRequests();
	
	@DefaultMessage("LC<br>Assignments")
	String colAssignedLCCourseRequests();
	
	@DefaultMessage("Complete<br>Schedule")
	String colStudentsWithCompleteSchedule();
	
	@DefaultMessage("Class / IM<br>Preference")
	String colSectioningSelection();
	
	@DefaultMessage("Time<br>Conflicts")
	String colTimeConflicts();
	
	@DefaultMessage("Distance<br>Conflicts")
	String colDistanceConflicts();
	
	@DefaultMessage("Unbalanced<br>Sections")
	String colDisbalancedSections();
	
	@DefaultMessage("Arrange<br>Hours")
	String colClassesWithoutTime();
	
	@DefaultMessage("Operations")
	String colOperations();
	
	@DefaultMessage("Publish")
	String opSectioningSolutionPublish();
	
	@DefaultMessage("Unpublish")
	String opSectioningSolutionUnpublish();
	
	@DefaultMessage("Load")
	String opSectioningSolutionLoad();
	
	@DefaultMessage("Unload")
	String opSectioningSolutionUnload();
	
	@DefaultMessage("Select")
	String opSectioningSolutionSelect();
	
	@DefaultMessage("Deselect")
	String opSectioningSolutionDeselect();
	
	@DefaultMessage("Remove")
	String opSectioningSolutionRemove();
	
	@DefaultMessage("Published Schedule Runs")
	String titlePublishedScheduleRuns();
	
	@DefaultMessage("Refresh")
	String opRefresh();
	
	@DefaultMessage("Do you realy want to delete this published schedule run?")
	String confirmDeletePublishedSectioningSolution();
	
	@DefaultMessage("Do you really want to unload this published student schedule?")
	String confirmStudentSolverUnpublish();
	
	@DefaultMessage("Unpublish")
	String opSolverUnpublish();
	
	@DefaultMessage("Override Properties:")
	String propOverrideProperties();
	
	@DefaultMessage("Number of weeks during which students are allowed to enroll to this course, defaults to {0} when left blank.")
	String hintNewEnrollmentDeadline(String wkEnrollmentDefault);
	
	@DefaultMessage("Number of weeks during which students are allowed to change existing enrollments, defaults to {0} when left blank.")
	String hintClassChangesDeadline(String wkChangeDefault);
	
	@DefaultMessage("Number of weeks during which students are allowed to drop from this course, defaults to {0} when left blank.")
	String hintCourseDropDeadline(String wkDropDefault);
	
	@DefaultMessage("Weeks start on the day of session start date, number of weeks is relative to class start ({0}).")
	String descriptionEnrollmentDeadlines(String weekStartDayOfWeek);
	
	@DefaultMessage("If checked, only students meeting reservations will be allowed to enroll into the offering.")
	String checkByReservationOnly();
	
	@DefaultMessage("Only students meeting reservations are allowed to enroll into this offering.")
	String descriptionByReservationOnly2();
	
	@DefaultMessage("Students are allowed to enroll in this course up to {0} week of classes.")
	String textLastWeekEnrollment(String wkEnrollment);
	
	@DefaultMessage("Students are allowed to change existing enrollments up to {0} week of classes.")
	String textLastWeekChange(String wkChange);
	
	@DefaultMessage("Students are allowed to drop from this course up to {0} week of classes.")
	String textLastWeekDrop(String wkDrop);
	
	@DefaultMessage("Prohibited Overrides:")
	String propertyDisabledOverrides();
	
	@DefaultMessage("Allow Time Conflict")
	String checkCanOverlap();
	
	@DefaultMessage("Can Assign Over Limit")
	String checkCanOverLimit();
	
	@DefaultMessage("Student Must Follow")
	String checkMustBeUsed();
	
	@DefaultMessage("Do Not Reserve Space")
	String checkAllwaysExpired();
	
	@DefaultMessage("Restriction")
	String checkReservationRestriction();
	
	@DefaultMessage("Not Set: Using application configuration settings")
	String reservationInclusiveNotSet();
	
	@DefaultMessage("Default: Reservation is enforced on all the levels")
	String reservationInclusiveDefaultTrue();
	
	@DefaultMessage("Default: Reservation is only enforced on the selected level")
	String reservationInclusiveDefaultFalse();
	
	@DefaultMessage("Reservation: Reservation is enforced on all the levels")
	String reservationInclusiveTrue();
	
	@DefaultMessage("Restriction: Reservation is only enforced on the selected level")
	String reservationInclusiveFalse();

	@DefaultMessage("Departments")
	String sectDepartments();

	@DefaultMessage("Add Department")
	String sectAddDepartment();

	@DefaultMessage("Update Data")
	String buttonDepartmentsUpdateData();
	
	@DefaultMessage("Buildings")
	String sectBuildings();
	
	@DefaultMessage("Add Building")
	String sectAddBuilding();
	
	@DefaultMessage("Edit Building")
	String sectEditBuilding();
	
	@DefaultMessage("<u>A</u>dd Building")
	String buttonAddBuilding();
	
	@DefaultMessage("Update Data")
	String buttonBuildingsUpdateData();
	
	@DefaultMessage("Map:")
	String propMap();
	
	@DefaultMessage("Update room coordinates to match the building coordinates.")
	String checkBuildingUpdateRoomCoordinates();
	
	@DefaultMessage("building")
	String objectBuilding();

	@DefaultMessage("department")
	String objectDepartment();

	@DefaultMessage("Edit Department")
	String sectEditDepartment();
	
	@DefaultMessage("The department and all associated data will be deleted. Continue?")
	String confirmDepartmentDelete();

	@DefaultMessage("Update data has failed: {0}")
	String failedDepartmentUpdateData(String reason);
	
	@DefaultMessage("course offering")
	String objectCourseOffering();
	
	@DefaultMessage("Update data has failed: {0}")
	String failedBuildingUpdateData(String reason);
	
	@DefaultMessage("The building and all its rooms will be deleted. Continue?")
	String confirmBuildingDelete();
	
	@DefaultMessage("Do you really want to send an email to {0}\u00a0students?")
	String confirmSendEmail(int studentCount);
	
	@DefaultMessage("Sending email...")
	String waitSendingEmail();
	
	@DefaultMessage("Email sent.")
	String emailSent();
	
	@DefaultMessage("Email failed: {0}")
	String failureSendingEmail(String message);
	
	@DefaultMessage("Campus")
	String utilSqlAcademicInitiative();

	@DefaultMessage("Term")
	String utilSqlAcademicTerm();

	@DefaultMessage("Year")
	String utilSqlAcademicYear();
	
	@DefaultMessage("Building")
	String utilSqlBuilding();
	
	@DefaultMessage("Room")
	String utilSqlRoom();
	
	@DefaultMessage("Room_Type")
	String utilSqlRoomType();
	
	@DefaultMessage("Capacity")
	String utilSqlRoomSize();
	
	@DefaultMessage("Campus_region")
	String utilSqlCampusRegion();
	
	@DefaultMessage("Part_of_LLR_or_LALR_Pool")
	String utilSqlLlrLalrPool();
	
	@DefaultMessage("Classroom_Subtype")
	String utilSqlClassroomSubtype();

	@DefaultMessage("Event_Type")
	String utilSqlEventType();

	@DefaultMessage("Event_Type_Description")
	String utilSqlEventTypeDescription();

	@DefaultMessage("Utilization_Type")
	String utilSqlUtilizationType();

	@DefaultMessage("Course_Department")
	String utilSqlDepartment();

	@DefaultMessage("Subject")
	String utilSqlSubject();

	@DefaultMessage("Course_Number")
	String utilSqlCourseNbr();

	@DefaultMessage("Instr_Type")
	String utilSqlItype();

	@DefaultMessage("Section")
	String utilSqlSection();

	@DefaultMessage("Room_Controling_Dept")
	String utilSqlRoomDept();
	
	@DefaultMessage("Size_Group")
	String utilSqlRangeOfSizes();

	@DefaultMessage("Day_Time")
	String utilSqlDayTime();

	@DefaultMessage("_Standard_Weekday_Hours")
	String utilSqlTotalStandardWeekdayHoursSuffix();

	@DefaultMessage("_Standard_Hours")
	String utilSqlTotalStandardHoursSuffix();

	@DefaultMessage("_All_Hours")
	String utilSqlTotalAllHoursSuffix();

	@DefaultMessage("Occupied_Stations")
	String utilSqlStationsUsed();

	@DefaultMessage("Requested_Stations")
	String utilSqlStationsRequested();

	@DefaultMessage("Room_Usage")
	String utilSqlUsage();

	@DefaultMessage("Station_Usage")
	String utilSqlUsageSeatHours();

	@DefaultMessage("Station_Occupancy_Rate")
	String utilSqlStationOccupancyRate();
	
	@DefaultMessage("Requested_Station_Occupancy_Rate")
	String utilSqlRequestedStationOccupancyRate();

	@DefaultMessage("Class")
	String utilSqlEventTypeClass();

	@DefaultMessage("Final Exam")
	String utilSqlEventTypeFinalExam();

	@DefaultMessage("Midterm Exam")
	String utilSqlEventTypeMidtermExam();

	@DefaultMessage("Course Related")
	String utilSqlEventTypeCourseRelated();

	@DefaultMessage("Special Event")
	String utilSqlEventTypeSpecialEvent();

	@DefaultMessage("Room Not Available")
	String utilSqlEventTypeRoomNotAvailable();

	@DefaultMessage("Traditional Utilization")
	String utilSqlUtilizationTypeTraditional();

	@DefaultMessage("Final Exams Week")
	String utilSqlUtilizationTypeFinalExamsWeek();

	@DefaultMessage("Special Event")
	String utilSqlUtilizationTypeSpecialEvent();

	@DefaultMessage("Subject Area")
	@DoNotTranslate
	String pageSubjectArea();

	@DefaultMessage("Subject Areas")
	@DoNotTranslate
	String pageSubjectAreas();

	@DefaultMessage("Funding Department")
	String fieldFundingDepartment();
	
	@DefaultMessage("Managers")
	String fieldManagers();
	
	@DefaultMessage("Last Change")
	String fieldLastChange();
	
	@DefaultMessage("No external funding department")
	String noFundingDepartment();
	
	@DefaultMessage("{0} by {1}")
	String lastChange(String date, String manager);

	@DefaultMessage("Departments")
	@DoNotTranslate
	String pageDepartments();

	@DefaultMessage("Add Department")
	@DoNotTranslate
	String pageAddDepartment();
	
	@DefaultMessage("Edit Department")
	@DoNotTranslate
	String pageEditDepartment();
	
	@DefaultMessage("Code")
	String colCode();
	
	
	@DefaultMessage("Code:")
	String propDeptCode();
	
	@DefaultMessage("Status Type")
	String propStatusType();
	
	@DefaultMessage("Number")
	String colNumber();
	
	@DefaultMessage("Abbreviation")
	String colAbbv();

	@DefaultMessage("Department Status:")
	String propDepartmentStatus();
	
	@DefaultMessage("External <br>Manager")
	String colExternalManager();

	@DefaultMessage("External Manager:")
	String propExternalManager();

	@DefaultMessage("External Manager Abbreviation:")
	String propExternalManagerAbbreviation();
	
	@DefaultMessage("External Manager Abbreviation")
	String fieldExternalManagerAbbreviation();
	
	@DefaultMessage("External Manager Abbreviation should only be used when the department is marked as External Manager")
	String errorExternalManagerAbbreviationUse();
	
	@DefaultMessage("External Manager Name:")
	String propExternalManagerName();
	
	@DefaultMessage("External Manager Name")
	String fieldExternalManagerName();

	@DefaultMessage("External Manager Name should only be used when the department is marked as External Manager")
	String errorExternalManagerNameUse();

	@DefaultMessage("Show all departments (including departments with no manager and no subject area)")
	String checkShowAllDepartments();
	
	@DefaultMessage("Dist Pref  <br> Priority")
	String colDistPrefPriority();

	@DefaultMessage("Distribution Preference Priority:")
	String propPrefPriority();
	
	@DefaultMessage("Allow <br> Required")
	String colAllowRequired();

	@DefaultMessage("Allow Required Time:")
	String propAllowReqTime();
	
	@DefaultMessage("Instructor <br> Preferences")
	String colInstructorPref();

	@DefaultMessage("Instructor <br> Preferences")
	String propInstructorPref();

	@DefaultMessage("Inherit Instructor Preferences:")
	String propInheritInstructorPref();
	
	@DefaultMessage("Allow Required Room:")
	String propAllowReqRoom();

	@DefaultMessage("Allow Required Distribution:")
	String propAllowReqDist();
	
	@DefaultMessage("Student Scheduling:")
	String propAllowStudentScheduling();

	@DefaultMessage("External Funding Department:")
	String propExternalFundingDept();

	@DefaultMessage("External <br> Funding Department")
	String colExternalFundingDept();
	
	@DefaultMessage("Event Management:")
	String propAllowEvents();
	
	@DefaultMessage("Events")
	String colEvents();
	
	@DefaultMessage("Add Status")
	String buttonDependentAddStatus();
	
	@DefaultMessage("Delete All")
	String buttonDependentDeleteAll();
	
	@DefaultMessage("Department/Session Default")
	String propDefaultDependentStatus();

	@DefaultMessage("Session Default")
	String propDepartmentStatusDefault();
	
	@DefaultMessage("-")
	String propDefaultDependentDepartment();
	
	@DefaultMessage("Student<br>Scheduling")
	String colStudentScheduling();
	
	@DefaultMessage("Student<br>Scheduling")
	String propStudentScheduling();
	
	@DefaultMessage("Department List - {0}")
	String propDepartmentlist(String name);
	
	@DefaultMessage("Assign Instructors")
	@DoNotTranslate
	String pageAssignInstructors();

	@DefaultMessage("CheckConflicts")
	String colCheckConflicts();

	@DefaultMessage("Display")
	String colDisplayInstructors();

	@DefaultMessage("Coordinators:")
	String labelCourseCoordinators();

	@DefaultMessage("Class")
	String fieldClassName();
	
	@DefaultMessage("% Share")
	String fieldPercentShare();
	
	@DefaultMessage("Check<br>Conflicts")
	String fieldCheckConflicts();
	
	@DefaultMessage("Responsibility")
	String fieldResponsibility();
	
	@DefaultMessage("Display<br>(All: {0})")
	String fieldDisplay(String buttonCharacter);

	@DefaultMessage("Class Id")
	String fieldClassUid();
	
	@DefaultMessage("Class Parent Id")
	String fieldClassParentUid();
	
	@DefaultMessage("Instructional Offering Config Unique Id")
	String fieldConfigUid();

	@DefaultMessage("Error")
	String fieldError();

	@DefaultMessage("Add Additional Instructor Row")
	String fieldAdd();
	
	@DefaultMessage("Insert a new row below this row.")
	String fieldInsertRowBelow();

	@DefaultMessage("Delete Instructor Row")
	String fieldDelete();
	
	@DefaultMessage("First Record For Class UniqueId")
	String fieldFirstRecordForClassUid();	

	@DefaultMessage("<u>U</u>nassign All")
	String buttonUnassignAll();
	
	@DefaultMessage("<u>C</u>opy to Sub-Classes")
	String buttonCopyInstructors();
	
	@DefaultMessage("This operation will copy all instructor assignments from parent classes to all their children with the same instructional type (Lec 1a and Lec 1b will have the same instructors as Lec 1, etc.).")
	String titleCopyInstructors();
	
	@DefaultMessage("Assign Instructors")
	String buttonAssignInstructors();
	
	@DefaultMessage("Instructional offering configuration id not provided.")
	String errorConfigurationIdNotProvided();

	@DefaultMessage("Instructional offering configuration with matching unique id not found.")
	String errorConfigurationIdNotFound();

	@DefaultMessage("User does not have permission to remove all instructors.")
	String errorDeleteAllInstructorsPermission();

	@DefaultMessage("Missing data for class: {0}")
	String errorInstructorInputDataNotFoundForClass(String className);
	
	@DefaultMessage("Duplicate instructor {0} with same responsibility {1} for class. {2}")
	String errorDuplicateInstructorData(String instrName, String responsibility, String course);
	
	@DefaultMessage("Initial setup of Instructional Offering Config has not been completed.")
	String exceptionInitialIOSetupIncomplete();
	
	@DefaultMessage("Failed to Send Update to External System:  {0} = {1}")
	String exceptionExternalSystemUpdateFailure(String exceptionType, String failedClassName);
	
	@DefaultMessage("Default (Wait-Listing Enabled)")
	String waitListDefaultEnabled();
	
	@DefaultMessage("Default (Wait-Listing Disabled)")
	String waitListDefaultDisabled();
	
	@DefaultMessage("Default (Re-Scheduling Enabled)")
	String waitListDefaultReschedule();
	
	@DefaultMessage("Wait-Listing Enabled")
	String waitListEnabled();
	
	@DefaultMessage("Wait-Listing Disabled")
	String waitListDisabled();
	
	@DefaultMessage("Re-Scheduling Enabled")
	String waitListReschedule();
	
	@DefaultMessage("Wait-listing is enabled for this offering.")
	String descWaitListEnabled();
	
	@DefaultMessage("Wait-listing is not enabled for this offering.")
	String descWaitListDisabled();
	
	@DefaultMessage("Wait-listing is not enabled, but students can be automatically re-scheduled for this offering.")
	String descWaitListReschedule();
	
	@DefaultMessage("Course Offering")
	String sectCourseOffering();
	
	@DefaultMessage("Partition of:")
	String propPartitionOf();
	
	@DefaultMessage("Partition of")
	String colPartitionOf();
	
	@DefaultMessage("Not a Partition")
	String itemNoParition();
	
	@DefaultMessage("Campus")
	String labelCampus();
	
	@DefaultMessage("Import of {0}")
	String itemImportActionName(String type);
	
	@DefaultMessage("Export of {0}")
	String itemExportActionName(String type);
	
	@DefaultMessage("Label")
	String fieldLabel();
	
	@DefaultMessage("Type")
	String fieldRoomClassType();
	
	@DefaultMessage("Rooms")
	String fieldNbrRooms();
	
	@DefaultMessage("Room")
	String typeRoomClass();
	
	@DefaultMessage("Other Location")
	String typeNonUniversityLocationClass();
	
	@DefaultMessage("Default Value")
	String fieldDefaulValue();
	
	@DefaultMessage("Allowed Values")
	String fieldAllowedValues();
	
	@DefaultMessage("Description")
	String fieldDescription();
	
	@DefaultMessage("Unload")
	String actionSolverUnload();
	
	@DefaultMessage("Deselect")
	String actionSolverDeselect();
	
	@DefaultMessage("Reload")
	String actionOnlineSolverReload();
	
	@DefaultMessage("Shutdown")
	String actionOnlineSolverShutdown();
	
	@DefaultMessage("Shutdown All")
	String actionOnlineSolverShutdownAll();
	
	@DefaultMessage("Un-Master")
	String actionOnlineSolverUnmaster();
	
	@DefaultMessage("Reconnect")
	String actionServerReconnect();
	
	@DefaultMessage("Reconnect Dabase")
	String actionServerReconnectHibernate();
	
	@DefaultMessage("Shutdown")
	String actionServerShutdown();
	
	@DefaultMessage("Reset")
	String actionServerReset();

	@DefaultMessage("Enable")
	String actionServerEnable();
	
	@DefaultMessage("Disable")
	String actionServerDisable();

	@DefaultMessage("{0} as {1}")
	String solverOwner(String owner, String problem);
	
	@DefaultMessage("Do you really want to unload this solver?")
	String confirmUnloadSolver();
	
	@DefaultMessage("Created")
	String colSolverCreated();
	
	@DefaultMessage("Last Used")
	String colSolverLastUsed();
	
	@DefaultMessage("Session")
	String colSolverSession();
	
	@DefaultMessage("Host")
	String colSolverHost();
	
	@DefaultMessage("Config")
	String colSolverConfigShort();
	
	@DefaultMessage("Status")
	String colSolverStatus();
	
	@DefaultMessage("Owner")
	String colSolverOwner();
	
	@DefaultMessage("Mode")
	String colSolverMode();
	
	@DefaultMessage("Mem")
	String colSolverMem();
	
	@DefaultMessage("Cores")
	String colSolverCores();
	
	@DefaultMessage("Assigned<br>Variables")
	String colSolverAssignedVariables();
	
	@DefaultMessage("Total<br>Value")
	String colSolverOverallValue();
	
	@DefaultMessage("Time<br>Prefs")
	String colSolverTimePrefs();
	
	@DefaultMessage("Student<br>Conflicts")
	String colSolverStudentConfs();
	
	@DefaultMessage("Room<br>Prefs")
	String colSolverRoomPrefs();
	
	@DefaultMessage("Distribution<br>Prefs")
	String colSolverDistrPrefs();
	
	@DefaultMessage("BTB Instr<br>Prefs")
	String colSolverBtbInstrPrefs();
	
	@DefaultMessage("MPP")
	String colSolverPerturbations();
	
	@DefaultMessage("Note")
	String colSolverNote();
	
	@DefaultMessage("Student<br>Confs")
	String colSolverExamStudentConfs();
	
	@DefaultMessage("Instr<br>Conf")
	String colSolverExamInstrConfs();
	
	@DefaultMessage("Period<br>Prefs")
	String colSolverExamPeriodPref();
	
	@DefaultMessage("Room<br>Prefs")
	String colSolverExamRoomPref();
	
	@DefaultMessage("Room<br>Split")
	String colSolverExamRoomSplits();
	
	@DefaultMessage("Room<br>Size")
	String colSolverExamRoomSize();
	
	@DefaultMessage("Distr<br>Prefs")
	String colSolverExamDistrPrefs();
	
	@DefaultMessage("Exam<br>Rotation")
	String colSolverExamRotation();
	
	@DefaultMessage("MPP")
	String colSolverExamPerturbations();
	
	@DefaultMessage("Course<br>Requests")
	String colSolverStudCourseReqs();
	
	@DefaultMessage("1st Choice<br>Assigned")
	String colSolverStud1stChoice();
	
	@DefaultMessage("Complete<br>Schedule")
	String colSolverStudCompleteStuds();
	
	@DefaultMessage("Class / IM<br>Preference")
	String colSolverStudSelection();
	
	@DefaultMessage("Distance<br>Conflicts")
	String colSolverStudDistanceConfs();
	
	@DefaultMessage("Time<br>Conflicts")
	String colSolverStudTimeOverlaps();
	
	@DefaultMessage("Unbalanced<br>Sections")
	String colSolverStudAvgDisbalance();
	
	@DefaultMessage("Unbalanced<br>Over 10%")
	String colSolverStudDisbOver10();
	
	@DefaultMessage("Free<br>Confs")
	String colSolverStudFreeConf();
	
	@DefaultMessage("MPP")
	String colSolverStudPerturbations();
	
	@DefaultMessage("Operation(s)")
	String colSolverOperations();
	
	@DefaultMessage("Attribute<br>Prefs")
	String colSolverInstrAtributePrefs();
	
	@DefaultMessage("Course<br>Prefs")
	String colSolverInstrCoursePrefs();
	
	@DefaultMessage("Instructor<br>Prefs")
	String colSolverInstrInstructorPrefs();
	
	@DefaultMessage("Teaching<br>Prefs")
	String colSolverInstrTeachingPrefs();
	
	@DefaultMessage("Time<br>Prefs")
	String colSolverInstrTimePrefs();
	
	@DefaultMessage("Same<br>Inst")
	String colSolverInstrSameInstructor();
	
	@DefaultMessage("Same<br>Lect")
	String colSolverInstrSameLecture();
	
	@DefaultMessage("Same<br>Days")
	String colSolverInstrSameDays();
	
	@DefaultMessage("Same<br>Room")
	String colSolverInstrSameRoom();
	
	@DefaultMessage("BTB")
	String colSolverInstrBTB();
	
	@DefaultMessage("Original")
	String colSolverInstrOriginalInstructor();
	
	@DefaultMessage("Manage Course Timetabling Solvers")
	String sectManageSolversCourse();
	
	@DefaultMessage("Manage Examination Timetabling Solvers")
	String sectManageSolversExam();
	
	@DefaultMessage("Manage Batch Student Scheduling Solvers")
	String sectManageSolversStudent();
	
	@DefaultMessage("Manage Instructor Scheduling Solvers")
	String sectManageSolversInstructor();
	
	@DefaultMessage("Manage Online Scheduling Servers")
	String sectManageSolversOnline();
	
	@DefaultMessage("Available Servers")
	String sectAvailableServers();
	
	@DefaultMessage("No solver is running.")
	String infoNoSolver();
	
	@DefaultMessage("Host")
	String colServerHost();
	
	@DefaultMessage("Version")
	String colServerVersion();

	@DefaultMessage("Started")
	String colServerStarted();
	
	@DefaultMessage("Available Memory")
	String colServerAvailableMemory();
	
	@DefaultMessage("NrCores")
	String colServerNrCores();
	
	@DefaultMessage("Ping")
	String colServerPing();
	
	@DefaultMessage("Usage")
	String colServerUsage();
	
	@DefaultMessage("NrInstances")
	String colServerNrInstances();
	
	@DefaultMessage("Active")
	String colServerActive();
	
	@DefaultMessage("Working")
	String colServerWorking();
	
	@DefaultMessage("Passivated")
	String colServerPassivated();
	
	@DefaultMessage("Operation(s)")
	String colServerOperations();
	
	@DefaultMessage("inactive")
	String serverInactive();
	
	@DefaultMessage("Do you really want to enable server {0} for the new solver instances?")
	String configServerEnable(String host);
	
	@DefaultMessage("Do you really want to disable server {0} for the new solver instances?")
	String confirmServerDisable(String host);
	
	@DefaultMessage("Do you really want to reset server {0}?")
	String confirmServerReset(String host);
	
	@DefaultMessage("Do you really want to shutdown server {0}?")
	String confirmServerShutdown(String host);
	
	@DefaultMessage("Do you really want to reconnect server {0}?")
	String confirmServerReconnect(String host);
	
	@DefaultMessage("Do you really want to reconnect the database? This will forcibly close all existing connections.")
	String confirmServerReconnectHibernate();
	
	@DefaultMessage("tomcat")
	String serverFlagTomcat();
	
	@DefaultMessage("coordinator")
	String serverFlagCoordinator();
	
	@DefaultMessage("unavailable")
	String serverFlagUnavailable();
	
	@DefaultMessage("(master)")
	String serverFlagMaster();
	
	@DefaultMessage("No solver server is running.")
	String infoNoServerRunning();
	
	@DefaultMessage("There is no online student scheduling server running at the moment.")
	String infoNoOnlineSolverRunning();
	
	@DefaultMessage("Do you really want to reload this server?")
	String confirmOnlineReload();
	
	@DefaultMessage("Do you really want to shutdown this server?")
	String confrimOnlineShutdown();
	
	@DefaultMessage("Do you really want to un-master this server?")
	String confirmOnlineUnMaster();
	
	@DefaultMessage("Move this record up.")
	String titleMoveUp();
	
	@DefaultMessage("Move this record down.")
	String titleMoveDown();
	
	@DefaultMessage("Order")
	String colOrder();
	
	@DefaultMessage("Students: {0}")
	String solverSolverParameterGroupStudents(String group);
	
	@DefaultMessage("Examinations: {0}")
	String solverSolverParameterGroupExams(String group);
	
	@DefaultMessage("Courses: {0}")
	String solverSolverParameterGroupCourses(String group);
	
	@DefaultMessage("Instructors: {0}")
	String solverSolverParameterGroupInstructors(String group);
	
	@DefaultMessage("Group")
	String fieldSolverParameterGroup();
	
	@DefaultMessage("Controlling Department Statuses")
	String sectControllingDepartmentStatuses();
	
	@DefaultMessage("Delete")
	String buttonDeleteLine();
	
	@DefaultMessage("this department")
	String thisDepartment();
	
	@DefaultMessage("Subject Area List - {0}")
	String sectSujectAreas(String session);
	
	@DefaultMessage("<u>A</u>dd Subject Area")
	String buttonAddSubjectArea();
	
	@DefaultMessage("A subject area with offered classes cannot be deleted")
	String errorCannotDeleteSubjectAreaWithClasses();
	
	@DefaultMessage("The subject area and all associated data will be deleted. Continue?")
	String confirmDeleteSubjectArea();
	
	@DefaultMessage("Re-Scheduling")
	String toggleReSchedule();

	@DefaultMessage("Position:")
	String propPosition();
	
	@DefaultMessage("Room Groups")
	String colRoomGroups();
	
	@DefaultMessage("Room Features")
	String colRoomFeatures();
	
	@DefaultMessage("Distributions")
	String colDistributions();
	
	@DefaultMessage("Other Requirements:")
	String propOtherPreferences();
		
	@DefaultMessage("Preferences & Requirements")
	String sectGeneralPreferences();
	
	@DefaultMessage("Individual Course Requirements")
	String sectCoursePreferences();
	
	@DefaultMessage("<u>C</u>opy<span class='unitime-ButtonArrow'>&#9660;</span>")
	String buttonCopyInstructorSurvey();
	
	@DefaultMessage("Preferences & Requirements from")
	String opCopyPreferencesRequirements();
	
	@DefaultMessage("Course Requirements from")
	String opCopyCourseRequirements();
	
	@DefaultMessage("Save for <u>L</u>ater")
	String buttonSaveInstructorSurvey();
	
	@DefaultMessage("<u>S</u>ubmit")
	String buttonSubmitInstructorSurvey();
	
	@DefaultMessage("<u>U</u>nsubmit")
	String buttonUnsubmitInstructorSurvey();
	
	@DefaultMessage("Please provide reason for the {0} {1}.")
	String hintProvideReasonFor(String pref, String item);
	
	@DefaultMessage("Please provide reason for the prohibited time(s).")
	String hintProvideReasonForProhibitedTimes();
	
	@DefaultMessage("Column Width")
	String fieldColumnWidth();
	
	@DefaultMessage("Submitted:")
	String propSubmitted();
	
	@DefaultMessage("Not Submitted")
	String notSubbitted();
	
	@DefaultMessage("The survey has been updated after the preferences have been copied to the instructor.")
	String surveyUpdatedAfterApply();
	
	@DefaultMessage("You are not allowed to submit an instructor survey for {0} at the moment.")
	String errorInstructorSurveyNotAllowed(String session);
	
	@DefaultMessage("Instructor survey cannot be edited at this moment.")
	String infoInstructorSurveyNotEditable();
	
	@DefaultMessage("Instructor survey has been submitted on {0}.")
	String infoInstructorSurveySubmitted(String ts);
	
	@DefaultMessage("<u>C</u>lose")
	String buttonCloseInstructorSurvey();
	
	@DefaultMessage("Saving instructor survey ...")
	String waitSavingInstructorSurvey();
	
	@DefaultMessage("Submitting instructor survey ...")
	String waitSubmittingInstructorSurvey();
	
	@DefaultMessage("Updating instructor survey ...")
	String waitUpdatingInstructorSurvey();
	
	@DefaultMessage("Instructor Survey")
	String sectInstructorSurvey();
	
	@DefaultMessage("Instructor Requirements")
	String sectInstructorRequirements();
	
	@DefaultMessage("Course Requirements:")
	String propInstructorCoursePreferences();
	
	@DefaultMessage("Edit <u>S</u>urvey")
	String buttonEditInstructorSurvey();
	
	@DefaultMessage("Delete Survey")
	String buttonDeleteInstructorSurvey();

	@DefaultMessage("Copy Preferences")
	String buttonApplyInstructorSurveyPreferences();
	
	@DefaultMessage("Time:")
	String propTimePrefs();
	
	@DefaultMessage("Existing instructor preferences will be overwritten with the preferences from the instructor survey. Are you ready to do that?")
	String questionApplyInstructorSurveyPreferences();
	
	@DefaultMessage("All the information provided by the instructor will be deleted. Do you ready want to do that?")
	String questionDeleteInstructorSurveys();
	
	@DefaultMessage("Instructor Survey")
	String fieldInstructorSurvey();
	
	@DefaultMessage("No instructor survey have been saved for the instructor yet.")
	String errorNoInstructorSurvey();
	
	@DefaultMessage("The survey has been sucessfully updated.")
	String infoInstructorSurveyUpdated();
	
	@DefaultMessage("You are about to submit the instructor survey. Once submitted you will not be able to make changes. Are you ready to do that?")
	String questionSubmitInstructorSurvey();
	
	@DefaultMessage("You are about to unsubmit the instructor survey. This will allow the instructor to make changes again. Do you really want to unsubmit the survey?")
	String questionUnsubmitInstructorSurvey();
	
	@DefaultMessage("Preferences Copied:")
	String propLastApplied();
	
	@DefaultMessage("{0} for {1}")
	String lastApply(String date, String dept);
	
	@DefaultMessage("Session {0} not found.")
	String errorSessionNotFound(String session);
	
	@DefaultMessage("There are unsaved changes in your survey. Do you really want to discard these changes without updating your survey?")
	String queryLeaveChangesOnYourInstructorSurvey();
	
	@DefaultMessage("There are unsaved changes in the instructor survey. Do you really want to discard these changes without updating the survey?")
	String queryLeaveChangesOnInstructorSurvey();
	
	@DefaultMessage("-- changed to")
	String instructorSurveyPreferenceLevelChangedTo();
	
	@DefaultMessage("-- added as")
	String instructorSurveyPreferenceAdded();
	
	@DefaultMessage("-- not used")
	String instructorSurveyPreferenceNotSet();
	
	@DefaultMessage("Highlight differences")
	String instructorSurveyCompareWithInstructorPrefs();
	
	@DefaultMessage("Other")
	String colOtherPref();
	
	@DefaultMessage("Requested\nTime Preferences")
	String colRequestedTimePrefs();
	
	@DefaultMessage("Requested\nRoom Preferences")
	String colRequestedRoomPrefs();
	
	@DefaultMessage("Requested\nDistribution Prefs")
	String colRequestedDistPrefs();
	
	@DefaultMessage("Other\nRequirements")
	String colRequestedOtherPrefs();
	
	@DefaultMessage("Instructor Survey for {0} ({1})")
	String instructorSurveyEmailSubject(String instructorName, String academicSession);
	
	@DefaultMessage("Student Scheduling Rule")
	@DoNotTranslate
	String pageStudentSchedulingRule();
	
	@DefaultMessage("Student Scheduling Rules")
	@DoNotTranslate
	String pageStudentSchedulingRules();
	
	@DefaultMessage("Rule")
	String fieldRuleName();
	
	@DefaultMessage("Student Filter")
	String fieldStudentFilter();
	
	@DefaultMessage("Initiative")
	String fieldInitiative();
	
	@DefaultMessage("Term")
	String fieldTerm();
	
	@DefaultMessage("First Year")
	String fieldFirstYear();
	
	@DefaultMessage("Last Year")
	String fieldLastYear();
	
	@DefaultMessage("Instr. Methods")
	String fieldInstructionalMethodRegExp();
	
	@DefaultMessage("Course Names")
	String fieldCourseNameRegExp();
	
	@DefaultMessage("Course Types")
	String fieldCourseTypeRegExp();
	
	@DefaultMessage("Mode")
	String fieldRuleMode();
	
	@DefaultMessage("Filter")
	String fieldAppliesToFilter();
	
	@DefaultMessage("Online")
	String fieldAppliesToOnline();

	@DefaultMessage("Batch")
	String fieldAppliesToBatch();
	
	@DefaultMessage("Advisor Override")
	String fieldAdvisorOverride();
	
	@DefaultMessage("Admin Override")
	String fieldAdminOverride();
	
	@DefaultMessage("AND")
	String ruleConjunctive();
	
	@DefaultMessage("OR")
	String ruleDisjunctive();
	
	@DefaultMessage("Notifications")
	String fieldNotifications();
	
	@DefaultMessage("Course Types")
	String fieldCourseTypes();
	
	@DefaultMessage("Other (No Type)")
	String toggleNoCourseType();
	
	@DefaultMessage("Do you realy want to delete this instructor attibute?")
	String confirmDeleteInstructorAttribute();
	
	@DefaultMessage("Do you realy want to delete this {0}?")
	String confirmDeleteItem(String item);
	
	@DefaultMessage("Do you realy want to delete this report?")
	String confirmDeleteReport();
	
	@DefaultMessage("Do you realy want to delete this script?")
	String confirmDeleteScript();
	
	@DefaultMessage("Too many users are using the page, please wait ...\nYour current position in waiting queue: {0}")
	String waitTooManyUsersWaitInQueue(int queue);
	
	@DefaultMessage("Inactive Warning")
	String dialogWarningInactive();
	
	@DefaultMessage("You have been inactive for {0} minutes. Do you want to continue?\n\nPlease choose to stay or to leave the {1}.")
	String warnInactive(int minutes, String pageName);
	
	@DefaultMessage("Stay ({0})")
	String buttonWarningInactiveStay(int seconds);
	
	@DefaultMessage("Leave")
	String buttonWarningInactiveLeave();
	
	@DefaultMessage("The {0} was closed due to inactivity.")
	String closedDueToInactivity(String pageName);
	
	@DefaultMessage("Student Filter:")
	String propStudentFilter();
	
	@DefaultMessage("Student filter cannot remain empty.")
	String hintReservationNoFilter();
	
	@DefaultMessage("(Link)")
	String roomExternalLink();
	
	@DefaultMessage("\u2026 {0} more")
	String moreItems(int count);
}