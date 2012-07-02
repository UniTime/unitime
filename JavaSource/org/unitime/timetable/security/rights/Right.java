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
package org.unitime.timetable.security.rights;

public enum Right {
	/** Session default: current session */
	SessionDefaultCurrent, // -- DEFAULT SESSION SELECTION
	/** Session default: first future session */
	SessionDefaultFirstFuture,
	/** Session default: first examination session */
	SessionDefaultFirstExamination,
	
	/** Session dependency -- if independent the role applies to all academic session */
	SessionIndependent,
	SessionIndependentIfNoSessionGiven,
	/** Session dependency -- test sessions are allowed */
	AllowTestSessions,
	
	/** Department dependency -- department must match */
	DepartmentIndependent,
	
	/** Status dependency -- session / department status must match */
	StatusIndependent,
	
	AddNonUnivLocation,
	AddSpecialUseRoom,
	ApplicationConfig,
	AssignedClasses,
	AssignedExams,
	AssignmentHistory,
	
	/** Class level rights */
	ClassDetail,
	ClassEdit,

	/** Curriculum rights */
    CurriculumView,
    CurriculumDetail,
    CurriculumAdd,
    CurriculumEdit,
    CurriculumDelete,
    CurriculumMerge,
    CurriculumAdmin

    ;
}
