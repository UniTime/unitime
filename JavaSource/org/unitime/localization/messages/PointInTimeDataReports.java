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
 * @author Stephanie Schluttenhofer
 */
public interface PointInTimeDataReports extends Messages {
	
	@DefaultMessage("Weekly Student Class Hour Report For Department By Class")
	String deptWSCHReportAllHoursForDepartmentByClass();
	
	@DefaultMessage("This report lists organized, not organized and total class hours for each class that occur between the session start date and the session class end date for the selected department.<br>")
	String deptWSCBReportAllHoursForDepartmentByClassNote();

	@DefaultMessage("Weekly Student Class Hour Report For Department By Class and Instructor")
	String deptWSCHReportAllHoursForDepartmentByClassAndInstructor();
	
	@DefaultMessage("This report lists organized, not organized and total class hours for each class and instructor that occur between the session start date and the session class end date for the selected department.<br>")
	String deptWSCBReportAllHoursForDepartmentByClassAndInstructorNote();

	@DefaultMessage("Weekly Student Class Hour Report For Department By Instructor Position")
	String deptWSCHReportAllHoursForDepartmentByPosition();
	
	@DefaultMessage("This report lists organized, not organized and total class hours and student class hours for instructor position that occur between the session start date and the session class end date for the selected departments.<br>")
	String deptWSCBReportAllHoursForDepartmentByPositionNote();

	@DefaultMessage("Weekly Student Class Hour Report For Department By Instructor")
	String deptWSCHReportAllHoursForDepartmentByInstructor();
	
	@DefaultMessage("This report lists organized, not organized and total class hours and student class hours for instructor that occur between the session start date and the session class end date for the selected departments.<br>")
	String deptWSCHReportAllHoursForDepartmentByInstructorNote();

	@DefaultMessage("Departmental Weekly Student Class Hour Report")
	String deptWSCHReportAllHours();
	
	@DefaultMessage("This report lists organized, not organized and total class and student class hours for each department that occur between the session start date and the session class end date.<br><br><b>Note:</b> this report will take a very long time to run.<br>")
	String deptWSCHReportAllHoursNote();

	@DefaultMessage("Room Utilization")
	String roomUtilizationReport();
	
	@DefaultMessage("This report lists organized, not organized and total room and student class hours for each location that is used between the session start date and the session class end date. <br><b>Note:</b> this report may take a very long time to run when selecting all departments and room types.<br>")
	String roomUtilizationReportNote();

	@DefaultMessage("Room Type Utilization by Department")
	String roomTypeUtilizationByDepartmentReport();
	
	@DefaultMessage("This report lists organized, not organized and total room and student class hours for each room type controled by a department that is used between the session start date and the session class end date. <br><b>Note:</b> this report may take a very long time to run when selecting all departments and room types.<br>")
	String roomTypeUtilizationByDepartmentReportNote();

	@DefaultMessage("Weekly Student Enrollment by Day Of Week and Period")
	String wseByDayOfWeekAndPeriodReport();
	
	@DefaultMessage("This report lists organized, not organized and total student enrollments for each period collectively for all classes that meet the report criteria that fall between the session start date and the session class end date. <br><br><b>Note:</b> this report may take a very long time to run when selecting all departments and room types.<br>")
	String wseByDayOfWeekAndPeriodReportNote();

	@DefaultMessage("Organized Weekly Student Class Hours by Day Of Week and Hour of Day")
	String wseByDayOfWeekAndHourOfDayReport();
	
	@DefaultMessage("This report lists organized student class hours for each hour in the day collectively for all classes that fall between the session start date and the session class end date.<br><br><b>Note:</b> this report will take a very long time to run.<br>")
	String wseByDayOfWeekAndHourOfDayReportNote();

	@DefaultMessage("Organized Weekly Student Class Hours by Building by Day Of Week and Hour of Day")
	String wseByBuildingDayOfWeekAndHourOfDayReport();
	
	@DefaultMessage("This report lists organized student class hours by building for each hour in the day collectively for all classes that fall between the session start date and the session class end date.<br><br><b>Note:</b> this report will take a very long time to run.<br>")
	String wseByBuildingDayOfWeekAndHourOfDayReportNote();

	@DefaultMessage("Weekly Student Class Hours by Instructional Type by Day Of Week and Hour of Day")
	String wseByItypeDayOfWeekAndHourOfDayReport();
	
	@DefaultMessage("This report lists student class hours by instructional type for each hour in the day collectively for all classes that fall between the session start date and the session class end date.<br><br><b>Note:</b> this report will take a very long time to run.<br>")
	String wseByItypeDayOfWeekAndHourOfDayReportNote();
	
	@DefaultMessage("Organized Weekly Student Class Hours by Department by Day Of Week and Hour of Day")
	String wseByDeptDayOfWeekAndHourOfDayReport();
	
