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

import com.google.gwt.i18n.client.Messages.DefaultMessage;

/**
 * @author Tomas Muller
 */
public interface ExaminationMessages extends Messages {
	
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
}