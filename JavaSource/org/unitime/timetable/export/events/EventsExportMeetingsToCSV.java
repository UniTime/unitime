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

/**
 * @author Tomas Muller
 */
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
		case SHOW_LAST_CHANGE: out.hideColumn(21); break;
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
				/* 20 */ MESSAGES.colApproval(),
				/* 21 */ MESSAGES.colLastChange());
		
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
					MESSAGES.approvalNotApproved(),
					event.getLastNote() != null ? df.format(event.getLastNote().getDate()) + " " + event.getLastNote().getType().getName() : null
					);
		}
		
		out.flush();
		out.close();
	}
}
