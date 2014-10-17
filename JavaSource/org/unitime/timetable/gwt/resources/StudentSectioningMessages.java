/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

/**
 * @author Tomas Muller
 */
public interface StudentSectioningMessages extends Messages {
	/*  General messages
	 */
	@DefaultMessage("{0} {1}")
	String courseName(String subject, String courseNbr);
	
	@DefaultMessage("{0} {1} - {2}")
	String courseNameWithTitle(String subject, String courseNbr, String title);

	
	/*  Common column names
	 */
	@DefaultMessage("Lock")
	String colLock();
	
	@DefaultMessage("Subject")
	String colSubject();
	
	@DefaultMessage("Course")
	String colCourse();
	
	@DefaultMessage("Type")
	String colSubpart();
	
	@DefaultMessage("Class")
	String colClass();
	
	@DefaultMessage("Avail")
	String colLimit();
	
	@DefaultMessage("Days")
	String colDays();
	
	@DefaultMessage("Time")
	String colTime();

	@DefaultMessage("Start")
	String colStart();
	
	@DefaultMessage("End")
	String colEnd();
	
	@DefaultMessage("Date")
	String colDate();
	
	@DefaultMessage("Room")
	String colRoom();
	
	@DefaultMessage("Instructor")
	String colInstructor();
	
	@DefaultMessage("Requires")
	String colParent();
	
	@DefaultMessage("Conflict")
	String colConflictType();

	@DefaultMessage("Name")
	String colConflictName();

	@DefaultMessage("Date")
	String colConflictDate();

	@DefaultMessage("Time")
	String colConflictTime();

	@DefaultMessage("Room")
	String colConflictRoom();
	
	@DefaultMessage("&nbsp;")
	String colIcons();

	@DefaultMessage("&nbsp;")
	String colSaved();
	
	@DefaultMessage("&nbsp;")
	String colHighDemand();

	@DefaultMessage("Title")
	String colTitle();
	
	@DefaultMessage("Note")
	String colNote();

	@DefaultMessage("Credit")
	String colCredit();

	@DefaultMessage("Year")
	String colYear();
	
	@DefaultMessage("Term")
	String colTerm();
	
	@DefaultMessage("Campus")
	String colCampus();

	@DefaultMessage("&nbsp;")
	String colNoteIcon();

	/* Academic Session Selector messages
	 */
	@DefaultMessage("No academic session is selected.")
	String sessionSelectorNoSession();
	
	@DefaultMessage("Click here to change the session.")
	String sessionSelectorHint();
	
	@DefaultMessage("Select Academic Session ...")
	String sessionSelectorSelect();
	
	@DefaultMessage("Loading academic sessions ...")
	String sessionSelectorLoading();
	
	@DefaultMessage("Session: {1} {0} ({2})")
	String sessionSelectorLabel(String year, String term, String campus);
	
	@DefaultMessage("{1} {0} ({2})")
	String sessionName(String year, String term, String campus);
	
	/* Course Requests Table messages
	 */
	@DefaultMessage("Validating...")
	String courseRequestsValidating();

	@DefaultMessage("Scheduling...")
	String courseRequestsScheduling();
	
	@DefaultMessage("Saving...")
	String courseRequestsSaving();
	
	@DefaultMessage("Loading...")
	String courseRequestsLoading();
	
	@DefaultMessage("Validation failed, see above for errors.")
	String validationFailed();

	@DefaultMessage("Validation failed: {0}")
	String validationFailedWithMessage(String message);

	@DefaultMessage("Course {0} used multiple times.")
	String validationMultiple(String course);
	
	@DefaultMessage("No primary course provided.")
	String validationNoCourse();
	
	@DefaultMessage("No alternative for a free time.")
	String validationFreeTimeWithAlt();
	
	@DefaultMessage("No free time alternative.")
	String validationAltFreeTime();
	
	@DefaultMessage("No first alternative provided.")
	String validationSecondAltWithoutFirst();
	
	@DefaultMessage("Course {0} does not exist.")
	String validationCourseNotExists(String course);

	@DefaultMessage("Course does not exist.")
	String validationUnknownCourseNotExists();
	
	@DefaultMessage("Course Requests")
	String courseRequestsCourses();
	
	@DefaultMessage("&darr; Wait-List")
	String courseRequestsWaitList();

	@DefaultMessage("{0}. Priority")
	String courseRequestsPriority(int i);
	
	@DefaultMessage("Alternate Course Requests")
	String courseRequestsAlternatives();
	
	@DefaultMessage("(used only if a course requested above is not available)")
	String courseRequestsAlternativesNote();

