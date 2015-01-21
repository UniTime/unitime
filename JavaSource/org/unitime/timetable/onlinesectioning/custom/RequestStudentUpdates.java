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
