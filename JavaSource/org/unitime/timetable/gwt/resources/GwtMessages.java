/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.resources;

import com.google.gwt.i18n.client.Messages;

public interface GwtMessages extends Messages {
	
	@DefaultMessage("{0} Help")
	String pageHelp(String pageTitle);

	@DefaultMessage("Version {0} built on {1}")
	String pageVersion(String version, String buildDate);
	
	@DefaultMessage("&copy; 2008 - 2013 UniTime LLC,<br>distributed under GNU General Public License.")
	String pageCopyright();
	
	@DefaultMessage("UniTime {0}, \u00A9 2008 - 2013 UniTime LLC, distributed under GNU GPL.")
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
	
	@DefaultMessage("&#10007; Remove")
	String opDeleteSelectedMeetings();
	
	@DefaultMessage("&#10008 Remove All")
	String opDeleteNewMeetings();
	
	@DefaultMessage("Setup Times ...")
	String opChangeOffsets();
	
	@DefaultMessage("Modify ...")
	String opModifyMeetings();

	@DefaultMessage("<b><i>+</i></b> Add Meetings ...")
	String opAddMeetings();

	@DefaultMessage("Sort by {0}")
	String opSortBy(String column);
	
	@DefaultMessage("&#9744; {0}")
	String opShow(String column);

	@DefaultMessage("&#9746; {0}")
	String opHide(String column);
	
	@DefaultMessage("&#10003; Approve ...")
	String opApproveSelectedMeetings();

	@DefaultMessage("&#10004; Approve All ...")
	String opApproveAllMeetings();

	@DefaultMessage("&#10007; Reject ...")
	String opRejectSelectedMeetings();

	@DefaultMessage("&#10008; Reject All ...")
	String opRejectAllMeetings();

	@DefaultMessage("&#10007; Cancel ...")
	String opCancelSelectedMeetings();

	@DefaultMessage("&#10008; Cancel All ...")
	String opCancelAllMeetings();

	@DefaultMessage("<i>?</i> Inquire ...")
	String opInquireSelectedMeetings();

	@DefaultMessage("<b><i>?</i></b> Inquire ...")
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
	
	@DefaultMessage("Copy iCalendar URL")
	String opCopyToClipboardICalendar();
	
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
	
	@DefaultMessage("Save")
	String opScriptSave();
	
	@DefaultMessage("Delete")
	String opScriptDelete();
	
	@DefaultMessage("Update")
	String opScriptUpdate();
	
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
	
	@DefaultMessage("Show Names")
	String opShowNames();
	
	@DefaultMessage("Show Abbreviations")
	String opShowAbbreviations();
	
	@DefaultMessage("Swap Axes")
	String opSwapAxes();
	
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

	@DefaultMessage("Approved")
	String colApproval();
	
	@DefaultMessage("Status")
	String colStatus();

	@DefaultMessage("Name")
	String colName();
	
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

	@DefaultMessage("Conflicts with {0}")
	String conflictWith(String event);
	
	@DefaultMessage("pending")
	String approvalNotApproved();

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
	
	@DefaultMessage("Conflicts with: ")
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

	@DefaultMessage("<u>O</u>k")
	String buttonOk();

	@DefaultMessage("<u>C</u>ancel")
	String buttonCancel();
	
	@DefaultMessage("<u>C</u>ancel Event")
	String buttonCancelEvent();

	@DefaultMessage("<u>S</u>earch")
	String buttonSearch();
	
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
	
	@DefaultMessage("{0} timetable for {1}")
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
	
	@DefaultMessage("Loading {0} timetable for {1} ...")
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
	
	@DefaultMessage("Uncaught exception: {0}")
	String failedUncaughtException(String reason);
	
	@DefaultMessage("Failed to load the page: {0}")
	String failedToLoadPage(String reason);
	
	@DefaultMessage("Failed to load the page: page not provided.")
	String failedToLoadPageNotProvided();
	
	@DefaultMessage("Failed to load the page: page {0} not registered.")
	String failedToLoadPageNotRegistered(String page);
	
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
	
	@DefaultMessage("No person matching the query found.")
	String errorNoPersonMatchingQuery();
	
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
	
	@DefaultMessage("<u>T</u>imetable")
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
	
	@DefaultMessage("Configuration {0} ({1})")
	String selectionConfiguration(String name, String limit);
	
	@DefaultMessage("unlimited")
	String configUnlimited();
	
