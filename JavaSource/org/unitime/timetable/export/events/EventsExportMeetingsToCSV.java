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
package org.unitime.timetable.export.events;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventFlag;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;

public class EventsExportMeetingsToCSV extends EventsExporter {
	
	@Override
	public String reference() {
		return "meetings.csv";
	}
	
	@Override
	protected void print(ExportHelper helper, List<EventInterface> events, int eventCookieFlags, EventMeetingSortBy sort) throws IOException {
		Printer printer = new CSVPrinter(helper.getWriter(), false);
		helper.setup(printer.getContentType(), reference(), false);
		hideColumns(printer, events, eventCookieFlags);
		print(printer, meetings(events, sort));
	}

	
	@Override
	protected void hideColumn(Printer out, List<EventInterface> events, EventFlag flag) {
		switch (flag) {
		case SHOW_SECTION: out.hideColumn(1); break;
		case SHOW_PUBLISHED_TIME: out.hideColumn(4); out.hideColumn(5); break;
		case SHOW_ALLOCATED_TIME: out.hideColumn(6); out.hideColumn(7); break;
		case SHOW_SETUP_TIME: out.hideColumn(8); break;
		case SHOW_TEARDOWN_TIME: out.hideColumn(9); break;
		case SHOW_CAPACITY: out.hideColumn(11); break;
		case SHOW_ENROLLMENT: out.hideColumn(12); break;
		case SHOW_LIMIT: out.hideColumn(13); break;
		case SHOW_SPONSOR: out.hideColumn(14); out.hideColumn(15); break;
		case SHOW_MAIN_CONTACT: out.hideColumn(16); out.hideColumn(17); break;
		}
	}
	
	protected void print(Printer out, Set<EventMeeting> meetings) throws IOException {
		out.printHeader(
				/*  0 */ MESSAGES.colName(),
				/*  1 */ MESSAGES.colSection(),
				/*  2 */ MESSAGES.colType(),
				/*  3 */ MESSAGES.colDate(),
				/*  4 */ MESSAGES.colPublishedStartTime(),
				/*  5 */ MESSAGES.colPublishedEndTime(),
				/*  6 */ MESSAGES.colAllocatedStartTime(),
				/*  7 */ MESSAGES.colAllocatedEndTime(),
				/*  8 */ MESSAGES.colSetupTimeShort(),
				/*  9 */ MESSAGES.colTeardownTimeShort(),
				/* 10 */ MESSAGES.colLocation(),
				/* 11 */ MESSAGES.colCapacity(),
				/* 12 */ MESSAGES.colEnrollment(),
				/* 13 */ MESSAGES.colLimit(),
				/* 14 */ MESSAGES.colSponsorOrInstructor(),
				/* 15 */ MESSAGES.colEmail(),
				/* 16 */ MESSAGES.colMainContact(),
				/* 17 */ MESSAGES.colEmail(),
				/* 18 */ MESSAGES.colApproval());
		
		DateFormat df = new SimpleDateFormat(CONSTANTS.eventDateFormat(), Localization.getJavaLocale());
		EventInterface last = null;
		
		for (EventMeeting em: meetings) {
			EventInterface event = em.getEvent();
			MeetingInterface meeting = em.getMeeting();
			
			if (last == null || !last.equals(event)) {
				out.flush();
				last = event;
			}
			
			out.printLine(
					getName(event),
					getSection(event),
					event.hasInstruction() ? event.getInstruction() : event.getType().getAbbreviation(),
					meeting.isArrangeHours() ? "" : df.format(meeting.getMeetingDate()),
					meeting.isArrangeHours() ? "" : meeting.getStartTime(CONSTANTS, true),
					meeting.isArrangeHours() ? "" : meeting.getEndTime(CONSTANTS, true),
					meeting.isArrangeHours() ? "" : meeting.getStartTime(CONSTANTS, false),
					meeting.isArrangeHours() ? "" : meeting.getEndTime(CONSTANTS, false),
					meeting.isArrangeHours() ? "" : String.valueOf(meeting.getStartOffset()),
					meeting.isArrangeHours() ? "" : String.valueOf(-meeting.getEndOffset()),
					meeting.getLocationName(),
					meeting.hasLocation() && meeting.getLocation().hasSize() ? meeting.getLocation().getSize().toString() : null,
					event.hasMaxCapacity() ? event.getMaxCapacity().toString() : null,
					event.hasEnrollment() ? event.getEnrollment().toString() : null,
					event.hasInstructors() ? event.getInstructorNames("\n") : event.hasSponsor() ? event.getSponsor().getName() : null,
					event.hasInstructors() ? event.getInstructorEmails("\n") : event.hasSponsor() ? event.getSponsor().getEmail() : null,
					event.hasContact() ? event.getContact().getName() : null,
					event.hasContact() ? event.getContact().getEmail() : null,
					meeting.isArrangeHours() ? "" : meeting.isApproved() ? df.format(meeting.getApprovalDate()) : MESSAGES.approvalNotApproved()
					);
		}
		
		out.flush();
		out.close();
	}
}
