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
 * @author Tomas Muller
 */
public interface ExaminationMessages extends Messages {
	
	@DefaultMessage("Filter")
	String filter();

	@DefaultMessage("Normal")
	String seatingNormal();

	@DefaultMessage("Exam")
	String seatingExam();

	@DefaultMessage("Final")
	String typeFinal();
	
	@DefaultMessage("Midterm")
	String typeMidterm();
	
	@DefaultMessage("{0} Examinations")
	String tableExaminations(String type);
	
	@DefaultMessage("Classes / Courses")
	String colExamOwner();
	
	@DefaultMessage("Length")
	String colExamLength();
	
	@DefaultMessage("Seating|Type")
	String colExamSeatingType();
	
	@DefaultMessage("Size")
	String colExamSize();
	
	@DefaultMessage("Max|Rooms")
	String colExamMaxRooms();
	
	@DefaultMessage("Instructor")
	String colExamInstructor();
	
	@DefaultMessage("Period|Preferences")
	String colExamPeriodPrefs();
	
	@DefaultMessage("Room|Preferences")
	String colExamRoomPrefs();
	
	@DefaultMessage("Distribution|Preferences")
	String colExamDistributionPrefs();
	
	@DefaultMessage("Assigned|Period")
	String colExamAssignedPeriod();
	
	@DefaultMessage("Assigned|Room")
	String colExamAssignedRoom();
	
	@DefaultMessage("Subject")
	String colExamOwnerSubject();
	
	@DefaultMessage("Course<br>Number")
	String colExamOwnerCourseNumber();
	
	@DefaultMessage("Config<br>Subpart")
	String colExamOwnerConfigSubpart();
	
	@DefaultMessage("Class<br>Number")
	String colExamOwnerClassNumber();
	
	@DefaultMessage("N/A")
	String examOwnerNotApplicable();
	
	@DefaultMessage("Object")
	String colExamOwnerObject();
	
	@DefaultMessage("Type")
	String colExamOwnerType();
	
	@DefaultMessage("Title")
	String colExamOwnerTitle();
	
	@DefaultMessage("Manager")
	String colExamOwnerManager();
	
	@DefaultMessage("Students")
	String colExamOwnerStudents();
	
	@DefaultMessage("Limit")
	String colExamOwnerLimit();
	
	@DefaultMessage("Assignment")
	String colExamOwnerAssignment();
	
	@DefaultMessage("Class")
	String examTypeClass();
	
	@DefaultMessage("Configuration")
	String examTypeConfig();
	
	@DefaultMessage("Offering")
	String examTypeOffering();
	
	@DefaultMessage("Course")
	String examTypeCourse();
	
	@DefaultMessage("-- Configurations --")
	String sctOwnerTypeConfigurations();
	
	@DefaultMessage("-- Subparts --")
	String sctOwnerTypeSubparts();

	@DefaultMessage("No examination matching the above criteria was found.")
	String errorNoMatchingExam();
	
	@DefaultMessage("Maximal Number of Rooms cannot be negative.")
	String errorNegativeMaxNbrRooms();
	
	@DefaultMessage("Length must be above zero.")
	String errorZeroExamLength();
	
	@DefaultMessage("Size must be a number.")
	String errorExamSizeNotNumber();
	
	@DefaultMessage("Print Offset must be a number")
	String errorExamPrintOffsetNotNumber();
	
	@DefaultMessage("One instructor is listed two or more times.")
	String errorDuplicateExamInstructors();
	
	@DefaultMessage("At least one class/course has to be specified.")
	String errorNoExamOwners();
	
	@DefaultMessage("Add Examination")
	String buttonAddExamination();
	
	@DefaultMessage("Export PDF")
	String buttonExportPDF();
	
	@DefaultMessage("Export CSV")
	String buttonExportCSV();
	
	@DefaultMessage("Search")
	String buttonSearch();
	
	@DefaultMessage("Edit")
	String actionExamEdit();
	
	@DefaultMessage("Edit Examination (Alt + E)")
	String titleExamEdit();
	
	@DefaultMessage("E")
	String accessExamEdit();
	
	@DefaultMessage("Clone")
	String actionExamClone();
	
