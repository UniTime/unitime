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
	
	@DefaultMessage("Sort classes only within scheduling subparts")
	String checkSortWithinSubparts();
	
	@DefaultMessage("All")
	String dropDeptAll();
	
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
	
	@DefaultMessage("Offered Coursses")
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
	@StrutsAction("searchInstructionalOfferings")
	String actionSearchInstructionalOfferings();
	
	@DefaultMessage("Search")
	@StrutsAction("searchClasses")
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
	
	@DefaultMessage("am")
	String timeAm();
	
	@DefaultMessage("pm")
	String timePm();
}