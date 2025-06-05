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
	
	@DefaultMessage("Set by")
	String colNoteAuthor();

	@DefaultMessage("Notes")
	String colNotes();
	
	@DefaultMessage("Request<br>Credit")
	String colRequestCredit();
	
	@DefaultMessage("Enrollment<br>Credit")
	String colEnrollCredit();

	@DefaultMessage("Credit")
	String colCredit();
	
	@DefaultMessage("Pending")
	String colPendingCredit();
	
	@DefaultMessage("Gr Md")
	String colGradeMode();
	
	@DefaultMessage("Pending Change")
	String colTitlePendingGradeMode();
	
	@DefaultMessage("Grade Mode")
	String colTitleGradeMode();
	
	@DefaultMessage("Year")
	String colYear();
	
	@DefaultMessage("Term")
	String colTerm();
	
	@DefaultMessage("Campus")
	String colCampus();

	@DefaultMessage("&nbsp;")
	String colNoteIcon();
	
	@DefaultMessage("Pref")
	String colClassSelection();
	
	@DefaultMessage("Distance<br>Conflicts")
	String colDistanceConflicts();
	
	@DefaultMessage("Longest Distance [min]")
	String colLongestDistance();
	
	@DefaultMessage("Overlap [min]")
	String colOverlapMins();
	
	@DefaultMessage("FreeTime [min]")
	String colFreeTimeOverlapMins();
	
	@DefaultMessage("Instr. Method<br>Preferences")
	String colPrefInstrMethConfs();
	
	@DefaultMessage("Section<br>Preferences")
	String colPrefSectionConfs();
	
	@DefaultMessage("Wait-List")
	String colWaitList();
	
	@DefaultMessage("No-Subs")
	String colNoSubs();
	
	@DefaultMessage("Priority")
	String colCritical();
	
	@DefaultMessage("Changes")
	String colChanges();
	
	@DefaultMessage("Preferences")
	String colPreferences();
	
	@DefaultMessage("Requirements")
	String colRequirements();
	
	@DefaultMessage("Warnings")
	String colWarnings();
	
	@DefaultMessage("Page")
	String colPage();
	
	@DefaultMessage("Can<br>Open")
	String colCanOpen();
	
	@DefaultMessage("<br>Student")
	String colCanStudentChange();
	
	@DefaultMessage("Changes<br>Advisor")
	String colCanAdvisorChange();
	
	@DefaultMessage("<br>Admin")
	String colCanAdminChange();
	
	@DefaultMessage("Position")
	String colWaitListPosition();
	
	@DefaultMessage("WL/Override")
	String colWaitListAndAllowedOverrides();
	
	@DefaultMessage("Override")
	String colAllowedOverrides();
	
	@DefaultMessage("Preference")
	String colSchedulingPreference();
	
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
	
	@DefaultMessage("Course {0} is not allowed or does not exist.")
	String validationCourseNotExists(String course);

	@DefaultMessage("Course does not exist.")
	String validationUnknownCourseNotExists();
	
	@DefaultMessage("Course Requests")
	String courseRequestsCourses();
	
	@DefaultMessage("Advisor Course Recommendations")
	String advisorRequestsCourses();
	
	@DefaultMessage("&darr; Wait-List")
	String courseRequestsWaitList();
	
	@DefaultMessage("Wait-List &darr;")
	String courseRequestsWaitListNoArrows();
	
	@DefaultMessage("&darr; No-Subs")
	String courseRequestsNoSubstitutions();
	
	@DefaultMessage("No-Subs &darr;")
	String courseRequestsNoSubstitutionsNoArrows();

	@DefaultMessage("{0}. Priority")
	String courseRequestsPriority(int i);
	
	@DefaultMessage("{0}. Alternative")
	String courseRequestsAlternative(int i);
	
	@DefaultMessage("Substitute Course Requests")
	String courseRequestsAlternatives();
	
	@DefaultMessage("Substitute Advisor Course Recommendations")
	String advisorRequestsAlternatives();
	
	@DefaultMessage("(used only if a course requested above is not available)")
	String courseRequestsAlternativesNote();

	@DefaultMessage("{0}. Substitute")
	String courseRequestsAlternate(int i);
	
	@DefaultMessage("Alternative to {0}")
	String courseRequestsHintAlt(String course);
	
	@DefaultMessage("Alt. to {0} & {1}")
	String courseRequestsHintAlt2(String course, String altCourse);
	
	@DefaultMessage("Alt. to {0}, {1}, ...")
	String courseRequestsHintAlt3(String course, String altCourse);
	
	@DefaultMessage("Course with the second highest priority.")
	String courseRequestsHint1();
	
	@DefaultMessage("Enter a course name, e.g., ENG 10600")
	String courseRequestsHint3();

	@DefaultMessage("or a free time, e.g., Free MWF 7:30 - 8:30")
	String courseRequestsHint4();
	
	@DefaultMessage("Course with the lowest priority.")
	String courseRequestsHint8();
	
	@DefaultMessage("Substitute request if course(s) above not available.")
	String courseRequestsHintA0();
	
	/* Course Selection Box messages
	 */
	@DefaultMessage("Course Finder")
	String courseSelectionDialog();
	
	@DefaultMessage("Course Finder -- Not Editable")
	String courseSelectionDialogDisabled();
	
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
	
	@DefaultMessage("Waiting for choices ...")
	String suggestionsLoadingChoices();
	
	@DefaultMessage("Alternatives for {0}")
	String suggestionsAlternatives(String source);
	
	@DefaultMessage("Choices for {0}")
	String suggestionsChoices(String source);

	@DefaultMessage("There are no alternatives for {0}.")
	String suggestionsNoAlternative(String source);

	@DefaultMessage("There are no alternatives for {0} matching {1}.")
	String suggestionsNoAlternativeWithFilter(String source, String filter);

	@DefaultMessage("{0} is not available.")
	String suggestionsNoChoices(String source);
	
	@DefaultMessage("{0} is not available (course is full).")
	String suggestionsNoChoicesCourseIsFull(String source);
	
	@DefaultMessage("{0} is not available (due to preferences selected).")
	String suggestionsNoChoicesDueToStudentPrefs(String source);
	
	@DefaultMessage("{0} conflicts with {1}.")
	String suggestionsNoChoicesCourseIsConflicting(String course, String conflict);
	
	@DefaultMessage("Include conflicting suggestions")
	String suggestionsShowAllChoices();

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
	
	@DefaultMessage("<u>G</u>rade Modes &amp; Credits")
	String buttonChangeGradeModesAndVariableCredits();
	
	@DefaultMessage("<u>G</u>rade Modes")
	String buttonChangeGradeModes();
	
	@DefaultMessage("Variable Title Course")
	String buttonRequestVariableTitleCourse();
	
	@DefaultMessage("<u>V</u>ariable Credits")
	String buttonChangeVariableCredits();
	
	@DefaultMessage("Register for the above schedule.")
	String hintEnroll();
	
	@DefaultMessage("Change grade mode of one or more courses.")
	String hintChangeGradeModes();
	
	@DefaultMessage("Change grade mode or variable credit of one or more courses.")
	String hintChangeGradeModesAndVariableCredits();
	
	@DefaultMessage("Change variable credit hours of one or more courses.")
	String hintChangeVariableCredits();
	
	@DefaultMessage("Request variable title course.")
	String hintRequestVariableTitleCourse();
	
	@DefaultMessage("Sumbit the above enrollment changes for approval.")
	String hintSpecialRegistration();
	
	@DefaultMessage("<u>P</u>rint")
	String buttonPrint();
	
	@DefaultMessage("Print the currently selected schedule.")
	String hintPrint();
	
	@DefaultMessage("E<u>x</u>port")
	String buttonExport();
	
	@DefaultMessage("E<u>x</u>port PDF")
	String buttonExportPdf();
	
	@DefaultMessage("M<u>o</u>re<span class='unitime-ButtonArrow'>&#9660;</span>")
	String buttonMoreOperations();
	
	@DefaultMessage("Export the currently selected schedule in iCalendar format.")
	String hintExport();

	@DefaultMessage("<u>S</u>ubmit Requests")
	String buttonSave();
	
	@DefaultMessage("Submit course requests.")
	String hintSave();
	
	@DefaultMessage("<u>S</u>elect")
	String buttonSelect();
	
	@DefaultMessage("Select <u>A</u>ll")
	String buttonSelectAll();
	
	@DefaultMessage("<u>P</u>ick {0}")
	String buttonPickN(int n);
	
	@DefaultMessage("Current Registration")
	String buttonStartOver();
	
	@DefaultMessage("Discard all changes and go back to your current registration.")
	String hintStartOver();
	
	@DefaultMessage("<u>C</u>ourse Requests")
	String tabRequests();

	@DefaultMessage("<u>L</u>ist of Classes")
	String tabClasses();
	
	@DefaultMessage("<u>T</u>ime Grid")
	String tabTimetable();
	
	@DefaultMessage("<u>N</u>otes")
	String tabNotes();
	
	@DefaultMessage("Requested <u>A</u>pprovals")
	String tabSpecialRegistrations();
	
	@DefaultMessage("<u>W</u>ait-Listed Courses")
	String tabWaitListedCourses();
	
	@DefaultMessage("Course requests have been successfully submitted.")
	String saveRequestsOK();
	
	@DefaultMessage("Course requests have been successfully submitted.\nDo you want to print a confirmation?")
	String saveRequestsConfirmation();
	
	@DefaultMessage("Unable to store requests: {0}")
	String saveRequestsFail(String reason);
	
	@DefaultMessage("Unable to retrieve approval requests: {0}")
	String requestSpecialRegistrationFail(String reason);
	
	@DefaultMessage("Approval request failed: {0}")
	String submitSpecialRegistrationFail(String reason);
	
	@DefaultMessage("Failed to cancel approval request: {0}")
	String cancelSpecialRegistrationFail(String reason);
	
	@DefaultMessage("Failed to load approval requests: {0}")
	String retrieveAllSpecialRegistrationsFail(String reason);
	
	@DefaultMessage("Failed to update approval request: {0}")
	String updateSpecialRegistrationFail(String reason);

	@DefaultMessage("Success!")
	String submitSecialRegistrationOK();

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
	
	@DefaultMessage("There are no course requests.")
	String emptyRequests();
	
	@DefaultMessage("There is no schedule.")
	String emptySchedule();
	
	@DefaultMessage("There are no notes.")
	String emptyNotes();
	
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
	
	@DefaultMessage("Please enter your {0} {1} PIN ...")
	String dialogPinForSession(String term, String year);
	
	@DefaultMessage("Username:")
	String username();
	
	@DefaultMessage("Password:")
	String password();
	
	@DefaultMessage("PIN:")
	String pin();
	
	@DefaultMessage("{0} {1} PIN:")
	String pinForSession(String term, String year);
	
	@DefaultMessage("Log&nbsp;In")
	String buttonUserLogin();
	
	@DefaultMessage("Guest")
	String buttonUserSkip();
	
	@DefaultMessage("Lookup")
	String buttonUserLookup();
	
	@DefaultMessage("<u>O</u>k")
	String buttonSetPin();
	
	@DefaultMessage("<u>C</u>ancel")
	String buttonCancelPin();

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
	
	@DefaultMessage("Not available (due to preferences selected).")
	String classNotAvailableDueToStudentPrefs();
	
	@DefaultMessage("Not available (course is full).")
	String courseIsFull();

	@DefaultMessage("Not assigned.")
	String courseNotAssigned();
	
	@DefaultMessage("To be wait-listed - click this line to configure additional wait-list preferences.")
	String courseToBeWaitListed();

	@DefaultMessage("Not Enrolled")
	String courseNotEnrolled();

	@DefaultMessage("Wait-Listed")
	String courseWaitListed();
	
	@DefaultMessage("Wait-Listing")
	String courseAllowsForWaitListing();
	
	@DefaultMessage("Course {0} allows to be wait-listed.")
	String courseAllowsForWaitListingTitle(String course);
	
	@DefaultMessage("No Substitutions")
	String courseNoSubs();
	
	@DefaultMessage("Course is not requested.")
	String courseNotRequested();
	
	@DefaultMessage("Reservation not used.")
	String reservationNotUsed();
	
	@DefaultMessage("Conflicts with {0}")
	String conflictWithFirst(String course);

	@DefaultMessage(", {0}")
	String conflictWithMiddle(String course);

	@DefaultMessage(" and/or {0}")
	String conflictWithLast(String course);
	
	@DefaultMessage(", assigned {0} instead")
	String conflictAssignedAlternative(String alt);
	
	@DefaultMessage("Exceeds maximum of {0,number,0.#} credit hours.")
	String conflictOverMaxCredit(float maxCredit);
	
	@DefaultMessage("Wait-listed on {0}.")
	String conflictWaitListed(String date);
	
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
	
	@DefaultMessage("Class {0} has been cancelled.")
	String classCancelled(String clazz);
	
	@DefaultMessage("You are instructing {0}.")
	String instructing(String clazz);
	
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

	@DefaultMessage("The selected offering has no students.")
	String offeringHasNoEnrollments();
	
	@DefaultMessage("The selected class has no students enrolled.")
	String classHasNoEnrollments();

	@DefaultMessage("Sort by {0}")
	String sortBy(String column);
	
	@DefaultMessage("Sort By")
	String opSort();
	
	@DefaultMessage("Add To Group")
	String opAddToGroup();
	
	@DefaultMessage("Remove From Group")
	String opRemoveFromGroup();
	
	@DefaultMessage("Student")
	String colStudent();

	@DefaultMessage("Area")
	String colArea();

	@DefaultMessage("Clasf")
	String colClassification();

	@DefaultMessage("Major")
	String colMajor();
	
	@DefaultMessage("Conc")
	String colConcentration();
	
	@DefaultMessage("Degr")
	String colDegree();
	
	@DefaultMessage("Program")
	String colProgram();
	
	@DefaultMessage("Minor")
	String colMinor();

	@DefaultMessage("Requested")
	String colRequestTimeStamp();
	
	@DefaultMessage("Enrolled")
	String colEnrollmentTimeStamp();

	@DefaultMessage("Approved")
	String colApproved();
	
	@DefaultMessage("Advised")
	String colAdvisedTimeStamp();

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
	
	@DefaultMessage("LC")
	String reservationLearningCommunity();
	
	@DefaultMessage("Override")
	String reservationOverride();

	@DefaultMessage("Dummy")
	String reservationDummy();
	
	@DefaultMessage("Filter")
	String reservationUniversal();

	@DefaultMessage("Other")
	String reservationOther();

	/* Enrollment dialog messages (opened from Enrollments table)
	 */
	
	@DefaultMessage("Loading student details for {0}...")
	String loadingEnrollment(String student);

	@DefaultMessage("{0}")
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

	@DefaultMessage("No course {0} matches the provided filter.")
	String exceptionNoCourseMatchingFilter(String course);

	@DefaultMessage("Academic session {0} does not exist.")
	String exceptionSessionDoesNotExist(String session);
	
	@DefaultMessage("Academic session not selected.")
	String exceptionNoAcademicSession();
	
	@DefaultMessage("No suitable academic sessions found.")
	String exceptionNoSuitableAcademicSessions();
	
	@DefaultMessage("No classes found for {0}.")
	String exceptionNoClassesForCourse(String course);
	
	@DefaultMessage("Class schedule is not available at the moment.")
	String exceptionClassScheduleNotAvaiable();
	
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
	
	@DefaultMessage("Unable to retrieve course details ({0}).")
	String exceptionCustomCourseDetailsFailed(String reason);
	
	@DefaultMessage("Unable to retrieve class details ({0}).")
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

	@DefaultMessage("No changes are allowed to the published solution.")
	String exceptionSolverPublished();
	
	@DefaultMessage("You are not authenticated, please log in first.")
	String exceptionEnrollNotAuthenticated();
	
	@DefaultMessage("You are not eligible to register in {0}.")
	String exceptionEnrollNotStudent(String session);
	
	@DefaultMessage("Unable to register for {0}, the class is no longer available.")
	String exceptionEnrollNotAvailable(String clazz);
	
	@DefaultMessage("Unable to register for {0}, the class is no longer available (it has been cancelled).")
	String exceptionEnrollCancelled(String clazz);
	
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
	
	@DefaultMessage("Failed to create course request provider: {0}")
	String exceptionCourseRequestProvider(String message);
	
	@DefaultMessage("Failed to create custom course lookup: {0}")
	String exceptionCustomCourseLookup(String message);
	
	@DefaultMessage("Failed to create critical courses provider: {0}")
	String exceptionCriticalCoursesProvider(String message);
	
	@DefaultMessage("Failed to create course request validation provider: {0}")
	String exceptionCourseRequestValidationProvider(String message);
	
	@DefaultMessage("Failed to create degree plans provider: {0}")
	String exceptionDegreePlansProvider(String message);
	
	@DefaultMessage("Failed to create special registration provider: {0}")
	String exceptionSpecialRegistrationProvider(String message);
	
	@DefaultMessage("Eligibility check failed: {0}")
	String exceptionFailedEligibilityCheck(String message);
	
	@DefaultMessage("Failed to create {0}: {1}")
	String exceptionCustomProvider(String provider, String message);

	@DefaultMessage("Processing...")
	String waitEnroll();
	
	@DefaultMessage("Processing...")
	String waitSpecialRegistration();
	
	@DefaultMessage("Checking eligibility...")
	String waitEligibilityCheck();
	
	@DefaultMessage("Checking approvals...")
	String waitOverridesCheck();
	
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
	
	@DefaultMessage("<span class='item' style='color:blue;'>Changes to the selected free time {0} are in blue,</span><span class='item' style='color:red;'> classes to be removed from the schedule are in red.</span><span class='item'> Changes to other classes or free times are in black.</span>")
	String suggestionsLegendOnFreeTime(String freeTime);

	@DefaultMessage("<span class='item' style='color:blue;'>Changes to the selected class {0} are in blue,</span><span class='item' style='color:red;'> classes to be removed from the schedule are in red.</span><span class='item'> Changes to other classes or free times are in black.</span>")
	String suggestionsLegendOnClass(String clazz);
	
	@DefaultMessage("<span class='item' style='color:blue;'>Changes to the selected course {0} are in blue,</span><span class='item' style='color:red;'> classes to be removed from the schedule are in red.</span><span class='item'> Changes to other courses or free times are in black.</span>")
	String suggestionsLegendOnCourse(String course);
	
	@DefaultMessage("<span class='item' style='color:blue;'>Choices for the new course {0} are in blue.</span><span class='item' style='color:red;'> Courses to be removed from the schedule are in red.</span><span class='item'> Changes to other courses or free times are in black.</span>")
	String suggestionsLegendOnNewCourse(String course);

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
	
	@DefaultMessage("Snapshot<br>Limit")
	String colSnapshotLimit();
	
	@DefaultMessage("Enrollment")
	String colEnrollment();
	
	@DefaultMessage("Not-Enrolled")
	String colWaitListed();
	
	@DefaultMessage("Alternative")
	String colUnassignedAlternative();
	
	@DefaultMessage("Reservation")
	String colReserved();
	
	@DefaultMessage("Consent")
	String colConsent();
	
	@DefaultMessage("Pending<br>Overrides")
	String colPendingOverrides();
	
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
	
	@DefaultMessage("<sup><font color='#2066CE'>n)</font></sup>")
	String htmlNoSubSign();
		
	@DefaultMessage(" (r)")
	String csvReservationSign();
	
	@DefaultMessage(" (w)")
	String csvWaitListSign();
	
	@DefaultMessage(" (n)")
	String csvNoSubSign();
	
	@DefaultMessage("No results matching filter {0} found.")
	String exceptionNoMatchingResultsFound(String filter);
	
	@DefaultMessage("Loading enrollments for {0}...")
	String loadingEnrollments(String classOrCourse);
	
	@DefaultMessage("Scheduling <u>A</u>ssistant")
	String buttonAssistant();
	
	@DefaultMessage("Course <u>R</u>equests")
	String buttonRegistration();

	@DefaultMessage("Student Scheduling Assistant for {0}")
	String dialogAssistant(String student);
	
	@DefaultMessage("Student Course Requests for {0}")
	String dialogRegistration(String student);
	
	@DefaultMessage("Close")
	String buttonClose();
	
	@DefaultMessage("Loading scheduling assistant for {0}...")
	String loadingAssistant(String student);
		
	@DefaultMessage("Need<br>Consent")
	String colNeedConsent();
	
	@DefaultMessage("Need<br>Override")
	String colNeedOverride();
	
	@DefaultMessage("<u>E</u>nrollments")
	String tabEnrollments();

	@DefaultMessage("<u>S</u>tudents")
	String tabStudents();
	
	@DefaultMessage("Change <u>L</u>og")
	String tabChangeLog();

	@DefaultMessage("<sup><font color='#2066CE'>({0}p)</font></sup>")
	String firstWaitListedPrioritySign(int priority);
	
	@DefaultMessage(" ({0}p)")
	String csvFirstWaitListedPrioritySign(int priority);
	
	@DefaultMessage("r) Space available only with a reservation.<br>w) Wait-listed and n) No-Subs requests.")
	String sectioningStatusReservationHint();
	
	@DefaultMessage("(p) denotes priority of the first wait-listed course request.")
	String sectioningStatusPriorityHint();
	
	@DefaultMessage("Enrollment disabled.")
	String exceptionEnrollmentDisabled();
	
	@DefaultMessage("Registration disabled.")
	String exceptionRegistrationDisabled();
	
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
	
	@DefaultMessage("Note")
	String colStudentNote();
	
	@DefaultMessage("Send email...")
	String sendStudentEmail();
	
	@DefaultMessage("Change status to {0}")
	String changeStatusTo(String newStatus);
	
	@DefaultMessage("Changing status to {0}...")
	String changingStatusTo(String newStatus);
	
	@DefaultMessage("Changing student note...")
	String changingStudentNote();
	
	@DefaultMessage("Set student status...")
	String setStudentStatus();

	@DefaultMessage("Set student note...")
	String setStudentNote();
		
	@DefaultMessage("Subject:")
	String emailSubject();

	@DefaultMessage("CC:")
	String emailCC();

	@DefaultMessage("Message:")
	String emailBody();
	
	@DefaultMessage("Include:")
	String emailInclude();
	
	@DefaultMessage("Custom:")
	String emailCustom();

	@DefaultMessage("Send")
	String emailSend();

	@DefaultMessage("No email on file.")
	String exceptionNoEmail();
	
	@DefaultMessage("Cancelled due to: {0}")
	String exceptionCancelled(String exception);
	
	@DefaultMessage("Change <u>L</u>og")
	String buttonChangeLog();
	
	@DefaultMessage("Approval Requests")
	String buttonSpecRegDashboard();
	
	@DefaultMessage("Send <u>E</u>mail")
	String buttonSendStudentEmail();
	
	@DefaultMessage("Loading change log for {0}...")
	String loadingChangeLog(String student);
	
	@DefaultMessage("Loading change log message...")
	String loadingChangeLogMessage();

	@DefaultMessage("Operation")
	String colOperation();

	@DefaultMessage("Date")
	String colTimeStamp();
	
	@DefaultMessage("Time [s]")
	String colExecutionTime();
	
	@DefaultMessage("Result")
	String colResult();

	@DefaultMessage("User")
	String colUser();

	@DefaultMessage("Message")
	String colMessage();

	@DefaultMessage("Advisor")
	String colAdvisor();
	
	@DefaultMessage("Advised<br>Credit")
	String colAdvisedCredit();
	
	@DefaultMessage("Missing<br>Courses")
	String colMissingCourses();
	
	@DefaultMessage("Not-Enrolled<br>Courses")
	String colNotAssignedCourses();
	
	@DefaultMessage("Advised Credit")
	String ordAdvisedCredit();
	
	@DefaultMessage("Missing Courses")
	String ordAdvisedCourses();
	
	@DefaultMessage("Not-Enrolled Courses")
	String ordNotAssignedCourses();
	
	@DefaultMessage("% Complete")
	String ordAdvisedPercentage();

	@DefaultMessage("Messages")
	String tableMessages();

	@DefaultMessage("CPU Time [s]")
	String colCpuTime();
	
	@DefaultMessage("Execution Time [s]")
	String colWallTime();
	
	@DefaultMessage("API GET Time [s]")
	String colApiGetTime();
	
	@DefaultMessage("API POST Time [s]")
	String colApiPostTime();
	
	@DefaultMessage("API Exception")
	String colApiException();
	
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
	
	@DefaultMessage("Course Requests")
	String emailCourseRequests();
	
	@DefaultMessage("Advisor Recommendations")
	String emailAdvisorRequests();
	
	@DefaultMessage("List of Classes")
	String emailClassList();
	
	@DefaultMessage("Timetable")
	String emailTimetable();

	@DefaultMessage("This email was sent on behalf of {0}.")
	String emailSentBy(String manager);

	@DefaultMessage("The changes in your schedule were made by {0}.")
	String emailChangesMadeBy(String manager);
	
	@DefaultMessage("For an up to date schedule, please visit <a href='{0}/selectPrimaryRole.action?target=gwt.jsp%3Fpage%3Dsectioning' style='color: inherit; background-color : transparent;'>{0}</a>.")
	String emailLinkToUniTime(String baseUrl);

	@DefaultMessage("Class schedule notification for %session%")
	String emailSubjectNotificationClassSchedule();
	
	@DefaultMessage("Course requests notification for %session%")
	String emailSubjectNotificationCourseRequests();
	
	@DefaultMessage("Student scheduling notification for %session%")
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
	
	@DefaultMessage("Unable to enroll a wait-listed course: {0} {1}")
	String emailEnrollmentFailedWaitListed(String subject, String courseNbr);
	
	@DefaultMessage("Unable to enroll {0} {1}: {2}")
	String emailEnrollmentFailed(String subject, String courseNbr, String error);
	
	@DefaultMessage("Unable to drop {0} {1}: {2}")
	String emailDropFailed(String subject, String courseNbr, String error);
	
	@DefaultMessage("The schedule change failed due to the following error: {0}\nAdditional approvals may be needed. Please consult your advisor.")
	String emailEnrollmentFailedMessage(String error);

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
	
	@DefaultMessage("Set Note")
	String buttonSetNote();
	
	@DefaultMessage("Set Status")
	String buttonSetStatus();
	
	@DefaultMessage("Cancelling selected students...")
	String massCanceling();
	
	@DefaultMessage("Schedule cancelation for %session%")
	String defaulSubjectMassCancel();
	
	@DefaultMessage("Class schedule for %session%")
	String defaulSubject();
	
	@DefaultMessage("Course requests for %session%")
	String defaulSubjectCourseRequests();
	
	@DefaultMessage("Advisor recommendations for %session%")
	String defaulSubjectAdvisorRequests();
	
	@DefaultMessage("Email notification for %session%")
	String defaulSubjectOther();
	
	@DefaultMessage("Mass cancellation is an irreversible operation that will delete all class enrollments and course requests for the selected student(s). Are you sure to do that?")
	String massCancelConfirmation();
	
	@DefaultMessage("Course Requests")
	String mailIncludeCourseRequests();
	
	@DefaultMessage("Class Schedule")
	String mailIncludeClassSchedule();
	
	@DefaultMessage("Advisor Recommendations")
	String mailIncludeAdvisorRequests();
	
	@DefaultMessage("<i>Arrange Hours</i>")
	String arrangeHours();
	
	@DefaultMessage("<i>No Dates</i>")
	String noDate();
	
	@DefaultMessage("<i>No Room</i>")
	String noRoom();
	
	@DefaultMessage("Wait-List")
	String toggleWaitList();
	
	@DefaultMessage("Course:")
	String propCourse();
	
	@DefaultMessage("Student:")
	String propStudent();
	
	@DefaultMessage("Note:")
	String propNote();
	
	@DefaultMessage("Permissions:")
	String propPermissions();
	
	@DefaultMessage("Course Wait-Lists:")
	String propWaitLists();
	
	@DefaultMessage("Allow No-Subs:")
	String propNoSubs();
	
	@DefaultMessage("Email Notification:")
	String propEmailNotification();
	
	@DefaultMessage("Effective Period:")
	String propEffectivePeriod();
	
	@DefaultMessage("Course Types:")
	String propCourseTypes();
	
	@DefaultMessage("Message:")
	String propStatusMessage();
	
	@DefaultMessage("Fallback Status:")
	String propFallbackStatus();
	
	@DefaultMessage("Approval Requests:")
	String propSpecialRegistration();
	
	@DefaultMessage("Course Request Validation:")
	String propCourseRequestValidation();
	
	@DefaultMessage("Require Sections / Instr. Methods:")
	String propCanRequire();
	
	@DefaultMessage("Personal Schedule:")
	String propStatusSchedule();
	
	@DefaultMessage("You are not registered for any classes yet. Please click the Build Schedule button in order to complete your registration.")
	String warnScheduleEmptyOnCourseRequest();
	
	@DefaultMessage("You have made some changes in your schedule. Please click the Build Schedule button to update your registration.")
	String warnScheduleChangedOnCourseRequest();

	@DefaultMessage("You are not registered for any classes yet. Please click the Submit Schedule button in order to complete your registration.")
	String warnScheduleEmptyOnClassSchedule();
	
	@DefaultMessage("You have made some changes in your schedule. Please click the Submit Schedule button to update your registration.")
	String warnScheduleChangedOnClassSchedule();
	
	@DefaultMessage("You have not submitted any courses yet. Please click the Submit Requests button in order to complete your submission.")
	String warnRequestsEmptyOnCourseRequest();
	
	@DefaultMessage("You have made some changes in your course requests. Please click the Submit Requests button to update your submission.")
	String warnRequestsChangedOnCourseRequest();
			
	@DefaultMessage("There are unsaved changes in your schedule. Do you really want to discard these changes without updating your registration?")
	String queryLeaveChangesOnClassSchedule();
	
	@DefaultMessage("There are unsaved changes in your course requests.  Do you really want to discard these changes without updating your submission?")
	String queryLeaveChangesOnCourseRequests();
	
	@DefaultMessage("Total Credit: {0,number,0.#}")
	String totalCredit(float total);
	
	@DefaultMessage("Total Credit: {0,number,0.#} - {1,number,0.#}")
	String totalCreditRange(float minTotal, float maxTotal);
	
	@DefaultMessage("Requested Credit: {0,number,0.#}")
	String requestedCredit(float total);
	
	@DefaultMessage("Requested Credit: {0,number,0.#} - {1,number,0.#}")
	String requestedCreditRange(float minTotal, float maxTotal);
	
	@DefaultMessage("Requested Credit")
	String rowRequestedCredit();
	
	@DefaultMessage("Reload Student")
	String reloadStudent();
	
	@DefaultMessage("Request Update")
	String requestStudentUpdate();
	
	@DefaultMessage("Check Override Status")
	String checkOverrideStatus();
	
	@DefaultMessage("Validate Overrides")
	String validateStudentOverrides();
	
	@DefaultMessage("Check Critical Courses")
	String validateReCheckCriticalCourses();
	
	@DefaultMessage("Requesting student update...")
	String requestingStudentUpdate();
	
	@DefaultMessage("Reloading student(s)...")
	String reloadingStudent();
	
	@DefaultMessage("Checking override status...")
	String checkingOverrideStatus();
	
	@DefaultMessage("Validating student overrides...")
	String validatingStudentOverrides();
	
	@DefaultMessage("Checking critical courses...")
	String recheckingCriticalCourses();
	
	@DefaultMessage("Student update request not allowed.")
	String exceptionRequestStudentUpdateNotAllowed();
	
	@DefaultMessage("Student update successfully requested. Please wait a while for the synchronization to take place.")
	String requestStudentUpdateSuccess();
	
	@DefaultMessage("Student(s) successfully reloaded. Click Search to see the changes.")
	String reloadStudentSuccess();
	
	@DefaultMessage("Student overrides successfully rechecked.")
	String checkStudentOverridesSuccess();
	
	@DefaultMessage("Student overrides successfully validated.")
	String validateStudentOverridesSuccess();
	
	@DefaultMessage("Critical courses successfully rechecked.")
	String recheckCriticalCoursesSuccess();
	
	@DefaultMessage("Click to lock the class. Any course changes will not affect locked course.")
	String hintUnlocked();
	
	@DefaultMessage("The class is locked. Any course changes will not affect locked course.")
	String hintLocked();
	
	@DefaultMessage("Students")
	String studentsTable();
	
	@DefaultMessage("{0} Students")
	String enrollmentTableFilter(String filter);
	
	@DefaultMessage("The selected offering has no {0} students.")
	String offeringHasNoEnrollmentsOfType(String type);
	
	@DefaultMessage("This schedule only displays classes with assigned time. To see your full list of classes, please open the List of classes tab.")
	String timeGridNotAssignedTimes();
	
	@DefaultMessage("Together with other registration changes, the course {0} will be dropped. Do you want to proceed?")
	String confirmEnrollmentCourseDrop(String course);
		
	@DefaultMessage("Together with other registration changes, the critical course {0} will be dropped. This may prohibit progress towards degree. Please consult with your academic advisor. Do you want to proceed?")
	String confirmEnrollmentCriticalCourseDrop(String course);
	
	@DefaultMessage("Together with other registration changes, the course {0} will have a long travel time. Do you want to proceed?")
	String confirmLongTravel(String course);
	
	@DefaultMessage("<u>N</u>ew Course")
	String buttonQuickAdd();
	
	@DefaultMessage("Add a new course to the schedule or drop an existing course from the schedule without going back to the Course Requests.")
	String hintQuickAdd();

	@DefaultMessage("Select a new course to add to the schedule or an existing course to drop from the schedule")
	String dialogQuickAdd();

	@DefaultMessage("Quick add failed.")
	String quickAddFailed();

	@DefaultMessage("Quick add failed: {0}")
	String quickAddFailedWithMessage(String message);

	@DefaultMessage("<u>D</u>rop {0}")
	String buttonQuickDrop(String course);
	
	@DefaultMessage("<u>W</u>ait-List {0}")
	String buttonWaitList(String course);

	@DefaultMessage("Do you want to drop {0} from your schedule?")
	String confirmQuickDrop(String course);
	
	@DefaultMessage("Do you want to wait-list {0}?")
	String confirmQuickWaitList(String course);
	
	@DefaultMessage("<u>D</u>egree Plan")
	String buttonDegreePlan();
	
	@DefaultMessage("Show degree plan for the selected academic session.")
	String hintDegreePlan();
	
	@DefaultMessage("There are multiple degree plans available, please select one to continue...")
	String dialogSelectDegreePlan();
	
	@DefaultMessage("<u>N</u>otes")
	String buttonLastNotes();
	
	@DefaultMessage("Show previous additional notes.")
	String hintLastNotes();
	
	@DefaultMessage("Previous Notes -- Click on a note to select it, Escape to hide the dialog.")
	String dialogLastNotes();

	@DefaultMessage("Approval Requests")
	String dialogSpecialRegistrations();
	
	@DefaultMessage("Degree Plan: {0}")
	String dialogDegreePlan(String name);
	
	@DefaultMessage("Description")
	String colDegreePlanName();
	
	@DefaultMessage("Degree")
	String colDegreePlanDegree();
	
	@DefaultMessage("Modified")
	String colDegreePlanLastModified();
	
	@DefaultMessage("Modified By")
	String colDegreePlanModifiedBy();
	
	@DefaultMessage("Course")
	String colDegreeItemName();
	
	@DefaultMessage("Title")
	String colDegreeItemDescription();
	
	@DefaultMessage("Request")
	String colRequestPriority();
	
	@DefaultMessage("Description")
	String colSpecRegName();
	
	@DefaultMessage("Submitted")
	String colSpecRegSubmitted();
	
	@DefaultMessage("Wait-Listed")
	String colWaitListedTimeStamp();
	
	@DefaultMessage("Registration Errors")
	String colSpecRegErrors();
	
	@DefaultMessage("Registration Errors")
	String colWaitListErrors();

	@DefaultMessage("Last Note")
	String colSpecRegNote();
	
	@DefaultMessage("<u>S</u>elect")
	String buttonDegreePlanSelect();
	
	@DefaultMessage("<u>C</u>ancel")
	String buttonDegreePlanCancel();
	
	@DefaultMessage("<u>A</u>pply")
	String buttonDegreePlanApply();
	
	@DefaultMessage("<u>C</u>lose")
	String buttonDegreePlanClose();
	
	@DefaultMessage("<u>B</u>ack")
	String buttonDegreePlanBack();
	
	@DefaultMessage("<u>N</u>ew Request")
	String buttonSpecRegCreateNew();
	
	@DefaultMessage("<u>S</u>elect")
	String buttonSpecRegSelect();
	
	@DefaultMessage("<u>C</u>ancel")
	String buttonSpecRegCancel();
	
	@DefaultMessage("Retrieving degree plan...")
	String waitListDegreePlans();
	
	@DefaultMessage("No degree plan is available.")
	String failedNoDegreePlans();
	
	@DefaultMessage("No approval requests are available.")
	String failedNoSpecialRegistrations();
	
	@DefaultMessage("Failed to load degree plan: {0}")
	String failedListDegreePlans(String reason);
	
	@DefaultMessage("Failed to validate wait-listed courses: {0}")
	String failedWaitListValidation(String reason);
	
	@DefaultMessage("This plan is locked.")
	String hintLockedPlan();
	
	@DefaultMessage("This plan is active.")
	String hintActivePlan();
	
	@DefaultMessage("The approval request has been approved.")
	String hintSpecRegApproved();
	
	@DefaultMessage("This approval request has been fully approved. Click on this request to apply it to your current schedule and then click Submit Schedule button to update your registration.")
	String hintSpecRegApprovedNoteApply();
	
	@DefaultMessage("The approval request has been cancelled.")
	String hintSpecRegCancelled();
	
	@DefaultMessage("The approval request has been submitted for processing. Waiting for approval...")
	String hintSpecRegPending();
	
	@DefaultMessage("The approval request has been submitted for processing. It can still be edited.")
	String hintSpecRegDraft();
	
	@DefaultMessage("One or more of the registration errors have been denied.")
	String hintSpecRegRejected();
	
	@DefaultMessage("The approval request has been already applied.")
	String hintSpecRegApplied();
	
	@DefaultMessage("{0}, {1}")
	String courseSeparatorMiddle(String list, String item);
	
	@DefaultMessage("{0}, and {1}")
	String courseSeparatorLast(String list, String item);
	
	@DefaultMessage("{0} and {1}")
	String courseSeparatorPair(String firstItem, String lastItem);
	
	@DefaultMessage("{0}, {1}")
	String choiceSeparatorMiddle(String list, String item);
	
	@DefaultMessage("{0}, or {1}")
	String choiceSeparatorLast(String list, String item);
	
	@DefaultMessage("{0} or {1}")
	String choiceSeparatorPair(String firstItem, String lastItem);
	
	@DefaultMessage("({0})")
	String surroundWithBrackets(String group);
	
	@DefaultMessage("Course {0} is not offered.")
	String plannedCourseNotOffered(String course);
	
	@DefaultMessage("Select {0}")
	String hintChoiceGroupSelection(String courseOrGroup);
	
	@DefaultMessage("{0}.")
	String degreeRequestedCourse(int priority);
	
	@DefaultMessage("{0}A.")
	String degreeRequestedCourseFirstAlt(int priority);
	
	@DefaultMessage("{0}B.")
	String degreeRequestedCourseSecondAlt(int priority);
	
	@DefaultMessage("{0}{1}.")
	String degreeRequestedCourseAlt(int priority, String alt);
	
	@DefaultMessage("Sub {0}.")
	String degreeRequestedAlternative(int priority);
	
	@DefaultMessage("Sub {0}A.")
	String degreeRequestedAlternativeFirstAlt(int priority);
	
	@DefaultMessage("Sub {0}B.")
	String degreeRequestedAlternativeSecondAlt(int priority);
	
	@DefaultMessage("Sub {0}{1}.")
	String degreeRequestedAlternativeAlt(int priority, String alt);
	
	@DefaultMessage("Class Schedule")
	String headerClassSchedule();
	
	@DefaultMessage("Course")
	String hintCourseWithNoTitle();
	
	@DefaultMessage("Free Time")
	String hintFreeTimeRequest();
	
	@DefaultMessage("Instructional Method")
	String hintInstructionalMethod();
	
	@DefaultMessage("Required Instructional Method")
	String hintRequiredInstructionalMethod();
	
	@DefaultMessage("Required {0}")
	String hintRequiredSection(String section);
	
	@DefaultMessage("method")
	String tagInstructionalMethod();
	
	@DefaultMessage("section")
	String tagSection();
	
	@DefaultMessage("override")
	String tagOverride();
	
	@DefaultMessage("Swap this course with its alternative")
	String altFilterSwapWithAlternative();
	
	@DefaultMessage("Add alernative course")
	String altFilterAddAlternative();
	
	@DefaultMessage("Remove alernative course")
	String altFilterRemoveAlternative();
	
	@DefaultMessage("Clear course request")
	String altFilterClearCourseRequest();
	
	@DefaultMessage("This course is not active. Click here to activate.")
	String altFilterActivate();
	
	@DefaultMessage("Student\nId")
	String reportStudentId();
	
	@DefaultMessage("Student\nName")
	String reportStudentName();
	
	@DefaultMessage("Student\nEmail")
	String reportStudentEmail();
	
	@DefaultMessage("Student\nPriority")
	String reportStudentPriority();
	
	@DefaultMessage("Curriculum")
	String reportStudentCurriculum();
	
	@DefaultMessage("Advisor")
	String reportStudentAdvisor();
	
	@DefaultMessage("Group")
	String reportStudentGroup();
	
	@DefaultMessage("Course")
	String reportUnassignedCourse();
	
	@DefaultMessage("Course")
	String reportRequestedCourse();
	
	@DefaultMessage("Conflict")
	String reportAssignmentConflict();
	
	@DefaultMessage("Student Filter")
	String reportUniversalReservationStudentFilter();
	
	@DefaultMessage("Not-Assigned Course Requests")
	String reportUnassignedCourseRequests();
	
	@DefaultMessage("Conflicting Course Requests")
	String reportConflictingCourseRequests();
	
	@DefaultMessage("Not-Assigned Critical Course Requests")
	String reportUnassignedCriticalCourseRequests();
	
	@DefaultMessage("Not-Assigned LC Course Requests")
	String reportUnassignedLCCourseRequests();
	
	@DefaultMessage("Not-Used Group Reservations")
	String reportUnusedGroupReservations();
	
	@DefaultMessage("Not-Used Individual Reservations")
	String reportUnusedIndividualReservations();
	
	@DefaultMessage("Not-Used Override Reservations")
	String reportUnusedOverrideReservations();
	
	@DefaultMessage("Not-Used Learning Community Reservations")
	String reportUnusedLearningCommunityReservations();
	
	@DefaultMessage("Not-Used Curriculum Reservations")
	String reportUnusedCurriculumReservations();
	
	@DefaultMessage("Not-Used Student Filter Reservations")
	String reportUnusedStudentFilterReservations();
	
	@DefaultMessage("Course Requests")
	String reportCourseRequestsWithPriorities();
	
	@DefaultMessage("Tableau Report")
	String reportTableauReport();
	
	@DefaultMessage("Tableau Report (Simplified)")
	String reportTableauSimpleReport();
	
	@DefaultMessage("Student Unavailability Conflicts")
	String reportStudentUnavailabilityConflicts();
	
	@DefaultMessage("Requested")
	String reportRequestedPriority();
	
	@DefaultMessage("Request\nPriority")
	String reportCourseRequestPriority();
	
	@DefaultMessage("Conflict\nPriority")
	String reportConflictingCourseRequestPriority();
	
	@DefaultMessage("Critical Courses")
	String reportCriticalCoursesReport();
	
	@DefaultMessage("Priority")
	String reportPriority();
	
	@DefaultMessage("Course")
	String reportCourse();
	
	@DefaultMessage("Class")
	String reportClass();
	
	@DefaultMessage("Allowed\nOverlap")
	String reportAllowedOverlap();
	
	@DefaultMessage("Meeting Time")
	String reportMeetingTime();
	
	@DefaultMessage("Date Pattern")
	String reportDatePattern();
	
	@DefaultMessage("Subpart\nOverlap")
	String reportSubpartOverlap();
	
	@DefaultMessage("Time\nOverride")
	String reportTimeOverride();
	
	@DefaultMessage("Ignore\nConflict")
	String reportIgnoreConflicts();
	
	@DefaultMessage("Conflicting\nCourse")
	String reportConflictingCourse();
	
	@DefaultMessage("Conflicting\nClass")
	String reportConflictingClass();
	
	@DefaultMessage("Conflicting\nMeeting Time")
	String reportConflictingMeetingTime();
	
	@DefaultMessage("Conflicting\nDate Pattern")
	String reportConflictingDatePattern();
	
	@DefaultMessage("Conflicting\nAssignment")
	String reportConflictingAssignment();
	
	@DefaultMessage("Overlap\n[min]")
	String reportOverlapMinutes();
	
	@DefaultMessage("Overlapping\nMeetings")
	String reportOverlappingMeetings();
	
	@DefaultMessage("Original\nClass")
	String reportOriginalClass();
	
	@DefaultMessage("Original\nTime")
	String reportOriginalTime();
	
	@DefaultMessage("Original\nDate")
	String reportOriginalDate();

	@DefaultMessage("Original\nRoom")
	String reportOriginalRoom();

	@DefaultMessage("Assigned\nClass")
	String reportAssignedClass();

	@DefaultMessage("Assigned\nTime")
	String reportAssignedTime();

	@DefaultMessage("Assigned\nDate")
	String reportAssignedDate();

	@DefaultMessage("Assigned\nRoom")
	String reportAssignedRoom();

	@DefaultMessage("Penalization")
	String reportPenalization();

	@DefaultMessage("Not Assigned")
	String reportNotAssigned();
	
	@DefaultMessage("Teaching\nOverlap")
	String reportTeachingOverlap();
	
	@DefaultMessage("Teaching\nAssignment")
	String reportTeachingAssignment();

	@DefaultMessage("1st Alt")
	String report1stAlt();
	
	@DefaultMessage("2nd Alt")
	String report2ndAlt();
	
	@DefaultMessage("Enrolled\nCourse")
	String reportEnrolledCourse();
	
	@DefaultMessage("Enrolled\nChoice")
	String reportEnrolledChoice();
	
	@DefaultMessage("Solution Statistics")
	String reportSolutionStatistics();
	
	@DefaultMessage("Accommodation Conflicts")
	String reportAccommodationConflicts();
	
	@DefaultMessage("No Break Time Back-to-Backs")
	String reportBackToBacksNoBreak();
	
	@DefaultMessage("Sectioning Issues (for Re-Scheduling)")
	String reportSectioningIssues();
	
	@DefaultMessage("Sectioning Issues (for Re-Scheduling, All Courses)")
	String reportSectioningIssuesAllCourses();
	
	@DefaultMessage("Reservations")
	String reportReservations();
	
	@DefaultMessage("BTB\n{0}")
	String reportBTB(String col);
	
	@DefaultMessage("Wait-Listing")
	String reportWaitListing();
	
	@DefaultMessage("Problem")
	String reportProblem();
	
	@DefaultMessage("Unavailability\n{0}")
	String reportUnavailability(String col);
	
	@DefaultMessage("Time conflict")
	String reportTimeConflict();
	
	@DefaultMessage("Overlaps for {0} minutes")
	String reportAllowedOverlapForMins(int mins);
	
	@DefaultMessage("Distance conflict ({0} minutes break, {1} minutes travel)")
	String reportDistanceConflict(int breakTime, int travelTimeInMins);
	
	@DefaultMessage("{0}")
	String teachingAssignment(String className);
	
	@DefaultMessage("Section is full")
	String sectionIsFull();
	
	@DefaultMessage("Section is full, overlaps with {0}")
	String noteFullSectionOverlapFirst(String classOrCourse);
	
	@DefaultMessage(
			"Based on your course selections, a conflict-free schedule is not possible.\n" +
			"Under extenuating circumstances, the faculty may approve student enrollments that include schedule conflicts.")
	String disclaimerNoSuggestionsWarning();
	
	@DefaultMessage("Do you want to explore approval for a schedule with time conflicts?")
	String disclaimerSpecRegAllowForTimeConflicts();
	
	@DefaultMessage("Do you want to explore approval for a schedule with limit conflicts?")
	String disclaimerSpecRegAllowForSpaceConflicts();
	
	@DefaultMessage("Do you want to explore approval for a schedule with conflicts?")
	String disclaimerSpecRegAllowForTimeSpaceConflicts();
	
	@DefaultMessage("My Students")
	String modeMyStudents();
	
	@DefaultMessage("My Students Advised")
	String modeMyStudentsAdvised();
	
	@DefaultMessage("My Students Not Advised")
	String modeMyStudentsNotAdvised();
	
	@DefaultMessage("Not Advised")
	String modeNotAdvised();
	
	@DefaultMessage("Advised")
	String modeAdvised();
	
	@DefaultMessage("Request is wait-listed.")
	String descriptionRequestWaitListed();
	
	@DefaultMessage("Request is marked as no-subs.")
	String descriptionRequestNoSubs();
	
	@DefaultMessage("Course request is critical.")
	String descriptionRequestCritical();
	
	@DefaultMessage("Course request is important.")
	String descriptionRequestImportant();
	
	@DefaultMessage("Course request is vital.")
	String descriptionRequestVital();
	
	@DefaultMessage("Course request is not critical.")
	String descriptionRequestNotCritical();
	
	@DefaultMessage("Course request has a matching LC reservation.")
	String descriptionRequestLC();
	
	@DefaultMessage("Face-to-face course request for a visiting student.")
	String descriptionRequestVisitingF2F();
	
	@DefaultMessage("Enrolled")
	String reqStatusEnrolled();

	@DefaultMessage("Approved")
	String reqStatusApproved();

	@DefaultMessage("Pending")
	String reqStatusPending();

	@DefaultMessage("Cancelled")
	String reqStatusCancelled();

	@DefaultMessage("Denied")
	String reqStatusRejected();
	
	@DefaultMessage("Submitted")
	String reqStatusRegistered();
	
	@DefaultMessage("Warning")
	String reqStatusWarning();
	
	@DefaultMessage("Not Requested")
	String reqStatusNeeded();
	
	@DefaultMessage("Override Not Needed")
	String reqStatusNotNeeded();

	@DefaultMessage("Wait-Listed")
	String reqStatusWaitListed();
	
	@DefaultMessage("You are already enrolled in {0}.\nThis course request cannot be modified or deleted.")
	String enrolled(String course);
	
	@DefaultMessage("{0} is already included in your current submission. No registration errors were detected.")
	String requested(String course);
	
	@DefaultMessage("The following registration errors have been detected:\n{0}")
	String overrideNeeded(String errors);
	
	@DefaultMessage("Registration errors were detected, but no override has been requested.")
	String overrideNotRequested();
	
	@DefaultMessage("Enrolled in a course higher in the list of alternatives. Wait-list not active.")
	String waitListLowPriority();
	
	@DefaultMessage("Current enrollment meets the requirements. Wait-list not active.")
	String waitListRequirementsMet();
	
	@DefaultMessage("The following issues have been detected:\n{0}")
	String requestWarnings(String errors);
	
	@DefaultMessage("Requested override for {0} has been denied.\nPlease select a different course or remove this course request.")
	String overrideRejected(String course);
	
	@DefaultMessage("Requested override for {0} has been denied.")
	String overrideRejectedWaitList(String course);
	
	@DefaultMessage("An override has been requested for {0}.\nWaiting for approval...")
	String overridePending(String course);
	
	@DefaultMessage("An override has been requested for {0}.")
	String overridePendingShort(String course);
	
	@DefaultMessage("Requested override for {0} has been cancelled.\nClick the Submit Requests button to re-validate this request and request a new override if it is still needed.")
	String overrideCancelled(String course);

	@DefaultMessage("Requested override for {0} has been cancelled.")
	String overrideCancelledWaitList(String course);

	@DefaultMessage("Requested override for {0} has been approved.")
	String overrideApproved(String course);
	
	@DefaultMessage("Requested override for {0} is not needed.")
	String overrideNotNeeded(String course);
	
	@DefaultMessage("An override for the following registration issues has been requested:\n{0}")
	String requestedWarnings(String errors);
	
	@DefaultMessage("Wait-list for {0} is not active.")
	String waitListInactive(String course);
	
	@DefaultMessage("The following approvals have been requested:")
	String requestedApprovals();
	
	@DefaultMessage("Request Note: {0}")
	String requestNote(String course);
	
	@DefaultMessage("Last Note: {0}")
	String overrideNote(String course);
	
	@DefaultMessage("Session Default ({0})")
	String studentStatusSessionDefault(String name);
	
	@DefaultMessage("System Default (All Enabled)")
	String studentStatusSystemDefault();
	
	@DefaultMessage("All except {0}")
	String courseTypesAllBut(String prohibitedTypes);
	
	@DefaultMessage("{0}")
	String courseTypesAllowed(String allowedTypes);
	
	@DefaultMessage("No course types are allowed")
	String courseTypesNoneAllowed();
	
	@DefaultMessage("-")
	String statusNoChange();
	
	@DefaultMessage("Course Requests page can be used by students, advisors, or admins to see the current requests.")
	String messageStatusCanAccessCourseRequests();
	
	@DefaultMessage("Course Requests page can NOT be used.")
	String messageStatusCanNotAccessCourseRequests();
	
	@DefaultMessage("Students can use the Course Requests to make changes to their course requests.")
	String messageStatusStudentsCanRegister();
	
	@DefaultMessage("Students can NOT use the Course Requests to make changes.")
	String messageStatusStudentsCanNotRegister();
	
	@DefaultMessage("Advisors can use the Course Requests to make changes to student course requests.")
	String messageStatusAdvisorsCanRegister();
	
	@DefaultMessage("Advisors can NOT use the Course Requests to make changes.")
	String messageStatusAdvisorsCanNotRegister();
	
	@DefaultMessage("Administrators can use the Course Requests to make changes to student course requests.")
	String messageStatusAdminsCanRegister();
	
	@DefaultMessage("Administrators can NOT use the Course Requests to make changes.")
	String messageStatusAdminsCanNotRegister();
	
	@DefaultMessage("Scheduling Assistant page can be used by students, advisors, or admins to see the current registration.")
	String messageStatusCanAccessSchedulingAssistant();
	
	@DefaultMessage("Scheduling Assistant page can NOT be used.")
	String messageStatusCanNotAccessSchedulingAssistant();
	
	@DefaultMessage("Students can use the Scheduling Assistant to make changes to their class enrollments.")
	String messageStatusStudentsCanEnroll();
	
	@DefaultMessage("Students can NOT use the Scheduling Assistant to make changes.")
	String messageStatusStudentsCanNotEnroll();
	
	@DefaultMessage("Advisors can use the Scheduling Assistant to make changes to student class enrollments.")
	String messageStatusAdvisorsCanEnroll();
	
	@DefaultMessage("Advisors can NOT use the Scheduling Assistant to make changes.")
	String messageStatusAdvisorsCanNotEnroll();
	
	@DefaultMessage("Administrators can use the Scheduling Assistant to make changes to student class enrollments.")
	String messageStatusAdminsCanEnroll();
	
	@DefaultMessage("Administrators can NOT use the Scheduling Assistant to make changes.")
	String messageStatusAdminsCanNotEnroll();
	
	@DefaultMessage("Courses can be wait-listed in UniTime.")
	String messageStatusCanWaitList();
	
	@DefaultMessage("Courses can NOT be wait-listed in UniTime.")
	String messageStatusCanNotWaitList();
	
	@DefaultMessage("Show the No-Subs toggle in the Course Requests table.")
	String messageStatusCanNoSubs();
	
	@DefaultMessage("Do not show the No-Subs toggle in the Course Requests table.")
	String messageStatusCanNotNoSubs();
	
	@DefaultMessage("Sections or Instructional Methods can be required by the student.")
	String messageStatusCanRequire();
	
	@DefaultMessage("Sections or Instructional Methods can NOT be required by the student.")
	String messageStatusCanNotRequire();
	
	@DefaultMessage("Student can see his/her personal schedule.")
	String messageStatusSchedule();
	
	@DefaultMessage("Student can NOT see his/her personal schedule.")
	String messageStatusNoSchedule();
	
	@DefaultMessage("Email notifications are enabled.")
	String messageStatusCanEmail();
	
	@DefaultMessage("Email notifications are disabled.")
	String messageStatusCanNotEmail();
	
	@DefaultMessage("Requests approval workflow is enabled.")
	String messageStatusCanSpecialRegistration();
	
	@DefaultMessage("Requests approval workflow is disabled.")
	String messageStatusCanNotSpecialRegistration();
	
	@DefaultMessage("Custom course request validation is enabled.")
	String messageStatusCanRequestValidation();
	
	@DefaultMessage("Custom course request validation is disabled.")
	String messageStatusCanNotRequestValidation();
	
	@DefaultMessage("before {0}")
	String messageEffectivePeriodBefore(String end);
	
	@DefaultMessage("after {0}")
	String messageEffectivePeriodAfter(String start);
	
	@DefaultMessage("between {0} and {1}")
	String messageEffectivePeriodBetween(String start, String end);
	
	@DefaultMessage("Your time window is {0}.")
	String messageTimeWindow(String effectivePeriod);
	
	@DefaultMessage("The request to increase the maximum credit hours has been denied.\nYou may not be able to get a full schedule.")
	String creditStatusDenied();
	
	@DefaultMessage("The request to increase the maximum credit hours has been denied.")
	String creditStatusDeniedShort();
	
	@DefaultMessage("You may not be able to get a full schedule.")
	String creditStatusTooHigh();
	
	@DefaultMessage("A max credit hours override has been requested.\nWaiting for approval...")
	String creditStatusPending();
	
	@DefaultMessage("A max credit hours override has been requested.")
	String creditStatusPendingShort();
	
	@DefaultMessage("The request to increase the maximum credit hours has been cancelled.\nClick the Submit Requests button to re-validate this request and request a new override if it is still needed.")
	String creditStatusCancelled();
	
	@DefaultMessage("The request to increase the maximum credit hours has been cancelled.")
	String creditStatusCancelledWaitList();
	
	@DefaultMessage("The request to increase the maximum credit hours has been approved.")
	String creditStatusApproved();
	
	@DefaultMessage("Instructional Method Preference:")
	String labelInstructionalMethodPreference();
	
	@DefaultMessage("Request Approvals")
	String dialogRequestOverrides();
	
	@DefaultMessage("Request Approvals")
	String buttonRequestOverrides();
	
	@DefaultMessage("Request approvals for the above registration errors")
	String titleRequestOverrides();
	
	@DefaultMessage("Cancel Request")
	String buttonCancelRequest();
	
	@DefaultMessage("Go back to explore other scheduling options")
	String titleCancelRequest();
	
	@DefaultMessage("The following registration errors have been detected:")
	String messageRegistrationErrorsDetected();
	
	@DefaultMessage(
			"\nPlease, select <b>Request Approvals</b> to request registration overrides or other approvals required to register " +
			"for the courses listed above. " +
			"Click <b>Cancel Request</b> to cancel this request and go back to explore other scheduling options.")
	String messageRequestOverridesOptions();
	
	@DefaultMessage(
			"<b>Request Note:</b>")
	String messageRequestOverridesNote();
	
	@DefaultMessage("\n<b>Disclaimer:</b>")
	String messageRequestOverridesDisclaimer();
	
	@DefaultMessage(
			"By checking this box, I understand that requesting approval does not guarantee that I will be given permission to register "+
			"for these courses, and even if the approvals are granted, I may not be able to register for these "+
			"courses due to space limitations, schedule conflicts or other policies. I also understand that if "+
			"approvals are granted, it may be my responsibility to process the schedule change within the established "+
			"registration deadlines.")
	String messageRequestOverridesDisclaimerMessage();
	
	@DefaultMessage("\n<span class='text-red'>If you proceed, the following pending approval requests will be canceled/replaced:</span>")
	String messageRequestOverridesCancel();
	
	@DefaultMessage("There are no registration errors for which an approval can be requested.")
	String errorNoRegistrationErrorsDetected();
	
	@DefaultMessage("No approvals can be requested at this time.")
	String errorRegistrationErrorsBadResponse();
	
	@DefaultMessage("Do you want to cancel this approval request?")
	String confirmOverrideRequestCancel();
	
	@DefaultMessage("If you proceed, the following pending approvals will be canceled:")
	String confirmOverrideRequestCancelCancelledErrors();
	
	@DefaultMessage("Registration Errors")
	String dialogEnrollmentConfirmation();
	
	@DefaultMessage("Registration failed with the following registration errors:")
	String messageEnrollmentFailedWithErrors();
	
	@DefaultMessage("Registration for the selected schedule was partially successful.\n" +
			"Some of the changes have been denied due to the following registration errors:")
	String messageEnrollmentSucceededWithErrors();
	
	@DefaultMessage("Checking approvals ...\nClick <b>Close Dialog</b> to hide this dialog without requesting any approvals.")
	String messageCheckingOverrides();
	
	@DefaultMessage("Request Approvals")
	String buttonEnrollmentRequestOverrides();
	
	@DefaultMessage("Review and request approvals for the above registration errors.")
	String titleEnrollmentRequestOverrides();
	
	@DefaultMessage("Close Dialog")
	String buttonEnrollmentHideConfirmation();
	
	@DefaultMessage("Hide the Registration Errors dialog without requesting any approvals.")
	String titleEnrollmentHideConfirmation();
	
	@DefaultMessage("Approvals cannot be requested: {0}\nClick <b>Close Dialog</b> to hide this dialog without requesting any approvals.")
	String messageCannotRequestOverrides(String message);
	
	@DefaultMessage("Approvals cannot be requested due to the following registration errors:")
	String messageCannotRequestOverridesErrors();
	
	@DefaultMessage("Click <b>Close Dialog</b> to hide this dialog without requesting any approvals.")
	String messageCannotRequestOverridesErrorsBottom();
	
	@DefaultMessage("It is possible to request approvals for all of the above registration errors.\n"+
			"If you have already discussed these courses with your advisor and were advised to request registration in them, please select <b>Request Approvals</b>. "+
			"If you aren\u2019t sure, click <b>Close Dialog</b> and consult with your advisor before registering for these courses.")
	String messageCanRequestOverridesAll();
	
	@DefaultMessage("It is possible to request approvals for some of the above registration errors.\n"+
			"If you have already discussed these courses with your advisor and were advised to request registration in them, please select <b>Request Approvals</b>. "+
			"If you aren\u2019t sure, click <b>Close Dialog</b> and consult with your advisor before registering for these courses.")
	String messageCanRequestOverridesSome();
	
	@DefaultMessage("Show all changes")
	String checkOverridesShowAllChanges();
	
	@DefaultMessage("You are trying to drop {0} as part of this approval request. Once this request is fully approved, click on this request to apply it to your current schedule and then click Submit Schedule button to update your registration.")
	String specRegUnassignment(String clazz);

	@DefaultMessage("You are trying to add {0} as part of this approval request. Once this request is fully approved, click on this request to apply it to your current schedule and then click Submit Schedule button to update your registration.")
	String specRegAssignment(String clazz);
	
	@DefaultMessage("One or more of your approval requests have been fully approved. \nPlease click on the approved request to apply it to your current schedule, review the new schedule, and then click Submit Schedule button to update your registration.")
	String statusOneOrMoreFullyApprovedRequestsNotYetApplied();
	
	@DefaultMessage("Maximum of {0,number,0.#} credit hours exceeded.")
	String creditWarning(float credit);
	
	@DefaultMessage("Published")
	String infoPublished();
	
	@DefaultMessage("Solver Published")
	String statusPublished();
	
	@DefaultMessage("- {0}")
	String courseMessage(String message);
	
	@DefaultMessage("Selection Required")
	String checkPreferencesAreRequired();
	
	@DefaultMessage("{0} ... {1}")
	String pageRange(int first, int last);
	
	@DefaultMessage("Any Preference")
	String termAnyPreference();
	
	@DefaultMessage("Met Preference")
	String termMetPreference();
	
	@DefaultMessage("Unmet Preference")
	String termUnmetPreference();
	
	@DefaultMessage("Any Requirement")
	String termAnyRequirement();
	
	@DefaultMessage("Met Requirement")
	String termMetRequirement();
	
	@DefaultMessage("Unmet Requirement")
	String termUnmetRequirement();
	
	@DefaultMessage("Course {0} is marked as critical.")
	String hintCriticalCourse(String courseOrGroup);
	
	@DefaultMessage("Elective {0} is marked as critical.")
	String hintCriticalGroup(String courseOrGroup);
	
	@DefaultMessage("Grade Modes & Variable Credits")
	String dialogChangeGradeModeAndVariableCredit();
	
	@DefaultMessage("Grade Modes")
	String dialogChangeGradeMode();
	
	@DefaultMessage("Variable Credits")
	String dialogChangeVariableCredit();
	
	@DefaultMessage("Request Variable Title Course")
	String dialogRequestVariableTitleCourse();
	
	@DefaultMessage("Retrieving available grade modes and/or variable credits...")
	String waitRetrieveGradeModes();
	
	@DefaultMessage("Requesting grade mode and/or credit changes...")
	String waitChangeGradeModes();
	
	@DefaultMessage("Requesting variable title course...")
	String waitRequestVariableTitleCourse();
	
	@DefaultMessage("Unable to get available grade modes and/or variable credits: {0}")
	String exceptionRetrieveGradeModes(String message);
	
	@DefaultMessage("Failed to request grade mode and/or variable credit changes: {0}")
	String exceptionChangeGradeModes(String message);
	
	@DefaultMessage("Failed to request variable title course: {0}")
	String exceptionRequestVariableTitleCourse(String message);
	
	@DefaultMessage("Failed to load request course: {0}")
	String exceptionGetVariableTitleCourseInfo(String message);
	
	@DefaultMessage("No grade mode or credit changes are available.")
	String statusNoGradeModeChangesAvailable();
	
	@DefaultMessage("No grade mode or credit changes have been requested.")
	String statusNoGradeModeChangesMade();
	
	@DefaultMessage("Grade mode and/or credit changes have been successfully requested.")
	String statusGradeModeChangesRequested();
	
	@DefaultMessage("Grade mode and/or credit  changes have been successfully applied.")
    String statusGradeModeChangesApplied();
	
	@DefaultMessage("Variable title course has been successfully requested.")
	String statusVariableCourseRequested();
	
	@DefaultMessage("A variable title course meeting your selection already exists. Do you want to submit a request anyway?")
	String questionVariableCourseAlreadyExists();
	
	@DefaultMessage("There are no courses with grade modes and/or variable credits.")
	String emptyGradeChanges();
	
	@DefaultMessage("Submit Changes")
	String buttonSubmitGradeModeChanges();
	
	@DefaultMessage("Close Dialog")
	String buttonCloseGradeModeChanges();
	
	@DefaultMessage("Request Course")
	String buttonSubmitVariableTitleCourse();
	
	@DefaultMessage("Close Dialog")
	String buttonCloseVariableTitleCourse();
	
	@DefaultMessage("The following grade mode changes are being requested:")
	String gradeModeListChanges();
	
	@DefaultMessage("The following credit changes are being requested:")
	String varCreditListChanges();
	
	@DefaultMessage("Maximum of {1,number,0.#} credit hours exceeded. An override for {0,number,0.#} credit hours will be requested.")
	String varCreditMaxExceeded(float curent, float max);
	
	@DefaultMessage("- {0}: No approval is needed.")
	String gradeModeNoApprovalNeeded(String gradeMode);
	
	@DefaultMessage("- {0}: {1} approval is needed.")
	String gradeModeApprovalNeeded(String gradeMode, String approvals);
	
	@DefaultMessage("- {0,number,0.#} credit hours: No approval is needed.")
	String varCreditNoApprovalNeeded(Float credit);
	
	@DefaultMessage("- {0,number,0.#} credit hours: {1} approval is needed.")
	String varCreditApprovalNeeded(Float credit, String approvals);
	
	@DefaultMessage(
			"\nPlease, select <b>Submit Changes</b> to apply for the selected grade mode and/or credit changes. "
			+ "The chosen grade mode and/or credit changes will be submitted for the required approvals. "
			+ "If no approval is necessary, grade mode and/or credit will change immediately. "
			+ "Click <b>Close Dialog</b> to cancel this request and go back to explore other scheduling options.")
	String gradeModeChangeOptions();
		
	@DefaultMessage("<b>Request Note:</b>")
	String gradeModeChangesNote();
	
	@DefaultMessage("\n<b>Disclaimer:</b>")
	String gradeModeDisclaimers();
	
	@DefaultMessage("Not Set")
	String gradeModeItemNotSet();
	
	@DefaultMessage("{0} (Mixed)")
	String gradeModeItemNotSame(String gm);
	
	@DefaultMessage("You are making changes to {0} that has an honors grade mode. This will change the grade mode back to the regular grade mode for this course. Do you want to proceed?")
    String confirmEnrollmentHonorsGradeModeChange(String course);
	
	@DefaultMessage("You are making changes to {0} that has variable credit hours. This will change the credit hours back to the minimum for this course. Do you want to proceed?")
    String confirmEnrollmentVariableCreditChange(String course);
	
	@DefaultMessage("Requested classes do not match your current schedule. The honors grade mode change cannot be processed. Please request a new grade mode change.")
	String hintSpecRegHonorsGradeModeNotMatchingSchedule();
	
	@DefaultMessage("{0} is no longer in your schedule.")
	String specRegRemoved(String clazz);
	
	@DefaultMessage("All approval requests have been approved and applied.")
	String specRegAllRequestsFullyApplied();
	
	@DefaultMessage("Critical")
	String opSetCritical();
	
	@DefaultMessage("Important")
	String opSetImportant();
	
	@DefaultMessage("Vital")
	String opSetVital();
	
	@DefaultMessage("LC")
	String opSetLC();
	
	@DefaultMessage("Not Critical")
	String opSetNotCritical();
	
	@DefaultMessage("Use Default")
	String opSetCriticalNotSet();
	
	@DefaultMessage("Visiting F2F")
	String opSetVisitingF2F();
	
	@DefaultMessage("Requested Overrides for {0}")
	String dialogChangeRequestNote(String course);
	
	@DefaultMessage("Requested Overrides")
	String dialogChangeCreditRequestNote();
	
	@DefaultMessage("Requested Approvals")
	String dialogChangeSpecRegRequestNote();
	
	@DefaultMessage("Change Note")
	String buttonChangeRequestNote();
	
	@DefaultMessage("Change request note for the above registration errors")
	String titleChangeRequestNote();
	
	@DefaultMessage("Close Dialog")
	String buttonHideRequestNote();
	
	@DefaultMessage("Close this dialog without making any changes")
	String titleHideRequestNote();
	
	@DefaultMessage("No request note. Click here to provide a note.")
	String noRequestNoteClickToChange();
	
	@DefaultMessage("Do you really want to change note for {0}\u00a0students?")
	String confirmNoteChange(int studentCount);
	
	@DefaultMessage("Do you really want to send an email to {0}\u00a0students?")
	String confirmSendEmail(int studentCount);
		
	@DefaultMessage("Do you really want to change status to {0} for {1}\u00a0students?")
	String confirmStatusChange(String newStatus, int studentCount);
	
	@DefaultMessage("Do you really want to change note and set status to {0} for {1}\u00a0students?")
	String confirmStatusNoteChange(String newStatus, int studentCount);
	
	@DefaultMessage("<u>L</u>ookup Student")
	String buttonLookupStudent();
	
	@DefaultMessage("<u>S</u>ubmit")
	String buttonSubmitPrint();
	
	@DefaultMessage("There are unsaved changes on this page. Do you really want to discard these changes?")
	String queryLeaveAdvisorsCourseRequestsNotSave();
	
	@DefaultMessage("Student\u2019s Name:")
	String propStudentName();
	
	@DefaultMessage("Email:")
	String propStudentEmail();
	
	@DefaultMessage("Id:")
	String propStudentExternalId();
	
	@DefaultMessage("Advisor/Email:")
	String propAdvisorEmail();
	
	@DefaultMessage("Term:")
	String propAcademicSession();
	
	@DefaultMessage("Additional Notes:")
	String propAdvisorNotes();
	
	@DefaultMessage("PIN:")
	String propStudentPin();
	
	@DefaultMessage("Lookup Student")
	String dialogStudentLookup();
	
	@DefaultMessage("Credit Hours&nbsp;&nbsp;&nbsp;Notes")
	String headCreditHoursNotes();
		
	@DefaultMessage("Wait-List")
	String headWaitList();
	
	@DefaultMessage("No-Subs")
	String headNoSubs();
	
	@DefaultMessage("{0,number,0.#}")
	String credit(float total);
	
	@DefaultMessage("{0,number,0.#} - {1,number,0.#}")
	String creditRange(float minTotal, float maxTotal);
	
	@DefaultMessage("Total Priority Credit Hours:")
	String labelTotalPriorityCreditHours();
	
	@DefaultMessage("Status:")
	String propStudentStatus();
	
	@DefaultMessage("Advisor Course Recommendations")
	String pdfHeaderAdvisorCourseRequests();
	
	@DefaultMessage("Validating...")
	String advisorCourseRequestsValidating();
	
	@DefaultMessage("Saving...")
	String advisorCourseRequestsSaving();
	
	@DefaultMessage("Exporting...")
	String advisorCourseRequestsExporting();
	
	@DefaultMessage("Advisor\u2019s Signature:     _____________________________________________")
	String pdfAdvisorSignature();
	
	@DefaultMessage("Student\u2019s Signature:     _____________________________________________")
	String pdfStudentSignature();
	
	@DefaultMessage("Date:      _______________")
	String pdfSignatureDate();
	
	@DefaultMessage("X")
	String pdfCourseWaitListed();
	
	@DefaultMessage("")
	String pdfCourseNotWaitListed();
	
	@DefaultMessage("X")
	String pdfCourseCritical();
	
	@DefaultMessage("X")
	String pdfCourseVital();
	
	@DefaultMessage("X")
	String pdfCourseImportant();

	@DefaultMessage("")
	String pdfCourseNotCritical();
	
	@DefaultMessage("A<u>d</u>visor Recommendations")
	String buttonAdvisorCourseRequests();
	
	@DefaultMessage("{0} reservation")
	String noteHasGroupReservation(String group);
	
	@DefaultMessage("Individual reservation")
	String noteHasIndividualReservation();
	
	@DefaultMessage("Enrolled in {0} on {1}")
	String noteEnrolled(String course, String date);
	
	@DefaultMessage("Success!")
	String advisorRequestsSubmitOK();
	
	@DefaultMessage("Click <a href=\"{0}\" style='color: inherit; background-color : transparent;' target='_blank'>here</a> to download the Advisor Course Recommendations PDF.")
	String advisorRequestsPdfLink(String link);
	
	@DefaultMessage("Email sent.")
	String advisorRequestsEmailSent();
	
	@DefaultMessage("Submit failed: {0}")
	String advisorRequestsSubmitFailed(String reason);
	
	@DefaultMessage("Validation failed: {0}")
	String advisorRequestsValidationFailed(String reason);
	
	@DefaultMessage("Email failed: {0}")
	String advisorRequestsEmailFailed(String reason);
	
	@DefaultMessage("Failed to load student: {0}")
	String advisorRequestsLoadFailed(String reason);
	
	@DefaultMessage("Loading data for {0}...")
	String loadingAdvisorRequests(String student);
	
	@DefaultMessage("Student Course Requests")
	String studentCourseRequests();
	
	@DefaultMessage("Advisor Course Recommendations")
	String advisorCourseRequests();
	
	@DefaultMessage("Advisor Course Recommendations")
	String dialogAdvisorCourseRequests();
	
	@DefaultMessage("Total Priority Credit Hours")
	String rowTotalPriorityCreditHours();
	
	@DefaultMessage("<u>A</u>pply")
	String buttonAdvisorRequestsApply();
	
	@DefaultMessage("<u>C</u>lose")
	String buttonAdvisorRequestsClose();
	
	@DefaultMessage("<u>A</u>dvisor Recommendations")
	String buttonAdvisorRequests();
	
	@DefaultMessage("Show course requests filled in with the advisor.")
	String hintAdvisorRequests();
	
	@DefaultMessage("Retrieving advisor course recommendations...")
	String waitAdvisorRequests();
	
	@DefaultMessage("Failed to load advisor course recommendations: {0}")
	String failedAdvisorRequests(String reason);
	
	@DefaultMessage("Advisor <u>R</u>ecommendations")
	String tabAdvisorRequests();
	
	@DefaultMessage("Course requests not filled in.")
	String advChangesNoCourseRequests();
	
	@DefaultMessage("&times; Missing {0}.")
	String advChangesMissingCourse(String course);
	
	@DefaultMessage("&times; Missing {0} and its alternatives.")
	String advChangesMissingCourseWithAllAlts(String course);
	
	@DefaultMessage("Missing {0} but has {1}.")
	String advChangesMissingCourseButHasAlt(String course, String alt);
	
	@DefaultMessage("&uArr; {0} moved to priority {1}.")
	String advChangesSubstituteToPrimary(String course, int priority);
	
	@DefaultMessage("&dArr; {0} moved to substitute {1}.")
	String advChangesPrimaryToSubstitute(String course, int priority);
	
	@DefaultMessage("1st choice changed to {0}.")
	String advChanges1stChoiceChanged(String courses);
	
	@DefaultMessage("Alternative to {0}.")
	String advChangesDifferent1stChoice(String courses);
	
	@DefaultMessage("Moved to 1st choice.")
	String advChangesMoved1stChoice();
	
	@DefaultMessage("Moved to 2nd choice.")
	String advChangesMoved2ndChoice();
	
	@DefaultMessage("Moved to 3rd choice.")
	String advChangesMoved3rdChoice();
	
	@DefaultMessage("Moved to priority {0}.")
	String advChangesMovedToPriority(int prio);
	
	@DefaultMessage("Moved to substitute {0}.")
	String advChangesMovedToSubstitute(int prio);
	
	@DefaultMessage("{0,number,0.#} - {1,number,0.#}")
	String advisedCreditRange(float minCred, float maxCred);

	@DefaultMessage("{0,number,0.#}")
	String advisedCredit(float cred);
	
	@DefaultMessage("Advisor note: {0} ({1} credit hours)")
	String advisorNoteWithCredit(String note, String credit);
	
	@DefaultMessage("Advisor note: {0}")
	String advisorNote(String note);
	
	@DefaultMessage("Total Priority Credit Hours: {0}")
	String hintAdvisedCredit(String crit);
	
	@DefaultMessage("Missing {0} critical course(s) and {1} other primary course(s).")
	String hintAdvisedMissingCriticalOther(int critical, int other);
	
	@DefaultMessage("Missing {0} critical course(s).")
	String hintAdvisedMissingCritical(int critical);
	
	@DefaultMessage("Missing {0} primary course(s).")
	String hintAdvisedMissingOther(int other);
	
	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> cr</sup></span> + {1}")
	@DoNotTranslate
	String advisedMissingCriticalOther(int critical, int other);
	
	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> critical</sup></span>")
	@DoNotTranslate
	String advisedMissingCritical(int critical);
	
	@DefaultMessage("{0}")
	@DoNotTranslate
	String advisedMissingPrimary(int primary);
	
	@DefaultMessage("Not enrolled {0} critical course(s) and {1} other primary course(s).")
	String hintAdvisedNotAssignedCriticalOther(int critical, int other);
	
	@DefaultMessage("Not enrolled {0} critical course(s).")
	String hintAdvisedNotAssignedCritical(int critical);
	
	@DefaultMessage("Not enrolled {0} primary course(s).")
	String hintAdvisedNotAssignedOther(int other);
	
	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> cr</sup></span> + {1}")
	@DoNotTranslate
	String advisedNotAssignedCriticalOther(int critical, int other);
	
	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> critical</sup></span>")
	@DoNotTranslate
	String advisedNotAssignedCritical(int critical);
	
	@DefaultMessage("{0}")
	@DoNotTranslate
	String advisedNotAssignedPrimary(int primary);
	
	@DefaultMessage("Missing critical course {0}.")
	String advMessageMissingCriticalCourse(String course);
	
	@DefaultMessage("Missing critical course {0} and its alternatives.")
	String advMessageMissingCriticalCourseWithAlts(String course);
	
	@DefaultMessage("Missing critical course {0}, but has alternative(s).")
	String advMessageMissingCriticalCourseHasAlts(String course);
	
	@DefaultMessage("Missing course {0}.")
	String advMessageMissingCourse(String course);
	
	@DefaultMessage("Missing course {0} and its alternatives.")
	String advMessageMissingCourseWithAlts(String course);
	
	@DefaultMessage("Missing course {0}, but has alternative(s).")
	String advMessageMissingCourseHasAlts(String course);
	
	@DefaultMessage("Missing substitute course {0}.")
	String advMessageMissingSubstituteCourse(String course);
	
	@DefaultMessage("Missing substitute course {0} and its alternatives.")
	String advMessageMissingSubstituteCourseWithAlts(String course);
	
	@DefaultMessage("Not-enrolled course {0} or its alternatives.")
	String advMessageNotEnrolledCourseWithAlts(String course);
	
	@DefaultMessage("Not-enrolled course {0}.")
	String advMessageNotEnrolledCourse(String course);
	
	@DefaultMessage("But has a substitue course {0} enrolled instead.")
	String advMessageHasEnrolledSubstituteCourse(String course);
	
	@DefaultMessage("Not-enrolled critical course {0} or its alternatives.")
	String advMessageNotEnrolledCriticalCourseWithAlts(String course);
	
	@DefaultMessage("Not-enrolled critical course {0}.")
	String advMessageNotEnrolledCriticalCourse(String course);
	
	@DefaultMessage("Missing some alternatives of {0}")
	String advMessageMissingAlternatives(String course);
	
	@DefaultMessage("Send email confirmation")
	String checkSendEmailConfirmation();
	
	@DefaultMessage("Course {0} is enrolled.")
	String titleCourseEnrolled(String course);
	
	@DefaultMessage("Click to copy to clipboard.")
	String changeLogClickToCopyToClipboard();
	
	@DefaultMessage("Property value copied to clipboard.")
	String changeLogPropertyValueCopiedToClipbard();
	
	@DefaultMessage("Your UniTime session may have expired. Click here to log in again.")
	String sessionExpiredClickToLogin();
	
	@DefaultMessage("PIN: {0}")
	String advisorNotePin(String pin);
	
	@DefaultMessage("Not released to the student.")
	String pinNotReleasedToStudent();
	
	@DefaultMessage("Count")
	String colAdvisorNotesCount();
	
	@DefaultMessage("Note")
	String colAdvisorNotesNote();
	
	@DefaultMessage("Last Used")
	String colAdvisorNotesTime();
	
	@DefaultMessage("Course:")
	String propReqVTCourseCourse();
	
	@DefaultMessage("Details:")
	String propReqVTCourseDetails();
	
	@DefaultMessage("Title:")
	String propReqVTCourseTitle();
	
	@DefaultMessage("Course title must be provided.")
	String errorReqVTCourseNoTitle();
	
	@DefaultMessage("Instructor must be provided.")
	String errorReqVTCourseNoInstructor();
	
	@DefaultMessage("Please put in the instructor name/email in the notes below.")
	String hintReqVTCourseNoInstructorMatch();
	
	@DefaultMessage("Credit:")
	String propReqVTCourseCredit();
	
	@DefaultMessage("Instructor:")
	String propReqVTCourseInstructor();
	
	@DefaultMessage("Grade Mode:")
	String propReqVTCourseGradeMode();
	
	@DefaultMessage("Dates:")
	String propReqVTCourseDates();
	
	@DefaultMessage("From: ")
	String propReqVTCourseDatesFrom();
	
	@DefaultMessage("- To: ")
	String propReqVTCourseDatesTo();
	
	@DefaultMessage("Note:")
	String propReqVTCourseNote();
	
	@DefaultMessage("Disclaimer:")
	String propReqVTCourseDisclaimer();
	
	@DefaultMessage("")
	String propReqVTMaxCredit();
	
	@DefaultMessage("An example dislaimer here.\nI solemnly swear that i am up to no good.")
	String disclaimerRequestVariableTitle();
	
	@DefaultMessage("None of the above")
	String itemReqVTNoInstructor();
	
	@DefaultMessage("Wait-list overrides not requested.")
	String waitListOverridesNotRequested();
	
	@DefaultMessage("Wait-list overrides have been successfully requested.")
	String waitListOverridesRequested();
	
	@DefaultMessage("Checking wait-lists...")
	String waitValidateWaitLists();
	
	@DefaultMessage("Requesting wait-list overrides...")
	String waitRequestWaitListOverrides();
	
	@DefaultMessage("Failed to request wait-list overrides: {0}")
	String failedRequestWaitListOverrides(String message);
	
	@DefaultMessage("Wait-Listed Courses")
	String panelWaitListedCourses();
	
	@DefaultMessage("Course {0} will no longer be wait-listed. Do you want to proceed?")
	String confirmCourseDropFromWaitList(String course);
	
	@DefaultMessage("Course {0} cannot be wait-listed as there is a pending approval requested already.")
	String errorWaitListApprovalAlreadyRequested(String course);
	
	@DefaultMessage("Please cancel the corresponding approval requests and click the Submit Schedule again.")
	String errorWaitListApprovalCancelFirst();
	
	@DefaultMessage("Course {0} has a pending approval requested already. Cancel the approval request before wait-listing it.")
	String messageWaitListApprovalAlreadyRequested(String course);
	
	@DefaultMessage("{0} of {1}")
	String waitListPosition(Integer position, Integer total);
	
	@DefaultMessage("Wait-List Preferences")
	String dialogWaitListedRequestPreferences();
	
	@DefaultMessage("Update Preferences")
	String buttonSubmitWaitListedRequestPreferences();
	
	@DefaultMessage("Close Dialog")
	String buttonCloseWaitListedRequestPreferences();
	
	@DefaultMessage("Update wait-list preferences for {0}")
	String iconWaitListedRequestPreferences(String course);
	
	@DefaultMessage("Wait-Listed:")
	String propWaitListSwapWithWaitListed();
	
	@DefaultMessage("New wait-list")
	String checkWaitListSwapWithNewWaitList();
	
	@DefaultMessage("Swap with course:")
	String propWaitListSwapWithCourseOffering();
	
	@DefaultMessage("When a course is selected, it will be automatically swapped with the wait-listed course. This means that it will be only dropped when you can get the wait-listed course instead.")
	String descWaitListSwapWithCourseOffering();
	
	@DefaultMessage("No swap course selected.")
	String itemWaitListSwapWithNoCourseOffering();
	
	@DefaultMessage("Replaces {0}.")
	String conflictWaitListSwapWithNoCourseOffering(String course);
	
	@DefaultMessage("Requires {0}.")
	String conflictRequiredPreferences(String prefs);
	
	@DefaultMessage("Replaces")
	String colWaitListSwapWithCourseOffering();

	@DefaultMessage("<u>P</u>references")
	String buttonStudentSchedulingPreferences();
	
	@DefaultMessage("Show student scheduling prefernences for the selected academic session.")
	String hintStudentSchedulingPreferences();
	
	@DefaultMessage("Student Scheduling Preferences")
	String dialogStudentSchedulingPreferences();
	
	@DefaultMessage("Class Dates:")
	String propSchedulingPrefDates();
	
	@DefaultMessage("From: ")
	String propSchedulingPrefDatesFrom();
	
	@DefaultMessage("- To: ")
	String propSchedulingPrefDatesTo();
	
	@DefaultMessage("The above dates are optional, and it is also possible to put in just the start or the end date. When provided, UniTime is not allowed to select a class that falls outside of the above dates.")
	String propSchedulingPrefDatesDescription();
	
	@DefaultMessage("Class Modality:")
	String propSchedulingPrefModality();
	
	@DefaultMessage("Prefer Face-to-Face")
	String itemSchedulingModalityPreferFaceToFace();
	
	@DefaultMessage("When possible, UniTime tries to avoid arranged hours and online classes.")
	String descSchedulingModalityPreferFaceToFace();
	
	@DefaultMessage("Prefer Online")
	String itemSchedulingModalityPreferOnline();
	
	@DefaultMessage("When possible, UniTime tries to avoid face-to-face classes.")
	String descSchedulingModalityPreferOnline();
	
	@DefaultMessage("Require Online")
	String itemSchedulingModalityRequireOnline();
	
	@DefaultMessage("UniTime is not allowed to select a class that is not online.")
	String descSchedulingModalityRequireOnline();
	
	@DefaultMessage("No Preference")
	String itemSchedulingModalityNoPreference();
	
	@DefaultMessage("")
	String descSchedulingModalityNoPreference();
	
	@DefaultMessage("Schedule Gaps:")
	String propSchedulingPrefBackToBack();
	
	@DefaultMessage("No Preference")
	String itemSchedulingBackToBackNoPreference();
	
	@DefaultMessage("")
	String descSchedulingBackToBackNoPreference();
	
	@DefaultMessage("Prefer Back-to-Backs")
	String itemSchedulingBackToBackPrefer();
	
	@DefaultMessage("When possible, UniTime tries to avoid gaps in the schedule (dense schedule).")
	String descSchedulingBackToBackPrefer();

	@DefaultMessage("Avoid Back-to-Backs")
	String itemSchedulingBackToBackDiscourage();
	
	@DefaultMessage("When possible, UniTime tries to avoid back-to-back classes (sparse schedule).")
	String descSchedulingBackToBackDiscourage();

	@DefaultMessage("<u>S</u>ave")
	String buttonSchedulingPrefApply();
	
	@DefaultMessage("<u>C</u>lose")
	String buttonSchedulingPrefClose();
	
	@DefaultMessage("Failed to update preferences: {0}")
	String failedToUpdatePreferences(String reason);
	
	@DefaultMessage("Failed to load preferences: {0}")
	String failedToLoadPreferences(String reason);
	
	@DefaultMessage("Student scheduling preferences have been updated.")
	String infoSchedulingPreferencesUpdated();
	
	@DefaultMessage("From {0}")
	String schedulingPrefClassesFrom(String date);
	
	@DefaultMessage("To {0}")
	String schedulingPrefClassesTo(String date);
	
	@DefaultMessage("Between {0} and {1}")
	String schedulingPrefClassesBetween(String fromDate, String toDate);

	@DefaultMessage("Together with other registration changes, the vital course {0} will be dropped. This may prohibit progress towards degree. Please consult with your academic advisor. Do you want to proceed?")
	String confirmEnrollmentVitalCourseDrop(String course);
	
	@DefaultMessage("Together with other registration changes, the important course {0} will be dropped. This may prohibit progress towards degree. Please consult with your academic advisor. Do you want to proceed?")
	String confirmEnrollmentImportantCourseDrop(String course);

	@DefaultMessage("Missing vital course {0}.")
	String advMessageMissingVitalCourse(String course);
	
	@DefaultMessage("Missing vital course {0} and its alternatives.")
	String advMessageMissingVitalCourseWithAlts(String course);
	
	@DefaultMessage("Missing vital course {0}, but has alternative(s).")
	String advMessageMissingVitalCourseHasAlts(String course);
	
	@DefaultMessage("Not-enrolled vital course {0} or its alternatives.")
	String advMessageNotEnrolledVitalCourseWithAlts(String course);
	
	@DefaultMessage("Not-enrolled vital course {0}.")
	String advMessageNotEnrolledVitalCourse(String course);

	@DefaultMessage("Missing {0} vital course(s) and {1} other primary course(s).")
	String hintAdvisedMissingVitalOther(int vital, int other);
	
	@DefaultMessage("Missing {0} vital course(s).")
	String hintAdvisedMissingVital(int vital);

	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> vt</sup></span> + {1}")
	@DoNotTranslate
	String advisedMissingVitalOther(int vital, int other);
	
	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> vital</sup></span>")
	@DoNotTranslate
	String advisedMissingVital(int vital);
	
	@DefaultMessage("Not enrolled {0} vital course(s) and {1} other primary course(s).")
	String hintAdvisedNotAssignedVitalOther(int vital, int other);
	
	@DefaultMessage("Not enrolled {0} vital course(s).")
	String hintAdvisedNotAssignedVital(int vital);
	
	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> vt</sup></span> + {1}")
	@DoNotTranslate
	String advisedNotAssignedVitalOther(int vital, int other);
	
	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> vital</sup></span>")
	@DoNotTranslate
	String advisedNotAssignedVital(int vital);
	
	@DefaultMessage("Missing important course {0}.")
	String advMessageMissingImportantCourse(String course);
	
	@DefaultMessage("Missing important course {0} and its alternatives.")
	String advMessageMissingImportantCourseWithAlts(String course);
	
	@DefaultMessage("Missing important course {0}, but has alternative(s).")
	String advMessageMissingImportantCourseHasAlts(String course);
	
	@DefaultMessage("Not-enrolled important course {0} or its alternatives.")
	String advMessageNotEnrolledImportantCourseWithAlts(String course);
	
	@DefaultMessage("Not-enrolled important course {0}.")
	String advMessageNotEnrolledImportantCourse(String course);

	@DefaultMessage("Missing {0} important course(s) and {1} other primary course(s).")
	String hintAdvisedMissingImportantOther(int important, int other);
	
	@DefaultMessage("Missing {0} important course(s).")
	String hintAdvisedMissingImportant(int important);

	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> im</sup></span> + {1}")
	@DoNotTranslate
	String advisedMissingImportantOther(int important, int other);
	
	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> important</sup></span>")
	@DoNotTranslate
	String advisedMissingImportant(int important);
	
	@DefaultMessage("Not enrolled {0} important course(s) and {1} other primary course(s).")
	String hintAdvisedNotAssignedImportantOther(int important, int other);
	
	@DefaultMessage("Not enrolled {0} important course(s).")
	String hintAdvisedNotAssignedImportant(int important);
	
	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> im</sup></span> + {1}")
	@DoNotTranslate
	String advisedNotAssignedImportantOther(int important, int other);
	
	@DefaultMessage("<span style='color:red;font-weight:bold;'>{0}<sup> important</sup></span>")
	@DoNotTranslate
	String advisedNotAssignedImportant(int important);
	
	@DefaultMessage("You are trying to wait-list for a different section of {0} without indicating which section(s) you need. The wait-list for {0} will not be active. Do you want to proceed?")
    String confirmSectionSwapNoPrefs(String course);
	
	@DefaultMessage("Max Credit")
	String tabRequestNoteMaxCredit();
	
	@DefaultMessage("There has been a change")
	String emailReschedulingReason();
	
	@DefaultMessage("One or more classes have been cancelled.")
	String reschedulingReasonCancelledClass();
	
	@DefaultMessage("There is a time conflict without an override.")
	String reschedulingReasonTimeConflict();
	
	@DefaultMessage("Enrolled in a class combination that is not allowed.")
	String reschedulingReasonClassLink();
	
	@DefaultMessage("Missing a class.")
	String reschedulingReasonMissingClass();
	
	@DefaultMessage("Enrolled in classes from two or more configurations.")
	String reschedulingReasonMultipleConfigs();
	
	@DefaultMessage("Enrolled in multiple classes of the same type.")
	String reschedulingReasonMultipleClasses();
	
	@DefaultMessage("Course is no longer requested.")
	String reschedulingReasonNoRequest();
	
	@DefaultMessage("Re-Scheduling:")
	String propStatusReSchedule();
	
	@DefaultMessage("Student can be re-scheduled after a course change.")
	String messageStatusReSchedule();
	
	@DefaultMessage("Student can NOT be re-scheduled after a course change.")
	String messageStatusNoReSchedule();
	
	@DefaultMessage("Credit:")
	String propCourseFinderFilterCredit();
	
	@DefaultMessage("From: ")
	String propCourseFinderFilterCreditFrom();
	
	@DefaultMessage("- To: ")
	String propCourseFinderFilterCreditTo();
	
	@DefaultMessage("Instructor:")
	String propCourseFinderFilterInstructor();
	
	@DefaultMessage("Filter")
	String sectCourseFinderFilter();
	
	@DefaultMessage("Click to open the filter.")
	String descCourseFinderFilterClosed();
	
	@DefaultMessage("Clock to close the filter.")
	String descCourseFinderFilterOpened();
	
	@DefaultMessage("classes between {0} and {1}")
	String filterClassesBetweenDates(String fromDate, String toDate);
	
	@DefaultMessage("classes from {0}")
	String filterClassesFromDate(String fromDate);
	
	@DefaultMessage("classes to {0}")
	String filterClassesToDate(String toDate);
	
	@DefaultMessage("{0,number,0.#} or more credit")
	String filterCreditFrom(float credit);
	
	@DefaultMessage("{0,number,0.#} or fewer credit")
	String filterCreditTo(float credit);
	
	@DefaultMessage("credit between {0,number,0.#} and {1,number,0.#}")
	String filterCreditBetween(float creditFrom, float creditTo);
	
	@DefaultMessage("{0,number,0.#} credit")
	String filterCreditEquals(float credit);
	
	@DefaultMessage("instructor {0}")
	String filterInstructor(String instructor);
	
	@DefaultMessage("Showing courses with {0}")
	String messageCourseFinderFilterText(String filter);
	
	@DefaultMessage("Show All Columns")
	String opShowAllColumns();
	
	@DefaultMessage("Enabled Notifications:")
	String propEnabledStudentNotifications();
	
	@DefaultMessage("Student Request Change")
	String notifStudentChangeRequest();
	
	@DefaultMessage("Student Enrollment Change")
	String notifStudentChangeEnrollment();
	
	@DefaultMessage("Admin Request Change")
	String notifAdminChangeRequest();
	
	@DefaultMessage("Admin Enrollment Change")
	String notifAdminChangeEnrollment();
	
	@DefaultMessage("Enrollment Approval")
	String notifAdminEnrollmentApproval();
	
	@DefaultMessage("Course Enrollment Change")
	String notifCourseChangeEnrollment();
	
	@DefaultMessage("Course Failed Enrollment Change")
	String notifCourseChangeEnrollmentFailed();
	
	@DefaultMessage("Course Schedule Change")
	String notifCourseChangeSchedule();
	
	@DefaultMessage("External Enrollment Change")
	String notifExternalChangeEnrollment();
	
	@DefaultMessage("Instructor Schedule")
	String emailInstructorDeafultTitle();
	
	@DefaultMessage("Instructor schedule change for %session%")
	String emailInstructorChangeSubject();
	
	@DefaultMessage("{0} {1} Assignment / Schedule Changes")
	String emailCourseAssignment(String subject, String courseNbr);
	
	@DefaultMessage("Assignment / Schedule Changes")
	String emailAssignmentChanges();
	
	@DefaultMessage("No schedule change detected.")
	String emailNoScheduleChange();
	
	@DefaultMessage("Current Instructor Assignments")
	String emailInstructorClassList();
	
	@DefaultMessage("For an up to date schedule, please visit <a href='{0}/selectPrimaryRole.action?target=gwt.jsp%3Fpage%3Dpersonal' style='color: inherit; background-color : transparent;'>{0}</a>.")
	String emailLinkToPersonalSchedule(String baseUrl);
	
	@DefaultMessage("Class cancelled.")
	String emailClassCancelled();
	
	@DefaultMessage("Class reopened.")
	String emailClassReopened();
	
	@DefaultMessage("Class assigned.")
	String emailClassAssigned();
	
	@DefaultMessage("Class removed.")
	String emailClassUnassigned();
	
	@DefaultMessage("Share")
	String colPercentShare();
	
	@DefaultMessage("{0} (was {1})")
	String textDiff(String newValue, String oldValue);
	
	@DefaultMessage("N/A")
	String textNotApplicable();
	
	@DefaultMessage("For an up to date schedule, please visit {0}.")
	String textEmailLinkToUniTime(String baseUrl);
	
	@DefaultMessage("Enrolled in {0} instead.")
	String enrolledInAlt(String course);
	
	@DefaultMessage("Advisor:")
	String propAdvisor();
	
	@DefaultMessage("{0} Recommendations")
	String otherSessionRecommendations(String campus);
	
	@DefaultMessage("PIN")
	String colStudentPin();
	
	@DefaultMessage("Release PIN")
	String releaseStudentPin();
	
	@DefaultMessage("Suppress PIN")
	String suppressStudentPin();
	
	@DefaultMessage("Releasing student PINs...")
	String releasingStudentPins();
	
	@DefaultMessage("Suppressing student PINs...")
	String supressingStudentPins();
	
	@DefaultMessage("PIN Released")
	String modePinReleased();
	
	@DefaultMessage("PIN Suppressed")
	String modePinSuppressed();
	
	@DefaultMessage("My PIN Released")
	String modeMyPinReleased();
	
	@DefaultMessage("My PIN Suppressed")
	String modeMyPinSuppressed();
	
	@DefaultMessage("Configuration {0} is not allowed due to a restriction.")
	String unavailableConfigNotAllowedDueToRestrictions(String cfg);
	
	@DefaultMessage("A reservation that must be followed is full.")
	String unavailableMustUseReservationIsFull();
	
	@DefaultMessage("Course {0} is full.")
	String unavailableCourseIsFull(String course);
	
	@DefaultMessage("Course {0} is reserved.")
	String unavailableCourseIsReserved(String course);
	
	@DefaultMessage("Configuration {0} is reserved.")
	String unavailableConfigIsReserved(String config);
	
	@DefaultMessage("Configuration {0} is full.")
	String unavailableConfigIsFull(String config);
	
	@DefaultMessage("Not allowed due to a restriction.")
	String unavailableNotAllowed();
	
	@DefaultMessage("Not allowed due to a reservation.")
	String unavailableDueToReservation();
	
	@DefaultMessage("Section {0} is cancelled.")
	String unavailableSectionCancelled(String section);
	
	@DefaultMessage("Section {0} is reserved.")
	String unavailableSectionReserved(String section);
	
	@DefaultMessage("Section {0} is disabled for student scheduling.")
	String unavailableSectionDisabled(String section);
	
	@DefaultMessage("Section {0} is conflicting with {1}.")
	String unavailableSectionConflict(String section, String other);
	
	@DefaultMessage("Cannot take {0} due to a reservation.")
	String unavailableDueToMustTakeReservation(String section);
	
	@DefaultMessage("Initial enrollment must be followed.")
	String unavailableNotInitial();
	
	@DefaultMessage("Fixed enrollment must be followed.")
	String unavailableNotFixed();
	
	@DefaultMessage("Section {0} is not allowed by student preferences.")
	String unavailableStudentPrefs(String section);
	
	@DefaultMessage("Section {0} is not allowed due to a restriction.")
	String unavailableStudentRestrictions(String section);
	
	@DefaultMessage("Section {0} overlaps with student unavailability.")
	String unavailableStudentUnavailabilities(String section);
	
	@DefaultMessage("A reservation that must be followed is full.")
	String unavailableMustTakeReservationIsFull();
}