	@DefaultMessage("Clone Examination (Alt + C)")
	String titleExamClone();
	
	@DefaultMessage("C")
	String accessExamClone();
	
	@DefaultMessage("Add Distribution Preference")
	String actionExamAddDistributionPref();
	
	@DefaultMessage("Add Distribution Preference (Alt + A)")
	String titleExamAddDistributionPref();
	
	@DefaultMessage("A")
	String accessExamAddDistributionPref();
	
	@DefaultMessage("Assign")
	String actionExamAssign();
	
	@DefaultMessage("Open Examination Assignment Dialog (Alt + X)")
	String titleExamAssign();
	
	@DefaultMessage("X")
	String accessExamAssign();
	
	@DefaultMessage("Previous")
	String actionExamPrevious();
	
	@DefaultMessage("Previous Examination (Alt + P)")
	String titleExamPrevious();
	
	@DefaultMessage("P")
	String accessExamPrevious();
	
	@DefaultMessage("Next")
	String actionExamNext();
	
	@DefaultMessage("Next Examination (Alt + N)")
	String titleExamNext();
	
	@DefaultMessage("N")
	String accessExamNext();
	
	@DefaultMessage("Back")
	String actionExamBack();
	
	@DefaultMessage("Return to %% (Alt + B)")
	String titleExamBack();
	
	@DefaultMessage("B")
	String accessExamBack();
	
	@DefaultMessage("Delete")
	String actionExamDelete();
	
	@DefaultMessage("Delete Examination (Alt + D)")
	String titleExamDelete();
	
	@DefaultMessage("D")
	String accessExamDelete();
	
	@DefaultMessage("Update")
	String actionExamUpdate();
	
	@DefaultMessage("Update Examination (Alt + U)")
	String titleExamUpdate();
	
	@DefaultMessage("U")
	String accessExamUpdate();
	
	@DefaultMessage("Save")
	String actionExamSave();
	
	@DefaultMessage("Save Examination (Alt + S)")
	String titleExamSave();
	
	@DefaultMessage("S")
	String accessExamSave();
	
	@DefaultMessage("Back")
	String actionBatckToDetail();
	
	@DefaultMessage("Do not make any change. Return to Detail Screen (ALT+B)")
	String titleBatckToDetail();
	
	@DefaultMessage("B")
	String accessBatckToDetail();
	
	@DefaultMessage("Course numbers can be specified using wildcard (*). E.g. 2*")
	String titleCourseNumberSuggestBox();
	
	@DefaultMessage("{0} Exams ({1})")
	String backExams(String type, String subjectOrCourse);
	
	@DefaultMessage("{0} ({1})")
	String backExaminationReports(String report, String subject);
	
	@DefaultMessage("Exam ({0})")
	String backExam(String name);
	
	@DefaultMessage("Clear Exam Preferences")
	String actionClearExamPreferences();
	
	@DefaultMessage("C")
	String accessClearExamPreferences();
	
	@DefaultMessage("Delete all Examination Preferences (Alt+C)")
	String titleClearExamPreferences();
	
	@DefaultMessage("Add Instructor")
	String actionAddInstructor();
	
	@DefaultMessage("I")
	String accessAddInstructor();
	
	@DefaultMessage("Add Instructor (Alt+I)")
	String titleAddInstructor();
	
	@DefaultMessage("Add Object")
	String actionAddObject();
	
	@DefaultMessage("O")
	String accessAddObject();
	
	@DefaultMessage("Add Class/Config/Course/Offering (Alt+O)")
	String titleAddObject();
	
	@DefaultMessage("Delete")
	String actionDeleteInstructor();
	
	@DefaultMessage("Delete Instructor")
	String titleDeleteInstructor();
	
	@DefaultMessage("Delete")
	String actionDeleteObject();
	
	@DefaultMessage("Delete Class/Config/Course/Offering")
	String titleDeleteObject();
	
	@DefaultMessage("Type:")
	String propExamType();
	
	@DefaultMessage("Subject:")
	String propExamSubject();
	
	@DefaultMessage("Course Number:")
	String propExamCourseNumber();
	
	@DefaultMessage("Name:")
	String propExamName();
	
	@DefaultMessage("Length:")
	String propExamLength();
	
