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
package org.unitime.localization.messages;

import org.unitime.timetable.action.ClassSearchAction;
import org.unitime.timetable.action.InstructionalOfferingSearchAction;

/**
 * @author Tomas Muller
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
	
	@DefaultMessage("Parent Class:")
	String propertyParentClass();
	
	@DefaultMessage("External Id:")
	String propertyExternalId();
	
	@DefaultMessage("Enrollment:")
	String propertyEnrollment();
	
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
	
	@DefaultMessage("Display Instructors:")
	String propertyDisplayInstructors();
	
	@DefaultMessage("Display In Schedule Book:")
	String propertyDisplayInScheduleBook();
	
	@DefaultMessage("Student Schedule Note:")
	String propertyStudentScheduleNote();
	
	@DefaultMessage("Requests / Notes:")
	String propertyRequestsNotes();
	
	@DefaultMessage("Instructors:")
	String propertyInstructors();
	
	@DefaultMessage("Time:")
	String propertyTime();
	
	@DefaultMessage("Examination Periods:")
	String propertyExaminationPeriods();
	
	@DefaultMessage("Room Groups:")
	String propertyRoomGroups();
	
	@DefaultMessage("Rooms:")
	String propertyRooms();
	
	@DefaultMessage("Buildings:")
	String propertyBuildings();
	
	@DefaultMessage("Room Features:")
	String propertyRoomFeatures();
	
	@DefaultMessage("Available Rooms:")
	String propertyAvailableRooms();
	
	@DefaultMessage("Distribution:")
	String propertyDistribution();
	
//	@DefaultMessage("Parent Class:")
//	String propertyParentClass();
	
	@DefaultMessage("Sort classes only within scheduling subparts")
	String checkSortWithinSubparts();
	
	@DefaultMessage("All")
	String dropDeptAll();
	
	@DefaultMessage("All")
	String dropManagerAll();
	
	@DefaultMessage("Department")
	String dropDeptDepartment();
	
	@DefaultMessage("All")
	String dropITypeAll();
	
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

	@DefaultMessage("Manager")
	String columnManager();

	@DefaultMessage("Date/Time Information")
	String columnDateTimeInformation();

	@DefaultMessage("Date Pattern")
	String columnDatePattern();
	
	@DefaultMessage("Mins Per Week")
	String columnMinPerWk();
	
	@DefaultMessage("Time Pattern")
	String columnTimePattern();
	
	@DefaultMessage("Preferences")
	String columnPreferences();
	
	@DefaultMessage("Instructor")
	String columnInstructor();
	
	@DefaultMessage("Timetable")
	String columnTimetable();
	
	@DefaultMessage("Catalog Information")
	String columnCatalogInformation();
	
	@DefaultMessage("Title")
	String columnTitle();

	@DefaultMessage("Offering Credit")
	String columnOfferingCredit();

	@DefaultMessage("Subpart Credit")
	String columnSubpartCredit();

	@DefaultMessage("Consent")
	String columnConsent();

	@DefaultMessage("Designator Required")
	String columnDesignatorRequired();

	@DefaultMessage("Schedule of Classes Note")
	String columnSchedulePrintNote();

	@DefaultMessage("Note to Schedule Manager")
	String columnNote();
	
	@DefaultMessage("Examinations")
	String columnExams();

	@DefaultMessage("Examination")
	String columnExam();

	@DefaultMessage("{0} {1} Examination")
	String tooltipExam(String label, String type);

	@DefaultMessage("Name")
	String columnExamName();

	@DefaultMessage("Period")
	String columnExamPeriod();

	@DefaultMessage("Room")
	String columnExamRoom();
	
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
	
	@DefaultMessage("Room")
	String columnAllRoomPref();
	
	@DefaultMessage("Time")
	String columnAssignedTime();
	
	@DefaultMessage("Room")
	String columnAssignedRoom();
	
	@DefaultMessage("Room Cap")
	String columnAssignedRoomCapacity();
	
	@DefaultMessage("Name")
	String columnInstructorName();

	@DefaultMessage("% Share")
	String columnInstructorShare();
	
	@DefaultMessage("Chceck Conflicts")
	String columnInstructorCheckConflicts();

	@DefaultMessage("{0} - Do Not Display In Schedule Book.")
	String tooltipDoNotDisplayInScheduleBook(String classLabelWithTitle);
	
	@DefaultMessage("Has Schedule Print Note")
	String altHasSchedulePrintNote();
	
	@DefaultMessage("Has Course Offering Note")
	String altHasCourseOfferingNote();
	
	@DefaultMessage("Has Note to Mgr")
	String altHasNoteToMgr();
	
	@DefaultMessage("Configuration {0}")
	String labelConfiguration(String name);
	
	@DefaultMessage("Not Offered Courses")
	String labelNotOfferedCourses();
	
	@DefaultMessage("Offered Courses")
	String labelOfferedCourses();
	
	@DefaultMessage("There are no courses currently offered for this subject.")
	String errorNoCoursesOffered();

	@DefaultMessage("All courses are currently being offered for this subject.")
	String errorAllCoursesOffered();
	
	@DefaultMessage("Sort By:")
	String filterSortBy();
	
	@DefaultMessage("Subject:")
	String filterSubject();

	@DefaultMessage("Course Number:")
	String filterCourseNumber();

	@DefaultMessage("Search")
	@StrutsAction(
		value = "searchInstructionalOfferings",
		apply = InstructionalOfferingSearchAction.class
	)
	String actionSearchInstructionalOfferings();
	
	@DefaultMessage("Search")
	@StrutsAction(
		value = "searchClasses",
		apply = ClassSearchAction.class
	)
	String actionSearchClasses();

	@DefaultMessage("Export PDF")
	@StrutsAction("exportPdf")
	String actionExportPdf();

	@DefaultMessage("Worksheet PDF")
	@StrutsAction("worksheetPdf")
	String actionWorksheetPdf();

	@DefaultMessage("Add New")
	@StrutsAction("addInstructionalOfferings")
	String actionAddNewInstructionalOffering();
	
	@DefaultMessage("Edit Class")
	String actionEditClass();
	
	@DefaultMessage("Add Distribution Preference")
	String actionAddDistributionPreference();
	
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
	
	@DefaultMessage("Next")
	String actionNextClass();

	@DefaultMessage("Next")
	String actionNextSubpart();
	
	@DefaultMessage("Next")
	String actionNextInstructor();
	
	@DefaultMessage("Next")
	String actionNextExamination();
	
	@DefaultMessage("Back")
	String actionBackClassDetail();
	
	@DefaultMessage("Back")
	String actionBackToDetail();
	
	@DefaultMessage("Update")
	String actionUpdatePreferences();
	
	@DefaultMessage("Update")
	String actionUpdateExamination();
	
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
	
	@DefaultMessage("Add Room Preference")
	String actionAddRoomPreference();
	
	@DefaultMessage("Delete")
	String actionRemoveRoomPreference();
	
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
	
	@DefaultMessage("Save")
	String actionSaveExamination();
	
	@DefaultMessage("Add Instructor")
	String actionAddInstructor();
	
	@DefaultMessage("Delete")
	String actionRemoveInstructor();
	
	@DefaultMessage("S")
	String accessSearchInstructionalOfferings();
	
	@DefaultMessage("S")
	String accessSearchClasses();

	@DefaultMessage("P")
	String accessExportPdf();
	
	@DefaultMessage("W")
	String accessWorksheetPdf();

	@DefaultMessage("A")
	String accessAddNewInstructionalOffering();

	@DefaultMessage("E")
	String accessEditClass();
	
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

	@DefaultMessage("N")
	String accessNextClass();
	
	@DefaultMessage("N")
	String accessNextSubpart();
	
	@DefaultMessage("N")
	String accessNextInstructor();
	
	@DefaultMessage("N")
	String accessNextExamination();
	
	@DefaultMessage("B")
	String accessBackClassDetail();
	
	@DefaultMessage("B")
	String accessBackToDetail();
	
	@DefaultMessage("I")
	String accessInstructionalOfferingDetail();
	
	@DefaultMessage("S")
	String accessSchedulingSubpartDetail();
	
	@DefaultMessage("U")
	String accessUpdatePreferences();
	
	@DefaultMessage("U")
	String accessUpdateExamination();
	
	@DefaultMessage("C")
	String accessClearClassPreferences();

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
	
	@DefaultMessage("I")
	String accessAddInstructor();
	
	@DefaultMessage("Search/Display Offerings (Alt+{0})")
	String titleSearchInstructionalOfferings(String accessKey);

	@DefaultMessage("Search/Display Classes (Alt+{0})")
	String titleSearchClasses(String accessKey);
	
	@DefaultMessage("Export PDF (Alt+{0})")
	String titleExportPdf(String accessKey);

	@DefaultMessage("Export Worksheet PDF (Alt+{0})")
	String titleWorksheetPdf(String accessKey);

	@DefaultMessage("Add New Offering (Alt+{0})")
	String titleAddNewInstructionalOffering(String accessKey);

	@DefaultMessage("Edit Class (Alt+{0})")
	String titleEditClass(String accessKey);
	
	@DefaultMessage("Add Distribution Preference (Alt+{0})")
	String titleAddDistributionPreference(String accessKey);
	
	@DefaultMessage("Open Class Assignment Dialog (Alt+{0})")
	String titleOpenClassAssignmentDialog(String accessKey);
		
	@DefaultMessage("Previous Class (ALT+{0})")
	String titlePreviousClass(String accessKey);
	
	@DefaultMessage("Update and go to previous Class (Alt+{0})")
	String titlePreviousClassWithUpdate(String accessKey);
	
	@DefaultMessage("Update and go to previous Scheduling Subpart (Alt+{0})")
	String titlePreviousSubpartWithUpdate(String accessKey);
	
	@DefaultMessage("Update and go to previous Instructor (Alt+{0})")
	String titlePreviousInstructorWithUpdate(String accessKey);
	
	@DefaultMessage("Update and go to previous Examination (Alt+{0})")
	String titlePreviousExaminationWithUpdate(String accessKey);

	@DefaultMessage("Next Class (ALT+{0})")
	String titleNextClass(String accessKey);
	
	@DefaultMessage("Update and go to next Class (Alt+{0})")
	String titleNextClassWithUpdate(String accessKey);
	
	@DefaultMessage("Update and go to next Scheduling Subpart (Alt+{0})")
	String titleNextSubpartWithUpdate(String accessKey);
	
	@DefaultMessage("Update and go to next Instructor (Alt+{0})")
	String titleNextInstructorWithUpdate(String accessKey);
	
	@DefaultMessage("Update and go to next Examination (Alt+{0})")
	String titleNextExaminationWithUpdate(String accessKey);
	
	@DefaultMessage("Update Examination (Alt+{0})")
	String titleUpdateExamination(String accessKey);
	
	@DefaultMessage("Return to %% (Alt+{0})")
	String titleBackClassDetail(String accessKey);
	
	@DefaultMessage("Do not commit any change. Return to Detail Screen (Alt+{0})")
	String titleBackToDetail(String accessKey);
	
	@DefaultMessage("Instructional Offering Detail (Alt+{0})")
	String titleInstructionalOfferingDetail(String accessKey);
	
	@DefaultMessage("Scheduling Subpart Detail (Alt+{0})")
	String titleSchedulingSubpartDetail(String accessKey);
	
	@DefaultMessage("Commit changes to database (Alt+{0})")
	String titleUpdatePreferences(String accessKey);
	
	@DefaultMessage("Delete all Class Preferences. Preferences will be inherited from the subpart (Alt+{0})")
	String titleClearClassPreferences(String accessKey);
	
	@DefaultMessage("Delete all Subpart Preferences (Alt+{0})")
	String titleClearSubpartPreferences(String accessKey);
	
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
	
	@DefaultMessage("Save Examination (Alt+{0})")
	String titleSaveExamination(String accessKey);
	
	@DefaultMessage("Add Instructor (Alt+{0})")
	String titleAddInstructor(String accessKey);
	
	@DefaultMessage("Remove Instructor")
	String titleRemoveInstructor();
		
	@DefaultMessage("Course numbers can be specified using wildcard (*). E.g. 2*")
	String tooltipCourseNumber();
	
	@DefaultMessage("Subject Area")
	String labelSubjectArea();
	
	@DefaultMessage("No records matching the search criteria were found.")
	String errorNoRecords();
	
	@DefaultMessage("Instructional Offerings")
	String labelInstructionalOfferings();
	
	@DefaultMessage("Unable to create PDF file.")
	String errorUnableToCreatePdf();
	
	@DefaultMessage("Unable to create worksheet PDF file: nothing to export.")
	String errorUnableToCreateWorksheetPdfNoData();

	@DefaultMessage("Unable to create worksheet PDF file: {0}.")
	String errorUnableToCreateWorksheetPdf(String reason);
	
	@DefaultMessage("Course Number cannot be matched to regular expression: {0}. Reason: {1}")
	String errorCourseDoesNotMatchRegEx(String regEx, String reason);
	
	@DefaultMessage("Access Denied.")
	String errorAccessDenied();

	@DefaultMessage("ERRORS")
	String errorsClassDetail();
	
	@DefaultMessage("ERRORS")
	String errorsClassEdit();
	
	@DefaultMessage("Invalid room group: Check for duplicate / blank selection. ")
	String errorInvalidRoomGroup();
	
	@DefaultMessage("Invalid building preference: Check for duplicate / blank selection. ")
	String errorInvalidBuildingPreference();
	
	@DefaultMessage("Invalid distribution preference: Check for duplicate / blank selection. ")
	String errorInvalidDistributionPreference();
	
	@DefaultMessage("Invalid room feature preference: Check for duplicate / blank selection. ")
	String errorInvalidRoomFeaturePreference();

	@DefaultMessage("Invalid instructor preference: Check for duplicate / blank selection. ")
	String errorInvalidInstructorPreference();
	
	@DefaultMessage("Time pattern not selected. ")
	String errorTimePatternNotSelected();
	
	@DefaultMessage("Invalid room preference: Check for duplicate / blank selection. ")
	String errorInvalidRoomPreference();
	
	@DefaultMessage("Null Operation not supported.")
	String errorNullOperationNotSupported();
	
	@DefaultMessage("Class Info not supplied.")
	String errorClassInfoNotSupplied();
	
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
	
	@DefaultMessage("Class {0}")
	String backClass(String className);

	@DefaultMessage("am")
	String timeAm();
	
	@DefaultMessage("pm")
	String timePm();
	
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
	
	@DefaultMessage("Instructors")
	String sectionTitleInstructors();
	
	@DefaultMessage("Requests / Notes to Schedule Manager")
	String sectionTitleNotesToScheduleManager();
	
	@DefaultMessage("Instructor Displayed")
	String titleInstructorDisplayed();
	
	@DefaultMessage("Instructor Not Displayed")
	String titleInstructorNotDisplayed();	
	
	@DefaultMessage("Displayed in Schedule Book")
	String titleDisplayedInScheduleBook();

	@DefaultMessage("Not Displayed in Schedule Book")
	String titleNotDisplayedInScheduleBook();
	
	@DefaultMessage("Are you sure you want to set room size to a value different from expected capacity? Continue?")
	String confirmRoomSizeDifferentFromCapacity();
	
	@DefaultMessage("Do you want to apply instructor preferences to this class?")
	String confirmApplyInstructorPreferencesToClass();
	
	@DefaultMessage("Do you want to remove any instructor preferences \\nthat may have been applied to this class?")
	String confirmRemoveInstructorPreferencesFromClass();
	
	@DefaultMessage("Select an instructor")
	String alertSelectAnInstructor();
	
}