	@DefaultMessage("{0}. Alternate")
	String courseRequestsAlternative(int i);
	
	@DefaultMessage("Alternative to {0}")
	String courseRequestsHintAlt(String course);
	
	@DefaultMessage("Alt. to {0} & {1}")
	String courseRequestsHintAlt2(String course, String altCourse);
	
	@DefaultMessage("Course with the second highest priority.")
	String courseRequestsHint1();
	
	@DefaultMessage("Enter a course name, e.g., ENG 10600")
	String courseRequestsHint3();

	@DefaultMessage("or a free time, e.g., Free MWF 7:30 - 8:30")
	String courseRequestsHint4();
	
	@DefaultMessage("Course with the lowest priority.")
	String courseRequestsHint8();
	
	@DefaultMessage("Alternate request if course(s) above not available.")
	String courseRequestsHintA0();
	
	/* Course Selection Box messages
	 */
	@DefaultMessage("Course Finder")
	String courseSelectionDialog();
	
	@DefaultMessage("No course selected.")
	String courseSelectionNoCourseSelected();
	
	@DefaultMessage("<u>D</u>etails")
	String courseSelectionDetails();
	
	@DefaultMessage("<u>L</u>ist of classes")
	String courseSelectionClasses();
	
	@DefaultMessage("<u>C</u>ourses")
	String courseSelectionCourses();
	
	@DefaultMessage("Free <u>T</u>ime")
	String courseSelectionFreeTime();
		
	@DefaultMessage("No course filter set.")
	String courseSelectionNoCourseFilter();
	
	@DefaultMessage("Looking for courses ...")
	String courseSelectionLoadingCourses();
	
	@DefaultMessage("Course {0} has no classes.")
	String courseSelectionNoClasses(String course);
	
	@DefaultMessage("Loading classes ...")
	String courseSelectionLoadingClasses();
	
	@DefaultMessage("Loading course details ...")
	String courseSelectionLoadingDetails();
	
	@DefaultMessage("Invalid free time.")
	String invalidFreeTime();
	
	@DefaultMessage("No free time entered.")
	String courseSelectionNoFreeTime();
	
	@DefaultMessage("Unable to interpret {0} as free time (error at position {1}).")
	String invalidFreeTimeGeneric(String text, int pos);
	
	@DefaultMessage("Unable to interpret {0} as free time (expected a day or a number at position {1}).")
	String invalidFreeTimeExpectedDayOrNumber(String text, int pos);
	
	@DefaultMessage("Unable to interpret {0} as free time (expected a number at position {1}).")
	String invalidFreeTimeExpectedNumber(String text, int pos);
	
	@DefaultMessage("Unable to interpret {0} as free time (start time before {1}).")
	String invalidFreeTimeStartBeforeFirst(String text, String first);
	
	@DefaultMessage("Unable to interpret {0} as free time (start time after {1}).")
	String invalidFreeTimeStartAfterLast(String text, String last);

	@DefaultMessage("Unable to interpret {0} as free time (end time before {1}).")
	String invalidFreeTimeEndBeforeFirst(String text, String first);
	
	@DefaultMessage("Unable to interpret {0} as free time (end time after {1}).")
	String invalidFreeTimeEndAfterLast(String text, String last);

	@DefaultMessage("Unable to interpret {0} as free time (start time is not before end time).")
	String invalidFreeTimeStartNotBeforeEnd(String text);
	
	@DefaultMessage("Unable to interpret {0} as free time (invalid start time).")
	String invalidFreeTimeInvalidStartTime(String text);

	@DefaultMessage("Unable to interpret {0} as free time (invalid end time).")
	String invalidFreeTimeInvalidEndTime(String text);

	/* Suggestion Box messages
	 */
	@DefaultMessage("Waiting for alternatives ...")
	String suggestionsLoading();
	
	@DefaultMessage("Alternatives for {0}")
	String suggestionsAlternatives(String source);

	@DefaultMessage("There are no alternatives for {0}.")
	String suggestionsNoAlternative(String source);

	@DefaultMessage("There are no alternatives for {0} matching {1}.")
	String suggestionsNoAlternativeWithFilter(String source, String filter);

	@DefaultMessage("Free Time {0} {1} - {2}")
	String freeTime(String days, String start, String end);

	@DefaultMessage("{0} {1}")
	String course(String subject, String course);

	@DefaultMessage("{0} {1} {2} {3}")
	String clazz(String subject, String course, String subpart, String section);
	
	/* Time Grid messages
	 */
	@DefaultMessage("Send {0} an email.")
	String sendEmail(String name);
	
	@DefaultMessage("(~{0} min)")
	String distanceConflict(int distanceInMinutes);
	