	@DefaultMessage("Seating Type:")
	String propExamSeatingType();
	
	@DefaultMessage("Maximum Number of Rooms:")
	String propExamMaxRooms();
	
	@DefaultMessage("Size:")
	String propExamSize();
	
	@DefaultMessage("Print Offset:")
	String propExamPrintOffset();
	
	@DefaultMessage("Examination Period:")
	String propExamAssignedPeriod();
	
	@DefaultMessage("Room:")
	String propExamAssignedRoom();
	
	@DefaultMessage("Rooms:")
	String propExamAssignedRooms();
	
	@DefaultMessage("Violated Distributions:")
	String propExamViolatedDistConstraints();
	
	@DefaultMessage("Student Conflicts:")
	String propExamStudentConflicts();
	
	@DefaultMessage("Instructor Conflicts:")
	String propExamInstructorConflicts();
	
	@DefaultMessage("minutes")
	String offsetUnitMinutes();
	
	@DefaultMessage("Instructors:")
	String propExamInstructors();
	
	@DefaultMessage("Average Period:")
	String propExamAvgPeriod();
	
	@DefaultMessage("Notes")
	String sectExamNotes();
	
	@DefaultMessage("Student Accommodations:")
	String propExamStudentAccommodations();
	
	@DefaultMessage("Classes / Courses")
	String sectExamOwners();
	
	@DefaultMessage("No relation defined for this exam.")
	String warnNoExamOwners();
	
	@DefaultMessage("Assignment")
	String sectExamAssignment();
	
	@DefaultMessage("Preferences")
	String sectExamPreferences();
	
	@DefaultMessage("Examination id not provided.")
	String errorNoExamId();
	
	@DefaultMessage("Exam ({0})")
	String backToExam(String name);
	
	@DefaultMessage("Examination Assignment")
	String dialogExamAssign();
	
	@DefaultMessage("(in minutes, only used for reports)")
	String noteExamPrintOffset();
	
	@DefaultMessage("The examination will be deleted. Continue?")
	String confirmExamDelete();
	
	@DefaultMessage("There are the following errors:")
	String formValidationErrors();
	
	@DefaultMessage("Select a distribution type.")
	String errorSelectDistributionType();
	
	@DefaultMessage("Select a preference level.")
	String errorSelectPreferenceLevel();
	
	@DefaultMessage("Cancel")
	String actionCancel();
	
	@DefaultMessage("Update")
	String actionUpdateDistributionPreference();
	
	@DefaultMessage("Save")
	String actionSaveNewDistributionPreference();

	@DefaultMessage("Delete")
	String actionDeleteDistributionPreference();
	
	@DefaultMessage("Back")
	String actionBackDistributionPreference();
	
	@DefaultMessage("Add Distribution Preference")
	String actionAddDistributionPreference();
	
	@DefaultMessage("Add Examination")
	String actionAddExamToDistribution();

	@DefaultMessage("Search")
	String actionSearchDistributionPreferences();

	@DefaultMessage("Export PDF")
	String actionExportPdf();

	@DefaultMessage("Export CSV")
	String actionExportCsv();
	
	@DefaultMessage("Delete")
	String actionDelete();
	
	@DefaultMessage("Update Distribution Preference (Alt+{0})")
	String titleUpdateDistributionPreference(String accessKey);

	@DefaultMessage("Save New Distribution Preference (Alt+{0})")
	String titleSaveNewDistributionPreference(String accessKey);
	
	@DefaultMessage("Delete Distribution Preference (Alt+{0})")
	String titleDeleteDistributionPreference(String accessKey);
	
	@DefaultMessage("Return to %% (Alt+{0})")
	String titleBackDistributionPreference(String accessKey);	
	
	@DefaultMessage("Add Examination (Alt+{0})")
	String titleAddExamToDistribution(String accessKey);
	
	@DefaultMessage("Move Up")
	String titleMoveUp();
	
	@DefaultMessage("Move Down")
	String titleMoveDown();
	
	@DefaultMessage("Search Distribution Preferences (Alt+{0})")
	String titleSearchDistributionPreferences(String accessKey);
	
	@DefaultMessage("Export PDF (Alt+{0})")
	String titleExportPdf(String accessKey);