	@DefaultMessage("Total {0} Enrollment")
	String totalEnrollmentOfType(String type);
	
	@DefaultMessage("Add Event")
	String pageAddEvent();
	
	@DefaultMessage("Edit Event")
	String pageEditEvent();

	@DefaultMessage("Event Detail")
	String pageEventDetail();
	
	@DefaultMessage("Edit Room Event Availability")
	String pageEditRoomEventAvailability();
	
	@DefaultMessage("Edit {0}")
	String pageEdit(String name);
	
	@DefaultMessage("Add {0}")
	String pageAdd(String name);
	
	@DefaultMessage("Academic Area")
	String pageAcademicArea();
	
	@DefaultMessage("Academic Areas")
	String pageAcademicAreas();
	
	@DefaultMessage("Academic Classification")
	String pageAcademicClassification();

	@DefaultMessage("Academic Classifications")
	String pageAcademicClassifications();
	
	@DefaultMessage("Course Credit Format")
	String pageCourseCreditFormat();

	@DefaultMessage("Course Credit Formats")
	String pageCourseCreditFormats();
	
	@DefaultMessage("Course Credit Type")
	String pageCourseCreditType();

	@DefaultMessage("Course Credit Types")
	String pageCourseCreditTypes();
	
	@DefaultMessage("Course Credit Unit")
	String pageCourseCreditUnit();

	@DefaultMessage("Course Credit Units")
	String pageCourseCreditUnits();
	
	@DefaultMessage("Course Type")
	String pageCourseType();
	
	@DefaultMessage("Course Types")
	String pageCourseTypes();

	@DefaultMessage("Event Date Mapping")
	String pageEventDateMapping();
	
	@DefaultMessage("Event Date Mappings")
	String pageEventDateMappings();
	
	@DefaultMessage("Event Status")
	String pageEventStatus();

	@DefaultMessage("Event Statuses")
	String pageEventStatuses();
	
	@DefaultMessage("Examination Type")
	String pageExaminationType();

	@DefaultMessage("Examination Types")
	String pageExaminationTypes();
	
	@DefaultMessage("Instructor Role")
	String pageInstructorRole();
	
	@DefaultMessage("Instructor Roles")
	String pageInstructorRoles();
	
	@DefaultMessage("Logging Level")
	String pageLoggingLevel();
	
	@DefaultMessage("Logging Levels")
	String pageLoggingLevels();
	
	@DefaultMessage("Major")
	String pageMajor();

	@DefaultMessage("Majors")
	String pageMajors();

	@DefaultMessage("Minor")
	String pageMinor();

	@DefaultMessage("Minors")
	String pageMinors();
	
	@DefaultMessage("Offering Consent Type")
	String pageOfferingConsentType();
	
	@DefaultMessage("Offering Consent Types")
	String pageOfferingConsentTypes();
	
	@DefaultMessage("Permission")
	String pagePermission();
	
	@DefaultMessage("Permissions")
	String pagePermissions();
	
	@DefaultMessage("Position Type")
	String pagePositionType();
	
	@DefaultMessage("Position Types")
	String pagePositionTypes();
	
	@DefaultMessage("Room Feature Type")
	String pageRoomFeatureType();
	
	@DefaultMessage("Room Feature Types")
	String pageRoomFeatureTypes();
	
	@DefaultMessage("Standard Event Note")
	String pageStandardEventNote();
	
	@DefaultMessage("Standard Event Notes")
	String pageStandardEventNotes();
	
	@DefaultMessage("Student Group")
	String pageStudentGroup();
	
	@DefaultMessage("Student Groups")
	String pageStudentGroups();
	
	@DefaultMessage("Student Scheduling Status Type")
	String pageStudentSchedulingStatusType();
	
	@DefaultMessage("Student Scheduling Status Types")
	String pageStudentSchedulingStatusTypes();
	
	@DefaultMessage("Role")
	String pageRole();
	
	@DefaultMessage("Roles")
	String pageRoles();
	
	@DefaultMessage("Add Curriculum")
	String pageAddCurriculum();
	
	@DefaultMessage("Edit Curriculum")
	String pageEditCurriculum();
	
	@DefaultMessage("Curriculum Details")
	String pageCurriculumDetails();
	
	@DefaultMessage("Curricula")
	String pageCurricula();
	
	@DefaultMessage("Course Reports")
	String pageCourseReports();
	
