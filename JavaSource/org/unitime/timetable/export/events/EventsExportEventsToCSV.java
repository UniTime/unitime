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

import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.EventFlag;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MultiMeetingInterface;

@Service("org.unitime.timetable.export.Exporter:events.csv")
public class EventsExportEventsToCSV extends EventsExporter {	
	@Override
	public String reference() {
		return "events.csv";
	}
	
	@Override
	protected void print(ExportHelper helper, List<EventInterface> events, int eventCookieFlags, EventMeetingSortBy sort, boolean asc) throws IOException {
		sort(events, sort, asc);
		Printer printer = new CSVPrinter(helper.getWriter(), false);
		helper.setup(printer.getContentType(), reference(), false);
		hideColumns(printer, events, eventCookieFlags);
		print(printer, events);
	}
	
	@Override
	protected void hideColumn(Printer out, List<EventInterface> events, EventFlag flag) {
		switch (flag) {
		case SHOW_SECTION: out.hideColumn(1); break;
		case SHOW_TITLE: out.hideColumn(3); break;
		case SHOW_NOTE: out.hideColumn(4); break;
		case SHOW_PUBLISHED_TIME: out.hideColumn(8); out.hideColumn(9); break;
		case SHOW_ALLOCATED_TIME: out.hideColumn(10); out.hideColumn(11); break;
		case SHOW_SETUP_TIME: out.hideColumn(12); break;
		case SHOW_TEARDOWN_TIME: out.hideColumn(13); break;
		case SHOW_CAPACITY: out.hideColumn(15); break;
		case SHOW_ENROLLMENT: out.hideColumn(16); break;
		case SHOW_LIMIT: out.hideColumn(17); break;
		case SHOW_SPONSOR: out.hideColumn(18); out.hideColumn(19); break;
		case SHOW_MAIN_CONTACT: out.hideColumn(20); out.hideColumn(21); break;
		case SHOW_APPROVAL: out.hideColumn(22); break;
		}
	}
	
	protected void print(Printer out, List<EventInterface> events) throws IOException {
		out.printHeader(
				/*  0 */ MESSAGES.colName(),
				/*  1 */ MESSAGES.colSection(),
				/*  2 */ MESSAGES.colType(),
				/*  3 */ MESSAGES.colTitle(),
				/*  4 */ MESSAGES.colNote(),
				/*  5 */ MESSAGES.colDayOfWeek(),
				/*  6 */ MESSAGES.colFirstDate(),
				/*  7 */ MESSAGES.colLastDate(),
				/*  8 */ MESSAGES.colPublishedStartTime(),
				/*  9 */ MESSAGES.colPublishedEndTime(),
				/* 10 */ MESSAGES.colAllocatedStartTime(),
				/* 11 */ MESSAGES.colAllocatedEndTime(),
				/* 12 */ MESSAGES.colSetupTimeShort(),
				/* 13 */ MESSAGES.colTeardownTimeShort(),
				/* 14 */ MESSAGES.colLocation(),
				/* 15 */ MESSAGES.colCapacity(),
				/* 16 */ MESSAGES.colEnrollment(),
				/* 17 */ MESSAGES.colLimit(),
				/* 18 */ MESSAGES.colSponsorOrInstructor(),
				/* 19 */ MESSAGES.colEmail(),
				/* 20 */ MESSAGES.colMainContact(),
				/* 21 */ MESSAGES.colEmail(),
				/* 22 */ MESSAGES.colApproval());
		
		DateFormat df = new SimpleDateFormat(CONSTANTS.eventDateFormat(), Localization.getJavaLocale());
		
		for (EventInterface event: events) {
			for (MultiMeetingInterface multi: EventInterface.getMultiMeetings(event.getMeetings(), false)) {
				MeetingInterface meeting = multi.getMeetings().first();
				out.printLine(
					getName(event),
					getSection(event),
					event.hasInstruction() ? event.getInstruction() : event.getType().getAbbreviation(CONSTANTS),
					getTitle(event),
					event.hasEventNote() ? event.getEventNote("\n").replace("<br>", "\n") : "",
					multi.getDays(CONSTANTS.shortDays(), CONSTANTS.shortDays(), CONSTANTS.daily()),
					multi.getFirstMeetingDate() == null ? "" : df.format(multi.getFirstMeetingDate()),
					multi.getLastMeetingDate() == null ? "" : multi.getNrMeetings() == 1 ? null : df.format(multi.getLastMeetingDate()),
					meeting.isArrangeHours() ? "" : meeting.getStartTime(CONSTANTS, true),
					meeting.isArrangeHours() ? "" : meeting.getEndTime(CONSTANTS, true),
					meeting.isArrangeHours() ? "" : meeting.getStartTime(CONSTANTS, false),
					meeting.isArrangeHours() ? "" : meeting.getEndTime(CONSTANTS, false),
					meeting.isArrangeHours() ? "" : String.valueOf(meeting.getStartOffset()),
					meeting.isArrangeHours() ? "" : String.valueOf(-meeting.getEndOffset()),
					meeting.getLocationName(),
					meeting.hasLocation() && meeting.getLocation().hasSize() ? meeting.getLocation().getSize().toString() : null,
					event.hasEnrollment() ? event.getEnrollment().toString() : null,
					event.hasMaxCapacity() ? event.getMaxCapacity().toString() : null,		
					event.hasInstructors() ? event.getInstructorNames("\n", MESSAGES) : event.hasSponsor() ? event.getSponsor().getName() : null,
					event.hasInstructors() ? event.getInstructorEmails("\n") : event.hasSponsor() ? event.getSponsor().getEmail() : null,
					event.hasContact() ? event.getContact().getName(MESSAGES) : null,
					event.hasContact() ? event.getContact().getEmail() : null,
					event.getType() == EventType.Unavailabile ? "" :
					multi.getApprovalStatus() == ApprovalStatus.Approved ? df.format(multi.getApprovalDate()) :
					multi.getApprovalStatus() == ApprovalStatus.Cancelled ? MESSAGES.approvalCancelled() :
					multi.getApprovalStatus() == ApprovalStatus.Rejected ? MESSAGES.approvalRejected() :
					multi.getApprovalStatus() == ApprovalStatus.Deleted ? MESSAGES.approvalDeleted() :
					multi.isPast() ? MESSAGES.approvalNotApprovedPast() :
					event.getExpirationDate() != null ? MESSAGES.approvalExpire(df.format(event.getExpirationDate())) :
					MESSAGES.approvalNotApproved()
					);
			}
			out.flush();
		}
		out.close();
	}
}
