/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.defaults;

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