	/* Student Sectioning widget messages
	 */
	@DefaultMessage("Add/Drop <u>C</u>ourses")
	String buttonRequests();
	
	@DefaultMessage("Go back to the Course Requests.")
	String hintRequests();
	
	@DefaultMessage("Rearrange Schedule")
	String buttonReset();
	
	@DefaultMessage("Compute a brand new schedule, ignoring current class selection and/or registration.")
	String hintReset();

	@DefaultMessage("<u>B</u>uild Schedule")
	String buttonSchedule();
	
	@DefaultMessage("Compute class schedule using the entered course requests.")
	String hintSchedule();
	
	@DefaultMessage("<u>S</u>ubmit Schedule")
	String buttonEnroll();
	
	@DefaultMessage("Register for the above schedule.")
	String hintEnroll();
	
	@DefaultMessage("<u>P</u>rint")
	String buttonPrint();
	
	@DefaultMessage("Print the currently selected schedule.")
	String hintPrint();
	
	@DefaultMessage("E<u>x</u>port")
	String buttonExport();
	
	@DefaultMessage("Export the currently selected schedule in iCalendar format.")
	String hintExport();

	@DefaultMessage("<u>S</u>ubmit Requests")
	String buttonSave();
	
	@DefaultMessage("Submit course requests.")
	String hintSave();
	
	@DefaultMessage("<u>S</u>elect")
	String buttonSelect();
	
	@DefaultMessage("Current Registration")
	String buttonStartOver();
	
	@DefaultMessage("Discard all changes and go back to your current registration.")
	String hintStartOver();

	@DefaultMessage("<u>L</u>ist of Classes")
	String tabClasses();
	
	@DefaultMessage("<u>T</u>ime Grid")
	String tabTimetable();
	
	@DefaultMessage("Requests stored.")
	String saveRequestsOK();
	
	@DefaultMessage("Unable to store requests: {0}")
	String saveRequestsFail(String reason);

	@DefaultMessage("Success!")
	String enrollOK();
	
	@DefaultMessage("Registration failed: {0}")
	String enrollFailed(String reason);
	
	@DefaultMessage("Student Schedule")
	String studentSchedule();

	@DefaultMessage("Preliminary Student Schedule")
	String studentScheduleNotEnrolled();

	@DefaultMessage("Free")
	String freeTimeSubject();

	@DefaultMessage("Time")
	String freeTimeCourse();
	
	@DefaultMessage("Computed schedule is empty.")
	String noSchedule();
	
	@DefaultMessage("There is no schedule.")
	String emptySchedule();
	
	/* User Authentication messages
	 */
	@DefaultMessage("User: Not authenticated")
	String userNotAuthenticated();
	
	@DefaultMessage("Click here to authenticate.")
	String userHint();
	
	@DefaultMessage("Click here to log in.")
	String userHintLogin();

	@DefaultMessage("Click here to log out.")
	String userHintLogout();
	
	@DefaultMessage("Click here to lookup a student.")
	String userHintLookup();
	
	@DefaultMessage("You can close the window now.")
	String userHintClose();

	@DefaultMessage("Please Log In ...")
	String dialogAuthenticate();
	
	@DefaultMessage("Please enter your PIN ...")
	String dialogPin();
	
	@DefaultMessage("Username:")
	String username();
	
	@DefaultMessage("Password:")
	String password();
	
	@DefaultMessage("PIN:")
	String pin();
	
	@DefaultMessage("Log&nbsp;In")
	String buttonUserLogin();
	
	@DefaultMessage("Guest")
	String buttonUserSkip();
	
	@DefaultMessage("Lookup")
	String buttonUserLookup();
	
	@DefaultMessage("<u>O</u>k")
	String buttonSetPin();

	@DefaultMessage("Guest")
	String userGuest();
	
	@DefaultMessage("User: {0}")
	String userLabel(String user);
	
	/* Validation Error messages
	 */
	@DefaultMessage("Please wait ...")
	String pleaseWait();
	
	/* Web Table messages
	 */
	@DefaultMessage("No data.")
	String tableEmpty();
	
	/* Interface messages
	 */
	@DefaultMessage("Distance to travel from {0} is approx. {1} minutes.")
	String backToBackDistance(String rooms, int distanceInMinutes);
	
	@DefaultMessage("&infin;")
	String unlimited();
	
	@DefaultMessage("Not available.")
	String classNotAvailable();
	
	@DefaultMessage("Not assigned.")
	String courseNotAssigned();

	@DefaultMessage("Not Enrolled")
	String courseNotEnrolled();

	@DefaultMessage("Wait-Listed")
	String courseWaitListed();

