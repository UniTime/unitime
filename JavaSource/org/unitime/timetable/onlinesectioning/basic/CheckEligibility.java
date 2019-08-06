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
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck.EligibilityFlag;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomSpecialRegistrationHolder;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;
import org.unitime.timetable.onlinesectioning.updates.ReloadStudent;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class CheckEligibility implements OnlineSectioningAction<OnlineSectioningInterface.EligibilityCheck> {
	private static final long serialVersionUID = 1L;
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	protected Long iStudentId;
	protected EligibilityCheck iCheck;
	protected boolean iCustomCheck = true;
	protected Boolean iPermissionCanEnroll;
	protected Boolean iPermissionCanRequirePreferences;
	
	public CheckEligibility forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}
	
	public CheckEligibility withCheck(EligibilityCheck check) {
		iCheck = check;
		return this;
	}
	
	public CheckEligibility includeCustomCheck(boolean customCheck) {
		iCustomCheck = customCheck;
		return this;
	}
	
	public CheckEligibility withPermission(boolean canEnroll, boolean canRequire) {
		iPermissionCanEnroll = canEnroll;
		iPermissionCanRequirePreferences = canRequire;
		return this;
	}
	
	protected void logCheck(OnlineSectioningLog.Action.Builder action, EligibilityCheck check) {
		for (EligibilityCheck.EligibilityFlag f: EligibilityCheck.EligibilityFlag.values())
			if (check.hasFlag(f))
				action.addOptionBuilder().setKey(f.name().replace('_', ' ')).setValue("true");
		if (check.hasMessage())
			action.addMessageBuilder().setText(check.getMessage()).setLevel(OnlineSectioningLog.Message.Level.WARN);
		if (check.hasFlag(EligibilityFlag.CAN_ENROLL))
			action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);
		else 
			action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
	}

	@Override
	public EligibilityCheck execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (iCheck == null)
			iCheck = new EligibilityCheck();
		
		OnlineSectioningLog.Action.Builder action = helper.getAction();

		Lock lock = (iStudentId == null ? null : server.lockStudent(iStudentId, null, name()));
		try {
			// Always allow for the assistant mode
			iCheck.setFlag(EligibilityFlag.CAN_USE_ASSISTANT, true);
			if (iStudentId != null)
				action.setStudent(OnlineSectioningLog.Entity.newBuilder().setUniqueId(iStudentId));
			
			iCheck.setFlag(EligibilityFlag.CAN_WAITLIST, server.getAcademicSession().isSectioningEnabled() && CustomStudentEnrollmentHolder.isAllowWaitListing());
			
			org.hibernate.Session hibSession = StudentDAO.getInstance().createNewSession();
			XStudent xstudent = null;
			try {
				Student student = (iStudentId == null ? null : StudentDAO.getInstance().get(iStudentId, hibSession));
				if (student == null) {
					if (!iCheck.hasFlag(EligibilityFlag.IS_ADMIN) && !iCheck.hasFlag(EligibilityFlag.IS_ADVISOR) && !iCheck.hasFlag(EligibilityFlag.IS_GUEST)
							&& server.getAcademicSession().isSectioningEnabled()) {
						iCheck.setMessage(MSG.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
						if (CustomStudentEnrollmentHolder.hasProvider() && CustomStudentEnrollmentHolder.getProvider().isCanRequestUpdates()) {
							hibSession.close(); hibSession = null;
							// UniTime does not know about the student, but there is an enrollment provider capable of requesting updates -> use check eligibility to request an update
							CustomStudentEnrollmentHolder.getProvider().checkEligibility(server, helper, iCheck, new XStudent(null, helper.getUser().getExternalId(), helper.getUser().getName()));
						}
					}
					logCheck(action, iCheck);
					action.setResult(OnlineSectioningLog.Action.ResultType.NULL);
					return iCheck;
				}
				
				action.getStudentBuilder().setExternalId(student.getExternalUniqueId());
				action.getStudentBuilder().setName(helper.getStudentNameFormat().format(student));
				
				StudentSectioningStatus status = student.getEffectiveStatus();
				boolean disabled = (status != null && !status.hasOption(StudentSectioningStatus.Option.enabled));
				if (disabled && status != null && status.hasOption(StudentSectioningStatus.Option.admin) && iCheck.hasFlag(EligibilityFlag.IS_ADMIN))
					disabled = false;
				if (disabled && status != null && status.hasOption(StudentSectioningStatus.Option.advisor) && iCheck.hasFlag(EligibilityFlag.IS_ADVISOR))
					disabled = false;
				
				boolean noenrl = false;
				if (iPermissionCanEnroll != null) {
					noenrl = !iPermissionCanEnroll;
				} else {
					noenrl = (status != null && !status.hasOption(StudentSectioningStatus.Option.enrollment));
					if (noenrl && status.hasOption(StudentSectioningStatus.Option.admin) && iCheck.hasFlag(EligibilityFlag.IS_ADMIN))
						noenrl = false;
					if (noenrl && status.hasOption(StudentSectioningStatus.Option.advisor) && iCheck.hasFlag(EligibilityFlag.IS_ADVISOR))
						noenrl = false;
				}
				
				if (status != null && !status.hasOption(StudentSectioningStatus.Option.waitlist))
					iCheck.setFlag(EligibilityFlag.CAN_WAITLIST, false);
				
				if (iPermissionCanRequirePreferences != null)
					iCheck.setFlag(EligibilityFlag.CAN_REQUIRE, iPermissionCanRequirePreferences);
				else
					iCheck.setFlag(EligibilityFlag.CAN_REQUIRE, iCheck.hasFlag(EligibilityFlag.IS_ADMIN) || iCheck.hasFlag(EligibilityFlag.IS_ADVISOR) || status == null || status.hasOption(StudentSectioningStatus.Option.canreq));
				
				if (disabled)
					iCheck.setFlag(EligibilityFlag.CAN_USE_ASSISTANT, false);
				
				if (server.getAcademicSession().isSectioningEnabled()) {
					if (!noenrl)
						iCheck.setFlag(EligibilityFlag.CAN_ENROLL, true);
				} else {
					iCheck.setFlag(EligibilityFlag.CAN_ENROLL, false);
				}

				StudentSectioningStatus s = student.getSectioningStatus();
				while (s != null && s.isPast() && s.getFallBackStatus() != null) s = s.getFallBackStatus();
				
				if (s != null && s.getMessage() != null)
					iCheck.setMessage(s.getMessage());
				else if (status != null && status.getMessage() != null)
					iCheck.setMessage(status.getMessage());
				else if (disabled)
					iCheck.setMessage(MSG.exceptionAccessDisabled());
				else if (noenrl)
					iCheck.setMessage(MSG.exceptionEnrollmentDisabled());
				
				String effectivePeriod = (s != null ? s.getEffectivePeriod() : status != null ? status.getEffectivePeriod() : null);
				if (effectivePeriod != null)
					iCheck.setMessage((iCheck.hasMessage() ? iCheck.getMessage() + "\n" : "") + MSG.messageTimeWindow(effectivePeriod));
					
				xstudent = server.getStudent(iStudentId);
			} finally {
				if (hibSession != null) hibSession.close();
			}

			if (xstudent == null && iStudentId != null) {
				// Server does not know about the student, but he/she is in the database --> try to reload it
				server.createAction(ReloadStudent.class).forStudents(iStudentId).execute(server, helper);
				xstudent = server.getStudent(iStudentId);
			}

			if (xstudent == null) {
				if (!iCheck.hasMessage())
					iCheck.setMessage(MSG.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
				iCheck.setFlag(EligibilityFlag.CAN_ENROLL, false);
			} else if (iCustomCheck) {
				if (CustomStudentEnrollmentHolder.hasProvider())
					CustomStudentEnrollmentHolder.getProvider().checkEligibility(server, helper, iCheck, xstudent);
				if (CustomSpecialRegistrationHolder.hasProvider())
					CustomSpecialRegistrationHolder.getProvider().checkEligibility(server, helper, iCheck, xstudent);
			}

			logCheck(action, iCheck);
			return iCheck;
		} catch (SectioningException e) {
			iCheck.setFlag(EligibilityFlag.CAN_ENROLL, false);
			iCheck.setMessage(MSG.exceptionFailedEligibilityCheck(e.getMessage()));
			helper.info(MSG.exceptionFailedEligibilityCheck(e.getMessage()));
			logCheck(action, iCheck);
			action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
			return iCheck;
		} finally {
			if (lock != null) lock.release();
		}
	}

	@Override
	public String name() {
		return "eligibility";
	}

}
