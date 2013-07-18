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
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.EventFlag;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.util.Formats;

@Service("org.unitime.timetable.export.Exporter:meetings.csv")
public class EventsExportMeetingsToCSV extends EventsExporter {
	
	@Override
	public String reference() {
		return "meetings.csv";
	}
	
	@Override
	protected void print(ExportHelper helper, List<EventInterface> events, int eventCookieFlags, EventMeetingSortBy sort, boolean asc) throws IOException {
		Printer printer = new CSVPrinter(helper.getWriter(), false);
		helper.setup(printer.getContentType(), reference(), false);
		hideColumns(printer, events, eventCookieFlags);
		print(printer, meetings(events, sort, asc));
	}

	
	@Override
	protected void hideColumn(Printer out, List<EventInterface> events, EventFlag flag) {
		switch (flag) {
		case SHOW_SECTION: out.hideColumn(1); break;
		case SHOW_TITLE: out.hideColumn(3); break;
		case SHOW_NOTE: out.hideColumn(4); break;
		case SHOW_PUBLISHED_TIME: out.hideColumn(6); out.hideColumn(7); break;
		case SHOW_ALLOCATED_TIME: out.hideColumn(8); out.hideColumn(9); break;
		case SHOW_SETUP_TIME: out.hideColumn(10); break;
		case SHOW_TEARDOWN_TIME: out.hideColumn(11); break;
		case SHOW_CAPACITY: out.hideColumn(13); break;
		case SHOW_ENROLLMENT: out.hideColumn(14); break;
		case SHOW_LIMIT: out.hideColumn(15); break;
		case SHOW_SPONSOR: out.hideColumn(16); out.hideColumn(17); break;
		case SHOW_MAIN_CONTACT: out.hideColumn(18); out.hideColumn(19); break;
		case SHOW_APPROVAL: out.hideColumn(20); break;
		}
	}
	
	protected void print(Printer out, Set<EventMeeting> meetings) throws IOException {
		out.printHeader(
				/*  0 */ MESSAGES.colName(),
				/*  1 */ MESSAGES.colSection(),
				/*  2 */ MESSAGES.colType(),
				/*  3 */ MESSAGES.colTitle(),
				/*  4 */ MESSAGES.colNote(),
				/*  5 */ MESSAGES.colDate(),
				/*  6 */ MESSAGES.colPublishedStartTime(),
				/*  7 */ MESSAGES.colPublishedEndTime(),
				/*  8 */ MESSAGES.colAllocatedStartTime(),
				/*  9 */ MESSAGES.colAllocatedEndTime(),
				/* 10 */ MESSAGES.colSetupTimeShort(),
				/* 11 */ MESSAGES.colTeardownTimeShort(),
				/* 12 */ MESSAGES.colLocation(),
				/* 13 */ MESSAGES.colCapacity(),
				/* 14 */ MESSAGES.colEnrollment(),
				/* 15 */ MESSAGES.colLimit(),
				/* 16 */ MESSAGES.colSponsorOrInstructor(),
				/* 17 */ MESSAGES.colEmail(),
				/* 18 */ MESSAGES.colMainContact(),
				/* 19 */ MESSAGES.colEmail(),
				/* 20 */ MESSAGES.colApproval());
		
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
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
					event.hasInstruction() ? event.getInstruction() : event.getType().getAbbreviation(CONSTANTS),
					getTitle(event),
					event.hasEventNote() ? event.getEventNote("\n").replace("<br>", "\n") : "",
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
					event.hasInstructors() ? event.getInstructorNames("\n", MESSAGES) : event.hasSponsor() ? event.getSponsor().getName() : null,
					event.hasInstructors() ? event.getInstructorEmails("\n") : event.hasSponsor() ? event.getSponsor().getEmail() : null,
					event.hasContact() ? event.getContact().getName(MESSAGES) : null,
					event.hasContact() ? event.getContact().getEmail() : null,
					event.getType() == EventType.Unavailabile ? "" :
					meeting.getApprovalStatus() == ApprovalStatus.Approved ? df.format(meeting.getApprovalDate()) :
					meeting.getApprovalStatus() == ApprovalStatus.Cancelled ? MESSAGES.approvalCancelled() :
					meeting.getApprovalStatus() == ApprovalStatus.Rejected ? MESSAGES.approvalRejected() :
					meeting.getApprovalStatus() == ApprovalStatus.Deleted ? MESSAGES.approvalDeleted() :
					meeting.isPast() ? MESSAGES.approvalNotApprovedPast() :
					event.getExpirationDate() != null ? MESSAGES.approvalExpire(df.format(event.getExpirationDate())) :
					MESSAGES.approvalNotApproved()
					);
		}
		
		out.flush();
		out.close();
	}
}