	@DefaultMessage("Conflicts with {0}")
	String conflictWithFirst(String course);

	@DefaultMessage(", {0}")
	String conflictWithMiddle(String course);

	@DefaultMessage(" and/or {0}")
	String conflictWithLast(String course);
	
	@DefaultMessage(", assigned {0} instead")
	String conflictAssignedAlternative(String alt);
	
	@DefaultMessage("Failed to load the application ({0}).")
	String failedToLoadTheApp(String message);
	
	@DefaultMessage("Expected {0} students, but only {1} spaces are available, please try to avoid this class.")
	String highDemand(int expected, int available);

	@DefaultMessage("Course {0} is undergoing maintenance / changes.")
	String courseLocked(String course);
	
	@DefaultMessage("You are currently registered for {0}.")
	String saved(String clazz);

	@DefaultMessage("You are currently registered for {0}, this enrollment will get dropped.")
	String unassignment(String clazz);

	@DefaultMessage("You are currently not registered for {0}. Please click the Submit Schedule button to update your registration.")
	String assignment(String clazz);
	
	@DefaultMessage("Show unassignments")
	String showUnassignments();
	
	@DefaultMessage("Free time is not allowed.")
	String freeTimeNotAllowed();
	
	@DefaultMessage("{0} is a course.")
	String notFreeTimeIsCourse(String text);
	
	/* Enrollment table messages
	 */
	
	@DefaultMessage("Failed to load enrollments: {0}")
	String failedToLoadEnrollments(String message);

	@DefaultMessage("The selected offering has no students enrolled.")
	String offeringHasNoEnrollments();
	
	@DefaultMessage("The selected class has no students enrolled.")
	String classHasNoEnrollments();

	@DefaultMessage("Sort by {0}")
	String sortBy(String column);
	
	@DefaultMessage("Student")
	String colStudent();

	@DefaultMessage("Area")
	String colArea();

	@DefaultMessage("Clasf")
	String colClassification();

	@DefaultMessage("Major")
	String colMajor();

	@DefaultMessage("Requested")
	String colRequestTimeStamp();
	
	@DefaultMessage("Enrolled")
	String colEnrollmentTimeStamp();

	@DefaultMessage("Approved")
	String colApproved();

	@DefaultMessage("Priority")
	String colPriority();
	
	@DefaultMessage("Alternative")
	String colAlternative();

	@DefaultMessage("Reservation")
	String colReservation();
	
	@DefaultMessage("External Id")
	String colStudentExternalId();

	@DefaultMessage("{0}.")
	String priority(int priority);
	
	@DefaultMessage("Total Enrolled: {0}")
	String totalEnrolled(int count);

	@DefaultMessage("Total Requested: {0}")
	String totalRequested(int count);

	@DefaultMessage("Total Not Enrolled: {0}")
	String totalNotEnrolled(int count);
	
	@DefaultMessage("Total Wait-Listed: {0}")
	String totalWaitListed(int count);
	
	@DefaultMessage("{0} by {1}")
	String approval(String approvedDate, String approvedBy);
	
	@DefaultMessage("Select All")
	String selectAll();

	@DefaultMessage("Clear All")
	String clearAll();
	
	@DefaultMessage("Approve Selected Enrollments")
	String approveSelectedEnrollments();
	
	@DefaultMessage("<u>A</u>pprove")
	String buttonApproveSelectedEnrollments();
	
	@DefaultMessage("Failed to approve enrollments: {0}")
	String failedToApproveEnrollments(String error);

	@DefaultMessage("Reject Selected Enrollments")
	String rejectSelectedEnrollments();
	
	@DefaultMessage("<u>R</u>eject")
	String buttonRejectSelectedEnrollments();

	@DefaultMessage("Failed to reject enrollments: {0}")
	String failedToRejectEnrollments(String error);
	
	@DefaultMessage("Consent approved on {0}")
	String consentApproved(String approvedDate);
	
	@DefaultMessage("Waiting for {0}")
	String consentWaiting(String consent);

	@DefaultMessage("Group")
	String reservationGroup();

	@DefaultMessage("Individual")
	String reservationIndividual();

	@DefaultMessage("Course")
	String reservationCourse();

	@DefaultMessage("Curriculum")
	String reservationCurriculum();

	/* Enrollment dialog messages (opened from Enrollments table)
	 */
	
	@DefaultMessage("Loading classes for {0}...")
	String loadingEnrollment(String student);

	@DefaultMessage("Classes for {0}")
	String dialogEnrollments(String student);
	
	@DefaultMessage("Show External Ids")
	String showExternalIds();
	
	@DefaultMessage("Show Class Numbers")
	String showClassNumbers();
	
