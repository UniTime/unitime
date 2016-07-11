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

	@DefaultMessage("Version {0} built on {1}")
	String pageVersion(String version, String buildDate);
	
	@DefaultMessage("&copy; 2008 - 2016 The Apereo Foundation,<br>distributed under the Apache License, Version 2.")
	String pageCopyright();
	
	@DefaultMessage("UniTime {0}, \u00A9 2008 - 2016 The Apereo Foundation, distributed under the Apache License.")
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

	@DefaultMessage("Export iCalendar")
	String opExportICalendar();
		
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
	
	@DefaultMessage("Classess")
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
	
	@DefaultMessage("Instructional<br>Offering")
	String colInstructionalOffering();
	
	@DefaultMessage("Reservation<br>Type")
	String colReservationType();
	
	@DefaultMessage("Owner")
	String colOwner();
	
	@DefaultMessage("Restrictions")
	String colRestrictions();
	
	@DefaultMessage("Reserved<br>Space")
	String colReservedSpace();
	
	@DefaultMessage("Expiration<br>Date")
	String colExpirationDate();
	
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
	
	@DefaultMessage("Instructor Pregerences:")
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

	@DefaultMessage("<u>O</u>k")
	String buttonOk();

	@DefaultMessage("<u>C</u>ancel")
	String buttonCancel();
	
	@DefaultMessage("<u>C</u>ancel Event")
	String buttonCancelEvent();

	@DefaultMessage("<u>S</u>earch")
	String buttonSearch();
	
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
	
	@DefaultMessage("Add&nbsp;<u>R</u>eservation")
	String buttonAddReservation();
	
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
	
	@DefaultMessage("<u>A</u>ssign")
	String buttonAssign();

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
	
	@DefaultMessage("Abbreviation is required.")
	String errorAbbreviationIsEmpty();
	
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
	
	@DefaultMessage("There is no {0} solution committed, {1} classes are not considered.")
	String warnSolverNoCommittedSolutionExternal(String owner, String ext);
	
	@DefaultMessage("There is no {0} solution committed, {1} departmental classes are not considered.")
	String warnSolverNoCommittedSolutionDepartmental(String owner, String subjects);

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
	
	@DefaultMessage("Your contact information will be updated.")
	String confirmYourContactChange();
	
	@DefaultMessage("Please confirm the change in the contact information for {0}.")
	String checkMainContactChange(String name);
	
	@DefaultMessage("Change future sessions as well.")
	String checkApplyToFutureSessions();
	
	@DefaultMessage("The contact information for {0} will be updated.")
	String confirmMainContactChange(String name);

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
	
	@DefaultMessage("Student Sectioning Reports")
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
	
	@DefaultMessage("Student Sectioning Dashboard")
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
	
	@DefaultMessage("Student Sectioning Solver Reports")
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
	
	@DefaultMessage("N/A")
	String itemNotApplicable();
	
	@DefaultMessage("All Departments")
	String itemAllDepartments();
	
	@DefaultMessage("None")
	String itemNone();
	
	@DefaultMessage("No Type")
	String itemNoFeatureType();

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
	
	@DefaultMessage("Details of {0}")
	String dialogDetailsOf(String item);
	
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
	
	@DefaultMessage("Main contact last name is required.")
	String reqMainContactLastName();

	@DefaultMessage("Main contact email is required.")
	String reqMainContactEmail();

	@DefaultMessage("No meetings were defined.")
	String reqMeetings();

	@DefaultMessage("One or more meetings is overlapping with an existing event.")
	String reqNoOverlaps();
	
	@DefaultMessage("No courses / clases were defined.")
	String reqCoursesOrClasses();
	
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
	
	@DefaultMessage("<u>A</u>dd Department...")
	String buttonAddDepartment();
	
	@DefaultMessage("<u>R</u>emove All")
	String buttonRemoveAll();
	
	@DefaultMessage("Add Department...")
	String dialogAddDepartment();
	
	@DefaultMessage("-- Add Department --")
	String separatorAddDepartment();
	
	@DefaultMessage("Add All Departments")
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
	
	@DefaultMessage("Expiration Date:")
	String propExpirationDate();
	
	@DefaultMessage("Restrictions:")
	String propRestrictions();
	
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

	@DefaultMessage("Access")
	String toggleAccess();
	
	@DefaultMessage("Enrollment")
	String toggleEnrollment();
	
	@DefaultMessage("Advisor")
	String toggleAdvisor();
	
	@DefaultMessage("Admin")
	String toggleAdmin();
	
	@DefaultMessage("Email")
	String toggleEmail();
	
	@DefaultMessage("Wait-Listing")
	String toggleWaitList();
	
	@DefaultMessage("No Batch")
	String toggleNoBatch();
	
	@DefaultMessage("Other")
	String toggleNoCourseType();
	
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
	
	@DefaultMessage("Instructor")
	String headerInstructor();
	
	@DefaultMessage("Available Assignments")
	String headerAvailableAssignments();
	
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
	
	@DefaultMessage("&nbsp;(of&nbsp;{0})")
	String curriculumProjectionRulesOfTotal(int total);
	
	@DefaultMessage("{0}&nbsp;&rarr;&nbsp;")
	String curriculumProjectionRulesOldValue(int lastLike);
	
	@DefaultMessage("Appearance: Courses")
	String flagAppearanceCourses();
	
	@DefaultMessage("Appearance: Examinations")
	String flagAppearanceExaminations();
	
	@DefaultMessage("Appearance: Student Sectioning")
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

	@DefaultMessage("Department Status Type")
	String optionDepartmentStatusType();

	@DefaultMessage("Department Status Types")
	String optionDepartmentStatusTypes();

	@DefaultMessage("Room Type")
	String optionRoomType();

	@DefaultMessage("Room Types")
	String optionRoomTypes();

	@DefaultMessage("Student Sectioning Status")
	String optionStudentSectioningStatus();

	@DefaultMessage("Student Sectioning Statuses")
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
	
	@DefaultMessage("Individual Reservation")
	String reservationIndividual();
	
	@DefaultMessage("Student Group Reservation")
	String reservationStudentGroup();
	
	@DefaultMessage("Curriculum Reservation")
	String reservationCurriculum();
	
	@DefaultMessage("Course Reservation")
	String reservationCourse();
	
	@DefaultMessage("Individual")
	String reservationIndividualAbbv();
	
	@DefaultMessage("Student Group")
	String reservationStudentGroupAbbv();
	
	@DefaultMessage("Curriculum")
	String reservationCurriculumAbbv();
	
	@DefaultMessage("Course")
	String reservationCourseAbbv();
	
	@DefaultMessage("Override")
	String reservationOverrideAbbv();
	
	@DefaultMessage("Unknown")
	String reservationUnknownAbbv();
	
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
	
	@DefaultMessage("Student Sectioning Solver")
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
	
	@DefaultMessage("classification")
	String tagClassification();
	
	@DefaultMessage("curriculum")
	String tagCurriculum();
	
	@DefaultMessage("mode")
	String tagReservationMode();
	
	@DefaultMessage("type")
	String tagReservationType();
	
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
	
	@DefaultMessage("All Sessions")
	String attrFlagAllSessions();
	
	@DefaultMessage("Conflicts")
	String attrFlagShowConflicts();
	
	@DefaultMessage("For an up to date version of the {0} event, please visit <a href=\"{1}/selectPrimaryRole.do?target=gwt.jsp%3Fpage%3Devents%23event%3D{2}%26term%3D{3}\" style='color: inherit; background-color : transparent;'>{1}</a>.")
	String emailOpenEventOnline(String eventName, String uniTimeUrl, Long eventId, Long sessionId);
	
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
	
	@DefaultMessage("Cancelled")
	String reservationCancelledClass();
	
	@DefaultMessage("All")
	String departmentsAllLabel();

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
	
	@DefaultMessage("Student")
	String enrollmentRoleStudent();
	
	@DefaultMessage("Instructor")
	String enrollmentRoleInstructor();
}