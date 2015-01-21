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
package org.unitime.timetable.security.permissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
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
			if (SubjectArea.getUserSubjectAreas(user, false).isEmpty()) return false;
			
			if (ExamType.findAllUsed(source.getUniqueId()).isEmpty()) return false;

			return permissionSession.check(user, source);
		}

		@Override
		public Class<Session> type() { return Session.class; }
	}
}