	@DefaultMessage("Export CSV (Alt+{0})")
	String titleExportCsv(String accessKey);
	
	@DefaultMessage("Add Distribution Preference (Alt+{0})")
	String titleAddDistributionPreference(String accessKey);
	
	@DefaultMessage("U")
	String accessUpdateDistributionPreference();

	@DefaultMessage("S")
	String accessSaveNewDistributionPreference();
	
	@DefaultMessage("D")
	String accessDeleteDistributionPreference();
	
	@DefaultMessage("B")
	String accessBackDistributionPreference();
	
	@DefaultMessage("A")
	String accessAddExamToDistribution();
	
	@DefaultMessage("S")
	String accessSearchDistributionPreferences();
	
	@DefaultMessage("P")
	String accessExportPdf();
	
	@DefaultMessage("C")
	String accessExportCsv();
	
	@DefaultMessage("A")
	String accessAddDistributionPreference();
	
	@DefaultMessage("Do you really want to delete this distribution preference?")
	String confirmDeleteDistributionPreference();
	
	@DefaultMessage("Add Examination Distribution Preference")
	String sectionAddDistributionPreference();
	
	@DefaultMessage("Edit Examination  Distribution Preference")
	String sectionEditDistributionPreference();
	
	@DefaultMessage("{0} Examinations in Distribution")
	String sectionExaminationsInDistribution(String examType);
	
	@DefaultMessage("{0} Examination Distribution Preferences")
	String sectionDistributionPreferences(String examType);

	@DefaultMessage("Distribution Type:")
	String propertyDistributionType();
	
	@DefaultMessage("Preference:")
	String propertyDistributionPreference();
	
	@DefaultMessage("Course numbers can be specified using wildcard (*). E.g. 2*")
	String tooltipCourseNumber();
	
	@DefaultMessage("Show classes/courses:")
	String filterShowClassesCourses();
	
	@DefaultMessage("Examination Problem:")
	String filterExaminationProblem();
	
	@DefaultMessage("Report:")
	String filterReport();
	
	@DefaultMessage("Filter:")
	String filterTextFilter();
	
	@DefaultMessage("Hint: use comma for conjunctions, semicolon for disjunctions, e.g., 'a,b;c means (a and b) or c'.")
	String titleTextFilter();
	
	@DefaultMessage("Subject Areas:")
	String filterSubjectAreas();
	
	@DefaultMessage("Apply")
	String buttonApply();
	
	@DefaultMessage("A")
	String accessApply();
	
	@DefaultMessage("Apply (Alt + A)")
	String titleApply();
	
	@DefaultMessage("Refresh")
	String buttonRefresh();
	
	@DefaultMessage("R")
	String accessRefresh();
	
	@DefaultMessage("Refresh (Alt + R)")
	String titleRefresh();
	
	@DefaultMessage("No exams matching the above criteria found.")
	String errorNoExamsFound();
	
	@DefaultMessage("Class / Course")
	String colOwner();
	
	@DefaultMessage("Examination")
	String colExamination();
	
	@DefaultMessage("Enrollment")
	String colEnrollment();
	
	@DefaultMessage("Seating\nType")
	String colSeatingType();
	
	@DefaultMessage("Date")
	String colDate();
	
	@DefaultMessage("Time")
	String colTime();
	
	@DefaultMessage("Room")
	String colRoom();
	
	@DefaultMessage("Name")
	String colStudentOrInstructorName();
	
	@DefaultMessage("Name")
	String colName();
	
	@DefaultMessage("Student Id")
	String colStudentId();
	
	@DefaultMessage("Instructor Id")
	String colInstructorId();
	
	@DefaultMessage("Instructor")
	String colInstructor();
	
	@DefaultMessage("Capacity")
	String colRoomCapacity();
	
	@DefaultMessage("Exam Capacity")
	String colExamCapacity();
	
	@DefaultMessage("Instructor\nConflicts")
	String colInstructorConflicts();
	
	@DefaultMessage("Student\nConflicts")
	String colStudentConflicts();
	
	@DefaultMessage("Classes / Courses")
	String colOwners();
	
	@DefaultMessage("Examinations")
	String colExaminations();
	
