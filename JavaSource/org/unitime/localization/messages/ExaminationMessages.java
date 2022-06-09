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

	@DefaultMessage("No examination matching the above criteria was found.")
	String errorNoMatchingExam();
	
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
	
	@DefaultMessage("Course numbers can be specified using wildcard (*). E.g. 2*")
	String titleCourseNumberSuggestBox();
	
	@DefaultMessage("{0} Exams ({1})")
	String backExams(String type, String subjectOrCourse);
	
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
}