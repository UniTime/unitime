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
package org.unitime.timetable.export;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;

public class EventsExportMeetingsToCSV extends EventsExportEventsToCSV {
	
	@Override
	public String reference() {
		return "meetings.csv";
	}
	
	@Override
	protected void print(Printer out, List<EventInterface> events) {
		out.println(MESSAGES.colName(), MESSAGES.colSection(), MESSAGES.colType(), MESSAGES.colDate(),
				MESSAGES.colPublishedStartTime(), MESSAGES.colPublishedEndTime(), MESSAGES.colAllocatedStartTime(), MESSAGES.colAllocatedEndTime(),
				MESSAGES.colLocation(), MESSAGES.colLimit(), MESSAGES.colEnrollment(),
				MESSAGES.colSponsorOrInstructor(), MESSAGES.colEmail(),
				MESSAGES.colMainContact(), MESSAGES.colEmail(), MESSAGES.colApproval());
		
		DateFormat df = new SimpleDateFormat(CONSTANTS.eventDateFormat(), Localization.getJavaLocale());
		
		for (EventInterface event: events) {
			for (MeetingInterface meeting: new TreeSet<MeetingInterface>(event.getMeetings())) {
				out.print(
					event.getName(),
					event.getSectionNumber(),
					event.hasInstruction() ? event.getInstruction() : event.getType().getAbbreviation(),
					df.format(meeting.getMeetingDate()),
					meeting.getStartTime(CONSTANTS, true),
					meeting.getEndTime(CONSTANTS, true),
					meeting.getStartTime(CONSTANTS, false),
					meeting.getEndTime(CONSTANTS, false),
					meeting.getLocationName(),
					meeting.hasLocation() && meeting.getLocation().hasSize() ? meeting.getLocation().getSize().toString() : null,
					event.hasInstructors() ? event.getInstructorNames("\n") : event.hasSponsor() ? event.getSponsor().getName() : null,
							event.hasInstructors() ? event.getInstructorEmails("\n") : event.hasSponsor() ? event.getSponsor().getEmail() : null,
					event.hasMaxCapacity() ? event.getMaxCapacity().toString() : null,
					event.hasContact() ? event.getContact().getName() : null,
					event.hasContact() ? event.getContact().getEmail() : null,
					meeting.isApproved() ? df.format(meeting.getApprovalDate()) : MESSAGES.approvalNotApproved()
					);
			}
			out.println();
		}
	}

}
