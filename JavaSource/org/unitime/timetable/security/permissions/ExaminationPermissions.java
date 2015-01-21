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
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class ExaminationPermissions {

	@PermissionForRight(Right.Examinations)
	public static class Examinations implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			if (SubjectArea.getUserSubjectAreas(user, false).isEmpty()) return false;
			
			if (ExamType.findAllUsed(source.getUniqueId()).isEmpty()) return false;
			
			if (user.getCurrentAuthority().hasRight(Right.DepartmentIndependent))
				return permissionSession.check(user, source, DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable);
			else
				return permissionSession.check(user, source, DepartmentStatusType.Status.ExamView);
		}

		@Override
		public Class<Session> type() { return Session.class; }
		
	}
	
	@PermissionForRight(Right.ExaminationDetail)
	public static class ExaminationDetail implements Permission<Exam> {
		@Autowired PermissionSession permissionSession;
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Exam source) {
			if (user.getCurrentAuthority().hasRight(Right.DepartmentIndependent))
				return permissionSession.check(user, source.getSession(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable);
			else {
				for (ExamOwner owner: source.getOwners()) {
					if (permissionDepartment.check(user, owner.getCourse().getSubjectArea().getDepartment(), DepartmentStatusType.Status.ExamView))
						return true;
				}
				return false;
			}
		}

		@Override
		public Class<Exam> type() { return Exam.class; }
		
	}
	
	@PermissionForRight(Right.ExaminationAdd)
	public static class AddExamination implements Permission<Session> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			if (ExamType.findAllUsed(source.getUniqueId()).isEmpty()) return false;
			
			if (user.getCurrentAuthority().hasRight(Right.DepartmentIndependent))
				return permissionSession.check(user, source, DepartmentStatusType.Status.ExamTimetable);
			else
				return permissionSession.check(user, source, DepartmentStatusType.Status.ExamEdit);
		}

		@Override
		public Class<Session> type() { return Session.class; }
		
	}
	
	@PermissionForRight(Right.ExaminationEdit)
	public static class EditExamination implements Permission<Exam> {
		@Autowired PermissionSession permissionSession;
		
		@Autowired PermissionDepartment permissionDepartment;

		@Override
		public boolean check(UserContext user, Exam source) {
			if (user.getCurrentAuthority().hasRight(Right.DepartmentIndependent))
				return permissionSession.check(user, source.getSession(), DepartmentStatusType.Status.ExamTimetable);
			else {
				for (ExamOwner owner: source.getOwners()) {
					if (permissionDepartment.check(user, owner.getCourse().getDepartment(), DepartmentStatusType.Status.ExamEdit))
						return true;
				}
				return false;
			}
		}

		@Override
		public Class<Exam> type() { return Exam.class; }
		
	}
	
	@PermissionForRight(Right.ExaminationDelete)
	public static class ExaminationDelete extends EditExamination{}
	
	@PermissionForRight(Right.ExaminationClone)
	public static class ExaminationClone extends EditExamination{}

	@PermissionForRight(Right.DistributionPreferenceExam)
	public static class DistributionPreferenceExam extends EditExamination{}
	
	@PermissionForRight(Right.ExaminationEditClearPreferences)
	public static class ExaminationEditClearPreferences extends EditExamination{}
	
	@PermissionForRight(Right.ExaminationAssignment)
	public static class ExaminationAssignment implements Permission<Exam> {
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Exam source) {
			return user.getCurrentAuthority().hasRight(Right.DepartmentIndependent) &&
				 permissionSession.check(user, source.getSession(), DepartmentStatusType.Status.ExamTimetable);
		}

		@Override
		public Class<Exam> type() { return Exam.class; }
		
	}

	@PermissionForRight(Right.ExaminationSchedule)
	public static class ExaminationSchedule implements Permission<Session> {
		
		@Autowired PermissionSession permissionSession;

		@Override
		public boolean check(UserContext user, Session source) {
			return permissionSession.check(user, source, DepartmentStatusType.Status.ReportExamsFinal, DepartmentStatusType.Status.ReportExamsMidterm);
		}

		@Override
		public Class<Session> type() { return Session.class; }
		
	}
	
	@PermissionForRight(Right.ExaminationDistributionPreferences)
	public static class ExaminationDistributionPreferences extends Examinations {}
	
	@PermissionForRight(Right.ExaminationDistributionPreferenceAdd)
	public static class ExaminationDistributionPreferenceAdd extends AddExamination {}
	
	@PermissionForRight(Right.ExaminationDistributionPreferenceEdit)
	public static class ExaminationDistributionPreferenceEdit implements Permission<DistributionPref> {
		
		@Autowired Permission<Exam> permissionExaminationEdit;

		@Override
		public boolean check(UserContext user, DistributionPref source) {
			for (DistributionObject distrObj: source.getDistributionObjects()) {
       			if (distrObj.getPrefGroup() instanceof Exam) {
       				if (!permissionExaminationEdit.check(user,  (Exam)distrObj.getPrefGroup()))
       					return false;
       			} else {
       				return false;
       			}
       		}
       		
       		return true;
		}

		@Override
		public Class<DistributionPref> type() { return DistributionPref.class;}
	}
	
	@PermissionForRight(Right.ExaminationDistributionPreferenceDelete)
	public static class ExaminationDistributionPreferenceDelete extends ExaminationDistributionPreferenceEdit {}
	
	@PermissionForRight(Right.ExaminationDistributionPreferenceDetail)
	public static class ExaminationDistributionPreferenceDetail implements Permission<DistributionPref> {
		@Autowired Permission<Exam> permissionExaminationDetail;

		@Override
		public boolean check(UserContext user, DistributionPref source) {
			for (DistributionObject distrObj: source.getDistributionObjects()) {
       			if (distrObj.getPrefGroup() instanceof Exam) {
       				if (permissionExaminationDetail.check(user,  (Exam)distrObj.getPrefGroup()))
       					return true;
       			}
       		}
			
       		return false;
		}

		@Override
		public Class<DistributionPref> type() { return DistributionPref.class;}
	}
}
