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