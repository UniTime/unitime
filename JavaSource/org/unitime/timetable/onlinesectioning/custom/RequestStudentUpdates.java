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
package org.unitime.timetable.onlinesectioning.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class RequestStudentUpdates implements OnlineSectioningAction<Boolean>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	Collection<Long> iStudentIds = null;
	
	public RequestStudentUpdates forStudents(Collection<Long> studentIds) {
		iStudentIds = studentIds;
		return this;
	}
	
	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (!CustomStudentEnrollmentHolder.isCanRequestUpdates())
			throw new SectioningException(MSG.exceptionRequestStudentUpdateNotAllowed());

		List<XStudent> students = new ArrayList<XStudent>();
		for (Long studentId: iStudentIds) {
			XStudent student = server.getStudent(studentId);
			if (student != null) {
				OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
				action.setStudent(OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(student.getStudentId())
					.setExternalId(student.getExternalId())
					.setName(student.getName()));
				students.add(student);
			}
		}
		
		return CustomStudentEnrollmentHolder.getProvider().requestUpdate(server, helper, students);
	}

	@Override
	public String name() {
		return "request-update";
	}

}
