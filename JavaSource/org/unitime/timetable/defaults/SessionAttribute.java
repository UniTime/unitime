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
package org.unitime.timetable.defaults;

/**
 * @author Tomas Muller
 */
public enum SessionAttribute {
	SelectedSolution("Solver.selectedSolutionId", "Selected course timetabling solution or solutions (String containing a comma separated list of ids)."),

	ClassAssignment("LastSolutionClassAssignmentProxy", "Last used class assignment proxy."),
	CourseTimetablingSolver("SolverProxy", "Last used course timetabling solver."),
	CourseTimetablingUser("ManageSolver.puid", "User id of the solver I am looking at (if different from user id, admin only)"),
	ExaminationSolver("ExamSolverProxy", "Last used examination solver."),
	ExaminationUser("ManageSolver.examPuid", "User id of the solver I am looking at (if different from user id, admin only)"),
	StudentSectioningSolver("StudentSolverProxy", "Last used student sectioning solver."),
	StudentSectioningUser("ManageSolver.sectionPuid", "User id of the solver I am looking at (if different from user id, admin only)"),
	InstructorSchedulingSolver("InstructorSchedulingProxy", "Last used instructor scheduling solver."),
	InstructorSchedulingUser("ManageSolver.instrPuid", "User id of the solver I am looking at (if different from user id, admin only)"),

	OfferingsSubjectArea("subjectAreaId", "Last used subject area or areas (String containing a comma separated list of ids)"),
	OfferingsCourseNumber("courseNbr", "Last used course number (String containing course number)"),
	ClassesSubjectAreas("crsLstSubjectAreaIds", "Last used subject areas (String containing a comma separated list of ids)"),
	ClassesCourseNumber("crsLstCrsNbr", "Last used course number (String containing course number)"),
	ClassAssignmentsSubjectAreas("crsAsgnLstSubjectAreaIds", "Last used subject areas (String containing a comma separated list of ids)"),
	
	DepartmentId("deptUniqueId", "Last department (String containing department unique id)"),
	DepartmentCodeRoom("deptCodeRoom", "Last department code (used by Rooms pages)"),
	
	TableOrder("OrderInfo", "WebTable order info"),
	
	ExamType("Exam.Type", "Examination type"),
	FormFactor("mgwt.formfactor", "Device form factor"),
	
	InstructionalOfferingConfigList("configsList", "Instructional Offering Config: Available ITypes"),
	LastUploadedFile("LAST_FILE", "Last uploaded file"),
	RoomPictures("ROOM_PICTURES", "Last uploaded room pictures"),
	CallingPage("callingPage", "Calling page"),
	CurriculaLastFilter("Curricula.LastFilter", "Curricula: last used filter"),
	ReservationsLastFilter("Reservations.LastFilter", "Reservations: last used filter"),
	PermissionRoles("Permissions.roleIds", "Permissions: last visible roles"),
	EventStatusServices("EventStatuses.Services", "Event Statuses: last services"),
	Back("BackTracker.back", "Last back"),
	NavigationLastIds("lastDispIds", "Navigation: last displayed ids"),
	SuggestionsModel("Suggestions.model", "Course Timetabling: last suggestions model"),
	
	OnlineSchedulingDummyServer("OnlineSectioning.DummyServer", "Online Student Scheduling: database server for academic sessions that are not loaded in"),
	OnlineSchedulingUser("user", "Online Student Scheduling: looked up user"),
	OnlineSchedulingPIN("pin", "Online Student Scheduling: last entered PIN"),
	OnlineSchedulingEligibility("eligibility", "Online Student Scheduling: output of the last eligibility check"),
	OnlineSchedulingLastRequest("request", "Online Student Scheduling: last course request"),
	OnlineSchedulingLastSession("sessionId", "Online Student Scheduling: last academic session id"),
	OnlineSchedulingLastSpecialRequest("specreq", "Online Student Scheduling: last special request id"),
	
	ClassInfoModel("ClassInfo.model", "Class Assignment page model"),
	ExamInfoModel("ExamInfo.model", "Examination Assignment page model"),
	;

	String iKey, iDefault, iDescription;
	SessionAttribute(String key, String defaultValue, String description) {
		iKey = key; iDefault = defaultValue; iDescription = defaultValue;
	}
	SessionAttribute(String key, String description) {
		this(key, null, description);
	}
	
	public String key() { return iKey; }
	public String defaultValue() { return iDefault; }
	public String description() { return iDescription; }
}