	@DefaultMessage("Export in iCalendar format.")
	String exportICalendar();
	
	@DefaultMessage("Enrollments")
	String enrollmentsTable();
	
	/* Sectioning exceptions
	 */
	@DefaultMessage("Course {0} does not exist.")
	String exceptionCourseDoesNotExist(String course);
	
	@DefaultMessage("Academic session {0} does not exist.")
	String exceptionSessionDoesNotExist(String session);
	
	@DefaultMessage("Academic session not selected.")
	String exceptionNoAcademicSession();
	
	@DefaultMessage("No suitable academic sessions found.")
	String exceptionNoSuitableAcademicSessions();
	
	@DefaultMessage("No classes found for {0}.")
	String exceptionNoClassesForCourse(String course);
	
	@DefaultMessage("Unable to compute a schedule ({0}).")
	String exceptionSectioningFailed(String message);
	
	@DefaultMessage("Unable to compute alternatives ({0}).")
	String exceptionSuggestionsFailed(String message);
	
	@DefaultMessage("Too many bad attempts, login disabled.")
	String exceptionTooManyLoginAttempts();
	
	@DefaultMessage("User name not provided.")
	String exceptionLoginNoUsername();
	
	@DefaultMessage("Wrong username and/or password.")
	String exceptionLoginFailed();
	
	@DefaultMessage("Login failed ({0}).")
	String exceptionLoginFailedUnknown(String message);
	
	@DefaultMessage("User is not logged in.")
	String exceptionUserNotLoggedIn();

	@DefaultMessage("Unable to load section information ({0}).")
	String exceptionCustomSectionNamesFailed(String reason);
	
	@DefaultMessage("Unable to retrive course details ({0}).")
	String exceptionCustomCourseDetailsFailed(String reason);
	
	@DefaultMessage("Unable to retrive class details ({0}).")
	String exceptionCustomSectionLimitsFailed(String reason);
	
	@DefaultMessage("Course detail interface not provided.")
	String exceptionNoCustomCourseDetails();
	
	@DefaultMessage("No details are available for course {0} {1}.")
	String infoCourseDetailsNotAvailable(String subject, String courseNbr);
	
	@DefaultMessage("Failed to load course details: {0}")
	String failedLoadCourseDetails(String error);
	
	@DefaultMessage("Last academic session failed ({0}).")
	String exceptionLastAcademicSessionFailed(String message);
	
	@DefaultMessage("Not a student.")
	String exceptionNoStudent();
	
	@DefaultMessage("Wrong student id.")
	String exceptionBadStudentId();
	
	@DefaultMessage("No requests stored for the student.")
	String exceptionNoRequests();
	
	@DefaultMessage("Online student scheduling is not available for this academic session.")
	String exceptionBadSession();
	
	@DefaultMessage("Student sectioning solver is not loaded in memory.")
	String exceptionNoSolver();

	
	@DefaultMessage("You are not authenticated, please log in first.")
	String exceptionEnrollNotAuthenticated();
	
	@DefaultMessage("You are not registered as a student in {0}.")
	String exceptionEnrollNotStudent(String session);
	
	@DefaultMessage("Unable to register for {0}, the class is no longer available.")
	String exceptionEnrollNotAvailable(String clazz);
	
	@DefaultMessage("Unable to register for {0}, the class is no longer available (it is after the deadline).")
	String exceptionEnrollDeadlineChange(String clazz);

	@DefaultMessage("Unable to register for {0}, the class is no longer available (it is after the deadline).")
	String exceptionEnrollDeadlineNew(String clazz);
	
	@DefaultMessage("Unable to drop from {0} (it is after the deadline).")
	String exceptionEnrollDeadlineDrop(String clazz);
	
	@DefaultMessage("Unable to register for {0}, registration is incomplete.")
	String exceptionEnrollmentIncomplete(String course);
	
	@DefaultMessage("Unable to register for {0}, registration is overlapping.")
	String exceptionEnrollmentOverlapping(String course);
	
	@DefaultMessage("Unable to register for {0}, registration is invalid.")
	String exceptionEnrollmentInvalid(String course);

	@DefaultMessage("Unable to register for {0}, registration is conflicting.")
	String exceptionEnrollmentConflicting(String course);

	@DefaultMessage("This feature is not supported in the current environment.")
	String exceptionNotSupportedFeature();
	
	@DefaultMessage("No schedule stored for the student.")
	String exceptionNoSchedule();
	
	@DefaultMessage("No courses requested.")
	String exceptionNoCourse();
	
	@DefaultMessage("Unable to compute a schedule (no solution found).")
	String exceptionNoSolution();