	@DefaultMessage("Examination Reports")
	String pageExaminationReports();
	
	@DefaultMessage("Student Sectioning Reports")
	String pageStudentSectioningReports();
	
	@DefaultMessage("Event Reports")
	String pageEventReports();
	
	@DefaultMessage("Administration Reports")
	String pageAdministrationReports();
	
	@DefaultMessage("Curriculum Requested Enrollments")
	String pageCurriculumRequestedEnrollments();
	
	@DefaultMessage("Reservation")
	String pageReservations();
	
	@DefaultMessage("Add Reservation")
	String pageAddReservation();
	
	@DefaultMessage("Edit Reservation")
	String pageEditReservation();
	
	@DefaultMessage("Curriculum Projection Rules")
	String pageCurriculumProjectionRules();
	
	@DefaultMessage("Student Scheduling Assistant")
	String pageStudentSchedulingAssistant();
	
	@DefaultMessage("Student Course Requests")
	String pageStudentCourseRequests();
	
	@DefaultMessage("Administration")
	String pageAdministration();
	
	@DefaultMessage("Events")
	String pageEvents();
	
	@DefaultMessage("Event Timetable")
	String pageEventTimetable();
	
	@DefaultMessage("Room Timetable")
	String pageRoomTimetable();
	
	@DefaultMessage("Online Student Sectioning Test")
	String pageOnlineStudentSectioningTest();
	
	@DefaultMessage("Online Student Scheduling Dashboard")
	String pageOnlineStudentSchedulingDashboard();
	
	@DefaultMessage("Student Sectioning Dashboard")
	String pageStudentSectioningDashboard();
	
	@DefaultMessage("Travel Times")
	String pageTravelTimes();
	
	@DefaultMessage("Classes")
	String pageClasses();
	
	@DefaultMessage("Examinations")
	String pageExaminations();
	
	@DefaultMessage("Personal Timetable")
	String pagePersonalTimetable();
	
	@DefaultMessage("Edit Room Availability")
	String pageEditRoomAvailability();
	
	@DefaultMessage("Scripts")
	String pageScripts();
	
	@DefaultMessage("Event Room Availability")
	String pageEventRoomAvailability();
	
	@DefaultMessage("N/A")
	String itemNotApplicable();
	
	@DefaultMessage("All Departments")
	String itemAllDepartments();
	
	@DefaultMessage("None")
	String itemNone();
	
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
	
	@DefaultMessage("Details of {0}")
	String dialogDetailsOf(String item);

	@DefaultMessage("{0}<br>{1}<br>{2} seats")
	String singleRoomSelection(String name, String type, String capacity);
	
	@DefaultMessage("{0}<br>{1}<br>{2} - {3}")
	String dateTimeHeader(String dow, String date, String start, String end);
	
	@DefaultMessage("{0}, {1} {2} - {3}")
	String dateTimeHint(String dow, String date, String start, String end);
	
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
		
	@DefaultMessage("an event")
	String anEvent();
	
	@DefaultMessage("Requested meeting is in the past or outside of {0}.")
	String conflictPastOrOutside(String academicSessionName);
	
	@DefaultMessage("{0} is not managed in UniTime or it is disabled for events.")
	String conflictNotEventRoom(String locationName);
	
	@DefaultMessage("Not allowed to request an event in {0}.")
	String conflictRoomDenied(String locationName);

	@DefaultMessage("Not enough permissions to make {0} not available.")
	String conflictCannotMakeUnavailable(String locationName);

	@DefaultMessage("<i>File {0} attached.</i>")
	String noteAttachement(String fileName);
	
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
	
	@DefaultMessage("Free For All")
	String legendFreeForAll();
	
	@DefaultMessage("")
	String codeFreeForAll();
	
	@DefaultMessage("Not Available")
	String legendNotAvailable();
	
	@DefaultMessage("N/A")
	String codeNotAvailable();
	
	@DefaultMessage("Available")
	String legendAvailable();
	
	@DefaultMessage("")
	String codeAvailable();
	
	@DefaultMessage("<u>A</u>dd Department...")
	String buttonAddDepartment();
	
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
	
	@DefaultMessage("Access")
	String toggleAccess();
	
	@DefaultMessage("Advisor")
	String toggleAdvisor();
	
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
	
	@DefaultMessage("Do you really want to execute script {0}?")
	String confirmScriptExecution(String name);
	
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
}