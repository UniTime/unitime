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
package org.unitime.timetable.export.events;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.gwt.client.events.EventComparator.EventMeetingSortBy;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApprovalStatus;
import org.unitime.timetable.gwt.shared.EventInterface.EventFlag;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MultiMeetingInterface;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
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
		case SHOW_LAST_CHANGE: out.hideColumn(23); break;
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
				/* 22 */ MESSAGES.colApproval(),
				/* 23 */ MESSAGES.colLastChange());
		
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EVENT);
		
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
					MESSAGES.approvalNotApproved(),
					event.getLastNote() != null ? df.format(event.getLastNote().getDate()) + " " + event.getLastNote().getType().getName() : null 
					);
			}
			out.flush();
		}
		out.close();
	}
}
