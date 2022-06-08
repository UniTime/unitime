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
}