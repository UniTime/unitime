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

import org.unitime.timetable.gwt.resources.Messages;

public interface TeachingScheduleMessages extends Messages {
	@DefaultMessage("Teaching Schedules")
	String sectCourseMeetings();
	
	@DefaultMessage("Course Details")
	String sectCourseDetails();
	
	@DefaultMessage("Course Selection")
	String sectSelectCourse();
	
	@DefaultMessage("Teaching Schedule")
	String sectInstructorTeachingSchedule();
	
	@DefaultMessage("<u>B</u>ack &larr;")
	String buttonBackToDetail();
	
	@DefaultMessage("<u>C</u>ourse &larr;")
	String buttonCourseSelection();
	
	@DefaultMessage("<u>D</u>ivisions &rarr;")
	String buttonDivisions();
	
	@DefaultMessage("<u>D</u>ivisions &larr;")
	String buttonDivisionsBack();
	
	@DefaultMessage("<u>A</u>ssignments &rarr;")
	String buttonAssignments();
	
	@DefaultMessage("<u>S</u>ave")
	String buttonSave();
	
	@DefaultMessage("<u>B</u>ack")
	String buttonBack();
	
	@DefaultMessage("<u>D</u>elete")
	String buttonDelete();
	
	@DefaultMessage("<u>E</u>dit")
	String buttonEdit();
	
	@DefaultMessage("<u>A</u>pply")
	String buttonApply();
	
	@DefaultMessage("Check")
	String buttonValidate();
	
	@DefaultMessage("<u>C</u>heck All")
	String buttonValidateAll();
	
	@DefaultMessage("<u>S</u>earch")
	String buttonSearch();
	
	@DefaultMessage("<u>A</u>dd New")
	String buttonAddNew();
	
	@DefaultMessage("Export <u>X</u>LS")
	String buttonExportXls();
	
	@DefaultMessage("Attribute")
	String colDivisionAttribute();
	
	@DefaultMessage("Name")
	String colDivisionName();
	
	@DefaultMessage("Instr. Type")
	String colDivisionType();
	
	@DefaultMessage("Classes")
	String colDivisionClasses();
	
	@DefaultMessage("Groups")
	String colDivisionGroups();
	
	@DefaultMessage("Hours")
	String colDivisionHours();
	
	@DefaultMessage("Parallels")
	String colDivisionParallels();
	
	@DefaultMessage("Date")
	String colMeetingDate();
	
	@DefaultMessage("Time")
	String colMeetingTime();
	
	@DefaultMessage("Hours")
	String colMeetingHours();
	
	@DefaultMessage("Room")
	String colMeetingRoom();
	
	@DefaultMessage("Division")
	String colMeetingDivision();
	
	@DefaultMessage("Instructor")
	String colMeetingInstructor();
	
	@DefaultMessage("Note")
	String colMeetingNote();
	
	@DefaultMessage("Offering")
	String colOffering();
	
	@DefaultMessage("Group")
	String colMeetingGroup();
	
	@DefaultMessage("Instructional Offering:")
	String propInstructionalOffering();
	
	@DefaultMessage("Attributes:")
	String propAttributeType();
	
	@DefaultMessage("Teaching Schedules")
	@DoNotTranslate
	String pageTeachingSchedules();
	
	@DefaultMessage("Edit Teaching Schedule")
	@DoNotTranslate
	String pageEditTeachingSchedule();
	
	@DefaultMessage("Teaching Schedule Detail")
	@DoNotTranslate
	String pageDetailTeachingSchedule();
	
	@DefaultMessage("Too many hours for {0} ({1}).")
	String errorDivisionTooManyHours(String type, String value);
	
	@DefaultMessage("Too little hours for {0} ({1}).")
	String errorDivisionTooLittleHours(String type, String value);
	
	@DefaultMessage("No groups for {0}.")
	String errorDivisionNoGroups(String type);
	
	@DefaultMessage("No hours for {0}. division of {1}.")
	String errorDivisionNoHours(int div, String type);
	
	@DefaultMessage("No name for {0}. division of {1}.")
	String errorDivisionNoName(int div, String type);
	
	@DefaultMessage("No division selected for {0}.")
	String errorClassHasNoDivision(String time);
	
	@DefaultMessage("Too many hours for {0} ({1}).")
	String errorClassTooManyHours(String type, String value);
	
	@DefaultMessage("Too little hours for {0} ({1}).")
	String errorClassTooLittleHours(String type, String value);
	
	@DefaultMessage("Instructor {0} not available on {1}.")
	String errorInstructorNotAvailable(String instructor, String date);
	
	@DefaultMessage("Instructor {0} prohibits time during {1}.")
	String errorInstructorProhibited(String instructor, String time);
	
	@DefaultMessage("Instructor {0} is teaching {2} during {1}.")
	String errorInstructorTeachingConflict(String instructor, String time, String other);
	
	@DefaultMessage("Too few parallel sessions of {0} for {1} ({2}).")
	String errorClassTooLittleGroups(String type, String time, String value);
	
	@DefaultMessage("Too many parallel sessions of {0} for {1} ({2}).")
	String errorClassTooManyGroups(String type, String time, String value);
	
	@DefaultMessage("Instructor {0} teaching overload for {1} ({2}).")
	String errorInstructorOverload(String instructor, String time, String value);
	
	@DefaultMessage("Instructor {0} is missing an attribute for {1} ({2}).")
	String errorInstructorNoAttribute(String instructor, String time, String value);
	
	@DefaultMessage("No issues detected for {0}.")
	String validationOk(String clazz);
	
	@DefaultMessage("Offering saved succesfully.")
	String savedOK();
	
	@DefaultMessage("All")
	String itemAllSubjects();
	
	@DefaultMessage("Subject Area:")
	String filterSubjectArea();
	
	@DefaultMessage("Working... Please wait...")
	String pleaseWait();
	
	@DefaultMessage("Are you sure you want to delete course meetings for {0}?")
	String confirmDelete(String course);
}