	@DefaultMessage("Total Enrollment")
	String colTotalEnrollment();
	
	@DefaultMessage("with {0}+ students")
	String withNOrMoreStudents(int n);
	
	@DefaultMessage("Totals")
	String colTotals();
	
	@DefaultMessage("Average\nDistance")
	String colAverageDistance();
	
	@DefaultMessage("1st")
	String col1stExam();
	
	@DefaultMessage("2nd")
	String col2ndExam();
	
	@DefaultMessage("3rd")
	String col3rdExam();
	
	@DefaultMessage("{0}th")
	String colNthExam(int n);
	
	@DefaultMessage("Preference")
	String colPreference();
	
	@DefaultMessage("Distribution")
	String colDistribution();
	
	@DefaultMessage("Students with\nNo Exam")
	String colStudentsWithNoExam();
	
	@DefaultMessage("Students with\nOne Exam")
	String colStudentsWithOneExam();
	
	@DefaultMessage("Students with\nTwo Exams")
	String colStudentsWithTwoExams();
	
	@DefaultMessage("Students with\nThree Exams")
	String colStudentsWithThreeExams();
	
	@DefaultMessage("Students with\nFour or More Exams")
	String colStudentsWithFourOrMoreExams();
	
	@DefaultMessage("Student\nBack-To-Back Exams")
	String colStudentBTBExams();
	
	@DefaultMessage("Student Distance\nBack-To-Back Exams")
	String colStudentDistanceBTBExams();
	
	@DefaultMessage("Type")
	String colType();
	
	@DefaultMessage("Distance")
	String colDistance();
	
	@DefaultMessage("Class")
	String typeClass();
	
	@DefaultMessage("Event")
	String typeEvent();
	
	@DefaultMessage("Direct")
	String conflictDirect();
	
	@DefaultMessage("Direct [%]")
	String colDirectPercent();
	
	@DefaultMessage("Back-To-Back")
	String conflictBackToBack();
	
	@DefaultMessage("Back-To-Back [%]")
	String colBackToBackPercent();
	
	@DefaultMessage("Distance [m]")
	String colDistanceMeters();
	
	@DefaultMessage(">2 A Day")
	String conflictMoreThanTwoADay();
	
	@DefaultMessage(">2 A Day [%]")
	String colMoreThanTwoADayPercent();
	
	@DefaultMessage("Value")
	String colValue();
	
	@DefaultMessage("Number of exams")
	String propNumberOfExams();
	
	@DefaultMessage("Classes")
	String typeClasses();
	
	@DefaultMessage("Configs")
	String typeConfigs();
	
	@DefaultMessage("Courses")
	String typeCourses();
	
	@DefaultMessage("Offerings")
	String typeOfferings();
	
	@DefaultMessage("{0} with an exam")
	String propOwnersWithAnExam(String type);
	
	@DefaultMessage("Students enrolled in classes")
	String propStudentsEnrolledInClasses();
	
	@DefaultMessage("Students having an exam")
	String propStudentsHavingAnExam();
	
	@DefaultMessage("Student exam enrollments")
	String propStudentExamEnrollments();
	
	@DefaultMessage("Registered instructors")
	String propRegisteredInstructors();
	
	@DefaultMessage("Instructors having an exam")
	String propInstructorsHavingAnExam();
	
	@DefaultMessage("Instructor exam enrollments")
	String propInstructorExamEnrollments();
	
	@DefaultMessage("Direct student conflicts")
	String propDirectStudentConflicts();
	
	@DefaultMessage("Conflict with other exam")
	String propConflictWithOtherExam();
	
	@DefaultMessage("Student not available")
	String propStudentNotAvailable();
	
	@DefaultMessage("More than 2 exams a day student conflicts")
	String propStudentMoreThanTwoExamsADayConflicts();
	
	@DefaultMessage("Back-to-back student conflicts")
	String propStudentBackToBackConflicts();
	
	@DefaultMessage("Distance back-to-back student conflicts")
	String propStudentDistanceBackToBackConflicts();
	
	@DefaultMessage("Direct instructor conflicts")
	String propDirectInstructorConflicts();
	
	@DefaultMessage("Instructor not available")
	String propInstructorNotAvailable();
	
