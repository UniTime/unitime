/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.security.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class ExaminationTimetablingPermissions {

	@PermissionForRight(Right.ExaminationTimetabling)
	public static class ExaminationTimetabling implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source, DepartmentStatusType.Status.ExamTimetable) &&
					(Exam.hasFinalExams(source.getUniqueId()) || Exam.hasMidtermExams(source.getUniqueId()));
		}

		@Override
		public Class<Session> type() { return Session.class; }
		
	}
	
	@PermissionForRight(Right.ExaminationSolver)
	public static class ExaminationSolver extends ExaminationTimetabling {}
	
	@PermissionForRight(Right.ExaminationSolutionExportXml)
	public static class ExaminationSolutionExportXml extends ExaminationSolver {}

	@PermissionForRight(Right.ExaminationTimetable)
	public static class ExaminationTimetable extends ExaminationTimetabling {}

	@PermissionForRight(Right.AssignedExaminations)
	public static class AssignedExaminations extends ExaminationTimetabling {}
	
	@PermissionForRight(Right.NotAssignedExaminations)
	public static class NotAssignedExaminations extends ExaminationTimetabling {}
	
	@PermissionForRight(Right.ExaminationAssignmentChanges)
	public static class ExaminationAssignmentChanges extends ExaminationTimetabling {}

	@PermissionForRight(Right.ExaminationConflictStatistics)
	public static class ExaminationConflictStatistics extends ExaminationTimetabling {}

	@PermissionForRight(Right.ExaminationSolverLog)
	public static class ExaminationSolverLog extends ExaminationTimetabling {}

	@PermissionForRight(Right.ExaminationReports)
	public static class ExaminationReports extends ExaminationTimetabling {}

	@PermissionForRight(Right.ExaminationPdfReports)
	public static class ExaminationPdfReports implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source);// && Exam.hasTimetable(source.getUniqueId());
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
}