	@DefaultMessage("{0}")
	String exceptionUnknown(String reason);
	
	@DefaultMessage("Academic session is not available for student scheduling.")
	String exceptionNoServerForSession();

	@DefaultMessage("Wrong class or instructional offering.")
	String exceptionBadClassOrOffering();
	
	@DefaultMessage("Wrong instructional offering.")
	String exceptionBadOffering();
	
	@DefaultMessage("Wrong course offering.")
	String exceptionBadCourse();

	@DefaultMessage("Insufficient user privileges.")
	String exceptionInsufficientPrivileges();
	
	@DefaultMessage("Your timetabling session has expired. Please log in again.")
	String exceptionHttpSessionExpired();
	
	@DefaultMessage("Login is required to use this page.")
	String exceptionLoginRequired();
	
	@DefaultMessage("Student emails are disabled.")
	String exceptionStudentEmailsDisabled();
	
	@DefaultMessage("Failed to create enrollment provider: {0}")
	String exceptionStudentEnrollmentProvider(String message);
	
	@DefaultMessage("Eligibility check failed: {0}")
	String exceptionFailedEligibilityCheck(String message);
	
	@DefaultMessage("Processing...")
	String waitEnroll();
	
	@DefaultMessage("Checking eligibility...")
	String waitEligibilityCheck();
	
	@DefaultMessage("Filter assignments of the selected class by name, day, start time, date, room or instructor." +
			"<br><br>You can also use the following tags:" +
			"<ul>" +
			"<li><i>name:</i> class name" + 
			"<li><i>day:</i> class must meet on this day or days (e.g., monday, MWF)" + 
			"<li><i>time:</i> class must start at this time (e.g., 730)" +
			"<li><i>before:</i> class must end before or by this time" +
			"<li><i>after:</i> class must start on or after this time" +
			"<li><i>date:</i> class must meet on this date" +
			"<li><i>room:</i> class must use this room or building" +
			"<li><i>instructor:</i> class must have this instructor" +
			"</ul>Use <i>or</i>, <i>and</i>, <i>not</i>, and brackets to build a boolean query." +
			"<br><br>Example: day: monday and (time: 730 or time: 830)")
	String suggestionsFilterHint();
	
	@DefaultMessage("Changes to the selected free time {0} are in <font color='blue'>blue</font>, classes to be removed from the schedule are in <font color='red'>red</font>. Changes to other classes or free times are in <font color='black'>black</font>.")
	String suggestionsLegendOnFreeTime(String freeTime);

	@DefaultMessage("Changes to the selected class {0} are in <font color='blue'>blue</font>, classes to be removed from the schedule are in <font color='red'>red</font>. Changes to other classes or free times are in <font color='black'>black</font>.")
	String suggestionsLegendOnClass(String clazz);
	
	@DefaultMessage("Changes to the selected course {0} are in <font color='blue'>blue</font>, classes to be removed from the schedule are in <font color='red'>red</font>. Changes to other courses or free times are in <font color='black'>black</font>.")
	String suggestionsLegendOnCourse(String course);

	@DefaultMessage("<u>S</u>earch")
	String buttonSearch();
	
	@DefaultMessage("Overlaps with {0}")
	String noteAllowedOverlapFirst(String classOrCourse);
	
	@DefaultMessage(", {0}")
	String noteAllowedOverlapMiddle(String classOrCourse);

	@DefaultMessage(" and {0}")
	String noteAllowedOverlapLast(String classOrCourse);
	
	@DefaultMessage("Filter:")
	String filter();
	
	@DefaultMessage("Loading data...")
	String loadingData();
	
	@DefaultMessage("Available")
	String colAvailable();
	
	@DefaultMessage("Projection")
	String colProjection();
	
	@DefaultMessage("Enrollment")
	String colEnrollment();
	
	@DefaultMessage("Not-Enrolled")
	String colWaitListed();
	
	@DefaultMessage("Reservation")
	String colReserved();
	
	@DefaultMessage("Consent")
	String colConsent();
	
	@DefaultMessage("Coordinator")
	String colCoordinator();

	@DefaultMessage("Enrollments of {0}")
	String titleEnrollments(String courseOrClass);
	
	@DefaultMessage("Total")
	String total();
	
	@DefaultMessage("Limit not defined.")
	String availableNoLimit();
	
	@DefaultMessage("Unlimited, reservation required")
	String availableUnlimitedWithReservation();
	
	@DefaultMessage("Unlimited, reservation not needed")
	String availableUnlimited();
	
	@DefaultMessage("No space available, limit of {0} spaces has been reached")
	String availableNot(int limit);
	
