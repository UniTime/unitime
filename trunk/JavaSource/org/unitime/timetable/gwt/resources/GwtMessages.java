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
	
	@DefaultMessage("Times:")
	String propTimes();
	
	@DefaultMessage("Locations:")
	String propLocations();
	
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
	
	@DefaultMessage("<u>P</u>rint")
	String buttonPrint();
	
	@DefaultMessage("E<u>x</u>port")
	String buttonExport();
	
	@DefaultMessage("M<u>o</u>re&nbsp;&or;")
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
	
	@DefaultMessage("S<u>o</u>rt&nbsp;by&nbsp;&or;")
	String buttonSortBy();

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
	String waitLoadingData(String session);
	
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
	
	@DefaultMessage("Failed to load {0}: {1}")
	String failedLoad(String name, String reason);
	
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
	
	@DefaultMessage("No rooms are matching the filter.")
	String errorNoMatchingRooms();
	
	@DefaultMessage("Wrong event id provided.")
	String errorBadEventId();

	@DefaultMessage("Room")
	String resourceRoom();
	
	@DefaultMessage("<u>T</u>imetable")
	String tabGrid();
	
	@DefaultMessage("List of <u>E</u>vents")
	String tabEventTable();
	
	@DefaultMessage("List of <u>M</u>eetings")
	String tabMeetingTable();
	
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
	
	@DefaultMessage("Add Event")
	String pageAddEvent();
	
	@DefaultMessage("Edit Event")
	String pageEditEvent();

	@DefaultMessage("Event Detail")
	String pageEventDetail();
	
	@DefaultMessage("Edit Room Event Availability")
	String pageEditRoomEventAvailability();
	
	@DefaultMessage("N/A")
	String itemNotApplicable();
	
	@DefaultMessage("All Departments")
	String itemAllDepartments();
	
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
	
	@DefaultMessage("<span title='Conflicting event' style='font-style:normal;'>&#9785;</span>")
	String signConflict();
	
	@DefaultMessage("<span title='Selected event' style='font-style:normal;'>&#9745;</span>")
	String signSelected();

	@DefaultMessage("<span title='Warning' style='font-style:normal;'>&#9888;</span>")
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
}