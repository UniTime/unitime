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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.Set;

import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;

public abstract class WaitlistedOnlineSectioningAction<T> implements OnlineSectioningAction<T> {
	private static final long serialVersionUID = 1L;
	private Set<String> iWaitlistStatuses = null;
	
	public boolean isWaitListed(XStudent student, XCourseRequest request, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		// Check wait-list toggle first
		if (request == null || !request.isWaitlist()) return false;
		
		// Check student status
		String status = student.getStatus();
		if (status == null) status = server.getAcademicSession().getDefaultSectioningStatus();
		if (status != null) {
			if (iWaitlistStatuses == null)
				iWaitlistStatuses = StudentSectioningStatus.getMatchingStatuses(StudentSectioningStatus.Option.waitlist);
			if (!iWaitlistStatuses.contains(status)) return false;
		}
		
		return true;
	}
}