	@DefaultMessage("Available {0} out of {1} spaces, reservation not needed")
	String available(int available, int limit);
	
	@DefaultMessage("Available {0} out of {1} spaces, reservation required")
	String availableWithReservation(int available, int limit);
	
	@DefaultMessage("Available {0} out of {1} spaces, reservation required for {2} of them")
	String availableSomeReservation(int available, int limit, int availableWithReservation);
	
	@DefaultMessage("<sup><font color='#2066CE'>r)</font></sup>")
	String htmlReservationSign();
	
	@DefaultMessage("<sup><font color='#2066CE'>w)</font></sup>")
	String htmlWaitListSign();
	
	@DefaultMessage("No results matching filter {0} found.")
	String exceptionNoMatchingResultsFound(String filter);
	
	@DefaultMessage("Loading enrollments for {0}...")
	String loadingEnrollments(String classOrCourse);
	
	@DefaultMessage("Scheduling <u>A</u>ssistant")
	String buttonAssistant();

	@DefaultMessage("Student Scheduling Assistant for {0}")
	String dialogAssistant(String student);
	
	@DefaultMessage("Close")
	String buttonClose();
	
	@DefaultMessage("Loading scheduling assistant for {0}...")
	String loadingAssistant(String student);
		
	@DefaultMessage("Need<br>Consent")
	String colNeedConsent();
	
	@DefaultMessage("<u>E</u>nrollments")
	String tabEnrollments();

	@DefaultMessage("<u>S</u>tudents")
	String tabStudents();
	
	@DefaultMessage("Change <u>L</u>og")
	String tabChangeLog();

	@DefaultMessage("<sup><font color='#2066CE'>({0}p)</font></sup>")
	String firstWaitListedPrioritySign(int priority);
	
	@DefaultMessage("r) Space available only with a reservation.<br>w) Wait-listed requests.")
	String sectioningStatusReservationHint();
	
	@DefaultMessage("(p) denotes priority of the first wait-listed course request.")
	String sectioningStatusPriorityHint();
	
	@DefaultMessage("Enrollment disabled.")
	String exceptionEnrollmentDisabled();
	
	@DefaultMessage("Access disabled.")
	String exceptionAccessDisabled();
	
	@DefaultMessage("Authentication PIN is required.")
	String exceptionAuthenticationPinRequired();
	
	@DefaultMessage("Authentication PIN was not provided.")
	String exceptionAuthenticationPinNotProvided();

	@DefaultMessage("Status")
	String colStatus();
	
	@DefaultMessage("Emailed")
	String colEmailTimeStamp();

	@DefaultMessage("Group")
	String colGroup();
	
	@DefaultMessage("Accommodation")
	String colAccommodation();
	
	@DefaultMessage("Send email...")
	String sendStudentEmail();
	
	@DefaultMessage("Change status to {0}")
	String changeStatusTo(String newStatus);
	
	@DefaultMessage("Changing status to {0}...")
	String changingStatusTo(String newStatus);
	
	@DefaultMessage("Subject:")
	String emailSubject();

	@DefaultMessage("CC:")
	String emailCC();

	@DefaultMessage("Message:")
	String emailBody();

	@DefaultMessage("Send")
	String emailSend();

	@DefaultMessage("No email on file.")
	String exceptionNoEmail();
	
	@DefaultMessage("Cancelled due to: {0}")
	String exceptionCancelled(String exception);
	
	@DefaultMessage("Change <u>L</u>og")
	String buttonChangeLog();
	
	@DefaultMessage("Loading change log for {0}...")
	String loadingChangeLog(String student);

	@DefaultMessage("Operation")
	String colOperation();

	@DefaultMessage("Date")
	String colTimeStamp();
	
	@DefaultMessage("Result")
	String colResult();

	@DefaultMessage("User")
	String colUser();

	@DefaultMessage("Message")
	String colMessage();

	@DefaultMessage("Messages")
	String tableMessages();

	@DefaultMessage("CPU Time [s]")
	String colCpuTime();
	
	@DefaultMessage("Change log for {0}")
	String dialogChangeLog(String student);
	
	@DefaultMessage("Change message for {0}")
	String dialogChangeMessage(String student);

	@DefaultMessage("Proto Buffer")
	String tableProto();
	
	@DefaultMessage("Class schedule change for %session%")
	String emailDeafultSubject();
	
	@DefaultMessage("Class Schedule")
	String emailDeafultTitle();
	
	@DefaultMessage("Message")
	String emailMessage();
	
	@DefaultMessage("List of Classes")
	String emailClassList();
	
	@DefaultMessage("Timetable")
	String emailTimetable();

	@DefaultMessage("This email was send on behalf of {0}.")
	String emailSentBy(String manager);

