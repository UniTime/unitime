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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.Set;

import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.WaitListValidationProvider;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XOverride;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public abstract class WaitlistedOnlineSectioningAction<T> implements OnlineSectioningAction<T> {
	private static final long serialVersionUID = 1L;
	private Set<String> iWaitlistStatuses = null;
	
	public boolean isWaitListed(XStudent student, XCourseRequest request, XOffering offering, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		// Check wait-list toggle first
		if (request == null || !request.isWaitlist()) return false;
		
		// Check student status
		String status = student.getStatus();
		if (status == null) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status != null) {
			if (iWaitlistStatuses == null)
				iWaitlistStatuses = StudentSectioningStatus.getMatchingStatuses(StudentSectioningStatus.Option.waitlist, server.getAcademicSession().getUniqueId());
			if (!iWaitlistStatuses.contains(status)) return false;
		}
		
		if (Customization.WaitListValidationProvider.hasProvider()) {
			for (XCourse course: offering.getCourses()) {
				if (!request.hasCourse(course.getCourseId())) continue;
				XOverride override = request.getOverride(course);
				if (override != null) {
					if ("TBD".equals(override.getExternalId())) return false; // override not requested --> ignore
					WaitListValidationProvider wp = Customization.WaitListValidationProvider.getProvider();
					if (wp.updateStudent(server, helper, student, helper.getAction()))
						override = request.getOverride(course);						
				}
				if (override != null && !override.isApproved()) return false;
			}
		}
		
		return true;
	}
}