	@DefaultMessage("More than 2 exams a day instructor conflicts")
	String propInstructorMoreThanTwoExamsADayConflicts();
	
	@DefaultMessage("Back-to-back instructor conflicts")
	String propInstructorBackToBackConflicts();
	
	@DefaultMessage("Distance back-to-back instructor conflicts")
	String propInstructorDistanceBackToBackConflicts();

	@DefaultMessage("Exam Assignment Report")
	String reportExamAssignmentReport();

	@DefaultMessage("Room Assignment Report")
	String reportRoomAssignmentReport();

	@DefaultMessage("Statistics")
	String reportStatistics();

	@DefaultMessage("Period Usage")
	String reportPeriodUsage();

	@DefaultMessage("Number of Exams A Day")
	String reportNrExamsADay();

	@DefaultMessage("Room Splits")
	String reportRoomSplits();

	@DefaultMessage("Violated Distribution Constraints")
	String reportViolatedDistributions();

	@DefaultMessage("Direct Student Conflicts")
	String reportDirectStudentConflicts();

	@DefaultMessage("More Than 2 Exams A Day Student Conflicts")
	String reportMore2ADayStudentConflicts();

	@DefaultMessage("Back-To-Back Student Conflicts")
	String reportBackToBackStudentConflicts();

	@DefaultMessage("Individual Student Conflicts")
	String reportIndividualStudentConflicts();

	@DefaultMessage("Individual Direct Student Conflicts")
	String reportIndividualDirectStudentConflicts();

	@DefaultMessage("Individual Back-To-Back Student Conflicts")
	String reportIndividualBackToBackStudentConflicts();

	@DefaultMessage("Individual More Than 2 Exams A Day Student Conflicts")
	String reportIndividualMore2ADayStudentConflicts();

	@DefaultMessage("Direct Instructor Conflicts")
	String reportDirectInstructorConflicts();

	@DefaultMessage("More Than 2 Exams A Day Instructor Conflicts")
	String reportMore2ADayInstructorConflicts();

	@DefaultMessage("Back-To-Back Instructor Conflicts")
	String reportBackToBackInstructorConflicts();

	@DefaultMessage("Individual Instructor Conflicts")
	String reportIndividualInstructorConflicts();

	@DefaultMessage("Individual Direct Instructor Conflicts")
	String reportIndividualDirectInstructorConflicts();

	@DefaultMessage("Individual Back-To-Back Instructor Conflicts")
	String reportIndividualBackToBackInstructorConflicts();

	@DefaultMessage("Individual More Than 2 Exams A Day Instructor Conflicts")
	String reportIndividualMore2ADayInstructorConflicts();

	@DefaultMessage("Individual Student Schedule")
	String reportIndividualStudentSchedule();

	@DefaultMessage("Individual Instructor Schedule")
	String reportIndividualInstructorSchedule();
	
	@DefaultMessage("Best")
	String changeBest();
	
	@DefaultMessage("Initial")
	String changeInitial();
	
	@DefaultMessage("Saved")
	String changeSaved();
	
	@DefaultMessage("Examination Assignment Changes")
	String sectExaminationAssingmentChanges();
	
	@DefaultMessage("Period")
	String colPeriod();
	
	@DefaultMessage("Students")
	String colStudents();
	
	@DefaultMessage("not-assigned")
	String notAssigned();
	
	@DefaultMessage("ERROR: {0}")
	String error(String message);
	
	@DefaultMessage("d:")
	String prefixDistanceConclict();
	
	@DefaultMessage("Exam Changes")
	String backExaminationAssignmentChanges();
	
	@DefaultMessage("Reversed mode:")
	String filterReverseMode();
	
	@DefaultMessage("(current &rarr; compared solution)")
	String hintReversedMode();
	
	@DefaultMessage("Compare with:")
	String filterCompareWith();
	
	@DefaultMessage("No examination timetable is loaded into the solver.")
	String messageNoSolver();
	
	@DefaultMessage("No subject area selected.")
	String messageNoSubject();
	
	@DefaultMessage("Solutions are identical.")
	String messageNoChanges();
	
	@DefaultMessage("There are no assignment changes between exams of {0} subject area.")
	String messageNoChangesInSubject(String subject);
}