	@DefaultMessage("The changes in your schedule were made by {0}.")
	String emailChangesMadeBy(String manager);
	
	@DefaultMessage("For an up to date schedule, please visit <a href='{0}/gwt.jsp?page=sectioning' style='color: inherit; background-color : transparent;'>{0}</a>.")
	String emailLinkToUniTime(String baseUrl);

	@DefaultMessage("Class schedule notification for %session%")
	String emailSubjectNotification();
	
	@DefaultMessage("No enrollment change detected.")
	String emailNoChange();
	
	@DefaultMessage("Enrollment Changes")
	String emailEnrollmentChanges();
	
	@DefaultMessage("You have not been granted the {0}, please contact your advisor for further information.")
	String emailConsentRejected(String consent);
	
	@DefaultMessage("Course is wait-listed.")
	String emailCourseWaitListed();
	
	@DefaultMessage("Course is wait-listed alternative.")
	String emailCourseWaitListedAlternative();
	
	@DefaultMessage("Course is not enrolled.")
	String emailCourseNotEnrolled();
	
	@DefaultMessage("Course is not enrolled alternative.")
	String emailCourseNotEnrolledAlternative();

	@DefaultMessage("Course {0} {1} dropped due to a reject.")
	String emailCourseDropReject(String subject, String courseNbr);
	

	@DefaultMessage("Course {0} {1} dropped due to a course change.")
	String emailCourseDropChange(String subject, String courseNbr);
	
	@DefaultMessage("Enrollment changed in {0} {1}")
	String emailEnrollmentChanged(String subject, String courseNbr);
	
	@DefaultMessage("{0} {1} Enrollment")
	String emailCourseEnrollment(String subject, String courseNbr);

	@DefaultMessage("You are now enrolled in {0} {1}")
	String emailEnrollmentNew(String subject, String courseNbr);

	@DefaultMessage("No class schedule.")
	String emailNoSchedule();
	
	@DefaultMessage("wait-listed")
	String emailWaitListedRequest();
	
	@DefaultMessage("wait-listed alternative")
	String emailWaitListedAlternativeRequest();
	
	@DefaultMessage("not enrolled")
	String emailNotEnrolledRequest();
	
	@DefaultMessage("not enrolled alternative")
	String emailNotEnrolledAlternativeRequest();

	@DefaultMessage("Arrange Hours")
	String emailArrangeHours();
	
	@DefaultMessage("Mass cancel...")
	String massCancel();
	
	@DefaultMessage("Status:")
	String newStatus();
	
	@DefaultMessage("Cancel Students")
	String buttonMassCancel();
	
	@DefaultMessage("Cancelling selected students...")
	String massCanceling();
	
	@DefaultMessage("Schedule cancelation for %session%")
	String defaulSubjectMassCancel();
	
	@DefaultMessage("Class schedule for %session%")
	String defaulSubject();
	
	@DefaultMessage("Mass cancellation is an irreversible operation that will delete all class enrollments and course requests for the selected student(s). Are you sure to do that?")
	String massCancelConfirmation();
	
	@DefaultMessage("<i>Arrange Hours</i>")
	String arrangeHours();
	
	@DefaultMessage("Wait-List")
	String toggleWaitList();
	
	@DefaultMessage("Course:")
	String propCourse();
	
	@DefaultMessage("Student:")
	String propStudent();
	
	@DefaultMessage("You are not registered for any classes yet. Please click the Submit Schedule button in order to complete your registration..")
	String warnScheduleEmpty();
	
	@DefaultMessage("You have made some changes in your schedule. Please click the Submit Schedule button to update your registration.")
	String warnScheduleChanged();
		
	@DefaultMessage("There are unsaved changes in your schedule. Do you really want to discard these changes without updating your registration?")
	String queryLeaveChanges();
	
	@DefaultMessage("Total Credit: {0,number,0.#}")
	String totalCredit(float total);
	
	@DefaultMessage("Request Update")
	String requestStudentUpdate();
	
	@DefaultMessage("Requesting student update...")
	String requestingStudentUpdate();
	
	@DefaultMessage("Student update request not allowed.")
	String exceptionRequestStudentUpdateNotAllowed();
	
	@DefaultMessage("Student update successfully requested. Please wait a while for the synchronization to take place.")
	String requestStudentUpdateSuccess();
	
	@DefaultMessage("Click to lock the class. Alternatives cannot change classes that are locked, except of the one that was clicked.")
	String hintUnlocked();
	
	@DefaultMessage("The class is locked. Alternatives cannot change classes that are locked, except of the one that was clicked.")
	String hintLocked();
}