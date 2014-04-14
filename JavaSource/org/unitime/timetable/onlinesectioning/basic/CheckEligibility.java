/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.basic;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck.EligibilityFlag;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

/**
 * @author Tomas Muller
 */
public class CheckEligibility implements OnlineSectioningAction<OnlineSectioningInterface.EligibilityCheck> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	private Long iStudentId;
	private EligibilityCheck iCheck;
	
	public CheckEligibility forStudent(Long studentId) {
		iStudentId = studentId;
		return this;
	}
	
	public CheckEligibility withCheck(EligibilityCheck check) {
		iCheck = check;
		return this;
	}

	@Override
	public EligibilityCheck execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (iCheck == null)
			iCheck = new EligibilityCheck();
		
		Lock lock = server.readLock();
		try {
			// Always allow for the assistant mode
			iCheck.setFlag(EligibilityFlag.CAN_USE_ASSISTANT, true);

			Student student = (iStudentId == null ? null : StudentDAO.getInstance().get(iStudentId, helper.getHibSession()));
			if (student == null) {
				if (!iCheck.hasFlag(EligibilityFlag.IS_ADMIN) && !iCheck.hasFlag(EligibilityFlag.IS_ADVISOR))
					iCheck.setMessage(MSG.exceptionEnrollNotStudent(server.getAcademicSession().toString()));
				return iCheck;
			}
			
			StudentSectioningStatus status = student.getSectioningStatus();
			if (status == null) status = student.getSession().getDefaultSectioningStatus();
			boolean disabled = (status != null && !status.hasOption(StudentSectioningStatus.Option.enabled));
			if (disabled && iCheck.hasFlag(EligibilityFlag.IS_ADMIN))
				disabled = false;
			if (disabled && status.hasOption(StudentSectioningStatus.Option.advisor) && iCheck.hasFlag(EligibilityFlag.IS_ADVISOR))
				disabled = false;
			if (disabled) {
				if (status.getMessage() == null)
					iCheck.setMessage(MSG.exceptionEnrollmentDisabled());
				else
					iCheck.setMessage(status.getMessage());
			}
			
			if (server.getAcademicSession().isSectioningEnabled()) {
				if (!disabled)
					iCheck.setFlag(EligibilityFlag.CAN_ENROLL, true);
			} else {
				// iCheck.setMessage(MSG.exceptionNoServerForSession());
				iCheck.setFlag(EligibilityFlag.CAN_ENROLL, false);
			}

			return iCheck;
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "eligibility";
	}

}
