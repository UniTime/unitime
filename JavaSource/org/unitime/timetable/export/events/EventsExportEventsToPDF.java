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
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.EventFlag;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MultiMeetingInterface;

@Service("org.unitime.timetable.export.Exporter:events.pdf")
public class EventsExportEventsToPDF extends EventsExporter {
	
	@Override
	public String reference() {
		return "events.pdf";
	}

	@Override
	protected void print(ExportHelper helper, List<EventInterface> events, int eventCookieFlags, EventMeetingSortBy sort) throws IOException {
		sort(events, sort);
		Printer printer = new PDFPrinter(helper.getOutputStream(), true);
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
		case SHOW_PUBLISHED_TIME: out.hideColumn(6); break;
		case SHOW_ALLOCATED_TIME: out.hideColumn(7); break;
		case SHOW_SETUP_TIME: out.hideColumn(8); break;
		case SHOW_TEARDOWN_TIME: out.hideColumn(9); break;
		case SHOW_CAPACITY: out.hideColumn(11); break;
		case SHOW_ENROLLMENT: out.hideColumn(12); break;
		case SHOW_LIMIT: out.hideColumn(13); break;
		case SHOW_SPONSOR: out.hideColumn(14); break;
		case SHOW_MAIN_CONTACT: out.hideColumn(15); break;
		case SHOW_APPROVAL: out.hideColumn(16); break;
		}
	}

	protected void print(Printer out, List<EventInterface> events) throws IOException {
		out.printHeader(
				/*  0 */ MESSAGES.colName(),
				/*  1 */ MESSAGES.colSection(),
				/*  2 */ MESSAGES.colType(),
				/*  3 */ MESSAGES.colTitle(),
				/*  4 */ MESSAGES.colNote(),
				/*  5 */ MESSAGES.colDate(),
				/*  6 */ MESSAGES.colPublishedTime(),
				/*  7 */ MESSAGES.colAllocatedTime(),
				/*  8 */ MESSAGES.colSetupTimeShort(),
				/*  9 */ MESSAGES.colTeardownTimeShort(),
				/* 10 */ MESSAGES.colLocation(),
				/* 11 */ MESSAGES.colCapacity(),
				/* 12 */ MESSAGES.colEnrollment(),
				/* 13 */ MESSAGES.colLimit(),
				/* 14 */ MESSAGES.colSponsorOrInstructor(),
				/* 15 */ MESSAGES.colMainContact(),
				/* 16 */ MESSAGES.colApproval());
		
		DateFormat df = new SimpleDateFormat(CONSTANTS.eventDateFormat(), Localization.getJavaLocale());
		DateFormat dfShort = new SimpleDateFormat(CONSTANTS.eventDateFormatShort(), Localization.getJavaLocale());
		DateFormat dfLong = new SimpleDateFormat(CONSTANTS.eventDateFormatLong(), Localization.getJavaLocale());
		
		for (EventInterface event: events) {
			for (MultiMeetingInterface multi: EventInterface.getMultiMeetings(event.getMeetings(), false)) {
				MeetingInterface meeting = multi.getMeetings().first();
				out.printLine(
					getName(event),
					getSection(event),
					event.hasInstruction() ? event.getInstruction() : event.getType().getAbbreviation(CONSTANTS),
					getTitle(event),
					event.hasNotes() ? event.getNotes().first().getNote() : "",
					meeting.isArrangeHours() ? CONSTANTS.arrangeHours() : (multi.getDays(CONSTANTS) + " " + 
							(multi.getNrMeetings() == 1 ? multi.getFirstMeetingDate() == null ? "" : dfLong.format(multi.getFirstMeetingDate()) : dfShort.format(multi.getFirstMeetingDate()) + " - " + dfLong.format(multi.getLastMeetingDate()))),
					meeting.getMeetingTime(CONSTANTS),
					meeting.getAllocatedTime(CONSTANTS),
					String.valueOf(meeting.getStartOffset()),
					String.valueOf(-meeting.getEndOffset()),
					meeting.getLocationName(),
					meeting.hasLocation() && meeting.getLocation().hasSize() ? meeting.getLocation().getSize().toString() : null,
					event.hasEnrollment() ? event.getEnrollment().toString() : null,
					event.hasMaxCapacity() ? event.getMaxCapacity().toString() : null,
					event.hasInstructors() ? event.getInstructorNames("\n", MESSAGES) : event.hasSponsor() ? event.getSponsor().getName() : null,
					event.hasContact() ? event.getContact().getName(MESSAGES) : null,
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
