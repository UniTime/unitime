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
package org.unitime.timetable.onlinesectioning.basic;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck.EligibilityFlag;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.basic.CheckEligibility;
import org.unitime.timetable.onlinesectioning.custom.CustomCourseRequestsValidationHolder;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;

/**
 * @author Tomas Muller
 */
public class CourseRequestEligibility extends CheckEligibility {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	@Override
	protected void logCheck(OnlineSectioningLog.Action.Builder action, EligibilityCheck check) {
		for (EligibilityCheck.EligibilityFlag f: EligibilityCheck.EligibilityFlag.values())
			if (check.hasFlag(f))
				action.addOptionBuilder().setKey(f.name().replace('_', ' ')).setValue("true");
		if (check.hasMessage())
			action.addMessageBuilder().setText(check.getMessage()).setLevel(OnlineSectioningLog.Message.Level.WARN);
		if (check.hasFlag(EligibilityFlag.CAN_REGISTER))
			action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);
		else 
			action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
	}
	
	@Override
	public OnlineSectioningInterface.EligibilityCheck execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (iCheck == null)
			iCheck = new EligibilityCheck();
		
		OnlineSectioningLog.Action.Builder action = helper.getAction();

		Lock lock = (iStudentId == null ? null : server.lockStudent(iStudentId, null, name()));
		try {
			iCheck.setFlag(EligibilityFlag.CAN_USE_ASSISTANT, true);
			if (iStudentId != null)
				action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(iStudentId));
			
			iCheck.setFlag(EligibilityFlag.CAN_WAITLIST, server.getAcademicSession().isSectioningEnabled() && CustomStudentEnrollmentHolder.isAllowWaitListing());
			
			org.hibernate.Session hibSession = StudentDAO.getInstance().createNewSession();
			try {
				Student student = (iStudentId == null ? null : StudentDAO.getInstance().get(iStudentId, hibSession));
				if (student == null) {
					if (!iCheck.hasFlag(EligibilityFlag.IS_ADMIN) && !iCheck.hasFlag(EligibilityFlag.IS_ADVISOR) && !iCheck.hasFlag(EligibilityFlag.IS_GUEST))
						iCheck.setMessage(MSG.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
					logCheck(action, iCheck);
					action.setResult(OnlineSectioningLog.Action.ResultType.NULL);
					return iCheck;
				}
				
				action.getStudentBuilder().setExternalId(student.getExternalUniqueId());
				action.getStudentBuilder().setName(helper.getStudentNameFormat().format(student));
				
				StudentSectioningStatus status = student.getEffectiveStatus();
				boolean disabled = (status != null && !status.hasOption(StudentSectioningStatus.Option.regenabled));
				
				boolean noreg = false;
				if (iPermissionCanEnroll != null) {
					noreg = !iPermissionCanEnroll;
				} else {
					noreg = (status != null && !status.hasOption(StudentSectioningStatus.Option.registration));
					if (noreg && status.hasOption(StudentSectioningStatus.Option.regadmin) && iCheck.hasFlag(EligibilityFlag.IS_ADMIN))
						noreg = false;
					if (noreg && status.hasOption(StudentSectioningStatus.Option.regadvisor) && iCheck.hasFlag(EligibilityFlag.IS_ADVISOR))
						noreg = false;
				}
				
				if (status != null && !status.hasOption(StudentSectioningStatus.Option.waitlist))
					iCheck.setFlag(EligibilityFlag.CAN_WAITLIST, false);
				
				if (disabled)
					iCheck.setFlag(EligibilityFlag.CAN_USE_ASSISTANT, false);
				
				if (student.getSession().getStatusType().canPreRegisterStudents()) {
					if (!noreg)
						iCheck.setFlag(EligibilityFlag.CAN_REGISTER, true);
				} else {
					iCheck.setFlag(EligibilityFlag.CAN_REGISTER, false);
				}
				
				StudentSectioningStatus s = student.getSectioningStatus();
				while (s != null && s.isPast() && s.getFallBackStatus() != null) s = s.getFallBackStatus();
				
				if (s != null && s.getMessage() != null)
					iCheck.setMessage(s.getMessage());
				else if (status != null && status.getMessage() != null)
					iCheck.setMessage(status.getMessage());
				else if (disabled)
					iCheck.setMessage(MSG.exceptionAccessDisabled());
				else if (noreg)
					iCheck.setMessage(MSG.exceptionEnrollmentDisabled());

				String effectivePeriod = (s != null ? s.getEffectivePeriod() : status != null ? status.getEffectivePeriod() : null);
				if (effectivePeriod != null)
					iCheck.setMessage((iCheck.hasMessage() ? iCheck.getMessage() + "\n" : "") + MSG.messageTimeWindow(effectivePeriod));
				
				if (iCustomCheck) {
					if (CustomCourseRequestsValidationHolder.hasProvider())
						CustomCourseRequestsValidationHolder.getProvider().checkEligibility(server, helper, iCheck, student);
				}
			} finally {
				if (hibSession != null) hibSession.close();
			}

			logCheck(action, iCheck);
			return iCheck;
		} catch (SectioningException e) {
			iCheck.setFlag(EligibilityFlag.CAN_REGISTER, false);
			iCheck.setMessage(MSG.exceptionFailedEligibilityCheck(e.getMessage()));
			helper.info(MSG.exceptionFailedEligibilityCheck(e.getMessage()));
			logCheck(action, iCheck);
			action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
			return iCheck;
		} finally {
			if (lock != null) lock.release();
		}
	}

}