	@DefaultMessage("This report lists organized student class hours by department for each hour in the day collectively for all classes that fall between the session start date and the session class end date.<br><br><b>Note:</b> this report will take a very long time to run.<br>")
	String wseByDeptDayOfWeekAndHourOfDayReportNote();

	@DefaultMessage("Organized Weekly Student Class Hours by Subject Area by Day Of Week and Hour of Day")
	String wseBySubjectAreaDayOfWeekAndHourOfDayReport();
	
	@DefaultMessage("This report lists organized student class hours by subject area for each hour in the day collectively for all classes that fall between the session start date and the session class end date.<br><br><b>Note:</b> this report will take a very long time to run.<br>")
	String wseBySubjectAreaDayOfWeekAndHourOfDayReportNote();

	@DefaultMessage("Department Code")
	String columnDepartmentCode();

	@DefaultMessage("Department Abbreviation")
	String columnDepartmentAbbreviation();

	@DefaultMessage("Department Name")
	String columnDepartmentName();

	@DefaultMessage("Room Department Code")
	String columnRoomDepartmentCode();

	@DefaultMessage("Room Department Abbr")
	String columnRoomDepartmentAbbreviation();

	@DefaultMessage("Room Department Name")
	String columnRoomDepartmentName();

	@DefaultMessage("Building / Location")
	String columnBuilding();

	@DefaultMessage("Room")
	String columnRoom();

	@DefaultMessage("Room Type")
	String columnRoomType();

	@DefaultMessage("Subject Area")
	String columnSubjectArea();

	@DefaultMessage("Course Number")
	String columnCourseNumber();

	@DefaultMessage("Instructional Type")
	String columnItype();

	@DefaultMessage("Organized")
	String columnOrganized();

	@DefaultMessage("Section Number")
	String columnSectionNumber();

	@DefaultMessage("External Id")
	String columnExternalId();

	@DefaultMessage("Weekly Room Hours")
	String columnWeeklyRoomHours();

	@DefaultMessage("Weekly Class Hours")
	String columnWeeklyClassHours();

	@DefaultMessage("Weekly Student Enrollment Per Period")
	String columnWeeklyStudentEnrollmentPerPeriod();

	@DefaultMessage("Weekly Student Enrollment Per Hour")
	String columnWeeklyStudentEnrollmentPerHour();

	@DefaultMessage("Weekly Student Class Hours")
	String columnWeeklyStudentClassHours();

	@DefaultMessage("Organized Weekly Room Hours")
	String columnOrganizedWeeklyRoomHours();

	@DefaultMessage("Organized Weekly Class Hours")
	String columnOrganizedWeeklyClassHours();

	@DefaultMessage("Organized Weekly Student Class Hours")
	String columnOrganizedWeeklyStudentClassHours();

	@DefaultMessage("Organized Weekly Student Enrollment Per Period")
	String columnOrganizedWeeklyStudentEnrollmentPerPeriod();

	@DefaultMessage("Organized Weekly Student Enrollment Per Hour")
	String columnOrganizedWeeklyStudentEnrollmentPerHour();

	@DefaultMessage("Not Organized Weekly Room Hours")
	String columnNotOrganizedWeeklyRoomHours();

	@DefaultMessage("Not Organized Weekly Class Hours")
	String columnNotOrganizedWeeklyClassHours();

	@DefaultMessage("Not Organized Weekly Student Class Hours")
	String columnNotOrganizedWeeklyStudentClassHours();

	@DefaultMessage("Not Organized Weekly Student Enrollment Per Period")
	String columnNotOrganizedWeeklyStudentEnrollmentPerPeriod();

	@DefaultMessage("Not Organized Weekly Student Enrollment Per Hour")
	String columnNotOrganizedWeeklyStudentEnrollmentPerHour();

	@DefaultMessage("Instructor")
	String columnInstructor();

	@DefaultMessage("External Id")
	String columnInstructorExternalId();

	@DefaultMessage("Position")
	String columnPosition();

	@DefaultMessage("Number of Class Meetings")
	String columnNumberOfClassMeetings();

	@DefaultMessage("Day of Week")
	String columnDayOfWeek();

	@DefaultMessage("Period")
	String columnPeriod();

	@DefaultMessage("Total")
	String labelTotal();

	@DefaultMessage("Unknown")
	String labelUnknown();

	@DefaultMessage("Capacity")
	String columnCapacity();

	@DefaultMessage("Station Hours")
	String columnStationHours();
	
	@DefaultMessage("Occupancy")
	String columnOccupancy();

	@DefaultMessage("Normalized Percent Share")
	String columnNormalizedPercentShare();

	@DefaultMessage("Gradable Itype Credit")
	String columnGradableItypeCredit();
	
}